package com.moonic.mgr;

/**
 * ���ɼ���
 * @author John
 */
public interface EventListener {
	
	/**
	 * �ص�
	 */
	public void callback(byte type, Object... param);
}
