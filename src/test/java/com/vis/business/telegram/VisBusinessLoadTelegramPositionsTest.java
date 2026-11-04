package com.vis.business.telegram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
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
import com.vis.entities.VisEntityTelegramPosition;

/**
 * Proves {@link VisBusinessLoadTelegramPositions} and {@link VisTelegramFileLoad} against the local Elasticsearch: each
 * file of the folder is read in a thread, a message repeated on the same day counts once, a message with no contact or
 * no skill is left out, only the files of messages are read, the positions reach the index and the files are confirmed.
 * Until 2026-11-03 the load had no test: it ran only by hand ({@code executaCargaTelegram.bat}).
 */
public class VisBusinessLoadTelegramPositionsTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				CcpLocalInstances.instantMessenger,
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

	private final VisTelegramLoadTestFiles files = new VisTelegramLoadTestFiles();

	@Before
	public void writeTheFiles() {
		this.files.write();
	}

	@After
	public void deleteThePositionsAndTheFiles() {
		this.files.deleteEverything();
	}

	private CcpJsonRepresentation load(int threads) {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON
				.put(VisTelegramLoadFields.folderPath, this.files.folderPath)
				.put(VisTelegramLoadFields.threads, threads);
		CcpJsonRepresentation counters = VisBusinessLoadTelegramPositions.INSTANCE.execute(input);
		return counters;
	}

	private int counter(CcpJsonRepresentation counters, VisTelegramLoadFields field) {
		Integer counter = counters.getAsIntegerNumber(field);
		return counter;
	}

	@Test
	public void thePositionsOfBothKindsOfFileReachTheIndex() {
		CcpJsonRepresentation counters = this.load(2);

		assertEquals(counters.toString(), 2, this.counter(counters, VisTelegramLoadFields.filesRead));
		assertEquals(counters.toString(), 5, this.counter(counters, VisTelegramLoadFields.messagesRead));
		assertEquals(counters.toString(), 1, this.counter(counters, VisTelegramLoadFields.repeatedMessages));
		assertEquals(counters.toString(), 1, this.counter(counters, VisTelegramLoadFields.noContact));
		assertEquals(counters.toString(), 1, this.counter(counters, VisTelegramLoadFields.noSkill));
		assertEquals(counters.toString(), 2, this.counter(counters, VisTelegramLoadFields.positionsWritten));
		assertEquals(counters.toString(), 0, this.counter(counters, VisTelegramLoadFields.notConfirmed));

		List<String> confirmedFiles = counters.getAsStringList(VisTelegramLoadFields.confirmedFiles);
		String htmlPath = this.files.pathOf(VisTelegramLoadTestFiles.HTML_FILE);
		String whatsAppPath = this.files.pathOf(VisTelegramLoadTestFiles.WHATSAPP_FILE);
		assertEquals(2, confirmedFiles.size());
		assertTrue(confirmedFiles.toString(), confirmedFiles.containsAll(Arrays.asList(htmlPath, whatsAppPath)));
		assertTrue(counters.toString(), counters.getAsStringList(VisTelegramLoadFields.failedFiles).isEmpty());

		List<CcpJsonRepresentation> positions = this.files.expectedPositions();
		for (CcpJsonRepresentation position : positions) {
			boolean inTheIndex = VisEntityTelegramPosition.ENTITY.exists(position);
			assertTrue(position.toString(), inTheIndex);
		}
	}

	@Test
	public void thePositionsReadFromTheFilesCarryTheirSkillMonthAndRegion() {
		List<CcpJsonRepresentation> positions = this.files.expectedPositions();

		CcpJsonRepresentation telegramPosition = positions.get(0);
		assertEquals("JAVA", telegramPosition.getAsString(VisEntityTelegramPosition.Fields.skill));
		assertEquals(VisTelegramLoadTestFiles.YEAR_MONTH, telegramPosition.getAsString(VisEntityTelegramPosition.Fields.yearMonth));
		assertEquals("senior", telegramPosition.getAsString(VisEntityTelegramPosition.Fields.seniority));
		assertEquals(this.files.telegramContact, telegramPosition.getAsString(VisEntityTelegramPosition.Fields.contact));
		// a message of WhatsApp is home office (ddd 10)
		CcpJsonRepresentation whatsAppPosition = positions.get(1);
		assertEquals(Integer.valueOf(10), whatsAppPosition.getAsIntegerNumber(VisEntityTelegramPosition.Fields.ddd));
	}

	@Test
	public void loadingTheSameFolderAgainWritesTheSameIds() {
		this.load(1);

		CcpJsonRepresentation secondLoad = this.load(1);

		assertEquals(secondLoad.toString(), 2, this.counter(secondLoad, VisTelegramLoadFields.positionsWritten));
		assertEquals(secondLoad.toString(), 0, this.counter(secondLoad, VisTelegramLoadFields.notConfirmed));
	}

	@Test
	public void anEmptyFolderReadsNothing() {
		this.files.deletePositionsAndFiles();

		CcpJsonRepresentation counters = this.load(1);

		assertEquals(counters.toString(), 0, this.counter(counters, VisTelegramLoadFields.filesRead));
		assertEquals(counters.toString(), 0, this.counter(counters, VisTelegramLoadFields.positionsWritten));
		assertTrue(counters.toString(), counters.getAsStringList(VisTelegramLoadFields.confirmedFiles).isEmpty());
	}

	@Test(expected = CcpJsonValidationError.class)
	public void theThreadsAreRequired() {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON.put(VisTelegramLoadFields.folderPath, this.files.folderPath);

		VisBusinessLoadTelegramPositions.INSTANCE.execute(input);
	}
}
