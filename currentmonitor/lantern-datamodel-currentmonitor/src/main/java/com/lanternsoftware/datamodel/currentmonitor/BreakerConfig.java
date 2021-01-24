package com.lanternsoftware.datamodel.currentmonitor;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@DBSerializable(autogen = false)
public class BreakerConfig {
	@PrimaryKey
	private int accountId;
	private List<Meter> meters;
	private List<BreakerPanel> panels;
	private List<BreakerHub> breakerHubs;
	private List<BreakerGroup> breakerGroups;

	public BreakerConfig() {
	}

	public BreakerConfig(List<BreakerGroup> _breakerGroups) {
		breakerGroups = _breakerGroups;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public List<Meter> getMeters() {
		return meters;
	}

	public void setMeters(List<Meter> _meters) {
		meters = _meters;
	}

	public List<BreakerPanel> getPanels() {
		return panels;
	}

	public void setPanels(List<BreakerPanel> _panels) {
		panels = _panels;
	}

	public List<BreakerHub> getBreakerHubs() {
		return breakerHubs;
	}

	public void setBreakerHubs(List<BreakerHub> _breakerHubs) {
		breakerHubs = _breakerHubs;
	}

	public List<BreakerGroup> getBreakerGroups() {
		return breakerGroups;
	}

	public void setBreakerGroups(List<BreakerGroup> _breakerGroups) {
		breakerGroups = _breakerGroups;
	}

	public List<Breaker> getAllBreakers() {
		List<Breaker> allBreakers = new ArrayList<>();
		for (BreakerGroup g : CollectionUtils.makeNotNull(breakerGroups)) {
			allBreakers.addAll(g.getAllBreakers());
		}
		return allBreakers;
	}

	public List<BreakerGroup> getAllBreakerGroups() {
		List<BreakerGroup> groups = new ArrayList<>();
		for (BreakerGroup g : CollectionUtils.makeNotNull(breakerGroups)) {
			groups.addAll(g.getAllBreakerGroups());
		}
		return groups;
	}

	public List<String> getAllBreakerGroupIds() {
		List<String> ids = new ArrayList<>();
		for (BreakerGroup g : CollectionUtils.makeNotNull(breakerGroups)) {
			ids.addAll(g.getAllBreakerGroupIds());
		}
		return ids;
	}

	public List<Breaker> getBreakersForHub(int _hub) {
		return CollectionUtils.filter(getAllBreakers(), _b -> _b.getHub() == _hub);
	}

	public BreakerHub getHub(int _hub) {
		return CollectionUtils.filterOne(breakerHubs, _h->_h.getHub() == _hub);
	}

	public boolean isSolarConfigured() {
		return CollectionUtils.anyQualify(getAllBreakers(), _b->_b.getPolarity() == BreakerPolarity.SOLAR);
	}

	public String nextGroupId() {
		List<Integer> ids = CollectionUtils.transform(getAllBreakerGroupIds(), NullUtils::toInteger);
		return String.valueOf(DaoSerializer.toInteger(CollectionUtils.getLargest(ids)) + 1);
	}

	public void addGroup(BreakerGroup _group) {
		if (NullUtils.isEmpty(_group.getId())) {
			_group.setId(nextGroupId());
		}
		if (breakerGroups == null)
			breakerGroups = new ArrayList<>();
		breakerGroups.add(_group);
	}

	public void removeInvalidGroups() {
		if (breakerGroups != null)
			breakerGroups.removeIf(_g->!_g.removeInvalidGroups(CollectionUtils.transformToSet(panels, BreakerPanel::getIndex)));
	}

	public String getGroupIdForBreaker(Breaker _breaker) {
		return getGroupIdForBreaker(_breaker.getKey());
	}

	public String getGroupIdForBreaker(int _panel, int _space) {
		return getGroupIdForBreaker(Breaker.key(_panel, _space));
	}

	public String getGroupIdForBreaker(String _breakerKey) {
		BreakerGroup group = getGroupForBreaker(_breakerKey);
		return group != null ? group.getId() : null;
	}

	public BreakerGroup getGroupForBreaker(Breaker _breaker) {
		return getGroupForBreaker(_breaker.getKey());
	}

	public BreakerGroup getGroupForBreaker(int _panel, int _space) {
		return getGroupForBreaker(Breaker.key(_panel, _space));
	}

	public BreakerGroup getGroupForBreaker(String _breakerKey) {
		if (_breakerKey == null)
			return null;
		for (BreakerGroup subGroup : CollectionUtils.makeNotNull(breakerGroups)) {
			BreakerGroup group = subGroup.getGroupForBreaker(_breakerKey);
			if (group != null)
				return group;
		}
		return null;
	}

	public BreakerGroup findParentGroup(BreakerGroup _group) {
		for (BreakerGroup group : CollectionUtils.makeNotNull(breakerGroups)) {
			BreakerGroup parent = group.findParentGroup(_group);
			if (parent != null)
				return parent;
		}
		return null;
	}
}
