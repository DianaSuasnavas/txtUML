#include "fmi2Functions.h"
#include "fmi2FunctionTypes.h"
#include "fmi2TypesPlatform.h"

#include "FMUEnvironment.hpp"
#include "$fmuclass.hpp"
#include "event.hpp"
#include "deployment.hpp"

#include <iostream>

struct FMU {
    fmu_environment *fmu_env;
    $fmuclass *fmu_class;
    Runtime *uml_rt;
};

struct fmu_variables {
  $valueattributes
};

class fmu_environment : public FMUEnvironment {
public:
  const fmi2CallbackFunctions* callbacks;
  fmu_variables *vars = new fmu_variables;

  bool process_event(EventBaseCRef event) {
    if (event.t == $controlevent_EE) {
      const $controlevent_EC *myevent = static_cast<const $controlevent_EC*>(&event);
      // set the environment variables by name-id pairs
      $setoutputvariables
      callbacks->stepFinished(NULL, fmi2OK);
      return true;
    }
    return false;
  }

  void setInitialState() {
    // nothing to do
  }
  
  fmu_environment() {
    Runtime::createRuntime()->setupObject(this);
  }
  ~fmu_environment() {}

};

size_t member_offsets[] = { $variableoffsets };

const char* fmi2GetTypesPlatform() {
  return "default";
}

const char* fmi2GetVersion() {
  return "2.0";
}

fmi2Status fmi2SetDebugLogging( fmi2Component /* c */,
                                fmi2Boolean /* loggingOn */,
                                size_t /* nCategories */,
                                const fmi2String /*categories*/ [] ) {
  return fmi2OK;
}

fmi2Component fmi2Instantiate( fmi2String /*instanceName*/,
                               fmi2Type /*fmuType*/,
                               fmi2String /*fmuGUID*/,
                               fmi2String /*fmuResourceLocation*/,
                               const fmi2CallbackFunctions* functions,
                               fmi2Boolean /*visible*/,
                               fmi2Boolean /*loggingOn*/ ) {
  FMU* fmu = new FMU;

  // start the runtime
  fmu->uml_rt = deployment::createThreadedRuntime();
  fmu->uml_rt->startRT();

  fmu->fmu_env = new fmu_environment;
  fmu->fmu_env->callbacks = functions;
  fmu->fmu_env->startSM();
  fmu->fmu_class = new $fmuclass(fmu->fmu_env);
  fmu->fmu_class->startSM();
  // TODO: check instance name, GUID
  return fmu;
}

void fmi2FreeInstance(fmi2Component /*c*/) {
  // TODO: safely stop execution
}

fmi2Status fmi2SetupExperiment( fmi2Component /*c*/,
                                fmi2Boolean /*toleranceDefined*/,
                                fmi2Real /*tolerance*/,
                                fmi2Real /*startTime*/,
                                fmi2Boolean /*stopTimeDefined*/,
                                fmi2Real /*stopTime*/ ) {
  return fmi2OK;
}

fmi2Status fmi2EnterInitializationMode(fmi2Component /*c*/) {
  return fmi2OK;
}

fmi2Status fmi2ExitInitializationMode(fmi2Component /*c*/) {
  return fmi2OK;
}

fmi2Status fmi2Terminate(fmi2Component /*c*/) {
  // TODO: safely stop execution
  return fmi2OK;
}

fmi2Status fmi2Reset(fmi2Component /*c*/) {
  // TODO: safely stop execution and prepare for re-initialization
  return fmi2OK;
}

fmi2Status fmi2GetReal ( fmi2Component c, 
                         const fmi2ValueReference vr[],
                         size_t nvr, 
                         fmi2Real value[] ) {
  FMU* fmu = static_cast<FMU*>(c);
  for (size_t i = 0; i < nvr; ++i) {
    if (vr[i] >= sizeof(member_offsets)/sizeof(size_t)) {
      return fmi2Error;
    }
    value[i] = *reinterpret_cast<fmi2Real*>(reinterpret_cast<char*>(fmu->fmu_env->vars) + member_offsets[vr[i]]);
  }
  return fmi2OK;
}

