package com.hess.metrichive.Service;

import com.hess.metrichive.Repository.MetricRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class MetricQueryService {

    public final MetricRepository metricRepository;
}
