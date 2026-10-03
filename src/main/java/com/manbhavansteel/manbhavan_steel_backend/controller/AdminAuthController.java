package com.manbhavansteel.manbhavan_steel_backend.controller;

import com.manbhavansteel.manbhavan_steel_backend.service.AdminAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173"
        "https://manbhavansteel.pages.dev"
})
@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request
    ) {

        boolean valid = adminAuthService.authenticate(
                request.username(),
                request.password()
        );

        if (!valid) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            "Invalid admin username or password"
                    ));
        }

        String token = adminAuthService.createToken();

        return ResponseEntity.ok(
                Map.of(
                        "token", token,
                        "username", request.username()
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {

        if (authorization != null
                && authorization.startsWith("Bearer ")) {

            String token = authorization.substring(7);

            adminAuthService.logout(token);
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Logged out successfully"
                )
        );
    }

    public record LoginRequest(
            String username,
            String password
    ) {}
}