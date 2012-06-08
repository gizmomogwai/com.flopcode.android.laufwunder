package com.flopcode.android.laufwunder.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import com.flopcode.android.laufwunder.model.Phase;
import com.flopcode.android.laufwunder.model.Workout;

public class WorkoutService extends Service {
	public static class SoundRunnable implements Runnable {
		private Context fContext;

		public SoundRunnable(Context c) {
			fContext = c;
		}

		public void run() {
			Ringtone r = RingtoneManager.getRingtone(fContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
			r.play();
		}
	}

	private static final int ONE_SECOND = 1000;
	private static final int ONE_MINUTE = 60 * ONE_SECOND;
	private Workout fWorkout;
	private HandlerThread fWorkoutThread;
	private TextToSpeech fTextToSpeech;
	private boolean fTextToSpeechAvailable = false;

	class TextRunnable implements Runnable {

		private String fText;
		private Handler fHandler;

		public TextRunnable(Handler h, String string) {
			fText = string;
			fHandler = h;
		}

		public void run() {
			if (fTextToSpeechAvailable) {
				Log.i("laufwunder", "finished runnable .-.. playing notification");
				fTextToSpeech.speak(fText, TextToSpeech.QUEUE_ADD, null);
			} else {
				Log.e("laufwunder", "retrying speech out");
				fHandler.postDelayed(this, 1000);
			}
		}
	}

	private void init(Intent intent) {
		fWorkout = (Workout) intent.getExtras().getSerializable("workout");
		if (fWorkoutThread != null) {
			fWorkoutThread.quit();
		}
		fWorkoutThread = new HandlerThread("workout");
		fWorkoutThread.start();

		fTextToSpeech = new TextToSpeech(this, new OnInitListener() {
			public void onInit(int status) {
				fTextToSpeechAvailable = true;
			}
		});

		long startTime = SystemClock.uptimeMillis();
		Handler h = new Handler(fWorkoutThread.getLooper());

		h.postAtTime(new TextRunnable(h, "ein drittel des ganzen workouts"), startTime + (fWorkout.fPhases.getDurationInMs() / 3));
		h.postAtTime(new TextRunnable(h, "halbzeit des ganzen workouts"), startTime + fWorkout.fPhases.getDurationInMs() / 2);
		h.postAtTime(new TextRunnable(h, "zwei drittel des ganzen workouts, weiter so"), startTime + 2 * (fWorkout.fPhases.getDurationInMs() / 3));
		h.postAtTime(new TextRunnable(h, "fertig"), startTime + fWorkout.fPhases.getDurationInMs());

		// add minute tickers to each phase
		long startOfPhase = startTime;
		for (Phase phase : fWorkout.fPhases.asList()) {
			long next = startOfPhase;
			h.postAtTime(new TextRunnable(h, "los gehts mit phase " + phase.fComment + " für " + phase.getMinutes() + " minuten"), next);
			next += ONE_MINUTE;
			while (true) {
				if (next < phase.getEndOfPhase(startOfPhase)) {
					h.postAtTime(new SoundRunnable(this), next);
				} else {
					break;
				}
				next += ONE_MINUTE;
			}
			startOfPhase = phase.getEndOfPhase(startOfPhase);
		}

		// add countdown to end of phase
		startOfPhase = startTime;
		for (Phase phase : fWorkout.fPhases.asList()) {
			long end = phase.getEndOfPhase(startOfPhase);
			addCountdown(h, end, 30);
			addCountdown(h, end, 15);
			addCountdown(h, end, 10);
			for (int i = 5; i > 0; i--) {
				addCountdown(h, end, i);
			}
			startOfPhase = end;
		}
	}

	private void addCountdown(Handler h, long end, int seconds) {
		h.postAtTime(new TextRunnable(h, "" + seconds), end - seconds * ONE_SECOND);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		init(intent);
		return START_STICKY;
	}

	// bind support
	public class LocalBinder extends Binder {
		public WorkoutService getService() {
			return WorkoutService.this;
		}
	}

	LocalBinder fBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return fBinder;
	}

}
