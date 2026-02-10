package com.hess.metrichive.Service;

import com.hess.metrichive.Repository.MetricRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricIngestionService {

    private final MetricRepository metricRepository;

}