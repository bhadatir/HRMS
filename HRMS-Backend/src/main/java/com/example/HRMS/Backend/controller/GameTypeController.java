package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.model.GameType;
import com.example.HRMS.Backend.repository.EmployeeGameInterestRepository;
import com.example.HRMS.Backend.repository.GameTypeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/gameType")
@RequiredArgsConstructor
public class GameTypeController {

    private final GameTypeRepository gameTypeRepository;

    @GetMapping("/allGames")
    public ResponseEntity<List<GameType>> showAllGames() {
        return ResponseEntity.ok(gameTypeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameType> findGameById(@PathVariable Long id){
        return ResponseEntity.ok(gameTypeRepository.findGameTypeById(id));
    }

}
