//package com.hess.metrichive.Infrastructure.RateLimit;
//
//import io.github.bucket4j.Bucket;
//import io.github.bucket4j.ConsumptionProbe;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//@Component
//public class hRateLimitInterceptor implements HandlerInterceptor {
//
//    private final RateLimitService rateLimitService;
//
//    public RateLimitInterceptor(RateLimitService rateLimitService) {
//        this.rateLimitService = rateLimitService;
//    }
//
//    @Override
//    public boolean preHandle(HttpServletRequest request,
//                             HttpServletResponse response,
//                             Object handler) throws Exception {
//
//        // For now, key by IP and default everyone to "free"
//        // When you add auth later, swap this out for userId + plan from your JWT/session
//        String key = "rate_limit:" + request.getRemoteAddr();
//        String planType = "free"; // hardcoded for now
//
//        Bucket bucket = rateLimitService.resolveBucket(key, planType);
//        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
//
//        if (probe.isConsumed()) {
//            response.addHeader("X-Rate-Limit-Remaining",
//                    String.valueOf(probe.getRemainingTokens()));
//            return true;
//        }
//
//        long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
//        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
//        response.addHeader("X-Rate-Limit-Retry-After-Seconds",
//                String.valueOf(retryAfterSeconds));
//        response.getWriter().write(
//                "Too many requests. Try again in " + retryAfterSeconds + "s.");
//        return false;
//    }
//}