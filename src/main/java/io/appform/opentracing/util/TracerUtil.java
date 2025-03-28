package io.appform.opentracing.util;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import io.appform.opentracing.Constants;
import io.appform.opentracing.FunctionData;
import io.appform.opentracing.TracingHandler;
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

import static io.appform.opentracing.Constants.SPAN_ID;
import static io.appform.opentracing.Constants.TRACE_ID;

/**
 * @author ankush.nakaskar
 */

public class TracerUtil {


    public static boolean isTracerEnabled() {
        return GlobalTracer.isRegistered();
    }
    public static Tracer getTracer() {
        return GlobalTracer.get();
    }

    public static void registerBraveTracer(){
        GlobalTracer.registerIfAbsent(BraveTracer.newBuilder(Tracing.newBuilder().build()).build());
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

    public static void destroyTracingForCurrentThread() {
        closeActiveSpan();
        MDC.remove(TRACE_ID);
        MDC.remove(SPAN_ID);
    }

    public static void populateMDCTracing(Span span) {
        if(Objects.nonNull(span)) {
            populateMDCTracing(span.context().toTraceId(),span.context().toSpanId());
        }
    }
    private static void closeActiveSpan(){
        if(getTracer().activeSpan()!=null){
            getTracer().activeSpan().finish();
        }
    }
    public static void startNewSpanWithMDCTracing(FunctionData functionData){
        if(isTracerEnabled()){
            Span span = TracingHandler.startSpan(TracerUtil.getTracer(), functionData, "");
            getTracer().activateSpan(span);
            TracerUtil.populateMDCTracing(span);
        }
    }

    public static boolean isTraceIDPresentIn(Map<String, Object> properties){
        if(properties!=null && !properties.isEmpty()){
            if(properties.containsKey(TRACE_ID) &&
                    properties.containsKey(SPAN_ID)){
                return true;
            }
        }
        return false;
    }

    public static void populateTracingFrom(Map<String, Object> properties){
        if(isTraceIDPresentIn(properties)){
            populateMDCTracing(String.valueOf(properties.get(TRACE_ID)),
                    String.valueOf(properties.get(SPAN_ID)));
        }
    }

    public static SpanContext buildSpanFromHeaders(Tracer tracer) {
        if(isTracePresent()){
            Map<String, String> headers = new HashMap<>();
            headers.put(Constants.X_B3_TRACE_ID.toLowerCase(), TracerUtil.getMDCTraceId());
            headers.put(Constants.X_B3_SPAN_ID.toLowerCase(), TracerUtil.getMDCSpanId());
            headers.put(Constants.X_B3_PARENT_SPAN_ID.toLowerCase(), TracerUtil.getMDCSpanId());
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
}
