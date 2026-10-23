package com.vis.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.vis.skills.SkillManager.JsonFields;

/**
 * Function of {@link SkillManager}: adds to the skill its parents, the other skills of the {@code adicionarParent=} line
 * that starts with it. A skill with no such line is returned as it is.
 */
class SkillWithItsParents implements Function<CcpJsonRepresentation, CcpJsonRepresentation> {

	/** The skills of each {@code adicionarParent=} line, the child first. */
	private final List<List<String>> skillLines;

	/**
	 * Keeps the lines of skills.
	 * @param skillLines the skills of each {@code adicionarParent=} line, the child first
	 */
	SkillWithItsParents(List<List<String>> skillLines) {
		this.skillLines = skillLines;
	}

	/**
	 * Adds the parents to the skill.
	 * @param x the skill
	 * @return the skill with its parents
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation x) {
		Optional<List<String>> lineStartingWithSkill = new ArrayList<>(this.skillLines).stream().filter(skillsInThisLine -> skillsInThisLine.indexOf(x.getAsString(JsonFields.skill)) == 0).findFirst();
		boolean hasNoParent = false == lineStartingWithSkill.isPresent();
		if(hasNoParent) {
			return x;
		}
		List<String> skillLine = lineStartingWithSkill.get();
		List<String> parent = skillLine.subList(1, skillLine.size());
		CcpJsonRepresentation skillWithParent = x.put(JsonFields.parent, parent);
		return skillWithParent;
	}
}
