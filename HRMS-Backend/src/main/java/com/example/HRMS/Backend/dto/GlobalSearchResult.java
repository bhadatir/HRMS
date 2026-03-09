package com.example.HRMS.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSearchResult {
   private Long id;
   private String title;
   private String subtitle;
   private String type;

}

