package gd.invernadero.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasMensualesDTO {

    private double temperaturaMediaMensual;
    private double temperaturaMaximaMensual;
    private double temperaturaMinimaMensual;

    private double humedadSueloMediaMensual;
    private double humedadAireMediaMensual;
    private int diasRiesgoFungico;

    private double luminosidadMediaMensual;

    private double usoPromedioCaloventor;
    private double usoPromedioHumidificador;
    private double usoPromedioVentanales;
    private double usoPromedioBomba;
}
