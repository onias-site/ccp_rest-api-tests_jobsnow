package com.vis.commons;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpErrorEntityPrimaryKeyIsMissing;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.jn.business.messages.JnBusinessSendUserToken;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginToken;
import com.jn.services.JnServiceLogin;
import com.jn.json.fields.validation.JnJsonCommonsFields;

public enum LoginActions implements CcpBusiness {
	SaveAnswers(JnEntityLoginAnswers.ENTITY),
	ExecuteLogin(JnEntityLoginSessionConflict.ENTITY, JnEntityLoginSessionValidation.ENTITY),
	SavePassword(JnEntityLoginPassword.ENTITY),
	ExecuteLogout(JnEntityLoginSessionValidation.ENTITY.getTwinEntity()),
	CreateLoginToken(JnEntityLoginToken.ENTITY, JnEntityEmailMessageSent.ENTITY),
	CreateLoginEmail(JnEntityLoginEmail.ENTITY),
	renameTokenField{
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation renameField = json.renameField(JsonFieldNames.sessionToken, JsonFieldNames.token);
			return renameField; 
		}
	},
	readTokenFromReceivedEmail{
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			String originalToken = new CcpStringDecorator("c:\\logs\\email\\"+ JnBusinessSendUserToken.class.getName() + ".json")
			.file().asSingleJson().getAsString(JsonFieldNames.originalToken);
			CcpJsonRepresentation put = json.put(JsonFieldNames.token, originalToken);
			return put;
		}
	},
	;
	private final CcpEntity[] entities;
	
	private LoginActions(CcpEntity... entities) {
		this.entities = entities;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		try {
			LoginActions[] values = values();
			for (LoginActions loginActions : values) {
				if(loginActions.entities.length == 0) {
					continue;
				}
				CcpJsonRepresentation jsonWithSubjectType = json.put(JnJsonCommonsFields.subjectType, JnBusinessSendUserToken.class.getName());
				loginActions.printAllStatus(jsonWithSubjectType);
			}
			JnServiceLogin valueOf = JnServiceLogin.valueOf(this.name());
			Map<String, Object> execute = valueOf.execute(json.content);
			CcpJsonRepresentation result = new CcpJsonRepresentation(execute);
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
		sessionToken, token, originalToken
	}

	/**
	 * Exceção lançada quando uma ação de login falha por um motivo que não é um desvio de fluxo esperado
	 * ({@code CcpErrorFlowDisturb}), ou seja, uma falha real na execução do serviço.
	 */
	@SuppressWarnings("serial")
	public static class VisErrorLoginActionFailed extends RuntimeException {
		/**
		 * Monta a mensagem informando qual ação falhou e encadeia a exceção original como causa.
		 * @param action a ação de login em execução
		 * @param cause a exceção original
		 */
		private VisErrorLoginActionFailed(LoginActions action, Throwable cause) {
			super("The login action '" + action + "' has failed", cause);
		}
	}
}
