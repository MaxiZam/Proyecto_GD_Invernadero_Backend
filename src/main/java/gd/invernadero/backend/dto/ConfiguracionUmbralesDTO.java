package gd.invernadero.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionUmbralesDTO implements Serializable {
    private Double tempMin;
    private Double tempMax;
    private Double humSueloMin;
    private Double humSueloMax;
    private Double humAireMin;
    private Double humAireMax;
    private Integer luzMin;
    private Integer luzMax;
}