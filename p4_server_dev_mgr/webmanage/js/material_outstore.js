
	//��Χ�����ò���
	//����һ��������
	//iLine : ��ǰ����һ������λ��,-1��ʾ����������
	function insertOutstoreMaterial(iLine)
	{
		var tbobj=document.all["outstorematerialtable"];
		
		if(iLine==-1){var trobj=tbobj.insertRow();}
		else{var trobj=tbobj.insertRow(iLine);}
		trobj.className="nrbgc1";
		n=trobj.rowIndex-1;
		
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<img src="../images/icon_mt_undifine.gif" id="mtflag" title="��δ��д����">';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="isdevice" type="hidden" value="no"><input name="dv_code" type="hidden" value=""><input name="dv_code_old" type="hidden" value=""><input name="sc_id" type="text" value="" class="input4" id="sc_id" size="10" onChange="checkOutstoreSC(this)"  must="true"  validator="M8">';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="sc_alias" type="text" value="" class="input4" id="sc_alias" size="8" onChange="checkOutstoreSC(this)">';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="sc_name" type="text" value="" class="input4" id="sc_name" size="15" readonly >';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="sc_type" type="text" value="" class="input4" id="sc_type" size="10" readonly >';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="sc_unit" type="text" value="" class="input4" id="sc_unit" size="6" readonly >';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="ot_quantity" type="text" value="" class="input4" id="ot_quantity" size="5" onBlur="checkOutstoreQuantity(this)" must="true" validator="PInt">';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="se_quantity" type="text" value="" class="input4" id="se_quantity" size="5" readonly >';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="ot_unitprice" type="text" value="" class="input4" id="ot_unitprice" size="8" readonly >';
		tdobj=trobj.insertCell();
		tdobj.align="center";
		tdobj.innerHTML='<img src="../images/icon_adddepart.gif" style="cursor:hand" alt="�ڵ�ǰλ�ò���1���հ׼�¼" border="0" onclick=insertOutstoreMaterial(this.parentElement.parentElement.rowIndex)>&nbsp;<img src="../images/icon_sub2.gif" style="cursor:hand" alt="ɾ����ǰ��¼" border="0" onclick=deleteOutstoreMaterial(this.parentElement.parentElement.rowIndex)>';
	}


	//��Χ�����ò���
	//ɾ��ָ����ǵ�һ��������
	//iLine : ��ǰ��Ҫɾ��һ������λ��,-1��ʾɾ��������һ��
	function deleteOutstoreMaterial(iLine)
	{
		var tbobj=document.all["outstorematerialtable"];

		if(iLine==-1)
		{
			if(tbobj.rows.length>1){tbobj.deleteRow(tbobj.rows.length-1);}
		}
		else
		{
			tbobj.deleteRow(iLine);
		}
	}


	//��Χ�����ò���	
	//������õ��������Ƿ���ȷ
	function checkOutstoreQuantity(obj)
	{
             var strdv_code="";
             var strdv_code_old="";
             
             if(obj.value==""){return;}
             var iMaxNum = obj.value;

      	    var rowCount = obj.parentElement.parentElement.parentElement.parentElement.rows.length-1
      	    var rowNum=obj.parentElement.parentElement.rowIndex-1;

      	    var strRowNum = "";
      	    if(rowCount>1){strRowNum = "[" + rowNum + "]";}
      	    
   	       eval("var isDevice=document.forms[0].isdevice" + strRowNum + ".value");
   	       eval("var sc_id=document.forms[0].sc_id" + strRowNum + ".value");
   	       eval("var se_quantity = document.forms[0].se_quantity" + strRowNum + ".value");
      	    
             if(sc_id==""){alert("����������Ϸ����!");obj.value="";return;}   
             if(se_quantity!=""){se_quantity = parseInt(se_quantity);}
             
             if(isType(obj)!=true){alert("����ֵ��ʽ������������ڵ���0������!");obj.focus();return;}
             else
             {
                if(obj.value==0)
                {
                  if(isDevice=="yes")
                  {
               	    eval("document.forms[0].dv_code" + strRowNum + ".value=\"\"");
               	    eval("document.all.mtflag" + strRowNum + ".src=\"../images/icon_mt_device.gif\"");
               	    eval("document.all.mtflag" + strRowNum + ".title=\"�豸����,��δѡ���豸���\"");
                  }
                  return;
                }
                
                //�������Ϊ0
               if(se_quantity==0 && obj.value!=0){alert("�������Ϊ0��������������Ϊ0��");obj.value="0";obj.focus();return;}
               //�ж���������������Ƿ񳬹��˿������
               if(obj.value>se_quantity){alert("�������������˿����������������д����������");obj.focus();return;}
               
               //���õ�������豸������������´���
               if(isDevice=="yes")
               {
            	   eval("strdv_code=document.forms[0].dv_code" + strRowNum + ".value");
            	   eval("strdv_code_old=document.forms[0].dv_code_old" + strRowNum + ".value");
                  createImportForm(obj,1,'/fdms/common/importform_dvcode_outstore.jsp','ѡ���豸',500,360,'yes','form1','dv_code',1,',',document.forms[0].se_id_src.value+','+sc_id,strdv_code,',',1,strdv_code_old,',',iMaxNum,'');
               }
             }             
	}
	
	
	//��Χ�����ò���	
	//�����Ϸ����
	function checkOutstoreSC(obj)
	{
		var parameter=escape(obj.value);
		var rowCount = obj.parentElement.parentElement.parentElement.parentElement.rows.length-1
		var rowNum=obj.parentElement.parentElement.rowIndex-1;
		
		var param = document.forms[0].se_id_src.value;

		if(obj.value!="" && (param=="" || param==null)){alert("������дԤ���ң�");obj.value = "";return false;}
		
		if(rowCount>1){
			for(i=0; i<rowCount; i++){
				if(rowNum!=i && eval("document.forms[0]." + obj.name + "[" + i + "].value")!="" && eval("document.forms[0]." + obj.name + "[" + i + "].value")==obj.value){
					alert("�ò����Ѿ�ѡ����ѡ���������ϣ�");
					document.forms[0].sc_id[rowNum].value = "";
					document.forms[0].sc_alias[rowNum].value = "";
					document.forms[0].sc_name[rowNum].value = "";
					document.forms[0].sc_type[rowNum].value = "";
					document.forms[0].sc_unit[rowNum].value = "";
					document.forms[0].isdevice[rowNum].value = "no";
					document.forms[0].dv_code[rowNum].value = "";
					document.forms[0].ot_quantity[rowNum].value = "";
					document.forms[0].se_quantity[rowNum].value = "";
					document.forms[0].ot_unitprice[rowNum].value = "";
					document.all.mtflag[rowNum].src = "../images/icon_mt_undifine.gif";
					document.all.mtflag[rowNum].title = "��δ��д����";
					return false;
				}
			}
		}	
		document.frames["getSC"].location.replace("/fdms/common/frameform_stuff.jsp?ctype=2&param="+ param +"&"+obj.name+"="+parameter+"&rowCount="+rowCount+"&rowNum="+rowNum);
	}		
	
	
	//��Χ�����ò���	
	//������õ��������Ƿ���ȷ
	function checkOutstoreQuantityByCourse(obj,iLine)
	{
             var strdv_code="";
             var strdv_code_old="";
             var iSingle = true;
             var deviceObj = document.forms[0].isdevice;
             if(deviceObj!=null){if(deviceObj.length!=null){iSingle = false;}}
             
             if(obj.value==""){return;}
             var iMaxNum = obj.value;

      	    var strRowNum = "";
      	    if(!iSingle){strRowNum = "[" + iLine + "]";}

   	       eval("var isDevice=document.forms[0].isdevice" + strRowNum + ".value");
   	       eval("var sc_id=document.forms[0].sc_id" + strRowNum + ".value");
   	       eval("var se_quantity = document.forms[0].se_quantity" + strRowNum + ".value");

             if(se_quantity!=""){se_quantity = parseInt(se_quantity);}
             
             if(isType(obj)!=true){alert("����ֵ��ʽ������������ڵ���0������!");obj.focus();return;}
             else
             {
                if(obj.value==0)
                {
                  if(isDevice=="yes")
                  {
               	    eval("document.forms[0].dv_code" + strRowNum + ".value=\"\"");
               	    eval("document.all.mtflag" + strRowNum + ".src=\"../images/icon_mt_device.gif\"");
               	    eval("document.all.mtflag" + strRowNum + ".title=\"�豸����,��δѡ���豸���\"");
                  }
                  return;
                }
                
                //�������Ϊ0
               if(se_quantity==0 && obj.value!=0){alert("�������Ϊ0��������������Ϊ0��");obj.value="0";obj.focus();return;}

               //�ж���������������Ƿ񳬹��˿������
               if(obj.value>se_quantity){alert("�������������˿����������������д����������");obj.focus();return;}
               
               //���õ�������豸������������´���
               if(isDevice=="yes")
               {
            	   eval("strdv_code=document.forms[0].dv_code" + strRowNum + ".value");
            	   eval("strdv_code_old=document.forms[0].dv_code_old" + strRowNum + ".value");
                  createImportForm(obj,-1,'/fdms/common/importform_dvcode_outstore.jsp','ѡ���豸',500,360,'yes','form1','dv_code',1,',',document.forms[0].se_id_src.value+','+sc_id,strdv_code,',',1,strdv_code_old,',',iMaxNum,strRowNum);
               }
             }             
	}	