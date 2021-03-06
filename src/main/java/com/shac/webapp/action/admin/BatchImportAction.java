package com.shac.webapp.action.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.doomdark.uuid.UUIDGenerator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.util.WebUtils;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;
import com.shac.dao.hibernate.DictDataDaoHibernate;
import com.shac.dao.hibernate.IssueTaskDaoHibernate;
import com.shac.dao.hibernate.TaskDeptItemDaoHibernate;
import com.shac.dao.hibernate.UserDaoHibernate;
import com.shac.dao.hibernate.ZnoDaoHibernate;
import com.shac.model.DictData;
import com.shac.model.IssueTask;
import com.shac.model.TCIssueTask;
import com.shac.model.TaskDeptItem;
import com.shac.model.User;
import com.shac.model.Zno;
import com.shac.util.Constants;
import com.shac.util.Page;

public class BatchImportAction extends ActionSupport implements Preparable {
	public static final UUIDGenerator uuidmaker = UUIDGenerator.getInstance();

	// ????????????
	private Integer totalCount;
	private Page pager;
	private Integer page = new Integer(1);

	// ????????????
	private List clientList;
	private List modelList;
	private List procDocClassList;// ??????????????????
	private List procModeList;// ????????????
	private List<DictData> seleteddep;

	// ???????????????
	private String initclient;
	private String initprocDocClass;
	private String initmodelCode;
	private String initprocMode;
	private String initprocessIn;
	private String inittaskdep;
	private String initcltPartNumb;
	private String initassembTitle;
	private String[] initseleteddep;

	// ???????????????
	private IssueTaskDaoHibernate issueTaskDao;
	private DictDataDaoHibernate dictDataDao;
	private UserDaoHibernate userDao;
	private ZnoDaoHibernate znoDao;
	private TaskDeptItemDaoHibernate taskDeptItemDao;
	// ????????????
	private String id;
	private IssueTask docu;

	// ????????????
	private transient File attach;
	private transient String attachFileName;
	private transient String attachContentType;

	// ????????????
	private List docList;

	// ?????????
	private String upfile;
	
	//??????????????????
	private List<DictData> recipients;
	
	private List<String>  deptSelected;
	
	//???????????????
	private String pickdate;
	
	private String hiddenpickdate;
	
	private String flagmessage;
	
	public String getFlagmessage() {
		return flagmessage;
	}

	public void setFlagmessage(String flagmessage) {
		this.flagmessage = flagmessage;
	}

	@Action(value = "batchlist", results = { @Result(name = "success", location = "batchlist.jsp") })
	public String list() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();
		User user = userDao.findByLoginID(userDetails.getUsername());
		// ??????????????????
		clientList = dictDataDao.findByDictType(Constants.DICTTYPE_CLIENT);
		modelList = dictDataDao.findByDictType(Constants.DICTTYPE_MODEL);
		procDocClassList = dictDataDao
				.findByDictType(Constants.DICTTYPE_PROCDOCCLASS);
		procModeList = dictDataDao.findByDictType(Constants.DICTTYPE_PROCMODE);
		
		seleteddep = dictDataDao.findChildren(user.getRegion().getId());
        
		// ?????????????????????????????????
		
		// ???????????????
		HttpServletRequest request = ServletActionContext.getRequest();
		Map filterMap = WebUtils.getParametersStartingWith(request, "docu.");
		Map orderMap = new HashMap();
		filterMap.put("user", user);
		filterMap.put("history", "0");
		//filterMap.put("docType", Constants.BATCH_DOC_DOCTYPE_PROC);
		filterMap.put("status", Constants.DOC_STATUS);
		
		if (upfile != null) {
			filterMap.put("attachFile", upfile);
		}

		orderMap.put("createTime", "desc");
		docList = issueTaskDao.findBy(filterMap, orderMap,
				(page.intValue() - 1) * 10, 10);
		totalCount = issueTaskDao.getCount(filterMap).intValue();

