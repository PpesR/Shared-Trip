package adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import remm.sharedtrip.R;

/**
 * Created by Mark on 11.12.2017.
 */

public class NewAdminChoiceAdapter extends RecyclerView.Adapter<NewAdminChoiceAdapter.NewAdminViewHolder> {

    private List<MiniUserModel> participators;
    private DetailsManager detailsManager;
    private Context context;
    private boolean isChoosing = false;

    public NewAdminViewHolder selected = null;


    public NewAdminChoiceAdapter(Context context, List<MiniUserModel> participators, DetailsManager manager) {
        this.participators = participators;
        this.context = context;
        this.detailsManager = manager;
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
        MiniUserModel model = participators.get(position);

        holder.userModel = model;
        holder.name.setText(model.firstName == null ? model.fullName : model.firstName);
        Glide
            .with(context)
            .load(model.profilePicture)
            .into(holder.profilePicture);
    }

    @Override
    public int getItemCount() {
        return participators.isEmpty() ? 1 : participators.size();
    }

    public void isChoosing() {
        this.isChoosing = true;
    }

    public void notChoosing() {
        this.isChoosing = false;
    }

    public interface DetailsManager {
        void onNewAdminSelectedDone(MiniUserModel newAdmin);
        void showConfirm();

        void openUserProfile(int id);
    }

    public static class MiniUserModel {
        public int id;
        public String fullName;
        public String firstName;
        public Uri profilePicture;
    }

    public class NewAdminViewHolder extends ViewHolder implements OnClickListener{

        public TextView name;
        public CircleImageView profilePicture;
        public MiniUserModel userModel;


        public NewAdminViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_detail_name);
            profilePicture = itemView.findViewById(R.id.user_detail_picture);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (isChoosing) {
                detailsManager.showConfirm();
                selected = this;
                profilePicture.setBorderWidth(10);
                profilePicture.setAlpha((float) 0.7);
                name.setTextColor(context.getResources().getColor(R.color.orangered));
                profilePicture.setBorderColor(context.getResources().getColor(R.color.orangered));
            }
            else {
                detailsManager.openUserProfile(userModel.id);
            }
        }
    }

    public void restoreLayout() {
        if (selected != null) {
            selected.profilePicture.setBorderWidth(0);
            selected.profilePicture.setAlpha((float) 1);
            selected.name.setTextColor(context.getResources().getColor(R.color.button_grey));
        }
    }
}
