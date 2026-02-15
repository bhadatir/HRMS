package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.dto.JobRequest;
import com.example.HRMS.Backend.dto.JobResponse;
import com.example.HRMS.Backend.dto.JobShareRequest;
import com.example.HRMS.Backend.dto.ReferFriendRequest;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import com.example.HRMS.Backend.service.EmailService;
import com.example.HRMS.Backend.service.JobService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final EmployeeRepository employeeRepository;
    private final JobTypeRepository jobTypeRepository;
    private final CvReviewerRepository cvReviewerRepository;
    private final JobShareRepository jobShareRepository;
    private final JobShareToRepository jobShareToRepository;
    private final ReferFriendRepositort referFriendRepositort;
    private final CvStatusTypeRepository cvStatusTypeRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;

    @Value("${img.path}")
    private String folderPath;

    @Value("${URL.path}")
    private String URL;

    @Override
    public void saveJob(@Valid JobRequest jobRequest, MultipartFile file) throws IOException {
        Job job = new Job();

        JobType jobType = jobTypeRepository.findJobTypesById(jobRequest.getFkJobTypeId());
        Employee employee = employeeRepository.findEmployeeById(jobRequest.getFkJobOwnerEmployeeId());

        String time = Instant.now().toString().replace(":","-");

        String originalFilePath = Objects.requireNonNull(file.getOriginalFilename()).replace(" ","_");
        String filePath = "job_description/" + jobRequest.getFkJobTypeId() +"_" + jobRequest.getFkJobOwnerEmployeeId() + "_" + time + "_" + originalFilePath;
        file.transferTo(new File(System.getProperty("user.dir") + "/" + folderPath + filePath));


        job.setJobCreatedAt(Instant.now());
        job.setJobSalary(jobRequest.getJobSalary());
        job.setJobTitle(jobRequest.getJobTitle());
        job.setJobDescriptionUrl(URL + filePath);
        job.setFkJobType(jobType);
        job.setFkJobOwnerEmployee(employee);
        jobRepository.save(job);
    }

    @Override
    public void saveCvReviewer(Long jobId, Long empCvReviewerId){
        CvReviewer cvReviewer = new CvReviewer();
        cvReviewer.setCvReviewerCreatedAt(Instant.now());
        cvReviewer.setFkJob(jobRepository.findJobById(jobId));
        cvReviewer.setFkCvReviewerEmployee(employeeRepository.findEmployeeById(empCvReviewerId));

        cvReviewerRepository.save(cvReviewer);
    }

    @Override
    public void shareJob(JobShareRequest jobShareRequest){
        JobShare jobShare = new JobShare();
        Job job = jobRepository.findJobById(jobShareRequest.getFkJobId());
        jobShare.setFkJob(job);
        jobShare.setJobShareCreatedAt(Instant.now());
        jobShare.setFkJobShareEmployee(employeeRepository.findEmployeeById(jobShareRequest.getFkJobShareEmployeeId()));

        jobShareRepository.save(jobShare);

        List<String> emails = jobShareRequest.getEmails();

        emailService.sendEmailWithAttachement(emails,job.getJobTitle(),"job summary",job.getJobDescriptionUrl());

        for(String email : emails){
            JobShareTo jobShareTo = new JobShareTo();
            jobShareTo.setFkJobShare(jobShare);
            jobShareTo.setJobShareToEmail(email);

            jobShareToRepository.save(jobShareTo);
        }
    }

    @Override
    public void referFriend(ReferFriendRequest referFriendRequest, MultipartFile file) throws IOException{
        ReferFriend referFriend = new ReferFriend();

        Long fkReferFriendEmployeeId = referFriendRequest.getFkReferFriendEmployeeId();

        String time = Instant.now().toString().replace(":","-");

        String originalFilePath = Objects.requireNonNull(file.getOriginalFilename()).replace(" ","_");
        String filePath = "refer_friend_cv/" + fkReferFriendEmployeeId +"_" + time + "_" + originalFilePath;

        file.transferTo(new File(System.getProperty("user.dir") + "/" + folderPath + filePath));

        CvStatusType cvStatusType = cvStatusTypeRepository.findById(
                referFriendRequest.getFkCvStatusTypeId()
        ).orElseThrow(() -> new RuntimeException("cv status type not found"));

        referFriend.setReferFriendShortNote(referFriendRequest.getReferFriendShortNote());
        referFriend.setReferFriendCvUrl(URL + filePath);
        referFriend.setFkCvStatusType(cvStatusType);

        Employee employee = employeeRepository.findEmployeeById(fkReferFriendEmployeeId);
        referFriend.setFkReferFriendEmployee(employee);
        referFriend.setReferFriendEmail(referFriendRequest.getReferFriendEmail());

        Job job =jobRepository.findJobById(referFriendRequest.getFkJobId());

        referFriend.setFkJob(job);
        referFriend.setReferFriendReviewStatusChangedAt(Instant.now());
        referFriend.setReferFriendName(referFriendRequest.getReferFriendName());

        List<CvReviewer> cvReviewers = cvReviewerRepository.findCvReviewerByFkJob_Id(job.getId());

        List<String> emails = new ArrayList<>();
        emails.add(referFriendRequest.getReferFriendEmail());
        emails.add(job.getFkJobOwnerEmployee().getEmployeeEmail());
        emails.add("tirthbhadani3@gmail.com");

        for(CvReviewer cvReviewer : cvReviewers)
        {
            emails.add(cvReviewer.getFkCvReviewerEmployee().getEmployeeEmail());
        }


        referFriendRepositort.save(referFriend);

        String absolutePath = new File(folderPath + filePath).getAbsolutePath();
        //it's not working

        File savedFile = new File(System.getProperty("user.dir") + "/" + folderPath + filePath);
        if(savedFile.exists() && savedFile.canRead()) {
            emailService.sendEmailWithAttachement(emails,
                    job.getId() + " " + job.getJobTitle(),
                    "refer friend details : " + "\n"
                            + "email : " + referFriendRequest.getReferFriendEmail()
                            + "name : " + referFriendRequest.getReferFriendName()
                            + "employee detail : " + employee.getId() + " " + employee.getEmployeeEmail()
                    , absolutePath);
        }
        else {
            throw new IOException("file not found at : "+savedFile.getAbsolutePath());
        }
    }

    @Override
    public List<JobResponse> showAllJobs(){
        List<JobResponse> jobResponses =new ArrayList<>();

        List<Job> jobs = jobRepository.findAll();

        for(Job job:jobs){
            JobResponse jobResponse = modelMapper.map(job,JobResponse.class);
            jobResponses.add(jobResponse);
        }

        return jobResponses;
    }

    @Override
    public JobResponse showJobByJobId(Long jobId){
        return modelMapper.map(jobRepository.findJobById(jobId),JobResponse.class);
    }

    @Transactional
    @Override
    public void updateStatus(Long referId, Long statusId){
        ReferFriend referFriend = referFriendRepositort.findById(referId).orElseThrow(
                () -> new RuntimeException("refer friend not found")
        );
        referFriend.setFkCvStatusType(cvStatusTypeRepository.findById(statusId).orElseThrow(
                () -> new RuntimeException("cv status type not found")
        ));
        referFriendRepositort.save(referFriend);
    }

}
