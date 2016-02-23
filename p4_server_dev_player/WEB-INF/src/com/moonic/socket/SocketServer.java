package com.moonic.socket;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.bac.ConfigBAC;
import com.moonic.bac.PlayerBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.bac.UserBAC;
import com.moonic.mgr.LockStor;
import com.moonic.servlet.STSServlet;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.MyLog;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;
import com.moonic.util.Out;
import com.moonic.util.ProcessQueue;
import com.moonic.util.ProcessQueueTask;
import com.moonic.util.STSNetSender;

import conf.Conf;
import conf.LogTbName;

/**
 * �����
 * @author John
 */
public class SocketServer {
	/**
	 * ���ӷ�����
	 */
	public static final short ACT_CONNECT_SERVER = 1;
	/**
	 * ������
	 */
	public static final short ACT_ONLINE = -1;
	
	/**
	 * ������Ϣ
	 */
	public static final short ACT_SYS_TEST = 101;
	/**
	 * ��������
	 */
	public static final short ACT_SYS_BEOFFLINE = 102;
	/**
	 * �����˳���Ϸ
	 */
	public static final short ACT_SYS_BEEXIT = 103;
	/**
	 * ϵͳ֪ͨ
	 */
	public static final short ACT_SYS_INFORM = 104;
	/**
	 * ����Ϣ
	 */
	public static final short ACT_MESSAGE_RECEIVE = 151;
	/**
	 * ��Ϸ����
	 */
	public static final short ACT_MESSAGE_GAMEPUSH = 152;
	/**
	 * ������Ϣ
	 */
	public static final short ACT_MESSAGE_TOP = 153;
	/**
	 * �������
	 */
	public static final short ACT_PLAYER_ONLINE = 201;
	/**
	 * �������
	 */
	public static final short ACT_PLAYER_OFFLINE = 202;
	/**
	 * ֪ͨ��Ϸ�����˸
	 */
	public static final short ACT_ICON_FLASH = 203;
	/**
	 * �г�ֵ
	 */
	public static final short ACT_PLAYER_RECHARGE = 204;
	/**
	 * ������Ȩ
	 */
	public static final short ACT_PLAYER_BUY_TQ = 205;
	/**
	 * �иı�ֵ
	 */
	public static final short ACT_PLAYER_CHANGEVALUE = 206;
	/**
	 * �н�ɫ����
	 */
	public static final short ACT_PLAYER_LVUP = 207;
	/**
	 * �н�ɫ����
	 */
	public static final short ACT_PLAYER_RENAME = 208;
	/**
	 * ���ʼ�
	 */
	public static final short ACT_MAIL_RECEIVER = 251;
	/**
	 * �м������
	 */
	public static final short ACT_FACTION_JOIN = 301;
	/**
	 * �и����������
	 */
	public static final short ACT_FACTION_JOINCOND = 302;
	/**
	 * �и��°�ṫ��
	 */
	public static final short ACT_FACTION_UPD_INFO = 303;
	/**
	 * �е���ְλ
	 */
	public static final short ACT_FACTION_ADJUSET_POSITION = 304;
	/**
	 * ���߳�����
	 */
	public static final short ACT_FACTION_REMOVE_MEMBER = 305;
	/**
	 * ���˳�����
	 */
	public static final short ACT_FACTION_EXIT = 306;
	/**
	 * ��������
	 */
	public static final short ACT_FACTION_SHANRANG = 307;
	/**
	 * ���߳�����
	 */
	public static final short ACT_FACTION_BEREMOVE = 308;
	/**
	 * ����
	 */
	public static final short ACT_FACTION_IMPEACH = 309;
	/**
	 * �а����ʽ�仯
	 */
	public static final short ACT_FACTION_CHANGEMONEY = 310;
	/**
	 * ���ð�������
	 */
	public static final short ACT_FACTION_RESETDATA = 311;
	/**
	 * �����¼�
	 */
	public static final short ACT_FACTION_LOG = 312;
	/**
	 * �г�Ա����
	 */
	public static final short ACT_FACTION_RENAME = 313;
	/**
	 * ��������
	 */
	public static final short ACT_FACTION_UPLEVEL = 314;
	/**
	 * ��Ĥ��
	 */
	public static final short ACT_FACTION_WORSHIP = 315;
	/**
	 * ���������
	 */
	public static final short ACT_FACTION_APPLY = 316;
	/**
	 * �г�������
	 */
	public static final short ACT_FACTION_REVOCATION_APPLY = 317;
	/**
	 * �д�������
	 */
	public static final short ACT_FACTION_PROCESS_APPLY = 318;
	/**
	 * ��ͬ������
	 */
	public static final short ACT_FACTION_AGREE_APPLY = 319;
	/**
	 * �оܾ�����
	 */
	public static final short ACT_FACTION_REFUSE_APPLY = 320;
	/**
	 * �а��ɿƼ�����
	 */
	public static final short ACT_FACTION_UP_TECHNOLOGY = 321;
	/**
	 * ������ս��
	 */
	public static final short ACT_JJC_RANKING_BATTLE = 451;
	/**
	 * �����ٻ�����Ʒˢ��
	 */
	public static final short ACT_SUMMON_DAY_ITEM = 501;
	/**
	 * �����ٻ�����Ʒˢ��
	 */
	public static final short ACT_SUMMON_WEEK_ITEM = 502;
	/**
	 * ���ɸ���-���븱��
	 */
	public static final short ACT_FACCOPYMAP_INTO = 551;
	/**
	 * ���ɸ���-ս������
	 */
	public static final short ACT_FACCOPYMAP_END = 552;
	/**
	 * ���ɸ���-���õ�ͼ
	 */
	public static final short ACT_FACCOPYMAP_RESETMAP = 553;
	/**
	 * ���ɸ���-�˳�
	 */
	public static final short ACT_FACCOPYMAP_EXIT = 554;
	/**
	 * ��ս-����ս
	 */
	public static final short ACT_CB_DECLAREWAR = 601;
	/**
	 * ��ս-��̫�ر��
	 */
	public static final short ACT_CB_CHANGELEADER = 602;
	/**
	 * ��ս-��ս����
	 */
	public static final short ACT_CB_BATTLE_END = 603;
	/**
	 * ��ս-ս�����
	 */
	public static final short ACT_CB_BATTLE_RESULT = 604;
	/**
	 * ��ս-�ɳ�����
	 */
	public static final short ACT_CB_DISPATCH = 605;
	/**
	 * ��ս-��һ������Ϣ
	 */
	public static final short ACT_CB_NEXT_BATTLE = 606;
	/**
	 * ��ս-���ɲ�������
	 */
	public static final short ACT_CB_FACTION_AWARD = 607;
	/**
	 * �Ӻ���
	 */
	public static final short ACT_FRIEND_ADD = 651;
	/**
	 * ɾ����
	 */
	public static final short ACT_FRIEND_DELETE = 652;
	/**
	 * ��������
	 */
	public static final short ACT_FRIEND_PRESENT = 653;
	/**
	 * ����BOSS��ʼ
	 */
	public static final short ACT_WORLD_BOSS_START = 701;
	/**
	 * ��ӻ-��ʼ
	 */
	public static final short ACT_TEAM_ACTI_START = 751;
	/**
	 * ��ӻ-�������
	 */
	public static final short ACT_TEAM_ACTI_JOIN = 752;
	/**
	 * ��ӻ-�߳�����
	 */
	public static final short ACT_TEAM_ACTI_KICK = 753;
	/**
	 * ��ӻ-����
	 */
	public static final short ACT_TEAM_ACTI_FORMAT = 754;
	/**
	 * ��ӻ-׼��
	 */
	public static final short ACT_TEAM_ACTI_BEREADY = 755;
	/**
	 * ��ӻ-ȡ��׼��
	 */
	public static final short ACT_TEAM_ACTI_CANCELREADY = 756;
	/**
	 * ��ӻ-ս��
	 */
	public static final short ACT_TEAM_ACTI_BATTLE = 757;
	/**
	 * ��ӻ-�رշ���
	 */
	public static final short ACT_TEAM_ACTI_CLOSE = 758;
	/**
	 * ��ӻ-�˳�����
	 */
	public static final short ACT_TEAM_ACTI_EXIT = 759;
	/**
	 * �ڿ�-��ռ��
	 */
	public static final short ACT_MINERALS_CLOCKIN = 801;
	/**
	 * �ڿ�-������
	 */
	public static final short ACT_MINERALS_CONDENT = 802;
	/**
	 * �ڿ�-��ʼ�
	 */
	public static final short ACT_MINERALS_START = 803;
	/**
	 * �ڿ�-�����
	 */
	public static final short ACT_MINERALS_END = 804;
	
