package de.evosec.fotilo;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.hardware.Camera.*;

/**
 * Created by Christian on 19.02.2016.
 */
public class Preview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private DrawingView drawingView;

    private AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if(success) {
                camera.cancelAutoFocus();
            }
        }
    };

    public Preview(Context context, Camera camera, DrawingView drawingView) {
        super(context);
        this.camera = camera;
        this.drawingView = drawingView;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        //only for Android versions under 3.0
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            if(camera.getParameters().getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                camera.autoFocus(autoFocusCallback);
            }
        } catch (IOException e) {
            Log.d("PREVIEW", "Error setting camera preview: " + e.getStackTrace());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        /*if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }*/
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        FrameLayout parent = (FrameLayout) getParent();
        if (changed && parent.getChildCount() > 0) {
            final View child = parent.getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if(camera != null) {
                Camera.Size mPreviewSize = camera.getParameters().getPreviewSize();
                if (mPreviewSize != null) {
                    previewWidth = mPreviewSize.width;
                    previewHeight = mPreviewSize.height;
                }
            }
            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("MyCam", "surfaceChanged()");
        try {
            requestLayout();
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Log.d("PREVIEW", "Error setting camera preview: " + e.getStackTrace());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void doTouchFocus(final Rect tfocusRect) {
        try {
            List<Area> focusList = new ArrayList<Area>();
            Area focusArea = new Area(tfocusRect, 1000);
            focusList.add(focusArea);

            Camera.Parameters params = camera.getParameters();
            params.setFocusAreas(focusList);
            camera.setParameters(params);

            camera.autoFocus(autoFocusCallback);
        } catch (Exception e) {
            Log.d("MyCam", e.getMessage());
            Log.d("MyCam", "Autofokus nicht möglich");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            Rect touchRect = new Rect((int)(x-100),(int)(y-100),(int)(x+100),(int)(y+100));

            final Rect targetFocusRect = new Rect(
                    touchRect.left * 2000/this.getWidth() - 1000,
                    touchRect.top * 2000/this.getHeight() - 1000,
                    touchRect.right * 2000/this.getWidth() - 1000,
                    touchRect.bottom * 2000/this.getHeight() - 1000);

            doTouchFocus(targetFocusRect);
            if (true) {//drawingViewSet) { //TODO
                drawingView.setHaveTouch(true, touchRect);
                drawingView.invalidate();

                // Remove the square indicator after 1000 msec
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        drawingView.setHaveTouch(false, new Rect(0,0,0,0));
                        drawingView.invalidate();
                    }
                }, 1000);
            }

        }

        return false;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }
}
