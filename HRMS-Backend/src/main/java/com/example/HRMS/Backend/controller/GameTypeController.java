package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.model.GameType;
import com.example.HRMS.Backend.repository.GameTypeRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/hr/gameType")
public class GameTypeController {

    private final GameTypeRepository gameTypeRepository;

    @Autowired
    public GameTypeController(GameTypeRepository gameTypeRepository)
    {
        this.gameTypeRepository=gameTypeRepository;
    }

    @PostMapping("/")
        public ResponseEntity<GameType> addNewGameType(@Valid @RequestBody GameType gameType) {
            return ResponseEntity.ok(gameTypeRepository.save(gameType));
        }

    @GetMapping("/allGames")
    public ResponseEntity<List<GameType>> showAllGames() {
        return ResponseEntity.ok(gameTypeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameType> findGameById(@PathVariable Long id){
        return ResponseEntity.ok(gameTypeRepository.findGameTypeById(id));
    }
}
