package com.taller.appointment_service.dto;

import java.time.LocalTime;

public record MechanicDto(
        Long id,
        String fullName,
        String specialty,
        String bayCode,
        LocalTime workStartTime,
        LocalTime workEndTime,
        Boolean active) {
}