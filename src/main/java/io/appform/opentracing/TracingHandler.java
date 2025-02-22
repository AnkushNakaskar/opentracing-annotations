package io.appform.opentracing;

import brave.Tracing;
import brave.opentracing.BraveSpan;
import brave.opentracing.BraveSpanBuilder;
import brave.opentracing.BraveSpanContext;
import brave.opentracing.BraveTracer;
import brave.propagation.*;
import com.fasterxml.jackson.databind.util.ArrayIterator;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopSpan;
import io.opentracing.noop.NoopSpanBuilder;
import io.opentracing.noop.NoopSpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility class that handles all span and scope related operations
 */
public class TracingHandler {

    private static final Logger log = LoggerFactory.getLogger(TracingHandler.class.getSimpleName());

    static Tracer getTracer() {
        try {
            return GlobalTracer.get();
        } catch (Exception e) {
            log.error("Error while getting tracer", e);
            return null;
        }
    }

    static Span startSpan(final Tracer tracer,
                          final FunctionData functionData,
                          final String parameterString) {
        try {
            GlobalTracer.registerIfAbsent(()->{
                log.info("Tracer is absent");
                return BraveTracer.newBuilder(Tracing.newBuilder().build()).build();
            });
            if (tracer == null) {
                return null;
            }

            SpanContext parentSpanContext = getParentSpanContext();
            Span span = tracer.buildSpan("method:" + functionData.getMethodName())
                    .asChildOf(parentSpanContext)
                    .withTag(TracingConstants.CLASS_NAME_TAG, functionData.getClassName())
                    .withTag(TracingConstants.METHOD_NAME_TAG, functionData.getMethodName())
                    .start();
            if (!Strings.isNullOrEmpty(parameterString)) {
                span.setTag(TracingConstants.PARAMETER_STRING_TAG, parameterString);
            }
            return span;
        } catch (Exception e) {
            log.error("Error while starting span", e);
            return null;
        }
    }

    private static SpanContext getParentSpanContext() {
        if (MDC.get("trace_id") == null) {
            return GlobalTracer.get().buildSpan("rootSpan").start().context();
        }
        return getSpan();


    }

    private static SpanContext getSpan() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-b3-traceid", MDC.get("trace_id"));
        headers.put("x-b3-spanid", MDC.get("span_id"));

        TextMap textMap =new TextMap() {
            @Override
            public Iterator<Map.Entry<String, String>> iterator() {
                return new TextMapExtractAdapter(headers).iterator();
            }
            @Override
            public void put(String s, String s1) {
            }
        };
        SpanContext spanContext = GlobalTracer.get().extract(Format.Builtin.TEXT_MAP, textMap);
        BraveSpanContext braveContext = BraveTracer.newBuilder(Tracing.newBuilder().build()).build().extract(Format.Builtin.TEXT_MAP, textMap);
        return braveContext;
    }

    static Span initialisedParentSpan(final Tracer tracer){
        GlobalTracer.registerIfAbsent(()->{
            return BraveTracer.newBuilder(Tracing.newBuilder().build()).build();
        });

        Span parentSpan = tracer.activeSpan();
        if (parentSpan == null) {
            parentSpan = GlobalTracer.get().buildSpan("rootSpan").start();
        }
        return parentSpan;
    }


    static Scope startScope(final Tracer tracer,
                            final Span span) {
        try {
            if (tracer == null || span == null) {
                return null;
            }
            return tracer.activateSpan(span);
        } catch (Exception e) {
            log.error("Error while starting scope", e);
            return null;
        }
    }

    static void addSuccessTagToSpan(final Span span) {
        try {
            if (span == null) {
                return;
            }
            addStatusTag("SUCCESS", span);
        } catch (Exception e) {
            log.error("Error while adding success tag to span", e);
        }
    }


    static void addErrorTagToSpan(final Span span) {
        try {
            if (span == null) {
                return;
            }
            addStatusTag("FAILURE", span);
        } catch (Exception e) {
            log.error("Error while adding failure tag to span", e);
        }
    }

    static void closeSpanAndScope(final Span span,
                                  final Scope scope) {
        try {
            if (scope != null) {
                scope.close();
            }
            if (span != null) {
                span.finish();
            }
        } catch (Exception e) {
            log.error("Error while closing span/scope", e);
        }
    }

    private static void addStatusTag(final String status,
                                     final Span span) {
        span.setTag(TracingConstants.METHOD_STATUS_TAG, status);
    }

    private static BraveSpanContext buildSpanFromHeaders(BraveTracer tracer) {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-b3-traceid", "6a2c4affac4d0296");
        headers.put("x-b3-spanid", "48f62d4b2eaf8fe0");
        headers.put("x-b3-parentspanid", "48f62d4b2eaf8fe0");
        return tracer.extract(Format.Builtin.TEXT_MAP, new TextMapAdapter(headers));
    }
    public static void main(String[] args) {
//        GlobalTracer.registerIfAbsent(() -> {
//            log.info("Tracer is absent");
//            return BraveTracer.newBuilder(Tracing.newBuilder().build()).build();
//        });

        BraveTracer tracer = BraveTracer.newBuilder(Tracing.newBuilder().build()).build();
        Span span =tracer.buildSpan("method").asChildOf(buildSpanFromHeaders(tracer)).start();


        System.out.println(span.context().toTraceId());
        System.out.println(span.context().toSpanId());
    }



}
