package com.moonic.util;

/**
 * ScheduledExecutorServiceִ�е����������
 * @author huangyan
 *
 */
public abstract class MyRunnable implements Runnable
{
	private boolean allowExec = true; //ȡ��������Ʊ���
	public void cancel()
	{
		allowExec = false;
	}
	public boolean allowRun()
	{
		return allowExec;
	}
}
