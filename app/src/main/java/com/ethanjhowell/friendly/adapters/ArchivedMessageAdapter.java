package com.ethanjhowell.friendly.adapters;

import com.ethanjhowell.friendly.models.Message;

import java.util.List;

// NOTE: this is different from the other adapter in that reactions and replies are disabled.
public class ArchivedMessageAdapter extends BaseMessageAdapter {
    public ArchivedMessageAdapter(List<Message> messages) {
        super(messages);
    }
}
