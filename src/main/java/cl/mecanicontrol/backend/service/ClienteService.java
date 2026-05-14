package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.dto.cliente.ClienteResponseDTO;
import cl.mecanicontrol.backend.dto.usuario.UsuarioResponseDTO;
import cl.mecanicontrol.backend.entity.Cliente;
import cl.mecanicontrol.backend.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<ClienteResponseDTO> findAll(){
        return clienteRepository.findAll().stream().map(this::toDTO).toList();
    }

    public ClienteResponseDTO findById(UUID id){
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return toDTO(cliente);
    }

    @Transactional
    public void addPuntos(UUID clienteId, int puntos){
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        int puntosActuales = cliente.getPuntosFidelizacion() == null ? 0 : cliente.getPuntosFidelizacion();
        cliente.setPuntosFidelizacion(puntosActuales + puntos);
        clienteRepository.save(cliente);
    }

    private ClienteResponseDTO toDTO(Cliente cliente){
        UsuarioResponseDTO usuarioDTO = new UsuarioResponseDTO(
                cliente.getUsuario().getId(),
                cliente.getUsuario().getNombre(),
                cliente.getUsuario().getApellido(),
                cliente.getUsuario().getEmail(),
                cliente.getUsuario().getRol().getNombre(),
                cliente.getUsuario().isActivo()
        );

        return new ClienteResponseDTO(
                cliente.getId(),
                usuarioDTO,
                cliente.getNivelFidelizacion().getNombre(),
                cliente.getPuntosFidelizacion() == null ? 0 : cliente.getPuntosFidelizacion(),
                cliente.getDescuentoDefault()
        );
    }
}
