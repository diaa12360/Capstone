package com.example.demo.model;

import lombok.Data;

import java.util.Date;

@Data
public class ParkingLot {
    private String name;
    private boolean available;
    private String customerName;
    private String carNumber;
    private String color;
    private Date startDate;
    private Date expirationDate;
    private String phoneNumber;
}
