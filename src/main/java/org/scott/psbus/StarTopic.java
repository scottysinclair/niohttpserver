package org.scott.psbus;

public class StarTopic extends ChildTopic {
	
	public StarTopic(Topic parent) {
		super(parent);
	}
	
	@Override
	protected String getName() {
		return "*";
	}

	@Override
	public boolean matchesSoFar(TopicAddress topic) {
		return true;
	}

}
