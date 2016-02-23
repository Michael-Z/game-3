package com.moonic.mirror;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;

import com.ehc.common.SqlString;
import com.moonic.mgr.LockStor;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPsRs;
import com.moonic.util.DBUtil;
import com.moonic.util.MyLog;
import com.moonic.util.Out;

import conf.Conf;

/**
 * �������(һ�Զ��ϵ)
 * @author John
 */
public abstract class Mirror {
	public final String tab;
	public final String col;
	
	public final String key_col;
	
	public boolean serverWhere;//�Ƿ�������ǰ�Զ�����SID���ڱ���SID��SERVERID����
	public boolean haveServerWhere;//�Ƿ�����SERVERIDΪ�ؼ��ֶεľ�����������Զ�����������
	
	public HashMap<Integer, JSONArray> q_mirror;//�Ѳ�ѯ���ݿ���ľ���Ԫ
	public HashMap<Integer, JSONArray> noq_mirror;//δ��ѯ�������ݿ⾵��Ԫ
	
	/**
	 * ����
	 */
	public Mirror(String tab, String col, String key_col){
		if(tab == null){
			throw new RuntimeException("�����������Ϊ��");
		}
		if(col == null){
			throw new RuntimeException("����ؼ��ֶβ���Ϊ��");
		}
		this.tab = tab;
		this.col = col;
		
		this.key_col = key_col;
		
		q_mirror = new HashMap<Integer, JSONArray>(32768);
		noq_mirror = new HashMap<Integer, JSONArray>(32768);
		
		ArrayList<Mirror> tab_mirrorobjList = MirrorMgr.tab_mirrorobjTab.get(tab);
		if(tab_mirrorobjList == null){
			tab_mirrorobjList = new ArrayList<Mirror>();
			MirrorMgr.tab_mirrorobjTab.put(tab, tab_mirrorobjList);
		}
		tab_mirrorobjList.add(this);
		//System.out.println("--------------\r\n"+tab+":"+tab_mirrorobjList+"\r\n-------------------");
		MirrorMgr.classname_mirror.put(getClass().getName(), this);
		Out.println("loading "+getClass().getName());
	}
	
	private static MyLog mirrorLog = new MyLog(MyLog.NAME_DATE, "mirrorlog", "MIRRORLOG", true, false, true, null);
	
