package de.evosec.fotilo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class ReviewPicturesActivity extends Activity
        implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

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
		final RecyclerView pictureGrid =
		        (RecyclerView) findViewById(R.id.recycler_view);
		pictureGrid.setHasFixedSize(true);
		RecyclerView.LayoutManager layoutManager =
		        new GridLayoutManager(getApplicationContext(), 4);
		pictureGrid.setLayoutManager(layoutManager);
		ImageButton showMenuButton =
		        (ImageButton) findViewById(R.id.menuToggle);
		showMenuButton.setOnClickListener(this);
		this.btnDelete = (Button) findViewById(R.id.btn_delete);
		this.btnOk = (Button) findViewById(R.id.btn_ok);
		this.btnFertig = (Button) findViewById(R.id.btn_done);
		btnDelete.setOnClickListener(this);
		btnOk.setOnClickListener(this);
		btnFertig.setOnClickListener(this);
		imageAdapter = new ImageAdapter(this, pictureUris, btnDelete);
		pictureGrid.setAdapter(imageAdapter);
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
		case R.id.btn_done:
			returnPictures(Activity.RESULT_FIRST_USER);
			break;
		case R.id.menuToggle:
			showMenu();
			break;
		default:
			break;
		}
	}

	private void showMenu() {
		View view = findViewById(R.id.menuToggle);
		PopupMenu popup = new PopupMenu(this, view);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.menu, popup.getMenu());
		popup.setOnMenuItemClickListener(this);
		popup.show();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.showPrivacyPolicy:
			Intent intent = new Intent(Intent.ACTION_VIEW,
			    Uri.parse("http://www.evosec.de/datenschutz"));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			return true;
		default:
			return false;
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
		List<String> selectedPictureUris = imageAdapter.getSelectedUris();
		for (String uri : selectedPictureUris) {
			LOG.debug("Picture deleted: " + uri);
			getContentResolver().delete(Uri.parse(uri), null, null);
		}
		this.pictureUris.removeAll(selectedPictureUris);
		imageAdapter.resetSelections();
		imageAdapter.notifyDataSetChanged();
	}
}
