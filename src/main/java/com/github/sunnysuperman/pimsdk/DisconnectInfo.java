package com.github.sunnysuperman.pimsdk;

import java.util.List;

import com.github.sunnysuperman.pimsdk.packet.Message;

public class DisconnectInfo {
	private int reason;
	private List<Message> pendingMessages;

	public int getReason() {
		return reason;
	}

	public void setReason(int reason) {
		this.reason = reason;
	}

	public List<Message> getPendingMessages() {
		return pendingMessages;
	}

	public void setPendingMessages(List<Message> pendingMessages) {
		this.pendingMessages = pendingMessages;
	}

}
