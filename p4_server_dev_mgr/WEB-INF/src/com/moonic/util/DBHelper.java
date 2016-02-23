package com.moonic.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;
import server.config.ServerConfig;
import server.database.DataBase;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.ehc.dbc.MyPreparedStatement;
import com.moonic.bac.ServerBAC;
import com.moonic.mgr.LockStor;

import conf.Conf;

/**
 * ���ݿ��������
 * @author John
 */
public class DBHelper {
	private DataBase db;
	private Connection conn;
	private static DataBase defaultDataBase;
	private ArrayList<Statement> stmtArrlist;
	
	public static int connectionAmount; //�ܴ�������
	public static int totalOpenRsAmount; //�ܴ�rs��
	
	private int openRsAmount; //��ǰ��rs��
	private Hashtable<ResultSet, String> rsHashTable = new Hashtable<ResultSet, String>();
	
	public static MyLog log;
	
	{
		log = new MyLog(MyLog.NAME_DATE, "log_db", "DB", false, false, false, null);
	}
	
	/**
	 * ��ȡ����������Ϣ
	 */
	public static String getConnAmInfo(){
		StringBuffer sb = new StringBuffer();
		sb.append("��������" + connectionAmount + "\r\n");
		sb.append("RS����" + totalOpenRsAmount + "\r\n");
		sb.append("��ǰ���ӳص���������������" + (ServerConfig.getDataBase().getNumActive()+ServerConfig.getDataBase().getNumIdle()) + "\r\n");
		sb.append("��ǰ���ӳػ����������" + ServerConfig.getDataBase().getNumActive() + "\r\n");
		sb.append("��ǰ���ӳش�������������" + ServerConfig.getDataBase().getNumIdle() + "\r\n\r\n");
		sb.append("��δ�رյ�RS�б�\r\n");
		for(int i = 0; i < dbhVec.size(); i++){
			sb.append(dbhVec.get(i).getHashtable());
		}
		return sb.toString();
	}
	
	public static Vector<DBHelper> dbhVec = new Vector<DBHelper>();
	
	/**
	 * ����
	 */
	public DBHelper() {
		if (defaultDataBase == null) {
			System.out.println("δ����Ĭ�����ݿ�");
		} else {
			db = defaultDataBase;
		}
	}
	
	/**
	 * ����
	 */
	public DBHelper(DataBase database) {
		db = database;
	}
	
	/**
	 * ����Ĭ�����ӵ����ݿ�
	 */
	public static void setDefaultDataBase(DataBase database) {
		defaultDataBase = database;
	}
	
	/**
	 * ��ȡ���ӵ����ݿ�
	 */
	public DataBase getDataBase() {
		return db;
	}
	
	/**
	 * ������
	 */
	public Connection openConnection() throws Exception {
		return openConnection(true);
	}	
	
	/**
	 * ������
	 */
	public Connection openConnection(boolean allowAutoCommit) throws Exception {
		if(conn != null) {//�������ֱ�ӷ���
			return conn;
		}
		try {
			conn = db.getConnection();
			conn.setAutoCommit(allowAutoCommit);
		} catch (Exception e) {
			e.printStackTrace();
			BACException.throwInstance("��ȡ���ݿ�����ʧ��");
		}
		if(rsHashTable == null) {
			rsHashTable = new Hashtable<ResultSet, String>();
		}
		connectionAmount++;
		log.d("������ ��������" + connectionAmount, Conf.out_sql);
		if(connectionAmount > 100) {
			log.e("������������100����ǰ������=" + connectionAmount);
		}
		if(!dbhVec.contains(this)) {
			dbhVec.add(this);
		}
		return conn;
	}
	
