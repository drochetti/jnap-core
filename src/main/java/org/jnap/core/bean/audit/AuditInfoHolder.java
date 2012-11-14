/*
 * AuditInfoHolder.java created on 25/06/2010
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
public final class AuditInfoHolder {

	private static ThreadLocal<String> ip = new ThreadLocal<String>();
	private static ThreadLocal<String> user = new ThreadLocal<String>();
	private static ThreadLocal<Date> when = new ThreadLocal<Date>();


	/**
	 * <code>Accessor</code> ("getter") method for threadlocal property <code>ip</code>.
	 */
	public static String getIp() {
		return AuditInfoHolder.ip.get();
	}

	/**
	 * <code>Mutator</code> ("setter") method for threadlocal property <code>ip</code>.
	 */
	public static void setIp(String ip) {
		AuditInfoHolder.ip.set(ip);
	}

	/**
	 * <code>Accessor</code> ("getter") method for threadlocal property <code>user</code>.
	 */
	public static String getUser() {
		return AuditInfoHolder.user.get();
	}

	/**
	 * <code>Mutator</code> ("setter") method for threadlocal property <code>user</code>.
	 */
	public static void setUser(String user) {
		AuditInfoHolder.user.set(user);
	}

	/**
	 * <code>Accessor</code> ("getter") method for threadlocal property <code>when</code>.
	 */
	public static Date getWhen() {
		return AuditInfoHolder.when.get();
	}

	/**
	 * <code>Mutator</code> ("setter") method for threadlocal property <code>when</code>.
	 */
	public static void setWhen(Date when) {
		AuditInfoHolder.when.set(when);
	}

	
}
