package hu.elte.txtuml.layout.export.elementinfo;

import hu.elte.txtuml.api.layout.elements.LayoutElement;
import hu.elte.txtuml.layout.export.DiagramType;
import hu.elte.txtuml.layout.export.interfaces.LinkMap;
import hu.elte.txtuml.layout.export.elementinfo.LinkInfo;
import hu.elte.txtuml.layout.export.elementinfo.impl.LinkGroupInfoImpl;

/**
 * 
 * @author D�vid J�nos N�meth
 *
 */
public interface LinkGroupInfo extends ElementInfo {

    static LinkGroupInfo create(Class<? extends LayoutElement> elementClass, DiagramType diagType, String asString) {
        return new LinkGroupInfoImpl(elementClass, diagType, asString);
    }
    
    boolean beingExported();
    
    void setBeingExported(boolean val);
    
    LinkMap getAllLinks();
    
    void addLink(LinkInfo link);
    
}
