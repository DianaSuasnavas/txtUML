package hu.elte.txtuml.examples.microwave;

import hu.elte.txtuml.api.model.Action;
import hu.elte.txtuml.api.model.Association;
import hu.elte.txtuml.api.model.From;
import hu.elte.txtuml.api.model.Model;
import hu.elte.txtuml.api.model.ModelClass;
import hu.elte.txtuml.api.model.ModelExecutor;
import hu.elte.txtuml.api.model.Signal;
import hu.elte.txtuml.api.model.To;
import hu.elte.txtuml.api.model.Trigger;
import hu.elte.txtuml.api.stdlib.Timer;
import hu.elte.txtuml.examples.microwave.MicrowaveModel.Close;
import hu.elte.txtuml.examples.microwave.MicrowaveModel.Food;
import hu.elte.txtuml.examples.microwave.MicrowaveModel.Get;
import hu.elte.txtuml.examples.microwave.MicrowaveModel.Human;
import hu.elte.txtuml.examples.microwave.MicrowaveModel.Open;
import hu.elte.txtuml.examples.microwave.MicrowaveModel.Put;
import hu.elte.txtuml.examples.microwave.MicrowaveModel.SetIntensity;
import hu.elte.txtuml.examples.microwave.MicrowaveModel.SetTime;
import hu.elte.txtuml.examples.microwave.MicrowaveModel.Start;
import hu.elte.txtuml.examples.microwave.MicrowaveModel.Stop;
import hu.elte.txtuml.examples.microwave.MicrowaveModel.Usage;

class MicrowaveModel extends Model
{
	
	// Classes
	class Consumable extends ModelClass
	{
		int timeNeeded;
		int timeToExplode;
		int heatLevel;
	}
	
	class Food extends Consumable
	{
	}
	
	class Drink extends Consumable
	{
	}
	
	class Microwave extends ModelClass
	{
		int intensity = 1;
		int time = 0;
		Consumable content;
		
		class Init extends Initial
		{
		}
		
		class Opened extends CompositeState
		{
			@Override
			public void entry()
			{
				Action.log("Microwave: enterring idle opened. Resetting values.");
				intensity = 1;
				time = 0;
			}
			
			class Init extends Initial
			{
			}
			
			class HasContent extends State
			{
				@Override
				public void entry()
				{
					Action.log("Microwave: enterring full opened state.");
				}
			}
			
			class HasNoContent extends State
			{
				@Override
				public void entry()
				{
					Action.log("Microwave: enterring empty opened state.");
				}
			}
			
			@From(Init.class)
			@To(HasContent.class)
			class Initialize1 extends Transition
			{
				@Override
				public boolean guard()
				{
					return content != null;
				}
			}
			
			@From(Init.class)
			@To(HasNoContent.class)
			class Initialize2 extends Transition
			{
				@Override
				public boolean guard()
				{
					return content == null;
				}
			}
			
			@From(HasContent.class)
			@To(HasNoContent.class)
			@Trigger(Get.class)
			class GetContent extends Transition
			{
				@Override
				public void effect()
				{
					content = null;
					Action.log("Microwave: content removed.");
				}
			}
			
			@From(HasNoContent.class)
			@To(HasContent.class)
			@Trigger(Put.class)
			class PutContent extends Transition
			{
				@Override
				public void effect()
				{
					content = getSignal(Put.class).item;
					Action.log("Microwave: content put in.");
				}
			}
		}
		
		class Closed extends CompositeState
		{
			@Override
			public void entry()
			{
				Action.log("Microwave: enterring idle closed.");
			}
			
			class Init extends Initial
			{
			}
			
			class HasContent extends State
			{
				@Override
				public void entry()
				{
					Action.log("Microwave: enterring full closed state.");
				}
			}
			
			class HasNoContent extends State
			{
				@Override
				public void entry()
				{
					Action.log("Microwave: enterring empty closed state.");
				}
			}
			
			@From(Init.class)
			@To(HasContent.class)
			class Initialize1 extends Transition
			{
				@Override
				public boolean guard()
				{
					return content != null;
				}
			}
			
			@From(Init.class)
			@To(HasNoContent.class)
			class Initialize2 extends Transition
			{
				@Override
				public boolean guard()
				{
					return content == null;
				}
			}
			
			@From(HasContent.class)
			@To(HasContent.class)
			@Trigger(SetIntensity.class)
			class AdjustIntensity extends Transition
			{
				@Override
				public void effect()
				{
					intensity = getSignal(SetIntensity.class).value;
					Action.log("Microwave: intensity set.");
				}
			}
			
			@From(HasContent.class)
			@To(HasContent.class)
			@Trigger(SetTime.class)
			class AdjustTime extends Transition
			{
				@Override
				public void effect()
				{
					time = getSignal(SetTime.class).value;
					Action.log("Microwave: time set.");
				}
			}
		}
		
		@From(Opened.class)
		@To(Closed.class)
		@Trigger(Close.class)
		class CloseDoor extends Transition
		{
		}
		
		@From(Closed.class)
		@To(Opened.class)
		@Trigger(Open.class)
		class OpenDoor extends Transition
		{
		}
		
		class Heating extends CompositeState
		{
			@Override
			public void entry()
			{
				Action.log("Microwave: enterring heating state.");
			}
			
			class Init extends Initial
			{
			}
			
