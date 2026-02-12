package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.*;
import org.springframework.web.multipart.MultipartFile;

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
}
