package adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import remm.sharedtrip.R;

/**
 * Created by Mark on 11.12.2017.
 */

public class RateUserAdapter extends RecyclerView.Adapter<RateUserAdapter.RateablePersonViewHolder> {

    public RateUserAdapter(Context context, List<RateablePerson> people) {
        this.context = context;
        this.people = people;
    }

    public static class RateablePerson {
        public RateablePerson() {}

        public int id;
        public String name;
        public String profilePictureUri;
        public int eventId;
        public String eventName;
        public int rating = -1;
        public boolean isAdmin = false;
    }

    public class RateablePersonViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView pic;
        private TextView name;
        private ImageButton thumbsUp;
        private ImageButton thumbsDown;
        private ImageButton favorite;
        private ImageButton smiley;
        private TextView tripName;

        public RateablePersonViewHolder(View itemView) {
            super(itemView);
            pic = itemView.findViewById(R.id.user_detail_picture);
            name = itemView.findViewById(R.id.user_detail_name);
            thumbsDown = itemView.findViewById(R.id.rate_user_thumbs_down);
            thumbsUp = itemView.findViewById(R.id.rate_user_thumbs_up);
            favorite = itemView.findViewById(R.id.rate_user_favorite);
            smiley = itemView.findViewById(R.id.rate_user_smiley);
            tripName = itemView.findViewById(R.id.rate_user_event_name);
        }

        public ImageButton selected = null;
        public void clearSelected() {
            if (selected != null) {
                selected.setColorFilter(context.getResources().getColor(R.color.light_gray), PorterDuff.Mode.SRC_ATOP);
            }
        }
        public void makeSelected(ImageButton button) {
            selected = button;
            button.setColorFilter(context.getResources().getColor(R.color.orangered), PorterDuff.Mode.SRC_ATOP);

        }
    }

    private Context context;
    private List<RateablePerson> people;



    @Override
    public RateablePersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.single_rate_a_user, parent,false);
        return new RateablePersonViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RateablePersonViewHolder holder, int position) {
        final RateablePerson person = people.get(position);
        Glide.with(context).load(Uri.parse(person.profilePictureUri)).into(holder.pic);
        String userDescription = (person.isAdmin ? "Participator of your " : "Travel companion from ") + person.eventName;
        holder.tripName.setText(userDescription);
        holder.name.setText(person.name);
        holder.thumbsDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                person.rating = 0;
                holder.clearSelected();
                holder.makeSelected(holder.thumbsDown);
            }
        });
        holder.thumbsUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                person.rating = 1;
                holder.clearSelected();
                holder.makeSelected(holder.thumbsUp);
            }
        });
        holder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                person.rating = 2;
                holder.clearSelected();
                holder.makeSelected(holder.favorite);
            }
        });
        holder.smiley.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                person.rating = 3;
                holder.clearSelected();
                holder.makeSelected(holder.smiley);
            }
        });

    }

    @Override
    public int getItemCount() {
        return people.size();
    }
}
