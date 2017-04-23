package org.scott.common.nio;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;

public interface InputConsumer {
	public void consumeInput(SelectionKey key, CharBuffer charBuffer) throws IOException;
}
