package com.nowcoder.community.fitler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 拦截所有service包及子包 所有方法 记录请求信息和响应信息到文件
 */
@Aspect
@Component
public class WebLogAspect {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //切入点
    @Pointcut("execution(public * com.nowcoder.community.service.*.*(..))")
    public void pointcutWebLog() {
    }

    //执行逻辑
    @Around("pointcutWebLog()")
    public Object webLog(ProceedingJoinPoint pjp) throws Throwable {
        Object rtValue;
        Object[] args = pjp.getArgs();//得到方法执行所需的参数

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            rtValue = pjp.proceed(args);//TODO
            return rtValue;
        }
        HttpServletRequest request = attributes.getRequest();
        logger.info("URL : " + request.getRequestURL().toString());
        logger.info("HTTP_METHOD : " + request.getMethod());
        logger.info("IP : " + request.getRemoteAddr());
        logger.info("CLASS_METHOD : " + pjp.getSignature().getDeclaringTypeName() + "." +
                pjp.getSignature().getName());
        logger.info("ARGS : " + Arrays.toString(args));

        rtValue = pjp.proceed(args);//明确调用业务层方法（切入点方法）

        logger.info("RESPONSE : " + new ObjectMapper().writeValueAsString(rtValue));


        return rtValue;
    }

}
