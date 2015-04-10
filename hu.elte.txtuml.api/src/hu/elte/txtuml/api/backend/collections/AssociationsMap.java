package hu.elte.txtuml.api.backend.collections;

import hu.elte.txtuml.api.AssociationEnd;
import hu.elte.txtuml.api.ModelClass;
import hu.elte.txtuml.api.backend.collections.impl.AssociationsMapImpl;

import java.util.Map;

/**
 * A mapping of classes representing association ends to instances of those
 * association ends. Used as the type of a private field of {@link ModelClass}.
 *
 * @author Gabor Ferenc Kovacs
 *
 */
public interface AssociationsMap extends
		Map<Class<? extends AssociationEnd<?>>, AssociationEnd<?>> {

	/**
	 * Creates a new instance of <code>AssociationsMap</code>.
	 * 
	 * @return the new instance
	 */
	static AssociationsMap create() {
		return new AssociationsMapImpl();
	}

	/*
	 * The toString() method of AssociationsMap should be overridden.
	 */
	@Override
	public String toString();

}
