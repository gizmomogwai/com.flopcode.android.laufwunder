package com.flopcode.android.laufwunder.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

import com.flopcode.android.laufwunder.R;
import com.flopcode.android.laufwunder.model.Interval;
import com.flopcode.android.laufwunder.model.Workout;
import com.flopcode.android.laufwunder.view.Workouts;

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
	private int fCurrentIndex = -1;

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
			postNotification(getPercentage(), fText);
		}
	}

	public void postNotification(float progress, String msg) {
		Notification n = new Notification(R.drawable.ic_launcher, "Laufwunder", System.currentTimeMillis());
		Intent intent = new Intent(this, Workouts.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

		n.setLatestEventInfo(getApplicationContext(), "ContentTitle", "ContentText", pendingIntent);
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, n);
	}
	
	private void init(Intent intent) {
		fWorkout = (Workout) intent.getExtras().getSerializable("workout");
		fCurrentIndex = intent.getExtras().getInt("index");
		
		
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

		fStartTime = SystemClock.uptimeMillis();
		Handler h = new Handler(fWorkoutThread.getLooper());

		h.postAtTime(new TextRunnable(h, "ein drittel des ganzen workouts"), fStartTime + (fWorkout.fIntervals.getDurationInMs() / 3));
		h.postAtTime(new TextRunnable(h, "halbzeit des ganzen workouts"), fStartTime + fWorkout.fIntervals.getDurationInMs() / 2);
		h.postAtTime(new TextRunnable(h, "zwei drittel des ganzen workouts, weiter so"), fStartTime + 2 * (fWorkout.fIntervals.getDurationInMs() / 3));
		fEndTime = fStartTime + fWorkout.fIntervals.getDurationInMs();
		h.postAtTime(new TextRunnable(h, "fertig"), fEndTime);
		h.postAtTime(new Runnable() {
			public void run() {
				stopSelf();
			}
		}, fEndTime + 5 * ONE_SECOND);

		// add minute tickers to each interval
		long startOfInterval = fStartTime;
		for (Interval interval : fWorkout.fIntervals.asList()) {
			long next = startOfInterval;
			h.postAtTime(new TextRunnable(h, "los gehts mit interval " + interval.fComment + " für " + interval.getMinutes() + " minuten"), next);
			next += ONE_MINUTE;
			while (true) {
				if (next < interval.getEndOfInterval(startOfInterval)) {
					h.postAtTime(new SoundRunnable(this), next);
				} else {
					break;
				}
				next += ONE_MINUTE;
			}
			startOfInterval = interval.getEndOfInterval(startOfInterval);
		}

		// add countdown to end of interval
		startOfInterval = fStartTime;
		for (Interval interval : fWorkout.fIntervals.asList()) {
			long end = interval.getEndOfInterval(startOfInterval);
			addCountdown(h, end, 30);
			addCountdown(h, end, 15);
			addCountdown(h, end, 10);
			for (int i = 5; i > 0; i--) {
				addCountdown(h, end, i);
			}
			startOfInterval = end;
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
	private long fStartTime;
	private long fEndTime;

	@Override
	public IBinder onBind(Intent intent) {
		return fBinder;
	}

	public int getActiveIndex() {
		return fCurrentIndex;
	}

	/** 
	 * @return Percentage from 0..1
	 */
	public float getPercentage() {
		float duration = fEndTime - fStartTime;
		float current = SystemClock.currentThreadTimeMillis() - fStartTime;
	  return current / duration;
  }

}
