package com.moonic.timertask;

import java.util.concurrent.TimeUnit;

import com.ehc.common.ReturnValue;
import com.moonic.bac.CBBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;
import com.moonic.util.Out;

/**
 * ��ս�����ʽ��ʱ��
 * @author John
 */
public class CBOutPutFacMoneyTT extends MyTimerTask {
	
	/**
	 * ִ��
	 */
	public void run2() {
		ReturnValue rv = CBBAC.getInstance().outputFacMoney();
		Out.println("��ս�����ʽ𣬽����"+rv.info);
	}
	
	/**
	 * ��ʼ����ʱ��
	 */
	public static void init(){
		long delay = MyTools.long_hour-((System.currentTimeMillis()-MyTools.getCurrentDateLong())%MyTools.long_hour);
		ServerBAC.timer.scheduleAtFixedRate(new CBOutPutFacMoneyTT(), delay, MyTools.long_hour, TimeUnit.MILLISECONDS);
		Out.println("������ս�����ʽ��ʱ����� �´β���ʱ�䣺"+MyTools.getTimeStr(System.currentTimeMillis()+delay));
	}
}
