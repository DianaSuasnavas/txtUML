package hu.elte.txtuml.stdlib;

import hu.elte.txtuml.api.ExternalClass;
import hu.elte.txtuml.api.ModelType;

public abstract class VariableType<T, MT extends ModelType<T>> extends
		ExternalClass {

	private MT value;

	protected VariableType(MT value) {
		this.value = value;
	}

	public MT get() {
		return value;
	}

	public void set(MT value) {
		this.value = value;
	}

	public abstract void set(T rawValue);

	public String toString() {
		return value.toString();
	}
}
