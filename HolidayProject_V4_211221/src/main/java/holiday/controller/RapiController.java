package holiday.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import holiday.entity.Event;
import holiday.entity.User;
import holiday.services.EventService;
import holiday.services.EventsDatesService;
import holiday.services.UserService;

@CrossOrigin("*")
@RestController
public class RapiController {

	private UserService userService;
	private EventService eventService;
	private EventsDatesService eventsDatesService;

	@Autowired
	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Autowired
	public void setEventsDatesService(EventsDatesService eventsDatesService) {
		this.eventsDatesService = eventsDatesService;
	}

	@PostMapping(value = "/postdata", consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<String> postData(@RequestBody List<Event> rEvents, Authentication authentication)
			throws JsonProcessingException {

		User authUser = userService.findByEmail(authentication.getName());
		Long authUserId = authUser.getId();

		List<Event> originalEvents = eventService.getUserEvents(authUserId);

		Integer newEventCounter = 0;
		for (int i = 0; originalEvents.size() > i; i++) {
			Event oevent = originalEvents.get(i);
			Boolean isExist = false;
			for (int j = 0; rEvents.size() > j; j++) {
				Event event = rEvents.get(j);
				try {
					if (oevent.getId().intValue() == event.getId().intValue()) { // olyan d??tum, ami szerepelt az eredeti list??ban is
						if (oevent.getDuration() != event.getDuration()) { // a szabads??g id??tartama megv??ltozott,
																			// fel??l??rjuk
							oevent.setDuration(event.getDuration());
							eventService.addNewEvent(authUser, event);
							newEventCounter++;
						}
						isExist = true;
						break;
					}
				} catch (NullPointerException e) {
					// arra az elk??pzelhetetlen esetre, ha avalamelyik id null lenne 
				}
			}
			if (!isExist) { // ez a d??tum l??tezik az eredetiben, de az ??j list??ban nem szerepel, teh??t
							// t??r??lt??k
				eventService.deleteEvent(oevent); // t??r??lt szabads??g, t??rl??se a DB-b??l is
			}
		}
		for (int j = 0; rEvents.size() > j; j++) {
			Event event = rEvents.get(j);
			if (event.getId() == -1) { // ??j szabads??gnap, mentj??k
				event.setId(null);
				eventService.addNewEvent(authUser, event);
				newEventCounter++;
			}
		}

		// ha van ??j/m??dos??tott szabads??gig??ny ??s van j??v??hagy?? , levelet ??ld??nk a
		// j??v??hagy??nak
		if (newEventCounter > 0 && authUser.getApproverId() != null && authUser.getApproverId() >= 0)
			eventService.sendEmailFromNewEvent(authUser, newEventCounter);

		return ResponseEntity.status(HttpStatus.OK).body(toJson("OK"));
	}

//	@PostMapping("/getdata")
//	public ResponseEntity<String> getData(@RequestBody String selector, Authentication authentication)
//			throws JsonProcessingException {
//		System.out.println(selector);
//		User authUser = userService.findByEmail(authentication.getName());
//		Long authUserId = authUser.getId();
//
//		switch (selector) {
//		case "leaveCounter": // m??r kivett szabik sz??ma
//			return ResponseEntity.status(HttpStatus.OK).body(toJson(eventService.getUserSumLeave(authUserId)));
//		case "sumLeave": // a teljes szabads??gkeret
//			return ResponseEntity.status(HttpStatus.OK).body(toJson(eventService.getSumLeaveDay(authUser)));
//		case "eventList": // szabads??gok
//			return ResponseEntity.status(HttpStatus.OK).body(toJson(eventService.getUserEvents(authUserId)));
//		case "exEventList": // kiv??telnapok
//			return ResponseEntity.status(HttpStatus.OK).body(toJson(eventsDatesService.getAllEvents()));
//
//		}
//
//		return ResponseEntity.status(HttpStatus.OK).body(toJson(null));
//
//	}

	private String toJson(Object ans) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(ans);
	}

}
