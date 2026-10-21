package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
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
import com.vis.business.recruiter.VisBusinessLoadRecruitersFromHtmlFolder;
import com.vis.business.recruiter.VisBusinessPrepareRecruiter;

/**
 * Proves the recruiters ({@code vis_recruiter}): the e-mail is kept as typed, the domain is the text after the
 * {@code @}, {@code professional} is false only for the non-professional domains of {@code jn_system_message}, and the
 * load of a folder of HTML files creates, with the source {@code telegram}, only the e-mails (without accents, in lower
 * case) that are neither in the entity nor in its twin.
 */
public class RecruiterLoadsTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				new CcpMindrotPasswordHandler(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp()
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
	}
}
