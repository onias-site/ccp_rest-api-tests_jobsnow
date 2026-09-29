package com.vis.resumes;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.json.transformers.CcpTransformers;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.utils.JnDeleteKeysFromCache;
import com.vis.entities.VisEntityResume;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.vis.json.fields.validation.VisJsonCommonsFields;

public enum ResumeTransformations implements CcpTransformers{
	AddDddsInResume {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			
			List<String> ddds = Arrays.asList("10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "21", "22",
					"24", "27", "28", "31", "32", "33", "34", "35", "37", "38", "41", "42", "43", "44", "45", "46",
					"47", "48", "49", "51", "53", "54", "55", "61", "62", "63", "64", "65", "66", "67", "68", "69",
					"71", "73", "74", "75", "77", "79", "81", "82", "83", "84", "85", "86", "87", "88", "89", "91",
					"92", "93", "94", "95", "96", "97", "98", "99");
			
			boolean willingToRelocate = json.getAsBoolean(JsonFieldNames.mudanca);

			if (willingToRelocate) {

				CcpJsonRepresentation jsonWithAllDdds = json.put(VisJsonCommonsFields.ddd, ddds);

				return jsonWithAllDdds;
			}

			boolean homeoffice = json.getAsBoolean(JsonFieldNames.homeoffice);

			if (homeoffice) {
				List<String> ddd10 = Arrays.asList("10");
				CcpJsonRepresentation jsonWithHomeOfficeDdd = json.put(VisJsonCommonsFields.ddd, ddd10);
				return jsonWithHomeOfficeDdd;
			}

			try {
				Integer ddd = json.getAsIntegerNumber(VisJsonCommonsFields.ddd);
				boolean isZeroDdd = Integer.valueOf(0).equals(ddd);
				if(isZeroDdd) {
					CcpJsonRepresentation jsonWithAllDdds = json.put(VisJsonCommonsFields.ddd, ddds);
					return jsonWithAllDdds;
				}
			} catch (Exception e) {

			}
			
			String ddd = json.getAsString(VisJsonCommonsFields.ddd);
			List<String> numericDdds = Arrays.asList(ddd).stream().filter(x -> new CcpStringDecorator(x).isLongNumber()).collect(Collectors.toList());
			CcpJsonRepresentation jsonWithNumericDdds = json.put(VisJsonCommonsFields.ddd, numericDdds);
			return jsonWithNumericDdds;
		}
	},
	AddExperience {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			
			boolean containsAllFields = json.containsAllFields(VisJsonCommonsFields.experience);
			if(containsAllFields) {
				return json;
			}
			
			Long inclusionDate = json.getAsLongNumber(JsonFieldNames.dataDeInclusao);
			
			Calendar cal = Calendar.getInstance();
			
			cal.setTimeInMillis(inclusionDate);
			
			int year = cal.get(Calendar.YEAR);
			
			CcpJsonRepresentation jsonWithExperience = json.put(VisJsonCommonsFields.experience, year);
			
			return jsonWithExperience;
		}
	},
	AddDisponibility {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation transformedJson = this.addLongValue(json, VisJsonCommonsFields.disponibility.name(), 0L);
			return transformedJson;
		}
	},
	CreateLoginAndSession {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			String email = json.getAsString(JsonFieldNames.id);
			
			CcpJsonRepresentation createdLogin = this.createLogin(email);
			try {
				CcpJsonRepresentation jsonWithSessionToken = this.executeLogin(email);
				
				CcpJsonRepresentation jsonWithLoginAndSession = json.mergeWithAnotherJson(jsonWithSessionToken).mergeWithAnotherJson(createdLogin);
				
				return jsonWithLoginAndSession;
			} catch (Exception e) {
				new CcpStringDecorator("c:\\logs\\resumes").folder().createNewFolderIfNotExists("wrongEmails").createNewFileIfNotExists(email);
				CcpJsonRepresentation jsonWithLogin = json.mergeWithAnotherJson(createdLogin);
				return jsonWithLogin;
			}
		}
		
		private CcpJsonRepresentation executeLogin(String email) {
			
			String path = "http://localhost:8080/login/{email}".replace("{email}", email);
			
			String requestBody = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.password, "Jobsnow1!").asUgglyJson();

			CcpHttpHandler http = new CcpHttpHandler(200, CcpOtherConstants.DO_NOTHING, path);
			
			CcpHttpResponse response = http.ccpHttp.executeHttpRequest(path, CcpHttpMethods.POST, CcpOtherConstants.EMPTY_JSON, requestBody, 200);
			
			CcpJsonRepresentation loginResponse = response.asSingleJson();
			return loginResponse;
		}
		
		private CcpJsonRepresentation createLogin(String email) {
			
			String originalToken = JnJsonTransformersFieldsEntityDefault.getOriginalToken();
			
			CcpJsonRepresentation loginData = CcpOtherConstants.EMPTY_JSON
			.put(JnJsonCommonsFields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
			.put(JnJsonTransformersFieldsEntityDefault.JsonFieldNames.originalToken, originalToken)
			.put(JnJsonTransformersFieldsEntityDefault.JsonFieldNames.token, originalToken)
			.put(JnJsonCommonsFields.ip, "127.0.0.1")
			.put(JnJsonCommonsFields.password, "Jobsnow1!")
			.put(JnEntityLoginAnswers.Fields.channel, "linkedin")
			.put(JnEntityLoginAnswers.Fields.goal, "jobs")
			.put(JnJsonCommonsFields.email, email)
			;
			
			JnExecuteBulkOperation.INSTANCE.executeBulk(
					loginData, 
					CcpBulkEntityOperationType.create, 
					JnDeleteKeysFromCache.INSTANCE,
					JnEntityLoginPassword.ENTITY,
					JnEntityLoginAnswers.ENTITY,
					JnEntityLoginToken.ENTITY,
					JnEntityLoginEmail.ENTITY
					);
			
			JnEntityLoginSessionValidation.ENTITY.delete(loginData);
			
			CcpJsonRepresentation loginDataWithEmail = loginData.renameField(JsonFieldNames.originalEmail, JsonFieldNames.email);
			return loginDataWithEmail;
		}

	},
	AddCltValue {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation transformedJson = this.addRequiredAtLeastOne(json, VisJsonCommonsFields.clt.name(), 1000, 
					VisJsonCommonsFields.clt.name(),
					VisJsonCommonsFields.pj.name()
					);
			return transformedJson;
		}
	},
	AddBtcValue {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation transformedJson = this.putMinValue(json, VisJsonCommonsFields.btc.name(), 1000);
			return transformedJson;
		}
	},
	AddMinCltValue {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation transformedJson = this.putMinValue(json, VisJsonCommonsFields.clt.name(), 1000);
			return transformedJson;
		}
	},
	AddMinPjValue {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation transformedJson = this.putMinValue(json, VisJsonCommonsFields.clt.name(), 1000);
			return transformedJson;
		}
	},
	AddDesiredJob {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation transformedJson = this.substring(json, VisEntityResume.Fields.desiredJob.name(), 100);
			return transformedJson;
		}
	},
	AddLastJob {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation transformedJson = this.substring(json, VisEntityResume.Fields.lastJob.name(), 100);
			return transformedJson;
		}
	},
	AddObservations {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation transformedJson = this.substring(json, "observations", 500);
			return transformedJson;
		}
	},
	;
	abstract public CcpJsonRepresentation apply(CcpJsonRepresentation json);
	enum JsonFieldNames implements CcpJsonFieldName{
		mudanca, homeoffice, dataDeInclusao, id, originalEmail, email
	}

}
