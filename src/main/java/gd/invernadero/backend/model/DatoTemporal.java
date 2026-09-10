package gd.invernadero.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "datos_temporales")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatoTemporal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private double temperatura;
    private double humedadSuelo;
    private double humedadAire;
    private int luminosidad;
    private boolean caloventorActivo;
    private boolean ventanalesAbiertos;
    private boolean persianasEnrolladas;
    private boolean humidificadorActivo;
    private boolean bombaActiva;
}