fmi2Status fmi2GetInteger ( fmi2Component c, 
                            const fmi2ValueReference vr[],
                            size_t nvr, 
                            fmi2Integer value[] ) {
  FMU* fmu = static_cast<FMU*>(c);
  for (size_t i = 0; i < nvr; ++i) {
    value[i] = *reinterpret_cast<fmi2Integer*>(reinterpret_cast<char*>(fmu->fmu_env->vars) + member_offsets[vr[i]]);
  }
  return fmi2OK;
}

fmi2Status fmi2GetBoolean ( fmi2Component c, 
                            const fmi2ValueReference vr[],
                            size_t nvr, 
                            fmi2Boolean value[] ) {
  FMU* fmu = static_cast<FMU*>(c);
  for (size_t i = 0; i < nvr; ++i) {
    value[i] = *reinterpret_cast<fmi2Boolean*>(reinterpret_cast<char*>(fmu->fmu_env->vars) + member_offsets[vr[i]]);
  }
  return fmi2OK;
}

fmi2Status fmi2GetString ( fmi2Component c, 
                           const fmi2ValueReference vr[],
                           size_t nvr, 
                           fmi2String value[] ) {
  FMU* fmu = static_cast<FMU*>(c);
  for (size_t i = 0; i < nvr; ++i) {
    value[i] = *reinterpret_cast<fmi2String*>(reinterpret_cast<char*>(fmu->fmu_env->vars) + member_offsets[vr[i]]);
  }
  return fmi2OK;
}

fmi2Status fmi2SetReal ( fmi2Component c, 
                         const fmi2ValueReference vr[],
                         size_t nvr, 
                         const fmi2Real value[] ) {
  FMU* fmu = static_cast<FMU*>(c);
  for (size_t i = 0; i < nvr; ++i) {
    *reinterpret_cast<fmi2Real*>(reinterpret_cast<char*>(fmu->fmu_env->vars) + member_offsets[vr[i]]) = value[i];
  }
  return fmi2OK;
}

fmi2Status fmi2SetInteger ( fmi2Component c, 
                            const fmi2ValueReference vr[],
                            size_t nvr, 
                            const fmi2Integer value[] ) {
  FMU* fmu = static_cast<FMU*>(c);
  for (size_t i = 0; i < nvr; ++i) {
    *reinterpret_cast<fmi2Integer*>(reinterpret_cast<char*>(fmu->fmu_env->vars) + member_offsets[vr[i]]) = value[i];
  }
  return fmi2OK;}

fmi2Status fmi2SetBoolean ( fmi2Component c, 
                            const fmi2ValueReference vr[],
                            size_t nvr, 
                            const fmi2Boolean value[] ) {
  FMU* fmu = static_cast<FMU*>(c);
  for (size_t i = 0; i < nvr; ++i) {
    *reinterpret_cast<fmi2Boolean*>(reinterpret_cast<char*>(fmu->fmu_env->vars) + member_offsets[vr[i]]) = value[i];
  }
  return fmi2OK;
}

fmi2Status fmi2SetString ( fmi2Component c, 
                           const fmi2ValueReference vr[],
                           size_t nvr, 
                           const fmi2String value[] ) {
  FMU* fmu = static_cast<FMU*>(c);
  for (size_t i = 0; i < nvr; ++i) {
    *reinterpret_cast<fmi2String*>(reinterpret_cast<char*>(fmu->fmu_env->vars) + member_offsets[vr[i]]) = value[i];
  }
  return fmi2OK;
}

fmi2Status fmi2GetFMUstate (fmi2Component /*c*/, fmi2FMUstate* /*FMUstate*/) {
  return fmi2Error; // not supported
}

fmi2Status fmi2SetFMUstate (fmi2Component /*c*/, fmi2FMUstate /*FMUstate*/) {
  return fmi2Error; // not supported
}

