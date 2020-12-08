package cn.com.glsx.kafka.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

@Aspect
@Slf4j
public class ExceptionLogAspect {
    @AfterThrowing(pointcut = "execution(* cn.com.glsx.kafka.service..*(..))",throwing = "ex")
    public void afterThrowing(JoinPoint jp, RuntimeException ex)
    {
        log.error("类名:" + jp.getSignature().getDeclaringTypeName() + ";方法:"
                + jp.getSignature().getName() + ";异常信息："+ex.getMessage());
        ex.printStackTrace();
    }
}
