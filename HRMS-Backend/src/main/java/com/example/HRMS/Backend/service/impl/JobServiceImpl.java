package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.dto.JobRequest;
import com.example.HRMS.Backend.dto.JobResponse;
import com.example.HRMS.Backend.dto.JobShareRequest;
import com.example.HRMS.Backend.dto.ReferFriendRequest;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import com.example.HRMS.Backend.service.JobService;
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

    @Value("${img.path}")
    private String folderPath;

    @Override
    public void saveJob(@Valid JobRequest jobRequest, MultipartFile file) throws IOException {
        Job job = new Job();

        JobType jobType = jobTypeRepository.findJobTypesById(jobRequest.getFkJobTypeId());
        Employee employee = employeeRepository.findEmployeeById(jobRequest.getFkJobOwnerEmployeeId());

        String time = Instant.now().toString().replace(":","-");

        String filePath = folderPath + jobRequest.getFkJobTypeId()+ " " + jobRequest.getFkJobOwnerEmployeeId()+ " " + time + "_" + file.getOriginalFilename();
        file.transferTo(new File(System.getProperty("user.dir") + "/" + filePath));

        job.setJobCreatedAt(Instant.now());
        job.setJobSalary(jobRequest.getJobSalary());
        job.setJobTitle(jobRequest.getJobTitle());
        job.setJobDescriptionUrl(filePath);
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

        jobShare.setFkJob(jobRepository.findJobById(jobShareRequest.getFkJobId()));
        jobShare.setJobShareCreatedAt(Instant.now());
        jobShare.setFkJobShareEmployee(employeeRepository.findEmployeeById(jobShareRequest.getFkJobShareEmployeeId()));

        jobShareRepository.save(jobShare);

        List<String> emails = jobShareRequest.getEmails();

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

        String filePath = folderPath + fkReferFriendEmployeeId + " " + time + "_" + file.getOriginalFilename();
        file.transferTo(new File(System.getProperty("user.dir") + "/" + filePath));

        CvStatusType cvStatusType = cvStatusTypeRepository.findById(
                referFriendRequest.getFkCvStatusTypeId()
        ).orElseThrow(() -> new RuntimeException("cv status type not found"));

        referFriend.setReferFriendShortNote(referFriendRequest.getReferFriendShortNote());
        referFriend.setReferFriendCvUrl(filePath);
        referFriend.setFkCvStatusType(cvStatusType);
        referFriend.setFkReferFriendEmployee(employeeRepository.findEmployeeById(
                referFriendRequest.getFkReferFriendEmployeeId()
        ));
        referFriend.setReferFriendEmail(referFriendRequest.getReferFriendEmail());
        referFriend.setFkJob(jobRepository.findJobById(referFriendRequest.getFkJobId()));
        referFriend.setReferFriendReviewStatusChangedAt(Instant.now());
        referFriend.setReferFriendName(referFriendRequest.getReferFriendName());

        referFriendRepositort.save(referFriend);
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
    public JobResponse showJobByJObId(Long jobId){
        return modelMapper.map(jobRepository.findJobById(jobId),JobResponse.class);
    }

}
