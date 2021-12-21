package holiday.services;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import holiday.entity.AppPojoList;
import holiday.entity.ApprovePojo;
import holiday.entity.Event;
import holiday.entity.EventDates;
import holiday.entity.User;
import holiday.entity.UserLeaves;
import holiday.repository.EventRepository;
import holiday.repository.UserRepository;

@Service
public class EventService {

//	private final static String USER_ROLE = "${roles.USER_ROLE}";
//	private final static String ADMIN_ROLE = "${roles.ADMIN_ROLE}";
//	private final static String HR_ROLE = "${roles.HR_ROLE}";
//	private final static String APPROVER_ROLE = "${roles.APPROVER_ROLE}";

	private EventsDatesService eventsDatesService;
	private LeaveService leaveService;
	private UserRepository userRepo;
	private EventRepository eventRepo;
	private ExecutorService emailExecutor = Executors.newFixedThreadPool(10);
	private JavaMailSender javaMailSender;

	@Autowired
	public void setJavaMailSender(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	@Autowired
	public void setEventsDatesService(EventsDatesService eventsDatesService) {
		this.eventsDatesService = eventsDatesService;
	}

	@Autowired
	public void setLeaveService(LeaveService leaveService) {
		this.leaveService = leaveService;
	}

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	public void setUserRepo(UserRepository userRepo) {
		this.userRepo = userRepo;
	}

	@Autowired
	public void setEventRepo(EventRepository eventRepo) {
		this.eventRepo = eventRepo;
	}

	public List<Event> getEvents() {

		return eventRepo.findAll();
	}

	public Event findByUserIdByEventId(Long userId, Long eventId) {

		return eventRepo.findByUserIdAndId(userId, eventId);
	}

	public Page<Event> findAllByUserIdOrderByStartDate(Long authUserId, Pageable pageable) {

		return eventRepo.findAllByUserIdOrderByStartDate(authUserId, pageable);
	}

	public List<Event> getUserEvents(long id) {

		return eventRepo.findAllByUserIdOrderByStartDate(id);
	}

//	public EventListWrapper getWrapUserEvents(long id) {
//
//		List<Event> sList = eventRepo.findAllByUserIdOrderByStartDate(id);
//		EventListWrapper rList=new EventListWrapper();
//		
//		sList.forEach(s -> rList.add(s));
//		return rList;
//	}

	public void deleteEvent(Event event) {
		eventRepo.delete(event);
	}

	public void addNewEvent(User user, Event event) {

		event.setUser(user);

		// ha nincs a userhez "jóváhagyó" rendelve, a szabadság kérelem jóváhagyásra
		// kerül automatikusan, egyébként 0(jóváhagyásra vár)

		if (user.getApproverId() != null && user.getApproverId() >= 0) {
			event.setApproved((byte) 0);
		} else
			event.setApproved((byte) 1);

		saveEvent(event);
	}

	public void sendEmailFromNewEvent(User user, Integer counter) {
		User approverUser = userRepo.findFirstById(user.getApproverId());
		String messageSubject = "Értesítés új szabadság jóváhagyási kérésről";
		String messageText = "Kérelmező: " + user.getName() + " (" + user.getEmail() + ") \n A kérelemben összesen  "
				+ counter + " munkanap szerepel.";
		emailExecutor.execute(new EmailService(approverUser, messageSubject, messageText, javaMailSender));
	}

	public void saveEvent(Event event) {

		eventRepo.save(event);

	}

	public Double getUserSumLeave(Long userId, int actYear) { // a tervezett és jóváhagyott szabik száma (az elutasított
																// nem)

		Double sumLeave = 0D;
		List<Event> events = getUserEvents(userId);
		for (Event event : events) {
			if (event.getApproved() >= 0 && event.getStartDate().getYear() == actYear)
				sumLeave = sumLeave + ((event.getDuration() == 1) ? 1 : 0.5);
		}
		return sumLeave;
	}

	public Integer getSumLeaveDay(User actUser) { // a teljes szabadságkeret

//		Integer bl = 0; // alapszabi
		Integer sumFrames;
		Integer thisYear = Year.now().getValue();
		UserLeaves actUserLeaves = leaveService.getUserLeavesByYear(thisYear, actUser);
		if (actUserLeaves == null) {
			sumFrames = 0;
		} else {
			sumFrames = actUserLeaves.getSumLeaveFrame();
		}
//		if (actUserLeaves.getBaseLeave() == null)
//			bl = 0;
//		else
//			bl = actUserLeaves.getBaseLeave();
//		Integer cl = 0; // tavalyról áthozott szabi
//		if (actUserLeaves.getCarriedLeave() == null)
//			cl = 0;
//		else
//			cl = actUserLeaves.getCarriedLeave();
//		Integer pl = 0; // szülői szabi
//		if (actUserLeaves.getParentalLeave() == null)
//			pl = 0;
//		else
//			pl = actUserLeaves.getParentalLeave();
//		Integer ol = 0; // egyéb szabi
//		if (actUserLeaves.getOtherLeave() == null)
//			ol = 0;
//		else
//			ol = actUserLeaves.getOtherLeave();
//		return bl + pl + cl + ol;

		return sumFrames;

	}

	public Boolean isThisWorkDay(LocalDate startDate, List<EventDates> exDays) {

		Boolean isWorkDay = true;
		if ((startDate.getDayOfWeek().getValue() == 6) || (startDate.getDayOfWeek().getValue() == 7))
			isWorkDay = false; // ha szombat vagy vasárnap, akkor alapesetben nem munkanap
		for (EventDates exDay : exDays) {
			if (exDay.getDate().compareTo(startDate) == 0)
				isWorkDay = exDay.getIsWorkDay(); // ha szerepel a kivételnapok közt, akkor a kivételnap típusa lesz
		}
		return isWorkDay;
	}

	public Map<LocalDate, Integer> googleEventTable(Long authUserId) {

		LocalDate startDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);
		Map<LocalDate, Integer> eventMap = new HashMap<>();
		List<EventDates> exDates = eventsDatesService.getAllEvents(LocalDate.now().getYear());

		// hétvégék és kivételnapok a táblába

		do {
			if (!isThisWorkDay(startDate, exDates)) {
				eventMap.put(startDate, 4); // nem munkanap
			}
			startDate = startDate.plusDays(1);
		} while (LocalDate.now().getYear() == startDate.getYear());

		// szabadságok táblába
		List<Event> leaveEventList = getUserEvents(authUserId);
		for (Event anevent : leaveEventList) {
			startDate = anevent.getStartDate();

			if (isThisWorkDay(startDate, exDates)) {// ha az adott szabadságnap munkanap, akkor táblába
				Integer result = ((anevent.getApproved() == -1) ? 0 : 1) * anevent.getDuration(); // ha elutasított
																									// kérelem, nem
																									// jelenítjük meg
				eventMap.put(startDate, result); // az adott dátumra eső szabadság (-7
													// egész nap, -14 délelőtt, -21 délután)
			}

		}

		return eventMap;
	}

