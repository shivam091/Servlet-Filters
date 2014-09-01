package org.servlet.filters.Servlets;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * HTTP servlet request wrapper that provides input streams and readers based on
 * a byte array whose bytes were read from the wrapped request stream.
 *
 * @author shivam
 */
public class ByteArrayRequestWrapper extends HttpServletRequestWrapper {

	/** HTTP request body write buffer size. */
	private static final int BUFFER_SIZE = 8092;

	/** Byte buffer backing all streams and readers. */
	private final byte[] buffer;

	/**
	 * Creates a new instance around the given request. The data, if any, in the
	 * request will be read and assigned to an internal buffer that will be used
	 * to back streams, {@link #getInputStream()}, and readers
	 * {@link #getReader()} obtained from this class.
	 *
	 * @param request
	 *            HTTP request to wrap.
	 */
	public ByteArrayRequestWrapper(final HttpServletRequest request) {
		super(request);
		buffer = readRequestData(request);
	}

	/**
	 * Gets the request body using the platform default character encoding to
	 * convert the raw bytes of the input stream to characters.
	 *
	 * @return HTTP request body as a string.
	 */
	public String getRequestBodyAsString() {
		return new String(buffer);
	}

	/**
	 * Gets a servlet input stream backed by a byte array.
	 *
	 * @return A memory-backed input stream.
	 *
	 * @throws IOException
	 *             On stream access failure.
	 */
	@Override
	public ServletInputStream getInputStream() throws IOException {
		return new ByteArrayServletInputStream(buffer);
	}

	/**
	 * Gets a buffered reader backed by a byte array.
	 *
	 * @return A memory-backed reader.
	 *
	 * @throws IOException
	 *             On reader access failure.
	 */
	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(
				new ByteArrayInputStream(buffer)));
	}

	/**
	 * Reads data from the entire request input stream and returns it in an
	 * array.
	 *
	 * @param request
	 *            HTTP request.
	 *
	 * @return Bytes read from input stream.
	 */
	private byte[] readRequestData(final HttpServletRequest request) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final byte[] buf = new byte[BUFFER_SIZE];
		int size = 0;
		try {
			final InputStream in = request.getInputStream();
			while ((size = in.read(buf)) > 0) {
				baos.write(buf, 0, size);
			}
		} catch (IOException e) {
			throw new RuntimeException("Error reading HTTP request stream.", e);
		}
		return baos.toByteArray();
	}
}