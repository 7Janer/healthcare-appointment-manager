package com.clinic.doctor.dto;

import jakarta.validation.constraints.*;
import java.time.*;
import java.util.UUID;

public final class DoctorDtos {
  private DoctorDtos(){}
  public record DoctorRequest(@NotNull UUID userId,@NotBlank String fullName,@NotBlank @Email String email,@NotBlank String specialisation,String qualifications,
                              @NotNull LocalTime workingStart,@NotNull LocalTime workingEnd,@Min(10) @Max(180) int slotDurationMinutes){}
  public record DoctorUpdateRequest(@NotBlank String fullName,@NotBlank @Email String email,@NotBlank String specialisation,String qualifications,
                                    @NotNull LocalTime workingStart,@NotNull LocalTime workingEnd,@Min(10) @Max(180) int slotDurationMinutes,boolean active){}
  public record DoctorResponse(UUID id,UUID userId,String fullName,String email,String specialisation,String qualifications,LocalTime workingStart,LocalTime workingEnd,int slotDurationMinutes,boolean active){}
  public record LeaveRequest(@NotNull @FutureOrPresent LocalDate date,@Size(max=255) String reason){}
  public record LeaveResponse(UUID id,UUID doctorId,LocalDate date,String reason){}
  public record AvailabilityResponse(UUID doctorId,UUID doctorUserId,boolean available,String reason,String doctorName,String doctorEmail,int slotDurationMinutes){}
}
