package io.appform.opentracing.util;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.util.GlobalTracer;
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

    public static Tracer getTracer() {
        GlobalTracer.registerIfAbsent(BraveTracer.newBuilder(Tracing.newBuilder().build()).build());
        return GlobalTracer.get();
    }

    public static Tracer getTracer(Tracing tracing) {
        if(tracer ==null || !(tracer instanceof BraveTracer)){
            tracer = BraveTracer.newBuilder(tracing).build();
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

    public static SpanContext buildSpanFromHeaders(Tracer tracer) {
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
        if(TracerUtil.getMDCTraceId()!=null && !TracerUtil.getMDCTraceId().isEmpty() &&  TracerUtil.getMDCSpanId()!=null && !TracerUtil.getMDCSpanId().isEmpty()){
            return true;
        }
        return false;
    }

    public static String stripToEmpty(String input){
        return input == null ? "" : input.trim();
    }
}
