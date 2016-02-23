package com.moonic.txtdata;

import server.common.Tools;

import com.moonic.util.DBPool;
import com.moonic.util.DBPoolClearListener;
import com.moonic.util.MyTools;

/**
 * �ڿ�
 * @author John
 */
public class MineralsData {
	//�������λ����
	public static int[] posamount;
	//��ʼʱ��
	public static String[] opentime;
	//ÿ�������ʱ�䣨���ӣ�
	public static int continuoustime;
	//�������㵥λʱ�䣨���ӣ�
	public static long rewardtime;
	//����������
	public static int robberynum;
	//������������۸񣨽𶧣�
	public static int buyrobbery;
	//����ʧ��CD�����ӣ�
	public static long losetime;
	//�������ջ���
	public static int[] markon;
	//��������
	public static String[][] awardpara;
	
	static {
		init();
		DBPool.getInst().addTxtClearListener(new DBPoolClearListener() {
			public void callback(String key) {
				if(key.equals("minerals")){
					init();
				}
			}
		});
	}
	
	public static void init() {
		try {
			String fileText = DBPool.getInst().readTxtFromPool("minerals");
			posamount = Tools.splitStrToIntArr(Tools.getStrProperty(fileText, "posamount"), ",");
			opentime = Tools.splitStr(Tools.getStrProperty(fileText, "opentime"), ",");
			continuoustime = Tools.getIntProperty(fileText, "continuoustime");
			rewardtime = Tools.getIntProperty(fileText, "rewardtime") * MyTools.long_minu;
			robberynum = Tools.getIntProperty(fileText, "robberynum");
			buyrobbery = Tools.getIntProperty(fileText, "buyrobbery");
			losetime = Tools.getIntProperty(fileText, "losetime") * MyTools.long_minu;
			markon = Tools.splitStrToIntArr(Tools.getStrProperty(fileText, "markon"), ",");
			awardpara = Tools.getStrLineArrEx2(fileText, "awardpara:", "awardparaEnd");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
