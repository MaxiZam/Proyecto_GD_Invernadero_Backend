package gd.invernadero.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "dato_historico", indexes = {
        @Index(name = "idx_historico_fechahora", columnList = "fechaHora")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatoHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime fechaHora;

    // Promedios
    private Double temperatura;
    private Double humedadSuelo;
    private Double humedadAire;
    private Integer luminosidad;

    // Porcentajes de activación (0.0% a 100.0%)
    private Double porcentajeCaloventor;
    private Double porcentajeHumidificador;
    private Double porcentajeVentanales;
    private Double porcentajePersianas;
    private Double porcentajeBomba;
}
