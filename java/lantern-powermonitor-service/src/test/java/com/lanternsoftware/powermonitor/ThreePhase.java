package com.lanternsoftware.powermonitor;

import com.lanternsoftware.powermonitor.dataaccess.PowerMonitorDao;
import com.lanternsoftware.powermonitor.dataaccess.MongoPowerMonitorDao;
import com.lanternsoftware.powermonitor.datamodel.Account;
import com.lanternsoftware.powermonitor.datamodel.Breaker;
import com.lanternsoftware.powermonitor.datamodel.BreakerConfig;
import com.lanternsoftware.powermonitor.datamodel.BreakerHub;
import com.lanternsoftware.powermonitor.datamodel.BreakerPolarity;
import com.lanternsoftware.powermonitor.datamodel.hub.BreakerSample;
import com.lanternsoftware.powermonitor.datamodel.hub.HubSample;
import com.lanternsoftware.powermonitor.datamodel.hub.PowerSample;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.MessageUtil;
import com.lanternsoftware.util.dao.DaoQuery;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import com.lanternsoftware.util.external.LanternFiles;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;

public class ThreePhase {
	private static final String path = "d:\\zwave\\Kristof\\";
	public static void main(String[] args) {
		PowerMonitorDao dao = new MongoPowerMonitorDao(MongoConfig.fromDisk(LanternFiles.CONFIG_PATH + "mongo.cfg"));

//		BreakerConfig config = DaoSerializer.parse(ResourceLoader.loadFileAsString(path+"config.json"), BreakerConfig.class);
		int accountId = 550;
		Account account = dao.getAccount(accountId);
		BreakerConfig config = dao.getConfig(accountId);
//		CollectionUtils.edit(config.getAllBreakers(), _b->_b.setPanel(0));

		Map<Integer, Breaker> solarBreakers = CollectionUtils.transformToMap(CollectionUtils.filter(config.getAllBreakers(), _b->_b.getPolarity() == BreakerPolarity.SOLAR), Breaker::getIntKey);
//		Map<Integer, Breaker> solarBreakers = CollectionUtils.transformToMap(config.getAllBreakers(), Breaker::getIntKey);
		TimeZone tz = TimeZone.getTimeZone(account.getTimezone());

//		HubSample sample = DaoSerializer.parse(ResourceLoader.loadFileAsString(path+"samples.json"), HubSample.class);
//		HubSample sample = dao.getProxy().queryOne(HubSample.class, new DaoQuery("account_id", accountId), DaoSort.sortDesc("sample_date"));
		HubSample sample = dao.getProxy().queryOne(HubSample.class, new DaoQuery("_id", "550-1670745850953"));
		if (sample != null) {
			List<BreakerSample> solarSamples = CollectionUtils.filter(sample.getBreakers(), _b->solarBreakers.containsKey(_b.key()));
			for (BreakerSample breakerSample : solarSamples) {
				Breaker breaker = solarBreakers.get(breakerSample.key());
				BreakerHub hub = config.getHub(breaker.getHub());
				long cycleLength = 1000000000 / hub.getFrequency();
				Map<Integer, List<PowerSample>> cycles = CollectionUtils.transformToMultiMap(breakerSample.getSamples(), _p->_p.cycle);
				for (List<PowerSample> cycleSamples : cycles.values()) {
					int phaseOffsetNs = breaker.getPhaseOffsetNs() - hub.getPhaseOffsetNs();
					if (phaseOffsetNs != 0) {
						long minNano;
						long maxNano = minNano = cycleSamples.get(0).nanoTime;
						for (PowerSample s : cycleSamples) {
							if (s.nanoTime < minNano)
								minNano = s.nanoTime;
							if (s.nanoTime > maxNano)
								maxNano = s.nanoTime;
						}
						TreeMap<Long, Double> offsetSamples = new TreeMap<>();
						for (PowerSample s : cycleSamples) {
							if (s.nanoTime + phaseOffsetNs < minNano)
								offsetSamples.put(s.nanoTime + phaseOffsetNs + cycleLength, s.voltage);
							else if (s.nanoTime + phaseOffsetNs > maxNano)
								offsetSamples.put(s.nanoTime + phaseOffsetNs - cycleLength, s.voltage);
							else
								offsetSamples.put(s.nanoTime + phaseOffsetNs, s.voltage);
						}
						for (PowerSample s : cycleSamples) {
							List<Double> voltages = new ArrayList<>();
							List<Double> times = new ArrayList<>();
							Entry<Long, Double> floorEntry = offsetSamples.floorEntry(s.nanoTime);
							if (floorEntry != null) {
								voltages.add(floorEntry.getValue());
								times.add((double)floorEntry.getKey());
							}
							Entry<Long, Double> ceilingEntry = offsetSamples.ceilingEntry(s.nanoTime);
							if (ceilingEntry != null) {
								voltages.add(ceilingEntry.getValue());
								times.add((double)ceilingEntry.getKey());
							}
							s.voltage = CollectionUtils.mean(voltages);
							long time = CollectionUtils.mean(times).longValue();
							System.out.println("requested: " + s.nanoTime + " actual: " + time + " error: " + (time-s.nanoTime));
						}
//						System.out.println(phaseOffsetNs);
					}
					double vOffset = 0.0;
					double iOffset = 0.0;
					for (PowerSample s : cycleSamples) {
						vOffset += s.voltage;
						iOffset += s.current;
					}
					vOffset /= cycleSamples.size();
					iOffset /= cycleSamples.size();
					int lowSamples = 0;
					double pSum = 0.0;
					double vRms = 0.0;
					double lowPassFilter = breaker.getLowPassFilter();

					for (PowerSample s : cycleSamples) {
						s.current -= iOffset;
						if (Math.abs(s.current) < lowPassFilter)
							lowSamples++;
						s.voltage -= vOffset;
						pSum += s.current * s.voltage;
						vRms += s.voltage * s.voltage;
					}
					vRms /= cycleSamples.size();
					vRms = hub.getVoltageCalibrationFactor() * Math.sqrt(vRms);
					int lowSampleRatio = (lowSamples * 100) / cycleSamples.size();
					double realPower = (hub.getVoltageCalibrationFactor() * hub.getPortCalibrationFactor() * breaker.getCalibrationFactor() * pSum) / cycleSamples.size();
					if ((lowSampleRatio > 75) && Math.abs(realPower) < 13.0)
						realPower = 0.0;
					if (breaker.getPolarity() == BreakerPolarity.NORMAL)
						realPower = Math.abs(realPower);
					else if (breaker.getPolarity() == BreakerPolarity.SOLAR)
						realPower = -Math.abs(realPower);
					else if (breaker.getPolarity() == BreakerPolarity.BI_DIRECTIONAL_INVERTED)
						realPower = -realPower;
					if (breaker.isDoublePower())
						realPower *= 2.0;
					System.out.println(MessageUtil.msg("Breaker = {}, Vrms = {}, Power = {}", breaker.getSpace(), vRms, realPower));
					drawPowerSamples(cycleSamples, breaker, hub);
				}
			}
		}
		dao.shutdown();
	}

