package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.BreakerGroupEnergy;
import com.lanternsoftware.datamodel.currentmonitor.EnergyBlock;
import com.lanternsoftware.datamodel.currentmonitor.EnergyBlockViewMode;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
			for (EnergyBlock b : _o.getEnergyBlocks()) {
				if (b.getStart().before(start))
					continue;
				if (now.before(start))
					break;
				while (start.before(b.getStart())) {
					bb.putFloat(0);
					start = _o.getViewMode().toBlockEnd(start, tz);
				}
				bb.putFloat((float) b.getJoules());
				start = _o.getViewMode().toBlockEnd(start, tz);
			}
			if (bb.position() < bb.limit())
				d.put("blocks", Arrays.copyOfRange(bb.array(), 0, bb.position()));
			else
				d.put("blocks", bb.array());
		}
		d.put("to_grid", _o.getToGrid());
		d.put("from_grid", _o.getFromGrid());
		return d;
	}

	@Override
	public BreakerGroupEnergy fromDaoEntity(DaoEntity _d)
	{
		BreakerGroupEnergy o = new BreakerGroupEnergy();
		o.setGroupId(DaoSerializer.getString(_d, "group_id"));
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setGroupName(DaoSerializer.getString(_d, "group_name"));
		o.setViewMode(DaoSerializer.getEnum(_d, "view_mode", EnergyBlockViewMode.class));
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
		o.setToGrid(DaoSerializer.getDouble(_d, "to_grid"));
		o.setFromGrid(DaoSerializer.getDouble(_d, "from_grid"));
		return o;
	}
}