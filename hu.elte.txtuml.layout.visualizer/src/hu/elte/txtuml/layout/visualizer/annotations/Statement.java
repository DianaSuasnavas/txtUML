package hu.elte.txtuml.layout.visualizer.annotations;

import hu.elte.txtuml.layout.visualizer.exceptions.InternalException;
import hu.elte.txtuml.layout.visualizer.exceptions.UnknownStatementException;

import java.util.ArrayList;

/**
 * Statement class represents a layout statement defined on a diagram object or
 * link. These support the algorithm to arrange the objects and the links.
 * 
 * @author Bal�zs Gregorics
 *
 */
public class Statement
{
	// Variables
	
	private StatementType _type;
	private ArrayList<String> _parameters;
	private StatementLevel _level;
	private Integer _group;
	
	// end Variables
	
	// Getters, setters
	
	/***
	 * Getter for the Type of the Statement.
	 * 
	 * @return Type of the Statement.
	 */
	public StatementType getType()
	{
		return _type;
	}
	
	/***
	 * Getter for the Parameters of the Statement.
	 * 
	 * @return ArrayList of Strings, representing the Parameters.
	 */
	public ArrayList<String> getParameters()
	{
		return _parameters;
	}
	
	/***
	 * Getter for a specific Parameter of the Statement.
	 * 
	 * @param i
	 *            Parameter's index.
	 * @return String representing the i-th Parameter.
	 */
	public String getParameter(Integer i)
	{
		return _parameters.get(i);
	}
	
	/**
	 * Setter for a particular element in the parameters of a statement.
	 * 
	 * @param i
	 *            Index of the parameter.
	 * @param value
	 *            Value to set.
	 */
	public void setParameter(Integer i, String value)
	{
		if (_parameters.size() > i)
		{
			_parameters.set(i, value);
		}
	}
	
	/**
	 * Method to get if this statement is user created or not.
	 * 
	 * @return True if this statement was created by the user.
	 */
	public Boolean isUserDefined()
	{
		return _level.equals(StatementLevel.User);
	}
	
	/**
	 * Getter for the level of the statement.
	 * 
	 * @return The level of the Statement.
	 */
	public StatementLevel getLevel()
	{
		return _level;
	}
	
	/**
	 * Getter for the id of the group this statement is in.
	 * 
	 * @return Id of the statement group.
	 */
	public Integer getGroupId()
	{
		return _group;
	}
	
	// end Getters, setters
	
	// Ctors
	
	/***
	 * Create Layout Statement.
	 * 
	 * @param t
	 *            Type of the Statement to create.
	 * @param params
	 *            Strings representing the Parameters of the Statement to
	 *            create.
	 */
	public Statement(StatementType t, String... params)
	{
		_type = t;
		_parameters = new ArrayList<String>();
		for (String s : params)
		{
			_parameters.add(s);
		}
		_level = StatementLevel.User;
		_group = null;
	}
	
	/**
	 * Create Layout Statement.
	 *
	 * @param t
	 *            Type of the Statement to create.
	 * @param level
	 *            The level of the statement.
	 * @param params
	 *            Strings representing the Parameters of the Statement to
	 *            create.
	 * @throws InternalException
	 *             Throws if the level is set to non-user but no group id is
	 *             set.
	 */
	public Statement(StatementType t, StatementLevel level, String... params)
			throws InternalException
	{
		_type = t;
		_parameters = new ArrayList<String>();
		for (String s : params)
		{
			_parameters.add(s);
		}
		_level = level;
		if (!_level.equals(StatementLevel.User))
			throw new InternalException("This statement should have group Id: "
					+ this.toString() + "!");
		_group = null;
	}
	
	/**
	 * Create Layout Statement.
	 *
	 * @param t
	 *            Type of the Statement to create.
	 * @param level
	 *            The level of the statement.
	 * @param id
	 *            Statement Group Id.
	 * @param params
	 *            Strings representing the Parameters of the Statement to
	 *            create.
	 * @throws InternalException
	 *             Throws if the level is set to non-user but no group id is
	 *             set.
	 */
	public Statement(StatementType t, StatementLevel level, Integer id, String... params)
			throws InternalException
	{
		_type = t;
		_parameters = new ArrayList<String>();
		for (String s : params)
		{
			_parameters.add(s);
		}
		_level = level;
		_group = id;
		if (!_level.equals(StatementLevel.User) && _group == null)
			throw new InternalException("This statement should have group Id: "
					+ this.toString() + "!");
	}
	
	/**
	 * Create Layout Statement.
	 * 
	 * @param t
	 *            Type of the Statement to create.
	 * @param params
	 *            ArrayList of Strings representing the Parameters of the
	 *            Statement to create.
	 */
	public Statement(StatementType t, ArrayList<String> params)
	{
		_type = t;
		_parameters = new ArrayList<String>();
		for (String s : params)
		{
			_parameters.add(s);
		}
		_level = StatementLevel.User;
		_group = null;
	}
	
