package hu.elte.txtuml.layout.visualizer.algorithms;

import hu.elte.txtuml.layout.visualizer.algorithms.boxes.ArrangeObjects;
import hu.elte.txtuml.layout.visualizer.algorithms.links.ArrangeAssociations;
import hu.elte.txtuml.layout.visualizer.annotations.Statement;
import hu.elte.txtuml.layout.visualizer.annotations.StatementType;
import hu.elte.txtuml.layout.visualizer.exceptions.CannotFindAssociationRouteException;
import hu.elte.txtuml.layout.visualizer.exceptions.ConflictException;
import hu.elte.txtuml.layout.visualizer.exceptions.ConversionException;
import hu.elte.txtuml.layout.visualizer.exceptions.InternalException;
import hu.elte.txtuml.layout.visualizer.exceptions.StatementTypeMatchException;
import hu.elte.txtuml.layout.visualizer.exceptions.UnknownStatementException;
import hu.elte.txtuml.layout.visualizer.helpers.Helper;
import hu.elte.txtuml.layout.visualizer.helpers.Pair;
import hu.elte.txtuml.layout.visualizer.helpers.StatementHelper;
import hu.elte.txtuml.layout.visualizer.model.LineAssociation;
import hu.elte.txtuml.layout.visualizer.model.Point;
import hu.elte.txtuml.layout.visualizer.model.RectangleObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is used to wrap the arrange of a whole diagram.
 * 
 * @author Bal�zs Gregorics
 */
public class LayoutVisualize
{
	/***
	 * Objects to arrange.
	 */
	private Set<RectangleObject> _objects;
	/***
	 * Links to arrange
	 */
	private Set<LineAssociation> _assocs;
	
	/***
	 * Statements which arrange.
	 * Later the statements on the objects.
	 */
	private ArrayList<Statement> _statements;
	/***
	 * Statements on links.
	 */
	private ArrayList<Statement> _assocStatements;
	
	/**
	 * The type of the diagram to arrange.
	 */
	@SuppressWarnings("unused")
	private DiagramType _diagramType;
	/**
	 * Whether to allow batching of links during link arrange or not.
	 */
	private Boolean _batching;
	/**
	 * Whether to print log messages.
	 */
	private Boolean _logging;
	
	/***
	 * Get the current set of Objects.
	 * 
	 * @return Set of Objects. Can be null.
	 */
	public Set<RectangleObject> getObjects()
	{
		return _objects;
	}
	
	/***
	 * Get the current set of Links.
	 * 
	 * @return Set of Links. Can be null.
	 */
	public Set<LineAssociation> getAssocs()
	{
		return _assocs;
	}
	
	/**
	 * Get the finishing set of Statements on objects.
	 * 
	 * @return List of Statements on objects.
	 */
	public ArrayList<Statement> getStatements()
	{
		return _statements;
	}
	
	/**
	 * Get the finishing set of Statements on links.
	 * 
	 * @return List of Statements on links.
	 */
	public ArrayList<Statement> getAssocStatements()
	{
		return _assocStatements;
	}
	
	/**
	 * Layout algorithm initialize. Use load(), then arrange().
	 */
	public LayoutVisualize()
	{
		_objects = null;
		_assocs = null;
		_diagramType = DiagramType.Class;
		_batching = false;
		_logging = false;
	}
	
	/**
	 * Layout algorithm initialize. Use load(), then arrange().
	 * 
	 * @param isLog
	 *            Whether to print out logging messages.
	 */
	public LayoutVisualize(boolean isLog)
	{
		_objects = null;
		_assocs = null;
		_diagramType = DiagramType.Class;
		_batching = false;
		_logging = isLog;
	}
	
	/**
	 * Layout algorithm initialize for setting the reflexive links arrange. Use
	 * load(), then arrange().
	 * 
	 * @param type
	 *            The type of the diagram to arrange.
	 * @param batch
	 *            Whether to use batching during link arrange or not.
	 */
	public LayoutVisualize(DiagramType type, Boolean batch)
	{
		_objects = null;
		_assocs = null;
		_diagramType = type;
		_batching = batch;
		_logging = true;
	}
	
	/**
	 * Layout algorithm initialize for setting the reflexive links arrange. Use
	 * load(), then arrange().
	 * 
	 * @param isLog
	 *            Whether to print out logging messages.
	 * @param type
	 *            The type of the diagram to arrange.
	 * @param batch
	 *            Whether to use batching during link arrange or not.
	 */
	public LayoutVisualize(boolean isLog, DiagramType type, Boolean batch)
	{
		_objects = null;
		_assocs = null;
		_diagramType = type;
		_batching = batch;
		_logging = isLog;
	}
	
