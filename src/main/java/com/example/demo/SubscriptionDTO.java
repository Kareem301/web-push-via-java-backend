package com.example.demo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SubscriptionDTO {
    private String firstName = "";
    private String lastName = "";

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
