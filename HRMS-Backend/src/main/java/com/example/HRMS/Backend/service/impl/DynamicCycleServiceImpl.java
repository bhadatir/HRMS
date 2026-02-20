package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.model.EmployeeGameInterest;
import com.example.HRMS.Backend.model.GameType;
import com.example.HRMS.Backend.repository.EmployeeGameInterestRepository;
import com.example.HRMS.Backend.repository.GameTypeRepository;
import com.example.HRMS.Backend.service.DynamicCycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DynamicCycleServiceImpl implements DynamicCycleService {

    private final GameTypeRepository gameTypeRepo;

    private final EmployeeGameInterestRepository employeeGameInterestRepository;

    //run every day to check cycle complete or not
    //if cycle complete so reset isPlayed flag
    @Override
    @Scheduled(cron = "0 0 * * * *")
    public void checkAndResetCycles() {
        List<GameType> allGames = gameTypeRepo.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (GameType game : allGames) {
            int cycleDays = calculateCycleDays(game);

            LocalDateTime nextReset = game.getLastCycleResetDatetime().plusDays(cycleDays);

            if (now.isAfter(nextReset)) {
                performCycleReset(game);
            }
        }
    }

    @Override
    public int calculateCycleDays(GameType game) {
        long interestedCount = employeeGameInterestRepository.countByFkGameTypeId(game.getId());
        int dailyCapacity = game.getTotalSlotsPerDay() * game.getGameMaxPlayerPerSlot();

        if (dailyCapacity == 0) return 1;
        return (int) Math.ceil((double) interestedCount / dailyCapacity);
    }

    @Override
    public void performCycleReset(GameType gameType) {
        resetFlagsByGameTypeId(gameType.getId());

        gameType.setLastCycleResetDatetime(LocalDateTime.now());
        gameTypeRepo.save(gameType);
    }

    @Override
    public void resetFlagsByGameTypeId(Long gameTypeId){
        List<EmployeeGameInterest> employeeGameInterests = employeeGameInterestRepository.getEmployeeGameInterestByFkGameType_Id(gameTypeId);

        for(EmployeeGameInterest employeeGameInterest : employeeGameInterests){
            employeeGameInterest.setPlayedInCurrentCycle(0);
        }
    }
}
