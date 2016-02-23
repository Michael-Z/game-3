package com.moonic.timertask;

import java.util.concurrent.TimeUnit;

import org.json.JSONArray;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.moonic.bac.CBBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.txtdata.CBDATA;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;
import com.moonic.util.Out;

/**
 * ��սNPC����
 * @author John
 */
public class CBNpcInvadeTT extends MyTimerTask {
	private String invadetimeStr;
	
	/**
	 * ����
	 */
	public CBNpcInvadeTT(String invadetimeStr){
		this.invadetimeStr = invadetimeStr;
	}
	
	/**
	 * ִ��
	 */
	public void run2() {
		DBHelper dbHelper = new DBHelper();
		try {
			for(int i = 0; i < CBDATA.invadedeclare.length; i++){
				if(invadetimeStr.equals(CBDATA.invadedeclare[i][0])){//������Ŀ
					int waramount = Tools.str2int(CBDATA.invadedeclare[i][2]);
					for(int k = 0; k < waramount; k++){//��Ҫ���ֳǳ���
						int[] npcamount = Tools.splitStrToIntArr(CBDATA.invadescale[MyTools.getRandom(0, CBDATA.invadescale.length-1)][1], ",");//ȷ�����ֲ��ӹ�ģ
						JSONArray npcinfluencearr = new JSONArray();//������������
						for(int m = 0; m < CBDATA.invadeinfluence.length; m++){
							npcinfluencearr.add(CBDATA.invadeinfluence[m][0]);
						}
						int citynum = 0;//���ֳ���
						int npcinfluence = 0;//��������
						while(npcinfluencearr.length() > 0){//�п�����������
							npcinfluence = npcinfluencearr.optInt(MyTools.getRandom(0, npcinfluencearr.length()-1));//ȷ����������
							//System.out.println("-------while------1----"+npcinfluencearr+"----"+npcinfluence+"----");
							DBPsRs citylistRs = DBPool.getInst().pQueryS(CBBAC.tab_cb_city, "citytype=3 and display=1");
							JSONArray cancitynumarr = new JSONArray();//���Ͽ����ڽ����ĳ���
							while(citylistRs.next()){
								cancitynumarr.add(citylistRs.getInt("num"));
							}
							while(cancitynumarr.length() > 0){//�пɽ�������
								//System.out.println("-------while------2------------");
								citynum = cancitynumarr.optInt(MyTools.getRandom(0, cancitynumarr.length()-1));//ȷ�ϳ��б��
								if(CBBAC.cbmgr.checkCityInWar(citynum)){//�˳�������ս����
									cancitynumarr.remove((Integer)citynum);
									continue;
								}
								DBPaRs cityRs = DBPool.getInst().pQueryA(CBBAC.tab_cb_city, "num="+citynum);
								DBPsRs cityStorRs = CBBAC.getInstance().getCityStorRs(dbHelper, cityRs);
								if(!MyTools.checkSysTimeBeyondSqlDate(cityStorRs.getTime("nowarendtime"))){//��ս���޷�����
									cancitynumarr.remove((Integer)cityStorRs.getInt("citynum"));
									continue;
								}
								if(cityStorRs.getInt("influencenum") == npcinfluence){//�����������Լ�������ͬ�ĳ���
									cancitynumarr.remove((Integer)cityStorRs.getInt("citynum"));
									continue;
								}
								break;//�ҵ����óǳأ���������
							}
							if(cancitynumarr.length() > 0){//ȷ���˳ǳأ���������
								break;
							} else {//δ�ҵ����óǳأ��Ƴ�����������������
								npcinfluencearr.remove(String.valueOf(npcinfluence));
							}
						}
						if(npcinfluencearr.length() > 0){//ȷ������������������
							ReturnValue rv = CBBAC.getInstance().npcInvade(citynum, npcinfluence, npcamount);
							Out.println("��սNPC���� ���������"+rv.info);
						} else {//�޿�����������ʾʧ��
							Out.println("û�пɽ����ĳ���");
						}
					}
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ʼ����ʱ��
	 */
	public static void init(){
		for(int i = 0; i < CBDATA.invadedeclare.length; i++){
			long time = MyTools.getTimeLong(MyTools.getDateStr()+" "+CBDATA.invadedeclare[i][0]);
			long delay = time - System.currentTimeMillis();
			if(delay <= 0){
				delay += MyTools.long_day;
			}
			//System.out.println("CBDATA.invadedeclare[i][0]:"+MyTools.getTimeStr(System.currentTimeMillis()+delay));
			ServerBAC.timer.scheduleAtFixedRate(new CBNpcInvadeTT(CBDATA.invadedeclare[i][0]), delay, MyTools.long_day, TimeUnit.MILLISECONDS);
		}
		Out.println("��սNPC���ּ�ʱ���������");
	}
}
