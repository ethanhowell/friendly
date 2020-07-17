package com.ethanjhowell.friendly.adapters;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
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
    private static final float OTHER_MESSAGE_MARGIN_START_DP = 80;
    private static final float OTHER_MESSAGE_MARGIN_END_DP = 64;
    private static final float OWN_MESSAGE_MARGIN_START_DP = 128;
    private static final float OWN_MESSAGE_MARGIN_END_DP = 16;
    private static final DisplayMetrics DISPLAY_METRICS = Resources.getSystem().getDisplayMetrics();
    private static final int OTHER_MESSAGE_MARGIN_START_PX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, OTHER_MESSAGE_MARGIN_START_DP, DISPLAY_METRICS);
    private static final int OTHER_MESSAGE_MARGIN_END_PX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, OTHER_MESSAGE_MARGIN_END_DP, DISPLAY_METRICS);
    private static final int OWN_MESSAGE_MARGIN_START_PX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, OWN_MESSAGE_MARGIN_START_DP, DISPLAY_METRICS);
    private static final int OWN_MESSAGE_MARGIN_END_PX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, OWN_MESSAGE_MARGIN_END_DP, DISPLAY_METRICS);
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
        private TextView tvMessageBody;
        private TextView tvAuthorName;
        private ImageView ivAuthorProfilePic;


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
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) tvMessageBody.getLayoutParams();
            // TODO: make the message bubble wrap the textview if length is less than the whole screen
            if (message.authorIsCurrentUser()) {
                ivAuthorProfilePic.setVisibility(View.GONE);
                tvAuthorName.setVisibility(View.GONE);
                layoutParams.setMarginStart(OWN_MESSAGE_MARGIN_START_PX);
                layoutParams.setMarginEnd(OWN_MESSAGE_MARGIN_END_PX);
                tvMessageBody.setGravity(Gravity.END);
            } else {
                ivAuthorProfilePic.setVisibility(View.VISIBLE);
                tvAuthorName.setVisibility(View.VISIBLE);
                tvAuthorName.setText(String.format("%s %s", author.getFirstName(), author.getLastName()));
                layoutParams.setMarginStart(OTHER_MESSAGE_MARGIN_START_PX);
                layoutParams.setMarginEnd(OTHER_MESSAGE_MARGIN_END_PX);
                tvMessageBody.setGravity(Gravity.START);
                Glide.with(itemView)
                        .load(author.getProfilePicture().getUrl())
                        .circleCrop()
                        .into(ivAuthorProfilePic);
            }
        }
    }
}
