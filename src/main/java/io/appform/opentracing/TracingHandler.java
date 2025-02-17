package io.appform.opentracing;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import com.google.common.base.Strings;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                return BraveTracer.newBuilder(Tracing.newBuilder().build()).build();
            });
            if (tracer == null) {
                return null;
            }
            Span parentSpan = tracer.activeSpan();
            if (parentSpan == null) {
                parentSpan = GlobalTracer.get().buildSpan("rootSpan").start();
            }
            Span span = tracer.buildSpan("method:" + functionData.getMethodName())
                    .asChildOf(parentSpan)
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
}
