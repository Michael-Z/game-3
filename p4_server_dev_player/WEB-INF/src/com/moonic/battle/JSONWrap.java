package com.moonic.battle;

import org.json.JSONObject;

/**
 * json��ֵת����װ����
 * @author huangyan
 *
 */
public class JSONWrap 
{
	private JSONObject json;	
	
	static public class KEY
	{
		public static final String ID="a"; //role id
		public static final String CMD="b"; //ս��ָ����
		public static final String TARGET_IDS="c"; //Ŀ��roles id
		public static final String SOURCE_HP_CHANGE="d"; //��Դ��ɫHP�仯ֵ		
		public static final String TARGET_HP_CHANGE="e"; //Ŀ���ɫHP�仯ֵ
		public static final String SKILL_NUM="f"; //���ܱ��
		public static final String CRITICAL="g"; //�Ƿ񱩻�
		public static final String BLOCK="h"; //�� 
		public static final String DODGE="i"; //����						
		public static final String ANGRY="j";  //ŭ���仯���ֵ
		public static final String WIN_TEAM="win";  //ʤ���Ķ����
		public static final String ADD_BUFF="add"; //�����е�buff
		public static final String QUEUE="que";  //ÿ�غ�ս������
		public static final String EXECBUFF="buf";  //ÿ�غ�ǰִ��BUFF���
		public static final String REMOVE_BUFF="del"; //�������buff		
		public static final String PRE="pre"; //սǰ��Ϊ	
		public static final String BLUE_BLOOD="blue"; //������ɫ��Ѫ	
		public static final String EXECBESKILL="beskl";  //ÿ�غ�ǰִ�б������ܽ��
		public static final String LEFTHP="left";  //ÿ�غ�ʣ��Ѫ��
	}
	
	public JSONWrap()
	{
		json = new JSONObject();
	}
	public void put(String key,int value)
	{
		json.put(key, value);
	}
	public void put(String key,Object value)
	{
		json.put(key, value);
	}
	public JSONObject getJsonObj()
	{
		return json;
	}
}
