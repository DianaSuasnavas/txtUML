package hu.elte.txtuml.export.cpp.templates;

class GenerationNames {
	public static final String EventHeaderName = "event";
	public static final String EventBaseName = "EventBase";
	public static final String EventBaseRefName = EventBaseName + "CRef";
	public static final String EventsEnumName = "Events";
	public static final String NoReturn = "void";
	public static final String HeaderExtension = "hpp";
	public static final String SourceExtension = "cpp";
	public static final String ClassType = "struct";

	// NDEBUG is the only thing guaranteed, DEBUG and _DEBUG is non-standard
	public static final String NoDebugSymbol = "NDEBUG";
	public static final String StandardIOinclude = "#include <iostream>\n";
	
	public static final String StandardLibaryFunctionsHeaderName = "standard_functions";

	public static final String NullPtr = "nullptr";
	public static final String Self = "this";
	public static final String MemoryAllocator = "new";
	public static final String PointerAccess = "->";
	public static final String SimpleAccess = ".";
	public static final String DeleteObject = "delete";
	public static final String cppString = "std::string";
	public static final String IncomingParamTypeId = "_";
	public static final String RealEventName = "realEvent";
	public static final String SmartPtr = "std::shared_ptr";
	public static final String EventClassTypeId = "_EC";
	public static final String EventEnumTypeId = "_EE";
	public static final String StateEnumTypeId = "_ST";
	public static final String EntryName = "Entry";
	public static final String ExitName = "Exit";
	public static final String EventParamName = "e";
	public static final String EventFParamName = formatIncomingParamName(EventParamName);
	public static final String StateParamName = "s_";
	public static final String TransitionTableName = "_mM";
	public static final String setStateFuncName = "setState";
	public static final String CurrentStateName = "_cS";
	public static final String DefaultInvalidState = "-1";
	public static final String DefaultStateInitialization = CurrentStateName + "(" + DefaultInvalidState + ")";
	public static final String FunctionPtrTypeName = "ActionFuncType";
	public static final String GuardFuncTypeName = "GuardFuncType";
	public static final String GuardActionName = "GuardAction";
	public static final String EventStateTypeName = "EventState";
	public static final String ProcessEventFName = "process_event";
	public static final String ProcessEventDeclShared = "bool " + ProcessEventFName + "(" + EventBaseRefName + " "
			+ EventFParamName + ")";
	public static final String ProcessEventDecl = ProcessEventDeclShared + ";\n";
	public static final String TransitionTable = "std::unordered_multimap<" + EventStateTypeName + "," + GuardActionName
			+ "> " + TransitionTableName + ";\n";
	public static final String SetStateDecl = NoReturn + " " + setStateFuncName + "(int "
			+ GenerationNames.StateParamName + ");\n";
	public static final String SetInitialStateName = "setInitialState";
	public static final String SetInitialStateDecl = NoReturn + " " + SetInitialStateName + "();";
	public static final String StatemachineBaseName = "StateMachineBase";
	public static final String StatemachineBaseHeaderName = "statemachinebase";
	public static final String DefaultGuardName = "defaultGuard";
	public static final String DummyProcessEventDef = ProcessEventDeclShared + "{return false;}\n";
	public static final String StartSmMethodName = "startSM";

	// hierarchical state machine
	public static final String ParentSmPointerName = "_parentSm";
	public static final String CompositeStateMapName = "_compSates";
	public static final String CurrentMachineName = "_cM";
	public static final String CompositeStateMapSmType = SmartPtr + "<" + StatemachineBaseName + ">";
	public static final String CompositeStateMap = "std::unordered_map<int," + CompositeStateMapSmType + " > "
			+ CompositeStateMapName + ";\n";
	public static final String CurrentMachine = pointerType(StatemachineBaseName) + " " + CurrentMachineName + ";\n";
	public static final String ActionCallerFName = "action_caller";
	public static final String ActionCallerDecl = "bool " + ActionCallerFName + "(" + EventBaseRefName + " "
			+ EventFParamName + ");\n";
	public static final String ParentSmName = "pSm";
	public static final String ParentSmMemberName = "_" + ParentSmName;

	public static final String Unknown = "?";
	public static final String AssocMultiplicityDataStruct = "std::list";
	public static final String InitStateMachine = "initStateMachine";

