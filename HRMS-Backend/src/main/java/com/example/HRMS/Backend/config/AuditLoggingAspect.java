package com.example.HRMS.Backend.config;

import com.example.HRMS.Backend.dto.JobShareRequest;
import com.example.HRMS.Backend.dto.ReferFriendRequest;
import com.example.HRMS.Backend.dto.TravelPlanRequest;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Aspect
@Component
//@Slf4j
@RequiredArgsConstructor
public class AuditLoggingAspect {

    private final AuditLogRepository auditRepo;
    private final CvStatusTypeRepository cvStatusTypeRepository;
    private final ShareJobDataRepository shareJobDataRepository;
    private final EmployeeRepository employeeRepository;
    private final AuthAuditRepository authAuditRepository;
    private final AuditTravelPlanRepository auditTravelPlanRepository;

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

//        log.info("AUDIT LOG - Referral ID: {} | Status changed to: {} | Timestamp: {}",
//                referralId, cvStatusType.getCvStatusTypeName() , Instant.now());

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
//        log.info("AUDIT LOG - New Referral Created | Recipient info logged | Timestamp: {}",
//                LocalDateTime.now());

        if(args.length>0){
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

    @AfterReturning(
            pointcut = "execution(* com.example.HRMS.Backend.service.JobService.shareJob(..))"
    )
    public void logJobShare(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
//        log.info("AUDIT LOG - Job Share By User | Recipient info logged | Timestamp: {}",
//                LocalDateTime.now());

        if(args.length>0){
            JobShareRequest jobShareRequest = (JobShareRequest) args[0];

            for(String recipientEmail : jobShareRequest.getEmails()){
                ShareJobData log = new ShareJobData();
                log.setShareJobBy(email);
                log.setFkJobId(jobShareRequest.getFkJobId());
                log.setReceiverEmail(recipientEmail);
                log.setTimestamp(Instant.now());

                shareJobDataRepository.save(log);
            }
        }
    }

    @AfterReturning(
            pointcut = "execution(* com.example.HRMS.Backend.service.impl.AuthServiceImpl.login(..))"
    )
    public void loginAuthAction(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if(args.length>0){
            Employee employee = employeeRepository.findEmployeeByEmployeeEmail(email).orElseThrow(
                    () -> new RuntimeException("employee not found")
            );

            AuthAudit log = new AuthAudit();
            log.setUserEmail(employee.getEmployeeEmail());
            log.setUserRoleName(employee.getFkRole().getRoleName());
            authAuditRepository.save(log);
        }
    }

    @AfterReturning(
            pointcut = "execution(* com.example.HRMS.Backend.service.impl.TravelPlanServiceImpl.addTravelPlan(..))"
    )
    public void logCreateTravelPlan(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if(args.length>0){
            TravelPlanRequest travelPlan = (TravelPlanRequest) args[0];
            Employee hrEmp = employeeRepository.findEmployeeById(travelPlan.getFkTravelPlanHREmployeeId());
            AuditTravelPlan log = new AuditTravelPlan();
            log.setAction("Create");
            log.setPerformedBy(email);
            log.setOwnerEmail(hrEmp.getEmployeeEmail());
            List<Long> membersId = travelPlan.getEmployeesInTravelPlanId();
            List<String> members = membersId.stream().map(memberId -> {
                return employeeRepository.findEmployeeById(memberId).getEmployeeEmail();
            }).toList();
            log.setAddedTravelMembers(members);
            auditTravelPlanRepository.save(log);
        }
    }
}