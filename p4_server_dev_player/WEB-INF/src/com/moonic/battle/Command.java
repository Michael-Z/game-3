package com.moonic.battle;

/**
 * ս��ָ�����
 * @author huangyan
 *
 */
public class Command 
{	
	public byte commandType; //ָ������
	
	public static final byte CMD_NONE=0; //���ж�
	public static final byte CMD_WAIT=1;  //����
	public static final byte CMD_ATTACK=2; //��ͨ����
	public static final byte CMD_AUTOSKILL=3; //�������
	public static final byte CMD_ANGRYSKILL=4; //ŭ����    	

    public int[] targetIds; //Ŀ��Ⱥ��id����
    //���ܹ���ʱ
    public int skillNum; //ʹ�õļ��ܱ��
    public byte skillLevel=1; //��������õļ��ܵȼ�
    
    public int ranStart; //ʹ�õ��������ʼֵ
    public int ranEnd; //ʹ�õ����������ֵ    
    
    
    public Command(int type)
    {
    	commandType = (byte)type;
    }
    public Command()
    {
    	
    }   
}
