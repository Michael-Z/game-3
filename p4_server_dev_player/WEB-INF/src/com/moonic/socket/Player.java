package com.moonic.socket;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.ResultSet;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ehc.common.ReturnValue;
import com.moonic.bac.MsgBAC;
import com.moonic.bac.PlayerBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.bac.UserBAC;
import com.moonic.battle.BattleBox;
import com.moonic.mgr.LockStor;
import com.moonic.servlet.STSServlet;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;
import com.moonic.util.STSNetSender;

/**
 * ��ɫ
 * @author John
 */
public class Player {
	public boolean isNew = true;
	public boolean isRun;
	public boolean isPush;//��������
	public byte connectmark;//�ڼ�������
	
	public Socket socket;
	public DataInputStream dis;
	public DataOutputStream dos;
	
	public BreakLineTT breaklineTT;//�Զ����߼�ʱ��
	
	public long overtime;//��������ʱ��
	public OvertimeTT overtimeTT;//���ڼ�ʱ������ʱ��
	
	public Vector<Short> breaklinepdindex = new Vector<Short>();//�����±껺��
	public Vector<PushData> breaklinepd = new Vector<PushData>();//���Ͷ��󻺴�
	
	public static int totalThreadAmount;//�߳�������
	
	public int tcpnum;//TCP���ӱ��
	
	public long lastmsgtime;//���һ�η��Ե�ʱ��
	
	public short pushmark = -1;//���ͼ���MARK
	
	public boolean conf_receive_game_push = true;//�Ƿ������Ϸ����
	
	public String tag;//�����ʶ
	
	public byte usepushindex = 0;//ʹ�õ������߳��±�
	
	//-----------------��������ʱ��ʼ��------------------
	
	public String sessionid;
	public int uid;
	public int pid;
	public String pname;
	public long createtime;
	public JSONArray openfunc;//�ѿ������ܼ���
	
	//-----------------���ӳɹ����ʼ��------------------
	
	public String mac;//�û�MAC
	public String imei;//�û�IMEI
	public String platform;//�û�����
	
	//-----------------��������ʱ��ֵ------------------
	
	public String ip;
	public String channel;
	
	//-----------------��Ϸ�����и�ֵ------------------
	
	public BattleBox verifybattle_battlebox;//��֤ս����¼ս��������
	
	/**
	 * ����
	 */
	public Player(String sessionid, int uid, int pid, String pname, long createtime, JSONArray openfunc){
		StringBuffer sb = new StringBuffer();
		sb.append(pid);
		sb.append("=[");
		sb.append(MyTools.getTimeStr());
		sb.append("]=");
		sb.append(hashCode());
		tag = sb.toString();
		SocketServer.getInstance().plainfolist.add(tag);
		synchronized (SocketServer.pushindex_lock) {
			usepushindex = SocketServer.nextuseindex;
			if(SocketServer.nextuseindex < SocketServer.pushPq.length-1){
				SocketServer.nextuseindex++;
			} else {
				SocketServer.nextuseindex = 0;
			}		
		}
		this.sessionid = sessionid;
		this.uid = uid;
		this.pid = pid;
		this.pname = pname;
		this.createtime = createtime;
		this.openfunc = openfunc;
		//System.out.println("usepushindex:"+usepushindex);
	}
	
