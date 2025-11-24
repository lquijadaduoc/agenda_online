package com.agendaonline.dto.appointment;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public class AppointmentCreateRequest {

    @NotNull
    private Long serviceId;

    private Long clientId;

    @NotNull
    private OffsetDateTime startDateTime;

    @NotNull
    private OffsetDateTime endDateTime;

    private String notes;

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
