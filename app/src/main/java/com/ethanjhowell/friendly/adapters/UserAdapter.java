package com.ethanjhowell.friendly.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ethanjhowell.friendly.databinding.ItemUserBinding;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private static final String TAG = UserAdapter.class.getCanonicalName();
    private final List<FriendlyParseUser> users;

    public UserAdapter(List<FriendlyParseUser> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // attach the layout to our viewholder
        ItemUserBinding binding = ItemUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProfilePic;
        private TextView tvName;

        public ViewHolder(@NonNull ItemUserBinding binding) {
            super(binding.getRoot());
            ivProfilePic = binding.ivProfilePic;
            tvName = binding.tvName;
        }

        public void bind(FriendlyParseUser user) {
            Log.d(TAG, "bind: ");
            tvName.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
            Glide.with(itemView)
                    .load(user.getProfilePicture().getUrl())
                    .circleCrop()
                    .into(ivProfilePic);
        }

    }
}
