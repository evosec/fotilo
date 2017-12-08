package de.evosec.fotilo;

import android.hardware.Camera;

public final class ZoomUtil {

	private static int maxZoomLevel;
	private static int currentZoomLevel;

	public static int getCurrentZoomLevel() {
		return currentZoomLevel;
	}

	public static int getMaxZoomLevel() {
		return maxZoomLevel;
	}

	public static void setCurrentZoomLevel(int currentZoomLevel) {
		ZoomUtil.currentZoomLevel = currentZoomLevel;
	}

	public static void setMaxZoomLevel(int maxZoomLevel) {
		ZoomUtil.maxZoomLevel = maxZoomLevel;
	}

	public static int zoomIn(Camera camera) {
		if (camera != null) {
			Camera.Parameters params = camera.getParameters();
			if (currentZoomLevel < maxZoomLevel
			        && params.getZoom() == currentZoomLevel) {
				currentZoomLevel++;
				params.setZoom(currentZoomLevel);
				camera.setParameters(params);

			}
		}
		return currentZoomLevel;
	}

	public static int zoom(Camera camera, float factor) {
		if (camera != null) {
			int sum;
			if (maxZoomLevel % 2 == 0) {
				sum = (int) Math.rint((maxZoomLevel * 0.1));
			} else {
				sum = (int) Math.round((maxZoomLevel * 0.1));
				if (sum % 2 != 2 && sum != 1) {
					sum -= 1;
				}
			}
			Camera.Parameters params = camera.getParameters();
			if (factor > 1.0f && (currentZoomLevel < maxZoomLevel + sum)) {
				currentZoomLevel += sum;
			} else if (factor < 1.0f && (currentZoomLevel > sum)) {
				currentZoomLevel -= sum;
			}
			// Math.max(0.1f, Math.min(factor, 5.0f));
			if (currentZoomLevel < 0)
				currentZoomLevel = 0;
			if (currentZoomLevel > maxZoomLevel)
				currentZoomLevel = maxZoomLevel;
			if (!camera.getParameters().isSmoothZoomSupported()) {
				params.setZoom(currentZoomLevel);
				camera.setParameters(params);
			}
		}
		return currentZoomLevel;
	}

	public static void updateCurrentZoom(Camera camera, int zoom) {
		if (camera != null) {
			ZoomUtil.currentZoomLevel = zoom;
			Camera.Parameters params = camera.getParameters();
			params.setZoom(zoom);
			camera.setParameters(params);
		}
	}

	public static int zoomOut(Camera camera) {
		if (camera != null) {
			Camera.Parameters params = camera.getParameters();
			if (currentZoomLevel > 0 && params.getZoom() == currentZoomLevel) {
				currentZoomLevel--;
				params.setZoom(currentZoomLevel);
				camera.setParameters(params);
			}
		}
		return currentZoomLevel;
	}

}
