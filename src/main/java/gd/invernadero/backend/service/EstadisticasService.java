package gd.invernadero.backend.service;

import gd.invernadero.backend.dto.ConfiguracionUmbralesDTO;
import gd.invernadero.backend.dto.EstadisticaDiariaDTO;
import gd.invernadero.backend.dto.EstadisticasMensualesDTO;
import gd.invernadero.backend.dto.PuntoSerieDTO;
import gd.invernadero.backend.model.DatoHistorico;
import gd.invernadero.backend.repository.DatoHistoricoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstadisticasService {

    private static final double UMBRAL_RIESGO_FUNGICO = 80.0;
    private static final int DIAS_VENTANA = 30;

    private final DatoHistoricoRepository repo;
    private final GestorUmbralesService gestorUmbrales;

    @Value("${invernadero.historico.intervalo-ms:60000}")
    private long intervaloMs;
    @Value("${invernadero.consumo.caloventor-kw:2.0}")
    private double kwCaloventor;
    @Value("${invernadero.consumo.humidificador-kw:0.3}")
    private double kwHumidificador;
    @Value("${invernadero.consumo.bomba-kw:0.1}")
    private double kwBomba;
    @Value("${invernadero.consumo.bomba-litros-hora:60}")
    private double litrosHoraBomba;

    // ------------------------------------------------------------------ resumen del período
    @Transactional(readOnly = true)
    public EstadisticasMensualesDTO ultimos30Dias() {
        List<DatoHistorico> d = ultimos();
        EstadisticasMensualesDTO dto = new EstadisticasMensualesDTO();
        if (d.isEmpty()) return dto;

        dto.setTemperaturaMediaMensual(avg(d, DatoHistorico::getTemperaturaMedia));
        dto.setTemperaturaMaximaMensual(d.stream().mapToDouble(DatoHistorico::getTemperaturaMax).max().orElse(0));
        dto.setTemperaturaMinimaMensual(d.stream().mapToDouble(DatoHistorico::getTemperaturaMin).min().orElse(0));
        dto.setHumedadSueloMediaMensual(avg(d, DatoHistorico::getHumedadSueloMedia));
        dto.setHumedadAireMediaMensual(avg(d, DatoHistorico::getHumedadAireMedia));
        dto.setLuminosidadMediaMensual(avg(d, DatoHistorico::getLuminosidadMedia));
        dto.setUsoPromedioCaloventor(avg(d, DatoHistorico::getPorcentajeCaloventor));
        dto.setUsoPromedioHumidificador(avg(d, DatoHistorico::getPorcentajeHumidificador));
        dto.setUsoPromedioVentanales(avg(d, DatoHistorico::getPorcentajeVentanales));
        dto.setUsoPromedioPersianas(avg(d, DatoHistorico::getPorcentajePersianas));
        dto.setUsoPromedioBomba(avg(d, DatoHistorico::getPorcentajeBomba));

        // Día con riesgo fúngico = humedad media del aire de ese día >= umbral
        long dias = agruparPorDia(d).values().stream()
                .filter(l -> media(l, DatoHistorico::getHumedadAireMedia) >= UMBRAL_RIESGO_FUNGICO)
                .count();
        dto.setDiasRiesgoFungico((int) dias);

        // Horas de operación: cada registro cubre un intervalo de tiempo real
        dto.setRegistrosAnalizados(d.size());
        dto.setHorasCubiertas(redondear(d.size() * intervaloMs / 3_600_000.0));
        double hCal = horasActivas(d, DatoHistorico::getPorcentajeCaloventor);
        double hHum = horasActivas(d, DatoHistorico::getPorcentajeHumidificador);
        double hVen = horasActivas(d, DatoHistorico::getPorcentajeVentanales);
        double hPer = horasActivas(d, DatoHistorico::getPorcentajePersianas);
        double hBom = horasActivas(d, DatoHistorico::getPorcentajeBomba);
        dto.setHorasCaloventor(redondear(hCal));
        dto.setHorasHumidificador(redondear(hHum));
        dto.setHorasVentanales(redondear(hVen));
        dto.setHorasPersianas(redondear(hPer));
        dto.setHorasBomba(redondear(hBom));

        // Consumo estimado: solo caloventor, humidificador y bomba (los motores de
        // ventanales y persianas consumen al moverse, no mientras permanecen en un estado)
        double kCal = hCal * kwCaloventor, kHum = hHum * kwHumidificador, kBom = hBom * kwBomba;
        dto.setKwhCaloventor(redondear(kCal));
        dto.setKwhHumidificador(redondear(kHum));
        dto.setKwhBomba(redondear(kBom));
        dto.setEnergiaEstimadaKwh(redondear(kCal + kHum + kBom));
        dto.setAguaEstimadaLitros(redondear(hBom * litrosHoraBomba));

        // % de registros dentro de los umbrales vigentes
        ConfiguracionUmbralesDTO u = gestorUmbrales.obtenerUmbralesActivos();
        dto.setEnRangoTemperatura(pctEnRango(d, DatoHistorico::getTemperaturaMedia, u.getTempMin(), u.getTempMax()));
        dto.setEnRangoHumedadSuelo(pctEnRango(d, DatoHistorico::getHumedadSueloMedia, u.getHumSueloMin(), u.getHumSueloMax()));
        dto.setEnRangoHumedadAire(pctEnRango(d, DatoHistorico::getHumedadAireMedia, u.getHumAireMin(), u.getHumAireMax()));
        return dto;
    }

    // ------------------------------------------------------------------ agregado diario
    @Transactional(readOnly = true)
    public List<EstadisticaDiariaDTO> diarias() {
        return agruparPorDia(ultimos()).entrySet().stream().map(e -> {
            List<DatoHistorico> l = e.getValue();
            return new EstadisticaDiariaDTO(
                    e.getKey(),
                    avg(l, DatoHistorico::getTemperaturaMedia),
                    l.stream().mapToDouble(DatoHistorico::getTemperaturaMin).min().orElse(0),
                    l.stream().mapToDouble(DatoHistorico::getTemperaturaMax).max().orElse(0),
                    avg(l, DatoHistorico::getHumedadSueloMedia),
                    avg(l, DatoHistorico::getHumedadAireMedia),
                    avg(l, DatoHistorico::getLuminosidadMedia),
                    avg(l, DatoHistorico::getPorcentajeCaloventor),
                    avg(l, DatoHistorico::getPorcentajeHumidificador),
                    avg(l, DatoHistorico::getPorcentajeVentanales),
                    avg(l, DatoHistorico::getPorcentajePersianas),
                    avg(l, DatoHistorico::getPorcentajeBomba),
                    l.size());
        }).toList();
    }

    // ------------------------------------------------------------------ serie reducida
    /** Promedia por tramos para devolver como máximo ~{@code puntos} puntos (en vez de ~43.000 filas). */
    @Transactional(readOnly = true)
    public List<PuntoSerieDTO> serie(int puntos) {
        List<DatoHistorico> d = ultimos();
        if (d.isEmpty()) return List.of();
        int tam = Math.max(1, (int) Math.ceil(d.size() / (double) Math.max(1, puntos)));
        List<PuntoSerieDTO> salida = new ArrayList<>();
        for (int i = 0; i < d.size(); i += tam) {
            List<DatoHistorico> g = d.subList(i, Math.min(d.size(), i + tam));
            salida.add(new PuntoSerieDTO(
                    g.get(0).getFechaHora().truncatedTo(ChronoUnit.SECONDS),
                    avg(g, DatoHistorico::getTemperaturaMedia),
                    avg(g, DatoHistorico::getHumedadSueloMedia),
                    avg(g, DatoHistorico::getHumedadAireMedia),
                    avg(g, DatoHistorico::getLuminosidadMedia)));
        }
        return salida;
    }

    // ------------------------------------------------------------------ utilidades
    private List<DatoHistorico> ultimos() {
        return repo.findByFechaHoraAfterOrderByFechaHoraAsc(LocalDateTime.now().minusDays(DIAS_VENTANA));
    }

    private Map<LocalDate, List<DatoHistorico>> agruparPorDia(List<DatoHistorico> d) {
        return d.stream().collect(Collectors.groupingBy(
                x -> x.getFechaHora().toLocalDate(), TreeMap::new, Collectors.toList()));
    }

    /** Horas en que el actuador estuvo activo = suma(%/100) * duración de cada registro. */
    private double horasActivas(List<DatoHistorico> l, ToDoubleFunction<DatoHistorico> pct) {
        return l.stream().mapToDouble(pct).sum() / 100.0 * intervaloMs / 3_600_000.0;
    }

    private double pctEnRango(List<DatoHistorico> l, ToDoubleFunction<DatoHistorico> f, double min, double max) {
        long ok = l.stream().mapToDouble(f).filter(v -> v >= min && v <= max).count();
        return redondear(ok * 100.0 / l.size());
    }

    private double media(List<DatoHistorico> l, ToDoubleFunction<DatoHistorico> f) {
        return l.stream().mapToDouble(f).average().orElse(0);
    }

    private double avg(List<DatoHistorico> l, ToDoubleFunction<DatoHistorico> f) {
        return redondear(media(l, f));
    }

    private double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}