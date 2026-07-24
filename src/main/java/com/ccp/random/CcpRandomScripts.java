package com.ccp.random;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpCollectionDecorator;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpFolderDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTextDecorator.CcpTemplateFunctions;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.query.CcpQueryExecutor;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityField;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.http.CcpHttpContentType;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.especifications.password.CcpPasswordHandler;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.instant.messenger.telegram.CcpTelegramInstantMessenger;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.implementations.text.extractor.apache.tika.CcpApacheTikaTextExtractor;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.global.engine.CcpJsonValidatorEngine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.business.login.JnBusinessExecuteLogout;
import com.jn.business.messages.JnBusinessSendInstantMessage;
import com.jn.entities.JnEntityDisposableTest;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginToken;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.vis.entities.VisEntityResume;
import com.vis.resumes.ImportResumeFromOldJobsNow;

public class CcpRandomScripts {

	static enum JsonFields implements CcpJsonFieldName{
		skill, 
		word, 
		parent, 
		synonym, 
		similar, 
		preRequisite, 
		positionsCount, 
		id, 
	}
	static CcpJsonRepresentation groupedCompanies = CcpOtherConstants.EMPTY_JSON;
	private static final String LINKEDIN_REGEX = "^https://(www\\.)?linkedi n\\.com/in/[a-zA-Z0-9-_%]+/?$";
	
	public static boolean isValidLinkedInUrl(String url) {
		return Pattern.matches(LINKEDIN_REGEX, url);
	}

	public static void main(String[] args) {
		JnEntityLoginAnswers.ENTITY.delete(CcpOtherConstants.EMPTY_JSON.put(() -> "email", "onias85@gmail.com"));
	}

	static void createVisEntities() {
		String pathToCreateEntityScript = "documentation\\vis\\database\\elasticsearch\\scripts\\entities\\create";
		String pathToJavaClasses = "..\\vis_business_jobsnow\\src\\main\\java\\com\\vis\\entities";
		String mappingJnEntitiesErrors = "c:\\logs\\mappingJnEntitiesErrors.json";
		String insertErrors = "c:\\logs\\insertErrors.json";
		CcpDbRequester database = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		database.createTables(pathToCreateEntityScript, pathToJavaClasses, mappingJnEntitiesErrors, insertErrors);
	}

	static void fodasse() {
		CcpDependencyInjection.loadAllDependencies(new CcpTelegramInstantMessenger());
		CcpTemplateFunctions.currentTimeMillis.get();
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnBusinessSendInstantMessage.JnJsonValidator.chatId, 751717896L)
				.put(JnBusinessSendInstantMessage.JnJsonValidator.templateId, "teste")
				.put(JnBusinessSendInstantMessage.JnMessageFileJsonValidator.fileName, "{chatId}.txt")
				.put(JnBusinessSendInstantMessage.JnMessageFileJsonValidator.caption, "{templateId}.{currentTimeMillis()}")
				.put(JnBusinessSendInstantMessage.JnMessageFileJsonValidator.contentType, CcpHttpContentType.TEXT_PLAIN)
				.put(JnBusinessSendInstantMessage.JnMessageFileJsonValidator.message, "mensagem de teste")
				.put(JnBusinessSendInstantMessage.JnJsonValidator.botName, JnBusinessSendInstantMessage.JnBotType.support)
				.put(JnBusinessSendInstantMessage.JnJsonValidator.instantMessageType, JnBusinessSendInstantMessage.JnInstantMessageType.text)
				;
		
