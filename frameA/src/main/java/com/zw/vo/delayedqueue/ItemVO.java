package com.zw.vo.delayedqueue;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class ItemVO<T> implements Delayed {

	private long expireTime;// 单位毫秒

	private T data;

	public ItemVO(long expireTime, T data) {
		super();
		this.expireTime = expireTime;
		this.data = data;
	}

	
	public long getExpireTime() {
		return expireTime;
	}


	public T getData() {
		return data;
	}

	@Override
	public int compareTo(Delayed o) {
		long d=this.getDelay(TimeUnit.MILLISECONDS)-o.getDelay(TimeUnit.MILLISECONDS);
		return (d==0)?0:((d>0?1:-1));
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(this.expireTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

	}

}
