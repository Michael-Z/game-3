package com.moonic.socket;

import java.sql.ResultSet;

import server.common.Tools;

import com.moonic.bac.FacMemBAC;
import com.moonic.bac.FansBAC;
import com.moonic.util.DBHelper;

import conf.Conf;


/**
 * ��������
 * @author John
 */
public class PushData {
	/**
	 * ָ�����
	 */
	public static final byte TARGET_PLA_SOME = 0;
	/**
	 * ���Լ�����������
	 */
	public static final byte TARGET_PLA_NOS = 1;
	/**
	 * �������
	 */
	public static final byte TARGET_PLA_ALL = 2;
	
	/**
	 * ����Ŀ������
	 */
	public byte target;
	
	/**
	 * ����
	 */
	public short act;
	/**
	 * ������Ϣ
	 */
	public String info;
	
	/**
	 * TARGET=TARGET_USER_SOME Ŀ������ || TARGET=TARGET_SOME Ŀ������
	 */
	public int[] byids;
	/**
	 * TARGET=TARGET_NOSELF_All ��ұ��
	 */
	public int myid;
	
	/**
	 * ����ʱ��
	 */
	public String overtimeStr;
	/**
	 * �쳣��Ϣ
	 */
	public String excepInfo;
	/**
	 * ����ʱ��
	 */
	public String pushtime;
	/**
	 * ����ʱ��
	 */
	public long timemark;
	/**
	 * ��ָ��������ɫʱ��֮ǰ�ŷ���
	 */
	public long beforecreatetime;
	
	/**
	 * �����������
	 */
	public boolean allowIgnore;
	/**
	 * �⻺��
	 */
	public boolean nopool;
	/**
	 * ���͸�ͬ����ʱ�ĳ������
	 */
	public int scenenum;
	/**
	 * ���͸�ͬ������������
	 */
	public short maxsend;
	/**
	 * ���͸�ͬ����ʱ�����Ʒ�Χ
	 */
	public int[] range;//X,Z,W,H
	/**
	 * ���͸�ͬ����ʱ�ų������ID
	 */
	public int[] excludepid;
	/**
	 * �Ƿ�Ϊϵͳ�ʺ���
	 */
	public boolean isSysMsg;
	
	/**
	 * ����
	 */
	public PushData(){
		timemark = System.currentTimeMillis();
	}
	
	/**
	 * ���ù���ʱ��
	 */
	public PushData setOverTime(String overtimeStr){
		this.overtimeStr = overtimeStr;
		return this;
	}
	
	/**
	 * �����Ƿ��������
	 */
	public PushData setAllowIgnore(boolean allowIgnore) {
		this.allowIgnore = allowIgnore;
		return this;
	}
	
	/**
	 * ����ʱ��(��ǰ�����͸��������������Ч)
	 */
	public PushData setBeforecreatetime(long beforecreatetime) {
		this.beforecreatetime = beforecreatetime;
		return this;
	}
	
	/**
	 * �����⻺��
	 */
	public PushData setNopool(boolean nopool) {
		this.nopool = nopool;
		return this;
	}
	
	/**
	 * ����Ϊ�ʺ���
	 */
	public PushData setSysMsg(){
		this.isSysMsg = true;
		return this;
	}
	
	/**
	 * ��д
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(pushtime != null){
			sb.append(pushtime+" - ");
		}
		sb.append("������");
		sb.append(act+",");
		sb.append("Ŀ�꣺");
		sb.append(target+",");
		sb.append("��Ϣ��");
		sb.append(info);
		return sb.toString();
	}
	
	//--------------������---------------
	
	/**
	 * ���͸��������
	 */
	public void sendPlaToOne(short act, String info, int byid){
		sendPlaToSome(act, info, new int[]{byid});
	}
	
	/**
	 * ���͸���ҵ�FANS
	 */
	public void sendPlaToFans(short act, String info, int playerid, byte type) throws Exception {
		int[] byids = FansBAC.getInstance().getFansIds(playerid, type);
		sendPlaToSome(act, info, byids);
	}
	
	/**
	 * ���͸�ָ�����
	 */
	public void sendPlaToSome(short act, String info, int[] byids){
		if(byids == null){
			return;
		}
		this.target = TARGET_PLA_SOME;
		this.act = act;
		this.info = info;
		this.byids = byids;
		SocketServer.getInstance().addPush(this, 1);
	}
	
	/**
	 * ���͸����Լ����������
	 */
	public void sendPlaToNosOL(short act, String info, int myid){
		this.target = TARGET_PLA_NOS;
		this.act = act;
		this.info = info;
		this.myid = myid;
		SocketServer.getInstance().addPush(this, 2);
	}
	
	/**
	 * ���͸������������
	 */
	public void sendPlaToAllOL(short act, String info){
		this.target = TARGET_PLA_ALL;
		this.act = act;
		this.info = info;
		SocketServer.getInstance().addPush(this, 3);
	}
	
	/**
	 * ���͸������ˣ��������������
	 */
	public void sendPlaToAllSql(DBHelper dbHelper, short act, String info) throws Exception {
		ResultSet plaRs = dbHelper.query("tab_player", "id", "serverid="+Conf.sid);
		byids = new int[dbHelper.getRsDataCount(plaRs)];
		while(plaRs.next()){
			byids[plaRs.getRow()-1] = plaRs.getInt("id");
		}
		this.target = TARGET_PLA_SOME;
		this.act = act;
		this.info = info;
		SocketServer.getInstance().addPush(this, 4);
	}
	
	/**
	 * �������а���
	 */
	public void sendPlaToFacMem(short act, String info, int factionid, int... excludeid) throws Exception {
		int[] byids = FacMemBAC.getInstance().getFacMemIDs(factionid, excludeid);
		PushData.getInstance().sendPlaToSome(act, info, byids);
	}
	
	/**
	 * �������а��ɹ���
	 */
	public void sendPlaToFacMgr(short act, String info, int factionid, int excludeid) throws Exception {
		int[] byids = FacMemBAC.getInstance().getFacMgrIDs(factionid, excludeid);
		PushData.getInstance().sendPlaToSome(act, info, byids);
	}
	
	/**
	 * ���͸�FANS�Ͱ���
	 */
	public void sendPlaToFansAndNosFac(short act, String info, int factionid, int myid, byte type) throws Exception {
		int[] byids1 = FansBAC.getInstance().getFansIds(myid, type);
		int[] byids2 = FacMemBAC.getInstance().getFacMemIDs(factionid, myid);
		for(int i = 0; byids2!=null && i<byids2.length; i++){
			if(!Tools.intArrContain(byids1, byids2[i])){
				byids1 = Tools.addToIntArr(byids1, byids2[i]);
			}
		}
		PushData.getInstance().sendPlaToSome(act, info, byids1);
	}
	
	//--------------��̬��---------------
	
	/**
	 * ��ȡʵ��
	 */
	public static PushData getInstance(){
		return new PushData();
	}
}
