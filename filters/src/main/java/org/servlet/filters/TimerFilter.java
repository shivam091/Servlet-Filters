package org.servlet.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * <code>Servlet filter that records the duration of all requests.</code>
 * <p>
 * <code>When the server calls init(), the filter saves a reference to the config in its config variable, 
 * which is later used in the doFilter() method to retrieve the ServletContext. 
 * When the server calls doFilter(), the filter times how long the request handling takes and logs the time once processing has completed. 
 * This filter nicely demonstrates before- and after-request processing. Notice that the parameters to the doFilter() method are not HTTP-aware objects, 
 * so to call the HTTP-specific getRequestURI() method requires a cast of the request to an HttpServletRequest type.</code>
 * 
 * @author shivam
 *
 */
public class TimerFilter implements Filter {

	private FilterConfig config = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
		config = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		long before = System.currentTimeMillis();
		chain.doFilter(request, response);
		long after = System.currentTimeMillis();

		String name = "";
		if (request instanceof HttpServletRequest) {
			name = ((HttpServletRequest) request).getRequestURI();
		}
		config.getServletContext().log(name + ": " + (after - before) + "ms");
	}
}