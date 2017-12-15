package adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import remm.sharedtrip.R;
import utils.MyEventsUtil.ApprovalTask;
import utils.MyEventsUtil.DenialTask;

import static utils.UtilBase.notNull;

/**
 * Created by Mark on 11.12.2017.
 */

public class JoinRequestsAdapter extends RecyclerView.Adapter<JoinRequestsAdapter.JoinRequestViewHolder> {

    private Context context;
    private JoinRequestManager manager;
    private List<RequestUserModel> requesters;

    public interface JoinRequestManager {
        int getAdminId();
        int getEventId();
        void increaseCount();

        void returnResult();
    }

    public JoinRequestsAdapter(Context context, JoinRequestManager manager, List<RequestUserModel> requesters) {
        this.context = context;
        this.manager = manager;
        this.requesters = requesters;
    }

    @Override
    public JoinRequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.single_multi_join_request, parent,false);
        JoinRequestViewHolder holder = new JoinRequestViewHolder(itemView);
        holder.profilePicture = itemView.findViewById(R.id.multi_request_picture);
        holder.acceptButton = itemView.findViewById(R.id.multi_request_approve);
        holder.rejectButton = itemView.findViewById(R.id.multi_request_reject);
        holder.requestMessage = itemView.findViewById(R.id.multi_request_message);
        holder.requesterName = itemView.findViewById(R.id.multi_request_name);
        return holder;
    }

    @Override
    public void onBindViewHolder(final JoinRequestViewHolder holder, int position) {
        final RequestUserModel requester = requesters.get(position);
        Glide.with(context).load(Uri.parse(requester.imageUriString)).into(holder.profilePicture);
        holder.requesterName.setText(requester.name);
        holder.requestMessage.setText(notNull(requester.requestMessage)
                        ? requester.requestMessage : requester.name+" wants to join");
        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ApprovalTask<Void> task = new ApprovalTask<>(
                        manager.getEventId(),
                        requester.id,
                        manager.getAdminId());
                try {
                    boolean success = task.execute().get();
                    if (success) {
                        manager.increaseCount();
                        if (requesters.size() > 1) {
                            int position = holder.getAdapterPosition();
                            requesters.remove(position);
                            notifyDataSetChanged();
                        }
                        else manager.returnResult();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        holder.rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DenialTask<Void> task = new DenialTask<>(
                        manager.getEventId(),
                        requester.id,
                        manager.getAdminId());
                try {
                    boolean success = task.execute().get();
                    if (success) {
                        manager.increaseCount();
                        if (requesters.size() > 1) {
                            int position = holder.getAdapterPosition();
                            requesters.remove(position);
                            notifyDataSetChanged();
                        }
                        else manager.returnResult();
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
        return requesters.size();
    }

    public class JoinRequestViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView profilePicture;
        public TextView requestMessage;
        public TextView requesterName;
        public Button acceptButton;
        public Button rejectButton;
        public RequestUserModel userModel;

        public JoinRequestViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class RequestUserModel extends FbGoogleUserModel {
        public RequestUserModel() {
            super();
        }

        public String requestMessage;
    }
}
