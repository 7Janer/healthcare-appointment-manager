package com.clinic.notification.dto;
import jakarta.validation.constraints.*;import java.time.*;import java.util.UUID;
public final class NotificationDtos{private NotificationDtos(){}
 public record EventRequest(@NotBlank String eventId,@NotBlank String type,@NotNull UUID appointmentId,@NotNull UUID patientId,@NotBlank @Email String patientEmail,@NotBlank String patientName,@NotNull UUID doctorUserId,@NotBlank @Email String doctorEmail,@NotBlank String doctorName,@NotNull OffsetDateTime startAt,@Min(10)int durationMinutes,@NotBlank String message){}
 public record MedicationPlanRequest(@NotBlank String eventId,@NotNull UUID userId,@NotBlank @Email String email,@NotNull UUID appointmentId,@NotBlank String medication,@NotBlank String instructions,@Min(1)@Max(168)int intervalHours,@NotNull Instant startAt,@NotNull Instant endAt){}
 public record CalendarUrlResponse(String authorizationUrl){}public record CalendarStatusResponse(boolean connected){}
}
