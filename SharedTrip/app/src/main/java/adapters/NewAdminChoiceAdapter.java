package adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import remm.sharedtrip.R;

/**
 * Created by Mark on 11.12.2017.
 */

public class NewAdminChoiceAdapter extends RecyclerView.Adapter<NewAdminChoiceAdapter.NewAdminViewHolder> {

    private List<PotentialAdminModel> participators;
    private DetailsManager manager;
    private Context context;


    public NewAdminChoiceAdapter(Context context, List<PotentialAdminModel> participators, DetailsManager manager) {
        this.participators = participators;
        this.context = context;
        this.manager = manager;
    }

    @Override
    public NewAdminViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.user_pic_and_name, parent,false);
        NewAdminViewHolder holder = new NewAdminViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(NewAdminViewHolder holder, int position) {
        PotentialAdminModel model = participators.get(position);

        holder.userModel = model;
        holder.name.setText(model.firstName == null ? model.fullName : model.firstName);
        holder.profilePicture.setImageURI(model.profilePicture);
    }

    @Override
    public int getItemCount() {
        return participators.size();
    }

    public interface DetailsManager {
        void onNewAdminSelected(PotentialAdminModel newAdmin);
    }

    public static class PotentialAdminModel {
        public int id;
        public String fullName;
        public String firstName;
        public Uri profilePicture;

        public PotentialAdminModel() {
        }
    }

    public class NewAdminViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView name;
        public CircleImageView profilePicture;
        public PotentialAdminModel userModel;


        public NewAdminViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_detail_name);
            profilePicture = itemView.findViewById(R.id.user_detail_picture);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            manager.onNewAdminSelected(userModel);
        }
    }
}
