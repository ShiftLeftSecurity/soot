package soot.tagkit;

public class LocalSignatureTag {
	private String signature;
	private String localName;

	public LocalSignatureTag(String signature, String localName){
		this.signature = signature;
		this.localName = localName;
	}

	public String toString() {
		return "LocalSignature: Name: " + localName + " Sig: " + signature;
	}

	/** Returns the tag name. */
	public String getName() {
		return "LocalSignatureTag";
	}

	public String getInfo(){
		return "LocalSignature";
	}

	public String getSignature(){
		return signature;
	}

	public String getLocalName(){
		return localName;
	}

	/** Returns the tag raw data. */
	public byte[] getValue() {
		throw new RuntimeException( "LocalSignatureTag has no value for bytecode" );
	}
}
