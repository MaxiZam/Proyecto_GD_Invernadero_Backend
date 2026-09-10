package gd.invernadero.backend.service;

import gd.invernadero.backend.model.DatoHistorico;
import gd.invernadero.backend.model.DatoTemporal;
import gd.invernadero.backend.repository.DatoHistoricoRepository;
import gd.invernadero.backend.repository.DatoTemporalRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistorialService {

    @Autowired
    private DatoTemporalRepository temporalRepo;

    @Autowired
    private DatoHistoricoRepository historicoRepo;

    // Guarda el dato que llega
    public void registrarDato(DatoTemporal dato) {
        dato.setTimestamp(LocalDateTime.now());
        temporalRepo.save(dato);
    }

    //@Scheduled(cron = "59 59 23 * * ?")
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void procesarCierreDiario() {
        List<DatoTemporal> datosDelDia = temporalRepo.findAll();

        // Si no hay datos, no hace nada
        if (datosDelDia.isEmpty()) {
            return;
        }

        // 1. Calcular promedios
        double tempMedia = datosDelDia.stream().mapToDouble(DatoTemporal::getTemperatura).average().orElse(0.0);
        double humSueloMedia = datosDelDia.stream().mapToDouble(DatoTemporal::getHumedadSuelo).average().orElse(0.0);
        double humAireMedia = datosDelDia.stream().mapToDouble(DatoTemporal::getHumedadAire).average().orElse(0.0);
        double lumMedia = datosDelDia.stream().mapToInt(DatoTemporal::getLuminosidad).average().orElse(0.0);

        // 2. Calcular porcentaje de uso de actuadores (% del dia encendidos)
        double total = datosDelDia.size();
        double caloventorUso = datosDelDia.stream().filter(DatoTemporal::isCaloventorActivo).count() * 100.0 / total;
        double ventanalesUso = datosDelDia.stream().filter(DatoTemporal::isVentanalesAbiertos).count() * 100.0 / total;
        double persianasUso = datosDelDia.stream().filter(DatoTemporal::isPersianasEnrolladas).count() * 100.0 / total;
        double humidificadorUso = datosDelDia.stream().filter(DatoTemporal::isHumidificadorActivo).count() * 100.0 / total;
        double bombaUso = datosDelDia.stream().filter(DatoTemporal::isBombaActiva).count() * 100.0 / total;

        // 3. Crear y guardar el registro historico
        DatoHistorico resumen = new DatoHistorico();
        resumen.setFecha(LocalDateTime.now());
        resumen.setTempPromedio(tempMedia);
        resumen.setHumSueloPromedio(humSueloMedia);
        resumen.setHumAirePromedio(humAireMedia);
        resumen.setLumPromedio(lumMedia);
        resumen.setCaloventorUsoPorcentaje(caloventorUso);
        resumen.setVentanalesUsoPorcentaje(ventanalesUso);
        resumen.setPersianasUsoPorcentaje(persianasUso);
        resumen.setHumidificadorUsoPorcentaje(humidificadorUso);
        resumen.setBombaUsoPorcentaje(bombaUso);

        historicoRepo.save(resumen);

        // 4. Limpiar la tabla temporal para el nuevo dia
        temporalRepo.deleteAll();

        // 5. Borrar datos historicos mas viejos a 30 dias
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        historicoRepo.deleteByFechaBefore(hace30Dias);

        System.out.println("Resumen diario completado exitosamente.");
    }
}
