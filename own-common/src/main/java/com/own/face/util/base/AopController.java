package com.own.face.util.base;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class AopController {

    @Pointcut("execution(public com.own.face.util.Resp *( ..))")
    public void CutResp(){ }

    @Around("CutResp()")
    public Map<String,Object> HandleResp(ProceedingJoinPoint proceedingJoinPoint){
        //最后会做日志处理
        Map<String,Object> result = new HashMap<>();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        log.info("HTTP:",request.getMethod());
        log.info("URL:",request.getRequestURL());
        log.info("PARAMS:", Arrays.toString(proceedingJoinPoint.getArgs()));
        log.info("METHED_CLASS",proceedingJoinPoint.getSignature().getDeclaringTypeName()+"."+proceedingJoinPoint.getSignature().getName());
        result.put("HTTP",request.getMethod());
        result.put("URL",request.getRequestURI());
        result.put("PARAMS",Arrays.toString(proceedingJoinPoint.getArgs()));
        result.put("METHED_CLASS",proceedingJoinPoint.getSignature().getDeclaringTypeName()+"."+proceedingJoinPoint.getSignature().getName());
        return result;
    }
}