	/**
	 * Create Layout Statement.
	 * 
	 * @param t
	 *            Type of the Statement to create.
	 * @param level
	 *            The level of the statement.
	 * @param params
	 *            ArrayList of Strings representing the Parameters of the
	 *            Statement to create.
	 * @throws InternalException
	 *             Throws if the level is set to non-user but no group id is
	 *             set.
	 */
	public Statement(StatementType t, StatementLevel level, ArrayList<String> params)
			throws InternalException
	{
		_type = t;
		_parameters = new ArrayList<String>();
		for (String s : params)
		{
			_parameters.add(s);
		}
		_level = level;
		if (!_level.equals(StatementLevel.User))
			throw new InternalException("This statement should have group Id: "
					+ this.toString() + "!");
		_group = null;
	}
	
	/**
	 * Create Layout Statement.
	 * 
	 * @param t
	 *            Type of the Statement to create.
	 * @param level
	 *            The level of the statement.
	 * @param id
	 *            Statement group Id.
	 * @param params
	 *            ArrayList of Strings representing the Parameters of the
	 *            Statement to create.
	 * @throws InternalException
	 *             Throws if the level is set to non-user but no group id is
	 *             set.
	 */
	public Statement(StatementType t, StatementLevel level, Integer id,
			ArrayList<String> params) throws InternalException
	{
		_type = t;
		_parameters = new ArrayList<String>();
		for (String s : params)
		{
			_parameters.add(s);
		}
		_level = level;
		_group = id;
		if (!_level.equals(StatementLevel.User) && _group == null)
			throw new InternalException("This statement should have group Id: "
					+ this.toString() + "!");
	}
	
	// end Ctors
	
	// Statics
	
	/***
	 * Function to parse String form of a Statement.
	 * 
	 * @param input
	 *            String input to parse.
	 * @return The Statement, parsed.
	 * @throws UnknownStatementException
	 *             Throws if any error occurs.
	 */
	public static Statement Parse(String input) throws UnknownStatementException
	{
		StatementType type = StatementType.unknown;
		String[] params;
		
		input = input.replaceAll(" ", "");
		
		Integer parOpen = input.indexOf("(");
		Integer parClose = input.lastIndexOf(")");
		
		if (parOpen == -1 || parClose == -1)
			throw new UnknownStatementException("No (not enough) parentheisis found!");
		
		try
		{
			type = Enum.valueOf(StatementType.class, input.substring(0, parOpen)
					.toLowerCase());
		}
		catch (IllegalArgumentException e)
		{
			throw new UnknownStatementException("No known statement such as: "
					+ input.substring(0, parOpen) + "!");
		}
		
		params = input.substring(parOpen + 1, parClose).split(",");
		
		if (!enoughParametersForType(type, params))
			throw new UnknownStatementException(
					"Not enough / Too many parameters for type!");
		
		return new Statement(type, params);
	}
	
	private static boolean enoughParametersForType(StatementType t, String[] p)
	{
		switch (t)
		{
			case north:
			case south:
			case east:
			case west:
				if (p.length == 2 || p.length == 3)
					return true;
				break;
			case above:
			case below:
			case right:
			case left:
			case horizontal:
			case vertical:
			case priority:
				if (p.length == 2)
					return true;
				break;
			case phantom:
				if (p.length == 1)
					return true;
				break;
			case unknown:
			default:
				break;
		}
		return false;
	}
	
	/***
	 * Equality function on Statements.
	 * 
	 * @param s1
	 *            First statement to check.
	 * @param s2
	 *            Second statement to check.
	 * @return If s1 == s2, then TRUE, else FALSE.
	 */
	public static boolean Equals(Statement s1, Statement s2)
	{
		return s1.equals(s2);
	}
	
	// end Statics
	
	// Methods
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass())
		{
			return false;
		}
		if (this instanceof Statement && obj instanceof Statement)
		{
			Statement s1 = (Statement) this;
			Statement s2 = (Statement) obj;
			return s1._type.equals(s2._type) && s1._parameters.equals(s2._parameters)
					&& s1._level.equals(s2._level);
		}
		
		return false;
	}
	
	/**
	 * Indicates whether some object is "equal" to this one beside the fact that
	 * they were created by user or not.
	 * 
	 * @param obj
	 *            the reference object with which to compare.
	 * @return true if this object is the same as the obj argument without
	 *         checking if it's user created or not; false otherwise.
	 */
	public boolean equalsByValue(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass())
		{
			return false;
		}
		if (this instanceof Statement && obj instanceof Statement)
		{
			Statement s1 = (Statement) this;
			Statement s2 = (Statement) obj;
			return s1._type.equals(s2._type) && s1._parameters.equals(s2._parameters);
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + _type.hashCode();
		result = prime * result + _parameters.hashCode();
		result = prime * result + _level.hashCode();
		return result;
	}
	
	@Override
	public String toString()
	{
		String result = _type.toString() + "(";
		for (String p : _parameters)
		{
			if (!result.substring(result.length() - 1, result.length()).equals("("))
				result += ", ";
			result += p;
		}
		result += ")";
		if (!_level.equals(StatementLevel.User))
			result += "_weak(" + _level.toString() + ","
					+ ((_group != null) ? _group.toString() : "0") + ")";
		return result;
	}
	
	// end Methods
	
}
