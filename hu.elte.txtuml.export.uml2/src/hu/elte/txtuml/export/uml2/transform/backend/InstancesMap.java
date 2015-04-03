package hu.elte.txtuml.export.uml2.transform.backend;

import java.util.Map;

/**
 * Represents a map for storing information for dummy instances.
 * Key: the dummy instance
 * Value: instance information for the dummy instance
 * @author �d�m Ancsin
 *
 */
public interface InstancesMap extends
		Map<Object, InstanceInformation> {

	/**
	 * Factory method for creating an InstancesMapImpl instance.
	 * @return The created map.
	 *
	 * @author �d�m Ancsin
	 */
	static InstancesMapImpl create()
	{
		return new InstancesMapImpl();
	}
		
}