fmi2Status fmi2FreeFMUstate (fmi2Component /*c*/, fmi2FMUstate* /*FMUstate*/) {
  return fmi2Error; // not supported
}

fmi2Status fmi2SerializedFMUstateSize(fmi2Component /*c*/, fmi2FMUstate /*FMUstate*/, size_t */*size*/) {
  return fmi2Error; // not supported
}

fmi2Status fmi2SerializeFMUstate ( fmi2Component /*c*/, 
                                   fmi2FMUstate /*FMUstate*/,
                                   fmi2Byte /*serializedState*/[], 
                                   size_t /*size*/ ) {
  return fmi2Error; // not supported
}

fmi2Status fmi2DeSerializeFMUstate ( fmi2Component /*c*/,
                                     const fmi2Byte /*serializedState*/[],
                                     size_t /*size*/, 
                                     fmi2FMUstate* /*FMUstate*/ ) {
  return fmi2Error; // not supported
}

fmi2Status fmi2GetDirectionalDerivative( fmi2Component /*c*/,
                                         const fmi2ValueReference /*vUnknown_ref*/[],
                                         size_t /*nUnknown*/,
                                         const fmi2ValueReference /*vKnown_ref*/[],
                                         size_t /*nKnown*/,
                                         const fmi2Real /*dvKnown*/[],
                                         fmi2Real /*dvUnknown*/[] ) {
  return fmi2Error; // not supported
}

fmi2Status fmi2SetRealInputDerivatives( fmi2Component /*c*/,
                                        const fmi2ValueReference /*vr*/[],
                                        size_t /*nvr*/,
                                        const fmi2Integer /*order*/[],
                                        const fmi2Real /*value*/[] ) {
  return fmi2Error; // not supported
}

fmi2Status fmi2GetRealOutputDerivatives ( fmi2Component /*c*/,
                                          const fmi2ValueReference /*vr*/[],
                                          size_t /*nvr*/,
                                          const fmi2Integer /*order*/[],
                                          fmi2Real /*value*/[] ) {
  return fmi2Error; // not supported
}

fmi2Status fmi2DoStep( fmi2Component c,
                       fmi2Real /*currentCommunicationPoint*/,
                       fmi2Real /*communicationStepSize*/,
                       fmi2Boolean /*noSetFMUStatePriorToCurrentPoint*/) {
  FMU* fmu = static_cast<FMU*>(c);
  ControlCycleSignal_EC* sig = new ControlCycleSignal_EC(fmu->fmu_env->vars->h, fmu->fmu_env->vars->v);
  fmu->fmu_class->send(std::shared_ptr<ControlCycleSignal_EC>(sig));
  return fmi2Pending;
}

fmi2Status fmi2CancelStep(fmi2Component /*c*/) {
  // TODO: terminate
  return fmi2OK;
}

fmi2Status fmi2GetStatus ( fmi2Component /*c*/, const fmi2StatusKind /*s*/, fmi2Status* /*value*/ ) {
  // TODO: get status
  return fmi2OK;
}

fmi2Status fmi2GetRealStatus (fmi2Component /*c*/, const fmi2StatusKind /*s*/, fmi2Real* /*value*/) {
  // TODO: get status
  return fmi2OK;
}

fmi2Status fmi2GetIntegerStatus(fmi2Component /*c*/, const fmi2StatusKind /*s*/, fmi2Integer* /*value*/) {
  // TODO: get status
  return fmi2OK;
}

fmi2Status fmi2GetBooleanStatus(fmi2Component /*c*/, const fmi2StatusKind /*s*/, fmi2Boolean* /*value*/) {
  // TODO: get status
  return fmi2OK;
}

fmi2Status fmi2GetStringStatus (fmi2Component /*c*/, const fmi2StatusKind /*s*/, fmi2String* /*value*/) {
  // TODO: get status
  return fmi2OK;
}