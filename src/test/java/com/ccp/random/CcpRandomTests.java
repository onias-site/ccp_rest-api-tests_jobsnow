package com.ccp.random;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpCollectionDecorator;
import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpFolderDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.query.CcpDbQueryOptions;
import com.ccp.especifications.db.query.CcpQueryExecutor;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.db.utils.CcpEntityCrudOperationType;
import com.ccp.especifications.db.utils.CcpEntityField;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.json.validations.global.engine.CcpJsonValidatorEngine;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.business.login.JnBusinessExecuteLogout;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.mensageria.JnFunctionMensageriaSender;
import com.jn.utils.JnDeleteKeysFromCache;
import com.vis.entities.VisEntityResume;

enum Fields implements CcpJsonFieldName{
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnEntityLoginAnswers.Fields.class)
	channel,
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	userAgent,
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	ip,
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	email,
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnEntityLoginAnswers.Fields.class)
	goal;
}
public class CcpRandomTests {
	enum JsonFieldNames implements CcpJsonFieldName{
		type, cause, stackTrace, email, mappings, properties, name, ddd, _id, docs, _source, id, mail, contato, vaga, channel, description, contactChannel, candidate, candidato
	}
	static {
		CcpDependencyInjection.loadAllDependencies(new CcpElasticSearchQueryExecutor(), new CcpElasticSearchDbRequest(),
				new CcpMindrotPasswordHandler(), new CcpElasticSerchDbBulk(), CcpLocalCacheInstances.map,
				new CcpElasticSearchCrud(), new CcpGsonJsonHandler(), new CcpApacheMimeHttp());
	}

	static Map<String, Object> getJson(CcpFileDecorator arquivo){
		boolean file = arquivo.isFile();
		Map<String, Object> json = new LinkedHashMap<>();
		
		if(file) {
			CcpJsonRepresentation asSingleJson = arquivo.asSingleJson();
			return asSingleJson.content;
		}
		CcpFolderDecorator asFolder = arquivo.asFolder();
		asFolder.readFiles(subFile -> {
			Map<String, Object> subJson = getJson(subFile);
			json.put(subFile.getName().replace(".json", ""), subJson);
		});
		return json;
	}
	
	public static void main(String[] args) {
		CcpJsonRepresentation json = new CcpJsonRepresentation("{\r\n"
				+ "      \"goal\": \"jobs\",\r\n"
				+ "      \"channel\": \"linkedin\",\r\n"
				+ "      \"userAgent\": \"Apache-HttpClient/4.5.4 (Java/17.0.9)\",\r\n"
				+ "      \"email\": \"6i0f5xb0@teste.com\",\r\n"
				+ "      \"ip\": \"127.0.0.1\"\r\n"
				+ "    }");
		CcpJsonValidatorEngine.INSTANCE.validateJson(Fields.class, json, "teste");
	}

	 static void qualquerCoisa() {
		Field[] declaredFields = JnEntityJobsnowError.Fields.class.getDeclaredFields();
		for (Field field : declaredFields) {
			System.out.println(field.getName());
		}
	}

	static void mudarLocalDoArquivo() {
		CcpFolderDecorator folder = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\github").folder();

		folder.readFiles(file -> {
			String oldName = file.content + "\\.gitignore";
			String newName = oldName.replace("github", "");
			new CcpStringDecorator(oldName).file().rename(newName);
		});
	}

	static void testarExpurgable2() {
		CcpJsonRepresentation json = new CcpJsonRepresentation("{\r\n" + "  \"email\": \"onias85@gmail.com\",\r\n"
				+ "  \"ip\": \"127.0.0.1\",\r\n" + "  \"password\": \"Jobsnow1!\",\r\n"
				+ "  \"token\": \"M6ZRDQ83\",\r\n" + "  \"originalToken\": \"M6ZRDQ83\",\r\n"
				+ "  \"userAgent\": \"Apache-HttpClient/4.5.4 (Java/17.0.9)\"\r\n" + "}");
		CcpEntity entity = JnEntityLoginSessionValidation.ENTITY;
		testarExpurgable(json, entity);
	}

