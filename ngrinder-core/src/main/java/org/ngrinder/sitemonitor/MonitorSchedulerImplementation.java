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
package org.ngrinder.sitemonitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.sitemonitor.messages.RegistScheduleMessage;
import org.ngrinder.util.AgentStateMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.grinder.engine.agent.SitemonitorScriptRunner;

/**
 * Manage process for execute sitemonitoring script.
 * 
 * @author Gisoo Gwon
 */
public class MonitorSchedulerImplementation implements MonitorScheduler {

	static final Logger LOGGER = LoggerFactory.getLogger("monitor scheduler impl");
	static final int THREAD_POOL_SIZE = 10;
	static final long DEFAULT_REPEAT_TIME = 60 * 1000;

	private final SitemonitorScriptRunner scriptRunner;
	private final AgentStateMonitor agentStateMonitor;

	private ExecutorService executor;
	private long repeatTime = DEFAULT_REPEAT_TIME;
	private boolean shutdown = false;

	Map<String, RegistScheduleMessage> sitemonitorMap = new HashMap<String, RegistScheduleMessage>();

	/**
	 * The constructor.
	 * Default repeat time is {@code DEFAULT_REPEAT_TIME}
	 * @param scriptRunner 
	 * @param agentStateMonitor
	 */
	public MonitorSchedulerImplementation(SitemonitorScriptRunner scriptRunner,
		AgentStateMonitor agentStateMonitor) {
		this(scriptRunner, agentStateMonitor, DEFAULT_REPEAT_TIME);
	}
	
	/**
	 * The constructor.
	 * 
	 * @param scriptRunner
	 * @param agentStateMonitor
	 * @param repeatTime
	 */
	public MonitorSchedulerImplementation(SitemonitorScriptRunner scriptRunner,
		AgentStateMonitor agentStateMonitor, long repeatTime) {
		this.agentStateMonitor = agentStateMonitor;
		this.scriptRunner = scriptRunner;
		this.repeatTime = repeatTime;
		executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		new ScriptRunnerDaemon().start();
	}

	/**
	 * @param sitemonitorId
	 * @param scriptname
	 */
	@Override
	public void regist(final RegistScheduleMessage message) {
		sitemonitorMap.put(message.getSitemonitorId(), message);
		agentStateMonitor.setRegistScriptCount(sitemonitorMap.size());
	}

	/**
	 * @param groupName
	 */
	@Override
	public void unregist(String sitemonitorId) {
		sitemonitorMap.remove(sitemonitorId);
		agentStateMonitor.clear();
		agentStateMonitor.setRegistScriptCount(sitemonitorMap.size());
	}

	public void setRepeatTime(long repeatTime) {
		this.repeatTime = repeatTime;
	}

	/**
	 * destroy process.
	 */
	@Override
	public void shutdown() {
		shutdown = true;
		scriptRunner.shutdown();
	}

	class ScriptRunnerDaemon extends Thread {

		ScriptRunnerDaemon() {
			setDaemon(true);
		}

		@Override
		public void run() {
			while (!shutdown) {
				LOGGER.error("Sitemonitor runner awake! regist sitemonitor cnt is {}",
					sitemonitorMap.size());

				long st = System.nanoTime();
				List<Future<Object>> futures = runScriptUsingThreadPool();
				waitScriptComplete(futures);
				long useTime = (System.nanoTime() - st) / 1000 / 1000 ;
				agentStateMonitor.recordUseTime(useTime);
				sleepForRepeatCycle(useTime);
			}
			System.err.println("Shut down ???");
		}

		private void sleepForRepeatCycle(long usedTime) {
			if (usedTime < repeatTime) {
				ThreadUtils.sleep(repeatTime - usedTime);
			}
		}

		private void waitScriptComplete(List<Future<Object>> futures) {
			for (Future<Object> future : futures) {
				try {
					future.get();
				} catch (Exception e) {
					LOGGER.error("script run failed {}", e.getMessage());
				}
			}
		}

		private List<Future<Object>> runScriptUsingThreadPool() {
			List<Future<Object>> futures = new LinkedList<Future<Object>>();
			for (Entry<String, RegistScheduleMessage> entry : sitemonitorMap.entrySet()) {
				final String sitemonitorId = entry.getKey();
				final RegistScheduleMessage message = entry.getValue();

				Callable<Object> task = new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						scriptRunner.runWorker(sitemonitorId, message.getScriptname(),
							message.getPropHosts(), message.getPropParam());
						return null;
					}
				};
				futures.add(executor.submit(task));
				LOGGER.debug("submit task for {}", sitemonitorId);
			}
			return futures;
		}
	}

}
