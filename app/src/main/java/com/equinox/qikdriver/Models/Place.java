package com.equinox.qikdriver.Models;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by mukht on 10/30/2016.
 */

public class Place {

    private String placeId, name, vicinity;
    private LatLng location;
    private Boolean openNow;
    private List<Photo> photos;
    private String iconURL, gMapURL, webURL;
    private Double totalRating;
    private String address, phoneNumber;
    private Periods periods;

    public String getIconURL() {
        return iconURL;
    }
    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }
    public String getPlaceId() {
        return placeId;
    }
    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getVicinity() {
        return vicinity;
    }
    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }
    public LatLng getLocation() {
        return location;
    }
    public void setLocation(LatLng location) {
        this.location = location;
    }
    public Boolean getOpenNow() {
        return openNow;
    }
    public void setOpenNow(Boolean openNow) {
        this.openNow = openNow;
    }
    public List<Photo> getPhoto() {
        return photos;
    }
    public void setPhoto(List<Photo> photos) { this.photos = photos;  }
    public Double getTotalRating() {
        return totalRating;
    }
    public void setTotalRating(Double totalRating) {
        this.totalRating = totalRating;
    }
    public String getgMapURL() {
        return gMapURL;
    }
    public void setgMapURL(String gMapURL) {
        this.gMapURL = gMapURL;
    }
    public String getWebURL() {
        return webURL;
    }
    public void setWebURL(String webURL) {
        this.webURL = webURL;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public Periods getPeriods() {
        return periods;
    }
    public void setPeriods(Periods periods) {
        this.periods = periods;
    }
}
