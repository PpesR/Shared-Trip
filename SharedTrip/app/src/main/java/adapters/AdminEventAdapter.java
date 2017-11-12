package adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import fragments.ParticipatorsFragment;
import models.AdminEventModel;
import remm.sharedtrip.AdminEventActivity;
import remm.sharedtrip.EventDetailsActivity;
import remm.sharedtrip.R;

/**
 * Created by Mark on 12.11.2017.
 */

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.AdminEventViewHolder>  {

    private AdminEventActivity aea;
    private List<AdminEventModel> myAdminEvents;
    private Context context;

    public AdminEventAdapter(Context context, List<AdminEventModel> events, AdminEventActivity aea) {
        this.myAdminEvents = events;
        this.context = context;
        this.aea = aea;
    }

    public class AdminEventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView name;
        public TextView location;
        public TextView badge;
        public TextView startDate;
        public ImageView imageView;
        public AdminEventModel eventModel;

        public AdminEventViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.admin_event_name);
            imageView = itemView.findViewById(R.id.admin_event_pic);
            badge = itemView.findViewById(R.id.admin_event_badge);
            startDate = itemView.findViewById(R.id.admin_event_start);
            location = itemView.findViewById(R.id.admin_event_location);
            imageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            aea.eventClicked(position, badge);
        }
    }

    @Override
    public AdminEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.single_admin_event, parent,false);
        return new AdminEventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AdminEventViewHolder holder, int position) {
        AdminEventModel event = myAdminEvents.get(position);
        holder.eventModel = event;
        holder.name.setText(event.getName());
        holder.startDate.setText(event.getStartDate());
        holder.location.setText(event.getLoc());
        if (event.getUsersPending() == 0){
            holder.badge.setVisibility(View.GONE);
        }
        else {
            holder.badge.setText(event.getUsersPending()+"");
        }
        Glide
            .with(context)
            .load(myAdminEvents.get(position).getImageLink())
            .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return myAdminEvents.size();
    }


}
