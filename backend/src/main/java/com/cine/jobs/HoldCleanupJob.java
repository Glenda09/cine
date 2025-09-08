package com.cine.jobs;

import com.cine.service.HoldService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HoldCleanupJob {
    private final HoldService holdService;

    public HoldCleanupJob(HoldService holdService) { this.holdService = holdService; }

    @Scheduled(fixedDelay = 60000)
    public void cleanup() {
        holdService.releaseExpiredHolds();
    }
}

