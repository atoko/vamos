package org.atoko.call4code.entrado.service.meta;

import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.atoko.call4code.entrado.actors.meta.DeviceSupervisor;
import org.atoko.call4code.entrado.actors.meta.GenesisBehavior;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ActorSystemService {
    private ActorSystem actorSystem;
    private ClusterSharding clusterSharding;
    private DeviceService deviceService;

    public ActorSystemService(@Autowired DeviceService deviceService) {
        Config completeConfig = ConfigFactory.load();
        this.actorSystem = ActorSystem.create(GenesisBehavior.main, "root-system", completeConfig);
        this.clusterSharding = ClusterSharding.get(actorSystem);
        this.deviceService = deviceService;

        clusterSharding.init(
                Entity.ofEventSourcedEntity(
                        DeviceSupervisor.entityTypeKey,
                        (context) -> new DeviceSupervisor(context.getEntityId(), context.getActorContext())
                )
        );

        get().tell(
                new DeviceSupervisor.GenesisCommand(deviceService.getDeviceId())
        );
    }

    @PostConstruct
    private void initializeShards() {

    }

    @Bean
    public ActorSystem actorSystem() {
        return actorSystem;
    }

    public long uptime() {
        return actorSystem.uptime();
    }

    public EntityRef get() {
        return child(DeviceSupervisor.entityTypeKey, DeviceSupervisor.getEntityId(deviceService.getDeviceId()));
    }

    public EntityRef child(EntityTypeKey entityTypeKey, String identifier) {
        return ClusterSharding.get(actorSystem).entityRefFor(entityTypeKey, identifier);
    }
}
