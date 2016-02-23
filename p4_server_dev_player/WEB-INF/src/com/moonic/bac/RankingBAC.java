package com.moonic.bac;

import org.json.JSONArray;

import com.ehc.common.ReturnValue;
import com.moonic.util.ConfFile;
import com.moonic.util.DBHelper;

/**
 * ����
 * @author John
 */
public class RankingBAC {
	private static final String FILENAME_BATTLEPOWERRANKING = "battlepower_ranking";
	public static JSONArray battlepowerranking_data;
	
	static {
		try {
			battlepowerranking_data = new JSONArray(ConfFile.getFileValueInStartServer(FILENAME_BATTLEPOWERRANKING, "[]"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ȡս������
	 */
	public ReturnValue getBattlePowerRanking(int playerid){
		try {
			return new ReturnValue(true, battlepowerranking_data.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ˢ�����а�����
	 */
	public ReturnValue refreshRanking(long refreshtime, String data){
		DBHelper dbHelper = new DBHelper();
		try {
			JSONArray datarr = new JSONArray(data);
			//TODO ˢ�±�������
			
			//��������
			try {
				battlepowerranking_data = datarr.optJSONArray(0);
				ConfFile.updateFileValue(FILENAME_BATTLEPOWERRANKING, battlepowerranking_data.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return new ReturnValue(true, "ˢ�����");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	//--------------��̬��--------------
	
	private static RankingBAC instance = new RankingBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static RankingBAC getInstance(){
		return instance;
	}
}
