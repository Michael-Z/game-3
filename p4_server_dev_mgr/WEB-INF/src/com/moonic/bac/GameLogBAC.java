package com.moonic.bac;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

import org.json.JSONObject;

import server.common.Tools;
import server.config.ServerConfig;

import com.ehc.common.SqlString;
import com.ehc.dbc.BaseActCtrl;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

import conf.LogTbName;

/**
 * ��Ϸ��־
 * @author John
 */
public class GameLogBAC extends BaseActCtrl {
	public static final String tab_game_log_datatype = "tab_game_log_datatype";
	
	/**
	 * ����
	 */
	public GameLogBAC() {
		super.setTbName(LogTbName.TAB_GAME_LOG());
		setDataBase(ServerConfig.getDataBase_Backup());
	}
	
	/**
	 * ��ȡ����
	 */
	public JSONObject getPageList(PageContext pageContext) {
		ServletRequest request = pageContext.getRequest();
		int page = Tools.str2int(request.getParameter("page"));
		if(page==0) {
			page=1;
		}
		int rpp=Tools.str2int(request.getParameter("rpp"));
		if(rpp==0) {
			rpp=10;
		}
		String ordertype=request.getParameter("ordertype");
		if(ordertype==null || ordertype.equals("")) {
			ordertype="DESC";
		}
		String showorder = request.getParameter("showorder");
		if(showorder==null || showorder.equals("")) {
			showorder="log."+showorder;
		}
		String[] moduleArr = request.getParameterValues("module"); //ģ��ɸѡ
		String[] actStrArr = request.getParameterValues("act"); //ָ��ɸѡ
		String playerName = request.getParameter("playerName");//ָ�������
		String search_act = request.getParameter("search_act");//ָ��������
		String startTime = request.getParameter("startTime");//ָ����ʼʱ��
		String endTime = request.getParameter("endTime");//ָ����ֹʱ��
		String serverId = request.getParameter("serverId");//ָ��������ID
		
		String[] changeArr = request.getParameterValues("otherchange");//�仯ɾѡ
		
		String consume = Tools.strNull(request.getParameter("consume"));//������Ϣģ����������
		String obtain = Tools.strNull(request.getParameter("obtain"));//��ȡ��Ϣģ����������
		String remark = Tools.strNull(request.getParameter("remark"));//��ע��Ϣģ����������
		
		if(startTime == null || startTime.equals("")) {
			startTime = MyTools.getTimeStr();
		}
		if(playerName!=null) {
			playerName = playerName.trim();
		}
		
		String orderClause = showorder + " " + ordertype;
		
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		for(int i=0; actStrArr!=null && i<actStrArr.length; i++) {
			if(i>0){
				sb.append(" or ");
			}
			sb.append("act="+actStrArr[i]);
		}
		sb.append(")");
		String actCondition = sb.toString();
		
		SqlString sqlS = new SqlString();
		if(serverId!=null && !serverId.equals("")) {
			sqlS.add("log.serverid", Tools.str2int(serverId));//����������
		}
		if(!consume.equals("")) {
			sqlS.add("log.consume", consume,"like");//��������
		}
		if(!obtain.equals("")) {
			sqlS.add("log.obtain", obtain,"like");//�������
		}
		if(!remark.equals("")) {
			sqlS.add("log.remark", remark,"like");//��ע����
		}
		if(playerName!=null && !playerName.equals("")) {//��ɫ������
			if(serverId!=null && !serverId.equals("")) {
				int playerId = PlayerBAC.getInstance().getIntValue("id", "name='"+playerName+"' and serverId="+serverId);
				sqlS.add("log.playerid", playerId);
			} else {
				sqlS.addWhere("log.playerid in (select id from tab_player where name='"+playerName+"')");				
			}
		}
		if(search_act!=null && !search_act.equals("")) {//�����Ų�ѯ����
			sqlS.add("log.act", Tools.str2int(search_act));
		}		
		if(startTime!=null && !startTime.equals("")) {//��ʼʱ������
			sqlS.addDateTime("log.createtime", startTime.trim(),">=");
		}
		if(endTime!=null && !endTime.equals("")) {//��ֹʱ������
			sqlS.addDateTime("log.createtime", endTime.trim(),"<=");
		} else {
			sqlS.addDateTime("log.createtime", MyTools.getTimeStr(MyTools.getCurrentDateLong()+MyTools.long_day),"<");
		}
		if(changeArr!=null){//��ֵ�仯����
			try {
				DBPsRs dtypeRs = DBPool.getInst().pQueryS(tab_game_log_datatype);
				while(dtypeRs.next()){
					if(MyTools.checkInStrArr(changeArr, dtypeRs.getString("dtype"))){
						sqlS.addWhereOr("log."+dtypeRs.getString("chacol")+" is not null");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(!actCondition.equals("()")) {//ָ��ɸѡ����
			sqlS.addWhere(actCondition);
		}
		if(moduleArr != null) {//ģ��ɸѡ����
			sqlS.addWhere("log.act in (select code from tab_game_func where module in("+Tools.strArr2Str(moduleArr)+"))");
		}
		String sql = "select log.* from "+LogTbName.TAB_GAME_LOG()+" log "+sqlS.whereStringEx()+" order by "+orderClause;
		//System.out.println("sql:"+sql);
		return getJsonPageListBySQL(sql, page, rpp);
	}
	
	//--------------��̬��--------------
	
	public static GameLogBAC instance = new GameLogBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static GameLogBAC getInstance(){
		return instance;
	}
}
