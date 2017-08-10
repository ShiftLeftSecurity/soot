package soot.tagkit;

public class MethodDescriptorTag implements Tag {
	private String descriptor;

	public MethodDescriptorTag(String descriptor) {
		this.descriptor = descriptor;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public String toString() {
		return "MethodDescriptor: " + descriptor;
	}

	@Override
	public String getName() {
		return "MethodDescriptorTag";
	}

	@Override
	public byte[] getValue() throws AttributeValueException {
		throw new RuntimeException( "MethodDescriptor has no value for bytecode" );
	}
}
