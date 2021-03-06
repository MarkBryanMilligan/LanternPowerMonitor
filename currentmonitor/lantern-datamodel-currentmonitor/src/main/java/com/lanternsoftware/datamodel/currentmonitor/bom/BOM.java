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

	public static BOM fromConfig(BreakerConfig _config) {
		BOM bom = new BOM();
		bom.setLineItems(new ArrayList<>());
		Map<Integer, AtomicInteger> ctCnts = new TreeMap<>();
		Map<Integer, AtomicInteger> ctDuplicates = new TreeMap<>();
		for (Breaker breaker : CollectionUtils.makeNotNull(_config.getAllBreakers())) {
			if (breaker.getSizeAmps() <= 20) {
				ctCnts.computeIfAbsent(20, (_k) -> new AtomicInteger(0)).getAndIncrement();
				if (breaker.getType() == BreakerType.DOUBLE_POLE_TOP_ONE_CT)
					ctDuplicates.computeIfAbsent(20, (_k) -> new AtomicInteger(0)).getAndIncrement();
			}
			else if (breaker.getSizeAmps() <= 30) {
				ctCnts.computeIfAbsent(30, (_k) -> new AtomicInteger(0)).getAndIncrement();
				if (breaker.getType() == BreakerType.DOUBLE_POLE_TOP_ONE_CT)
					ctDuplicates.computeIfAbsent(30, (_k) -> new AtomicInteger(0)).getAndIncrement();
			}
			else {
				ctCnts.computeIfAbsent(50, (_k) -> new AtomicInteger(0)).getAndIncrement();
				if (breaker.getType() == BreakerType.DOUBLE_POLE_TOP_ONE_CT)
					ctDuplicates.computeIfAbsent(50, (_k) -> new AtomicInteger(0)).getAndIncrement();
			}
		}
		for (Map.Entry<Integer, AtomicInteger> ctCnt : ctDuplicates.entrySet()) {
			AtomicInteger cnt = ctCnts.get(ctCnt.getKey());
			if (cnt != null)
				cnt.getAndAdd(-ctCnt.getValue().get());
		}
		int breakerCnt = CollectionUtils.sumIntegers(CollectionUtils.transform(ctCnts.values(), AtomicInteger::get));
		int hubCnt = (int)Math.ceil(breakerCnt/15.0);

		bom.getLineItems().add(new LineItem("Lantern Power Monitor Case", "LPMC1", "https://github.com/MarkBryanMilligan/LanternPowerMonitor/tree/main/case", 0.10, 3.00, hubCnt));
		bom.getLineItems().add(new LineItem("Lantern Power Monitor Case Lid", "LPMCL1", "https://github.com/MarkBryanMilligan/LanternPowerMonitor/tree/main/case", 0.10, 2.00, hubCnt));
		bom.getLineItems().add(new LineItem("Lantern Power Monitor Soldering Jig", "LPMSJ1", "https://github.com/MarkBryanMilligan/LanternPowerMonitor/tree/main/case", 0.10, 4.00, 1));
		bom.getLineItems().add(new LineItem("Lantern Power Monitor PCB", "LPMPCB1", "https://github.com/MarkBryanMilligan/LanternPowerMonitor/tree/main/pcb", 1.00, 5.00, hubCnt));
		bom.getLineItems().add(new LineItem("Raspberry Pi 3 Model A+", "3A+", "https://www.raspberrypi.org/products/raspberry-pi-3-model-a-plus/", 25.0, 35.0, hubCnt));
		bom.getLineItems().add(new LineItem("Jameco 12V AC/AC Adapter", "10428", "https://www.jameco.com/z/ACU120100Z9121-Jameco-Reliapro-AC-to-AC-Wall-Adapter-Transformer-12-Volt-AC-1000mA-Black-Straight-3-5mm-Male-Plug_10428.html", 10.95, 15.00, hubCnt));
		bom.getLineItems().add(new LineItem("16gb memory card", "P-SDU16GU185GW-GE", "https://www.microcenter.com/product/486146/micro-center-16gb-microsdhc-class-10-flash-memory-card", 4.00, 5.00, hubCnt));
		bom.getLineItems().add(new LineItem("40-pin GPIO header", "C169819", "https://lcsc.com/product-detail/Pin-Header-Female-Header_Ckmtw-Shenzhen-Cankemeng-C169819_C169819.html", 0.36, 0.80, hubCnt));
		bom.getLineItems().add(new LineItem("MCP3008", "MCP3008-I-P", "https://www.digikey.com/en/products/detail/microchip-technology/MCP3008-I-P/319422", 2.41, 4.00, hubCnt*2));
		bom.getLineItems().add(new LineItem("10uF 25V 4*5 Capacitor", "C43846", "https://lcsc.com/product-detail/Aluminum-Electrolytic-Capacitors-Leaded_CX-Dongguan-Chengxing-Elec-10uF-25V-4-5_C43846.html", 0.01, 0.10, hubCnt));
		bom.getLineItems().add(new LineItem("22uF 25V 4*7 Capacitor", "C43840", "https://lcsc.com/product-detail/Aluminum-Electrolytic-Capacitors-Leaded_CX-Dongguan-Chengxing-Elec-22uF-25V-4-7_C43840.html", 0.01, 0.10, hubCnt));
		bom.getLineItems().add(new LineItem("10KΩ Resistor", "C385441", "https://lcsc.com/product-detail/Metal-Film-Resistor-TH_TyoHM-RN1-2WS10K%CE%A9FT-BA1_C385441.html", 0.01, 0.10, hubCnt*2));
		bom.getLineItems().add(new LineItem("12KΩ Resistor", "C385449", "https://lcsc.com/product-detail/Metal-Film-Resistor-TH_TyoHM-RN1-2WS12K%CE%A9FT-BA1_C385449.html", 0.01, 0.10, hubCnt));
		bom.getLineItems().add(new LineItem("180KΩ Resistor", "C385460", "https://lcsc.com/product-detail/Metal-Film-Resistor-TH_TyoHM-RN1-2WS180K%CE%A9FT-BA1_C385460.html", 0.01, 0.10, hubCnt));
		bom.getLineItems().add(new LineItem("33KΩ Resistor", "C385498", "https://lcsc.com/product-detail/Metal-Film-Resistor-TH_TyoHM-RN1-2WS33K%CE%A9FT-BA1_C385498.html", 0.01, 0.10, hubCnt));
		bom.getLineItems().add(new LineItem("68KΩ Resistor", "C385541", "https://lcsc.com/product-detail/Metal-Film-Resistor-TH_TyoHM-RN1-2WS68K%CE%A9FT-BA1_C385541.html", 0.01, 0.10, hubCnt));
		bom.getLineItems().add(new LineItem("3.5mm Headphone Jack", "PJ-3583-B", "https://lcsc.com/product-detail/Audio-Video-Connectors_XKB-Enterprise-PJ-3583-B_C397337.html", 0.16, 0.25, hubCnt*16));
		bom.getLineItems().add(new LineItem("M2.5x10mm Cap Screw", "A15120300ux0225", "https://www.amazon.com/gp/product/B01B1OD7IK", 0.10, 0.20, hubCnt*8));
		bom.getLineItems().add(new LineItem("M2.5x11mm Female x Female Standoff", "", "https://www.ebay.com/itm/50pcs-M2-5-Female-Hex-Screw-Brass-PCB-Standoffs-Hexagonal-Spacers/172746413434", 0.15, 0.25, hubCnt*4));
		bom.getLineItems().add(new LineItem("M2.5x12mm Female x Male Standoff", "", "https://www.ebay.com/itm/M2-5-2-5mm-Thread-6mm-Brass-Standoff-Spacer-Male-x-Female-20-50pcs-New/283432513974", 0.15, 0.25, hubCnt*4));
		for (Map.Entry<Integer, AtomicInteger> ctCnt : ctCnts.entrySet()) {
			bom.getLineItems().add(new LineItem(String.format("%d Amp Current Transformer", ctCnt.getKey()), String.format("SCT-013-0%d", ctCnt.getKey()), "N/A", 5.00, 7.00, ctCnt.getValue().get()));
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
}