	//public static final int MAX_USER = 1000;//������
	
	//public static final int TCP_PORT = Conf.socket_port;//���Ӷ˿�
	
	private boolean isRun;
	private ServerSocket serversocket;
	
	public Hashtable<Integer, Player> plamap = new Hashtable<Integer, Player>(8192);//(KEY=PID)
	public Hashtable<String, Player> session_plamap = new Hashtable<String, Player>(8192);//(KEY=SESSIONID)
	
	public ArrayList<String> plainfolist = new ArrayList<String>();
	
	private LinkedList<PushData> pushQueue = new LinkedList<PushData>();
	
	public MyLog pushlog;
	public MyLog log;
	public MyLog connectlog;
	public MyLog oclog;
	
	/**
	 * ����
	 */
	public SocketServer(){
		pushlog = new MyLog(MyLog.NAME_DATE, "log_socket_push", "SOCKET_PUSH", Conf.debug, false, true, null);
		log = new MyLog(MyLog.NAME_DATE, "log_socket", "Socket", Conf.debug, false, true, null);
		connectlog = new MyLog(MyLog.NAME_DATE, "log_socket_connect", "SOCKET_CONNECT", Conf.debug, false, true, null);
		oclog = new MyLog(MyLog.NAME_DATE, "log_socket_oc", "SOCKET_OC", Conf.debug, false, true, null);
	}
	
