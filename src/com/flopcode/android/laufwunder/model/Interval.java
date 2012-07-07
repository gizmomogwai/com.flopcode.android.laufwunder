package com.flopcode.android.laufwunder.model;

import java.io.Serializable;

import org.json.JSONObject;

public class Interval implements Serializable {
	private static final long serialVersionUID = 1L;
	public String fComment;
	public String fColor;

	/** time in seconds */
	public int fTime;

	public Interval(JSONObject object) throws Exception {
		fComment = object.getString("comment");
		fColor = object.getString("color");
		String time = object.getString("time");
		String timeWithoutId = time.substring(0, time.length()-1);
		if (time.endsWith("s")) {
			fTime = Integer.parseInt(timeWithoutId);
		} else if (time.endsWith("m")) {
			fTime = Integer.parseInt(timeWithoutId) * 60;
		} else {
			throw new IllegalArgumentException("could not parse time of interval '" + fComment + "'");
		}
	}

	public Interval(String comment, String color, int time) {
	  fComment = comment;
	  fColor = color;
	  fTime = time;
  }

	public long getDurationInMs() {
		return fTime * 1000;
	}

	public long getEndOfInterval(long startOfInterval) {
		return startOfInterval + getDurationInMs();
  }

	public int getMinutes() {
	  return fTime / 60;
  }
}
