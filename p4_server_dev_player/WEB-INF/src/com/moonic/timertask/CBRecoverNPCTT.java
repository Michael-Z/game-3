package com.moonic.timertask;

import java.util.concurrent.TimeUnit;

import com.ehc.common.ReturnValue;
import com.moonic.bac.CBBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;
import com.moonic.util.Out;

/**
 * ��ս�ָ�NPC��ʱ��
 * @author John
 */
public class CBRecoverNPCTT extends MyTimerTask {
	
	/**
	 * ִ��
	 */
	public void run2() {
		ReturnValue rv = CBBAC.getInstance().recoverNPC();
		Out.println("��ս�ָ�NPC���ָ������"+rv.info);
	}
	
	/**
	 * ��ʼ����ʱ��
	 */
	public static void init(){
		long delay = MyTools.long_hour-((System.currentTimeMillis()-MyTools.getCurrentDateLong())%MyTools.long_hour);
		ServerBAC.timer.scheduleAtFixedRate(new CBRecoverNPCTT(), delay, MyTools.long_hour, TimeUnit.MILLISECONDS);
		Out.println("������ս�ָ�NPC��ʱ����� �´λָ�ʱ�䣺"+MyTools.getTimeStr(System.currentTimeMillis()+delay));
	}
}
