package com.ethanjhowell.friendly.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanjhowell.friendly.OnDoubleTapListener;
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
        viewHolder.tvMessageBody.setOnTouchListener(new OnDoubleTapListener(parent.getContext()) {
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
                        } else {
                            viewHolder.tvReactions.setText(message.getReactionString());
                        }
                    });
                    Log.d(TAG, "onDoubleTap: " + message.getBody());
                }
            }
        });
        return viewHolder;
    }
}
