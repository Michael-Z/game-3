package com.moonic.util;

/**
 * ��ʱ������
 * @author John
 */
public abstract class MyTimerTask implements Runnable {
	private boolean allowRun = true;
	
	public final void run() {
		try {//��ֹ�����׳��쳣���º�������ȡ��
			if(allowRun){
				run2();
			} 
			/*else {
				System.out.println("��ʱ����ȡ��");
			}*/		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void cancel(){
		allowRun = false;
	}
	
	public abstract void run2();
}
