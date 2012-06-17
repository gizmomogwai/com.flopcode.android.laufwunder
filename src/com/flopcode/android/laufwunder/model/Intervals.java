package com.flopcode.android.laufwunder.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Intervals implements Serializable {
	private static final long serialVersionUID = 1L;
	List<Interval> fIntervals = new ArrayList<Interval>();

	public Intervals() {}
	
	public Intervals(JSONArray a) throws Exception {
		for (int i = 0; i < a.length(); i++) {
			JSONObject h = (JSONObject) a.get(i);
			add(new Interval(h));
		}
	}

	public void add(Interval interval) {
		fIntervals.add(interval);	  
  }

	public long getDurationInMs() {
		int res = 0;
		for (Interval interval : fIntervals) {
			res += interval.getDurationInMs();
		}
		return res;
	}

	public long getDurationInMinutes() {
		return getDurationInMs() / 1000 / 60;
	}

	public List<Interval> asList() {
		return fIntervals;
	}
}
