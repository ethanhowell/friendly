package com.ethanjhowell.friendly.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanjhowell.friendly.MessageGestureListener;
import com.ethanjhowell.friendly.R;
import com.ethanjhowell.friendly.activities.ChatActivity;
import com.ethanjhowell.friendly.models.Message;
import com.parse.ParseUser;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ActiveMessageAdapter extends BaseMessageAdapter {
    private static final String TAG = ActiveMessageAdapter.class.getCanonicalName();

    public ActiveMessageAdapter(List<Message> messages) {
        super(messages);
    }

    // TODO: make this accessible
    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        ChatActivity chatActivity = (ChatActivity) parent.getContext();

        viewHolder.tvMessageBody.setOnTouchListener(new MessageGestureListener(chatActivity) {
            private final LinearLayout llEmojiBar = chatActivity.findViewById(R.id.llEmojiBar);

            @Override
            public void onDown(MotionEvent e) {
                llEmojiBar.setVisibility(View.GONE);
            }

            @Override
            public void onDoubleTap(MotionEvent e) {
                int pos = viewHolder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Message message = messages.get(pos);
                    Map<String, String> reactions = message.getReactions();
                    String personId = ParseUser.getCurrentUser().getObjectId();
                    if (reactions.containsKey(personId) && Objects.equals(reactions.get(personId), "👍")) {
                        reactions.remove(personId);
                    } else {
                        reactions.put(personId, "👍");
                    }
                    message.setReactions(reactions);
                    message.saveInBackground(e1 -> {
                        if (e1 != null) {
                            Log.e(TAG, "adding reaction to message", e1);
                        }
                    });
                    Log.d(TAG, "onDoubleTap: " + message.getBody());
                }
            }

            @Override
            public void onLongPress(MotionEvent e) {
                int pos = viewHolder.getAdapterPosition();
                llEmojiBar.setVisibility(View.VISIBLE);
                if (pos != RecyclerView.NO_POSITION) {
                    Message message = messages.get(pos);
                    Log.d(TAG, "onLongClick: " + message.getBody());
                    chatActivity.findViewById(R.id.tvReactionThumbsUp).setOnClickListener(
                            view -> emojiReaction((TextView) view, message)
                    );
                    chatActivity.findViewById(R.id.tvReactionJoy).setOnClickListener(
                            view -> emojiReaction((TextView) view, message)
                    );
                    chatActivity.findViewById(R.id.tvReactionSmile).setOnClickListener(
                            view -> emojiReaction((TextView) view, message)
                    );
                    chatActivity.findViewById(R.id.tvReactionWow).setOnClickListener(
                            view -> emojiReaction((TextView) view, message)
                    );
                    chatActivity.findViewById(R.id.tvReactionHope).setOnClickListener(
                            view -> emojiReaction((TextView) view, message)
                    );
                    chatActivity.findViewById(R.id.tvReactionWink).setOnClickListener(
                            view -> emojiReaction((TextView) view, message)
                    );
                    chatActivity.findViewById(R.id.tvReactionThumbsDown).setOnClickListener(
                            view -> emojiReaction((TextView) view, message)
                    );
                }
            }

            private void emojiReaction(TextView tvReaction, Message message) {
                Map<String, String> reactions = message.getReactions();
                String personId = ParseUser.getCurrentUser().getObjectId();
                reactions.put(personId, (String) tvReaction.getText());
                message.setReactions(reactions);
                message.saveInBackground(e1 -> {
                    if (e1 != null) {
                        Log.e(TAG, "adding reaction to message", e1);
                    }
                    llEmojiBar.setVisibility(View.GONE);
                });
            }
        });
        return viewHolder;
    }
}
