package com.manbhavansteel.manbhavan_steel_backend.controller;

import com.manbhavansteel.manbhavan_steel_backend.entity.User;
import com.manbhavansteel.manbhavan_steel_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/customer/auth")
public class CustomerAuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final Map<String, LoginSession> sessions = new ConcurrentHashMap<>();

    private static final long TOKEN_VALIDITY_SECONDS = 24 * 60 * 60;

    public CustomerAuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // =========================
    // CUSTOMER REGISTRATION
    // =========================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        if (request == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Invalid registration request."));
        }

        String name = request.name == null ? "" : request.name.trim();
        String phone = request.phone == null ? "" : request.phone.trim();
        String email = request.email == null ? "" : request.email.trim();
        String password = request.password == null ? "" : request.password;
        String confirmPassword =
                request.confirmPassword == null ? "" : request.confirmPassword;

        // Name validation
        if (name.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Please enter your name."));
        }

        // Phone validation
        if (phone.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Please enter your mobile number."));
        }

        if (!phone.matches("^[0-9]{10}$")) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Please enter a valid 10-digit mobile number."));
        }

        // Password validation
        if (password.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Please enter a password."));
        }

        if (password.length() < 6) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Password must be at least 6 characters."));
        }

        // Confirm password
        if (!password.equals(confirmPassword)) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Passwords do not match."));
        }

        // Optional email validation
        if (!email.isEmpty()) {
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "Please enter a valid email address."));
            }

            if (userRepository.existsByEmail(email)) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "This email is already registered."));
            }
        }

        // Phone already registered
        if (userRepository.existsByPhone(phone)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "This mobile number is already registered."));
        }

        // Create new user
        User user = new User();

        user.setName(name);
        user.setPhone(phone);

        if (email.isEmpty()) {
            user.setEmail(null);
        } else {
            user.setEmail(email);
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setRole(User.Role.CUSTOMER);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Registration successful.",
                        "user", toUserResponse(savedUser)
                ));
    }

    // =========================
    // CUSTOMER LOGIN
    // =========================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        if (request == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Invalid login request."));
        }

        String phone = request.phone == null ? "" : request.phone.trim();
        String password = request.password == null ? "" : request.password;

        if (phone.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Please enter your mobile number."));
        }

        if (password.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Please enter your password."));
        }

        Optional<User> optionalUser = userRepository.findByPhone(phone);

        if (optionalUser.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid mobile number or password."));
        }

        User user = optionalUser.get();

        // Account active check
        if (!Boolean.TRUE.equals(user.getActive())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Your account is inactive."));
        }

        // Customer only
        if (user.getRole() != User.Role.CUSTOMER) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Customer login is not allowed for this account."));
        }

        // Password check
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid mobile number or password."));
        }

        // Create login token
        String token = UUID.randomUUID().toString();

        sessions.put(
                token,
                new LoginSession(
                        user.getId(),
                        Instant.now().getEpochSecond()
                )
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful.",
                        "token", token,
                        "user", toUserResponse(user)
                )
        );
    }

    // =========================
    // CURRENT LOGGED-IN USER
    // =========================
    @GetMapping("/me")
    public ResponseEntity<?> me(
            @RequestHeader(value = "Authorization", required = false)
            String authorization
    ) {

        User user = getAuthenticatedUser(authorization);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized."));
        }

        return ResponseEntity.ok(
                Map.of(
                        "user", toUserResponse(user)
                )
        );
    }

    // =========================
    // LOGOUT
    // =========================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false)
            String authorization
    ) {

        String token = extractToken(authorization);

        if (token != null) {
            sessions.remove(token);
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", "Logout successful."
                )
        );
    }

    // =========================
    // AUTHENTICATED USER HELPER
    // =========================
    public User getAuthenticatedUser(String authorization) {
        String token = extractToken(authorization);

        if (token == null) {
            return null;
        }

        LoginSession session = sessions.get(token);

        if (session == null) {
            return null;
        }

        long now = Instant.now().getEpochSecond();

        if ((now - session.getCreatedAt()) > TOKEN_VALIDITY_SECONDS) {
            sessions.remove(token);
            return null;
        }

        Optional<User> optionalUser =
                userRepository.findById(session.getUserId());

        if (optionalUser.isEmpty()) {
            sessions.remove(token);
            return null;
        }

        User user = optionalUser.get();

        if (!Boolean.TRUE.equals(user.getActive())) {
            sessions.remove(token);
            return null;
        }

        if (user.getRole() != User.Role.CUSTOMER) {
            sessions.remove(token);
            return null;
        }

        return user;
    }

    // =========================
    // TOKEN EXTRACTION
    // =========================
    private String extractToken(String authorization) {

        if (authorization == null) {
            return null;
        }

        if (authorization.isBlank()) {
            return null;
        }

        if (!authorization.startsWith("Bearer ")) {
            return null;
        }

        String token = authorization.substring(7).trim();

        if (token.isEmpty()) {
            return null;
        }

        return token;
    }

    // =========================
    // USER RESPONSE
    // =========================
    private UserResponse toUserResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    // =========================
    // REGISTER REQUEST
    // =========================
    public static class RegisterRequest {

        public String name;
        public String phone;
        public String email;
        public String password;
        public String confirmPassword;
    }

    // =========================
    // LOGIN REQUEST
    // =========================
    public static class LoginRequest {

        public String phone;
        public String password;
    }

    // =========================
    // USER RESPONSE
    // =========================
    public static class UserResponse {

        private final Long id;
        private final String name;
        private final String phone;
        private final String email;
        private final String role;

        public UserResponse(
                Long id,
                String name,
                String phone,
                String email,
                String role
        ) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.role = role;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }
    }

    // =========================
    // LOGIN SESSION
    // =========================
    private static class LoginSession {

        private final Long userId;
        private final long createdAt;

        public LoginSession(Long userId, long createdAt) {
            this.userId = userId;
            this.createdAt = createdAt;
        }

        public Long getUserId() {
            return userId;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }
}