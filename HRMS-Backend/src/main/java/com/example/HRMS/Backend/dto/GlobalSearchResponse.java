package com.example.HRMS.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalSearchResponse {
    Page<GlobalSearchResult> employees;
    Page<GlobalSearchResult> travelPlans;
    Page<GlobalSearchResult> job;
    Page<GlobalSearchResult> post;
    Page<GlobalSearchResult> gameBooking;
    List<GlobalSearchResult> teamMember;
}
