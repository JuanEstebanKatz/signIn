package com.example.controller;


import com.example.dto.SignUpRequest;
import com.example.dto.UserResponse;
import com.example.model.User;
import com.example.security.JwtUtil;
import com.example.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody SignUpRequest userRequestDTO) {
        UserResponse  response  = userService.signUp(userRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/login")
    public ResponseEntity<SignUpRequest> login(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        // Se espera que el token venga como: Bearer eyJh...
        String cleanedToken = token.replace("Bearer ", "");

        String email = jwtUtil.extractUsername(cleanedToken);

        if (!jwtUtil.validateToken(cleanedToken, email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.getUserByEmail(email);

        // Generar nuevo token al momento del login
        String newToken = jwtUtil.generateToken(email);

        // Actualizar el lastLogin
        user.setLastLogin(new java.util.Date());

        return ResponseEntity.ok(new UserResponse(user, newToken));
    }
}

