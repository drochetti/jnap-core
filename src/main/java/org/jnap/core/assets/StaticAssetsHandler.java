/*
 * StaticAssetsHandler.java created on 2012-08-29
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;
import org.springframework.web.util.WebUtils;

/**
 * @author Daniel Rochetti
 */
public class StaticAssetsHandler implements ServletContextAware, PriorityOrdered {

	protected static Logger logger = LoggerFactory.getLogger(StaticAssetsHandler.class);

	protected static Map<String, String> availableStaticAssets = new ConcurrentHashMap<String, String>();

	private boolean monitor = false;

	private long monitorInterval = 2000L;

	protected String encoding = "utf-8";

	protected Resource path;

	protected String source = "*.*";

	protected String destination = null;

	protected boolean compress = false;

	protected String compressedFilename = "{0}.min.{1}";

	protected ResourcePatternResolver resourceResolver;

	private FileAlterationMonitor fileMonitor;

	protected ServletContext servletContext;

	protected int order = Integer.MAX_VALUE;

	@PostConstruct
	public void init() throws Exception {
		Assert.notNull(servletContext);
		this.resourceResolver = new ServletContextResourcePatternResolver(this.servletContext);
		if (this.shouldInit()) {
			this.handle();
			if (monitor) {
				fileMonitor = new FileAlterationMonitor(this.monitorInterval);
				FileAlterationObserver observer = new FileAlterationObserver(path.getFile());
				observer.addListener(new FileAlterationListenerAdaptor() {
					public void onFileCreate(File file) {
						StaticAssetsHandler.this.handle();
					}
					public void onFileChange(File file) {
						StaticAssetsHandler.this.handle();
					}
					public void onFileDelete(File file) {
						StaticAssetsHandler.this.handle();
					}
				});
				fileMonitor.addObserver(observer);
				fileMonitor.start();
				logger.info(getClass().getSimpleName() + " started and watching for file changes...");
			}
			logger.info(getClass().getSimpleName() + " executed!");
		}
	}

	@PreDestroy
	public void destroy() {
		if (fileMonitor != null) {
			try {
				fileMonitor.stop();
			} catch (Exception e) {
				logger.warn(e.getMessage());
			}
		}
	}

	protected boolean shouldInit() {
		try {
			return path != null && path.getFile().isDirectory();
		} catch (IOException e) {
			return false;
		}
	}

	public void handle() {
		try {
			Resource[] resources = this.resourceResolver.getResources(source);
			if (destination != null) {
				Resource destRes = new ServletContextResource(servletContext, destination);
				resetResource(destRes);
				BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(destRes.getFile(),
						this.encoding, true));
				for (Resource resource : resources) {
					IOUtils.copy(resource.getInputStream(), writer);
				}
				IOUtils.closeQuietly(writer);
				resources = new Resource[1];
				resources[0] = destRes;
			}

			for (Resource resource : resources) {
//				doHandle(resource);
//				File file = resource.getFile();
				String digest = DigestUtils.shaHex(resource.getInputStream());
				availableStaticAssets.put(resource.getFilename(), digest);
				if (compress) {
					Resource compressedResource = doCompression(resource);
					digest =  DigestUtils.shaHex(compressedResource.getInputStream());
					availableStaticAssets.put(compressedResource.getFilename(), digest);
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	protected Resource doCompression(Resource resource) throws IOException {
		throw new UnsupportedOperationException("");
	}

	protected void resetResource(Resource resource) throws IOException {
		File file = null;
		if (!resource.exists()) {
			ServletContextResource servletResource = (ServletContextResource) resource;
			 file = new File(WebUtils.getRealPath(servletContext, "/") + servletResource.getPath());
		} else {
			file = resource.getFile();
		}
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
	}

	public void setMonitor(boolean monitor) {
		this.monitor = monitor;
	}

	public void setMonitorInterval(long monitorInterval) {
		this.monitorInterval = monitorInterval;
	}

	public void setPath(Resource path) {
		this.path = path;
	}

	public void setSource(String patterns) {
		this.source = patterns;
	}

	public void setDestination(String destFile) {
		this.destination = destFile;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public void setCompressedFilename(String compressedFilename) {
		this.compressedFilename = compressedFilename;
	}

	public void setResourceResolver(ResourcePatternResolver resourceResolver) {
		this.resourceResolver = resourceResolver;
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
