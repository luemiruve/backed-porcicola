package com.luemi.porcicola.service;

import com.luemi.porcicola.dto.AuthRequest;
import com.luemi.porcicola.dto.AuthResponse;
import com.luemi.porcicola.dto.RegistroRequest;
import com.luemi.porcicola.enums.RolUsuario;
import com.luemi.porcicola.model.Granja;
import com.luemi.porcicola.model.Usuario;
import com.luemi.porcicola.repository.GranjaRepository;
import com.luemi.porcicola.repository.UsuarioRepository;
import com.luemi.porcicola.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GranjaRepository granjaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public AuthResponse registro(RegistroRequest request){
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        //Crear primero la granja por que el usuario necesita una referencia a la granja
        Granja granja = new Granja();
        granja.setNombre(request.getNombreGranja());
        granja.setUbicacion(request.getUbicacionGranja());
        granja = granjaRepository.save(granja);

        //Crear el Usuario Administrador
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setTelefono(request.getTelefono());
        usuario.setGranja(granja);
        usuario.setRol(RolUsuario.valueOf("ADMIN")); //asignacion del enum correcto
        usuarioRepository.save(usuario);

        //Generar token
        String token = jwtUtil.generateToken(
                usuario.getEmail(),
                usuario.getRol().name(), // Convertir el enum a String para el token
                granja.getIdGranja());

                return new AuthResponse(token, usuario.getRol().name(), granja.getIdGranja());

    }

    public AuthResponse login(AuthRequest request) {
        //valida credenciales si esta mal lanza un error automatico
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );


        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + request.getEmail()));


        String token = jwtUtil.generateToken(
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getGranja().getIdGranja()
        );

        return new AuthResponse(token, usuario.getRol().name(), usuario.getGranja().getIdGranja());
    }

}
