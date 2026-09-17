package gd.invernadero.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComandoActuadorDTO implements Serializable {
    private String nombreActuador;  // caloventor, humidificador, ventanales, persianas, bomba
    private boolean modoAutomatico; // true: regla autónoma, false: control manual forzado
    private boolean estadoManual;    // true: encendido/abierto, false: apagado/cerrado
}
