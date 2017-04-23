package org.scott.psbus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Topic the datastructure used to keep track of subscriptions
 * an address like page.navgiation.home is a chain of three topics page + .navigation + home
 * @author apatecsinclair
 *
 */
public abstract class Topic extends FuturePublication {
	
	private static final Logger LOG = Logger.getLogger(Topic.class.getName());
	
	private Set<TopicListener> listeners;
	private TopicChildren children;
	
	public Topic() {
		children = new TopicChildren();
		listeners = new HashSet<TopicListener>();
	}
			
	public Topic getOrCreateTopic(String topic) {
		return getOrCreateTopic(new TopicAddress(topic));
	}
	
	public void publish(String topic, Object data) {
		publish(new TopicAddress(topic), data);
	}
	
	public void subscribe(TopicListener listener) {
		synchronized(listeners) {
			listeners.add(listener);
		}
	}
	
	protected TopicChildren children() {
		return children;
	}

	/**
	 * 
	 * @param topic
	 * @param listener
	 * @return
	 */
	protected Topic getOrCreateChildTopic(TopicAddress topic) {
		Topic child = children.getOrCreateChild(this, topic);
		return child.getOrCreateTopic(topic);
	}
	
	protected boolean fullyMatchesTopicAddress(TopicAddress topic) {
		return matchesSoFar(topic) && topic.lastPart();
	}
		
	protected void publishToListeners(TopicAddress topic, Object data){
		PublicationData publishData = new PublicationData(topic.getText(), data);
		topicPublished(publishData);
		for (TopicListener l: copy(listeners)) {
			LOG.fine("publishing " + topic.getText() + " on " + getFullName());
			l.topicPublished(publishData);
		}
	}
	
	private <T> Collection<T> copy(Collection<T> coll) {
		synchronized (coll) {
			return new ArrayList<T>(coll);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getFullName() + "=> " + listeners.size() + " listeners\n");
		for (Topic child: children.copy()) {
			sb.append(child.toString());
		}
		return sb.toString();
	}
	
	
	
	protected abstract String getName();
	
	protected abstract String getFullName();
	
	protected abstract Topic getOrCreateTopic(TopicAddress topic);

	protected abstract void publish(TopicAddress topic, Object data);
	
	protected abstract boolean matchesSoFar(TopicAddress topic);
	
}


