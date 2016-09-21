package de.evosec.fotilo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class DrawingView extends View {

	private static int COLOR_WHITE = 0xeed7d7d7;

	private boolean haveTouch = false;
	private Rect touchArea;
	private final Paint paint;

	public DrawingView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		paint = new Paint();
		paint.setColor(COLOR_WHITE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);
		haveTouch = false;
	}

	public void setHaveTouch(boolean val, Rect rect) {
		haveTouch = val;
		touchArea = rect;
	}

	@Override
	public void onDraw(Canvas canvas) {
		bringToFront();
		super.onDraw(canvas);
		if (haveTouch) {
			canvas.drawRect(touchArea.left, touchArea.top, touchArea.right,
			    touchArea.bottom, paint);
		}
	}

}
