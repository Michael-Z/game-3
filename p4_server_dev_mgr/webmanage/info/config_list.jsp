<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="inc_import.jsp"%>
<%@ include file="../system/inc_getuser.jsp"%>
<%
String model="基本数据";
String perm="参数设置";
%>
<%@ include file="../system/inc_checkperm.jsp"%>

<script src="../js/meizzDate.js"></script>
<html>
<head>
<title></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta HTTP-EQUIV="expires" CONTENT="Tue, 1 Jan 1980 00:00 GMT">
<script src="../js/common.js"></script>

<script>
function del(id)
{
	if(confirm("确认删除吗？"))
	{
		wait();		
		document.getElementById("hiddenFrame").src="config_del.jsp?id=" + id;
	}
}
function modify(id)
{
openWindow("config_edit.jsp?id=" + id,"modify",500,500,true,true);
}
function add()
{
openWindow("config_edit.jsp","add",500,500,true,true);
}
</script>

<link href="../css/style1.css" rel="stylesheet" type="text/css">
</head>

<body bgcolor="#EFEFEF">

<form name="form1" method="post">
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
                    <td nowrap background="../images/tab_midbak.gif">参数设置</td>
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
								<table width="100%" border="0" cellpadding="0" cellspacing="0">
                                  <tr>
                                    <td align="right">
									
									<table border="0" cellspacing="1" cellpadding="2">
									<tr>
										<td align="right">&nbsp;
										
									  	</td>
									
										<td align="right">&nbsp;
										
									  	</td>
									
										<td align="right">&nbsp;
										
									  	</td>
									</tr>
								</table>
									
									</td>
                                  </tr>
                                </table>
								<table width="100%" border="0" cellpadding="2" cellspacing="1"
									class="tbbgc1">
									<tr class="listtopbgc">
										<td width="10" align="center" nowrap>No.</td>
										<td align="center" nowrap>参数名</td>
<td align="center" nowrap>参数值</td>
<td align="center" nowrap>描述</td>
									
										<td align="center" nowrap>操作</td>
									</tr>
									
									<%
																			DBPsRs confRs = DBPool.getInst().pQueryS(ConfigBAC.tb_config);
																									while(confRs.next()) {
																										String bgcolor = "#EFEFEF";
																										String value = confRs.getString("value");
																										if(value!=null && value.length() > 30) {
																											value = value.substring(0,30)+"...";
																										}
																		%>
									<tr bgcolor="<%=bgcolor %>">
										<td align="center" nowrap><%=confRs.getRow()%></td>
										<td align="center" nowrap><%=confRs.getString("name")%></td>
										<td align="center" nowrap><%=value%></td>
										<td align="center" nowrap><%=confRs.getString("memo")%></td>
										
										<td align="center" nowrap>
										<img
											src="../images/icon_modify.gif" alt="修改"
											align="absmiddle" style="cursor: hand"
											onClick="modify(<%=confRs.getInt("id")%>)">
										<img
											src="../images/icon_del2.gif" alt="删除" align="absmiddle"
											style="cursor: hand" onClick="del(<%=confRs.getInt("id")%>)">
										</td>
									</tr>
									<%
									}
									%>
								</table>
								<br>
								<table width="100%" border="0" cellspacing="1" cellpadding="2">
									<tr>
										<td align="right">
										<table width="50" border="0" cellspacing="0" cellpadding="2">
											<tr>
												<td height="21" nowrap class="btntd" style="cursor: hand"
													onMouseDown="this.className='btntd_mousedown'"
													onMouseUp="this.className='btntd'"
													onMouseOut="this.className='btntd'"
													onselectstart="return false" onClick="add()"><img
													src="../images/icon_adddepart.gif" width="16" height="16"
													align="absmiddle"> 添加</td>	
											</tr>
								</table>
								</td>
							</tr>
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
<iframe id="hiddenFrame" name="hiddenFrame" width=10 height=10></iframe>
</body>
</html>
