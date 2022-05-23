package com.lanternsoftware.datamodel.currentmonitor.bom;

import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.datamodel.currentmonitor.BreakerConfig;
import com.lanternsoftware.datamodel.currentmonitor.BreakerType;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.csv.CSV;
import com.lanternsoftware.util.dao.DaoSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BOM {
	List<LineItem> lineItems;
	private static final Map<Integer, String> ctSizes = new TreeMap<>();
	static {
		ctSizes.put(15, "https://store.lanternpowermonitor.com/product/15a-yhdc-current-transformer/3");
		ctSizes.put(20, "https://store.lanternpowermonitor.com/product/20a-yhdc-current-transformer/4");
		ctSizes.put(30, "https://store.lanternpowermonitor.com/product/30a-yhdc-current-transformer/5");
		ctSizes.put(50, "https://store.lanternpowermonitor.com/product/50a-yhdc-current-transformer/6");
		ctSizes.put(60, "https://store.lanternpowermonitor.com/product/60a-yhdc-current-transformer/7");
		ctSizes.put(100, "https://store.lanternpowermonitor.com/product/100a-yhdc-current-transformer/8");
	}

	public static BOM fromConfig(BreakerConfig _config) {
		BOM bom = new BOM();
		bom.setLineItems(new ArrayList<>());
		Map<Integer, AtomicInteger> ctCnts = new TreeMap<>();
		Map<Integer, Breaker> breakers = CollectionUtils.transformToMap(_config.getAllBreakers(), Breaker::getIntKey);
		for (Breaker breaker : breakers.values()) {
			if (bom.isUntrackedBottom(breakers, breaker))
				continue;
			for (int size : ctSizes.keySet()) {
				if (breaker.getSizeAmps() <= size) {
					ctCnts.computeIfAbsent(size, (_k) -> new AtomicInteger(0)).getAndIncrement();
					break;
				}
			}
		}
		int breakerCnt = CollectionUtils.sumIntegers(CollectionUtils.transform(ctCnts.values(), AtomicInteger::get));
		int hubCnt = (int)Math.ceil(breakerCnt/15.0);

		bom.getLineItems().add(new LineItem("Lantern Power Monitor Case", "LPMC1", "https://github.com/MarkBryanMilligan/LanternPowerMonitor/tree/main/case", 0.10, 3.00, hubCnt));
		bom.getLineItems().add(new LineItem("Lantern Power Monitor Case Lid", "LPMCL1", "https://github.com/MarkBryanMilligan/LanternPowerMonitor/tree/main/case", 0.10, 2.00, hubCnt));
		bom.getLineItems().add(new LineItem("Lantern Power Monitor PCB", "LPMPCB1", "https://store.lanternpowermonitor.com/product/assembled-lantern-power-monitor-pcb/1", 1.00, 5.00, hubCnt));
		bom.getLineItems().add(new LineItem("Raspberry Pi 3 Model A+", "3A+", "https://www.raspberrypi.org/products/raspberry-pi-3-model-a-plus/", 25.0, 35.0, hubCnt));
		bom.getLineItems().add(new LineItem("Jameco 12V AC/AC Adapter", "10428", "https://store.lanternpowermonitor.com/product/120vac-to-12vac-voltage-transformer/2", 10.95, 15.00, hubCnt));
		bom.getLineItems().add(new LineItem("8gb Sandisk Industrial memory card", "SDSDQAF3-008G-I", "https://www.amazon.com/gp/product/B07BZ5SY18", 4.00, 5.00, hubCnt));
		bom.getLineItems().add(new LineItem("M2.5x10mm Cap Screw", "A15120300ux0225", "https://www.amazon.com/gp/product/B01B1OD7IK", 0.10, 0.20, hubCnt*8));
		bom.getLineItems().add(new LineItem("M2.5x11mm Female x Female Standoff", "", "https://www.ebay.com/itm/50pcs-M2-5-Female-Hex-Screw-Brass-PCB-Standoffs-Hexagonal-Spacers/172746413434", 0.15, 0.25, hubCnt*4));
		bom.getLineItems().add(new LineItem("M2.5x12mm Female x Male Standoff", "", "https://www.ebay.com/itm/M2-5-2-5mm-Thread-6mm-Brass-Standoff-Spacer-Male-x-Female-20-50pcs-New/283432513974", 0.15, 0.25, hubCnt*4));
		for (Map.Entry<Integer, AtomicInteger> ctCnt : ctCnts.entrySet()) {
			bom.getLineItems().add(new LineItem(String.format("%d Amp Current Transformer", ctCnt.getKey()), String.format("SCT-013-%03d", ctCnt.getKey()), ctSizes.get(ctCnt.getKey()), 5.00, 6.00, ctCnt.getValue().get()));
		}
		return bom;
	}

	public List<LineItem> getLineItems() {
		return lineItems;
	}

	public void setLineItems(List<LineItem> _lineItems) {
		lineItems = _lineItems;
	}

	public CSV toCsv(boolean _includePrices) {
		double selfCost = CollectionUtils.sum(CollectionUtils.transform(lineItems, _l->_l.getListCost()*_l.getQuantity()));
		double shippedCost = CollectionUtils.sum(CollectionUtils.transform(lineItems, _l->_l.getOrderCost()*_l.getQuantity()));
		List<String> headers = CollectionUtils.asArrayList("Item Name", "Part Number", "URL", "Quantity Required", "~Self-Source Unit Price", "Shipped Unit Price", "~Self-Source Total Price", "Shipped Total Price");
		List<List<String>> rows = CollectionUtils.transform(lineItems, _l->CollectionUtils.asArrayList(_l.getName(), _l.getPartNumber(), _l.getUrl(), DaoSerializer.toString(_l.getQuantity()), String.format("$%.2f", _l.getListCost()), String.format("$%.2f", _l.getOrderCost()), String.format("$%.2f", _l.getListCost() * _l.getQuantity()), String.format("$%.2f", _l.getOrderCost() * _l.getQuantity())));
		if (!_includePrices) {
			headers = headers.subList(0, 4);
			rows = CollectionUtils.transform(rows, _r->_r.subList(0, 4));
		}
		else
			rows.add(CollectionUtils.asArrayList("Total", "", "", "", "", "", String.format("$%.2f", selfCost), String.format("$%.2f", shippedCost)));
		return new CSV(headers, rows, headers.size());
	}

	private boolean isUntrackedBottom(Map<Integer, Breaker> _breakers, Breaker _breaker) {
		if (_breaker.getType() != BreakerType.DOUBLE_POLE_BOTTOM)
			return false;
		Breaker topBreaker = _breakers.get(Breaker.intKey(_breaker.getPanel(), _breaker.getSpaceIndex() - 2));
		return topBreaker != null && topBreaker.getType() == BreakerType.DOUBLE_POLE_TOP_ONE_CT;
	}
}