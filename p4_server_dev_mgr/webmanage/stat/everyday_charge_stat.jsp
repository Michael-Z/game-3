<%@ page contentType="text/html; charset=UTF-8"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@ include file="inc_import.jsp"%>
<%@ include file="../system/inc_getuser.jsp"%>
<%
String model="市场统计";
String perm="充值分析表";

String pagetitle = request.getParameter("title");
%>
<%@ include file="../system/inc_checkperm.jsp"%>
<%

String channel = request.getParameter("channel");
String startDate = Tools.strNull(request.getParameter("startDate"));
String endDate = Tools.strNull(request.getParameter("endDate"));
String statChannel = request.getParameter("statChannel");
String statServer = request.getParameter("statServer");

String statBtn = request.getParameter("statBtn");

JsonRs returnRs = null;
if(statBtn != null){
	returnRs = MarketStatBAC.getInstance().getChargeDayData(pageContext);
}
%> 

<html>
<head>
<title></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta HTTP-EQUIV="expires" CONTENT="Tue, 1 Jan 1980 00:00 GMT">
<link href="../css/style1.css" rel="stylesheet" type="text/css">
<script src="../js/common.js"></script>
<script src="../js/Calendar3.js"></script>
<script>
var allValue=new Object();
allValue.channel="<%=channel%>";
allValue.startDate="<%=startDate%>";
allValue.endDate="<%=endDate%>";
allValue.statChannel="<%=statChannel%>";
allValue.statServer="<%=statServer%>";
</script>
<script>
function stat()
{
var theForm = document.forms[0];
if(document.getElementById("startDate").value=="")
{
alert("必须选择开始日期");
document.getElementById("startDate").focus();
return ;
}
wait();
theForm.submit();
}
</script>
</head>
<body bgcolor="#EFEFEF">
<form name="form1" method="post" action="" onSubmit="return stat()">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr> 
    <td valign="top" > 
      <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr> 
          <td > 
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr> 
                <td width="1" valign="bottom" ><table width="100%"  border="0" cellspacing="0" cellpadding="0">
                  <tr>
                    <td><img src="../images/tab_left.gif" width="6" height="22"></td>
                    <td nowrap background="../images/tab_midbak.gif">充值分析表</td>
                    <td><img src="../images/tab_right.gif" width="6" height="22"></td>
                  </tr>
                </table></td>
                <td valign="bottom" > 
                  <table width="100%" border="0" cellspacing="0" cellpadding="0" height="22">
                    <tr> 
                      <td></td>
                      <td width=1></td>
                    </tr>
                    <tr > 
                      <td bgcolor="#FFFFFF" colspan="2" height=1></td>
                    </tr>
                    <tr > 
                      <td height=3></td>
                      <td bgcolor="#848284"  height=3 width="1"></td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr> 
                <td rowspan="2" bgcolor="#FFFFFF" width="1"><img src="../images/spacer.gif" width="1" height="1"></td>
                <td valign="top" align="center"> 
                  <table width="95%" border="0" cellspacing="1" cellpadding="2">
							<tr>
								<td>
								<table width="100%" border="0" cellspacing="1" cellpadding="2">
									<tr>
									  <td>
											<select name="channel" id="channel">
												<option value="">联运渠道</option>
												<%
												DBPsRs channelRs = DBPool.getInst().pQueryS(TabStor.tab_channel);
												while(channelRs.next()) {
												%>
												<option value="<%=channelRs.getString("CODE")%>"><%=channelRs.getString("NAME")%>(<%=channelRs.getString("CODE")%>)</option>
												<%
												}
												%>
									    	</select>									 
