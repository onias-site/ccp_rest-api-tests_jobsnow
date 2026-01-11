package com.ccp.random;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
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
import com.ccp.especifications.db.query.CcpQueryExecutor;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityField;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.especifications.password.CcpPasswordHandler;
import com.ccp.especifications.text.extractor.CcpTextExtractor;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.implementations.text.extractor.apache.tika.CcpApacheTikaTextExtractor;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.business.login.JnBusinessExecuteLogout;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginToken;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.vis.entities.VisEntityResume;
import com.vis.resumes.ImportResumeFromOldJobsNow;
import com.vis.services.VisServiceSkills;

public class CcpRandomTests {

	static CcpJsonRepresentation groupedCompanies = CcpOtherConstants.EMPTY_JSON;

	public static void main(String[] args) {
		relatoriosDasSkillsNosCurriculos();
	}

		static void aumentarArquivoDeSinonimos() {
		CcpFileDecorator synonymsFile = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\skills\\synonyms.json").file();
		CcpFileDecorator countByWords = new CcpStringDecorator("c:/logs/skills/countByWords.txt").file();
		List<String> existingWords = countByWords.getLines().subList(0, 1408).stream().map(x -> x.split(" = ")[0]).collect(Collectors.toList());
		List<CcpJsonRepresentation> synonyms = new ArrayList<CcpJsonRepresentation>(synonymsFile.asJsonList());
		System.out.println(synonyms.size());

		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll();
		Set<String> words = new HashSet<>();
		Consumer<CcpJsonRepresentation> consumer = json -> {
			String[] fields = new String[] {"requisitosDesejaveis", "requisitosObrigatorios", "must", "should"};
			for (String field : fields) {
				List<String> list = json.getDynamicVersion().getAsStringList(field).stream()
						.map(x -> new CcpStringDecorator(x).text().stripAccents().sanitize().getContent().toUpperCase().trim())
						.filter(x -> x.length() > 2 && x.length() <= 35)
						.filter(x -> false == existingWords.contains(x))
						.collect(Collectors.toList());
				
				words.addAll(list);
				
			}
			System.out.println(counter++ + " = " + words.size());
		};
		queryExecutor.consumeQueryResult(query, new String[] {"pesquisa_curriculos"}, "1m", 10_000, consumer, "requisitosDesejaveis", "requisitosObrigatorios", "must", "should");
		

		for (String word : words) {
			CcpJsonRepresentation synonym = CcpOtherConstants.EMPTY_JSON.getDynamicVersion().put("skill", word);
			synonyms.add(synonym);
		}
		System.out.println(synonyms.size());
		synonymsFile.reset().append(synonyms.toString());
	}

	static void addNewWords() {
		CcpFileDecorator synonymsFile = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\skills\\synonyms.json").file();
		List<CcpJsonRepresentation> synonyms = synonymsFile.asJsonList();
		Set<String> allSimilar = new HashSet<>();
		for (CcpJsonRepresentation synonym : synonyms) {
			var similar = synonym.getDynamicVersion().getAsJsonList("similar")
					.stream()
					.map(x -> x.getDynamicVersion().getAsString("word").replace("_", " "))
					.collect(Collectors.toList())
					;
			allSimilar.addAll(similar);
		}
		System.out.println(allSimilar.size());
		for (CcpJsonRepresentation synonym : synonyms) {
			{
				String skill = synonym.getDynamicVersion().getAsString("skill");
				allSimilar.remove(skill);
			}
			List<String> parents = synonym.getDynamicVersion().getAsStringList("parent");
			for (String parent : parents) {
				allSimilar.remove(parent);
			}
			
			List<CcpJsonRepresentation> asJsonList = synonym.getDynamicVersion().getAsJsonList("synonym");
			for (var sym : asJsonList) {
				String skill = sym.getDynamicVersion().getAsString("skill");
				allSimilar.remove(skill);
				
			}
		}
		System.out.println(allSimilar.size());
		var newWords = new CcpStringDecorator("c:/logs/skills/newWords.txt").file().reset();
		var countByWords = new CcpStringDecorator("c:/logs/skills/countByWords.txt").file().getLines();
		
		for (String similar : allSimilar) {
			String string = countByWords.stream().filter(x -> x.startsWith(similar)).findFirst().get();
			newWords.append(string);
		}
	
	}
	
