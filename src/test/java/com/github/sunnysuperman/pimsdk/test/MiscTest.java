package com.github.sunnysuperman.pimsdk.test;

import com.github.sunnysuperman.pimsdk.ClientID;
import com.github.sunnysuperman.pimsdk.packet.Message;
import com.github.sunnysuperman.pimsdk.packet.MessageAck;

import junit.framework.TestCase;

public class MiscTest extends TestCase {

	public void test_ClientID() {
		assertTrue(ClientID.wrap("user.1").equals(ClientID.wrap("user.1")));
		assertTrue(ClientID.wrap("user.1@ios").equals(ClientID.wrap("user.1@ios")));

		assertFalse(ClientID.wrap("user.1").equals(ClientID.wrap("user.2")));
		assertFalse(ClientID.wrap("user.1").equals(ClientID.wrap("user.2@ios")));
		assertFalse(ClientID.wrap("user.1").equals(ClientID.wrap("user.1@ios")));
		assertFalse(ClientID.wrap("user.1@ios").equals(ClientID.wrap("user.1")));
		assertFalse(ClientID.wrap("user.1@ios").equals(ClientID.wrap("user.1@android")));
	}

	public void test_MessageAck() {
		MessageAck ack = (MessageAck) new MessageAck("111", "222").marshall();
		MessageAck ack2 = MessageAck.deserialize(ack.getBody());
		assertTrue(ack2.getSequenceID().equals(ack.getSequenceID()));
		assertTrue(ack2.getMsgID().equals(ack.getMsgID()));
	}

	private boolean isBytesEqual(byte[] b1, byte[] b2) {
		if (b1.length != b2.length) {
			return false;
		}
		for (int i = 0; i < b1.length; i++) {
			if (b1[i] != b2[i]) {
				return false;
			}
		}
		return true;
	}

	public void test_Message() {
		byte[] b1 = new byte[] { 1, 2, 3, -5 };
		Message msg = new Message();
		msg.setExtra(b1);
		msg.setTo(ClientID.wrap("user.101@ios"));
		msg.setFrom(ClientID.wrap("user.102@android"));
		byte[] body = msg.getBody();
		System.out.println(body.length);
		Message m2 = Message.deserialize(body);
		byte[] b2 = m2.getExtra();
		assertTrue(isBytesEqual(b1, b2));
		System.out.println(m2.getTo());
		System.out.println(m2.getFrom());
		assertTrue(msg.getTo().equals(m2.getTo()));
		assertTrue(msg.getFrom().equals(m2.getFrom()));
	}

}
