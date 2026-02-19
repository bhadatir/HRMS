package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Post;
import com.example.HRMS.Backend.model.PostTag;
import com.example.HRMS.Backend.model.TagType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag,Long> {
    List<PostTag> findPostTagsByFkPost_Id(Long fkPostId);

    void removePostTagByFkPostAndFkTagType(Post fkPost, TagType fkTagType);
}
