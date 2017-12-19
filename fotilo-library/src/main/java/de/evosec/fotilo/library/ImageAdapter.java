package de.evosec.fotilo.library;

import java.util.ArrayList;
import java.util.List;

import com.squareup.picasso.Picasso;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class ImageAdapter
        extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

	private final Context context;
	private final List<String> uris;
	private final Button btnDelete;
	private final SparseBooleanArray selectedPositions =
	        new SparseBooleanArray();
	private final List<String> selectedUris = new ArrayList<>();

	public static class ViewHolder extends RecyclerView.ViewHolder {

		private final ImageView imageView;

		ViewHolder(View itemView) {
			super(itemView);
			imageView = (ImageView) itemView.findViewById(R.id.image);
		}
	}

	public ImageAdapter(Context context, List<String> uris, Button btnDelete) {
		this.context = context;
		this.uris = uris;
		this.btnDelete = btnDelete;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
		    .inflate(R.layout.row_layout, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {
		if (selectedPositions.get(holder.getAdapterPosition())) {
			holder.imageView.setBackgroundColor(Color.BLUE);
		} else {
			holder.imageView.setBackgroundColor(Color.TRANSPARENT);
		}
		Picasso.with(context)
		    .load(Uri.parse(uris.get(holder.getAdapterPosition())))
		    .resize(640, 480).centerInside().into(holder.imageView);
		holder.imageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean isSelected =
				        !selectedPositions.get(holder.getAdapterPosition());
				selectedPositions.put(holder.getAdapterPosition(), isSelected);
				if (isSelected) {
					selectedUris.add(uris.get(holder.getAdapterPosition()));
				} else {
					selectedUris.remove(uris.get(holder.getAdapterPosition()));
				}
				btnDelete.setEnabled(!selectedUris.isEmpty());
				notifyItemChanged(holder.getAdapterPosition());
			}
		});
	}

	@Override
	public int getItemCount() {
		return uris.size();
	}

	public List<String> getSelectedUris() {
		return selectedUris;
	}

	public void resetSelections() {
		selectedPositions.clear();
		selectedUris.clear();
		btnDelete.setEnabled(false);
	}

}
