package org.servlet.filters.clickstream;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.servlet.filters.clickstream.Clickstream;

/**
 * <code>This filter tracks user requests (a.k.a. clicks) and request sequences (a.k.a.
 * clickstreams) to show a site administrator who's visiting her site and what
 * pages each visitor has accessed so far.</code>
 * 
 * @author shivam
 *
 */
public class ClickstreamFilter implements Filter {
	protected FilterConfig filterConfig;
	private final static String FILTER_APPLIED = "_clickstream_filter_applied";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// Ensure that filter is only applied once per request.
		if (request.getAttribute(FILTER_APPLIED) == null) {
			request.setAttribute(FILTER_APPLIED, Boolean.TRUE);
			HttpSession session = ((HttpServletRequest) request).getSession();
			Clickstream stream = (Clickstream) session
					.getAttribute("clickstream");
			stream.addRequest(((HttpServletRequest) request));
		}

		// pass the request on
		chain.doFilter(request, response);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
	}
}