package com.ccp.especifications;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.cache.CcpCache;
import com.ccp.especifications.db.bulk.CcpExecuteBulkOperation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.file.bucket.CcpFileBucketOperation;
import com.ccp.especifications.instant.messenger.CcpErrorInstantMessageThisBotWasBlockedByThisUser;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;
import com.ccp.especifications.mensageria.sender.CcpMensageriaSender;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import org.junit.Test;

/**
 * Coverage of {@code CcpNullParameterAspect} over the specifications that have few pending
 * methods: {@code CcpCache}, {@code CcpFileBucketOperation}, {@code CcpMensageriaReceiver},
 * {@code CcpMensageriaSender} and the instant messenger's blocked-bot exception.
 */
public class CcpSpecificationsMiscAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), CcpLocalCacheInstances.mock,
				CcpLocalInstances.syncMensageriaListener);
	}

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	/** Minimal implementation to reach the concrete methods of {@code CcpMensageriaReceiver}. */


	// ── CcpCache ──────────────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void cacheIsPresentNullTest() {
		CcpCache cache = CcpDependencyInjection.getDependency(CcpCache.class);
		cache.isPresent(null);
	}

	// ── CcpFileBucketOperation ────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void fileBucketExecuteVarargsTenantNullTest() {
		CcpFileBucketOperation.get.execute(null, "folder", "file1", "file2");
	}

	@Test(expected = CcpNullParameterException.class)
	public void fileBucketExecuteVarargsFolderNullTest() {
		CcpFileBucketOperation.get.execute("tenant", null, "file1", "file2");
	}

	@Test(expected = CcpNullParameterException.class)
	public void fileBucketExecuteVarargsFilesNullTest() {
		CcpFileBucketOperation.get.execute("tenant", "folder", (String[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void fileBucketExecuteTenantNullTest() {
		CcpFileBucketOperation.get.execute(null, "folder", "file");
	}

	@Test(expected = CcpNullParameterException.class)
	public void fileBucketExecuteFolderNullTest() {
		CcpFileBucketOperation.get.execute("tenant", null, "file");
	}

	@Test(expected = CcpNullParameterException.class)
	public void fileBucketExecuteFileNullTest() {
		CcpFileBucketOperation.get.execute("tenant", "folder", (String) null);
	}

	// ── CcpErrorInstantMessageThisBotWasBlockedByThisUser ─────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void errorBotBlockedConstructorNullTest() {
		new CcpErrorInstantMessageThisBotWasBlockedByThisUser(null);
	}

	// ── CcpMensageriaReceiver ─────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void mensageriaReceiverConstructorNullTest() {
		new CcpMensageriaReceiver(null) {

			public CcpExecuteBulkOperation getExecuteBulkOperation() {
				return com.jn.db.bulk.JnExecuteBulkOperation.INSTANCE;
			}

			public Consumer<String[]> getFunctionToDeleteKeysInTheCache() {
				return com.jn.utils.JnDeleteKeysFromCache.INSTANCE;
			}

			protected CcpEntity getTwinEntity(CcpEntity entity) {
				return null;
			}

			protected CcpEntity getCustomEntity(Object newInstance) {
				return null;
			}
		};
	}

	@Test(expected = CcpNullParameterException.class)
	public void mensageriaReceiverGetProcessNameNullTest() {
		new MensageriaReceiverForTest().getProcess(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void mensageriaReceiverGetProcessJsonNullTest() {
		new MensageriaReceiverForTest().getProcess("com.jn.utils.JnDeleteKeysFromCache", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void mensageriaReceiverGetInstanceNullTest() {
		CcpMensageriaReceiver.getInstance(null);
	}

	// ── CcpMensageriaSender ───────────────────────────────────────────────────

	private static CcpMensageriaSender sender() {
		return CcpDependencyInjection.getDependency(CcpMensageriaSender.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void senderSendToMensageriaListTopicNullTest() {
		sender().sendToMensageria(null, CcpSpecificationsMiscAopNullTest.class,
				new ArrayList<CcpJsonRepresentation>());
	}

	@Test(expected = CcpNullParameterException.class)
	public void senderSendToMensageriaListValidationClassNullTest() {
		sender().sendToMensageria("topic", null, new ArrayList<CcpJsonRepresentation>());
	}

	@Test(expected = CcpNullParameterException.class)
	public void senderSendToMensageriaListMsgsNullTest() {
		sender().sendToMensageria("topic", CcpSpecificationsMiscAopNullTest.class,
				(java.util.List<CcpJsonRepresentation>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void senderSendToMensageriaVarargsTopicNullTest() {
		sender().sendToMensageria(null, CcpSpecificationsMiscAopNullTest.class, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void senderSendToMensageriaVarargsValidationClassNullTest() {
		sender().sendToMensageria("topic", null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void senderSendToMensageriaVarargsMsgsNullTest() {
		sender().sendToMensageria("topic", CcpSpecificationsMiscAopNullTest.class,
				(CcpJsonRepresentation[]) null);
	}
}