			class Working extends State
			{
				@Override
				public void entry()
				{
					--time;
					content.heatLevel += intensity;
					Action.log("Microwave: remaining time: " + time + " second(s).");
					
					Timer.start(Microwave.this, new TimedOut(), 1000);
				}
			}
			
			class Finished extends State
			{
				@Override
				public void entry()
				{
					Action.log("Microwave: DING!");
					Human h = Microwave.this.assoc(Usage.userOfMicrowave.class)
							.selectAny();
					Action.send(h, new Ding());
				}
			}
			
			@From(Init.class)
			@To(Working.class)
			class Initialize extends Transition
			{
			}
			
			@From(Working.class)
			@To(Working.class)
			@Trigger(TimedOut.class)
			class Loop extends Transition
			{
				@Override
				public boolean guard()
				{
					return time > 0;
				}
				
				@Override
				public void effect()
				{
					
				}
			}
			
			@From(Working.class)
			@To(Finished.class)
			@Trigger(TimedOut.class)
			class LoopQuit extends Transition
			{
				@Override
				public boolean guard()
				{
					return time == 0;
				}
			}
			
		}
		
		@From(Init.class)
		@To(Closed.class)
		class Initialize extends Transition
		{
			@Override
			public void effect()
			{
				Action.log("\tMicrowave: initializing...");
				content = null;
			}
		}
		
		@From(Closed.class)
		@To(Heating.class)
		@Trigger(Start.class)
		class StartHeating extends Transition
		{
			@Override
			public void effect()
			{
				Action.log("Microwave: start heating...");
			}
		}
		
		@From(Heating.class)
		@To(Closed.class)
		@Trigger(Stop.class)
		class StopHeating1 extends Transition
		{
			@Override
			public void effect()
			{
				intensity = 1;
				time = 0;
			}
		}
		
		@From(Heating.class)
		@To(Opened.class)
		@Trigger(Open.class)
		class StopHeating2 extends Transition
		{
			@Override
			public void effect()
			{
				intensity = 1;
				time = 0;
			}
		}
		
		@From(Heating.Finished.class)
		@To(Closed.class)
		class Finishing extends Transition
		{
		}
		
	}
	
	class Human extends ModelClass
	{
		class Init extends Initial
		{
		}
		
		class Work extends State
		{
			@Override
			public void entry()
			{
				
			}
		}
		
		@From(Init.class)@To(Work.class)
		class Initialize extends Transition{}
		
	}
	
	// associations
	
	class Usage extends Association
	{
		class usedMicrowave extends One<Microwave>
		{
		}
		
		class userOfMicrowave extends Many<Human>
		{
		}
	}
	
	// signals
	
	static class Open extends Signal
	{
	}
	
	static class Close extends Signal
	{
	}
	
	static class Put extends Signal
	{
		Consumable item;
		
		public Put(Consumable it)
		{
			item = it;
		}
	}
	
	static class Get extends Signal
	{
	}
	
	static class SetIntensity extends Signal
	{
		int value;
		
		public SetIntensity(int value)
		{
			this.value = value;
		}
	}
	
	static class SetTime extends Signal
	{
		int value;
		
		public SetTime(int value)
		{
			this.value = value;
		}
	}
	
	static class Start extends Signal
	{
	}
	
	static class Stop extends Signal
	{
	}
	
	static class TimedOut extends Signal
	{
	}
	
	static class Ding extends Signal{}
	
	// Signal classes are allowed to be static for simpler use.
	
}

class MicrowaveTester
{
	
	void test() throws InterruptedException
	{
		ModelExecutor.Settings.setExecutorLog(false);
		
		MicrowaveModel.Microwave m = Action.create(MicrowaveModel.Microwave.class);
		Human h = Action.create(Human.class);
		
		Action.link(Usage.usedMicrowave.class,
				m,
				Usage.userOfMicrowave.class,
				h);
		
		Action.log("Machine and human are starting.");
		Action.start(m);
		Action.start(h);
		
		String inp = "";
		do
		{
			System.out.println("Do Action: ");
			inp = System.console().readLine();
			inp = inp.toLowerCase();
			
			switch(inp)
			{
				case "open":
					Action.send(m, new Open());
					break;
				case "close":
					Action.send(m, new Close());
					break;
				case "put":
					Food f = Action.create(Food.class);
					
					Action.send(m, new Put(f));
					break;
				case "get":
					Action.send(m, new Get());
					break;
				case "setintensity":
					System.out.println("  Intensity Level (1-5): ");
					Integer i = Integer.parseInt(System.console().readLine());
					Action.send(m, new SetIntensity(i));
					break;
				case "settime":
					System.out.println("  Time in sec(s): ");
					Integer t = Integer.parseInt(System.console().readLine());
					Action.send(m, new SetTime(t));
					break;
				case "start":
					Action.send(m, new Start());
					break;
				case "stop":
					Action.send(m, new Stop());
					break;
				case "quit":
					
					break;
				case "help":
					default:
						System.out.println("Actions to use: Open, Close, Put, "
								+ "Get, SetIntensity, SetTime, Start, Stop.");
						break;
			}
			
			
		}while(!inp.equals("quit"));
		
		ModelExecutor.shutdown();
	}
	
}

public class Microwave
{
	public static void main(String[] args) throws InterruptedException
	{
		new MicrowaveTester().test();
	}
}
