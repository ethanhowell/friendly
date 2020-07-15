package com.ethanjhowell.friendly.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanjhowell.friendly.databinding.ItemGroupBinding;
import com.ethanjhowell.friendly.models.Group;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    ItemGroupBinding binding;
    List<Group> groups;

    public GroupAdapter(List<Group> groups) {
        this.groups = groups;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        binding = ItemGroupBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(groups.get(position));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // TODO: on click listeners to take us to the chat view
            tvGroupName = binding.tvGroupName;
        }

        public void bind(Group group) {
            tvGroupName.setText(group.getGroupName());
        }
    }
}
