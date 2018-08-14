package com.zw.vo;

public class TaskResult<R> {

	private final TaskResultType taskResultType;
	private final R resultValue;
	private final String reason;
	public TaskResult(TaskResultType taskResultType, R resultValue, String reason) {
		super();
		this.taskResultType = taskResultType;
		this.resultValue = resultValue;
		this.reason = reason;
	}
	public TaskResult(TaskResultType taskResultType, R resultValue) {
		super();
		this.taskResultType = taskResultType;
		this.resultValue = resultValue;
		this.reason = "Success";
	}
	public TaskResultType getTaskResultType() {
		return taskResultType;
	}
	public R getResultValue() {
		return resultValue;
	}
	public String getReason() {
		return reason;
	}
	@Override
	public String toString() {
		return "TaskResult [taskResultType=" + taskResultType + ", resultValue=" + resultValue + ", reason=" + reason
				+ "]";
	}
	
	
}
