package hu.elte.txtuml.layout.visualizer.helpers;

import hu.elte.txtuml.layout.visualizer.algorithms.DiagramType;
import hu.elte.txtuml.layout.visualizer.annotations.Statement;
import hu.elte.txtuml.layout.visualizer.annotations.StatementType;
import hu.elte.txtuml.layout.visualizer.exceptions.ConversionException;
import hu.elte.txtuml.layout.visualizer.model.Direction;
import hu.elte.txtuml.layout.visualizer.model.LineAssociation;
import hu.elte.txtuml.layout.visualizer.model.Point;
import hu.elte.txtuml.layout.visualizer.model.RectangleObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Static class for various algorithm related helping tasks, such as
 * conversions, cloning or parsing.
 * 
 * @author Bal�zs Gregorics
 *
 */
public class Helper
{
	
	/**
	 * Method to clone a specific Set. A set of {@link Point}s.
	 * 
	 * @param toClone
	 *            The set to clone.
	 * @return A completly cloned set.
	 */
	public static Set<Point> clonePointSet(Set<Point> toClone)
	{
		Set<Point> result = new HashSet<Point>();
		
		for (Point p : toClone)
		{
			result.add(p.clone());
		}
		
		return result;
	}
	
	/**
	 * Method to clone a specific List. A list of {@link Statement}s.
	 * 
	 * @param toClone
	 *            List of {@link Statement}s
	 * @return The cloned list.
	 */
	public static ArrayList<Statement> cloneStatementList(ArrayList<Statement> toClone)
	{
		ArrayList<Statement> result = new ArrayList<Statement>();
		
		for (Statement s : toClone)
		{
			result.add(new Statement(s));
		}
		
		return result;
	}
	
	/**
	 * Method to clone a specific Set. A set of Strings.
	 * 
	 * @param toClone
	 *            The set to clone.
	 * @return A completly cloned set.
	 */
	public static Set<String> cloneStringSet(Set<String> toClone)
	{
		Set<String> result = new HashSet<String>();
		
		for (String s : toClone)
		{
			result.add(new String(s));
		}
		
		return result;
	}
	
	/**
	 * Method to clone a specific map. A map of String and ArrayList\<String\>.
	 * 
	 * @param toClone
	 *            The map to clone.
	 * @return A completly cloned map.
	 */
	public static HashMap<String, ArrayList<String>> cloneMap(
			HashMap<String, ArrayList<String>> toClone)
	{
		HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		
		for (Entry<String, ArrayList<String>> entry : toClone.entrySet())
		{
			result.put(new String(entry.getKey()), cloneStringList(entry.getValue()));
		}
		
		return result;
	}
	
	/**
	 * Method to clone a specific list. A list of Strings.
	 * 
	 * @param toClone
	 *            The list to clone.
	 * @return A completly cloned list.
	 */
	public static ArrayList<String> cloneStringList(ArrayList<String> toClone)
	{
		ArrayList<String> result = new ArrayList<String>();
		
		for (String s : toClone)
		{
			result.add(new String(s));
		}
		
		return result;
	}
	
	/**
	 * Method to clone a specific list. A list of Points.
	 * 
	 * @param toClone
	 *            The list to clone.
	 * @return A completly cloned list.
	 */
	public static ArrayList<Point> clonePointList(ArrayList<Point> toClone)
	{
		ArrayList<Point> result = new ArrayList<Point>();
		
		for (Point p : toClone)
		{
			result.add(p.clone());
		}
		
		return result;
	}
	
	/**
	 * Method to clone a specific list. A list of LineAssociations.
	 * 
	 * @param toClone
	 *            The list to clone.
	 * @return A completly cloned list.
	 */
	public static ArrayList<LineAssociation> cloneLinkList(
			ArrayList<LineAssociation> toClone)
	{
		ArrayList<LineAssociation> result = new ArrayList<LineAssociation>();
		
		for (LineAssociation a : toClone)
		{
			result.add(a.clone());
		}
		
		return result;
	}
	
	/**
	 * Method for parsing a String value to Integer if possible.
	 * 
	 * @param value
	 *            String to parse.
	 * @return True if the value can be parsed, else False.
	 */
	public static boolean tryParseInt(String value)
	{
		try
		{
			Integer.parseInt(value);
			return true;
		}
		catch (NumberFormatException nfe)
		{
			return false;
		}
	}
	
