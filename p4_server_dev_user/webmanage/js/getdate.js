var datebox=null;
function getdate(obj){
var w=215,h=170,newwindow;
datebox=obj;
newwindow=window.open("../common/getdate.htm","��ȡ����","width="+w+",height="+h);
newwindow.moveTo((window.screen.width-w)/2,(window.screen.height-h)/2);
}