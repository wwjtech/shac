package com.shac.webapp.action.admin;

import java.util.Date;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.opensymphony.xwork2.ActionSupport;
import com.shac.dao.hibernate.DictDataDaoHibernate;
import com.shac.dao.hibernate.IssueRuleDaoHibernate;
import com.shac.dao.hibernate.IssueTaskDaoHibernate;
import com.shac.dao.hibernate.RuleRecipientDaoHibernate;
import com.shac.dao.hibernate.TaskDeptItemDaoHibernate;
import com.shac.dao.hibernate.UserDaoHibernate;
import com.shac.model.DictData;
import com.shac.model.IssueRule;
import com.shac.model.IssueTask;
import com.shac.model.TaskDeptItem;
import com.shac.model.User;
import com.shac.service.MailEngine;
import com.shac.util.Constants;

public class ForwardDocumentAction extends ActionSupport{
	
	private String deptItemId;
	private IssueTask docu;
	private IssueTaskDaoHibernate issueTaskDao;
	private DictDataDaoHibernate dictDataDao;
	private TaskDeptItemDaoHibernate taskDeptItemDao;
	private RuleRecipientDaoHibernate ruleRecipientDao;
	private UserDaoHibernate userDao;
	private MailEngine mailEngine;
	private List<String>  deptSelected;
	
	private List<DictData> recipients;
	
	@Action(value="editDocuToForward",results={@Result(name="success",location="editDocuToForward.jsp")})
	public String  edit(){
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User currentUser = userDao.findByLoginID(userDetails.getUsername());
		
		docu = taskDeptItemDao.get(deptItemId).getTask();
		IssueRule rule = new IssueRule();
		rule.setDocType(docu.getDocType());
		rule.setProcDocClass(docu.getProcDocClass());
		rule.setProcMode(docu.getProcMode());
		rule.setProcessIn(docu.getProcessIn());
		List<DictData> ruleRecips = ruleRecipientDao.findRecipientsByRule(rule);
		recipients = dictDataDao.findChildren(currentUser.getRegion().getId());
		for(DictData rcp:recipients){
			for(DictData ruleRcp:ruleRecips){
				if(rcp.getId().equals(ruleRcp.getId())){
					rcp.setSent(Constants.FALSE_STRING);//0
				}
			}
		}
		
		return SUCCESS;
	}
	
	@Action(value="forwardDocument",results={@Result(name="success",location="docInbox.do",type="redirect")})
	public String forward(){
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User currentUser = userDao.findByLoginID(userDetails.getUsername());
		User releaseUser = userDao.findByLoginID(Constants.USER_RELEASEOP_LOGINID);
		TaskDeptItem item = taskDeptItemDao.get(deptItemId);
		docu = issueTaskDao.get(item.getTask().getId());
		
		for(String dept:deptSelected){
			DictData rcpt = dictDataDao.get(dept);
			
			TaskDeptItem deptItem = new TaskDeptItem();
			deptItem.setCreateTime(new Date());
			deptItem.setDept(rcpt.getParent());
			deptItem.setWorkshop(rcpt);
			deptItem.setStatus("0");
			deptItem.setHistory("0");
			deptItem.setTask(item.getTask());
			taskDeptItemDao.saveTaskDeptItem(deptItem);
			
			SimpleMailMessage noticeMail = new SimpleMailMessage();
			noticeMail.setFrom(currentUser.getEmail());
			User workshopAdminUser = userDao.findWorkshopAdminUser(rcpt.getId());
			if(workshopAdminUser!=null){
				noticeMail.setTo(workshopAdminUser.getEmail());
			}else{
				noticeMail.setTo(releaseUser.getEmail());
			}
			String subject = subject = "??????????????????--"+docu.getPartid();
			noticeMail.setSubject(subject);
			String bodyText = "??????:"+docu.getPartid()+"??????:"+docu.getDocVersion()+"??????????????????,???????????????????????????????????????";
			noticeMail.setText(bodyText);
	        mailEngine.sendAsync(noticeMail);
		}
		
		//??????????????????,????????????
		taskDeptItemDao.updateHistoryBySysUID(item.getTask(), currentUser.getRegion().getId(),Constants.ROLE_RECVADMIN,null);
		//????????????TaskDeptItem??????????????????????????????????????????
		taskDeptItemDao.updateIssueType(deptItemId, "1");
		//?????????????????????TaskDeptItem??????????????????
		taskDeptItemDao.updateItemStatusOfWorkshop(item.getTask().getId(), item.getDept().getId(), "0");
		
		
		//????????????
		if(Constants.SEND_RECEIVE_FEEDBACK){
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setFrom(currentUser.getEmail());
			mailMessage.setTo(releaseUser.getEmail());
			String subject = "????????????????????????--??????:"+docu.getPartid()+" ??????:"+docu.getDocVersion()+"["+currentUser.getRegion().getDictValue()+"]";
	        mailMessage.setSubject(subject);
	        String bodyText = currentUser.getRegion().getDictValue()+"?????????:"+docu.getPartid()+" ??????:"+docu.getDocVersion()+"??????????????????";
	        mailMessage.setText(bodyText);
	        mailEngine.sendAsync(mailMessage);
		}
		return SUCCESS;
	}

	public IssueTask getDocu() {
		return docu;
	}

	public void setDocu(IssueTask docu) {
		this.docu = docu;
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

	public TaskDeptItemDaoHibernate getTaskDeptItemDao() {
		return taskDeptItemDao;
	}

	public void setTaskDeptItemDao(TaskDeptItemDaoHibernate taskDeptItemDao) {
		this.taskDeptItemDao = taskDeptItemDao;
	}
	
	public RuleRecipientDaoHibernate getRuleRecipientDao() {
		return ruleRecipientDao;
	}

	public void setRuleRecipientDao(RuleRecipientDaoHibernate ruleRecipientDao) {
		this.ruleRecipientDao = ruleRecipientDao;
	}

	public UserDaoHibernate getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDaoHibernate userDao) {
		this.userDao = userDao;
	}

	public List<DictData> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<DictData> recipients) {
		this.recipients = recipients;
	}

	public String getDeptItemId() {
		return deptItemId;
	}

	public void setDeptItemId(String deptItemId) {
		this.deptItemId = deptItemId;
	}

	public List<String> getDeptSelected() {
		return deptSelected;
	}

	public void setDeptSelected(List<String> deptSelected) {
		this.deptSelected = deptSelected;
	}

	public MailEngine getMailEngine() {
		return mailEngine;
	}

	public void setMailEngine(MailEngine mailEngine) {
		this.mailEngine = mailEngine;
	}
	
	
	
	
}
