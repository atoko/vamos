package org.atoko.call4code.entrado;

import akka.actor.ActorSystem;
import org.atoko.call4code.entrado.config.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EntradoApplication {
    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(EntradoApplication.class, args);
    }

    @Bean
    public ActorSystem actorSystem() {
        ActorSystem system = ActorSystem.create("akka-spring-demo");
        SpringExtension.SPRING_EXTENSION_PROVIDER.get(system).initialize(applicationContext);
        return system;
    }

}
