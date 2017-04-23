package org.scott.psbus;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class TopicChildren {
	
	private static final Logger LOG = Logger.getLogger(TopicChildren.class.getName());

	private static final List<String> specialChildrenNames = Arrays.asList(new String[]{"*", "**"});

	private Map<String, Topic> childrenByPartName;

	public TopicChildren() {
		childrenByPartName = new HashMap<String, Topic>();
	}
	
	public synchronized Topic getOrCreateChild(Topic parent, TopicAddress topic) {
		Topic child = childrenByPartName.get(topic.getPart());
		if (child != null) {
			return child;
		}
		return addChildTopic(parent, topic); 
	}
	
	public synchronized Collection<Topic> findMatches(TopicAddress topic) {
		Set<Topic> matches = new HashSet<Topic>();
		if (!childrenByPartName.isEmpty()) {
			Topic child = childrenByPartName.get(topic.getPart());
			if (child != null) {
				matches.add(child);
			}
			Set<String> specialChildren = new HashSet<String>(childrenByPartName.keySet());
			specialChildren.retainAll(specialChildrenNames);
			for (String partName: specialChildren) {
				child = childrenByPartName.get(partName);
				if (child.matchesSoFar(topic)) {
					matches.add(child);
				}
			}
		}
		return matches;
	}
	
	public synchronized Collection<Topic> copy() {
		return new HashSet<Topic>(childrenByPartName.values());
	}
	
	protected synchronized Topic addChildTopic(Topic parent, TopicAddress topic) {
		LOG.fine("adding child " + topic.getPart() + " to " + parent.getFullName());
		Topic t = null;
		if ("*".equals(topic.getPart())) {
			t = new StarTopic(parent);
		}
		else if ("**".equals(topic.getPart())) {
			t = new StarStarTopic(parent);
		}
		else {
			t = new TextTopic(parent, topic.getPart());
		}
		childrenByPartName.put(topic.getPart(), t);
		return t;
	}
	

	

}
