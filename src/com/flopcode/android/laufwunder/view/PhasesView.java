package com.flopcode.android.laufwunder.view;

import com.flopcode.android.laufwunder.model.Phase;
import com.flopcode.android.laufwunder.model.Phases;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PhasesView extends View {

	private Phases fPhases;

	public PhasesView(Context context) {
		super(context);
	}

	public PhasesView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public PhasesView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setPhases(Phases p) {
		fPhases = p;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint p = new Paint();
		double totalTime = fPhases.getDurationInMs();
		double w = getWidth();
		double f = w / totalTime;
		double current = 0;
		for (Phase phase : fPhases.asList()) {
			p.setColor(Color.parseColor(phase.fColor));
			double newCurrent = current + phase.getDurationInMs();
			canvas.drawRect((int) (current * f), 0, (int) (newCurrent * f), getHeight(), p);
			current = newCurrent;
		}
	}
}
