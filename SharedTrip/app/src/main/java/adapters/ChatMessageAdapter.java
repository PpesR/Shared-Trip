package adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import interfaces.UserModelHolder;
import remm.sharedtrip.R;
import utils.MessageUtil.ChatMessage;

import static utils.UtilBase.notNull;

/**
 * Created by Mark on 15.12.2017.
 */

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder> {

    public static final int TYPE_OWN = 976;
    public static final int TYPE_REGULAR = 954;

    ChatMessageViewHolder lastClickedDisplayingTime = null;

    private List<ChatMessage> messages;
    private Context context;
    private UserModelHolder userHolder;

    public ChatMessageAdapter(List<ChatMessage> messages, Context context) {
        userHolder = (UserModelHolder)context;
        this.messages = messages;
        this.context = context;

    }

    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = null;
        switch (viewType) {
            case TYPE_OWN:
                itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.single_chat_message_own, parent,false);
                break;
            default:
                    itemView = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.single_chat_message, parent,false);
                    break;
        }
        return new ChatMessageViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).isOwn(userHolder.getLoggedInUser())) {
            return TYPE_OWN;
        }
        return TYPE_REGULAR;
    }

    @Override
    public void onBindViewHolder(ChatMessageViewHolder holder, int position) {
        ChatMessage model = messages.get(position);
        holder.model = model;

        long timeDiff = new Date().getTime() - model.timeSent.getTime();
        if (timeDiff > 1.5 * 1000 * 60 * 60 * 24){
            holder.time.setText(model.dateFormatDisplayLong.format(model.timeSent));
        }
        else holder.time.setText(model.dateFormatDisplayShort.format(model.timeSent));

        holder.message.setText(model.text);
        Glide.with(context).load(model.senderPicture).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateMessages(List<ChatMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public class ChatMessageViewHolder extends RecyclerView.ViewHolder {

        private ChatMessage model;
        private TextView time;
        private TextView message;
        private CircleImageView image;

        public ChatMessageViewHolder(View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.single_message_time);
            message = itemView.findViewById(R.id.single_message_text);
            image = itemView.findViewById(R.id.single_message_profile_picture);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (notNull(lastClickedDisplayingTime))
                        lastClickedDisplayingTime.time.setVisibility(View.GONE);
                    time.setVisibility(View.VISIBLE);
                    lastClickedDisplayingTime = ChatMessageViewHolder.this;
                    return true;
                }
            });
        }
    }
}
