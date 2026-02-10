package com.hess.metrichive.Service;

import com.hess.metrichive.Repository.MetricRepository;
import com.hess.metrichive.dto.MetricDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetricIngestionService {

    private final MetricRepository metricRepository;
    public MetricIngestionService(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    public void ingestMetrics(Long tenantId, List<MetricDTO> metricDTOs) {
        log.info
    }


}