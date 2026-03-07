package com.hess.metrichive.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryResult {
    private Map<String, String> dimensions; // group_by fields
    private List<IntervalData> intervals;
    private Double total;
    private Double average;
    private Long count;
}
