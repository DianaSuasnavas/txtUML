package hu.elte.txtuml.layout.visualizer.algorithms;

import hu.elte.txtuml.layout.visualizer.annotations.Statement;
import hu.elte.txtuml.layout.visualizer.annotations.StatementType;
import hu.elte.txtuml.layout.visualizer.exceptions.CannotFindAssociationRouteException;
import hu.elte.txtuml.layout.visualizer.exceptions.CannotStartAssociationRouteException;
import hu.elte.txtuml.layout.visualizer.exceptions.ConversionException;
import hu.elte.txtuml.layout.visualizer.exceptions.InternalException;
import hu.elte.txtuml.layout.visualizer.helpers.Helper;
import hu.elte.txtuml.layout.visualizer.helpers.Pair;
import hu.elte.txtuml.layout.visualizer.model.Direction;
import hu.elte.txtuml.layout.visualizer.model.LineAssociation;
import hu.elte.txtuml.layout.visualizer.model.LineAssociation.RouteConfig;
import hu.elte.txtuml.layout.visualizer.model.Point;
import hu.elte.txtuml.layout.visualizer.model.RectangleObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

class ArrangeAssociations
{
	private Integer _widthOfObjects;
	private Integer _transformAmount;
	private ArrayList<LineAssociation> _assocs;
	private HashMap<Pair<String, RouteConfig>, Point> _modifies;
	private HashMap<Pair<String, RouteConfig>, HashSet<Point>> _possibleStarts;
	
	/**
	 * Gets the maximum amount which the diagram was enlarged during the arrange
	 * of the associations.
	 * 
	 * @return Integer of the amount.
	 */
	public Integer getTransformAmount()
	{
		return _transformAmount;
	}
	
	public Integer getWidthAmount()
	{
		return _widthOfObjects;
	}
	
	/**
	 * Arranges associations between objects, on the grid.
	 * 
	 * @param diagramObjects
	 *            Objects on the grid.
	 * @param diagramAssocs
	 *            Associations to arrange on the grid.
	 * @param stats
	 *            Statements on associations.
	 * @throws ConversionException
	 *             Throws if algorithm cannot convert certain StatementType into
	 *             Direction.
	 * @throws InternalException
	 *             Throws if any error occurs which should not happen. Contact
	 *             developer!
	 * @throws CannotFindAssociationRouteException
	 */
	public ArrangeAssociations(Set<RectangleObject> diagramObjects,
			Set<LineAssociation> diagramAssocs, ArrayList<Statement> stats)
			throws ConversionException, InternalException,
			CannotFindAssociationRouteException
	{
		if (diagramAssocs == null || diagramAssocs.size() == 0 || diagramAssocs == null
				|| diagramAssocs.size() == 0)
			return;
		
		_transformAmount = 2;
		_widthOfObjects = 1;
		OLDarrange(diagramObjects, diagramAssocs, stats);
	}
	
	public ArrangeAssociations(Set<RectangleObject> diagramObjects,
			Set<LineAssociation> diagramAssocs, ArrayList<Statement> stats, boolean trynew)
			throws ConversionException, InternalException,
			CannotFindAssociationRouteException
	{
		if (diagramAssocs == null || diagramAssocs.size() == 0 || diagramAssocs == null
				|| diagramAssocs.size() == 0)
			return;
		
		_transformAmount = 2;
		_widthOfObjects = 1;
		
		if (trynew)
			arrange(diagramObjects, diagramAssocs, stats);
		else
			OLDarrange(diagramObjects, diagramAssocs, stats);
	}
	