	/**
	 * Method to convert {@link StatementType} to {@link Direction}.
	 * 
	 * @param ty
	 *            StatementType to convert.
	 * @return The converted Direction if possible.
	 * @throws ConversionException
	 *             Throws if the given StatementType is cannot be converted to
	 *             any Direction.
	 */
	public static Direction asDirection(StatementType ty) throws ConversionException
	{
		switch (ty)
		{
			case north:
				return Direction.north;
			case south:
				return Direction.south;
			case east:
				return Direction.east;
			case west:
				return Direction.west;
			case above:
			case below:
			case horizontal:
			case left:
			case phantom:
			case priority:
			case right:
			case unknown:
			case vertical:
			default:
				throw new ConversionException("Cannot convert type " + ty
						+ " to Direction!");
		}
	}
	
	/**
	 * Method to convert {@link Direction} to {@link StatementType}.
	 * 
	 * @param dir
	 *            Dire.ction to convert
	 * @return The converted StatementType
	 * @throws ConversionException
	 *             Throws if the given Direction is cannot be converted to any
	 *             StatementType.
	 */
	public static StatementType asStatementType(Direction dir) throws ConversionException
	{
		switch (dir)
		{
			case north:
				return StatementType.north;
			case south:
				return StatementType.south;
			case east:
				return StatementType.east;
			case west:
				return StatementType.west;
			default:
				throw new ConversionException("Cannot convert direction " + dir
						+ " to StatementType!");
		}
	}
	
	/**
	 * Detects whether we should arrange reflexive links on an object's same
	 * side or not, depending on the diagram's type.
	 * 
	 * @param type
	 *            Diagram' type we want to layout.
	 * @return True if the reflexive links should be on the same side of an
	 *         object.
	 */
	public static boolean isReflexiveOnSameSide(DiagramType type)
	{
		switch (type)
		{
			case Class:
				return true;
			case State:
			case Activity:
				return false;
			default:
				return true;
		}
	}
	
	/**
	 * Converts a Point(Vector) to a Direction.
	 * 
	 * @param p
	 *            Point to convert.
	 * @return The converted Direction.
	 * @throws ConversionException
	 *             Throws if no such Direction exists.
	 */
	public static Direction asDirection(Point p) throws ConversionException
	{
		if (p.getX() == 0 && p.getY() > 0)
			return Direction.north;
		if (p.getX() == 0 && p.getY() < 0)
			return Direction.south;
		if (p.getX() > 0 && p.getY() == 0)
			return Direction.east;
		if (p.getX() < 0 && p.getY() == 0)
			return Direction.west;
		
		throw new ConversionException("Cannot convert Point " + p.toString()
				+ " to any Direction!");
	}
	
	/**
	 * Concatenates a list of lists of points to a single list of points.
	 * 
	 * @param list
	 *            List to flatten.
	 * @return List of points.
	 */
	public static ArrayList<Point> concatPointList(ArrayList<ArrayList<Point>> list)
	{
		ArrayList<Point> result = new ArrayList<Point>();
		
		for (ArrayList<Point> innerlist : list)
		{
			for (Point p : innerlist)
			{
				result.add(p);
			}
		}
		
		return result;
	}
	
	/**
	 * Concatenates a set of sets of points to a single set of points.
	 * 
	 * @param set
	 *            Set to flatten.
	 * @return Set of points.
	 */
	public static Set<Point> concatPointSet(Set<Set<Point>> set)
	{
		Set<Point> result = new HashSet<Point>();
		
		for (Set<Point> innerset : set)
		{
			for (Point p : innerset)
			{
				result.add(p);
			}
		}
		
		return result;
	}
	
	/**
	 * Returns whether the Point p is the corner point of the Object obj.
	 * 
	 * @param p
	 *            Point to check.
	 * @param obj
	 *            Object to check.
	 * @return True if p is a corner point of obj.
	 */
	public static Boolean isCornerPoint(Point p, RectangleObject obj)
	{
		return Math.abs(p.getX() - obj.getPosition().getX()) == Math.abs(p.getY()
				- obj.getPosition().getY());
	}
	
}
