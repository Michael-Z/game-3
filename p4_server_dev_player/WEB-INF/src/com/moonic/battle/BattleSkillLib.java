package com.moonic.battle;


import java.util.ArrayList;
import com.moonic.util.DBPool;
import com.moonic.util.DBPoolClearListener;

import server.common.Tools;


public class BattleSkillLib 
{
	static
	{
		readAutoSkillData();
		DBPool.getInst().addTxtClearListener(new DBPoolClearListener() {
			@Override
			public void callback(String key) {
				if(key.equals("skill_auto")){
					readAutoSkillData();
				}
			}
		});
		readAngrySkillData();
		DBPool.getInst().addTxtClearListener(new DBPoolClearListener() {
			@Override
			public void callback(String key) {
				if(key.equals("skill_anger")){
					readAngrySkillData();
				}
			}
		});
		readBeSkillData();
		DBPool.getInst().addTxtClearListener(new DBPoolClearListener() {			
			public void callback(String key) {
				if(key.equals("skill_be")){
					readBeSkillData();
				}
			}
		});
	}
	public static ArrayList<BattleSkillFixData> autoSkillFixDataVC;
	public static ArrayList<BattleSkillFixData> angrySkillFixDataVC;
	public static ArrayList<BattleSkillFixData> beSkillFixDataVC;
	
	
	//����ֶ� ��1���	2������	3ͼ�����	4���ȼ�	5������BUFF���	6��������	7ʩ��ʱ��	8��������	9�Եж���	10��Χ	11��Χ����	12��󹥻�����	13ս��	14�������	15��������	16BUFF���	17BUFF����	18��������	19������������	20������	21������ЧCFG���	22�ӳ�	23�Ƿ�������	24Ʈ�֡���������Чʱ���룩	25Ŀ��������Ч���	26��������ƫ��	27������Ч	28������ʽ	29������
	
	public enum AUTO_DATA_INDEX //������������ֶ���
    {
        NUM, //1���
        NAME, //2������
        ICON_PATH, //3ͼ�����
        MAXLV, //4���ȼ�
        SELFBUFF, //5������BUFF���
        RATE,  //6��������
        USE_TURN, //7ʩ��ʱ��
        TYPE, //8�������� 1:�˺�,2:�ָ�,3:BUFF
        TARGET_TYPE, //9�Եж���1:���� 2:�Ե�
        RANGE,  //10������Χ
        RANGE_ARGS, //11��Χ����
        MAXUSETIMES, //12���ʹ�ò���
        BATTLE_POWER, //13ս��
        HARM_ARGS, //14�������
        CRITICAL_ADD, //15��������
        BUFF, //16BUFF���
        BUFF_RATE, //17BUFF����
        OTHER_ARGS, //18��������
        OTHER_RATE, //19������������
        HIT_RATE, //20������
    };
    
  //ŭ�ֶΣ�  1���	2������	3ͼ�����	4���ȼ�	5ŭ������	6������BUFF���	7��������	8�Եж���	9��Χ	10��Χ����	11��󹥻�����	12ս��	13�������	14��������	15BUFF���	16BUFF����	17��������	18������������	19��ɢ����	20������	21������ЧCFG���	22�ӳ�	23�Ƿ�������	24Ʈ�֡���������Чʱ���룩	25Ŀ��������Ч���	26��������ƫ��	27������Ч	28������ʽ
	
    public enum ANGRY_DATA_INDEX //ŭ���������ֶ���
    {
        NUM, //1���
        NAME, //2������
        ICON_PATH, //3ͼ�����
        MAXLV, //4���ȼ�
        POWER_LEFT, //5ŭ������
        SELFBUFF, //6������BUFF���                
        TYPE, //7�������� 1:�˺�,2:�ָ�,3:BUFF
        TARGET_TYPE, //8�Եж���1:���� 2:�Ե�
        RANGE,  //9������Χ
        RANGE_ARGS, //10��Χ����
        MAXUSETIMES, //11���ʹ�ò���
        BATTLE_POWER, //12ս��
        HARM_ARGS, //13�������
        CRITICAL_ADD, //14��������
        BUFF, //15BUFF���
        BUFF_RATE, //16BUFF����
        OTHER_ARGS, //17��������
        OTHER_RATE, //18����������
        CLEAR_AMOUNT, //19��ɢ����
        HIT_RATE, //20������
    };
    
