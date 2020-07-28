package com.ethanjhowell.friendly.models;

import androidx.annotation.Nullable;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.Map;

@ParseClassName("Message")
public class Message extends ParseObject {
    public static final String KEY_BODY = "body";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_GROUP = "group";
    public static final String KEY_REACTIONS = "reactions";

    public Message() {
    }

    public Message(String body, Group group) {
        put(KEY_BODY, body);
        put(KEY_GROUP, group);
        put(KEY_AUTHOR, ParseUser.getCurrentUser());
    }

    public Map<String, String> getReactions() {
        Map<String, String> map = getMap(KEY_REACTIONS);
        if (map == null) {
            return new HashMap<>();
        } else {
            return map;
        }
    }


    public void setReactions(Map<String, String> reactions) {
        put(KEY_REACTIONS, reactions);
    }

    public String getBody() {
        return getString(KEY_BODY);
    }

    public ParseUser getAuthor() {
        return getParseUser(KEY_AUTHOR);
    }

    public Group getGroup() {
        return (Group) getParseObject(KEY_GROUP);
    }

    public boolean authorIsCurrentUser() {
        return getAuthor().getObjectId().equals(ParseUser.getCurrentUser().getObjectId());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || obj.getClass() != Message.class) {
            return false;
        }
        Message other = (Message) obj;
        return this.getObjectId().equals(other.getObjectId());
    }
}
