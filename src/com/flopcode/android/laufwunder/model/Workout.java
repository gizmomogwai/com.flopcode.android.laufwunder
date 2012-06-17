package com.flopcode.android.laufwunder.model;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class Workout implements Serializable {
	private static final long serialVersionUID = 1L;
	public String fTitle;
	public Intervals fIntervals;

	public Workout(JSONObject jsonObject) throws Exception {
		fTitle = (String) jsonObject.get("title");
		fIntervals = new Intervals((JSONArray) jsonObject.get("intervals"));
	}

	public static List<Workout> findAll() {
		List<Workout> res = new ArrayList<Workout>();
		File f = new File("/sdcard/laufwunder/workouts");
		if (f.exists()) {
			File[] files = f.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.getAbsolutePath().endsWith(".workout");
				}
			});
			Arrays.sort(files);
			for (File file : files) {
				try {
					res.add(new Workout(new JSONObject(getContent(file))));
				} catch (Exception e) {
					Log.e("laufwunder", "problem with file: " + file.getAbsolutePath(), e);
				}
			}
		}
		return res;
	}

	private static String getContent(File file) throws Exception {
		Reader r = new InputStreamReader(new FileInputStream(file), "UTF-8");
		StringBuilder res = new StringBuilder();
		char buffer[] = new char[1024];
		int read = r.read(buffer);
		while (read != -1) {
			res.append(buffer, 0, read);
			read = r.read(buffer);
		}
		return res.toString();
	}
}
