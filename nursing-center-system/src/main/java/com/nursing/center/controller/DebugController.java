package com.nursing.center.controller;

import com.nursing.center.common.result.Result;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class DebugController {

    @GetMapping("/auth-info")
    public Result<Map<String, Object>> getAuthInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> authInfo = new HashMap<>();
        
        if (auth != null) {
            authInfo.put("isAuthenticated", auth.isAuthenticated());
            authInfo.put("principal", auth.getPrincipal().toString());
            authInfo.put("authorities", auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
            authInfo.put("name", auth.getName());
        } else {
            authInfo.put("message", "No authentication found");
        }
        
        return Result.success(authInfo);
    }

    @GetMapping("/test-admin")
    public Result<String> testAdmin() {
        return Result.success("Admin access successful");
    }
}
