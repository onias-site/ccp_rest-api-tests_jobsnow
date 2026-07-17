package com.vis.skills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpCollectionDecorator;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;

public class SkillManager {
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
}
