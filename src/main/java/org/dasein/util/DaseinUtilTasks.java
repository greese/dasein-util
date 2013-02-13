/**
 * Copyright (C) 1998-2013 enStratus Networks Inc
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.dasein.util;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DaseinUtilTasks {
    static private Logger logger = Logger.getLogger(DaseinUtilTasks.class);

    private static class DaemonThreadFactory implements ThreadFactory {
        private static final ThreadFactory threadFactory = Executors.defaultThreadFactory();
        public Thread newThread(Runnable r) {
            Thread t = threadFactory.newThread(r);
            t.setName("Dasein Util Task");
            t.setDaemon(true);
            return t;
        }
    }

    static private final ExecutorService executorService;
    static {
        executorService = Executors.newCachedThreadPool(new DaemonThreadFactory());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    executorService.shutdown();
                    boolean terminated = executorService.awaitTermination(100L, TimeUnit.MILLISECONDS);
                    if (!terminated) {
                        List<Runnable> pending = executorService.shutdownNow();
                        if (!pending.isEmpty()) {
                            logger.debug(pending.size() + " dasein-util tasks pending");
                        }
                    }
                } catch (Throwable t) {
                    logger.error("Problem shutting down dasein-util task pool: " + t.getMessage());
                }
            }
        });
        logger.debug("Created dasein-util task pool");
    }

    static public void shutdown() {
        logger.debug("Shutting down dasein-util task pool");
        executorService.shutdown();
    }

    static public List<Runnable> shutdownNow() {
        logger.debug("Forcing dasein-util task pool shutdown");
        return executorService.shutdownNow();
    }

    static public boolean isShutdown() {
        return executorService.isShutdown();
    }

    static public boolean isTerminated() {
        return executorService.isTerminated();
    }

    static public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    static public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(task);
    }

    static public <T> Future<T> submit(Runnable task, T result) {
        return executorService.submit(task, result);
    }

    static public Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }

    static public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executorService.invokeAll(tasks);
    }

    static public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.invokeAll(tasks, timeout, unit);
    }

    static public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executorService.invokeAny(tasks);
    }

    static public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return executorService.invokeAny(tasks, timeout, unit);
    }

    static public void execute(Runnable command) {
        executorService.execute(command);
    }
}
