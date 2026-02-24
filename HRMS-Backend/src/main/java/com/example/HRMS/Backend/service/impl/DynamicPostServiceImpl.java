package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.GameType;
import com.example.HRMS.Backend.model.Post;
import com.example.HRMS.Backend.model.PostTag;
import com.example.HRMS.Backend.repository.*;
import com.example.HRMS.Backend.service.DynamicPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DynamicPostServiceImpl implements DynamicPostService {

    private final EmployeeRepository employeeRepository;
    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final TagTypesRepository tagTypesRepository;
    private final PostVisibilityRepository postVisibilityRepository;

    @Override
    @Scheduled(cron = "0 0 4 * * *")
    public void checkEmployeeDOBAndJoinDateAndCreatePostForBirthDaysAndWorkAnniversaries() {
        List<Employee> employees = employeeRepository.findAll();

        LocalDate localDate = LocalDate.now();

        for (Employee employee : employees) {
            if(employee.getEmployeeDob().getMonth() == localDate.getMonth() &&
                    employee.getEmployeeDob().getDayOfMonth() == localDate.getDayOfMonth()){

                Post post = new Post();

                post.setPostContentUrl("http://localhost:8080/uploads/happyBirthDay.png");
                post.setPostTitle("Happy BirthDay");
                post.setPostContent("Wish you Happy BirthDay " + employee.getEmployeeFirstName());
                post.setFkPostVisibility(postVisibilityRepository.findPostVisibilitiesById(1L));

                postRepository.save(post);

                PostTag postTagBirthday = new PostTag();
                postTagBirthday.setFkPost(post);
                postTagBirthday.setFkTagType(tagTypesRepository.findTagTypeById(4L));

                postTagRepository.save(postTagBirthday);

                PostTag postTag = new PostTag();
                postTag.setFkPost(post);
                postTag.setFkTagType(tagTypesRepository.findTagTypeById(1L));

                postTagRepository.save(postTag);
            }
            if(employee.getEmployeeHireDate().getMonth() == localDate.getMonth() &&
                    employee.getEmployeeHireDate().getDayOfMonth() == localDate.getDayOfMonth()){

                int year = localDate.getYear() - employee.getEmployeeHireDate().getYear();

                Post post = new Post();

                post.setPostContentUrl("http://localhost:8080/uploads/WorkingAnniversary.png");
                post.setPostTitle("Happy Working Anniversary");
                post.setPostContent("Happy Working Anniversary " + employee.getEmployeeFirstName() +
                        " to complete : " + year + " years at ROIMA.");
                post.setFkPostVisibility(postVisibilityRepository.findPostVisibilitiesById(1L));

                postRepository.save(post);

                PostTag postTagWorkAnniversary = new PostTag();
                postTagWorkAnniversary.setFkPost(post);
                postTagWorkAnniversary.setFkTagType(tagTypesRepository.findTagTypeById(5L));

                postTagRepository.save(postTagWorkAnniversary);

                PostTag postTag = new PostTag();
                postTag.setFkPost(post);
                postTag.setFkTagType(tagTypesRepository.findTagTypeById(1L));

                postTagRepository.save(postTag);
            }
        }
    }
}