    public enum BESKILL_DATA_INDEX //�������������ֶ���
    {
        NUM, //1���
        NAME, //2������
        ICON,
        ARGS, // Ч������       
        POWER, //ս��
        OWNER  //�ж�����
    }
    
    public static void readBeSkillData()
    {		
		beSkillFixDataVC = new ArrayList<BattleSkillFixData>();

        //byte[] fileBytes = Tools.getBytesFromFile(ServerConfig.getAppRootPath()+"WEB-INF/res/battleskill.txt");      
        String battleSkillTxt=null;
		try {
			battleSkillTxt = DBPool.getInst().readTxtFromPool("skill_be");
		} catch (Exception e) {		
			e.printStackTrace();
			return;
		}
		if(battleSkillTxt==null)
		{
			System.out.println("tab_txt��skill_be�����ڣ�");	
		}
		
        String[][] battleSkillData = Tools.getStrLineArrEx2(battleSkillTxt, "data:","dataEnd");  //������ܶ�ά����       
     
        for (int i = 0; battleSkillData != null && i < battleSkillData.length; i++)
        {
        	BattleSkillFixData data = new BattleSkillFixData();
        	data.skillCategory = Const.SKILLCATEGORY_BE;
        	data.num = Tools.str2int(battleSkillData[i][AUTO_DATA_INDEX.NUM.ordinal()]);
        	data.name = battleSkillData[i][AUTO_DATA_INDEX.NAME.ordinal()];
        	data.args = Tools.splitStrToShortArr2(battleSkillData[i][(int)BESKILL_DATA_INDEX.ARGS.ordinal()],"|",",");
        	data.owner = Tools.str2byte(battleSkillData[i][(int)BESKILL_DATA_INDEX.OWNER.ordinal()]);        	
        	beSkillFixDataVC.add(data);
        }
    }
	
	public static void readAutoSkillData()
    {		
		autoSkillFixDataVC = new ArrayList<BattleSkillFixData>();

        //byte[] fileBytes = Tools.getBytesFromFile(ServerConfig.getAppRootPath()+"WEB-INF/res/battleskill.txt");      
        String battleSkillTxt=null;
		try {
			battleSkillTxt = DBPool.getInst().readTxtFromPool("skill_auto");
		} catch (Exception e) {		
			e.printStackTrace();
			return;
		}
		if(battleSkillTxt==null)
		{
			System.out.println("tab_txt��skill_auto�����ڣ�");	
		}
		//System.out.println("battleSkillTxt="+battleSkillTxt);
		
        String[][] battleSkillData = Tools.getStrLineArrEx2(battleSkillTxt, "data:","dataEnd");  //������ܶ�ά����
        //System.out.println("battleSkillData ="+Tools.strArr2Str2(battleSkillData));
        /*NUM, //1���
        NAME, //2������
        ICON_PATH, //3ͼ�����
        MAXLV, //4���ȼ�
        SELFBUFF, //5������BUFF���
        RATE,  //6��������
        RELEASE, //7ʩ��ʱ��
        TYPE, //8�������� 1:�˺�,2:�ָ�,3:BUFF
        TARGET_TYPE, //9�Եж���1:���� 2:�Ե�
        RANGE,  //10������Χ
        RANGE_ARGS, //11��Χ����
        MAXUSETIMES, //12���ʹ�ô���
        BATTLE_POWER, //13ս��
        CALC_ARGS, //14�������
        CRITICAL_ADD, //15��������
        BUFF, //16BUFF���
        BUFF_RATE, //17BUFF����
        OTHER_ARGS, //18��������
        OTHER_RATE, //19������������
        HIT_RATE, //20������
*/        
       
        //battleSkillFixDataArr = new BattleSkillFixData[battleSkillData.length];
        for (int i = 0; battleSkillData != null && i < battleSkillData.length; i++)
        {
        	BattleSkillFixData data = new BattleSkillFixData();
        	data.skillCategory = Const.SKILLCATEGORY_AUTO;
        	data.num = Tools.str2int(battleSkillData[i][AUTO_DATA_INDEX.NUM.ordinal()]);
        	data.name = battleSkillData[i][AUTO_DATA_INDEX.NAME.ordinal()];
        	data.maxLv = Tools.str2byte(battleSkillData[i][(int)AUTO_DATA_INDEX.MAXLV.ordinal()]);
        	data.selfBuff = Tools.str2short(battleSkillData[i][(int)AUTO_DATA_INDEX.SELFBUFF.ordinal()]);
        	data.rate = Tools.str2byte(battleSkillData[i][(int)AUTO_DATA_INDEX.RATE.ordinal()]);
        	data.useTurn = Tools.splitStrToShortArr(battleSkillData[i][(int)AUTO_DATA_INDEX.USE_TURN.ordinal()],",",true);
        	data.type = Tools.str2byte(battleSkillData[i][(int)AUTO_DATA_INDEX.TYPE.ordinal()]);
        	data.targetType = Tools.str2byte(battleSkillData[i][(int)AUTO_DATA_INDEX.TARGET_TYPE.ordinal()]); 
        	data.range = Tools.str2byte(battleSkillData[i][(int)AUTO_DATA_INDEX.RANGE.ordinal()]);        
        	data.maxUseTimes = Tools.str2byte(battleSkillData[i][(int)AUTO_DATA_INDEX.MAXUSETIMES.ordinal()]);   
        	data.harmArgs = Tools.splitStrToShortArr(battleSkillData[i][(int)AUTO_DATA_INDEX.HARM_ARGS.ordinal()], ",",true);
        	data.criticalAdd = Tools.str2short(battleSkillData[i][(int)AUTO_DATA_INDEX.CRITICAL_ADD.ordinal()]);
        	data.buffs = Tools.splitStrToShortArr(battleSkillData[i][(int)AUTO_DATA_INDEX.BUFF.ordinal()],",",true);
        	data.buffsRate = Tools.splitStrToShortArr2(battleSkillData[i][(int)AUTO_DATA_INDEX.BUFF_RATE.ordinal()],"|",",");
        	data.otherArgs = Tools.splitStrToShortArr(battleSkillData[i][(int)AUTO_DATA_INDEX.OTHER_ARGS.ordinal()],",",true);
        	data.otherRate = Tools.str2byte(battleSkillData[i][(int)AUTO_DATA_INDEX.OTHER_RATE.ordinal()]);
        	data.hitRate = Tools.str2byte(battleSkillData[i][(int)AUTO_DATA_INDEX.HIT_RATE.ordinal()]);
        	data.rangeArgs = Tools.splitStrToShortArr(battleSkillData[i][(int)AUTO_DATA_INDEX.RANGE_ARGS.ordinal()],",",true);
        	autoSkillFixDataVC.add(data);
        }
    }
	
