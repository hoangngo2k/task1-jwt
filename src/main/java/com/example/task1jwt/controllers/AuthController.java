package com.example.task1jwt.controllers;

import com.example.task1jwt.config.jwt.JwtUtils;
import com.example.task1jwt.domains.*;
import com.example.task1jwt.domains.exceptions.TokenRefreshException;
import com.example.task1jwt.domains.requests.SignInRequest;
import com.example.task1jwt.domains.requests.SignUpRequest;
import com.example.task1jwt.domains.requests.TokenRefreshRequest;
import com.example.task1jwt.domains.responses.JwtResponse;
import com.example.task1jwt.domains.responses.MessageResponse;
import com.example.task1jwt.domains.responses.TokenRefreshResponse;
import com.example.task1jwt.services.RefreshTokenService;
import com.example.task1jwt.services.RoleService;
import com.example.task1jwt.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(value = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RoleService roleService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, RoleService roleService, RefreshTokenService refreshTokenService, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.roleService = roleService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping({"/signin"})
    public ResponseEntity<?> authenticateUser(@RequestBody @Valid SignInRequest signInRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        return ResponseEntity.ok(
                new JwtResponse(
                        jwt,
                        refreshToken.getToken(),
                        userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles));
    }

    @PostMapping({"/signup"})
    public ResponseEntity<?> registerUser(@RequestBody @Valid SignUpRequest signUpRequest) {
        if (userService.existsUserByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken"));
        }

        if (userService.existsUserByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already taken"));
        }

        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();

        Set<Role> roles = new HashSet();
        if (!strRoles.isEmpty() && strRoles != null) {
            strRoles.forEach((role) -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleService.getRoleByName(ERole.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "moderator":
                        Role moderatorRole = roleService.getRoleByName(ERole.ROLE_MODERATOR).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(moderatorRole);
                        break;
                    case "user":
                        Role userRole = roleService.getRoleByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                        break;
                }
            });
        } else {
            Role role = roleService.getRoleByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(role);
        }

        user.setRoles(roles);
        userService.createUser(user);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping({"/refresh-token"})
    public ResponseEntity<?> refreshToken(@RequestBody @Valid TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        Optional<RefreshToken> refreshToken = refreshTokenService.getRefreshTokenByToken(requestRefreshToken);
        return refreshToken.map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map((user) -> {
                    String token = this.jwtUtils.generateTokenFromUsername(user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                }).orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
    }
}
