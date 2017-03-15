package com.github.sunnysuperman.pimsdk.keepalive;

import com.github.sunnysuperman.pimsdk.DisconnectInfo;
import com.github.sunnysuperman.pimsdk.Packet;

public interface KeepAlivePolicy {

	void onConnected();

	void onDisconnected(DisconnectInfo disconnectInfo);

	void onReadPacket(Packet packet);

	void onWritePacket(Packet packet);

}
