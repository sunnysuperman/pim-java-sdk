package com.github.sunnysuperman.pimsdk;

public interface PimLogger {
	boolean isInfoEnabled();

	void info(String msg);

	void warn(String msg);

	void error(String msg);

	void error(Throwable t);

	void error(String msg, Throwable t);
}
