package com.github.sunnysuperman.pimsdk;

public class LoginResponse {
	private int errorCode;
	private int compressThreshold;

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public int getCompressThreshold() {
		return compressThreshold;
	}

	public void setCompressThreshold(int compressThreshold) {
		this.compressThreshold = compressThreshold;
	}
}
