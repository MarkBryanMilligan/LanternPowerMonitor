package com.lanternsoftware.powermonitor.datamodel.archive;

import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Date;

@DBSerializable(autogen = false)
public class ArchiveStatus {
	private int accountId;
	private Date month;
	private float progress;

	public ArchiveStatus() {
	}

	public ArchiveStatus(int _accountId, Date _month, float _progress) {
		accountId = _accountId;
		month = _month;
		progress = _progress;
	}

	public String getId() {
		return String.format("%d-%d", accountId, DateUtils.toLong(month));
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public Date getMonth() {
		return month;
	}

	public void setMonth(Date _month) {
		month = _month;
	}

	public float getProgress() {
		return progress;
	}

	public void setProgress(float _progress) {
		progress = _progress;
	}
}
