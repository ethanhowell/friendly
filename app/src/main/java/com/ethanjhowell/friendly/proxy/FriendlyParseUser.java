package com.ethanjhowell.friendly.proxy;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class FriendlyParseUser {
    // class that wraps ParseUser in order to provide convenience methods for setting additional fields
    public static final String KEY_FIRST_NAME = "firstName";
    public static final String KEY_LAST_NAME = "lastName";
    public static final String KEY_PROFILE_PICTURE = "profilePicture";
    public static final String KEY_PHONE_NUMBER = "phoneNumber";

    private ParseUser user;

    public FriendlyParseUser(ParseUser parseUser) {
        user = parseUser;
    }

    public FriendlyParseUser() {
        this(new ParseUser());
    }

    public String getFirstName() {
        return user.getString(KEY_FIRST_NAME);
    }

    public void setFirstName(String firstName) {
        user.put(KEY_FIRST_NAME, firstName);
    }

    public String getLastName() {
        return user.getString(KEY_LAST_NAME);
    }

    public void setLastName(String lastName) {
        user.put(KEY_LAST_NAME, lastName);
    }

    public ParseFile getProfilePicture() {
        return user.getParseFile(KEY_PROFILE_PICTURE);
    }

    public void setProfilePicture(ParseFile profilePicture) {
        user.put(KEY_PROFILE_PICTURE, profilePicture);
    }

    public String getPhoneNumber() {
        return user.getString(KEY_PHONE_NUMBER);
    }

    public void setPhoneNumber(String phoneNumber) {
        user.put(KEY_PHONE_NUMBER, phoneNumber);
    }

    public void setEmail(String email) {
        user.setEmail(email);
    }

    public void setUsername(String username) {
        user.setUsername(username);
    }

    public void setPassword(String password) {
        user.setPassword(password);
    }

    public void signUpInBackground(SignUpCallback callback) {
        user.signUpInBackground(callback);
    }

    public boolean isNew() {
        return user.isNew();
    }

    public void save() throws ParseException {
        user.save();
    }
}
