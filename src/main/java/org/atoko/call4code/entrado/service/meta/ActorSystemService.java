package org.atoko.call4code.entrado.service.meta;

import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.atoko.call4code.entrado.actors.activity.ActivityManager;
import org.atoko.call4code.entrado.actors.meta.DeviceSupervisor;
import org.atoko.call4code.entrado.actors.meta.GenesisBehavior;
import org.atoko.call4code.entrado.actors.person.PersonManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.function.Function;

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
    }

    private Map<EntityTypeKey, Function<EntityContext, Object>> shardManagers(String deviceId) {
        return Map.of(//DeviceSupervisor
                DeviceSupervisor.entityTypeKey(deviceId),
                (context) -> new DeviceSupervisor(deviceId, context.getEntityId(), context.getActorContext()),
                //PersonManager
                PersonManager.entityTypeKey(deviceId),
                (context) -> new PersonManager(deviceId, context.getEntityId(), context.getActorContext()),
                //ActivityManager
                ActivityManager.entityTypeKey(deviceId),
                (context) -> new ActivityManager(deviceId, context.getEntityId(), context.getActorContext())
        );
    }

    @PostConstruct
    private void initializeShards() {
        String deviceId = deviceService.getDeviceId();
        shardManagers(deviceId).forEach((typeKey, constructor) -> {

            clusterSharding.init(
                    Entity.ofEventSourcedEntity(
                            typeKey,
                            (context) -> (EventSourcedEntity<Object, Object, Object>) constructor.apply(context)
                    )
            );
        });


        get().tell(
                new DeviceSupervisor.GenesisCommand(deviceId)
        );
    }

    @Bean
    public ActorSystem actorSystem() {
        return actorSystem;
    }

    public long uptime() {
        return actorSystem.uptime();
    }

    private EntityRef shard;
    public EntityRef get() {
        String deviceId = deviceService.getDeviceId();
        if (shard == null) {
            shard = child(DeviceSupervisor.entityTypeKey(deviceId), DeviceSupervisor.getEntityId(deviceId));
        }

        return shard;
    }

    public EntityRef child(EntityTypeKey entityTypeKey, String identifier) {
        return clusterSharding.entityRefFor(entityTypeKey, identifier);
    }
}
