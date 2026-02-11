package com.hess.metrichive.Config;

import com.hess.metrichive.Model.Metric;
import com.hess.metrichive.Model.Tenant;
import com.hess.metrichive.Repository.MetricRepository;
import com.hess.metrichive.Repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Profile("dev")  // Only runs in dev profile
@RequiredArgsConstructor
@Slf4j
public class DevelopmentDataLoader implements CommandLineRunner {

    public final MetricRepository metricRepository;
    public final TenantRepository tenantRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Data Loader running");

        if (tenantRepository.count() == 0) {

        }




    }
}