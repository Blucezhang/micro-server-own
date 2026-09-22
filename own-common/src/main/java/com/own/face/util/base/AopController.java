package com.own.face.util.base;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
public class AopController {

    @Pointcut("execution(public com.own.face.util.Resp *( ..))")
    public void CutResp(){ }

    @Around("CutResp()")
    public Object HandleResp(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.info("HTTP: {}", request.getMethod());
            log.info("URL: {}", request.getRequestURL());
        }
        // Controller arguments routinely contain credentials, tokens, addresses and
        // message bodies. Record only their count; request correlation belongs in
        // the correlation ID/MDC path rather than in serialized argument logs.
        Object[] args = proceedingJoinPoint.getArgs();
        log.info("ARGUMENT_COUNT: {}", args == null ? 0 : args.length);
        log.info("METHOD_CLASS: {}.{}", proceedingJoinPoint.getSignature().getDeclaringTypeName(),
                proceedingJoinPoint.getSignature().getName());
        return proceedingJoinPoint.proceed();
    }
}
