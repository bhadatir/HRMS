package com.example.HRMS.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalSearchResponse {
    Page<GlobalSearchResult> employees;
    Page<GlobalSearchResult> travelPlans;
}
