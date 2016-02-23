package com.moonic.mirror;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import org.json.JSONArray;

import server.common.Tools;
import server.config.ServerConfig;

import com.ehc.common.SqlString;
import com.moonic.mgr.LockStor;
import com.moonic.util.DBUtil;
import com.moonic.util.MyTools;

/**
 * �������
 * @author John
 */
public class MirrorMgr {
	/**
	 * ����-�������(com.moonic.bac.PetBAC)
	 */
	public static Hashtable<String, Mirror> classname_mirror = new Hashtable<String, Mirror>();
	/**
	 * ����-������󼯺�
	 */
	public static Hashtable<String, ArrayList<Mirror>> tab_mirrorobjTab = new Hashtable<String, ArrayList<Mirror>>();
	/**
	 * ��Ը�����Ҿ��񣺱���-�������
	 */
	public static Hashtable<String, Mirror> pla_mirrorobjTab = new Hashtable<String, Mirror>();
	
	/**
	 * ��ʼ��
	 */
	public static void init() throws Exception {
		File file = new File(ServerConfig.getWebInfPath()+"classes/com/moonic/bac");
		File[] files = file.listFiles();
		for(int i = 0; files != null && i < files.length; i++){
			String fileName = files[i].getName();
			if(fileName.endsWith(".class")){
				String classname = fileName.replace(".class", "");
				Class.forName("com.moonic.bac."+classname);		
			}
		}
	}
	
	/**
	 * ��ȡ��ɫ���о���
	 */
	public static String getPlaMirrorData(int playerid){
		Mirror[] mirrorobjs = pla_mirrorobjTab.values().toArray(new Mirror[pla_mirrorobjTab.size()]);
		StringBuffer sb = new StringBuffer();
		for(int i = 0; mirrorobjs != null && i < mirrorobjs.length; i++){
			sb.append(mirrorobjs[i].tab+"\r\n"+getPlaMirrorData(playerid, mirrorobjs[i])+"\r\n\r\n");
		}
		StringBuffer sb2 = new StringBuffer();
		sb2.append("����������"+sb.length()+"\r\n");
		sb2.append(sb);
		return sb2.toString();
	}
	
	/**
	 * ��ȡ��ɫָ������
	 */
	public static String getPlaMirrorData(int playerid, String tab){
		Mirror obj = pla_mirrorobjTab.get(tab);
		if(obj != null){
			return getPlaMirrorData(playerid, obj);
		} else {
			return "����δ�ҵ�";
		}
	}
	
	/**
	 * ��ȡ��ɫ��������
	 */
	public static String getPlaMirrorData(int playerid, Mirror mirrorobj){
		try {
			return mirrorobj.query(playerid, mirrorobj.col+"="+playerid).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}
	}
	
	/**
	 * ��ȡ��������
	 */
	public static String getTabMirrorData(String tab){
		StringBuffer sb = new StringBuffer();
		ArrayList<Mirror> tab_mirrorobjList = tab_mirrorobjTab.get(tab);
		for(int i = 0; tab_mirrorobjList != null && i < tab_mirrorobjList.size(); i++){
			Mirror mirrorobj = tab_mirrorobjList.get(i);
			JSONArray totalarr1 = new JSONArray();
			JSONArray[] q_arrs = mirrorobj.q_mirror.values().toArray(new JSONArray[mirrorobj.q_mirror.size()]);
			for(int j = 0; j < q_arrs.length; j++){
				MyTools.combJsonarr(totalarr1, q_arrs[j]);
			}
			JSONArray totalarr2 = new JSONArray();
			JSONArray[] noq_arrs = mirrorobj.noq_mirror.values().toArray(new JSONArray[mirrorobj.noq_mirror.size()]);
			for(int j = 0; j < noq_arrs.length; j++){
				MyTools.combJsonarr(totalarr2, noq_arrs[j]);
			}
			sb.append(mirrorobj.getClass().getName()+"\r\n");
			sb.append("�Ѳ�ѯ\r\n");
			sb.append(DBUtil.getFormatStr(tab, totalarr1));
			sb.append("δ��ѯ\r\n");
			sb.append(DBUtil.getFormatStr(tab, totalarr2));
			sb.append("-----------\r\n");
		}
		return sb.toString();
	}
	
	/**
	 * ֪ͨ����
	 */
	public static void sendInsertMessage(Mirror src_mirror, String tab, JSONArray data){
		ArrayList<Mirror> tab_mirrorobjList = tab_mirrorobjTab.get(tab);
		if(tab_mirrorobjList != null && tab_mirrorobjList.size() > 1){
			for(int i = 0; i < tab_mirrorobjList.size(); i++){
				Mirror mirror = tab_mirrorobjList.get(i);
				if(!mirror.equals(src_mirror)){
					mirror.callbackMirrorInsert(data);
				}
			}		
		}
	}
	
	/**
	 * ֪ͨɾ��
	 */
	public static void sendDeleteMessage(Mirror src_mirror, String tab, JSONArray delarr){
		ArrayList<Mirror> tab_mirrorobjList = tab_mirrorobjTab.get(tab);
		if(tab_mirrorobjList != null && tab_mirrorobjList.size() > 1){
			for(int i = 0; i < tab_mirrorobjList.size(); i++){
				Mirror mirror = tab_mirrorobjList.get(i);
				if(!mirror.equals(src_mirror)){
					mirror.callbackMirrorDelete(delarr);
				}
			}		
		}
	}
	
	/**
	 * ֪ͨ����
	 */
	public static void sendUpdateMessage(Mirror src_mirror, String tab, JSONArray old_updarr, SqlString sqlStr){
		ArrayList<Mirror> tab_mirrorobjList = tab_mirrorobjTab.get(tab);
		if(tab_mirrorobjList != null && tab_mirrorobjList.size() > 1){
			for(int i = 0; i < tab_mirrorobjList.size(); i++){
				Mirror mirror = tab_mirrorobjList.get(i);
				if(!mirror.equals(src_mirror) && sqlStr.containCol(mirror.col)){
					mirror.callbackMirrorUpdate(old_updarr, Tools.str2int(sqlStr.getColValue(mirror.col)));
				}
			}		
		}
	}
	
	/**
	 * �����������
	 */
	public static void clearTabData(String tab, boolean clearCol){
		synchronized (LockStor.getLock(LockStor.PLA_MIRROR, tab)) {
			if(clearCol){
				DBUtil.clearColData(tab);
			}
			ArrayList<Mirror> tab_mirrorobjList = tab_mirrorobjTab.get(tab);
			for(int i = 0; tab_mirrorobjList != null && i < tab_mirrorobjList.size(); i++){
				tab_mirrorobjList.get(i).clear();
			}
		}
	}
}
