package org.scott.psbus;

class TopicAddress {
	private String text;
	private String parts[];
	private int pos;
	
	public TopicAddress(String text) {
		this(text, text.split("\\."), 0);
	}
	public TopicAddress(String text, String[] parts, int pos) {
		this.text = text;
		this.parts = parts;
		this.pos = pos;
	}
	
	public String getText() {
		return text;
	}
	public String getPart() {
		return parts[ pos ];
	}
	
	public boolean lastPart() {
		return pos == parts.length -1; 
	} 
	
	public TopicAddress next() {
		if (pos+1 == parts.length) {
			throw new IllegalStateException("address '" + text + "' does not have position '" + (pos+1) + "'");
		}
		return new TopicAddress(text, parts, pos+1);
	}
}