	/**
	 * ����������
	 */
	public ReturnValue start() {
		if(!isRun) {
			try {
				isRun = true;
				timer = MyTools.createTimer(3);
				(new ConnectListener()).start();
				(new Pusher()).start();
				TCPAmount=0;
				return new ReturnValue(true,"��"+Conf.socket_port+"�˿ڵ�TCP�������������ɹ���");
			} catch(Exception ex) {
				ex.printStackTrace();
				return new ReturnValue(false,"��"+Conf.socket_port+"�˿ڵ�TCP������������ʧ�ܡ�");
			}
		} else {
			return new ReturnValue(false,"��"+Conf.socket_port+"�˿ڵ�TCP���������������У�����ֹͣ��");
		}
	}
	
	/**
	 * ֹͣ������
	 */
	public ReturnValue stop() {
		if(isRun) {
			isRun = false;
			try {
				Player[] plaarr = plamap.values().toArray(new Player[plamap.size()]);
				for(int i = 0; i < plaarr.length; i++){
					plaarr[i].stop("������ֹͣ");
				}
				MyTools.cancelTimer(timer);
				serversocket.close();
				return new ReturnValue(true,"��"+Conf.socket_port+"�˿ڵ�TCP��������ֹͣ�ɹ���");
			} catch (Exception e) {
				e.printStackTrace();
				return new ReturnValue(true,"��"+Conf.socket_port+"�˿ڵ�TCP��������ֹͣʧ��"+e.toString());
			}
		} else {
			return new ReturnValue(false,"��"+Conf.socket_port+"�˿ڵ�TCP��������û������������������");
		}
	}
	
	/**
	 * ��ȡ���Ͷ�������
	 */
	public String getPushQueueData(){
		PushData[] pdarr = pushQueue.toArray(new PushData[pushQueue.size()]);
		StringBuffer sb = new StringBuffer();
		sb.append("�����Ͷ��У�\r\n\r\n");
		for(int i = 0; pdarr!=null && i < pdarr.length; i++){
			sb.append(pdarr[i].toString()+"\r\n");
		}
		return sb.toString();
	}
	
	/**
	 * ������Ͷ���
	 */
	public String clearPushQueue(){
		pushQueue.clear();
		return "����ɹ�";
	}
	
	/**
	 * ���ָ����ɫ�Ƿ�����
	 */
	public boolean checkOnline(int playerid){
		return plamap.get(playerid) != null;
	}
	
	/**
	 * ��ȡ����״̬
	 */
	public ReturnValue getRunState(){
		StringBuffer sb = new StringBuffer();
		if(isRun){
			sb.append("\r\n");
			sb.append("��������\r\n");
			sb.append("��ǰ������(AM/MAP)��" + Player.totalThreadAmount + "/" + plamap.size()+"\r\n");
			sb.append("�����ͳ��ȣ�" + pushQueue.size()+"\r\n");
			sb.append("������¼��\r\n" );
			/*for(int i = 0; i < pushop.size(); i++){
				sb.append(pushop.get(i)+"\r\n");
			}
			sb.append("�ȴ���¼��\r\n");
			for(int i = 0; i < pushwait.size(); i++){
				sb.append(pushwait.get(i)+"\r\n");
			}
			sb.append("������ʷ��\r\n");
			for(int i = 0; i < pushhistory.size(); i++){
				sb.append(pushhistory.get(i)+"\r\n");
			}*/
		} else {
			sb.append("��ֹͣ");
		}
		return new ReturnValue(isRun, sb.toString());
	}
	
