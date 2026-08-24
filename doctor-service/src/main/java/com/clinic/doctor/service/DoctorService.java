package com.clinic.doctor.service;

import com.clinic.doctor.domain.*;
import com.clinic.doctor.dto.DoctorDtos.*;
import com.clinic.doctor.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import java.time.*;
import java.util.*;

@Service
public class DoctorService {
  private final DoctorProfileRepository doctors; private final DoctorLeaveRepository leaves; private final RestClient appointmentClient; private final String internalKey;
  public DoctorService(DoctorProfileRepository doctors,DoctorLeaveRepository leaves,RestClient.Builder builder,
      @Value("${clients.appointment-service-url}") String appointmentUrl,@Value("${app.internal-api-key}") String internalKey){
    this.doctors=doctors;this.leaves=leaves;this.appointmentClient=builder.baseUrl(appointmentUrl).build();this.internalKey=internalKey;
  }
  public List<DoctorResponse> search(String specialisation){return doctors.findByActiveTrueAndSpecialisationContainingIgnoreCaseOrderByFullName(specialisation==null?"":specialisation).stream().map(this::view).toList();}
  public DoctorResponse get(UUID id){return view(entity(id));}
  public DoctorResponse mine(UUID userId){return doctors.findByUserId(userId).map(this::view).orElseThrow(()->new NoSuchElementException("Doctor profile not found"));}
  @Transactional public DoctorResponse create(DoctorRequest r){validateHours(r.workingStart(),r.workingEnd());return view(doctors.save(new DoctorProfile(r.userId(),r.fullName(),r.email(),r.specialisation(),r.qualifications(),r.workingStart(),r.workingEnd(),r.slotDurationMinutes())));}
  @Transactional public DoctorResponse update(UUID id,DoctorUpdateRequest r){validateHours(r.workingStart(),r.workingEnd());DoctorProfile d=entity(id);d.update(r.fullName(),r.email(),r.specialisation(),r.qualifications(),r.workingStart(),r.workingEnd(),r.slotDurationMinutes(),r.active());return view(d);}
  @Transactional public LeaveResponse addLeave(UUID doctorId,LeaveRequest r){
    DoctorProfile doctor=entity(doctorId); if(leaves.existsByDoctorIdAndLeaveDate(doctorId,r.date()))throw new IllegalArgumentException("Leave already exists for this date");
    DoctorLeave leave=leaves.save(new DoctorLeave(doctorId,r.date(),r.reason()));
    appointmentClient.post().uri("/internal/appointments/doctor-leave").header("X-Internal-Key",internalKey).contentType(MediaType.APPLICATION_JSON)
      .body(Map.of("doctorId",doctorId,"date",r.date(),"doctorName",doctor.getFullName(),"reason",r.reason()==null?"Doctor unavailable":r.reason())).retrieve().toBodilessEntity();
    return new LeaveResponse(leave.getId(),doctorId,r.date(),r.reason());
  }
  public List<LeaveResponse> leaves(UUID doctorId){return leaves.findByDoctorIdAndLeaveDateGreaterThanEqualOrderByLeaveDate(doctorId,LocalDate.now()).stream().map(l->new LeaveResponse(l.getId(),l.getDoctorId(),l.getLeaveDate(),l.getReason())).toList();}
  @Transactional public void removeLeave(UUID doctorId,UUID leaveId){DoctorLeave l=leaves.findById(leaveId).orElseThrow();if(!l.getDoctorId().equals(doctorId))throw new IllegalArgumentException("Leave does not belong to doctor");leaves.delete(l);}
  public AvailabilityResponse availability(UUID doctorId,OffsetDateTime startAt){
    DoctorProfile d=entity(doctorId); LocalTime time=startAt.toLocalTime(); boolean leave=leaves.existsByDoctorIdAndLeaveDate(doctorId,startAt.toLocalDate());
    boolean aligned=!time.isBefore(d.getWorkingStart()) && !time.plusMinutes(d.getSlotDurationMinutes()).isAfter(d.getWorkingEnd()) && Duration.between(d.getWorkingStart(),time).toMinutes()%d.getSlotDurationMinutes()==0;
    boolean available=d.isActive()&&!leave&&aligned&&startAt.isAfter(OffsetDateTime.now()); String reason=available?"AVAILABLE":(!d.isActive()?"INACTIVE":leave?"ON_LEAVE":!aligned?"OUTSIDE_WORKING_HOURS":"IN_THE_PAST");
    return new AvailabilityResponse(doctorId,d.getUserId(),available,reason,d.getFullName(),d.getEmail(),d.getSlotDurationMinutes());
  }
  private DoctorProfile entity(UUID id){return doctors.findById(id).orElseThrow(()->new NoSuchElementException("Doctor not found"));}
  private DoctorResponse view(DoctorProfile d){return new DoctorResponse(d.getId(),d.getUserId(),d.getFullName(),d.getEmail(),d.getSpecialisation(),d.getQualifications(),d.getWorkingStart(),d.getWorkingEnd(),d.getSlotDurationMinutes(),d.isActive());}
  private void validateHours(LocalTime start,LocalTime end){if(!start.isBefore(end))throw new IllegalArgumentException("workingStart must be before workingEnd");}
}
