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

    // --- Promedios de Sensores ---
    @Column(nullable = false)
    private Double temperaturaMedia;

    @Column(nullable = false)
    private Double humedadSueloMedia;

    @Column(nullable = false)
    private Double humedadAireMedia;

    @Column(nullable = false)
    private Double luminosidadMedia;

    // --- Valores Mínimos y Máximos del Intervalo ---
    @Column(nullable = false)
    private Double temperaturaMin;
    @Column(nullable = false)
    private Double temperaturaMax;

    @Column(nullable = false)
    private Double humedadSueloMin;
    @Column(nullable = false)
    private Double humedadSueloMax;

    @Column(nullable = false)
    private Double humedadAireMin;
    @Column(nullable = false)
    private Double humedadAireMax;

    @Column(nullable = false)
    private Integer luminosidadMin;
    @Column(nullable = false)
    private Integer luminosidadMax;

    // --- Porcentajes de Activación de Actuadores (0.0% a 100.0%) ---
    @Column(nullable = false)
    private Double porcentajeCaloventor;

    @Column(nullable = false)
    private Double porcentajeHumidificador;

    @Column(nullable = false)
    private Double porcentajeVentanales;

    @Column(nullable = false)
    private Double porcentajePersianas;

    @Column(nullable = false)
    private Double porcentajeBomba;
}