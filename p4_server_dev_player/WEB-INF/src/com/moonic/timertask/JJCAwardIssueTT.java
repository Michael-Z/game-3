package com.moonic.timertask;

import java.util.concurrent.TimeUnit;

import com.moonic.bac.PlaJJCRankingBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.txtdata.JJCChallengeData;
import com.moonic.util.ConfFile;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;
import com.moonic.util.Out;

/**
 * �������������ż�ʱ��
 * @author John
 */
public class JJCAwardIssueTT extends MyTimerTask {
	public static final String TIME_JJC_AWARD_ISSUE = "jjcawardissue";
	
	/**
	 * ִ��
	 */
	public void run2() {
		try {
			Out.println("��ʼ���ž���������");
			long nexttime = MyTools.getCurrentDateLong()+JJCChallengeData.forbiddenstarttime;
			System.out.println("------���������Ž�������------nexttime:"+nexttime+" curr"+System.currentTimeMillis());
			PlaJJCRankingBAC.getInstance().issueAward("��ʱ��");
			if(MyTools.checkSysTimeBeyondSqlDate(nexttime)){
				nexttime += MyTools.long_day;
			}
			String nexttimeStr = MyTools.getTimeStr(nexttime);
			ConfFile.updateFileValue(TIME_JJC_AWARD_ISSUE, nexttimeStr);
			Out.println("���ž�����������ɣ��´η��Ž���ʱ�䣺"+nexttimeStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ʼ��������������ʱ��
	 */
	public static void init(){
		try {
			long defaulttime = MyTools.getCurrentDateLong()+JJCChallengeData.forbiddenstarttime;
			if(defaulttime < System.currentTimeMillis()){
				defaulttime += MyTools.long_day;
			}
			long filetime = MyTools.getTimeLong(ConfFile.getFileValueInStartServer(TIME_JJC_AWARD_ISSUE, MyTools.getTimeStr(defaulttime)));
			long delay = 0;
			if(MyTools.checkSysTimeBeyondSqlDate(filetime)){
				ServerBAC.timer.schedule(new JJCAwardIssueTT(), 0, TimeUnit.MILLISECONDS);
				delay = defaulttime-System.currentTimeMillis();
			} else {
				delay = filetime-System.currentTimeMillis();
			}
			ServerBAC.timer.scheduleAtFixedRate(new JJCAwardIssueTT(), delay, MyTools.long_day, TimeUnit.MILLISECONDS);
			Out.println("�����������������ż�ʱ����� �´�ִ��ʱ�䣺"+MyTools.getTimeStr(System.currentTimeMillis()+delay));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
