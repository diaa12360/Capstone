package com.example.demo.repository;

import com.example.demo.model.ParkingLot;

import java.util.List;
import java.util.Optional;

public interface ParkingLotsDAO {
    Optional<List<ParkingLot>> findAll();
    Optional<List<ParkingLot>> findAllByID(String id);
    Optional<ParkingLot> findByID(String id);
    void save(ParkingLot parkingLot);
    void deleteByID(String id);


}
