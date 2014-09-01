package org.servlet.filters;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>ClientCertFilter</code> is a filter which can be used to restrict
 * access to a servlet by verifying aspects of the client certificate chain
 * given to the servlet.
 *
 * @author shivam
 */

public class ClientCertFilter implements Filter {

	/** Init param for setting requireCert. */
	public static final String REQUIRE_CERT = "requireCert";

	/** Init param for setting issuerDnPattern. */
	public static final String ISSUER_DN = "issuerDn";

	/** Init param for setting subjectDnPattern. */
	public static final String SUBJECT_DN = "subjectDn";

	/** Log for this class. */
	private static final Log LOG = LogFactory.getLog(ClientCertFilter.class);

	/** Whether a client cert is required by this filter. */
	private boolean requireCert;

	/** Pattern for comparing issuer dn. */
	private Pattern issuerDnPattern;

	/** Pattern for comparing subject dn. */
	private Pattern subjectDnPattern;

	/**
	 * Initialize this filter.
	 *
	 * @param config
	 *            <code>FilterConfig</code>
	 */
	public void init(final FilterConfig config) {
		this.requireCert = Boolean.valueOf(
				config.getInitParameter(REQUIRE_CERT)).booleanValue();
		if (LOG.isDebugEnabled()) {
			LOG.debug("requireCert = " + this.requireCert);
		}

		final String issuerDn = config.getInitParameter(ISSUER_DN);
		if (issuerDn != null) {
			this.issuerDnPattern = Pattern.compile(issuerDn);
		}

		final String subjectDn = config.getInitParameter(SUBJECT_DN);
		if (subjectDn != null) {
			this.subjectDnPattern = Pattern.compile(subjectDn);
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
		final X509Certificate[] certChain = (X509Certificate[]) request
				.getAttribute("javax.servlet.request.X509Certificate");
		if (LOG.isDebugEnabled()) {
			if (certChain != null && certChain[0] != null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Received the following client certificate: "
							+ certChain[0].getSubjectDN().getName());
				}
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Did not receive a client certificate");
				}
			}
		}

		if (certChain != null && certChain[0] != null) {
			final String issuer = certChain[0].getIssuerX500Principal()
					.getName();
			final String subject = certChain[0].getSubjectX500Principal()
					.getName();
			if (this.issuerDnPattern != null && this.subjectDnPattern != null) {
				if (this.issuerDnPattern.matcher(issuer).matches()
						&& this.subjectDnPattern.matcher(subject).matches()) {
					if (LOG.isDebugEnabled()) {
						LOG.debug(issuer + " matches "
								+ this.issuerDnPattern.pattern() + " and "
								+ subject + " matches "
								+ this.subjectDnPattern.pattern());
					}
					success = true;
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug(issuer + " does not match "
								+ this.issuerDnPattern.pattern() + " or "
								+ subject + " does not match "
								+ this.subjectDnPattern.pattern());
					}
				}
			} else if (this.issuerDnPattern != null) {
				if (this.issuerDnPattern.matcher(issuer).matches()) {
					if (LOG.isDebugEnabled()) {
						LOG.debug(issuer + " matches "
								+ this.issuerDnPattern.pattern());
					}
					success = true;
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug(issuer + " does not match "
								+ this.issuerDnPattern.pattern());
					}
				}
			} else if (this.subjectDnPattern != null) {
				if (this.subjectDnPattern.matcher(subject).matches()) {
					if (LOG.isDebugEnabled()) {
						LOG.debug(subject + " matches "
								+ this.subjectDnPattern.pattern());
					}
					success = true;
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug(subject + " does not match "
								+ this.subjectDnPattern.pattern());
					}
				}
			} else {
				success = true;
			}
		} else if (!this.requireCert) {
			success = true;
		}

		if (!success) {
			if (response instanceof HttpServletResponse) {
				((HttpServletResponse) response).sendError(
						HttpServletResponse.SC_FORBIDDEN,
						"Request blocked by filter");
				return;
			} else {
				throw new ServletException("Request blocked by filter");
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