package com.moonic.txtdata;

import server.common.Tools;

import com.moonic.util.DBPool;
import com.moonic.util.DBPoolClearListener;

/**
 * �ֻ�������
 * @author wkc
 */
public class TowerData {
	//��������
	public static int[] basicArr;
	//�Ǽ�����
	public static int[] starArr;
	//�Ѷȶ�Ӧ����������
	public static byte[] typeArr;
	//��ֹ��ս��ʼʱ��
	public static String forbiddenstarttime;
	//��ֹ��ս��ֹʱ��
	public static String forbiddenendtime;
	
	static {
		init();
		DBPool.getInst().addTxtClearListener(new DBPoolClearListener() {
			public void callback(String key) {
				if(key.equals("tower")){
					init();
				}
			}
		});
	}
	
	public static void init() {
		try {
			String fileText = DBPool.getInst().readTxtFromPool("tower");
			basicArr = Tools.splitStrToIntArr(Tools.getStrProperty(fileText, "basic"), ",");
			starArr = Tools.splitStrToIntArr(Tools.getStrProperty(fileText, "star"), ",");
			typeArr = Tools.splitStrToByteArr(Tools.getStrProperty(fileText, "type"), ",");
			forbiddenstarttime = Tools.getStrProperty(fileText, "forbiddenstarttime");
			forbiddenendtime = Tools.getStrProperty(fileText, "forbiddenendtime");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
