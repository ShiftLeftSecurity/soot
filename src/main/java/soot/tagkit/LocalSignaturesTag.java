package soot.tagkit;

import java.util.ArrayList;
import java.util.Map;

public class LocalSignaturesTag implements Tag {
	private int num_params;
	private Map<String, LocalSignatureTag> localSigTags;

	public LocalSignaturesTag(int num) {
		this.num_params = num;
	}

	@Override
	public String getName() {
		return "LocalSignaturesTag";
	}

	@Override
	public byte[] getValue() {
		throw new RuntimeException("LocalSignaturesTag has no value for bytecode");
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Local Signatures: num params: " + num_params);
		if (localSigTags != null) {
			for (LocalSignatureTag tag : localSigTags.values()) {
				if (tag != null) {
					sb.append("\n");
					sb.append(tag.toString());
				}
			}
		}
		sb.append("\n");
		return sb.toString();
	}

	public String getInfo() {
		return "ParameterSignature";
	}

	public Map<String, LocalSignatureTag> getLocalSignatures() {
		return localSigTags;
	}

	public void addSignatures(Map<String, LocalSignatureTag> tags) {
		localSigTags = tags;
	}
}
