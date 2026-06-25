package cl.mecanicontrol.backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ResendEmailService {

    private final JavaMailSender mailSender;
    private final ConfiguracionService configuracionService;

    @Value("${mail.from}")
    private String fromAddress;

    public ResendEmailService(JavaMailSender mailSender, ConfiguracionService configuracionService) {
        this.mailSender           = mailSender;
        this.configuracionService = configuracionService;
    }

    public void enviarVerificacion(String destinatario, String nombreCliente, String urlVerificacion) {
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#f4f6fb;">
                  <div style="background:linear-gradient(135deg,#1a1a2e 0%%,#16213e 100%%);padding:36px 32px;border-radius:16px 16px 0 0;text-align:center;">
                    <div style="display:inline-block;background:#f97316;border-radius:12px;padding:8px 20px;margin-bottom:12px;">
                      <span style="color:#fff;font-weight:900;font-size:18px;letter-spacing:2px;">MECANICAHUB</span>
                    </div>
                    <p style="color:#94a3b8;margin:4px 0 0;font-size:13px;letter-spacing:1px;">VERIFICA TU CORREO ELECTRONICO</p>
                  </div>
                  <div style="background:#fff;padding:36px 32px;border-radius:0 0 16px 16px;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                    <p style="font-size:16px;color:#1e293b;margin-top:0;">Hola <strong>%s</strong>,</p>
                    <p style="font-size:15px;color:#475569;line-height:1.6;">
                      Gracias por registrarte en <strong>MecanicaHub</strong>. Para activar tu cuenta y comenzar a gestionar tus vehiculos, verifica tu correo electronico.
                    </p>
                    <div style="text-align:center;margin:32px 0;">
                      <a href="%s"
                         style="background:#f97316;color:#fff;text-decoration:none;padding:16px 40px;border-radius:10px;font-weight:900;font-size:15px;display:inline-block;letter-spacing:1px;box-shadow:0 4px 14px rgba(249,115,22,0.4);">
                        VERIFICAR MI CORREO
                      </a>
                    </div>
                    <div style="background:#fef3c7;border-left:4px solid #f97316;border-radius:6px;padding:12px 16px;margin-top:20px;">
                      <p style="margin:0;font-size:13px;color:#92400e;">
                        Este enlace expira en 24 horas. Si no creaste una cuenta, ignora este correo.
                      </p>
                    </div>
                    <p style="font-size:12px;color:#94a3b8;text-align:center;margin-top:28px;">
                      MecanicaHub &mdash; Taller Mecanico Digital
                    </p>
                  </div>
                </div>
                """.formatted(nombreCliente, urlVerificacion);

        enviar(destinatario, "Verifica tu correo — MecanicaHub", html);
    }

    public void enviarNotificacionRepuesto(String destinatario, String nombreCliente,
                                            String codigoOt, String patente,
                                            String nombreRepuesto, int cantidad,
                                            java.math.BigDecimal precioUnitario, String origen) {
        boolean clienteTrae = "CLIENTE".equals(origen);
        String accion  = clienteTrae ? "debes traer al taller" : "sera suministrado por el taller";
        String colorBadge = clienteTrae ? "#f97316" : "#3b82f6";
        String labelOrigen = clienteTrae ? "Lo trae el cliente" : "Lo provee MecanicaHub";
        String total = precioUnitario != null
            ? "$" + precioUnitario.multiply(java.math.BigDecimal.valueOf(cantidad))
                      .setScale(0, java.math.RoundingMode.HALF_UP)
                      .toPlainString()
            : "$0";

        String html = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#f4f6fb;">
                  <div style="background:linear-gradient(135deg,#1a1a2e 0%%,#16213e 100%%);padding:36px 32px;border-radius:16px 16px 0 0;text-align:center;">
                    <div style="display:inline-block;background:#f97316;border-radius:12px;padding:8px 20px;margin-bottom:12px;">
                      <span style="color:#fff;font-weight:900;font-size:18px;letter-spacing:2px;">MECANICAHUB</span>
                    </div>
                    <p style="color:#94a3b8;margin:4px 0 0;font-size:13px;letter-spacing:1px;">INFORMACION SOBRE REPUESTO</p>
                  </div>
                  <div style="background:#fff;padding:36px 32px;border-radius:0 0 16px 16px;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                    <p style="font-size:16px;color:#1e293b;margin-top:0;">Hola <strong>%s</strong>,</p>
                    <p style="font-size:15px;color:#475569;line-height:1.6;">
                      El tecnico ha registrado un repuesto para tu vehiculo <strong>%s</strong> (OT: <strong>%s</strong>).
                      Este repuesto <strong>%s</strong>.
                    </p>
                    <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:20px;margin:24px 0;">
                      <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:12px;">
                        <div>
                          <p style="margin:0;font-size:12px;color:#94a3b8;letter-spacing:1px;text-transform:uppercase;">Repuesto</p>
                          <p style="margin:4px 0 0;font-size:18px;font-weight:900;color:#1e293b;">%s</p>
                          <p style="margin:4px 0 0;font-size:13px;color:#64748b;">Cantidad: %d</p>
                        </div>
                        <div style="text-align:right;">
                          <p style="margin:0;font-size:12px;color:#94a3b8;letter-spacing:1px;text-transform:uppercase;">Total</p>
                          <p style="margin:4px 0 0;font-size:22px;font-weight:900;color:#f97316;">%s</p>
                        </div>
                      </div>
                      <div style="margin-top:16px;padding-top:16px;border-top:1px solid #e2e8f0;">
                        <span style="display:inline-block;background:%s;color:#fff;font-weight:900;font-size:12px;padding:6px 16px;border-radius:50px;letter-spacing:1px;">
                          %s
                        </span>
                      </div>
                    </div>
                    %s
                    <p style="font-size:13px;color:#94a3b8;margin-top:28px;">
                      Si tienes alguna duda, no dudes en contactarnos.<br>
                      <strong style="color:#f97316;">Equipo MecanicaHub</strong>
                    </p>
                  </div>
                </div>
                """.formatted(
                    nombreCliente, patente, codigoOt, accion,
                    nombreRepuesto, cantidad, total,
                    colorBadge, labelOrigen,
                    clienteTrae
                        ? "<div style=\"background:#fff7ed;border-left:4px solid #f97316;border-radius:6px;padding:14px 16px;\">" +
                          "<p style=\"margin:0;font-size:14px;font-weight:700;color:#c2410c;\">Accion requerida</p>" +
                          "<p style=\"margin:6px 0 0;font-size:13px;color:#7c2d12;\">Por favor trae este repuesto al taller para continuar con el servicio de tu vehiculo.</p></div>"
                        : ""
                );
        enviar(destinatario, "Repuesto registrado para tu vehiculo (" + patente + ")", html);
    }

    public void enviarActualizacionFase(String destinatario, String nombreCliente,
                                        String codigoOt, String patente,
                                        String fase, String observaciones) {
        String html = construirHtml(nombreCliente, codigoOt, patente, fase, observaciones);
        enviar(destinatario, "Actualizacion de tu vehiculo (" + patente + ") — " + labelFase(fase), html);
    }

    private void enviar(String destinatario, String asunto, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(html, true);
            String[] bcc = configuracionService.getBccAdmins();
            if (bcc.length > 0) helper.setBcc(bcc);
            mailSender.send(message);
            log.info("Correo enviado a {}", destinatario);
        } catch (Exception e) {
            log.warn("No se pudo enviar correo a {}: {}", destinatario, e.getMessage());
        }
    }

    private String labelFase(String nombre) {
        return switch (nombre) {
            case "RECEPCION"       -> "Recepcion";
            case "DIAGNOSTICO"     -> "Diagnostico";
            case "EN_TRABAJO"      -> "En Trabajo";
            case "CONTROL_CALIDAD" -> "Control de Calidad";
            case "LISTO_ENTREGA"   -> "Listo para Entrega";
            default                -> nombre;
        };
    }

    private String iconoFase(String nombre) {
        return switch (nombre) {
            case "RECEPCION"       -> "&#128221;";
            case "DIAGNOSTICO"     -> "&#128269;";
            case "EN_TRABAJO"      -> "&#128296;";
            case "CONTROL_CALIDAD" -> "&#9989;";
            case "LISTO_ENTREGA"   -> "&#127881;";
            default                -> "&#128313;";
        };
    }

    private String colorFase(String nombre) {
        return switch (nombre) {
            case "RECEPCION"       -> "#3b82f6";
            case "DIAGNOSTICO"     -> "#8b5cf6";
            case "EN_TRABAJO"      -> "#f97316";
            case "CONTROL_CALIDAD" -> "#06b6d4";
            case "LISTO_ENTREGA"   -> "#22c55e";
            default                -> "#64748b";
        };
    }

    private String mensajeFase(String fase) {
        return switch (fase) {
            case "RECEPCION"       -> "Hemos recibido tu vehiculo correctamente. Ya esta registrado en nuestro sistema.";
            case "DIAGNOSTICO"     -> "Nuestros tecnicos estan realizando el diagnostico completo de tu vehiculo.";
            case "EN_TRABAJO"      -> "Excelentes noticias! Tu vehiculo esta siendo atendido en este momento por nuestros especialistas.";
            case "CONTROL_CALIDAD" -> "El trabajo esta terminado. Estamos realizando el control de calidad final para asegurar el mejor resultado.";
            case "LISTO_ENTREGA"   -> "Tu vehiculo esta listo y te espera. Puedes pasar a buscarlo cuando gustes.";
            default                -> "Tu vehiculo ha avanzado a una nueva fase.";
        };
    }

    private String construirHtml(String nombre, String codigoOt, String patente, String fase, String observaciones) {
        String label   = labelFase(fase);
        String icono   = iconoFase(fase);
        String color   = colorFase(fase);
        String mensaje = mensajeFase(fase);

        String bloqueObservaciones = "";
        if (observaciones != null && !observaciones.isBlank()) {
            bloqueObservaciones = """
                    <div style="margin-top:20px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:10px;padding:20px;">
                      <p style="margin:0 0 8px;font-size:12px;font-weight:900;color:#64748b;letter-spacing:1px;text-transform:uppercase;">Notas del Tecnico</p>
                      <p style="margin:0;font-size:14px;color:#334155;line-height:1.6;">%s</p>
                    </div>
                    """.formatted(observaciones);
        }

        return """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#f4f6fb;">
                  <!-- Header -->
                  <div style="background:linear-gradient(135deg,#1a1a2e 0%%,#16213e 100%%);padding:36px 32px;border-radius:16px 16px 0 0;text-align:center;">
                    <div style="display:inline-block;background:#f97316;border-radius:12px;padding:8px 20px;margin-bottom:12px;">
                      <span style="color:#fff;font-weight:900;font-size:18px;letter-spacing:2px;">MECANICAHUB</span>
                    </div>
                    <p style="color:#94a3b8;margin:4px 0 0;font-size:13px;letter-spacing:1px;">ACTUALIZACION DE ORDEN DE TRABAJO</p>
                  </div>

                  <!-- Body -->
                  <div style="background:#fff;padding:36px 32px;border-radius:0 0 16px 16px;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                    <p style="font-size:16px;color:#1e293b;margin-top:0;">Hola <strong>%s</strong>,</p>
                    <p style="font-size:15px;color:#475569;line-height:1.6;">%s</p>

                    <!-- Fase badge -->
                    <div style="text-align:center;margin:28px 0;">
                      <div style="display:inline-block;background:%s;border-radius:50px;padding:14px 32px;">
                        <span style="font-size:22px;">%s</span>
                        <span style="color:#fff;font-weight:900;font-size:16px;margin-left:10px;letter-spacing:1px;">%s</span>
                      </div>
                    </div>

                    <!-- OT Info -->
                    <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:10px;padding:20px;display:flex;gap:0;">
                      <div style="flex:1;text-align:center;border-right:1px solid #e2e8f0;padding-right:16px;">
                        <p style="margin:0;font-size:11px;font-weight:900;color:#94a3b8;letter-spacing:1px;text-transform:uppercase;">Orden de Trabajo</p>
                        <p style="margin:6px 0 0;font-size:18px;font-weight:900;color:%s;">%s</p>
                      </div>
                      <div style="flex:1;text-align:center;padding-left:16px;">
                        <p style="margin:0;font-size:11px;font-weight:900;color:#94a3b8;letter-spacing:1px;text-transform:uppercase;">Patente</p>
                        <p style="margin:6px 0 0;font-size:18px;font-weight:900;color:#1e293b;">%s</p>
                      </div>
                    </div>

                    %s

                    <!-- Footer message -->
                    <p style="font-size:13px;color:#94a3b8;margin-top:28px;line-height:1.6;">
                      Si tienes alguna pregunta sobre el estado de tu vehiculo, no dudes en contactarnos.<br>
                      <strong style="color:#f97316;">Equipo MecanicaHub</strong>
                    </p>
                  </div>
                </div>
                """.formatted(nombre, mensaje, color, icono, label, color, codigoOt, patente, bloqueObservaciones);
    }
}