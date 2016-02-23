package com.moonic.timertask;

import java.util.concurrent.TimeUnit;

import com.moonic.bac.CustomActivityBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.bac.SysMailBAC;
import com.moonic.util.ConfFile;
import com.moonic.util.DBHelper;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;
import com.moonic.util.Out;

/**
 * �������ݼ�ʱ��
 * @author John
 */
public class ClearDataTT extends MyTimerTask {
	private static final String CLEAR_DATA_TIME = "cleardatatime_v1";
	
	/**
	 * ִ��
	 */
	public void run2() {
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			String nexttimeStr = MyTools.getTimeStr(MyTools.getCurrentDateLong()+MyTools.long_day+MyTools.long_hour*3);
			SysMailBAC.getInstance().clearData(dbHelper);
			CustomActivityBAC.getInstance().clearData(dbHelper);
			ConfFile.updateFileValue(CLEAR_DATA_TIME, nexttimeStr);
			Out.println("ִ���������� �´�����ʱ�䣺"+nexttimeStr);
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
		long defaulttime = MyTools.getCurrentDateLong()+MyTools.long_hour*3;
		if(System.currentTimeMillis() > defaulttime){
			defaulttime += MyTools.long_day;
		}
		long filetime = MyTools.getTimeLong(ConfFile.getFileValueInStartServer(CLEAR_DATA_TIME, MyTools.getTimeStr(defaulttime)));
		long delay = 0;
		if(MyTools.checkSysTimeBeyondSqlDate(filetime)){
			ServerBAC.timer.schedule(new ClearDataTT(), 0, TimeUnit.MILLISECONDS);
			delay = defaulttime-System.currentTimeMillis();
		} else {
			delay = filetime-System.currentTimeMillis();
		}
		ServerBAC.timer.scheduleAtFixedRate(new ClearDataTT(), delay, MyTools.long_day, TimeUnit.MILLISECONDS);
		Out.println("�����������ݼ�ʱ�����");
	}
}
