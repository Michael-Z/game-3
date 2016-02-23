package com.moonic.txtdata;

import server.common.Tools;

import com.moonic.util.DBPool;
import com.moonic.util.DBPoolClearListener;

/**
 * �������
 * @author wkc
 */
public class TeamActivityData {
	//�����
	public static int[] weekarr;
	//�ʱ��
	public static String[] timearr;
	//�ʱ��
	public static int actiTimeLen;
	//�ɻ�ý�������
	public static int times;
	
	static {
		init();
		DBPool.getInst().addTxtClearListener(new DBPoolClearListener() {
			public void callback(String key) {
				if(key.equals("team")){
					init();
				}
			}
		});
	}
	
	public static void init() {
		try {
			String fileText = DBPool.getInst().readTxtFromPool("team");
			weekarr = Tools.splitStrToIntArr(Tools.getStrProperty(fileText, "week"), ",");
			timearr = Tools.splitStr(Tools.getStrProperty(fileText, "time"), ",");
			actiTimeLen = Tools.getIntProperty(fileText, "timelen");
			times = Tools.getIntProperty(fileText, "times");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
