package gd.invernadero.backend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatosInvernaderoDTO {
    private Double temperatura;
    private Double humedadSuelo;
    private Double humedadAire;
    private Integer luminosidad;
    private boolean caloventorActivo;
    private boolean humidificadorActivo;
    private boolean ventanalesAbiertos;
    private boolean persianasEnrolladas;
    private boolean bombaActiva;
}