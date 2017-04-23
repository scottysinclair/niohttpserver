package org.scott.psbus;

public class TextTopic extends ChildTopic {
	private String text;

	public TextTopic(Topic parent, String text) {
		super(parent);
		this.text = text;
	}

	public boolean matchesSoFar(TopicAddress topic) {
		return text.equals(topic.getPart());
	}

	@Override
	protected String getName() {
		return text;
	}

}
