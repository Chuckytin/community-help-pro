package com.communityhelp.app.auth.service;

import com.communityhelp.app.auth.dto.AuthResponse;
import com.communityhelp.app.user.dto.UserLoginRequestDto;

public interface AuthService {

    AuthResponse login(UserLoginRequestDto dto);

}
