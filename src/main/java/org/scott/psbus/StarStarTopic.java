package org.scott.psbus;

public class StarStarTopic extends ChildTopic {

	public StarStarTopic(Topic parent) {
		super(parent);
	}

	@Override
	protected String getName() {
		return "**";
	}
	
	@Override
	protected boolean fullyMatchesTopicAddress(TopicAddress topic) {
		return true;
	}

	@Override
	public boolean matchesSoFar(TopicAddress topic) {
		return true;
	}

}
