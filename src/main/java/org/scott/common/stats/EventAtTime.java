package org.scott.common.stats;

public class EventAtTime<T> {
    public T event;
    public final long time;
    public EventAtTime(T event, long time) {
        this.event = event;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public T getEvent() {
        return event;
    }
		
}
