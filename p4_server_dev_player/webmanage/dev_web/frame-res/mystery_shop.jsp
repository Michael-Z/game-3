<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="../inc_import.jsp"%>
<html>
<head>
<title></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<link href="../css/style1.css" rel="stylesheet" type="text/css">
<link href="../css/bubbletips.css" type="text/css" rel="stylesheet" />
<script type="text/javascript" src="http://www.jq-school.com/js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="../js/bubbletips.js"></script>
</head>
<body>

<form id="form" name="form" method="post" action="../dev_request.jsp" target="frame_result" enctype="multipart/form-data">
  <table width="1024" border="0">
	<tr>
      <td width="765">神秘商店</td>
    </tr>
    <tr>
      <td><input name="mystery_shop_get" type="submit" id="mystery_shop_get" value="获取商店数据"></td>
    </tr>
    <tr>
      <td><input name="mystery_shop_buy" type="submit" id="mystery_shop_buy" value="购买物品">
        物品下标
        ：
      <input name="mystery_shop_buy1" type="text" id="mystery_shop_buy1" size="10"></td>
    </tr>
	<tr>
      <td>&nbsp;</td>
    </tr>
	<tr>
      <td><font color="#FF0000">调试功能</font></td>
    </tr>
	<tr>
      <td><input name="debug_mystery_shop_add" type="submit" id="debug_mystery_shop_add" value="调试增加累计消耗体力">
数量
        ：
      <input name="debug_mystery_shop_add1" type="text" id="debug_mystery_shop_add1" size="10"></td>
    </tr>
	<tr>
      <td><input name="debug_mystery_shop_reset" type="submit" id="debug_mystery_shop_reset" value="调试重置结束时间"></td>
    </tr>
	<tr>
      <td>&nbsp;</td>
    </tr>
 </table>
</form>
</body>

</html>