package hu.elte.txtuml.layout.export.diagramexporters;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.List;

import hu.elte.txtuml.api.Association;
import hu.elte.txtuml.api.AssociationEnd;
import hu.elte.txtuml.api.Model;
import hu.elte.txtuml.api.ModelClass;
import hu.elte.txtuml.layout.export.DiagramType;
import hu.elte.txtuml.layout.export.interfaces.ElementExporter;
import hu.elte.txtuml.layout.export.interfaces.NodeMap;
import hu.elte.txtuml.layout.lang.elements.LayoutLink;
import hu.elte.txtuml.layout.lang.elements.LayoutNode;
import hu.elte.txtuml.utils.Pair;

/**
 * 
 * @author G�bor Ferenc Kov�cs
 * @author D�vid J�nos N�meth
 *
 */
public class ClassDiagramExporter {
    
    private final ElementExporter elementExporter;
    private Class<? extends Model> model;
    private List<Class<?>> links; // user defined links in the current model
    
    public ClassDiagramExporter(ElementExporter elementExporter) {
        this.elementExporter = elementExporter;
        this.model = null;
        this.links = new LinkedList<Class<?>>();
    }
	
	public static boolean isNode(Class<?> cls) {
		return ModelClass.class.isAssignableFrom(cls);
	}
	
	public static boolean isLink(Class<?> cls) {
		return Association.class.isAssignableFrom(cls);
	}
	
	public static boolean isModel(Class<?> cls) {
	    return Model.class.isAssignableFrom(cls);
	}

	public static Pair<Class<? extends LayoutNode>, Class<? extends LayoutNode>> startAndEndOfLink(Class<?> link) {
		Class<?>[] classes = link.getDeclaredClasses();
		if (classes.length < 2) {
			return null;
		}
		Class<? extends LayoutNode> end1 = getClassTypeFromAssocEnd(classes[0]);
		Class<? extends LayoutNode> end2 = getClassTypeFromAssocEnd(classes[1]);
		if (end1 == null || end2 == null) {
			return null;
		}
		return new Pair<>(end1, end2);
	}
	
	@SuppressWarnings("unchecked")
    public void exportAssociationsStartingFromThisNode(Class<?> node) {
	    Class<?> declaringClass = node.getDeclaringClass();
        if (!isModel(declaringClass)) {
            // show error
            return;
        }
        
	    if (model == null) {        	                
	        model = (Class<? extends Model>) declaringClass;	        
	        for (Class<?> innerClass : model.getDeclaredClasses()) {
	            if (isLink(innerClass)) {
	                links.add(innerClass);
	            }
	        }         
	    }
	    
        if (declaringClass != model) {
            // show error
            return;
        }
	        
	    NodeMap nodes = elementExporter.getNodes();    
	    for (Class<?> link : links) {
	        Pair<Class<? extends LayoutNode>, Class<? extends LayoutNode>> p = startAndEndOfLink(link);
	        
	        // nodes.containsKey(node) is guaranteed here
            if ((p.getKey().equals(node) && nodes.containsKey(p.getValue())) 
                    || ((p.getValue().equals(node) && nodes.containsKey(p.getKey()))))
            {
                elementExporter.exportLink((Class<? extends LayoutLink>) link);
            }
	    }
	    
	    if (elementExporter.getDiagramTypeBasedOnElements() == DiagramType.Class) {
	        Class<?> base = node.getSuperclass();
	        if (base != null && nodes.containsKey(base)) {
	            elementExporter.exportGeneralization((Class<? extends LayoutNode>) base,
	                    (Class<? extends LayoutNode>) node);
	        } 
	    }
	}
	
	@SuppressWarnings("unchecked")
	private static Class<? extends LayoutNode> getClassTypeFromAssocEnd(Class<?> end) {
		if (!AssociationEnd.class.isAssignableFrom(end)) {
			return null;
		}
		try {
			return (Class<? extends ModelClass>) ((ParameterizedType)end.getGenericSuperclass()).getActualTypeArguments()[0];
		} catch (Exception e) {
		}
		return null;
	}
	
}
