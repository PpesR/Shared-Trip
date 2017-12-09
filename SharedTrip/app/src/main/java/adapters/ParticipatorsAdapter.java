package adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import adapters.MyEventsAdapter.MyEventsManager;
import de.hdodenhof.circleimageview.CircleImageView;
import models.MyEventModel;
import models.ParticipatorModel;
import remm.sharedtrip.AdminActivity;
import remm.sharedtrip.R;

/**
 * Created by Mark on 12.11.2017.
 */

public class ParticipatorsAdapter extends RecyclerView.Adapter<ParticipatorsAdapter.ParticipatorViewHolder> {

    private MyEventsManager manager;
    public MyEventModel eventModel;
    public List<ParticipatorModel> participators;
    private Context context;
    private TextView badge;

    public ParticipatorsAdapter(MyEventsManager manager, Context context, List<ParticipatorModel> participators, MyEventModel eventModel, TextView badge) {
        this.eventModel = eventModel;
        this.manager = manager;
        this.participators = participators;
        this.context = context;
        this.badge = badge;
    }

    public class ParticipatorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public MyEventModel eventModel;
        public Context context;
        public TextView name;
        public CircleImageView imageView;
        public ParticipatorModel participator;
        public Button approveButton;
        public Button rejectButton;

        public ParticipatorViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.admin_event_participator_name);
            imageView = itemView.findViewById(R.id.admin_event_participator_pic);
            approveButton = itemView.findViewById(R.id.admin_event_participator_yes);
            rejectButton = itemView.findViewById(R.id.admin_event_participator_no);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) { }
    }

    @Override
    public ParticipatorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.single_admin_event_participator, parent,false);
        ParticipatorViewHolder holder = new ParticipatorViewHolder(itemView);
        holder.eventModel = eventModel;
        holder.context = context;
        return holder;
    }

    @Override
    public void onBindViewHolder(ParticipatorViewHolder holder, final int position) {
        final ParticipatorModel participator = participators.get(position);
        holder.participator = participator;
        holder.name.setText(participator.getName());
        holder.approveButton.getBackground().setColorFilter(context.getResources().getColor(R.color.light_green), PorterDuff.Mode.MULTIPLY);
        holder.approveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventModel.decreaseUsersPending();
                manager.onUserApproved(position, eventModel, badge);
            }
        });
        holder.rejectButton.getBackground().setColorFilter(context.getResources().getColor(R.color.calm_red), PorterDuff.Mode.MULTIPLY);
        holder.rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventModel.decreaseUsersPending();
                manager.onUserBanned(position, eventModel, badge);
            }
        });
        Glide
            .with(context)
            .load(participators.get(position).getImageUri())
            .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return participators.size();
    }
}