开始日期：<input name="startDate" type="text" id="startDate" onClick="new Calendar().show(this)">
结束日期：<input name="endDate" type="text" id="endDate" onClick="new Calendar().show(this)">
<label><input name="statChannel" type="checkbox" id="statChannel" value="1">分渠道统计</label>
<input name="statBtn" type="submit" id="statBtn" value="统计">
									</td>
									</tr>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="1" class="tbbgc1">
                                  <tr class="listtopbgc" >                                    								   
                                    <td align="center" nowrap>日期</td>
                                    <td align="center" nowrap>渠道</td>
                                    <td align="center" nowrap>真实激活数</td>
                                    <td align="center" nowrap>真实注册数</td>
                                    <td align="center" nowrap>首充人数</td>
                                    <td align="center" nowrap>活跃人数</td>
                                    <td align="center" nowrap>充值人数</td>
                                    <td align="center" nowrap>充值总金额</td>
                                    <td align="center" nowrap>激活注册率</td>
                                    <td align="center" nowrap>付费率</td>
                                    <td align="center" nowrap>ARPPU值</td>
                                    <td align="center" nowrap>ARPDPU值</td>
                                    <td align="center" nowrap>充值环比增长</td>
                                  </tr>
                                 	<%
                                 	DecimalFormat df = new DecimalFormat("0.0");
                                 	int yesterday_charge = 0;
                                 	while(returnRs!=null && returnRs.next()){
                                 		String channelStr = "";
                                 		String serverStr = "";
                                 		if(statChannel != null){
channelStr = TabStor.getListVal(TabStor.tab_channel, "code='"+returnRs.getString("channel")+"'", "name")+"("+returnRs.getString("channel")+")";
                                 		}
                                 	%>
                                  <tr class="nrbgc1">
<td align="center" nowrap><%=MyTools.getDateStr(returnRs.getTime("log_date"))%></td>									 
<td align="center" nowrap><%=channelStr%></td>
<td align="center" nowrap><%=returnRs.getString("sum_day_real_equ")%></td>
<td align="center" nowrap><%=returnRs.getString("sum_day_real_reg")%></td>
<td align="center" nowrap><%=returnRs.getString("sum_first_infull_num")%></td>
<td align="center" nowrap><%=returnRs.getString("sum_active_num")%></td>
<td align="center" nowrap><%=returnRs.getString("sum_infull_user")%></td>
<td align="center" nowrap><%=returnRs.getString("sum_infull_num")%></td>
<td align="center" nowrap><%=df.format(100*returnRs.getDouble("sum_day_real_reg")/returnRs.getDouble("sum_day_real_equ"))+"%"%></td>
<td align="center" nowrap><%=df.format(100*returnRs.getDouble("sum_infull_user")/returnRs.getDouble("sum_active_num"))+"%"%></td>
<td align="center" nowrap><%=df.format(returnRs.getDouble("sum_infull_num")/returnRs.getDouble("sum_infull_user"))%></td>
<td align="center" nowrap><%=df.format(returnRs.getDouble("sum_infull_num")/returnRs.getDouble("sum_active_num"))%></td>
<td align="center" nowrap><%=yesterday_charge>0?(df.format(100*(returnRs.getDouble("sum_infull_num")-yesterday_charge)/yesterday_charge)+"%"):"-"%></td>
                                  </tr>
								  	<%
								  	yesterday_charge = returnRs.getInt("sum_infull_num");
								  	}
								  	%>
                                </table>
                                
                                <table width="100%">
								<tr>
								<td>
								注：	<br>
								总真实注册率=总真实注册/总真实激活<br>
								日Arppu值=总充值金额/每日充值人数总和	<br>
								总Arppu值=总充值金额/充值总人数（去重）<br>
								日付费率=每日充值人数/日活跃用户 <br>
								总付费率=充值总人数（去重）/总真实注册数<br>	
								账户渠道单个用户收益=总充值金额/总注册人数。<br>	
								充值环比增长=（当日充值-昨日充值）/昨日充值<br>
								</td></tr>
								</table>
						</td>						
					</tr>
				</table>			
				
					
				
                </td>
                <td rowspan="2" bgcolor="#848284" width="1"><img src="../images/spacer.gif" width="1" height="1"></td>
              </tr>
              <tr> 
                <td bgcolor="#848284" height="1"></td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
</form>
<script>
autoChoose(allValue);
</script>
<iframe name=hiddenFrame width=0 height=0></iframe>
</body>
</html>
