package org.scott.common.stats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ValuesOverTime implements Iterable<ValueAtTime>{
	
	private List<ValueAtTime> hits;
	private ValueAtTime currentHits;
	private long timeSliceInMillis;
	
	public ValuesOverTime(long timeSliceInMillis) {
		this.timeSliceInMillis = timeSliceInMillis;
		hits = new LinkedList<ValueAtTime>();
	}

	public synchronized Iterator<ValueAtTime> iterator() {
		return new ArrayList<ValueAtTime>(hits).iterator();
	}

	public synchronized void add(double value, long timeInMillis) {
		if (currentHits == null) {
			currentHits = new ValueAtTime(0, timeInMillis);
		}
		else if (timeInMillis >= (currentHits.getTime() + timeSliceInMillis)) {
		  long nextTimeSlice = currentHits.getTime() + timeSliceInMillis;
		  while(timeInMillis >= nextTimeSlice) { 
				hits.add(currentHits = new ValueAtTime(0, nextTimeSlice));
				nextTimeSlice += timeSliceInMillis;
		  }
		}

		currentHits.incValue( value );
	}

}
