package br.edu.uepb.nutes.ocariot.data.model.ocariot;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import br.edu.uepb.nutes.ocariot.data.model.common.UserAccess;

/**
 * Represents Child object.
 *
 * @author Copyright (c) 2018, NUTES/UEPB
 */
public class Child extends User implements Comparable<Child> {
    @SerializedName("gender")
    private String gender;

    @SerializedName("age")
    private int age;

    @SerializedName("last_sync")
    private String lastSync;

    @SerializedName("fitbit_access")
    private UserAccess fitBitAccess;

    public Child() {
    }

    public Child(String username, String password) {
        super(username, password);
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getLastSync() {
        return lastSync;
    }

    public void setLastSync(String lastSync) {
        this.lastSync = lastSync;
    }

    public UserAccess getFitBitAccess() {
        return fitBitAccess;
    }

    public void setFitBitAccess(UserAccess fitBitAccess) {
        this.fitBitAccess = fitBitAccess;
    }

    /**
     * Convert json to Object.
     *
     * @param json String
     * @return User
     */
    public static Child jsonDeserialize(String json) {
        return new Gson().fromJson(json, Child.class);
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public int compareTo(Child o) {
        return this.username.compareToIgnoreCase(o.getUsername());
    }
}
