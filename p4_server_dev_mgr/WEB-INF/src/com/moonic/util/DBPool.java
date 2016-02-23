package com.moonic.util;

import java.sql.ResultSet;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ehc.common.ReturnValue;
import com.moonic.mgr.LockStor;

/**
 * ���ݿ⻺��
 * @author John
 */
public class DBPool {
	
	/**
	 * �б�������
	 * �ṹ��
	 * ���1��������[TABLE_NAME+MAIN_KEY]	>>	������
	 * ���2��MAIN_VALUE						>>	һ������
	 * ���3��������							>>	����ֵ
	 */
	private JSONObject tabpool = new JSONObject();
	/**
	 * �ı���������
	 * �ṹ��
	 * ������[TXTNAME]						>>	TXT����
	 */
	private JSONObject txtpool = new JSONObject();
	
	public MyLog log = new MyLog(MyLog.NAME_DATE, "log_dbp", "DB_P", true, false, true, null);
	
	/**
	 * �б����嵥
	 */
	public ReturnValue TestA(){
		try {
			JSONArray jsonarr = tabpool.names();
			if(jsonarr == null){
				jsonarr = new JSONArray();
			}
			return new ReturnValue(true, jsonarr.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * �ı������嵥
	 */
	public ReturnValue TestB(){
		try {
			JSONArray jsonarr = txtpool.names();
			if(jsonarr == null){
				jsonarr = new JSONArray();
			}
			return new ReturnValue(true, jsonarr.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ȡ�б�������
	 */
	public ReturnValue Test1(String tab){
		try {
			JSONArray jsonarr = readTableFromPool(tab);
			String str = DBUtil.getFormatStr(tab, jsonarr);
			return new ReturnValue(true, str);
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ����б�������
	 */
	public ReturnValue Test2(String tab){
		try {
			clearTableFromPool(tab);
			return new ReturnValue(true);
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ȡ�ı���������
	 */
	public ReturnValue Test3(String key) {
		try {
			String str = readTxtFromPool(key);
			return new ReturnValue(true, str);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ����ı���������
	 */
	public ReturnValue Test4(String key) {
		clearTxtFromPool(key);
		return new ReturnValue(true, "����ɹ�");
	}
	
	/**
	 * ��ѯ
	 */
	public DBPaRs pQueryA(String table, String where) throws Exception {
		return new DBPaRs(pQueryS(table, where));
	}
	
	/**
	 * ��ѯ
	 */
	public DBPsRs pQueryS(String table) throws Exception {
		return pQueryS(table, null);
	}
	
	/**
	 * ��ѯ
	 */
	public DBPsRs pQueryS(String table, String where) throws Exception {
		return pQueryS(table, where, null);
	}
	
	/**
	 * ��ѯ
	 */
	public DBPsRs pQueryS(String table, String where, String order) throws Exception {
		return pQueryS(table, where, order, 0, 0);
	}
	
	/**
	 * ��ѯ
	 */
	public DBPsRs pQueryS(String table, String where, String order, int rows) throws Exception {
		return pQueryS(table, where, order, 1, rows);
	}
	
	/**
	 * ��ѯ
	 * @param table ��
	 * @param where ���� ���Դ�Сд ���ƣ�����AND,OR���Ӻ�С����Ƕ�ף�������Q_COMP_SPLIT�еıȽϷ�(����COLUMN1=1 AND COLUMN2>1 AND COLUMN3 IS NULL)
	 * @param order ���� ���Դ�Сд ���ƣ���������������(��1��ID ��2��ID DESC ��3��ASC)
	 * @param minRow ��ʼ�к� �кŴ�1��ʼ
	 * @param maxRow ��ֹ�к�
	 */
	public DBPsRs pQueryS(String table, String where, String order, int minRow, int maxRow) throws Exception {
		JSONArray json = DBUtil.jsonQuery(table, readTableFromPool(table), where, order, minRow, maxRow);
		return new DBPsRs(table, where, json);
	}
	
	/**
	 * ���б����ݻ����л�ȡָ���б�����
	 * --------------------------------
	 * 1.key����ΪСд
	 * 2.��������Ϊ�б�
	 */
	public JSONArray readTableFromPool(String table) throws Exception {
		synchronized(LockStor.getLock(LockStor.DB_POOL_TAB)){
			JSONArray jsonarr = tabpool.optJSONArray(table);
			if(jsonarr == null){
				DBHelper dbHelper = new DBHelper();
				try {
					dbHelper.openConnection();
					jsonarr = DBUtil.convertRsToFormat(table, dbHelper.query(table, null, null, "id"));
				} catch (Exception e) {
					throw e;
				} finally {
					dbHelper.closeConnection();
				}
				if(jsonarr.length() > 0){
					tabpool.put(table, jsonarr);
					log.d("�����б��棺"+table);
				} else {
					log.e("�����ݿ��ȡ "+table+" ʧ�ܣ�������");
				}
			}
			return jsonarr;
		}
	}
	
	private ArrayList<DBPoolClearListener> tabclearListeners = new ArrayList<DBPoolClearListener>();
	
	/**
	 * ������������
	 */
	public void addTabClearListener(DBPoolClearListener listener){
		tabclearListeners.add(listener);
	}
	
	/**
	 * �ӻ��������ָ���б���
	 */
	public void clearTableFromPool(String table) {
		synchronized(LockStor.getLock(LockStor.DB_POOL_TAB)){
			DBUtil.clearColData(table);
			JSONArray jsonarr = tabpool.optJSONArray(table);
			if(jsonarr != null){
				tabpool.remove(table);
				log.d("����б��棺"+table, true);
				for(int i = 0; i < tabclearListeners.size(); i++){
					tabclearListeners.get(i).callback(table);
				}
			}
		}
	}
	
	/**
	 * �ӻ����ȡָ�������ļ�����
	 */
	public String readTxtFromPool(String key) throws Exception {
		synchronized(LockStor.getLock(LockStor.DB_POOL_TXT)){
			String fileText = txtpool.optString(key, null);
			if(fileText == null){
				DBHelper dbHelper = new DBHelper();
				try {
					dbHelper.openConnection();
					ResultSet rs = dbHelper.query("tab_txt", "txtvalue", "txtkey='"+key+"'");
					if(!rs.next()){
						BACException.throwAndPrintInstance("ȱ�������ļ���" + key + ".txt");
					}
					fileText = new String(rs.getBytes("txtvalue"), "UTF-8");
					txtpool.put(key, fileText);
					log.d("�����ı����棺"+key);	
				} catch (Exception e) {
					throw e;
				} finally {
					dbHelper.closeConnection();
				}
			}
			return fileText;
		}
	}
	
	private ArrayList<DBPoolClearListener> txtclearListeners = new ArrayList<DBPoolClearListener>();
	
	/**
	 * �����ı��������
	 */
	public void addTxtClearListener(DBPoolClearListener listener){
		txtclearListeners.add(listener);
	}
	
	/**
	 * ����ı������е�ָ�������ļ�����
	 */
	public void clearTxtFromPool(String key){
		synchronized(LockStor.getLock(LockStor.DB_POOL_TXT)){
			String fileText = txtpool.optString(key, null);
			if(fileText != null){
				txtpool.remove(key);
				log.d("����ı����棺"+key, true);
				for(int i = 0; i < txtclearListeners.size(); i++){
					txtclearListeners.get(i).callback(key);
				}
			}		
		}
	}
	
	//--------------��̬��--------------
	
	private static DBPool instance = new DBPool();
	
	/**
	 * ��ȡʵ��
	 */
	public static DBPool getInst(){
		return instance;
	}
}
