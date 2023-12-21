package com.example.groupexpensetracker.Entities;

public class HistoryTrip {
    String start_date, end_date, name, country, tripId;

    public HistoryTrip(String start_date, String end_date, String name, String country, String tripId) {
        this.start_date = start_date;
        this.end_date = end_date;
        this.name = name;
        this.country = country;
        this.tripId = tripId;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
