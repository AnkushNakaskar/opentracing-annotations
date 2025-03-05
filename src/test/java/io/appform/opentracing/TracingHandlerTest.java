package io.appform.opentracing;

import brave.Tracing;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.opentracing.BraveScope;
import brave.opentracing.BraveSpan;
import brave.propagation.TraceContext;
import brave.sampler.Sampler;
import io.appform.opentracing.util.TracerUtil;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopScopeManager;
import io.opentracing.noop.NoopSpan;
import io.opentracing.util.GlobalTracer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases related to TracingHandler
 */
class TracingHandlerTest {

    private TagCapturingHandler tagCapturingHandler;
    private Tracer tracer;

    private static final Map<String,String> localTagMap = new HashMap<>();


    @BeforeEach
    void setup() {
        localTagMap.clear();
        tagCapturingHandler = new TagCapturingHandler();
        Tracing tracing = Tracing.newBuilder()
                .sampler(Sampler.ALWAYS_SAMPLE)
                .addSpanHandler(tagCapturingHandler) // Register the custom handler
                .build();
         tracer = TracerUtil.getTracer(tracing);

    }

    @AfterEach
    void cleanup() {
        tracer.close();
        localTagMap.clear();
    }

    @Test
    void testGetTracer() {
        Tracer tracer = TracingHandler.getTracer();
        Assertions.assertNotNull(tracer);
    }

    @Test
    void testStartSpan() {

        final String methodName = "test";
        final String className = "testClass";

        Span span = TracingHandler.startSpan(new FunctionData(className, methodName), "test");
        span.finish();
        tracer.close();
        Assertions.assertNotNull(span);
        Assertions.assertTrue(span instanceof BraveSpan);
        System.out.println(tagCapturingHandler.getTags());
        Assertions.assertEquals(methodName, localTagMap.get(TracingConstants.METHOD_NAME_TAG));
        Assertions.assertEquals(className, localTagMap.get(TracingConstants.CLASS_NAME_TAG));
        Assertions.assertEquals("test", localTagMap.get(TracingConstants.PARAMETER_STRING_TAG));
    }

    @Test
    void testStartScope() {
        Assertions.assertNull(TracingHandler.startScope(NoopSpan.INSTANCE));
        Assertions.assertNull(TracingHandler.startScope( null));
        final String methodName = "test";
        final String className = "testClass";
        Span span = TracingHandler.startSpan(new FunctionData(className, methodName), "test");
        Scope scope = TracingHandler.startScope(span);
        Assertions.assertNotNull(scope);
        Assertions.assertTrue(scope instanceof BraveScope);
        span.finish();
        tracer.close();
    }

    @Test
    void testAddSuccessTagToSpan() {
        final String methodName = "test";
        final String className = "testClass";
        Span span = TracingHandler.startSpan(new FunctionData(className, methodName), "test");
        tracer.activateSpan(span);
        Assertions.assertDoesNotThrow(() -> TracingHandler.addSuccessTagToSpan(null));
        Assertions.assertDoesNotThrow(() -> TracingHandler.addSuccessTagToSpan(tracer.activeSpan()));
        span.finish();
        Assertions.assertEquals("SUCCESS", localTagMap.get(TracingConstants.METHOD_STATUS_TAG));
    }

    @Test
    void testAddErrorTagToSpan() {
        final String methodName = "test";
        final String className = "testClass";
        Span span = TracingHandler.startSpan(new FunctionData(className, methodName), "test");
        tracer.activateSpan(span);
        Assertions.assertDoesNotThrow(() -> TracingHandler.addErrorTagToSpan(null));
        Assertions.assertDoesNotThrow(() -> TracingHandler.addErrorTagToSpan(tracer.activeSpan()));
        span.finish();
        tracer.close();
        tagCapturingHandler.getTags().get(TracingConstants.METHOD_STATUS_TAG);
        Assertions.assertEquals("FAILURE", localTagMap.get(TracingConstants.METHOD_STATUS_TAG));
    }

    @Test
    void testCloseSpanAndScope() {
        Assertions.assertDoesNotThrow(() -> TracingHandler.closeSpanAndScope(null, null));
        Assertions.assertDoesNotThrow(() -> TracingHandler.closeSpanAndScope(null, NoopScopeManager.NoopScope.INSTANCE));
        Assertions.assertDoesNotThrow(() -> TracingHandler.closeSpanAndScope(NoopSpan.INSTANCE, null));
        Assertions.assertDoesNotThrow(() -> TracingHandler.closeSpanAndScope(NoopSpan.INSTANCE, NoopScopeManager.NoopScope.INSTANCE));
    }

    static class TagCapturingHandler extends SpanHandler {

        @Override
        public boolean end(TraceContext context, MutableSpan span, Cause cause) {
            Map<String, String> tags = span.tags();
            System.out.println("Captured Tags: " + tags);
            localTagMap.putAll(tags);
            return true;
        }
        public Map<String,String> getTags(){
            return localTagMap;
        }
    }
}
