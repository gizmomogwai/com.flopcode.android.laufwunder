package com.flopcode.android.laufwunder.model;

import java.io.Serializable;

import org.json.JSONObject;

public class Phase implements Serializable {
	private static final long serialVersionUID = 1L;
	public String fComment;
	public String fColor;

	/** time in minutes */
	public int fTime;

	public Phase(JSONObject object) throws Exception {
		fTime = object.getInt("time");
		fComment = object.getString("comment");
		fColor = object.getString("color");
	}

	public long getDurationInMs() {
		return fTime * 1000 * 60;
	}

	public long getEndOfPhase(long startOfPhase) {
		return startOfPhase + getDurationInMs();
  }

	public int getMinutes() {
	  return fTime;
  }
}
