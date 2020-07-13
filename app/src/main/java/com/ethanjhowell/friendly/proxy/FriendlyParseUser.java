package com.ethanjhowell.friendly.proxy;

import com.parse.ParseFile;
import com.parse.ParseUser;

public class FriendlyParseUser extends ParseUser {
    public static final String KEY_FIRST_NAME = "firstName";
    public static final String KEY_LAST_NAME = "lastName";
    public static final String KEY_PROFILE_PICTURE = "profilePicture";
    public static final String KEY_PHONE_NUMBER = "phoneNumber";


    public String getFirstName() {
        return getString(KEY_FIRST_NAME);
    }

    public void setFirstName(String firstName) {
        put(KEY_FIRST_NAME, firstName);
    }

    public String getLastName() {
        return getString(KEY_LAST_NAME);
    }

    public void setLastName(String lastName) {
        put(KEY_LAST_NAME, lastName);
    }

    public ParseFile getProfilePicture() {
        return getParseFile(KEY_PROFILE_PICTURE);
    }

    public void setProfilePicture(ParseFile profilePicture) {
        put(KEY_PROFILE_PICTURE, profilePicture);
    }

    public String getPhoneNumber() {
        return getString(KEY_PHONE_NUMBER);
    }

    public void setPhoneNumber(String phoneNumber) {
        put(KEY_PHONE_NUMBER, phoneNumber);
    }
}
