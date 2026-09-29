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
import com.ccp.decorators.CcpJsonFieldName;
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
	static void deleteAllSkillGroupings() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll();
		queryExecutor.delete(query, "group_positions_by_skills");
	} 

	static void searchedSkillsReport(String...skills) {
		for (String skill : skills) {
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
		Map<String, Object> skillsResult = VisServiceSkills.GetSkillsFromText.execute(sessionValues);

		
		CcpJsonRepresentation extractedSkills = new CcpJsonRepresentation(skillsResult);
		return extractedSkills;
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
		String[] wordPieces = word.split(" ");
		if(wordPieces.length != 2) {
			List<String> singleWordList = Arrays.asList(word);
			HashSet<String> singleWordSet = new HashSet<>(singleWordList);
			return singleWordSet;
		}
		
		String wordWithoutSpace = word.replace(" ", "");
		String hyphenatedWord = word.replace(" ", "-");
		String dottedWord = word.replace(" ", ".");
		
		Set<String> response = new HashSet<>( Arrays.asList(wordWithoutSpace, hyphenatedWord, dottedWord));
		String secondPiece = wordPieces[1];
		
		boolean secondPieceIsNumber = new CcpStringDecorator(secondPiece).isLongNumber();
		
		if(secondPieceIsNumber) {
			return response;
		}
		String firstPiece = wordPieces[0];
		
		String reverse = secondPiece + " " + firstPiece;
		
		response.add(reverse);
		
		return response;
	}	
	
	static Set<String> getAllWords(List<String> lines){
		List<Set<String>> wordGroups = lines.stream().map(x -> Arrays.asList(x.split(",")).stream().map(y -> y.trim().toUpperCase()).filter(y -> y.length() > 1).collect(Collectors.toSet())).collect(Collectors.toList());
		Set<String> allWords = new HashSet<>();
		for (Set<String> wordGroup : wordGroups) {
			allWords.addAll(wordGroup);
		}
		return allWords;
	}
	
	
	static Set<String> getOtherWords(Set<String> otherWords){
		Set<String> response = new HashSet<>();
		for (String word : otherWords) {
			if("JAVAGRAPHQL".equals(word)) {
				System.out.println();
			}
			Set<String> wordVariants = getOtherWords(word);
			response.addAll(wordVariants);
		}
		return response;
	}
	
	
	static void saveSynonyms() {
		String folder = "C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\database\\elasticsearch\\";
		List<CcpJsonRepresentation> skillsReport = new CcpStringDecorator(folder+ "report_skills.json").file().asJsonList();
		List<String> lines = new CcpStringDecorator(folder+ "synonyms.txt").file().getLines();
		CcpFileDecorator synonymsOutputFile = new CcpStringDecorator(folder+ "synonyms2.txt").file().reset();
		List<Set<String>> synonymGroups = lines.stream().map(x -> Arrays.asList(x.split(",")).stream().map(y -> y.trim().toUpperCase()).filter(y -> y.length() > 1).collect(Collectors.toSet())).collect(Collectors.toList());
		List<String> allSynonyms = new ArrayList<>(lines);
		for (CcpJsonRepresentation json : skillsReport) {
			String skill = json.getAsString(JsonFields.skill);
			boolean skillFound = false;
			for (Set<String> synonymGroup : synonymGroups) {
				skillFound = synonymGroup.contains(skill);
				if(skillFound) {
					break;
				}
			}
			if(false == skillFound) {
				allSynonyms.add(skill);
			}
		}
		
		allSynonyms.sort((a, b) -> a.compareTo(b));
		
		for (String synonym : allSynonyms) {
			String normalizedSynonym = synonym.toUpperCase().trim();
			if(normalizedSynonym.length() < 2) {
				continue;
			}
			synonymsOutputFile.append(normalizedSynonym);
		}
	}

	
	static void saveSkills() {
		String folder = "C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\database\\elasticsearch\\";
		List<String> lines = new CcpStringDecorator(folder+ "ajustes_synonyms.txt").file().getLines();
		List<List<String>> skillLines = lines
		.stream()
		.filter(x -> x.startsWith("adicionarParent="))
		.map(x-> x.split("=")[1])
		.map(x -> x.split(","))
		.map(x -> Arrays.asList(x).stream().map(y -> y.trim().toUpperCase()).filter(y -> false == y.isEmpty()).collect(Collectors.toList()))
		.collect(Collectors.toList())
		;
		
		HashSet<String> skills = new HashSet<>();
		
		for (List<String> skillLine : skillLines) {
			skills.addAll(skillLine);
		}
		
		List<CcpJsonRepresentation> report = skills
		.stream()
		.map(x -> CcpOtherConstants.EMPTY_JSON.put(JsonFields.skill, x))
		.map(x -> x.put(JsonFields.childrenCount, new ArrayList<>(skillLines).stream().filter(skillsInThisLine -> skillsInThisLine.indexOf(x.getAsString(JsonFields.skill)) > 0).count()))
		.map(x -> {
			Optional<List<String>> lineStartingWithSkill = new ArrayList<>(skillLines).stream().filter(skillsInThisLine -> skillsInThisLine.indexOf(x.getAsString(JsonFields.skill)) == 0).findFirst();
			boolean hasNoParent = false == lineStartingWithSkill.isPresent();
			if(hasNoParent) {
				return x;
			}
			List<String> skillLine = lineStartingWithSkill.get();
			List<String> parent = skillLine.subList(1, skillLine.size());
			CcpJsonRepresentation skillWithParent = x.put(JsonFields.parent, parent);
			return skillWithParent;
		})
		.map(x -> x.put(JsonFields.hasNoParent, new ArrayList<>( skillLines).stream().allMatch(skillsInThisLine -> skillsInThisLine.indexOf(x.getAsString(JsonFields.skill)) != 0)))
		.collect(Collectors.toList());
		
		
		Comparator<? super CcpJsonRepresentation> sorter = getSorter("hasRepeatedParent", "hasSkillsWithCommonParentsSize", "hasMirror", "childrenCount", "skill");
		
		List<CcpJsonRepresentation> skillsWithMirrors = report
		.stream()
		.map(x -> x.put(JsonFields.mirror, getSynonym(x, report)))
		.map(x -> x.put(JsonFields.hasMirror, false == x.getAsString(JsonFields.mirror).isEmpty()))
		.map(x -> getSkillsWithCommonParentsSize(x, report))
		.collect(Collectors.toList());
		
		
		CcpFileDecorator reportFile = new CcpStringDecorator(folder+ "report_skills.json").file().reset();
		
		List<CcpJsonRepresentation> newList = new ArrayList<>();
		
		List<String> synonymLines = new CcpStringDecorator(folder+ "synonyms3.txt").file().getLines();

		List<Set<String>> synonyms = synonymLines.stream()
				.map(x -> Arrays.asList(x.split(",")).stream()
						.map(y -> y.trim().toUpperCase())
						.filter(y -> y.length() > 1)
						.filter(y -> y.length() < 50)
						.collect(Collectors.toSet()))
				.map(x -> getOtherWords(x))
				.collect(Collectors.toList());
		
		for (CcpJsonRepresentation json : skillsWithMirrors) {
			List<String> allParents = new ArrayList<>();
			getAllParents(allParents, report, json);
			
			HashSet<String> distinctParents = new HashSet<String>(allParents);
			boolean hasRepeatedParent = distinctParents.size() != allParents.size();
			CcpJsonRepresentation skillWithAllParents = json.put(JsonFields.allParents, allParents.stream()
					.map(skill -> CcpOtherConstants.EMPTY_JSON.put(JsonFields.skill, skill)
							
							.put(JsonFields.parent, getParent(skill, report))
							)
					.collect(Collectors.toList())
					)
					.put(JsonFields.hasRepeatedParent, hasRepeatedParent);
					
					;
			
			String skill = skillWithAllParents.getAsString(JsonFields.skill);
			
			List<Set<String>> foundSynonyms = synonyms.stream()
					.filter(x -> x.stream().anyMatch(y -> y.trim().equals(skill)))
					.collect(Collectors.toList());
			
			if(foundSynonyms.isEmpty()) {
				throw new VisErrorSkillWithoutSynonyms(skill);
			}

			if(foundSynonyms.size() > 1) {
				throw new VisErrorSkillWithManySynonymGroups(skill, foundSynonyms);
			}

			List<CcpJsonRepresentation> synonymsAsJson = foundSynonyms.get(0).stream()
					.filter(x -> false == x.equals(skill))
					.map(x -> CcpOtherConstants.EMPTY_JSON.put(JsonFields.skill, x)).collect(Collectors.toList());
			
			CcpJsonRepresentation withSynonym = skillWithAllParents
					.put(JsonFields.synonym, synonymsAsJson)
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
				CcpStringDecorator firstFieldValue = a.getAsStringDecorator(new CcpFieldName(field));

				if(firstFieldValue.isLongNumber()) {
					Integer secondNumber = b.getAsIntegerNumber(new CcpFieldName(field));
					Integer firstNumber = a.getAsIntegerNumber(new CcpFieldName(field));
					int difference = secondNumber - firstNumber;
					if(difference == 0) {
						continue;
					}
					return difference;
				}

				var firstString = a.getAsString(new CcpFieldName(field));
				var secondString = b.getAsString(new CcpFieldName(field));

				if(firstFieldValue.isBoolean()) {
					int comparison = secondString.compareTo(firstString);
					if(comparison == 0) {
						continue;
					}
					return comparison;
				}
				int comparison = firstString.compareTo(secondString);
				if(comparison == 0) {
					continue;
				}
				return comparison;
			}
			
			return 0;
		};
		return sorter;
	}

	static CcpJsonRepresentation getSkillsWithCommonParentsSize(CcpJsonRepresentation json, List<CcpJsonRepresentation> report) {
		List<String> skillsWithCommonParents = report.stream()
		.filter(x -> false == x.getAsString(JsonFields.skill).equals(json.getAsString(JsonFields.skill)))
		.map(x -> x.put(JsonFields.commonParents, getCommonParents(x, json)))
		.filter(x -> x.getAsStringList(JsonFields.commonParents).size() > 1)
		.map(x -> x.getAsString(JsonFields.skill))
		.collect(Collectors.toList());
		CcpJsonRepresentation jsonWithCommonParents = json.put(JsonFields.hasSkillsWithCommonParentsSize, false == skillsWithCommonParents.isEmpty())
				.put(JsonFields.skillsWithCommonParents, skillsWithCommonParents);
		return jsonWithCommonParents;
	}
	
	static List<String> getCommonParents(CcpJsonRepresentation json1, CcpJsonRepresentation json2) {
		List<String> firstParents = json1.getAsStringList(JsonFields.parent);
		List<String> secondParents = json2.getAsStringList(JsonFields.parent);
		List<String> commonParents = new CcpCollectionDecorator(firstParents).getIntersectList(secondParents);
		return commonParents;
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
		List<String> parents = json.getAsStringList(JsonFields.parent);
		if(parents.size() != 1) {
			return "";
		}
		String parentName = parents.get(0);
		String synonym = new ArrayList<>(report)
		.stream()
		.filter(x -> x.getAsString(JsonFields.skill).equals(parentName))
		.filter(x -> x.getAsIntegerNumber(JsonFields.childrenCount) == 1)
		.map(x -> x.getAsString(JsonFields.skill))
		.findFirst()
		.orElseGet(() -> "");
		
		return synonym;
	}
	
	static void getMissingWords() {
		HashSet<String> allAdjustedSkills = new HashSet<>();
		List<List<String>> adjustedSkillLines = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\database\\elasticsearch\\ajustes_synonyms.txt").file().getLines()
		.stream().filter(x -> x.startsWith("adicionarParent="))
		.map(x -> x.trim().split("=")[1])
		.map(x -> x.split(","))
		.map(x -> Arrays.asList(x).stream().map(y -> y.trim().toUpperCase()).collect(Collectors.toList()))
		.collect(Collectors.toList());
		
		for (List<String> adjustedSkillLine : adjustedSkillLines) {
			allAdjustedSkills.addAll(adjustedSkillLine);
		}
		CcpFileDecorator synonymsFile = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\skills\\synonyms.json").file();
		List<CcpJsonRepresentation> synonyms = new ArrayList<CcpJsonRepresentation>(synonymsFile.asJsonList());
		
		
		int adjustedCount = 0;
		List<CcpJsonRepresentation> missing = new ArrayList<>();
		for (CcpJsonRepresentation json : synonyms) {
			
			String skill = json.getAsString(JsonFields.skill);


			if(allAdjustedSkills.contains(skill)) {
				adjustedCount ++;
				continue;
			}
			List<CcpJsonRepresentation> synonymsOfSkill = json.getAsJsonList(JsonFields.synonym);
			boolean anySynonymAdjusted = synonymsOfSkill.stream().map(x -> x.getAsString(JsonFields.skill)).anyMatch(x -> allAdjustedSkills.contains(x));
			
			if(anySynonymAdjusted) {
				adjustedCount ++;
				continue;
			}
			missing.add(json);
		}
		List<String> removedSkills = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\database\\elasticsearch\\removidas.txt").file().getLines().stream().map(x -> x.trim().split(" = ")[0]).collect(Collectors.toList());
		int toStudyCount = 0;
		for (CcpJsonRepresentation missingSkill : missing) {
			String skill = missingSkill.getAsString(JsonFields.skill);
			if(removedSkills.contains(skill)) {
				continue;
			}
			System.out.println(skill);
			toStudyCount++;
		}
		System.out.println("START: " + synonyms.size());
		System.out.println("ALL: " + allAdjustedSkills.size());
		System.out.println("REMOVED: " + removedSkills.size());
		System.out.println("ADJUSTED: " + adjustedCount);
		System.out.println("TO STUDY: " + toStudyCount);
	}

		static void increaseSynonymsFile() {
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
				List<String> newFieldWords = json.getAsStringList(new CcpFieldName(field)).stream()
						.map(x -> new CcpStringDecorator(x).text().stripAccents().sanitize().getContent().toUpperCase().trim())
						.filter(x -> x.length() > 2 && x.length() <= 50)
						.filter(x -> false == existingWords.contains(x))
						.collect(Collectors.toList());
				
				words.addAll(newFieldWords);
				
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

			List<CcpJsonRepresentation> synonymsOfSkill = synonym.getAsJsonList(JsonFields.synonym);
			for (CcpJsonRepresentation synonymEntry : synonymsOfSkill) {
				String skill = synonymEntry.getAsString(JsonFields.skill);
				allSimilar.remove(skill);
				
			}
		}
		System.out.println(allSimilar.size());
		var newWords = new CcpStringDecorator("c:/logs/skills/newWords.txt").file().reset();
		var countByWords = new CcpStringDecorator("c:/logs/skills/countByWords.txt").file().getLines();
		
		for (String similar : allSimilar) {
			String wordCountLine = countByWords.stream().filter(x -> x.startsWith(similar)).findFirst().get();
			newWords.append(wordCountLine);
		}
	}
	
	static void skillsInResumesReports() {
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
				CcpJsonRepresentation extractedSkills = getSkillsFromText(resumeText);
				
				String id = json.getAsString(JsonFields.id);
				String positionType = json.getAsString(JsonFields.tipoVaga);

				List<CcpJsonRepresentation> skills = extractedSkills.getAsJsonList(JsonFields.skill).stream()
						.collect(Collectors.toList());
				
				CcpTextDecorator paddedSkillCount = new CcpStringDecorator(""+ skills.size()).text().completeLeft('0', 3);
				
				String fileName = paddedSkillCount + "_" + id  + "_" + positionType + ".json";
				
				for (CcpJsonRepresentation skill : skills) {
					String word = skill.getAsString(JsonFields.word);
					Integer wordCount = countByWords.getOrDefault(word, 0) + 1;
					countByWords.put(word, wordCount);
					Set<String> resumesWithWord = groupedResumes.getOrDefault(word, new HashSet<>());
					resumesWithWord.add(id);
					groupedResumes.put(word, resumesWithWord);
				}
				
				countByResume.put(id, skills.size());
				CcpJsonRepresentation subReportFromSkills = getSubReportFromSkills(extractedSkills, id + "_" + positionType);
				
				String resumeSkillsJson = extractedSkills
						.put(JsonFields.skill, skills)
						.asPrettyJson();
				
				reports.put(fileName, subReportFromSkills);
				
				new CcpStringDecorator("c:/logs/skills/" + fileName).file().reset().append(resumeSkillsJson);
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
		
		CcpFileDecorator removedWordsFile = new CcpStringDecorator("c:/logs/skills/removidas.txt").file().reset();
		for (String removedWord : allWords) {
			removedWordsFile.append(removedWord);
		}
		CcpFileDecorator groupedResumesFile = new CcpStringDecorator("c:/logs/skills/groupedResumes.txt").file().reset();
		Set<String> skills = groupedResumes.keySet();
		for (String skill : skills) {
			groupedResumesFile.append(skill + "=" + groupedResumes.get(skill));
		}
		
		Set<String> reportFileNames = reports.keySet();
		double skillsCount = 0;
		double parentsCount = 0;
		
		for (String reportFileName : reportFileNames) {
			CcpJsonRepresentation report = reports.get(reportFileName);
			Integer parentSize = report.getAsIntegerNumber(JsonFields.parentSize);
			Integer skillSize = report.getAsIntegerNumber(JsonFields.skillSize);
			
			skillsCount += skillSize;
			parentsCount += parentSize;
		}
		
		System.out.println(skillsCount / parentsCount);
		
		List<CcpJsonRepresentation> sortedReports = new ArrayList<>(reports.values());
		sortedReports.sort((a, b) -> {
			
			if(false == b.containsAllFields(JsonFields.skillsPerParent)) {
				return -1;
			}

			if(false == a.containsAllFields(JsonFields.skillsPerParent)) {
				return 1;
			}

			Integer secondSkillsPerParent = (int)(b.getAsDoubleNumber(JsonFields.skillsPerParent) * 1000) ;
			Integer firstSkillsPerParent = (int)(a.getAsDoubleNumber(JsonFields.skillsPerParent) * 1000);
			
			return secondSkillsPerParent - firstSkillsPerParent;
		});
		
		CcpFileDecorator report = new CcpStringDecorator("c:/logs/skills/report.txt").file().reset();
		
		for (CcpJsonRepresentation sortedReport : sortedReports) {
			report.append(sortedReport.asUgglyJson());
		}
	}
	
	static CcpJsonRepresentation getSubReportFromSkills(CcpJsonRepresentation extractedSkills, String id) {
		List<CcpJsonRepresentation> skills = extractedSkills.getAsJsonList(JsonFields.skill);

		Set<String> parentsOfSkills = new HashSet<>();
		Set<String> allSkills = new HashSet<>();
		Set<String> allWords = new HashSet<>();

		for (CcpJsonRepresentation skill : skills) {
			List<String> parents = skill.getAsStringList(JsonFields.parent);
			String skillName = skill.getAsString(JsonFields.skill);
			String word = skill.getAsString(JsonFields.word);
			parentsOfSkills.addAll(parents);
			allSkills.add(skillName);
			allWords.add(word);
		}
		
		List<String> allParents = parentsOfSkills.stream().filter(parent -> false == allSkills.contains(parent)).collect(Collectors.toList());
		
		double allSkillsSize = allSkills.size();
		double allParentsSize = allParents.size();
		

		CcpJsonRepresentation subReport = CcpOtherConstants.EMPTY_JSON
				.put(JsonFields.parentSize, allParentsSize)
				.put(JsonFields.skillSize, allSkillsSize)
				.put(JsonFields.words, allWords)
				.put(JsonFields.id, id)
				;

		if(allParentsSize > 0) {
			double skillsPerParent = allSkillsSize / allParentsSize;
			subReport = subReport.put(JsonFields.skillsPerParent, skillsPerParent);
		}
		return subReport;
	}
	
	static List<String> getAllWords() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll().setSize(10000);
		List<CcpJsonRepresentation> resultAsList = queryExecutor.getResultAsList(query, new String[] {"group_positions_by_skills"}, "skill");
		
		Set<String> uniqueWords = new HashSet<>();
		for (CcpJsonRepresentation json : resultAsList) {
			List<CcpJsonRepresentation> skillsOfGroup = json.getAsJsonList(JsonFields.skill);
			List<String> wordsOfGroup = skillsOfGroup.stream().map(x -> x.getAsString(JsonFields.word)).collect(Collectors.toList());
			uniqueWords.addAll(wordsOfGroup);
		}
		ArrayList<String> sortedWords = new ArrayList<>(uniqueWords);
		sortedWords.sort((a,b) -> a.length() - b.length());
		return sortedWords;
	}

	static void createReport(Map<String, Integer> reportSource, String reportFile, Consumer<String> consumer) {
		
		ArrayList<String> sortedWords = new ArrayList<String>(reportSource.keySet());
		sortedWords.sort((a, b) -> reportSource.get(b) - reportSource.get(a));
		CcpFileDecorator report = new CcpStringDecorator(reportFile).file().reset();
		Integer total = 0;
		Map<Integer, Integer> numbers = new TreeMap<>();
		for (String word : sortedWords) {
			Integer count = reportSource.get(word);
			report.append(word + " = " + count);
			consumer.accept(word);
			total += count;
			Integer wordsWithThisCount = numbers.getOrDefault(count, 0) + 1;
			numbers.put(count, wordsWithThisCount);
		}
		Integer average = total / sortedWords.size();
		
		report.append("AVERAGE = " + average);
		
		Set<Integer> distinctCounts = numbers.keySet();
		for (Integer distinctCount : distinctCounts) {
			Integer wordsWithThisCount = numbers.get(distinctCount);
			report.append(distinctCount + " = " + wordsWithThisCount);
		}
	}

	/**
	 * Exception thrown when a skill is not found in any synonym group.
	 */
	@SuppressWarnings("serial")
	public static class VisErrorSkillWithoutSynonyms extends RuntimeException {
		/**
		 * Builds the message stating which skill has no synonyms.
		 * @param skill the skill without a synonym group
		 */
		private VisErrorSkillWithoutSynonyms(String skill) {
			super(skill + " has no synonyms");
		}
	}

	/**
	 * Exception thrown when a skill appears in more than one synonym group, which makes the choice
	 * of the correct group ambiguous.
	 */
	@SuppressWarnings("serial")
	public static class VisErrorSkillWithManySynonymGroups extends RuntimeException {
		/**
		 * Builds the message stating the ambiguous skill and every group it was found in.
		 * @param skill the skill found in more than one group
		 * @param foundSynonyms the synonym groups that contain the skill
		 */
		private VisErrorSkillWithManySynonymGroups(String skill, List<Set<String>> foundSynonyms) {
			super(skill + " has more than one synonym: " + foundSynonyms);
		}
	}
}