	public static void readAngrySkillData()
    {		
		angrySkillFixDataVC = new ArrayList<BattleSkillFixData>();

        //byte[] fileBytes = Tools.getBytesFromFile(ServerConfig.getAppRootPath()+"WEB-INF/res/battleskill.txt");      
        String battleSkillTxt=null;
		try {
			battleSkillTxt = DBPool.getInst().readTxtFromPool("skill_anger");
		} catch (Exception e) {		
			e.printStackTrace();
			return;
		}
		if(battleSkillTxt==null)
		{
			System.out.println("tab_txt��skill_anger�����ڣ�");	
		}
		//System.out.println("battleSkillTxt="+battleSkillTxt);
		
        String[][] battleSkillData = Tools.getStrLineArrEx2(battleSkillTxt, "data:","dataEnd");  //������ܶ�ά����
        //System.out.println("battleSkillData ="+Tools.strArr2Str2(battleSkillData));
        /*NUM, //1���
        NAME, //2������
        ICON_PATH, //3ͼ�����
        MAXLV, //4���ȼ�
        POWER_LEFT, //5ŭ������
        SELFBUFF, //6������BUFF���                
        TYPE, //7�������� 1:�˺�,2:�ָ�,3:BUFF
        TARGET_TYPE, //8�Եж���1:���� 2:�Ե�
        RANGE,  //9������Χ
        RANGE_ARGS, //10��Χ����
        MAXUSETIMES, //11���ʹ�ò���
        BATTLE_POWER, //12ս��
        CALC_ARGS, //13�������
        CRITICAL_ADD, //14��������
        BUFF, //15BUFF���
        BUFF_RATE, //16BUFF����
        OTHER_ARGS, //17��������
        OTHER_RATE, //18����������
        CLEAR_AMOUNT, //19��ɢ����
        HIT_RATE, //20������
*/        
       
        //battleSkillFixDataArr = new BattleSkillFixData[battleSkillData.length];
        for (int i = 0; battleSkillData != null && i < battleSkillData.length; i++)
        {
        	BattleSkillFixData data = new BattleSkillFixData();
        	data.skillCategory = Const.SKILLCATEGORY_ANGRY;
        	data.num = Tools.str2int(battleSkillData[i][ANGRY_DATA_INDEX.NUM.ordinal()]);
        	data.name = battleSkillData[i][ANGRY_DATA_INDEX.NAME.ordinal()];
        	data.maxLv = Tools.str2byte(battleSkillData[i][(int)ANGRY_DATA_INDEX.MAXLV.ordinal()]);
        	data.powerLeft = Tools.str2byte(battleSkillData[i][(int)ANGRY_DATA_INDEX.MAXLV.ordinal()]);        	
        	data.selfBuff = Tools.str2short(battleSkillData[i][(int)ANGRY_DATA_INDEX.SELFBUFF.ordinal()]);
        	data.type = Tools.str2byte(battleSkillData[i][(int)ANGRY_DATA_INDEX.TYPE.ordinal()]);
        	data.targetType = Tools.str2byte(battleSkillData[i][(int)ANGRY_DATA_INDEX.TARGET_TYPE.ordinal()]); 
        	data.range = Tools.str2byte(battleSkillData[i][(int)ANGRY_DATA_INDEX.RANGE.ordinal()]);        
        	data.maxUseTimes = Tools.str2byte(battleSkillData[i][(int)ANGRY_DATA_INDEX.MAXUSETIMES.ordinal()]);   
        	data.harmArgs = Tools.splitStrToShortArr(battleSkillData[i][(int)ANGRY_DATA_INDEX.HARM_ARGS.ordinal()], ",",true);
        	data.criticalAdd = Tools.str2short(battleSkillData[i][(int)ANGRY_DATA_INDEX.CRITICAL_ADD.ordinal()]);
        	data.buffs = Tools.splitStrToShortArr(battleSkillData[i][(int)ANGRY_DATA_INDEX.BUFF.ordinal()],",",true);
        	data.buffsRate = Tools.splitStrToShortArr2(battleSkillData[i][(int)ANGRY_DATA_INDEX.BUFF_RATE.ordinal()],"|",",");
        	data.otherArgs = Tools.splitStrToShortArr(battleSkillData[i][(int)ANGRY_DATA_INDEX.OTHER_ARGS.ordinal()],",",true);
        	data.otherRate = Tools.str2byte(battleSkillData[i][(int)ANGRY_DATA_INDEX.OTHER_RATE.ordinal()]);
        	data.clearAmount = Tools.str2byte(battleSkillData[i][(int)ANGRY_DATA_INDEX.CLEAR_AMOUNT.ordinal()]);
        	data.hitRate = Tools.str2byte(battleSkillData[i][(int)ANGRY_DATA_INDEX.HIT_RATE.ordinal()]);
        	data.rangeArgs = Tools.splitStrToShortArr(battleSkillData[i][(int)ANGRY_DATA_INDEX.RANGE_ARGS.ordinal()],",",true);
        	angrySkillFixDataVC.add(data);
        }
    }
	