		boolean exists = JnEntityInstantMessengerMessageSent.ENTITY.exists(json);
		System.out.println(exists);
	}

	static void enviarArquivoPorTelegram() {
		CcpDependencyInjection.loadAllDependencies(new CcpTelegramInstantMessenger());
		CcpInstantMessenger dependency = CcpDependencyInjection.getDependency(CcpInstantMessenger.class);
		
		CcpJsonRepresentation sendFile = dependency.sendFile(CcpOtherConstants.EMPTY_STRING, "1154866992:AAGvXIU01UXgpA1gFOBE4pJXjhicf7JnRd8", 751717896L, 0L, "teste.txt", "legenda",
				new CcpStringDecorator("teste do tio onias").getBytes());
		
		System.out.println(sendFile);
	}

	static void testarDisposable() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityDisposableTest.Fields.email, "onias85@gmail.com")
				;
		CcpEntity entity = JnEntityDisposableTest.ENTITY;
		entity.save(json);
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class); 
		for(int k = 0; k < 70; k++) {
			CcpSelectUnionAll unionAll = crud.unionAll(json, JnDeleteKeysFromCache.INSTANCE, entity);
			CcpTimeDecorator ctd = new CcpTimeDecorator();
			String formattedDateTime = CcpEntityExpurgableOptions.millisecond.getFormattedDate();
			Supplier<CcpJsonRepresentation> jsonSupplier = json.getJsonSupplier();
			var exists = entity.getRecordFromUnionAll(unionAll, jsonSupplier);
			System.out.println(formattedDateTime + ": " + exists);
			ctd.sleep(60*1000);
		}
	}

	
	static void sanitizarArquivoDeSinonimos() {
		List<String> removidas = new CcpStringDecorator("c:/logs/skills/removidas.txt").file().getLines().stream().map(x -> x.trim().split(" = ")[0]).collect(Collectors.toList());
		CcpFileDecorator synonymsFile = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\skills\\synonyms.json").file();
		List<CcpJsonRepresentation> synonyms = synonymsFile.asJsonList();
		List<CcpJsonRepresentation> newSynonyms = new ArrayList<>();
		
		for (CcpJsonRepresentation synonym : synonyms) {
			
				String skill = synonym.getAsString(JsonFields.skill);
				if(skill.trim().length() < 2) {
					synonym = synonym.removeFields(JsonFields.skill);
				}
				if(skill.trim().length() > 50) {
					synonym = synonym.removeFields(JsonFields.skill);
				}
				if(removidas.contains(skill)) {
					synonym = synonym.removeFields(JsonFields.skill);
				}
				List<String> parent = synonym.getAsStringList(JsonFields.parent).stream()
						.filter(x -> x.length() > 2 && x.length() <= 50)
						.filter(x -> false == removidas.contains(x))
						.collect(Collectors.toList());
				
				
				synonym = synonym.put(JsonFields.parent, parent);
				{
					List<CcpJsonRepresentation> collect = synonym.getAsJsonList(JsonFields.synonym).stream().filter(x -> {
						String word = x.getAsString(JsonFields.skill);
						return word.length() > 2 && word.length() <= 50 && false == removidas.contains(word) && false == skill.equals(word);
					}).collect(Collectors.toList());

					synonym = synonym.put(JsonFields.synonym, collect);
				}
				{
					List<CcpJsonRepresentation> collect = synonym.getAsJsonList(JsonFields.preRequisite).stream().filter(x -> {
						String word = x.getAsString(JsonFields.word);
						return word.length() > 2 && word.length() <= 50 && false == removidas.contains(word);
					}).collect(Collectors.toList());

					synonym = synonym.put(JsonFields.preRequisite, collect);
				}
				{
					List<CcpJsonRepresentation> collect = synonym.getAsJsonList(JsonFields.similar).stream().filter(x -> {
						String word = x.getAsString(JsonFields.word).replace("_", " ");
						return word.length() > 2 && word.length() <= 50 && false == removidas.contains(word);
					}).collect(Collectors.toList());

					synonym = synonym.put(JsonFields.similar, collect);
				}
				
				newSynonyms.add(synonym);
			}

		List<CcpJsonRepresentation> collect = newSynonyms.stream()
		.filter(x -> x.containsAllFields(JsonFields.skill) || false == x.getAsJsonList(JsonFields.synonym).isEmpty())
		.map(x -> x.containsAllFields(JsonFields.skill) ? x : transferSynonymToSkill(x))
		.map(x -> false == x.getAsObjectList(JsonFields.parent).isEmpty() ?  x :  x.removeFields(JsonFields.parent))
		.map(x -> false == x.getAsObjectList(JsonFields.similar).isEmpty() ?  x :  x.removeFields(JsonFields.similar))
		.map(x -> false == x.getAsObjectList(JsonFields.synonym).isEmpty() ?  x :  x.removeFields(JsonFields.synonym))
		.map(x -> false == x.getAsObjectList(JsonFields.preRequisite).isEmpty() ?  x :  x.removeFields(JsonFields.preRequisite))
		.collect(Collectors.toList());
		CcpFileDecorator newSynonymsFile = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\skills\\new_synonyms.json").file().reset();
		newSynonymsFile.append(collect.toString());
		for (CcpJsonRepresentation json : collect) {
			System.out.println(json.getAsString(JsonFields.skill));
		}
		System.out.println(collect.size());
	}
	
	static CcpJsonRepresentation transferSynonymToSkill(CcpJsonRepresentation x) {
		List<CcpJsonRepresentation> synonym = x.getAsJsonList(JsonFields.synonym);
		ArrayList<CcpJsonRepresentation> list = new ArrayList<>(synonym);
		list.sort((a,b) ->  b.getAsIntegerNumber(JsonFields.positionsCount) - a.getAsIntegerNumber(JsonFields.positionsCount));
		String skill = list.remove(0).getAsString(JsonFields.skill);
		CcpJsonRepresentation put = x.put(JsonFields.skill, skill).put(JsonFields.synonym, list);
		return put;
	}
	
	
	
	static void saveGroupedCompanies() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll();
		
		Consumer<CcpJsonRepresentation> consumer = json -> {
			String x = json.getAsString(JsonFields.id);
				String[] split = x.split("@");
				if(split.length != 2) {
					return;
				}
				
				
			String domain = split[1];
			
			String[] split1 = domain.split("\\.");			

			String companyName = split1[0].toUpperCase().trim();
			
			if(companyName.length() < 3) {
				return;
			}
			
			String capitalizedCompanyName = new CcpStringDecorator(companyName).text().capitalize().content;
			
			String initials = companyName.substring(0, 3);
			
			LinkedHashSet<String> orDefault = groupedCompanies.getOrDefault(new CcpFieldName(initials), () -> new LinkedHashSet<>());
			orDefault.add(capitalizedCompanyName);
			groupedCompanies = groupedCompanies.put(new CcpFieldName(initials), orDefault);
		};
		queryExecutor.consumeQueryResult(query, new String[] {"old_recruiters"}, "1s", 10000, consumer, "id");

		ArrayList<String> arrayList = new ArrayList<> (groupedCompanies.fieldSet());
		arrayList.sort((a, b) ->{
			Set<String> set1 = groupedCompanies.getAsObject(new CcpFieldName(a));
			Set<String> set2 = groupedCompanies.getAsObject(new CcpFieldName(b));
			return set2.size() - set1.size();
		});
		
		CcpJsonRepresentation result = CcpOtherConstants.EMPTY_JSON;
		int total = 0;
		for (String string : arrayList) {
			Set<String> value = groupedCompanies.getAsObject(new CcpFieldName(string));
			result = result.put(new CcpFieldName(string), value);
			int size = value.size();
			System.out.println(string +" = " +  size);
			total += size;
		}
		System.out.println(total);
		new CcpStringDecorator("c:\\logs\\teste.json").file().reset().append(result.asPrettyJson());
	}

	static void saveResume() {
		VisEntityResume.ENTITY.delete(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, "onias85@gmail.com"));
		String path = "http://localhost:9200/profissionais2/_doc/onias85@gmail.com/_source";
		CcpHttpHandler http = new CcpHttpHandler(200, CcpOtherConstants.DO_NOTHING, path);
		CcpHttpMethods method = CcpHttpMethods.GET;
		CcpJsonRepresentation headers = CcpOtherConstants.EMPTY_JSON;
		String asUgglyJson = "";
		CcpHttpResponse response = http.ccpHttp.executeHttpRequest(path, method, headers, asUgglyJson);
		CcpJsonRepresentation asSingleJson = response.asSingleJson();
		ImportResumeFromOldJobsNow.INSTANCE.accept(asSingleJson);
	}

	static void transferToReverseEntity() {
		CcpJsonRepresentation json = new CcpJsonRepresentation("{\r\n"
				+ "    \"userAgent\": \"Apache-HttpClient/4.5.4 (Java/17.0.9)\",\r\n"
				+ "    \"ip\": \"127.0.0.1\",\r\n"
				+ "    \"email\": \"exhhi8gp@teste.com\",\r\n"
				+ "    \"password\": \"Jobsnow1!\",\r\n"
				+ "    \"token\": \"4ISALLOL\"\r\n"
				+ "  }");
		JnEntityLoginToken.ENTITY.save(json);
		JnEntityLoginToken.ENTITY.delete(json);
	}

	static void saveLoginToken() {
		String value = "12345678";
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityLoginToken.Fields.email, "onias85@gmail.com")
				.put(JnEntityLoginToken.Fields.token, value)
				.put(JnEntityLoginSessionValidation.Fields.ip, "127.0.0.1")
				.put(JnEntityLoginSessionValidation.Fields.userAgent, "teste");
		JnEntityLoginToken.ENTITY.save(json);
		CcpJsonRepresentation oneById = JnEntityLoginToken.ENTITY.getOneById(json);
		String token = oneById.getAsString(JnEntityLoginToken.Fields.token);
		CcpPasswordHandler dependency = CcpDependencyInjection.getDependency(CcpPasswordHandler.class);
		System.out.println(dependency.matches(value, token));
	}

	enum JsonFieldNames implements CcpJsonFieldName{
		type, cause, stackTrace, email, mappings, properties, name, ddd, _id, docs, _source, id, mail, contato, vaga, channel, description, contactChannel, candidate, candidato
	}
	static {
		CcpDependencyInjection.loadAllDependencies(
				new CcpElasticSearchQueryExecutor(), 
				new CcpElasticSearchDbRequest(),
				CcpLocalInstances.mensageriaSender,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				new CcpApacheTikaTextExtractor(),
				new CcpMindrotPasswordHandler(), 
				new CcpElasticSerchDbBulk(), 
				CcpLocalCacheInstances.map,
				new CcpElasticSearchCrud(), 
				new CcpGsonJsonHandler(), 
				new CcpApacheMimeHttp()
				);
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
		testarExpurgable(json, JnEntityLoginSessionValidation.ENTITY);
	}

	static void testarExpurgable() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.type, "teste")
				.put(JsonFieldNames.stackTrace, "teste")
				.put(JsonFieldNames.cause, "teste");
		CcpEntity entity = JnEntityJobsnowError.ENTITY;
		testarExpurgable(json, entity);
	}

	private static void testarExpurgable(CcpJsonRepresentation json, CcpEntity entity) {
		entity.save(json);
		CcpJsonRepresentation[] jsons = new CcpJsonRepresentation[] { json };
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		for (int k = 0; k < 60; k++) {
			CcpTimeDecorator ctd = new CcpTimeDecorator();
			String formattedDateTime = ctd.getFormattedDateTime("dd/MM/yyyy HH:mm:ss.SSS");
			boolean exists = entity.exists(json);
			System.out.println("Exists: " + formattedDateTime + " = " + exists);
			CcpSelectUnionAll unionAll = crud.unionAll(jsons, JnDeleteKeysFromCache.INSTANCE, entity);
			boolean presentInThisUnionAll = entity.isPresentInThisUnionAll(unionAll, json);
			System.out.println("unionAll1: " + formattedDateTime + " = " + presentInThisUnionAll);
			ctd.sleep(1000);
		}

	}

	static void testarSalvamentoDeSenha() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityLoginPassword.Fields.password, "123456").put(JsonFieldNames.email, "onias85@gmail.com");
		JnEntityLoginPassword.ENTITY.save(json);

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
			VisEntityResume.ENTITY.save(json);
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

		JnEntityLoginSessionValidation.ENTITY.getTwinEntity().save(json);
		JnBusinessExecuteLogout.INSTANCE.apply(json);
	}

	static void testarExpurgableEntity() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames.cause, new CcpJsonRepresentation("{'nome':'onias'}")).put(JsonFieldNames.stackTrace, "{'nome':'vieira'}")
				.put(JsonFieldNames.type, "any");
		CcpJsonRepresentation oneById = JnEntityJobsnowError.ENTITY.getEntityMetaData().getOneByIdOrHandleItIfThisIdWasNotFound(json,
				CcpOtherConstants.RETURNS_EMPTY_JSON);
		System.out.println(oneById);
	}

	static void criarArquivoDeVagas() {
		CcpQueryOptions queryMatchAll = CcpQueryOptions.INSTANCE.matchAll();
		queryMatchAll.startAggregations().startBucket("x", null, 1).startAggregations().addAvgAggregation(null, null);
		Set<Object> emailsDasVisualizacoes = getEmails(queryMatchAll, "visualizacao_de_curriculo", "email");
		Set<Object> emailsDasVagas = getEmails(queryMatchAll, "vagas", "email", "mail");

		List<Object> intersectList = new CcpCollectionDecorator(emailsDasVisualizacoes)
				.getIntersectList(emailsDasVagas);

		CcpJsonRepresentation mgetJson = CcpOtherConstants.EMPTY_JSON;
		CcpEntityField idField = new CcpEntityField("email", false, true, CcpOtherConstants.DO_NOTHING);

		CcpQueryOptions queryToSearchViews = CcpQueryOptions.INSTANCE.startSimplifiedQuery()
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
			mgetJson = mgetJson.addToList(JsonFieldNames.docs, doc);
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
			resumes = resumes.put(new CcpFieldName(id), curriculo);
		}

		CcpJsonRepresentation candidatosAgrupadosPorRecrutadores = getCandidatosAgrupadosPorRecrutadores(queryMatchAll);
		CcpJsonRepresentation vagasAgrupadosPorRecrutadores = getVagasAgrupadosPorRecrutadores(intersectList);
		Set<String> recrutadores = vagasAgrupadosPorRecrutadores.fieldSet();
		List<CcpJsonRepresentation> todasAsVagas = new ArrayList<>();
		CcpJsonRepresentation res = new CcpJsonRepresentation(resumes.content);
		for (String recrutador : recrutadores) {
			List<CcpJsonRepresentation> curriculos = candidatosAgrupadosPorRecrutadores.getAsStringList(new CcpFieldName(recrutador))
					.stream().map(x -> res.getInnerJson(new CcpFieldName(x))).collect(Collectors.toList());

			List<CcpJsonRepresentation> vagas = vagasAgrupadosPorRecrutadores.getAsJsonList(new CcpFieldName(recrutador));

			int k = 0;
			for (CcpJsonRepresentation curriculo : curriculos) {
				CcpJsonRepresentation vaga = vagas.get(k++ % vagas.size());
				CcpJsonRepresentation putAll = vaga.mergeWithAnotherJson(curriculo);
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

			this.vagasAgrupadasPorRecrutadores = this.vagasAgrupadasPorRecrutadores.addToList(new CcpFieldName(recrutador), vaga);
		}

	}

	static class AgruparCandidatosPorRecrutadores implements java.util.function.Consumer<CcpJsonRepresentation> {

		CcpJsonRepresentation candidatosAgrupadosPorRecrutadores = CcpOtherConstants.EMPTY_JSON;

		public void accept(CcpJsonRepresentation json) {
			String candidato = json.getAsObject(JsonFieldNames.candidate, JsonFieldNames.candidato);
			String recrutador = json.getAsString(JsonFieldNames.email);
			this.candidatosAgrupadosPorRecrutadores = this.candidatosAgrupadosPorRecrutadores.addToList(new CcpFieldName(recrutador),
					candidato);
		}
	}

	static CcpJsonRepresentation getVagasAgrupadosPorRecrutadores(List<Object> intersectList) {
		CcpEntityField idField = new CcpEntityField("mail", false, true, CcpOtherConstants.DO_NOTHING);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.startSimplifiedQuery().terms(idField, intersectList)
				.endSimplifiedQueryAndBackToRequest();

		String[] resourcesNames = new String[] { "vagas" };
		AgruparVagasPorRecrutadores consumer = new AgruparVagasPorRecrutadores();
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		queryExecutor.consumeQueryResult(query, resourcesNames, "10s", 10000, consumer, "contato", "vaga", "mail");
		return consumer.vagasAgrupadasPorRecrutadores;
	}

	static CcpJsonRepresentation getCandidatosAgrupadosPorRecrutadores(CcpQueryOptions query) {

		String[] resourcesNames = new String[] { "visualizacao_de_curriculo" };
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);

		AgruparCandidatosPorRecrutadores consumer = new AgruparCandidatosPorRecrutadores();
		queryExecutor.consumeQueryResult(query, resourcesNames, "10s", 10000, consumer, "candidate", "candidato",
				"email");
		return consumer.candidatosAgrupadosPorRecrutadores;
	}

	static Set<Object> getEmails(CcpQueryOptions query, String tabela, String... fields) {
		String[] resourcesNames = new String[] { tabela };
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		Set<Object> set = new HashSet<>();
		queryExecutor.consumeQueryResult(query, resourcesNames, "10s", 10000, json -> {
			for (String field : fields) {

				String value = json.getAsTextDecorator(new CcpFieldName(field)).content.trim().toLowerCase();

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
		JnEntityJobsnowError.ENTITY.save(json);
		while (true) {
			boolean exists = JnEntityJobsnowError.ENTITY.exists(json);
			if (false == exists) {
				JnEntityJobsnowError.ENTITY.save(json);
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
		CcpQueryOptions queryToSearchLastUpdatedResumes = CcpQueryOptions.INSTANCE.matchAll();
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
enum Fields implements CcpJsonFieldName{
	@CcpEntityFieldPrimaryKey
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	email, 
	ddd,
}

