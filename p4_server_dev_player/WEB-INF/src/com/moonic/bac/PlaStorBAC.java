package com.moonic.bac;

import com.moonic.mirror.Mirror;
import com.moonic.mirror.MirrorMgr;




/**
 * ��ɫ�������
 * @author John
 */
public abstract class PlaStorBAC extends Mirror {
	
	/**
	 * ����
	 */
	public PlaStorBAC(String tab, String col, String id_col){
		super(tab, col, id_col);
		MirrorMgr.pla_mirrorobjTab.put(tab, this);
	}
}
