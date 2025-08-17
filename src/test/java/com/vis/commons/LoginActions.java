package com.vis.commons;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpReflectionConstructorDecorator;
import com.ccp.decorators.CcpReflectionMethodDecorator;
import com.ccp.decorators.CcpReflectionOptionsDecorator;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.db.utils.CcpErrorEntityPrimaryKeyIsMissing;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.jn.business.login.JnBusinessSendUserToken;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginToken;
import com.jn.services.JnServiceLogin;


public enum LoginActions implements Function<CcpJsonRepresentation, CcpJsonRepresentation> {
	saveAnswers(JnEntityLoginAnswers.ENTITY),
	executeLogin(JnEntityLoginSessionConflict.ENTITY, JnEntityLoginSessionValidation.ENTITY),
	savePassword(JnEntityLoginPassword.ENTITY),
	executeLogout(JnEntityLoginSessionValidation.ENTITY.getTwinEntity()),
	createLoginToken(JnEntityLoginToken.ENTITY, JnEntityEmailMessageSent.ENTITY),
	createLoginEmail(JnEntityLoginEmail.ENTITY),
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
				CcpJsonRepresentation jsonWithSubjectType = json.put(JnEntityEmailMessageSent.Fields.subjectType, JnBusinessSendUserToken.class.getName());
				loginActions.printAllStatus(jsonWithSubjectType);
			}
			
			Class<? extends JnServiceLogin> clazz = JnServiceLogin.INSTANCE.getClass();
			
			String name = this.name();
			CcpReflectionConstructorDecorator reflectionConstructor = new CcpReflectionConstructorDecorator(clazz);
			CcpReflectionOptionsDecorator fromNewInstance = reflectionConstructor.fromNewInstance();
			CcpReflectionMethodDecorator methodOperationSpecification = fromNewInstance.fromDeclaredMethod(name, CcpJsonRepresentation.class);
			
			CcpJsonRepresentation nextJson = methodOperationSpecification.invokeFromMethod(json);
			
			return nextJson;
		}catch (Exception e) {
			
			Throwable cause = e.getCause();
			
			boolean thisMethodDoesNotThrownAnException = cause instanceof InvocationTargetException == false;
			
			if(thisMethodDoesNotThrownAnException) {
				throw new RuntimeException(e);
			}
			
			Throwable subCause = cause.getCause();
			
			boolean theExceptionThrownByTheMethodIsNotFlowDeviation = subCause instanceof CcpErrorFlowDisturb == false;
			
			if(theExceptionThrownByTheMethodIsNotFlowDeviation) {
				throw new RuntimeException(e);
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
			String entityName = entity.getEntityName();
			try {
				boolean exists = entity.exists(json);
				allStatus = allStatus.getDynamicVersion().put(entityName, exists);
			} catch (CcpErrorEntityPrimaryKeyIsMissing e) {
				allStatus = allStatus.getDynamicVersion().put(entityName, false);
			}
		}
	}
	enum JsonFieldNames implements CcpJsonFieldName{
		sessionToken, token, originalToken
	}
}
