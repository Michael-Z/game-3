package com.moonic.mode;

import com.moonic.mgr.ActMgr;


/**
 * �û�
 * @author John
 */
public class User extends ActMgr {
	public int uid;
	public String channel;
	
	/**
	 * ��ȡKEY
	 */
	public String getKey() {
		return String.valueOf(uid);
	}
}
