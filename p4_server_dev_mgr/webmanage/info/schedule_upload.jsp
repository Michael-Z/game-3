<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="inc_import.jsp"%>
<%@ include file="../system/inc_getuser.jsp"%>

<%
String model="数据维护";
String perm="计划任务";
%>
<%@ include file="../system/inc_checkperm.jsp"%>


<script src="../js/common.js"></script>

<%

ScheduleBAC scheduleBAC = ScheduleBAC.getInstance();
ReturnValue rv = scheduleBAC.save(pageContext);
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
