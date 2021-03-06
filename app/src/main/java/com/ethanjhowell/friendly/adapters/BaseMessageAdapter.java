package com.ethanjhowell.friendly.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ethanjhowell.friendly.R;
import com.ethanjhowell.friendly.databinding.ItemMessageBinding;
import com.ethanjhowell.friendly.models.Message;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.parse.ParseUser;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class BaseMessageAdapter extends RecyclerView.Adapter<BaseMessageAdapter.ViewHolder> {
    protected static final float OTHER_MESSAGE_MARGIN_START_DP = 80;
    protected static final float OTHER_MESSAGE_MARGIN_END_DP = 64;
    protected static final float OWN_MESSAGE_MARGIN_START_DP = 128;
    protected static final float OWN_MESSAGE_MARGIN_END_DP = 16;
    protected static final float REACTION_MARGIN_END_DP = 4;
    protected static final Resources RESOURCES = Resources.getSystem();
    protected static final DisplayMetrics DISPLAY_METRICS = Resources.getSystem().getDisplayMetrics();
    protected static final int OTHER_MESSAGE_MARGIN_START_PX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, OTHER_MESSAGE_MARGIN_START_DP, DISPLAY_METRICS);
    protected static final int OTHER_MESSAGE_MARGIN_END_PX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, OTHER_MESSAGE_MARGIN_END_DP, DISPLAY_METRICS);
    protected static final int OWN_MESSAGE_MARGIN_START_PX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, OWN_MESSAGE_MARGIN_START_DP, DISPLAY_METRICS);
    protected static final int OWN_MESSAGE_MARGIN_END_PX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, OWN_MESSAGE_MARGIN_END_DP, DISPLAY_METRICS);
    protected static final int REACTION_MARGIN_END_PX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, REACTION_MARGIN_END_DP, DISPLAY_METRICS);
    protected static final float REACTION_BUBBLE_ELEVATION = 2;

    protected static final int COLOR_BLACK = 0xff000000;
    protected static final int COLOR_WHITE = 0xffffffff;

    private static final String TAG = BaseMessageAdapter.class.getCanonicalName();
    protected final List<Message> messages;

    public BaseMessageAdapter(List<Message> messages) {
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
        holder.bind(
                position != 0 ? messages.get(position - 1) : null,
                messages.get(position),
                position < messages.size() - 1 ? messages.get(position + 1) : null
        );
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMessageBody;
        final TextView tvAuthorName;
        final ImageView ivAuthorProfilePic;
        final LinearLayout llMessage;
        final ConstraintLayout.LayoutParams layoutParams;
        final LinearLayout llReactions;
        private final Context context;


        public ViewHolder(@NonNull ItemMessageBinding binding) {
            super(binding.getRoot());

            tvMessageBody = binding.tvMessageBody;
            llReactions = binding.llReactions;
            tvAuthorName = binding.tvAuthorName;
            ivAuthorProfilePic = binding.ivAuthorProfilePic;
            llMessage = binding.llMessage;
            context = itemView.getContext();

            layoutParams = (ConstraintLayout.LayoutParams) llMessage.getLayoutParams();
        }

        private void setReactions(Map<String, String> reactions) {
            llReactions.removeAllViews();
            if (!reactions.isEmpty()) {
                Multiset<String> reactionCounts = LinkedHashMultiset.create();
                reactionCounts.addAll(reactions.values());

                String userId = ParseUser.getCurrentUser().getObjectId();
                String userEmoji = reactions.containsKey(userId) ? reactions.get(userId) : null;

                for (String emoji : reactionCounts.elementSet()) {
                    TextView textView = new TextView(context);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMarginEnd(REACTION_MARGIN_END_PX);
                    textView.setLayoutParams(layoutParams);
                    textView.setElevation(REACTION_BUBBLE_ELEVATION);
                    int count = reactionCounts.count(emoji);
                    if (count > 1) {
                        textView.setText(String.format(Locale.US, "%s %d", emoji, count));
                    } else {
                        textView.setText(emoji);
                    }
                    if (emoji.equals(userEmoji)) {
                        Log.d(TAG, "setReactions: match");
                        textView.setBackgroundResource(R.drawable.reaction_bubble_self);
                        textView.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark, null));
                    } else {
                        textView.setBackgroundResource(R.drawable.reaction_bubble_other);
                        textView.setTextColor(COLOR_BLACK);
                    }
                    llReactions.addView(textView);
                }
            }
        }

        public void bind(Message prev, Message message, Message next) {
            Log.d(TAG, "bind: " + message.getBody());
            FriendlyParseUser author = FriendlyParseUser.fromParseUser(message.getAuthor());

            tvMessageBody.setText(message.getBody());
            setReactions(message.getReactions());
            if (message.authorIsCurrentUser()) {
                tvAuthorName.setVisibility(View.GONE);
                tvMessageBody.setTextColor(COLOR_WHITE);
                tvMessageBody.setBackgroundResource(R.drawable.bubble_dark);
                ivAuthorProfilePic.setVisibility(View.GONE);
                layoutParams.setMarginStart(OWN_MESSAGE_MARGIN_START_PX);
                layoutParams.setMarginEnd(OWN_MESSAGE_MARGIN_END_PX);
                llMessage.setGravity(Gravity.END);
            } else {
                if (prev != null && prev.getAuthor().getObjectId().equals(message.getAuthor().getObjectId())) {
                    ivAuthorProfilePic.setVisibility(View.GONE);
                    tvAuthorName.setVisibility(View.GONE);
                } else {
                    ivAuthorProfilePic.setVisibility(View.VISIBLE);
                    tvAuthorName.setVisibility(View.VISIBLE);
                    tvAuthorName.setText(String.format("%s %s", author.getFirstName(), author.getLastName()));
                }
                tvMessageBody.setTextColor(COLOR_BLACK);
                tvMessageBody.setBackgroundResource(R.drawable.bubble_light);
                layoutParams.setMarginStart(OTHER_MESSAGE_MARGIN_START_PX);
                layoutParams.setMarginEnd(OTHER_MESSAGE_MARGIN_END_PX);
                llMessage.setGravity(Gravity.START);
                Glide.with(itemView)
                        .load(author.getProfilePicture().getUrl())
                        .circleCrop()
                        .into(ivAuthorProfilePic);
            }
        }
    }
}
