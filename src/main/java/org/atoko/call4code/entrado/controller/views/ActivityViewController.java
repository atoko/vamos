package org.atoko.call4code.entrado.controller.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atoko.call4code.entrado.controller.api.activity.ActivityController;
import org.atoko.call4code.entrado.controller.api.activity.StationController;
import org.atoko.call4code.entrado.controller.api.person.PersonQueryController;
import org.atoko.call4code.entrado.controller.views.store.ActivityStore;
import org.atoko.call4code.entrado.model.details.ActivityDetails;
import org.atoko.call4code.entrado.model.details.ActivityStationDetails;
import org.atoko.call4code.entrado.model.details.PersonDetails;
import org.atoko.call4code.entrado.model.identifiers.PersonIdentifier;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static org.atoko.call4code.entrado.controller.views.store.AuthenticationStore.AUTHENTICATION_CURRENT;

@Controller
@RequestMapping("/www/activity")
@PreAuthorize("hasRole('ROLE_PERSON')")
public class ActivityViewController {

    static final byte[] encryptionKey = UUID.randomUUID().toString().getBytes();
    static Cipher encryptCipher;
    static Cipher decryptCypher;
    static private ObjectMapper objectMapper = new ObjectMapper();

    static {
        try {
            SecureRandom sr = new SecureRandom(encryptionKey);
            KeyGenerator kg = KeyGenerator.getInstance("RC4");
            kg.init(sr);
            SecretKey sk = kg.generateKey();
            // create an instance of encryptCipher
            encryptCipher = Cipher.getInstance("RC4");
            encryptCipher.init(Cipher.ENCRYPT_MODE, sk);


            decryptCypher = Cipher.getInstance("RC4");
            decryptCypher.init(Cipher.DECRYPT_MODE, sk);
        } catch (Exception e) {

        }
    }

    private static String encrypt(byte[] key) {
        try {
            return objectMapper.writeValueAsString(encryptCipher.doFinal(key));
        } catch (Throwable t) {
            LoggerFactory.getLogger("ACTIVITY_VIEW").error("Error creating crypted ID", t);
            return "";
        }
    }

    private static String decrypt(String encrypted) {
        try {
            byte[] bytes = objectMapper.readTree(encrypted).binaryValue();
            return new String(decryptCypher.doFinal(bytes));
        } catch (Throwable t) {
            LoggerFactory.getLogger("ACTIVITY_VIEW").error("Error reading crypted ID", t);
            return "";
        }
    }



    @Autowired
    private ActivityController activityController;

    @Autowired
    private StationController stationController;

    @Autowired
    private PersonQueryController personQueryController;

    @RequestMapping(value = {"", "/"})
    public Mono<String> index(Model model) {
        return activityController.get(null).map((getResponse) -> {
            List<ActivityDetails> activities = (List<ActivityDetails>) getResponse.getBody().get("data");
            model.addAttribute("_activity_activities", activities);
            return "www/activity/list";
        });
    }

    @RequestMapping(value = {"/details/{activityId}"})
    public Mono<String> index(
            @PathVariable String activityId,
            Model model,
            WebSession webSession
    ) {
        return activityController.get(activityId).flatMap((response) -> {
            ActivityDetails activityDetails = (ActivityDetails) response.getBody().get("data");
            String cryptedId = encrypt(activityDetails.activityId.getBytes());
            model.addAttribute("_activity_details", activityDetails);
            model.addAttribute("_activity_activityId$$", cryptedId);
            ActivityStore.setCurrentActivity(webSession, activityDetails);

            List<String> personIds = new ArrayList(activityDetails.personIds.stream().map((personIdentifier) -> {
                return personIdentifier.personId;
            }).collect(Collectors.toList()));

            return personQueryController.getPerson(personIds).map((people) -> {
                Map<String, PersonDetails> personMap = new HashMap<>();
                for (PersonDetails person : people.getBody().get("data")) {
                    personMap.put(person.personId, person);
                }
                model.addAttribute("_person_map", personMap);

                return "www/activity/details";
            });
        });
    }

    @RequestMapping(value = {"/create"})
    public Mono<String> create() {

        return Mono.just("www/activity/create/index");
    }

    @PostMapping("/create/post")
    public Mono<String> postCreate(
            ServerWebExchange webExchange,
            WebSession webSession
    ) {
        return webExchange.getFormData().flatMap((fd) -> {
            return activityController.post(fd.getFirst("name"))
                    .map((response) -> {
                        ActivityDetails activityDetails = response.getBody().get("data");
                        ActivityStore.setCurrentActivity(webSession, activityDetails);
                        return "redirect:/www/activity/details/" + activityDetails.getActivityId();
                    });
        });
    }

    @PostMapping("/join")
    public Mono<String> postJoin(
            ServerWebExchange webExchange,
            WebSession webSession,
            Model model
    ) {
        return Mono.zip((Mono<PersonDetails>)model.getAttribute(AUTHENTICATION_CURRENT), webExchange.getFormData())
            .flatMap((authAndForm) -> {
                PersonDetails personDetails = authAndForm.getT1();
                MultiValueMap<String, String> formData = authAndForm.getT2();
                String activityId = decrypt(formData.getFirst("_activityId$$"));

                return activityController.join(activityId, personDetails.personId).map((response) -> {
                    ActivityDetails activityDetails = response.getBody().get("data");
                    return "redirect:/www/activity/details/" + activityDetails.getActivityId();
                });
            });
    }

