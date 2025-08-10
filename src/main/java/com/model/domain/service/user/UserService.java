/*
 * SPDX-License-Identifier: BSD-3-Clause
 *
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * Licensed under the BSD 3-Clause License.
 * See LICENSE file in the project root for full license information.
 */
package com.model.domain.service.user;

import com.model.core.error.ErrorCode;
import com.model.core.error.custom.NotFoundException;
import com.model.domain.database.model.User;
import com.model.domain.database.repository.UserRepository;
import com.model.domain.http.dto.AuthenticationRequestDto;
import com.model.domain.http.dto.AuthenticationResponseDto;
import com.model.domain.http.dto.RegisterRequestDto;
import com.model.domain.service.token.JwtGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtGeneratorService jwtGeneratorService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponseDto userRegister(RegisterRequestDto request) {
        User user = new User();
        user.setUserName(request.userName());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.roles());

        userRepository.save(user);
        var jwtToken = jwtGeneratorService.generateToken(user);

        return AuthenticationResponseDto.builder().token(jwtToken).build();
    }

    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.userName(),
                        request.password()
                )
        );

        var user = this.userIsPresent(request.userName());
        var jwtToken = jwtGeneratorService.generateToken(user);

        return AuthenticationResponseDto.builder().token(jwtToken).build();
    }

    public User userIsPresent(String userName) {
        // TODO: Adicionar a norma I18n
        return userRepository.findByUserName(userName).orElseThrow(
                () -> new NotFoundException(ErrorCode.NOT_FOUND, "Usuário inserido não foi encontrado!"));
    }
}
