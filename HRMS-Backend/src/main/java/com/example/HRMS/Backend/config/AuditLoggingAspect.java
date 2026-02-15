package com.example.HRMS.Backend.config;

import com.example.HRMS.Backend.dto.ReferFriendRequest;
import com.example.HRMS.Backend.model.AuditLog;
import com.example.HRMS.Backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLoggingAspect {

    private final AuditLogRepository auditRepo;


    @AfterReturning(
            pointcut = "execution(* com.example.HRMS.Backend.service.JobService.updateStatus(..))"
    )
    public void logStatusChange(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        Long referralId = (Long) args[0];
        String newStatus = (String) args[1];

        log.info("AUDIT LOG - Referral ID: {} | Status changed to: {} | Timestamp: {}",
                referralId, newStatus, Instant.now());

        AuditLog log = new AuditLog();
        log.setEntityName("Referral");
        log.setEntityId((Long) args[0]);
        log.setAction("STATUS_CHANGE");
        log.setNewStatus((String) args[1]);
        log.setTimestamp(Instant.now());

        auditRepo.save(log);
    }

    @AfterReturning(
            pointcut = "execution(* com.example.HRMS.Backend.service.JobService.referFriend(..))"
    )
    public void logNewReferral(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        log.info("AUDIT LOG - New Referral Created | Recipient info logged | Timestamp: {}",
                LocalDateTime.now());

        if(args.length>0 && args[0] instanceof ReferFriendRequest){
            ReferFriendRequest referFriendRequest = (ReferFriendRequest) args[0];
            AuditLog log = new AuditLog();
            log.setEntityName("Referral");
            log.setEntityId(referFriendRequest.getFkReferFriendEmployeeId());
            log.setAction("Referral_Created");
            log.setTimestamp(Instant.now());

            auditRepo.save(log);
        }

    }
}