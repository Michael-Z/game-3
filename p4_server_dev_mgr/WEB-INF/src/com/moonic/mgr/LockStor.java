package com.moonic.mgr;

import java.util.HashMap;

import com.moonic.util.Out;

/**
 * ���ֿ�
 * @author John
 */
public class LockStor {
	private static final HashMap<String, byte[]> stor = new HashMap<String, byte[]>(131072);
	
	/**
	 * ��������
	 */
	public static final byte[] LOCK = new byte[0];
	
	//-----------------ϵͳ��-----------------
	
	/**
	 * ���ݿ��б���
	 */
	public static final short DB_POOL_TAB = 101;
	/**
	 * ���ݿ��ı�����
	 */
	public static final short DB_POOL_TXT = 102;
	/**
	 * �����
	 */
	public static final short RANDOM_NEXT = 103;
	/**
	 * ���������ʱ��
	 */
	public static final short RANDOM_TIME = 104;
	/**
	 * ����־����־
	 */
	public static final short LOG_SAVE = 105;
	/**
	 * �ָ���־�洢��ʱ��
	 */
	public static final short LOG_EXC_RECOVER = 106;
	
	//-----------------�Զ�����-----------------
	
	/**
	 * ��ȡ��
	 */
	public static byte[] getLock(short lockname, Object... keys){
		StringBuffer sb = new StringBuffer();
		sb.append(lockname);
		for(int i = 0; i < keys.length; i++){
			sb.append("0");
			sb.append(keys[i]);
		}
		String str = sb.toString();
		synchronized (LOCK) {
			long t1 = System.currentTimeMillis();
			byte[] lock = stor.get(str);
			if(lock == null){
				if(stor.size() >= 98000){
					stor.clear();
				}
				lock = new byte[0];
				stor.put(str, lock);
			}
			long t2 = System.currentTimeMillis();
			if(t2-t1>5){
				Out.println("getLock ��ȡ"+sb.toString()+"��ʱ��"+(t2-t1)+" len:"+stor.size());
			}
			return lock;
		}
	}
}
