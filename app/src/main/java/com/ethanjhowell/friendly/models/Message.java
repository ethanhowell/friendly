package com.ethanjhowell.friendly.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Message")
public class Message extends ParseObject {
    public static final String KEY_BODY = "body";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_GROUP = "group";

    public Message() {
    }

    public Message(String body, Group group) {
        put(KEY_BODY, body);
        put(KEY_GROUP, group);
        put(KEY_AUTHOR, ParseUser.getCurrentUser());
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
}
