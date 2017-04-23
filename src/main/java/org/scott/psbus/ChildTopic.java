package org.scott.psbus;

public abstract class ChildTopic extends Topic {
	
	private Topic parent;

	public ChildTopic(Topic parent) {
		this.parent = parent;
	}

	protected String getFullName() {
		return parent.getFullName() + "." + getName();
	}
	
	protected Topic getOrCreateTopic(TopicAddress topic){
		if (matchesSoFar(topic)) {
			if (topic.lastPart()) { 
				return this;
			}
			else {
				return getOrCreateChildTopic(topic.next());
			}
		}
		return null;
	}
	
	protected void publish(TopicAddress topic, Object data) {
		if (matchesSoFar(topic)) {
			if (fullyMatchesTopicAddress(topic)) {
				publishToListeners(topic, data);
			}
			else {
				for (Topic child: children().findMatches(topic.next())) {
					child.publish(topic.next(), data);
				}
			}
		}
	}

		
}
