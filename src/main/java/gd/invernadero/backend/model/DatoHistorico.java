package gd.invernadero.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "datos_historicos")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatoHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fecha;
    private double tempPromedio;
    private double humSueloPromedio;
    private double humAirePromedio;
    private double lumPromedio;

    private double caloventorUsoPorcentaje;
    private double ventanalesUsoPorcentaje;
    private double persianasUsoPorcentaje;
    private double humidificadorUsoPorcentaje;
    private double bombaUsoPorcentaje;
}
