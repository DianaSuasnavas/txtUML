package txtuml.api;

public class ModelType<T> implements ModelElement {
	protected ModelType(T val) {
		value = val;
	}
	protected ModelType() {
		this(null);
	}
	T getValue() {
		return value;
	}
	public String toString() {
		return value.toString(); // TODO should not be used in the model
									// instead we should force the user to use ModelString ( an alternative method returning ModelString should be provided ) 
	}
		
	private final T value;
}

