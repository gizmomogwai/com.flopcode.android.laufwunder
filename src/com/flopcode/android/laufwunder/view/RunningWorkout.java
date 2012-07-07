package com.flopcode.android.laufwunder.view;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.flopcode.android.laufwunder.R;
import com.flopcode.android.laufwunder.model.Workout;
import com.flopcode.android.laufwunder.service.WorkoutService;
import com.flopcode.android.laufwunder.service.WorkoutService.LocalBinder;

public class RunningWorkout extends Activity {

	private TextView fTitle;

	private TextView fTotalTime;

	private TextView fTotalPercentage;

	private TextView fCurrentInterval;

	private Button fStop;

	private TextView fCurrentPercentage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_running_workout);
		fTitle = (TextView) findViewById(R.id.workout_title);
		fTotalTime = (TextView) findViewById(R.id.total_time);
		fTotalPercentage = (TextView) findViewById(R.id.total_percentage);
		fCurrentInterval = (TextView) findViewById(R.id.current_interval);
		fCurrentPercentage = (TextView) findViewById(R.id.current_percentage);
		fStop = (Button) findViewById(R.id.stop);
		fStop.setEnabled(false);
		fStop.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (fService != null) {
					fService.cancelWorkout();
				}
			}
		});

		bindService(getServiceIntent(), fServiceConnection, 0);
	}

	@Override
	protected void onPause() {
		if (fService != null) {
			fService.removeListener(fWorkoutServiceListener);
		}
		unbindService(fServiceConnection);
		
		super.onPause();
	}

	private Intent getServiceIntent() {
		return new Intent(this, WorkoutService.class);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_running_workout, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		/*
		 * case android.R.id.home: NavUtils.navigateUpFromSameTask(this); return
		 * true;
		 */
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateUi() {
		if (fService != null) {
			Workout workout = fService.getWorkout();
			fTitle.setText(workout.fTitle);
			
			int secondsOfWorkout = (int)((workout.fIntervals.getDurationInMs() * fService.getPercentage()) / 1000);
			int minutes = secondsOfWorkout / 60;
			int seconds = secondsOfWorkout % 60;
			fTotalTime.setText(String.format("TotalTime: %d:%d", minutes, seconds));
			fTotalPercentage.setText(String.format("%d%%", (int) (100 * fService.getPercentage())));
			
			fCurrentInterval.setText(fService.getCurrentInterval().fComment);
			fCurrentPercentage.setText(String.format("%d%%", (int)(100 * fService.getCurrentIntervalPercentage())));
		}
	}

	private WorkoutService.Listener fWorkoutServiceListener = new WorkoutService.Listener() {
		public void stateChanged() {
			System.out.println("state changed in running service");
			runOnUiThread(new Runnable() {
				public void run() {
					updateUi();
				}

			});
		}
	};

	private WorkoutService fService;

	private ServiceConnection fServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			fService = binder.getService();
			fService.addListener(fWorkoutServiceListener);
			fStop.setEnabled(true);
		}

		public void onServiceDisconnected(ComponentName name) {
			fService = null;
			fStop.setEnabled(false);
		}
	};

}