    public static BattleSkillFixData getSkillDataByNum(int num)
    {
    	if(num>=2001)  //�������
    	{
    		for (int i = 0; autoSkillFixDataVC != null && i < autoSkillFixDataVC.size(); i++)
            {
            	BattleSkillFixData data = autoSkillFixDataVC.get(i);
                if (data.num == num)
                {
                    return data;
                }
            }
    		System.out.println("������ܱ��"+num+"������");
    	}
    	else if(num>=1001 && num<=2000)   //��������
    	{
    		for (int i = 0; beSkillFixDataVC != null && i < beSkillFixDataVC.size(); i++)
            {
    			BattleSkillFixData data = beSkillFixDataVC.get(i);
                if (data.num == num)
                {
                    return data;
                }
            }
            System.out.println("�������ܱ��"+num+"������");
    	}
    	else if(num>=1 && num<=1000)
    	{
    		for (int i = 0; angrySkillFixDataVC != null && i < angrySkillFixDataVC.size(); i++)
            {
            	BattleSkillFixData data = angrySkillFixDataVC.get(i);
                if (data.num == num)
                {
                    return data;
                }
            }
            System.out.println("ŭ���ܱ��"+num+"������");
    	}        
        return null;
    }
    public static BattleSkill getSkill(int skillNum,int level)
    {
    	BattleSkillFixData data =  getSkillDataByNum(skillNum);
    	BattleSkill skill = new BattleSkill();
    	skill.level = (byte)level;
    	skill.battleSkillFixData = data;
    	return skill;    	
    }
}
