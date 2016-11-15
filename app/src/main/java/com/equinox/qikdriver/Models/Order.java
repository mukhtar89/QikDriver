package com.equinox.qikdriver.Models;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by mukht on 11/12/2016.
 */

public class Order {

    private String orderId;
    private String orderFrom, orderFromName, orderFromEmail, orderFromPhotoURL, orderFromPhone, orderStatus;
    private LatLng locationFrom;
    private List<Item> orderItems;
    private Long timestamp;
    private Boolean exchangeItem;

    public Float getOrderValue() {
        Float value = (float) 0.00;
        for (Item item : orderItems) {
            if (item.getItemPriceValue() == null)  return null;
            value += (item.getItemPriceValue() * item.getItemQuantity());
        }
        return value;
    }

    public String getOrderId() {
        return orderId;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    public List<Item> getOrderItems() {
        return orderItems;
    }
    public void setOrderItems(List<Item> orderItems) {
        this.orderItems = orderItems;
    }
    public Long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    public Boolean getExchangeItem() {
        return exchangeItem;
    }
    public void setExchangeItem(Boolean exchangeItem) {
        this.exchangeItem = exchangeItem;
    }
    public String getOrderFrom() {
        return orderFrom;
    }
    public void setOrderFrom(String orderFrom) {
        this.orderFrom = orderFrom;
    }
    public String getOrderFromName() {
        return orderFromName;
    }
    public void setOrderFromName(String orderFromName) {
        this.orderFromName = orderFromName;
    }
    public String getOrderFromEmail() {
        return orderFromEmail;
    }
    public void setOrderFromEmail(String orderFromEmail) {
        this.orderFromEmail = orderFromEmail;
    }
    public String getOrderFromPhotoURL() {
        return orderFromPhotoURL;
    }
    public void setOrderFromPhotoURL(String orderFromPhotoURL) {
        this.orderFromPhotoURL = orderFromPhotoURL;
    }
    public String getOrderFromPhone() {
        return orderFromPhone;
    }
    public void setOrderFromPhone(String orderFromPhone) {
        this.orderFromPhone = orderFromPhone;
    }
    public LatLng getLocationFrom() {
        return locationFrom;
    }
    public void setLocationFrom(LatLng locationFrom) {
        this.locationFrom = locationFrom;
    }
    public String getOrderStatus() {
        return orderStatus;
    }
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
