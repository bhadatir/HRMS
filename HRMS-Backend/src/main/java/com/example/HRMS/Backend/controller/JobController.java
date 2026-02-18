package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.*;
import com.example.HRMS.Backend.repository.JobRepository;
import com.example.HRMS.Backend.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping("/shareJob")
    public ResponseEntity<String> shareJob(@RequestBody JobShareRequest jobShareRequest) {
        jobService.shareJob(jobShareRequest);
        return ResponseEntity.ok("share job successfully");
    }

    @PostMapping(value = "/referFriend", consumes = {"multipart/form-data"})
    public ResponseEntity<String> referFriend(@Valid @RequestPart("referFriendRequest") ReferFriendRequest referFriendRequest,
                                              @RequestPart("file") MultipartFile file) throws IOException {
        jobService.referFriend(referFriendRequest,file);
        return ResponseEntity.ok("refer friend successfully");
    }

    @GetMapping("/referData/{jobId}")
    public ResponseEntity<List<ReferFriendResponse> > getReferDataByJobId(@PathVariable Long jobId){
        return ResponseEntity.ok(jobService.getReferDataByJobId(jobId));
    }

    @GetMapping("/")
    public ResponseEntity<List<JobResponse>> allJob() {
        return ResponseEntity.ok(jobService.showAllJobs());
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> jobByJobId(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.showJobByJobId(jobId));
    }

}
