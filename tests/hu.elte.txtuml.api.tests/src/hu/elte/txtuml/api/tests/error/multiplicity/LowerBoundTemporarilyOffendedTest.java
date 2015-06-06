package hu.elte.txtuml.api.tests.error.multiplicity;

import hu.elte.txtuml.api.Action;
import hu.elte.txtuml.api.tests.base.SimpleModelTestsBase;
import hu.elte.txtuml.api.tests.models.SimpleModel.A_B;
import hu.elte.txtuml.api.tests.util.SeparateClassloaderTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SeparateClassloaderTestRunner.class)
public class LowerBoundTemporarilyOffendedTest extends SimpleModelTestsBase {
	
	@Test
	public void test() {
		Action.link(A_B.a.class, a, A_B.b.class, b);
		Action.unlink(A_B.a.class, a, A_B.b.class, b);
		Action.start(a);
		Action.link(A_B.a.class, a, A_B.b.class, b);
		
		stopModelExecution();

		Assert.assertArrayEquals(
				new String[] { },
				executorErrorStream.getOutputAsArray());

	}
}
