package de.evosec.fotilo.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Toast;

public class FotiloActivity extends AppCompatActivity
        implements SensorEventListener {

	private static final Logger LOG =
	        LoggerFactory.getLogger(FotiloActivity.class);

	private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 123;
	private SensorManager sensorManager;
	private int orientation = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorManager.registerListener(this,
		    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
		    SensorManager.SENSOR_DELAY_NORMAL);
		if (savedInstanceState == null && checkAndRequestPermissions()) {
			startCameraFragment();
		}
	}

	private boolean checkAndRequestPermissions() {
		int cameraPermission = ContextCompat.checkSelfPermission(this,
		    Manifest.permission.CAMERA);
		int storagePermission = ContextCompat.checkSelfPermission(this,
		    Manifest.permission.WRITE_EXTERNAL_STORAGE);
		List<String> listPermissionsNeeded = new ArrayList<>();
		if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
			listPermissionsNeeded.add(Manifest.permission.CAMERA);
		}
		if (storagePermission != PackageManager.PERMISSION_GRANTED) {
			listPermissionsNeeded
			    .add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		}
		if (!listPermissionsNeeded.isEmpty()) {
			ActivityCompat.requestPermissions(this,
			    listPermissionsNeeded
			        .toArray(new String[listPermissionsNeeded.size()]),
			    REQUEST_ID_MULTIPLE_PERMISSIONS);
			return false;
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
	        String[] permissions, int[] grantResults) {
		LOG.debug("Permission callback called-------");
		switch (requestCode) {
		case REQUEST_ID_MULTIPLE_PERMISSIONS:
			handleMultiplePermissionsRequest(requestCode, permissions,
			    grantResults);
			break;
		default:
			break;
		}
	}

	private void handleMultiplePermissionsRequest(int requestCode,
	        String[] permissions, int[] grantResults) {
		Map<String, Integer> perms = new HashMap<>();
		// Initialize the map with both permissions
		perms.put(Manifest.permission.CAMERA,
		    PackageManager.PERMISSION_GRANTED);
		perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE,
		    PackageManager.PERMISSION_GRANTED);
		// Fill with actual results from user
		if (grantResults.length > 0) {
			for (int i = 0; i < permissions.length; i++) {
				perms.put(permissions[i], grantResults[i]);
			}
			// Check for both permissions
			if (perms.get(
			    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
			        && perms.get(
			            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				LOG.debug("camera & writeExternalStorage permission granted");
				// process the normal flow
				startCameraFragment();
				// else any one or both the permissions are not granted
			} else {
				LOG.debug("Some permissions are not granted ask again ");
				// permission is denied (this is the first time, when "never
				// ask again" is not checked) so ask again explaining the
				// usage of permission
				// shouldShowRequestPermissionRationale will return true
				// show the dialog or snackbar saying its necessary and try
				// again otherwise proceed with setup.
				if (ActivityCompat.shouldShowRequestPermissionRationale(this,
				    Manifest.permission.CAMERA)
				        || ActivityCompat.shouldShowRequestPermissionRationale(
				            this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					showMessageOKCancel(
					    "Kamera- und Speicherkartenzugriff sind für diese App notwendig!",
					    new DialogInterface.OnClickListener() {

						    @Override
						    public void onClick(DialogInterface dialog,
						            int which) {
							    switch (which) {
							    case DialogInterface.BUTTON_POSITIVE:
								    if (checkAndRequestPermissions()) {
									    startCameraFragment();
								    }
								    break;
							    case DialogInterface.BUTTON_NEGATIVE:
								    // proceed with logic by disabling the
								    // related features or quit the app.
								    finish();
								    break;
							    default:
								    break;
							    }
						    }
					    });
				} else {
					// permission is denied (and never ask again is checked)
					// shouldShowRequestPermissionRationale will return false
					Toast
					    .makeText(this, "Go to settings and enable permissions",
					        Toast.LENGTH_LONG)
					    .show();
					// proceed with logic by disabling the related features
					// or quit the app.
					finish();
				}
			}
		}
	}

	private void showMessageOKCancel(String message,
	        DialogInterface.OnClickListener okListener) {
		new AlertDialog.Builder(FotiloActivity.this).setMessage(message)
		    .setPositiveButton("OK", okListener)
		    .setNegativeButton("Cancel", null).create().show();
	}

	private void startCameraFragment() {
		LOG.info("start Fragment");
		getSupportFragmentManager().beginTransaction()
		    .add(R.id.rootView, CamFragment.newInstance(), "CamFragment")
		    .commitAllowingStateLoss();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		try {
			if (((CamFragment) getSupportFragmentManager()
			    .findFragmentByTag("CamFragment")).onKeyUp(keyCode)) {
				return true;
			} else {
				return super.onKeyUp(keyCode, event);
			}
		} catch (Exception e) {
			LOG.debug("onKeyUp()", e);
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			if (((CamFragment) getSupportFragmentManager()
			    .findFragmentByTag("CamFragment")).onKeyDown(keyCode)) {
				return true;
			} else {
				return super.onKeyDown(keyCode, event);
			}
		} catch (Exception e) {
			LOG.debug("onKeyDown()", e);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.values[1] < 6.5 && event.values[1] > -6.5) {
			if (orientation != 1) {
				// Landscape
				((CamFragment) getSupportFragmentManager()
				    .findFragmentByTag("CamFragment")).rotateLandscape();
			}
			orientation = 1;
		} else {
			if (orientation != 0) {
				((CamFragment) getSupportFragmentManager()
				    .findFragmentByTag("CamFragment")).rotatePortrait();
			}
			orientation = 0;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	protected void onPause() {
		sensorManager.unregisterListener(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this,
		    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
		    SensorManager.SENSOR_DELAY_NORMAL);
	}
}
