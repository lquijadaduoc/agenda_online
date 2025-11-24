package com.agendaonline.dto.availability;

import java.time.OffsetDateTime;

public class AvailableSlotResponse {
    private OffsetDateTime start;
    private OffsetDateTime end;

    public AvailableSlotResponse(OffsetDateTime start, OffsetDateTime end) {
        this.start = start;
        this.end = end;
    }

    public OffsetDateTime getStart() {
        return start;
    }

    public void setStart(OffsetDateTime start) {
        this.start = start;
    }

    public OffsetDateTime getEnd() {
        return end;
    }

    public void setEnd(OffsetDateTime end) {
        this.end = end;
    }
}
