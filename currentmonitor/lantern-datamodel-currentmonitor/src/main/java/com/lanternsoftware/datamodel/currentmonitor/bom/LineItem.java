package com.lanternsoftware.datamodel.currentmonitor.bom;

public class LineItem {
	private String name;
	private String partNumber;
	private String url;
	private double listCost;
	private double orderCost;
	private int quantity;

	public LineItem() {
	}

	public LineItem(String _name, String _partNumber, String _url, double _listCost, double _orderCost, int _quantity) {
		name = _name;
		partNumber = _partNumber;
		url = _url;
		listCost = _listCost;
		orderCost = _orderCost;
		quantity = _quantity;
	}

	public String getName() {
		return name;
	}

	public void setName(String _name) {
		name = _name;
	}

	public String getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(String _partNumber) {
		partNumber = _partNumber;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String _url) {
		url = _url;
	}

	public double getListCost() {
		return listCost;
	}

	public void setListCost(double _listCost) {
		listCost = _listCost;
	}

	public double getOrderCost() {
		return orderCost;
	}

	public void setOrderCost(double _orderCost) {
		orderCost = _orderCost;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int _quantity) {
		quantity = _quantity;
	}
}
