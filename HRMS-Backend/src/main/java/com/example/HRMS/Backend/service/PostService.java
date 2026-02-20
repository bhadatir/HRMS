package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.*;
import com.example.HRMS.Backend.model.JobType;
import com.example.HRMS.Backend.model.PostVisibility;
import com.example.HRMS.Backend.model.TagType;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.beans.Visibility;
import java.io.IOException;
import java.util.List;

public interface PostService {
    void savePost(PostRequest postRequest, MultipartFile file) throws IOException;

    List<PostResponse> showAllPosts();

    PostResponse showPostByPostId(Long postId);

    void addTagOnPost(Long postId, Long tagTypeId);

    List<PostTagResponse> getPostTagsById(Long postId);

    void addComment(CommentRequest commentRequest);

    List<CommentResponse> getCommentsById(Long postId);

    void addLike(LikeRequest likeRequest);

    List<LikeResponse> getLikeByPostId(Long postId);

    List<LikeResponse> getLikeByCommentId(Long commentId);

    void removeCommentByHr(Long commentId, String reason);

    void removePostByHr(Long postId, String reason);

    void removeComment(Long commentId, String reason);

    void removePost(Long postId, String reason);

    void updatePost(Long postId, @Valid PostRequest postRequest, MultipartFile file) throws IOException;

    void removeLikeByCommentId(Long commentId,Long employeeId);

    void removeLikeByPostId(Long postId,Long employeeId);

    List<TagType> getPostTagTypes();

    List<PostVisibility> getAllVisibilities();

    void removePostTagFromPost(Long postId, Long tagTypeId);

}
