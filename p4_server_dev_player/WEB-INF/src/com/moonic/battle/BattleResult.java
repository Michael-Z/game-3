package com.moonic.battle;

import java.util.ArrayList;

import server.common.Tools;

public class BattleResult {    
    public int toRoleHpChange; //��ʩ����Ѫ���ı仯���˺�ֵΪ�����ָ�ֵΪ��       
    public int fromRoleHpChange; //ʩ����Ѫ���ı仯���˺�ֵΪ�����ָ�ֵΪ��   
    
    public boolean isCriticalAtk; //�Ƿ񱩻�    
    public boolean isDodge; //Ŀ��ɹ�����
    public boolean isBlock; //Ŀ��ɹ���
    
    public boolean angryValueChanged; //ŭ��ֵ�仯��
    public byte angryValue; //�仯���ŭ��ֵ
    
    //private ArrayList<Buff> addBuffs; //���е�buff
    //public int[] removeBuffNums; //�Ƴ���buff
    
    public int comboCount; //��������    
    
     
    /*public short[] getBuffIds()
    {
    	if(addBuffs!=null && addBuffs.size()>0)
    	{    		
    		short[] ids = null;
    		for(int i=0;i<addBuffs.size();i++)
    		{
    			Buff buff = addBuffs.get(i);
    			ids = Tools.addToShortArr(ids, buff.buffFixData.num);
    		}
    		return ids;
    	}
    	return null;
    }*/
}
