package com.vis.skills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpCollectionDecorator;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTextDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.query.CcpQueryExecutor;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.especifications.text.extractor.CcpTextExtractor;
import com.vis.entities.VisEntityGroupPositionsBySkills;
import com.vis.services.VisServiceSkills;

public class SkillManagerOld {
	static enum JsonFields implements CcpJsonFieldName{
		implicitSkills, 
		skill, 
		word, 
		childrenCount, 
		hasNoParent, 
		parent, 
		mirror, 
		hasMirror, 
		allParents, 
		hasRepeatedParent, 
		directParent, 
		commonParents, 
		hasSkillsWithCommonParentsSize, 
		skillsWithCommonParents, 
		synonym, 
		similar, 
		preRequisite, 
		positionsCount, 
		parentSize, 
		skillSize, 
		words, 
		id, 
		skillsPerParent, 
		tipoVaga, 
		curriculo, 
		conteudo, 
		text
	}
	static int counter;
	static void apagarTodosOsAgrupamentosDeSkills() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll();
		queryExecutor.delete(query, "group_positions_by_skills");
	} 

	static void relatorioDeSkillsPesquisadas(String...skils) {
		for (String skill : skils) {
			int wordStatus = VisEntityGroupPositionsBySkills.getWordStatus(skill);
			System.out.println(skill + " = " + wordStatus);
		}
	}
	
	static CcpJsonRepresentation getSkillsFromText(String resumeText) {
		String text = new CcpStringDecorator(resumeText).text().stripAccents().getContent();
		Map<String, Object> sessionValues =  CcpOtherConstants.EMPTY_JSON
				 .put(JsonFields.text, text)
				 .content
				 ;
		Map<String, Object> execute = VisServiceSkills.GetSkillsFromText.execute(sessionValues);

		
		CcpJsonRepresentation md = new CcpJsonRepresentation(execute);
		return md;
	}

	static void getSkillsFromText() {
		CcpJsonRepresentation skillsFromText = getSkillsFromText(" VAGA | Arquiteto de Integração Java / Telecom\r\n"
				+ "📌 Projeto: 5 meses\r\n"
				+ "\r\n"
				+ "🔧 Requisitos Técnicos\r\n"
				+ "Arquitetura de Integração, SOA e Microservices\r\n"
				+ "Java 8+ / Spring Boot / Spring Cloud\r\n"
				+ "APIs REST/JSON, SOAP, GraphQL\r\n"
				+ "Mensageria: Kafka, RabbitMQ, JMS\r\n"
				+ "Arquitetura event-driven e integrações assíncronas\r\n"
				+ "Experiência em ambientes Telecom (BSS/OSS)\r\n"
				+ "Cloud (AWS, Azure ou GCP)\r\n"
				+ "Docker e Kubernetes\r\n"
				+ "Observabilidade (ELK, Prometheus, Grafana)\r\n"
				+ "\r\n"
				+ "🎯 Desejável\r\n"
				+ "TM Forum Open APIs\r\n"
				+ "eTOM, SID, TAM\r\n"
				+ "DDD / TOGAF\r\n"
				+ "\r\n"
				+ "📩 Interessou ou conhece alguém com esse perfil?\r\n"
				+ "Envie o cv com pretensão salarial no e-mail: isadora@bluesix.com.br");
		System.out.println(skillsFromText.removeFields(JsonFields.implicitSkills));
	}

	static void countWords() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll();
		Set<String> words = new HashSet<>();
		Consumer<CcpJsonRepresentation> consumer = json -> {
			List<CcpJsonRepresentation> skills = json.getAsJsonList(JsonFields.skill);
			for (CcpJsonRepresentation skill : skills) {
				String word = skill.getAsString(JsonFields.word);
				words.add(word);
			}
		};
		
		queryExecutor.consumeQueryResult(query, new String[] {"group_positions_by_skills"}, "1m", 10_000, consumer, "skill");
		System.out.println(words.size());
	}


	static Set<String> getOtherWords(String word){
		String[] split = word.split(" ");
		if(split.length != 2) {
			List<String> asList = Arrays.asList(word);
			HashSet<String> hashSet = new HashSet<>(asList);
			return hashSet;
		}
		
		String r1 = word.replace(" ", "");
		String r2 = word.replace(" ", "-");
		String r3 = word.replace(" ", ".");
		
		Set<String> response = new HashSet<>( Arrays.asList(r1, r2, r3));
		String secondPiece = split[1];
		
		boolean longNumber = new CcpStringDecorator(secondPiece).isLongNumber();
		
		if(longNumber) {
			return response;
		}
		String firstPiece = split[0];
		
		String reverse = secondPiece + " " + firstPiece;
		
		response.add(reverse);
		
		return response;
	}	
	
	static Set<String> getAllWords(List<String> lines){
		List<Set<String>> collect = lines.stream().map(x -> Arrays.asList(x.split(",")).stream().map(y -> y.trim().toUpperCase()).filter(y -> y.length() > 1).collect(Collectors.toSet())).collect(Collectors.toList());
		Set<String> allWords = new HashSet<>();
		for (Set<String> set : collect) {
			allWords.addAll(set);
		}
		return allWords;
	}
	
	
	static Set<String> getOtherWords(Set<String> otherWords){
		Set<String> response = new HashSet<>();
		for (String word : otherWords) {
			if("JAVAGRAPHQL".equals(word)) {
				System.out.println();
			}
			Set<String> otherWords2 = getOtherWords(word);
			response.addAll(otherWords2);
		}
		return response;
	}
	
	
	static void saveSynonyms() {
		String folder = "C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\database\\elasticsearch\\";
		List<CcpJsonRepresentation> asJsonList = new CcpStringDecorator(folder+ "report_skills.json").file().asJsonList();
		List<String> lines = new CcpStringDecorator(folder+ "synonyms.txt").file().getLines();
		CcpFileDecorator synonyms2 = new CcpStringDecorator(folder+ "synonyms2.txt").file().reset();
		List<Set<String>> collect = lines.stream().map(x -> Arrays.asList(x.split(",")).stream().map(y -> y.trim().toUpperCase()).filter(y -> y.length() > 1).collect(Collectors.toSet())).collect(Collectors.toList());
		List<String> allSynonyms = new ArrayList<>(lines);
		for (CcpJsonRepresentation json : asJsonList) {
			String skill = json.getAsString(JsonFields.skill);
			boolean skillFound = false;
			for (Set<String> set : collect) {
				skillFound = set.contains(skill);
				if(skillFound) {
					break;
				}
			}
			if(false == skillFound) {
				allSynonyms.add(skill);
			}
		}
		
		allSynonyms.sort((a, b) -> a.compareTo(b));
		
		for (String string : allSynonyms) {
			String trim = string.toUpperCase().trim();
			if(trim.length() < 2) {
				continue;
			}
			synonyms2.append(trim);
		}
	}

	
	static void saveSkills() {
		String folder = "C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\database\\elasticsearch\\";
		List<String> lines = new CcpStringDecorator(folder+ "ajustes_synonyms.txt").file().getLines();
		List<List<String>> filtered = lines
		.stream()
		.filter(x -> x.startsWith("adicionarParent="))
		.map(x-> x.split("=")[1])
		.map(x -> x.split(","))
		.map(x -> Arrays.asList(x).stream().map(y -> y.trim().toUpperCase()).filter(y -> false == y.isEmpty()).collect(Collectors.toList()))
		.collect(Collectors.toList())
		;
		
		HashSet<String> skills = new HashSet<>();
		
		for (List<String> list : filtered) {
			skills.addAll(list);
		}
		
		List<CcpJsonRepresentation> report = skills
		.stream()
		.map(x -> CcpOtherConstants.EMPTY_JSON.put(JsonFields.skill, x))
		.map(x -> x.put(JsonFields.childrenCount, new ArrayList<>(filtered).stream().filter(skillsNestaLinha -> skillsNestaLinha.indexOf(x.getAsString(JsonFields.skill)) > 0).count()))
		.map(x -> {
			Optional<List<String>> findFirst = new ArrayList<>(filtered).stream().filter(skillsNestaLinha -> skillsNestaLinha.indexOf(x.getAsString(JsonFields.skill)) == 0).findFirst();
			boolean hasNoParent = false == findFirst.isPresent();
			if(hasNoParent) {
				return x;
			}
			List<String> list = findFirst.get();
			List<String> parent = list.subList(1, list.size());
			CcpJsonRepresentation put = x.put(JsonFields.parent, parent);
			return put;
		})
		.map(x -> x.put(JsonFields.hasNoParent, new ArrayList<>( filtered).stream().allMatch(skillsNestaLinha -> skillsNestaLinha.indexOf(x.getAsString(JsonFields.skill)) != 0)))
		.collect(Collectors.toList());
		
		
		Comparator<? super CcpJsonRepresentation> sorter = getSorter("hasRepeatedParent", "hasSkillsWithCommonParentsSize", "hasMirror", "childrenCount", "skill");
		
		List<CcpJsonRepresentation> collect = report
		.stream()
		.map(x -> x.put(JsonFields.mirror, getSynonym(x, report)))
		.map(x -> x.put(JsonFields.hasMirror, false == x.getAsString(JsonFields.mirror).isEmpty()))
		.map(x -> getSkillsWithCommonParentsSize(x, report))
		.collect(Collectors.toList());
		
		
		CcpFileDecorator reportFile = new CcpStringDecorator(folder+ "report_skills.json").file().reset();
		
		List<CcpJsonRepresentation> newList = new ArrayList<>();
		
		List<String> synonyms3 = new CcpStringDecorator(folder+ "synonyms3.txt").file().getLines();

		List<Set<String>> synonyms = synonyms3.stream()
				.map(x -> Arrays.asList(x.split(",")).stream()
						.map(y -> y.trim().toUpperCase())
						.filter(y -> y.length() > 1)
						.filter(y -> y.length() < 50)
						.collect(Collectors.toSet()))
				.map(x -> getOtherWords(x))
				.collect(Collectors.toList());
		
		for (CcpJsonRepresentation json : collect) {
			List<String> allParents = new ArrayList<>();
			getAllParents(allParents, report, json);
			
			HashSet<String> set = new HashSet<String>(allParents);
			boolean hasRepeatedParent = set.size() != allParents.size();
			CcpJsonRepresentation put = json.put(JsonFields.allParents, allParents.stream()
					.map(skill -> CcpOtherConstants.EMPTY_JSON.put(JsonFields.skill, skill)
							
							.put(JsonFields.parent, getParent(skill, report))
							)
					.collect(Collectors.toList())
					)
					.put(JsonFields.hasRepeatedParent, hasRepeatedParent);
					
					;
			
			String skill = put.getAsString(JsonFields.skill);
			
			List<Set<String>> foundSynonyms = synonyms.stream()
					.filter(x -> x.stream().anyMatch(y -> y.trim().equals(skill)))
					.collect(Collectors.toList());
			
			if(foundSynonyms.isEmpty()) {
				throw new RuntimeException(skill + " has no synonyms");
			}
			
			if(foundSynonyms.size() > 1) {
				throw new RuntimeException(skill + " has more than one synonym: " + foundSynonyms);
			}

			List<CcpJsonRepresentation> foundSynonym = foundSynonyms.get(0).stream()
					.filter(x -> false == x.equals(skill))
					.map(x -> CcpOtherConstants.EMPTY_JSON.put(JsonFields.skill, x)).collect(Collectors.toList());
			
			CcpJsonRepresentation withSynonym = put
					.put(JsonFields.synonym, foundSynonym)
					.getJsonPiece(
							JsonFields.skill, JsonFields.childrenCount
					, JsonFields.parent
					)
					.renameField(JsonFields.parent, JsonFields.directParent)
					.renameField(JsonFields.allParents, JsonFields.parent)
					.getJsonPiece(JsonFields.parent, JsonFields.skill, JsonFields.directParent, JsonFields.childrenCount, JsonFields.synonym, JsonFields.hasNoParent)
					.removeFields(JsonFields.synonym)
					;
			newList.add(withSynonym);	
		}
		newList.sort(sorter);
		reportFile.append(newList.toString());
	}
	
	static List<String> getParent(String skill, List<CcpJsonRepresentation> report){
		return report.stream().filter(x -> skill.equals(x.getAsString(JsonFields.skill))).findFirst().get()
				.getAsStringList(JsonFields.parent);
	}
	static Comparator<? super CcpJsonRepresentation> getSorter(String... fields){
		Comparator<? super CcpJsonRepresentation> sorter = (a, b) -> {
			
			for (String field : fields) {
				CcpStringDecorator sd1 = a.getAsStringDecorator(new CcpFieldName(field));

				if(sd1.isLongNumber()) {
					Integer int2 = b.getAsIntegerNumber(new CcpFieldName(field));
					Integer int1 = a.getAsIntegerNumber(new CcpFieldName(field));
					int subtration = int2 - int1;
					if(subtration == 0) {
						continue;
					}
					return subtration;
				}

				var b1 = a.getAsString(new CcpFieldName(field));
				var b2 = b.getAsString(new CcpFieldName(field));

				if(sd1.isBoolean()) {
					int compareTo = b2.compareTo(b1);
					if(compareTo == 0) {
						continue;
					}
					return compareTo;
				}
				int compareTo = b1.compareTo(b2);
				if(compareTo == 0) {
					continue;
				}
				return compareTo;
			}
			
			return 0;
		};
		return sorter;
	}

	static CcpJsonRepresentation getSkillsWithCommonParentsSize(CcpJsonRepresentation json, List<CcpJsonRepresentation> report) {
		List<String> collect = report.stream()
		.filter(x -> false == x.getAsString(JsonFields.skill).equals(json.getAsString(JsonFields.skill)))
		.map(x -> x.put(JsonFields.commonParents, getCommonParents(x, json)))
		.filter(x -> x.getAsStringList(JsonFields.commonParents).size() > 1)
		.map(x -> x.getAsString(JsonFields.skill))
		.collect(Collectors.toList());
		CcpJsonRepresentation put = json.put(JsonFields.hasSkillsWithCommonParentsSize, false == collect.isEmpty())
				.put(JsonFields.skillsWithCommonParents, collect);
		return put;
	}
	
	static List<String> getCommonParents(CcpJsonRepresentation json1, CcpJsonRepresentation json2) {
		List<String> parent1 = json1.getAsStringList(JsonFields.parent);
		List<String> parent2 = json2.getAsStringList(JsonFields.parent);
		List<String> intersectList = new CcpCollectionDecorator(parent1).getIntersectList(parent2);
		return intersectList;
	}
	
	
	static List<String> getAllParents(List<String> allParents, List<CcpJsonRepresentation> report, CcpJsonRepresentation json){
		
		List<String> parents = json.getAsStringList(JsonFields.parent);

		String skill = json.getAsString(JsonFields.skill);
		System.out.println(skill + ": " + parents);
		allParents.addAll(parents);
		for (String parent : parents) {
			CcpJsonRepresentation parentJson = report.stream()
			.filter(x -> parent.equals(x.getAsString(JsonFields.skill)))
			.findFirst()
			.get();
			getAllParents(allParents, report, parentJson);
		}
		return allParents;
	}

	static String getSynonym(CcpJsonRepresentation json, List<CcpJsonRepresentation> report) {
		List<String> parent = json.getAsStringList(JsonFields.parent);
		if(parent.size() != 1) {
			return "";
		}
		String parentName = parent.get(0);
		String orElseGet = new ArrayList<>(report)
		.stream()
		.filter(x -> x.getAsString(JsonFields.skill).equals(parentName))
		.filter(x -> x.getAsIntegerNumber(JsonFields.childrenCount) == 1)
		.map(x -> x.getAsString(JsonFields.skill))
		.findFirst()
		.orElseGet(() -> "");
		
		return orElseGet;
	}
	
	static void getMissingWords() {
		HashSet<String> todos = new HashSet<>();
		List<List<String>> collect = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\database\\elasticsearch\\ajustes_synonyms.txt").file().getLines()
		.stream().filter(x -> x.startsWith("adicionarParent="))
		.map(x -> x.trim().split("=")[1])
		.map(x -> x.split(","))
		.map(x -> Arrays.asList(x).stream().map(y -> y.trim().toUpperCase()).collect(Collectors.toList()))
		.collect(Collectors.toList());
		
		for (List<String> list : collect) {
			todos.addAll(list);
		}
		CcpFileDecorator synonymsFile = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\skills\\synonyms.json").file();
		List<CcpJsonRepresentation> synonyms = new ArrayList<CcpJsonRepresentation>(synonymsFile.asJsonList());
		
		
		int ajustadas = 0;
		List<CcpJsonRepresentation> missing = new ArrayList<>();
		for (CcpJsonRepresentation json : synonyms) {
			
			String skill = json.getAsString(JsonFields.skill);


			if(todos.contains(skill)) {
				ajustadas ++;
				continue;
			}
			List<CcpJsonRepresentation> asJsonList = json.getAsJsonList(JsonFields.synonym);
			boolean anyMatch = asJsonList.stream().map(x -> x.getAsString(JsonFields.skill)).anyMatch(x -> todos.contains(x));
			
			if(anyMatch) {
				ajustadas ++;
				continue;
			}
			missing.add(json);
		}
		List<String> removidas = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\database\\elasticsearch\\removidas.txt").file().getLines().stream().map(x -> x.trim().split(" = ")[0]).collect(Collectors.toList());
		int estudar = 0;
		for (CcpJsonRepresentation copy : missing) {
			String skill = copy.getAsString(JsonFields.skill);
			if(removidas.contains(skill)) {
				continue;
			}
			System.out.println(skill);
			estudar++;
		}
		System.out.println("INICIO: " + synonyms.size());
		System.out.println("TODOS: " + todos.size());
		System.out.println("REMOVIDAS: " + removidas.size());
		System.out.println("AJUSTADAS: " + ajustadas);
		System.out.println("ESTUDAR: " + estudar);
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
				List<String> list = json.getAsStringList(new CcpFieldName(field)).stream()
						.map(x -> new CcpStringDecorator(x).text().stripAccents().sanitize().getContent().toUpperCase().trim())
						.filter(x -> x.length() > 2 && x.length() <= 50)
						.filter(x -> false == existingWords.contains(x))
						.collect(Collectors.toList());
				
				words.addAll(list);
				
			}
			System.out.println(counter++ + " = " + words.size());
		};
		queryExecutor.consumeQueryResult(query, new String[] {"pesquisa_curriculos"}, "1m", 10_000, consumer, "requisitosDesejaveis", "requisitosObrigatorios", "must", "should");
		

		for (String word : words) {
			CcpJsonRepresentation synonym = CcpOtherConstants.EMPTY_JSON.put(JsonFields.skill, word);
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
			var similar = synonym.getAsJsonList(JsonFields.similar)
					.stream()
					.map(x -> x.getAsString(JsonFields.word).replace("_", " "))
					.collect(Collectors.toList())
					;
			allSimilar.addAll(similar);
		}
		System.out.println(allSimilar.size());
		for (CcpJsonRepresentation synonym : synonyms) {
			{
				String skill = synonym.getAsString(JsonFields.skill);
				allSimilar.remove(skill);
			}
			List<String> parents = synonym.getAsStringList(JsonFields.parent);
			for (String parent : parents) {
				allSimilar.remove(parent);
			}

			List<CcpJsonRepresentation> asJsonList = synonym.getAsJsonList(JsonFields.synonym);
			for (CcpJsonRepresentation sym : asJsonList) {
				String skill = sym.getAsString(JsonFields.skill);
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
	
	static void relatoriosDasSkillsNosCurriculos() {
		CcpTextExtractor textExtractor = CcpDependencyInjection.getDependency(CcpTextExtractor.class);
		
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll();
		Map<String, Integer> countByResume = new HashMap<>();
		Map<String, Integer> countByWords = new HashMap<>();
		Map<String, Set<String>> groupedResumes = new HashMap<>();
		Map<String, CcpJsonRepresentation> reports = new HashMap<>();
		Consumer<CcpJsonRepresentation> consumer = json -> {
			try {
				String base64 = json.getValueFromPath("", JsonFields.curriculo, JsonFields.conteudo);
				
				String resumeText = textExtractor.extractText(base64);
				CcpJsonRepresentation md = getSkillsFromText(resumeText);
				
				String id = json.getAsString(JsonFields.id);
				String tipoVaga = json.getAsString(JsonFields.tipoVaga);

				List<CcpJsonRepresentation> skills = md.getAsJsonList(JsonFields.skill).stream()
						.collect(Collectors.toList());
				
				CcpTextDecorator completeLeft = new CcpStringDecorator(""+ skills.size()).text().completeLeft('0', 3);
				
				String fileName = completeLeft + "_" + id  + "_" + tipoVaga + ".json";
				
				for (CcpJsonRepresentation skill : skills) {
					String word = skill.getAsString(JsonFields.word);
					Integer counter = countByWords.getOrDefault(word, 0) + 1;
					countByWords.put(word, counter);
					Set<String> orDefault = groupedResumes.getOrDefault(word, new HashSet<>());
					orDefault.add(id);
					groupedResumes.put(word, orDefault);
				}
				
				countByResume.put(id, skills.size());
				CcpJsonRepresentation subReportFromSkills = getSubReportFromSkills(md, id + "_" + tipoVaga);
				
				String asPrettyJson = md
						.put(JsonFields.skill, skills)
						.asPrettyJson();
				
				reports.put(fileName, subReportFromSkills);
				
				new CcpStringDecorator("c:/logs/skills/" + fileName).file().reset().append(asPrettyJson);
				System.out.println(counter++ + " = " + fileName);
				
			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println(counter++);
			}
		
		};
		queryExecutor.consumeQueryResult(query, new String[] {"profissionais2"}, "100m", 10, consumer, "curriculo.conteudo", "id", "tipoVaga");
		List<String> allWords = getAllWords();

		createReport(countByWords, "c:/logs/skills/countByWords.txt",  word -> {
			allWords.remove(word);
		});
		
		createReport(countByResume, "c:/logs/skills/countByResume.txt",  word -> {});
		
		CcpFileDecorator removidas = new CcpStringDecorator("c:/logs/skills/removidas.txt").file().reset();
		for (String removedWord : allWords) {
			removidas.append(removedWord);
		}
		CcpFileDecorator groupedResumesFile = new CcpStringDecorator("c:/logs/skills/groupedResumes.txt").file().reset();
		Set<String> skills = groupedResumes.keySet();
		for (String skill : skills) {
			groupedResumesFile.append(skill + "=" + groupedResumes.get(skill));
		}
		
		Set<String> keySet = reports.keySet();
		double skillsCount = 0;
		double parentsCount = 0;
		
		for (String string : keySet) {
			CcpJsonRepresentation report = reports.get(string);
			Integer parentSize = report.getAsIntegerNumber(JsonFields.parentSize);
			Integer skillSize = report.getAsIntegerNumber(JsonFields.skillSize);
			
			skillsCount += skillSize;
			parentsCount += parentSize;
		}
		
		System.out.println(skillsCount / parentsCount);
		
		List<CcpJsonRepresentation> arrayList = new ArrayList<>(reports.values());
		arrayList.sort((a, b) -> {
			
			if(false == b.containsAllFields(JsonFields.skillsPerParent)) {
				return -1;
			}

			if(false == a.containsAllFields(JsonFields.skillsPerParent)) {
				return 1;
			}

			Integer x2 = (int)(b.getAsDoubleNumber(JsonFields.skillsPerParent) * 1000) ;
			Integer x1 = (int)(a.getAsDoubleNumber(JsonFields.skillsPerParent) * 1000);
			
			return x2 - x1;
		});
		
		CcpFileDecorator report = new CcpStringDecorator("c:/logs/skills/report.txt").file().reset();
		
		for (CcpJsonRepresentation rep : arrayList) {
			report.append(rep.asUgglyJson());
		}
	}
	
	static CcpJsonRepresentation getSubReportFromSkills(CcpJsonRepresentation md, String id) {
		List<CcpJsonRepresentation> skills = md.getAsJsonList(JsonFields.skill);

		Set<String> set1 = new HashSet<>();
		Set<String> allSkills = new HashSet<>();
		Set<String> allWords = new HashSet<>();

		for (CcpJsonRepresentation skill : skills) {
			List<String> parents = skill.getAsStringList(JsonFields.parent);
			String skillName = skill.getAsString(JsonFields.skill);
			String word = skill.getAsString(JsonFields.word);
			set1.addAll(parents);
			allSkills.add(skillName);
			allWords.add(word);
		}
		
		List<String> allParents = set1.stream().filter(parent -> false == allSkills.contains(parent)).collect(Collectors.toList());
		
		double allSkillsSize = allSkills.size();
		double allParentsSize = allParents.size();
		

		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(JsonFields.parentSize, allParentsSize)
				.put(JsonFields.skillSize, allSkillsSize)
				.put(JsonFields.words, allWords)
				.put(JsonFields.id, id)
				;

		if(allParentsSize > 0) {
			double skillsPerParent = allSkillsSize / allParentsSize;
			put = put.put(JsonFields.skillsPerParent, skillsPerParent);
		}
		return put;
	}
	
	static List<String> getAllWords() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll().setSize(10000);
		List<CcpJsonRepresentation> resultAsList = queryExecutor.getResultAsList(query, new String[] {"group_positions_by_skills"}, "skill");
		
		Set<String> set = new HashSet<>();
		for (CcpJsonRepresentation json : resultAsList) {
			List<CcpJsonRepresentation> list = json.getAsJsonList(JsonFields.skill);
			List<String> collect = list.stream().map(x -> x.getAsString(JsonFields.word)).collect(Collectors.toList());
			set.addAll(collect);
		}
		ArrayList<String> list = new ArrayList<>(set);
		list.sort((a,b) -> a.length() - b.length());
		return list;
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
}
