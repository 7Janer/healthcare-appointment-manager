package com.clinic.notification.repository;
import com.clinic.notification.domain.NotificationJob;import org.springframework.data.domain.Pageable;import org.springframework.data.jpa.repository.JpaRepository;import java.time.Instant;import java.util.*;
public interface JobRepository extends JpaRepository<NotificationJob,UUID>{List<NotificationJob> findByStatusInAndNextAttemptAtLessThanEqualOrderByNextAttemptAt(List<NotificationJob.Status> statuses,Instant now,Pageable page);boolean existsByDedupeKey(String key);}
