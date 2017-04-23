package org.scott.common.stats;

public class ValueAtTime {
    public double value;
    public final long time;
    public ValueAtTime(double value, long time) {
        this.value = value;
        this.time = time;
    }

    public long getTime() {
        return time;
    }
    
    public double incValue(double d) {
    	return value += d;
    }

    public double getValue() {
        return value;
    }
	
}
