
   //�������ѵ�ʹ�ý���ϵͳ�Զ�ͳ��
   function checkLock()
   {
      if(document.forms[0].lockmoney.checked==true)
      {
            document.forms[0].ac_money.readOnly = true;
            calcAmount();
      }
      else
      {
            document.forms[0].ac_money.readOnly = false;   
      }
   }



	//��Χ���������
	//����һ������������
	//iLine : ��ǰ����һ������λ��,-1��ʾ����������
	function insertInstoreMaterial(iLine)
	{
		var tbobj=document.all["instorematerialtable"];
		
		if(iLine==-1){var trobj=tbobj.insertRow();}
		else{var trobj=tbobj.insertRow(iLine);}
		trobj.className="nrbgc1";
		n=trobj.rowIndex-1;
		
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<img src="../images/icon_mt_undifine.gif" id="mtflag" title="��δ��д����">';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="isdevice" type="hidden" value="no"><input name="sc_id" type="text" value="" class="input4" id="sc_id" size="10" onChange="checkInstoreSC(this)" must="true"  validator="M8">';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="sc_alias" type="text" value="" class="input4" id="sc_alias" size="8" onChange="checkInstoreSC(this)">';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="sc_name" type="text" value="" class="input4" id="sc_name" size="15" readonly >';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="sc_type" type="text" value="" class="input4" id="sc_type" size="15" readonly >';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="sc_unit" type="text" value="" class="input4" id="sc_unit" size="6" readonly >';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="it_quantity" type="text" value="" class="input4" id="it_quantity" size="5" onBlur="checkInstoreQuantity(this)" must="true" validator="PInt">';
		tdobj=trobj.insertCell();
		tdobj.align="left";
		tdobj.innerHTML='<input name="it_unitprice" type="text" value="" class="input4" id="it_unitprice" size="8"  onBlur="checkInstorePrice(this)" must="true" validator="Num">';
		tdobj=trobj.insertCell();
		tdobj.align="center";
		tdobj.innerHTML='<input name="this_amount" type="hidden" value="0"><img src="../images/icon_adddepart.gif" style="cursor:hand" alt="�ڵ�ǰλ�ò���1���հ׼�¼" border="0" onclick=insertInstoreMaterial(this.parentElement.parentElement.rowIndex)>&nbsp;<img src="../images/icon_sub2.gif" style="cursor:hand" alt="ɾ����ǰ��¼" border="0" onclick="delDevice(this.parentElement.children[0]);deleteInstoreMaterial(this.parentElement.parentElement.rowIndex)">';
	}



	//��Χ���������
	//ɾ��ָ����ǵ�һ��������
	//iLine : ��ǰ��Ҫɾ��һ������λ��,-1��ʾɾ��������һ��
	function deleteInstoreMaterial(iLine)
	{
		var tbobj=document.all["instorematerialtable"];

		if(iLine==-1)
		{
			if(tbobj.rows.length>1){tbobj.deleteRow(tbobj.rows.length-1);}
		}
		else
		{
			tbobj.deleteRow(iLine);
		}
		
      //���¼����ܽ��
      if(document.forms[0].lockmoney.checked==true){calcAmount();}		
	}
	
	//��ɾ���豸����ʱ���豸��Ӧ����ϸ��Ҳ��ɾ��
	function delDevice(obj)
	{
       var isDevice = "";
       var sc_id = "";
       
       if(obj==-1)
       {
             var isDeviceObj=document.forms[0].isdevice;   
             var sc_idObj=document.forms[0].sc_id;
             if(isDeviceObj!=null)
             {
                  if(isDeviceObj.length!=null)
                  {
                     isDevice = isDeviceObj[isDeviceObj.length-1].value;
                     sc_id = sc_idObj[sc_idObj.length-1].value;
                  }
                  else
                  {
                      isDevice = isDeviceObj.value;  
                      sc_id = sc_idObj.value;  
                  }  
             }   
       }
       else
       {
             var rowCount = obj.parentElement.parentElement.parentElement.parentElement.rows.length-1
             var rowNum=obj.parentElement.parentElement.rowIndex-1;
      
      
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
         
       }
       
      if(isDevice=="yes")
      {
         insertMaterialDataTable(0,sc_id);
      }       
       
	}

	//��Χ���������
	//�����豸��ϸ��
	function insertMaterialDataTable(count,sc_id)
	{
		var strHTML = "";
		eval("var deleteObj = document.all.A" + sc_id);
		if(deleteObj!=null){deleteObj.outerHTML=strHTML;}
		
		if(count>=0)
		{
   		var insertObj=document.all.tableInsertId;
   		strHTML = '<span id="tableInsertId"></span>';
   		strHTML = strHTML + '<span id="A'+ sc_id +'">';
   		for(var i=0;i<count;i++)
   		{
   		   strHTML = strHTML + deviceItems(sc_id);
   		}
   		strHTML = strHTML + "</span>";
   		insertObj.outerHTML=strHTML;
		}
		
	}	

   var js_dv_code = "";
   var js_dv_spec = "";
   var js_dv_country = "";
   var js_dv_unitprice = "";
   var js_dv_credence = "";

   var js_dv_factory = "";
   var js_dv_factory_id = "";
   var js_dv_factory_date = "";
   var js_dv_maintainor = "";
   var js_dv_subject = "";

   var js_dv_useway = "";
   var js_dv_buydate = "";
   var js_dv_affix = "";   
       
   
   function copyIt(obj)
   {
         var allTrObj = obj.parentElement.parentElement.children[1].children[0].children;
         js_dv_code = allTrObj[0].children[1].children[0].value;
         js_dv_spec = allTrObj[0].children[3].children[0].value;
         js_dv_country = allTrObj[0].children[5].children[0].value;
         js_dv_unitprice = allTrObj[0].children[7].children[0].value;
         js_dv_credence = allTrObj[0].children[9].children[0].value;

         js_dv_factory = allTrObj[1].children[1].children[0].value;
         js_dv_factory_id = allTrObj[1].children[3].children[0].value;
         js_dv_factory_date = allTrObj[1].children[5].children[0].value;
         js_dv_maintainor = allTrObj[1].children[7].children[0].value;
         js_dv_subject = allTrObj[1].children[9].children[0].value;         

         js_dv_useway = allTrObj[2].children[1].children[0].value;
         js_dv_buydate = allTrObj[2].children[3].children[0].value;
         js_dv_affix = allTrObj[2].children[5].children[0].value;
   }

   function pasteIt(obj)
   {
         var allTrObj = obj.parentElement.parentElement.children[1].children[0].children;
         allTrObj[0].children[1].children[0].value = js_dv_code;
         allTrObj[0].children[3].children[0].value = js_dv_spec;
         allTrObj[0].children[5].children[0].value = js_dv_country;
         allTrObj[0].children[7].children[0].value = js_dv_unitprice;
         allTrObj[0].children[9].children[0].value = js_dv_credence;         

         allTrObj[1].children[1].children[0].value =js_dv_factory;
         allTrObj[1].children[3].children[0].value=js_dv_factory_id;
         allTrObj[1].children[5].children[0].value=js_dv_factory_date;
         allTrObj[1].children[7].children[0].value=js_dv_maintainor;
         allTrObj[1].children[9].children[0].value=js_dv_subject;         

         allTrObj[2].children[1].children[0].value=js_dv_useway;
         allTrObj[2].children[3].children[0].value=js_dv_buydate;
         allTrObj[2].children[5].children[0].value=js_dv_affix;         
   }
      
   
	
	//��Χ���������
	//����һ���豸��ϸ��
	//sc_id ���豸�Ĳ��Ϸ����
	function deviceItems(sc_id)
	{
		var strHTML = "";
		strHTML = strHTML
			+ '    <fieldset>'
			+ '      <legend>��<font color=red>' + sc_id + '</font> �豸��ϸ����<input name="copybtn" type="image" src="../images/icon_copy.gif" width="16" height="16" border="0" title="����" onclick="copyIt(this)"> &nbsp;<input name="pastebtn" type="image" src="../images/icon_paste.gif" width="16" height="16" border="0" title="ճ��" onclick="pasteIt(this)"></legend>'
			+ '      <table width="100%" border="0" cellspacing="1" cellpadding="2" class="tbbgc1">'
			+ '        <tr> '
			+ '          <td align="right" nowrap class="nrbgc1">�豸��ţ�</td>'
			+ '          <td class="nrbgc1"><input name="dv_code" type="text" class="input4" size="6"  must="true"  validator="M20"></td>'
			+ '          <td align="right" nowrap class="nrbgc1">���</td>'
			+ '          <td class="nrbgc1"><input name="dv_spec" type="text" class="input4" size="6" validator="M30"></td>'
			+ '          <td align="right" nowrap class="nrbgc1">������</td>'
			+ '          <td class="nrbgc1"><input name="dv_country" type="text" class="input4" size="6" validator="M30"></td>'
			+ '          <td align="right" nowrap class="nrbgc1">�豸���ۣ�</td>'
			+ '          <td class="nrbgc1"><input name="dv_unitprice" type="text" class="input4" value="" size="6" must="true" validator="Num"></td>'
			+ '          <td align="right" nowrap class="nrbgc1">����ƾ֤��</td>'
			+ '          <td class="nrbgc1"><input name="dv_credence" type="text" class="input4" size="6" validator="M20"></td>'
			+ '        </tr>'
			+ '        <tr> '
			+ '          <td align="right" nowrap class="nrbgc1">���ң�</td>'
			+ '          <td class="nrbgc1"><input name="dv_factory" type="text" class="input4" size="6" validator="M30"></td>'
			+ '          <td align="right" nowrap class="nrbgc1">�����ţ�</td>'
			+ '          <td class="nrbgc1"><input name="dv_factory_id" type="text" class="input4" size="6" validator="M20"></td>'
			+ '          <td align="right" nowrap class="nrbgc1">�������ڣ�</td>'
			+ '          <td class="nrbgc1"><input name="dv_factory_date" type="text" class="input4" size="6" validator="date"></td>'
			+ '          <td align="right" nowrap class="nrbgc1">�����ˣ�</td>'
			+ '          <td class="nrbgc1"><input name="dv_maintainor" type="text" class="input4" size="6" validator="M30"></td>'
			+ '          <td align="right" nowrap class="nrbgc1">���ѿ�Ŀ��</td>'
			+ '          <td class="nrbgc1"><input name="dv_subject" type="text" class="input4" size="6" validator="M20"></td>'
			+ '        </tr> '
			+ '        <tr> '
			+ '          <td align="right" nowrap class="nrbgc1">ʹ�÷���</td>'
			+ '          <td class="nrbgc1"><input name="dv_useway" type="text" class="input4" size="6" validator="M20"></td>'
			+ '          <td align="right" nowrap class="nrbgc1">�������ڣ�</td>'
			+ '          <td class="nrbgc1"><input name="dv_buydate" type="text" class="input4" size="6" validator="date"></td>'
			+ '          <td align="right" nowrap class="nrbgc1">������</td>'
			+ '          <td class="nrbgc1" colspan="5"><input name="dv_affix" type="text" class="input4" size="40" validator="M10"><input name="dv_sc_id" type="hidden" id="dv_sc_id" value="'+ sc_id +'"</td>'
			+ '        </tr> '              
			+ '      </table>'
			+ '      </fieldset>		';
		return(strHTML);
	} 	
	
	//��Χ���������	
	//�����Ϸ����
	function checkInstoreSC(obj){
		
		var parameter=escape(obj.value);
		var rowCount = obj.parentElement.parentElement.parentElement.parentElement.rows.length-1
		var rowNum=obj.parentElement.parentElement.rowIndex-1;
		if(rowCount>1){
			for(i=0; i<rowCount; i++){
				if(rowNum!=i && eval("document.forms[0]." + obj.name + "[" + i + "].value")!="" && eval("document.forms[0]." + obj.name + "[" + i + "].value")==obj.value){
					alert("�ò����Ѿ�ѡ����ѡ���������ϣ�");
					document.forms[0].sc_id[rowNum].value = "";
					document.forms[0].sc_alias[rowNum].value = "";
					document.forms[0].sc_name[rowNum].value = "";
					document.forms[0].sc_type[rowNum].value = "";
					document.forms[0].sc_unit[rowNum].value = "";
					return false;
				}
			}
		}

		document.frames["getSC"].location.replace("/fdms/common/frameform_stuff.jsp?ctype=1&"+obj.name+"="+parameter+"&rowCount="+rowCount+"&rowNum="+rowNum);
	}		

	//�Զ������ܽ��
	function calcAmount()
	{
       var total_price = 0;
       
       this_amountObj = document.forms[0].this_amount;
         
      if(this_amountObj!=null)
      {
          if(this_amountObj.length!=null)
          {
              for(var i=0;i<this_amountObj.length;i++)
              {
                   if(this_amountObj[i].value*1==0 || this_amountObj[i].value==null){continue;}
                   total_price = total_price*1 + 1*this_amountObj[i].value;
              }
          }  
          else
          {
                   if(this_amountObj.value*1!=0 || this_amountObj.value!=null){
                        total_price = total_price*1 + 1*this_amountObj.value;
                   }
          }
      }
      
      document.forms[0].ac_money.value = dataFormat(total_price,3);
	}



	//��Χ���������	
	//�����ⵥ�ļ۸��Ƿ���ȷ
	function checkInstorePrice(obj)
	{
       if(obj.value==""){return;}

       var rowCount = obj.parentElement.parentElement.parentElement.parentElement.rows.length-1
       var rowNum=obj.parentElement.parentElement.rowIndex-1;

       var sc_id = "";
       var it_quantity = "";
       var this_amount = 0;
       
       if(rowCount>1)
       {
         sc_id=document.forms[0].sc_id[rowNum].value;
         it_quantity=document.forms[0].it_quantity[rowNum].value;
       }
       else
       {
         sc_id=document.forms[0].sc_id.value;
         it_quantity=document.forms[0].it_quantity.value;
       }
       if(sc_id==""){alert("����������Ϸ����!");obj.value="";return;}  

       if(isType(obj)!=true){alert("���ݸ�ʽ����ȷ!");obj.focus();return;}

       if(it_quantity=="" || isNaN(it_quantity)){return;}     
       
       this_amount = it_quantity*1*obj.value;

       if(rowCount>1)
       {
         document.forms[0].this_amount[rowNum].value = this_amount;
       }
       else
       {
         document.forms[0].this_amount.value = this_amount;
       }               

       if(document.forms[0].lockmoney.checked==true){calcAmount();}
	}


	//��Χ���������	
	//�����ⵥ�������Ƿ���ȷ
	function checkInstoreQuantity(obj)
	{
       if(obj.value==""){return;}
   
       var rowCount = obj.parentElement.parentElement.parentElement.parentElement.rows.length-1
       var rowNum=obj.parentElement.parentElement.rowIndex-1;
       var isDevice = "";
       var sc_id = "";
       var it_unitprice = "";
       var this_amount = 0;

       
       if(rowCount>1)
       {
         isDevice=document.forms[0].isdevice[rowNum].value;
         sc_id=document.forms[0].sc_id[rowNum].value;
         it_unitprice=document.forms[0].it_unitprice[rowNum].value;
       }
       else
       {
         isDevice=document.forms[0].isdevice.value;
         sc_id=document.forms[0].sc_id.value;
         it_unitprice=document.forms[0].it_unitprice.value;
       }
       if(sc_id==""){alert("����������Ϸ����!");obj.value="";return;}
       
       if(isType(obj)!=true){alert("����ֵ��ʽ�������������0������!");obj.focus();return;}
       else
       {
          
          if(!isNaN(it_unitprice))
          {
                this_amount = it_unitprice*1*obj.value;
         
                if(rowCount>1)
                {
                  document.forms[0].this_amount[rowNum].value = this_amount;
                }
                else
                {
                  document.forms[0].this_amount.value = this_amount;
                }   
          }
          if(document.forms[0].lockmoney.checked==true){calcAmount();}            
          
         
         if(isDevice=="yes")
         {
            if(obj.value>50)
            {
            	if(confirm("ȷ��Ҫ��ʾ" + obj.value + "���豸��ϸ����",false))
            	{
                  insertMaterialDataTable(obj.value,sc_id);
               }
            }
            else
            {
               insertMaterialDataTable(obj.value,sc_id);
            }
            
         }
       }      
	}

