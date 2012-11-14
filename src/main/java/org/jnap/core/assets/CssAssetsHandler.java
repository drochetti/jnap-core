/*
 * JavascriptAssetsHandler.java created on 29/08/2012
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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.IOUtils;

import com.yahoo.platform.yui.compressor.CssCompressor;

/**
 * @author Daniel Rochetti
 *
 */
public class CssAssetsHandler extends YuiCompressorAssetsHandler {

	public CssAssetsHandler() {
		setSource("*.css");
		setCompress(true);
	}

	@Override
	protected void runYuiCompressor(File sourceFile, File file) throws IOException {
		Reader reader = new FileReader(sourceFile);
		CssCompressor compressor = new CssCompressor(reader);
		FileWriter out = new FileWriter(file);
		compressor.compress(out, linebreakColumn);
		IOUtils.closeQuietly(out);
		IOUtils.closeQuietly(reader);
	}

}
