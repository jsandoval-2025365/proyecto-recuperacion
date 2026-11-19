package com.taller.appointment_service.service;

import com.taller.appointment_service.client.MechanicAvailabilityResponse;
import com.taller.appointment_service.client.MechanicClient;
import com.taller.appointment_service.dto.AppointmentRequest;
import com.taller.appointment_service.dto.AppointmentResponse;
import com.taller.appointment_service.entity.Appointment;
import com.taller.appointment_service.entity.AppointmentStatus;
import com.taller.appointment_service.exception.ApiException;
import com.taller.appointment_service.repository.AppointmentRepository;
import com.taller.appointment_service.validator.MechanicAvailabilityValidator;
import com.taller.appointment_service.validator.OverlapValidator;
import com.taller.appointment_service.validator.StatusTransitionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentCancellationTest {

    private static final LocalDate DATE = LocalDate.of(2026, 12, 1);
    private static final LocalTime START = LocalTime.of(9, 0);
    private static final LocalTime END = LocalTime.of(10, 0);

    @Mock
    private AppointmentRepository repository;

    @Mock
    private MechanicClient mechanicClient;

    private AppointmentService service;

    @BeforeEach
    void setUp() {
        service = new AppointmentService(
                repository,
                new MechanicAvailabilityValidator(mechanicClient),
                new OverlapValidator(repository),
                new StatusTransitionValidator());
    }

    @Test
    void cancel_releasesSlot_allowsNewAppointmentInSameRange() {
        MechanicAvailabilityResponse availability = new MechanicAvailabilityResponse();
        availability.setMechanicId(1L);
        availability.setActive(true);
        availability.setWithinSchedule(true);
        availability.setAvailable(true);

        Appointment existing = Appointment.builder()
                .id(1L)
                .mechanicId(1L)
                .appointmentDate(DATE)
                .startTime(START)
                .endTime(END)
                .durationMinutes(60)
                .status(AppointmentStatus.PROGRAMADA)
                .build();

        AppointmentRequest request = new AppointmentRequest();
        request.setMechanicId(1L);
        request.setCustomerName("Carlos Ruiz");
        request.setVehiclePlate("P456DEF");
        request.setVehicleModel("Honda Civic");
        request.setServiceType("Brakes");
        request.setAppointmentDate(DATE);
        request.setStartTime(START);
        request.setDurationMinutes(60);

        when(mechanicClient.checkAvailability(1L, DATE, START, 60)).thenReturn(availability);
        when(repository.existsOverlap(1L, DATE, START, END, AppointmentStatus.CANCELADA))
                .thenReturn(true, false);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        ApiException ex = assertThrows(ApiException.class, () -> service.create(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());

        assertEquals(AppointmentStatus.CANCELADA, service.cancel(1L).getStatus());

        AppointmentResponse created = service.create(request);
        assertEquals(AppointmentStatus.PROGRAMADA, created.getStatus());
        assertEquals(END, created.getEndTime());
    }
}