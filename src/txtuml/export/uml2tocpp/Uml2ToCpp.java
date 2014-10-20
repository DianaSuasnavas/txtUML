package txtuml.export.uml2tocpp;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.SignalEvent;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.Vertex;

import txtuml.export.uml2tocpp.Util.Pair;


public class Uml2ToCpp {

		public static void main(String[] arg) {
			/*if(args.length != 2) {
				System.out.println("Two command line arguments needed.");
				return;
			}*/

			try {
				String args[] ={"import/Model1.uml","export/"};
				Model model= Util.loadModel(args[0]);
				EList<Element> elements=model.allOwnedElements();
				
				String source=createEventSource(elements);
				writeOutSource(args[1],(GenerationTemplates.EventHeaderName + ".hh"),source);

				
				List<Class> classList=new ArrayList<Class>();
				getTypedElements(classList,elements,UMLPackage.Literals.CLASS);
				for(Class item:classList)
				{
					source="";
					source=createClassHeaderSource(item);
					writeOutSource(args[1],item.getName()+".hh", source);
					source="";
					source=createClassCppSource(item);
					writeOutSource(args[1],item.getName()+".cpp","#include \""+item.getName()+".hh\"\n\n"+source);
				}
	            
			} catch(IOException ioe) {
				System.out.println("IO error.");
	        }
		}
		
		private static String createClassHeaderSource(Class class_) {
			String source="";
			List<StateMachine> smList=new ArrayList<StateMachine>();
			getTypedElements(smList,class_.allOwnedElements(),UMLPackage.Literals.STATE_MACHINE);
			if(!smList.isEmpty())
			{
				source=GenerationTemplates.StateMachineClassHeader(class_.getName(), createParts(class_,"public")+
																   GenerationTemplates.StateEnum(getStateList(smList.get(0)))+
																   GenerationTemplates.EventEnum(getEventList(smList.get(0))),
																   createTransitionFunctionDecl(smList.get(0))+createParts(class_,"private"));
			}
			else
			{
				source=GenerationTemplates.ClassHeader(class_.getName(), createParts(class_,"public"), createParts(class_,"private"));
			}
			return source;
		}
		
		private static String createTransitionFunctionDecl(StateMachine machine_) 
		{
			String source="";
			EList<Region> regions=machine_.getRegions();
			Region r=regions.get(0);
			EList<Transition> transitions=r.getTransitions();
			for(Transition item:transitions)
			{
				source+=GenerationTemplates.TransitionActionDecl(item.getName());
			}
			
			return source+"\n";
		}
		
		private static String createTransitionFunctionsDef(String className_,StateMachine machine_) 
		{
			String source="";
			EList<Region> regions=machine_.getRegions();
			Region r=regions.get(0);
			EList<Transition> transitions=r.getTransitions();
			for(Transition item:transitions)
			{
				String body="";
				Behavior b=item.getEffect();
				if(b != null && b.eClass().equals(UMLPackage.Literals.ACTIVITY))
				{
					Activity a=(Activity)b;
					body=createfunctionBody(a);
				}
				source+=GenerationTemplates.TransitionActionDef(className_,item.getName(),body+GenerationTemplates.setState(item.getTarget().getName())+"\n");
			}
			
			return source+"\n";
		}


		private static String createfunctionBody(Activity activity_) {
			String source="";
			//el kell maj indulni a start node-t�l �s addig konvert�lni am�g el nem �rj�k az end node-ot
			activity_.getNodes();
			activity_.getEdges();
			//
			return source;
		}

		private static String createParts(Class class_,String modifyer_)
		{
			String source="";
			EList<Operation> operations=class_.getAllOperations();
			for(Operation item:operations)
			{
				if(item.getVisibility().toString().equals(modifyer_))
				{
					String returnType="void";
					if(item.getReturnResult() != null)
					{
						returnType=item.getReturnResult().getType().getName();
					}
					source+=GenerationTemplates.FunctionDecl(returnType, item.getName(),operationParamTypes(item));
				}
			}
			
			EList<Property> propertis=class_.getAttributes();
			for(Property item:propertis)
			{
				if(item.getVisibility().toString().equals(modifyer_))
				{
					source+=GenerationTemplates.Property(item.getType().getName(),item.getName());
				}
			}
			
			return source;
		}
		
		private static List<String> operationParamTypes(Operation op_)//ki kell szedni a param typest
		{
			List<String> ret=new ArrayList<String>();
			return ret;
		}
		
		private static List<Util.Pair<String,String>> operationParams(Operation op_)
		{
			List<Util.Pair<String,String>> ret=new ArrayList<Util.Pair<String,String>>();
			return ret;
		}
		
