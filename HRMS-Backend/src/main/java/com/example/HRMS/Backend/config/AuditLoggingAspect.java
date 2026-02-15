package com.example.HRMS.Backend.config;

import com.example.HRMS.Backend.dto.ReferFriendRequest;
import com.example.HRMS.Backend.model.AuditLog;
import com.example.HRMS.Backend.model.CvStatusType;
import com.example.HRMS.Backend.repository.AuditLogRepository;
import com.example.HRMS.Backend.repository.CvStatusTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLoggingAspect {

    private final AuditLogRepository auditRepo;
    private final CvStatusTypeRepository cvStatusTypeRepository;

    @AfterReturning(
            pointcut = "execution(* com.example.HRMS.Backend.service.JobService.updateStatus(..))"
    )
    public void logStatusChange(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long referralId = (Long) args[0];
        Long newStatus = (Long) args[1];
        CvStatusType cvStatusType = cvStatusTypeRepository.findCvStatusTypeById(newStatus);

        log.info("AUDIT LOG - Referral ID: {} | Status changed to: {} | Timestamp: {}",
                referralId, cvStatusType.getCvStatusTypeName() , Instant.now());

        AuditLog log = new AuditLog();
        log.setEntityName("Referral");
        log.setEntityId((Long) args[0]);
        log.setAction("STATUS_CHANGE");
        log.setNewStatus(cvStatusType.getCvStatusTypeName());
        log.setTimestamp(Instant.now());
        log.setPerformedBy(email);

        auditRepo.save(log);
    }

    @AfterReturning(
            pointcut = "execution(* com.example.HRMS.Backend.service.JobService.referFriend(..))"
    )
    public void logNewReferral(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.info("AUDIT LOG - New Referral Created | Recipient info logged | Timestamp: {}",
                LocalDateTime.now());

        if(args.length>0 && args[0] instanceof ReferFriendRequest){
            ReferFriendRequest referFriendRequest = (ReferFriendRequest) args[0];
            AuditLog log = new AuditLog();
            log.setEntityName("Referral");
            log.setEntityId(referFriendRequest.getFkReferFriendEmployeeId());
            log.setAction("Referral_Created");
            log.setTimestamp(Instant.now());
            log.setPerformedBy(email);

            auditRepo.save(log);
        }

    }
}