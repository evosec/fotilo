package de.evosec.fotilo.library;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageButton;

public class ShowThumbnailTask extends AsyncTask<Uri, Void, Bitmap> {

	private static final Logger LOG =
	        LoggerFactory.getLogger(ShowThumbnailTask.class);
	private static final int THUMBNAIL_SIZE = 100;

	private final ContentResolver contentResolver;
	private final ImageButton pictureReview;

	public ShowThumbnailTask(ImageButton imageButton,
	        ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
		this.pictureReview = imageButton;
	}

	@Override
	protected Bitmap doInBackground(Uri... uris) {
		Bitmap bm = null;
		try {
			bm = getThumbnail(uris[0]);
		} catch (IOException e) {
			LOG.debug("" + e);
		}
		return bm;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		pictureReview.setImageBitmap(result);
	}

	@SuppressWarnings("resource")
	private Bitmap getThumbnail(Uri uri) throws IOException {
		BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();

		InputStream input = null;
		try {
			input = contentResolver.openInputStream(uri);

			onlyBoundsOptions.inJustDecodeBounds = true;
			onlyBoundsOptions.inDither = true;// optional
			onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
			BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
		} finally {
			if (input != null) {
				input.close();
			}
		}

		if (onlyBoundsOptions.outWidth == -1
		        || onlyBoundsOptions.outHeight == -1) {
			return null;
		}

		int originalSize =
		        onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth
		                ? onlyBoundsOptions.outHeight
		                : onlyBoundsOptions.outWidth;

		double ratio = originalSize > THUMBNAIL_SIZE
		        ? originalSize / (double) THUMBNAIL_SIZE : 1.0;

		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
		bitmapOptions.inDither = true;// optional
		bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional

		try {
			input = contentResolver.openInputStream(uri);
			return BitmapFactory.decodeStream(input, null, bitmapOptions);
		} finally {
			if (input != null) {
				input.close();
			}
		}
	}

	private static int getPowerOfTwoForSampleRatio(double ratio) {
		int k = Integer.highestOneBit((int) Math.floor(ratio));
		if (k == 0) {
			return 1;
		} else {
			return k;
		}
	}
}
