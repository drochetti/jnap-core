package org.jnap.core.mvc.view;

import static java.text.MessageFormat.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.util.Assert;
import org.springframework.web.servlet.View;

/**
 * @author Daniel Rochetti
 */
public class StreamView implements View {

	protected static final String CONTENT_DISPOSITION_INLINE = "inline";

	protected static final String CONTENT_DISPOSITION_ATTACHMENT = "attachment; filename=\"{0}\"";

	private String contentType;
	private InputStream inputStream;
	private String contentDisposition = CONTENT_DISPOSITION_INLINE;
	private String fileName;
	private int bufferSize = 1024;
	

	public StreamView(String contentType, InputStream inputStream) {
		this.contentType = contentType;
		this.inputStream = inputStream;
	}

	public StreamView(String contentType, File file) {
		this.contentType = contentType;
		try {
			this.inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("Could open read stream to file", e);
		}
		this.setFileName(file.getName());
	}

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public void render(Map<String, ?> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		Assert.notNull(this.inputStream);
		OutputStream out = response.getOutputStream();

		// writting the output
		try {
			byte[] buffer = new byte[this.bufferSize];
			int totalLength = 0;
			int length = 0;
			while ((length = this.inputStream.read(buffer)) != -1) {
				out.write(buffer, 0, length);
				totalLength += length;
			}
	
			// configuring response
			response.setContentLength(totalLength);
			response.setContentType(this.contentType);
			response.addHeader("Content-Disposition", this.contentDisposition);
		} finally {
			IOUtils.closeQuietly(this.inputStream);
			out.flush();
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * 
	 * @see #asAttachment(String)
	 * @return the {@code StreamView} itself for fluent method call.
	 */
	public StreamView asAttachment() {
		String fileName = this.fileName != null ? this.fileName : "file";
		setContentDisposition(format(CONTENT_DISPOSITION_ATTACHMENT, fileName));
		return this;
	}

	/**
	 * Serve the stream as an attachment.
	 * @param fileName
	 * @return the {@code StreamView} itself for fluent method call.
	 */
	public StreamView asAttachment(String fileName) {
		setFileName(fileName);
		return asAttachment();
	}

	public StreamView asInline() {
		setContentDisposition(CONTENT_DISPOSITION_INLINE);	
		return this;
	}

	public void setContentDisposition(String contentDisposition) {
		this.contentDisposition = contentDisposition;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

}
