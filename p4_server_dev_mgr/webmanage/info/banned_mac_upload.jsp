<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="inc_import.jsp"%>
<%@ include file="../system/inc_getuser.jsp"%>

<%
String model="客服管理";
String perm="mac禁言";
%>
<%@ include file="../system/inc_checkperm.jsp"%>


<script src="../js/common.js"></script>

<%

BannedMacBAC bannedMacBAC = BannedMacBAC.getInstance();
ReturnValue rv = bannedMacBAC.save(pageContext);
%>
<%if(rv.success){%>
<script>
wait_end(parent);
alert("<%=rv.info%>");
parent.opener.document.location.reload();
top.close();
</script>
<%}else{%>
<script>
wait_end(parent);
alert("<%=rv.info%>");
</script>
<%}%>
