package com.moonic.mgr;

import com.ehc.common.ReturnValue;
import com.moonic.util.MyLog;

import conf.Conf;

/**
 * ��������
 * @author John
 */
public abstract class ActMgr {
	public ActBag reqact;
	
	public long lasttime;//�������ʱ��
	
	public static MyLog log = new MyLog(MyLog.NAME_DATE, "log_actmgr", "ACTMGR", Conf.debug, false, true, null);
	
	/**
	 * �������ڴ��������
	 */
	public ReturnValue addReqing(short act, long time){
		synchronized (this) {
			ReturnValue val = null;
			long currtime = System.currentTimeMillis();
			if(currtime>lasttime+5){//����Ƶ�ʿ���
				log.d(getKey()+"�пͻ�������" + act + "," + time);
				if(reqact != null && !reqact.req_finish){//�������������
					String str = "���������ڴ���(act="+reqact.act+")...";
					val = new ReturnValue(false, str);
					log.d(str);
				} else {
					if(reqact != null && reqact.act == act && reqact.time == time){//�ͻ���Ϊ���յ�������ٴη���ͬ���󣬷�����ʷ���
						val = reqact.rv;
						val.parameter = "";//��Ƿ�����ʷ����
						log.d("������ʷ���");
					} else {//�µ����󣬴����µĶ���
						reqact = new ActBag();
						reqact.act = act;
						reqact.time = time;
						val = new ReturnValue(true);
						log.d("��ʼ��������");
					}
				}
				lasttime = currtime;
			} else {
				log.d(getKey()+" ����Ƶ�ʹ��죬�ϴ���������"+reqact.act+"���ϴ�����ʱ�䣺"+lasttime+"��������������"+act+"����������ʱ�䣺"+currtime, true);
				val = new ReturnValue(false, "����Ƶ�ʹ��죬�ϴ���������"+reqact.act+"���ϴ�����ʱ�䣺"+lasttime+"��������������"+act+"����������ʱ�䣺"+currtime);
			}
			return val;
		}
	}
	
	/**
	 * �Ƴ����ڱ����������
	 */
	public void removeReqing(short act, ReturnValue rv){
		synchronized (this) {
			if(reqact != null && reqact.act == act){
				reqact.rv = rv;//��¼����������Ľ��
				reqact.req_finish = true;
				log.d("��������" + reqact.act + "," + reqact.time + "���");
			} else {
				log.d("���������쳣 " + reqact.act + "/" + act);
			}
		}
	}
	
	//-----------������------------
	
	/**
	 * ��ȡKEY
	 */
	public abstract String getKey();
	
	//-----------�ڲ���------------
	
	/**
	 * �����
	 */
	class ActBag {
		public short act;
		public long time;
		public boolean req_finish;
		public ReturnValue rv;
		
		/**
		 * ��д
		 */
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			sb.append(act);
			sb.append(",");
			sb.append(time);
			sb.append("]");
			return sb.toString();
		}
	}
}
