package com.hess.metrichive.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hess.metrichive.Exception.ErrorResponse;
import com.hess.metrichive.Model.Tenant;
import com.hess.metrichive.Repository.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final TenantRepository tenantRepository;
    private final ObjectMapper objectMapper; // To format JSON errors

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Extract the API Key from the Header
        String apiKey = request.getHeader("X-API-Key");

        // Alternative: Check for "Authorization: Bearer <key>"
        if (apiKey == null && request.getHeader("Authorization") != null && request.getHeader("Authorization").startsWith("Bearer ")) {
            apiKey = request.getHeader("Authorization").substring(7);
        }

        // 2. If no key is provided, let Spring Security block it later (or we can block it here)
        if (apiKey == null || apiKey.trim().isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Verify the key against the database
        Optional<Tenant> tenantOpt = tenantRepository.findByApiKey(apiKey);

        if (tenantOpt.isEmpty()) {
            // Fake or revoked API key! Kick them out immediately.
            sendUnauthorizedError(response, "Invalid API Key provided.");
            return;
        }

        // 4. IT'S A MATCH! Securely set the context.
        Tenant tenant = tenantOpt.get();

        try {
            // Set our custom context for business logic
            TenantContext.setTenantId(tenant.getId());

            // Tell Spring Security that this request is officially authenticated
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    tenant.getId(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Let the request pass through to the Controller
            filterChain.doFilter(request, response);

        } finally {
            // 5. CRITICAL: Always clean up after the request is done to prevent data leaks!
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("UNAUTHORIZED")
                .message(message)
                .timestamp(Instant.now())
                .requestId("req_pending_phase4")
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}