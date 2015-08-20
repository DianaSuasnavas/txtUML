package hu.elte.txtuml.api.tests.base;

import hu.elte.txtuml.api.Action;
import hu.elte.txtuml.api.tests.models.HierarchicalModel.A;

import org.junit.Before;

public class HierarchicalModelTestsBase extends TestsBase {

	protected A a;

	@Before
	public void initializeModel() {
		a = new A();
		Action.start(a);
	}
}
