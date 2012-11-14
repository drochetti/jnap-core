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

import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.support.ServletContextResource;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessOptions;

/**
 * @author Daniel Rochetti
 */
public class LessAssetsHandler extends StaticAssetsHandler {

	private boolean css;

	@Override
	public void handle() {
		LessEngine less = new LessEngine(getOptions());
		try {
			Resource sourceRes = new ServletContextResource(servletContext, source);
			Resource destRes = new ServletContextResource(servletContext, destination);
			resetResource(destRes);
			less.compile(sourceRes.getFile(), destRes.getFile());
		} catch (Exception e) {
			logger.error("Error compiling LESS file!", e);
		}
	}

	private LessOptions getOptions() {
		LessOptions options = new LessOptions();
		options.setCharset(this.encoding);
		options.setCss(this.css);
		return options;
	}

	@Override
	protected boolean shouldInit() {
		return super.shouldInit()
				&& ClassUtils.isPresent("com.asual.lesscss.LessEngine", this.getClass().getClassLoader());
	}

	public void setCss(boolean css) {
		this.css = css;
	}

}
