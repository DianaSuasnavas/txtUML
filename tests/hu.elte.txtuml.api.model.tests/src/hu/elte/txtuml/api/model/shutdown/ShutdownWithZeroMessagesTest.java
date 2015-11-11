package hu.elte.txtuml.api.model.shutdown;

import hu.elte.txtuml.api.model.ModelExecutor;
import hu.elte.txtuml.api.model.base.SimpleModelTestsBase;
import hu.elte.txtuml.api.model.util.MutableBoolean;
import hu.elte.txtuml.api.model.util.SeparateClassloaderTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SeparateClassloaderTestRunner.class)
public class ShutdownWithZeroMessagesTest extends SimpleModelTestsBase {

	@Test
	public void test() {
		MutableBoolean actionPerformed = new MutableBoolean();
		ModelExecutor.addToShutdownQueue(() -> actionPerformed.value = true);

		Assert.assertEquals(false, actionPerformed.value);
		stopModelExecution();
		Assert.assertEquals(true, actionPerformed.value);

		executionAsserter.assertEvents(x -> x.executionTerminated());
	}

}
