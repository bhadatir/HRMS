package com.example.HRMS.Backend.service.impl;

import ch.qos.logback.core.model.Model;
import com.example.HRMS.Backend.dto.*;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import com.example.HRMS.Backend.service.EmailService;
import com.example.HRMS.Backend.service.NotificationService;
import com.example.HRMS.Backend.service.PostService;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.beans.Visibility;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private final EmailService emailService;
    private final PostVisibilityRepository postVisibilityRepository;
    private final NotificationService notificationService;

    @Value("${img.path}")
    private String folderPath;

    @Value("${URL.path}")
    private String URL;

    @Override
    public void savePost(PostRequest postRequest, MultipartFile file) throws IOException {
        Post post = new Post();

        String time = Instant.now().toString().replace(":","-");

        String originalFilePath = Objects.requireNonNull(file.getOriginalFilename()).replace(" ","_");
        String filePath = "post_content/" + time +"_" + postRequest.getFkPostEmployeeId() + "_" + originalFilePath;

        file.transferTo(new File(System.getProperty("user.dir") + "/" +folderPath + filePath));

        post.setFkPostEmployee(employeeRepository.findEmployeeById(postRequest.getFkPostEmployeeId()));
        post.setPostContent(postRequest.getPostContent());
        post.setPostTitle(postRequest.getPostTitle());
        post.setPostContentUrl(URL + filePath);
        post.setFkPostVisibility(postVisibilityRepository.findPostVisibilitiesById(postRequest.getFkPostVisibilityId()));

        postRepository.save(post);
    }

    @Override
    public Page<PostResponse> showAllPosts(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Post> postPage = postRepository.findByPostIsDeletedFalse(pageable);

        return postPage.map(post -> {

            PostResponse postResponse = modelMapper.map(post, PostResponse.class);

            List<PostTag> postTags = postTagRepository
                    .findPostTagsByFkPost_Id(post.getId());

            List<PostTagResponse> postTagResponses = postTags.stream()
                    .map(tag -> modelMapper.map(tag, PostTagResponse.class))
                    .toList();

            postResponse.setPostTagResponses(postTagResponses);

            return postResponse;
        });
    }

    @Override
    public PostResponse showPostByPostId(Long postId){
        return modelMapper.map(postRepository.findPostsById(postId),PostResponse.class);
    }

    @Override
    public void addTagOnPost(Long postId, Long tagTypeId){

        if(Boolean.TRUE.equals(postRepository.findPostsById(postId).getPostIsDeleted())){
            throw new RuntimeException("deleted post cannot be add tags.");
        }

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
    public void updatePost(Long postId, @Valid PostRequest postRequest, MultipartFile file) throws IOException {
        Post post = postRepository.findPostsById(postId);

        if(Boolean.TRUE.equals(post.getPostIsDeleted())){
            throw new RuntimeException("deleted post cannot be edit it.");
        }

        if(file != null && !file.isEmpty()){
            String time = Instant.now().toString().replace(":","-");

            String originalFilePath = Objects.requireNonNull(file.getOriginalFilename()).replace(" ","_");
            String filePath = "post_content/" + postRequest.getFkPostEmployeeId()
                                + "_" + time + "_" + originalFilePath;
            file.transferTo(new File(System.getProperty("user.dir") + "/" + folderPath + filePath));
            post.setPostContentUrl(URL + filePath);
        }

        post.setPostTitle(postRequest.getPostTitle());
        post.setPostContent(postRequest.getPostContent());
        post.setFkPostVisibility(postVisibilityRepository.findPostVisibilitiesById(postRequest.getFkPostVisibilityId()));

        postRepository.save(post);
    }

    @Override
    public void addComment(CommentRequest commentRequest){

        if(commentRequest.getFkPostId()!=null &&  Boolean.TRUE.equals(postRepository.findPostsById(commentRequest.getFkPostId()).getPostIsDeleted())){
            throw new RuntimeException("deleted post cannot be add comments.");
        }

        if(commentRequest.getParentCommentId()!=null && commentsRepository.findCommentById(commentRequest.getParentCommentId()) != null) {
            Comment parentComment = commentsRepository.findCommentById(commentRequest.getParentCommentId());
            if (Boolean.TRUE.equals(parentComment.getFkPost().getPostIsDeleted())) {
                throw new RuntimeException("deleted parent comment cannot be add sub comments.");
            }
        }

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
            if(comment.getParentComment() != null) {
                Comment comment1 = comment.getParentComment();
                Employee employee = comment1.getFkCommentEmployee();
                commentResponse.setParentCommentEmployeeEmail(employee.getEmployeeEmail());
            }
            commentResponses.add(commentResponse);
        }

        return commentResponses;
    }

    @Override
    public void addLike(LikeRequest likeRequest){

        if(likeRequest.getFkPostId()!=null && postRepository.findPostsById(likeRequest.getFkPostId()) == null){
            Comment comment = commentsRepository.findCommentById(likeRequest.getFkCommentId());
            if(comment.getFkPost() != null && Boolean.TRUE.equals(comment.getFkPost().getPostIsDeleted())){
                throw new RuntimeException("deleted parent comment cannot be add like.");
            }
        }
        if(likeRequest.getFkPostId() == null && likeRequest.getFkCommentId() != null){
            Comment comment = commentsRepository.findCommentById(likeRequest.getFkCommentId());
            if(comment.getFkPost() != null && Boolean.TRUE.equals(comment.getFkPost().getPostIsDeleted())){
                throw new RuntimeException("deleted parent comment cannot be add like.");
            }
        }


        if(likeRequest.getFkPostId()!=null && postRepository.findPostsById(likeRequest.getFkPostId()) != null && Boolean.TRUE.equals(postRepository.findPostsById(likeRequest.getFkPostId()).getPostIsDeleted())){
            throw new RuntimeException("deleted post cannot be add like.");
        }


        Like like =new Like();

        like.setFkLikeEmployee(employeeRepository.findEmployeeById(likeRequest.getFkLikeEmployeeId()));

        if (likeRequest.getFkCommentId() != null) {
            like.setFkComment(commentsRepository.findCommentById(likeRequest.getFkCommentId()));
        }

        if (likeRequest.getFkPostId() != null) {
            like.setFkPost(postRepository.findPostsById(likeRequest.getFkPostId()));
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

    @Override
    public void removeCommentByHr(Long commentId, String reason){

        Comment comment = commentsRepository.findCommentById(commentId);

        if(comment.getFkPost() != null && Boolean.TRUE.equals(comment.getFkPost().getPostIsDeleted())){
            throw new RuntimeException("deleted post cannot be remove comments.");
        }

        if(comment.getParentComment()!=null && comment.getParentComment().getFkPost() != null && Boolean.TRUE.equals(comment.getParentComment().getFkPost().getPostIsDeleted())){
            throw new RuntimeException("deleted parent comment cannot be remove sub comments.");
        }

        commentsRepository.makeCommentIdDeleted(commentId);
        commentsRepository.addReasonForDeletion(commentId, reason);
        likesRepository.removeLikesForDeletedComment(commentId);

        String email = comment.getFkCommentEmployee().getEmployeeEmail();
        List<String> emails = new ArrayList<>();
        emails.add(email);
        emailService.sendEmail(emails, "Warning mail", "Do not share this type of content second time :" + comment.getCommentContent());
        notificationService.createNotification(comment.getFkCommentEmployee().getId()
                ,"WARNING : comment is deleted by hr"
                , " at :" + LocalDateTime.now() + " comment content : "
                        + comment.getCommentContent()
        );
    }

    @Override
    @Transactional
    public void removePostByHr(Long postId, String reason){
        Post post = postRepository.findPostsById(postId);

        if(Boolean.TRUE.equals(post.getPostIsDeleted())){
            throw new RuntimeException("deleted post cannot be remove again.");
        }

        post.setPostIsDeleted(true);
        post.setReasonForDeletePost(reason);
        List<Comment> comments = commentsRepository.findCommentByFkPost_Id(postId);

        for(Comment comment : comments)
        {
            commentsRepository.makeCommentIdDeleted(comment.getId());
            likesRepository.removeLikesForDeletedComment(comment.getId());
        }

        likesRepository.removeLikesByFkPost(post);

        if(post.getFkPostEmployee() != null){
        String email = post.getFkPostEmployee().getEmployeeEmail();
        List<String> emails = new ArrayList<>();
        emails.add(email);
        emailService.sendEmail(emails, "Warning mail", "Do not share this type of content second time :" + post.getPostContent());

        postRepository.save(post);

        notificationService.createNotification(post.getFkPostEmployee().getId()
                ,"WARNING : post is deleted by hr"
                , " at :" + LocalDateTime.now() + " post title : "
                        + post.getPostTitle() + " post content : "
                        + post.getPostContent()
        );
        }
    }

    @Override
    public void removeComment(Long commentId, String reason){

        Comment comment = commentsRepository.findCommentById(commentId);

        if(comment.getFkPost() != null && Boolean.TRUE.equals(comment.getFkPost().getPostIsDeleted())){
            throw new RuntimeException("deleted post cannot be remove comments.");
        }

        if(comment.getParentComment()!=null && comment.getParentComment().getFkPost() != null && Boolean.TRUE.equals(comment.getParentComment().getFkPost().getPostIsDeleted())){
            throw new RuntimeException("deleted parent comment cannot be remove sub comments.");
        }

        commentsRepository.makeCommentIdDeleted(commentId);
        commentsRepository.addReasonForDeletion(commentId, reason);
        likesRepository.removeLikesForDeletedComment(commentId);
    }

    @Override
    @Transactional
    public void removePost(Long postId, String reason){
        Post post = postRepository.findPostsById(postId);

        if(Boolean.TRUE.equals(post.getPostIsDeleted())){
            throw new RuntimeException("deleted post cannot be remove again.");
        }

        post.setPostIsDeleted(true);
        post.setReasonForDeletePost(reason);
        postRepository.save(post);
        List<Comment> comments = commentsRepository.findCommentByFkPost_Id(postId);

        for(Comment comment : comments)
        {
            commentsRepository.makeCommentIdDeleted(comment.getId());
            likesRepository.removeLikesForDeletedComment(comment.getId());
        }
        likesRepository.removeLikesByFkPost(post);
    }

    @Override
    @Transactional
        public void removeLikeByCommentId(Long commentId, Long employeeId){
        Comment comment = commentsRepository.findCommentById(commentId);

        if(comment.getFkPost() != null && Boolean.TRUE.equals(comment.getFkPost().getPostIsDeleted())){
            throw new RuntimeException("deleted post cannot be remove likes.");
        }

        if(comment.getParentComment()!=null && comment.getParentComment().getFkPost() != null && Boolean.TRUE.equals(comment.getParentComment().getFkPost().getPostIsDeleted())){
            throw new RuntimeException("deleted parent comment cannot be remove sub comments likes.");
        }

        Employee employee = employeeRepository.findEmployeeById(employeeId);
        likesRepository.removeLikeByFkCommentAndFkLikeEmployee(comment,employee);
    }

    @Override
    @Transactional
    public void removeLikeByPostId(Long postId, Long employeeId){
        Post post = postRepository.findPostsById(postId);

        if(Boolean.TRUE.equals(post.getPostIsDeleted())){
            throw new RuntimeException("deleted post cannot be remove likes.");
        }

        Employee employee = employeeRepository.findEmployeeById(employeeId);
        likesRepository.removeLikeByFkPostAndFkLikeEmployee(post,employee);
    }

    @Override
    public List<TagType> getPostTagTypes(){
        return tagTypesRepository.findAll();
    }

    @Override
    public List<PostVisibility> getAllVisibilities(){
        return postVisibilityRepository.findAll();
    }

    @Override
    @Transactional
    public void removePostTagFromPost(Long postId, Long tagTypeId){
        Post post = postRepository.findPostsById(postId);

        if(Boolean.TRUE.equals(post.getPostIsDeleted())){
            throw new RuntimeException("deleted post cannot be remove tags.");
        }

        TagType tagType = tagTypesRepository.findTagTypeById(tagTypeId);
        postTagRepository.removePostTagByFkPostAndFkTagType(post, tagType);
    }
}
