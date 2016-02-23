package com.moonic.mgr;

import java.util.ArrayList;


/**
 * �¼�����
 * @author John
 */
public class EventCenter {
	private static ArrayList<EventListener> listeners = new ArrayList<EventListener>();
	
	/**
	 * ��Ӽ���
	 */
	public static void addListener(EventListener listener){
		synchronized (listeners) {
			listeners.add(listener);		
		}
	}
	
	/**
	 * �Ƴ�����
	 */
	public static void removeListener(EventListener listener){
		synchronized (listeners) {
			listeners.remove(listener);		
		}
	}
	
	/**
	 * �����ص�
	 */
	public static void send(byte type, Object... param){
		for(int i = 0; i < listeners.size(); i++){
			EventListener lis = listeners.get(i);
			if(lis != null){
				lis.callback(type, param);
			}
		}
	}
}
