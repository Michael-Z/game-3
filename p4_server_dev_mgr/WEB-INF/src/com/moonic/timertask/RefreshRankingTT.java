package com.moonic.timertask;

import java.util.concurrent.TimeUnit;

import com.moonic.bac.RankingBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;
import com.moonic.util.Out;

/**
 * ˢ����Ϸ���м�ʱ��
 * @author John
 */
public class RefreshRankingTT extends MyTimerTask {
	public long exetime;
	
	/**
	 * ����
	 */
	public RefreshRankingTT(long exetime){
		this.exetime = exetime;
	}
	
	/**
	 * ִ��
	 */
	public void run2() {
		try {
			RankingBAC.getInstance().refreshRanking(exetime);
			exetime += MyTools.long_minu*30;
			Out.println("ִ��ˢ����Ϸ���� �´�ִ��ʱ�䣺"+MyTools.getTimeStr(exetime)+"("+exetime+")");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ʼ����ʱ��
	 */
	public static void init(){
		long currtime = System.currentTimeMillis();
		long delay = MyTools.long_minu*30-(currtime-MyTools.getCurrentDateLong())%(MyTools.long_minu*30);
		ServerBAC.timer.scheduleAtFixedRate(new RefreshRankingTT(currtime+delay), delay, MyTools.long_minu*30, TimeUnit.MILLISECONDS);
		Out.println("����ˢ����Ϸ���м�ʱ����� �´�ִ��ʱ�䣺"+MyTools.getTimeStr(currtime+delay)+"("+(currtime+delay)+")");
	}
}
