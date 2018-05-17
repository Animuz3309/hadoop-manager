package edu.scut.cs.hm.docker;

import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.common.mb.LazySubscriptions;
import edu.scut.cs.hm.docker.res.ResultCode;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.model.WithInterrupter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class DockerMethodSubscriber<E, A extends WithInterrupter> implements LazySubscriptions.Subscriber<E> {
    @Data
    public static class Builder<E, A extends WithInterrupter> {
        /**
         * String which identity resource, used for logging
         */
        private String id;
        private DockerService docker;
        private ExecutorService executorService;
        private Function<Consumer<E>, A> argument;
        private Function<A, ServiceCallResult> method;

        public Builder<E, A> argument(Function<Consumer<E>, A> argument) {
            setArgument(argument);
            return this;
        }

        public Builder<E, A> method(Function<A, ServiceCallResult> method) {
            setMethod(method);
            return this;
        }

        public Builder<E, A> id(String id) {
            setId(id);
            return this;
        }

        public Builder<E, A> docker(DockerService service) {
            setDocker(service);
            return this;
        }

        public Builder<E, A> executorService(ExecutorService executorService) {
            setExecutorService(executorService);
            return this;
        }

        public DockerMethodSubscriber<E, A> build() {
            return new DockerMethodSubscriber<>(this);
        }
    }

    private final String id;
    private final ExecutorService executorService;
    private final Function<Consumer<E>, A> argument;
    private final Function<A, ServiceCallResult> method;

    private DockerMethodSubscriber(Builder<E, A> b) {
        this.id = b.id;
        this.argument = b.argument;
        this.executorService = b.executorService;
        this.method = b.method;
    }

    public static <E, A extends WithInterrupter> Builder<E, A> builder() {
        return new Builder<E, A>();
    }

    @Override
    public Runnable subscribe(LazySubscriptions<E>.Context context) {
        A arg = argument.apply(context::accept);
        Future<ServiceCallResult> future = executorService.submit(() -> {
            //here we use sys auth because it shared between different users
            // may be we need to use different subscriptions for each users?
            try (TempAuth ta = TempAuth.asSystem()) {
                return method.apply(arg);
            } finally {
                context.close();
            }
        });
        try {
            ServiceCallResult result = future.get(2, TimeUnit.MILLISECONDS);
            if(result.getCode() != ResultCode.OK) {
                log.warn("Can not subscribe on id=\"{}\" due error {}: {}", id, result.getCode(), result.getMessage());
            }
        } catch (TimeoutException e) {
            //it is ok
        } catch (Exception e) {
            log.warn("Can not subscribe on id=\"{}\" due error", id, e);
        }
        return () -> {
            arg.getInterrupter().set(true);
            future.cancel(true);
        };
    }
}
