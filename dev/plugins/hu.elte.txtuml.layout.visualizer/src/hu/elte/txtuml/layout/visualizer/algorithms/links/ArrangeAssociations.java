package hu.elte.txtuml.layout.visualizer.algorithms.links;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import hu.elte.txtuml.layout.visualizer.algorithms.links.utils.DefaultAssocStatements;
import hu.elte.txtuml.layout.visualizer.algorithms.links.utils.GraphSearch;
import hu.elte.txtuml.layout.visualizer.algorithms.links.utils.graphsearchutils.Boundary;
import hu.elte.txtuml.layout.visualizer.algorithms.links.utils.graphsearchutils.Color;
import hu.elte.txtuml.layout.visualizer.algorithms.links.utils.graphsearchutils.Node;
import hu.elte.txtuml.layout.visualizer.algorithms.utils.Helper;
import hu.elte.txtuml.layout.visualizer.model.utils.RectangleObjectTreeEnumerator;
import hu.elte.txtuml.layout.visualizer.events.ProgressManager;
import hu.elte.txtuml.layout.visualizer.exceptions.CannotFindAssociationRouteException;
import hu.elte.txtuml.layout.visualizer.exceptions.CannotStartAssociationRouteException;
import hu.elte.txtuml.layout.visualizer.exceptions.ConversionException;
import hu.elte.txtuml.layout.visualizer.exceptions.InternalException;
import hu.elte.txtuml.layout.visualizer.exceptions.UnknownStatementException;
import hu.elte.txtuml.layout.visualizer.model.Direction;
import hu.elte.txtuml.layout.visualizer.model.LineAssociation;
import hu.elte.txtuml.layout.visualizer.model.Options;
import hu.elte.txtuml.layout.visualizer.model.LineAssociation.RouteConfig;
import hu.elte.txtuml.layout.visualizer.model.Point;
import hu.elte.txtuml.layout.visualizer.model.RectangleObject;
import hu.elte.txtuml.layout.visualizer.model.SpecialBox;
import hu.elte.txtuml.layout.visualizer.statements.Statement;
import hu.elte.txtuml.layout.visualizer.statements.StatementType;
import hu.elte.txtuml.utils.Logger;
import hu.elte.txtuml.utils.Pair;

/**
 * This class arranges the lines of links.
 */
public class ArrangeAssociations {
	private final Integer MINIMAL_CORRIDOR_SIZE = 1;
	private final Integer MAXIMUM_TRY_COUNT = 100;

	private Integer _widthOfCells;
	private Integer _heightOfCells;

	private Set<RectangleObject> _objects;
	private HashMap<String, Point> _cellPositions;
	private List<LineAssociation> _assocs;
	private List<Statement> _statements;
	private HashMap<Pair<String, RouteConfig>, HashSet<Point>> _possibleStarts;

	private Integer _gId;
	private Options _options;

	/**
	 * Gets the final width of the boxes, that was computed during the running
	 * of the ArrangeAssociation algorithm
	 * 
	 * @return Integer number of the width of boxes.
	 */
	public Integer getWidthAmount() {
		return _widthOfCells;
	}

	/**
	 * Gets the final height of the boxes, that was computed during the running
	 * of the ArrangeAssociation algorithm
	 * 
	 * @return Integer number of the height of boxes.
	 */
	public Integer getHeightAmount() {
		return _heightOfCells;
	}

