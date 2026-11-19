package com.taller.appointment_service.service;

import com.taller.appointment_service.dto.AppointmentRequest;
import com.taller.appointment_service.dto.AppointmentResponse;
import com.taller.appointment_service.entity.Appointment;
import com.taller.appointment_service.entity.AppointmentStatus;
import com.taller.appointment_service.exception.ApiException;
import com.taller.appointment_service.repository.AppointmentRepository;
import com.taller.appointment_service.validator.MechanicAvailabilityValidator;
import com.taller.appointment_service.validator.OverlapValidator;
import com.taller.appointment_service.validator.StatusTransitionValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 12, 1);
    private static final LocalTime START = LocalTime.of(9, 0);

    @Mock
    private AppointmentRepository repository;

    @Mock
    private MechanicAvailabilityValidator availabilityValidator;

    @Mock
    private OverlapValidator overlapValidator;

    @Spy
    private StatusTransitionValidator transitionValidator;

    @InjectMocks
    private AppointmentService service;

    private AppointmentRequest request() {
        AppointmentRequest r = new AppointmentRequest();
        r.setMechanicId(1L);
        r.setCustomerName("Maria Lopez");
        r.setVehiclePlate("P123ABC");
        r.setVehicleModel("Toyota Hilux");
        r.setServiceType("Oil change");
        r.setAppointmentDate(DATE);
        r.setStartTime(START);
        r.setDurationMinutes(90);
        return r;
    }

    private Appointment appointment(AppointmentStatus status) {
        return Appointment.builder()
                .id(1L)
                .mechanicId(1L)
                .customerName("Maria Lopez")
                .vehiclePlate("P123ABC")
                .vehicleModel("Toyota Hilux")
                .serviceType("Oil change")
                .appointmentDate(DATE)
                .startTime(START)
                .endTime(LocalTime.of(10, 30))
                .durationMinutes(90)
                .status(status)
                .build();
    }

    @Test
    void create_validRequest_calculatesEndAndSetsProgramada() {
        when(repository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = service.create(request());

        assertEquals(LocalTime.of(10, 30), response.getEndTime());
        assertEquals(AppointmentStatus.PROGRAMADA, response.getStatus());
        verify(availabilityValidator).validate(1L, DATE, START, 90);
        verify(overlapValidator).validate(1L, DATE, START, LocalTime.of(10, 30), null);
    }

    @Test
    void create_overlap_throwsConflictAndDoesNotSave() {
        doThrow(new ApiException(HttpStatus.CONFLICT, "overlap"))
                .when(overlapValidator).validate(any(), any(), any(), any(), any());

        ApiException ex = assertThrows(ApiException.class, () -> service.create(request()));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(repository, never()).save(any());
    }

    @Test
    void create_mechanicRejected_doesNotCheckOverlapNorSave() {
        doThrow(new ApiException(HttpStatus.CONFLICT, "inactive"))
                .when(availabilityValidator).validate(any(), any(), any(), anyInt());

        assertThrows(ApiException.class, () -> service.create(request()));

        verifyNoInteractions(overlapValidator);
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({
            "PROGRAMADA,EN_TALLER",
            "PROGRAMADA,CANCELADA",
            "EN_TALLER,COMPLETADA",
            "EN_TALLER,CANCELADA"
    })
    void changeStatus_validTransitions_updatesStatus(AppointmentStatus from, AppointmentStatus to) {
        when(repository.findById(1L)).thenReturn(Optional.of(appointment(from)));
        when(repository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(to, service.changeStatus(1L, to).getStatus());
    }

    @ParameterizedTest
    @CsvSource({
            "PROGRAMADA,COMPLETADA",
            "EN_TALLER,PROGRAMADA",
            "COMPLETADA,EN_TALLER",
            "COMPLETADA,CANCELADA",
            "CANCELADA,PROGRAMADA",
            "CANCELADA,EN_TALLER"
    })
    void changeStatus_invalidTransitions_throwsConflict(AppointmentStatus from, AppointmentStatus to) {
        when(repository.findById(1L)).thenReturn(Optional.of(appointment(from)));

        ApiException ex = assertThrows(ApiException.class, () -> service.changeStatus(1L, to));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(repository, never()).save(any());
    }

    @Test
    void cancel_programada_setsCancelada() {
        when(repository.findById(1L)).thenReturn(Optional.of(appointment(AppointmentStatus.PROGRAMADA)));
        when(repository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(AppointmentStatus.CANCELADA, service.cancel(1L).getStatus());
    }

    @Test
    void update_cancelledAppointment_throwsConflictWithoutValidating() {
        when(repository.findById(1L)).thenReturn(Optional.of(appointment(AppointmentStatus.CANCELADA)));

        ApiException ex = assertThrows(ApiException.class, () -> service.update(1L, request()));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verifyNoInteractions(availabilityValidator, overlapValidator);
    }

    @Test
    void findById_notFound_throwsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.findById(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }
}