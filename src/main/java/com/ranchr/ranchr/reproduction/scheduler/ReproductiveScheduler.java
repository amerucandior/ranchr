package com.ranchr.ranchr.reproduction.scheduler;

import com.ranchr.ranchr.reproduction.service.ReproductiveCycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReproductiveScheduler {

    private final ReproductiveCycleService reproductiveCycleService;

    /**
     * Executes daily at 07:00 AM server time to evaluate Day 21 non-return,
     * Day 60 pregnancy diagnosis, and Day 280 calving milestones.
     */
    @Scheduled(cron = "${ranchr.reproduction.cron:0 0 7 * * *}")
    public void runDailyReproductiveMilestones() {
        log.info("[Scheduler] Starting daily reproductive milestone evaluation...");
        try {
            reproductiveCycleService.processScheduledAlerts();
            log.info("[Scheduler] Completed daily reproductive milestone evaluation.");
        } catch (Exception ex) {
            log.error("[Scheduler] Error processing reproductive milestones: {}", ex.getMessage(), ex);
        }
    }
}
