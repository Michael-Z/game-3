package com.moonic.battle;


/**
 * ���峣��ֵר�� 
 *
 */
public class Const 
{
	public static final byte teamA=0; //A�ӳ���
	public static final byte teamB=1; //B�ӳ���
	public static final byte teamCheat=-1; //�������
	
	public static final byte PROP_MAXHP=0; //�������ֵ
	public static final byte PROP_ATTACK=1; //����
	public static final byte PROP_DEFENCE=2; //�շ�
	public static final byte PROP_MAGICDEF=3; //ħ��
	public static final byte PROP_DODGE=4; //�ر�
	public static final byte PROP_HIT=5; //����
	public static final byte PROP_BLOCK=6; //��
	public static final byte PROP_BREAK=7; //�ƻ�
	public static final byte PROP_TOUGHNESS=8; //����
	public static final byte PROP_CRITICAL=9; //����
	public static final byte PROP_CRITICAL_MUL=10; //�����˺�ֵ
	public static final byte PROP_SPEED=11; //�ٶ�
	public static final byte PROP_HP=12; //��ǰ����ֵ
	public static final byte PROP_ANGER=13; //��ǰŭ��
	public static final byte PROP_CRIT_ADD_DAMAGE=14; //���ӱ����˺�
	public static final byte PROP_BECRIT_SUB_DAMAGE=15; //���ٱ������˺�
	
	public static final String[] BATTLE_PROP_NAME = {"�������ֵ", "����", "�շ�", "ħ��", "�ر�", "����", "��", "�ƻ�", "����", "����", "��������", "�ٶ�", "��ǰ����ֵ", "��ǰŭ��ֵ", "���ӱ����˺�", "���ٱ������˺�"};
	
	public static final byte RANGE_FRONT_SINGLE=1;
	public static final byte RANGE_FRONT_LINE=2;
	public static final byte RANGE_COL=3;
	public static final byte RANGE_BACK_SINGLE=4;
	public static final byte RANGE_SELF=5;
	public static final byte RANGE_N_HIGH=6;
	public static final byte RANGE_N_LOW=7;
	public static final byte RANGE_ALL=8;
	public static final byte RANGE_SINGLE=9;
	public static final byte RANGE_LINE=10;
	public static final byte RANGE_BACK_LINE=11;
	public static final byte RANGE_SPLIT=12;
	
	public static final byte DISTANCE_NEAR=1;
	public static final byte DISTANCE_FAR=2;

	
	public static final byte ACTTYPE_NEAR=1;
	public static final byte ACTTYPE_FAR=2;
	public static final byte ACTTYPE_BACK=3;
	
	public static final byte TARGET_FRIEND=1;
	public static final byte TARGET_ENEMY=2;
	
	public static final byte SKILLTYPE_HARM=1;
	public static final byte SKILLTYPE_RECOVER=2;
	public static final byte SKILLTYPE_BUFF=3;
	
	public static final byte SKILLCATEGORY_AUTO=1;
	public static final byte SKILLCATEGORY_ANGRY=2;
	public static final byte SKILLCATEGORY_BE=3;
	
	//ս����ְҵ
	public static final byte SERIES_NEAR=1;
	public static final byte SERIES_MAGIC=2;
	public static final byte SERIES_DEF=3;
	public static final byte SERIES_KILL=4;
	public static final byte SERIES_HELP=5;
	
	//buff����
	public static byte BUFF_TYPE_PROP=1; //�ı�����
    public static byte BUFF_TYPE_SHELL=2;  //���ӵֵ�ָ���˺�ֵ�Ļ���
    public static byte BUFF_TYPE_ABNORMAL_SHELL=3; //��������ָ�������쳣״̬�Ļ���
    public static byte BUFF_TYPE_CHANGEBODY=4;  //����
    public static byte BUFF_TYPE_CHANGEHARM=5;  //�ı��ܵ��˺�ֵ
    public static byte BUFF_TYPE_DIZZY=6;  //��ѣ
    public static byte BUFF_TYPE_UNCUREABLE=7;  //���ܱ���Ѫ
    public static byte BUFF_TYPE_LOSTHP=8;  //ÿ�غϿ�ʼǰ��Ѫ
    public static byte BUFF_TYPE_CONFUSE=9;  //���ң�ֻ�����Լ���
    public static byte BUFF_TYPE_SILENCE=10;  //��Ĭ���޷�ʩ���������ܺ͸��ʼ���
    public static byte BUFF_TYPE_NOTADDANGRY=11;  //�̶��غ������޷�ͨ���κ�;�����ŭ��
    public static byte BUFF_TYPE_WEAK=12;  //�������Եз���ɵ��˺�����
    public static byte BUFF_TYPE_CORPSE_EXPLODE=13;  //ʬ�屬ը�����˱��������ȼ��ϸ�BUFF�ټ����˺�����BUFF�����ι�����Ч������ڴ��ڸ�BUFFʱ��λ����ɱ����Եз����е�λ����˺��������������ã�
    public static byte BUFF_TYPE_DODGE_ALL=14;  //100%���������˺�
    public static byte BUFF_TYPE_SPRIT_EAT=15;  //Ԫ�����ɣ����˱��������ȼ��ϸ�BUFF�ټ����˺�����BUFF�����ι�����Ч������ڴ��ڸ�BUFFʱ��λ����ɱ��������˺�ֵ�ָ����������������������ã�
    public static byte BUFF_TYPE_COMBO=16;  //��ն�����˱��������ȼ��ϸ�BUFF�ټ����˺�����BUFF�����ι�����Ч������ڴ��ڸ�BUFFʱ��λ����ɱ���򹥻������ι����������빥�������ڣ��������ٴ�ʹ��ŭ�����ܹ�����һ����λ�������������ã�
    public static byte BUFF_TYPE_DETER=17; //Ԫ�����壺���͵з�ָ��������ߵĵ�λ��ָ������    ��ʽ��17,�������Ա��#���͵����Ա�ţ����������,�ָ�#
    
    public static byte HARM_TYPE_POINT=1;
    public static byte HARM_TYPE_DEBUFF=2;
    public static byte HARM_TYPE_PHY=3;
    public static byte HARM_TYPE_MAGIC=4; 
    
    public static byte BESKILL_TYPE3=3;
    public static byte BESKILL_TYPE4=4;
    public static byte BESKILL_TYPE5=5;
    public static byte BESKILL_TYPE6=6;
    public static byte BESKILL_TYPE7=7;
    public static byte BESKILL_TYPE8=8;
    public static byte BESKILL_TYPE9=9;
    public static byte BESKILL_TYPE11=11;
    public static byte BESKILL_TYPE12=12;
    public static byte BESKILL_TYPE13=13;
    public static byte BESKILL_TYPE14=14;
    public static byte BESKILL_TYPE17=17;
    
    static String[] propNames=new String[]{"����","����","�շ�","����","�ر�","����","��","�ƻ�","����","����","�����˺�ֵ","�ٶ�","��ǰ����ֵ","��ǰŭ��","���ӱ����˺�","���ٱ������˺�"};
    
    public static byte EXEC_RESULT_TYPE_HP=1;  //�غ�ǰִ��Ӱ��Ѫ
    public static byte EXEC_RESULT_TYPE_ANGRY=2;  //�غ�ǰִ��Ӱ��ŭ��
    public static byte EXEC_RESULT_TYPE_ADDBUFF=3;  //�غ�ǰִ�м�buff
    
}
