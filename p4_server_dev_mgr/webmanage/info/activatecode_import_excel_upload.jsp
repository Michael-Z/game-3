<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="inc_import.jsp"%>
<%@ include file="../system/inc_getuser.jsp"%>

<%
String model="游戏管理";
String perm="激活码";
%>
<%@ include file="../system/inc_checkperm.jsp"%>


<script src="../js/common.js"></script>

<%

ActivateCodeBAC activateCodeBAC = new ActivateCodeBAC();
ReturnValue rv = activateCodeBAC.importChannelPhoneExcel(pageContext);
%>
<%if(rv.success){%>
<script>
wait_end(parent);
alert("<%=rv.info%>");
parent.opener.checkForm();
top.close();
</script>
<%}else{%>
<script>
wait_end(parent);
alert("<%=rv.info%>");
</script>
<%}%>