	/**
	 * ��ʼ��
	 */
	private void initMirror(int colid) throws Exception {
		DBHelper dbHelper = new DBHelper();
		try {
			if(!haveServerWhere && q_mirror.size()+noq_mirror.size()>4000){
				mirrorLog.d("�������� ��"+tab+" Q_LEN:"+q_mirror.size()+" NOQ_LEN:"+noq_mirror.size());
				MirrorMgr.clearTabData(tab, false);
			}
			dbHelper.openConnection();
			String where = col+"="+colid;
			if(serverWhere){
				where += " and serverid="+Conf.sid;
			}
			ResultSet rs = dbHelper.query(tab, null, where);
			JSONArray dbarr = DBUtil.jsonQuery(tab, DBUtil.convertRsToFormat(tab, rs), null, "id", 0, 0);//�����ݿ��ó���������
			dbHelper.closeRs(rs);
			if(noq_mirror.containsKey(colid)){
				JSONArray noqarr = DBUtil.jsonQuery(tab, noq_mirror.get(colid), null, "id", 0, 0);//�Ѵ��ڵ�����
				int k = 0;
				for(int i = 0; i < noqarr.length(); i++){//1,4,5
					JSONArray noq = noqarr.optJSONArray(i);
					for(; k < dbarr.length(); k++){//1,2,3,4,5,6,7
						JSONArray db = dbarr.optJSONArray(k);
						if(noq.optInt(0)==db.optInt(0)){
							dbarr.put(k, noq);//���Ѵ��ڵĶ����滻���ݿ����
							k++;
							break;
						} else {
							MirrorMgr.sendInsertMessage(this, tab, db);//��������֪ͨ
						}
					}
				}
				for(int i = k; i < dbarr.length(); i++){//6,7
					MirrorMgr.sendInsertMessage(this, tab, dbarr.optJSONArray(i));//��������֪ͨ
				}
				noq_mirror.remove(colid);
			} else {
				for(int k = 0; k < dbarr.length(); k++){
					MirrorMgr.sendInsertMessage(this, tab, dbarr.optJSONArray(k));//��������֪ͨ
				}
			}
			q_mirror.put(colid, dbarr);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ������Ƿ��ѯ��
	 */
	private boolean checkHas(int colid) throws Exception {
		boolean has = q_mirror.containsKey(colid);
		if(!has){
			initMirror(colid);
		}
		return has;
	}
	
	/**
	 * �����������
	 */
	public void clear(){
		q_mirror.clear();
		noq_mirror.clear();
	}
	
	/**
	 * ���룬Ĭ�ϴ���COLID=-1��ʹ�ò����¼���ɵ�ID
	 */
	public int insertByAutoID(DBHelper dbHelper, SqlString sqlStr) throws Exception {
		return insert(dbHelper, -1, sqlStr);
	}
	
	/**
	 * ����
	 * @param colid ��-1ʱ��ʾ�ò������ݺ�õ���ֵ��ΪKEY
	 */
	public int insert(DBHelper dbHelper, int colid, SqlString sqlStr) throws Exception {
		if(colid == 0){
			throw new RuntimeException("��Ҫ����ֵ����Ϊ0");
		}
		int id = dbHelper.insertAndGetId(tab, sqlStr);
		if(colid == -1){
			colid = id;
		}
		mirrorInsert(colid, sqlStr, id);
		return id;
	}
	
	/**
	 * ����
	 */
	public void insert(DBHelper dbHelper, int colid, SqlString sqlStr, int id) throws Exception {
		if(colid == 0){
			throw new RuntimeException("��Ҫ����ֵ����Ϊ0");
		}
		dbHelper.insert(tab, sqlStr, id);
		mirrorInsert(colid, sqlStr, id);
	}
	
	/**
	 * �������
	 */
	private void mirrorInsert(int colid, SqlString sqlStr, int id) throws Exception {
		long t1 = System.currentTimeMillis();
		synchronized (LockStor.getLock(LockStor.PLA_MIRROR, tab)) {
			boolean has = checkHas(colid);
			if(has){//�����������
				JSONObject colobj = DBUtil.colmap.optJSONObject(tab);
				JSONArray data = new JSONArray();
				for(int i = 0; i < colobj.length(); i++){
					data.add(null);
				}
				sqlStr.updateMirror(colobj, data);
				data.put(colobj.optInt("id"), id);
				JSONArray unit_mirror = q_mirror.get(colid);
				unit_mirror.add(data);
				MirrorMgr.sendInsertMessage(this, tab, data);
			}
		}
		long t2 = System.currentTimeMillis();
		if(t2-t1>10){
			mirrorLog.d("�����ʱ��"+(t2-t1)+" ��"+tab+" q_mirror��"+q_mirror.size()+" noq_mirror��"+noq_mirror.size()+"("+getClass().getName()+")");
		}
	}
	
	/**
	 * �ص��������
	 */
	public void callbackMirrorInsert(JSONArray data){
		JSONObject colobj = DBUtil.colmap.optJSONObject(tab);
		int colid = data.optInt(colobj.optInt(col));
		if(colid != 0){
			HashMap<Integer, JSONArray> use_mirror = null;
			if(q_mirror.containsKey(colid)){
				use_mirror = q_mirror;
			} else {
				use_mirror = noq_mirror;
			}
			JSONArray unit_mirror = use_mirror.get(colid);
			if(unit_mirror == null){
				unit_mirror = new JSONArray();
				use_mirror.put(colid, unit_mirror);
			}
			unit_mirror.add(data);
		}
	}
	
	/**
	 * ɾ��
	 */
	public void delete(DBHelper dbHelper, int colid, String where) throws Exception {
		if(colid == 0){
			throw new RuntimeException("��Ҫ����ֵ����Ϊ0");
		}
		if(where.indexOf(col) == -1){
			throw new RuntimeException("ȱ����Ҫ���� "+col);
		}
		long t1 = System.currentTimeMillis();
		synchronized (LockStor.getLock(LockStor.PLA_MIRROR, tab)) {
			checkHas(colid);
			JSONArray unit_mirror = q_mirror.get(colid);
			JSONArray delarr = DBUtil.jsonQuery(tab, unit_mirror, where, null, 0, 0);
			for(int i = 0; delarr != null && i < delarr.length(); i++){
				unit_mirror.remove(delarr.opt(i));
			}
			MirrorMgr.sendDeleteMessage(this, tab, delarr);
		}
		long t2 = System.currentTimeMillis();
		if(t2-t1>10){
			mirrorLog.d("ɾ����ʱ��"+(t2-t1)+" ��"+tab+" q_mirror��"+q_mirror.size()+" noq_mirror��"+noq_mirror.size()+"("+getClass().getName()+")");
		}
		if(serverWhere){
			where += " and serverid="+Conf.sid;
		}
		dbHelper.delete(tab, where);
	}
	
	/**
	 * ����ص�ɾ��
	 */
	public void callbackMirrorDelete(JSONArray delarr){
		JSONObject colobj = DBUtil.colmap.optJSONObject(tab);
		for(int i = 0; delarr != null && i < delarr.length(); i++){
			JSONArray data = delarr.optJSONArray(i);
			int colid = data.optInt(colobj.optInt(col));
			if(colid != 0){
				HashMap<Integer, JSONArray> use_mirror = null;
				if(q_mirror.containsKey(colid)){
					use_mirror = q_mirror;
				} else {
					use_mirror = noq_mirror;
				}
				JSONArray unit_mirror = use_mirror.get(colid);
				if(unit_mirror != null){
					unit_mirror.remove(data);
				}		
			}
		}
	}
	
	/**
	 * ����
	 */
	public void update(DBHelper dbHelper, int colid, SqlString sqlStr, String where) throws Exception {
		if(colid == 0){
			throw new RuntimeException("��Ҫ����ֵ����Ϊ0");
		}
		if(where.indexOf(col) == -1){
			throw new RuntimeException("ȱ����Ҫ���� "+col);
		}
		long t1 = System.currentTimeMillis();
		synchronized (LockStor.getLock(LockStor.PLA_MIRROR, tab)) {
			checkHas(colid);
			JSONArray unit_mirror = q_mirror.get(colid);
			JSONObject colobj = DBUtil.colmap.optJSONObject(tab);
			JSONArray updarr = DBUtil.jsonQuery(tab, unit_mirror, where, null, 0, 0);
			boolean updcol = sqlStr.containCol(col);
			JSONArray new_unit_mirror = null;
			if(updcol){
				int new_colid = Tools.str2int(sqlStr.getColValue(col));
				if(new_colid != 0){
					HashMap<Integer, JSONArray> new_use_mirror = null;
					if(q_mirror.containsKey(new_colid)){
						new_use_mirror = q_mirror;
					} else {
						new_use_mirror = noq_mirror;
					}
					new_unit_mirror = new_use_mirror.get(new_colid);
					if(new_unit_mirror == null){
						new_unit_mirror = new JSONArray();
						new_use_mirror.put(new_colid, new_unit_mirror);
					}		
				}
			}
			MirrorMgr.sendUpdateMessage(this, tab, updarr, sqlStr);
			for(int i = 0; updarr != null && i < updarr.length(); i++){
				JSONArray data = updarr.optJSONArray(i);
				sqlStr.updateMirror(colobj, data);
				if(updcol){
					unit_mirror.remove(data);
					if(new_unit_mirror != null){
						new_unit_mirror.add(data);
					}
				}
			}
		}
		long t2 = System.currentTimeMillis();
		if(t2-t1>10){
			mirrorLog.d("���º�ʱ��"+(t2-t1)+" ��"+tab+" q_mirror��"+q_mirror.size()+" noq_mirror��"+noq_mirror.size()+"("+getClass().getName()+")");
		}
		if(serverWhere){
			where += " and serverid="+Conf.sid;
		}
		dbHelper.update(tab, sqlStr, where);
	}
	
	/**
	 * ����ص�����
	 */
	public void callbackMirrorUpdate(JSONArray updarr, int new_colid){
		JSONObject colobj = DBUtil.colmap.optJSONObject(tab);
		JSONArray new_unit_mirror = null;
		if(new_colid != 0){
			HashMap<Integer, JSONArray> new_use_mirror = null;
			if(q_mirror.containsKey(new_colid)){
				new_use_mirror = q_mirror;
			} else {
				new_use_mirror = noq_mirror;
			}
			new_unit_mirror = new_use_mirror.get(new_colid);
			if(new_unit_mirror == null){
				new_unit_mirror = new JSONArray();
				new_use_mirror.put(new_colid, new_unit_mirror);
			}	
		}
		for(int i = 0; updarr != null && i < updarr.length(); i++){
			JSONArray data = updarr.optJSONArray(i);
			int colid = data.optInt(colobj.optInt(col));
			if(colid != 0){
				HashMap<Integer, JSONArray> use_mirror = null;
				if(q_mirror.containsKey(colid)){
					use_mirror = q_mirror;
				} else {
					use_mirror = noq_mirror;
				}
				JSONArray unit_mirror = use_mirror.get(colid);
				if(unit_mirror != null){
					unit_mirror.remove(data);
				}
			}
			if(new_unit_mirror != null){
				new_unit_mirror.add(data);
			}
		}
	}
	
	/**
	 * ��ѯ
	 */
	public DBPsRs query(int colid, String where) throws Exception {
		return query(colid, where, null, 0, 0);
	}
	
	/**
	 * ��ѯ
	 */
	public DBPsRs query(int colid, String where, String order) throws Exception {
		return query(colid, where, order, 0, 0);
	}
	
	/**
	 * ��ѯ
	 */
	public DBPsRs query(int colid, String where, String order, int minRow, int maxRow) throws Exception {
		if(colid == 0){
			throw new RuntimeException("��Ҫ����ֵ����Ϊ0");
		}
		if(where.indexOf(col) == -1){
			throw new RuntimeException("ȱ����Ҫ���� "+col);
		}
		long t1 = System.currentTimeMillis();
		JSONArray json = null;
		synchronized (LockStor.getLock(LockStor.PLA_MIRROR, tab)) {
			checkHas(colid);
			JSONArray unit_mirror = q_mirror.get(colid);
			json = DBUtil.jsonQuery(tab, unit_mirror, where, order, minRow, maxRow);
		}
		long t2 = System.currentTimeMillis();
		if(t2-t1>10){
			mirrorLog.d("��ѯ��ʱ��"+(t2-t1)+" ��"+tab+" q_mirror��"+q_mirror.size()+" noq_mirror��"+noq_mirror.size()+"("+getClass().getName()+")");
		}
		return new DBPsRs(tab, where, json);
	}
	
	/**
	 * ����KEYɾ��
	 */
	public void deleteByKey(DBHelper dbHelper, int colid, int key) throws Exception {
		if(key_col == null){
			throw new RuntimeException("key_col is null");
		}
		delete(dbHelper, colid, col+"="+colid+" and "+key_col+"="+key);
	}
	
	/**
	 * ����KEY����
	 */
	public void updateByKey(DBHelper dbHelper, int colid, SqlString sqlStr, int key) throws Exception {
		if(key_col == null){
			throw new RuntimeException("key_col is null");
		}
		update(dbHelper, colid, sqlStr, col+"="+colid+" and "+key_col+"="+key);
	}
	
	/**
	 * ����KEY��ȡ���ݼ�
	 */
	public DBPaRs getDataRsByKey(int colid, int key) throws Exception {
		if(key_col == null){
			throw new RuntimeException("key_col is null");
		}
		return new DBPaRs(query(colid, col+"="+colid+" and "+key_col+"="+key));
	}
}
