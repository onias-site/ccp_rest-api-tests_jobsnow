package com.vis.resumes;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.query.CcpQueryExecutor;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.jn.services.JnServiceLogin;
import com.jn.utils.JnLanguage;
import com.vis.entities.VisEntityResume;
import com.vis.services.VisServiceResume;
import com.vis.json.fields.validation.VisJsonCommonsFields;

public class ImportResumeFromOldJobsNow implements Consumer<CcpJsonRepresentation>{
	enum JsonFieldNames implements CcpJsonFieldName{
		id, curriculo, conteudo, resumeBase64, arquivo, fileName, disponibilidade, profissaoDesejada, empresas, ultimaProfissao, experiencia, pretensaoClt, pretensaoPj, bitcoin, observacao, observations, name, originalEmail, status, language
	}

	public static final ImportResumeFromOldJobsNow INSTANCE = new ImportResumeFromOldJobsNow();
	private Set<String> ids;			
	int contador;

	private ImportResumeFromOldJobsNow() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = 
				CcpQueryOptions.INSTANCE
					.matchAll()
					.maxResults()
				;
		String[] resourcesNames = VisEntityResume.ENTITY.getEntityMetaData().getEntitiesToSelect();
		this.ids = queryExecutor.getResultAsList(query, resourcesNames, "email").stream().map(x -> x.getAsString(JsonFieldNames.id)).collect(Collectors.toSet());
	}
	
	public void accept(CcpJsonRepresentation candidate) {
		
		boolean alreadyInserted = this.contador++ < this.ids.size();
		
		if(alreadyInserted) {
//			return;
		}
		
		CcpJsonRepresentation resumeFile = candidate.getInnerJson(JsonFieldNames.curriculo)
				.renameField(JsonFieldNames.conteudo, JsonFieldNames.resumeBase64)
				.renameField(JsonFieldNames.arquivo, JsonFieldNames.fileName)
				.getJsonPiece(JsonFieldNames.resumeBase64, JsonFieldNames.fileName)
		;
		CcpJsonRepresentation resume = candidate
		.renameField(JsonFieldNames.disponibilidade, VisJsonCommonsFields.disponibility)
		.renameField(JsonFieldNames.profissaoDesejada, VisEntityResume.Fields.desiredJob)
		.renameField(JsonFieldNames.empresas,VisEntityResume.Fields.notAllowedCompany)
		.renameField(JsonFieldNames.ultimaProfissao, VisEntityResume.Fields.lastJob)
		.renameField(JsonFieldNames.experiencia, VisJsonCommonsFields.experience)
		.renameField(JsonFieldNames.pretensaoClt, VisJsonCommonsFields.clt)
		.renameField(JsonFieldNames.pretensaoPj, VisJsonCommonsFields.pj)
		.renameField(JsonFieldNames.bitcoin, VisJsonCommonsFields.btc)
		.renameField(JsonFieldNames.observacao, JsonFieldNames.observations)
		.put(JsonFieldNames.name, "NOME DO CANDIDATO")
		.mergeWithAnotherJson(resumeFile)
		.copyIfNotContains(VisEntityResume.Fields.lastJob, VisEntityResume.Fields.desiredJob)
		.putIfNotContains(VisEntityResume.Fields.notAllowedCompany, Arrays.asList())
		.putIfNotContains(VisJsonCommonsFields.disponibility, 0)
		.putIfNotContains(VisEntityResume.Fields.desiredJob, "-")
		.putIfNotContains(VisEntityResume.Fields.lastJob, "-")
		.putIfNotContains(JsonFieldNames.observations, "-")
		.getTransformedJson(
				ResumeTransformations.AddBtcValue,
				ResumeTransformations.AddCltValue,
				ResumeTransformations.AddDddsInResume,
				ResumeTransformations.AddDesiredJob,
				ResumeTransformations.AddDisponibility,
				ResumeTransformations.AddExperience,
				ResumeTransformations.AddLastJob,
				ResumeTransformations.AddMinCltValue,
				ResumeTransformations.AddMinPjValue,
				ResumeTransformations.AddObservations
				,ResumeTransformations.CreateLoginAndSession,
				JnServiceLogin.ValidateLogin
				)
		.getJsonPiece(
				VisEntityResume.Fields.notAllowedCompany
				,VisJsonCommonsFields.disponibility
				,VisEntityResume.Fields.desiredJob
				,VisJsonCommonsFields.experience
				,VisEntityResume.Fields.lastJob
				,VisJsonCommonsFields.email
				,VisJsonCommonsFields.clt
				,VisJsonCommonsFields.btc
				,VisJsonCommonsFields.ddd
				,VisJsonCommonsFields.pj
				,JsonFieldNames.originalEmail
				,JsonFieldNames.resumeBase64
				,JsonFieldNames.observations
				,JsonFieldNames.fileName
				,JsonFieldNames.name
				);
	
//		SyncServiceVisResume.INSTANCE.save(resume);
		
		String email = candidate.getAsString(JsonFieldNames.id);
		CcpJsonRepresentation put = resume.put(VisJsonCommonsFields.email, email)
				.put(JsonFieldNames.language, JnLanguage.portuguese)
				;
		
		VisServiceResume.Save.execute(put.content);
		
		Integer status = candidate.getAsIntegerNumber(JsonFieldNames.status);
		
		boolean inactiveResume = Integer.valueOf(0).equals(status);
		
		if(inactiveResume) {
//			SyncServiceVisResume.INSTANCE.changeStatus(resume);
		}
	}
}
