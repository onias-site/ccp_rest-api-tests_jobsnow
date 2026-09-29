package com.vis.commons;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpErrorEntityPrimaryKeyIsMissing;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.jn.business.messages.JnMessages.JnNotifyUserAboutLoginToken;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginToken;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.services.JnServiceLogin;

public enum LoginActions implements CcpBusiness {
	SaveAnswers(JnEntityLoginAnswers.ENTITY),
	ExecuteLogin(JnEntityLoginSessionConflict.ENTITY, JnEntityLoginSessionValidation.ENTITY),
	SavePassword(JnEntityLoginPassword.ENTITY),
	ExecuteLogout(JnEntityLoginSessionValidation.ENTITY.getTwinEntity()),
	CreateLoginToken(JnEntityLoginToken.ENTITY, JnEntityEmailMessageSent.ENTITY),
	CreateLoginEmail(JnEntityLoginEmail.ENTITY),
	renameTokenField{
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation jsonWithRenamedToken = json.renameField(JsonFieldNames.sessionToken, JsonFieldNames.token);
			return jsonWithRenamedToken;
		}
	},
	readTokenFromReceivedEmail{
		/**
		 * The {@code JnNotifyUserAboutLoginToken} template renames {@code originalToken} to
		 * {@code token} before composing the message, so it is under {@code token} that the plain value
		 * reaches the e-mail saved on disk.
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			String originalToken = new CcpStringDecorator("c:\\logs\\email\\"+ JnNotifyUserAboutLoginToken.class.getName() + ".json")
			.file().asSingleJson().getAsString(JsonFieldNames.token);
			CcpJsonRepresentation jsonWithToken = json.put(JsonFieldNames.token, originalToken);
			return jsonWithToken;
		}
	},
	;
	private final CcpEntity[] entities;
	
	private LoginActions(CcpEntity... entities) {
		this.entities = entities;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		try {
			LoginActions[] allActions = values();
			for (LoginActions loginAction : allActions) {
				if(loginAction.entities.length == 0) {
					continue;
				}
				CcpJsonRepresentation jsonWithSubjectType = json.put(JnJsonCommonsFields.subjectType, JnNotifyUserAboutLoginToken.class.getName());
				loginAction.printAllStatus(jsonWithSubjectType);
			}
			JnServiceLogin service = JnServiceLogin.valueOf(this.name());
			Map<String, Object> serviceResult = service.execute(json.content);
			CcpJsonRepresentation result = new CcpJsonRepresentation(serviceResult);
			return result;
		}catch (Exception e) {
			
			Throwable cause = e.getCause();
			
			boolean thisMethodDoesNotThrownAnException = false == cause instanceof InvocationTargetException;
			
			if(thisMethodDoesNotThrownAnException) {
				throw new VisErrorLoginActionFailed(this, e);
			}
			
			Throwable subCause = cause.getCause();
			
			boolean theExceptionThrownByTheMethodIsNotFlowDeviation = false == subCause instanceof CcpErrorFlowDisturb;
			
			if(theExceptionThrownByTheMethodIsNotFlowDeviation) {
				throw new VisErrorLoginActionFailed(this, e);
			}
			System.out.println(subCause.getMessage());
			throw (CcpErrorFlowDisturb) subCause;
		}
	}
	
	private void printAllStatus(CcpJsonRepresentation json) {
		if(this.entities.length == 0) {
			return;
		}

		CcpJsonRepresentation allStatus = CcpOtherConstants.EMPTY_JSON;
		
		for (CcpEntity entity : this.entities) {
			CcpEntityMetaData entityDetails = entity.getEntityMetaData();
			String entityName = entityDetails.entityName;
			try {
				boolean exists = entity.exists(json);
				allStatus = allStatus.put(new CcpFieldName(entityName), exists);
			} catch (CcpErrorEntityPrimaryKeyIsMissing e) {
				allStatus = allStatus.put(new CcpFieldName(entityName), false);
			}
		}
	}
	enum JsonFieldNames implements CcpJsonFieldName{
		sessionToken, token
	}

	/**
	 * Exception thrown when a login action fails for a reason that is not an expected flow deviation
	 * ({@code CcpErrorFlowDisturb}), that is, a real failure in the execution of the service.
	 */
	@SuppressWarnings("serial")
	public static class VisErrorLoginActionFailed extends RuntimeException {
		/**
		 * Builds the message stating which action failed and chains the original exception as the cause.
		 * @param action the login action being executed
		 * @param cause the original exception
		 */
		private VisErrorLoginActionFailed(LoginActions action, Throwable cause) {
			super("The login action '" + action + "' has failed", cause);
		}
	}
}
