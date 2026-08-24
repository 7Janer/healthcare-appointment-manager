package com.clinic.appointment.service;

import com.clinic.appointment.client.*;
import com.clinic.appointment.dto.AppointmentDtos.HoldRequest;
import com.clinic.appointment.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AppointmentServiceTest {
    @Test void rejectsSecondHoldWhenAtomicInsertLosesTheRace() {
        AppointmentRepository appointments = mock(AppointmentRepository.class);
        SlotReservationRepository slots = mock(SlotReservationRepository.class);
        DoctorClient doctors = mock(DoctorClient.class);
        UUID doctorId = UUID.randomUUID();
        OffsetDateTime at = OffsetDateTime.now().plusDays(1);
        when(doctors.availability(doctorId, at)).thenReturn(new DoctorClient.Availability(doctorId, UUID.randomUUID(), true, "AVAILABLE", "Dr Test", "doctor@example.com", 30));
        when(slots.tryInsert(any(), eq(doctorId), eq(at), any(), any(), any())).thenReturn(0);
        AppointmentService service = new AppointmentService(appointments, slots, mock(PrescriptionItemRepository.class), doctors, mock(AiClient.class), mock(OutboxPublisher.class), new ObjectMapper(), "Asia/Kolkata");

        assertThatThrownBy(() -> service.hold(UUID.randomUUID(), new HoldRequest(doctorId, at)))
                .isInstanceOf(AppointmentService.SlotConflictException.class);
    }
}
