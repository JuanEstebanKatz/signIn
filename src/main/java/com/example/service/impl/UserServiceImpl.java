package com.example.service.impl;

import com.example.dto.PhoneDto;
import com.example.dto.SignUpRequest;
import com.example.dto.UserResponse;
import com.example.model.Phone;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.security.JwtUtil;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @Autowired
    private PasswordEncoder passwordEncoder;


    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=(?:.*\\d){2})(?!.*\\d{3,})(?=\\S+$)[A-Za-z\\d]{8,12}$");

    public UserServiceImpl(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    @Override
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
            for (PhoneDto p : request.getPhones()) {
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

    @Override
    public UserResponse signUp(SignUpRequest request) {
        User user = new User();
        // settear campos...

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        return new UserResponse(user, token);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setId(UUID.randomUUID());
                    newUser.setEmail(email);
                    newUser.setName("Nombre por defecto"); // Ajusta si tienes más lógica
                    newUser.setPassword(passwordEncoder.encode("passwordPorDefecto123")); // IMPORTANTE: solo si tiene sentido
                    newUser.setCreated(LocalDateTime.now());
                    newUser.setLastLogin(LocalDateTime.now());
                    newUser.setIsActive(true);
                    // Podrías generar un token o no, depende de tu lógica
                    newUser.setToken(jwtUtil.generateToken(email));

                    return userRepository.save(newUser);
                });
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
        return new UserResponse(user, user.getToken());
    }
}
