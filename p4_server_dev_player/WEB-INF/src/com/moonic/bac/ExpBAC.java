package com.moonic.bac;

import org.json.JSONArray;

import com.moonic.gamelog.GameLog;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;

/**
 * ����
 * @author John
 */
public class ExpBAC {
	
	/**
	 * ���Ӿ���
	 * @param logname ����־�е�����
	 */
	public JSONArray addExp(String table, int lv, long exp, long addexp, int maxlv, String logname, GameLog gl) throws Exception{
		long beforeexp = exp;//����ǰ����
		long beforelv = lv;//����ǰ�ȼ�
		exp += addexp;
		long totalexp = exp;//���Ӻ��ܾ���
		long destroyexp = 0;//���ٵľ���
		while(true){
			DBPaRs expRs = DBPool.getInst().pQueryA(table, "lv="+lv);
			if(!expRs.exist() || expRs.getLong("needexp")==-1){//�ȼ��Ѵ��Ѵ�����
				destroyexp = exp;//��¼���پ�����
				exp = 0;//�������
				break;
			}
			long needexp = expRs.getLong("needexp");//������Ҫ����
			if(maxlv > 0 && lv >= maxlv){//�Ƿ��е����ĵȼ���������
				if(exp > needexp){//�����ǰӵ�о��鳬��������Ҫ�������������ٳ����ľ���
					destroyexp = exp - needexp;
					exp = needexp;
				}
				break;
			}
			if(exp < needexp){//���鲻�����˳�ѭ��
				break;
			}
			lv++;
			exp -= needexp;
		}
		if(beforeexp != exp){//����仯
			gl.addChaNote(logname+"����", beforeexp, addexp);
		}
		if(destroyexp > 0){//���پ�����
			if(exp == 0){
				gl.addRemark(logname+"�ȼ����������پ���"+destroyexp);
			} else {
				gl.addRemark(logname+"�ȼ��ﵽָ�����ƣ����پ���"+destroyexp);
			}
		}
		if(beforelv != lv){//�ȼ��仯
			gl.addChaNote(logname+"����", totalexp, exp-totalexp);
			gl.addChaNote(logname+"�ȼ�", beforelv, lv-beforelv);
		}
		if(beforelv != lv || beforeexp != exp){//�ȼ�����仯
			JSONArray returnarr = new JSONArray();
			returnarr.add(lv);
			returnarr.add(exp);
			returnarr.add(totalexp-beforeexp-destroyexp);//ʵ��ʹ�õľ���
			return returnarr;
		} else {
			return null;
		}
	}
	
	//--------------��̬��--------------
	
	private static ExpBAC instance = new ExpBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static ExpBAC getInstance(){
		return instance;
	}
}
