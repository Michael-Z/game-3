package com.moonic.bac;

import org.json.JSONArray;

import com.ehc.common.ReturnValue;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.worldboss.WorldBoss;

/**
 * ����BOSS BAC
 * @author wkc
 */
public class WorldBossBAC {
	public static WorldBoss worldboss;//����BOSS
	
	/**
	 * ��������BOSS
	 */
	public ReturnValue start(long actiTimeLen, byte isConstraint){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			if(worldboss != null){
				if(isConstraint == 1){
					worldboss.endHandle(dbHelper);
				} else{
					BACException.throwInstance("����BOSS������");
				}
			}
			WorldBoss wb = new WorldBoss(actiTimeLen);
			worldboss = wb;
			wb.start();
			return new ReturnValue(true, "�����ɹ�");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��������BOSS
	 */
	public ReturnValue join(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			if(worldboss == null){
				BACException.throwInstance("����BOSSδ��ʼ���ѽ���");
			}
			JSONArray json = worldboss.join(playerid);
			return new ReturnValue(true, json.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��BOSSս��
	 */
	public ReturnValue toBattle(int playerid, String posStr){
		DBHelper dbHelper = new DBHelper();
		try {
			if(worldboss == null){
				BACException.throwInstance("����BOSSδ��ʼ���ѽ���");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_WORLD_BOSS_BATTLE);
			JSONArray jsonarr = worldboss.toBattle(dbHelper, playerid, posStr, gl);
			gl.save();
			return new ReturnValue(true, jsonarr.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ����
	 */
	public ReturnValue getData(int playerid){
		try {
			if(worldboss == null){
				BACException.throwInstance("����BOSSδ��ʼ���ѽ���");
			}
			JSONArray jsonarr = worldboss.getData(playerid);
			return new ReturnValue(true, jsonarr.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} 
	}
	
	/**
	 * ��ȡ��½����
	 */
	public JSONArray getLoginData() {
		JSONArray jsonarr = new JSONArray();
		if(worldboss != null) {
			jsonarr = worldboss.getLoginData();
		}
		return jsonarr;
	}
	
	//-------------------��̬��---------------------
	
	private static WorldBossBAC instance = new WorldBossBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static WorldBossBAC getInstance(){
		return instance;
	}
}
