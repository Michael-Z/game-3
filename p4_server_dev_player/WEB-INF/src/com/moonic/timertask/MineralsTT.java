package com.moonic.timertask;

import java.util.concurrent.TimeUnit;

import com.moonic.bac.PlaMineralsBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.txtdata.MineralsData;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;
import com.moonic.util.Out;

/**
 * �ڿ��ʱ��
 * @author John
 */
public class MineralsTT {
	
	static class StartTT extends MyTimerTask {

		public void run2() {
			PlaMineralsBAC.getInstance().start("��ʱ��");
			ServerBAC.timer.schedule(new EndTT(), MineralsData.continuoustime, TimeUnit.MINUTES);
		}
	}
	
	static class EndTT extends MyTimerTask {
		
		public void run2() {
			PlaMineralsBAC.getInstance().end("��ʱ��");
			createStartTT();
		}
	}
	
	public static void createStartTT(){
		try {
			long curr_pointtime = System.currentTimeMillis()-MyTools.getCurrentDateLong();
			long delay = 0;
			for(int i = 0; i < MineralsData.opentime.length; i++){//Ĭ����Ϊ����ʱ���С����
				long pointtime = MyTools.getPointTimeLong(MineralsData.opentime[i]);
				if(pointtime > curr_pointtime){
					delay = pointtime - curr_pointtime;
					break;
				}
			}
			if(delay == 0){
				delay = MyTools.long_day + MyTools.getPointTimeLong(MineralsData.opentime[0]) - curr_pointtime;
			}
			Out.println("�´������ڿ�ʱ�䣺"+MyTools.getTimeStr(System.currentTimeMillis()+delay));
			ServerBAC.timer.schedule(new StartTT(), delay, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void init(){
		createStartTT();
	}
}
