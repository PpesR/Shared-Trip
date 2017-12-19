package adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.ExecutionException;

import adapters.JoinRequestsAdapter.RequestUserModel;
import de.hdodenhof.circleimageview.CircleImageView;
import models.MyEventModel;
import remm.sharedtrip.EventDetailsActivity;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import remm.sharedtrip.ProfileActivity;
import remm.sharedtrip.R;
import utils.MyEventsUtil.ApprovalTask;
import utils.MyEventsUtil.DenialTask;
import utils.MyEventsUtil.PendingRequestsTask;

import static utils.UtilBase.notNull;
import static utils.UtilBase.valueOrNull;

/**
 * Created by Mark on 12.11.2017.
 */

public class MyEventsAdapter extends RecyclerView.Adapter<MyEventsAdapter.MyEventViewHolder>  {

    public interface MyEventsManager {

        int getUserModelId();

        Drawable getDrawableById(int id);
        Resources getResources();
        FbGoogleUserModel getUserModel();

        void startDetailsActivity(Intent detailViewIntent);
        void provideEvents(final List<MyEventModel> events);
        void startRequestManagementActivity(int id, List<RequestUserModel> pending, MyEventsAdapter.MyEventViewHolder holder);

        LayoutInflater getLayoutInflater();
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

    public class MyEventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView name;
        public TextView amount;
        public TextView status;
        public ImageView imageView;
        public MyEventModel eventModel;
        public LinearLayout badge;


        public MyEventViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.my_event_name);
            imageView = itemView.findViewById(R.id.my_event_pic);
            amount = itemView.findViewById(R.id.my_event_request_amount);
            status = itemView.findViewById(R.id.my_event_status);
            badge = itemView.findViewById(R.id.my_event_requests_badge);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent detailViewIntent = new Intent(context, EventDetailsActivity.class);

            String gsonString = gson.toJson(eventModel.toDetailsWithoutBitmap());
            detailViewIntent.putExtra("event", gsonString);
            detailViewIntent.putExtra("user", gson.toJson(manager.getUserModel()));
            manager.startDetailsActivity(detailViewIntent);
        }
    }

    @Override
    public MyEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.single_my_event, parent,false);
        return new MyEventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyEventViewHolder holder, int position) {
        final MyEventModel event = myAdminEvents.get(position);

        holder.eventModel = event;
        holder.name.setText(event.getName());
        Drawable statusIcon;
        if (event.isAdmin()) {
            if (event.getUsersPending()>0) {
                holder.amount.setText(event.getUsersPending()+"");
                holder.badge.setVisibility(View.VISIBLE);
            }

            int color = manager.getResources().getColor(R.color.golden);
            statusIcon = manager.getDrawableById(R.drawable.ic_star_black_20dp);
            holder.status.setCompoundDrawablesWithIntrinsicBounds(statusIcon, null, null, null);
            statusIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            holder.status.setText("admin");
            holder.status.setTextColor(color);
        }
        else if (event.isApproved()){
            int color = manager.getResources().getColor(R.color.light_gray);
            statusIcon = manager.getDrawableById(R.drawable.ic_check_black_24dp);
            statusIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            holder.status.setCompoundDrawablesWithIntrinsicBounds(statusIcon, null, null, null);
            holder.status.setText("participating");
        }
        else if (event.isBanned()){ holder.status.setText("banned"); }
        else { holder.status.setText("pending"); }

        if (event.getUsersPending() == 0){ holder.amount.setVisibility(View.GONE); }
        else { holder.amount.setText(event.getUsersPending()+""); }

        Bitmap bitmap = event.getBitmap();
        if (notNull(bitmap)) holder.imageView.setImageBitmap(bitmap);
        else {
            Glide
                .with(context)
                .load(event.getImageLink())
                .into(holder.imageView);
        }
        holder.badge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PendingRequestsTask<Void> task = new PendingRequestsTask<>(event.getId(), manager);
                try {
                    List<RequestUserModel> pending = task.execute().get();
                    if (pending.size() == 1) {
                        final FbGoogleUserModel model = pending.get(0);
                        LayoutInflater inflater = manager.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.dialog_single_request, null);
                        CircleImageView requestPicture = dialogView.findViewById(R.id.only_request_picture);
                        Glide
                            .with(context)
                            .load(Uri.parse(model.imageUriString))
                            .into(requestPicture);
                        dialogView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent profileIntent = new Intent(context, ProfileActivity.class);
                                profileIntent.putExtra("user", gson.toJson(model));
                                profileIntent.putExtra("notMine", true);
                                context.startActivity(profileIntent);
                            }
                        });

                        TextView requestText = dialogView.findViewById(R.id.only_request_text);
                        String text = (valueOrNull(model.firstName) == null ? model.name : model.firstName)+" wishes to join";
                        requestText.setText(text);
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context)
                                .setTitle(valueOrNull(model.firstName) == null ? "Join request" : model.firstName+"'s request")
                                .setView(dialogView)
                                .setPositiveButton("APPROVE", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        ApprovalTask<Void> approvalTask = new ApprovalTask<>(
                                                event.getId(),
                                                model.id,
                                                manager.getUserModelId());
                                        try {
                                            boolean result = approvalTask.execute().get();
                                            if (result) {
                                                event.setUsersPending(0);
                                                holder.amount.setText("0");
                                                holder.badge.setVisibility(View.GONE);
                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        } catch (ExecutionException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                })
                                .setNegativeButton("REJECT", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        DenialTask<Void> denialTask = new DenialTask<>(
                                                event.getId(),
                                                model.id,
                                                manager.getUserModelId());
                                        try {
                                            boolean result = denialTask.execute().get();
                                            if (result) {
                                                event.setUsersPending(0);
                                                holder.amount.setText("0");
                                                holder.badge.setVisibility(View.GONE);
                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        } catch (ExecutionException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        dialogBuilder.create().show();
                    }
                    else {
                        manager.startRequestManagementActivity(event.getId(), pending, holder);
                    }


                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return myAdminEvents.size();
    }


}
