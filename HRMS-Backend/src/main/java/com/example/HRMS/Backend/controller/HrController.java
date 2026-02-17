package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.JobRequest;
import com.example.HRMS.Backend.dto.ReferFriendResponse;
import com.example.HRMS.Backend.dto.TravelDocResponse;
import com.example.HRMS.Backend.dto.TravelPlanRequest;
import com.example.HRMS.Backend.model.GameType;
import com.example.HRMS.Backend.model.Job;
import com.example.HRMS.Backend.model.TravelDoc;
import com.example.HRMS.Backend.model.TravelPlan;
import com.example.HRMS.Backend.repository.EmployeeRepository;
import com.example.HRMS.Backend.repository.GameTypeRepository;
import com.example.HRMS.Backend.repository.JobRepository;
import com.example.HRMS.Backend.repository.TravelDocRepository;
import com.example.HRMS.Backend.service.ExpenseService;
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
    private final ExpenseService expenseService;

    @PostMapping("/travelPlan")
    public ResponseEntity<String > addNewTravelPlan(@Valid @RequestBody TravelPlanRequest travelPlanRequest) {
        travelPlanService.addTravelPlan(travelPlanRequest);
        return ResponseEntity.ok("Travel plan add successfully");
    }

    @PutMapping("/travelPlan/{travelPlanId}")
    public ResponseEntity<String> updateTravelPLan(@Valid @RequestBody TravelPlanRequest travelPlanRequest, @PathVariable Long travelPlanId){
        travelPlanService.updateTravelPlan(travelPlanRequest,travelPlanId);
        return ResponseEntity.ok("Travel Plan Update successfully");
    }

    @PostMapping("/travelPlanDoc/{employeeId}/{travelPlanId}/{docTypeId}")
    public ResponseEntity<String > addTravelPlanDocByHR(@PathVariable Long travelPlanId, @PathVariable Long employeeId,
                                                        @RequestParam("file") MultipartFile file, @PathVariable Long docTypeId ) throws IOException {
        travelPlanService.saveDocByHr(travelPlanId, file, docTypeId,employeeId);
        return ResponseEntity.ok("travel plan doc add successfully");
    }

    @GetMapping("/travelPlanDoc")
    public ResponseEntity<List<TravelDocResponse>> showAllTravelPlanDoc() throws IOException {
        return ResponseEntity.ok(travelPlanService.findAllTravelDoc());
    }

    @PatchMapping("/expense/{expId}/{statusId}")
    public ResponseEntity<String > updateExpenseStatus(@PathVariable Long expId, @PathVariable Long statusId)
    {   expenseService.updateExpenseStatus(expId, statusId);
        return ResponseEntity.ok("status update successful");
    }

    @PostMapping("/game")
    public ResponseEntity<String> addNewGameType(@Valid @RequestBody GameType gameType) {
        gameTypeRepository.save(gameType);
        return ResponseEntity.ok("Game add successfully");
    }

    @PostMapping(value = "/job", consumes = {"multipart/form-data"})
    public ResponseEntity<String> addJob(@Valid @RequestPart JobRequest jobRequest,
                                         @RequestPart("file") MultipartFile file) throws IOException {
        jobService.saveJob(jobRequest,file);
        return ResponseEntity.ok("Job add successfully");
    }

    @PutMapping(value = "/job/{jobId}", consumes = {"multipart/form-data"})
    public ResponseEntity<String> updateJob(@PathVariable Long jobId,
                                         @Valid @RequestPart JobRequest jobRequest,
                                         @RequestPart(value = "file",required = false) MultipartFile file) throws IOException {
        jobService.updateJob(jobId,jobRequest,file);
        return ResponseEntity.ok("Job update successfully");
    }

    @PostMapping("/cvReviewer/{jobId}/{empCvReviewerId}")
    public ResponseEntity<String> addCvReviewer(@PathVariable Long jobId, @PathVariable Long empCvReviewerId) {
        jobService.saveCvReviewer(jobId,empCvReviewerId);
        return ResponseEntity.ok("Cv Reviewer add successfully");
    }

    @PatchMapping("/referCV/{referId}/{statusId}")
    public ResponseEntity<String > updateReferCvStatus(@PathVariable Long referId, @PathVariable Long statusId)
    {   jobService.updateStatus(referId, statusId);
        return ResponseEntity.ok("status update successful");
    }

    @GetMapping("/referData/{jobId}")
    public ResponseEntity<List<ReferFriendResponse> > getReferDataByJobId(@PathVariable Long jobId){
        return ResponseEntity.ok(jobService.getReferDataByJobId(jobId));
    }

    @PatchMapping("/comment/{commentId}")
    public ResponseEntity<String> removeComment(@PathVariable Long commentId) {
        postService.removeCommentByHr(commentId);
        return ResponseEntity.ok("comment remove successfully");
    }

    @PatchMapping("/post/{postId}")
    public ResponseEntity<String> removePost(@PathVariable Long postId) {
        postService.removePostByHr(postId);
        return ResponseEntity.ok("post remove successfully");
    }
}
