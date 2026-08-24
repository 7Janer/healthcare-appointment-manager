package com.clinic.appointment.dto;
import com.clinic.appointment.domain.Appointment;import jakarta.validation.Valid;import jakarta.validation.constraints.*;import java.time.*;import java.util.*;
public final class AppointmentDtos{private AppointmentDtos(){}
 public record HoldRequest(@NotNull UUID doctorId,@NotNull @Future OffsetDateTime startAt){}public record HoldResponse(UUID holdToken,Instant expiresAt){}
 public record BookingRequest(@NotNull UUID holdToken,@NotBlank @Size(max=100)String patientName,@NotBlank @Size(min=10,max=5000)String symptoms){}
 public record RescheduleRequest(@NotNull UUID holdToken){}
 public record PrescriptionRequest(@NotBlank String medication,@NotBlank @Size(max=1000)String instructions,@Min(1)@Max(168)int intervalHours,@Min(1)@Max(365)int durationDays){}
 public record PostVisitRequest(@NotBlank @Size(max=10000)String clinicalNotes,@NotEmpty List<@Valid PrescriptionRequest> prescriptions){}
 public record AppointmentResponse(UUID id,UUID patientId,String patientName,String patientEmail,UUID doctorId,UUID doctorUserId,String doctorName,String doctorEmail,OffsetDateTime startAt,int durationMinutes,Appointment.Status status,String symptoms,String preVisitSummary,String urgencyLevel,String clinicalNotes,String prescriptionText,String postVisitSummary,String cancellationReason){}
 public record SlotResponse(OffsetDateTime startAt,boolean available){}
 public record DoctorLeaveRequest(@NotNull UUID doctorId,@NotNull LocalDate date,@NotBlank String doctorName,@NotBlank String reason){}
}
