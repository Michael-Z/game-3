package com.moonic.bac;

import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.MyTools;
import com.moonic.util.Out;

/**
 * ��Ȩ
 * @author John
 */
public class TqBAC {
	public static final String tab_prerogative = "tab_prerogative";
	
	/**
	 * �ı���Ȩ
	 */
	public void changeTQ(DBHelper dbHelper, int playerid, int tqnum, int adddays, GameLog gl) throws Exception {
		DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
		changeTQ(dbHelper, plaRs, playerid, tqnum, adddays, gl);
	}
	
	/**
	 * �ı���Ȩ
	 */
	public void changeTQ(DBHelper dbHelper, DBPaRs plaRs, int playerid, int tqnum, int adddays, GameLog gl) throws Exception {
		SqlString sqlStr = new SqlString();
		addChangeTQToSqlStr(plaRs, tqnum, adddays, sqlStr, gl);
		PlayerBAC.getInstance().update(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ���Ӹı���Ȩ���ݵ�SqlString
	 */
	public void addChangeTQToSqlStr(DBPaRs plaRs, int tqnum, int adddays, SqlString sqlStr, GameLog gl) throws Exception {
		if(MyTools.checkSysTimeBeyondSqlDate(plaRs.getTime("tqduetime"))){
			long duetime = System.currentTimeMillis()+MyTools.long_day*adddays;
			sqlStr.add("tqnum", tqnum);
			sqlStr.addDateTime("tqduetime", MyTools.getTimeStr(duetime));
			gl.addChaNote("��Ȩ���", 0, tqnum);
			gl.addChaNote("��Ȩ����ʱ��", 0, duetime);
		} else {
			int old_tqnum = plaRs.getInt("tqnum");
			if(tqnum > old_tqnum){
				sqlStr.add("tqnum", tqnum);
				gl.addChaNote("��Ȩ���", old_tqnum, tqnum-old_tqnum);
			}
			long old_duetime = plaRs.getTime("tqduetime");
			long duetime = old_duetime+MyTools.long_day*adddays;
			sqlStr.addDateTime("tqduetime", MyTools.getTimeStr(duetime));
			gl.addChaNote("��Ȩ����ʱ��", old_duetime, duetime-old_duetime);
		}
	}
	
	/**
	 * �����Ȩ�����Ƿ��ѿ���
	 */
	public boolean checkTQFuncOpen(DBPaRs plaRs, int funcnum) throws Exception {
		return getTQFuncData(plaRs, funcnum) == 1;
	}
	
	/**
	 * ��ȡ��Ȩ��������
	 */
	public int getTQFuncData(DBPaRs plaRs, int funcnum) throws Exception {
		int num = getTQNum(plaRs);
		DBPaRs tqRs = DBPool.getInst().pQueryA(tab_prerogative, "num="+num);
		if(tqRs.exist()){
			return tqRs.getInt("func"+funcnum);
		} else {
			Out.println("�������Ȩ��ţ�"+num);
			return 0;
		}
	}
	
	/**
	 * ��ȡ��Ȩ���
	 */
	public int getTQNum(int playerid) throws Exception {
		DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
		return getTQNum(plaRs);
	}
	
	/**
	 * ��ȡ��Ȩ���
	 */
	public int getTQNum(DBPaRs plaRs) throws Exception {
		int num = plaRs.getInt("tqnum");
		if(MyTools.checkSysTimeBeyondSqlDate(plaRs.getTime("tqduetime"))){
			num = 0;
		}
		return num;
	}
	
	//------------------��̬��--------------------
	
	private static TqBAC instance = new TqBAC();

	public static TqBAC getInstance() {
		return instance;
	}
}
