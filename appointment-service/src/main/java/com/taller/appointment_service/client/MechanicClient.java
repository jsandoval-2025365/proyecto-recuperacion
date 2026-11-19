package com.taller.appointment_service.client;

import com.taller.appointment_service.dto.MechanicDto;
import com.taller.appointment_service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class MechanicClient {

    private final RestClient mechanicRestClient;

    public MechanicDto getMechanicById(Long id) {
        MechanicDto mechanic = mechanicRestClient.get()
                .uri("/api/mechanics/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    throw new ResourceNotFoundException("Mechanic not found with id " + id);
                })
                .body(MechanicDto.class);
        if (mechanic == null) {
            throw new ResourceNotFoundException("Mechanic not found with id " + id);
        }
        return mechanic;
    }
}