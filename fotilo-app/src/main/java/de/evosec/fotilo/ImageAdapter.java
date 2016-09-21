package de.evosec.fotilo;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private final Context context;
    private final List<String> uris;
    private List<String> selectedUris = Collections.emptyList();

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image);
        }
    }

    public ImageAdapter(Context context, List<String> uris) {
        this.context = context;
        this.uris = uris;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Picasso.with(context).load(Uri.parse(uris.get(position))).resize(480,480).centerInside().into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return uris.size();
    }

    public List<String> getSelectedUris() {
        return selectedUris;
    }

    public void setSelectedUris(List<String> selectedUris) {
        this.selectedUris = selectedUris;
    }

}
