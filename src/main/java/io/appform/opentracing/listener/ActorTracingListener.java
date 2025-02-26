package io.appform.opentracing.listener;

import io.appform.opentracing.util.JoinPointUtils;
import io.appform.opentracing.util.TracerUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ankush.nakaskar
 */
@Aspect
public class ActorTracingListener {

    private static final Logger log = LoggerFactory.getLogger(ActorTracingListener.class.getSimpleName());

    @Pointcut("execution(* io.appform.dropwizard.actors.actor.BaseActor+.handle(..))")
    public void tracingHandleActorMethodCalled() {
        //Empty as required
    }

    @Pointcut("execution(* *(..))")
    public void anyFunctionCalled() {
        //Empty as required
    }

    @Before("tracingHandleActorMethodCalled() && anyFunctionCalled()")
    public void before(JoinPoint joinPoint) throws Throwable {
        log.info("before tracingHandleActorMethodCalled LoggingAspect called..!");
        Object[] args = JoinPointUtils.getMethodParameters(joinPoint);
        log.info("args tracingHandleActorMethodCalled LoggingAspect called..! {}",args);

    }

    @After("tracingHandleActorMethodCalled() && anyFunctionCalled()")
    public void after(JoinPoint joinPoint) throws Throwable {
        log.debug("after tracingHandleActorMethodCalled LoggingAspect called..!");
        TracerUtil.destroyTracingForRequest();
    }






}
