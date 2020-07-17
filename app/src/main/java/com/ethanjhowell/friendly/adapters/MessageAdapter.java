package com.ethanjhowell.friendly.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ethanjhowell.friendly.databinding.ItemMessageBinding;
import com.ethanjhowell.friendly.models.Message;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private static final String TAG = MessageAdapter.class.getCanonicalName();
    private List<Message> messages;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // attach the layout to our viewholder
        ItemMessageBinding binding = ItemMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageBody;
        TextView tvAuthorName;
        ImageView ivAuthorProfilePic;


        public ViewHolder(@NonNull ItemMessageBinding binding) {
            super(binding.getRoot());

            tvMessageBody = binding.tvMessageBody;
            tvAuthorName = binding.tvAuthorName;
            ivAuthorProfilePic = binding.ivAuthorProfilePic;
        }

        public void bind(Message message) {
            Log.d(TAG, "bind: " + message.getBody());
            FriendlyParseUser author = FriendlyParseUser.fromParseUser(message.getAuthor());

            tvMessageBody.setText(message.getBody());
            if (message.authorIsCurrentUser()) {
                ivAuthorProfilePic.setVisibility(View.INVISIBLE);
                tvAuthorName.setVisibility(View.GONE);
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) tvMessageBody.getLayoutParams();
                layoutParams.setMarginStart(176);
                layoutParams.setMarginEnd(44);
                Log.d(TAG, String.format("bind: %d %d %d %d", layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin, layoutParams.leftMargin));
            } else {
                ivAuthorProfilePic.setVisibility(View.VISIBLE);
                tvAuthorName.setVisibility(View.VISIBLE);
                tvAuthorName.setText(String.format("%s %s", author.getFirstName(), author.getLastName()));
                Glide.with(itemView)
                        .load(author.getProfilePicture().getUrl())
                        .circleCrop()
                        .into(ivAuthorProfilePic);
            }
        }
    }
}
