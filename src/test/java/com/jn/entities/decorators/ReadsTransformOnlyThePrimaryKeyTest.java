package com.jn.entities.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.password.CcpPasswordHandler;
import com.ccp.hash.CcpHashAlgorithm;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * A read only needs the id of the record, so the field transformer applies to it only the transformers of the primary
 * key fields; a write still gets every one. The password entity tells them apart: its key, the e-mail, becomes a SHA-1
 * hash, while its password becomes a BCrypt hash, which costs about 250 ms and, being salted, could never be part of an
 * id. Until 2026-10-09 every read of a json carrying the typed password paid that hash, which is what still made the
 * login and the password change slow (one to two seconds) after the fix of the empty password.
 */
public class ReadsTransformOnlyThePrimaryKeyTest {

	private static final CountingPasswordHandler PASSWORDS = new CountingPasswordHandler();

	private static final String EMAIL = "leitura.so.da.chave@teste.com";

	private static final String PASSWORD = "Leitura@2026";

	@BeforeClass
	public static void loadDependencies() {
		CcpInstanceProvider<CcpPasswordHandler> passwords = () -> PASSWORDS;
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				new CcpElasticSearchDbRequest(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpElasticSearchQueryExecutor(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				passwords
		);
	}

	@After
	public void deleteTheRecord() {
		JnEntityLoginPassword.ENTITY.delete(this.login());
		PASSWORDS.reset();
	}

	@Test
	public void theJsonOfAReadHasOnlyThePrimaryKeyTransformed() {
		PASSWORDS.reset();

		CcpJsonRepresentation handled = JnEntityLoginPassword.ENTITY.getJsonWithHandledPrimaryKey(this.login());

		assertEquals(this.emailHash(), handled.getAsString(JnJsonCommonsFields.email));
		assertEquals(PASSWORD, handled.getAsString(JnJsonCommonsFields.password));
		assertEquals(0, PASSWORDS.hashes.get());
	}

	@Test
	public void theJsonOfAWriteHasEveryFieldTransformed() {
		PASSWORDS.reset();

		CcpJsonRepresentation handled = JnEntityLoginPassword.ENTITY.getHandledJson(this.login());

		String hashedPassword = handled.getAsString(JnJsonCommonsFields.password);
		assertEquals(this.emailHash(), handled.getAsString(JnJsonCommonsFields.email));
		assertNotEquals(PASSWORD, hashedPassword);
		assertTrue(PASSWORDS.matches(PASSWORD, hashedPassword));
		assertEquals(1, PASSWORDS.hashes.get());
	}

	/**
	 * Every way of reading the record, with the typed password in the json as the login sends it, finds the record
	 * without computing a single hash; the password saved is still the BCrypt hash of the typed one.
	 */
	@Test
	public void readingWithThePasswordInTheJsonComputesNoHash() {
		CcpJsonRepresentation login = this.login();
		JnEntityLoginPassword.ENTITY.save(login);
		PASSWORDS.reset();

		assertTrue(JnEntityLoginPassword.ENTITY.exists(login));

		CcpJsonRepresentation saved = JnEntityLoginPassword.ENTITY.getOneById(login);
		String savedPassword = saved.getAsString(JnJsonCommonsFields.password);
		assertTrue(PASSWORDS.matches(PASSWORD, savedPassword));

		List<CcpJsonRepresentation> parametersToSearch = JnEntityLoginPassword.ENTITY.getParametersToSearch(login);
		// the main entity and its twin, where a locked password goes
		assertEquals(2, parametersToSearch.size());

		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpSelectUnionAll unionAll = crud.unionAll(login, JnDeleteKeysFromCache.INSTANCE, JnEntityLoginPassword.ENTITY);
		assertTrue(JnEntityLoginPassword.ENTITY.isPresentInThisUnionAll(unionAll, login));
		Supplier<CcpJsonRepresentation> loginSupplier = login.getJsonSupplier();
		CcpJsonRepresentation fromTheUnionAll = JnEntityLoginPassword.ENTITY.getRecordFromUnionAll(unionAll, loginSupplier);
		assertFalse(fromTheUnionAll.isEmpty());

		assertEquals(0, PASSWORDS.hashes.get());
	}

	private CcpJsonRepresentation login() {
		CcpJsonRepresentation loginWithEmail = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.email, EMAIL);
		CcpJsonRepresentation login = loginWithEmail.put(JnJsonCommonsFields.password, PASSWORD);
		return login;
	}

	private String emailHash() {
		CcpStringDecorator email = new CcpStringDecorator(EMAIL);
		String emailHash = email.email().hash().asString(CcpHashAlgorithm.SHA1);
		return emailHash;
	}
}
