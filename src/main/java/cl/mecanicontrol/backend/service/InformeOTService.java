package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.entity.FaseVehiculo;
import cl.mecanicontrol.backend.entity.OrdenTrabajo;
import cl.mecanicontrol.backend.repository.FaseVehiculoRepository;
import cl.mecanicontrol.backend.repository.OrdenTrabajoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InformeOTService {

    private final OrdenTrabajoRepository otrRepository;
    private final FaseVehiculoRepository faseVehiculoRepository;

    public InformeOTService(OrdenTrabajoRepository otrRepository,
                            FaseVehiculoRepository faseVehiculoRepository) {
        this.otrRepository = otrRepository;
        this.faseVehiculoRepository = faseVehiculoRepository;
    }

    public String generarPDF(UUID otId){
        OrdenTrabajo ot = otrRepository.findById(otId)
                .orElseThrow(()-> new RuntimeException("OT no encontrada"));

        List<FaseVehiculo> fases = faseVehiculoRepository.findByOrdenTrabajoId(otId);

        String contenido = construirContenidoPDF(ot, fases);

        String urlPDF = subirASupabase(otId, contenido);

        return urlPDF;
    }

    private String construirContenidoPDF(OrdenTrabajo ot, List<FaseVehiculo> fases) {
        StringBuilder sb = new StringBuilder();
        sb.append("ORDEN DE TRABAJO: ").append(ot.getCodigoOt()).append("\n");
        sb.append("Estado: ").append(ot.getEstadoOt().getNombre()).append("\n");
        sb.append("Diagnostico: ").append(ot.getDiagnostico()).append("\n");
        sb.append("Trabajo Realizado: ").append(ot.getTrabajoRealizado()).append("\n");
        sb.append("Costo Mano de Obra: ").append(ot.getCostoManoObra()).append("\n");
        sb.append("Costo Repuestos: ").append(ot.getCostoRepuestos()).append("\n");
        sb.append("Total: ").append(ot.getTotal()).append("\n");
        sb.append("\nFases: \n");
        fases.forEach(f -> {
            sb.append("- ").append(f.getFase().getNombre())
                    .append(" | Inicio: ").append(f.getInicioAt())
                    .append(" | Fin: ").append(f.getFinAt()).append("\n");
        });

        return sb.toString();
    }

    private String subirASupabase(UUID otId, String contenido) {

        //config
        return "https://supabase.co/storage/v1/object/public/documentos/OT-" + otId + ".pdf";

    }
}
