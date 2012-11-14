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
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * @author Daniel Rochetti
 *
 */
public class JavascriptAssetsHandler extends YuiCompressorAssetsHandler {

	protected boolean doNotObfuscate = false;

	protected boolean preserveAllSemiColons = false;

	protected boolean disableOptimizations = false;

	public JavascriptAssetsHandler() {
		setSource("*.js");
	}

	@Override
	protected void runYuiCompressor(File sourceFile, File file) throws IOException {
		Reader reader = new FileReader(sourceFile);
		JavaScriptCompressor compressor = new JavaScriptCompressor(reader, getErrorReporter());
		FileWriter out = new FileWriter(file);
		compressor.compress(out, linebreakColumn, doNotObfuscate, false, preserveAllSemiColons, disableOptimizations);
		IOUtils.closeQuietly(out);
		IOUtils.closeQuietly(reader);
	}

	protected ErrorReporter getErrorReporter() {
		return new ErrorReporter() {
			@Override
			public void warning(String message, String sourceName, int line,
					String lineSource, int lineOffset) {
				logger.warn(message);
			}
			@Override
			public EvaluatorException runtimeError(String message, String sourceName,
					int line, String lineSource, int lineOffset) {
				logger.error(message);
				return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
			}
			@Override
			public void error(String message, String sourceName, int line,
					String lineSource, int lineOffset) {
				logger.error(message);
			}
		};
	}

	public void setDoNotObfuscate(boolean doNotObfuscate) {
		this.doNotObfuscate = doNotObfuscate;
	}

	public void setPreserveAllSemiColons(boolean preserveAllSemiColons) {
		this.preserveAllSemiColons = preserveAllSemiColons;
	}

	public void setDisableOptimizations(boolean disableOptimizations) {
		this.disableOptimizations = disableOptimizations;
	}

}
