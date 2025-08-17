package com.vis.resumes;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.query.CcpDbQueryOptions;
import com.ccp.especifications.db.query.CcpQueryExecutor;
import com.jn.business.login.JnBusinessSessionValidate;
import com.vis.entities.VisEntityResume;
public class ImportResumeFromOldJobsNow implements Consumer<CcpJsonRepresentation>{
	enum JsonFieldNames implements CcpJsonFieldName{
		id, curriculo, conteudo, resumeBase64, arquivo, fileName, disponibilidade, profissaoDesejada, empresas, ultimaProfissao, experiencia, pretensaoClt, pretensaoPj, bitcoin, observacao, observations, name, originalEmail, status
	}

	public static final ImportResumeFromOldJobsNow INSTANCE = new ImportResumeFromOldJobsNow();
	private Set<String> ids;			
	int contador;

	private ImportResumeFromOldJobsNow() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpDbQueryOptions query = 
				CcpDbQueryOptions.INSTANCE
					.matchAll()
					.maxResults()
				;
		String[] resourcesNames = VisEntityResume.ENTITY.getEntitiesToSelect();
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
		.renameField(JsonFieldNames.disponibilidade, VisEntityResume.Fields.disponibility)
		.renameField(JsonFieldNames.profissaoDesejada, VisEntityResume.Fields.desiredJob)
		.renameField(JsonFieldNames.empresas,VisEntityResume.Fields.companiesNotAllowed)
		.renameField(JsonFieldNames.ultimaProfissao, VisEntityResume.Fields.lastJob)
		.renameField(JsonFieldNames.experiencia, VisEntityResume.Fields.experience)
		.renameField(JsonFieldNames.pretensaoClt, VisEntityResume.Fields.clt)
		.renameField(JsonFieldNames.pretensaoPj, VisEntityResume.Fields.pj)
		.renameField(JsonFieldNames.bitcoin, VisEntityResume.Fields.btc)
		.renameField(JsonFieldNames.observacao, JsonFieldNames.observations)
		.put(JsonFieldNames.name, "NOME DO CANDIDATO")
		.putAll(resumeFile)
		.copyIfNotContains(VisEntityResume.Fields.lastJob, VisEntityResume.Fields.desiredJob)
		.putIfNotContains(VisEntityResume.Fields.companiesNotAllowed, Arrays.asList())
		.putIfNotContains(VisEntityResume.Fields.disabilities, Arrays.asList())
		.putIfNotContains(VisEntityResume.Fields.disponibility, 0)
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
				ResumeTransformations.AddObservations,
				ResumeTransformations.CreateLoginAndSession,
				JnBusinessSessionValidate.INSTANCE
				)
		.getJsonPiece(
				VisEntityResume.Fields.companiesNotAllowed
				,VisEntityResume.Fields.disponibility
				,VisEntityResume.Fields.disabilities
				,VisEntityResume.Fields.desiredJob
				,VisEntityResume.Fields.experience
				,VisEntityResume.Fields.lastJob
				,VisEntityResume.Fields.email
				,VisEntityResume.Fields.clt
				,VisEntityResume.Fields.btc
				,VisEntityResume.Fields.ddd
				,VisEntityResume.Fields.pj
				,JsonFieldNames.originalEmail
				,JsonFieldNames.resumeBase64
				,JsonFieldNames.observations
				,JsonFieldNames.fileName
				,JsonFieldNames.name
				);
	
		System.out.println(resume);
		
//		SyncServiceVisResume.INSTANCE.save(resume);
		
		Integer status = candidate.getAsIntegerNumber(JsonFieldNames.status);
		
		boolean inactiveResume = Integer.valueOf(0).equals(status);
		
		if(inactiveResume) {
//			SyncServiceVisResume.INSTANCE.changeStatus(resume);
		}
	}
}
