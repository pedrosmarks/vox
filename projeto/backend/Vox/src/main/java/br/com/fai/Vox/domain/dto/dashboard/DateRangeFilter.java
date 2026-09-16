package br.com.fai.Vox.domain.dto.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Filtro de intervalo de datas aplicado às consultas do painel administrativo.
 *
 * <p>Representa a janela {@code [from, to]} sobre a coluna {@code created_at}
 * dos registros. O caso de uso típico é o administrador selecionar uma data
 * inicial (ex.: início do último mês/semana/bimestre) e buscar tudo daquela
 * data até hoje.</p>
 *
 * <ul>
 *   <li>{@code from}: início do intervalo (inclusive). {@code null} = sem limite inferior.</li>
 *   <li>{@code to}: fim do intervalo (inclusive). {@code null} = sem limite superior (até agora).</li>
 * </ul>
 *
 * As datas recebidas ({@link LocalDate}) são normalizadas para {@link LocalDateTime}:
 * {@code from} no início do dia (00:00:00) e {@code to} no fim do dia (23:59:59.999999999).
 */
public final class DateRangeFilter {

    private final LocalDateTime from;
    private final LocalDateTime to;

    private DateRangeFilter(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Cria um filtro a partir de datas (dia). {@code from} vira início do dia e
     * {@code to} vira fim do dia. Qualquer um pode ser {@code null}.
     *
     * @throws IllegalArgumentException se {@code from} for posterior a {@code to}
     */
    public static DateRangeFilter of(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final");
        }
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.atTime(LocalTime.MAX);
        return new DateRangeFilter(fromDateTime, toDateTime);
    }

    /** Filtro vazio (sem restrição de datas). */
    public static DateRangeFilter unbounded() {
        return new DateRangeFilter(null, null);
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    /** {@code true} quando há pelo menos um limite (inferior ou superior). */
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
