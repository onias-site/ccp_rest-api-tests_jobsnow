package com.ccp.random;

import java.util.LinkedHashSet;
import java.util.function.Consumer;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.random.CcpRandomScripts.JsonFields;

/**
 * Consumer of {@link CcpRandomScripts#saveGroupedCompanies()}: adds the company of the recruiter e-mail (first part of the
 * domain, capitalized, at least 3 letters) to {@link CcpRandomScripts#groupedCompanies}, by its first three letters.
 */
class RandomScriptsCompanyGrouper implements Consumer<CcpJsonRepresentation> {

	/** The single instance. */
	public static final RandomScriptsCompanyGrouper INSTANCE = new RandomScriptsCompanyGrouper();

	private RandomScriptsCompanyGrouper() {}

	public void accept(CcpJsonRepresentation json) {
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
		LinkedHashSet<String> orDefault = CcpRandomScripts.groupedCompanies.getOrDefault(new CcpFieldName(initials), () -> new LinkedHashSet<>());
		orDefault.add(capitalizedCompanyName);
		CcpRandomScripts.groupedCompanies = CcpRandomScripts.groupedCompanies.put(new CcpFieldName(initials), orDefault);
	}
}
