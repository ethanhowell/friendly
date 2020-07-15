package com.ethanjhowell.friendly.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

// through model to access groups from users and vice versa
@ParseClassName("Group__User")
public class Group__User extends ParseObject {
    public static final String KEY_USER = "user";
    public static final String KEY_GROUP = "group";

    public Group__User() {
    }

    public Group__User(Group group, ParseUser user) {
        setGroup(group);
        setUser(user);
    }


    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public Group getGroup() {
        return (Group) getParseObject(KEY_GROUP);
    }

    public void setGroup(Group group) {
        put(KEY_GROUP, group);
    }
}
