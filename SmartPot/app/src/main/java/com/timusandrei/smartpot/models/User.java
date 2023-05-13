package com.timusandrei.smartpot.models;

public final class User {

    public User() {
        email = "";
        firstName = "";
        lastName = "";
    }

    public User(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    private final String email;
    private final String firstName;
    private final String lastName;
}
