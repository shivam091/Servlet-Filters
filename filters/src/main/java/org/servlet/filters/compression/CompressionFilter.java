package org.servlet.filters.compression;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <code>automatically compresses the response output stream, improving bandwidth utilization and 
 * providing a great demonstration of response object wrapping.</code>
 * <p>
 * <code>The strategy of the CompressionFilter class is to examine the request headers to determine 
 * if the client supports compression, and if so, wrap the response object with a custom response 
 * whose getOutputStream() and getWriter() methods have been customized to utilize a compressed 
 * output stream.</code>
 * </p>
 * 
 * <p>
 * <code>When called before the filter is put into service, 
 * this init() method looks for the presence of a filter init parameter to determine 
 * the compression threshold -- the amount of bytes that must be in the response before 
 * it's worth compressing.</code>
 * </p>
 * 
 * <p>
 * <code>The doFilter() method, called when the request comes in, retrieves the Accept-Encoding header, 
 * and if the header value includes gzip, wraps the response
 * and sets the threshold on the wrapper:</code>
 * </p>
 * 
 * @author shivam
 *
 */
public class CompressionFilter implements Filter {

	@SuppressWarnings("unused")
	private FilterConfig config = null;

	protected int compressionThreshold;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(FilterConfig filterConfig) {
		config = filterConfig;
		compressionThreshold = 0;
		if (filterConfig != null) {
			String str = filterConfig.getInitParameter("compressionThreshold");
			if (str != null) {
				compressionThreshold = Integer.parseInt(str);
			} else {
				compressionThreshold = 0;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
		this.config = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		boolean supportCompression = false;
		if (request instanceof HttpServletRequest) {
			@SuppressWarnings("rawtypes")
			Enumeration e = ((HttpServletRequest) request)
					.getHeaders("Accept-Encoding");
			while (e.hasMoreElements()) {
				String name = (String) e.nextElement();
				if (name.indexOf("gzip") != -1) {
					supportCompression = true;
				}
			}
		}
		if (!supportCompression) {
			chain.doFilter(request, response);
		} else {
			if (response instanceof HttpServletResponse) {
				CompressionResponseWrapper wrappedResponse = new CompressionResponseWrapper(
						(HttpServletResponse) response);
				wrappedResponse.setCompressionThreshold(compressionThreshold);
				chain.doFilter(request, wrappedResponse);
			}
		}
	}
}