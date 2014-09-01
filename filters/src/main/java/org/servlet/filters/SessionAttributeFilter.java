package org.servlet.filters;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>SessionAttributeFilter</code> is a filter which can be used to redirect
 * requests based on attributes found in the session.
 *
 * @author shivam
 */

public final class SessionAttributeFilter implements Filter {

	/** Init param for setting requireAttribute. */
	public static final String REQUIRE_ATTRIBUTE = "requireAttribute";

	/** Log for this class. */
	private static final Log LOG = LogFactory
			.getLog(SessionAttributeFilter.class);

	/** Whether attributes are required to exist by this filter. */
	private boolean requireAttribute;

	/** Used to forward requests. */
	private ServletContext context;

	/** Session attribute names and values. */
	private Map<String, Pattern> attributes = new HashMap<String, Pattern>();

	/** Redirect URL for each session attribute. */
	private Map<String, String> redirects = new HashMap<String, String>();

	/**
	 * Initialize this filter.
	 *
	 * @param config
	 *            <code>FilterConfig</code>
	 */
	public void init(final FilterConfig config) {
		this.context = config.getServletContext();
		this.requireAttribute = Boolean.valueOf(
				config.getInitParameter(REQUIRE_ATTRIBUTE)).booleanValue();
		if (LOG.isDebugEnabled()) {
			LOG.debug("requireAttribute = " + this.requireAttribute);
		}

		final Enumeration<?> e = config.getInitParameterNames();
		while (e.hasMoreElements()) {
			final String name = (String) e.nextElement();
			if (!name.equals(REQUIRE_ATTRIBUTE)) {
				final String value = config.getInitParameter(name);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Loaded attribute name:value " + name + ":"
							+ value);
				}

				final StringTokenizer st = new StringTokenizer(name);
				final String attrName = st.nextToken();
				final String attrValue = st.nextToken();

				this.attributes.put(attrName, Pattern.compile(attrValue));
				this.redirects.put(attrName, value);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Stored attribute " + attrName + " for pattern "
							+ attrValue + " with redirect of " + value);
				}
			}
		}
	}

	/**
	 * Handle all requests sent to this filter.
	 *
	 * @param request
	 *            <code>ServletRequest</code>
	 * @param response
	 *            <code>ServletResponse</code>
	 * @param chain
	 *            <code>FilterChain</code>
	 *
	 * @throws ServletException
	 *             if an error occurs
	 * @throws IOException
	 *             if an error occurs
	 */
	public void doFilter(final ServletRequest request,
			final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		boolean success = false;
		String redirect = null;
		if (request instanceof HttpServletRequest) {
			final HttpSession session = ((HttpServletRequest) request)
					.getSession(true);
			final Iterator<String> i = this.attributes.keySet().iterator();
			boolean loop = true;
			while (i.hasNext() && loop) {
				final String name = i.next();
				final Pattern pattern = this.attributes.get(name);
				final Object sessionAttr = session.getAttribute(name);
				if (sessionAttr != null) {
					final String value = String.valueOf(sessionAttr);
					if (pattern.matcher(value).matches()) {
						if (LOG.isDebugEnabled()) {
							LOG.debug(value + " matches " + pattern.pattern());
						}
						success = true;
					} else {
						if (LOG.isDebugEnabled()) {
							LOG.debug(value + " does not match "
									+ pattern.pattern());
						}
						redirect = this.redirects.get(name);
						success = false;
						loop = false;
					}
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("No session attribute found for " + name);
					}
					if (this.requireAttribute) {
						redirect = this.redirects.get(name);
						success = false;
						loop = false;
					} else {
						success = true;
					}
				}
			}
		}

		if (!success) {
			if (redirect != null && !"".equals(redirect)) {
				final StringBuffer url = new StringBuffer(redirect);
				if (((HttpServletRequest) request).getRequestURI() != null) {
					url.append("?url=").append(
							URLEncoder.encode(((HttpServletRequest) request)
									.getRequestURI(), "UTF-8"));
					if (((HttpServletRequest) request).getQueryString() != null) {
						url.append(URLEncoder.encode("?", "UTF-8")).append(
								URLEncoder.encode(
										((HttpServletRequest) request)
												.getQueryString(), "UTF-8"));
					}
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug("Forwarding request to " + url.toString());
				}
				this.context.getRequestDispatcher(url.toString()).forward(
						request, response);
				return;
			} else {
				if (response instanceof HttpServletResponse) {
					((HttpServletResponse) response)
							.sendError(HttpServletResponse.SC_FORBIDDEN,
									"Request blocked by filter, unable to perform redirect");
					return;
				} else {
					throw new ServletException(
							"Request blocked by filter, unable to perform redirect");
				}
			}
		}
		chain.doFilter(request, response);
	}

	/**
	 * Called by the web container to indicate to a filter that it is being
	 * taken out of service.
	 */
	public void destroy() {
	}
}