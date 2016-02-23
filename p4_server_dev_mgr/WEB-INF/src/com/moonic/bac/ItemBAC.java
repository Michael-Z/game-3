package com.moonic.bac;

import com.moonic.util.BACException;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;

/**
 * ��ƷBAC
 * @author John
 */
public class ItemBAC {
	public static final String tab_item_type = "tab_item_type";
	
	public static final byte TYPE_ALL = 0;
	public static final byte TYPE_PROP_CONSUME = 1;
	public static final byte TYPE_MATERIAL = 3;
	public static final byte TYPE_GIFT = 6;
	public static final byte TYPE_EQUIP_ORDINARY = 8;
	
	/**
	 * �����Ʒ�б����ݼ�
	 * @param itemtype ��Ʒ����
	 * @param itemnum ��Ʒ���
	 */
	public DBPaRs getListRs(int itemtype, int itemnum) throws Exception{
		try {
			DBPaRs rs = DBPool.getInst().pQueryA(getTab(itemtype), "num="+itemnum);
			if(!rs.exist()){
				BACException.throwInstance("�����ڵ���Ʒ��� itemtype="+itemtype+" itemnum="+itemnum);
			}
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * ��ȡ����
	 */
	public String getTab(int itemtype) {
		try {
			DBPaRs typeRs = DBPool.getInst().pQueryA(tab_item_type, "itemtype="+itemtype);
			if(!typeRs.exist()){
				BACException.throwInstance("��Ʒ���Ͳ����� itemtype="+itemtype);
			}
			return typeRs.getString("tabname");		
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//--------------��̬��--------------
	
	private static ItemBAC instance = new ItemBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static ItemBAC getInstance(){
		return instance;
	}
}
