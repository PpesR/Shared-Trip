package adapters;

/**
 * Created by Mark on 02-Nov-17.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.List;

import interfaces.UserModelHolder;
import models.UserEventModel;
import remm.sharedtrip.EventDetailsActivity;
import remm.sharedtrip.R;

import static utils.UtilBase.API_PREFIX;


public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private Context context;
    private List<UserEventModel> events;

    public EventAdapter(Context context, List<UserEventModel> events) {
        this.context = context;
        this.events = events;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.card, parent,false);

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

            Intent detailViewIntent = new Intent(context, EventDetailsActivity.class);

            String gsonString = gson.toJson(eventModel.copyWithoutBitmap());
            detailViewIntent.putExtra("event", gsonString);
            detailViewIntent.putExtra("prefix", API_PREFIX);
            detailViewIntent.putExtra("user", ((UserModelHolder)context).getSerializedLoggedInUserModel());

            context.startActivity(detailViewIntent);
        }


    }
}