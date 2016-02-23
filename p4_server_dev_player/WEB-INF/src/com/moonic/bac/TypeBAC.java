package com.moonic.bac;

import com.moonic.util.BACException;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;

/**
 * ����
 */
public class TypeBAC {
	
	/**
	 * ��ȡ�������ݼ� 
	 */
	public DBPaRs getTypeListRs(String tab, int num) throws Exception {
		DBPaRs typeListRs = DBPool.getInst().pQueryA(tab, "num=" + num);
		if(!typeListRs.exist()) {
			BACException.throwInstance("��"+tab+"��������" + num);
		}
		return typeListRs;
	}
	
	//--------------��̬��--------------
	
	private static TypeBAC instance = new TypeBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static TypeBAC getInstance(){
		return instance;
	}
}