	/**
	 * Arranges the previously loaded model with the given {@link Statement}s.
	 * 
	 * @param par_stats
	 *            List of {@link Statement}s.
	 * @throws UnknownStatementException
	 *             Throws if an unknown {@link Statement} is provided.
	 * @throws ConversionException
	 *             Throws if algorithm cannot convert certain type into
	 *             other type (Missing Statement upgrade?).
	 * @throws ConflictException
	 *             Throws if there are some conflicts in the user statements.
	 * @throws StatementTypeMatchException
	 *             Throws if any of the statements are not in correct format.
	 * @throws InternalException
	 *             Throws if any error occurs which should not happen. Contact
	 *             developer for more details!
	 * @throws CannotFindAssociationRouteException
	 *             Throws if the algorithm cannot find the route for a
	 *             association.
	 */
	public void arrange(ArrayList<Statement> par_stats) throws InternalException,
			ConflictException, ConversionException, StatementTypeMatchException,
			CannotFindAssociationRouteException, UnknownStatementException
	{
		if (_objects == null)
			return;
		
		if (_logging)
			System.err.println("Starting arrange...");
		
		_statements = Helper.cloneStatementList(par_stats);
		
		// Set next Group Id
		Optional<Integer> tempMax = _statements.stream()
				.filter(s -> s.getGroupId() != null).map(s -> s.getGroupId())
				.max((a, b) ->
				{
					return Integer.compare(a, b);
				});
		Integer maxGroupId = tempMax.isPresent() ? tempMax.get() : 0;
		
		// Split statements on assocs
		_assocStatements = StatementHelper.splitAssocs(_statements, _assocs);
		_statements.removeAll(_assocStatements);
		_assocStatements = StatementHelper.reduceAssocs(_assocStatements);
		
		// Transform special associations into statements
		Pair<ArrayList<Statement>, Integer> tempPair = StatementHelper.transformAssocs(
				_assocs, maxGroupId);
		_statements.addAll(tempPair.First);
		maxGroupId = tempPair.Second;
		
		// Transform Phantom statements into Objects
		Set<String> phantoms = new HashSet<String>();
		phantoms.addAll(StatementHelper.extractPhantoms(_statements));
		for (String p : phantoms)
			_objects.add(new RectangleObject(p));
		_statements.removeAll(_statements.stream()
				.filter(s -> s.getType().equals(StatementType.phantom))
				.collect(Collectors.toSet()));
		
		// Set Default Statements
		DefaultStatements ds = new DefaultStatements(_objects, _assocs, _statements,
				maxGroupId);
		_statements.addAll(ds.value());
		maxGroupId = ds.getGroupId();
		
		StatementHelper.checkTypes(_statements, _assocStatements, _objects, _assocs);
		
		if (_logging)
			System.err.println("> Starting box arrange...");
		
		// Arrange objects
		ArrangeObjects ao = new ArrangeObjects(_objects, _statements, maxGroupId,
				_logging);
		_objects = new HashSet<RectangleObject>(ao.value());
		_statements = ao.statements();
		maxGroupId = ao.getGId();
		
		if (_logging)
			System.err.println("> Box arrange DONE!");
		
		// Set start-end positions for associations
		for (LineAssociation a : _assocs)
		{
			ArrayList<Point> al = new ArrayList<Point>();
			Point startend = null;
			Point endend = null;
			int ends = 2;
			for (RectangleObject o : _objects)
			{
				if (a.getFrom().equals(o.getName()))
				{
					startend = o.getPosition();
					--ends;
				}
				if (a.getTo().equals(o.getName()))
				{
					endend = o.getPosition();
					--ends;
				}
				
				if (ends == 0)
					break;
			}
			al.add(startend);
			al.add(endend);
			a.setRoute(al);
		}
		
		// Remove phantom objects
		Set<RectangleObject> toDeleteSet = _objects.stream()
				.filter(o -> phantoms.contains(o.getName())).collect(Collectors.toSet());
		_objects.removeAll(toDeleteSet);
		
		// Arrange associations between objects
		if (_assocs.size() <= 0)
			return;
		
		if (_logging)
			System.err.println("> Starting link arrange...");
		
		ArrangeAssociations aa = new ArrangeAssociations(_objects, _assocs,
				_assocStatements, maxGroupId, _batching, _logging);
		_assocs = aa.value();
		_objects = aa.objects();
		
		if (_logging)
			System.err.println("> Link arrange DONE!");
	}
	
	/***
	 * This function is used to load data to arrange.
	 * 
	 * @param os
	 *            Set of RectangleObjects to arrange on a grid.
	 * @param as
	 *            Set of LineAssociations to arrange between objects.
	 */
	public void load(Set<RectangleObject> os, Set<LineAssociation> as)
	{
		_objects = os;
		_assocs = as;
	}
	
}
