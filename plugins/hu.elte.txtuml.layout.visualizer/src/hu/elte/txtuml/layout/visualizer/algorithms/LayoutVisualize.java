package hu.elte.txtuml.layout.visualizer.algorithms;

import hu.elte.txtuml.layout.visualizer.algorithms.boxes.ArrangeObjects;
import hu.elte.txtuml.layout.visualizer.algorithms.links.ArrangeAssociations;
import hu.elte.txtuml.layout.visualizer.annotations.Statement;
import hu.elte.txtuml.layout.visualizer.annotations.StatementType;
import hu.elte.txtuml.layout.visualizer.events.ProgressEmitter;
import hu.elte.txtuml.layout.visualizer.events.ProgressManager;
import hu.elte.txtuml.layout.visualizer.exceptions.BoxArrangeConflictException;
import hu.elte.txtuml.layout.visualizer.exceptions.BoxOverlapConflictException;
import hu.elte.txtuml.layout.visualizer.exceptions.CannotFindAssociationRouteException;
import hu.elte.txtuml.layout.visualizer.exceptions.ConversionException;
import hu.elte.txtuml.layout.visualizer.exceptions.InternalException;
import hu.elte.txtuml.layout.visualizer.exceptions.StatementTypeMatchException;
import hu.elte.txtuml.layout.visualizer.exceptions.StatementsConflictException;
import hu.elte.txtuml.layout.visualizer.exceptions.UnknownStatementException;
import hu.elte.txtuml.layout.visualizer.helpers.Helper;
import hu.elte.txtuml.layout.visualizer.helpers.Pair;
import hu.elte.txtuml.layout.visualizer.helpers.StatementHelper;
import hu.elte.txtuml.layout.visualizer.model.LineAssociation;
import hu.elte.txtuml.layout.visualizer.model.Point;
import hu.elte.txtuml.layout.visualizer.model.RectangleObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Observer;
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
	/**
	 * Whether to arrange overlapping elements or not.
	 */
	private Boolean _arrangeOverlaps;
	
	private Integer _corridorPercent;
	
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
	 * Returns the ProgressEmitter class.
	 * 
	 * @return the ProgressEmitter class.
	 */
	public ProgressEmitter getProgressEmitter()
	{
		return ProgressManager.getEmitter();
	}
	
	/**
	 * Adds an observer to the Event Emitter.
	 * 
	 * @param o
	 *            Observer to add.
	 */
	public void addObserver(Observer o)
	{
		ProgressManager.addObserver(o);
	}
	
	/**
	 * Setter for logging property.
	 * 
	 * @param value
	 *            Whether to enable or disable this feature.
	 */
	public void setLogging(Boolean value)
	{
		_logging = value;
	}
	
	/**
	 * Setter for arrange overlapping boxes property.
	 * 
	 * @param value
	 *            Whether to enable or disable this feature.
	 */
	public void setArrangeOverlaps(Boolean value)
	{
		_arrangeOverlaps = value;
	}
	
	/**
	 * Setter for batching of special links property.
	 * 
	 * @param value
	 *            Whether to enable or disable this feature.
	 */
	public void setBatching(Boolean value)
	{
		_batching = value;
	}
	
	/**
	 * Setter for the percent of the corridors' width relative to boxes' width.
	 * 
	 * @param percent
	 *            The amount of percent.
	 */
	public void setCorridor(Integer percent)
	{
		_corridorPercent = percent;
	}
	
	/**
	 * Layout algorithm initialize. Use load(), then arrange().
	 */
	public LayoutVisualize()
	{
		_diagramType = DiagramType.Class;
		setDefaults();
	}
	
	/**
	 * Layout algorithm initialize. Use load(), then arrange().
	 * 
	 * @param type
	 *            Type of the diagram to arrange.
	 */
	public LayoutVisualize(DiagramType type)
	{
		_diagramType = type;
		setDefaults();
	}
	
	private void setDefaults()
	{
		ProgressManager.start();
		
		_objects = null;
		_assocs = null;
		_batching = false;
		_arrangeOverlaps = true;
		_logging = false;
		_corridorPercent = 100;
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
	 *             other type.
	 * @throws BoxArrangeConflictException
	 *             Throws if there are some conflicts during the layout of
	 *             boxes.
	 * @throws StatementTypeMatchException
	 *             Throws if any of the {@link Statement}s are not in correct
	 *             format.
	 * @throws CannotFindAssociationRouteException
	 *             Throws if the algorithm cannot find the route for a
	 *             {@link LineAssociation}.
	 * @throws StatementsConflictException
	 *             Thorws if there are some conflicts in the {@link Statement}s
	 *             given by the user.
	 * @throws BoxOverlapConflictException
	 *             Throws if the algorithm encounters an unsolvable overlap
	 *             during the arrangement of the boxes.
	 * @throws InternalException
	 *             Throws if any error occurs which should not happen. Contact
	 *             developer for more details!
	 */
	public void arrange(ArrayList<Statement> par_stats) throws InternalException,
			BoxArrangeConflictException, ConversionException,
			StatementTypeMatchException, CannotFindAssociationRouteException,
			UnknownStatementException, BoxOverlapConflictException,
			StatementsConflictException
	{
		if (_objects == null)
			return;
		
		if (_logging)
			System.err.println("Starting arrange...");
		
		// Clone statements into local working copy
		_statements = Helper.cloneStatementList(par_stats);
		
		// Set next Group Id
		Integer maxGroupId = getGroupId();
		
		// Transform special associations into statements
		maxGroupId = transformAssocsIntoStatements(maxGroupId);
		
		// Split statements on assocs
		splitStatements();
		
		// Transform Phantom statements into Objects
		Set<String> phantoms = addPhantoms();
		
		// Set Default Statements
		maxGroupId = addDefaultStatements(maxGroupId);
		
		// Check the types of Statements
		StatementHelper.checkTypes(_statements, _assocStatements, _objects, _assocs);
		
		// Box arrange
		maxGroupId = boxArrange(maxGroupId);
		
		// Set start-end positions for associations
		updateAssocsEnd();
		
		// Remove phantom objects
		removePhantoms(phantoms);
		
		// Arrange associations between objects
		maxGroupId = linkArrange(maxGroupId);
		
		if (_logging)
			System.err.println("End of arrange!");
		
		ProgressManager.end();
	}
	
	private Integer getGroupId()
	{
		Optional<Integer> tempMax = _statements.stream()
				.filter(s -> s.getGroupId() != null).map(s -> s.getGroupId())
				.max((a, b) ->
				{
					return Integer.compare(a, b);
				});
		return tempMax.isPresent() ? tempMax.get() : 0;
	}
	
	private void splitStatements() throws ConversionException,
			StatementsConflictException
	{
		_assocStatements = StatementHelper.splitAssocs(_statements, _assocs);
		_statements.removeAll(_assocStatements);
		_assocStatements = StatementHelper.reduceAssocs(_assocStatements);
	}
	
	private Integer transformAssocsIntoStatements(Integer maxGroupId)
			throws InternalException
	{
		Pair<ArrayList<Statement>, Integer> tempPair = StatementHelper.transformAssocs(
				_assocs, maxGroupId);
		_statements.addAll(tempPair.First);
		return tempPair.Second;
	}
	
	private Set<String> addPhantoms()
	{
		Set<String> result = new HashSet<String>();
		result.addAll(StatementHelper.extractPhantoms(_statements));
		for (String p : result)
			_objects.add(new RectangleObject(p));
		_statements.removeAll(_statements.stream()
				.filter(s -> s.getType().equals(StatementType.phantom))
				.collect(Collectors.toSet()));
		
		return result;
	}
	
	private Integer addDefaultStatements(Integer maxGroupId) throws InternalException
	{
		DefaultStatements ds = new DefaultStatements(_objects, _assocs, _statements,
				maxGroupId);
		_statements.addAll(ds.value());
		return ds.getGroupId();
	}
	
	private Integer boxArrange(Integer maxGroupId) throws BoxArrangeConflictException,
			InternalException, ConversionException, BoxOverlapConflictException
	{
		if (_logging)
			System.err.println("> Starting box arrange...");
		
		// Arrange objects
		ArrangeObjects ao = new ArrangeObjects(_objects, _statements, maxGroupId,
				_logging, _arrangeOverlaps);
		_objects = new HashSet<RectangleObject>(ao.value());
		_statements = ao.statements();
		
		if (_logging)
			System.err.println("> Box arrange DONE!");
		
		return ao.getGId();
	}
	
	private void updateAssocsEnd()
	{
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
	}
	
	private void removePhantoms(Set<String> phantoms)
	{
		Set<RectangleObject> toDeleteSet = _objects.stream()
				.filter(o -> phantoms.contains(o.getName())).collect(Collectors.toSet());
		_objects.removeAll(toDeleteSet);
	}
	
	private Integer linkArrange(Integer maxGroupId) throws ConversionException,
			InternalException, CannotFindAssociationRouteException,
			UnknownStatementException
	{
		if (_assocs.size() <= 0)
			return maxGroupId;
		
		if (_logging)
			System.err.println("> Starting link arrange...");
		
		ArrangeAssociations aa = new ArrangeAssociations(_objects, _assocs,
				_assocStatements, maxGroupId, _corridorPercent, _batching, _logging);
		_assocs = aa.value();
		_objects = aa.objects();
		
		if (_logging)
			System.err.println("> Link arrange DONE!");
		
		return aa.getGId();
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
