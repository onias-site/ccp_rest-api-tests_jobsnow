package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.query.CcpQueryExecutor;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityGrouper;
import com.jn.utils.JnGroupers;
import com.vis.business.recruiter.VisBusinessLoadRecruitersFromHtmlFolder;
import com.vis.business.recruiter.VisBusinessPrepareRecruiter;
import com.vis.services.VisServiceCompany;

/**
 * Proves the recruiters ({@code vis_recruiter}): the e-mail is kept as typed, the domain is the text after the
 * {@code @}, {@code professional} is false only for the non-professional domains of {@code jn_system_message}, and the
 * load of a folder of HTML files creates, with the source {@code telegram}, only the e-mails (without accents, in lower
 * case) that are neither in the entity nor in its twin. Since 2026-10-27, also the groups of the first three letters of the
 * domain in {@code jn_grouper} (a non-professional domain is in none), kept by the save, the delete, the seed and the
 * folder load, and the search of companies that reads them.
 */
public class RecruiterLoadsTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE
		);
	}

	private final long now = System.currentTimeMillis();

	private CcpJsonRepresentation input(String email, String source) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityRecruiter.Fields.email, email)
				.put(VisEntityRecruiter.Fields.source, source);
	}

	@Test
	public void theDomainIsTheWholeTextAfterTheAtSign() {
		CcpJsonRepresentation recruiter = VisBusinessPrepareRecruiter.INSTANCE.execute(this.input("maria.silva@rh.empresa.com.br", "telegram"));
		assertEquals("rh.empresa.com.br", recruiter.getAsString(VisEntityRecruiter.Fields.domain));
	}

	@Test
	public void aCompanyDomainIsProfessional() {
		CcpJsonRepresentation recruiter = VisBusinessPrepareRecruiter.INSTANCE.execute(this.input("maria@empresa.com.br", "telegram"));
		assertTrue(recruiter.getAsBoolean(VisEntityRecruiter.Fields.professional));
	}

	@Test
	public void aWebmailDomainIsNotProfessional() {
		CcpJsonRepresentation recruiter = VisBusinessPrepareRecruiter.INSTANCE.execute(this.input("maria@gmail.com", "telegram"));
		assertFalse(recruiter.getAsBoolean(VisEntityRecruiter.Fields.professional));
	}

	@Test
	public void theEmailLosesAccentsCapitalLettersAndSpacesAndIsNotHashed() {
		CcpJsonRepresentation recruiter = VisBusinessPrepareRecruiter.INSTANCE.execute(this.input("  João.Conceição@Empresa.com.br ", "telegram"));
		assertEquals("joao.conceicao@empresa.com.br", recruiter.getAsString(VisEntityRecruiter.Fields.email));
	}

	@Test(expected = CcpJsonValidationError.class)
	public void aTextThatIsNotAnEmailIsRefused() {
		VisBusinessPrepareRecruiter.INSTANCE.execute(this.input("not an email", "telegram"));
	}

	@Test(expected = CcpJsonValidationError.class)
	public void aSourceLongerThanTwentyCharactersIsRefused() {
		VisBusinessPrepareRecruiter.INSTANCE.execute(this.input("maria@empresa.com.br", "a source longer than twenty"));
	}

	@Test
	public void theFolderLoadCreatesOnlyTheEmailsThatAreNeitherInTheEntityNorInItsTwin() throws IOException {
		String newEmail = "novo" + this.now + "@recrutadora.com.br";
		String activeEmail = "ativo" + this.now + "@recrutadora.com.br";
		String inactiveEmail = "inativo" + this.now + "@recrutadora.com.br";
		CcpEntity twin = VisEntityRecruiter.ENTITY.getTwinEntity();

		VisEntityRecruiter.ENTITY.save(VisBusinessPrepareRecruiter.INSTANCE.execute(this.input(activeEmail, "legacy")));
		twin.save(VisBusinessPrepareRecruiter.INSTANCE.execute(this.input(inactiveEmail, "legacy")));

		Path folder = Files.createTempDirectory("recruiters");
		String accentedNewEmail = newEmail.replace("novo", "NÓVO");
		String html = "<html><body><p>Vaga: envie para <a href=\"mailto:" + accentedNewEmail + "\">" + accentedNewEmail + "</a>.</p>"
				+ "<p>" + activeEmail + "</p><div>" + inactiveEmail.toUpperCase() + "</div></body></html>";
		Files.write(folder.resolve("messages.html"), html.getBytes(StandardCharsets.UTF_8));
		Files.write(folder.resolve("ignored.txt"), ("ignorado" + this.now + "@recrutadora.com.br").getBytes(StandardCharsets.UTF_8));

		CcpJsonRepresentation folderInput = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("folderPath"), folder.toString());
		CcpJsonRepresentation result = VisBusinessLoadRecruitersFromHtmlFolder.INSTANCE.execute(folderInput);

		assertEquals(Integer.valueOf(1), result.getAsIntegerNumber(new CcpFieldName("filesRead")));
		assertEquals(Integer.valueOf(3), result.getAsIntegerNumber(new CcpFieldName("emailsFound")));
		assertEquals(Integer.valueOf(1), result.getAsIntegerNumber(new CcpFieldName("created")));
		assertEquals(Integer.valueOf(2), result.getAsIntegerNumber(new CcpFieldName("alreadyExisting")));

		CcpJsonRepresentation created = VisEntityRecruiter.ENTITY.getOneById(this.input(newEmail, "telegram"));
		assertEquals("telegram", created.getAsString(VisEntityRecruiter.Fields.source));
		assertEquals("recrutadora.com.br", created.getAsString(VisEntityRecruiter.Fields.domain));
		assertTrue(created.getAsBoolean(VisEntityRecruiter.Fields.professional));
		// the bulk goes around the decorator, so the load itself puts the new recruiter in the group of its domain
		String createdId = VisEntityRecruiter.ENTITY.calculateId(created);
		assertTrue(this.recruitersInTheGroupOf(false, "recrutadora.com.br").contains(createdId));

		CcpJsonRepresentation active = VisEntityRecruiter.ENTITY.getOneById(this.input(activeEmail, "telegram"));
		assertEquals("legacy", active.getAsString(VisEntityRecruiter.Fields.source));
		assertFalse(VisEntityRecruiter.ENTITY.exists(this.input(inactiveEmail, "telegram")));

		CcpJsonRepresentation secondResult = VisBusinessLoadRecruitersFromHtmlFolder.INSTANCE.execute(folderInput);
		assertEquals(Integer.valueOf(0), secondResult.getAsIntegerNumber(new CcpFieldName("created")));

		List<String> ids = List.of(newEmail, activeEmail);
		for (String email : ids) {
			VisEntityRecruiter.ENTITY.deleteAnyWhere(this.input(email, "telegram"));
		}
		twin.deleteAnyWhere(this.input(inactiveEmail, "telegram"));
		assertFalse(this.recruitersInTheGroupOf(false, "recrutadora.com.br").contains(createdId));
	}

	@Test
	public void theRecruiterFollowsTheGroupOfItsDomainWhenDeactivatedAndReactivated() {
		String domain = "qzk" + this.now + ".com.br";
		CcpJsonRepresentation recruiter = VisBusinessPrepareRecruiter.INSTANCE.execute(this.input("rh@" + domain, "login"));
		String recruiterId = VisEntityRecruiter.ENTITY.calculateId(recruiter);
		CcpEntity twin = VisEntityRecruiter.ENTITY.getTwinEntity();

		VisEntityRecruiter.ENTITY.save(recruiter);
		assertTrue(this.recruitersInTheGroupOf(false, domain).contains(recruiterId));
		assertFalse(this.recruitersInTheGroupOf(true, domain).contains(recruiterId));

		VisEntityRecruiter.ENTITY.delete(recruiter);
		assertFalse(this.recruitersInTheGroupOf(false, domain).contains(recruiterId));
		assertTrue(this.recruitersInTheGroupOf(true, domain).contains(recruiterId));

		twin.delete(recruiter);
		assertTrue(this.recruitersInTheGroupOf(false, domain).contains(recruiterId));
		assertFalse(this.recruitersInTheGroupOf(true, domain).contains(recruiterId));

		VisEntityRecruiter.ENTITY.deleteAnyWhere(recruiter);
		assertFalse(this.recruitersInTheGroupOf(false, domain).contains(recruiterId));
		assertFalse(this.recruitersInTheGroupOf(true, domain).contains(recruiterId));
	}

	@Test
	public void aRecruiterOfANonProfessionalDomainIsInNoGroup() {
		CcpJsonRepresentation recruiter = VisBusinessPrepareRecruiter.INSTANCE.execute(this.input("grupo" + this.now + "@gmail.com", "login"));
		String recruiterId = VisEntityRecruiter.ENTITY.calculateId(recruiter);

		VisEntityRecruiter.ENTITY.save(recruiter);
		try {
			assertFalse(this.recruitersInTheGroupOf(false, "gmail.com").contains(recruiterId));
		} finally {
			VisEntityRecruiter.ENTITY.deleteAnyWhere(recruiter);
		}
	}

	@Test
	public void theSeedPutsTheProfessionalRecruitersInTheGroupsOfTheirDomains() {
		CcpQueryExecutor executor = (CcpQueryExecutor) Proxy.newProxyInstance(this.getClass().getClassLoader(),
				new Class<?>[] { CcpQueryExecutor.class }, new LegacyRecruitersQueryHandler("ana@jobsnow.com", "bia@jobsbrasil.com.br", "caio@gmail.com", "davi@jobsnow.com"));
		CcpInstanceProvider<CcpQueryExecutor> provider = () -> executor;
		CcpDependencyInjection.loadAllDependencies(provider);
		List<CcpBulkItem> seed;
		try {
			seed = VisEntityRecruiter.INSTANCE.getFirstRecordsToInsert();
		} finally {
			CcpDependencyInjection.loadAllDependencies(CcpElasticSearchQueryExecutor.INSTANCE);
		}

		String grouperName = JnEntityGrouper.ENTITY.getEntityMetaData().entityName;
		List<CcpBulkItem> groups = seed.stream().filter(item -> grouperName.equals(item.entity.getEntityMetaData().entityName)).collect(Collectors.toList());
		assertEquals(1, groups.size());
		CcpJsonRepresentation group = groups.get(0).json;
		assertEquals(CcpBulkEntityOperationType.create, groups.get(0).operation);
		assertEquals(VisEntityVirtualRecruiterDomainInitialsGrouper.ENTITY.getEntityMetaData().entityName, group.getAsString(JnEntityGrouper.Fields.parentName));
		assertEquals("JOB", group.getInnerJson(JnEntityGrouper.Fields.parentJson).getAsString(VisEntityVirtualRecruiterDomainInitialsGrouper.Fields.firstThreeInitials));
		List<String> expectedChildren = Stream.of("ana@jobsnow.com", "bia@jobsbrasil.com.br", "davi@jobsnow.com")
				.map(email -> VisEntityRecruiter.ENTITY.calculateId(this.input(email, "legacy"))).collect(Collectors.toList());
		assertEquals(expectedChildren, group.getAsStringList(JnEntityGrouper.Fields.child));
		assertEquals(Integer.valueOf(3), group.getAsIntegerNumber(JnEntityGrouper.Fields.childCount));
	}

	@Test
	public void theCompanySearchSuggestsTheCompaniesOfTheRecruitersOfTheGroup() {
		String techDomain = "qzw" + this.now + "tech.com.br";
		String labsDomain = "qzw" + this.now + "labs.com";
		CcpJsonRepresentation techRecruiter = VisBusinessPrepareRecruiter.INSTANCE.execute(this.input("rh@" + techDomain, "login"));
		CcpJsonRepresentation labsRecruiter = VisBusinessPrepareRecruiter.INSTANCE.execute(this.input("rh@" + labsDomain, "login"));
		VisEntityRecruiter.ENTITY.save(techRecruiter);
		VisEntityRecruiter.ENTITY.save(labsRecruiter);
		String techCompany = VisEntityVirtualRecruiterDomainInitialsGrouper.getCompanyName(techDomain);
		String labsCompany = VisEntityVirtualRecruiterDomainInitialsGrouper.getCompanyName(labsDomain);
		try {
			assertEquals(List.of(labsCompany, techCompany), this.searchCompanies("qzw" + this.now));
			assertEquals(List.of(techCompany), this.searchCompanies("QZW" + this.now + "T"));
			assertTrue(this.searchCompanies("qzw").containsAll(List.of(labsCompany, techCompany)));
			assertEquals(List.of("Qzw" + this.now + "Nada"), this.searchCompanies("Qzw" + this.now + "Nada"));
		} finally {
			VisEntityRecruiter.ENTITY.deleteAnyWhere(techRecruiter);
			VisEntityRecruiter.ENTITY.deleteAnyWhere(labsRecruiter);
		}
		assertEquals(List.of("qzw" + this.now), this.searchCompanies("qzw" + this.now));
	}

	private List<String> searchCompanies(String search) {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON.put(VisServiceCompany.FieldsToSearchCompaniesByTheirFirstThreeInitials.search, search);
		CcpJsonRepresentation suggestions = VisServiceCompany.SearchCompaniesByTheirFirstThreeInitials.execute(input);
		return suggestions.getAsStringList(VisServiceCompany.FieldsToSearchCompaniesByTheirFirstThreeInitials.companies);
	}

	/** The ids of the recruiters in the group of the first three letters of the domain, on one side of the grouper. */
	private List<String> recruitersInTheGroupOf(boolean twinSide, String domain) {
		String parentName = VisEntityVirtualRecruiterDomainInitialsGrouper.ENTITY.getEntityMetaData().entityName;
		String childName = VisEntityRecruiter.ENTITY.getEntityMetaData().entityName;
		String parentId = VisEntityVirtualRecruiterDomainInitialsGrouper.getParentId(domain);
		return JnGroupers.getChildren(twinSide, parentName, childName, parentId);
	}
}
