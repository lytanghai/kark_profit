package com.money.kark_profit.config;

import com.money.kark_profit.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String path = request.getRequestURI();

        // Skip auth for public endpoints
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if token exists
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No valid auth header for path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // ✅ Now this works with the new validateToken method!
        if (!jwtUtils.validateToken(token)) {
            log.warn("❌ Invalid or expired token for path: {}", path);

            // Get expiration for better error message
            Date expiration = jwtUtils.getExpirationDate(token);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            String errorMessage = String.format(
                    "{\"error\": \"Token expired or invalid\", \"expired_at\": \"%s\"}",
                    expiration != null ? expiration.toString() : "unknown"
            );

            response.getWriter().write(errorMessage);
            return;
        }

        // Token is valid, extract username
        String username = jwtUtils.extractUsername(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            username, null, new ArrayList<>()
                    );

            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("✅ User authenticated: {}", username);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/public") ||
                path.startsWith("/auth") ||
                path.equals("/") ||
                path.startsWith("/health");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip filter for OPTIONS requests (CORS preflight)
        return request.getMethod().equals("OPTIONS");
    }
}