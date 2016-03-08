package hu.elte.txtuml.export.cpp.wizardz;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class CompileTests {
	
	private static class Config {
		final String project;
		final String model;
		final String deployment;
		Config(String project, String model, String deployment) {
			this.project = project;
			this.model = model;
			this.deployment = deployment;
		}
	}
	
	private static final Config[] testProjects = {
			new Config("hu.elte.txtuml.examples.machine", "hu.elte.txtuml.examples.machine.model1", "hu.elte.txtuml.examples.machine.Machine1Configuration"),
			new Config("hu.elte.txtuml.examples.monitoring", "hu.elte.txtuml.examples.monitoring.model", "hu.elte.txtuml.examples.monitoring.MonitoringConfiguration"),
			new Config("hu.elte.txtuml.examples.producer_consumer", "hu.elte.txtuml.examples.producer_consumer.model", "hu.elte.txtuml.examples.producer_consumer.ProducerConsumerConfiguration"),
			new Config("hu.elte.txtuml.examples.train", "hu.elte.txtuml.examples.train.model", "hu.elte.txtuml.examples.train.TrainConfiguration"),
	};
	
	@Test
	public void exportTest() {
		for (Config config : testProjects) {
			TxtUMLToCppWizard cppgen = new TxtUMLToCppWizard(true);
			boolean runsOk = true;
			try {
				cppgen.uml2ToCpp(config.project, config.model, config.deployment, false);
			} catch (Exception e) {
				runsOk = false;
				e.printStackTrace();
			}
			assertThat(runsOk, is(true));
		}
	}
}

