package de.evosec.fotilo;

import android.app.Application;

public class FotiloApplication extends Application {

    private static final Logger LOG =
            LoggerFactory.getLogger(EvoCamApplication.class);

    private Thread.UncaughtExceptionHandler defaultHandler;

    private final Thread.UncaughtExceptionHandler handler =
            new Thread.UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    LOG.error("Uncaught exception", ex);
                    defaultHandler.uncaughtException(thread, ex);
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }

}
