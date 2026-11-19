package com.taller.appointment_service.repository;

import com.taller.appointment_service.entity.Appointment;
import com.taller.appointment_service.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    List<Appointment> findByMechanicIdAndAppointmentDateAndStatusNot(
            Long mechanicId, LocalDate appointmentDate, AppointmentStatus status);
}