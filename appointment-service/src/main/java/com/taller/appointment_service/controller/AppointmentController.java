package com.taller.appointment_service.controller;

import com.taller.appointment_service.dto.AppointmentRequest;
import com.taller.appointment_service.dto.AppointmentResponse;
import com.taller.appointment_service.dto.StatusChangeRequest;
import com.taller.appointment_service.entity.AppointmentStatus;
import com.taller.appointment_service.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse create(@Valid @RequestBody AppointmentRequest request) {
        return AppointmentResponse.from(service.create(request));
    }

    @GetMapping
    public List<AppointmentResponse> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long mechanicId,
            @RequestParam(required = false) AppointmentStatus status) {
        return service.search(date, mechanicId, status).stream().map(AppointmentResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AppointmentResponse findById(@PathVariable Long id) {
        return AppointmentResponse.from(service.getById(id));
    }

    @PutMapping("/{id}")
    public AppointmentResponse update(@PathVariable Long id, @Valid @RequestBody AppointmentRequest request) {
        return AppointmentResponse.from(service.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public AppointmentResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusChangeRequest request) {
        return AppointmentResponse.from(service.updateStatus(id, request.getStatus()));
    }

    @PatchMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable Long id) {
        return AppointmentResponse.from(service.cancel(id));
    }
}