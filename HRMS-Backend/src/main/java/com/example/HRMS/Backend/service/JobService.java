package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.JobRequest;
import com.example.HRMS.Backend.dto.JobResponse;
import com.example.HRMS.Backend.dto.JobShareRequest;
import com.example.HRMS.Backend.dto.ReferFriendRequest;
import com.example.HRMS.Backend.model.Job;
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

    JobResponse showJobByJObId(Long jobId);
}
