package org.scott.common.nio;

/**
 * Created by IntelliJ IDEA.
 * User: N006719
 * Date: 25-Jun-2006
 * Time: 13:19:31
 * To change this template use File | Settings | File Templates.
 */
public class KeyInfo {
    private OutputManager.OutputSender outputSender;
    private InputConsumer inputConsumer;

    public OutputManager.OutputSender getOutputSender() {
        return outputSender;
    }

    public void setOutputSender(OutputManager.OutputSender outputSender) {
        this.outputSender = outputSender;
    }

    public InputConsumer getInputConsumer() {
        return inputConsumer;
    }

    public void setInputConsumer(InputConsumer httpParser) {
        this.inputConsumer = httpParser;
    }


}
