package com.example.groupexpensetracker.Entities;

public class FindTraveler {
    public String profile_image, fullname, country;

    public FindTraveler() {
    }

    public FindTraveler(String profileImage, String fullName, String country) {
        this.profile_image = profileImage;
        this.fullname = fullName;
        this.country = country;
    }

    public String getProfileImage() {
        return profile_image;
    }

    public void setProfileImage(String profileImage) {
        this.profile_image = profileImage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullName) {
        this.fullname = fullName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
