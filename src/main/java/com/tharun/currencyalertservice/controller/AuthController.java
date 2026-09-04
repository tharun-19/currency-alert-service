package com.tharun.currencyalertservice.controller;

import com.tharun.currencyalertservice.security.JwtUtil;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for username={} from endpoint=/api/auth/login", request.username());

        if (!jwtUtil.isValidCredentials(request.username(), request.password())) {
            log.warn("Login failed for username={} reason=invalid_credentials", request.username());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        String token = jwtUtil.generateToken(request.username());
        log.info("Login successful for username={} tokenIssued=true", request.username());
        return ResponseEntity.ok(Map.of("token", token));
    }

    public record LoginRequest(String username, String password) {
    }
}