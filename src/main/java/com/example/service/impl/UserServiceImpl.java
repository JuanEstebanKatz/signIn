package com.example.service.impl;

import com.example.dto.PhoneRequest;
import com.example.dto.SignUpRequest;
import com.example.dto.UserResponse;
import com.example.model.Phone;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=(?:.*\\d){2})(?!.*\\d{3,})(?=\\S+$)[A-Za-z\\d]{8,12}$");

    public UserServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    @Transactional
    public UserResponse registerUser(SignUpRequest request) {
        validateEmail(request.getEmail());
        validatePassword(request.getPassword());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El usuario ya existe");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreated(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        user.setIsActive(true);

        if (request.getPhones() != null) {
            for (PhoneRequest p : request.getPhones()) {
                Phone phone = new Phone();
                phone.setNumber(p.getNumber());
                phone.setCitycode(p.getCitycode());
                phone.setContrycode(p.getContrycode());
                phone.setUser(user);
                user.getPhones().add(phone);
            }
        }

        user.setToken(jwtUtil.generateToken(user.getEmail()));

        userRepository.save(user);

        return buildResponse(user);
    }

    @Override
    public UserResponse login(String token) {
        String email = jwtUtil.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setLastLogin(LocalDateTime.now());
        user.setToken(jwtUtil.generateToken(email));

        userRepository.save(user);

        return buildResponse(user);
    }

    private void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new RuntimeException("Email con formato inválido");
        }
    }

    private void validatePassword(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new RuntimeException("Contraseña inválida. Debe tener una mayúscula, dos números, 8-12 caracteres.");
        }
    }

    private UserResponse buildResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getCreated(),
                user.getLastLogin(),
                user.getToken(),
                user.getIsActive(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getPhones()
        );
    }
}
