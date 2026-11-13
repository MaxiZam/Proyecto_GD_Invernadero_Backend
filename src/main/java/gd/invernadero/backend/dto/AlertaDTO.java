package gd.invernadero.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertaDTO {
    private String tipo;        // "UMBRAL", "ANOMALIA", "HARDWARE"
    private String codigo;      // condición concreta (ej. "AIRE_HUMEDO");
    private String componente;  // "Sensor de Temperatura", "Bomba de Agua", etc.
    private String mensaje;
    private String severidad;   // "ADVERTENCIA", "CRITICO"
    private LocalDateTime timestamp;
}