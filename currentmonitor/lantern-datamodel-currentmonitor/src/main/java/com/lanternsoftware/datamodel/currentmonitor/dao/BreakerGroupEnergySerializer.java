package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.BreakerGroupEnergy;
import com.lanternsoftware.datamodel.currentmonitor.EnergyBlock;
import com.lanternsoftware.datamodel.currentmonitor.EnergyViewMode;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;

import java.nio.ByteBuffer;
import java.util.*;

public class BreakerGroupEnergySerializer extends AbstractDaoSerializer<BreakerGroupEnergy>
{
	@Override
	public Class<BreakerGroupEnergy> getSupportedClass()
	{
		return BreakerGroupEnergy.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(BreakerGroupEnergy _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("_id", _o.getId());
		d.put("account_id", _o.getAccountId());
		d.put("group_id", _o.getGroupId());
		d.put("group_name", _o.getGroupName());
		d.put("view_mode", DaoSerializer.toEnumName(_o.getViewMode()));
		d.put("start", DaoSerializer.toLong(_o.getStart()));
		d.put("sub_groups", DaoSerializer.toDaoEntities(_o.getSubGroups(), DaoProxyType.MONGO));
		TimeZone tz = DateUtils.defaultTimeZone(_o.getTimeZone());
		d.put("timezone", tz.getID());
		if (CollectionUtils.size(_o.getEnergyBlocks()) > 0) {
			Date start = _o.getStart();
			Date now = new Date();
			ByteBuffer bb = ByteBuffer.allocate(_o.getViewMode().blockCount(start, tz) * 4);
			ByteBuffer cb = ByteBuffer.allocate(_o.getViewMode().blockCount(start, tz) * 8);
			for (EnergyBlock b : _o.getEnergyBlocks()) {
				if (b.getStart().before(start))
					continue;
				if (now.before(start))
					break;
				while (start.before(b.getStart())) {
					bb.putFloat(0);
					cb.putDouble(0);
					start = _o.getViewMode().toBlockEnd(start, tz);
				}
				bb.putFloat((float) b.getJoules());
				cb.putDouble(b.getCharge());
				start = _o.getViewMode().toBlockEnd(start, tz);
			}
			if (bb.position() < bb.limit()) {
				d.put("blocks", Arrays.copyOfRange(bb.array(), 0, bb.position()));
				d.put("charges", Arrays.copyOfRange(cb.array(), 0, cb.position()));
			}
			else {
				d.put("blocks", bb.array());
				d.put("charges", cb.array());
			}
		}
		d.put("to_grid", _o.getToGrid());
		d.put("from_grid", _o.getFromGrid());
		d.put("peak_to_grid", _o.getPeakToGrid());
		d.put("peak_from_grid", _o.getPeakFromGrid());
		d.put("peak_production", _o.getPeakProduction());
		d.put("peak_consumption", _o.getPeakConsumption());
		return d;
	}

	@Override
	public BreakerGroupEnergy fromDaoEntity(DaoEntity _d)
	{
		BreakerGroupEnergy o = new BreakerGroupEnergy();
		o.setGroupId(DaoSerializer.getString(_d, "group_id"));
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setGroupName(DaoSerializer.getString(_d, "group_name"));
		o.setViewMode(DaoSerializer.getEnum(_d, "view_mode", EnergyViewMode.class));
		o.setStart(DaoSerializer.getDate(_d, "start"));
		o.setSubGroups(DaoSerializer.getList(_d, "sub_groups", BreakerGroupEnergy.class));
		o.setTimeZone(DateUtils.fromTimeZoneId(DaoSerializer.getString(_d, "timezone")));
		List<EnergyBlock> blocks = new ArrayList<>();
		byte[] blockData = DaoSerializer.getByteArray(_d, "blocks");
		if (CollectionUtils.length(blockData) > 0) {
			ByteBuffer bb = ByteBuffer.wrap(blockData);
			Date start = o.getStart();
			while (bb.hasRemaining()) {
				EnergyBlock block = new EnergyBlock(start, o.getViewMode().toBlockEnd(start, o.getTimeZone()), bb.getFloat());
				blocks.add(block);
				start = block.getEnd();
			}
		}
		o.setEnergyBlocks(blocks);
		byte[] chargeData = DaoSerializer.getByteArray(_d, "charges");
		int idx = 0;
		if (CollectionUtils.length(chargeData) > 0) {
			ByteBuffer bb = ByteBuffer.wrap(chargeData);
			while (bb.hasRemaining()) {
				blocks.get(idx++).setCharge(bb.getDouble());
			}
		}
		o.setToGrid(DaoSerializer.getDouble(_d, "to_grid"));
		o.setFromGrid(DaoSerializer.getDouble(_d, "from_grid"));
		o.setPeakToGrid(DaoSerializer.getDouble(_d, "peak_to_grid"));
		o.setPeakFromGrid(DaoSerializer.getDouble(_d, "peak_from_grid"));
		o.setPeakProduction(DaoSerializer.getDouble(_d, "peak_production"));
		o.setPeakConsumption(DaoSerializer.getDouble(_d, "peak_consumption"));
		return o;
	}
}