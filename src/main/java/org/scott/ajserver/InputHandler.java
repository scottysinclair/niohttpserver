package org.scott.ajserver;

import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.scott.common.nio.ConnectManager;
import org.scott.common.nio.InputConsumer;
import org.scott.common.nio.InputManager;
import org.scott.common.nio.KeyInfo;
import org.scott.common.nio.OutputManager;
import org.scott.common.nio.http.HttpRequestParser;

import java.nio.channels.ServerSocketChannel;
import java.nio.CharBuffer;
import java.io.IOException;

/**
 * receives bytes from the InputManager and is responsible
 * for building a HTTP request scheduling it for execution.
 */
public class InputHandler implements ConnectManager.ConnectionListener, InputConsumer {
    private AjaxProcessor ajaxProcessor;
    private InputManager inputManager;
    private OutputManager outputManager;

    public void setAjaxProcessor(AjaxProcessor ajaxProcessor) {
        this.ajaxProcessor = ajaxProcessor;
    }

    public void setInputManager(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    public void setOutputManager(OutputManager outputManager) {
        this.outputManager = outputManager;
    }
    
    public void connectionReceived(SocketChannel channel) {
        try {
//            System.out.println("Received new connection");
            if (channel != null) {
                channel.configureBlocking(false);
                inputManager.addChannel(channel, null);
            }
  //          System.out.println("Ready to read");
        }
        catch(IOException x) {
            x.printStackTrace(System.err);
//            key.cancel();
//            try { key.selector().selectNow(); } catch(IOException x2){x2.printStackTrace(System.err); }
            if (channel != null && channel.isOpen())  {
                try { channel.close(); }
                catch(IOException x2) {x2.printStackTrace(System.err);}
            }
        }
    }

    public void consumeInput(SelectionKey key, CharBuffer charBuffer) {
        try {
            KeyInfo keyInfo = (KeyInfo)key.attachment();
            if (keyInfo == null) {
                keyInfo = new KeyInfo();
                keyInfo.setOutputSender(outputManager.registerChannel((SocketChannel)key.channel()));
                keyInfo.setInputConsumer(new HttpRequestParser(ajaxProcessor));
                key.attach(keyInfo);
            }
            InputConsumer inputConsumer = keyInfo.getInputConsumer();
            inputConsumer.consumeInput(key, charBuffer);
        }
        catch(IOException x) {
            x.printStackTrace(System.err);
            key.cancel();
        }
    }


}
