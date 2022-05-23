package com.lanternsoftware.datamodel.currentmonitor;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.IIdentical;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@DBSerializable(autogen = false)
public class BreakerConfig implements IIdentical<BreakerConfig> {
	@PrimaryKey
	private int accountId;
	private List<Meter> meters;
	private List<BreakerPanel> panels;
	private List<BreakerHub> breakerHubs;
	private List<BreakerGroup> breakerGroups;
	private List<BillingRate> billingRates;
	private List<BillingPlan> billingPlans;
	private int version;

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

	public List<BillingPlan> getBillingPlans() {
		return billingPlans;
	}

	public void setBillingPlans(List<BillingPlan> _billingPlans) {
		billingPlans = _billingPlans;
	}

	public List<BillingRate> getBillingRates() {
		return billingRates;
	}

	public void setBillingRates(List<BillingRate> _billingRates) {
		billingRates = _billingRates;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int _version) {
		version = _version;
	}

	public BreakerGroup getRootGroup() {
		return CollectionUtils.getFirst(breakerGroups);
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

	public Meter getMeterForHub(int _hub) {
		Meter m = null;
		Breaker b = CollectionUtils.filterOne(getAllBreakers(), _b->_b.getHub() == _hub);
		if (b != null)
			m = CollectionUtils.filterOne(meters, _m->_m.getIndex() == b.getMeter());
		return (m != null) ? m : new Meter(getAccountId(), 0, "Main");
	}

	public BreakerGroup findParentGroup(BreakerGroup _group) {
		for (BreakerGroup group : CollectionUtils.makeNotNull(breakerGroups)) {
			BreakerGroup parent = group.findParentGroup(_group);
			if (parent != null)
				return parent;
		}
		return null;
	}

	public boolean containsPolarity(Set<String> _groupIds, BreakerPolarity _polarity) {
		for (BreakerGroup subGroup : CollectionUtils.makeNotNull(breakerGroups)) {
			if (subGroup.containsPolarity(_groupIds, _polarity))
				return true;
		}
		return false;
	}

	public BillingCurrency getCurrency() {
		return CollectionUtils.getFirst(CollectionUtils.transformToSet(CollectionUtils.aggregate(billingPlans, BillingPlan::getRates), BillingRate::getCurrency));
	}

	public boolean isMainsPowerTrackedForMeter(int _meter) {
		return CollectionUtils.anyQualify(getAllBreakers(), _b->_b.isMain() && (_b.getMeter() == _meter));
	}

	@Override
	public boolean equals(Object _o) {
		if (this == _o) return true;
		if (_o == null || getClass() != _o.getClass()) return false;
		BreakerConfig that = (BreakerConfig) _o;
		return accountId == that.accountId && CollectionUtils.isEqual(meters, that.meters) && CollectionUtils.isEqual(panels, that.panels) && CollectionUtils.isEqual(breakerHubs, that.breakerHubs) && CollectionUtils.isEqual(breakerGroups, that.breakerGroups) && CollectionUtils.isEqual(billingPlans, that.billingPlans);
	}

	@Override
	public boolean isIdentical(BreakerConfig _o) {
		if (this == _o) return true;
		return accountId == _o.accountId && CollectionUtils.isIdentical(meters, _o.meters) && CollectionUtils.isIdentical(panels, _o.panels) && CollectionUtils.isIdentical(breakerHubs, _o.breakerHubs) && CollectionUtils.isIdentical(breakerGroups, _o.breakerGroups) && CollectionUtils.isIdentical(billingPlans, _o.billingPlans);
	}

	@Override
	public int hashCode() {
		return Objects.hash(accountId, meters, panels, breakerHubs, breakerGroups, billingRates, version);
	}
}
