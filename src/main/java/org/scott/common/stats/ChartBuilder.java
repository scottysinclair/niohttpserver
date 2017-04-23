package org.scott.common.stats;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.Socket;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.scott.common.nio.events.ChannelEvent;
import org.scott.common.nio.events.DataTransferredEvent;
import org.scott.psbus.PubSubBus;
import org.scott.psbus.PublicationData;
import org.scott.psbus.TopicListener;

public class ChartBuilder implements TopicListener {

    private PubSubBus bus;
    private Map<String, ValuesOverTime> hitsByTopic;
    private Map<String, List<EventAtTime<String>>> channels;

    public ChartBuilder(PubSubBus bus) {
        this.bus = bus;
        this.hitsByTopic = Collections.synchronizedMap(new HashMap<String, ValuesOverTime>());
        this.channels = Collections.synchronizedMap(new HashMap<String, List<EventAtTime<String>>>());
    }

    public void subscribe(String topic) {
        bus.subscribeTopic(topic, this);
    }

    public void topicPublished(PublicationData data) {
        ValuesOverTime hits = hitsByTopic.get(data.getTopic());
        if (hits == null) {
            hitsByTopic.put(data.getTopic(), hits = new ValuesOverTime(1000));
        }
        if (data.getData() instanceof DataTransferredEvent) {
            hits.add(((DataTransferredEvent)data.getData()).getBytes(), System.currentTimeMillis());
        }
        else {
            hits.add(1, System.currentTimeMillis());
        }
        Channel channel = (Channel)((ChannelEvent)data.getData()).getChannel();
        Socket sok = ((SocketChannel)channel).socket();
        List<EventAtTime<String>> events = channels.get(sok.toString());
        if (events == null) {
          channels.put(sok.toString(), events = Collections.synchronizedList(new LinkedList<EventAtTime<String>>()));
        }
        events.add(new EventAtTime<String>(data.getTopic(), System.currentTimeMillis()));
    }

    public void clearChannels() {
        channels.clear();
    }

    public Map<String, List<EventAtTime<String>>> getChannels() {
        Map<String, List<EventAtTime<String>>> m = new HashMap<String, List<EventAtTime<String>>>();
        synchronized(channels) {
          for(Map.Entry<String, List<EventAtTime<String>>> e: channels.entrySet()) {
            m.put(e.getKey(), new ArrayList<EventAtTime<String>>(e.getValue()));
          }
        }
        return m;
    }

    public Set<String> getTopics() {
      synchronized(hitsByTopic) {
        return new HashSet<String>(hitsByTopic.keySet());
      }
    }

    public Map<String, BufferedImage> chartAll(int width, int height) {
        Map<String, BufferedImage> charts = new HashMap<String, BufferedImage>();
        Map<String, ValuesOverTime> copy = new HashMap<String, ValuesOverTime>();
        synchronized (hitsByTopic) {
            copy.putAll(hitsByTopic);
        }
        for (Map.Entry<String, ValuesOverTime> entry: copy.entrySet()) {
            charts.put(entry.getKey(), chart(entry.getKey(), width, height));
        }
        return charts;
    }

    public BufferedImage chart(String topic, int width, int height) {
        ValuesOverTime hits = hitsByTopic.get(topic);
        JFreeChart chart = getLineChart(topic, hits);
        if (chart != null) {
            return chart.createBufferedImage(width, height);
        }
        return null;

    }

     private JFreeChart getLineChart(String topic, Iterable<ValueAtTime> values) {
            DefaultTableXYDataset xyDataset = new DefaultTableXYDataset();
            XYSeries series = new XYSeries(topic, false, false);
            synchronized(values){
                double startTime = 0;
                for (ValueAtTime v: values) {
                    if (startTime == 0) {
                      startTime = v.getTime();
                    }
                    series.add((v.getTime()-startTime) / 1000, v.getValue());
                }
            }
            if (series.getItemCount() > 0) {
                xyDataset.addSeries(series);
                JFreeChart chart =  ChartFactory.createXYLineChart(topic,   // Title
                              "time",           // Dataset
                              "hits",
                              xyDataset,                  // Show legend
                              PlotOrientation.VERTICAL,
                              true,true, true);
                chart.setBackgroundPaint(new Color(255, 255, 255));
                return chart;
            }
            return null;
        }

}
