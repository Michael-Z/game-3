package com.moonic.battle;

public class BattleSkillFixData 
{   
	public int skillCategory;  //���ܴ��ࣺ1�����2ŭ
	
	//����ֶ� ��1���	2������	3ͼ�����	4���ȼ�	5������BUFF���	6��������	7ʩ��ʱ��	8��������	9�Եж���	10��Χ	11��Χ����	12��󹥻�����	13ս��	14�������	15��������	16BUFF���	17BUFF����	18��������	19������������	20������	21������ЧCFG���	22�ӳ�	23�Ƿ�������	24Ʈ�֡���������Чʱ���룩	25Ŀ��������Ч���	26��������ƫ��	27������Ч	28������ʽ	29������
	//ŭ�ֶΣ�  1���	2������	3ͼ�����	4���ȼ�	5ŭ������	6������BUFF���	7��������	8�Եж���	9��Χ	10��Χ����	11��󹥻�����	12ս��	13�������	14��������	15BUFF���	16BUFF����	17��������	18������������	19��ɢ����	20������	21������ЧCFG���	22�ӳ�	23�Ƿ�������	24Ʈ�֡���������Чʱ���룩	25Ŀ��������Ч���	26��������ƫ��	27������Ч	28������ʽ
	
	public int num;
    public String name;
    public byte maxLv; //���ȼ�
    public short selfBuff; //������BUFF���
    public short powerLeft; //ŭ������
    public byte rate; //��������
    public short[] useTurn; //ʩ��ʱ��
    public byte type; //�������� 1:�˺�,2:�ָ�,3:buff
    public byte targetType; //�Եж���1:����2���Ե�
    public byte range; //������Χ
    public short[] rangeArgs; //��Χ����
    public byte maxUseTimes; //��󹥻�����
    public short[] harmArgs; //�������
    public short criticalAdd; //��������
    public short[] buffs; //BUFF���
    public short[][] buffsRate; //BUFF��������
    public short[] otherArgs; //��������
    public byte otherRate; //������������
    public byte hitRate; //������
    public byte clearAmount; //��ɢ����    
    
    //��������ר�ò���
    public short[][] args;
    public byte owner;
    
   
    public int calcExtraRecover(BattleRole battleRole,int maxHP)
    {    	       
        return 0;
    }
    /// <summary>
    /// ���㹥��ħ��ʵ���˺�
    /// </summary>
    /// <param name="baseValue"></param>
    /// <returns></returns>
    public int calcExtraHarm(BattleRole battleRole,int baseValue)
    {    	    
        return 0;
    }
    
}
