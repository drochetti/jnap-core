/*
 * YuiCompressorAssetsHandler.java created on 29/08/2012
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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.ServletContextResource;

/**
 * @author drochetti
 *
 */
public abstract class YuiCompressorAssetsHandler extends StaticAssetsHandler {

	protected int linebreakColumn = 10000;

	@Override
	protected Resource doCompression(Resource resource) throws IOException {
		File sourceFile = resource.getFile();
		Resource minifiedResource = new ServletContextResource(servletContext,
				format(((ServletContextResource) this.path).getPath() + compressedFilename,
						FilenameUtils.getBaseName(sourceFile.getName()),
						FilenameUtils.getExtension(sourceFile.getName())));
		resetResource(minifiedResource);
		runYuiCompressor(sourceFile, minifiedResource.getFile());
		return minifiedResource;
	}

	protected abstract void runYuiCompressor(File sourceFile, File file) throws IOException;

	public void setLinebreakColumn(int linebreakColumn) {
		this.linebreakColumn = linebreakColumn;
	}

}
