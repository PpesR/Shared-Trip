package adapters;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.List;

import models.MyEventModel;
import models.ParticipatorModel;
import remm.sharedtrip.EventDetailsActivity;
import remm.sharedtrip.MainActivity;
import remm.sharedtrip.R;

import static utils.ValueUtil.notNull;

/**
 * Created by Mark on 12.11.2017.
 */

public class MyEventsAdapter extends RecyclerView.Adapter<MyEventsAdapter.AdminEventViewHolder>  {

    public interface MyEventsManager {
        void provideEvents(final List<MyEventModel> events);
        void provideParticipators(List<ParticipatorModel> models, final MyEventModel aeModel, final TextView badge);
        void setSubAdapter(RecyclerView subRecycler, final MyEventModel aem, final TextView badge);
        void eventClicked(int i, TextView badge);
        void onUserApproved(int participatorPosition, MyEventModel eventModel, TextView badge);
        void onUserBanned(int participatorPosition, MyEventModel eventModel, TextView badge);
        Drawable getDrawableById(int id);
        Resources getResources();
        String getApiPrefix();
        MainActivity.FbGoogleUserModel getUserModel();

        void startActivity(Intent detailViewIntent);
    }

    private MyEventsManager manager;
    private List<MyEventModel> myAdminEvents;
    private Context context;
    private Gson gson = new Gson();

    public MyEventsAdapter(Context context, List<MyEventModel> events, MyEventsManager manager) {
        this.myAdminEvents = events;
        this.context = context;
        this.manager = manager;
    }

    public class AdminEventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView name;
        public TextView badge;
        public TextView status;
        public ImageView imageView;
        public MyEventModel eventModel;


        public AdminEventViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.admin_event_name);
            imageView = itemView.findViewById(R.id.admin_event_pic);
            badge = itemView.findViewById(R.id.admin_event_badge);
            status = itemView.findViewById(R.id.my_event_status);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            /*manager.eventClicked(position, badge);*/
            Intent detailViewIntent = new Intent(context, EventDetailsActivity.class);

            String gsonString = gson.toJson(eventModel.toDetailsWithoutBitmap());
            detailViewIntent.putExtra("event", gsonString);
            detailViewIntent.putExtra("prefix", manager.getApiPrefix());
            detailViewIntent.putExtra("user", gson.toJson(manager.getUserModel()));
            manager.startActivity(detailViewIntent);
        }
    }

    @Override
    public AdminEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.single_my_event, parent,false);
        return new AdminEventViewHolder(itemView);
    }

    @SuppressLint("ResourceAsColor")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(AdminEventViewHolder holder, int position) {
        MyEventModel event = myAdminEvents.get(position);

        holder.eventModel = event;
        holder.name.setText(event.getName());
        Drawable statusIcon;
        if (event.isAdmin()) {
            int golden = manager.getResources().getColor(R.color.golden);
            statusIcon = manager.getDrawableById(R.drawable.ic_star_black_24dp);
            holder.status.setCompoundDrawablesWithIntrinsicBounds(statusIcon, null, null, null);
            statusIcon.setTint(golden);
            holder.status.setText("admin");
            holder.status.setTextColor(golden);

        }
        else if (event.isApproved()){
            int gray = manager.getResources().getColor(R.color.light_gray);
            statusIcon = manager.getDrawableById(R.drawable.ic_check_black_24dp);
            statusIcon.setTint(gray);
            holder.status.setCompoundDrawablesWithIntrinsicBounds(statusIcon, null, null, null);
            holder.status.setText("participating");
        }
        else if (event.isBanned()){
            holder.status.setText("banned");
        }
        else {
            holder.status.setText("pending");
        }

        if (event.getUsersPending() == 0){ holder.badge.setVisibility(View.GONE); }
        else { holder.badge.setText(event.getUsersPending()+""); }

        Bitmap bitmap = event.getBitmap();
        if (notNull(bitmap))
            holder.imageView.setImageBitmap(bitmap);
        else {
            Glide
                .with(context)
                .load(event.getImageLink())
                .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return myAdminEvents.size();
    }


}
