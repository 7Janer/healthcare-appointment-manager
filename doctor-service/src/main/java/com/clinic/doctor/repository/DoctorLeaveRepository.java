package com.clinic.doctor.repository;
import com.clinic.doctor.domain.DoctorLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;
public interface DoctorLeaveRepository extends JpaRepository<DoctorLeave,UUID>{
    boolean existsByDoctorIdAndLeaveDate(UUID doctorId, LocalDate date);
    List<DoctorLeave> findByDoctorIdAndLeaveDateGreaterThanEqualOrderByLeaveDate(UUID doctorId,LocalDate date);
}
