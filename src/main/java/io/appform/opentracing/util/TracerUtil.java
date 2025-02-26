package io.appform.opentracing.util;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import com.rabbitmq.client.AMQP;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;

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
    public static boolean isTraceIDPresentInQueueMessage(AMQP.BasicProperties properties){

        if(properties.getHeaders()!=null && !properties.getHeaders().isEmpty()){
            if(properties.getHeaders().containsKey(TracerUtil.TRACE_ID) &&
                    properties.getHeaders().containsKey(TracerUtil.SPAN_ID)){
                return true;
            }
        }
        return false;
    }
    public static void populateTraceIDInQueueMessage(AMQP.BasicProperties properties){
        if(properties.getHeaders()!=null) {
            properties.getHeaders().put(TracerUtil.TRACE_ID, TracerUtil.getMDCTraceId());
            properties.getHeaders().put(TracerUtil.SPAN_ID, TracerUtil.getMDCSpanId());
        }
    }

    public void populateTracingFromQueue(AMQP.BasicProperties properties){
        if(isTraceIDPresentInQueueMessage(properties)){
            populateMDCTracing(String.valueOf(properties.getHeaders().get(TracerUtil.TRACE_ID)),
                    String.valueOf(properties.getHeaders().get(TracerUtil.SPAN_ID)));
        }
    }
}
