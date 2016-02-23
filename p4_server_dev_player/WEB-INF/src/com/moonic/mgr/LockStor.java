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
	/**
	 * ��ɫ������
	 */
	public static final short PLA_MIRROR = 108;
	/**
	 * ������
	 */
	public static final short PUSH_LOCK = 109;
	/**
	 * ������
	 */
	public static final short FLOW_LOCK = 110;
	
	//-----------------�Զ�����-----------------
	
	/**
	 * ������ɫ
	 */
	public static final short PLAYER_NAME = 201;
	/**
	 * ���ð��ɵ�������
	 */
	public static final short FACTION_RESET_DAYDATA = 202;
	/**
	 * ������
	 */
	public static final short FACTION_NAME = 203;
	/**
	 * ���ɼӾ���
	 */
	public static final short FACTION_ADDMONEY = 204;
	/**
	 * ��������
	 */
	public static final short FACTION_RAKNING = 205;
	/**
	 * ������
	 */
	public static final short FACTION_MEMBER = 206;
	/**
	 * �ƹ�Ա
	 */
	public static final short EXTENSION_AGENT = 207;
	/**
	 * �������������
	 */
	public static final short JJC_MAX_RANKING = 208;
	/**
	 * ��������ս
	 */
	public static final short JJC_BATTLE = 209;
	/**
	 * ������ҵ�������
	 */
	public static final short PLAYER_RESET_DAYDATE = 210;
	/**
	 * ȫ��ϵͳ�ʼ�
	 */
	public static final short SMAIL_INSERT = 211;
	/**
	 * ��ս��ս
	 */
	public static final short CB_DECLAREWAR = 212;
	/**
	 * ��ս��ϯ
	 */
	public static final short CB_LEADER = 213;
	/**
	 * ���ɸ���
	 */
	public static final short FAC_COPYMAP = 214;
	/**
	 * �ڿ�
	 */
	public static final short MINERALS = 215;
	/**
	 * ������������
	 */
	public static final short FRIEND_PRESENT = 216;
	/**
	 * ��ӻ
	 */
	public static final short TEAM_ACTI = 217;
	
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
