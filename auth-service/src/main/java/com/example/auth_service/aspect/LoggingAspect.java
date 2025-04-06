package com.example.auth_service.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Slf4j
@Component
public class LoggingAspect {
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerPointcut() {}

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void servicePointcut() {}

    @Before("controllerPointcut()")
    public void controllerLogBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }
        if (request != null) {
            log.info("NEW REQUEST: URL: {}, HTTP METHOD: {}, CONTROLLER METHOD {}",
                    request.getRequestURL().toString(),
                    request.getMethod(),
                    joinPoint.getSignature().getName());
        }
    }

    @Before("servicePointcut()")
    public void serviceLogBefore(JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        Object[] args = joinPoint.getArgs();

        String argsString = args.length > 0 ? Arrays.toString(args) : "METHOD HAS NO ARGUMENTS";

        log.info("RUN SERVICE METHOD: SERVICE METHOD: {}.{}\nMETHOD ARGUMENTS: [{}] ",
                className, methodName, argsString);
    }


    @AfterReturning(returning = "returnObject", pointcut = "controllerPointcut()")
    public void controllerLogAfterReturning(Object returnObject) {
        log.info("RETURN RESULT: {}", returnObject);
    }


    @AfterThrowing(throwing = "ex", pointcut = "controllerPointcut()")
    public void controllerLogAfterThrowing(JoinPoint joinPoint,Exception ex) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        log.error("Exception in {}.{} with arguments {}. Exception message: {}",
                className, methodName, Arrays.toString(joinPoint.getArgs()), ex.getMessage());

    }
}
