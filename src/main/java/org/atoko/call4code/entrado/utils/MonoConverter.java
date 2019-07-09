package org.atoko.call4code.entrado.utils;

import reactor.core.publisher.Mono;
import scala.compat.java8.FutureConverters;
import scala.concurrent.Future;

public class MonoConverter {
    static public <T> Mono<T> toMono(Future<T> future) {
        return Mono.fromFuture(FutureConverters.toJava(future).toCompletableFuture());
    }
}
