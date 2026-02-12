package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.*;
import com.example.HRMS.Backend.service.PostService;
import com.example.HRMS.Backend.service.impl.OrgChartServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(value = "/", consumes = {"multipart/form-data"})
        public ResponseEntity<String> addPost(@Valid @RequestPart("postRequest") PostRequest postRequest,
                                         @RequestPart("file") MultipartFile file) throws IOException {
        postService.savePost(postRequest,file);
        return ResponseEntity.ok("Post add successfully");
    }

    @GetMapping("/")
    public ResponseEntity<List<PostResponse>> allJob() {
        return ResponseEntity.ok(postService.showAllPosts());
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> jobByJobId(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.showPostByPostId(postId));
    }

    @PostMapping("/tag/{postId}/{tagTypeId}")
    public ResponseEntity<String> addPostTagOnPost(@PathVariable Long postId, @PathVariable Long tagTypeId) {
        postService.addTagOnPost(postId,tagTypeId);
        return ResponseEntity.ok("tag add on post successfully");
    }

    @GetMapping("/postTag/{postId}")
    public ResponseEntity<List<PostTagResponse>> getPostTagsById(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPostTagsById(postId));
    }

    @PostMapping("/comment")
    public ResponseEntity<String> addComment(@RequestBody CommentRequest commentRequest) {
        postService.addComment(commentRequest);
        return ResponseEntity.ok("comment add successfully");
    }

    @GetMapping("/comment/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsById(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getCommentsById(postId));
    }

    @PostMapping("/like")
    public ResponseEntity<String> addLike(@RequestBody LikeRequest likeRequest) {
        postService.addLike(likeRequest);
        return ResponseEntity.ok("like add successfully");
    }

    @GetMapping("/likeByPostId/{postId}")
    public ResponseEntity<List<LikeResponse>> getLikeByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getLikeByPostId(postId));
    }

    @GetMapping("/likeByCommentId/{commentId}")
    public ResponseEntity<List<LikeResponse>> getLikeByCommentId(@PathVariable Long commentId) {
        return ResponseEntity.ok(postService.getLikeByCommentId(commentId));
    }
}