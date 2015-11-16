package hu.elte.txtuml.api.model.tests.error.deletion;

import hu.elte.txtuml.api.model.Action;
import hu.elte.txtuml.api.model.tests.base.SimpleModelTestsBase;
import hu.elte.txtuml.api.model.tests.models.simple.A_B;
import hu.elte.txtuml.api.model.tests.util.SeparateClassloaderTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SeparateClassloaderTestRunner.class)
public class LinkingDeletedTest extends SimpleModelTestsBase {

	@Test
	public void test() {

		Action.delete(a);
		Action.link(A_B.a.class, a, A_B.b.class, b);

		stopModelExecution();

		executionAsserter.assertErrors(x -> x.linkingDeletedObject(a));

	}

}
