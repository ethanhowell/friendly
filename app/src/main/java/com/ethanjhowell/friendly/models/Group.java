package com.ethanjhowell.friendly.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

@ParseClassName("Group")
public class Group extends ParseObject {
    private Date dateLeft;

    public static final String KEY_GROUP_NAME = "groupName";

    public String getGroupName() {
        return getString(KEY_GROUP_NAME);
    }

    public void setGroupName(String groupName) {
        put(KEY_GROUP_NAME, groupName);
    }

    public Date getDateLeft() {
        return dateLeft;
    }

    public void setDateLeft(Date dateLeft) {
        this.dateLeft = dateLeft;
    }
}
