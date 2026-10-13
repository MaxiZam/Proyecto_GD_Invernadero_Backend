package gd.invernadero.backend.controller;

import gd.invernadero.backend.dto.ConfiguracionUmbralesDTO;
import gd.invernadero.backend.service.GestorUmbralesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/umbrales")
@RequiredArgsConstructor
public class UmbralesController {

    private final GestorUmbralesService gestorUmbrales;

    @GetMapping
    public ResponseEntity<ConfiguracionUmbralesDTO> consultarUmbrales() {
        return ResponseEntity.ok(gestorUmbrales.obtenerUmbralesActivos());
    }

    @PutMapping
    public ResponseEntity<String> actualizar(@RequestBody ConfiguracionUmbralesDTO u) {
        if (Stream.of(u.getTempMin(), u.getTempMax(), u.getHumSueloMin(), u.getHumSueloMax(),
                u.getHumAireMin(), u.getHumAireMax(), u.getLuzMin(), u.getLuzMax()).anyMatch(Objects::isNull))
            return ResponseEntity.badRequest().body("Todos los umbrales son obligatorios.");
        if (u.getTempMin() >= u.getTempMax() || u.getHumSueloMin() >= u.getHumSueloMax()
                || u.getHumAireMin() >= u.getHumAireMax() || u.getLuzMin() >= u.getLuzMax())
            return ResponseEntity.badRequest().body("Cada mínimo debe ser menor que su máximo.");
        gestorUmbrales.actualizarUmbrales(u);
        return ResponseEntity.ok("Umbrales actualizados y replicados al simulador.");
    }
}
