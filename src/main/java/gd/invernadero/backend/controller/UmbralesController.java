package gd.invernadero.backend.controller;

import gd.invernadero.backend.dto.ConfiguracionUmbralesDTO;
import gd.invernadero.backend.service.GestorUmbralesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> actualizarUmbrales(@RequestBody ConfiguracionUmbralesDTO nuevosUmbrales) {
        gestorUmbrales.actualizarUmbrales(nuevosUmbrales);
        return ResponseEntity.ok("Umbrales actualizados y replicados al simulador.");
    }
}
