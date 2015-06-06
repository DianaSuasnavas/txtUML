package hu.elte.txtuml.api.backend.collections.impl;

import hu.elte.txtuml.api.ModelClass;
import hu.elte.txtuml.api.backend.collections.JavaCollectionOfMany;

import java.util.LinkedList;

/**
 * Default implementation for {@link JavaCollectionOfMany}.
 * <p>
 * Despite being a subclass of the {@link java.io.Serializable} interface
 * through {@link LinkedList}, this class does not provide a
 * <code>serialVersionUID</code> because serialization is never used on it.
 * 
 * @param <T>
 *            the type of model objects to be contained in this collection
 */
@SuppressWarnings("serial")
public class JavaCollectionOfManyImpl<T extends ModelClass> extends
		LinkedList<T> implements JavaCollectionOfMany<T> {
}
