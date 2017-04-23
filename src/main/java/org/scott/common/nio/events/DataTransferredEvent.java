package org.scott.common.nio.events;

import java.nio.channels.Channel;

public class DataTransferredEvent extends ChannelEvent {

	private int bytes;
	
	public DataTransferredEvent(Channel channel, int bytes) {
		super(channel);
		this.bytes = bytes;
	}

	public int getBytes() {
		return bytes;
	}
	
}
