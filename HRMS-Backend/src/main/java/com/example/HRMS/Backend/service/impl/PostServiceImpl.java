package com.example.HRMS.Backend.service.impl;

import ch.qos.logback.core.model.Model;
import com.example.HRMS.Backend.dto.*;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import com.example.HRMS.Backend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final CommentsRepository commentsRepository;
    private final LikesRepository likesRepository;
    private final TagTypesRepository tagTypesRepository;
    private final ModelMapper modelMapper;
    private final EmployeeRepository employeeRepository;

    @Value("${img.path}")
    private String folderPath;

    @Override
    public void savePost(PostRequest postRequest, MultipartFile file) throws IOException {
        Post post = new Post();

        String time = Instant.now().toString().replace(":","-");

        String filePath = folderPath + time + postRequest.getFkPostEmployeeId() + "_" + file.getOriginalFilename();
        file.transferTo(new File(System.getProperty("user.dir") + "/" + filePath));

        post.setFkPostEmployee(employeeRepository.findEmployeeById(postRequest.getFkPostEmployeeId()));
        post.setPostContent(postRequest.getPostContent());
        post.setPostTitle(postRequest.getPostTitle());
        post.setPostContentUrl(filePath);

        postRepository.save(post);
    }

    @Override
    public List<PostResponse> showAllPosts(){
        List<PostResponse> postResponses =new ArrayList<>();

        List<Post> posts = postRepository.findAll();

        for(Post post:posts){
            PostResponse postResponse = modelMapper.map(post,PostResponse.class);
            postResponses.add(postResponse);
        }

        return postResponses;
    }

    @Override
    public PostResponse showPostByPostId(Long postId){
        return modelMapper.map(postRepository.findPostsById(postId),PostResponse.class);
    }

    @Override
    public void addTagOnPost(Long postId, Long tagTypeId){
        PostTag postTag = new PostTag();
        postTag.setFkPost(postRepository.findById(postId).orElseThrow(
                () -> new RuntimeException("post id is required")
        ));
        postTag.setFkTagType(tagTypesRepository.findById(tagTypeId).orElseThrow(
                () -> new RuntimeException("tag type is required")
        ));
        postTagRepository.save(postTag);
    }

    @Override
    public List<PostTagResponse> getPostTagsById(Long postId){
        List<PostTagResponse> postTagResponses = new ArrayList<>();
        List<PostTag> postTags = postTagRepository.findPostTagsByFkPost_Id(postId);
        for(PostTag postTag: postTags)
        {
            PostTagResponse postTagResponse = modelMapper.map(postTag, PostTagResponse.class);
            postTagResponses.add(postTagResponse);
        }

        return postTagResponses;
    }

    @Override
    public void addComment(CommentRequest commentRequest){
        Comment comment = new Comment();

        comment.setCommentContent(commentRequest.getCommentContent());
        comment.setFkCommentEmployee(employeeRepository.findEmployeeById(commentRequest.getFkCommentEmployeeId()));
        comment.setFkPost(postRepository.findPostsById(commentRequest.getFkPostId()));

        if(commentRequest.getParentCommentId() != null){
            comment.setParentComment(commentsRepository.findById(commentRequest.getParentCommentId()).orElseThrow(
            ()->new RuntimeException("parent comment id is required"))
            );
        }
        commentsRepository.save(comment);
    }

    @Override
    public List<CommentResponse> getCommentsById(Long postId){
        List<CommentResponse> commentResponses = new ArrayList<>();

        List<Comment> comments = commentsRepository.findCommentByFkPost_Id(postId);

        for(Comment comment: comments)
        {
            CommentResponse commentResponse = modelMapper.map(comment, CommentResponse.class);
            commentResponses.add(commentResponse);
        }

        return commentResponses;
    }

    @Override
    public void addLike(LikeRequest likeRequest){
        Like like =new Like();

        like.setFkPost(postRepository.findPostsById(likeRequest.getFkPostId()));
        like.setFkLikeEmployee(employeeRepository.findEmployeeById(likeRequest.getFkLikeEmployeeId()));

        if (likeRequest.getFkCommentId() != null) {
            like.setFkComment(commentsRepository.findCommentById(likeRequest.getFkCommentId()));
        }

        likesRepository.save(like);
    }

    @Override
    public List<LikeResponse> getLikeByPostId(Long postId){
        List<LikeResponse> likeResponses = new ArrayList<>();

        List<Like> likes = likesRepository.findLikesByFkPost_Id(postId);

        for(Like like: likes)
        {
            LikeResponse likeResponse = modelMapper.map(like, LikeResponse.class);
            likeResponses.add(likeResponse);
        }

        return likeResponses;
    }

    @Override
    public List<LikeResponse> getLikeByCommentId(Long commentId){
        List<LikeResponse> likeResponses = new ArrayList<>();

        List<Like> likes = likesRepository.findLikesByFkComment_Id(commentId);

        for(Like like: likes)
        {
            LikeResponse likeResponse = modelMapper.map(like, LikeResponse.class);
            likeResponses.add(likeResponse);
        }

        return likeResponses;
    }
}
