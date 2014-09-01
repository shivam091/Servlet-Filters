<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="org.servlet.filters.clickstream.Clickstream"%>
<%@ page info="This JSP provides visitors details." %>

<%
	if (request.getParameter("sid") == null) {
		response.sendRedirect("clickstreams.jsp");
		return;
	}

	Map<?, ?> clickstreams = (Map<?, ?>) application.getAttribute("clickstreams");

	Clickstream stream = null;

	if (clickstreams.get(request.getParameter("sid")) != null) {
		stream = (Clickstream) clickstreams.get(request
				.getParameter("sid"));
	}

	if (stream == null) {
		response.sendRedirect("clickstreams.jsp");
		return;
	}
%>

<font face="Verdana" size="-1">
	<div align="right">
		<a href="clickstreams.jsp">All streams</a>
	</div>

	<h1>
		Clickstream for
		<%=stream.getHostname()%></h1> <b>Initial Referrer</b>: <a
	href="<%=stream.getInitialReferrer()%>"><%=stream.getInitialReferrer()%></a><br>
	<b>Hostname</b>: <%=stream.getHostname()%><br> <b>Session ID</b>:
	<%=request.getParameter("sid")%><br> <b>Bot</b>: <%=stream.isBot() ? "Yes" : "No"%><br>
	<b>Stream Start</b>: <%=stream.getStart()%><br> <b>Last
		Request</b>: <%=stream.getLastRequest()%><br> <%
 	long streamLength = stream.getLastRequest().getTime()
 			- stream.getStart().getTime();
 %> <b>Session Length</b>: <%=(streamLength > 3600000 ? " " + (streamLength / 3600000)
					+ " hours" : "")
					+ (streamLength > 60000 ? " "
							+ ((streamLength / 60000) % 60) + " minutes" : "")
					+ (streamLength > 1000 ? " " + ((streamLength / 1000) % 60)
							+ " seconds" : "")%><br> <b># of Requests</b>: <%=stream.getStream().size()%>

	<p>
		<b>Click stream</b>:<br>
		<%
			Iterator<?> clickstreamIt = stream.getStream().iterator();

			int count = 0;
			while (clickstreamIt.hasNext()) {
				count++;
				String click = (String) clickstreamIt.next();
		%>
		<%=count%>: <a href="http://<%=click%>"><%=click%></a><br>
		<%
} 
%>
	</p>
</font>