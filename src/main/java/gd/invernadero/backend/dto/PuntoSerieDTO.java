package gd.invernadero.backend.dto;

import java.time.LocalDateTime;

public record PuntoSerieDTO(
        LocalDateTime fechaHora,
        double temperatura, double humedadSuelo, double humedadAire, double luminosidad) {}