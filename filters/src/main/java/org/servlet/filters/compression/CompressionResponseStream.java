package org.servlet.filters.compression;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * <code>Any call to getOutputStream() or getWriter() returns an object using CompressResponseStream 
 * under the covers. It extends ServletOutputStream and compresses the stream using the 
 * java.util.zip.GZIPOutputStream class.</code>
 * 
 * @author shivam
 *
 */
public class CompressionResponseStream extends ServletOutputStream {

	public CompressionResponseStream(HttpServletResponse response)
			throws IOException {
		super();
		closed = false;
		commit = false;
		count = 0;
		this.response = response;
		this.output = response.getOutputStream();
	}

	// The threshold number which decides to compress or not.
	// Users can configure in web.xml to set it to fit their needs.
	protected int compressionThreshold = 0;

	// The buffer through which all of our output bytes are passed.
	protected byte[] buffer = null;

	// Is it big enough to compress?
	protected boolean compressionThresholdReached = false;

	// The number of data bytes currently in the buffer.
	protected int bufferCount = 0;

	// The underlying gzip output stream to which we should write data.
	protected GZIPOutputStream gzipstream = null;

	// Has this stream been closed?
	protected boolean closed = false;

	// Should we commit the response when we are flushed?
	protected boolean commit = true;

	// The number of bytes which have already been written to this stream.
	protected int count = 0;

	// The content length past which we will not write, or -1 if there is
	// no defined content length.
	protected int length = -1;

	// The response with which this servlet output stream is associated.
	protected HttpServletResponse response = null;

	// The underlying servket output stream to which we should write data.
	protected ServletOutputStream output = null;

	boolean getCommit() {
		return commit;
	}

	void setCommit(boolean commit) {
		this.commit = commit;
	}

	protected void setBuffer(int threshold) {
		compressionThreshold = threshold;
		buffer = new byte[compressionThreshold];
	}

	// Close this output stream, causing any buffered data to be flushed and
	// any further output data to throw an IOException.
	public void close() throws IOException {
		if (closed) {
			throw new IOException("This output stream has already been closed");
		}
		if (gzipstream != null) {
			gzipstream.close();
		}
		flush();
		closed = true;
	}

	public void flush() throws IOException {
		if (closed) {
			throw new IOException("Cannot flush a closed output stream");
		}
		if (commit) {
			if (bufferCount > 0) {
				output.write(buffer, 0, bufferCount);
				bufferCount = 0;
			}
		}
	}

	public void flushToGZip() throws IOException {
		if (bufferCount > 0) {
			gzipstream.write(buffer, 0, bufferCount);
			bufferCount = 0;
		}
	}

	public void write(int b) throws IOException {
		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		if ((bufferCount >= buffer.length) || (count >= compressionThreshold)) {
			compressionThresholdReached = true;
		}

		if (compressionThresholdReached) {
			writeToGZip(b);
		} else {
			buffer[bufferCount++] = (byte) b;
			count++;
		}
	}

	public void writeToGZip(int b) throws IOException {
		if (gzipstream == null) {
			gzipstream = new GZIPOutputStream(output);
			flushToGZip();
			response.addHeader("Content-Encoding", "gzip");
		}
		gzipstream.write(b);
	}

	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	public void write(byte b[], int off, int len) throws IOException {
		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		if (len == 0) {
			return;
		}
		if (len <= (buffer.length - bufferCount)) {
			System.arraycopy(b, off, buffer, bufferCount, len);
			bufferCount += len;
			count += len;
			return;
		}

		// buffer full, start writing to gzipstream
		writeToGZip(b, off, len);
		count += len;
	}

	public void writeToGZip(byte b[], int off, int len) throws IOException {
		if (gzipstream == null) {
			gzipstream = new GZIPOutputStream(output);
			flushToGZip();
			response.addHeader("Content-Encoding", "gzip");
		}
		gzipstream.write(b, off, len);
	}

	boolean closed() {
		return closed;
	}

	void reset() {
		count = 0;
	}
}
