package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Objects;

@Aspect
@Component
public class AdminAspect {

    private static final Logger logger = LoggerFactory.getLogger(AdminAspect.class);
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public AdminAspect(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Around("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..)) || " +
            "execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public Object loggingAdminApi(ProceedingJoinPoint joinPoint) throws Throwable {
        LocalDateTime localDateTime = LocalDateTime.now();

        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String requestURI = request.getRequestURI();

        String authorization = request.getHeader("Authorization");
        String jwt = jwtUtil.substringToken(authorization);
        Claims claims = jwtUtil.extractClaims(jwt);
        String userId = claims.getSubject();

        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();

        logger.info("요청한 사용자의 ID: {}", userId);
        logger.info("API 요청 시각: {}", localDateTime);
        logger.info("API 요청 URL: {}", requestURI);

        if ("changeUserRole".equals(methodName)) {
            Object requestBody = args[1];
            try {
                logger.info("요청 본문: {}", objectMapper.writeValueAsString(requestBody));
            } catch (Exception e) {
                logger.warn("요청 본문 직렬화 실패: {}", e.getMessage());
            }
        }

        if ("deleteComment".equals(methodName)) {
            Object commentId = args[0];
            logger.info("요청 본문이 없습니다.");
        }

        Object result = joinPoint.proceed();
        logger.info("응답 본문: {}", result == null ? "void" : objectMapper.writeValueAsString(result));
        return result;
    }
}
