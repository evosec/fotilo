package de.evosec.fotilo;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

public class ReviewPicturesActivity extends AppCompatActivity
        implements View.OnClickListener {

	private static final Logger LOG =
	        LoggerFactory.getLogger(ReviewPicturesActivity.class);

	private ArrayList<String> pictureUris;
	private Button btnOk;
	private Button btnDelete;
	private Button btnFertig;
	private ImageAdapter imageAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_review_pictures);
		Bundle bundle = getIntent().getBundleExtra("data");
		pictureUris = bundle.getStringArrayList("pictures");
		final GridView pictureGrid = (GridView) findViewById(R.id.pictureGrid);
		this.btnDelete = (Button) findViewById(R.id.btn_delete);
		this.btnOk = (Button) findViewById(R.id.btn_ok);
		this.btnFertig = (Button) findViewById(R.id.btn_fertig);
		btnDelete.setOnClickListener(this);
		btnOk.setOnClickListener(this);
		btnFertig.setOnClickListener(this);
		imageAdapter = new ImageAdapter(this, getContentResolver(), pictureUris,
		    btnDelete);
		pictureGrid.setAdapter(imageAdapter);
		pictureGrid.setOnItemClickListener(imageAdapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_delete:
			deletePictures();
			break;
		case R.id.btn_ok:
			returnPictures(Activity.RESULT_OK);
			break;
		case R.id.btn_fertig:
			returnPictures(Activity.RESULT_FIRST_USER);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			returnPictures(Activity.RESULT_OK);
			break;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void returnPictures(int resultCode) {
		Intent resIntent = new Intent();
		Bundle resBundle = new Bundle();
		resBundle.putStringArrayList("pictures", this.pictureUris);
		resIntent.putExtra("data", resBundle);
		setResult(resultCode, resIntent);
		finish();
	}

	private void deletePictures() {
		ArrayList<String> selectedPictureUris =
		        (ArrayList<String>) imageAdapter.getSelectedUris();
		for (String uri : selectedPictureUris) {
			LOG.debug("Picture deleted: " + uri);
			getContentResolver().delete(Uri.parse(uri), null, null);
		}
		this.pictureUris.removeAll(selectedPictureUris);
		imageAdapter.setSelectedUris(new ArrayList<String>());
		imageAdapter.notifyDataSetChanged();
	}
}
