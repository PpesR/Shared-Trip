package adapters;

/**
 * Created by Mark on 02-Nov-17.
 */

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.List;

import models.UserEventModel;
import remm.sharedtrip.EventDetailsActivity;
import remm.sharedtrip.ExplorationActivity;
import remm.sharedtrip.R;
import remm.sharedtrip.SearchActivity;


public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private Context context;
    private List<UserEventModel> events;
    public AppCompatActivity browseActivity;

    public SearchResultAdapter(Context context, List<UserEventModel> events) {
        this.context = context;
        this.events = events;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DisplayMetrics displayMetrics = new DisplayMetrics();

        //getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width_px = Resources.getSystem().getDisplayMetrics().widthPixels;

        int height_px =Resources.getSystem().getDisplayMetrics().heightPixels;

        int pixeldpi = Resources.getSystem().getDisplayMetrics().densityDpi;


        int width_dp = (width_px/pixeldpi)*255;
        int height_dp = (height_px/pixeldpi)*255;

        View itemView =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.card, parent,false);
        int height = parent.getMeasuredHeight();
        int width = parent.getMeasuredWidth() / 2;
        itemView.setLayoutParams(new RecyclerView.LayoutParams(width_dp, height_dp));
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserEventModel model = events.get(position);
        holder.eventModel = model;
        holder.name.setText(model.getName());
        holder.loc.setText(model.getLoc());
        Bitmap bitmap = model.getBitmap();
        if (bitmap!=null)
            holder.imageView.setImageBitmap(bitmap);
        else {
            Glide
                .with(context)
                .load(model.getImageLink())
                .into(holder.imageView);
        }
    }


    @Override
    public int getItemCount() {
        return events.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView name;
        public TextView loc;
        public ImageView imageView;
        public UserEventModel eventModel;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            imageView = itemView.findViewById(R.id.image);
            loc = itemView.findViewById(R.id.location);
            imageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            int position = getAdapterPosition();
            Gson gson = new Gson();

            Intent detailViewIntent = new Intent(browseActivity, EventDetailsActivity.class);

            String gsonString = gson.toJson(eventModel.copyWithoutBitmap());
            detailViewIntent.putExtra("event", gsonString);
            detailViewIntent.putExtra("prefix", ((SearchActivity) browseActivity).getApiPrefix());
            detailViewIntent.putExtra("user", gson.toJson(((SearchActivity) browseActivity).getUserModel()));

            browseActivity.startActivity(detailViewIntent);
        }


    }
}