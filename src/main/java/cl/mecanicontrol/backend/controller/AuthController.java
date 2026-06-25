package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.dto.auth.AuthRequestDTO;
import cl.mecanicontrol.backend.dto.auth.AuthResponseDTO;
import cl.mecanicontrol.backend.dto.auth.RegisterConVehiculoRequestDTO;
import cl.mecanicontrol.backend.dto.auth.RegisterRequestDTO;
import cl.mecanicontrol.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request){
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequestDTO request){
        authService.register(request);
        return ResponseEntity.status(201).body(Map.of("mensaje", "Revisa tu correo para verificar tu cuenta"));
    }

    @PostMapping("/register-con-vehiculo")
    public ResponseEntity<Map<String, String>> registerConVehiculo(@Valid @RequestBody RegisterConVehiculoRequestDTO request){
        authService.registerConVehiculo(request);
        return ResponseEntity.status(201).body(Map.of("mensaje", "Revisa tu correo para verificar tu cuenta"));
    }

    @GetMapping("/verificar")
    public ResponseEntity<Map<String, String>> verificarEmail(@RequestParam String token) {
        authService.verificarEmail(token);
        return ResponseEntity.ok(Map.of("mensaje", "Correo verificado correctamente. Ya puedes iniciar sesión."));
    }
}
