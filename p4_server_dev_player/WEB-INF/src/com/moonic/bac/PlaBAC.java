package com.moonic.bac;

import com.moonic.mirror.MirrorMgr;
import com.moonic.mirror.MirrorOne;
import com.moonic.util.DBHelper;

/**
 * ��ɫ���ݳ�����
 * @author John
 */
public abstract class PlaBAC extends MirrorOne {
	
	/**
	 * ����
	 */
	public PlaBAC(String tab, String col){
		super(tab, col);
		MirrorMgr.pla_mirrorobjTab.put(tab, this);
	}
	
	//------------------������--------------------
	
	public abstract void init(DBHelper dbHelper, int playerid, Object... parm) throws Exception;
}
