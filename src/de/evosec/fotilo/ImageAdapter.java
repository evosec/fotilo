package de.evosec.fotilo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by Christian on 19.08.2016.
 */
public class ImageAdapter extends BaseAdapter
        implements AdapterView.OnItemClickListener {

	private static final Logger LOG =
	        LoggerFactory.getLogger(ImageAdapter.class);
	private static final int THUMBNAIL_SIZE = 250;
	private static final int SELECTED_COLOR = Color.argb(255, 50, 178, 225);

	private final SparseBooleanArray checkStatus;

	private final Context mContext;
	private final ContentResolver mContentResolver;
	private final ArrayList<String> uris;
	private ArrayList<String> selectedUris;
	private final Button btn_delete;

	public ImageAdapter(Context c, ContentResolver cR, ArrayList<String> uris,
	        Button btn_delete) {
		mContext = c;
		mContentResolver = cR;
		this.uris = uris;
		this.btn_delete = btn_delete;
		this.selectedUris = new ArrayList<>();
		checkStatus = new SparseBooleanArray();
	}

	private void setChecked(int position, boolean b) {
		checkStatus.put(position, b);
	}

	private boolean isChecked(int position) {
		return checkStatus.get(position);
	}

	@Override
	public int getCount() {
		return uris.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	// create a new ImageView for each item referenced by the Adapter
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			// if it's not recycled, initialize some attributes
			imageView = new ImageView(mContext);
			imageView.setLayoutParams(new GridView.LayoutParams(250, 250));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(15, 15, 15, 15);
		} else {
			imageView = (ImageView) convertView;
		}

		try {
			imageView
			    .setImageBitmap(getThumbnail(Uri.parse(uris.get(position))));
		} catch (IOException e) {
			LOG.debug(e.getMessage());
		}
		return imageView;
	}

	private Bitmap getThumbnail(Uri uri)
	        throws FileNotFoundException, IOException {
		InputStream input = mContentResolver.openInputStream(uri);
		BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
		onlyBoundsOptions.inJustDecodeBounds = true;
		onlyBoundsOptions.inDither = true;// optional
		onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
		BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
		input.close();
		if (onlyBoundsOptions.outWidth == -1
		        || onlyBoundsOptions.outHeight == -1) {
			return null;
		}

		int originalSize =
		        onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth
		                ? onlyBoundsOptions.outHeight
		                : onlyBoundsOptions.outWidth;

		double ratio = originalSize > THUMBNAIL_SIZE
		        ? originalSize / THUMBNAIL_SIZE : 1.0;

		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
		bitmapOptions.inDither = true;// optional
		bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
		input = mContentResolver.openInputStream(uri);
		Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
		input.close();
		return bitmap;
	}

	private static int getPowerOfTwoForSampleRatio(double ratio) {
		int k = Integer.highestOneBit((int) Math.floor(ratio));
		if (k == 0) {
			return 1;
		} else {
			return k;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
	        long id) {
		if (view instanceof ImageView) {
			boolean isChecked = !isChecked(position);
			if (isChecked(position)) {
				view.setSelected(false);
				view.setBackgroundColor(Color.TRANSPARENT);
				selectedUris.remove(uris.get(position));
			} else {
				view.setSelected(true);
				view.setBackgroundColor(SELECTED_COLOR);
				selectedUris.add(uris.get(position));
			}
			setChecked(position, isChecked);
			if (selectedUris.size() > 0) {
				btn_delete.setEnabled(true);
			} else {
				btn_delete.setEnabled(false);
			}
		}
	}

	public ArrayList<String> getSelectedUris() {
		return selectedUris;
	}

	public void setSelectedUris(ArrayList<String> selectedUris) {
		this.selectedUris = selectedUris;
	}
}
