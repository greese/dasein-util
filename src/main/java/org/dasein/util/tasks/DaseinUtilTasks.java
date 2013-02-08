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

package org.dasein.util.tasks;

import org.apache.log4j.Logger;
import org.dasein.util.DaseinUtilProperties;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DaseinUtilTasks {
    static private Logger logger = Logger.getLogger(DaseinUtilTasks.class);

    static private final ExecutorService executorService;
    static {
        if (DaseinUtilProperties.isTaskSystemEnabled()) {
            executorService = Executors.newCachedThreadPool();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        executorService.shutdown();
                        executorService.awaitTermination(100L, TimeUnit.MILLISECONDS);
                        List<Runnable> pending = executorService.shutdownNow();
                        if (!pending.isEmpty()) {
                            logger.debug(pending.size() + " dasein-util tasks pending");
                        }
                    } catch (Throwable t) {
                        logger.error("Problem shutting down dasein-util task pool: " + t.getMessage());
                    }
                }
            });
            logger.debug("Created dasein-util task pool");
        } else {
            executorService = null;
        }
    }

    static public void shutdown() {
        check();
        logger.info("Shutting down dasein-util task pool");
        executorService.shutdown();
    }

    static public List<Runnable> shutdownNow() {
        check();
        logger.info("Forcing dasein-util task pool shutdown");
        return executorService.shutdownNow();
    }

    static public boolean isShutdown() {
        check();
        return executorService.isShutdown();
    }

    static public boolean isTerminated() {
        check();
        return executorService.isTerminated();
    }

    static public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        check();
        return executorService.awaitTermination(timeout, unit);
    }

    static public <T> Future<T> submit(Callable<T> task) {
        check();
        return executorService.submit(task);
    }

    static public <T> Future<T> submit(Runnable task, T result) {
        check();
        return executorService.submit(task, result);
    }

    static public Future<?> submit(Runnable task) {
        check();
        return executorService.submit(task);
    }

    static public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        check();
        return executorService.invokeAll(tasks);
    }

    static public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        check();
        return executorService.invokeAll(tasks, timeout, unit);
    }

    static public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        check();
        return executorService.invokeAny(tasks);
    }

    static public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        check();
        return executorService.invokeAny(tasks, timeout, unit);
    }

    static public void execute(Runnable command) {
        check();
        executorService.execute(command);
    }

    static private void check() {
        if (executorService == null) {
            throw new RuntimeException("Illegal use of " + DaseinUtilTasks.class.getSimpleName() + ": not enabled");
        }
    }
}
