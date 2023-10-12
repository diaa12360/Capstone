package com.example.demo.resources;

import com.example.demo.model.ParkingLot;
import com.example.demo.service.ParkingLotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parking-lot")
public class ParkingLotResource {
    private final ParkingLotService parkingLotService;

    @Autowired
    public ParkingLotResource(ParkingLotService parkingLotService) {
        this.parkingLotService = parkingLotService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<ParkingLot>> getAllParkingLots() {
        List<ParkingLot> parkingLots = parkingLotService.findAllParkingLots();
        return new ResponseEntity<>(parkingLots, HttpStatus.OK);
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<ParkingLot> getParkingLotById(@PathVariable("id") String id) {
        ParkingLot parkingLot = parkingLotService.findParkingLotById(id);
        return new ResponseEntity<>(parkingLot, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<ParkingLot> addParkingLot(@RequestBody ParkingLot parkingLot) {
        parkingLotService.addParkingLot(parkingLot);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<ParkingLot> updateParkingLot(@RequestBody ParkingLot parkingLot) {
        System.out.println(parkingLot);
        ParkingLot updateparkingLot = parkingLotService.updateParkingLot(parkingLot);
        return new ResponseEntity<>(updateparkingLot, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteParkingLot(@PathVariable("id") String id) {
        parkingLotService.deleteParkingLot(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}