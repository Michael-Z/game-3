//��ʾ����Ŀ¼���ڵ�
function showhide(srcobj,disp){
var liobj,imgobj;
if(srcobj.tagName=="IMG"){
liobj=srcobj.parentElement;
imgobj=srcobj;
}
if(srcobj.tagName=="LI"){
liobj=srcobj;
imgobj=liobj.children.item(0);
}

var ulobj=liobj.children.tags("ul").item(0);
if(disp!=null){
	if(disp==""){
	ulobj.style.display="";
	imgobj.src="../images/icon_sub.gif";
	}
	if(disp=="none"){
	ulobj.style.display="none";
	imgobj.src="../images/icon_add.gif";
	}
}else if(ulobj.style.display=="none"){
   ulobj.style.display="";
   imgobj.src="../images/icon_sub.gif";
   if(liobj.id!=null && liobj.id!=""){
   	eval("opener.menu."+liobj.id+"=''");  //���ڵ�״̬���浽�ⲿ���ҳ����
   	}
  }else
  {
   ulobj.style.display="none";
   imgobj.src="../images/icon_add.gif";
   if(liobj.id!=null && liobj.id!=""){
   	eval("opener.menu."+liobj.id+"='none'");  //���ڵ�״̬���浽�ⲿ���ҳ����
   	}
   }
}

//��ǰһ�α����Ŀ¼���ڵ�״̬չ��Ŀ¼��
function expandMenu(){
var eleArr = document.all;
for(var i=0;i<eleArr.length;i++){
	if(eleArr[i].tagName=="LI"){
		if(eleArr[i].id!=null && eleArr[i].id!=""){
			if(eval("opener.menu."+eleArr[i].id)!=null)
				{
				showhide(eleArr[i],eval("opener.menu."+eleArr[i].id))
				}
				
			}
		}
	}
}
window.onload=expandMenu;
