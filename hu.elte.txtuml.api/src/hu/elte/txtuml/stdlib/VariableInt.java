package hu.elte.txtuml.stdlib;

import hu.elte.txtuml.api.ModelInt;

public class VariableInt extends VariableType<Integer, ModelInt> {

	public VariableInt() {
		super(ModelInt.ZERO);
	}

	public VariableInt(ModelInt value) {
		super(value);
	}

	public VariableInt(int rawValue) {
		super(new ModelInt(rawValue));
	}

	@Override
	public void set(Integer rawValue) {
		set(new ModelInt(rawValue));
	}

}