	/**
	 * �ύ
	 */
	public void commit() {
		try {
			if (!conn.getAutoCommit()) {
				conn.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * �ع�
	 */
	public void rollback() {
		try {
			if (conn!=null && !conn.getAutoCommit()) {
				conn.rollback();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * �ر�����
	 */
	public void closeConnection() {
		if(conn != null) {
			try {
				conn.setAutoCommit(true);			
			} catch (SQLException e) {
				e.printStackTrace();
			}
			closeAllStatement();
			db.closeConnection(conn);
			conn = null;
			connectionAmount--;
			totalOpenRsAmount-=openRsAmount;
			openRsAmount=0;
			log.d("--- �ܴ�rs��="+totalOpenRsAmount);
			log.d("�ر����� ��������" + connectionAmount, Conf.out_sql);
		}
		if(dbhVec.contains(this)){
			dbhVec.remove(this);
		}
	}
	
	/**
	 * LOGDB����
	 */
	public static void logInsert(String table, SqlString sqlStr){
		synchronized (LockStor.getLock(LockStor.LOG_SAVE)) {
			SaveLogTask slt = new SaveLogTask(table, sqlStr);
			pqarr[pquseInd].addTask(slt);
			pquseInd++;
			if(pquseInd >= pqarr.length){
				pquseInd = 0;
			}
		}
	}
	private static byte pquseInd;
	private static byte timeoutAm;
	private static ProcessQueue[] pqarr = new ProcessQueue[10];
	
	static {
		for(int i = 0; i < pqarr.length; i++){
			pqarr[i] = new ProcessQueue();
		}
	}
	
	/**
	 * ���ò�����־ʧ�ܴ���
	 */
	public static ReturnValue resetInsertLogTimeoutAm(){
		timeoutAm = 0;
		return new ReturnValue(true, "���óɹ�");
	}
	
	/**
	 * ��ȡ���̶߳�������״̬
	 */
	public static ReturnValue getSaveLogPQState(){
		StringBuffer sb = new StringBuffer();
		sb.append("������־ʧ�ܴ�����"+timeoutAm+"\r\n");
		for(int i = 0; i < pqarr.length; i++){
			sb.append("���� "+(i+1)+" ��������������" + pqarr[i].getQueueSize()+"\r\n");
		}
		return new ReturnValue(true , sb.toString());
	}
	
	/**
	 * ����־����
	 * @author John
	 */
	static class SaveLogTask implements ProcessQueueTask {
		public String table;
		public SqlString sqlStr;
		public SaveLogTask(String table, SqlString sqlStr){this.table=table;this.sqlStr=sqlStr;}
		public void execute() {
			if(timeoutAm <= 50){
				DBHelper dbHelper = new DBHelper(ServerConfig.getDataBase_Log());
				try {
					dbHelper.openConnection();
					dbHelper.insert(table, sqlStr);
					timeoutAm = 0;
				} catch (Exception e) {
					timeoutAm++;
					System.out.println("�洢��־�쳣("+timeoutAm+"):"+e.getMessage()+"�� ["+table+"]["+sqlStr.colString()+"]["+sqlStr.valueString()+"]");
				} finally {
					dbHelper.closeConnection();
				}		
			} else {
				System.out.println("�洢��־�쳣"+timeoutAm+"����ֹͣ��¼��־ ["+table+"]["+sqlStr.colString()+"]["+sqlStr.valueString()+"]");
				synchronized (LockStor.getLock(LockStor.LOG_EXC_RECOVER)) {
					if(recoverSaveLogTask == null){
						recoverSaveLogTask = new RecoverSaveLogTT();
						ServerBAC.timer.schedule(recoverSaveLogTask, MyTools.long_minu*5, TimeUnit.MILLISECONDS);
					}		
				}
			}
		}
	}
	
	private static RecoverSaveLogTT recoverSaveLogTask = null;
	
	/**
	 * �ָ���¼��־��ʱ��
	 */
	static class RecoverSaveLogTT extends MyTimerTask {
		public void run2() {
			timeoutAm = 0;
			recoverSaveLogTask = null;
			System.out.println("�ָ���¼��־");
		}
	}
	
	/**
	 * ��ȡ��һ����ID
	 */
	public int getNextId(String table) throws Exception {
		ResultSet rs = executeQuery("select "+SqlString.getSeqNextStr(table)+" from dual");
		rs.next();
		int id = rs.getInt("nextval");
		closeRs(rs);
		return id;
	}
	
	/**
	 * ��ȡָ��RS������
	 */
	public int getRsDataCount(ResultSet rs) throws Exception {
		int row = rs.getRow();
		rs.last();
		int count = rs.getRow();
		if(row == 0){
			rs.beforeFirst();
		} else {
			rs.absolute(row);	
		}
		return count;
	}
	
	/**
	 * ��ȡ�Ĳ���� STMT
	 */
	public MyPreparedStatement getInsertStmt(String table, SqlString sqlStr) throws Exception {
		if(conn==null) {
			openConnection();
		}
		MyPreparedStatement stmt = sqlStr.getInsertPreparedStatementAutoID(conn, table);//ʹ���Զ�ID��������ʹ��ָ��ID�ϲ�
		return stmt;
	}
	
	/**
	 * ��ȡ�Ĳ���� STMT
	 */
	public MyPreparedStatement getInsertStmt(String table, SqlString sqlStr, int id) throws Exception{
		sqlStr.add("id", id);
		if(conn==null) {
			openConnection();
		}
		MyPreparedStatement stmt = sqlStr.getInsertPreparedStatement(conn, table);//ʹ��ָ��ID
		return stmt;
	}
	
	/**
	 * ����
	 */
	public void insert(String table, SqlString sqlStr) throws Exception {
		if(conn==null) {
			openConnection();
		}
		MyPreparedStatement stmt = sqlStr.getInsertPreparedStatementAutoID(conn, table);//ʹ���Զ�ID��������ʹ��ָ��ID�ϲ�
		execute(stmt);
	}
	
	/**
	 * ����
	 */
	public int insertAndGetId(String table, SqlString sqlStr) throws Exception {
		int id = getNextId(table);
		insert(table, sqlStr, id);
		return id;
	}
	
	/**
	 * ����
	 */
	public void insert(String table, SqlString sqlStr, int id) throws Exception {
		sqlStr.add("id", id);
		if(conn==null) {
			openConnection();
		}
		MyPreparedStatement stmt = sqlStr.getInsertPreparedStatement(conn, table);//ʹ��ָ��ID
		execute(stmt);
	}
	
	/**
	 * ɾ��
	 */
	public void delete(String table, String where) throws Exception{
		if(where == null || where.equals("")){
			BACException.throwAndPrintInstance("ɾ������ִ��ʧ�ܣ�����Ϊ�գ���" + table);
		}
		String where1 = where;
		SqlString wStr = new SqlString();
		where = DBUtil.convertWhere(where, wStr);
		if(conn==null) {
			openConnection();
		}		
		MyPreparedStatement stmt = wStr.getDeletePreparedStatement(conn, table, "where "+where);
		stmt.where1 = where1;
		stmt.where2 = where;
		stmt.wStr = wStr;
		execute(stmt);
	}
	
	/**
	 * ��ȡ�޸ĵ� STMT
	 */
	public MyPreparedStatement getUpdateStmt(String table, SqlString sqlStr, String where) throws Exception{
		if(where == null || where.equals("")){
			BACException.throwAndPrintInstance("��ȡʧ�ܣ�����Ϊ�գ���" + table);
		}
		String where1 = where;
		SqlString wStr = new SqlString();
		where = DBUtil.convertWhere(where, wStr);
		if(conn==null) {			
			openConnection();			
		}
		MyPreparedStatement stmt = sqlStr.getUpdatePreparedStatement(conn, table, wStr, "where "+where);
		stmt.where1 = where1;
		stmt.where2 = where;
		stmt.wStr = wStr;
		return stmt;
	}
	
	/**
	 * �޸�
	 * @param where ��null�޸����У���""�޸�ʧ��
	 */
	public void update(String table, SqlString sqlStr, String where) throws Exception{
		if(where != null && where.equals("")){
			BACException.throwAndPrintInstance("�޸�����ִ��ʧ�ܣ�����Ϊ�գ���" + table);
		}
		if(sqlStr.getColCount() == 0){
			BACException.throwAndPrintInstance("�޸�����ִ��ʧ�ܣ��ֶ�Ϊ�գ���" + table);
		}
		String where1 = where;
		SqlString wStr = new SqlString();
		where = DBUtil.convertWhere(where, wStr);
		if(where != null && !where.equals("")){
			where = "where "+where;
		} else {
			where = "";
		}
		if(conn==null) {			
			openConnection();			
		}
		MyPreparedStatement stmt = sqlStr.getUpdatePreparedStatement(conn, table, wStr, where);
		stmt.where1 = where1;
		stmt.where2 = where;
		stmt.wStr = wStr;
		execute(stmt);
	}
	
	/**
	 * ��ѯ�����JSONARR����
	 */
	public JSONArray queryJsonArray(String sql) {
		JSONArray jsonarr = null;
		try {
			openConnection();
			ResultSet rs = executeQuery(sql);
			jsonarr = DBUtil.convertRsToJsonarr(rs);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeConnection();
		}
		return jsonarr;
	}
	
	/**
	 * ��ѯ�����JSONOBJ����
	 */
	public JSONObject queryJsonObj(String sql) {
		JSONObject jsonobj = null;
		try {
			openConnection();
			ResultSet rs = executeQuery(sql);
			jsonobj = DBUtil.convertRsToJsonobj(rs);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeConnection();
		}
		return jsonobj;
	}
	
	/**
	 * ��ѯ���������Ķ����¼
	 */
	public JSONArray queryJsonArray(String table, String target, String where, String order) throws Exception {
		return queryJsonArray(table, target, where, order, null, 0, 0);
	}
	
	/**
	 * ��ѯ�����JSONOBJ��ʽ����
	 */
	public JSONObject queryJsonObj(String table, String target, String where) throws Exception {
		return queryJsonObj(table, target, where, null, null, 0, 0);
	}
	
	
	/**
	 * ��ѯ���������Ķ����¼
	 */
	public JSONArray queryJsonArray(String table, String target, String where, String order, String group, int minRow, int maxRow) throws Exception {
		JSONArray jsonarr = null;
		try {
			openConnection();
			ResultSet rs = query(table, target, where, order, group, minRow, maxRow);
			jsonarr = DBUtil.convertRsToJsonarr(rs);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeConnection();
		}
		return jsonarr;
	}
	
	/**
	 * ��ѯ������¼
	 */
	public JSONObject queryJsonObj(String table, String target, String where, String order, String group, int minrows, int maxrows) throws Exception {
		JSONObject jsonobj = null;
		try {
			openConnection();
			ResultSet rs = query(table, target, where, order, group, minrows, maxrows);
			jsonobj = DBUtil.convertRsToJsonobj(rs);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeConnection();
		}
		return jsonobj;
	}
	
	/**
	 * ��ȡ��ֵ
	 */
	public int getIntValue(String table, String target, String where) throws Exception {
		return Tools.str2int(getStrValue(table, target, where));
	}
	
	/**
	 * ��ȡ�ַ���ֵ
	 */
	public String getStrValue(String table, String target, String where) throws Exception {
		ResultSet rs = query(table, target, where);
		if (!rs.next()) {
			BACException.throwInstance("��¼������");
		}
		String val = rs.getString(target);
		closeRs(rs);
		return val;
	}
	
	/**
	 * �����ѯ
	 * @param tables ��������null
	 * @param targets ��������null
	 */
	public ResultSet query(String[] tables, String[] targets, String where) throws Exception{
		StringBuffer tableStr = new StringBuffer("");
		StringBuffer targetStr = new StringBuffer("");
		for(int i = 0; i < tables.length; i++){
			if(tableStr.length() > 0){
				tableStr.append(",");
			}
			tableStr.append(tables[i]);
			String[] columns = Tools.splitStr(targets[i], ",");
			for(int k = 0; k < columns.length; k++){
				if(targetStr.length() > 0){
					targetStr.append(",");
				}
				targetStr.append(tables[i]);
				targetStr.append(".");
				targetStr.append(columns[k]);
			}
		}
		return query(tableStr.toString(), targetStr.toString(), where);
	}
	
	/**
	 * ������ѯ
	 * @param tables ����    ��Ҫָ������
	 */
	public ResultSet queryInnerJoin(String[] tables, String  targets, String where,String onWhere) throws Exception{
		StringBuffer tableStr = new StringBuffer();
		for(int i = 0; i < tables.length; i++){
			if(i> 0&&i<tableStr.length()-1){
				tableStr.append(" inner join ");	
			}
			tableStr.append(tables[i]);
		}
		tableStr.append(" on "+onWhere+" ");
		return query(tableStr.toString(), targets, where);
	}
	
	/**
	 * �������ѯ
	 * @param tables ��������null
	 * @param targets ��������null
	 */
	public ResultSet queryLeftJoin(String[] tables, String[] targets, String where,String onWhere) throws Exception{
		StringBuffer tableStr = new StringBuffer("");
		StringBuffer targetStr = new StringBuffer("");
		for(int i = 0; i < tables.length; i++){
			if(tableStr.length() > 0){
				tableStr.append(" left join ");
			}
			tableStr.append(tables[i]);
			String[] columns = Tools.splitStr(targets[i], ",");
			for(int k = 0; k < columns.length; k++){
				if(targetStr.length() > 0){
					targetStr.append(",");
				}
				targetStr.append(tables[i]);
				targetStr.append(".");
				targetStr.append(columns[k]);
			}
		}
		tableStr.append(" on "+onWhere+" ");
		return query(tableStr.toString(), targetStr.toString(), where);
	}
	
	/**
	 * ��ѯ�Ƿ���ڷ��������ļ�¼
	 */
	public boolean queryExist(String table, String where) throws Exception{
		boolean exist = false;
		ResultSet rs = query(table, "id", where);
		exist = rs.next();
		closeRs(rs);
		return exist;
	}
	
	/**
	 * ��ѯ���������ļ�¼��
	 */
	public int queryCount(String table, String where) throws Exception {
		int amount = 0;
		ResultSet rs = query(table, "count(1)", where);
		if (rs.next()) {
			amount = rs.getInt(1);
		}
		closeRs(rs);
		return amount;
	}
	
	/**
	 * ��ѯ
	 */
	public ResultSet query(String table, String target, String where) throws Exception{
		return query(table, target, where, null, null, 0, 0);
	}
	
	/**
	 * ��ѯ������
	 */
	public ResultSet query(String table, String target, String where, String order) throws Exception {
		return query(table, target, where, order, null, 0, 0);
	}
	
	/**
	 * ��ѯ���������
	 */
	public ResultSet query(String table, String target, String where, String order, String group) throws Exception {
		return query(table, target, where, order, group, 0, 0);
	}
	
	/**
	 * ��ѯ����ȡָ������
	 */
	public ResultSet query(String table, String target, String where, String order, int rows) throws Exception {
		return query(table, target, where, order, null, 0, rows);
	}
	
	/**
	 * ��ѯ����ȡָ����ֹ����
	 */
	public ResultSet query(String table, String target, String where, String order, int minrows, int maxrows) throws Exception {
		return query(table, target, where, order, null, minrows, maxrows);
	}
	
	/**
	 * ��ѯ
	 * ('target' Ϊ�ձ�ʾ'*'��ѯ)
	 * ('where' Ϊ�ձ�ʾ������)
	 * ('order' Ϊ�ձ�ʾ������)
	 * ('group' Ϊ�ձ�ʾ�޷���)
	 */
	public ResultSet query(String table, String target, String where, String order, String group, int minRow, int maxRow) throws Exception{
		String where1 = where;
		SqlString wStr = new SqlString();
		where = DBUtil.convertWhere(where, wStr);
		if(where != null && !where.equals("")){
			where = "where "+where;
		} else {
			where = "";
		}
		if(conn==null) {
			openConnection();
		}
		MyPreparedStatement stmt = wStr.getQueryPreparedStatement(conn, table, target, where, order, group, minRow, maxRow);
		stmt.where1 = where1;
		stmt.where2 = where;
		stmt.wStr = wStr;
		return executeQuery(stmt);
	}
	
	/**
	 * ִ�и���
	 */
	public void execute(MyPreparedStatement stmt) throws Exception {
		log.d("ִ��SQL������䣺" + stmt.getSql(), Conf.out_sql);
		try {
			db.preparedExecute(stmt);
		} catch(SQLException ex) {
			System.out.println("ִ��SQL�������ʧ�� �쳣��Ϣ��"+ex.toString()+"\r\n" + stmt.getExceptionMsg());
			ex.printStackTrace();
			BACException.throwInstance("ִ��SQL�������ʧ�� �쳣��Ϣ��"+ex.toString());
		}
	}
	
	/**
	 * ��ȡSTMT
	 */
	public MyPreparedStatement getStmt(String sql) throws Exception {
		if(conn==null) {
			openConnection();			
		}
		PreparedStatement stmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		MyPreparedStatement myStmt = new MyPreparedStatement(conn,stmt,sql);
		return myStmt;
	}
	
	/**
	 * ִ�и���
	 */
	public boolean execute(String sql) throws Exception {
		log.d("ִ��SQL������䣺" + sql, Conf.out_sql);
		if(conn==null) {			
			openConnection();
		}
		return db.executeWithException(conn, sql);
	}
	
	/**
	 * ִ�в�ѯ
	 */
	public ResultSet executeQuery(MyPreparedStatement stmt) throws Exception {
		try {
			log.d("ִ��SQL��ѯ��䣺" + stmt.getSql(), Conf.out_sql);
			ResultSet rs = db.preparedQuery(stmt);
			addStatement(rs.getStatement());
			openRsAmount++;
			totalOpenRsAmount++;
			log.d("��RS �ܴ�rs��=" + totalOpenRsAmount);
			if(rsHashTable!=null) {
				rsHashTable.put(rs, stmt.getSql());
			}
			return rs;
		} catch(SQLException ex) {
			System.out.println("ִ��SQL��ѯ���ʧ�� �쳣��Ϣ��"+ex.toString()+"\r\n" + stmt.getExceptionMsg());
			ex.printStackTrace();
			BACException.throwInstance("ִ��SQL��ѯ���ʧ�� �쳣��Ϣ��"+ex.toString());
		}
		return null;
	}
	
	/**
	 * ִ�в�ѯ
	 */
	public ResultSet executeQuery(String sql) throws Exception {
		log.d("ִ��SQL��ѯ��䣺" + sql, Conf.out_sql);
		if(conn==null) {
			openConnection();
		}
		ResultSet rs = db.executeQueryWithException(conn, sql);
		addStatement(rs.getStatement());
		openRsAmount++;
		totalOpenRsAmount++;
		log.d("��RS �ܴ�rs��=" + totalOpenRsAmount);
		rsHashTable.put(rs, sql);
		return rs;
	}
	
	/**
	 * ��ȡ������ֶ�����
	 */
	public String getTabColumn(String table, String column){
		StringBuffer sb = new StringBuffer();
		sb.append(table);
		sb.append(".");
		sb.append(column);
		return sb.toString();
	}
	
	/**
	 * �ر�����STMT
	 */
	public void closeAllStatement() {
		Enumeration<ResultSet> enu = rsHashTable.keys();
		while (enu.hasMoreElements()) {
			try {
				enu.nextElement().close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		rsHashTable = null;
		for (int i = 0; stmtArrlist != null && i < stmtArrlist.size(); i++) {
			try {
				((Statement) stmtArrlist.get(i)).close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		stmtArrlist = null;
		totalOpenRsAmount -= openRsAmount;
		openRsAmount = 0;
		log.d("�ر�Rs �ܴ�rs��=" + totalOpenRsAmount);
	}
	
	/**
	 * �ر�ָ��RS
	 */
	public void closeRs(ResultSet rs) {
		try {
			if(rs!=null) {
				Statement stmt = rs.getStatement();
				if(stmt != null){
					rs.close();
					stmt.close();
					if (openRsAmount > 0) {
						openRsAmount--;
					}
					if (totalOpenRsAmount > 0) {
						totalOpenRsAmount--;
					}
					log.d("�ر�RS �ܴ�rs��=" + totalOpenRsAmount);
					rsHashTable.remove(rs);
				}
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ӡ��ǰ�򿪵�RS
	 */
	public String getHashtable() {
		StringBuffer sb = new StringBuffer();
		Enumeration<ResultSet> enu = rsHashTable.keys();
		while (enu.hasMoreElements()) {
			sb.append(rsHashTable.get(enu.nextElement())+"\r\n");
		}
		return sb.toString();
	}
	
	/**
	 * ��¼�򿪵�STMT
	 */
	private void addStatement(Statement stmt) {
		if (stmtArrlist == null) {
			stmtArrlist = new ArrayList<Statement>();
		}
		if (!stmtArrlist.contains(stmt)) {
			stmtArrlist.add(stmt);
		}
	}
}
