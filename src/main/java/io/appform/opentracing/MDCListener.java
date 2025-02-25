package io.appform.opentracing;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

/**
 * @author ankush.nakaskar
 */
public class MDCListener implements ServletRequestListener {
    public void requestInitialized(ServletRequestEvent e) {
    }

    public void requestDestroyed(ServletRequestEvent e) {
        TracerUtil.destroyTracingForRequest();
    }
}
