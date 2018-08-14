package com.zw.vo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class JobInfo<R> {
	// 区分唯一的工作
	private final String jobName;

	// 工作的任务个数
	private final int jobLength;

	// 这个工作的任务处理器
	private final ITaskProcessor<?, ?> taskProcessor;

	// 成功处理的任务数
	private final AtomicInteger successCount;

	// 已处理的任务数
	private final AtomicInteger currentCount;

	// 结果队列，拿结果从头拿，放结果从尾部放
	private final LinkedBlockingDeque<TaskResult<R>> taskDetailQueue;

	// 工作的完成保存的时间，超过这个时间从缓存中清除
	private final long expireTime;

	public JobInfo(String jobName, int jobLength, ITaskProcessor<?, ?> taskProcessor, long exprieTime) {
		super();
		this.jobName = jobName;
		this.jobLength = jobLength;
		this.taskProcessor = taskProcessor;
		this.successCount = new AtomicInteger(0);
		this.currentCount = new AtomicInteger(0);
		this.taskDetailQueue = new LinkedBlockingDeque<TaskResult<R>>(jobLength);
		this.expireTime = exprieTime;
	}

	public String getJobName() {
		return jobName;
	}

	// 返回成功处理的结果数
	public int getSuccessCount() {
		return successCount.get();
	}

	// 返回当前已处理的结果数
	public int getCurrentCount() {
		return currentCount.get();
	}

	// 提供工作中失败的次数
	public int getFailCount() {
		return currentCount.get() - successCount.get();
	}

	public String getTotalProcess() {
		return "Success[" + successCount.get() + "]/Current[" + currentCount.get() + "] Total[" + jobLength + "]";
	}

	public ITaskProcessor<?, ?> getTaskProcessor() {
		return taskProcessor;
	}

	// 获得工作中每个任务的处理详情
	public List<TaskResult<R>> getTaskDetail() {
		TaskResult<R> result;
		List<TaskResult<R>> resultList = new LinkedList<>();
		while ((result = taskDetailQueue.pollFirst()) != null) {
			resultList.add(result);
		}
		return resultList;
	}

	// 放任务的结果，从业务应用角度来说，保证最终一致性即可，不需要对方法加锁.
	public void addTaskResult(TaskResult<R> taskResult, CheckJobProcessor checkJob) {
		if (taskResult.getTaskResultType() == TaskResultType.SUCCESS) {
			successCount.incrementAndGet();
		}
		currentCount.incrementAndGet();
		taskDetailQueue.addLast(taskResult);

		if (jobLength == currentCount.get()) {
			checkJob.putJobInfo(jobName, expireTime);
		}

	}

}
