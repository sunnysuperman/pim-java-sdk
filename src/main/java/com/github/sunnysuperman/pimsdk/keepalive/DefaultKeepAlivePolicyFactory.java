package com.github.sunnysuperman.pimsdk.keepalive;

import com.github.sunnysuperman.pimsdk.PimClient;
import com.github.sunnysuperman.pimsdk.keepalive.DefaultKeepAlivePolicy.DefaultKeepAlivePolicyOptions;

public class DefaultKeepAlivePolicyFactory implements KeepAlivePolicyFactory {
	private DefaultKeepAlivePolicyOptions options;

	public DefaultKeepAlivePolicyFactory(DefaultKeepAlivePolicyOptions options) {
		this.options = options != null ? options : new DefaultKeepAlivePolicyOptions();
	}

	@Override
	public KeepAlivePolicy create(PimClient client) {
		return new DefaultKeepAlivePolicy(client, options);
	}

}