	private static void drawPowerSamples(List<PowerSample> _samples, Breaker _breaker, BreakerHub _hub) {
		int width = 1920;
		int height = 1080;
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		g.setPaint(Color.white);
		g.fillRect(0,0,width,height);
		List<Double> voltages = CollectionUtils.transform(_samples, PowerSample::getVoltage);
		double maxVoltage = CollectionUtils.getLargest(voltages);
		double minVoltage = CollectionUtils.getSmallest(voltages);
		double vMax = Math.max(maxVoltage, Math.abs(minVoltage));
		List<Double> currents = CollectionUtils.transform(_samples, PowerSample::getCurrent);
		double maxCurrent = CollectionUtils.getLargest(currents);
		double minCurrent = CollectionUtils.getSmallest(currents);
		double cMax = Math.max(maxCurrent, Math.abs(minCurrent));
		long xMin = _samples.get(0).nanoTime;
		long xWidth = CollectionUtils.getLast(_samples).nanoTime - xMin;
		Double prevX = null;
		Double prevVy = null;
		Double prevCy = null;
		double x;
		double vy;
		double cy;
		int halfHeight = height/2;
		for (PowerSample sample : _samples) {
			x = (((double)sample.nanoTime - xMin) / xWidth)*width;
			vy = (sample.voltage/vMax)*halfHeight+halfHeight;
			cy = (-sample.current/cMax)*halfHeight+halfHeight;
			if (prevX != null) {
				g.setPaint(Color.red);
				g.drawLine(prevX.intValue(), prevVy.intValue(), (int)x, (int)vy);
				g.setPaint(Color.green);
				g.drawLine(prevX.intValue(), prevCy.intValue(), (int)x, (int)cy);

			}
			prevX = x;
			prevVy = vy;
			prevCy = cy;
		}
		g.setPaint(Color.red);
		g.drawString("VRms: " + String.format("%.1f", _hub.getVoltageCalibrationFactor() * Math.sqrt(CollectionUtils.mean(CollectionUtils.transform(_samples, _p -> _p.voltage * _p.voltage)))), 10, 20);
		g.setPaint(Color.green);
		g.drawString("IRms: " + String.format("%.1f", _hub.getPortCalibrationFactor() * _breaker.getCalibrationFactor() * Math.sqrt(CollectionUtils.mean(CollectionUtils.transform(_samples, _p -> _p.current * _p.current)))), 10, 40);
		try {
			File outputfile = new File(path+ _breaker.getKey() + "-" + _samples.get(0).cycle + ".png");
			ImageIO.write(bi, "png", outputfile);
		} catch (IOException _e) {
			_e.printStackTrace();
		}
	}
}
