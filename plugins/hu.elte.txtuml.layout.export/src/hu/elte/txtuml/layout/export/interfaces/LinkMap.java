package hu.elte.txtuml.layout.export.interfaces;

import hu.elte.txtuml.layout.export.elementinfo.LinkInfo;
import hu.elte.txtuml.layout.export.impl.LinkMapImpl;
import hu.elte.txtuml.layout.lang.elements.LayoutLink;
import hu.elte.txtuml.layout.visualizer.model.LineAssociation;

import java.util.Map;
import java.util.Set;

/**
 * 
 * @author G�bor Ferenc Kov�cs
 *
 */
public interface LinkMap extends Map<Class<? extends LayoutLink>, LinkInfo> {
	
	static LinkMap create() {
		return new LinkMapImpl();
	}
	
	Set<LineAssociation> convert();
	
}