		private static String createClassCppSource(Class class_) {
			String source="";
			List<StateMachine> smList=new ArrayList<StateMachine>();
			getTypedElements(smList,class_.allOwnedElements(),UMLPackage.Literals.STATE_MACHINE);
			if(!smList.isEmpty())
			{
				StateMachine sm=smList.get(0);
				int eventNum=getEventList(sm).size();//pazarl�s
				int stateNum=getStateList(sm).size();//pazarl�s
				Map<Util.Pair<String,String>,String> smMap=createMachine(sm);
				source+=GenerationTemplates.StateMachineClassConstructor(class_.getName(),smMap, eventNum, stateNum);
				
				source+=createTransitionFunctionsDef(class_.getName(),smList.get(0));
				
			}
			
			EList<Operation> operations=class_.getAllOperations();
			for(Operation item:operations)
			{
				String returnType="void";
				if(item.getReturnResult() != null)
				{
					returnType=item.getReturnResult().getType().getName();
				}
				source+=GenerationTemplates.FunctionDef(class_.getName(),returnType, item.getName(),operationParams(item),"");
			}
			
			return source;
		}
		
		/*
		 * Map<Util.Pair<String,String>,String>
		 * event,state,handlerName
		 * */
		private static Map<Util.Pair<String,String>,String> createMachine(StateMachine machine_)
		{
			Map<Util.Pair<String,String>,String> smMap=new HashMap<Util.Pair<String,String>,String>();
			
			EList<Region> regions=machine_.getRegions();
			Region r=regions.get(0);
			EList<Transition> transitions=r.getTransitions();
			for(Transition item:transitions)
			{
				Util.Pair<String,String> eventSignalPair=null;
				for(Trigger tri:item.getTriggers())
				{
					Event e=tri.getEvent();
					if(e != null && e.eClass().equals(UMLPackage.Literals.SIGNAL_EVENT))
					{
						SignalEvent se=(SignalEvent)e;
						if(se != null)
						{
							eventSignalPair=new Util.Pair<String,String> (se.getSignal().getName(),item.getSource().getName());
						}
					}
				}
				if(eventSignalPair != null)
				{
					smMap.put(eventSignalPair,item.getName());
				}
			}
			return smMap;
		}
		
		private static List<State> getStateList(StateMachine machine_)
		{
			List<State> stateList=new ArrayList<State>();
			EList<Region> regions=machine_.getRegions();
			Region r=regions.get(0);
			EList<Vertex> states=r.getSubvertices();
			for(Vertex item:states)
			{
				if(item.eClass().equals(UMLPackage.Literals.STATE))
				{
					stateList.add((State)item);
				}
			}
			return stateList;
		}
		
		private static List<SignalEvent> getEventList(StateMachine machine_)
		{
			List<SignalEvent> eventList=new ArrayList<SignalEvent>();
			EList<Region> regions=machine_.getRegions();
			Region r=regions.get(0);
			EList<Transition> transitions=r.getTransitions();
			for(Transition item:transitions)
			{
					for(Trigger tri:item.getTriggers())
					{
						Event e=tri.getEvent();
						if(e != null && e.eClass().equals(UMLPackage.Literals.SIGNAL_EVENT))
						{
							SignalEvent se=(SignalEvent)e;
							if(se != null && ! eventList.contains(se))
							{
								eventList.add(se);
							}
						}
					}
			}
			return eventList;
		}
		
		//param�teres signal kezel�s m�g nincs
		private static String createEventSource(EList<Element> elements_)
		{
			List<Signal> signalList=new ArrayList<Signal>();
			getTypedElements(signalList,elements_,UMLPackage.Literals.SIGNAL);
			String source = "";
			
			for(Signal item:signalList)
			{
				source+=GenerationTemplates.EventClass(item.getName(),new ArrayList<Util.Pair<String,String>>());//ki kell majd szedni a param�tereket
			}
			
			return GenerationTemplates.HeaderGuard(source,GenerationTemplates.EventHeaderName);
		}
		
		
		private static <ElementType,EClassType> 
		void getTypedElements(Collection<ElementType> dest_,Collection<Element> source_,EClassType eClass_)
		{
			for(Element item:source_)
			{
				if(item.eClass().equals(eClass_))
				{
					dest_.add((ElementType)item);//it is safe ...
				}
			}
		} 
		
		
		private static void writeOutSource(String path_,String fileName_,String source_) throws FileNotFoundException, UnsupportedEncodingException
		{
			PrintWriter writer = new PrintWriter(path_+fileName_, "UTF-8");
            writer.println(source_);
            writer.close();
		}
}
