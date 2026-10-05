package com.vis.resumes;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.query.CcpQueryExecutor;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.jn.services.JnServiceLogin;
import com.jn.utils.JnLanguage;
import com.vis.entities.VisEntityResume;
import com.vis.services.VisServiceResume;
import com.vis.json.fields.validation.VisJsonCommonsFields;

/**
 * One-off migration tool: converts each candidate of the old JobsNow (Portuguese field names) into a resume of the
 * visualization module, creating login and session for it, and saves it through {@code VisServiceResume.Save}.
 */
public class ImportResumeFromOldJobsNow implements Consumer<CcpJsonRepresentation>{
	/** Fields of the old candidate and of the converted resume. */
	enum JsonFieldNames implements CcpJsonFieldName{
		/** The {@code id} field. */
		id,
		/** The {@code curriculo} field. */
		curriculo,
		/** The {@code conteudo} field. */
		conteudo,
		/** The {@code resumeBase64} field. */
		resumeBase64,
		/** The {@code arquivo} field. */
		arquivo,
		/** The {@code fileName} field. */
		fileName,
		/** The {@code disponibilidade} field. */
		disponibilidade,
		/** The {@code profissaoDesejada} field. */
		profissaoDesejada,
		/** The {@code empresas} field. */
		empresas,
		/** The {@code ultimaProfissao} field. */
		ultimaProfissao,
		/** The {@code experiencia} field. */
		experiencia,
		/** The {@code pretensaoClt} field. */
		pretensaoClt,
		/** The {@code pretensaoPj} field. */
		pretensaoPj,
		/** The {@code bitcoin} field. */
		bitcoin,
		/** The {@code observacao} field. */
		observacao,
		/** The {@code observations} field. */
		observations,
		/** The {@code name} field. */
		name,
		/** The {@code originalEmail} field. */
		originalEmail,
		/** The {@code status} field. */
		status,
		/** The {@code language} field. */
		language
	}

	/** The single instance. */
	public static final ImportResumeFromOldJobsNow INSTANCE = new ImportResumeFromOldJobsNow();
	/** Ids of the resumes already present when the tool started. */
	private Set<String> ids;			
	/** How many candidates were handled. */
	int counter;

	/** Loads the ids of the resumes already present. */
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
	
	/**
	 * Converts and saves one candidate (the check of already imported candidates is disabled).
	 * @param candidate the old candidate
	 */
	public void accept(CcpJsonRepresentation candidate) {
		
		boolean alreadyInserted = this.counter++ < this.ids.size();
		
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
		CcpJsonRepresentation resumeWithEmailAndLanguage = resume.put(VisJsonCommonsFields.email, email)
				.put(JsonFieldNames.language, JnLanguage.portuguese)
				;
		
		VisServiceResume.Save.execute(resumeWithEmailAndLanguage.content);
		
		Integer status = candidate.getAsIntegerNumber(JsonFieldNames.status);
		
		boolean inactiveResume = Integer.valueOf(0).equals(status);
		
		if(inactiveResume) {
//			SyncServiceVisResume.INSTANCE.changeStatus(resume);
		}
	}
}
