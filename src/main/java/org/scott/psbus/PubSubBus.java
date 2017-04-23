package org.scott.psbus;

public class PubSubBus {
	
	public static final PubSubBus NULL_BUS = new PubSubBus(){
		@Override
		public void publishTopic(String topic, Object data) {
		}
	};
		
	private RootTopic root;
	
	public PubSubBus() {
		root = new RootTopic();
	}
	
	public void publishTopic(Class clazz, String append, Object data) {
		publishTopic(clazz.getName() + "." + append, data);
	}

	public void publishTopic(String topic, Object data) {
		root.publish(topic, data);
	}
	
	public void subscribeTopic(Class clazz, String append, TopicListener listener) {
		subscribeTopic(clazz.getName() + "." + append, listener);
	}
	
	public void subscribeTopic(String topic, TopicListener listener) {
		Topic t = root.getOrCreateTopic(topic);
		t.subscribe(listener);
	}
	
	/**
	 * blocks the thread until the next publication
	 * @param topic
	 * @return the next publication
	 * @throws InterruptedException
	 */
	public PublicationData getNextPublication(String topic) throws InterruptedException {
		Topic t = root.getOrCreateTopic(topic);
		return t.waitForNextPublication();
	}

	@Override
	public String toString() {
		return root.toString();
	}
	
}
