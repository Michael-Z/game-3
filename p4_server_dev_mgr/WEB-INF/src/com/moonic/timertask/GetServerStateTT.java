package com.moonic.timertask;

import java.util.concurrent.TimeUnit;

import org.json.JSONArray;

import com.ehc.common.SqlString;
import com.moonic.bac.ServerBAC;
import com.moonic.mail.MailSender;
import com.moonic.servlet.STSServlet;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;
import com.moonic.util.NetResult;
import com.moonic.util.STSNetSender;

import conf.Conf;
import conf.LogTbName;

/**
 * ������״̬��ؼ�ʱ��
 * @author John
 */
public class GetServerStateTT extends MyTimerTask {
	
	/**
	 * ִ��
	 */
	public void run2() {
		StringBuffer sb = new StringBuffer();
		try {
			STSNetSender sender1 = new STSNetSender(STSServlet.M_GET_SERVER_RUN_STATE);
			NetResult[] nrs1 = ServerBAC.getInstance().sendReq(ServerBAC.STS_USER_SERVER, null, sender1);
			processing(nrs1, sb);
			STSNetSender sender2 = new STSNetSender(STSServlet.G_GET_SERVER_RUN_STATE);
			NetResult[] nrs2 = ServerBAC.getInstance().sendReq(ServerBAC.STS_GAME_SERVER, null, sender2);
			processing(nrs2, sb);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if(Conf.sendServerExcEmail && sb.length() > 0){
				String head = "<body style='font-size:12px'>";
				String end = "</body>";
				DBPsRs addrRs = DBPool.getInst().pQueryS(ServerBAC.tab_server_exc_mail_addr);
				while(addrRs.next()){
					MailSender.sendMail(addrRs.getString("mailaddr"), "�������쳣����", head+sb.toString()+end);	
				}
			}		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ������Ϣ
	 */
	public void processing(NetResult[] nrs, StringBuffer sb) throws Exception{
		for(int i = 0; nrs != null && i < nrs.length; i++){
			if(nrs[i].rv.success){
				JSONArray statearr = new JSONArray(nrs[i].rv.info);
				int numactive = statearr.optInt(0);
				int numidle = statearr.optInt(1);
				int nummax = statearr.optInt(2);
				int freemem = statearr.optInt(3);
				int totalmem = statearr.optInt(4);
				int maxmem = statearr.optInt(5);
				int totalthread = statearr.optInt(6);
				String exc = null;
				if(numactive >= nummax*4/5){
					exc = "<font color='#FF0000'>"+nrs[i].name+"</font><br>�쳣��Ϣ��ռ�����ݿ��������ѽӽ���ֵ";
				} else 
				if((totalmem-freemem)>=maxmem*4/5){
					exc = "<font color='#FF0000'>"+nrs[i].name+"</font><br>�쳣��Ϣ��ռ���ڴ��ѽӽ���ֵ";
				}
				if(exc != null){
					StringBuffer thesb = new StringBuffer();
					thesb.append("<br>����ӣ�"+numactive);
					thesb.append("<br>�������ӣ�"+numidle);
					thesb.append("<br>������ӣ�"+nummax);
					thesb.append("<br>�����ڴ棺"+freemem+"M");
					thesb.append("<br>�����ڴ棺"+totalmem+"M");
					thesb.append("<br>����ڴ棺"+maxmem+"M");
					thesb.append("<br>���߳�����"+totalthread);
					sb.append(exc+"<br>��������Ϣ��"+thesb.toString()+"<br><br>");
				}
				SqlString sqlStr = new SqlString();
				sqlStr.add("servertype", nrs[i].servertype);
				sqlStr.add("serverid", nrs[i].serverid);
				sqlStr.add("acticonn", numactive);
				sqlStr.add("freeconn", numidle);
				sqlStr.add("maxconn", nummax);
				sqlStr.add("freemem", freemem);
				sqlStr.add("totalmem", totalmem);
				sqlStr.add("maxmem", maxmem);
				sqlStr.add("totalthread", totalthread);
				sqlStr.addDateTime("createtime", MyTools.getTimeStr());
				DBHelper.logInsert(LogTbName.TAB_SERVER_STATE_LOG(), sqlStr);
			} else {
				sb.append("<font color='#FF0000'>"+nrs[i].name+"</font><br>�쳣��Ϣ��"+nrs[i].rv.info+"<br><br>");
			}
		}
	}
	
	//-------------��̬��----------------
	
	/**
	 * ��ʼ��
	 */
	public static void init(){
		ServerBAC.timer.scheduleAtFixedRate(new GetServerStateTT(), MyTools.long_minu*5, MyTools.long_minu*5, TimeUnit.MILLISECONDS);
	}
}
