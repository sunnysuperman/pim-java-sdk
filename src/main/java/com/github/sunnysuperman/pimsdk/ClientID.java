package com.github.sunnysuperman.pimsdk;

public class ClientID {
	private static final char SEPARATE_CHAR = '/';
	private String username;
	private String resource;

	private ClientID(String username, String resource) {
		super();
		this.username = username;
		this.resource = resource;
	}

	public static ClientID wrap(String username, String resource) {
		return new ClientID(username, resource);
	}

	public static ClientID wrap(String s) {
		if (s == null || s.length() > 255) {
			return null;
		}
		int offset = s.indexOf(SEPARATE_CHAR);
		String username = null;
		String resource = null;
		if (offset > 0) {
			username = s.substring(0, offset);
			resource = s.substring(offset + 1);
		} else {
			username = s;
		}
		return new ClientID(username, resource);

	}

	public String getUsername() {
		return username;
	}

	public String getResource() {
		return resource;
	}

	public String toString() {
		if (resource == null) {
			return username;
		}
		return new StringBuilder(username.length() + 1 + resource.length()).append(username).append(SEPARATE_CHAR)
				.append(resource).toString();
	}

	public boolean sameValue(ClientID another) {
		if (!username.equals(another.username)) {
			return false;
		}
		if (resource == another.resource) {
			return true;
		}
		String r1 = resource == null ? "" : resource;
		String r2 = another.resource == null ? "" : another.resource;
		return r1.equals(r2);
	}

	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof ClientID)) {
			return false;
		}
		ClientID another = (ClientID) o;
		return sameValue(another);
	}

	public int hashCode() {
		return toString().hashCode();
	}

}
