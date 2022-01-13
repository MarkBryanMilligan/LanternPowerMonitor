package com.lanternsoftware.rules;

import com.lanternsoftware.dataaccess.currentmonitor.CurrentMonitorDao;
import com.lanternsoftware.dataaccess.currentmonitor.MongoCurrentMonitorDao;
import com.lanternsoftware.dataaccess.rules.MongoRulesDataAccess;
import com.lanternsoftware.dataaccess.rules.RulesDataAccess;
import com.lanternsoftware.datamodel.currentmonitor.Account;
import com.lanternsoftware.datamodel.rules.Action;
import com.lanternsoftware.datamodel.rules.ActionType;
import com.lanternsoftware.datamodel.rules.Criteria;
import com.lanternsoftware.datamodel.rules.Event;
import com.lanternsoftware.datamodel.rules.EventId;
import com.lanternsoftware.datamodel.rules.EventType;
import com.lanternsoftware.datamodel.rules.Rule;
import com.lanternsoftware.rules.actions.ActionImpl;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RulesEngine {
	protected static final Logger LOG = LoggerFactory.getLogger(RulesEngine.class);

	private static RulesEngine INSTANCE;
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final RulesDataAccess dao;
	private final CurrentMonitorDao cmDao;
	private final Map<ActionType, ActionImpl> actions = new HashMap<>();
	private final Map<Integer, EventTimeTask> timeTasks = new HashMap<>();
	private Timer timer;


	public static RulesEngine instance() {
		if (INSTANCE == null)
			INSTANCE = new RulesEngine();
		return INSTANCE;
	}

	public RulesEngine() {
		ServiceLoader.load(ActionImpl.class).forEach(_action->actions.put(_action.getType(), _action));
		dao = new MongoRulesDataAccess(MongoConfig.fromDisk(LanternFiles.OPS_PATH + "mongo.cfg"));
		cmDao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.OPS_PATH + "mongo.cfg"));
		timer = new Timer("RulesEngine Timer");
	}

	public void start() {
		for (String id : cmDao.getProxy().queryForField(Account.class, null, "_id")) {
			scheduleNextTimeEventForAccount(DaoSerializer.toInteger(id));
		}
	}

	public RulesDataAccess dao() {
		return dao;
	}

	public void fireEvent(Event _event) {
		if (_event.getType() != EventType.TIME)
			dao.putEvent(_event);
		executor.submit(()->{
			TimeZone tz = TimeZone.getTimeZone("America/Chicago"); //TODO: Get from the current monitor account
			List<Rule> rules = CollectionUtils.filter(dao.getRulesForAccount(_event.getAccountId()), _r->_r.triggers(_event));
			if (!rules.isEmpty()) {
				for (Rule rule : rules) {
					List<Event> events = CollectionUtils.asArrayList(_event);
					List<Criteria> critNeedingData = rule.getCriteriaNeedingData(_event);
					if (!critNeedingData.isEmpty()) {
						Set<EventId> eventsToGet = CollectionUtils.transformToSet(critNeedingData, Criteria::toEventId);
						for (EventId id : eventsToGet) {
							Event event = dao.getMostRecentEvent(_event.getAccountId(), id.getType(), id.getSourceId());
							if (event != null)
								events.add(event);
						}
					}
					if (rule.isMet(events, tz)) {
						for (Action action : CollectionUtils.makeNotNull(rule.getActions())) {
							ActionImpl impl = actions.get(action.getType());
							impl.invoke(rule, events, action);
						}
					}
				}
			}
		});
	}

	private void scheduleNextTimeEventForAccount(int _accountId) {
		TimeZone tz = TimeZone.getTimeZone("America/Chicago"); //TODO: Get from the current monitor account
		EventTimeTask nextTask = timeTasks.remove(_accountId);
		if (nextTask != null)
			nextTask.cancel();
		List<Rule> rules = CollectionUtils.filter(dao.getRulesForAccount(_accountId), _r->CollectionUtils.anyQualify(_r.getAllCriteria(), _c->_c.getType() == EventType.TIME));
		if (rules.isEmpty())
			return;
		Collection<Date> dates = CollectionUtils.aggregate(rules, _r->CollectionUtils.transform(_r.getAllCriteria(), _c->_c.getNextTriggerDate(tz)));
		Date nextDate = CollectionUtils.getSmallest(dates);
		LOG.info("Scheduling next time event for account {} at {}", _accountId, DateUtils.format("MM/dd/yyyy HH:mm:ss", nextDate));
		nextTask = new EventTimeTask(_accountId, nextDate);
		timer.schedule(nextTask, nextDate);
	}

	public void schedule(TimerTask _task, long _delay) {
		if (timer == null)
			return;
		timer.schedule(_task, _delay);
	}

	public static void shutdown() {
		if (INSTANCE == null)
			return;
		INSTANCE.executor.shutdown();
		INSTANCE.dao.shutdown();
		INSTANCE.cmDao.shutdown();
		INSTANCE.timer.cancel();
		INSTANCE.timer = null;
		INSTANCE = null;
	}

	private class EventTimeTask extends TimerTask {
		private final int accountId;
		private final Date eventTime;

		EventTimeTask(int _accountId, Date _eventTime) {
			accountId = _accountId;
			eventTime = _eventTime;
		}

		@Override
		public void run() {
			LOG.info("Firing time event for account {}", accountId);
			Event event = new Event();
			event.setAccountId(accountId);
			event.setTime(eventTime);
			event.setType(EventType.TIME);
			fireEvent(event);
			scheduleNextTimeEventForAccount(accountId);
		}
	}
}
