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

import utils.FriendsUtil.FriendEvent;
import utils.FriendsUtil.FriendEventListener;

/**
 * Created by Mark on 8.12.2017.
 */

public class FriendsEventsAdapter extends Adapter<FriendsEventsAdapter.FriendEventViewHolder> {

    private Context context; // needed for displaying images

    private FriendEventListener listenerActivity;
    private List<FriendEvent> friendEvents;

    public FriendsEventsAdapter(Context context, FriendEventListener listenerActivity, List<FriendEvent> friendEvents) {
        this.context = context;
        this.listenerActivity = listenerActivity;
        this.friendEvents = friendEvents;
    }

    @Override
    public void onBindViewHolder(FriendEventViewHolder holder, int position) {
        FriendEvent model = friendEvents.get(position);
        holder.ownModel = model;

        // TODO: set other layout elements' values just like this:
        holder.eventName.setText(model.eventName);

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

                // TODO: replace 0 with an actual layout name
                .inflate(0 /* R.layout.single_friend_event_layout*/, parent,false);
        return new FriendEventViewHolder(itemView);
    }

    public class FriendEventViewHolder extends ViewHolder implements OnClickListener {

        FriendEvent ownModel;

        // TODO: list the rest of relevant layout elements
        ImageView eventPicHolder;
        TextView eventName;

        ImageView friendProfPicHolder;

        FriendEventViewHolder(View itemView) {
            super(itemView);

            // TODO: replace 0 with an actual layout element id
            eventName = itemView.findViewById(0 /* R.id.friend_event_name_or_sth*/);

            // TODO: initialize other layout elements like friend's profile pic holder etc
        }

        @Override
        public void onClick(View v) {
            // int index = getAdapterPosition(); // own index in adapter
            listenerActivity.onEventClicked(ownModel);
        }
    }


}
