package org.atoko.call4code.entrado.controller.views.store;

import org.atoko.call4code.entrado.controller.api.activity.ActivityController;
import org.atoko.call4code.entrado.model.details.ActivityDetails;
import org.atoko.call4code.entrado.service.meta.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.WebSession;

@ControllerAdvice
public class ActivityStore {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private ActivityController activityController;

    @ModelAttribute("__activity_current")
    private ActivityDetails currentActivity(WebSession session) {
        ActivityDetails activityDetails = getCurrentActivity(session);
        return activityDetails;
    }
    static public void setCurrentActivity(WebSession session, ActivityDetails activityDetails) {
        session.getAttributes().put("_activity_current", activityDetails);
    }

    static private ActivityDetails getCurrentActivity(WebSession session) {
        return (ActivityDetails)session.getAttributes().get("_activity_current");
    }
}