	private void arrange(Set<RectangleObject> diagramObjects,
			Set<LineAssociation> diagramAssocs, ArrayList<Statement> stats)
			throws ConversionException, InternalException
	{
		_assocs = (ArrayList<LineAssociation>) diagramAssocs.stream().collect(
				Collectors.toList());
		// _modifies = new HashMap<Pair<String, RouteConfig>, Point>();
		_possibleStarts = new HashMap<Pair<String, RouteConfig>, HashSet<Point>>();
		
		ArrayList<LineAssociation> originalAssocs = Helper.cloneLinkList(_assocs);
		
		// Transform Links' start and end route point
		transformAssocs();
		diagramObjects = updateObjects(diagramObjects);
		
		Boolean repeat = true;
		Boolean doLargeObjects = false;
		
		while (repeat)
		{
			// Process statements, priority and direction
			processStatements(stats, diagramObjects);
			Set<Point> occupiedLinks = new HashSet<Point>();
			repeat = false;
			
			try
			{
				// Search for the route of every Link
				for (int i = 0; i < _assocs.size(); ++i)
				{
					LineAssociation a = _assocs.get(i);
					Point START = a.getRoute(LineAssociation.RouteConfig.START);
					Point END = a.getRoute(LineAssociation.RouteConfig.END);
					Integer WIDTH = diagramObjects.stream().findFirst().get().getWidth();
					
					Set<Point> STARTSET = setStartSet(
							new Pair<String, RouteConfig>(a.getId(), RouteConfig.START),
							START, WIDTH, occupiedLinks);
					Set<Point> ENDSET = setEndSet(new Pair<String, RouteConfig>(
							a.getId(), RouteConfig.END), END, WIDTH, occupiedLinks);
					
					// Assemble occupied points
					Set<Point> OBJS = new HashSet<Point>();
					OBJS.addAll(occupiedLinks);
					// Add objects transformed place to occupied list
					Set<Point> occupied = new HashSet<Point>();
					for (RectangleObject obj : diagramObjects)
					{
						if (END.equals(obj.getPosition()))
							continue;
						
						for (Point p : obj.getPoints())
							occupied.add(p);
					}
					OBJS.addAll(occupied);
					
					// Maximum distance between objects
					Integer top = Math.max(10 * calcMaxDistance(occupied), 10);
					
					// Search for the route
					if (STARTSET.size() == 0 || ENDSET.size() == 0)
					{
						throw new CannotStartAssociationRouteException();
					}
					
					GraphSearch gs = new GraphSearch(START, STARTSET, END, ENDSET, OBJS,
							null, null, top);
					a.setRoute(gs.value());
					a.setTurns(gs.turns());
					a.setExtends(gs.extendsNum());
					
					_assocs.set(i, a);
					
					// Update occupied places with the route of this link
					if (a.getRoute().size() < 3)
						throw new InternalException("Route is shorter then 3!");
					
					for (int ri = 1; ri < a.getRoute().size() - 1; ++ri)
					{
						occupiedLinks.add(new Point(a.getRoute().get(ri)));
					}
				}
			}
			catch (CannotStartAssociationRouteException e)
			{
				System.out.println("Outer Catch Start!");
				repeat = true;
				// Grid * 2, ObjectWidth * 2 + 1
				_transformAmount = _transformAmount * 2;
				diagramObjects = enlargeObjects(diagramObjects);
				_assocs = Helper.cloneLinkList(originalAssocs);
				transformAssocs();
			}
			catch (CannotFindAssociationRouteException e)
			{
				System.out.println("Outer Catch Find!");
				repeat = true;
				_transformAmount = _transformAmount * 2;
				if (doLargeObjects)
				{
					diagramObjects = enlargeObjects(diagramObjects);
				}
				_assocs = Helper.cloneLinkList(originalAssocs);
				diagramObjects = updateObjects(diagramObjects);
				transformAssocs();
				doLargeObjects = !doLargeObjects;
			}
		}
		
	}
	
	private void transformAssocs()
	{
		for (int i = 0; i < _assocs.size(); ++i)
		{
			LineAssociation mod = _assocs.get(i);
			ArrayList<Point> temp = mod.getRoute();
			ArrayList<Point> route = new ArrayList<Point>();
			for (Point p : temp)
			{
				route.add(Point.Multiply(p, _transformAmount));
			}
			mod.setRoute(route);
			_assocs.set(i, mod);
		}
	}
	
	private Set<RectangleObject> updateObjects(Set<RectangleObject> objs)
	{
		Set<RectangleObject> result = new HashSet<RectangleObject>();
		
		for (RectangleObject o : objs)
		{
			RectangleObject temp = new RectangleObject(o);
			temp.setPosition(transformDimension(o.getPosition()));
			result.add(temp);
		}
		
		return result;
	}
	
	private void processStatements(ArrayList<Statement> stats, Set<RectangleObject> objs)
			throws ConversionException, InternalException
	{
		if (stats != null && stats.size() != 0)
		{
			// Set priority
			HashMap<String, Integer> priorityMap = setPriorityMap(stats);
			
			// Order based on priority
			_assocs.sort((a1, a2) ->
			{
				if (priorityMap.containsKey(a1.getId()))
				{
					if (priorityMap.containsKey(a2.getId()))
					{
						Integer v = priorityMap.get(a1.getId());
						Integer w = priorityMap.get(a2.getId());
						return (v - w > 0) ? 1 : -1;
					}
					else
						return -1;
				}
				else
				{
					if (priorityMap.containsKey(a2.getId()))
						return 1;
					else
						return 0;
				}
			});
			
			ArrayList<Statement> priorityless = new ArrayList<Statement>(stats);
			priorityless.removeIf(s -> s.getType().equals(StatementType.priority));
			
			// Set starts/ends for statemented assocs
			setPossibles(priorityless, objs);
		}
	}
	
