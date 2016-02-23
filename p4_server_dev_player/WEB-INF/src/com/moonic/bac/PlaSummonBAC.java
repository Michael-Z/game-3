package com.moonic.bac;

import org.json.JSONArray;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

/**
 * ��ɫ�ٻ�
 * @author wkc
 */
public class PlaSummonBAC extends PlaBAC{
	public static final String tab_summon_ordinary = "tab_summon_ordinary";
	public static final String tab_summon_advanced = "tab_summon_advanced";
	public static final String tab_summon_mystery = "tab_summon_mystery";
	public static final String tab_summon_consume = "tab_summon_consume";
	public static final String tab_summon_first = "tab_summon_first";
	public static final String tab_summon_ordinary_type = "tab_summon_ordinary_type";
	public static final String tab_summon_advanced_type = "tab_summon_advanced_type";
	
	public static JSONArray mystery_week;//�����ٻ�������
	public static JSONArray mystery_day;//�����ٻ�������
	
	/**
	 * ����
	 */
	public PlaSummonBAC() {
		super("tab_pla_summon", "playerid");
	}
	
	/**
	 * ��ʼ��Ŀ������
	 */
	public void init(DBHelper dbHelper, int playerid, Object... parm) throws Exception {
		SqlString sqlStr = new SqlString();
		sqlStr.add("playerid", playerid);
		sqlStr.add("daily1", 0);
		sqlStr.add("total1", 0);
		sqlStr.add("daily2", 0);
		sqlStr.add("single2", 0);
		sqlStr.add("total2", 0);
		sqlStr.add("multi2", 0);
		sqlStr.add("summonprop", 0);
		insert(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ��ͨ�ٻ�
	 */
	public ReturnValue summonOrdinary(int playerid, byte multi){
		DBHelper dbHelper = new DBHelper();
		try {
			int redo = 0;//�ٻ�����
			String priceCol = null;
			if(multi == 1){
				redo = 1;
				priceCol = "price1";
			} else
			if(multi == 2){
				redo = 10;
				priceCol = "price2";
			} else{
				BACException.throwInstance("������������"+multi);
			}
			dbHelper.openConnection();
			DBPaRs plaSummonRs = getDataRs(playerid);
			int times = plaSummonRs.getInt("daily1");
			DBPaRs conListRs = getConListRs(1);
			int freeTimes = conListRs.getInt("free");
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLA_SUMMON_ORDINARY);
			if(!(multi == 1 && times < freeTimes)){//ÿ�յ�����Ѵ���
				int[] conarr = Tools.splitStrToIntArr(conListRs.getString(priceCol), ",");
				subValue(dbHelper, playerid, "summonprop", conarr[1], gl, "�ٻ�����");
			}
			int total = plaSummonRs.getInt("total1");
			StringBuffer remarkSb = new StringBuffer();
			remarkSb.append("��"+(total+1));
			StringBuffer awardSb = new StringBuffer();//�����ַ���
			JSONArray summonarr = new JSONArray();//�鵽������
			String itemStr = null;
			if(multi == 1 && total == 0){//�״λ��ָ����Ż��
				itemStr = getFirstData("frist1");
				awardSb.append(itemStr);
				summonarr.add(itemStr);
				gl.addRemark("�״���ͨ�ٻ�");
				total++;
			} else{
				int random = 0;
				if(multi == 2){
					random = MyTools.getRandom(1, 10);
				}
				for(int i = 1; i <= redo; i++){
					total++;
					String where = null;
					if(total % 50 == 0){//������50�����⴦��,�������1��2
						where = "itemtype=1 or itemtype=2";
					} else{
						if(i == random){
							where = "itemtype=4";
						} else{
							int type = randomItemType(tab_summon_ordinary_type, "type!=1 and type!=2");
							where = "itemtype="+type;
						}
					}
					DBPsRs ordinaryRs = DBPool.getInst().pQueryS(tab_summon_ordinary, where);
					int[] oddsarr = new int[ordinaryRs.count()];
					while(ordinaryRs.next()){
						int odds = ordinaryRs.getInt("odds");
						int type = ordinaryRs.getInt("itemtype");
						if(type == 2){
							int[] item = Tools.splitStrToIntArr(ordinaryRs.getString("item"), ",");
							DBPsRs partnerRs = PartnerBAC.getInstance().query(playerid, "playerid="+playerid+" num="+item[1]);
							int count = partnerRs.count();
							if(count > 0){
								odds /= 2;
							}
						}
						oddsarr[ordinaryRs.getRow()-1] = odds;
					}
					int index = MyTools.getIndexOfRandom(oddsarr);
					ordinaryRs.setRow(index+1);
					itemStr = ordinaryRs.getString("item");
					if(i > 1){
						awardSb.append("|");
					}
					awardSb.append(itemStr);
					summonarr.add(itemStr);
				}
			}
			SqlString sqlStr = new SqlString();
			if(multi == 1){
				sqlStr.addChange("daily1", 1);
			}
			sqlStr.add("total1", total);
			update(dbHelper, playerid, sqlStr);
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, awardSb.toString(), ItemBAC.SHORTCUT_MAIL, 1, gl);
			PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_CALL_PARTNER, redo, gl);
			
			JSONArray returnarr = new JSONArray();
			returnarr.add(summonarr);//��Ʒ����
			returnarr.add(awardarr);//��������
			
			if(multi == 2){
				remarkSb.append("~"+total);
			}
			remarkSb.append("���ٻ�");
			gl.addRemark(remarkSb);
			gl.addRemark("�ٻ��������"+summonarr.toString());
			
			gl.save();
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �����ٻ�
	 */
	public ReturnValue summonAdvanced(int playerid, byte multi){
		DBHelper dbHelper = new DBHelper();
		try {
			int redo = 0;//�ٻ�����
			String priceCol = null;
			if(multi == 1){
				redo = 1;
				priceCol = "price1";
			} else
			if(multi == 2){
				redo = 10;
				priceCol = "price2";
			} else{
				BACException.throwInstance("������������"+multi);
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLA_SUMMON_ADVANCED);
			DBPaRs conListRs = getConListRs(2);
			int freeTimes = conListRs.getInt("free");
			DBPaRs plaSummonRs = getDataRs(playerid);
			int times = plaSummonRs.getInt("daily2");
			int[] conarr = Tools.splitStrToIntArr(conListRs.getString(priceCol), ",");
			int need = conarr[1];
			if(multi == 1){//��������Ѵ�����
				if(times < freeTimes){
					need = 0;
				}
			}
			dbHelper.openConnection();
			if(need > 0){
				PlayerBAC.getInstance().useCoin(dbHelper, playerid, need, gl);
			}
			StringBuffer remarkSb = new StringBuffer();
			int total = plaSummonRs.getInt("total2");//�����ܼ�
			remarkSb.append("��"+(total+1));
			int times10 = plaSummonRs.getInt("multi2");//10�������ܼ�
			int firstcoin = plaSummonRs.getInt("single2");//�Ƿ���й��״����Ľ�
			StringBuffer awardSb = new StringBuffer();
			boolean isFirstCoin = false;//�Ƿ�Ϊ�״����Ľ�
			JSONArray summonarr = new JSONArray();//�鵽������
			if(multi == 1 && total == 0){//�״λ��ָ����Ż��
				String first = getFirstData("frist2");
				awardSb.append(first);//���ָ����Ż��
				summonarr.add(first);
				total++;
			} else
			if(multi == 1 && need > 0 && times == freeTimes && firstcoin == 0){
				String first = getFirstData("frist3");
				awardSb.append(first);//���ָ����Ż��
				summonarr.add(first);
				isFirstCoin = true;
				total++;
			} else
			{
				boolean isFirst10 = false;//�Ƿ����״�10��
				if(multi == 2){
					if(times10 == 0){
						isFirst10 = true;
						if(firstcoin == 0){
							isFirstCoin = true;
						}
					}
				}
				int[] arr = new int[redo];
				for(int i = 1; i <= redo; i++){
					arr[i-1] = i;
				}
				int maxRanAm = 2;//������ֵ���������
				int ranAm = 0;
				if(isFirst10){
					ranAm = 1;
					maxRanAm = 1;
				}
				if(isFirstCoin){
					ranAm = 2;
					maxRanAm = 0;
				}
				int[] randomArr = null;
				if(ranAm > 0){
					randomArr = getDiffRandom(arr, ranAm);
				}
				int partnerAm = 0;//���ֻ�����
				JSONArray parterArr = new JSONArray();
				for(int i = 1; i <= redo; i++){
					total++;
					String itemStr = null;
					if(randomArr != null && Tools.contain(randomArr, i)){
						if(MyTools.getIndexByInt(randomArr, i) == 0){
							itemStr = getFirstData("frist4");
						} else{
							itemStr = getFirstData("frist3");
						}
					} else{
						String where = null;
						if(total % 10 == 0){//10�αصû��
							if(multi == 1){
								where = "itemtype=1 or itemtype=2";
							} else{
								if(!isFirst10){
									where = "itemtype=2 or itemtype=3";
								}
							}
						} else{
							int type = 0;
							if(partnerAm >= maxRanAm){//���ֻ����������Ϊ2
								type = randomItemType(tab_summon_advanced_type, "type!=1 and type!=2 and type!=3");
							} else{
								type = randomItemType(tab_summon_advanced_type, null);
							}
							where = "itemtype="+type;
						}
						DBPsRs advancedRs = DBPool.getInst().pQueryS(tab_summon_advanced, where);
						int[] oddsarr = new int[advancedRs.count()];
						while(advancedRs.next()){
							int itemtype = advancedRs.getInt("itemtype");
							int odds = advancedRs.getInt("odds");
							int[] item = Tools.splitStrToIntArr(advancedRs.getString("item"), ",");
							if(itemtype == 3){//�ѻ�õ�3�Ǽ���Ϊ����
								DBPsRs partnerRs = PartnerBAC.getInstance().query(playerid, "playerid="+playerid+" num="+item[1]);
								int count = partnerRs.count();
								if(count > 0){
									odds /= 2;
								}
							}
							if(itemtype == 2){//�ѻ�õ�2�Ǽ���Ϊ����
								DBPsRs partnerRs = PartnerBAC.getInstance().query(playerid, "playerid="+playerid+" num="+item[1]);
								int count = partnerRs.count();
								if(count > 0){
									odds /= 2;
								}
							}
							if((itemtype == 1 || itemtype == 2 || itemtype == 3) && parterArr.contains(item[1])){
								odds = 1/2;
							}
							oddsarr[advancedRs.getRow()-1] = odds;
						}
						int index = MyTools.getIndexOfRandom(oddsarr);
						advancedRs.setRow(index+1);
						itemStr = advancedRs.getString("item");
					}
					if(i > 1){
						awardSb.append("|");
					}
					int[] itemarr = Tools.splitStrToIntArr(itemStr, ",");
					if(itemarr[0] == 9){
						partnerAm++;
						parterArr.add(itemarr[1]);
					} 
					awardSb.append(itemStr);
					summonarr.add(itemStr);
				}
			}
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, awardSb.toString(), ItemBAC.SHORTCUT_MAIL, 1, gl);
			SqlString sqlStr = new SqlString();
			sqlStr.add("total2", total);
			if(multi == 1){
				sqlStr.addChange("daily2", 1);
			} else{
				sqlStr.addChange("multi2", 1);
			}
			if(isFirstCoin == true){
				sqlStr.add("single2", 1);
			}
			update(dbHelper, playerid, sqlStr);
			PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_CALL_PARTNER, redo, gl);
			JSONArray returnarr = new JSONArray();
			returnarr.add(summonarr);//�������
			returnarr.add(awardarr);//�����Ľ�������
			
			if(multi == 2){
				remarkSb.append("~"+total);
			}
			remarkSb.append("���ٻ�");
			gl.addRemark(remarkSb);
			gl.addRemark("�ٻ��������"+summonarr.toString());
			
			gl.save();
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �����ٻ�
	 */
	public ReturnValue summonMystery(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs conListRs = getConListRs(4);
			int[] conarr = Tools.splitStrToIntArr(conListRs.getString("price1"), ",");
			int need = conarr[1];
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLA_SUMMON_MYSTERY);
			if(need > 0){
				PlayerBAC.getInstance().useCoin(dbHelper, playerid, need, gl);
			}
			JSONArray mysteryArr = new JSONArray();
			for(int i = 0; i < mystery_day.length(); i++){
				mysteryArr.add(mystery_day.getJSONArray(i));
			}
			for(int i = 0; i < mystery_week.length(); i++){
				mysteryArr.add(mystery_week.getJSONArray(i));
			}
			int[] oddsarr = new int[mysteryArr.length()];
			for(int i = 0; i < mysteryArr.length(); i++){
				JSONArray item = mysteryArr.optJSONArray(i);
				oddsarr[i] = item.optInt(2);
			}
			int index = MyTools.getIndexOfRandom(oddsarr);
			JSONArray item = mysteryArr.optJSONArray(index);
			int random = MyTools.getRandom(1, 100);
			StringBuffer awardSb = new StringBuffer();
			if(random < item.optInt(3)){//�����
				awardSb.append("9,");
				awardSb.append(item.optInt(0));
			} else{
				awardSb.append("1,7,");
				awardSb.append(item.optInt(1));
				awardSb.append(",");
				awardSb.append(MyTools.getRandom(item.optInt(4), item.optInt(5)));
			}
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, awardSb.toString(), ItemBAC.SHORTCUT_MAIL, 1, gl);
			PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_CALL_PARTNER, gl);
			
			JSONArray returnarr = new JSONArray();
			returnarr.add(awardSb.toString());//��������
			returnarr.add(awardarr);//�ӱ�������
			gl.save();
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ���������ٻ�����Ʒ
	 * @param type 1:����Ʒ��2:����Ʒ
	 * @param amount �����Ʒ����������Ʒ1һ��������Ʒ3��
	 */
	public JSONArray createMysteryItem(byte type, int amount) throws Exception {
		JSONArray jsonarr = new JSONArray();
		if(type == 2){//����Ʒָ�����
			String paramStr = CustomActivityBAC.getInstance().getFuncActiPara(CustomActivityBAC.TYPE_SUMMON_PARTNER_COERCE);
			if(paramStr != null){
				int param = Tools.str2int(paramStr);
				DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_summon_mystery, "partner="+param);
				if(!partnerRs.exist()){
					BACException.throwInstance("����Ų�����"+param);
				}
				JSONArray item = new JSONArray();
				item.add(param);//�����
				item.add(partnerRs.getInt("soul"));//��Ӧ��ʯ���
				item.add(partnerRs.getInt("hit"));//���м���
				item.add(partnerRs.getInt("odds"));//����Ʒ����
				int[] amarr = Tools.splitStrToIntArr(partnerRs.getString("amount"), ",");
				item.add(amarr[0]);//��ʯ������Сֵ
				item.add(amarr[1]);//��ʯ�������ֵ
				jsonarr.add(item);
				amount -= 1;
			} 
		}
		DBPsRs mysteryRs = DBPool.getInst().pQueryS(tab_summon_mystery, "cycle="+type);
		int[] oddsarr = new int[mysteryRs.count()];
		while(mysteryRs.next()){
			oddsarr[mysteryRs.getRow()-1] = mysteryRs.getInt("show");
		}
		for(int i = 0; i < amount; i++){
			int index = MyTools.getIndexOfRandom(oddsarr);
			mysteryRs.setRow(index+1);
			JSONArray item = new JSONArray();
			item.add(mysteryRs.getInt("partner"));//�����
			item.add(mysteryRs.getInt("soul"));//��Ӧ��ʯ���
			item.add(mysteryRs.getInt("hit"));//���м���
			item.add(mysteryRs.getInt("odds"));//����Ʒ����
			int[] amarr = Tools.splitStrToIntArr(mysteryRs.getString("amount"), ",");
			item.add(amarr[0]);//��ʯ������Сֵ
			item.add(amarr[1]);//��ʯ�������ֵ
			jsonarr.add(item);
			oddsarr[index] = 0;
		}
		return jsonarr;
	}

	/**
	 * �������ı�Ż�ȡ��������
	 */
	public DBPaRs getConListRs(int num) throws Exception {
		DBPaRs conListRs = DBPool.getInst().pQueryA(tab_summon_consume, "num="+num);
		if(!conListRs.exist()){
			BACException.throwInstance("���ı�Ų�����");
		}
		return conListRs;
	}
	
	/**
	 * ��ȡ�״����⴦������
	 */
	public String getFirstData(String column) throws Exception {
		DBPsRs firstListRs = DBPool.getInst().pQueryS(tab_summon_first);
		firstListRs.next();
		return firstListRs.getString(column);
	}
	
	/**
	 * �������л�ȡ������ͬ�������
	 */
	public int[] getDiffRandom(int[] arr, int amount){
		int[] ranarr = new int[amount];
		for(int i = 0; i < amount; i++){
			int ranInd = MyTools.getRandom(0, arr.length-1);
			ranarr[i] = arr[ranInd];
			arr = Tools.removeOneFromIntArr(arr, ranInd);
		}
		return ranarr;
	}
	
	/**
	 * �����Ʒ����
	 * @throws Exception 
	 */
	public int randomItemType(String tab, String where) throws Exception{
		DBPsRs typeRs = DBPool.getInst().pQueryS(tab, where);
		int[] oddsarr = new int[typeRs.count()];
		while(typeRs.next()){
			oddsarr[typeRs.getRow()-1] = typeRs.getInt("odds");
		}
		int index = MyTools.getIndexOfRandom(oddsarr);
		typeRs.setRow(index+1);
		return typeRs.getInt("type");
	}
	
	
	/**
	 * ����ÿ������
	 */
	public void resetData(DBHelper dbHelper, int playerid) throws Exception {
		SqlString sqlStr = new SqlString();
		sqlStr.add("daily1", 0);
		sqlStr.add("daily2", 0);
		update(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ��ȡ����
	 */
	public JSONArray getData(int playerid) throws Exception {
		DBPaRs plaSummonRs = getDataRs(playerid);
		JSONArray jsonarr = new JSONArray();
		jsonarr.add(mystery_day);//ÿ�������ٻ���Ʒ
		jsonarr.add(mystery_week);//ÿ�������ٻ���Ʒ
		jsonarr.add(plaSummonRs.getInt("daily1"));//ÿ����ͨ�����ٻ�����
		jsonarr.add(plaSummonRs.getInt("daily2"));//ÿ�����𵥴��ٻ�����
		jsonarr.add(plaSummonRs.getInt("total1"));//��ͨ�ٻ��ܴ���
		jsonarr.add(plaSummonRs.getInt("total2"));//�����ٻ��ܴ���
		jsonarr.add(plaSummonRs.getInt("summonprop"));//�ٻ���������
		return jsonarr;
	}
	
	//--------------��̬��--------------
	
	private static PlaSummonBAC instance = new PlaSummonBAC();

	/**
	 * ��ȡʵ��
	 */
	public static PlaSummonBAC getInstance() {
		return instance;
	}
	
	//--------------������--------------
	
	/**
	 * �����ٻ���¼
	 */
	public ReturnValue debugResetSummon(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			SqlString sqlStr = new SqlString();
			sqlStr.add("daily1", 0);
			sqlStr.add("total1", 0);
			sqlStr.add("daily2", 0);
			sqlStr.add("single2", 0);
			sqlStr.add("total2", 0);
			sqlStr.add("multi2", 0);
			update(dbHelper, playerid, sqlStr);
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �����ٻ�����
	 */
	public ReturnValue debugAddSummonprop(int playerid, int amount){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			SqlString sqlStr = new SqlString();
			sqlStr.addChange("summonprop", amount);
			update(dbHelper, playerid, sqlStr);
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
}
