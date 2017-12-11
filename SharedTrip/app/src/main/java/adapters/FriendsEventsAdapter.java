package adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import remm.sharedtrip.R;
import utils.FriendsUtil.FriendEvent;
import utils.FriendsUtil.FriendEventListener;
import utils.ValueUtil;

import static utils.ValueUtil.isNull;
import static utils.ValueUtil.valueOrNull;

/**
 * Created by Mark on 8.12.2017.
 */

public class FriendsEventsAdapter extends Adapter<FriendsEventsAdapter.FriendEventViewHolder> {

    private Context context; // needed for displaying images

    private FriendEventListener listenerActivity;
    public List<FriendEvent> friendEvents;

    public FriendsEventsAdapter(Context context, FriendEventListener listenerActivity, List<FriendEvent> friendEvents) {
        this.context = context;
        this.listenerActivity = listenerActivity;
        this.friendEvents = friendEvents;
    }

    @Override
    public void onBindViewHolder(FriendEventViewHolder holder, int position) {
        FriendEvent model = friendEvents.get(position);
        holder.ownModel = model;
        holder.eventName.setText(model.eventName);
        holder.location.setText(model.location);
        holder.friendName.setText(
                isNull(valueOrNull(model.friendFirstName))
                        ? model.friendFullName : model.friendFirstName);

        // The two ImageViews are different.
        // Event picture is a bitmap:
        holder.eventPicHolder.setImageBitmap(model.eventPicture);

        // User picture is a URI:
        Glide.with(context)
                .load(model.friendProfilePicture)
                .into(holder.friendProfPicHolder);

    }

    @Override
    public int getItemCount() {
        return friendEvents.size();
    }

    @Override
    public FriendEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.single_friends_event, parent,false);
        return new FriendEventViewHolder(itemView);
    }

    public class FriendEventViewHolder extends ViewHolder implements OnClickListener {

        FriendEvent ownModel;

        ImageView eventPicHolder;
        TextView eventName;
        TextView location;

        TextView friendName;
        CircleImageView friendProfPicHolder;

        FriendEventViewHolder(View itemView) {
            super(itemView);

            eventPicHolder = itemView.findViewById(R.id.friend_event_picture);
            eventName = itemView.findViewById(R.id.friend_event_trip_name);
            location = itemView.findViewById(R.id.friend_event_location);
            friendName = itemView.findViewById(R.id.friend_event_user_name);
            friendProfPicHolder = itemView.findViewById(R.id.friend_event_profile_picture);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // int index = getAdapterPosition(); // own index in adapter
            listenerActivity.onEventClicked(ownModel);
        }
    }


}
