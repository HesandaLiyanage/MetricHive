package com.hess.metrichive.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // This forces it to be the absolute FIRST filter in the entire application
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_KEY = "request_id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Generate a unique ID for this specific HTTP request
        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        // 2. Put it in the logging ThreadLocal (MDC)
        MDC.put(REQUEST_ID_KEY, requestId);

        try {
            // 3. Add the request ID to the HTTP Response headers so the client/Postman can see it
            response.addHeader("X-Request-ID", requestId);

            filterChain.doFilter(request, response);
        } finally {
            // 4. CRITICAL: Prevent memory leaks in the thread pool
            MDC.clear();
        }
    }
}