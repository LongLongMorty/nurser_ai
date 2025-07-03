package com.nursing.center.controller;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.controller
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  12:51
 * @Description: TODO
 * @Version: 1.0
 */

import com.nursing.center.common.result.Result;
import com.nursing.center.dto.LoginRequest;
import com.nursing.center.dto.LoginResponse;
import com.nursing.center.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return Result.success("登录成功", response);
    }

    @PostMapping("/logout")
    public Result<String> logout() {  // 改为 Result<String>
        // JWT是无状态的，客户端删除token即可
        return Result.success("登出成功");
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        LoginResponse response = authService.refreshToken(authHeader);
        return Result.success("刷新成功", response);
    }
}