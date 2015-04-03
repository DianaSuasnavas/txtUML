package hu.elte.txtuml.export.uml2.transform.backend;


/**
 * Represents information of a dummy instance needed by the importer.
 * @author �d�m Ancsin
 *
 */
public interface InstanceInformation {

	/**
	 * Factory method for creating instance information for a literal.
	 * @param expression The expression of the dummy instance.
	 * @return The created instance information.
	 *
	 * @author �d�m Ancsin
	 */
	static InstanceInformation createLiteral(String expression)
	{
		return new InstanceInformationImpl (expression,true,false);
	}
	
	/**
	 * Factory method for creating instance information for a calculated instance.
	 * @param expression The expression of the dummy instance.
	 * @return The created instance information.
	 *
	 * @author �d�m Ancsin
	 */
	static InstanceInformation createCalculated(String expression)
	{
		return new InstanceInformationImpl (expression,false,true);
	}
	
	/**
	 * Factory method for creating instance information for a dummy instance that is
	 * not a literal nor a calculated instance.
	 * @param expression The expression of the dummy instance.
	 * @return The created instance information.
	 *
	 * @author �d�m Ancsin
	 */
	static InstanceInformation create(String expression)
	{
		return new InstanceInformationImpl (expression);
	}

	/**
	 * Gets the expression of the dummy instance.
	 * @return The expression of the dummy instance.
	 *
	 * @author �d�m Ancsin
	 */
	String getExpression();

	/**
	 * Decides if the dummy instance is a literal.
	 * @return The decision.
	 *
	 * @author �d�m Ancsin
	 */
	boolean isLiteral();

	/**
	 * Decides if the dummy instance is a calculated instance.
	 * @return The decision.
	 *
	 * @author �d�m Ancsin
	 */
	boolean isCalculated();
}
