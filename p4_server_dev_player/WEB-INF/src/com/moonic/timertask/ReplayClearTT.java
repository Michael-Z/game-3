package com.moonic.timertask;

import java.util.concurrent.TimeUnit;

import com.moonic.bac.BattleReplayBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;
import com.moonic.util.Out;

import conf.Conf;

public class ReplayClearTT extends MyTimerTask {

	public void run2() {
		BattleReplayBAC.getInstance().clearExpirationReplay();
		Out.println("�������ս���ط�");
		
	}
	
	/**
	 * ��ʼ����ʱ��
	 */
	public static void init(){
		if(Conf.useClearReplayTT){
			ServerBAC.timer.scheduleAtFixedRate(new ReplayClearTT(), 0, MyTools.long_hour, TimeUnit.MILLISECONDS);
			Out.println("�����������ս���طż�ʱ��");		
		}
	}
}