	/**
	 * Returns the last use Group Id number.
	 * 
	 * @return the last use Group Id number.
	 */
	public Integer getGId() {
		return _gId;
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
	 * @param gid
	 *            The max group id yet existing.
	 * @param opt
	 *            Options of the algorithm.
	 * @throws ConversionException
	 *             Throws if algorithm cannot convert certain StatementType into
	 *             Direction.
	 * @throws InternalException
	 *             Throws if any error occurs which should not happen. Contact
	 *             developer!
	 * @throws CannotFindAssociationRouteException
	 *             Throws if a certain link's path cannot be found.
	 * @throws UnknownStatementException
	 *             Throws if some unknown statements are found during
	 *             processing.
	 */
	public ArrangeAssociations(Set<RectangleObject> diagramObjects, Set<LineAssociation> diagramAssocs,
			List<Statement> stats, Integer gid, Options opt) throws ConversionException, InternalException,
					CannotFindAssociationRouteException, UnknownStatementException {
		// Nothing to arrange
		if (diagramAssocs == null)
			return;

		// Setup variables and default values
		_gId = gid;
		_options = opt;
		_cellPositions = new HashMap<String, Point>();

		_widthOfCells = 1;
		_heightOfCells = 1;
		
		_objects = new HashSet<>(diagramObjects);
		_assocs = diagramAssocs.stream().collect(Collectors.toList());
		_statements = new ArrayList<>(stats);

		// Emit Event
		ProgressManager.getEmitter().OnLinkArrangeStart();

		// Arrange the links
		arrange();

		// Emit Event
		ProgressManager.getEmitter().OnLinkArrangeEnd();
	}

	private void arrange() throws ConversionException, InternalException {
		_possibleStarts = new HashMap<Pair<String, RouteConfig>, HashSet<Point>>();

		// Inflate diagram to start with a object width enough for the
		// maximum number of links
		defaultGrid();
		
		if(isPresentMultiLevelLinks())
		{
			//TODO
		}

		// Get default statements on links
		DefaultAssocStatements das = new DefaultAssocStatements(_gId, _statements, _assocs);
		_statements = das.value();
		_gId = das.getGroupId();

		Boolean needRepeat = true;

		// Arrange until (needs a repeat or reached maximum try count)
		Integer tryCount = 0;
		while (needRepeat && tryCount <= MAXIMUM_TRY_COUNT) {
			++tryCount;

			// Process statements, priority and direction
			processStatements();
			needRepeat = false;

			try {
				// Maximum distance between objects
				Boundary bounds = calculateBoundary();
				bounds.addError(20, _widthOfCells);

				// Add objects transformed place to occupied list
				Map<Point, Color> occupied = new HashMap<Point, Color>();
				for (RectangleObject obj : _objects) {
					for (Point p : obj.getPoints()) {
						occupied.put(p, Color.Red);
					}
				}

				// Search for the route of every Link
				arrangeLinks(occupied, bounds);
			} catch (CannotStartAssociationRouteException | CannotFindAssociationRouteException e) {
				if (_options.Logging)
					Logger.sys.info("(Normal) Expanding grid!");

				needRepeat = true;
				// Grid * 2
				expandGrid();
				continue;
			}
		}
	}

	private Integer calculateMaxLinks() {
		if (_assocs.size() == 0)
			return 0;

		// Gather data
		HashMap<String, Integer> data = new HashMap<String, Integer>();

		Integer countMod = 1;
		for (LineAssociation a : _assocs) {
			// From
			if (data.containsKey(a.getFrom())) {
				data.put(a.getFrom(), data.get(a.getFrom()) + countMod);
			} else {
				data.put(a.getFrom(), countMod);
			}

			if (a.isReflexive())
				continue;
			// To
			if (data.containsKey(a.getTo())) {
				data.put(a.getTo(), data.get(a.getTo()) + countMod);
			} else {
				data.put(a.getTo(), countMod);
			}
		}

		// Find max
		Integer max = data.entrySet().stream().max((e1, e2) -> Integer.compare(e1.getValue(), e2.getValue())).get()
				.getValue();

		return max;
	}

	private void defaultGrid() {
		Integer k = calculateMaxLinks();
		
		_widthOfCells = k;
		_heightOfCells = k;

		// Get the smallest of boxes to compute the grid dimensions
		Pair<Double, Double> pixelPair = getPixelPerGrid(k);

		// Set the grid sizes of boxes based on their pixel sizes
		Set<RectangleObject> result = setBoxSizes(pixelPair);

		// Set positions according to width/height of the cells
		_objects = setBoxCells(result);

		// Update the links' positions
		updateLinks();

		if (_options.Logging)
			Logger.sys.info("(Default) Expanding Grid!");
	}
	
	private Pair<Double, Double> getPixelPerGrid(Integer maxLinkNumber)
	{
		Integer smallestPixelWidth = _objects.stream().filter(box -> !box.getSpecial().equals(SpecialBox.Initial))
				.min((o1, o2) -> {
					return Integer.compare(o1.getPixelWidth(), o2.getPixelWidth());
				}).get().getPixelWidth();
		Integer smallestPixelHeight = _objects.stream().filter(box -> !box.getSpecial().equals(SpecialBox.Initial))
				.min((o1, o2) -> {
					return Integer.compare(o1.getPixelHeight(), o2.getPixelHeight());
				}).get().getPixelHeight();
		
		return Pair.of(Math.floor(smallestPixelWidth / (maxLinkNumber + 2.0)),
				Math.floor(smallestPixelHeight / (maxLinkNumber + 2.0)));
	}

	private Set<RectangleObject> setBoxSizes(Pair<Double, Double> pixelPair)
	{
		Set<RectangleObject> result = new HashSet<RectangleObject>();
		
		Double pixelPerGridWidth = pixelPair.getFirst();
		Double pixelPerGridHeight = pixelPair.getSecond();
		
		for (RectangleObject obj : _objects) {
			RectangleObject mod = new RectangleObject(obj);

			if (mod.getSpecial().equals(SpecialBox.Initial)) {
				mod.setWidth(3);
				mod.setHeight(3);
			} else {
				mod.setWidth((int) Math.ceil(mod.getPixelWidth() / pixelPerGridWidth) + 1);
				mod.setPixelWidth((int) ((mod.getWidth() - 1) * Math.floor(pixelPerGridWidth)));
				mod.setHeight((int) Math.ceil(mod.getPixelHeight() / pixelPerGridHeight) + 1);
				mod.setPixelHeight((int) ((mod.getHeight() - 1) * Math.floor(pixelPerGridHeight)));
			}

			if (_widthOfCells < mod.getWidth())
				_widthOfCells = mod.getWidth();
			if (_heightOfCells < mod.getHeight())
				_heightOfCells = mod.getHeight();

			result.add(mod);
		}
		
		return result;
	}
	
	private Set<RectangleObject> setBoxCells(Set<RectangleObject> result)
	{
		for (RectangleObject o : result) {
			Point tempPos = new Point();

			// Calculate the position of the cell
			tempPos.setX(o.getPosition().getX() * calculateCorridorSize(_widthOfCells, _options.CorridorRatio));
			tempPos.setY(o.getPosition().getY() * calculateCorridorSize(_heightOfCells, _options.CorridorRatio));
			_cellPositions.put(o.getName(), new Point(tempPos));

			// Calculate the position of the box in the cell
			o.setPosition(getPositionInCell(tempPos, o));
		}
		
		return result;
	}
	
	private Integer calculateCorridorSize(Integer cellSize, Double multiplier) {
		Integer result = (int) Math.floor(cellSize * (multiplier + 1.0));

		if (result < MINIMAL_CORRIDOR_SIZE)
			result = MINIMAL_CORRIDOR_SIZE;

		return result;
	}
	
	private void updateLinks()
	{
		for (LineAssociation a : _assocs) {
			ArrayList<Point> route = new ArrayList<Point>();
			route.add(_objects.stream().filter(o -> o.getName().equals(a.getFrom())).findFirst().get().getPosition());
			route.add(_objects.stream().filter(o -> o.getName().equals(a.getTo())).findFirst().get().getPosition());

			a.setRoute(route);
		}
	}
	
	private Point getPositionInCell(Point old_value, RectangleObject box)
	{
		Point result = new Point(old_value);
		
		Integer freeGridCountWidth = _widthOfCells - box.getWidth();
		Integer freeGridCountHeight = _heightOfCells - box.getHeight();

		result.setX(result.getX() + freeGridCountWidth / 2);
		result.setY(result.getY() - freeGridCountHeight / 2);
		
		return result;
	}
	
	private boolean isPresentMultiLevelLinks()
	{
		for(LineAssociation link : _assocs)
		{
			if(isEndInInnerDiagram(link.getTo()) || isEndInInnerDiagram(link.getFrom()))
			{
				return true;
			}	
		}
		
		return false;
	}
	
	private boolean isEndInInnerDiagram(String boxName)
	{
		for(RectangleObject box : _objects)
		{
			if(box.getName().equals(boxName))
			{
				return false;
			}
		}
		
		return true;
	}
	
	private void expandGrid() throws ConversionException {
		Set<RectangleObject> result = new HashSet<RectangleObject>();
		_widthOfCells = _widthOfCells * 2;
		_heightOfCells = _heightOfCells * 2;

		for (RectangleObject o : _objects) {
			RectangleObject mod = new RectangleObject(o);
			mod.setWidth(mod.getWidth() * 2);
			mod.setHeight(mod.getHeight() * 2);

			// Calculate the position of the box in the cell
			Point tempPos = Point.Multiply(_cellPositions.get(o.getName()), 2);
			_cellPositions.put(o.getName(), new Point(tempPos));
			
			// Calculate the position of the box in the cell
			mod.setPosition(getPositionInCell(tempPos, o));

			result.add(mod);
		}

		for (LineAssociation mod : _assocs) {
			RectangleObject fromBox = result.stream().filter(box -> box.getName().equals(mod.getFrom())).findFirst()
					.get();
			RectangleObject toBox = result.stream().filter(box -> box.getName().equals(mod.getTo())).findFirst().get();

			ArrayList<Point> route = new ArrayList<Point>();
			for (int j = 0; j < mod.getRoute().size(); ++j) {
				Point p = new Point(mod.getRoute().get(j));

				Point temp = Point.Multiply(p, 2);

				if (mod.isPlaced() && j > 1 && j < mod.getRoute().size() - 1) {
					Direction beforeDirection = Helper
							.asDirection(Point.Substract(mod.getRoute().get(j - 1), mod.getRoute().get(j)));
					Point before = Point.Add(temp, beforeDirection);

					if (fromBox.getPerimiterPoints().contains(before) || toBox.getPerimiterPoints().contains(before)
							|| (!fromBox.getPoints().contains(before) && !toBox.getPoints().contains(before)))
						route.add(before);
				}

				if (fromBox.getPerimiterPoints().contains(temp) || toBox.getPerimiterPoints().contains(temp)
						|| (!fromBox.getPoints().contains(temp) && !toBox.getPoints().contains(temp)))
					route.add(temp);
			}

			mod.setRoute(route);
		}

		_objects = result;
	}

	private void processStatements() throws ConversionException, InternalException {
		if (_statements != null && _statements.size() != 0) {
			// Set priority
			HashMap<String, Integer> priorityMap = setPriorityMap();

			// Order based on priority
			_assocs.sort((a1, a2) -> {
				if (priorityMap.containsKey(a1.getId())) {
					if (priorityMap.containsKey(a2.getId())) {
						return -1 * Integer.compare(priorityMap.get(a1.getId()), priorityMap.get(a2.getId()));
					} else
						return -1;
				} else {
					if (priorityMap.containsKey(a2.getId()))
						return 1;
					else
						return 0;
				}
			});

			ArrayList<Statement> priorityless = new ArrayList<Statement>(_statements);
			priorityless.removeIf(s -> s.getType().equals(StatementType.priority));

			// Set starts/ends for statemented assocs
			setPossibles(priorityless);
		}
	}

	private Set<Pair<Node, Double>> setSet(Pair<String, RouteConfig> key, Point start, Integer p_width,
			Integer p_height, Set<Point> occupied, Boolean isReflexive, Boolean isStart) throws InternalException {
		Set<Pair<Point, Double>> result = new HashSet<Pair<Point, Double>>();
		Double defaultWeight = -1.0;

		// Statement given points
		if (_possibleStarts.containsKey(key))
			result.addAll(_possibleStarts.get(key).stream().map(poi -> new Pair<Point, Double>(poi, defaultWeight))
					.collect(Collectors.toSet()));

		// Add Object's points
		RectangleObject tempObj = new RectangleObject("TEMP", start);
		tempObj.setWidth(p_width);
		tempObj.setHeight(p_height);

		if (result.size() == 0) {
			result.addAll(tempObj.getPerimiterPoints().stream().map(poi -> new Pair<Point, Double>(poi, defaultWeight))
					.collect(Collectors.toSet()));
			if (isReflexive) {
				if (isStart)
					result = setReflexiveSet(result, tempObj, true);
				else
					result = setReflexiveSet(result, tempObj, false);
			}
		}

		// Remove occupied points
		result.removeIf(p -> occupied.contains(p.getFirst()));
		// Remove corner points
		result.removeIf(p -> Helper.isCornerPoint(p.getFirst(), tempObj));

		// Set the weights of nodes.
		for (Pair<Point, Double> pair : result) {
			if (pair.getSecond() < 0.0) {
				Double distance1 = Point.Substract(pair.getFirst(), tempObj.getTopLeft()).length();
				Double distance2 = Point.Substract(pair.getFirst(), tempObj.getBottomRight()).length();

				Double w = p_width / 2.0;
				Double h = p_height / 2.0;

				if (Helper.isAlmostEqual(distance1, w, 0.8) || Helper.isAlmostEqual(distance1, h, 0.8)
						|| Helper.isAlmostEqual(distance2, w, 0.8) || Helper.isAlmostEqual(distance2, h, 0.8)) {
					pair = new Pair<Point, Double>(pair.getFirst(), 0.0);
				} else {
					pair = new Pair<Point, Double>(pair.getFirst(), 1.0);
				}
			}
		}

		if (isStart)
			return convertToNodes(result, tempObj);
		else
			return convertToInvertedNodes(result, tempObj);
	}

	private Set<Pair<Point, Double>> setReflexiveSet(Set<Pair<Point, Double>> fromSet, RectangleObject obj,
			Boolean isStart) {
		Set<Pair<Point, Double>> result = new HashSet<Pair<Point, Double>>(fromSet);

		Integer halfwayH = ((obj.getWidth() % 2) == 0) ? ((obj.getWidth() / 2) - 1) : ((obj.getWidth() - 1) / 2);
		Integer halfwayV = ((obj.getHeight() % 2) == 0) ? ((obj.getHeight() / 2) - 1) : ((obj.getHeight() - 1) / 2);

		Point northern = Point.Add(obj.getPosition(), new Point(halfwayH, 0));
		if (isStart)
			result.removeIf(p -> p.getFirst().getY().equals(northern.getY()) && p.getFirst().getX() >= northern.getX());
		else
			result.removeIf(p -> p.getFirst().getY().equals(northern.getY()) && p.getFirst().getX() <= northern.getX());

		Point eastern = Point.Add(obj.getBottomRight(), new Point(0, halfwayV));
		if (isStart)
			result.removeIf(p -> p.getFirst().getX().equals(eastern.getX()) && p.getFirst().getY() <= eastern.getY());
		else
			result.removeIf(p -> p.getFirst().getX().equals(eastern.getX()) && p.getFirst().getY() >= eastern.getY());

		Point southern = Point.Add(obj.getBottomRight(), new Point(-1 * halfwayH, 0));
		if (isStart)
			result.removeIf(p -> p.getFirst().getY().equals(southern.getY()) && p.getFirst().getX() <= southern.getX());
		else
			result.removeIf(p -> p.getFirst().getY().equals(southern.getY()) && p.getFirst().getX() >= southern.getX());

		Point western = Point.Add(obj.getPosition(), new Point(0, -1 * halfwayV));
		if (isStart)
			result.removeIf(p -> p.getFirst().getX().equals(western.getX()) && p.getFirst().getY() >= western.getY());
		else
			result.removeIf(p -> p.getFirst().getX().equals(western.getX()) && p.getFirst().getY() <= western.getY());

		// Set the weights of points
		for (Pair<Point, Double> pair : result) {
			pair = new Pair<Point, Double>(pair.getFirst(), 0.0);
		}

		return result;
	}

	private Set<Pair<Node, Double>> convertToNodes(Set<Pair<Point, Double>> ps, RectangleObject obj)
			throws InternalException {
		Set<Pair<Node, Double>> result = new HashSet<Pair<Node, Double>>();

		for (Pair<Point, Double> pair : ps) {
			if (obj.getTopLeft().getX().equals(pair.getFirst().getX()))
				result.add(new Pair<Node, Double>(new Node(pair.getFirst(), Point.Add(pair.getFirst(), Direction.west)),
						pair.getSecond()));
			else if (obj.getTopLeft().getY().equals(pair.getFirst().getY()))
				result.add(new Pair<Node, Double>(
						new Node(pair.getFirst(), Point.Add(pair.getFirst(), Direction.north)), pair.getSecond()));
			else if (obj.getBottomRight().getX().equals(pair.getFirst().getX()))
				result.add(new Pair<Node, Double>(new Node(pair.getFirst(), Point.Add(pair.getFirst(), Direction.east)),
						pair.getSecond()));
			else if (obj.getBottomRight().getY().equals(pair.getFirst().getY()))
				result.add(new Pair<Node, Double>(
						new Node(pair.getFirst(), Point.Add(pair.getFirst(), Direction.south)), pair.getSecond()));
			else
				throw new InternalException("BOOM1!");
		}

		return result;
	}

	private Set<Pair<Node, Double>> convertToInvertedNodes(Set<Pair<Point, Double>> ps, RectangleObject obj)
			throws InternalException {
		Set<Pair<Node, Double>> result = new HashSet<Pair<Node, Double>>();

		for (Pair<Point, Double> pair : ps) {
			if (obj.getTopLeft().getX().equals(pair.getFirst().getX()))
				result.add(new Pair<Node, Double>(new Node(Point.Add(pair.getFirst(), Direction.west), pair.getFirst()),
						pair.getSecond()));
			else if (obj.getTopLeft().getY().equals(pair.getFirst().getY()))
				result.add(new Pair<Node, Double>(
						new Node(Point.Add(pair.getFirst(), Direction.north), pair.getFirst()), pair.getSecond()));
			else if (obj.getBottomRight().getX().equals(pair.getFirst().getX()))
				result.add(new Pair<Node, Double>(new Node(Point.Add(pair.getFirst(), Direction.east), pair.getFirst()),
						pair.getSecond()));
			else if (obj.getBottomRight().getY().equals(pair.getFirst().getY()))
				result.add(new Pair<Node, Double>(
						new Node(Point.Add(pair.getFirst(), Direction.south), pair.getFirst()), pair.getSecond()));
			else
				throw new InternalException("BOOM2!");
		}

		return result;
	}

	private Boundary calculateBoundary() throws InternalException {
		Integer minX = Integer.MAX_VALUE;
		Integer maxX = Integer.MIN_VALUE;
		Integer minY = Integer.MAX_VALUE;
		Integer maxY = Integer.MIN_VALUE;

		for (RectangleObject o : _objects) {
			if (minX > o.getTopLeft().getX())
				minX = o.getTopLeft().getX();
			if (maxX < o.getBottomRight().getX())
				maxX = o.getBottomRight().getX();

			if (minY > o.getBottomRight().getY())
				minY = o.getBottomRight().getY();
			if (maxY < o.getTopLeft().getY())
				maxY = o.getTopLeft().getY();
		}

		return new Boundary(maxY, minY, minX, maxX);
	}

	private HashMap<String, Integer> setPriorityMap() {
		HashMap<String, Integer> result = new HashMap<String, Integer>();

		for (Statement s : _statements) {
			if (s.getType().equals(StatementType.priority)) {
				result.put(s.getParameter(0), Integer.parseInt(s.getParameter(1)));
			}
		}

		return result;
	}

	private void setPossibles(List<Statement> prioritylessStats)
			throws ConversionException, InternalException {
		for (Statement s : prioritylessStats) {
			try {
				LineAssociation link = _assocs.stream().filter(a -> a.getId().equals(s.getParameter(0))).findFirst()
						.get();
				RectangleObject obj = _objects.stream().filter(o -> o.getName().equals(s.getParameter(1))).findFirst()
						.get();
				if (link.getFrom().equals(obj.getName())
						&& (s.getParameters().size() == 2 || s.getParameter(2).toLowerCase().equals("start"))) {
					// RouteConfig.START
					Point startPoint = getStartingPoint(Helper.asDirection(s.getType()), obj);
					Direction moveDir = getMoveDirection(s.getType());
					generatePossiblePoints(link, obj, startPoint, moveDir, RouteConfig.START);
				}
				if (link.getTo().equals(obj.getName())
						&& (s.getParameters().size() == 2 || s.getParameter(2).toLowerCase().equals("end"))) {
					// RouteConfig.END
					Point startPoint = getStartingPoint(Helper.asDirection(s.getType()), obj);
					Direction moveDir = getMoveDirection(s.getType());
					generatePossiblePoints(link, obj, startPoint, moveDir, RouteConfig.END);
				}
			} catch (NoSuchElementException e) {
				throw new InternalException("Inner Exception: [" + e.getMessage() + "], "
						+ "Probably a statment shouldn't have reached this code!");
			}
		}
	}

	private Point getStartingPoint(Direction dir, RectangleObject o) throws InternalException {
		if (dir.equals(Direction.north) || dir.equals(Direction.west))
			return o.getPosition();
		else if (dir.equals(Direction.south) || dir.equals(Direction.east))
			return o.getBottomRight();
		else
			throw new InternalException("Unknown Direction!");
	}

	private Direction getMoveDirection(StatementType ty) throws InternalException {
		switch (ty) {
		case north:
			return Direction.east;
		case west:
			return Direction.south;
		case south:
			return Direction.west;
		case east:
			return Direction.north;
		case above:
		case below:
		case horizontal:
		case left:
		case phantom:
		case priority:
		case right:
		case unknown:
		case vertical:
		case corridorsize:
		case overlaparrange:
		default:
			throw new InternalException("Cannot evaluate MoveDirection for " + ty.toString() + "!");
		}
	}

	private void generatePossiblePoints(LineAssociation toModify, RectangleObject connectsTo, Point first,
			Direction toMove, RouteConfig r) {
		HashSet<Point> points = new HashSet<Point>();

		Integer endOfSide = (toMove.equals(Direction.north) || toMove.equals(Direction.south)) ? connectsTo.getHeight()
				: connectsTo.getWidth();

		for (int i = 0; i < endOfSide; ++i) {
			points.add(Point.Add(first, Point.Multiply(toMove, i)));
		}
		Pair<String, RouteConfig> key = new Pair<String, LineAssociation.RouteConfig>(toModify.getId(), r);

		_possibleStarts.put(key, points);
	}

	private void arrangeLinks(Map<Point, Color> occupied, Boundary bounds)
			throws CannotStartAssociationRouteException, CannotFindAssociationRouteException, InternalException,
			ConversionException {
		Map<Point, Color> occupiedLinks = new HashMap<Point, Color>();

		Integer c = 0;
		for (LineAssociation a : _assocs) {
			++c;
			if (_options.Logging)
				Logger.sys.info(c + "/" + _assocs.size() + ": " + a.getId() + " ... ");

			if (a.isPlaced()) {
				if (_options.Logging)
					Logger.sys.info("NOTHING TO DO!");

				Map<Point, Color> routePoints = getRoutePaintedPoints(a);
				occupiedLinks.putAll(routePoints);
				occupied.putAll(routePoints);
				continue;
			}

			doGraphSearch(a, occupiedLinks, occupied, bounds);

			if (_options.Logging)
				Logger.sys.info("DONE!");

			if (a.getRoute().size() < 3)
				throw new InternalException("Route is shorter then 3!");

			// Update occupied places with the route of this link
			Map<Point, Color> routePoints = getRoutePaintedPoints(a);
			occupiedLinks.putAll(routePoints);
			occupied.putAll(routePoints);

			if (_assocs.indexOf(a) == (int) (_assocs.size() * 25.0 / 100.0)) {
				ProgressManager.getEmitter().OnLinkArrangeFirstQuarter();
			} else if (_assocs.indexOf(a) == (int) (_assocs.size() * 50.0 / 100.0)) {
				ProgressManager.getEmitter().OnLinkArrangeHalf();
			} else if (_assocs.indexOf(a) == (int) (_assocs.size() * 75.0 / 100.0)) {
				ProgressManager.getEmitter().OnLinkArrangeThirdQuarter();
			}
		}
	}

	private void doGraphSearch( LineAssociation a, Map<Point, Color> occupiedLinks,
			Map<Point, Color> occupied, Boundary bounds) throws InternalException,
					CannotStartAssociationRouteException, CannotFindAssociationRouteException, ConversionException {
		Map<Point, Color> ignores = new HashMap<Point, Color>();
		
		RectangleObject STARTBOX = getBox(a.getFrom(), ignores);
		RectangleObject ENDBOX = getBox(a.getTo(), ignores);
		
		Set<Pair<Node, Double>> STARTSET = setSet(new Pair<String, RouteConfig>(a.getId(), RouteConfig.START),
				STARTBOX.getPosition(), STARTBOX.getWidth(), STARTBOX.getHeight(), occupiedLinks.keySet(),
				a.isReflexive(), true);

		Set<Pair<Node, Double>> ENDSET = setSet(new Pair<String, RouteConfig>(a.getId(), RouteConfig.END),
				ENDBOX.getPosition(), ENDBOX.getWidth(), ENDBOX.getHeight(), occupiedLinks.keySet(), a.isReflexive(),
				false);

		// Search for the route
		if (STARTSET.size() == 0 || ENDSET.size() == 0) {
			// Cannot start or end
			throw new CannotStartAssociationRouteException("Cannot get out of start, or cannot enter end!");
		}

		GraphSearch gs = new GraphSearch(STARTSET, ENDSET, occupied, ignores, bounds);

		a.setRoute(convertFromNodes(gs.value(), STARTBOX.getPosition(), ENDBOX.getPosition()));
		a.setExtends(gs.extendsNum());
	}
	
	private RectangleObject getBox(String name, Map<Point, Color> ignores)
	{
		RectangleObject result = _objects.stream().filter(o -> o.getName().equals(name)).findFirst()
				.orElse(null);
		
		if(result == null)
		{
			//Link goes into sub package
			
			//Not handled currently
			for(RectangleObject box : new RectangleObjectTreeEnumerator(_objects))
			{
				if(box.getName().equals(name))
				{
					return	box;
				}
			}
		}
		
		return result;
	}

	private Map<Point, Color> getRoutePaintedPoints(LineAssociation link) {
		Map<Point, Color> result = new HashMap<Point, Color>();

		for (int ri = 1; ri < link.getRoute().size() - 1; ++ri) {
			if (!Point.Substract(link.getRoute().get(ri - 1), link.getRoute().get(ri))
					.equals(Point.Substract(link.getRoute().get(ri), link.getRoute().get(ri + 1)))) {
				result.put(new Point(link.getRoute().get(ri)), Color.Red);
			} else {
				result.put(new Point(link.getRoute().get(ri)), Color.Yellow);
			}
		}

		return result;
	}

	private ArrayList<Point> convertFromNodes(ArrayList<Node> nodes, Point start, Point end) {
		ArrayList<Point> result = new ArrayList<Point>();

		result.add(start);
		result.add(new Point(nodes.get(0).getFrom()));

		for (int i = 0; i < nodes.size(); ++i) {
			result.add(new Point(nodes.get(i).getTo()));
		}

		result.add(end);

		return result;
	}

	/**
	 * Returns the final value of the lines of links.
	 * 
	 * @return Set of the arranged lines of links.
	 */
	public Set<LineAssociation> value() {
		Set<LineAssociation> result = _assocs.stream().collect(Collectors.toSet());

		return result;
	}

	/**
	 * Returns the final layout of the boxes.
	 * 
	 * @return the final layout of the boxes.
	 */
	public Set<RectangleObject> objects() {
		return _objects;
	}

}
