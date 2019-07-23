package org.atoko.call4code.entrado.controller.views.store;

import org.atoko.call4code.entrado.controller.api.activity.ActivityController;
import org.atoko.call4code.entrado.exception.FrontendException;
import org.atoko.call4code.entrado.model.details.ActivityDetails;
import org.atoko.call4code.entrado.service.meta.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.Duration;

@ControllerAdvice
public class ActivityStore {

    public static final String ACTIVITY_CURRENT = "__activity_current";
    @Autowired
    private DeviceService deviceService;

    @Autowired
    private ActivityController activityController;

    static public void setCurrentActivity(WebSession session, ActivityDetails activityDetails) {
        session.getAttributes().put(ACTIVITY_CURRENT, activityDetails);
    }

    static private ActivityDetails getCurrentActivity(WebSession session) {
        return (ActivityDetails) session.getAttributes().get(ACTIVITY_CURRENT);
    }

    @ModelAttribute(ACTIVITY_CURRENT)
    private Mono<ActivityDetails> currentActivity(
            Principal principal,
            WebSession session,
            ServerWebExchange serverWebExchange
    ) {
        ActivityDetails activityDetails = getCurrentActivity(session);
        Boolean isActivityPath = serverWebExchange.getRequest().getPath().value().contains("/www/activity");
        return Mono.delay(Duration.ofMillis(2l)).flatMap((qq) -> {
            if (principal != null && !isActivityPath && activityDetails == null) {
                throw new FrontendException(
                        new RuntimeException("No activity set"),
                        "/www/activity?ex=NO_ACTIVITY_SELECTED"
                );
            }

            return Mono.justOrEmpty(activityDetails);
        });
    }
}
