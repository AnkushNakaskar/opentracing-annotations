package io.appform.opentracing;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import io.opentracing.Tracer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;

/**
 * @author ankush.nakaskar
 */

public class TracerUtil {

    public static final String TRACE_ID = "trace_id";
    public static final String SPAN_ID = "span_id";

    @NotNull
    public static Tracer getTracer(Tracer tracer) {
        if(tracer ==null || !(tracer instanceof BraveTracer)){
            tracer = BraveTracer.newBuilder(Tracing.newBuilder().build()).build();
        }
        return tracer;
    }

    public static void populateMDCTracing(String traceId,String spanId){
        MDC.put(TRACE_ID,traceId);
        MDC.put(SPAN_ID,spanId);
    }

    public static String getMDCTraceId(){
        return MDC.get(TRACE_ID);
    }

    public static String getMDCSpanId(){
        return MDC.get(SPAN_ID);
    }
}
