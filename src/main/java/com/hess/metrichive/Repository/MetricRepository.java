package com.hess.metrichive.Repository;

import com.hess.metrichive.Model.Metric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricRepository extends JpaRepository<Metric, String> {

}
