package org.servlet.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Sets the request and/or response character encoding based on the presence of
 * either or both of the following init params, {@link #REQUEST_CHARSET_NAME}
 * {@link #RESPONSE_CHARSET_NAME}.
 *
 * <p>
 * <strong>NOTE:</strong> this filter should be as close to the beginning of the
 * filter chain as possible. Any filters that obtain request parameters, e.g.
 * <code>request.getParameter(...)</code>, prior to setting the character set
 * via this filter will use the platform default character set.
 * </p>
 *
 * @author shivam
 */
public class CharacterEncodingFilter implements Filter {

	/**
	 * Name of init param for setting request character encoding. The value
	 * should be a valid Java character set name, e.g. UTF-8.
	 */
	public static final String REQUEST_CHARSET_NAME = "requestCharsetName";

	/**
	 * Name of init param for setting response character encoding. The value
	 * should be a valid Java character set name, e.g. UTF-8.
	 */
	public static final String RESPONSE_CHARSET_NAME = "responseCharsetName";

	/** Request character set name. */
	private String requestCharsetName;

	/** Response character set name. */
	private String responseCharsetName;

	/** {@inheritDoc} */
	public void init(final FilterConfig filterConfig) throws ServletException {
		if (filterConfig.getInitParameter(REQUEST_CHARSET_NAME) != null) {
			requestCharsetName = filterConfig
					.getInitParameter(REQUEST_CHARSET_NAME);
		}
		if (filterConfig.getInitParameter(RESPONSE_CHARSET_NAME) != null) {
			responseCharsetName = filterConfig
					.getInitParameter(RESPONSE_CHARSET_NAME);
		}
	}

	/** {@inheritDoc} */
	public void doFilter(final ServletRequest request,
			final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		if (requestCharsetName != null) {
			request.setCharacterEncoding(requestCharsetName);
		}
		if (responseCharsetName != null) {
			response.setCharacterEncoding(responseCharsetName);
		}
		chain.doFilter(request, response);
	}

	/** {@inheritDoc} */
	public void destroy() {
	}
}