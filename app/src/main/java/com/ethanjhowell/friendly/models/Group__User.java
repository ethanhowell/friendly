package com.ethanjhowell.friendly.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;

// through model to access groups from users and vice versa
@ParseClassName("Group__User")
public class Group__User extends ParseObject {
    public static final String KEY_USER = "user";
    public static final String KEY_GROUP = "group";
    public static final String KEY_DATE_LEFT = "dateLeft";

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
        Group g = (Group) getParseObject(KEY_GROUP);
        assert g != null;
        g.setDateLeft(getDateLeft());
        return g;
    }

    public void setGroup(Group group) {
        put(KEY_GROUP, group);
    }

    public boolean hasLeft() {
        return getDate(KEY_DATE_LEFT) != null;
    }

    public Date getDateLeft() {
        return getDate(KEY_DATE_LEFT);
    }

    public void setDateLeft(Date date) {
        put(KEY_DATE_LEFT, date);
    }

    public void type(SaveCallback callback) {
        // just a flag so that the server actually performs the update
        put("active", true);
        saveInBackground(callback);
    }
}
