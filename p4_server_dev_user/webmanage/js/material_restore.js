
	//��Χ�����ò���	
	//������õ��������Ƿ���ȷ
	function checkRestoreQuantity(obj)
	{

       var strdv_code="";
       var strdv_code_old="";
       var strsql_where="";
       
       
       if(obj.value==""){return;}
       var iMaxNum = obj.value;
       //alert(iMaxNum);

	    var rowCount = obj.parentElement.parentElement.parentElement.parentElement.rows.length-1
	    var rowNum=obj.parentElement.parentElement.rowIndex-1;
	    var isDevice = "";
	    var sc_id = "";
	    var borrow_quantity = "";
	    
	    if(rowCount>1)
	    {
	      isDevice=document.forms[0].isdevice[rowNum].value;
	      sc_id=document.forms[0].sc_id[rowNum].value;
	      borrow_quantity = document.forms[0].borrow_quantity[rowNum].value;
	    }
	    else
	    {
	      isDevice=document.forms[0].isdevice.value;
	      sc_id=document.forms[0].sc_id.value;
	      borrow_quantity = document.forms[0].borrow_quantity.value;
	    }
       if(sc_id==""){alert("����������Ϸ����!");obj.value="";return;}   
       if(borrow_quantity!=""){borrow_quantity = parseInt(borrow_quantity);}
       
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
          

         //�ж���������������Ƿ񳬹��˿������
         if(obj.value>borrow_quantity){
            alert("�黹���������˽�����������������д�黹������");
            obj.focus();return;
         }
         
         //���õ�������豸������������´���
         if(isDevice=="yes")
         {
      	    if(rowCount>1)
      	    {
					strdv_code = document.forms[0].dv_code[rowNum].value;
					strdv_code_old = document.forms[0].dv_code_old[rowNum].value;
					strsql_where = document.forms[0].sql_where[rowNum].value;
					
      	    }
      	    else
      	    {
					strdv_code = document.forms[0].dv_code.value;
					strdv_code_old = document.forms[0].dv_code_old.value;
					strsql_where = document.forms[0].sql_where.value;
      	    }                  
            //alert(strsql_where);
            //alert(strdv_code_old);
            
            createImportForm(obj,1,'/fdms/common/importform_dvcode_restore.jsp','ѡ���豸',500,360,'yes','form1','dv_code',1,',',strsql_where,strdv_code,',',1,strdv_code_old,',',iMaxNum,'');
            //function createImportForm(obj,iNoUseLine,sFile,sTitle,iWidth,iHeight,sControl,sFormName,sInputs,iMultiSelect,chSplit,sParam,sDefault,sDefaultSplit,iList,sHasSelected,sSelectedSplit,iMaxNum,sIndexValue)
         }
       }             
	}
	
