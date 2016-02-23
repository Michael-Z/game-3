package com.moonic.timertask;

import java.util.concurrent.TimeUnit;

import com.moonic.bac.ServerBAC;
import com.moonic.bac.ServerFacBAC;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;

/**
 * ���°���������ʱ��
 * @author John
 */
public class FacRankingTT extends MyTimerTask {
	
	/**
	 * ִ��
	 */
	public void run2() {
		try {
			ServerFacBAC.getInstance().updateFactionRanking();		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//-------------��̬��----------------
	
	/**
	 * ��ʼ��
	 */
	public static void init(){
		ServerBAC.timer.scheduleAtFixedRate(new FacRankingTT(), 0, MyTools.long_minu*10, TimeUnit.MILLISECONDS);
	}
}
