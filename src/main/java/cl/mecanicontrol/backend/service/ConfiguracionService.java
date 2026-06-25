package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.entity.Configuracion;
import cl.mecanicontrol.backend.repository.ConfiguracionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfiguracionService {

    private static final String CLAVE_BCC = "notificaciones_bcc_admins";

    private final ConfiguracionRepository repo;

    @Value("${mail.admins-bcc:}")
    private String fallbackBcc;

    public String[] getBccAdmins() {
        return repo.findById(CLAVE_BCC)
                .map(c -> c.getValor())
                .filter(v -> !v.isBlank())
                .map(v -> Arrays.stream(v.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .toArray(String[]::new))
                .orElseGet(() -> {
                    if (fallbackBcc != null && !fallbackBcc.isBlank()) {
                        return Arrays.stream(fallbackBcc.split(","))
                                .map(String::trim).filter(s -> !s.isBlank())
                                .toArray(String[]::new);
                    }
                    return new String[0];
                });
    }

    public List<String> getBccAdminsList() {
        return Arrays.asList(getBccAdmins());
    }

    public int getCapacidadMaximaTaller() {
        return repo.findById("capacidad_maxima_taller")
                .map(c -> { try { return Integer.parseInt(c.getValor().trim()); } catch (Exception e) { return 15; } })
                .orElse(15);
    }

    public void setCapacidadMaximaTaller(int capacidad) {
        Configuracion cfg = repo.findById("capacidad_maxima_taller").orElseGet(() -> {
            Configuracion c = new Configuracion();
            c.setClave("capacidad_maxima_taller");
            c.setDescripcion("Número máximo de vehículos físicamente en el taller al mismo tiempo");
            return c;
        });
        cfg.setValor(String.valueOf(capacidad));
        repo.save(cfg);
    }

    public void setBccAdmins(List<String> correos) {
        Configuracion cfg = repo.findById(CLAVE_BCC).orElseGet(() -> {
            Configuracion c = new Configuracion();
            c.setClave(CLAVE_BCC);
            c.setDescripcion("Correos que reciben copia oculta (BCC) de todas las notificaciones del sistema");
            return c;
        });
        cfg.setValor(String.join(",", correos));
        repo.save(cfg);
    }
}