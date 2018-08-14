package com.zw.vo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PendingJobPool {

	// 单例模式
	private PendingJobPool() {
	}

	private static class InstanceHolder {
		static PendingJobPool instance = new PendingJobPool();
	}

	public static PendingJobPool getInstance() {
		return InstanceHolder.instance;
	}
	// 单例模式

	private static CheckJobProcessor checkJob=CheckJobProcessor.getInstance();
	
	
	// 保守估计
	private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

	// 任务队列
	private static BlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<>(5000);

	// 线程池
	private static ExecutorService pool = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, 60, TimeUnit.SECONDS,
			taskQueue);

	// job的存放容器
	private static ConcurrentHashMap<String, JobInfo<?>> jobInfoMap = new ConcurrentHashMap<>();

	// 获取job的存放容器
	public static Map<String, JobInfo<?>> getJobInfoMap() {
		return jobInfoMap;
	}

	// 1 注册工作
	public <R> void registerJob(String jobName, int jobLength, ITaskProcessor<?, ?> taskProcessor, long exprieTime) {
		JobInfo<R> jobInfo = new JobInfo<>(jobName, jobLength, taskProcessor, exprieTime);
		if (jobInfoMap.putIfAbsent(jobName, jobInfo) != null)
			throw new RuntimeException(jobName + "已经注册了！");
	}

	// 2 对工作中的任务进行包装，提交给线程池使用，并处理任务的结果，写入缓存以供查询
	private static class PendingTask<T, R> implements Runnable {

		private JobInfo<R> jobInfo;
		private T processData;

		public PendingTask(JobInfo<R> jobInfo, T processData) {
			super();
			this.jobInfo = jobInfo;
			this.processData = processData;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			R r = null;
			TaskResult<R> taskResult = null;
			ITaskProcessor<T, R> taskProcessor = (ITaskProcessor<T, R>) jobInfo.getTaskProcessor();

			try {
				taskResult = taskProcessor.taskExecute(processData);
				if (taskResult == null) {
					taskResult = new TaskResult<R>(TaskResultType.EXCEPTION, r, "result is null");
				} else if (taskResult.getTaskResultType() == null) {
					if (taskResult.getReason() == null) {
						taskResult = new TaskResult<R>(TaskResultType.EXCEPTION, r,
								"resultType is null,reason is null");
					} else {
						taskResult = new TaskResult<R>(TaskResultType.EXCEPTION, r,
								"resultType is null but reason is " + taskResult.getReason());
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				taskResult = new TaskResult<R>(TaskResultType.EXCEPTION, r, e.getMessage());
			} finally {
				jobInfo.addTaskResult(taskResult,checkJob);
			}

		}
	}

	// 3 提交工作
	public <T, R> void putTask(String jobName, T processData) {
		JobInfo<R> jobInfo = getJob(jobName);
		PendingTask<T, R> task = new PendingTask<T, R>(jobInfo, processData);
		pool.execute(task);
	}

	// 根据工作名称检索工作
	@SuppressWarnings("unchecked")
	public <R> JobInfo<R> getJob(String name) {
		JobInfo<R> jobInfo = (JobInfo<R>) jobInfoMap.get(name);
		if (null == jobInfo)
			throw new RuntimeException(name + "是个非法任务。");
		return jobInfo;
	}

	//获得每个任务的处理详情
	@SuppressWarnings("unchecked")
	public <R> List<TaskResult<R>> getTaskDetail(String jobName){
		JobInfo<R> jobInfo = (JobInfo<R>) jobInfoMap.get(jobName);
		return jobInfo.getTaskDetail();
	}
	
	//获得工作的整体处理进度
	public <R> String getTaskProgess(String jobName) {
		JobInfo<R> jobInfo = getJob(jobName);
		return jobInfo.getTotalProcess();	
	}
	
	
	
	
	
}
