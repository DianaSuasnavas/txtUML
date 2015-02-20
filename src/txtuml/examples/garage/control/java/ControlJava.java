package txtuml.examples.garage.control.java;

import java.util.Timer;
import java.util.TimerTask;

import txtuml.examples.garage.interfaces.IControl;
import txtuml.examples.garage.interfaces.IControlled;

public class ControlJava implements IControl {
	static IControlled controlled;
	enum DoorSecurityState {Disabled, Enabled};
	static DoorSecurityState doorSecurityState = DoorSecurityState.Enabled;
	enum DoorState {HeadingUp, HeadingDown, Up, Down};
	static DoorState doorState = DoorState.HeadingUp;
	static Timer doorSecurityTimer = new Timer();
	enum AlarmState {Idle, Alarmed, CodeExpected, InAlarm, OldCodeExpected, NewCodeExpected};
	static AlarmState alarmState = AlarmState.Alarmed;
	static private int code = 8;
	static Timer alarmTimer = new Timer();
	static int alarmDelay = 5000;
	static int alarmDelayInterval = 50;

	public ControlJava(IControlled ctd) {
		controlled = ctd;
	}
	
	@Override
	public void remoteControlButtonPressed() {
		if(doorSecurityState == DoorSecurityState.Enabled) {
			switch(doorState) {
			case HeadingUp:
				doorState = DoorState.Up;
				controlled.startDoorUp();
				break;
			case HeadingDown:
				doorState = DoorState.Down;
				controlled.startDoorDown();
				break;
			case Up:
				doorState = DoorState.HeadingDown;
				controlled.stopDoor();
				break;
			case Down:
				doorState = DoorState.HeadingUp;
				controlled.stopDoor();
				break;
			}
		}
	}

	public void motionSensorActivated() {
		controlled.stopDoor();
		switch(doorState) {
		case Up:
		case Down:
			doorSecurityState = DoorSecurityState.Disabled;
			doorSecurityTimer.cancel();
			doorSecurityTimer = new Timer();
			doorSecurityTimer.schedule(new Enable(this), 2000);
			break;
		default:
		}
	}
	
	public void doorReachedTop() {
		controlled.stopDoor();
		doorState = DoorState.HeadingDown;
	}
	
	public void doorReachedBottom() {
		controlled.stopDoor();
		doorState = DoorState.HeadingUp;
		if(alarmState == AlarmState.Idle) {
			alarmState = AlarmState.Alarmed;
			controlled.alarmOn();
		}
	}

	public void keyPress(int nr) {
		if(alarmState == AlarmState.CodeExpected || alarmState == AlarmState.Alarmed) {
			alarmTimer.cancel();
			alarmTimer = new Timer();
			if(nr == code) {
				alarmState = AlarmState.Idle;
				controlled.alarmOff();
			} else {
				alarmState = AlarmState.InAlarm;
				controlled.startSiren();
				controlled.codeExpected();
			}
		} else if(alarmState == AlarmState.InAlarm) {
			if(nr == code) {
				alarmState = AlarmState.Idle;
				controlled.stopSiren();
				controlled.alarmOff();
			}
		} else if(alarmState == AlarmState.OldCodeExpected) {
			alarmTimer.cancel();
			alarmTimer = new Timer();
			if(nr == code) {
				alarmState = AlarmState.NewCodeExpected;
				alarmTimer.scheduleAtFixedRate(new CodeDelay(this), alarmDelayInterval, alarmDelayInterval);
			} else {
				alarmState = AlarmState.Idle;
				controlled.alarmOff();
			}			
		} else if(alarmState == AlarmState.NewCodeExpected) {
			alarmTimer.cancel();
			alarmTimer = new Timer();
			code = nr;
			alarmState = AlarmState.Idle;
			controlled.alarmOff();
		}
	}

	public void starPressed() {
		if(alarmState == AlarmState.Idle) {
			alarmState = AlarmState.Alarmed;
			controlled.alarmOn();
		}
	}
	
	public void hashPressed() {
		if(alarmState == AlarmState.Idle) {
			alarmState = AlarmState.OldCodeExpected;
			alarmTimer.scheduleAtFixedRate(new CodeDelay(this), alarmDelayInterval, alarmDelayInterval);			
		}
	}
	
	public void alarmSensorActivated() {
		if(alarmState == AlarmState.Alarmed) {
			alarmState = AlarmState.CodeExpected;
			alarmTimer.scheduleAtFixedRate(new CodeDelay(this), alarmDelayInterval, alarmDelayInterval);
		}
	}
	
	class Enable extends TimerTask {
		@SuppressWarnings("unused") // TODO check warning
		private ControlJava parent;
		
		Enable(ControlJava p) {
			parent = p;
		}
		
		public void run() {
			if(ControlJava.doorSecurityState == DoorSecurityState.Disabled) {
				ControlJava.doorSecurityState = DoorSecurityState.Enabled;
				switch(ControlJava.doorState) {
				case Down:
					ControlJava.controlled.startDoorDown();
					break;
				case Up:
					ControlJava.controlled.startDoorUp();
					break;
				default:
				}
			}
		}
	}
	
	class CodeDelay extends TimerTask {
		private int elapsed = 0;
		@SuppressWarnings("unused") // TODO check warning
		private ControlJava parent;
		
		CodeDelay(ControlJava p) {
			parent = p;
		}
		
		@Override
		public void run() {
			elapsed += ControlJava.alarmDelayInterval;
			elapsed = Math.min(elapsed,ControlJava.alarmDelay);
			ControlJava.controlled.progress(100*elapsed/ControlJava.alarmDelay);
			if(alarmState == AlarmState.CodeExpected) {
				ControlJava.controlled.codeExpected();
			} else if(alarmState == AlarmState.OldCodeExpected) {
				ControlJava.controlled.oldCodeExpected();
			} else if(alarmState == AlarmState.NewCodeExpected) {
				ControlJava.controlled.newCodeExpected();
			}
			if(elapsed >= ControlJava.alarmDelay) {
				ControlJava.alarmTimer.cancel();
				ControlJava.alarmTimer = new Timer();
				if(ControlJava.alarmState == AlarmState.CodeExpected) {
					ControlJava.alarmState = AlarmState.InAlarm;
					ControlJava.controlled.startSiren();
				} else if(ControlJava.alarmState == AlarmState.OldCodeExpected
					   || ControlJava.alarmState == AlarmState.NewCodeExpected) {
					ControlJava.alarmState = AlarmState.Idle;
					ControlJava.controlled.alarmOff();
				}
			}
		}	
	}
}
