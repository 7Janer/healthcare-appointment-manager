package com.clinic.doctor.repository;
import com.clinic.doctor.domain.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile,UUID>{
    List<DoctorProfile> findByActiveTrueAndSpecialisationContainingIgnoreCaseOrderByFullName(String specialisation);
    Optional<DoctorProfile> findByUserId(UUID userId);
}
