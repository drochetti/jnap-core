/*
 * FreemarkerEmail.java created on 2010-10-16
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

import javax.mail.internet.MimeMessage;


/**
 * @author Daniel Rochetti
 */
public abstract class TemplateEmail extends Email {

	private String headerTemplateName;
	private String bodyTemplateName;
	private String footerTemplateName;


	public TemplateEmail() {
		super();
		setMixedContent(true);
	}

	@Override
	public void prepare(MimeMessage mimeMessage) throws Exception {
		this.processTemplate();
		super.prepare(mimeMessage);
	}

	/**
	 * 
	 */
	protected abstract void processTemplate();

	public String getHeaderTemplateName() {
		return headerTemplateName;
	}

	public void setHeaderTemplateName(String headerTemplateName) {
		this.headerTemplateName = headerTemplateName;
	}

	public String getBodyTemplateName() {
		return bodyTemplateName;
	}

	public void setBodyTemplateName(String templateName) {
		this.bodyTemplateName = templateName;
	}

	public String getFooterTemplateName() {
		return footerTemplateName;
	}

	public void setFooterTemplateName(String footerTemplateName) {
		this.footerTemplateName = footerTemplateName;
	}

}