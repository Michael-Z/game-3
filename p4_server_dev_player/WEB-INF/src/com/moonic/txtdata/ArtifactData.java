package com.moonic.txtdata;

import server.common.Tools;

import com.moonic.util.DBPool;
import com.moonic.util.DBPoolClearListener;
import com.moonic.util.MyTools;

/**
 * ����
 * @author John
 */
public class ArtifactData {
	//�ָ����������λʱ�䣨���ӣ�
	public static long robrecovertimelen;
	//����ۻ��������
	public static int maxrobtimes;
	//�������������
	public static int[] openprotectcoin;
	//������λʱ�������ӣ�
	public static long protecttimelen;
	
	static {
		init();
		DBPool.getInst().addTxtClearListener(new DBPoolClearListener() {
			public void callback(String key) {
				if(key.equals("artifact")){
					init();
				}
			}
		});
	}
	
	public static void init() {
		try {
			String fileText = DBPool.getInst().readTxtFromPool("minerals");
			robrecovertimelen = Tools.getIntProperty(fileText, "robrecovertimelen")*MyTools.long_minu;
			maxrobtimes = Tools.getIntProperty(fileText, "maxrobtimes");
			openprotectcoin = Tools.splitStrToIntArr(Tools.getStrProperty(fileText, "openprotectcoin"), ",");
			protecttimelen = Tools.getIntProperty(fileText, "protecttimelen")*MyTools.long_minu;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