		return SUCCESS;
	}

	@Action(value = "tobatch", results = { @Result(name = "success", location = "editbatch.jsp") })
	public String tobatch() {
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User currentUser = userDao.findByLoginID(userDetails.getUsername());
		// ???????????????
		clientList = dictDataDao.findByDictType(Constants.DICTTYPE_CLIENT);
		modelList = dictDataDao.findByDictType(Constants.DICTTYPE_MODEL);
		procDocClassList = dictDataDao
				.findByDictType(Constants.DICTTYPE_PROCDOCCLASS);
		procModeList = dictDataDao.findByDictType(Constants.DICTTYPE_PROCMODE);
		
		recipients = dictDataDao.findChildren(currentUser.getRegion().getId());
        
		if (id != null) {
			docu = issueTaskDao.get(id);
			String depselected = docu.getTaskdep();
			if(depselected!=null && depselected.length()>0){
				String []  depselects = depselected.split(",");
				for(DictData rcp:recipients){
					for(String seldep:depselects){
						if(rcp.getDictValue().equalsIgnoreCase(seldep)){
							rcp.setSent(Constants.FALSE_STRING);
						}
					}
				}
			}
		} else {
			docu = new IssueTask();
			docu.setClient(initclient);
			docu.setProcDocClass(initprocDocClass);
			docu.setModelCode(initmodelCode);
			docu.setProcessIn(initprocessIn);
			docu.setProcMode(initprocMode);
			docu.setTaskdep(inittaskdep);
			docu.setCltPartNumb(initcltPartNumb);
			docu.setAssembTitle(initassembTitle);
			//????????????????????????
			if(initseleteddep!=null){
				for(DictData rcp:recipients){
					for(String seldep:initseleteddep){
						if(rcp.getDictValue().equalsIgnoreCase(seldep)){
							rcp.setSent(Constants.FALSE_STRING);
						}
					}
				}
			}
			
		}
		return SUCCESS;
	}

	@Action(value = "savebatch", results = {
			@Result(name = "success", location = "batchlist.jsp"),
			@Result(name = "input", location = "editbatch.jsp") }, interceptorRefs = {
			@InterceptorRef(params = {
					"allowedTypes",
					"image/png,image/gif,image/jpeg,image/pjpeg,image/bmp,image/x-png,text/plain,application/msword,"
							+ "application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/pdf",
					"maximumSize", "102400000" }, value = "fileUpload"),
			@InterceptorRef("params"), @InterceptorRef("defaultStack") })
	public String savebatch() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();
		User user = userDao.findByLoginID(userDetails.getUsername());

		Date date = new Date();
		boolean isNew = docu.getId() == null || docu.getId().equals("");
		if (isNew) {
			docu.setCreateTime(date);
			docu.setTaskStatus(Constants.ISSUE_TASK_WAIT);
			docu.setTaskType(Constants.ISSUE_TASKTYPE_MANU);
			docu.setHistory("0");
			docu.setUpdated("0");
			docu.setDocVersion("001");
			// ??????????????????????????????????????????id??????ZNO????????????,?????????status?????????D,?????????????????????
			docu.setStatus(Constants.DOC_STATUS);
			docu.setUser(user);

			Zno temp = new Zno();
			temp.setUser(user);
			temp.setNoTime(new Date());
			znoDao.saveZno(temp);
			String partid = "G" + String.format("%08d", temp.getId());
			docu.setPartid(partid);

			IssueTask foundDocu = issueTaskDao.findDocuByPartId(
					docu.getPartid(), docu.getAssembly());
			if (foundDocu != null) {
				addActionError("??????ID???[" + docu.getPartid() + "] ????????? ?????????");
				return INPUT;
			}
		}

		if (attach != null) {
			FileInputStream fis = null;
			FileOutputStream fos = null;
			try {
				fis = new FileInputStream(attach);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			Random r = new Random(date.getTime());
			String fileType = getAttachFileName().substring(
					getAttachFileName().lastIndexOf(".") + 1);
			String newFileNamePrefix = ServletActionContext.getRequest()
					.getSession().getId()
					+ r.nextInt(10000);
			String newFileName = newFileNamePrefix + "." + fileType;
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String dateDir = formatter.format(date);
			String tmp = File.separator + dateDir + File.separator;
			String uploadDir = ServletActionContext.getServletContext()
					.getRealPath("/" + Constants.DOCU_UPLOAD_PATH) + tmp;
			String viewPathPrefix = "/" + Constants.DOCU_UPLOAD_PATH + "/"
					+ dateDir + "/";
			String viewPath = viewPathPrefix + newFileName;
			File dirPath = new File(uploadDir);
			if (!dirPath.exists()) {
				dirPath.mkdirs();
			}
			try {
				fos = new FileOutputStream(uploadDir + newFileName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			try {
				while ((bytesRead = fis.read(buffer, 0, 1024)) != -1) {
					fos.write(buffer, 0, bytesRead);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				fos.close();
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			buffer = null;
			docu.setAttachFile(viewPath);

			// ????????????+????????????
			if (docu.getDocType().equals(Constants.BATCH_DOC_DOCTYPE_PROC)) {
				System.out.println("??????????????????");

				String pdfPath = docu.getAttachFile();
				String realPath = ServletActionContext.getServletContext()
						.getRealPath(pdfPath);
				File pdfFile = new File(realPath);
				String fileFullName = pdfFile.getName();
				String fileName = fileFullName.substring(0,
						fileFullName.lastIndexOf("."));
				String waterMarkFileName = pdfFile.getParent() + File.separator
						+ fileName + "-sct.pdf";
				File waterMarkFile = new File(waterMarkFileName);
				FileOutputStream os = null;
				try {
					os = new FileOutputStream(waterMarkFile);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}

				try {
					PdfReader reader = new PdfReader(realPath, null);
					PdfStamper writer = new PdfStamper(reader, os);
					Image image = Image.getInstance(ServletActionContext
							.getServletContext().getRealPath("/img")
							+ "/mark_security.png");
					int pageSize = reader.getNumberOfPages();
					BaseFont base = null;
					// base = BaseFont.createFont(BaseFont.HELVETICA,
					// BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
					base = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",
							BaseFont.NOT_EMBEDDED);
					for (int i = 1; i <= pageSize; i++) {
						Rectangle rectangle = reader.getPageSizeWithRotation(i);
						float x = rectangle.getWidth() / 2;
						float y = rectangle.getHeight() / 2;
						int fontSize = (int) (x * 0.1);

						PdfContentByte content = writer.getOverContent(i);
						content.saveState();
						// ????????????
						PdfGState gs = new PdfGState();
						gs.setFillOpacity(0.1f);// ?????????
						content.setGState(gs);

						// ????????????-start
						// image.setAbsolutePosition(0,0);
						// content.addImage(image);
						// ????????????-end

						// ????????????-start
						content.beginText();
						content.setColorFill(BaseColor.RED);
						content.setFontAndSize(base, fontSize);
						content.showTextAligned(Element.ALIGN_CENTER, "????????????",
								x, y, 35);// 35?????????
						content.endText();

						// ????????????????????????
						content.beginText();
						content.setColorFill(BaseColor.RED);
						content.setFontAndSize(base, fontSize);
						content.showTextAligned(Element.ALIGN_CENTER,
								docu.getProcessIn(), x, y - fontSize - 20, 35);// 35?????????
						content.endText();

						content.restoreState();
						// ????????????-end
					}
					reader.close();
					writer.close();

					try {
						// -s poly2bitmap
						// ?????????????????????pdf?????????swf????????????????????????????????????????????????????????????????????????-s bitmap?????????
						Process process = Runtime
								.getRuntime()
								.exec("C:\\SWFTools\\pdf2swf.exe "
										+ waterMarkFileName
										+ " "
										+ pdfFile.getParent()
										+ File.separator
										+ fileName
										+ ".swf  -f -T 9 -t -s bitmap -s zoom=100 -s languagedir=C:\\xpdf\\xpdf-chinese-simplified");
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println(e);
					}

				} catch (IOException e) {
					e.printStackTrace();
				} catch (DocumentException e) {
					e.printStackTrace();
				}
				docu.setViewFile(pdfPath.substring(0, pdfPath.lastIndexOf("/"))
						+ "/" + fileName + ".swf");
			}
			// ????????????+????????????
			
			if(!isNew){
				//????????????????????????,??????printFile ??? adminPrintFile,
				//?????? ???????????????????????? ??? ?????????????????????????????????????????????
				docu.setPrintFile(null);
				docu.setAdminPrintFile(null);
			}
		}

		// sysUID
		if (isNew) {
			String sysUID = uuidmaker.generateTimeBasedUUID().toString()
					.replaceAll("-", "");
			docu.setSysUID(sysUID);
		}

		//??????????????????
		String defaultdep = "";
		if(deptSelected!=null){
			for(String dept:deptSelected){
				defaultdep = defaultdep + dept + ",";
			}
		}
		docu.setTaskdep(defaultdep);
		
		issueTaskDao.save(docu);

		// ??????????????????
		clientList = dictDataDao.findByDictType(Constants.DICTTYPE_CLIENT);
		modelList = dictDataDao.findByDictType(Constants.DICTTYPE_MODEL);
		procDocClassList = dictDataDao
				.findByDictType(Constants.DICTTYPE_PROCDOCCLASS);
		procModeList = dictDataDao.findByDictType(Constants.DICTTYPE_PROCMODE);
		seleteddep = dictDataDao.findChildren(user.getRegion().getId());
		
		//????????????????????????
		if(initseleteddep!=null){
			String[] temp_select = initseleteddep[0].split(",");
			for(DictData rcp:seleteddep){			
				for(String seldep:temp_select){
					if(rcp.getDictValue().equalsIgnoreCase(seldep.trim())){
						rcp.setSent(Constants.FALSE_STRING);
					}
				}
			}
		}
		
		Map filterMap = new HashMap();
		Map orderMap = new HashMap();
		filterMap.put("user", user);
		filterMap.put("history", "0");
		//filterMap.put("docType", Constants.BATCH_DOC_DOCTYPE_PROC);
		filterMap.put("status", Constants.DOC_STATUS);
		orderMap.put("createTime", "desc");
		docList = issueTaskDao.findBy(filterMap, orderMap,
				(page.intValue() - 1) * 10, 10);
		totalCount = issueTaskDao.getCount(filterMap).intValue();
		return SUCCESS;
	}
    
	/**
	 * ???????????????
	 * 
	 */
	@Action(value = "tooff", results = { @Result(name = "success", location = "official.jsp") })
	public String toofficial(){		
		//???????????????
		if(pickdate!=null&&pickdate!=""){
			Map filterMap = new HashMap();
			Map orderMap = new HashMap();
			filterMap.put("history", "0");
			filterMap.put("pickdate", pickdate);
			//filterMap.put("docType", Constants.BATCH_DOC_DOCTYPE_PROC);
			filterMap.put("status", Constants.DOC_STATUS);
			orderMap.put("createTime", "desc");
			docList = issueTaskDao.findBy(filterMap, orderMap,
					0, 10000);
			hiddenpickdate=pickdate;
		}
		return SUCCESS;
	}
	
	/**
	 *  ??????????????????
	 */
	@Action(value = "clearoff", results = { @Result(name = "success", location = "official.jsp") })
	public String clearoff(){	
		
		taskDeptItemDao.removeErroWorkshipData();
		taskDeptItemDao.removeErroCityData();
		flagmessage = "????????????";
		return SUCCESS;
	}
	
	
	/**
	 * ??????????????????
	 * @return
	 */
	@Action(value = "dooff", results = { @Result(name = "success", location = "official.jsp") })
	public String dooff(){
		
		Map filterMap = new HashMap();
		Map orderMap = new HashMap();
		filterMap.put("history", "0");
		filterMap.put("pickdate", hiddenpickdate);
		filterMap.put("status", Constants.DOC_STATUS);
		orderMap.put("createTime", "desc");
		docList = issueTaskDao.findBy(filterMap, orderMap,
				0, 10000);
		pickdate=hiddenpickdate;
		
		List<IssueTask> docListit = docList;
		
		for(IssueTask it : docListit){
		   if(it.getDocType().equalsIgnoreCase(Constants.BATCH_DOC_DOCTYPE_PROC) && it.getAttachFile()!=null){
			//??????????????????????????????,?????????????????????????????????,???????????????????????????
				it.setDocType(Constants.DOC_DOCTYPE_PROC);	    
		    //??????????????????????????????,????????????????????????????????????????????????????????????????????????????????????
				User factoryDR = it.getUser();
				TaskDeptItem deptItem = new TaskDeptItem();
				deptItem.setCreateTime(new Date());
				deptItem.setDept(factoryDR.getRegion());
				deptItem.setHistory("0");
				deptItem.setTask(it);
				deptItem.setStatus("1");//???????????????
				deptItem.setIssueType("1");//????????????????????????,?????????????????????????????????????????????????????????
				//??????????????????
				if(!checkalreadyin(deptItem,0)){
					taskDeptItemDao.saveTaskDeptItem(deptItem);
					//??????--???????????????????????????????????????
					taskDeptItemDao.updateHistoryBySysUID(it, factoryDR.getRegion().getId(),Constants.ROLE_RECVADMIN,null);
				}
				
				it.setTaskStatus("1");
				
				if(it.getTaskdep() != null && it.getTaskdep().length() > 1){
					String [] taskdepselected = it.getTaskdep().split(",");	
					List<DictData> bumens = dictDataDao.findChildren(factoryDR.getRegion().getId());
				    //???????????????2?????????	
					for(String dept:taskdepselected){
						for(DictData bumen:bumens){	
							if(bumen.getDictValue().equalsIgnoreCase(dept)){				
								TaskDeptItem deptItembumen = new TaskDeptItem();
								deptItembumen.setCreateTime(new Date());
								deptItembumen.setDept(factoryDR.getRegion());
								deptItembumen.setWorkshop(bumen);
								deptItembumen.setStatus("1");
								deptItembumen.setHistory("0");
								deptItembumen.setTask(it);
								if(!checkalreadyin(deptItembumen,1)){
									taskDeptItemDao.saveTaskDeptItem(deptItembumen);					
									taskDeptItemDao.updateHistoryBySysUID(it, factoryDR.getRegion().getId(),Constants.ROLE_RECVADMIN_WORKSHOP,bumen.getId());
								}
							}
						}
					}
					
				}
			    
				//????????????????????????
				if(Constants.DEFAULT_DEPIDS !=null && Constants.DEFAULT_DEPIDS.length>0){
					for (String dep : Constants.DEFAULT_DEPIDS) {
						DictData depdict = null;
						// ???????????????????????????????????????id
						HashMap filter = new HashMap();
						filter.put("dictType", Constants.DICTTYPE_REGION);
						filter.put("dictID", dep);
						List<DictData> temp = dictDataDao.findByAll(filter,
								null);
						if (temp.size() > 0) {
							depdict = temp.get(0);
							TaskDeptItem tempdepitem = new TaskDeptItem();
							tempdepitem.setCreateTime(new Date());
							tempdepitem.setDept(depdict);
							tempdepitem.setHistory("0");
							tempdepitem.setTask(it);
							tempdepitem.setStatus("1");//???????????????						
							//??????????????????
							if(!checkalreadyin(tempdepitem,0)){
								taskDeptItemDao.saveTaskDeptItem(tempdepitem);
								//??????--???????????????????????????????????????
								taskDeptItemDao.updateHistoryBySysUID(it, depdict.getId(),Constants.ROLE_RECVADMIN,null);
							}
						}																	
					}										
				}
				
				
				issueTaskDao.updateTask(it);
		   }	
		}
		
		docList = docListit;
		
		return SUCCESS;
	}
	
	/**
	 * ?????????????????????
	 * 
	 * @param dealtc
	 * @return
	 */
	private boolean checkalreadyin(TaskDeptItem deptItem,int fl) {
		// TODO Auto-generated method stub
		boolean flag = false;
		HashMap filter = new HashMap();
		HashMap orderMap = new HashMap();
		filter.put("taskid", deptItem.getTask().getId());
		if(fl == 0){
			filter.put("dept", deptItem.getDept().getId());
		}
		if(fl == 1){
			filter.put("item_workshop", deptItem.getWorkshop());
		}
		List taskList;
		taskList = taskDeptItemDao.findByAll(filter, orderMap);
		if (taskList.size() > 0) {
			flag = true;
		}

		return flag;
	}
	
	
	/**
	 * ?????? ??????????????????????????????????????????????????????
	 * 
	 * @return
	 */
	@Action(value = "removeBatchProcDoc", results = { @Result(name = "success", location = "batchlist.do", type = "redirect") })
	public String remove() {
		issueTaskDao.remove(id);
		return SUCCESS;
	}

	public List getClientList() {
		return clientList;
	}

	public void setClientList(List clientList) {
		this.clientList = clientList;
	}

	public List getModelList() {
		return modelList;
	}

	public void setModelList(List modelList) {
		this.modelList = modelList;
	}

	public List getProcDocClassList() {
		return procDocClassList;
	}

	public void setProcDocClassList(List procDocClassList) {
		this.procDocClassList = procDocClassList;
	}

	public List getProcModeList() {
		return procModeList;
	}

	public void setProcModeList(List procModeList) {
		this.procModeList = procModeList;
	}

	public IssueTaskDaoHibernate getIssueTaskDao() {
		return issueTaskDao;
	}

	public void setIssueTaskDao(IssueTaskDaoHibernate issueTaskDao) {
		this.issueTaskDao = issueTaskDao;
	}

	public DictDataDaoHibernate getDictDataDao() {
		return dictDataDao;
	}

	public void setDictDataDao(DictDataDaoHibernate dictDataDao) {
		this.dictDataDao = dictDataDao;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Page getPager() {
		return pager;
	}

	public void setPager(Page pager) {
		this.pager = pager;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public String getInitclient() {
		return initclient;
	}

	public void setInitclient(String initclient) {
		this.initclient = initclient;
	}

	public String getInitprocDocClass() {
		return initprocDocClass;
	}

	public void setInitprocDocClass(String initprocDocClass) {
		this.initprocDocClass = initprocDocClass;
	}

	public String getInitmodelCode() {
		return initmodelCode;
	}

	public void setInitmodelCode(String initmodelCode) {
		this.initmodelCode = initmodelCode;
	}

	public String getInitprocMode() {
		return initprocMode;
	}

	public void setInitprocMode(String initprocMode) {
		this.initprocMode = initprocMode;
	}

	public String getInitprocessIn() {
		return initprocessIn;
	}

	public void setInitprocessIn(String initprocessIn) {
		this.initprocessIn = initprocessIn;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public IssueTask getDocu() {
		return docu;
	}

	public void setDocu(IssueTask docu) {
		this.docu = docu;
	}

	public File getAttach() {
		return attach;
	}

	public void setAttach(File attach) {
		this.attach = attach;
	}

	public String getAttachFileName() {
		return attachFileName;
	}

	public void setAttachFileName(String attachFileName) {
		this.attachFileName = attachFileName;
	}

	public String getAttachContentType() {
		return attachContentType;
	}

	public void setAttachContentType(String attachContentType) {
		this.attachContentType = attachContentType;
	}

	public UserDaoHibernate getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDaoHibernate userDao) {
		this.userDao = userDao;
	}

	public ZnoDaoHibernate getZnoDao() {
		return znoDao;
	}

	public void setZnoDao(ZnoDaoHibernate znoDao) {
		this.znoDao = znoDao;
	}

	public String getInittaskdep() {
		return inittaskdep;
	}

	public void setInittaskdep(String inittaskdep) {
		this.inittaskdep = inittaskdep;
	}

	public List getDocList() {
		return docList;
	}

	public void setDocList(List docList) {
		this.docList = docList;
	}

	public String getUpfile() {
		return upfile;
	}

	public void setUpfile(String upfile) {
		this.upfile = upfile;
	}

	public List<DictData> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<DictData> recipients) {
		this.recipients = recipients;
	}
	
	

	public List<String> getDeptSelected() {
		return deptSelected;
	}

	public void setDeptSelected(List<String> deptSelected) {
		this.deptSelected = deptSelected;
	}

	
	
	public String getInitcltPartNumb() {
		return initcltPartNumb;
	}

	public void setInitcltPartNumb(String initcltPartNumb) {
		this.initcltPartNumb = initcltPartNumb;
	}

	public String getInitassembTitle() {
		return initassembTitle;
	}

	public void setInitassembTitle(String initassembTitle) {
		this.initassembTitle = initassembTitle;
	}

	public String getPickdate() {
		return pickdate;
	}

	public void setPickdate(String pickdate) {
		this.pickdate = pickdate;
	}

	
	
	public String getHiddenpickdate() {
		return hiddenpickdate;
	}

	public void setHiddenpickdate(String hiddenpickdate) {
		this.hiddenpickdate = hiddenpickdate;
	}

	public String[] getInitseleteddep() {
		return initseleteddep;
	}

	public void setInitseleteddep(String[] initseleteddep) {
		this.initseleteddep = initseleteddep;
	}

	public List<DictData> getSeleteddep() {
		return seleteddep;
	}

	public void setSeleteddep(List<DictData> seleteddep) {
		this.seleteddep = seleteddep;
	}

	public void prepare() throws Exception {
		// TODO Auto-generated method stub

	}
	
	

	public TaskDeptItemDaoHibernate getTaskDeptItemDao() {
		return taskDeptItemDao;
	}

	public void setTaskDeptItemDao(TaskDeptItemDaoHibernate taskDeptItemDao) {
		this.taskDeptItemDao = taskDeptItemDao;
	}

	public void prepareSavebatch() throws Exception {
		clientList = dictDataDao.findByDictType(Constants.DICTTYPE_CLIENT);
		modelList = dictDataDao.findByDictType(Constants.DICTTYPE_MODEL);
		procDocClassList = dictDataDao
				.findByDictType(Constants.DICTTYPE_PROCDOCCLASS);
		procModeList = dictDataDao.findByDictType(Constants.DICTTYPE_PROCMODE);
		if (!docu.getId().equals("")) {
			docu = issueTaskDao.get(docu.getId());
		}
	}

}
