package hu.elte.txtuml.api;

/**
 * Base class of association ends having a 1..* multiplicity.
 * <p>
 * Inherits its implementation from <code>BaseMany</code>.
 * <p>
 * See the documentation of the {@link hu.elte.txtuml.api} package to get an
 * overview on modeling in txtUML.
 * 
 * @author G�bor Ferenc Kov�cs
 *
 * @param <T> the type of model objects to be contained in this collection
 */
class BaseSome<T extends ModelClass> extends BaseMany<T> {

	@Override
	boolean checkLowerBound() {
		return getSize() > 0;
	}

}
