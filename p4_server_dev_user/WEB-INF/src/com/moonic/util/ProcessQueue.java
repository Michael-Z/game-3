package com.moonic.util;

import java.util.LinkedList;

/**
 * �������
 * @author John
 */
public class ProcessQueue {
	private LinkedList<ProcessQueueTask> taskQueue = new LinkedList<ProcessQueueTask>();
	private Processer processer;
	private boolean isRun;
	private long dalay;
	
	private static boolean PQ_RUN_STATE = true;
	
	/**
	 * �������д�����е�����״̬
	 */
	public static void setRunState(boolean state){
		PQ_RUN_STATE = state;
	}
	
	/**
	 * ����(��Ĭ�ϵ�0������ʱ��)
	 */
	public ProcessQueue() {
		this(0);
	}
	
	/**
	 * ����
	 * @param dalay ������ÿ�����������ʱ��
	 */
	public ProcessQueue(int dalay) {
		isRun = true;
		processer = new Processer();
		processer.start();
	}
	
	/**
	 * �߳�
	 * @author John
	 */
	class Processer extends Thread {
		public void run() {
			while(PQ_RUN_STATE && isRun){
				while(taskQueue.size() == 0){
					try {
						Thread.sleep(100);
						if(!(isRun && PQ_RUN_STATE)){
							return;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				ProcessQueueTask task = taskQueue.get(0);
				task.execute();
				removeTask();
				try {
					Thread.sleep(dalay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}	
	}
	
	/**
	 * ��ȡ��ǰ���г���
	 */
	public int getQueueSize(){
		return taskQueue.size();
	}
	
	/**
	 * ֹͣ
	 */
	public void stop(){
		isRun = false;
	}
	
	/**
	 * ��������
	 */
	public void addTask(ProcessQueueTask task){
		synchronized (this) {
			taskQueue.offer(task);
		}
	}
	
	/**
	 * �Ƴ�����
	 */
	public void removeTask(){
		synchronized (this) {
			taskQueue.poll();
		}
	}
}
