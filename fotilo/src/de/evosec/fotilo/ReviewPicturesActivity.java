package de.evosec.fotilo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ReviewPicturesActivity extends AppCompatActivity implements View.OnClickListener {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewPicturesActivity.class);

    private ArrayList<String> pictureUris;
    private Button btn_ok;
    private Button btn_delete;
    private Button btn_fertig;
    private ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_pictures);
        Bundle bundle = getIntent().getBundleExtra("data");
        pictureUris = bundle.getStringArrayList("pictures");
        final GridView pictureGrid = (GridView) findViewById(R.id.pictureGrid);
        this.btn_delete = (Button) findViewById(R.id.btn_delete);
        this.btn_ok = (Button) findViewById(R.id.btn_ok);
        this.btn_fertig = (Button) findViewById(R.id.btn_fertig);
        btn_delete.setOnClickListener(this);
        btn_ok.setOnClickListener(this);
        btn_fertig.setOnClickListener(this);
        imageAdapter = new ImageAdapter(this, getContentResolver(), pictureUris, btn_delete);
        pictureGrid.setAdapter(imageAdapter);
        pictureGrid.setOnItemClickListener(imageAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_delete:
                deletePictures();
                break;
            case  R.id.btn_ok:
                returnPictures(Activity.RESULT_OK);
                break;
            case R.id.btn_fertig:
                returnPictures(Activity.RESULT_FIRST_USER);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                returnPictures(Activity.RESULT_OK);
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
        ArrayList<String> selectedPictureUris = imageAdapter.getSelectedUris();
        for (String uri : selectedPictureUris) {
            LOG.debug("Picture deleted: " + uri);
            getContentResolver().delete(Uri.parse(uri), null, null);
        }
        this.pictureUris.removeAll(selectedPictureUris);
        imageAdapter.setSelectedUris(new ArrayList<String>());
        imageAdapter.notifyDataSetChanged();
    }
}
