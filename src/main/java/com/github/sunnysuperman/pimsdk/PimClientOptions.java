package com.github.sunnysuperman.pimsdk;

import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

import com.github.sunnysuperman.pimsdk.keepalive.KeepAlivePolicyFactory;
import com.github.sunnysuperman.pimsdk.packet.Message;

public abstract class PimClientOptions {
	private volatile String serverHost;
	private volatile int serverPort;
	private volatile boolean ssl;
	private volatile SSLSocketFactory sslSocketFactory;
	private volatile int connectTimeoutSeconds;
	private volatile String username;
	private volatile String password;
	private volatile Map<String, Object> loginOptions;
	private volatile boolean sendReceipt;
	private volatile boolean sendTimeoutCheck;
	private volatile int sendTimeoutSeconds;
	private volatile boolean compressEnabled;
	private volatile PimLogger logger;
	private volatile KeepAlivePolicyFactory KeepAlivePolicyFactory;

	protected abstract boolean onPacket(Packet packet);

	protected abstract void onSendMessageTimeout(List<Message> timeoutMessages);

	protected abstract void onDisconnected(DisconnectInfo disconnectInfo);

	protected abstract byte[] makeLoginRequest();

	protected abstract LoginResponse parseLoginResponse(byte[] body);

	public PimClientOptions clone() throws CloneNotSupportedException {
		return (PimClientOptions) super.clone();
	}

	public String getServerHost() {
		return serverHost;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isSendReceipt() {
		return sendReceipt;
	}

	public void setSendReceipt(boolean sendReceipt) {
		this.sendReceipt = sendReceipt;
	}

	public Map<String, Object> getLoginOptions() {
		return loginOptions;
	}

	public void setLoginOptions(Map<String, Object> loginOptions) {
		this.loginOptions = loginOptions;
	}

	public boolean isSendTimeoutCheck() {
		return sendTimeoutCheck;
	}

	public void setSendTimeoutCheck(boolean sendTimeoutCheck) {
		this.sendTimeoutCheck = sendTimeoutCheck;
	}

	public int getSendTimeoutSeconds() {
		return sendTimeoutSeconds;
	}

	public void setSendTimeoutSeconds(int sendTimeoutSeconds) {
		this.sendTimeoutSeconds = sendTimeoutSeconds;
	}

	public boolean isCompressEnabled() {
		return compressEnabled;
	}

	public void setCompressEnabled(boolean compressEnabled) {
		this.compressEnabled = compressEnabled;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public SSLSocketFactory getSslSocketFactory() {
		return sslSocketFactory;
	}

	public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
	}

	public int getConnectTimeoutSeconds() {
		return connectTimeoutSeconds;
	}

	public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
		this.connectTimeoutSeconds = connectTimeoutSeconds;
	}

	public PimLogger getLogger() {
		return logger;
	}

	public void setLogger(PimLogger logger) {
		this.logger = logger;
	}

	public KeepAlivePolicyFactory getKeepAlivePolicyFactory() {
		return KeepAlivePolicyFactory;
	}

	public void setKeepAlivePolicyFactory(KeepAlivePolicyFactory keepAlivePolicyFactory) {
		KeepAlivePolicyFactory = keepAlivePolicyFactory;
	}

}
