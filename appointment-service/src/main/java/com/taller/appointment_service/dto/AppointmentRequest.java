package com.taller.appointment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {

    @NotNull
    private Long mechanicId;

    @NotBlank
    @Size(max = 120)
    private String customerName;

    @NotBlank
    @Size(max = 20)
    private String vehiclePlate;

    @NotBlank
    @Size(max = 80)
    private String vehicleModel;

    @NotBlank
    @Size(max = 80)
    private String serviceType;

    @NotNull
    private LocalDate appointmentDate;

    @NotNull
    private LocalTime startTime;

    @NotNull
    @Positive
    private Integer durationMinutes;

    @Size(max = 500)
    private String observations;
}