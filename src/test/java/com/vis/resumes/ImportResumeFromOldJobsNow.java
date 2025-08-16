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
 enum ImportResumeFromOldJobsNowConstants  implements CcpJsonFieldName{
	id, curriculo, conteudo, resumeBase64, arquivo, fileName, disponibilidade, profissaoDesejada, empresas, ultimaProfissao, experiencia, pretensaoClt, pretensaoPj, bitcoin, observacao, observations, name, originalEmail, status
	 
 }
public class ImportResumeFromOldJobsNow implements Consumer<CcpJsonRepresentation>{
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
		this.ids = queryExecutor.getResultAsList(query, resourcesNames, "email").stream().map(x -> x.getAsString(ImportResumeFromOldJobsNowConstants.id)).collect(Collectors.toSet());
	}
	
	public void accept(CcpJsonRepresentation candidate) {
		
		boolean alreadyInserted = this.contador++ < this.ids.size();
		
		if(alreadyInserted) {
//			return;
		}
		
		CcpJsonRepresentation resumeFile = candidate.getInnerJson(ImportResumeFromOldJobsNowConstants.curriculo)
				.renameField(ImportResumeFromOldJobsNowConstants.conteudo, ImportResumeFromOldJobsNowConstants.resumeBase64)
				.renameField(ImportResumeFromOldJobsNowConstants.arquivo, ImportResumeFromOldJobsNowConstants.fileName)
				.getJsonPiece(ImportResumeFromOldJobsNowConstants.resumeBase64, ImportResumeFromOldJobsNowConstants.fileName)
		;
		CcpJsonRepresentation resume = candidate
		.renameField(ImportResumeFromOldJobsNowConstants.disponibilidade, VisEntityResume.Fields.disponibility)
		.renameField(ImportResumeFromOldJobsNowConstants.profissaoDesejada, VisEntityResume.Fields.desiredJob)
		.renameField(ImportResumeFromOldJobsNowConstants.empresas,VisEntityResume.Fields.companiesNotAllowed)
		.renameField(ImportResumeFromOldJobsNowConstants.ultimaProfissao, VisEntityResume.Fields.lastJob)
		.renameField(ImportResumeFromOldJobsNowConstants.experiencia, VisEntityResume.Fields.experience)
		.renameField(ImportResumeFromOldJobsNowConstants.pretensaoClt, VisEntityResume.Fields.clt)
		.renameField(ImportResumeFromOldJobsNowConstants.pretensaoPj, VisEntityResume.Fields.pj)
		.renameField(ImportResumeFromOldJobsNowConstants.bitcoin, VisEntityResume.Fields.btc)
		.renameField(ImportResumeFromOldJobsNowConstants.observacao, ImportResumeFromOldJobsNowConstants.observations)
		.put(ImportResumeFromOldJobsNowConstants.name, "NOME DO CANDIDATO")
		.putAll(resumeFile)
		.copyIfNotContains(VisEntityResume.Fields.lastJob, VisEntityResume.Fields.desiredJob)
		.putIfNotContains(VisEntityResume.Fields.companiesNotAllowed, Arrays.asList())
		.putIfNotContains(VisEntityResume.Fields.disabilities, Arrays.asList())
		.putIfNotContains(VisEntityResume.Fields.disponibility, 0)
		.putIfNotContains(VisEntityResume.Fields.desiredJob, "-")
		.putIfNotContains(VisEntityResume.Fields.lastJob, "-")
		.putIfNotContains(ImportResumeFromOldJobsNowConstants.observations, "-")
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
				,ImportResumeFromOldJobsNowConstants.originalEmail
				,ImportResumeFromOldJobsNowConstants.resumeBase64
				,ImportResumeFromOldJobsNowConstants.observations
				,ImportResumeFromOldJobsNowConstants.fileName
				,ImportResumeFromOldJobsNowConstants.name
				);
	
		System.out.println(resume);
		
//		SyncServiceVisResume.INSTANCE.save(resume);
		
		Integer status = candidate.getAsIntegerNumber(ImportResumeFromOldJobsNowConstants.status);
		
		boolean inactiveResume = Integer.valueOf(0).equals(status);
		
		if(inactiveResume) {
//			SyncServiceVisResume.INSTANCE.changeStatus(resume);
		}
	}
}
