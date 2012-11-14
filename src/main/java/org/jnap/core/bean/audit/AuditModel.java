/*
 * AuditModel.java created on 24/06/2010
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
package org.jnap.core.bean.audit;

import java.util.Date;


/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public interface AuditModel {

	public void setWhen(Date when);

	public void setIp(String ip);

	public void setUser(String user);

	public void setEvent(AuditEvent event);

	public void setEntityName(String entityName);

}
