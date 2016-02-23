package com.moonic.battle;

import server.common.Tools;

import com.moonic.util.DBPool;

public class BuffLib 
{
	public static BuffFixData[] buffArr;
	public static short[][] buffConflictArr;
	
	static
	{
		readFixData();		
	}

    public enum DATA_INDEX  //�����ֶ���
    {
        NUM, //���
        NAME, //buff��           
        BUFFTYPE, //����
        BUFFGROUP, //��
        PRIORITY, //���ȼ�
        UPDOWN, //1���� -1����
        GOODBAD, //�������
        CLEANABLE, //�ɾ���        
        TURNS, //�����غ���
        LIMITTIMES, //��Ч��������
        ARGS //��ȼ�����        
    }    

    public static void readFixData()
    {
        if (buffArr != null) return;  //ֻ����һ��

        String battleSkillTxt=null;
		try {
			battleSkillTxt = DBPool.getInst().readTxtFromPool("buff");
		} catch (Exception e) {		
			e.printStackTrace();
			return;
		}
		if(battleSkillTxt==null)
		{
			System.out.println("tab_txt��buff�����ڣ�");
			return;
		}
		
		String[][] arrData = Tools.getStrLineArrEx2(battleSkillTxt, "data:", "dataEnd", "\t");
		

        buffArr = new BuffFixData[arrData.length];
        for (int i = 0; buffArr != null && i < buffArr.length; i++)
        {
            buffArr[i] = new BuffFixData();
            buffArr[i].num = Tools.str2int(arrData[i][(int)DATA_INDEX.NUM.ordinal()]);
            buffArr[i].name = arrData[i][(int)DATA_INDEX.NAME.ordinal()];
            buffArr[i].buffTypes = Tools.splitStrToShortArr2(arrData[i][(int)DATA_INDEX.BUFFTYPE.ordinal()], "|", ",", true);
            buffArr[i].buffGroup = Tools.str2short(arrData[i][(int)DATA_INDEX.BUFFGROUP.ordinal()]);
            buffArr[i].updown = Tools.str2byte(arrData[i][(int)DATA_INDEX.UPDOWN.ordinal()]);  
            buffArr[i].goodbad = Tools.str2byte(arrData[i][(int)DATA_INDEX.GOODBAD.ordinal()]);  
            buffArr[i].cleanable = Tools.str2boolean(arrData[i][(int)DATA_INDEX.CLEANABLE.ordinal()]);
            buffArr[i].turns = Tools.str2byte(arrData[i][(int)DATA_INDEX.TURNS.ordinal()]);
            buffArr[i].limitTimes = Tools.str2byte(arrData[i][(int)DATA_INDEX.LIMITTIMES.ordinal()]);
            buffArr[i].args = Tools.splitStrToIntArr2(arrData[i][(int)DATA_INDEX.ARGS.ordinal()],"|",",",true);           
        }
    }

    public static BuffFixData getBuffFixDataByNum(int num)
    {
        for (int i = 0; buffArr != null && i < buffArr.length; i++)
        {
            if (buffArr[i].num == num)
            {
                return buffArr[i];
            }
        }
        return null;
    }
    public static Buff createBuff(int num,int level)
    {
    	BuffFixData buffData = getBuffFixDataByNum(num);
    	if(buffData!=null)
    	{
    		Buff buff = new Buff();
    		buff.buffFixData = buffData;
    		buff.level = (byte)level;   
    		buff.turns = buffData.turns;
			return buff;
    	}
    	return null;
    }
   
}
