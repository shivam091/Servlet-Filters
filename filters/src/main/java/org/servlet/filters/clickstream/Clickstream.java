package org.servlet.filters.clickstream;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * <code>Class that operates like a struct to hold data.</code>
 * 
 * @author shivam
 *
 */
public class Clickstream implements Serializable {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("rawtypes")
	List clickstream = new ArrayList();
	String hostname = null;
	HttpSession session = null;
	String initialReferrer = null;
	Date start = new Date();
	Date lastRequest = new Date();
	boolean bot = false;

	public Clickstream() {
	}

	@SuppressWarnings("unchecked")
	public void addRequest(HttpServletRequest request) {
		lastRequest = new Date();

		if (hostname == null) {
			hostname = request.getRemoteHost();
			session = request.getSession();
		}

		// if this is the first request in the click stream
		if (clickstream.size() == 0) {
			// setup initial referrer
			if (request.getHeader("REFERER") != null) {
				initialReferrer = request.getHeader("REFERER");
			} else {
				initialReferrer = "";
			}

			// decide whether this is a bot
			bot = BotChecker.isBot(request, this);
		}

		clickstream.add(request.getServerName()
				+ (request.getServerPort() != 80 ? ":"
						+ request.getServerPort() : "")
				+ request.getRequestURI()
				+ (request.getQueryString() != null ? "?"
						+ request.getQueryString() : ""));

		// System.out.println(this.toString());
	}

	public String getHostname() {
		return hostname;
	}

	public boolean isBot() {
		return bot;
	}

	public void setBot(boolean value) {
		this.bot = value;
	}

	public HttpSession getSession() {
		return session;
	}

	public String getInitialReferrer() {
		return initialReferrer;
	}

	public Date getStart() {
		return start;
	}

	public Date getLastRequest() {
		return lastRequest;
	}

	@SuppressWarnings("rawtypes")
	public List getStream() {
		return clickstream;
	}

	public String toString() {
		StringBuffer output = new StringBuffer();

		output.append("Clickstream for: " + hostname + "\n");
		output.append("Session ID: "
				+ (session != null ? session.getId() + "" : "") + "\n");
		output.append("Initial Referrer: " + initialReferrer + "\n");
		output.append("Stream started: " + start + "\n");
		output.append("Last request: " + lastRequest + "\n");

		long streamLength = lastRequest.getTime() - start.getTime();

		output.append("Stream length:"
				+ (streamLength > 3600000 ? " " + (streamLength / 3600000)
						+ " hours" : "")
				+ (streamLength > 60000 ? " " + ((streamLength / 60000) % 60)
						+ " minutes" : "")
				+ (streamLength > 1000 ? " " + ((streamLength / 1000) % 60)
						+ " seconds" : "") + "\n");

		Iterator<?> clickstreamIt = clickstream.iterator();

		int count = 0;
		while (clickstreamIt.hasNext()) {
			count++;

			output.append(count + ": " + clickstreamIt.next() + "\n");
		}

		return output.toString();
	}
}