	/**
	 * �Ͽ����н�ɫ
	 */
	public ReturnValue clearAllPla(String info, int type){
		StringBuffer sb = new StringBuffer();
		try {
			sb.append("\r\n�Ͽ��б�\r\n");
			Player[] plaarr = plamap.values().toArray(new Player[plamap.size()]);
			for(int i = 0; i < plaarr.length; i++){
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					DataOutputStream baos_dos = new DataOutputStream(baos);
					baos_dos.writeShort(type==0?SocketServer.ACT_SYS_BEEXIT:SocketServer.ACT_SYS_BEOFFLINE);
					baos_dos.writeShort(-100);//��ʾ����MARKƥ��
					baos_dos.write(info.getBytes("UTF-8"));
					baos_dos.close();
					byte[] pushdata = baos.toByteArray();
					exePush(plaarr[i].dos, pushdata);	
				} catch (Exception e) {}
				STSNetSender sender = new STSNetSender(STSServlet.M_USER_LOGOUT);
				sender.dos.writeInt(plaarr[i].uid);
				sender.dos.writeUTF("�Ͽ����н�ɫ");
				ServerBAC.getInstance().sendReqToMain(sender);
				PlayerBAC.getInstance().logout(plaarr[i].pid, "�Ͽ����н�ɫ");
				Thread.sleep(10);
				sb.append("�û�ID��"+plaarr[i].uid+"-"+plaarr[i].pname+"("+plaarr[i].pid+")\r\n");
			}
			MyTools.cancelTimer(timer);
			timer = MyTools.createTimer(3);
			return new ReturnValue(true, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * �Ͽ�ָ����ɫ
	 */
	public ReturnValue breakOnePla(int playerid, String info){
		try {
			Player pla = plamap.get(playerid);
			if(pla == null){
				BACException.throwInstance("��ɫ������");
			}
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream baos_dos = new DataOutputStream(baos);
				baos_dos.writeShort(SocketServer.ACT_SYS_BEEXIT);
				baos_dos.writeShort(-100);//��ʾ����MARKƥ��
				baos_dos.write(info.getBytes("UTF-8"));
				baos_dos.close();
				byte[] pushdata = baos.toByteArray();
				exePush(pla.dos, pushdata);
			} catch (Exception e) {}
			STSNetSender sender = new STSNetSender(STSServlet.M_USER_LOGOUT);
			sender.dos.writeInt(pla.uid);
			sender.dos.writeUTF("�Ͽ�ָ����ɫ");
			ServerBAC.getInstance().sendReqToMain(sender);
			PlayerBAC.getInstance().logout(pla.pid, "�Ͽ�ָ����ɫ");
			String str = "�û�ID��"+pla.uid+"-"+pla.pname+"("+pla.pid+")\r\n";
			return new ReturnValue(true, str);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ȡ���߽�ɫ����
	 */
	public Player[] getPlaArr(){
		return plamap.values().toArray(new Player[plamap.size()]);
	}
	
	/**
	 * ��ָ����ɫ������Ϣ
	 */
	public void push(int pid, PushData pd){
		Player pla = plamap.get(pid);
		if(pla != null){
			push(pla, pd);
		}
	}
	
	/**
	 * ��ָ����ɫ������Ϣ
	 */
	public void push(Player pla, PushData pd){
		if(pla!=null){
			push(pla, pd, pla.pushmark, true);
		}
	}
	
	/**
	 * ��ָ����ɫ������Ϣ
	 */
	public void push(Player pla, PushData pd, short pushmark, boolean isnew){
		synchronized (LockStor.getLock(LockStor.PUSH_LOCK, pla.pid)) {
			try {
				if(pla.pushmark==-1 && !pd.isSysMsg){
					return;
				}
				//System.out.println("pla.conf_receive_game_log:"+pla.conf_receive_game_log+" pd.allowIgnore:"+pd.allowIgnore);
				if(!pla.conf_receive_game_push && pd.allowIgnore){
					return;
				}
				if(pd.nopool){
					pushmark = -100;
				} else {
					if(isnew){
						pla.breaklinepdindex.add(pla.pushmark);
						pla.breaklinepd.add(pd);
						pushlog.d(pla.pname+"("+pla.pid+","+pla.isPush+")��������Ϣ�������ͻ���");
						if(pla.pushmark < Short.MAX_VALUE){
							pla.pushmark++;
						} else {
							pla.pushmark = 0;
						}
						pushlog.d(pla.pname+"("+pla.pid+")�������ǩֵ:"+pla.pushmark);		
					}	
				}
				if(pla.isPush){
					pushPq[pla.usepushindex].addTask(new PushTask(pla, pd, pushmark, pla.connectmark));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static ProcessQueue[] pushPq = new ProcessQueue[10];
	public static byte nextuseindex;
	public static byte[] pushindex_lock = new byte[0];
	static {
		for(int i = 0; i < pushPq.length; i++){
			pushPq[i] = new ProcessQueue();
		}
	}
	
	/**
	 * ��������
	 * @author John
	 */
	class PushTask implements ProcessQueueTask {
		private Player pla;
		private PushData pd;
		private short pushmark;
		private byte connectmark;
		public PushTask(Player pla, PushData pd, short pushmark, byte connectmark){
			this.pla = pla;
			this.pd = pd;
			this.pushmark = pushmark;
			this.connectmark = connectmark;
		}
		public void execute() {
			try {
				if(pla.isPush && pla.connectmark==connectmark){
					DiscardPushTT discardPushTt = null;
					try {
						discardPushTt = new DiscardPushTT(pla, pd);
						timer.schedule(discardPushTt, 1000, TimeUnit.MILLISECONDS);
						pushlog.d("�� " + pla.pname+"("+pla.pid+") ���� " + pd.act + "," + pushmark + "," + pd.info);
						byte[] infoBytes = pd.info.getBytes("UTF-8");
						//addToPushOp(PS_PUSH1);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						DataOutputStream baos_dos = new DataOutputStream(baos);
						baos_dos.writeShort(pd.act);
						baos_dos.writeShort(pushmark);
						baos_dos.write(infoBytes);
						baos_dos.close();
						byte[] pushdata = baos.toByteArray();
						exePush(pla.dos, pushdata);
					} catch (SocketException e) {
						pushlog.e("�����쳣("+pla.pname+")��"+e.toString());
						pla.exceptionstop("����SocketException�쳣");
					} catch (Exception e) {
						e.printStackTrace();
						pla.exceptionstop("����"+e.toString()+"�쳣");
					} finally {
						discardPushTt.cancel();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * ִ������
	 */
	public void exePush(DataOutputStream dos, byte[] pushdata) throws Exception {
		synchronized (dos) {
			dos.writeInt(pushdata.length);
			dos.write(pushdata);
			dos.flush();
		}
	}
	
	/**
	 * �����ͼ�ʱ��
	 */
	class DiscardPushTT extends MyTimerTask {
		private Player pla;
		//private PushData pd;
		public DiscardPushTT(Player pla, PushData pd){
			this.pla = pla;
			//this.pd = pd;
		}
		public void run2() {
			try {
				//Out.println("���ͳ�ʱ "+pla.pname+"("+pla.pid+")"+pd.toString());
				pla.exceptionstop("���ͳ�ʱ");
				//Out.println("�쳣ǿ���ж� "+pla.pname+"("+pla.pid+") ����ʧ��");
				//pla.dos.close();
				//pla.dis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * ����������־
	 */
	public void createPushLog(PushData pd) throws Exception {
		String content = pd.info;
		if(content!=null && content.length()>500){
			content = content.substring(0, 499);
		}
		SqlString sqlStr = new SqlString();
		sqlStr.add("act", pd.act);
		sqlStr.add("target", pd.target);
		sqlStr.add("content", content);
		sqlStr.add("offlinesave", 0);
		sqlStr.addDateTime("overtime", pd.overtimeStr);
		sqlStr.add("exception", pd.excepInfo);
		sqlStr.add("timemark", pd.timemark);
		sqlStr.addDateTime("createtime", Tools.getCurrentDateTimeStr());
		DBHelper.logInsert(LogTbName.TAB_PUSH_LOG(), sqlStr);
	}
	
	public static byte[] QUEUE_LOCK = new byte[0];
	/**
	 * ������������
	 */
	public void addPush(PushData pushdata, int from){
		synchronized(QUEUE_LOCK){
			if(pushdata != null){
				pushQueue.offer(pushdata);		
			} else {
				Out.println("��Ҫ����Ŀ����Ͷ������ԣ�" + from);
			}		
		}
	}
	
	/**
	 * ��ȡ��������
	 */
	public int getOnlinePlayerAmount() {
		if(session_plamap==null) {
			return 0;
		} else {			
			return session_plamap.size();
		}
	}
	
	public static byte[] REMOVE_PLA = new byte[0];
	/**
	 * �Ƴ���ɫ
	 */
	public void removePla(int pid, String reason){
		synchronized (REMOVE_PLA) {
			Player pla = plamap.get(pid);
			if(pla != null){
				pla.stop("�Ƴ����("+reason+")");
				if(pla.breaklineTT != null){
					pla.breaklineTT.cancel();
					pla.breaklineTT = null;
					log.d("�Ƴ�����û�"+pla.pname+"("+pla.pid+")��ֹͣ����������ʱ��");
				}
				pushlog.d(pla.pname+"("+pla.pid+")�����ͻ����е�������Ϣ�洢�����ݿ�("+pla.breaklinepd.size()+")");
				plamap.remove(pid);
				int old_amount = session_plamap.size();
				session_plamap.remove(pla.sessionid);
				UserBAC.session_usermap.remove(pla.sessionid);
				//System.out.println("�Ƴ�USER��"+pla.sessionid);
				connectlog.d("�Ƴ���ɫ��" + pla.pname + "("+ pla.pid + "," + pla.sessionid + ")" + " ������" + old_amount + " -> " + session_plamap.size());
			}
		}
	}
	
	public ScheduledExecutorService timer;
	
	//-------------�ڲ���---------------
	public static int TCPAmount=0; //����TCP���Ӽ���
	
	public DecimalFormat deciamalformat = new DecimalFormat("000000");
	
	/**
	 * ���Ӽ�����
	 * @author John
	 */
	class ConnectListener extends Thread {
		public void run() {
			Player.totalThreadAmount=0; //��ɫ�߳�����λ
			try {				
				Out.println("׼������TCP����,����"+Conf.socket_port+"�˿�");
				serversocket = new ServerSocket(Conf.socket_port,1000);
				Out.println("����TCP�������,��ʼ����"+Conf.socket_port+"�˿�");
				connectlog.d("����TCP�������,��ʼ����"+Conf.socket_port+"�˿�");
				while (isRun) {
					Socket socket = null;
					try {
						socket = serversocket.accept();
						ConnectionThread ct = new ConnectionThread(socket);
						ct.start();
					} catch (Exception e) {
						e.printStackTrace();
					}					
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				SocketServer.this.stop();
				TCPAmount=0;
				Player.totalThreadAmount=0;
				connectlog.d("�˿�"+Conf.socket_port+"��TCP�����ѹر�");
				connectlog.save();
				Out.println(Tools.getCurrentDateTimeStr()+"--"+"�˿�"+Conf.socket_port+"��TCP�����ѹر�");
			}
		}
	}
	
	/**
	 * �����߳�
	 */
	class ConnectionThread extends Thread {
		private Socket socket;
		public ConnectionThread(Socket socket){
			this.socket = socket;
		}
		public void run() {
			DataInputStream dis = null;
			DataOutputStream dos = null;
			String sessionid = null;
			ReturnValue val = null;
			SqlString reqSqlStr = null;
			long reqtime = 0;
			try {
				if(socket == null){
					BACException.throwInstance("�������ͷ�����ʧ��");
				}
				TCPAmount++;
				oclog.d("[open] -- [" + deciamalformat.format(TCPAmount) + "]");
				connectlog.d("�ɹ���������"+socket.getRemoteSocketAddress()+"�ĵ�"+TCPAmount+"��TCP����");
				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());
				connectlog.d("��ȡ������������");
				connectlog.d("����SESSION_ID��ȡ��ʱ��");
				SessionidTT sessionidTT = new SessionidTT(dis);
				timer.schedule(sessionidTT, 10000, TimeUnit.MILLISECONDS);
				long t1 = System.currentTimeMillis();
				sessionid = dis.readUTF();
				sessionidTT.cancel();
				long t2 = System.currentTimeMillis();
				connectlog.d("��dis��ȡSESSION_ID��" + sessionid + "��ʱ[" + (t2-t1) + "]ms ȡ����ȡ��ʱ��");
				reqtime = System.currentTimeMillis();
				reqSqlStr = new SqlString();
				reqSqlStr.addDateTime("reqtime", Tools.getCurrentDateTimeStr());
				reqSqlStr.add("reqdata", sessionid);
				Player pla = null;
				/*if(session_plamap.size() >= Conf.max_player) {
					val = new ReturnValue(false, "��������������,���Ժ�����");
					LogBAC.logout("tcplogin", "�ﵽ�����������="+Conf.max_player+",��ǰ���������="+session_plamap.size());
					connectlog.d("TCP������������");
				} else*/ 
				if(sessionid == null || sessionid.equals("")) {
					val = new ReturnValue(false, "��Ч����");
					connectlog.d("SESSIONID��Ч������ȡ��");
				} else {
					pla = session_plamap.get(sessionid);
					if(pla != null){
						if(pla.isRun) {
							try {
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								DataOutputStream baos_dos = new DataOutputStream(baos);
								baos_dos.writeShort(SocketServer.ACT_SYS_BEOFFLINE);
								baos_dos.writeShort(-100);//��ʾ����MARKƥ��
								baos_dos.write("����ʺ��������ط����ߣ��㽫�������ߡ�".getBytes("UTF-8"));
								baos_dos.close();
								byte[] pushdata = baos.toByteArray();
								exePush(pla.dos, pushdata);	
							} catch (Exception e) {}
							pla.stop("����ٴ����ӣ��Ͽ���������");
							try {
								Thread.sleep(200);	//��ʱ�ȴ����߳���������
							} catch(Exception ex) {
								ex.printStackTrace();
							}
							connectlog.d("�����û���һ����");
						}
						pla.socket = socket;
						pla.dis = dis;
						pla.dos = dos;
						pla.tcpnum = TCPAmount;
						pla.start();
						JSONArray successarr = new JSONArray();
						successarr.add(pla.pushmark);
						if(pla.isNew){
							successarr.add("���ӷ������ɹ�");
							connectlog.d("���ӿͻ���  " + pla.pname + "�ɹ�");
						} else {
							successarr.add("���������������ɹ�");
							connectlog.d("���������ͻ���  " + pla.pname + "��ֹͣ����������ʱ��");
						}
						val = new ReturnValue(true, successarr.toString());
						reqSqlStr.add("userid", pla.pid);
					} else {
						val = new ReturnValue(false, "��δ��¼��ɫ");
					}
				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream baos_dos = new DataOutputStream(baos);
				baos_dos.writeShort(ACT_CONNECT_SERVER);
				baos_dos.writeByte(val.success ? 1 : 0);
				baos_dos.writeUTF(val.info);
				baos_dos.close();
				byte[] pushdata = baos.toByteArray();
				exePush(dos, pushdata);
				connectlog.d("�������ӽ����" + val.success + "," + val.info);
				if(val.success){
					if(pla.breaklineTT != null){
						pla.breaklineTT.cancel();
						pla.breaklineTT = null;
					}
				} else {
					socket.close();
					connectlog.d("����ʧ�ܣ��ر�SOCKET");
					oclog.d("[close] -- [" + deciamalformat.format(TCPAmount) + "]" + " -- ԭ��" + val.info);
				}
			} catch (Exception e) {
				connectlog.d("���ӳ����쳣��" + e.toString() + " ��ֹ����" + sessionid + ",Socket���ر� ");
				//System.out.println("Socket���ӳ����쳣");
				//e.printStackTrace();
				val = new ReturnValue(false, e.toString());
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					DataOutputStream baos_dos = new DataOutputStream(baos);
					baos_dos.writeShort(ACT_CONNECT_SERVER);
					baos_dos.writeByte(0);
					baos_dos.writeUTF(e.toString());
					baos_dos.close();
					byte[] pushdata = baos.toByteArray();
					exePush(dos, pushdata);
					connectlog.d("�����쳣������" + e.toString());
				} catch(Exception ex){
					//ex.printStackTrace();
					connectlog.d("�����쳣��ķ����쳣������" + ex.getMessage());
				} finally {
					if(socket != null){
						try {
							socket.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						connectlog.d("�����쳣���ر�SOCKET");
						oclog.d("[close] -- [" + deciamalformat.format(TCPAmount) + "]" + " -- ԭ��" + e.toString());
					}
				}
			} finally {
				try {
					if(reqSqlStr != null){
						reqSqlStr.addDateTime("resptime", Tools.getCurrentDateTimeStr());
						reqSqlStr.add("respresult", val.success ? 0 : 1);
						reqSqlStr.add("respdata", val.info);
						reqSqlStr.add("usedtime", System.currentTimeMillis()-reqtime);
						DBHelper.logInsert(LogTbName.TAB_SOCKET_LOG(), reqSqlStr);
						connectlog.d("���ӹ��̽���������������־");
					}
				} catch (Exception e){
					e.printStackTrace();
					connectlog.d("����������־�����쳣��" + e.toString());
				}
				connectlog.d("---------------------------------");
			}
		}
	}
	
	/**
	 * SESSION_ID��ȡ��ʱ��
	 */
	class SessionidTT extends MyTimerTask{
		public DataInputStream dis;
		public SessionidTT(DataInputStream dis){
			this.dis = dis;
		}
		public void run2() {
			try {
				connectlog.d("SESSION_ID��ʱ��ʱ�䵽���رն�����");
				dis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//public ArrayList<String> pushop = new ArrayList<String>();
	//public ArrayList<String> pushwait = new ArrayList<String>();
	//public ArrayList<PushData> pushhistory = new ArrayList<PushData>();
	
	/**
	 * ���������¼
	 */
	/*public void addToPushOp(byte i){
		pushop.add(Tools.getCurrentDateTimeStr()+" "+pushstateStr[i]);
		if(pushop.size() > 10){
			pushop.remove(0);
		}
	}*/
	
	/**
	 * ����ȴ���¼
	 */
	/*public void addToPushWait(){
		pushwait.add(Tools.getCurrentDateTimeStr());
		if(pushwait.size() > 10){
			pushwait.remove(0);
		}
	}*/
	
	/**
	 * ������ʷ���ͼ�¼
	 */
	/*public void addTopushHistory(PushData pd){
		pd.pushtime = Tools.getCurrentDateTimeStr();
		pushhistory.add(pd);
		if(pushhistory.size() > 10){
			pushhistory.remove(0);
		}
	}*/
	
	//public static final byte PS_TOPUSH = 0;
	//public static final byte PS_PUSH1 = 1;
	//public static final byte PS_PUSH2 = 2;
	//public static final byte PS_PUSH3 = 3;
	//public static final byte PS_FINSIH = 4;
	//public static final byte PS_LOG = 5;
	//public static final byte PS_REMOVE = 6;
	//public static final byte PS_STOP = 7;
	
	
	//public static String[] pushstateStr = {"׼������", "����1", "����2", "����3", "�������", "������־", "�Ӷ����Ƴ����Ͷ���", "��ֹͣ"};
	
	/**
	 * ����
	 * @author John
	 */
	class Pusher extends Thread {
		public void run() {
			while(isRun){
				long t1 = System.currentTimeMillis();
				while(pushQueue.size() == 0){
					if(!isRun){
						//addToPushOp(PS_STOP);
						return;
					}
					//addToPushWait();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					t1 = System.currentTimeMillis();
				}
				//addToPushOp(PS_TOPUSH);
				PushData pd = pushQueue.get(0);
				try {
					if(pd.target == PushData.TARGET_PLA_SOME){
						for(int i = 0; pd.byids != null && i < pd.byids.length; i++){
							push(pd.byids[i], pd);
						}
					} else 
					if(pd.target == PushData.TARGET_PLA_NOS){
						Player[] plaarr = plamap.values().toArray(new Player[plamap.size()]);
						for(int i = 0; i < plaarr.length; i++){
							if(plaarr[i].pid != pd.myid){
								push(plaarr[i], pd);
							}
						}
					} else 
					if(pd.target == PushData.TARGET_PLA_ALL){
						Player[] plaarr = plamap.values().toArray(new Player[plamap.size()]);
						for(int i = 0; i < plaarr.length; i++){
							if(!(pd.beforecreatetime!=0 && plaarr[i].createtime>pd.beforecreatetime)){
								push(plaarr[i], pd);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					pd.excepInfo = e.toString();
				}
				//addToPushOp(PS_FINSIH);
				long t2 = System.currentTimeMillis();
				if(ConfigBAC.getBoolean("push_log") && t2-t1>ConfigBAC.getInt("logout_push_threshold")){
					try {
						createPushLog(pd);
						Out.println("������ʱ�䳬��"+ConfigBAC.getInt("logout_push_threshold")+"ms��"+pd.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				//addToPushOp(PS_LOG);
				synchronized(QUEUE_LOCK){
					pushQueue.poll();
				}
				//addToPushOp(PS_REMOVE);
				//addTopushHistory(pd);
			}
		}
	}
	
	//-------------��̬��---------------
	
	private static SocketServer ss = new SocketServer();
	
	/**
	 * ��ȡʵ������
	 */
	public static SocketServer getInstance(){
		return ss;
	}
}
