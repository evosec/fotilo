package de.evosec.fotilo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment
 * must implement the {@link CamFragment.OnFragmentInteractionListener}
 * interface to handle interaction events. Use the
 * {@link CamFragment#newInstance} factory method to create an instance of this
 * fragment.
 */
public class CamFragment extends Fragment
        implements View.OnClickListener, Camera.PictureCallback {

	private static final Logger LOG =
	        LoggerFactory.getLogger(CamFragment.class);
	private static final int REVIEW_PICTURES_ACTIVITY_REQUEST = 123;

	private static int MEDIA_TYPE_IMAGE = 1;
	private int maxPictures;
	private int picturesTaken;
	private ArrayList<String> pictures;
	private Intent resultIntent;
	private Bundle resultBundle;
	private Camera camera;
	private Preview preview;
	private DrawingView drawingView;
	private RadioGroup flashmodes;
	private int maxZoomLevel;
	private int currentZoomLevel;
	private ProgressDialog progress;
	private boolean safeToTakePicture = false;

	public CamFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 *
	 * @return A new instance of fragment CamFragment.
	 */
	public static CamFragment newInstance() {
		return new CamFragment();
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		// wenn maxPictures noch nicht erreicht
		if (picturesTaken < maxPictures || maxPictures == 0) {
			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				LOG.debug(
				    "Konnte Daei nicht erstellen, Berechtigungen überprüfen");
				return;
			}
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
				final Uri imageUri =
				        getImageContentUri(getContext(), pictureFile);
				pictures.add(imageUri.toString());
				showLastPicture(imageUri);
				picturesTaken++;
				if (maxPictures > 0) {
					Toast.makeText(getContext(),
					    "Picture " + picturesTaken + " / " + maxPictures,
					    Toast.LENGTH_SHORT).show();
				} else {
					LOG.debug("Picture " + picturesTaken + " / " + maxPictures);
				}
				displayPicturesTaken();
				// set Result
				resultBundle.putStringArrayList("pictures", pictures);
				resultIntent.putExtra("data", resultBundle);

				sendNewPictureBroadcast(imageUri);
				camera.startPreview();
				safeToTakePicture = true;
			} catch (FileNotFoundException e) {
				LOG.debug("File not found: " + e);
			} catch (IOException e) {
				LOG.debug("Error accessing file: " + e);
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						LOG.debug("" + e);
					}
				}
				progress.dismiss();
			}
		}

		// wenn maxPictures erreicht, Bildpfade zurückgeben
		if (picturesTaken == maxPictures) {
			LOG.debug("maxPictures erreicht");
			getActivity().setResult(Activity.RESULT_OK, resultIntent);
			getActivity().finish();
		}
	}

	private void displayPicturesTaken() {
		TextView txtpicturesTaken =
		        (TextView) getActivity().findViewById(R.id.picturesTaken);
		txtpicturesTaken.setText("Bilder: " + picturesTaken);
	}

	private void showLastPicture(Uri imageUri) {
		ImageButton pictureReview =
		        (ImageButton) getActivity().findViewById(R.id.pictureReview);
		pictureReview.setOnClickListener(this);
		pictureReview.setVisibility(View.VISIBLE);
		new ShowThumbnailTask(pictureReview, getActivity().getContentResolver())
		    .execute(imageUri);
	}

	private void sendNewPictureBroadcast(Uri imageUri) {
		Intent intent = new Intent("com.android.camera.NEW_PICTURE");
		try {
			intent.setData(imageUri);
		} catch (Exception e) {
			LOG.debug("" + e);
		}
		getActivity().sendBroadcast(intent);
	}

	private static Uri getImageContentUri(Context context, File imageFile) {
		String filePath = imageFile.getAbsolutePath();
		Cursor cursor = context.getContentResolver().query(
		    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		    new String[] {MediaStore.Images.Media._ID},
		    MediaStore.Images.Media.DATA + "=? ", new String[] {filePath},
		    null);
		if (cursor != null && cursor.moveToFirst()) {
			int id = cursor
			    .getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
			Uri baseUri = Uri.parse("content://media/external/images/media");
			return Uri.withAppendedPath(baseUri, "" + id);
		} else {
			if (imageFile.exists()) {
				ContentValues values = new ContentValues();
				values.put(MediaStore.Images.Media.DATA, filePath);
				return context.getContentResolver().insert(
				    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			} else {
				return null;
			}
		}
	}

	private static File getOutputMediaFile(int type) {
		File storageDir =
		        new File(Environment.getExternalStoragePublicDirectory(
		            Environment.DIRECTORY_PICTURES), "MyCam");

		// Wenn Verzeichnis nicht existiert, erstellen
		if (!storageDir.exists() && !storageDir.mkdirs()) {
			LOG.debug("Konnte Bilderverzeichnis nicht erstellen!");
			return null;
		}

		// Dateinamen erzeugen
		String timeStmp =
		        new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(storageDir.getPath() + File.separator + "IMG_"
			        + timeStmp + ".jpg");
		} else {
			return null;
		}

		return mediaFile;
	}

	private void initFlashmodes() {
		flashmodes =
		        (RadioGroup) getView().findViewById(R.id.radioGroup_Flashmodes);
		List<String> supportedFlashModes =
		        camera.getParameters().getSupportedFlashModes();
		if (supportedFlashModes != null) {
			for (String flashMode : supportedFlashModes) {
				switch (flashMode) {
				case Camera.Parameters.FLASH_MODE_AUTO:
					getView().findViewById(R.id.radio_flashmode_auto)
					    .setVisibility(View.VISIBLE);
					break;
				case Camera.Parameters.FLASH_MODE_ON:
					getView().findViewById(R.id.radio_flashmode_on)
					    .setVisibility(View.VISIBLE);
					break;
				case Camera.Parameters.FLASH_MODE_OFF:
					getView().findViewById(R.id.radio_flashmode_off)
					    .setVisibility(View.VISIBLE);
					break;
				case Camera.Parameters.FLASH_MODE_RED_EYE:
					getView().findViewById(R.id.radio_flashmode_redEye)
					    .setVisibility(View.VISIBLE);
					break;
				default:
					break;
				}
			}
			setDefaultFlashmode();
			updateFlashModeIcon();
			initSelectedFlashmode();
		} else {
			flashmodes.setVisibility(View.INVISIBLE);
		}
	}

	private void initSelectedFlashmode() {
		if (camera.getParameters().getSupportedFlashModes() != null) {
			switch (camera.getParameters().getFlashMode()) {
			case Camera.Parameters.FLASH_MODE_AUTO:
				((RadioButton) getView()
				    .findViewById(R.id.radio_flashmode_auto)).setChecked(true);
				break;
			case Camera.Parameters.FLASH_MODE_ON:
				((RadioButton) getView().findViewById(R.id.radio_flashmode_on))
				    .setChecked(true);
				break;
			case Camera.Parameters.FLASH_MODE_OFF:
				((RadioButton) getView().findViewById(R.id.radio_flashmode_off))
				    .setChecked(true);
				break;
			case Camera.Parameters.FLASH_MODE_RED_EYE:
				((RadioButton) getView()
				    .findViewById(R.id.radio_flashmode_redEye))
				        .setChecked(true);
				break;
			default:
				break;
			}
		}
	}

	public void onRadioButtonClicked(View view) {
		String flashMode = "";
		switch (view.getId()) {
		case R.id.radio_flashmode_auto:
			flashMode = Camera.Parameters.FLASH_MODE_AUTO;
			break;
		case R.id.radio_flashmode_on:
			flashMode = Camera.Parameters.FLASH_MODE_ON;
			break;
		case R.id.radio_flashmode_off:
			flashMode = Camera.Parameters.FLASH_MODE_OFF;
			break;
		case R.id.radio_flashmode_redEye:
			flashMode = Camera.Parameters.FLASH_MODE_RED_EYE;
			break;
		default:
			break;
		}
		Camera.Parameters params = camera.getParameters();
		params.setFlashMode(flashMode);
		camera.setParameters(params);
		updateFlashModeIcon();
		flashmodes.setVisibility(View.INVISIBLE);
		Toast.makeText(getContext(), "Flashmode = " + flashMode,
		    Toast.LENGTH_SHORT).show();
	}

	private void setDefaultFlashmode() {
		Camera.Parameters params = camera.getParameters();
		if (camera.getParameters().getSupportedFlashModes()
		    .contains(Camera.Parameters.FLASH_MODE_AUTO)) {
			params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
			camera.setParameters(params);
		}
	}

	private void findOptimalPictureSize() {
		// Ausgewählte Auflösung von rufender Activity einstellen
		Intent i = getActivity().getIntent();
		Camera.Parameters params = camera.getParameters();
		int w = 0;
		int h = 0;
		double ratio = 0;
		w = i.getIntExtra("width", w);
		h = i.getIntExtra("height", h);
		ratio = i.getDoubleExtra("aspectratio", ratio);
		LOG.debug("w = " + w + "; h = " + h + "; ratio = " + ratio);
		Camera.Size bestSize = null;
		if (w > 0 && h > 0) {
			// Mindestauflösung setzen
			findOptimalPictureSizeBySize(w, h);
		} else if (ratio > 0) {
			bestSize = getLargestResolutionByAspectRatio(
			    camera.getParameters().getSupportedPictureSizes(), ratio);
			if (bestSize.width == camera.getParameters().getPreviewSize().width
			        && bestSize.height == camera.getParameters()
			            .getPreviewSize().height) {
				// Errormeldung keine Auflösung mit passendem Seitenverhältnis
				// gefunden
				String error =
				        "Fehler: Keine Auflösung mit diesem Seitenverhältnis verfügbar!";
				Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
				resultBundle.putString("error", error);
				resultIntent.putExtra("data", resultBundle);
				getActivity().setResult(Activity.RESULT_CANCELED, resultIntent);
				getActivity().finish();
				return;
			}
			configurePictureSize(bestSize, params);
			LOG.debug(bestSize.width + " x " + bestSize.height);
		} else {
			// keine Auflösung vorgegeben: höchste 4:3 Auflösung wählen
			configureLargestFourToThreeRatioPictureSize();
		}
	}

	public void configurePictureSize(Camera.Size size,
	        Camera.Parameters params) {
		params.setPictureSize(size.width, size.height);
		camera.setParameters(params);
		scalePreviewSize();
	}

	public void scalePreviewSize() {
		Camera.Size pictureSize = camera.getParameters().getPictureSize();
		Camera.Size previewSize = camera.getParameters().getPreviewSize();
		LOG.debug(
		    "PictureSize = " + pictureSize.width + " x " + pictureSize.height);
		double pictureRatio =
		        (double) pictureSize.width / (double) pictureSize.height;
		previewSize = getLargestResolutionByAspectRatio(
		    camera.getParameters().getSupportedPreviewSizes(), pictureRatio);
		LOG.debug(
		    "PreviewSize = " + previewSize.width + " x " + previewSize.height);
		configurePreviewSize(previewSize);
	}

	public Camera.Size getLargestResolutionByAspectRatio(
	        List<Camera.Size> sizes, double aspectRatio) {
		Camera.Size largestSize = camera.getParameters().getPreviewSize();
		largestSize.width = 0;
		largestSize.height = 0;
		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / (double) size.height;
			if (Math.abs(ratio - aspectRatio) < 0.00000001
			        && size.width >= largestSize.width
			        && size.height >= largestSize.height) {
				largestSize = size;
			}
		}
		return largestSize;
	}

	public void configurePreviewSize(Camera.Size size) {
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		Camera.Parameters params = camera.getParameters();
		int screenWidth = display.getWidth();
		int screenHeight = display.getHeight();
		Camera.Size bestPreviewSize = size;
		params.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
		camera.setParameters(params);
		preview.getLayoutParams().width = bestPreviewSize.width;
		preview.getLayoutParams().height = bestPreviewSize.height;

		FrameLayout frameLayout =
		        (FrameLayout) getView().findViewById(R.id.preview);
		RelativeLayout.LayoutParams layoutPreviewParams =
		        (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
		layoutPreviewParams.width = bestPreviewSize.width;
		layoutPreviewParams.height = bestPreviewSize.height;
		layoutPreviewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		frameLayout.setLayoutParams(layoutPreviewParams);

		LOG.debug("screenSize = " + screenWidth + " x " + screenHeight);

		LOG.debug("PreviewSize = " + bestPreviewSize.width + " x "
		        + bestPreviewSize.height);
	}

	private void configureLargestFourToThreeRatioPictureSize() {
		Camera.Parameters params = camera.getParameters();
		List<Camera.Size> supportedPictureSizes =
		        params.getSupportedPictureSizes();
		Camera.Size bestSize = params.getPictureSize();
		bestSize.width = 0;
		bestSize.height = 0;
		double fourToThreeRatio = 4.0 / 3.0;
		for (Camera.Size supportedSize : supportedPictureSizes) {
			if (Math
			    .abs((double) supportedSize.width / supportedSize.height
			            - fourToThreeRatio) == 0
			        && supportedSize.width >= bestSize.width
			        && supportedSize.height >= bestSize.height) {
				bestSize = supportedSize;
			}
		}
		params.setPictureSize(bestSize.width, bestSize.height);
		camera.setParameters(params);
		configurePictureSize(bestSize, params);
		LOG.debug(bestSize.width + " x " + bestSize.height);
	}

	private void findOptimalPictureSizeBySize(int w, int h) {
		Camera.Parameters params = camera.getParameters();
		double tempDiff = 0;
		double diff = Integer.MAX_VALUE;
		Camera.Size bestSize = null;
		for (Camera.Size supportedSize : params.getSupportedPictureSizes()) {
			// nächst größere Auflösung suchen
			if (supportedSize.width >= w && supportedSize.height >= h) {
				// Pythagoras
				tempDiff = Math
				    .sqrt(Math.pow((double) supportedSize.width - w, 2)
				            + Math.pow((double) supportedSize.height - h, 2));
				// minimalste Differenz suchen
				if (tempDiff < diff) {
					diff = tempDiff;
					bestSize = supportedSize;
				}
			}
		}
		// beste Auflösung setzen
		if (bestSize != null) {
			configurePictureSize(bestSize, params);
			LOG.debug(bestSize.width + " x " + bestSize.height + " px");
		} else {
			// Fehlermeldung zurückgeben
			String error = "Fehler: Auflösung zu hoch!";
			Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
			resultBundle.putString("error", error);
			resultIntent.putExtra("data", resultBundle);
			getActivity().setResult(Activity.RESULT_CANCELED, resultIntent);
			getActivity().finish();
		}
	}

	public void onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			resultIntent.putExtra("data", resultBundle);
			getActivity().setResult(Activity.RESULT_CANCELED, resultIntent);
			getActivity().finish();
		} else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
			if (safeToTakePicture) {
				takePicture();
			}
		} else if (keyCode == KeyEvent.KEYCODE_ZOOM_IN) {
			zoomIn();
		} else if (keyCode == KeyEvent.KEYCODE_ZOOM_OUT) {
			zoomOut();
		}
	}

	private void takePicture() {
		// Anwender signalisieren, dass ein Bild aufgenommen wird
		progress = ProgressDialog.show(getActivity(), "Speichern",
		    "Bild wird gespeichert...");
		MediaActionSound sound = new MediaActionSound();
		sound.play(MediaActionSound.SHUTTER_CLICK);
		camera.takePicture(null, null, this);
		safeToTakePicture = false;
	}

	private void initPreview() {
		drawingView = (DrawingView) getView().findViewById(R.id.drawingView);
		preview = new Preview(getContext(), camera, drawingView);
		FrameLayout frameLayout =
		        (FrameLayout) getView().findViewById(R.id.preview);
		frameLayout.addView(preview);
		safeToTakePicture = true;
	}

	private void zoomIn() {
		if (camera != null) {
			Camera.Parameters params = camera.getParameters();
			if (this.currentZoomLevel < this.maxZoomLevel) {
				currentZoomLevel++;
				params.setZoom(this.currentZoomLevel);
				camera.setParameters(params);
				viewCurrentZoom();
			}
		}
	}

	private void zoomOut() {
		if (camera != null) {
			Camera.Parameters params = camera.getParameters();
			if (this.currentZoomLevel > 0) {
				currentZoomLevel--;
				params.setZoom(this.currentZoomLevel);
				camera.setParameters(params);
				viewCurrentZoom();
			}
		}
	}

	private void viewCurrentZoom() {
		TextView txtCurrentZoom =
		        (TextView) getView().findViewById(R.id.txtCurrentZoom);
		txtCurrentZoom.setVisibility(View.VISIBLE);
		txtCurrentZoom.setText(
		    "Zoom: " + this.currentZoomLevel + " / " + this.maxZoomLevel);
	}

	private void updateFlashModeIcon() {
		ImageButton btnFlashmode =
		        (ImageButton) getView().findViewById(R.id.btn_flashmode);
		if (camera.getParameters().getSupportedFlashModes() != null) {
			switch (camera.getParameters().getFlashMode()) {
			case Camera.Parameters.FLASH_MODE_AUTO:
				btnFlashmode
				    .setImageResource(R.drawable.ic_flash_auto_black_24dp);
				break;
			case Camera.Parameters.FLASH_MODE_ON:
				btnFlashmode
				    .setImageResource(R.drawable.ic_flash_on_black_24dp);
				break;
			case Camera.Parameters.FLASH_MODE_OFF:
				btnFlashmode
				    .setImageResource(R.drawable.ic_flash_off_black_24dp);
				break;
			case Camera.Parameters.FLASH_MODE_RED_EYE:
				btnFlashmode
				    .setImageResource(R.drawable.ic_remove_red_eye_black_24dp);
				break;
			default:
				break;
			}
		} else {
			btnFlashmode.setVisibility(View.INVISIBLE);
		}
	}

	private void releaseCamera() {
		if (camera != null) {
			camera.stopPreview();
			camera.setPreviewCallback(null);
			if (preview != null) {
				preview.getHolder().removeCallback(preview);
			}
			camera.release();
			camera = null;
			if (preview != null && preview.getCamera() != null) {
				preview.setCamera(camera);
			}
			LOG.debug("camera released");
		}
	}

	@Override
	public void onPause() {
		LOG.debug("onPause()");
		super.onPause();
		releaseCamera();
	}

	@Override
	public void onDestroyView() {
		LOG.debug("onDestroyView()");
		super.onDestroyView();
		releaseCamera();
	}

	@Override
	public void onResume() {
		super.onResume();
		Intent i = getActivity().getIntent();
		this.maxPictures = i.getIntExtra("maxPictures", maxPictures);
		LOG.debug("onResume() maxPictures = " + maxPictures);
		if (camera == null) {
			camera = getCameraInstance();
			if (preview != null && preview.getCamera() == null) {
				preview.setCamera(camera);
			}

		}
	}

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception ex) {
			// Camera in use or does not exist
			LOG.debug("Error: keine Kamera bekommen: " + ex);
		}
		if (c != null) {
			LOG.debug("camera opened");
			LOG.debug("Camera = " + c.toString());
		}
		return c;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		LOG.debug("onCreate()");
		super.onCreate(savedInstanceState);
		resultIntent = new Intent();
		resultBundle = new Bundle();
		Intent i = getActivity().getIntent();

		// max. Anzahl Bilder von rufender Activity auslesen
		this.maxPictures = i.getIntExtra("maxPictures", maxPictures);

		LOG.debug("onCreate() maxPictures = " + maxPictures);
		this.picturesTaken = 0;
		this.pictures = new ArrayList<String>();
		camera = getCameraInstance();
		if (preview != null && preview.getCamera() == null) {
			preview.setCamera(camera);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
		LOG.debug("onCreateView()");
		LOG.debug("CamFragment");
		View view = inflater.inflate(R.layout.fragment_cam, container, false);
		ImageButton btnFlashmode =
		        (ImageButton) view.findViewById(R.id.btn_flashmode);
		btnFlashmode.setOnClickListener(this);
		ImageButton btnCapture =
		        (ImageButton) view.findViewById(R.id.btn_capture);
		btnCapture.setOnClickListener(this);
		ImageButton btnZoomin = (ImageButton) view.findViewById(R.id.btnZoomIn);
		btnZoomin.setOnClickListener(this);
		ImageButton btnZoomOut =
		        (ImageButton) view.findViewById(R.id.btnZoomOut);
		btnZoomOut.setOnClickListener(this);
		Button radioFlashmodeAuto =
		        (Button) view.findViewById(R.id.radio_flashmode_auto);
		radioFlashmodeAuto.setOnClickListener(this);
		Button radioFlashmodeOn =
		        (Button) view.findViewById(R.id.radio_flashmode_on);
		radioFlashmodeOn.setOnClickListener(this);
		Button radioFlashmodeOff =
		        (Button) view.findViewById(R.id.radio_flashmode_off);
		radioFlashmodeOff.setOnClickListener(this);
		Button radioFlashmodeRedEye =
		        (Button) view.findViewById(R.id.radio_flashmode_redEye);
		radioFlashmodeRedEye.setOnClickListener(this);
		return view;
	}

	@Override
	public void onStart() {
		LOG.debug("onStart()");
		super.onStart();
		if (camera == null) {
			camera = getCameraInstance();
			if (preview != null && preview.getCamera() == null) {
				preview.setCamera(camera);
			}
		}
		if (camera.getParameters().isZoomSupported()) {
			ImageButton btnZoomin =
			        (ImageButton) getView().findViewById(R.id.btnZoomIn);
			ImageButton btnZoomOut =
			        (ImageButton) getView().findViewById(R.id.btnZoomOut);
			btnZoomin.setVisibility(View.VISIBLE);
			btnZoomOut.setVisibility(View.VISIBLE);
			this.maxZoomLevel = camera.getParameters().getMaxZoom();
			this.currentZoomLevel = 0;
			viewCurrentZoom();
		}
		updateFlashModeIcon();
		initPreview();
		findOptimalPictureSize();
		initFlashmodes();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_flashmode:
			flashmodes.setVisibility(View.VISIBLE);
			break;
		case R.id.btn_capture:
			if (safeToTakePicture) {
				takePicture();
			}
			break;
		case R.id.btnZoomIn:
			zoomIn();
			break;
		case R.id.btnZoomOut:
			zoomOut();
			break;
		case R.id.radio_flashmode_auto:
		case R.id.radio_flashmode_off:
		case R.id.radio_flashmode_on:
		case R.id.radio_flashmode_redEye:
			this.onRadioButtonClicked(v);
			break;
		case R.id.pictureReview:
			startReviewPicturesActivity();
			break;
		default:
			break;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			this.pictures = savedInstanceState.getStringArrayList("pictures");
			this.maxPictures = savedInstanceState.getInt("maxPictures");
			this.picturesTaken = this.pictures.size();
			if (picturesTaken > 0) {
				showLastPicture(
				    Uri.parse(this.pictures.get(picturesTaken - 1)));
			}
			displayPicturesTaken();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putStringArrayList("pictures", pictures);
		outState.putInt("maxPictures", maxPictures);
		outState.putInt("picturesTaken", picturesTaken);
		super.onSaveInstanceState(outState);
	}

	private void startReviewPicturesActivity() {
		Bundle bundle = new Bundle();
		Intent intent = new Intent(getActivity(), ReviewPicturesActivity.class);
		bundle.putStringArrayList("pictures", pictures);
		intent.putExtra("data", bundle);
		startActivityForResult(intent, REVIEW_PICTURES_ACTIVITY_REQUEST);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REVIEW_PICTURES_ACTIVITY_REQUEST:
			if (resultCode == Activity.RESULT_OK
			        || resultCode == Activity.RESULT_FIRST_USER) {
				Bundle bundle = data.getBundleExtra("data");
				this.pictures = bundle.getStringArrayList("pictures");
				this.picturesTaken = this.pictures.size();
				if (pictures.isEmpty()) {
					showLastPicture(
					    Uri.parse(this.pictures.get(pictures.size() - 1)));
				}
				displayPicturesTaken();
				if (resultCode == Activity.RESULT_FIRST_USER) {
					resultIntent.putExtra("data", resultBundle);
					getActivity().setResult(Activity.RESULT_CANCELED,
					    resultIntent);
					getActivity().finish();
				}
			}
			break;
		default:
			break;
		}
	}

	private Context getContext() {
		return getActivity();
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated to
	 * the activity and potentially other fragments contained in that activity.
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {

		void onFragmentInteraction(Uri uri);
	}
}
