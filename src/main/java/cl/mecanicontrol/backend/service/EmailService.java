package cl.mecanicontrol.backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final ConfiguracionService configuracionService;

    @Value("${mail.from}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender, ConfiguracionService configuracionService) {
        this.mailSender           = mailSender;
        this.configuracionService = configuracionService;
    }

    public void enviar(String para, String asunto, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(para);
            helper.setSubject(asunto);
            helper.setText(htmlBody, true);
            String[] bcc = configuracionService.getBccAdmins();
            if (bcc.length > 0) helper.setBcc(bcc);
            mailSender.send(message);
            log.info("Email enviado a {}: {}", para, asunto);
        } catch (Exception e) {
            log.error("Error enviando email a {}: {}", para, e.getMessage());
        }
    }

    public void enviarConfirmacionAgendamiento(String email, String nombre,
            String servicio, String fechaHora, String codigoOt) {
        String html = """
            <div style="font-family:sans-serif;max-width:560px;margin:0 auto">
              <div style="background:#ea580c;padding:24px;border-radius:12px 12px 0 0">
                <h1 style="color:white;margin:0;font-size:22px">MecaniControl</h1>
              </div>
              <div style="background:#f9fafb;padding:32px;border-radius:0 0 12px 12px;border:1px solid #e5e7eb">
                <h2 style="color:#111827;margin-top:0">¡Tu agendamiento está confirmado!</h2>
                <p style="color:#374151">Hola <strong>%s</strong>, tu hora ha sido reservada exitosamente.</p>
                <div style="background:white;border:1px solid #e5e7eb;border-radius:8px;padding:20px;margin:20px 0">
                  <p style="margin:4px 0;color:#6b7280;font-size:13px">Servicio</p>
                  <p style="margin:0 0 12px;font-weight:bold;color:#111827">%s</p>
                  <p style="margin:4px 0;color:#6b7280;font-size:13px">Fecha y hora</p>
                  <p style="margin:0 0 12px;font-weight:bold;color:#111827">%s</p>
                  %s
                </div>
                <p style="color:#9ca3af;font-size:12px;margin-top:24px">MecaniControl — Taller Mecánico</p>
              </div>
            </div>
            """.formatted(
            nombre, servicio, fechaHora,
            codigoOt != null
                ? "<p style=\"margin:4px 0;color:#6b7280;font-size:13px\">Código OT</p><p style=\"margin:0;font-weight:bold;color:#ea580c\">%s</p>".formatted(codigoOt)
                : ""
        );
        enviar(email, "Agendamiento confirmado — " + servicio, html);
    }

    public void enviarVehiculoListo(String email, String nombre,
            String servicio, String codigoOt) {
        String html = """
            <div style="font-family:sans-serif;max-width:560px;margin:0 auto">
              <div style="background:#16a34a;padding:24px;border-radius:12px 12px 0 0">
                <h1 style="color:white;margin:0;font-size:22px">MecaniControl</h1>
              </div>
              <div style="background:#f9fafb;padding:32px;border-radius:0 0 12px 12px;border:1px solid #e5e7eb">
                <h2 style="color:#111827;margin-top:0">Tu vehículo está listo</h2>
                <p style="color:#374151">Hola <strong>%s</strong>, el servicio <strong>%s</strong> ha finalizado.</p>
                <div style="background:white;border:2px solid #16a34a;border-radius:8px;padding:20px;margin:20px 0;text-align:center">
                  <p style="color:#16a34a;font-weight:bold;font-size:18px;margin:0">Listo para retirar</p>
                  <p style="color:#6b7280;font-size:13px;margin:8px 0 0">OT: <strong>%s</strong></p>
                </div>
                <p style="color:#9ca3af;font-size:12px;margin-top:24px">MecaniControl — Taller Mecánico</p>
              </div>
            </div>
            """.formatted(nombre, servicio, codigoOt != null ? codigoOt : "—");
        enviar(email, "Tu vehículo está listo — " + servicio, html);
    }
}