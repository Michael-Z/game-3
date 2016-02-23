package com.moonic.battle;

import org.json.JSONArray;

import server.common.Tools;

import com.moonic.bac.PartnerBAC;
import com.moonic.util.BACException;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;

import conf.Conf;

/**
 * ս����������
 * @author John
 */
public class SpriteBox 
{		
	public int id; //ս���е�Ψһ��ʶid
	public int playerId;//��������ID
	public int partnerId;//���ID
	public byte type;//ս��������(1.TAB_PARTNER 2.TAB_ENEMY)
	public int num;//���
	public short level; //�ȼ�
	public String name;//����
	public byte phase = 1;//������
	public byte star = 1;//����Ǽ�
	
	//---��������---
	
	public byte battletype;//ս���߹�������(ְҵ) ְҵ��ţ�1 ��ս��  2 ������ 3 ����� 4 ��ɱ�� 5 ������
	public byte sex;//�Ա� 1�� 2Ů
	
	//---���²�������Ҫ�ڴ���SPRITEBOX����ʱ��ֵ---
	
	public byte teamType; //0:A�� 1:B��
	public byte posNum;//λ�ñ�� ��1��ʼ�����󵽺󣬴�ǰ����˳����
	
	//---���²�������Ҫ������ֵ---
	
	public static final byte BATTLE_PROP_LEN = 16;
	
	public int[] battle_prop = new int[BATTLE_PROP_LEN];//ս������
	
	public JSONArray autoSkills = new JSONArray();//����[[���ܱ��,���ܵȼ�],...]
	public JSONArray angrySkills = new JSONArray();//����[[���ܱ��,���ܵȼ�],...] --�����������һ��
	public JSONArray beSkills = new JSONArray();//����[[���ܱ��,���ܵȼ�],...]
	
	public int skillAddBattlerPower;//���ܶ������ӵ�ս��
	
	//---���²�������ת��ʱ������ֵ����---
	
	public int[] battle_prop_perc = new int[BATTLE_PROP_LEN];//ս�����Լӳɰٷֱ�
	public int[] battle_prop_save = new int[BATTLE_PROP_LEN];//���ڼ���ս�����Լӳɰٷֱȵ�����
	public int[] battle_prop_src = new int[BATTLE_PROP_LEN];//���ɳ�����ԭʼ����
	
	/**
	 * �������ڼ���ս�����Լӳɰٷֱȵ�����
	 */
	public void saveBattlePropSave(){
		for(int i = 0; i < battle_prop_perc.length; i++){
			battle_prop_save[i] = battle_prop[i];
		}
	}
	
	/**
	 * ����ԭʼ����
	 */
	public void saveBattlePropSrc(){
		for(int i = 0; i < battle_prop_perc.length; i++){
			battle_prop_src[i] = battle_prop[i];
		}
	}
	
	/**
	 * ת��
	 */
	public void conver() throws Exception {
		//��������
		for(int i = 0; i < beSkills.length(); i++){
			JSONArray skiarr = beSkills.optJSONArray(i);
			int skinum = skiarr.optInt(0);
			int skilv = skiarr.optInt(1);
			DBPaRs skiRs = DBPool.getInst().pQueryA(PartnerBAC.tab_bskill, "num="+skinum);
			double[][] attr = Tools.splitStrToDoubleArr2(skiRs.getString("attr"), "|", ",");
			for(int k = 0; k < attr.length; k++){
				if(attr[k][0] == 1){
					addProp(attr[k][0], attr[k][1], attr[k][2], attr[k][3]+attr[k][4]*(skilv-1));
				}
			}
		}
		updateIngredientData("��������");
		//����ս��
		JSONArray[] skigrouparr = {beSkills, autoSkills, angrySkills};
		for(int s = 0; s < skigrouparr.length; s++){
			for(int i = 0; i < skigrouparr[s].length(); i++){
				JSONArray skiarr = skigrouparr[s].optJSONArray(i);
				int skinum = skiarr.optInt(0);
				int skilv = skiarr.optInt(1);
				DBPaRs sbpRs = DBPool.getInst().pQueryA(PartnerBAC.tab_skill_battlepower, "num="+skinum);
				int[] power = Tools.splitStrToIntArr(sbpRs.getString("power"), ",");
				skillAddBattlerPower += power[0] + power[1] * (skilv - 1);
			}	
		}
		//���԰ٷֱ�
		if(battle_prop_save == null){//Ϊ���򲹳�
			saveBattlePropSave();
		}
		for(int i = 0; i < battle_prop_perc.length; i++){
			if(battle_prop_perc[i] > 0){
				addProp(1, i, 1, battle_prop_save[i] * battle_prop_perc[i] / 100);
			}
		}
		updateIngredientData("���԰ٷֱ�");
		//Ѫ����ʼ��
		battle_prop[Const.PROP_HP] = battle_prop[Const.PROP_MAXHP];
		updateIngredientData("Ѫ����ʼ��");
		//��ԭʼս������
		saveBattlePropSrc();
	}
	