    @RequestMapping(value = {"/details/{activityId}/station/{stationId}"})
    public Mono<String> stationDetails(
            @PathVariable String activityId,
            @PathVariable String stationId,
            Model model,
            WebSession webSession
    ) {
        return activityController.get(activityId).flatMap((response) -> {
            ActivityDetails activityDetails = (ActivityDetails) response.getBody().get("data");

            model.addAttribute("_activity_details", activityDetails);
            if (activityDetails.stations.stream().anyMatch((station) -> station.stationId.equals(stationId))) {
                ActivityStationDetails activityStationDetails = activityDetails.stations.stream().filter(
                        (asd -> asd.stationId.equals(stationId))
                ).findFirst().get();
                model.addAttribute("_station_details", activityStationDetails);
                model.addAttribute(
                        "_activity_activityId$$",
                        encrypt(activityDetails.activityId.getBytes())
                );
                model.addAttribute(
                        "_station_stationId$$",
                        encrypt(activityStationDetails.stationId.getBytes())
                );

                List<String> personIds = new ArrayList(activityStationDetails.queue.stream().map((personIdentifier) -> {
                    return personIdentifier.personId;
                }).collect(Collectors.toList()));

                if (activityStationDetails.assignedPersonId != null) {
                    personIds.add(activityStationDetails.assignedPersonId.personId);
                }

                return personQueryController.getPerson(personIds).map((people) -> {
                    Map<String, PersonDetails> personMap = new HashMap<>();
                    for (PersonDetails person : people.getBody().get("data")) {
                        personMap.put(person.personId, person);
                    }

                    model.addAttribute(
                            "_person_map",
                            personMap
                    );
                    return "www/activity/station/details";
                });
            } else {
                return Mono.just("www/activity/details");
            }
        });
    }


    @RequestMapping(value = {"/details/{activityId}/station/create"})
    public Mono<String> createStation(
            @PathVariable String activityId,
            Model model
    ) {
        return activityController.get(activityId).map((activity) -> {
            ActivityDetails activityDetails = (ActivityDetails) activity.getBody().get("data");
            model.addAttribute("_activity_details", activityDetails);
            model.addAttribute(
                    "_activity_activityId$$",
                    encrypt(activityDetails.activityId.getBytes())
            );

            return "www/activity/station/create/index";
        });
    }


    @PostMapping("/details/station/create/post")
    public Mono<String> postStationCreate(
            ServerWebExchange webExchange,
            WebSession webSession
    ) {
        return webExchange.getFormData().flatMap((fd) -> {
            String activityId = decrypt(fd.getFirst("_activityId$$"));

            return stationController.post(activityId, fd.getFirst("name"))
                    .map((response) -> {
                        ActivityDetails activityStationDetails = response.getBody().get("data");
                        return "redirect:/www/activity/details/" + activityStationDetails.getActivityId();
                    });
        });

    }

    @PostMapping("/details/station/join")
    public Mono<String> postStationJoin(
            ServerWebExchange webExchange,
            WebSession webSession,
            Model model
    ) {
        return Mono.zip((Mono<PersonDetails>)model.getAttribute(AUTHENTICATION_CURRENT), webExchange.getFormData())
                .flatMap((authAndForm) -> {
            PersonDetails personDetails = authAndForm.getT1();
            MultiValueMap<String, String> formData = authAndForm.getT2();

            String activityId = decrypt(formData.getFirst("_activityId$$"));
            String stationId = decrypt(formData.getFirst("_stationId$$"));
            String personId = personDetails.personId;

            return stationController.join(activityId, stationId, personId)
                    .map((response) -> {
                        ActivityDetails activityStationDetails = response.getBody().get("data");
                        return "redirect:/www/activity/details/" + activityId + "/station/" + stationId;
                    });
        });
    }

    @PostMapping("/details/station/assign")
    public Mono<String> postStationAssign(
            ServerWebExchange webExchange,
            WebSession webSession,
            Model model
    ) {
        return Mono.zip((Mono<PersonDetails>)model.getAttribute(AUTHENTICATION_CURRENT), webExchange.getFormData())
                .flatMap((authAndForm) -> {
                    PersonDetails personDetails = authAndForm.getT1();
                    MultiValueMap<String, String> formData = authAndForm.getT2();

                    String activityId = decrypt(formData.getFirst("_activityId$$"));
                    String stationId = decrypt(formData.getFirst("_stationId$$"));
                    String personId = personDetails.personId;

                    return stationController.assign(activityId, stationId, personId)
                            .map((response) -> {
                                ActivityDetails activityStationDetails = response.getBody().get("data");
                                return "redirect:/www/activity/details/" + activityId + "/station/" + stationId;
                            });
                });
    }
}
