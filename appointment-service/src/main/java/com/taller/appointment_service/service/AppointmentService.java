package com.taller.appointment_service.service;

import com.taller.appointment_service.client.MechanicClient;
import com.taller.appointment_service.dto.AppointmentRequest;
import com.taller.appointment_service.dto.MechanicDto;
import com.taller.appointment_service.entity.Appointment;
import com.taller.appointment_service.entity.AppointmentStatus;
import com.taller.appointment_service.exception.BusinessValidationException;
import com.taller.appointment_service.exception.ResourceNotFoundException;
import com.taller.appointment_service.exception.ScheduleConflictException;
import com.taller.appointment_service.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.taller.appointment_service.entity.AppointmentStatus.*;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final Map<AppointmentStatus, Set<AppointmentStatus>> TRANSITIONS = Map.of(
            PROGRAMADA, Set.of(EN_TALLER, CANCELADA),
            EN_TALLER, Set.of(COMPLETADA, CANCELADA),
            COMPLETADA, Set.of(),
            CANCELADA, Set.of());

    private final AppointmentRepository appointmentRepository;
    private final MechanicClient mechanicClient;

    @Transactional
    public Appointment create(AppointmentRequest request) {
        validateSchedule(request, null);
        Appointment appointment = new Appointment();
        appointment.setStatus(PROGRAMADA);
        apply(appointment, request);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment update(Long id, AppointmentRequest request) {
        Appointment appointment = getById(id);
        if (appointment.getStatus() == COMPLETADA || appointment.getStatus() == CANCELADA) {
            throw new BusinessValidationException("Cannot modify an appointment in status " + appointment.getStatus());
        }
        validateSchedule(request, id);
        apply(appointment, request);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment updateStatus(Long id, AppointmentStatus newStatus) {
        Appointment appointment = getById(id);
        if (!TRANSITIONS.get(appointment.getStatus()).contains(newStatus)) {
            throw new BusinessValidationException(
                    "Invalid status transition from " + appointment.getStatus() + " to " + newStatus);
        }
        appointment.setStatus(newStatus);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment cancel(Long id) {
        return updateStatus(id, CANCELADA);
    }

    @Transactional(readOnly = true)
    public Appointment getById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<Appointment> search(LocalDate date, Long mechanicId, AppointmentStatus status) {
        Specification<Appointment> spec = (root, query, cb) -> cb.conjunction();
        if (date != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("appointmentDate"), date));
        }
        if (mechanicId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("mechanicId"), mechanicId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return appointmentRepository.findAll(spec, Sort.by("appointmentDate", "startTime"));
    }

    private void validateSchedule(AppointmentRequest request, Long excludeId) {
        MechanicDto mechanic = mechanicClient.getMechanicById(request.getMechanicId());
        if (!Boolean.TRUE.equals(mechanic.active())) {
            throw new BusinessValidationException("Mechanic " + request.getMechanicId() + " is inactive");
        }

        LocalTime start = request.getStartTime();
        LocalTime end = start.plusMinutes(request.getDurationMinutes());
        if (!end.isAfter(start) || start.isBefore(mechanic.workStartTime()) || end.isAfter(mechanic.workEndTime())) {
            throw new BusinessValidationException("Appointment is outside the mechanic's working hours ("
                    + mechanic.workStartTime() + " - " + mechanic.workEndTime() + ")");
        }

        List<Appointment> existing = appointmentRepository.findByMechanicIdAndAppointmentDateAndStatusNot(
                request.getMechanicId(), request.getAppointmentDate(), CANCELADA);
        for (Appointment other : existing) {
            if (excludeId != null && excludeId.equals(other.getId())) {
                continue;
            }
            if (start.isBefore(other.getEndTime()) && end.isAfter(other.getStartTime())) {
                throw new ScheduleConflictException("Mechanic " + request.getMechanicId()
                        + " already has an appointment overlapping " + start + " - " + end);
            }
        }
    }

    private void apply(Appointment appointment, AppointmentRequest request) {
        appointment.setMechanicId(request.getMechanicId());
        appointment.setCustomerName(request.getCustomerName());
        appointment.setVehiclePlate(request.getVehiclePlate());
        appointment.setVehicleModel(request.getVehicleModel());
        appointment.setServiceType(request.getServiceType());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setDurationMinutes(request.getDurationMinutes());
        appointment.setObservations(request.getObservations());
    }
}