	static void sanitizarArquivoDeSinonimos() {
		List<String> removidas = new CcpStringDecorator("c:/logs/skills/removidas.txt").file().getLines().stream().map(x -> x.trim()).collect(Collectors.toList());
		CcpFileDecorator synonymsFile = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\skills\\synonyms.json").file();
		List<CcpJsonRepresentation> synonyms = synonymsFile.asJsonList();
		List<CcpJsonRepresentation> newSynonyms = new ArrayList<>();
		
		for (CcpJsonRepresentation synonym : synonyms) {
			
				String skill = synonym.getDynamicVersion().getAsString("skill");
				if(skill.trim().length() < 2) {
					synonym = synonym.getDynamicVersion().removeField("skill");
				}
				if(skill.trim().length() > 35) {
					synonym = synonym.getDynamicVersion().removeField("skill");
				}
				if(removidas.contains(skill)) {
					synonym = synonym.getDynamicVersion().removeField("skill");
				}
				List<String> parent = synonym.getDynamicVersion().getAsStringList("parent").stream()
						.filter(x -> x.length() > 2 && x.length() <= 35)
						.filter(x -> false == removidas.contains(x))
						.collect(Collectors.toList());
				
				
				synonym = synonym.getDynamicVersion().put("parent", parent);
				{
					List<CcpJsonRepresentation> collect = synonym.getDynamicVersion().getAsJsonList("synonym").stream().filter(x -> {
						String word = x.getDynamicVersion().getAsString("skill");
						return word.length() > 2 && word.length() <= 35 && false == removidas.contains(word) && false == skill.equals(word);
					}).collect(Collectors.toList());
					
					synonym = synonym.getDynamicVersion().put("synonym", collect);
				}
				{
					List<CcpJsonRepresentation> collect = synonym.getDynamicVersion().getAsJsonList("preRequisite").stream().filter(x -> {
						String word = x.getDynamicVersion().getAsString("word");
						return word.length() > 2 && word.length() <= 35 && false == removidas.contains(word);
					}).collect(Collectors.toList());
					
					synonym = synonym.getDynamicVersion().put("preRequisite", collect);
				}
				{
					List<CcpJsonRepresentation> collect = synonym.getDynamicVersion().getAsJsonList("similar").stream().filter(x -> {
						String word = x.getDynamicVersion().getAsString("word").replace("_", " ");
						return word.length() > 2 && word.length() <= 35 && false == removidas.contains(word);
					}).collect(Collectors.toList());
					
					synonym = synonym.getDynamicVersion().put("similar", collect);
				}
				
				newSynonyms.add(synonym);
			}

		List<CcpJsonRepresentation> collect = newSynonyms.stream()
		.filter(x -> x.getDynamicVersion().containsAllFields("skill") || false == x.getDynamicVersion().getAsJsonList("synonym").isEmpty())
		.map(x -> x.getDynamicVersion().containsAllFields("skill") ? x : transferSynonymToSkill(x))
		.map(x -> false == x.getDynamicVersion().getAsObjectList("parent").isEmpty() ?  x :  x.getDynamicVersion().removeField("parent"))
		.map(x -> false == x.getDynamicVersion().getAsObjectList("similar").isEmpty() ?  x :  x.getDynamicVersion().removeField("similar"))
		.map(x -> false == x.getDynamicVersion().getAsObjectList("synonym").isEmpty() ?  x :  x.getDynamicVersion().removeField("synonym"))
		.map(x -> false == x.getDynamicVersion().getAsObjectList("preRequisite").isEmpty() ?  x :  x.getDynamicVersion().removeField("preRequisite"))
		.collect(Collectors.toList());
		CcpFileDecorator newSynonymsFile = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\skills\\new_synonyms.json").file().reset();
		newSynonymsFile.append(collect.toString());
		for (CcpJsonRepresentation json : collect) {
			System.out.println(json.getDynamicVersion().getAsString("skill"));
		}
		System.out.println(collect.size());
	}
	
