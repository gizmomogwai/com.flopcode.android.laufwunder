package com.flopcode.android.laufwunder.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Phases implements Serializable {
	private static final long serialVersionUID = 1L;
	List<Phase> fPhases = new ArrayList<Phase>();

	public Phases(JSONArray a) throws Exception {
		for (int i = 0; i < a.length(); i++) {
			JSONObject h = (JSONObject) a.get(i);
			fPhases.add(new Phase(h));
		}
	}

	public long getDurationInMs() {
		int res = 0;
		for (Phase phase : fPhases) {
			res += phase.getDurationInMs();
		}
		return res;
	}

	public long getDurationInMinutes() {
		return getDurationInMs() / 1000 / 60;
	}

	public List<Phase> asList() {
		return fPhases;
	}
}
