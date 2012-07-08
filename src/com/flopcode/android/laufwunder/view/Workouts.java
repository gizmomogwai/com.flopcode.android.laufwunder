package com.flopcode.android.laufwunder.view;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flopcode.android.laufwunder.R;
import com.flopcode.android.laufwunder.model.Workout;
import com.flopcode.android.laufwunder.service.WorkoutService;
import com.flopcode.android.laufwunder.service.WorkoutService.Listener;
import com.flopcode.android.laufwunder.service.WorkoutService.LocalBinder;

public class Workouts extends Activity {
	private ListView fList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final List<Workout> workouts = Workout.findAll();
		fList = (ListView) findViewById(R.id.workouts);
		ArrayAdapter<Workout> adapter = new ArrayAdapter<Workout>(this, R.layout.workout_item, R.id.workout_title) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View target = convertView;
				if (target == null) {
					target = getLayoutInflater().inflate(R.layout.workout_item, null);
				}
				TextView tv = (TextView) target.findViewById(R.id.workout_title);
				Workout w = workouts.get(position);
				tv.setText(w.fTitle + " (" + w.fIntervals.getDurationInMinutes() + "m)");

				IntervalsView pv = (IntervalsView) target.findViewById(R.id.intervals);
				pv.setIntervals(w.fIntervals);

				ImageView runIndicator = (ImageView) target.findViewById(R.id.run_indicator);
				runIndicator.setVisibility(View.GONE);
				if (fService != null) {
					boolean active = fService.getActiveIndex() == position;

					if (active) {
						((AnimationDrawable) (runIndicator.getBackground())).start();
						runIndicator.setVisibility(View.VISIBLE);
						pv.setPercentage(fService.getPercentage());
					}
				}

				return target;
			}
		};

		for (Workout workout : workouts) {
			adapter.add(workout);
		}
		fList.setAdapter(adapter);

		fList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int idx, long id) {
				if (fService != null) {
					Toast.makeText(Workouts.this, "Workout already running .. please stop running workout first", Toast.LENGTH_LONG).show();
					startActivity(new Intent(Workouts.this, RunningWorkout.class));
				} else {
					Workout w = workouts.get(idx);
					Intent i = getServiceIntent();
					i.putExtra("workout", w);
					i.putExtra("index", idx);
					startService(i);
				}
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		bindService(getServiceIntent(), fServiceConnection, 0);
	}

	Listener fListener = new Listener() {
		public void stateChanged() {
			runOnUiThread(new Runnable() {
				public void run() {
					fList.invalidateViews();
				}
			});
		}
	};

	@Override
	protected void onPause() {
		if (fService != null) {
			fService.removeListener(fListener);
		}
		unbindService(fServiceConnection);
		fService = null;
		super.onPause();
	}

	private Intent getServiceIntent() {
		return new Intent(this, WorkoutService.class);
	}

	private WorkoutService fService;
	private ServiceConnection fServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			fService = binder.getService();
			fService.addListener(fListener);
		}

		public void onServiceDisconnected(ComponentName name) {
			fService = null;
		}
	};

}