	/**
	 * ��ʼ�����ӳɹ��������
	 */
	public void initConnectSuccessData(){
		DBHelper dbHelper = new DBHelper();
		try {
			if(platform == null){
				ResultSet userRs = dbHelper.query(UserBAC.tab_user, "id,channel,username,wifi,devuser,onlinestate,serverid,playerid,sessionid,mac,imei,platform", "id="+uid);
				if(!userRs.next()){
					BACException.throwInstance("�û�δ�ҵ�");
				}
				initUserInfo(userRs);
			}
			//������Ҫ��ʼ������Ϸ���ݼ�������
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ʼ���û���Ϣ
	 */
	public void initUserInfo(ResultSet userRs) throws Exception {
		mac = userRs.getString("mac");
		imei = userRs.getString("imei");
		platform = userRs.getString("platform");
	}
	
	/**
	 * ����
	 */
	public void start() {
		if(!isRun){
			isRun = true;
			overtimeTT = new OvertimeTT();
			SocketServer.getInstance().timer.scheduleAtFixedRate(overtimeTT, MyTools.long_minu/2, MyTools.long_minu/2, TimeUnit.MILLISECONDS);
			//(new ReqHandler()).start();
			totalThreadAmount++;
			//Out.println("�û��߳��������ﵽ"+totalThreadAmount);
			initConnectSuccessData();
		}
	}
	
	/**
	 * ֹͣ
	 */
	public void stop(String tip){
		try {
			if(isRun){
				isRun = false;
				isPush = false;
				dis.close();
				dos.close();
				socket.close();
				overtimeTT.cancel();
				totalThreadAmount--;
				SocketServer.getInstance().oclog.d("[close] -- [" + SocketServer.getInstance().deciamalformat.format(tcpnum) + "]" + " -- ԭ��" + tip);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * �쳣����
	 */
	public void exceptionstop(String info){
		if(isRun){
			stop(info);
			startBreakLineTT(info);
		}
	}
	
	/**
	 * �������߼�ʱ��
	 */
	public void startBreakLineTT(String info){
		startBreakLineTT(MyTools.long_minu, info);
	}
	
	/**
	 * �������߼�ʱ��
	 */
	public void startBreakLineTT(long delay, String info){
		if(breaklineTT == null){
			breaklineTT = new BreakLineTT(info);
			SocketServer.getInstance().timer.schedule(breaklineTT, delay, TimeUnit.MILLISECONDS);
		}
	}
	
	/**
	 * ���¹���ʱ��
	 */
	public void updateOverTime(){
		overtime = System.currentTimeMillis() + MyTools.long_minu*2;
		SocketServer.getInstance().log.d("�յ� "+pname+" �����������¹���ʱ�䵽"+MyTools.getTimeStr(overtime));
	}
	
	/**
	 * ��������
	 */
	public void openPush() throws Exception {
		if(!isRun){
			BACException.throwAndOutInstance("��δ����Socket����("+pid+")");
		}
		if(isPush){
			BACException.throwAndOutInstance("�����Ѵ�("+pid+")");
		}
		SocketServer.getInstance().connectlog.d(pname+"("+pid+")��������");
		if(isNew){
			connectmark++;
			isPush = true;
			isNew = false;
			MsgBAC.getInstance().sendLoginSysMsg(pid);//���͵�¼�ʺ���
		} else {
			/*if(breaklineTT != null){
				breaklineTT.cancel();
				breaklineTT = null;
			}*/
			synchronized (LockStor.getLock(LockStor.PUSH_LOCK, pid)) {
				connectmark++;
				isPush = true;
				for(int k = 0; k < breaklinepd.size(); k++){
					SocketServer.getInstance().push(this, breaklinepd.get(k), breaklinepdindex.get(k), false);
				}	
			}
			SocketServer.getInstance().pushlog.d(pname+"("+pid+")�����ͻ����е����Ͷ�����뵽���������в��ÿ�����("+breaklinepd.size()+")");
		}
	}
	
	/**
	 * ��������״̬
	 * @param clearmark ��ʲô��ǩ��ʼ����
	 */
	public void updateOnlineState(short clearmark){
		if(isRun){
			short startmark = 0;
			boolean clear = false;
			synchronized (LockStor.getLock(LockStor.PUSH_LOCK, pid)) {
				for(int i = 0; i < breaklinepdindex.size(); i++){
					if(breaklinepdindex.get(i)==clearmark){
						startmark = breaklinepdindex.get(0);
						for(int k = 0; k <= i; k++){
							breaklinepdindex.remove(0);
							breaklinepd.remove(0);
						}
						clear = true;
						break;
					}
				}	
			}
			if(clear){
				SocketServer.getInstance().pushlog.d("�յ� "+pname+" �����������("+startmark+"~"+clearmark+")�����ͻ��� | "+breaklinepdindex.size());
			} else {
				SocketServer.getInstance().pushlog.d("�յ� "+pname+" ����������ʶ("+clearmark+")���������� | "+breaklinepdindex.size());
			}
			updateOverTime();
		} else {
			SocketServer.getInstance().log.d("�յ� "+pname+" �������������ѶϿ������¹���ʱ��ʧ��");
		}
	}
	
	/**
	 * ���ն���(��д)
	 */
	protected void finalize() throws Throwable {
		SocketServer.getInstance().plainfolist.remove(tag);
		super.finalize();
	}
	
	// ---------------�ڲ���---------------
	
	/**
	 * ������
	 * @author John
	 */
	class ReqHandler extends Thread {
		public void run() {
			try {
				while (isRun) {
					short act = dis.readShort();
					if(act == SocketServer.ACT_ONLINE){
						byte mark = dis.readByte();
						updateOnlineState(mark);
					} else {
						String str = dis.readUTF();
						SocketServer.getInstance().log.d("SocketServer�յ��ͻ������ݣ�" + str);
						JSONObject jsonobj = new JSONObject(str);
						ReturnValue val = processingReq(jsonobj);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						DataOutputStream baos_dos = new DataOutputStream(baos);
						baos_dos.writeShort(act);
						baos_dos.writeByte(val.success ? 1 : 0);
						baos_dos.writeUTF(val.info);
						baos_dos.close();
						byte[] pushdata = baos.toByteArray();
						SocketServer.getInstance().exePush(dos, pushdata);
					}
				}
			} catch (EOFException e) {
				SocketServer.getInstance().log.d("�Ͽ��ͻ���  " + pname + " �쳣" + e.toString());
			} catch (SocketException e) {
				SocketServer.getInstance().log.d("�Ͽ��ͻ���  " + pname + " �쳣" + e.toString());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				exceptionstop("��������ʱ�����쳣");
				SocketServer.getInstance().log.d(pname + " Socket�쳣���ߣ�����������������ʱ");
			}
		}
		public ReturnValue processingReq(JSONObject jsonobj) throws Exception{
			{
				return new ReturnValue(false, "��Ч����");
			}
		}
	}
	
	/**
	 * �����û���ʱ����
	 */
	class BreakLineTT extends MyTimerTask {
		public String info;
		public BreakLineTT(String info){
			this.info = info+"("+MyTools.getTimeStr()+")";
			SocketServer.getInstance().log.d(pname+"("+pid+") Socket�����쳣���ߵ���ʱ("+this.info+")");
		}
		public void run2() {
			try {
				PushData.getInstance().sendPlaToOne(SocketServer.ACT_SYS_TEST, "�쳣���ߵ���ʱ������ע���û���¼("+info+")", pid);
				STSNetSender sender = new STSNetSender(STSServlet.M_USER_LOGOUT);
				sender.dos.writeInt(uid);
				sender.dos.writeUTF("�쳣���ߵ���ʱ������ע���û���¼");
				ServerBAC.getInstance().sendReqToMain(sender);
				PlayerBAC.getInstance().logout(pid, "�쳣���ߵ���ʱ������ע���û���¼");
				SocketServer.getInstance().log.d(pname+"("+pid+") Socket�쳣���ߵ���ʱ������ע���û���¼("+info+")");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * ����ʱ��������
	 */
	class OvertimeTT extends MyTimerTask {
		public OvertimeTT(){
			updateOverTime();
		}
		public void run2() {
			if(System.currentTimeMillis() > overtime){
				exceptionstop("�ѵ�����ʱ��"+MyTools.getTimeStr(overtime));
				SocketServer.getInstance().log.d(pname + " Socket�ѵ�����ʱ�䣬����������������ʱ");
			}
		}
	}
}