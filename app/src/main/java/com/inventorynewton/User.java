package com.inventorynewton;

public class User {

    public String name, email, profile, id;

    public User() {
        // Default constructor required for Firebase deserialization
    }
    public User(String email, String id, String fullName, String profile){
        this.email = email;
        this.id = id;
        this.name = fullName;
        this.profile = profile;
    }

}
