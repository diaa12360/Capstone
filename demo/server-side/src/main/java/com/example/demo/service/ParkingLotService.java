package com.example.demo.service;

import com.example.demo.exception.ParkingLotNotFoundException;
import com.example.demo.repository.MyParkingLotsRepository;
import com.example.demo.model.ParkingLot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@EnableScheduling
public class ParkingLotService {
    private final MyParkingLotsRepository parkingLotRepository;

    @Autowired
    public ParkingLotService(MyParkingLotsRepository parkingLotRepository) {
        this.parkingLotRepository = parkingLotRepository;
    }


    public void addParkingLot(ParkingLot parkingLot) {
        parkingLotRepository.save(parkingLot);
    }

    public List<ParkingLot> findAllParkingLots() {
        return parkingLotRepository.findAll().orElseThrow(
                () -> new ParkingLotNotFoundException("There is no Parking lots!!")
        );
    }

    //TODO, Make it update.
    public ParkingLot updateParkingLot(ParkingLot parkingLot) {
        parkingLotRepository.save(parkingLot);
        return parkingLot;
    }

    public ParkingLot findParkingLotById(String id) {
        return parkingLotRepository.findByID(id).orElseThrow(
                () -> new ParkingLotNotFoundException("User by id " + id + " was not found")
        );
    }

    public void deleteParkingLot(String id) {
        parkingLotRepository.deleteByID(id);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void schedule() {
        try {
            findAllParkingLots().stream().filter((parkingLot) ->
                    parkingLot.getExpirationDate() != null &&
                            (parkingLot.getExpirationDate().before(new Date()) ||
                                    parkingLot.getExpirationDate().equals(new Date()))
            ).forEach((parkingLot) -> {
                parkingLot.setAvailable(true);
                parkingLot.setColor(null);
                parkingLot.setCarNumber(null);
                parkingLot.setStartDate(null);
                parkingLot.setExpirationDate(null);
                updateParkingLot(parkingLot);
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}