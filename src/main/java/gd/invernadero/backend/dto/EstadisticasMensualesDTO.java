package gd.invernadero.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasMensualesDTO {

    // --- Resumen de sensores ---
    private double temperaturaMediaMensual;
    private double temperaturaMaximaMensual;
    private double temperaturaMinimaMensual;
    private double humedadSueloMediaMensual;
    private double humedadAireMediaMensual;
    private double luminosidadMediaMensual;
    private int diasRiesgoFungico;

    // --- Uso de actuadores (% del tiempo en estado activo) ---
    private double usoPromedioCaloventor;
    private double usoPromedioHumidificador;
    private double usoPromedioVentanales;
    private double usoPromedioBomba;
    private double usoPromedioPersianas;

    // --- Derivados ---
    private int registrosAnalizados;
    private double horasCubiertas;             // tiempo real cubierto por los registros

    private double horasCaloventor;
    private double horasHumidificador;
    private double horasVentanales;            // horas con los ventanales abiertos
    private double horasPersianas;             // horas con las persianas enrolladas
    private double horasBomba;

    private double kwhCaloventor;              // estimación con potencias nominales
    private double kwhHumidificador;
    private double kwhBomba;
    private double energiaEstimadaKwh;
    private double aguaEstimadaLitros;

    private double enRangoTemperatura;         // % de registros dentro de los umbrales actuales
    private double enRangoHumedadSuelo;
    private double enRangoHumedadAire;
}
