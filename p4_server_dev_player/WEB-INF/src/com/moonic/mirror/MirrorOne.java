package com.moonic.mirror;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

/**
 * �������(һ��һ��ϵ)
 * @author John
 */
public class MirrorOne extends Mirror {
	public boolean needcheck = true;
	
	/**
	 * ����
	 */
	public MirrorOne(String tab, String col){
		super(tab, col, null);
	}
	
	/**
	 * ���Ըı�ֵ
	 */
	public ReturnValue debugChangeValue(int colid, String column, long value, long min, long max, String logname){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			long srcval = getLongValue(colid, column);
			if(srcval+value>max){
				BACException.throwInstance("���ɳ������ֵ "+max);
			} else 
			if(srcval+value<min){
				BACException.throwInstance("����С����Сֵ "+min);
			}
			SqlString sqlStr = new SqlString();
			sqlStr.addChange(column, value);
			update(dbHelper, colid, sqlStr);
			
			GameLog.getInst(colid, GameServlet.ACT_DEBUG_GAME_LOG)
			.addChaNote(logname, srcval, value)
			.save();
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ������������
	 */
	public ReturnValue debugSetTime(int colid, String column, String timeStr, String logname){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(colid, GameServlet.ACT_DEBUG_GAME_LOG);
			setTime(dbHelper, colid, column, timeStr, gl, logname);
			
			gl.save();
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ֵ
	 */
	public void addValue(DBHelper dbHelper, int colid, String column, long addVal, GameLog gl, String logname) throws Exception{
		if(addVal <= 0){
			String str = "����ֵʧ�� �޸��" + column + " ����ֵ��" + addVal + "("+colid+")";
			try {
				throw new Exception(str);
			} catch (Exception e) {
				e.printStackTrace();
			}
			BACException.throwAndPrintInstance(str);
		}
		DBPaRs rs = getDataRs(colid);
		long srcVal = rs.getLong(column);
		SqlString sqlStr = new SqlString();
		sqlStr.addChange(column, addVal);
		update(dbHelper, colid, sqlStr);
		gl.addChaNote(logname, srcVal, addVal);
	}
	
	/**
	 * ��ֵ
	 */
	public void subValue(DBHelper dbHelper, int colid, String column, long subVal, GameLog gl, String logname) throws Exception{
		if(subVal <= 0){
			String str = "����ֵʧ�� �޸��" + column + " ����ֵ��" + subVal + "("+colid+")";
			try {
				throw new Exception(str);
			} catch (Exception e) {
				e.printStackTrace();
			}
			BACException.throwInstance(str);
		}
		DBPaRs rs = getDataRs(colid);
		long srcVal = rs.getLong(column);
		if(srcVal < subVal){
			BACException.throwAndPrintInstance(column + "����" + "("+colid+":"+subVal+"/"+srcVal+")");
		}
		SqlString sqlStr = new SqlString();
		sqlStr.addChange(column, -subVal);
		update(dbHelper, colid, sqlStr);
		subTrigger(dbHelper, colid, column, srcVal, srcVal-subVal);
		gl.addChaNote(logname, srcVal, -subVal);
	}
	
	/**
	 * ��ֵ������(�ص�����������subValueʱ���ص��˷�������д�˷���ʵ����������)
	 */
	public void subTrigger(DBHelper dbHelper, int colid, String col, long srcVal, long nowVal) throws Exception {}
	
	/**
	 * ����ʱ��
	 */
	public void setTime(DBHelper dbHelper, int colid, String column, String timeStr, GameLog gl, String logname) throws Exception{
		SqlString sqlStr = new SqlString();
		sqlStr.addDateTime(column, MyTools.getTimeStr(MyTools.getTimeLong(timeStr)));
		update(dbHelper, colid, sqlStr);
		gl.addRemark(logname+" ����Ϊ "+timeStr);
	}
	
	/**
	 * ����ֵ
	 */
	public void setValue(DBHelper dbHelper, int colid, String column, long value, GameLog gl, String logname) throws Exception{
		SqlString sqlStr = new SqlString();
		sqlStr.add(column, value);
		update(dbHelper, colid, sqlStr);
		gl.addRemark(logname+" ����Ϊ "+value);
	}
	
	/**
	 * ����ֵ
	 */
	public void setValue(DBHelper dbHelper, int colid, String column, String value, GameLog gl, String logname) throws Exception{
		SqlString sqlStr = new SqlString();
		sqlStr.add(column, value);
		update(dbHelper, colid, sqlStr);
		gl.addRemark(logname+" ����Ϊ "+value);
	}
	
	/**
	 * ��ȡINTֵ
	 */
	public int getIntValue(int colid, String column) throws Exception{
		return Tools.str2int(getStrValue(colid, column));
	}
	
	/**
	 * ��ȡLONGֵ
	 */
	public long getLongValue(int colid, String column) throws Exception{
		return Tools.str2long(getStrValue(colid, column));
	}
	
	/**
	 * ��ȡSTRINGֵ
	 */
	public String getStrValue(int colid, String column) throws Exception{
		DBPaRs rs = getDataRs(colid);
		String value = rs.getString(column);
		return value;
	}
	
	/**
	 * ����
	 */
	public void update(DBHelper dbHelper, int colid, SqlString sqlStr) throws Exception {
		update(dbHelper, colid, sqlStr, col+"="+colid);
	}
	
	/**
	 * ��ȡ���ݼ�
	 */
	public DBPaRs getDataRs(int colid) throws Exception {
		DBPsRs rs = query(colid, col+"="+colid);
		if(needcheck && rs.count()<=0){
			BACException.throwAndPrintInstance("��ȡ���ݼ��쳣" + " TAB:" + tab + " WHERE:" + col + "=" + colid);
		}
		return new DBPaRs(rs);
	}
}