	//
	public static final String PoolIdSetter = "setPoolId";
	public static final String InitialEventName = "InitSignal";
	public static final String SendSignal = "send";
	

	public static String friendClassDecl(String className) {
		return "friend " + GenerationNames.ClassType + " " + className + ";\n";
	}

	public static final String parentSmPointerNameDef(String parentType) {
		return pointerType(parentType) + " " + ParentSmPointerName;
	}

	public static String actionCallerDef(String className) {
		return "bool " + className + "::" + ActionCallerFName + "(" + EventBaseRefName + " " + EventFParamName + ")\n"
				+ simpleProcessEventDefBody();
	}

	public static String simpleProcessEventDef(String className) {
		return "bool " + className + "::" + ProcessEventFName + "(" + EventBaseRefName + " " + EventFParamName + ")\n"
				+ simpleProcessEventDefBody();
	}

	public static String hierachicalProcessEventDef(String className) {
		return "bool " + className + "::" + ProcessEventFName + "(" + EventBaseRefName + " " + EventFParamName + ")\n"
				+ "{\n" + "bool handled=false;\n" + "if(" + CurrentMachineName + ")\n" + "{\n" + "if("
				+ CurrentMachineName + "->" + ProcessEventFName + "(" + EventFParamName + "))\n" + "{\n"
				+ "handled=true;\n" + "}\n" + "}\n" + "if(!handled)\n" + "{\n" + "handled=handled || "
				+ ActionCallerFName + "(" + EventFParamName + ");\n" + "}\n//else unhandled event in this state\n"
				+ "return handled;\n" + "}\n";
	}

	private static final String simpleProcessEventDefBody() {
		return "{\n" + "bool handled=false;\n" + "auto range = " + TransitionTableName + ".equal_range(EventState("
				+ EventFParamName + ".t," + CurrentStateName + "));\n" + "if(range.first!=" + TransitionTableName
				+ ".end())\n" + "{\n" + "for(auto it=range.first;it!=range.second;++it)\n" + "{\n"
				+ "if((it->second).first(*this," + EventFParamName + "))//Guard call\n" + "{\n" + ExitName + "();\n"
				+ "(it->second).second(*this," + EventFParamName + ");//Action Call\n" + "handled=true;\n" + "break;\n"
				+ "}\n" + "}\n" + "}\n" + "return handled;\n" + "}\n";
	}

	public static String simpleSetStateDef(String className) {
		return NoReturn + " " + className + "::" + setStateFuncName + "(int " + GenerationNames.StateParamName + "){"
				+ CurrentStateName + "=" + GenerationNames.StateParamName + ";" + EntryName + "();}\n";
	}

	public static String hierachicalSetStateDef(String className) {
		return NoReturn + " " + className + "::" + setStateFuncName + "(int " + GenerationNames.StateParamName + ")\n"
				+ "{\n" + "auto it=" + CompositeStateMapName + ".find(" + GenerationNames.StateParamName + ");\n"
				+ "if(it!=" + CompositeStateMapName + ".end())\n" + "{\n" + CurrentMachineName
				+ "=(it->second).get();\n" + CurrentMachineName + "->" + SetInitialStateName
				+ "();//restarting from initial state\n" + CurrentMachineName + "->" + ProcessEventFName + "("
				+ GenerationNames.InitialEventName + "_EC());\n" + "}\n" + "else\n" + "{\n"
				+ CurrentMachineName + "=" + NullPtr + ";\n" + "}\n" + CurrentStateName + "="
				+ GenerationNames.StateParamName + ";\n" + EntryName + "();\n" + "}\n";
	}

	public static String eventClassName(String eventName) {
		return eventName + EventClassTypeId;
	}

	public static String eventEnumName(String eventName) {
		return eventName + EventEnumTypeId;
	}

	public static String stateEnumName(String stateName) {
		return stateName + StateEnumTypeId;
	}

	public static String derefenrencePointer(String pointer) {
		return "(*" + pointer + ")";
	}

	public static String pointerType(String typeName) {
		return typeName + "*";
	}

	public static String formatIncomingParamName(String paramName) {
		return paramName + IncomingParamTypeId;
	}
}
