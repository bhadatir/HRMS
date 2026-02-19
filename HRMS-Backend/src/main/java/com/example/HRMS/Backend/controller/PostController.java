package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.*;
import com.example.HRMS.Backend.model.Comment;
import com.example.HRMS.Backend.repository.CommentsRepository;
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
    private final CommentsRepository commentsRepository;

    @PostMapping(value = "/", consumes = {"multipart/form-data"})
        public ResponseEntity<String> addPost(@Valid @RequestPart("postRequest") PostRequest postRequest,
                                         @RequestPart("file") MultipartFile file) throws IOException {
        postService.savePost(postRequest,file);
        return ResponseEntity.ok("Post add successfully");
    }

    @GetMapping("/")
    public ResponseEntity<List<PostResponse>> allPost() {
        return ResponseEntity.ok(postService.showAllPosts());
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> postByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.showPostByPostId(postId));
    }

    @PutMapping(value = "/{postId}", consumes = {"multipart/form-data"})
    public ResponseEntity<String> updatePost(@PathVariable Long postId,
                                            @Valid @RequestPart PostRequest postRequest,
                                            @RequestPart(value = "file",required = false) MultipartFile file) throws IOException {
        postService.updatePost(postId, postRequest, file);
        return ResponseEntity.ok("Post update successfully");
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

    @DeleteMapping("/commentLike/{commentId}/{employeeId}")
    public ResponseEntity<String> removeLikeByCommentId(@PathVariable Long commentId
                                                        ,@PathVariable Long employeeId) {
        postService.removeLikeByCommentId(commentId, employeeId);
        return ResponseEntity.ok("remove like successfully");
    }

    @DeleteMapping("/postLike/{postId}/{employeeId}")
    public ResponseEntity<String> removeLikeByPostId(@PathVariable Long postId
                                                    ,@PathVariable Long employeeId) {
        postService.removeLikeByPostId(postId, employeeId);
        return ResponseEntity.ok("remove like successfully");
    }


    @PatchMapping("/comment/{commentId}")
    public ResponseEntity<String> removeComment(@PathVariable Long commentId) {
        postService.removeComment(commentId);
        return ResponseEntity.ok("comment remove successfully");
    }

    @PatchMapping("/rmPost/{postId}")
    public ResponseEntity<String> removePost(@PathVariable Long postId) {
        postService.removePost(postId);
        return ResponseEntity.ok("post remove successfully");
    }
}