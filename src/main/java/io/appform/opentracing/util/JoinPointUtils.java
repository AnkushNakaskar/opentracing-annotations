package io.appform.opentracing.util;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
/**
 * @author ankush.nakaskar
 */
public class JoinPointUtils {

    public static Object[] getMethodParameters(JoinPoint joinPoint) {
        return joinPoint.getArgs();
    }

    public static Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }

    public static String getMethodName(JoinPoint joinPoint) {
        return joinPoint.getSignature().getName();
    }

    public static String getClassName(JoinPoint joinPoint) {
        return joinPoint.getSignature().getDeclaringTypeName();
    }

    public static Object getTargetObject(JoinPoint joinPoint) {
        return joinPoint.getTarget();
    }

    public static Class<?>[] getParameterTypes(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getParameterTypes();
    }

}