	private Set<Point> setStartSet(Pair<String, RouteConfig> key, Point start,
			Integer width, Set<Point> occupied)
	{
		Set<Point> statementPoints = _possibleStarts.get(key);
		if (statementPoints != null)
		{
			statementPoints.removeIf(p -> occupied.contains(p));
			return statementPoints;
		}
		
		Set<Point> result = new HashSet<Point>();
		
		if (width == 1)
		{
			result.add(Point.Add(start, Direction.north));
			result.add(Point.Add(start, Direction.east));
			result.add(Point.Add(start, Direction.south));
			result.add(Point.Add(start, Direction.west));
			
			result.removeIf(p -> occupied.contains(p));
			
			return result;
		}
		
		// Add Object's points
		RectangleObject tempObj = new RectangleObject("TEMP", start);
		tempObj.setWidth(width);
		result.addAll(tempObj.getPoints());
		
		// Remove inner points, so only the outer rim remains.
		tempObj.setWidth(width - 1);
		result.removeAll(tempObj.getPoints());
		
		// Remove occupied points
		result.removeIf(p -> occupied.contains(p));
		
		return result;
	}
	
	private Set<Point> setEndSet(Pair<String, RouteConfig> key, Point end, Integer width,
			Set<Point> occupied)
	{
		Set<Point> statementPoints = _possibleStarts.get(key);
		if (statementPoints != null)
		{
			statementPoints.removeIf(p -> occupied.contains(p));
			return statementPoints;
		}
		
		Set<Point> result = new HashSet<Point>();
		
		RectangleObject temp = new RectangleObject("TEMP", end);
		temp.setWidth(width);
		result.addAll(temp.getPoints());
		result.removeIf(p -> occupied.contains(p));
		
		return result;
	}
	
	private Set<RectangleObject> enlargeObjects(Set<RectangleObject> objs)
	{
		Set<RectangleObject> result = new HashSet<RectangleObject>();
		
		for (RectangleObject o : objs)
		{
			RectangleObject temp = new RectangleObject(o);
			_widthOfObjects = o.getWidth() + 2;
			temp.setPosition(Point.Multiply(o.getPosition(), 2));
			temp.setWidth(_widthOfObjects);
			result.add(temp);
		}
		
		return result;
	}
	
