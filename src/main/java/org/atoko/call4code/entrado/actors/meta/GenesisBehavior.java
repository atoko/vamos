package org.atoko.call4code.entrado.actors.meta;

import akka.actor.typed.Behavior;
import akka.actor.typed.SpawnProtocol;
import akka.actor.typed.javadsl.Behaviors;

public abstract class GenesisBehavior {
    public static final Behavior<SpawnProtocol> main =
            Behaviors.setup(
                    context -> {
                        return Behaviors.receiveMessage(
                                message -> {
                                    return SpawnProtocol.behavior();
                                });
                    });

    private GenesisBehavior() {
    }
}
