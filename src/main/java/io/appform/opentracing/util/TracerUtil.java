package io.appform.opentracing.util;

import brave.Tracing;
import brave.opentracing.BraveSpanContext;
import brave.opentracing.BraveTracer;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author ankush.nakaskar
 */

public class TracerUtil {

    public static final String TRACE_ID = "trace_id";
    public static final String SPAN_ID = "span_id";

    private static  Tracer tracer=null;

    @NotNull
    public static Tracer getTracer() {
        if(tracer ==null || !(tracer instanceof BraveTracer)){
            tracer = BraveTracer.newBuilder(Tracing.newBuilder().build()).build();
        }
        return tracer;
    }

    private static void populateMDCTracing(String traceId,String spanId){
        MDC.put(TRACE_ID,traceId);
        MDC.put(SPAN_ID,spanId);
    }

    public static String getMDCTraceId(){
        return MDC.get(TRACE_ID);
    }

    public static String getMDCSpanId(){
        return MDC.get(SPAN_ID);
    }

    public static void destroyTracingForRequest() {
        MDC.remove(TRACE_ID);
        MDC.remove(SPAN_ID);
    }

    public static void populateMDCTracing(Span span) {
        if(Objects.nonNull(span)) {
            populateMDCTracing(span.context().toTraceId(),span.context().toSpanId());
        }
    }
    public static boolean isTraceIDPresentInQueueMessage(Map<String, Object> properties){

        if(properties!=null && !properties.isEmpty()){
            if(properties.containsKey(TracerUtil.TRACE_ID) &&
                    properties.containsKey(TracerUtil.SPAN_ID)){
                return true;
            }
        }
        return false;
    }

    public static void populateTracingFromQueue(Map<String, Object> properties){
        if(isTraceIDPresentInQueueMessage(properties)){
            populateMDCTracing(String.valueOf(properties.get(TracerUtil.TRACE_ID)),
                    String.valueOf(properties.get(TracerUtil.SPAN_ID)));
        }
    }

    public static BraveSpanContext buildSpanFromHeaders(BraveTracer tracer) {
        if(isTracePresent()){
            Map<String, String> headers = new HashMap<>();
            headers.put("x-b3-traceid", TracerUtil.getMDCTraceId());
            headers.put("x-b3-spanid", TracerUtil.getMDCSpanId());
            headers.put("x-b3-parentspanid", TracerUtil.getMDCSpanId());
            return tracer.extract(Format.Builtin.TEXT_MAP, new TextMapAdapter(headers));
        }
        return null;
    }

    public static boolean isTracePresent() {
        return StringUtils.isNotBlank(TracerUtil.getMDCTraceId()) && StringUtils.isNotBlank(TracerUtil.getMDCSpanId());
    }
}
