package com.taller.appointment_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments", indexes = @Index(name = "idx_mechanic_date", columnList = "mechanic_id, appointment_date"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long mechanicId;

    @Column(nullable = false, length = 120)
    private String customerName;

    @Column(nullable = false, length = 20)
    private String vehiclePlate;

    @Column(nullable = false, length = 80)
    private String vehicleModel;

    @Column(nullable = false, length = 80)
    private String serviceType;

    @Column(nullable = false)
    private LocalDate appointmentDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(length = 500)
    private String observations;

    public LocalTime getEndTime() {
        return startTime.plusMinutes(durationMinutes);
    }
}