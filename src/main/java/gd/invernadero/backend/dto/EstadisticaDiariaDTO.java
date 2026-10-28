package gd.invernadero.backend.dto;

import java.time.LocalDate;

public record EstadisticaDiariaDTO(
        LocalDate fecha,
        double temperaturaMedia, double temperaturaMin, double temperaturaMax,
        double humedadSueloMedia, double humedadAireMedia, double luminosidadMedia,
        double usoCaloventor, double usoHumidificador, double usoVentanales,
        double usoPersianas, double usoBomba,
        int registros) {}
