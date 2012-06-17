package com.flopcode.android.laufwunder.view;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.flopcode.android.laufwunder.R;
import com.flopcode.android.laufwunder.model.Workout;
import com.flopcode.android.laufwunder.service.WorkoutService;

public class Workouts extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		final List<Workout> workouts = Workout.findAll();
		ListView lv = (ListView) findViewById(R.id.workouts);
		ArrayAdapter<Workout> adapter = new ArrayAdapter<Workout>(this, R.layout.workout_item, R.id.title) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View target = convertView;
				if (target == null) {
					target = getLayoutInflater().inflate(R.layout.workout_item, null);
				}
				TextView tv = (TextView) target.findViewById(R.id.title);
				Workout w = workouts.get(position);
				tv.setText(w.fTitle + " (" + w.fIntervals.getDurationInMinutes() + "m)");

				IntervalsView pv = (IntervalsView) target.findViewById(R.id.intervals);
				pv.setIntervals(w.fIntervals);

				return target;
			}
		};

		for (Workout workout : workouts) {
			adapter.add(workout);
		}
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int idx, long id) {
				Workout w = workouts.get(idx);
				Intent i = new Intent(Workouts.this, WorkoutService.class);
				i.putExtra("workout", w);
				startService(i);
			}
		});
	}
}
