package com.moonic.util;

import conf.Conf;

/**
 * ������֮������ͨѶ
 * @author John
 */
public class STSNetSender extends NetSender {
	
	/**
	 * ����
	 */
	public STSNetSender(short act) throws Exception {
		super(act);
		if(Conf.stsKey == null){
			Conf.stsKey = "ʶ���ܳ�δ����";
		}
		dos.writeUTF(Conf.stsKey);
	}
}
