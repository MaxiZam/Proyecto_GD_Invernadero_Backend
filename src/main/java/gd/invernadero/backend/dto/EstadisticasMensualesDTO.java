package gd.invernadero.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasMensualesDTO {

    // --- Resumen de sensores
    private double temperaturaMediaMensual;
    private double temperaturaMaximaMensual;
    private double temperaturaMinimaMensual;
    private double humedadSueloMediaMensual;
    private double humedadAireMediaMensual;
    private double luminosidadMediaMensual;
    private int diasRiesgoFungico;

    // --- Uso de actuadores
    private double usoPromedioCaloventor;
    private double usoPromedioHumidificador;
    private double usoPromedioVentanales;
    private double usoPromedioBomba;
    private double usoPromedioPersianas;

    // --- Derivados
    private int registrosAnalizados;
    private double horasCubiertas;

    private double horasCaloventor;
    private double horasHumidificador;
    private double horasVentanales;
    private double horasPersianas;
    private double horasBomba;

    // --- Energía gastada
    private double kwhCaloventor;
    private double kwhHumidificador;
    private double kwhBomba;
    private double energiaEstimadaKwh;
    private double aguaEstimadaLitros;

    private double enRangoTemperatura;
    private double enRangoHumedadSuelo;
    private double enRangoHumedadAire;
}
