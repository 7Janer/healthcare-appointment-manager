package com.clinic.doctor.controller;

import com.clinic.doctor.dto.DoctorDtos.*;
import com.clinic.doctor.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
public class DoctorController {
  private final DoctorService service; public DoctorController(DoctorService service){this.service=service;}
  @GetMapping("/api/doctors") public List<DoctorResponse> search(@RequestParam(required=false)String specialisation){return service.search(specialisation);}
  @GetMapping("/api/doctors/{id}") public DoctorResponse get(@PathVariable UUID id){return service.get(id);}
  @GetMapping("/api/doctors/me") public DoctorResponse mine(@RequestHeader("X-User-Id")UUID userId,@RequestHeader("X-User-Role")String role){doctor(role);return service.mine(userId);}
  @GetMapping("/api/doctors/{id}/availability") public AvailabilityResponse availability(@PathVariable UUID id,@RequestParam OffsetDateTime startAt){return service.availability(id,startAt);}
  @GetMapping("/api/doctors/{id}/leaves") public List<LeaveResponse> leaves(@PathVariable UUID id){return service.leaves(id);}
  @PostMapping("/api/admin/doctors") @ResponseStatus(HttpStatus.CREATED) public DoctorResponse create(@Valid @RequestBody DoctorRequest r){return service.create(r);}
  @PutMapping("/api/admin/doctors/{id}") public DoctorResponse update(@PathVariable UUID id,@Valid @RequestBody DoctorUpdateRequest r){return service.update(id,r);}
  @PostMapping("/api/admin/doctors/{id}/leaves") @ResponseStatus(HttpStatus.CREATED) public LeaveResponse addLeave(@PathVariable UUID id,@Valid @RequestBody LeaveRequest r){return service.addLeave(id,r);}
  @DeleteMapping("/api/admin/doctors/{id}/leaves/{leaveId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void removeLeave(@PathVariable UUID id,@PathVariable UUID leaveId){service.removeLeave(id,leaveId);}
  private void doctor(String role){if(!"DOCTOR".equals(role)&&!"ADMIN".equals(role))throw new SecurityException("Doctor access required");}
}
