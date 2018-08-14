package com.zw.vo;

import java.util.concurrent.DelayQueue;

import com.zw.vo.delayedqueue.ItemVO;

public class CheckJobProcessor {
	static {
		Thread processThread = new Thread(new CheckJob());
		processThread.setDaemon(true);
		processThread.start();
		System.out.println("开启任务过期检查守护线程................");
	}

	// 单例模式------
	private CheckJobProcessor() {
	}

	private static class ProcesserHolder {
		public static CheckJobProcessor processer = new CheckJobProcessor();
	}

	public static CheckJobProcessor getInstance() {
		return ProcesserHolder.processer;
	}
	// 单例模式------

	// 存放已完成任务等待过期的队列
	private static DelayQueue<ItemVO<String>> delayQueue = new DelayQueue<ItemVO<String>>();

	// 存放已完成任务信息
	public <R> void putJobInfo(String jobName, long expireTime) {
		ItemVO<String> item = new ItemVO<String>(expireTime, jobName);
		delayQueue.offer(item);
		System.out.println("Job[" + jobName + "已经放入了过期检查缓存，过期时长：" + expireTime);
	}

	// 处理队列中到期任务的实行
	private static class CheckJob implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					ItemVO<String> item = delayQueue.take();
					String jobName = (String) item.getData();
					PendingJobPool.getJobInfoMap().remove(jobName);
					System.out.println(jobName + " is out of date,remove from map!");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}
