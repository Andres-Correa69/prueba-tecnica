package develope.auth.infrastructure.adapter.in.rest;

import develope.auth.application.port.in.AuthResult;
import develope.auth.application.port.in.LoginCommand;
import develope.auth.application.port.in.LoginUseCase;
import develope.auth.application.port.in.RegisterUserCommand;
import develope.auth.application.port.in.RegisterUserUseCase;
import develope.auth.infrastructure.adapter.in.rest.dto.LoginRequest;
import develope.auth.infrastructure.adapter.in.rest.dto.LoginResponse;
import develope.auth.infrastructure.adapter.in.rest.dto.RegisterRequest;
import develope.auth.infrastructure.adapter.in.rest.dto.RegisterResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Adaptador de entrada: traduce peticiones HTTP en llamadas a los puertos de entrada.
 *
 * <p>Nótese que este controlador tiene ~20 líneas. Eso es intencional: los únicos
 * trabajos del controlador son (1) mapear DTO ↔ Command y (2) mapear Result ↔ DTO.
 * Toda la lógica — validación, chequeo de unicidad, hasheo, emisión de token — vive
 * en los casos de uso. Si alguna vez ves lógica de negocio colándose en un
 * controlador, muévela una capa abajo.</p>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUserUseCase registerUser;
    private final LoginUseCase login;

    public AuthController(RegisterUserUseCase registerUser, LoginUseCase login) {
        this.registerUser = registerUser;
        this.login = login;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest body) {
        UUID userId = registerUser.register(new RegisterUserCommand(body.username(), body.password()));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new RegisterResponse(userId, body.username().toLowerCase()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest body) {
        AuthResult result = login.login(new LoginCommand(body.username(), body.password()));
        return ResponseEntity.ok(new LoginResponse(
                result.token(), result.userId(), result.username(), result.expiresAt()));
    }
}
