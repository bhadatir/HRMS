package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.JobRequest;
import com.example.HRMS.Backend.dto.TravelDocResponse;
import com.example.HRMS.Backend.dto.TravelPlanRequest;
import com.example.HRMS.Backend.model.GameType;
import com.example.HRMS.Backend.model.Job;
import com.example.HRMS.Backend.model.TravelDoc;
import com.example.HRMS.Backend.repository.EmployeeRepository;
import com.example.HRMS.Backend.repository.GameTypeRepository;
import com.example.HRMS.Backend.repository.JobRepository;
import com.example.HRMS.Backend.repository.TravelDocRepository;
import com.example.HRMS.Backend.service.JobService;
import com.example.HRMS.Backend.service.PostService;
import com.example.HRMS.Backend.service.TravelPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
public class HrController {

    private final TravelPlanService travelPlanService;
    private final TravelDocRepository travelDocRepository;
    private final GameTypeRepository gameTypeRepository;
    private final JobService jobService;
    private final PostService postService;

    @PostMapping("/travelPlan")
    public ResponseEntity<String > addNewTravelPlan(@Valid @RequestBody TravelPlanRequest travelPlanRequest) {
        travelPlanService.addTravelPlan(travelPlanRequest);
        return ResponseEntity.ok("Travel plan add successfully");
    }

    @PostMapping("/travelPlanDoc/{travelPlanId}/{docType}")
    public ResponseEntity<String > addTravelPlanDocByHR(@PathVariable Long travelPlanId, @RequestParam("file") MultipartFile file, @PathVariable("docType") Long docTypeId ) throws IOException {
        travelPlanService.saveDoc(travelPlanId, file, docTypeId);
        return ResponseEntity.ok("travel plan doc add successfully");
    }

    @GetMapping("/travelPlanDoc")
    public ResponseEntity<List<TravelDocResponse>> showAllTravelPlanDoc() throws IOException {
        return ResponseEntity.ok(travelPlanService.findAllTravelDoc());
    }

    @GetMapping("/travelDoc/{id}")
    public ResponseEntity<List<TravelDoc>> showTravelDocByEmpId(@PathVariable Long id) {
        return ResponseEntity.ok(travelDocRepository.findTravelDocByFkEmployee_Id(id));
    }

    @PostMapping("/game")
    public ResponseEntity<String> addNewGameType(@Valid @RequestBody GameType gameType) {
        gameTypeRepository.save(gameType);
        return ResponseEntity.ok("Game add successfully");
    }

    @PostMapping(value = "/job", consumes = {"multipart/form-data"})
    public ResponseEntity<String> addJob(@Valid @RequestPart("jobRequest") JobRequest jobRequest,
                                         @RequestPart("file") MultipartFile file) throws IOException {
        jobService.saveJob(jobRequest,file);
        return ResponseEntity.ok("Job add successfully");
    }

    @PostMapping("/cvReviewer/{jobId}/{empCvReviewerId}")
    public ResponseEntity<String> addCvReviewer(@PathVariable("jobId") Long jobId, @PathVariable("empCvReviewerId") Long empCvReviewerId) {
        jobService.saveCvReviewer(jobId,empCvReviewerId);
        return ResponseEntity.ok("Cv Reviewer add successfully");
    }

    @PatchMapping("/comment/{commentId}")
    public ResponseEntity<String> removeComment(@PathVariable("commentId") Long commentId) {
        postService.removeCommentByHr(commentId);
        return ResponseEntity.ok("comment remove successfully");
    }

    @PatchMapping("/post/{postId}")
    public ResponseEntity<String> removePost(@PathVariable("postId") Long postId) {
        postService.removePostByHr(postId);
        return ResponseEntity.ok("post remove successfully");
    }
}
