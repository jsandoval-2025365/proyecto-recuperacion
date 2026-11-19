package com.taller.appointment_service.dto;

import com.taller.appointment_service.entity.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusChangeRequest {

    @NotNull
    private AppointmentStatus status;
}