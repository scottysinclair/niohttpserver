package org.scott.psbus;

public class PublicationData {
	
	private String topic;

	private Object data;

	public PublicationData(String topic, Object data) {
		this.topic = topic;
		this.data = data;
	}

	public String getTopic() {
		return topic;
	}

	public Object getData() {
		return data;
	}
}
