package com.ethanjhowell.friendly.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanjhowell.friendly.databinding.ItemGroupBinding;
import com.ethanjhowell.friendly.models.Group;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private static final String TAG = GroupAdapter.class.getCanonicalName();
    List<Group> groups;

    public GroupAdapter(List<Group> groups) {
        this.groups = groups;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGroupBinding binding = ItemGroupBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(groups.get(position));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName;

        public ViewHolder(@NonNull ItemGroupBinding binding) {
            super(binding.getRoot());
            // TODO: on click listeners to take us to the chat view
            tvGroupName = binding.tvGroupName;
        }

        public void bind(Group group) {
            Log.d(TAG, "bind: " + group.getGroupName());
            tvGroupName.setText(group.getGroupName());
        }
    }
}