package io.appform.opentracing;

import brave.Tracing;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.opentracing.BraveSpan;
import brave.opentracing.BraveTracer;
import brave.propagation.TraceContext;
import brave.sampler.Sampler;
import io.appform.opentracing.util.TracerUtil;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.noop.NoopScopeManager;
import io.opentracing.noop.NoopSpan;
import io.opentracing.util.GlobalTracer;
import io.opentracing.util.ThreadLocalScope;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases related to TracingHandler
 */
class TracingHandlerTest {

    private static MockTracer mockTracer = new MockTracer();

    @BeforeEach
    void setup() {
        GlobalTracer.registerIfAbsent(mockTracer);
    }

    @AfterEach
    void cleanup() {
        mockTracer.reset();
    }

    @Test
    void testGetTracer() {
        Tracer tracer = TracingHandler.getTracer();
        Assertions.assertNotNull(tracer);
    }

    @Test
    void testStartSpan() {
        TagCapturingHandler tagCapturingHandler = new TagCapturingHandler();
        Tracing tracing = Tracing.newBuilder()
                .sampler(Sampler.ALWAYS_SAMPLE)
                .addSpanHandler(tagCapturingHandler) // Register the custom handler
                .build();
        Tracer tracer = TracerUtil.getTracer(tracing);

        final String methodName = "test";
        final String className = "testClass";

//        brave.Tracer tracer = tracing.tracer();
        Span span = TracingHandler.startSpan(new FunctionData(className, methodName), "test");
        span.setTag("http.url", "http://example.com/api");
        span.setTag("http.method", "GET");
        span.finish();
        tracing.close();
//        Span span = TracingHandler.startSpan(new FunctionData(className, methodName), "test");
//        Assertions.assertNotNull(span);
//        Assertions.assertTrue(span instanceof BraveSpan);
//        SpanContext tags = span.context();
        System.out.println(tagCapturingHandler.getTags());
//        Assertions.assertEquals(methodName, tags.get(TracingConstants.METHOD_NAME_TAG));
//        Assertions.assertEquals(className, tags.get(TracingConstants.CLASS_NAME_TAG));
//        Assertions.assertEquals("test", tags.get(TracingConstants.PARAMETER_STRING_TAG));
    }

    @Test
    void testStartScope() {
        mockTracer.activateSpan(mockTracer.buildSpan("test").start());
        Assertions.assertNull(TracingHandler.startScope(null, NoopSpan.INSTANCE));
        Assertions.assertNull(TracingHandler.startScope(GlobalTracer.get(), null));
        Scope scope = TracingHandler.startScope(GlobalTracer.get(), mockTracer.activeSpan());
        Assertions.assertNotNull(scope);
        Assertions.assertTrue(scope instanceof ThreadLocalScope);
    }

    @Test
    void testAddSuccessTagToSpan() {
        mockTracer.activateSpan(mockTracer.buildSpan("test").start());
        Assertions.assertDoesNotThrow(() -> TracingHandler.addSuccessTagToSpan(null));
        Assertions.assertDoesNotThrow(() -> TracingHandler.addSuccessTagToSpan(mockTracer.activeSpan()));
        Map<String, Object> tags = ((MockSpan) mockTracer.activeSpan()).tags();
        Assertions.assertEquals("SUCCESS", tags.get(TracingConstants.METHOD_STATUS_TAG));
    }

    @Test
    void testAddErrorTagToSpan() {
        mockTracer.activateSpan(mockTracer.buildSpan("test").start());
        Assertions.assertDoesNotThrow(() -> TracingHandler.addErrorTagToSpan(null));
        Assertions.assertDoesNotThrow(() -> TracingHandler.addErrorTagToSpan(mockTracer.activeSpan()));
        Map<String, Object> tags = ((MockSpan) mockTracer.activeSpan()).tags();
        Assertions.assertEquals("FAILURE", tags.get(TracingConstants.METHOD_STATUS_TAG));
    }

    @Test
    void testCloseSpanAndScope() {
        Assertions.assertDoesNotThrow(() -> TracingHandler.closeSpanAndScope(null, null));
        Assertions.assertDoesNotThrow(() -> TracingHandler.closeSpanAndScope(null, NoopScopeManager.NoopScope.INSTANCE));
        Assertions.assertDoesNotThrow(() -> TracingHandler.closeSpanAndScope(NoopSpan.INSTANCE, null));
        Assertions.assertDoesNotThrow(() -> TracingHandler.closeSpanAndScope(NoopSpan.INSTANCE, NoopScopeManager.NoopScope.INSTANCE));
    }

    static class TagCapturingHandler extends SpanHandler {

        Map<String,String> localTagMap = new HashMap<>();
        @Override
        public boolean end(TraceContext context, MutableSpan span, Cause cause) {
            Map<String, String> tags = span.tags();
            System.out.println("Captured Tags: " + tags);
            this.localTagMap.putAll(tags);
            return true;
        }
        public Map<String,String> getTags(){
            return localTagMap;
        }
    }
}
