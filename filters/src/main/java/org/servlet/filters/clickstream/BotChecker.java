package org.servlet.filters.clickstream;

import javax.servlet.http.HttpServletRequest;

/**
 * <code>Class that determines if a client is a robot (using simple logic, like "Did they request robots.txt?").</code>
 * 
 * @author shivam
 *
 */
public class BotChecker {
	public static String[] botHosts = { "inktomi.com", "inktomisearch.com",
			"googlebot.com", "linuxtoday.com.au" };

	public static boolean isBot(HttpServletRequest request, Clickstream stream) {
		String requestURI = request.getRequestURI();

		// if it requested robots.txt, it's a bot
		if (requestURI.indexOf("robots.txt") >= 0) {
			return true;
		}

		// it requested a RSS feed from our backend, it's a bot
		if (requestURI.indexOf("/backend/") >= 0) {
			return true;
		}

		for (int i = 0; i < botHosts.length; i++) {
			if (request.getRemoteHost().indexOf(botHosts[i]) >= 0) {
				return true;
			}
		}

		return false;
	}
}