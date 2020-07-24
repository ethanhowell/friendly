package com.ethanjhowell.friendly.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.ethanjhowell.friendly.databinding.ItemMessageBinding;
import com.ethanjhowell.friendly.models.Message;

import java.util.List;

public class ActiveMessageAdapter extends BaseMessageAdapter {
    public ActiveMessageAdapter(List<Message> messages) {
        super(messages);
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
}
