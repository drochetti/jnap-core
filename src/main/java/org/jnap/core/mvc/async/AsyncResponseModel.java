/*
 * AsyncResponseModel.java created on 2011-11-21
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
package org.jnap.core.mvc.async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atmosphere.annotation.Suspend;
import org.atmosphere.cpr.AtmosphereResourceEventListener;
import org.atmosphere.cpr.Broadcaster;
import org.jnap.core.mvc.ResponseModel;
import org.springframework.web.servlet.view.AbstractView;

/**
 * @author Daniel Rochetti
 */
public class AsyncResponseModel extends ResponseModel {

    private Suspend.SCOPE scope;
    private boolean outputComments;
    private boolean resumeOnBroadcast;
    private Collection<AtmosphereResourceEventListener> listeners;
	private Broadcaster broadcaster;

	protected AsyncResponseModel() {
		super(new AbstractView() {
			@Override
			protected void renderMergedOutputModel(Map<String, Object> model,
					HttpServletRequest request, HttpServletResponse response)
					throws Exception {
				// do nothing, async stream will handle the response
			}
		});
		init();
	}

	/**
	 * 
	 */
	protected void init() {
		this.scope = Suspend.SCOPE.APPLICATION;
		this.outputComments = true;
		this.resumeOnBroadcast = false;
		this.listeners = new ArrayList<AtmosphereResourceEventListener>();
	}

	public static AsyncResponseModel ok() {
		return new AsyncResponseModel();
	}

	public AsyncResponseModel addListener(AtmosphereResourceEventListener listener) {
		this.listeners.add(listener);
		return this;
	}

	public AsyncResponseModel scope(Suspend.SCOPE scope) {
		this.scope = scope;
		return this;
	}

	public AsyncResponseModel broadcaster(Broadcaster broadcaster) {
		this.broadcaster = broadcaster;
		return this;
	}

	public Suspend.SCOPE getScope() {
		return scope;
	}

	public boolean isOutputComments() {
		return outputComments;
	}

	public boolean isResumeOnBroadcast() {
		return resumeOnBroadcast;
	}

	public Collection<AtmosphereResourceEventListener> getListeners() {
		return listeners;
	}

	public Broadcaster getBroadcaster() {
		return broadcaster;
	}

}
