package com.moonic.txtdata;

import server.common.Tools;

import com.moonic.util.DBPool;
import com.moonic.util.DBPoolClearListener;
import com.moonic.util.MyTools;

/**
 * ��ս
 * @author John
 */
public class CBDATA {
	//������ս��ʼʱ��
	public static long declarewarstarttime;
	//������ս��ֹʱ��
	public static long declarewarendtime;
	//�����ս��ʼʱ��
	public static long joinwarstarttime;
	//�����ս��ֹʱ��
	public static long joinwarendtime;
	//�����ʽ������ʼʱ��
	public static long cityoutputstarttime;
	//�����ʽ������ֹʱ��
	public static long cityoutputendtime;
	//��սʱ�������ӣ�
	public static int declarewarwaittimelen;
	//ս�����ʱ�����룩
	public static int battlespacetimelen;
	//��սʱ�������ӣ�
	public static int nowartimelen;
	//�Ƿ����ظ��ٶȣ�ÿСʱ��
	public static byte npcrecoverspeed;
	//̫��������ʼʱ��
	public static long leaderstarttime;
	//̫��������ֹʱ��
	public static long leaderendtime;
	//̫������ʱ���������ӣ�
	public static int leaderspacetimelen;
	//̫�ؽ�������ʱ��
	public static long leaderawardissuetime;
	//̫�ط���ʱ���������ӣ�
	public static int leadergiveupspacetimelen;
	//��������������
	public static byte assist;
	//����ȼ�ˢ��ʱ��
	public static long worldclassrefresh;
	//���ֹ�ģ
	public static String[][] invadescale;
	//��������
	public static String[][] invadeinfluence;
	//������ս
	public static String[][] invadedeclare;
	//����۸�
	public static int reliveprice;
	
	static {
		init();
		DBPool.getInst().addTxtClearListener(new DBPoolClearListener() {
			public void callback(String key) {
				if(key.equals("cb")){
					init();
				}
			}
		});
	}
	
	public static void init() {
		try {
			String fileText = DBPool.getInst().readTxtFromPool("cb");
			declarewarstarttime = MyTools.getPointTimeLong(Tools.getStrProperty(fileText, "declarewarstarttime"));
			declarewarendtime = MyTools.getPointTimeLong(Tools.getStrProperty(fileText, "declarewarendtime"));
			joinwarstarttime = MyTools.getPointTimeLong(Tools.getStrProperty(fileText, "joinwarstarttime"));
			joinwarendtime = MyTools.getPointTimeLong(Tools.getStrProperty(fileText, "joinwarendtime"));
			cityoutputstarttime = MyTools.getPointTimeLong(Tools.getStrProperty(fileText, "cityoutputstarttime"));
			cityoutputendtime = MyTools.getPointTimeLong(Tools.getStrProperty(fileText, "cityoutputendtime"));
			declarewarwaittimelen = Tools.getIntProperty(fileText, "declarewarwaittimelen");
			battlespacetimelen = Tools.getIntProperty(fileText, "battlespacetimelen");
			nowartimelen = Tools.getIntProperty(fileText, "nowartimelen");
			npcrecoverspeed = Tools.getByteProperty(fileText, "npcrecoverspeed");
			leaderstarttime = MyTools.getPointTimeLong(Tools.getStrProperty(fileText, "leaderstarttime"));
			leaderendtime = MyTools.getPointTimeLong(Tools.getStrProperty(fileText, "leaderendtime"));
			leaderspacetimelen = Tools.getIntProperty(fileText, "leaderspacetimelen");
			leaderawardissuetime = MyTools.getPointTimeLong(Tools.getStrProperty(fileText, "leaderawardissuetime"));
			leadergiveupspacetimelen = Tools.getIntProperty(fileText, "leadergiveupspacetimelen");
			assist = Tools.getByteProperty(fileText, "assist");
			worldclassrefresh = MyTools.getPointTimeLong(Tools.getStrProperty(fileText, "worldclassrefresh"));
			invadescale = Tools.getStrLineArrEx2(fileText, "invadescale:", "invadescaleEnd");
			invadeinfluence = Tools.getStrLineArrEx2(fileText, "invadeinfluence:", "invadeinfluenceEnd");
			invadedeclare = Tools.getStrLineArrEx2(fileText, "invadedeclare:", "invadedeclareEnd");
			reliveprice = Tools.getIntProperty(fileText, "reliveprice");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getInfluenceName(int num){
		for(int m = 0; m < invadeinfluence.length; m++){
			if(num == Tools.str2int(invadeinfluence[m][0])){
				return invadeinfluence[m][1];
			}
		}
		return null;
	}
}
