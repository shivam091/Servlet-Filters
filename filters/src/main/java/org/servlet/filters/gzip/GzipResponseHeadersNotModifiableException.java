package org.servlet.filters.gzip;

import javax.servlet.ServletException;

public class GzipResponseHeadersNotModifiableException extends ServletException {

	private static final long serialVersionUID = -1363441742791848373L;

	public GzipResponseHeadersNotModifiableException(String message) {
		super(message);
	}
}
