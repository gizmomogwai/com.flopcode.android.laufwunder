package com.flopcode.android.laufwunder.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.flopcode.android.laufwunder.model.Interval;
import com.flopcode.android.laufwunder.model.Intervals;

public class IntervalsView extends View {

	private static Intervals fDefaultIntervals;
	static {
		fDefaultIntervals = new Intervals();
		fDefaultIntervals.add(new Interval("hello world", "#ff0000", 10));
		fDefaultIntervals.add(new Interval("hello world", "#00ff00", 10));
		fDefaultIntervals.add(new Interval("hello world", "#0000ff", 10));
	}
	private Intervals fIntervals = fDefaultIntervals;
	private float fPercentage = -1;

	public IntervalsView(Context context) {
		super(context);
	}

	public IntervalsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public IntervalsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setIntervals(Intervals p) {
		fIntervals = p;
	}

	Paint p = new Paint();
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		double totalTime = fIntervals.getDurationInMs();
		double w = getWidth();
		double f = w / totalTime;
		double current = 0;
		for (Interval interval : fIntervals.asList()) {
			p.setColor(Color.parseColor(interval.fColor));
			double newCurrent = current + interval.getDurationInMs();
			canvas.drawRect((int) (current * f), 0, (int) (newCurrent * f), getHeight(), p);
			current = newCurrent;
		}
		if ((fPercentage >= 0) && (fPercentage <= 1)) {
			int pos = (int) (fPercentage * getWidth());
			p.setColor(Color.argb(0xff, 0xff, 0xff, 0));
			canvas.drawLine(pos, 0, pos, getHeight(), p);
		}
	}

	public void setProgress(float percentage) {
		fPercentage  = percentage;
		postInvalidate();
  }
}
