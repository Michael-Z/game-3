




	//��Χ���黹����
	//����һ��������
	//iLine : ��ǰ����һ������λ��,-1��ʾ����������
	function insertRestoreMaterial(iLine,iRestoreType)
	{
		var tbobj=document.all["restorematerialtable"];
		
		if(iLine==-1){var trobj=tbobj.insertRow();}
		else{var trobj=tbobj.insertRow(iLine);}
		trobj.className="nrbgc1";
		n=trobj.rowIndex-1;
		
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<img src="../images/icon_mt_undifine.gif" id="mtflag" title="��δ��д����">';
		
		if(iRestoreType==1){
   		tdobj=trobj.insertCell();
   		tdobj.align="left";
   		tdobj.innerHTML='<input name="isdevice" type="hidden" value="no"><input name="dv_code" type="hidden" value=""><input name="sc_id" type="text" value="" class="input4" id="sc_id" size="18" onBlur="checkRestoreSC(this,document.forms[0].se_id_src.value)" must="true">';
   		tdobj=trobj.insertCell();
   		tdobj.align="left";
   		tdobj.innerHTML='<input name="sc_alias" type="text" value="" class="input4" id="sc_alias" size="15" onBlur="checkRestoreSC(this,document.forms[0].se_id_src.value)" must="true">';
		
		}else{
   		tdobj=trobj.insertCell();
   		tdobj.align="left";
   		tdobj.innerHTML='<input name="isdevice" type="hidden" value="no"><input name="dv_code" type="hidden" value=""><input name="sc_id" type="text" value="" class="input4" id="sc_id" size="18" onBlur="checkRestoreSC(this,document.forms[0].om_outstore_ids.value)" must="true">';
   		tdobj=trobj.insertCell();
   		tdobj.align="left";
   		tdobj.innerHTML='<input name="sc_alias" type="text" value="" class="input4" id="sc_alias" size="15" onBlur="checkRestoreSC(this,document.forms[0].om_outstore_ids.value)" must="true">';
	   }
		
		
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="sc_name" type="text" value="" class="input4" id="sc_name" size="15" readonly >';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="sc_type" type="text" value="" class="input4" id="sc_type" size="10" readonly >';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="rt_quantity" type="text" value="" class="input4" id="rt_quantity" size="5" validator="Dn" onBlur="checkRestoreQuantity(this)">';
		tdobj=trobj.insertCell();
		tdobj.align="center";
		tdobj.innerHTML='<img src="../images/icon_adddepart.gif" style="cursor:hand" alt="�ڵ�ǰλ�ò���1���հ׼�¼" border="0" onclick=insertRestoreMaterial(this.parentElement.parentElement.rowIndex)>&nbsp;<img src="../images/icon_sub2.gif" style="cursor:hand" alt="ɾ����ǰ��¼" border="0" onclick=deleteRestoreMaterial(this.parentElement.parentElement.rowIndex)>';
	}


	//��Χ���黹����
	//ɾ��ָ����ǵ�һ��������
	//iLine : ��ǰ��Ҫɾ��һ������λ��,-1��ʾɾ��������һ��
	function deleteRestoreMaterial(iLine)
	{
		var tbobj=document.all["restorematerialtable"];

		if(iLine==-1)
		{
			if(tbobj.rows.length>1){tbobj.deleteRow(tbobj.rows.length-1);}
		}
		else
		{
			tbobj.deleteRow(iLine);
		}
	}



	//��Χ���𻵲���
	//����һ��������
	//iLine : ��ǰ����һ������λ��,-1��ʾ����������
	function insertWastageMaterial(iLine)
	{
		var tbobj=document.all["wastagematerialtable"];
		
		if(iLine==-1){var trobj=tbobj.insertRow();}
		else{var trobj=tbobj.insertRow(iLine);}
		trobj.className="nrbgc1";
		n=trobj.rowIndex-1;
		
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<img src="../images/icon_mt_undifine.gif" id="mtflag" title="��δ��д����">';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="isdevice" type="hidden" value="no"><input name="dv_code" type="hidden" value=""><input name="sc_id" type="text" value="" class="input4" id="sc_id" size="18" onBlur="checkWastageSC(this,document.forms[0].se_id_src.value)" must="true">';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="sc_alias" type="text" value="" class="input4" id="sc_alias" size="15" onBlur="checkWastageSC(this,document.forms[0].se_id_src.value)" must="true">';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="sc_name" type="text" value="" class="input4" id="sc_name" size="15" readonly >';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="sc_type" type="text" value="" class="input4" id="sc_type" size="10" readonly >';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="wt_quantity" type="text" value="" class="input4" id="wt_quantity" size="5" validator="Dn" onBlur="checkWastageQuantity(this)">';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="wt_unitprice" type="text" value="" class="input4" id="wt_unitprice" size="8" readonly >';

		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="wt_amount" type="text" value="" class="input4" id="wt_amount" size="8" >';

		

		tdobj=trobj.insertCell();
		tdobj.align="center";
		tdobj.innerHTML='<img src="../images/icon_adddepart.gif" style="cursor:hand" alt="�ڵ�ǰλ�ò���1���հ׼�¼" border="0" onclick=insertWastageMaterial(this.parentElement.parentElement.rowIndex)>&nbsp;<img src="../images/icon_sub2.gif" style="cursor:hand" alt="ɾ����ǰ��¼" border="0" onclick=deleteWastageMaterial(this.parentElement.parentElement.rowIndex)>';
	}


	//��Χ���𻵲���
	//ɾ��ָ����ǵ�һ��������
	//iLine : ��ǰ��Ҫɾ��һ������λ��,-1��ʾɾ��������һ��
	function deleteWastageMaterial(iLine)
	{
		var tbobj=document.all["wastagematerialtable"];

		if(iLine==-1)
		{
			if(tbobj.rows.length>1){tbobj.deleteRow(tbobj.rows.length-1);}
		}
		else
		{
			tbobj.deleteRow(iLine);
		}
	}

	//�����������Ƿ���ȷ
	function checkQuantity(obj,ctype,retype)
	{
		switch(ctype){//SWITCH1 BEGIN
		   
		   //�������
		   case 1:  //CASE1 BEGIN
             if(obj.value==""){return;}
      
      	    var rowCount = obj.parentElement.parentElement.parentElement.parentElement.rows.length-1
      	    var rowNum=obj.parentElement.parentElement.rowIndex-1;
      	    var isDevice = "";
      	    var sc_id = "";
      	    
      	    if(rowCount>1)
      	    {
      	      isDevice=document.forms[0].isdevice[rowNum].value;
      	      sc_id=document.forms[0].sc_id[rowNum].value;
      	    }
      	    else
      	    {
      	      isDevice=document.forms[0].isdevice.value;
      	      sc_id=document.forms[0].sc_id.value;
      	    }
      	    if(sc_id==""){alert("����������Ϸ����!");obj.value="";return;}
             
             if(isType(obj)!=true){alert("����ֵ��ʽ�������������0������!");obj.focus();return;}
             else
             {
               if(isDevice=="yes")
               {
                  alert("�����"+sc_id+"�Ĳ������豸��������д�����Ӧ���豸��ϸ����");
                  insertMaterialDataTable(obj.value,sc_id);
               }
             }
      		break;//CASE1 END		   
		   
		   //���ò���
		   case 2:  //CASE2 BEGIN
             if(obj.value==""){return;}

      	    var rowCount = obj.parentElement.parentElement.parentElement.parentElement.rows.length-1
      	    var rowNum=obj.parentElement.parentElement.rowIndex-1;
      	    var isDevice = "";
      	    var sc_id = "";
      	    var se_quantity = "";
      	    
      	    if(rowCount>1)
      	    {
      	      isDevice=document.forms[0].isdevice[rowNum].value;
      	      sc_id=document.forms[0].sc_id[rowNum].value;
      	      se_quantity = document.forms[0].se_quantity[rowNum].value;
      	    }
      	    else
      	    {
      	      isDevice=document.forms[0].isdevice.value;
      	      sc_id=document.forms[0].sc_id.value;
      	      se_quantity = document.forms[0].se_quantity.value;
      	    }
             if(sc_id==""){alert("����������Ϸ����!");obj.value="";return;}   
             if(se_quantity!=""){se_quantity = parseInt(se_quantity);}
             
             if(isType(obj)!=true){alert("����ֵ��ʽ������������ڵ���0������!");obj.focus();return;}
             else
             {
                if(obj.value==0){
                  if(isDevice=="yes"){
               	    if(rowCount>1)
               	    {
         					document.forms[0].dv_code[rowNum].value = "";
         					document.all.mtflag[rowNum].src = "../images/icon_mt_device.gif";
         					document.all.mtflag[rowNum].title = "�豸����,��δѡ���豸���";
               	    }
               	    else
               	    {
         					document.forms[0].dv_code.value = "";
         					document.all.mtflag.src = "../images/icon_mt_device.gif";
         					document.all.mtflag.title = "�豸����,��δѡ���豸���";
               	    }
                  }
                  return;
                }
                
                //�������Ϊ0
                if(se_quantity==0 && obj.value!=0){
                  alert("�������Ϊ0��������������Ϊ0��");
                  obj.value="0";
                  obj.focus();return;
                }

               //�ж���������������Ƿ񳬹��˿������
               if(obj.value>se_quantity){
                  alert("�������������˿����������������д����������");
                  obj.focus();return;
               }
               
               //���õ�������豸������������´���
               if(isDevice=="yes")
               {
                  alert("��ѡ�������豸��ţ�");
                  createImportForm(obj,1,'/fdms/common/importform_dvcode.jsp','ѡ���豸',500,360,'yes','form1','dv_code',1,',',document.forms[0].se_id_src.value+','+sc_id+',1,1,' + retype);
               }
             }       
             break;//CASE2 END


		   //�黹����
		   case 3:  //CASE3 BEGIN
             if(obj.value==""){return;}

      	    var rowCount = obj.parentElement.parentElement.parentElement.parentElement.rows.length-1
      	    var rowNum=obj.parentElement.parentElement.rowIndex-1;
      	    var isDevice = "";
      	    var sc_id = "";
      	    var se_quantity = "";
      	    
      	    if(rowCount>1)
      	    {
      	      isDevice=document.forms[0].isdevice[rowNum].value;
      	      sc_id=document.forms[0].sc_id[rowNum].value;
      	    }
      	    else
      	    {
      	      isDevice=document.forms[0].isdevice.value;
      	      sc_id=document.forms[0].sc_id.value;
      	    }
             if(sc_id==""){alert("����������Ϸ����!");obj.value="";return;}   
             
             if(isType(obj)!=true){alert("����ֵ��ʽ������������ڵ���0������!");obj.focus();return;}
             else
             {
                if(obj.value==0){
                  if(isDevice=="yes"){
               	    if(rowCount>1)
               	    {
         					document.forms[0].dv_code[rowNum].value = "";
         					document.all.mtflag[rowNum].src = "../images/icon_mt_device.gif";
         					document.all.mtflag[rowNum].title = "�豸����,��δѡ���豸���";
               	    }
               	    else
               	    {
         					document.forms[0].dv_code.value = "";
         					document.all.mtflag.src = "../images/icon_mt_device.gif";
         					document.all.mtflag.title = "�豸����,��δѡ���豸���";
               	    }
                  }
                  return;
                }
                
               
               //�黹��������豸������������´���
               if(isDevice=="yes")
               {
                  alert("��ѡ�������豸��ţ�");
                  if(retype==1){
                     createImportForm(obj,1,'/fdms/common/importform_dvcode.jsp','ѡ���豸',500,360,'yes','form1','dv_code',1,',',document.forms[0].se_id_src.value+','+sc_id+',1,3,'+retype);
                  }else{
                     createImportForm(obj,1,'/fdms/common/importform_dvcode.jsp','ѡ���豸',500,360,'yes','form1','dv_code',1,',',document.forms[0].om_outstore_ids.value+','+sc_id+',1,3,'+retype);
                  }
               }
             }       
             break;//CASE3 END   


		   //�𻵲���
		   case 4:  //CASE4 BEGIN
             if(obj.value==""){return;}

      	    var rowCount = obj.parentElement.parentElement.parentElement.parentElement.rows.length-1
      	    var rowNum=obj.parentElement.parentElement.rowIndex-1;
      	    var isDevice = "";
      	    var sc_id = "";
      	    var wt_unitprice,wt_amount;
      	    
      	    if(rowCount>1)
      	    {
      	      isDevice=document.forms[0].isdevice[rowNum].value;
      	      sc_id=document.forms[0].sc_id[rowNum].value;
      	      wt_unitprice=document.forms[0].wt_unitprice[rowNum].value;
      	      
      	    }
      	    else
      	    {
      	      isDevice=document.forms[0].isdevice.value;
      	      sc_id=document.forms[0].sc_id.value;
      	      wt_unitprice=document.forms[0].wt_unitprice.value;
      	    }
             if(sc_id==""){alert("����������Ϸ����!");obj.value="";return;}   
             
             if(isType(obj)!=true){alert("����ֵ��ʽ������������ڵ���0������!");obj.focus();return;}
             else
             {
                if(obj.value==0){
                  if(isDevice=="yes"){
               	    if(rowCount>1)
               	    {
         					document.forms[0].dv_code[rowNum].value = "";
         					document.all.mtflag[rowNum].src = "../images/icon_mt_device.gif";
         					document.all.mtflag[rowNum].title = "�豸����,��δѡ���豸���";
               	    }
               	    else
               	    {
         					document.forms[0].dv_code.value = "";
         					document.all.mtflag.src = "../images/icon_mt_device.gif";
         					document.all.mtflag.title = "�豸����,��δѡ���豸���";
               	    }
                  }
                  return;
                }else{
                     wt_amount = wt_unitprice*1*obj.value;
               	    if(rowCount>1)
               	    {
         					document.forms[0].wt_amount[rowNum].value = wt_amount;
               	    }
               	    else
               	    {
         					document.forms[0].wt_amount.value = wt_amount;
               	    }                     
                }
               
               //���õ�������豸������������´���
               if(isDevice=="yes")
               {
                  alert("��ѡ�������豸��ţ�");
                  createImportForm(obj,1,'/fdms/common/importform_dvcode.jsp','ѡ���豸',500,360,'yes','form1','dv_code',1,',',document.forms[0].se_id_src.value+','+sc_id+',1,1,' + retype);
               }
             }       
             break;//CASE4 END
                       
      }//SWITCH1 END
	}	 

	
	

		

	//��Χ���黹����	
	//������õ��������Ƿ���ȷ
	function checkRestoreQuantity(obj,retype){
      checkQuantity(obj,3,retype);
	}	

	//��Χ���𻵲���	
	//����𻵵��������Ƿ���ȷ
	function checkWastageQuantity(obj){
      checkQuantity(obj,4,0);
	}
	
	//�����Ϸ����
	function checkSC(obj,ctype,param,retype){
		var parameter=escape(obj.value);
		var rowCount = obj.parentElement.parentElement.parentElement.parentElement.rows.length-1
		var rowNum=obj.parentElement.parentElement.rowIndex-1;
		
		switch(ctype){
		   
		   //�������
		   case 1:
      		if(rowCount>1){
      			for(i=0; i<rowCount; i++){
      				if(rowNum!=i && eval("document.forms[0]." + obj.name + "[" + i + "].value")!="" && eval("document.forms[0]." + obj.name + "[" + i + "].value")==obj.value){
      					alert("�ò����Ѿ�ѡ����ѡ���������ϣ�");
      					document.forms[0].sc_id[rowNum].value = "";
      					document.forms[0].sc_alias[rowNum].value = "";
      					document.forms[0].sc_name[rowNum].value = "";
      					document.forms[0].sc_type[rowNum].value = "";
      					return false;
      				}
      			}
      		}
      		break;		   
		   
		   //���ò���
		   case 2:
      		
      		if(obj.value!="" && (param=="" || param==null)){
      		   alert("������дԴ�ֿ⣡");
      		   obj.value = "";
      		   return false;
      		}
      		
      		if(rowCount>1){
      			for(i=0; i<rowCount; i++){
      				if(rowNum!=i && eval("document.forms[0]." + obj.name + "[" + i + "].value")!="" && eval("document.forms[0]." + obj.name + "[" + i + "].value")==obj.value){
      					alert("�ò����Ѿ�ѡ����ѡ���������ϣ�");
      					document.forms[0].sc_id[rowNum].value = "";
      					document.forms[0].sc_alias[rowNum].value = "";
      					document.forms[0].sc_name[rowNum].value = "";
      					document.forms[0].sc_type[rowNum].value = "";
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
      		break;  


		   //�黹����
		   case 3:
      		
      		if(retype==1){
         		if(obj.value!="" && (param=="" || param==null)){
         		   alert("������дԴ�ֿ⣡");
         		   obj.value = "";
         		   return false;
         		}
      		}

      		if(retype==2){
         		if(obj.value!="" && (param=="" || param==null)){
         		   alert("������д�����ˣ�");
         		   obj.value = "";
         		   return false;
         		}
      		}
      		
      		if(rowCount>1){
      			for(i=0; i<rowCount; i++){
      				if(rowNum!=i && eval("document.forms[0]." + obj.name + "[" + i + "].value")!="" && eval("document.forms[0]." + obj.name + "[" + i + "].value")==obj.value){
      					alert("�ò����Ѿ�ѡ����ѡ���������ϣ�");
      					document.forms[0].sc_id[rowNum].value = "";
      					document.forms[0].sc_alias[rowNum].value = "";
      					document.forms[0].sc_name[rowNum].value = "";
      					document.forms[0].sc_type[rowNum].value = "";
      					document.forms[0].isdevice[rowNum].value = "no";
      					document.forms[0].dv_code[rowNum].value = "";
      					document.forms[0].rt_quantity[rowNum].value = "";
      					document.all.mtflag[rowNum].src = "../images/icon_mt_undifine.gif";
      					document.all.mtflag[rowNum].title = "��δ��д����";
      					return false;
      				}
      			}
      		}		 
      		break;
      		

		   //�𻵲���
		   case 4:
      		
      		if(obj.value!="" && (param=="" || param==null)){
      		   alert("������дԴ�ֿ⣡");
      		   obj.value = "";
      		   return false;
      		}
      		
      		if(rowCount>1){
      			for(i=0; i<rowCount; i++){
      				if(rowNum!=i && eval("document.forms[0]." + obj.name + "[" + i + "].value")!="" && eval("document.forms[0]." + obj.name + "[" + i + "].value")==obj.value){
      					alert("�ò����Ѿ�ѡ����ѡ���������ϣ�");
      					document.forms[0].sc_id[rowNum].value = "";
      					document.forms[0].sc_alias[rowNum].value = "";
      					document.forms[0].sc_name[rowNum].value = "";
      					document.forms[0].sc_type[rowNum].value = "";
      					document.forms[0].isdevice[rowNum].value = "no";
      					document.forms[0].dv_code[rowNum].value = "";
      					document.forms[0].wt_quantity[rowNum].value = "";
      					document.forms[0].wt_unitprice[rowNum].value = "";
      					document.all.mtflag[rowNum].src = "../images/icon_mt_undifine.gif";
      					document.all.mtflag[rowNum].title = "��δ��д����";
      					return false;
      				}
      			}
      		}		 
      		break;        		 		   
		}
		document.frames["getSC"].location.replace("/fdms/common/frameform_stuff.jsp?retype="+retype+"&param="+param+"&ctype="+ctype+"&"+obj.name+"="+parameter+"&rowCount="+rowCount+"&rowNum="+rowNum);
	}
	
	

	//��Χ���黹����	
	//�����Ϸ����
	function checkRestoreSC(obj,param,retype){
      checkSC(obj,3,param,retype);
	}			
	
	//��Χ���𺦲���	
	//�����Ϸ����
	function checkWastageSC(obj,param){
      checkSC(obj,4,param,0);
	}	