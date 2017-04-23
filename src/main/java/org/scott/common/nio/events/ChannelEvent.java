package org.scott.common.nio.events;

import java.nio.channels.Channel;

public class ChannelEvent {

	private Channel channel;
	
	public ChannelEvent(Channel channel) {
		this.channel = channel;
	}

	public Channel getChannel() {
		return channel;
	}

}
