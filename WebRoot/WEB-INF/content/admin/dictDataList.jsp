<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="security" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>上海汇众文档发布系统</title>
<link href="../css/style.css" rel="stylesheet" type="text/css" />
<script type="text/javascript">
function MM_swapImgRestore() { //v3.0
  var i,x,a=document.MM_sr; for(i=0;a&&i<a.length&&(x=a[i])&&x.oSrc;i++) x.src=x.oSrc;
}
function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

function MM_findObj(n, d) { //v4.01
  var p,i,x;  if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {
    d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
  if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
  for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document);
  if(!x && d.getElementById) x=d.getElementById(n); return x;
}

function MM_swapImage() { //v3.0
  var i,j=0,x,a=MM_swapImage.arguments; document.MM_sr=new Array; for(i=0;i<(a.length-2);i+=3)
   if ((x=MM_findObj(a[i]))!=null){document.MM_sr[j++]=x; if(!x.oSrc) x.oSrc=x.src; x.src=a[i+2];}
}
</script>
</head>

<body onload="MM_preloadImages('../img/btn_wdxc.png','../img/btn_jcsj.png','../img/btn_yhgl.png','../img/btn_tjfx.png','../img/btn_xtsz.png')">
<jsp:include page="inc_header.jsp"></jsp:include>
<div class="main">
    <div class="left">
    <ul class="menu">
      <li class="menu_1" ><a href="issueTaskList.do" onmouseout="MM_swapImgRestore()" onmouseover="MM_swapImage('文档发布','','../img/btn_wdfb.png',1)"><img src="../img/btn_wdfb_g.png" alt="文档发布" name="文档发布" width="170" height="69" border="0" id="文档发布"/></a></li>
      <li class="menu_1" ><a href="listManaDocu.do" onmouseout="MM_swapImgRestore()" onmouseover="MM_swapImage('文档管理','','../img/btn_wdgl.png',1)"><img src="../img/btn_wdgl_g.png" alt="文档管理" name="文档管理" width="170" height="69" border="0" id="文档管理" /></a></li>
      <li class="menu_1" ><a href="docQueryList.do" onmouseout="MM_swapImgRestore()" onmouseover="MM_swapImage('文档查询','','../img/btn_wdxc.png',1)"><img src="../img/btn_wdxc_g.png" alt="文档查询" name="文档查询" width="170" height="69" border="0" id="文档查询" /></a></li>
      <li class="menu_2" ><a href="dictDataList.do" ><img src="../img/btn_jcsj.png"/></a></li>
      <li class="menu_1" ><a href="userMngList.do" onmouseout="MM_swapImgRestore()" onmouseover="MM_swapImage('用户管理','','../img/btn_yhgl.png',1)"><img src="../img/btn_yhgl_g.png" alt="用户管理" name="用户管理" width="170" height="69" border="0" id="用户管理" /></a></li>
      <li class="menu_1" ><a href="docReport.do" onmouseout="MM_swapImgRestore()" onmouseover="MM_swapImage('统计分析','','../img/btn_tjfx.png',1)"><img src="../img/btn_tjfx_g.png" alt="统计分析" name="统计分析" width="170" height="69" border="0" id="统计分析" /></a></li>
      <li class="menu_1" ><a href="issueRuleList.do" onmouseout="MM_swapImgRestore()" onmouseover="MM_swapImage('系统设置','','../img/btn_xtsz.png',1)"><img src="../img/btn_xtsz_g.png" alt="系统设置" name="系统设置" width="170" height="69" border="0" id="系统设置" /></a></li>
    </ul>
    <div><img name="" src="../img/left_line.jpg" width="180" height="20" alt="" /></div>
    <div class="font_gray">　软件版权信息：<br />
      　上海汇众汽车制造有限公司
    
    </div>
  </div>
  <div class="right">
    <div class="text_c">
      当前位置：首页 &gt; 基础数据 
    </div>
    <div class="btn_frame">
      <div style="float:left">
      <form action="<s:url value="/admin/dictDataList.do"/>" method="post">
      数据类型：<s:select onchange="this.form.submit();" theme="simple" cssClass="u38" cssStyle="width:152px" list="#{'dept':'厂部','workshop':'车间科室','client':'客户','model':'车型','techdoc':'技术文件类型','drawsize':'图幅','procdoc':'工艺文档分类','procmode':'工艺方式'}" name="data.dictType"  listKey="key" listValue="value" headerKey="" headerValue="请选择"></s:select>
      </form>
      </div>
      <div style="float:right">
      <input name="button" type="button" class="btn_ty" id="button" value="新增"  onclick="window.location='dictDataEdit.do'"/>
      </div>
    </div>
    <table width="99%" border="0" cellpadding="0" cellspacing="0" class="table_list">
		<tr>
          <th>序号</th>
          <th>数据类型</th>
          <th>上级</th>
          <th>数据描述</th>
          <th>排序</th>
          <th>操作</th>
        </tr>
        <s:iterator value="dataList" status="stat">
        <tr <s:if test="#stat.even==true">class="td_bg1"</s:if> <s:if test="id==#parameters.id[0]">style="background:orange"</s:if>>
          <td> <s:property value="#stat.count"/> </td>
          <td>
          	<s:if test='dictType=="dept"'>厂部</s:if>
          	<s:if test='dictType=="workshop"'>车间科室</s:if>
          	<s:if test='dictType=="client"'>客户</s:if>
          	<s:if test='dictType=="model"'>车型</s:if>
          	<s:if test='dictType=="techdoc"'>技术文件类型</s:if>
          	<s:if test='dictType=="drawsize"'>图幅</s:if>
          	<s:if test='dictType=="procdoc"'>工艺文档分类</s:if>
          	<s:if test='dictType=="procmode"'>工艺方式</s:if>
          </td>
          <td> 
          <s:property value="parent.dictValue"/>&nbsp;
          </td>
          <td> 
          <s:property value="dictValue"/>
          </td>
          <td> <s:property value="order"/>   <a href="<s:url action="dictDataUp" namespace="/admin"><s:param name="id" value="id"/></s:url>"><font color="red">升</font></a> <a href="<s:url action="dictDataDown" namespace="/admin"><s:param name="id" value="id"/></s:url>"><font color="green">降</font></a></td>
          <td><a href="<s:url action="dictDataEdit" namespace="/admin"><s:param name="id" value="id"/></s:url>" class="font_green"><strong><u>修改</u></strong></a></td>
        </tr>
        </s:iterator>
      </table>
  </div>
  <div class="space"></div>
</div>
</body>
</html>
