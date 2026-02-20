package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.*;
import com.example.HRMS.Backend.model.Job;
import com.example.HRMS.Backend.model.JobType;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface JobService {

    void saveJob(@Valid JobRequest job, MultipartFile file) throws IOException;

    void saveCvReviewer(Long jobId, Long empCvReviewerId);

    void shareJob(JobShareRequest jobShareRequest);

    void referFriend(ReferFriendRequest referFriendRequest, MultipartFile file) throws IOException;

    List<JobResponse> showAllJobs();

    JobResponse showJobByJobId(Long jobId);

    void updateStatus(Long referId, Long statusId, String reason);

    void updateJob(Long jobId, @Valid JobRequest jobRequest, MultipartFile file) throws IOException;

    List<ReferFriendResponse> getReferDataByJobId(Long jobId);

    List<JobType> getAllJobTypes();

    void changeJobStatus(Long jobId, String reason);
}
