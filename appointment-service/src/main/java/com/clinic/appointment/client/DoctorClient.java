package com.clinic.appointment.client;
import org.springframework.beans.factory.annotation.Value;import org.springframework.stereotype.Component;import org.springframework.web.client.RestClient;import java.time.*;import java.util.UUID;
@Component public class DoctorClient{private final RestClient client;public DoctorClient(RestClient.Builder b,@Value("${clients.doctor-service-url}")String url){client=b.baseUrl(url).build();}
 public Availability availability(UUID doctor,OffsetDateTime at){return client.get().uri(u->u.path("/api/doctors/{id}/availability").queryParam("startAt",at).build(doctor)).retrieve().body(Availability.class);}
 public DoctorProfile doctor(UUID id){return client.get().uri("/api/doctors/{id}",id).retrieve().body(DoctorProfile.class);}
 public record Availability(UUID doctorId,UUID doctorUserId,boolean available,String reason,String doctorName,String doctorEmail,int slotDurationMinutes){}
 public record DoctorProfile(UUID id,UUID userId,String fullName,String email,String specialisation,String qualifications,LocalTime workingStart,LocalTime workingEnd,int slotDurationMinutes,boolean active){}
}
