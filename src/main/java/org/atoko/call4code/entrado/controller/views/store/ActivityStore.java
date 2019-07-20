package org.atoko.call4code.entrado.controller.views.store;

import org.atoko.call4code.entrado.controller.api.activity.ActivityController;
import org.atoko.call4code.entrado.exception.FrontendException;
import org.atoko.call4code.entrado.model.details.ActivityDetails;
import org.atoko.call4code.entrado.service.meta.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;

import java.security.Principal;

@ControllerAdvice
public class ActivityStore {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private ActivityController activityController;

    @ModelAttribute("__activity_current")
    private ActivityDetails currentActivity(
            Principal principal,
            WebSession session,
            ServerWebExchange serverWebExchange
    ) {
        ActivityDetails activityDetails = getCurrentActivity(session);
        Boolean isActivityPath = serverWebExchange.getRequest().getPath().value().contains("/www/activity");
        if (principal != null && !isActivityPath && activityDetails == null) {
            throw new FrontendException(
                    new RuntimeException("No activity set"),
                    "/www/activity?from=NO_ACTIVITY_SELECTED"
            );
        }
        return activityDetails;
    }
    static public void setCurrentActivity(WebSession session, ActivityDetails activityDetails) {
        session.getAttributes().put("_activity_current", activityDetails);
    }

    static private ActivityDetails getCurrentActivity(WebSession session) {
        return (ActivityDetails)session.getAttributes().get("_activity_current");
    }
}
