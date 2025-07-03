package com.nursing.center.security;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.security
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  11:48
 * @Description: TODO
 * @Version: 1.0
 */
import com.nursing.center.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        log.info("处理请求: {} {}", request.getMethod(), requestPath);
        
        try {
            String jwt = getJwtFromRequest(request);
            log.info("从请求中获取到的JWT: {}", jwt != null ? "存在" : "不存在");

            if (StringUtils.hasText(jwt)) {
                log.info("JWT格式验证: {}", jwtUtils.validateTokenFormat(jwt));
                
                if (jwtUtils.validateTokenFormat(jwt)) {
                    String username = jwtUtils.getUsernameFromToken(jwt);
                    log.info("从JWT中解析的用户名: {}", username);

                    if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        log.info("加载用户详情: {}, 权限: {}", userDetails.getUsername(), userDetails.getAuthorities());

                        boolean tokenValid = jwtUtils.validateToken(jwt, username);
                        log.info("JWT token验证结果: {}", tokenValid);
                        
                        if (tokenValid) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());

                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            log.info("用户认证成功: {}, 权限: {}", username, userDetails.getAuthorities());
                        } else {
                            log.warn("JWT token验证失败: {}", username);
                        }
                    }
                }
            } else {
                log.info("请求中未包含JWT token");
            }
        } catch (Exception e) {
            log.error("JWT认证过滤器异常: {}", e.getMessage(), e);
            // 清理上下文，确保认证失败时不会保留错误的认证信息
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中获取JWT token
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
