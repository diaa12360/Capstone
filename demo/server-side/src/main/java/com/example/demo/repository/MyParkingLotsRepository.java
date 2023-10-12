package com.example.demo.repository;

import com.example.demo.model.ParkingLot;
import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Component
@Repository
public class MyParkingLotsRepository implements ParkingLotsDAO {

    private final DatabaseUtil databaseUtil;

    @PostConstruct
    public void setDatabaseUtil() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", "string");
        jsonObject.put("name", "String");
        jsonObject.put("available", "boolean");
        jsonObject.put("customerName", "String");
        jsonObject.put("color", "String");
        jsonObject.put("startDate", "String");
        jsonObject.put("expiration_date", "String");
        jsonObject.put("phoneNumber", "number");
        databaseUtil.setCollectionName("parkingLots", jsonObject);
    }

    @Override
    public Optional<List<ParkingLot>> findAll() {
        List<ParkingLot> parkingLots = new ArrayList<>();
        Gson gson = new Gson();
        for (JSONObject s : databaseUtil.findAll()) {
            parkingLots.add(gson.fromJson(s.toJSONString(), ParkingLot.class));
        }
        return Optional.of(parkingLots);
    }

    @Override
    public Optional<List<ParkingLot>> findAllByID(String id) {
        List<ParkingLot> parkingLots = new ArrayList<>();
        Gson gson = new Gson();
        for (JSONObject s : databaseUtil.findAllByID(String.valueOf(id))) {
            parkingLots.add(gson.fromJson(s.toJSONString(), ParkingLot.class));
        }
        return Optional.of(parkingLots);
    }

    @Override
    public Optional<ParkingLot> findByID(String id) {
        return Optional.of(new Gson().fromJson(databaseUtil.findByID(String.valueOf(id)).toJSONString(), ParkingLot.class));
    }

    @Override
    public void save(ParkingLot parkingLot) {
        try {
            databaseUtil.save((JSONObject) new JSONParser().parse(new Gson().toJson(parkingLot)));
        } catch (ParseException e) {

        }
    }

    @Override
    public void deleteByID(String id) {
        ParkingLot parkingLot = findByID(id).orElseThrow();
        databaseUtil.delete(parkingLot);
    }
}
