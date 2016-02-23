package com.moonic.txtdata;

import server.common.Tools;

import com.moonic.util.DBPool;
import com.moonic.util.DBPoolClearListener;

/**
 * ����BOSS����
 * @author wkc
 */
public class WorldBossData {
	//�����
	public static int[] weekarr;
	//�ʱ��
	public static String[] timearr;
	//�ʱ��
	public static int actiTimeLen;
	//��ս����
	public static int chaTimes;
	//ս���������
	public static int bgNum;
	
	static {
		init();
		DBPool.getInst().addTxtClearListener(new DBPoolClearListener() {
			public void callback(String key) {
				if(key.equals("worldboss")){
					init();
				}
			}
		});
	}
	
	public static void init() {
		try {
			String fileText = DBPool.getInst().readTxtFromPool("worldboss");
			weekarr = Tools.splitStrToIntArr(Tools.getStrProperty(fileText, "week"), ",");
			timearr = Tools.splitStr(Tools.getStrProperty(fileText, "time"), ",");
			actiTimeLen = Tools.getIntProperty(fileText, "timelen");
			chaTimes = Tools.getIntProperty(fileText, "chatimes");
			bgNum = Tools.getIntProperty(fileText, "bgnum");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
