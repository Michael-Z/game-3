package com.moonic.timertask;

import java.util.concurrent.TimeUnit;

import server.config.ServerConfig;

import com.moonic.bac.ServerBAC;
import com.moonic.util.ConfFile;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;
import com.moonic.util.Out;

/**
 * ���ݿ�������������ʱ��
 * @author John
 */
public class DBIdleAdjustTT extends MyTimerTask {
	public static final String MIN_IDLE = "min_idle_v1";
	
	public void run2() {
		try {
			int minIdle = ServerConfig.getDataBase().adjustMinIdle();
			ConfFile.updateFileValue(MIN_IDLE, String.valueOf(minIdle));		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ʼ����ʱ��
	 */
	public static void init(){
		long daley = MyTools.getCurrentDateLong()+MyTools.long_day+MyTools.long_hour*4-System.currentTimeMillis();
		ServerBAC.timer.scheduleAtFixedRate(new DBIdleAdjustTT(), daley, MyTools.long_day, TimeUnit.MILLISECONDS);
		Out.println("�������ݿ�������������ʱ����� �´�ִ��ʱ�䣺"+MyTools.getTimeStr(System.currentTimeMillis()+daley));
	}
}
