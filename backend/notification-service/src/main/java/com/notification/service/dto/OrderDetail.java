package com.notification.service.dto;

import lombok.*;

@Data
public class OrderDetail {

    private String orderId;
    private  String email;
    private  String userId;
    private  String userPhone;
    private  boolean orderPaymentStatus=false;
    private  boolean orderStatus=false;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public boolean isOrderPaymentStatus() {
        return orderPaymentStatus;
    }

    public void setOrderPaymentStatus(boolean orderPaymentStatus) {
        this.orderPaymentStatus = orderPaymentStatus;
    }

    public boolean isOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(boolean orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    private  String courseId;
    ///.. amount..

}