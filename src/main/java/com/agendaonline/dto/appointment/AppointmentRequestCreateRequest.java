package com.agendaonline.dto.appointment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public class AppointmentRequestCreateRequest {

    @NotNull
    private Long serviceId;

    @NotBlank
    private String clientName;

    private String clientEmail;
    private String clientPhone;

    @NotNull
    private OffsetDateTime requestedDateTime;

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public OffsetDateTime getRequestedDateTime() {
        return requestedDateTime;
    }

    public void setRequestedDateTime(OffsetDateTime requestedDateTime) {
        this.requestedDateTime = requestedDateTime;
    }
}