	private void OLDarrange(Set<RectangleObject> diagramObjects,
			Set<LineAssociation> diagramAssocs, ArrayList<Statement> stats)
			throws ConversionException, InternalException,
			CannotFindAssociationRouteException
	{
		Set<Point> occupiedLinks = new HashSet<Point>();
		
		_assocs = (ArrayList<LineAssociation>) diagramAssocs.stream().collect(
				Collectors.toList());
		_modifies = new HashMap<Pair<String, RouteConfig>, Point>();
		// Transform everything to new dimensions (*2)
		// Transform Links' start and end route point
		for (int i = 0; i < _assocs.size(); ++i)
		{
			LineAssociation mod = _assocs.get(i);
			ArrayList<Point> temp = mod.getRoute();
			ArrayList<Point> route = new ArrayList<Point>();
			for (Point p : temp)
			{
				route.add(transformDimension(p));
			}
			mod.setRoute(route);
			_assocs.set(i, mod);
		}
		
		// Process statements, priority and direction
		if (stats != null && stats.size() != 0)
		{
			// Set priority
			HashMap<String, Integer> priorityMap = setPriorityMap(stats);
			
			// Order based on priority
			_assocs.sort((a1, a2) ->
			{
				if (priorityMap.containsKey(a1.getId()))
				{
					if (priorityMap.containsKey(a2.getId()))
					{
						Integer v = priorityMap.get(a1.getId());
						Integer w = priorityMap.get(a2.getId());
						return (v - w > 0) ? 1 : -1;
					}
					else
						return -1;
				}
				else
				{
					if (priorityMap.containsKey(a2.getId()))
						return 1;
					else
						return 0;
				}
			});
			
			// Set starts/ends for statemented assocs
			setModified(stats, diagramObjects);
		}
		
		// Search for the route of every Link
		for (int i = 0; i < _assocs.size(); ++i)
		{
			LineAssociation a = _assocs.get(i);
			Point START = a.getRoute(LineAssociation.RouteConfig.START);
			Point END = a.getRoute(LineAssociation.RouteConfig.END);
			Point BEFORE = _modifies.get(new Pair<String, RouteConfig>(a.getId(),
					RouteConfig.START));
			Point AFTER = _modifies.get(new Pair<String, RouteConfig>(a.getId(),
					RouteConfig.END));
			Set<Point> ENDSET = new HashSet<Point>();
			
			// Assemble occupied points
			Set<Point> OBJS = new HashSet<Point>();
			OBJS.addAll(occupiedLinks);
			// Add objects transformed place to occupied list
			Set<Point> occupied = new HashSet<Point>();
			for (RectangleObject obj : diagramObjects)
			{
				if (obj.getName().equals(a.getTo()))
					continue;
				
				for (Point p : obj.getPoints())
					occupied.add(transformDimension(p));
			}
			OBJS.addAll(occupied);
			// Maximum distance between objects
			Integer top = 2 * calcMaxDistance(OBJS);
			
			// Search for the route
			GraphSearch gs;
			try
			{
				gs = new GraphSearch(START, null, END, ENDSET, OBJS, BEFORE, AFTER, top);
				a.setRoute(gs.value());
				a.setTurns(gs.turns());
				a.setExtends(gs.extendsNum());
			}
			catch (CannotStartAssociationRouteException e)
			{
				throw new CannotFindAssociationRouteException(e.getMessage());
			}
			
			_assocs.set(i, a);
			
			// Update occupied places with the route of this link
			if (a.getRoute().size() < 3)
				throw new InternalException("Route is shorter then 3!");
			
			for (int ri = 1; ri < a.getRoute().size() - 1; ++ri)
			{
				occupiedLinks.add(new Point(a.getRoute().get(ri)));
			}
		}
	}
	
	private Integer calcMaxDistance(Set<Point> grid)
	{
		Integer maxval = 0;
		
		for (Point p1 : grid)
		{
			for (Point p2 : grid)
			{
				int dx = Math.abs(p1.getX() - p2.getX());
				int dy = Math.abs(p1.getY() - p2.getY());
				if (dx > maxval)
					maxval = dx;
				if (dy > maxval)
					maxval = dy;
			}
		}
		
		return maxval;
	}
	
	private HashMap<String, Integer> setPriorityMap(ArrayList<Statement> stats)
	{
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		
		for (Statement s : stats)
		{
			if (s.getType().equals(StatementType.priority))
			{
				result.put(s.getParameter(0), Integer.parseInt(s.getParameter(1)));
			}
		}
		
		return result;
	}
	
	private void setPossibles(ArrayList<Statement> stats, Set<RectangleObject> objs)
			throws ConversionException, InternalException
	{
		for (Statement s : stats)
		{
			try
			{
				LineAssociation link = _assocs.stream()
						.filter(a -> a.getId().equals(s.getParameter(0))).findFirst()
						.get();
				RectangleObject obj = objs.stream()
						.filter(o -> o.getName().equals(s.getParameter(1))).findFirst()
						.get();
				if (link.getFrom().equals(obj.getName()))
				{
					// RouteConfig.START
					if (obj.getWidth() == 1)
					{
						Pair<String, RouteConfig> tempKey = new Pair<String, RouteConfig>(
								link.getId(), RouteConfig.START);
						HashSet<Point> tempSet = new HashSet<Point>();
						tempSet.add(Point.Add(obj.getPosition(),
								Helper.asDirection(s.getType())));
						_possibleStarts.put(tempKey, tempSet);
					}
					else
					{
						Point startPoint = getStartingPoint(
								Helper.asDirection(s.getType()), obj);
						Direction moveDir = getMoveDirection(s.getType());
						generatePossiblePoints(link, obj, startPoint, moveDir,
								RouteConfig.START);
					}
				}
				else
				{
					// RouteConfig.END
					if (obj.getWidth() == 1)
					{
						Pair<String, RouteConfig> tempKey = new Pair<String, RouteConfig>(
								link.getId(), RouteConfig.END);
						HashSet<Point> tempSet = new HashSet<Point>();
						tempSet.add(Point.Add(obj.getPosition(),
								Helper.asDirection(s.getType())));
						_possibleStarts.put(tempKey, tempSet);
					}
					else
					{
						Point startPoint = getStartingPoint(
								Helper.asDirection(s.getType()), obj);
						Direction moveDir = getMoveDirection(s.getType());
						generatePossiblePoints(link, obj, startPoint, moveDir,
								RouteConfig.END);
					}
				}
			}
			catch (NoSuchElementException e)
			{
				throw new InternalException("Inner Exception: [" + e.getMessage() + "], "
						+ "Probably a statment shouldn't have reached this code!");
			}
		}
	}
	
