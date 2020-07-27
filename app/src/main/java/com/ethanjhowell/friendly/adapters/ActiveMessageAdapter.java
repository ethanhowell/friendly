package com.ethanjhowell.friendly.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanjhowell.friendly.OnDoubleTapListener;
import com.ethanjhowell.friendly.models.Message;

import java.util.List;

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
                    // TODO: perform double tap to thumb's up message here
                    Log.d(TAG, "onDoubleTap: " + message.getBody());
                    Toast.makeText(parent.getContext(), message.getBody(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        return viewHolder;
    }
}
