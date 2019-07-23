package org.atoko.call4code.entrado.utils;

import reactor.core.publisher.Mono;
import scala.compat.java8.FutureConverters;
import scala.concurrent.Future;

import java.util.concurrent.CompletionStage;

public class MonoConverter {
    static public <T> Mono<T> toMono(Future<T> future) {
        return Mono.fromFuture(FutureConverters.toJava(future).toCompletableFuture());
    }

    static public <T> Mono<T> toMono(CompletionStage<T> completionStage) {
        return Mono.fromCompletionStage(completionStage);
    }
}