	private Point getStartingPoint(Direction dir, RectangleObject o)
			throws InternalException
	{
		if (dir.equals(Direction.north) || dir.equals(Direction.west))
			return o.getTopLeft();
		else if (dir.equals(Direction.south) || dir.equals(Direction.east))
			return o.getBottomRight();
		else
			throw new InternalException("Unknown Direction!");
	}
	
	private Direction getMoveDirection(StatementType ty) throws InternalException
	{
		switch (ty)
		{
			case north:
				return Direction.east;
			case west:
				return Direction.south;
			case south:
				return Direction.west;
			case east:
				return Direction.north;
			default:
				throw new InternalException("Cannot evaluate MoveDirection for "
						+ ty.toString() + "!");
		}
	}
	
	private void setModified(ArrayList<Statement> stats, Set<RectangleObject> objs)
			throws ConversionException
	{
		for (Statement s : stats)
		{
			if (s.getType().equals(StatementType.north))
			{
				modifyIfInList(s, Helper.asDirection(StatementType.north), objs);
			}
			else if (s.getType().equals(StatementType.south))
			{
				modifyIfInList(s, Helper.asDirection(StatementType.south), objs);
			}
			else if (s.getType().equals(StatementType.east))
			{
				modifyIfInList(s, Helper.asDirection(StatementType.east), objs);
			}
			else if (s.getType().equals(StatementType.west))
			{
				modifyIfInList(s, Helper.asDirection(StatementType.west), objs);
			}
		}
	}
	
	private void generatePossiblePoints(LineAssociation toModify,
			RectangleObject connectsTo, Point first, Direction toMove, RouteConfig r)
	{
		HashSet<Point> points = new HashSet<Point>();
		
		for (int i = 0; i < connectsTo.getWidth(); ++i)
		{
			points.add(Point.Add(first, Point.Multiply(toMove, i)));
		}
		Pair<String, RouteConfig> key = new Pair<String, LineAssociation.RouteConfig>(
				toModify.getId(), r);
		
		_possibleStarts.put(key, points);
	}
	
	private void modifyIfInList(Statement stat, Direction dir, Set<RectangleObject> objs)
	{
		
		String nameOfAssoc = stat.getParameter(0);
		// Search 'nameOfAssoc' in '_assocs'
		LineAssociation toModify = _assocs.stream()
				.filter(a -> a.getId().equals(nameOfAssoc)).collect(Collectors.toList())
				.get(0);
		Integer indexOfAssoc = _assocs.indexOf(toModify);
		
		String nameOfObject = stat.getParameter(1);
		// Search 'nameOfObject' in 'objs'
		RectangleObject ob = objs.stream().filter(o -> o.getName().equals(nameOfObject))
				.collect(Collectors.toList()).get(0);
		
		ArrayList<Point> route = toModify.getRoute();
		if (toModify.getRoute(RouteConfig.START).equals(
				transformDimension(ob.getPosition())))
		{
			route.set(0, Point.Add(transformDimension(ob.getPosition()), dir));
			_modifies.put(new Pair<String, LineAssociation.RouteConfig>(toModify.getId(),
					RouteConfig.START), transformDimension(ob.getPosition()));
		}
		if (toModify.getRoute(RouteConfig.END).equals(
				transformDimension(ob.getPosition())))
		{
			route.set(1, Point.Add(transformDimension(ob.getPosition()), dir));
			_modifies.put(new Pair<String, LineAssociation.RouteConfig>(toModify.getId(),
					RouteConfig.END), transformDimension(ob.getPosition()));
		}
		toModify.setRoute(route);
		
		// Set in List
		_assocs.set(indexOfAssoc, toModify);
	}
	
	private Point transformDimension(Point p)
	{
		return Point.Multiply(p, _transformAmount);
	}
	
	public Set<LineAssociation> value()
	{
		return (Set<LineAssociation>) _assocs.stream().collect(Collectors.toSet());
	}
	
}
