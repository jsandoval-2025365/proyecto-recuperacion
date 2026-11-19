package com.taller.appointment_service.dto;

import com.taller.appointment_service.entity.Appointment;
import com.taller.appointment_service.entity.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class AppointmentResponse {

    private Long id;
    private Long mechanicId;
    private String customerName;
    private String vehiclePlate;
    private String vehicleModel;
    private String serviceType;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int durationMinutes;
    private AppointmentStatus status;
    private String observations;

    public static AppointmentResponse from(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .mechanicId(a.getMechanicId())
                .customerName(a.getCustomerName())
                .vehiclePlate(a.getVehiclePlate())
                .vehicleModel(a.getVehicleModel())
                .serviceType(a.getServiceType())
                .appointmentDate(a.getAppointmentDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .durationMinutes(a.getDurationMinutes())
                .status(a.getStatus())
                .observations(a.getObservations())
                .build();
    }
}