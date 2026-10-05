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
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;

/** One-off tool that builds the skills hierarchy and synonym reports from the text files of the documentation folder. */
public class SkillManager {
	/** Fields of the reports. */
	static enum JsonFields implements CcpJsonFieldName{
		/** The {@code implicitSkills} field. */
		implicitSkills, 
		/** The {@code skill} field. */
		skill, 
		/** The {@code word} field. */
		word, 
		/** The {@code childrenCount} field. */
		childrenCount, 
		/** The {@code hasNoParent} field. */
		hasNoParent, 
		/** The {@code parent} field. */
		parent, 
		/** The {@code mirror} field. */
		mirror, 
		/** The {@code hasMirror} field. */
		hasMirror, 
		/** The {@code allParents} field. */
		allParents, 
		/** The {@code hasRepeatedParent} field. */
		hasRepeatedParent, 
		/** The {@code directParent} field. */
		directParent, 
		/** The {@code commonParents} field. */
		commonParents, 
		/** The {@code hasSkillsWithCommonParentsSize} field. */
		hasSkillsWithCommonParentsSize, 
		/** The {@code skillsWithCommonParents} field. */
		skillsWithCommonParents, 
		/** The {@code synonym} field. */
		synonym, 
		/** The {@code similar} field. */
		similar, 
		/** The {@code preRequisite} field. */
		preRequisite, 
		/** The {@code positionsCount} field. */
		positionsCount, 
		/** The {@code parentSize} field. */
		parentSize, 
		/** The {@code skillSize} field. */
		skillSize, 
		/** The {@code words} field. */
		words, 
		/** The {@code id} field. */
		id, 
		/** The {@code skillsPerParent} field. */
		skillsPerParent, 
		/** The {@code tipoVaga} field. */
		tipoVaga, 
		/** The {@code curriculo} field. */
		curriculo, 
		/** The {@code conteudo} field. */
		conteudo, 
		/** The {@code text} field. */
		text
	}
	/** Unused counter. */
	static int counter;
	
	/**
	 * Returns the spelling variants of a two-word skill (joined, hyphenated, dotted and, unless the second word is a number,
	 * reversed); a skill with another number of words is returned alone.
	 * @param word the skill
	 * @return the variants
	 */
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
	
	
	
	/**
	 * Returns the variants of every skill of the set.
	 * @param otherWords the skills
	 * @return the variants
	 */
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
	
	
	/** Writes {@code synonyms2.txt}: the synonym lines plus every reported skill missing from them, uppercase and sorted. */
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

	
	/**
	 * Writes {@code report_skills.json}: every skill of the {@code adicionarParent=} lines with its parents, children count
	 * and synonyms.
	 * @throws VisErrorSkillWithoutSynonyms when a skill is in no synonym group
	 * @throws VisErrorSkillWithManySynonymGroups when a skill is in more than one synonym group
	 */
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
	
	/**
	 * Returns the parents of the skill in the report.
	 * @param skill the skill
	 * @param report the report
	 * @return the parents
	 */
	static List<String> getParent(String skill, List<CcpJsonRepresentation> report){
		return report.stream().filter(x -> skill.equals(x.getAsString(JsonFields.skill))).findFirst().get()
				.getAsStringList(JsonFields.parent);
	}
	/**
	 * Builds a comparator over the fields in order: numbers descending, booleans true first, texts ascending.
	 * @param fields the fields
	 * @return the comparator
	 */
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

	/**
	 * Adds the skills that share more than one parent with this one.
	 * @param json the skill
	 * @param report the report
	 * @return the skill with {@code skillsWithCommonParents}
	 */
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
	
	/**
	 * Returns the parents both skills have.
	 * @param json1 a skill
	 * @param json2 another skill
	 * @return the common parents
	 */
	static List<String> getCommonParents(CcpJsonRepresentation json1, CcpJsonRepresentation json2) {
		List<String> firstParents = json1.getAsStringList(JsonFields.parent);
		List<String> secondParents = json2.getAsStringList(JsonFields.parent);
		List<String> commonParents = new CcpCollectionDecorator(firstParents).getIntersectList(secondParents);
		return commonParents;
	}
	
	
	/**
	 * Collects the parents of the skill recursively, repeated ones included.
	 * @param allParents the accumulated parents
	 * @param report the report
	 * @param json the skill
	 * @return the accumulated parents
	 */
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

	/**
	 * Returns the single parent of the skill when that parent has exactly one child (a "mirror"), otherwise an empty text.
	 * @param json the skill
	 * @param report the report
	 * @return the mirror or an empty text
	 */
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
