<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="inc_import.jsp"%>
<%@ include file="../system/inc_getuser.jsp"%>

<%
String model="系统功能";
String perm="游戏服升级";
%>
<%@ include file="../system/inc_checkperm.jsp"%>


<script src="../js/common.js"></script>

<%
	ServerUpdateBAC gameServerUpdateBAC = ServerUpdateBAC.getInstance();
ReturnValue rv = gameServerUpdateBAC.update(pageContext);
%>
<%if(rv.success){%>
<script>
wait_end(parent);
alert("<%=rv.info%>");
parent.opener.document.location.reload();
//top.close();
</script>
<%}else{%>
<script>
wait_end(parent);
alert("<%=rv.info%>");
</script>
<%}%>
