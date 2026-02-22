package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.JobRequest;
import com.example.HRMS.Backend.dto.ReferFriendResponse;
import com.example.HRMS.Backend.dto.TravelDocResponse;
import com.example.HRMS.Backend.dto.TravelPlanRequest;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import com.example.HRMS.Backend.service.*;
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
    private final CommentsRepository commentsRepository;
    private final GameTypeRepository gameTypeRepository;
    private final JobService jobService;
    private final PostService postService;
    private final ExpenseService expenseService;
    private final GameBookingService gameBookingService;

    @PostMapping("/travelPlan")
    public ResponseEntity<String > addNewTravelPlan(@Valid @RequestBody TravelPlanRequest travelPlanRequest) {
        travelPlanService.addTravelPlan(travelPlanRequest);
        return ResponseEntity.ok("Travel plan add successfully");
    }

    @PatchMapping("/deleteTravel/{travelPlanId}")
    public ResponseEntity<String> softDeleteUser(@PathVariable Long travelPlanId,@RequestParam String reason) {
        travelPlanService.markAsDeleted(travelPlanId, reason);
        return ResponseEntity.ok("Travel plan delete successfully");
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

    @PostMapping("/gameType")
    public ResponseEntity<String> addGame(@RequestBody GameType gameType){
        gameTypeRepository.save(gameType);
        return ResponseEntity.ok("game type added successfully");
    }

    @PutMapping("/gameType/{gameTypeId}")
    public ResponseEntity<String> updateGame(@PathVariable Long gameTypeId, @RequestBody GameType gameType){
        gameBookingService.updateGame(gameTypeId, gameType);
        return ResponseEntity.ok("game type updated successfully");
    }

    @PostMapping(value = "/job", consumes = {"multipart/form-data"})
    public ResponseEntity<String> addJob(@Valid @RequestPart JobRequest jobRequest,
                                         @RequestPart("file") MultipartFile file) throws IOException {
        jobService.saveJob(jobRequest,file);
        return ResponseEntity.ok("Job add successfully");
    }

    @GetMapping("/jobTypes")
    public ResponseEntity<List<JobType>> getAllJobTypes() {
        return ResponseEntity.ok(jobService.getAllJobTypes());
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
    public ResponseEntity<String > updateReferCvStatus(@PathVariable Long referId, @PathVariable Long statusId,@RequestParam String reason)
    {   jobService.updateStatus(referId, statusId, reason);
        return ResponseEntity.ok("status update successful");
    }

    @PatchMapping("/comment/{commentId}")
    public ResponseEntity<String> removeComment(@PathVariable Long commentId, @RequestParam String reason) {
        postService.removeCommentByHr(commentId, reason);
        return ResponseEntity.ok("comment remove successfully");
    }

    @PatchMapping("/rmPost/{postId}")
    public ResponseEntity<String> removePost(@PathVariable Long postId, @RequestParam String reason) {
        postService.removePostByHr(postId, reason);
        return ResponseEntity.ok("post remove successfully");
    }

    @PatchMapping("/jobStatus/{jobId}")
    public ResponseEntity<String > changeJobStatus(@PathVariable Long jobId,@RequestParam String reason)
    {   jobService.changeJobStatus(jobId,reason);
        return ResponseEntity.ok("job status update successful");
    }
}
