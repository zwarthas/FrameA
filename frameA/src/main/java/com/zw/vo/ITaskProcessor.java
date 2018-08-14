package com.zw.vo;

public interface ITaskProcessor<T, R> {
	TaskResult<R> taskExecute(T data);
}
