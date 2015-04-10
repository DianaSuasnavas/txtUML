package hu.elte.txtuml.api.backend.collections;

import hu.elte.txtuml.api.ModelClass;
import hu.elte.txtuml.api.backend.collections.impl.JavaCollectionOfManyImpl;

/**
 * A Java collection of {@link ModelClass} objects for implementations of
 * association ends having a multiplicity of <i>many</i> (0..*).
 *
 * @author Gabor Ferenc Kovacs
 *
 * @param <T>
 *            the type of model objects to be contained in this collection
 */
public interface JavaCollectionOfMany<T extends ModelClass> extends
		java.util.Collection<T> {

	/**
	 * Creates a new <code>JavaCollectionOfMany</code> instance.
	 * 
	 * @return the new instance
	 */
	static <T extends ModelClass> JavaCollectionOfMany<T> create() {
		return new JavaCollectionOfManyImpl<>();
	}

}