	/**
	 * ��ȡ����
	 */
	public JSONArray getJSONArray(){
		JSONArray dataarr = new JSONArray();
		dataarr.add(id);
		dataarr.add(playerId);
		dataarr.add(partnerId);
		dataarr.add(type);
		dataarr.add(num);
		dataarr.add(level);
		dataarr.add(name);
		dataarr.add(phase);
		dataarr.add(star);
		
		dataarr.add(teamType);
		dataarr.add(posNum);
		
		dataarr.add(battle_prop);
		
		dataarr.add(autoSkills);
		dataarr.add(angrySkills);
		dataarr.add(beSkills);
		
		dataarr.add(battletype);
		dataarr.add(sex);
		return dataarr;
	}
	
	/**
	 * ����ս���������鵽������
	 */
	public void addBattleProp(int[] data) throws Exception {
		if(data.length != BATTLE_PROP_LEN){
			return;
		}
		for(int i = 0; i < BATTLE_PROP_LEN; i++){
			addProp(1, i, 1, data[i]);
		}
	}
	
	/**
	 * �������Ե�������
	 */
	public void addProp(String data) throws Exception {
		if(data.equals("-1")){
			return;
		}
		addProp(Tools.splitStrToDoubleArr2(data, "|", ","));
	}
	
	/**
	 * �������Ե�������
	 */
	public void addProp(double[][] data) throws Exception {
		for(int i = 0; data != null && i < data.length; i++){
			addProp(data[i]);
		}
	}
	
	/**
	 * ��������
	 */
	public void addProp(double... data) throws Exception {
		int type = (int)data[0];
		if(type != 1){//ս������){
			BACException.throwAndOutInstance("��ս���������� type="+type);
		}
		if(data[2] == 0){
			battle_prop_perc[(int)data[1]] += data[3];
		} else {
			battle_prop[(int)data[1]] += data[3];
		}
	}
	
	/**
	 * ���뼼��
	 */
	public void addSkill(int num, int level) throws Exception {
		if(num <= 1000){
			angrySkills.add(new JSONArray(new int[]{num, level}));
		} else 
		if(num <= 2000) {
			beSkills.add(new JSONArray(new int[]{num, level}));
		} else 
		{
			autoSkills.add(new JSONArray(new int[]{num, level}));
		}
	}
	
	public String[] ingredientTag;
	public long[][] ingredientData = new long[BATTLE_PROP_LEN*2][];
	
	/**
	 * ���³ɷ�����
	 */
	public void updateIngredientData(String tag){
		if(!Conf.gdout){
			return;
		}
		ingredientTag = Tools.addToStrArr(ingredientTag, tag);
		int index = 0;
		for(int i = 0; i < battle_prop.length; i++,index++){
			ingredientData[index] = Tools.addToLongArr(ingredientData[index], battle_prop[i]);
		}
		for(int i = 0; i < battle_prop_perc.length; i++,index++){
			ingredientData[index] = Tools.addToLongArr(ingredientData[index], battle_prop_perc[i]);
		}
	}
	
	/**
	 * ��ȡ�ɷ������ַ���
	 */
	public String getIngredientStr(){
		if(ingredientTag == null){
			return "";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("-------------------------------------"+name+"("+partnerId+")-------------------------------------\r\n");
		int index = 0;
		for(int i = 0; i < battle_prop.length; i++,index++){
			sb.append(Const.BATTLE_PROP_NAME[i] + getOneAttrIngredientStr(ingredientTag, ingredientData[index]));
		}
		for(int i = 0; i < battle_prop_perc.length; i++,index++){
			sb.append(Const.BATTLE_PROP_NAME[i] + "�ٷֱ�" + getOneAttrIngredientStr(ingredientTag, ingredientData[index]));
		}
		return sb.toString();
	}
	
	/**
	 * ���� tag �����鷵���ַ���
	 */
	private String getOneAttrIngredientStr(String[] tag, long[] data){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < tag.length; i++){
			long addvalue = i==0 ? data[i] : data[i]-data[i-1];
			//if(addvalue > 0)
			{
				sb.append(" ");
				sb.append(tag[i] + ":" + addvalue);		
			}
		}
		sb.append(" ");
		sb.append("��:"+data[data.length-1]);
		sb.append("\r\n");
		return sb.toString();
	}
}
