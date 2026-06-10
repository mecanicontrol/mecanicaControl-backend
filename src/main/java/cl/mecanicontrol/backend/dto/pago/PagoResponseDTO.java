package cl.mecanicontrol.backend.dto.pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PagoResponseDTO(
        UUID id,
        String codigoOt,
        String cliente,
        String metodoPago,
        String estadoPago,
        BigDecimal montoTotal,
        BigDecimal montoPagado,
        String referenciaExterna,
        LocalDateTime fechaPago
) {
}
