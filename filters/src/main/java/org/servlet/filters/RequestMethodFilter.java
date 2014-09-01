package org.servlet.filters;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>RequestMethodFilter</code> is a filter which can be used to restrict
 * access to a servlet by using any of the methods available in <code>
 * javax.servlet.ServletRequest</code> or <code>
 * javax.servlet.http.HttpServletRequest</code>.
 *
 * @author shivam
 */

public class RequestMethodFilter implements Filter {

	/** Log for this class. */
	private static final Log LOG = LogFactory.getLog(RequestMethodFilter.class);

	/** Methods to call on the request object. */
	private Map<String, Method> servletMethods = new HashMap<String, Method>();

	/** Methods to call on the request object. */
	private Map<String, Method> httpServletMethods = new HashMap<String, Method>();

	/** Arguments for the request object methods. */
	private Map<String, Object[]> arguments = new HashMap<String, Object[]>();

	/** Pattern that method results must match. */
	private Map<String, Pattern> patterns = new HashMap<String, Pattern>();

	/**
	 * Initialize this filter.
	 *
	 * @param config
	 *            <code>FilterConfig</code>
	 */
	public void init(final FilterConfig config) {
		final Enumeration<?> e = config.getInitParameterNames();
		while (e.hasMoreElements()) {
			final String name = (String) e.nextElement();
			final String value = config.getInitParameter(name);

			final StringTokenizer st = new StringTokenizer(name);
			final String methodName = st.nextToken();
			Object[] args = null;
			Class<?>[] params = null;
			if (st.countTokens() > 0) {
				args = new Object[st.countTokens()];
				params = new Class[st.countTokens()];

				int i = 0;
				while (st.hasMoreTokens()) {
					final String token = st.nextToken();
					args[i] = token;
					params[i++] = token.getClass();
				}
			}
			try {
				this.servletMethods.put(methodName,
						ServletRequest.class.getMethod(methodName, params));
				if (LOG.isDebugEnabled()) {
					LOG.debug("Found method " + methodName
							+ " for ServletRequest");
				}
			} catch (NoSuchMethodException ex) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Could not find method " + methodName
							+ " for ServletRequest");
				}
			}
			try {
				this.httpServletMethods.put(methodName,
						HttpServletRequest.class.getMethod(methodName, params));
				if (LOG.isDebugEnabled()) {
					LOG.debug("Found method " + methodName
							+ " for HttpServletRequest");
				}
			} catch (NoSuchMethodException ex) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Could not find method " + methodName
							+ " for HttpServletRequest");
				}
			}
			this.arguments.put(methodName, args);
			this.patterns.put(methodName, Pattern.compile(value));
			if (LOG.isDebugEnabled()) {
				if (this.arguments.get(methodName) != null) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Stored method name = "
								+ methodName
								+ ", pattern = "
								+ value
								+ " with these arguments "
								+ Arrays.asList((Object[]) this.arguments
										.get(methodName)));
					}
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Stored method name = " + methodName
								+ ", pattern = " + value + " with no arguments");
					}
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
		Set<Map.Entry<String, Method>> entries = null;
		if (request instanceof HttpServletRequest) {
			entries = this.httpServletMethods.entrySet();
		} else {
			entries = this.servletMethods.entrySet();
		}
		for (Map.Entry<String, Method> entry : entries) {
			final String methodName = entry.getKey();
			if (LOG.isDebugEnabled()) {
				if (this.arguments.get(methodName) != null) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Calling "
								+ methodName
								+ " with these arguments "
								+ Arrays.asList((Object[]) this.arguments
										.get(methodName)));
					}
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Calling " + methodName
								+ " with no arguments");
					}
				}
			}

			String methodResult = null;
			try {
				methodResult = String.valueOf(entry.getValue().invoke(request,
						(Object[]) this.arguments.get(methodName)));
				if (LOG.isDebugEnabled()) {
					LOG.debug(methodName + " returned " + methodResult);
				}
			} catch (InvocationTargetException e) {
				if (LOG.isErrorEnabled()) {
					LOG.error("Could not invoke method " + methodName, e);
				}
			} catch (IllegalAccessException e) {
				if (LOG.isErrorEnabled()) {
					LOG.error("Could not access method " + methodName, e);
				}
			}
			if (!this.verifyResult(methodName, methodResult)) {
				if (response instanceof HttpServletResponse) {
					((HttpServletResponse) response).sendError(
							HttpServletResponse.SC_FORBIDDEN,
							"Request blocked by filter");
					return;
				} else {
					throw new ServletException("Request blocked by filter");
				}
			}
		}
		chain.doFilter(request, response);
	}

	/**
	 * This verifies that the supplied result matches the expected result for
	 * the supplied method name. This method returns true if result is null;
	 *
	 * @param name
	 *            of invoked method
	 * @param result
	 *            of method invocation
	 *
	 * @return whether the supplied result is valid
	 */
	private boolean verifyResult(final String name, final String result) {
		boolean success = false;
		if (result != null) {
			final Pattern pattern = this.patterns.get(name);
			if (!pattern.matcher(result).matches()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug(result + " does not match " + pattern.pattern());
				}
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug(result + " matches " + pattern.pattern());
				}
				success = true;
			}
		} else {
			success = true;
		}
		return success;
	}

	/**
	 * Called by the web container to indicate to a filter that it is being
	 * taken out of service.
	 */
	public void destroy() {
	}
}