	static CcpJsonRepresentation transferSynonymToSkill(CcpJsonRepresentation x) {
		List<CcpJsonRepresentation> synonym = x.getDynamicVersion().getAsJsonList("synonym");
		ArrayList<CcpJsonRepresentation> list = new ArrayList<>(synonym);
		list.sort((a,b) ->  b.getDynamicVersion().getAsIntegerNumber("positionsCount") - a.getDynamicVersion().getAsIntegerNumber("positionsCount"));
		String skill = list.remove(0).getDynamicVersion().getAsString("skill");
		CcpJsonRepresentation put = x.getDynamicVersion().put("skill", skill).getDynamicVersion().put("synonym", list);
		return put;
	}
	
	static List<String> getAllWords() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll().setSize(10000);
		List<CcpJsonRepresentation> resultAsList = queryExecutor.getResultAsList(query, new String[] {"group_positions_by_skills"}, "skill");
		
		Set<String> set = new HashSet<>();
		for (CcpJsonRepresentation json : resultAsList) {
			List<CcpJsonRepresentation> list = json.getDynamicVersion().getAsJsonList("skill");
			List<String> collect = list.stream().map(x -> x.getDynamicVersion().getAsString("word")).collect(Collectors.toList());
			set.addAll(collect);
		}
		ArrayList<String> list = new ArrayList<>(set);
		list.sort((a,b) -> a.length() - b.length());
		return list;
	}

	static void relatoriosDasSkillsNosCurriculos() {
		CcpTextExtractor textExtractor = CcpDependencyInjection.getDependency(CcpTextExtractor.class);
		
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll();
		Map<String, Integer> countByResume = new HashMap<>();
		Map<String, Integer> countByWords = new HashMap<>();
		
		
		Consumer<CcpJsonRepresentation> consumer = json -> {
			try {
				String base64 = json.getDynamicVersion().getValueFromPath("", "curriculo", "conteudo");
				
				String resumeText = textExtractor.extractText(base64);
				String text = new CcpStringDecorator(resumeText).text().stripAccents().getContent();
				Map<String, Object> sessionValues =  CcpOtherConstants.EMPTY_JSON
						 .getDynamicVersion()
						 .put("text", text)
						 .content
						 ;
				Map<String, Object> execute = VisServiceSkills.GetSkillsFromText.execute(sessionValues);
				String id = json.getDynamicVersion().getAsString("id");

				String fileName = id + ".json";
				
				CcpJsonRepresentation md = new CcpJsonRepresentation(execute);
				

				List<CcpJsonRepresentation> skills = md.getDynamicVersion().getAsJsonList("skill").stream()
						.collect(Collectors.toList());
				
				for (CcpJsonRepresentation skill : skills) {
					String word = skill.getDynamicVersion().getAsString("word");
					Integer counter = countByWords.getOrDefault(word, 0) + 1;
					countByWords.put(word, counter);
				}
				
				countByResume.put(id, skills.size());
				
				new CcpStringDecorator("c:/logs/skills/" + fileName).file().reset().append(md.getDynamicVersion().put("skill", skills).asPrettyJson());
				System.out.println(counter++ + " = " + fileName);
				
			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println(counter++);
			}
		
		};
		queryExecutor.consumeQueryResult(query, new String[] {"profissionais2"}, "1m", 10, consumer, "curriculo.conteudo", "id");
		List<String> allWords = getAllWords();

		createReport(countByWords, "c:/logs/skills/countByWords.txt",  word -> {
			allWords.remove(word);
		});
		
		createReport(countByResume, "c:/logs/skills/countByResume.txt",  word -> {});
		
		CcpFileDecorator removidas = new CcpStringDecorator("c:/logs/skills/removidas.txt").file().reset();
		for (String removedWord : allWords) {
			removidas.append(removedWord);
		}
	}
	
	static void createReport(Map<String, Integer> reportSource, String reportFile, Consumer<String> consumer) {
		
		ArrayList<String> list = new ArrayList<String>(reportSource.keySet());
		list.sort((a, b) -> reportSource.get(b) - reportSource.get(a));
		CcpFileDecorator report = new CcpStringDecorator(reportFile).file().reset();
		Integer total = 0;
		Map<Integer, Integer> numbers = new TreeMap<>();
		for (String word : list) {
			Integer count = reportSource.get(word);
			report.append(word + " = " + count);
			consumer.accept(word);
			total += count;
			Integer orDefault = numbers.getOrDefault(count, 0) + 1;
			numbers.put(count, orDefault);
		}
		Integer average = total / list.size();
		
		report.append("AVERAGE = " + average);
		
		Set<Integer> keySet = numbers.keySet();
		for (Integer integer : keySet) {
			Integer value = numbers.get(integer);
			report.append(integer + " = " + value);
		}
		
	}
	
	
	static void saveGroupedCompanies() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll();
		
		Consumer<CcpJsonRepresentation> consumer = json -> {
			String x = json.getDynamicVersion().getAsString("id");
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
			
			LinkedHashSet<String> orDefault = groupedCompanies.getDynamicVersion().getOrDefault(initials, new LinkedHashSet<>());
			orDefault.add(capitalizedCompanyName);
			groupedCompanies = groupedCompanies.getDynamicVersion().put(initials, orDefault);
		};
		queryExecutor.consumeQueryResult(query, new String[] {"old_recruiters"}, "1s", 10000, consumer, "id");

		ArrayList<String> arrayList = new ArrayList<> (groupedCompanies.fieldSet());
		arrayList.sort((a, b) ->{
			Set<String> set1 = groupedCompanies.getDynamicVersion().getAsObject(a);
			Set<String> set2 = groupedCompanies.getDynamicVersion().getAsObject(b);
			return set2.size() - set1.size();
		});
		
		CcpJsonRepresentation result = CcpOtherConstants.EMPTY_JSON;
		int total = 0;
		for (String string : arrayList) {
			Set<String> value = groupedCompanies.getDynamicVersion().getAsObject(string);
			result = result.getDynamicVersion().put(string, value);
			int size = value.size();
			System.out.println(string +" = " +  size);
			total += size;
		}
		System.out.println(total);
		new CcpStringDecorator("c:\\logs\\teste.json").file().reset().append(result.asPrettyJson());
	}

	static void saveResume() {
		VisEntityResume.ENTITY.delete(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, "onias85@gmail.com"));
		CcpHttpHandler http = new CcpHttpHandler(200, CcpOtherConstants.DO_NOTHING);
		String path = "http://localhost:9200/profissionais2/_doc/onias85@gmail.com/_source";
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
		JnEntityLoginToken.ENTITY.transferToReverseEntity(json);
	}

	static void saveLoginToken() {
		String value = "12345678";
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityLoginToken.Fields.email, "onias85@gmail.com")
				.put(JnEntityLoginToken.Fields.token, value)
				.put(JnEntityLoginToken.Fields.ip, "127.0.0.1")
				.put(JnEntityLoginToken.Fields.userAgent, "teste");
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
		CcpJsonRepresentation oneById = JnEntityJobsnowError.ENTITY.getOneByIdOrHandleItIfThisIdWasNotFound(json,
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
		CcpEntityField idField = new CcpEntityField("email", false, CcpOtherConstants.DO_NOTHING);

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

			this.vagasAgrupadasPorRecrutadores = this.vagasAgrupadasPorRecrutadores.getDynamicVersion().addToList(recrutador, vaga);
		}

	}

	static class AgruparCandidatosPorRecrutadores implements java.util.function.Consumer<CcpJsonRepresentation> {

		CcpJsonRepresentation candidatosAgrupadosPorRecrutadores = CcpOtherConstants.EMPTY_JSON;

		public void accept(CcpJsonRepresentation json) {
			String candidato = json.getAsObject(JsonFieldNames.candidate, JsonFieldNames.candidato);
			String recrutador = json.getAsString(JsonFieldNames.email);
			this.candidatosAgrupadosPorRecrutadores = this.candidatosAgrupadosPorRecrutadores.getDynamicVersion().addToList(recrutador,
					candidato);
		}
	}

	static CcpJsonRepresentation getVagasAgrupadosPorRecrutadores(List<Object> intersectList) {
		CcpEntityField idField = new CcpEntityField("mail", false, CcpOtherConstants.DO_NOTHING);
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

