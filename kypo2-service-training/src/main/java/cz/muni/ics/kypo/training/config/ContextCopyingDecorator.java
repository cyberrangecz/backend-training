package cz.muni.ics.kypo.training.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author Pavel Seda
 */
public class ContextCopyingDecorator implements TaskDecorator {

    @Nonnull
    @Override
    public Runnable decorate(@Nonnull Runnable runnable) {
        RequestAttributes context =
                RequestContextHolder.currentRequestAttributes();
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            try {
                RequestContextHolder.setRequestAttributes(context);
                MDC.setContextMap(contextMap);
                runnable.run();
            } finally {
                MDC.clear();
                RequestContextHolder.resetRequestAttributes();
            }
        };
    }

}
