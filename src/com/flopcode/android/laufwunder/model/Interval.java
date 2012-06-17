package com.flopcode.android.laufwunder.model;

import java.io.Serializable;

import org.json.JSONObject;

public class Interval implements Serializable {
	private static final long serialVersionUID = 1L;
	public String fComment;
	public String fColor;

	/** time in minutes */
	public int fTime;

	public Interval(JSONObject object) throws Exception {
		fTime = object.getInt("time");
		fComment = object.getString("comment");
		fColor = object.getString("color");
	}

	public Interval(String comment, String color, int time) {
	  fComment = comment;
	  fColor = color;
	  fTime = time;
  }

	public long getDurationInMs() {
		return fTime * 1000 * 60; // correct
//		return fTime * 500;
//		return fTime * 1000;
	}

	public long getEndOfInterval(long startOfInterval) {
		return startOfInterval + getDurationInMs();
  }

	public int getMinutes() {
	  return fTime;
  }
}
