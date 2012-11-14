/*
 * Email.java created on 2011-01-16
 *
 * Created by Brushing Bits Labs
 * http://www.brushingbits.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jnap.core.email;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jnap.core.mvc.i18n.CurrentLocaleHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public class Email extends SimpleMailMessage implements MimeMessagePreparator, InitializingBean {

	private String htmlText;
	private boolean mixedContent = false;
	private EmailAccountInfo accountInfo;
	private Map<String, Resource> inlineResources;
	private Map<String, Resource> attachments;
	private Map<String, String> headers = new HashMap<String, String>();
	private Map<String, Object> defaultProperties = new HashMap<String, Object>();
	private MessageSource messageSource;
	private Locale locale;
	private String encoding = "utf-8";

	protected Map<String, Object> values = new HashMap<String, Object>();

	@Override
	public void afterPropertiesSet() throws Exception {
		this.values.putAll(defaultProperties);
		this.locale = CurrentLocaleHolder.get() == null
				? Locale.getDefault() : CurrentLocaleHolder.get();
		put("subject", getMessage(getSubject()));
	}

	public Object put(String key, Object value) {
		return values.put(key, value);
	}

	public void addTo(String to) {
		setTo((String[]) ArrayUtils.add(getTo(), to));
	}

	public void addCc(String cc) {
		setCc((String[]) ArrayUtils.add(getCc(), cc));
	}

	public void addBcc(String bcc) {
		setBcc((String[]) ArrayUtils.add(getBcc(), bcc));
	}

	public void prepare(MimeMessage mimeMessage) throws Exception {
		final EmailAccountInfo acc = getAccountInfo();
		boolean multipart = StringUtils.isNotBlank(getHtmlText())
				|| (getInlineResources() != null && getInlineResources().size() > 0)
				|| (getAttachments() != null && getAttachments().size() > 0);
		
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, multipart);
		if (acc.getFromName() != null) {
			helper.setFrom(acc.getFromEmailAddress(), acc.getFromName());
		} else {
			this.setFrom(acc.getFromEmailAddress());
		}
		helper.setTo(getTo());
		if (getCc() != null) {
			helper.setCc(getCc());
		}
		if (getBcc() != null) {
			helper.setBcc(getBcc());
		}
		helper.setSentDate(new Date());
		mimeMessage.setSubject(getMessage(getSubject()), this.encoding);

		// sender info
		if (acc != null && StringUtils.isNotBlank(acc.getFromName())) {
			helper.setFrom(acc.getFromEmailAddress(), getMessage(acc.getFromName()));
		} else {
			helper.setFrom(acc.getFromEmailAddress());
		}
		if (acc != null && StringUtils.isNotBlank(acc.getReplyToEmailAddress())) {
			if (StringUtils.isNotBlank(acc.getReplyToName())) {
				helper.setReplyTo(acc.getReplyToEmailAddress(), acc.getReplyToName());
			} else {
				helper.setReplyTo(acc.getReplyToEmailAddress());
			}
		}

		final boolean hasHtmlText = StringUtils.isNotBlank(getHtmlText());
		final boolean hasText = StringUtils.isNotBlank(getText());
		if (hasHtmlText && hasText) {
			helper.setText(getText(), getHtmlText());
		} else if (hasHtmlText || hasText) {
			helper.setText(hasHtmlText ? getHtmlText() : getText());
		}

		// set headers
		final Map<String, String> mailHeaders = this.getHeaders();
		for (String header : mailHeaders.keySet()) {
			mimeMessage.addHeader(header, mailHeaders.get(header));			
		}

		// add inline resources
		final Map<String, Resource> inlineRes = this.getInlineResources();
		if (inlineRes != null) {
			for (String cid : inlineRes.keySet()) {
				helper.addInline(cid, inlineRes.get(cid));
			}
		}
		// add attachments
		final Map<String, Resource> attachments = this.getAttachments();
		if (attachments != null) {
			for (String attachmentName : attachments.keySet()) {
				helper.addAttachment(attachmentName, attachments.get(attachmentName));
			}
		}
	}

	private String getMessage(String key) {
		return getMessageSource() != null
				? getMessageSource().getMessage(key, new Object[] {}, getLocale())
				: key;
	}

	/**
	 * Simple RegEx replace used to remove all HTML tags.
	 * 
	 * @param html Text with HTML content.
	 * @return the plain text, without the HTML tags.
	 */
	protected String extractTextFromHtml(String html) {
		return html.replaceAll("\\<.*?>", StringUtils.EMPTY);
	}

	public EmailAccountInfo getAccountInfo() {
		return accountInfo;
	}

	public void setAccountInfo(EmailAccountInfo accountInfo) {
		this.accountInfo = accountInfo;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public String getHtmlText() {
		return htmlText;
	}

	public void setHtmlText(String htmlText) {
		this.htmlText = htmlText;
	}

	public Map<String, Resource> getInlineResources() {
		return inlineResources;
	}

	public void setInlineResources(Map<String, Resource> inlineItems) {
		this.inlineResources = inlineItems;
	}

	public Map<String, Resource> getAttachments() {
		return attachments;
	}

	public void addInline(String cid, Resource inlineData) {
		if (this.inlineResources == null) {
			this.inlineResources = new HashMap<String, Resource>();
		}
		this.inlineResources.put(cid, inlineData);
	}

	public void setAttachments(Map<String, Resource> attachments) {
		this.attachments = attachments;
	}

	public void addAttachment(String name, Resource attachment) {
		if (this.attachments == null) {
			this.attachments = new HashMap<String, Resource>();
		}
		this.attachments.put(name, attachment);
	}

	public boolean isMixedContent() {
		return mixedContent;
	}

	public void setMixedContent(boolean mixedContent) {
		this.mixedContent = mixedContent;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public Map<String, Object> getDefaultProperties() {
		return defaultProperties;
	}

	public void setDefaultProperties(Map<String, Object> defaultProperties) {
		this.defaultProperties = defaultProperties;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public void addHeader(String headerName, String value) {
		this.headers.put(headerName, value);
	}

}
