package hu.elte.txtuml.api.model;

/**
 * Base class for association ends having a multiplicity of 1.
 * <p>
 * Inherits its implementation from <code>BaseMaybeOne</code>.
 * <p>
 * Directly unusable by the user.
 * 
 * @author Gabor Ferenc Kovacs
 *
 * @param <T>
 *            the type of model objects to be contained in this collection
 */
class BaseOne<T extends ModelClass> extends BaseMaybeOne<T> {

	@Override
	boolean checkLowerBound() {
		return getSize() > 0;
	}

}
