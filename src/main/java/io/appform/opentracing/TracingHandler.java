package io.appform.opentracing;

import brave.opentracing.BraveSpanContext;
import brave.opentracing.BraveTracer;
import com.google.common.base.Strings;
import io.appform.opentracing.util.TracerUtil;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class that handles all span and scope related operations
 */
public class TracingHandler {

    private static final Logger log = LoggerFactory.getLogger(TracingHandler.class.getSimpleName());

    static Tracer getTracer() {
        try {
            return TracerUtil.getTracer();
        } catch (Exception e) {
            log.error("Error while getting tracer", e);
            return null;
        }
    }

    public static Span startSpan(final FunctionData functionData,
                          final String parameterString) {
        try {
            Tracer tracer = TracerUtil.getTracer();
            SpanContext parentSpanContext = TracerUtil.buildSpanFromHeaders((BraveTracer) tracer);
            Span span = tracer.buildSpan("method:" + functionData.getMethodName())
                    .asChildOf(parentSpanContext)
                    .withTag(TracingConstants.CLASS_NAME_TAG, StringUtils.stripToEmpty(functionData.getClassName()))
                    .withTag(TracingConstants.METHOD_NAME_TAG, StringUtils.stripToEmpty(functionData.getMethodName()))
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