	public AppPojoList findMyApprovList(Long authUserId) {

		AppPojoList approveList = new AppPojoList();
		// kikeressük azokat a Usereket, akinek az authUser a jóváhagyója
		List<User> userList = userRepo.findAllByApproverId(authUserId);

		userList.forEach(user -> {
			List<Event> events = user.getEvents(); // lekérjük a szabadságok listáját
			// List<Event> events= eventRepo.findByUserIdAndApprovedEquals(authUserId,0); //
			// szűrt Lista is lehet, de egyelőre lekérem mindet

			events.forEach(event -> {
				ApprovePojo aPojo = new ApprovePojo();
				if (event.getApproved() == null)
					event.setApproved((byte) 0);
				if (event.getApproved() == 0) { // ha jóváhagyásra vár, akkor listába vele
					aPojo.setUserId(user.getId());
					aPojo.setEventId(event.getId());
					aPojo.setUserName(user.getName());
					aPojo.setUserEmail(user.getEmail());
					aPojo.setStartDate(event.getStartDate());
					aPojo.setDuration(event.getDuration());
					aPojo.setApproved(event.getApproved());
					approveList.add(aPojo);
				}
			});
		});
		return approveList;
	}

//-------------------------------------------------------------------------------------------	

//	@PostConstruct
	public void init() throws ParseException {

		Event event;
		Integer thisYear = Year.now().getValue();

		// ---USER--------------------------------------------

		User user = new User("User", "user@user.com", passwordEncoder.encode("pass"), "USER", true);
		user.setStatus(true);
		userRepo.save(user);

//		var leaves = new UserLeaves(user, thisYear, 20, 0, 0, 0);
//		leaveService.saveLeave(leaves);
//
//		date1 = LocalDate.of(2021, 6, 9);
//
//		event = new Event(date1, user, (byte) (1), (byte) (1));
//		eventRepo.save(event);
//
//		date1 = LocalDate.of(2021, 9, 13);
//		event = new Event(date1, user, (byte) (1), (byte) (1));
//		eventRepo.save(event);

		// -----HR------------------------------------------

		user = new User("HR", "HR@hr.com", passwordEncoder.encode("pass"), "HR", true);
		user.setStatus(true);
		userRepo.save(user);
//
//		leaves = new UserLeaves(user, thisYear, 30, 4, 0, 0);
//		leaveService.saveLeave(leaves);
//
//		date1 = LocalDate.of(2021, 03, 11);
//
//		event = new Event(date1, user, (byte) (1), (byte) (1));
//		eventRepo.save(event);
//
//		date1 = LocalDate.of(2021, 6, 22);
//		event = new Event(date1, user, (byte) (1), (byte) (1));
//		eventRepo.save(event);

		// ------APPROVER-----------------------------------------

		user = new User("Approver", "approver@approver.com", passwordEncoder.encode("pass"), "APPROVER", true);
		user.setStatus(true);
		userRepo.save(user);
//
//		leaves = new UserLeaves(user, thisYear, 26, 2, 0, 0);
//		leaveService.saveLeave(leaves);
//
//		date1 = LocalDate.of(2021, 1, 11);
//
//		event = new Event(date1, user, (byte) (1), (byte) (1));
//		eventRepo.save(event);
//
//		date1 = LocalDate.of(2021, 12, 11);
//		event = new Event(date1, user, (byte) (1), (byte) (1));
//		eventRepo.save(event);

		// --ADMIN---------------------------------------------
		user = new User("Mezei Csaba", "mezeicsaba72@gmail.com", passwordEncoder.encode("pass"), "ADMIN", true);
		user.setStatus(true);
		userRepo.save(user);

		UserLeaves leaves = new UserLeaves(user, thisYear, 20, 3, 0, 0);
		leaveService.saveLeave(leaves);

		LocalDate date1 = LocalDate.of(2021, 12, 11);
		event = new Event(date1, user, (byte) (1), (byte) (1));
		eventRepo.save(event);
		// -----------------------------------------------

	}

}
