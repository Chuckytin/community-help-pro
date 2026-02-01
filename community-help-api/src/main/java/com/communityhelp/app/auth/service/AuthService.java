package com.communityhelp.app.auth.service;

import com.communityhelp.app.auth.dto.AuthResponse;
import com.communityhelp.app.user.dto.LoginRequestDto;
import com.communityhelp.app.user.dto.UserCreateRequestDto;

public interface AuthService {

    AuthResponse login(LoginRequestDto dto);

    AuthResponse register(UserCreateRequestDto dto);
}
