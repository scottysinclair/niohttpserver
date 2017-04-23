package org.scott.psbus;

/**
 * class which notifies threads
 * when a publication arrives
 * @author apatecsinclair
 *
 */
public class FuturePublication implements TopicListener {
	
	private PublicationData data;
	
	public synchronized PublicationData getLastPublication() {
		return data;
	}

	public synchronized void topicPublished(PublicationData data) {
		this.data = data;
		notifyAll();
	}
	
	public synchronized PublicationData waitForNextPublication() throws InterruptedException {
		wait();
		return data;
	}
	
	public PublicationData waitForNextPublicationWithNoInterrupt() {
		while(true) {
			try {
				return waitForNextPublication();
			}
			catch(InterruptedException x){}
		}
	}

}
