package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.model.GameType;

public interface DynamicCycleService {

    void checkAndResetCycles();

    int calculateCycleDays(GameType game);

    void performCycleReset(GameType gameType);
}
