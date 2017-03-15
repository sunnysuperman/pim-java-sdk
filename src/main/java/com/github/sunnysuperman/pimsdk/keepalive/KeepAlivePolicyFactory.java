package com.github.sunnysuperman.pimsdk.keepalive;

import com.github.sunnysuperman.pimsdk.PimClient;

public interface KeepAlivePolicyFactory {

	KeepAlivePolicy create(PimClient client);

}
