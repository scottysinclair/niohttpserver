package org.scott.psbus;

public class RootTopic extends Topic {
	
	public RootTopic() {
	}
	
	@Override
	protected String getName() {
		return "ROOT";
	}

	@Override
	protected String getFullName() {
		return getName();
	}

	@Override
	public boolean matchesSoFar(TopicAddress topic) {
		return true;
	}
	
	@Override
	protected Topic getOrCreateTopic(TopicAddress topic) {
		return getOrCreateChildTopic(topic);
	}

	protected void publish(TopicAddress topic, Object data) {
		for (Topic child: children().findMatches(topic)) {
			child.publish(topic, data);
		}
	}
	
}
