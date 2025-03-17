package com.raah.seedhiraah;

public class ModelMosque {

    String mosqueId, ownerId, imageId, mosqueImage, mosqueName, cityName, ownerName,mosqueDescription, mosqueCapacity;
    double longitude, latitude;

    public ModelMosque() {
    }

    public ModelMosque(String mosqueId, String ownerId, String imageId, String mosqueImage, String mosqueName, String cityName, String ownerName, String mosqueDescription, String mosqueCapacity, double longitude, double latitude, boolean isLadiesFacilityAvailable) {
        this.mosqueId = mosqueId;
        this.ownerId = ownerId;
        this.imageId = imageId;
        this.mosqueImage = mosqueImage;
        this.mosqueName = mosqueName;
        this.cityName = cityName;
        this.ownerName = ownerName;
        this.mosqueDescription = mosqueDescription;
        this.mosqueCapacity = mosqueCapacity;
        this.longitude = longitude;
        this.latitude = latitude;
    }
    public String getMosqueCapacity() {
        return mosqueCapacity;
    }

    public void setMosqueCapacity(String mosqueCapacity) {
        this.mosqueCapacity = mosqueCapacity;
    }

    public String getMosqueId() {
        return mosqueId;
    }

    public void setMosqueId(String mosqueId) {
        this.mosqueId = mosqueId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getMosqueImage() {
        return mosqueImage;
    }

    public void setMosqueImage(String mosqueImage) {
        this.mosqueImage = mosqueImage;
    }

    public String getMosqueName() {
        return mosqueName;
    }

    public void setMosqueName(String mosqueName) {
        this.mosqueName = mosqueName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getMosqueDescription() {
        return mosqueDescription;
    }

    public void setMosqueDescription(String mosqueDescription) {
        this.mosqueDescription = mosqueDescription;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}