	static void testarExpurgable() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.type, "teste")
				.put(JsonFieldNames.stackTrace, "teste")
				.put(JsonFieldNames.cause, "teste");
		CcpEntity entity = JnEntityJobsnowError.ENTITY;
		testarExpurgable(json, entity);
	}

	private static void testarExpurgable(CcpJsonRepresentation json, CcpEntity entity) {
		entity.createOrUpdate(json);
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		for (int k = 0; k < 60; k++) {
			CcpTimeDecorator ctd = new CcpTimeDecorator();
			System.out.println(
					"Exists: " + ctd.getFormattedDateTime("dd/MM/yyyy HH:mm:ss.SSS") + " = " + entity.exists(json));
			CcpSelectUnionAll unionAll = crud.unionAll(new CcpJsonRepresentation[] { json },
					JnDeleteKeysFromCache.INSTANCE, entity);
			System.out.println("unionAll1: " + ctd.getFormattedDateTime("dd/MM/yyyy HH:mm:ss.SSS") + " = "
					+ entity.isPresentInThisUnionAll(unionAll, json));
			ctd.sleep(60000);
		}

	}

	static void testarSalvamentoDeSenha() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityLoginPassword.Fields.password, "123456").put(JsonFieldNames.email, "onias85@gmail.com");
		JnEntityLoginPassword.ENTITY.createOrUpdate(json);

		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpSelectUnionAll unionAll = crud.unionAll(new CcpJsonRepresentation[] { json }, JnDeleteKeysFromCache.INSTANCE,
				JnEntityLoginPassword.ENTITY);
		System.out.println(unionAll);

		System.out.println(com.ccp.dependency.injection.CcpDependencyInjection
				.getDependency(com.ccp.especifications.password.CcpPasswordHandler.class)
				.matches("123456", "$2a$12$mjfndltYxA2TsM9Eo8rnSOaNCr3QTTerfoVcj5ucAGO5C/vavQofC"));
		System.out.println(com.ccp.dependency.injection.CcpDependencyInjection
				.getDependency(com.ccp.especifications.password.CcpPasswordHandler.class)
				.matches("123456", "$2a$12$FYwjF4ysRKHCwg9cp1H/meLBRLeevbDlT5ZQvoSGQX6D1osAtWVde"));
	}

	static void extracted() {
		CcpFolderDecorator folder = new CcpStringDecorator(
				"documentation/database/elasticsearch/scripts/entities/create").folder();
		Map<String, List<String>> map = new TreeMap<>();
		folder.readFiles(file -> {
			String fileName = file.getName();
			String javaClassName = "JnEntity" + new CcpStringDecorator(fileName).text().toCamelCase().toString();
			CcpJsonRepresentation json = file.asSingleJson().getInnerJsonFromPath(JsonFieldNames.mappings, JsonFieldNames.properties);
			Set<String> fields = json.fieldSet();
			for (String field : fields) {
				List<String> javaClassesName = map.getOrDefault(field, new ArrayList<>());
				javaClassesName.add(javaClassName);
				map.put(field, javaClassesName);
			}
		});
		CcpFileDecorator reset = new CcpStringDecorator("c:\\logs\\fields.txt").file().reset();
		Set<String> keySet = map.keySet();
		for (String string : keySet) {
			List<String> list = map.get(string);
			reset.append(string + " = " + list);
		}
	}

	static void testarValidacoes() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.name, "L")
				.put(JsonFieldNames.ddd, 20);
		try {

			new JnFunctionMensageriaSender(VisEntityResume.ENTITY, CcpEntityCrudOperationType.save).apply(json);
		} catch (CcpJsonValidationError e) {
			new CcpStringDecorator("C:\\logs\\errosDeCurriculo.json").file().reset().append(e.json.asPrettyJson());
		}
	}

	static void metodoDoLucas() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.email, "onias85@gmail.com")

		;
		List<CcpJsonRepresentation> parametersToSearch = JnEntityLoginSessionValidation.ENTITY
				.getParametersToSearch(json);

		System.out.println(parametersToSearch);

		JnEntityLoginSessionValidation.ENTITY.getTwinEntity().createOrUpdate(json);
		JnBusinessExecuteLogout.INSTANCE.apply(json);
	}

	static void testarExpurgableEntity() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames.cause, new CcpJsonRepresentation("{'nome':'onias'}")).put(JsonFieldNames.stackTrace, "{'nome':'vieira'}")
				.put(JsonFieldNames.type, "any");
		CcpJsonRepresentation oneById = JnEntityJobsnowError.ENTITY.getOneById(json,
				CcpOtherConstants.RETURNS_EMPTY_JSON);
		System.out.println(oneById);
	}

	static void criarArquivoDeVagas() {
		CcpDbQueryOptions queryMatchAll = CcpDbQueryOptions.INSTANCE.matchAll();
		queryMatchAll.startAggregations().startBucket("x", null, 1).startAggregations().addAvgAggregation(null, null);
		Set<Object> emailsDasVisualizacoes = getEmails(queryMatchAll, "visualizacao_de_curriculo", "email");
		Set<Object> emailsDasVagas = getEmails(queryMatchAll, "vagas", "email", "mail");

		List<Object> intersectList = new CcpCollectionDecorator(emailsDasVisualizacoes)
				.getIntersectList(emailsDasVagas);

		CcpJsonRepresentation mgetJson = CcpOtherConstants.EMPTY_JSON;
		CcpEntityField idField = new CcpEntityField("email", false, CcpOtherConstants.DO_NOTHING);

		CcpDbQueryOptions queryToSearchViews = CcpDbQueryOptions.INSTANCE.startSimplifiedQuery()
				.terms(idField, intersectList).endSimplifiedQueryAndBackToRequest();
		Set<Object> candidatos = getEmails(queryToSearchViews, "visualizacao_de_curriculo", "candidato", "candidate");
		CcpJsonRepresentation template = new CcpJsonRepresentation("{\r\n" + "    \"_index\": \"profissionais2\",\r\n"
				+ "    \"_source\": {\r\n" + "        \"include\": [\r\n" + "            \"curriculo.arquivo\",\r\n"
				+ "            \"pretensaoPj\",\r\n" + "            \"pretensaoClt\",\r\n" + "            \"pcd\",\r\n"
				+ "            \"mudanca\",\r\n" + "            \"homeoffice\",\r\n"
				+ "            \"experiencia\",\r\n" + "            \"disponibilidade\",\r\n"
				+ "            \"ddd\",\r\n" + "            \"bitcoin\"\r\n" + "        ]\r\n" + "    }\r\n" + "}");
		for (Object candidato : candidatos) {
			CcpJsonRepresentation doc = template.put(JsonFieldNames._id, candidato);
			mgetJson = mgetJson.addToList("docs", doc);
		}

		CcpHttpResponse executeHttpRequest = CcpDependencyInjection.getDependency(CcpHttpRequester.class)
				.executeHttpRequest("http://localhost:9200/_mget", CcpHttpMethods.POST, CcpOtherConstants.EMPTY_JSON,
						mgetJson.asUgglyJson());
		CcpJsonRepresentation asSingleJson = executeHttpRequest.asSingleJson();

		List<CcpJsonRepresentation> collect = asSingleJson.getAsJsonList(JsonFieldNames.docs).stream().map(json -> {
			String id = json.getAsString(JsonFieldNames._id);
			CcpJsonRepresentation source = json.getInnerJson(JsonFieldNames._source);
			CcpJsonRepresentation put = source.put(JsonFieldNames.id, id);
			return put;
		}).collect(Collectors.toList());

		CcpJsonRepresentation resumes = CcpOtherConstants.EMPTY_JSON;

		for (CcpJsonRepresentation curriculo : collect) {
			String id = curriculo.getAsString(JsonFieldNames.id);
			resumes = resumes.getDynamicVersion().put(id, curriculo);
		}

		CcpJsonRepresentation candidatosAgrupadosPorRecrutadores = getCandidatosAgrupadosPorRecrutadores(queryMatchAll);
		CcpJsonRepresentation vagasAgrupadosPorRecrutadores = getVagasAgrupadosPorRecrutadores(intersectList);
		Set<String> recrutadores = vagasAgrupadosPorRecrutadores.fieldSet();
		List<CcpJsonRepresentation> todasAsVagas = new ArrayList<>();
		CcpJsonRepresentation res = new CcpJsonRepresentation(resumes.content);
		for (String recrutador : recrutadores) {
			List<CcpJsonRepresentation> curriculos = candidatosAgrupadosPorRecrutadores.getDynamicVersion().getAsStringList(recrutador)
					.stream().map(x -> res.getDynamicVersion().getInnerJson(x)).collect(Collectors.toList());

			List<CcpJsonRepresentation> vagas = vagasAgrupadosPorRecrutadores.getDynamicVersion().getAsJsonList(recrutador);

			int k = 0;
			for (CcpJsonRepresentation curriculo : curriculos) {
				CcpJsonRepresentation vaga = vagas.get(k++ % vagas.size());
				CcpJsonRepresentation putAll = vaga.putAll(curriculo);
				todasAsVagas.add(putAll);
			}
		}
		CcpFileDecorator vagasFile = new CcpStringDecorator("c:\\logs\\vagas.json").file().reset();
		vagasFile.append(todasAsVagas.toString());
	}

	static class AgruparVagasPorRecrutadores implements java.util.function.Consumer<CcpJsonRepresentation> {

		CcpJsonRepresentation vagasAgrupadasPorRecrutadores = CcpOtherConstants.EMPTY_JSON;

		public void accept(CcpJsonRepresentation json) {

			String recrutador = json.getAsObject(JsonFieldNames.mail);
			String contato = json.getAsString(JsonFieldNames.contato);
			String texto = json.getAsString(JsonFieldNames.vaga);
			String contactChannel = new CcpStringDecorator(contato.trim()).email().isValid() ? "email" : "link";

			CcpJsonRepresentation vaga = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.channel, contato).put(JsonFieldNames.email, recrutador)
					.put(JsonFieldNames.description, texto).put(JsonFieldNames.contactChannel, contactChannel);

			this.vagasAgrupadasPorRecrutadores = this.vagasAgrupadasPorRecrutadores.addToList(recrutador, vaga);
		}

	}

	static class AgruparCandidatosPorRecrutadores implements java.util.function.Consumer<CcpJsonRepresentation> {

		CcpJsonRepresentation candidatosAgrupadosPorRecrutadores = CcpOtherConstants.EMPTY_JSON;

		public void accept(CcpJsonRepresentation json) {
			String candidato = json.getAsObject(JsonFieldNames.candidate, JsonFieldNames.candidato);
			String recrutador = json.getAsString(JsonFieldNames.email);
			this.candidatosAgrupadosPorRecrutadores = this.candidatosAgrupadosPorRecrutadores.addToList(recrutador,
					candidato);
		}
	}

	static CcpJsonRepresentation getVagasAgrupadosPorRecrutadores(List<Object> intersectList) {
		CcpEntityField idField = new CcpEntityField("mail", false, CcpOtherConstants.DO_NOTHING);
		CcpDbQueryOptions query = CcpDbQueryOptions.INSTANCE.startSimplifiedQuery().terms(idField, intersectList)
				.endSimplifiedQueryAndBackToRequest();

		String[] resourcesNames = new String[] { "vagas" };
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		AgruparVagasPorRecrutadores consumer = new AgruparVagasPorRecrutadores();
		queryExecutor.consumeQueryResult(query, resourcesNames, "10s", 10000, consumer, "contato", "vaga", "mail");
		return consumer.vagasAgrupadasPorRecrutadores;
	}

	static CcpJsonRepresentation getCandidatosAgrupadosPorRecrutadores(CcpDbQueryOptions query) {

		String[] resourcesNames = new String[] { "visualizacao_de_curriculo" };
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);

		AgruparCandidatosPorRecrutadores consumer = new AgruparCandidatosPorRecrutadores();
		queryExecutor.consumeQueryResult(query, resourcesNames, "10s", 10000, consumer, "candidate", "candidato",
				"email");
		return consumer.candidatosAgrupadosPorRecrutadores;
	}

	static Set<Object> getEmails(CcpDbQueryOptions query, String tabela, String... fields) {
		String[] resourcesNames = new String[] { tabela };
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		Set<Object> set = new HashSet<>();
		queryExecutor.consumeQueryResult(query, resourcesNames, "10s", 10000, json -> {
			for (String field : fields) {

				String value = json.getDynamicVersion().getAsTextDecorator(field).content.trim().toLowerCase();

				if (value.isEmpty()) {
					continue;
				}
				set.add(value);
			}

		}, fields);
		return set;
	}

	static void excluirCurriculo() {
		CcpHttpResponse executeHttpRequest = CcpDependencyInjection.getDependency(CcpHttpRequester.class)
				.executeHttpRequest("http://localhost:9200/profissionais2/_doc/lucascavalcantedeo@gmail.com",
						CcpHttpMethods.DELETE, CcpOtherConstants.EMPTY_JSON, "");
		System.out.println(executeHttpRequest);
	}

	static void testarTempo() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames.cause, new CcpJsonRepresentation("{'nome':'onias'}"))
				.put(JsonFieldNames.stackTrace, "{'nome':'vieira'}")
				.put(JsonFieldNames.type, "any");
		JnEntityJobsnowError.ENTITY.delete(json);
		JnEntityJobsnowError.ENTITY.createOrUpdate(json);
		while (true) {
			boolean exists = JnEntityJobsnowError.ENTITY.exists(json);
			if (exists == false) {
				JnEntityJobsnowError.ENTITY.createOrUpdate(json);
				System.out.println(new CcpTimeDecorator().getFormattedDateTime("dd/MM/yyyy HH:mm:ss.SSS"));
			}
			new CcpTimeDecorator().sleep(1000);

		}
	}

	static void errarInfinitamente() {
		CcpTimeDecorator ccpTimeDecorator = new CcpTimeDecorator();
		CcpHttpRequester dependency = CcpDependencyInjection.getDependency(CcpHttpRequester.class);

		while (true) {
			dependency.executeHttpRequest("http://localhost:8080/login/r066u1bd@teste.com", CcpHttpMethods.GET,
					CcpOtherConstants.EMPTY_JSON, "");
			ccpTimeDecorator.sleep(60_000);
		}
	}

	static int counter;

	static void salvarVagaDoJobsNowAntigo() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpDbQueryOptions queryToSearchLastUpdatedResumes = CcpDbQueryOptions.INSTANCE.matchAll();
		CcpFileDecorator file = new CcpStringDecorator("vagas.txt").file();
		String[] resourcesNames = new String[] { "vagas" };
		queryExecutor.consumeQueryResult(queryToSearchLastUpdatedResumes, resourcesNames, "10s", 10000, vaga -> {
			String texto = vaga.getAsString(JsonFieldNames.vaga).replace("\n", "").trim();
			String completeLeft = new CcpStringDecorator("" + ++counter).text().completeLeft('0', 6).content;
			file.append(completeLeft + ": " + texto);
//					CcpTimeDecorator.appendLog(counter);
		}, "vaga");
	}

}

abstract class Pai {
	abstract void a();
}

class Filho1 extends Pai {
	void a() {

	}
}

class Filho2 extends Pai {
	void a() {

	}
}

interface MinhaInterface {
	String meuMetodo(String p1, String p2);
}

class Pessoa {
	final int idade;
	final String nome;

	public Pessoa(int idade, String nome) {
		this.idade = idade;
		this.nome = nome;
	}

	@Override
	public String toString() {
		return "Pessoa [idade=" + idade + ", nome=" + nome + "]";
	}
}

class A {
	Object a;
}
class B extends A{
	static Object b;
}
