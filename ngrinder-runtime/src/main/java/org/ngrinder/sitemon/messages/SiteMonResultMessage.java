/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.sitemon.messages;

import java.util.LinkedList;
import java.util.List;

import org.ngrinder.sitemon.model.SiteMonResult;

import net.grinder.communication.Message;

/**
 * Message of sitemon execute result.
 * 
 * @author Gisoo Gwon
 */
public class SiteMonResultMessage implements Message {

	private static final long serialVersionUID = -2658696439516448216L;
	
	private final List<SiteMonResult> results;

	public SiteMonResultMessage() {
		this.results = new LinkedList<SiteMonResult>();
	}

	public SiteMonResultMessage(List<SiteMonResult> results) {
		this.results = new LinkedList<SiteMonResult>(results);
	}
	
	public void addAll(List<SiteMonResult> results) {
		this.results.addAll(results);
	}

	public List<SiteMonResult> getResults() {
		return results;
	}

}