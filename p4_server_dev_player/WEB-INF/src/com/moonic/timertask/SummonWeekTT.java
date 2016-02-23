package com.moonic.timertask;

import java.util.concurrent.TimeUnit;

import org.json.JSONArray;

import com.moonic.bac.PlaSummonBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.socket.PushData;
import com.moonic.socket.SocketServer;
import com.moonic.util.ConfFile;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;
import com.moonic.util.Out;

/**
 * �����ٻ�����Ʒ��ʱ��
 * @author wkc
 */
public class SummonWeekTT extends MyTimerTask {
	public static final String SUMMON_WEEK = "summon_week_v1";
	public static final String TIME_SUMMON_WEEK = "time_summon_week_v1";
	
	/**
	 * ִ��
	 */
	public void run2() {
		try {
			PlaSummonBAC.mystery_week = PlaSummonBAC.getInstance().createMysteryItem((byte)1, 1);
			ConfFile.updateFileValue(SUMMON_WEEK, PlaSummonBAC.mystery_week.toString());
			ConfFile.updateFileValue(TIME_SUMMON_WEEK, MyTools.getTimeStr(MyTools.getNextWeekDay(System.currentTimeMillis(), 2, MyTools.long_hour*5) + MyTools.long_hour*5));
			PushData.getInstance().sendPlaToAllOL(SocketServer.ACT_SUMMON_WEEK_ITEM, PlaSummonBAC.mystery_week.toString());
			Out.println("ִ��ˢ�������ٻ�����Ʒ");
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	//-------------��̬��----------------
	
	/**
	 * ��ʼ��
	 */
	public static void init(){
		try {
			long defaulttime = MyTools.getNextWeekDay(System.currentTimeMillis(), 2, MyTools.long_hour*5) + MyTools.long_hour*5;
			String weekStr = ConfFile.getFileValueInStartServer(SUMMON_WEEK, "");
			if(!weekStr.equals("")){
				PlaSummonBAC.mystery_week = new JSONArray(weekStr);
			} else{
				ServerBAC.timer.schedule(new SummonWeekTT(), 0, TimeUnit.MILLISECONDS);
			}
			long filetime = MyTools.getTimeLong(ConfFile.getFileValueInStartServer(TIME_SUMMON_WEEK, MyTools.getTimeStr(defaulttime)));
			long delay = 0;
			if(MyTools.checkSysTimeBeyondSqlDate(filetime)){
				ServerBAC.timer.schedule(new SummonWeekTT(), 0, TimeUnit.MILLISECONDS);
				delay = defaulttime - System.currentTimeMillis();
			} else {
				delay = filetime - System.currentTimeMillis();
			}
			ServerBAC.timer.scheduleAtFixedRate(new SummonWeekTT(), delay, MyTools.long_day*7, TimeUnit.MILLISECONDS);
			Out.println("������ʼ�������ٻ�����Ʒ��ʱ�����"+MyTools.getTimeStr(filetime));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
