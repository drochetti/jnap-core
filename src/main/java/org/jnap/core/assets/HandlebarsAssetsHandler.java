/*
 * LessAssetsHandler.java created on 2012-08-29
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
package org.jnap.core.assets;

import static java.text.MessageFormat.format;

import java.io.BufferedWriter;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.support.ServletContextResource;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

/**
 * @author Daniel Rochetti
 */
public class HandlebarsAssetsHandler extends StaticAssetsHandler {

	private boolean bindToBackboneView = true;

	@Override
	public void handle() {
		final Handlebars handlebars = new Handlebars();
		try {
			Resource[] resources = this.resourceResolver.getResources(source);
			Resource destRes = new ServletContextResource(servletContext, destination);
			resetResource(destRes);
			BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(destRes.getFile(),
					this.encoding, true));

			writer.write("(function() {");
			writer.write(IOUtils.LINE_SEPARATOR);
			writer.write("var template = Handlebars.template, ");
			writer.write("templates = Handlebars.templates = Handlebars.templates || {};");
			writer.write(IOUtils.LINE_SEPARATOR);
			final Set<String> templateNames = new TreeSet<String>();
			for (Resource resource : resources) {
				Template template = handlebars.compile(StringUtils.trimToEmpty(
						IOUtils.toString(resource.getInputStream())));
				final String templateName = FilenameUtils.getBaseName(resource.getFilename());
				templateNames.add(templateName);
				writer.write("templates[\"" + templateName + "\"] = ");
				writer.write("template(");
				writer.write(template.toJavaScript());
				writer.write(");");
				writer.write(IOUtils.LINE_SEPARATOR);
			}

			writer.write(IOUtils.LINE_SEPARATOR);
			if (this.bindToBackboneView) {
				writer.write("$(function() {");
				writer.write(IOUtils.LINE_SEPARATOR);
				for (String templateName : templateNames) {
					writer.write(format("if (window[\"{0}\"]) {0}.prototype.template " +
							"= templates[\"{0}\"];", templateName));
					writer.write(IOUtils.LINE_SEPARATOR);
				}
				writer.write("});");
				writer.write(IOUtils.LINE_SEPARATOR);
			}

			writer.write("})();");
			IOUtils.closeQuietly(writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected boolean shouldInit() {
		return super.shouldInit()
				&& ClassUtils.isPresent("com.github.jknack.handlebars.Handlebars",
						this.getClass().getClassLoader());
	}

	public void setBindToBackboneView(boolean bindToBackboneView) {
		this.bindToBackboneView = bindToBackboneView;
	}

}
