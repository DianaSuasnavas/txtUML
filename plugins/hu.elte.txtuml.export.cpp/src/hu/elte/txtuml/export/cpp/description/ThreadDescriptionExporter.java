package hu.elte.txtuml.export.cpp.description;


import hu.elte.txtuml.api.model.ModelClass;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ThreadDescriptionExporter {
	
	private Map<String, ThreadPoolConfiguration > configMap;
	private boolean descriptionExported = false;
	private boolean isMultiThreading = true;;
	
	List<String> warningList;
	List<String> errorList;
	
	
	int numberOfConfigurations;
	
	Set<String> exportedClasses;
	
	
	public ThreadDescriptionExporter(){
		configMap = new HashMap<String, ThreadPoolConfiguration >();
		exportedClasses = new HashSet<String>();
		numberOfConfigurations = 0;
	}
	
	
	
	public Map<String, ThreadPoolConfiguration > getConfigMap(){
		return configMap;
	}
	
	public void exportDescription(Class<? extends Configuration> description){
		
		if(descriptionExported) return;
		
		for(Annotation annotaion: description.getAnnotations() ){
			if(annotaion instanceof GroupContainer){
				
				for(Annotation annotation: ((GroupContainer) annotaion).value()){
					numberOfConfigurations = numberOfConfigurations + 1;
					
					Group g = (Group) annotation;
					ThreadPoolConfiguration config = new ThreadPoolConfiguration(numberOfConfigurations, g.gradient(), g.constant());
					config.setMaxThreads(g.max());
	
					
					checkEmptyGroup(g.contains());
					
					for(Class<? extends ModelClass> cls: g.contains()){
						if(!exportedClasses.contains(cls.getSimpleName())){
							configMap.put(cls.getSimpleName(), config);
						}
						else{
							warningList.add(cls.getSimpleName() + " configured more times!");
						}
						
					}
				}
			}
			else if(annotaion instanceof Multithreading){
				Multithreading mlt = (Multithreading) annotaion;
				if(mlt.value()){
					isMultiThreading = true;
				}
				else{
					isMultiThreading = false;
				}
			}
			else{
				warningList.add("Unknown annotaiton detected!");
			}
		}
		
		descriptionExported = true;
		
	}
	
	public boolean successfulExportation(){
		if(!descriptionExported){
			return false;
		}
		else{
			return errorList.isEmpty();
		}
	}
	
	public List<String> getErrors(){
		return errorList;
	}
	
	public List<String> getWarnings(){
		return warningList;
	}
	
	public boolean isMultiThreading() {
		return isMultiThreading;
	}

	private void checkEmptyGroup(Class<? extends ModelClass>[] classes) {
		if(classes.length == 0){
			warningList.add("Empty Serves annotation");
		}
		
	}
}
