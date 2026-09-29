package br.com.fai.Vox.domain.dto.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class DateRangeFilter {

    private final LocalDateTime from;
    private final LocalDateTime to;

    private DateRangeFilter(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }

    public static DateRangeFilter of(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final");
        }
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.atTime(LocalTime.MAX);
        return new DateRangeFilter(fromDateTime, toDateTime);
    }

    public static DateRangeFilter unbounded() {
        return new DateRangeFilter(null, null);
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public boolean hasAnyBound() {
        return from != null || to != null;
    }

    public boolean hasFrom() {
        return from != null;
    }

    public boolean hasTo() {
        return to != null;
    }
}
