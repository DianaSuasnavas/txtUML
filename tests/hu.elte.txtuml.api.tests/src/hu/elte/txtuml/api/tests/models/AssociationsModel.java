package hu.elte.txtuml.api.tests.models;

import hu.elte.txtuml.api.Association;
import hu.elte.txtuml.api.Model;
import hu.elte.txtuml.api.ModelClass;

public class AssociationsModel extends Model {

	public static class A extends ModelClass {}
	public static class B extends ModelClass {}
	
	public class Assoc1 extends Association {
		public class a extends Many<A> {}
		public class b extends HiddenMany<B> {}
	}

	public class Assoc2 extends Association {
		public class a extends Many<A> {}
		public class b extends Many<B> {}
	}

	public class Refl extends Association {
		public class a1 extends Many<A> {}
		public class a2 extends Many<A> {}
	}

}
