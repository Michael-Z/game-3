package com.moonic.txtdata;

import server.common.Tools;

import com.moonic.util.DBPool;
import com.moonic.util.DBPoolClearListener;
import com.moonic.util.MyTools;

/**
 * �̵�����
 * @author wkc
 */
public class ShopData {
	//��ͨ�̵�ˢ��ʱ��
	public static String[] time_ordinary;
	//�����̵�ˢ��ʱ��
	public static String[] time_mystery;
	//�����̵�ˢ��ʱ��
	public static String[] time_jj;
	//�����̵�ˢ��ʱ��
	public static String[] time_faction;
	//����̵�ˢ��ʱ��
	public static String[] time_sp;
	//�ֻ����̵�ˢ��ʱ��
	public static String[] time_tower;
	
	static {
		init();
		DBPool.getInst().addTxtClearListener(new DBPoolClearListener() {
			public void callback(String key) {
				if(key.equals("shop")){
					init();
				}
			}
		});
	}
	
	public static void init() {
		try {
			String fileText = DBPool.getInst().readTxtFromPool("shop");
			time_ordinary = Tools.splitStr(Tools.getStrProperty(fileText, "ordinary"), ",");
			time_mystery = Tools.splitStr(Tools.getStrProperty(fileText, "mystery"), ",");
			time_jj = Tools.splitStr(Tools.getStrProperty(fileText, "jj"), ",");
			time_faction = Tools.splitStr(Tools.getStrProperty(fileText, "faction"), ",");
			time_sp = Tools.splitStr(Tools.getStrProperty(fileText, "sp"), ",");
			time_tower = Tools.splitStr(Tools.getStrProperty(fileText, "tower"), ",");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static long getNextRefreshTime(String[] timeArr) throws Exception{
		long nextTime = 0;
		for(int i = 0; i < timeArr.length; i++){
			long targetTime = MyTools.getTimeLong(MyTools.getDateStr()+" "+timeArr[i]);
			if(System.currentTimeMillis() < targetTime){
				nextTime = targetTime;
				break;
			}
		}
		if(nextTime == 0){
			nextTime = MyTools.getTimeLong(MyTools.getDateStr()+" "+timeArr[0]) + MyTools.long_day;
		}
		return nextTime;
	}
}
