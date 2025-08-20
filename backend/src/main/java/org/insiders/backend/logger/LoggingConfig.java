package org.insiders.backend.logger;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;

@Configuration
public class LoggingConfig implements InitializingBean, DisposableBean {

    @Override
    public void afterPropertiesSet() {
        AsyncLogManager logManager = AsyncLogManager.getInstance();
        logManager.addLogger(new ConsoleLogger());
        logManager.addLogger(new FileLogger("application.log"));

        logManager.log("INFO", "Application starting up");
    }

    @Override
    public void destroy() {
        AsyncLogManager logManager = AsyncLogManager.getInstance();
        logManager.log("INFO", "Application shutting down");
        logManager.shutdown();
    }
}