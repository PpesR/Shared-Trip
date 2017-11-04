package remm.sharedtrip;

/**
 * Created by Mark on 02-Nov-17.
 */

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;


public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private Context context;
    private List<EventModel> events;

    public EventAdapter(Context context, List<EventModel> events) {
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
        holder.name.setText(events.get(position).getname());
        holder.loc.setText(events.get(position).getLoc());
        Glide
            .with(context)
            .load(events.get(position).getImageLink())
            .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView name;
        public TextView loc;
        public ImageView imageView;

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
        }
    }
}