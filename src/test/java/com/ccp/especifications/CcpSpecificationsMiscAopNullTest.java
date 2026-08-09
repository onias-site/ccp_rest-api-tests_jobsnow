package com.ccp.especifications;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.cache.CcpCache;
import com.ccp.especifications.db.bulk.CcpExecuteBulkOperation;
import com.ccp.especifications.file.bucket.CcpFileBucketOperation;
import com.ccp.especifications.instant.messenger.CcpErrorInstantMessageThisBotWasBlockedByThisUser;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;
import com.ccp.especifications.mensageria.sender.CcpMensageriaSender;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre as especificações que possuem poucos métodos
 * pendentes: {@code CcpCache}, {@code CcpFileBucketOperation}, {@code CcpMensageriaReceiver},
 * {@code CcpMensageriaSender} e a exceção de bot bloqueado do instant messenger.
 */
public class CcpSpecificationsMiscAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), CcpLocalCacheInstances.mock,
				CcpLocalInstances.mensageriaSender);
	}

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	/** Implementação mínima para alcançar os métodos concretos de {@code CcpMensageriaReceiver}. */
	private static final class MensageriaReceiverForTest extends CcpMensageriaReceiver {

		MensageriaReceiverForTest() {
			super("operation");
		}

		public CcpExecuteBulkOperation getExecuteBulkOperation() {
			return com.jn.db.bulk.JnExecuteBulkOperation.INSTANCE;
		}

		public Consumer<String[]> getFunctionToDeleteKeysInTheCache() {
			return com.jn.utils.JnDeleteKeysFromCache.INSTANCE;
		}
	}

	// ── CcpCache ──────────────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void cacheIsPresentNullTest() {
		CcpCache cache = CcpDependencyInjection.getDependency(CcpCache.class);
		cache.isPresent(null);
	}

	// ── CcpFileBucketOperation ────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void fileBucketExecuteVarargsTenantNullTest() {
		CcpFileBucketOperation.get.execute(null, "pasta", "arquivo1", "arquivo2");
	}

	@Test(expected = CcpNullParameterException.class)
	public void fileBucketExecuteVarargsFolderNullTest() {
		CcpFileBucketOperation.get.execute("tenant", null, "arquivo1", "arquivo2");
	}

	@Test(expected = CcpNullParameterException.class)
	public void fileBucketExecuteVarargsFilesNullTest() {
		CcpFileBucketOperation.get.execute("tenant", "pasta", (String[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void fileBucketExecuteTenantNullTest() {
		CcpFileBucketOperation.get.execute(null, "pasta", "arquivo");
	}

	@Test(expected = CcpNullParameterException.class)
	public void fileBucketExecuteFolderNullTest() {
		CcpFileBucketOperation.get.execute("tenant", null, "arquivo");
	}

	@Test(expected = CcpNullParameterException.class)
	public void fileBucketExecuteFileNullTest() {
		CcpFileBucketOperation.get.execute("tenant", "pasta", (String) null);
	}

	// ── CcpErrorInstantMessageThisBotWasBlockedByThisUser ─────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void errorBotBlockedConstrutorNullTest() {
		new CcpErrorInstantMessageThisBotWasBlockedByThisUser(null);
	}

	// ── CcpMensageriaReceiver ─────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void mensageriaReceiverConstrutorNullTest() {
		new CcpMensageriaReceiver(null) {

			public CcpExecuteBulkOperation getExecuteBulkOperation() {
				return com.jn.db.bulk.JnExecuteBulkOperation.INSTANCE;
			}

			public Consumer<String[]> getFunctionToDeleteKeysInTheCache() {
				return com.jn.utils.JnDeleteKeysFromCache.INSTANCE;
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
		sender().sendToMensageria("topico", null, new ArrayList<CcpJsonRepresentation>());
	}

	@Test(expected = CcpNullParameterException.class)
	public void senderSendToMensageriaListMsgsNullTest() {
		sender().sendToMensageria("topico", CcpSpecificationsMiscAopNullTest.class,
				(java.util.List<CcpJsonRepresentation>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void senderSendToMensageriaVarargsTopicNullTest() {
		sender().sendToMensageria(null, CcpSpecificationsMiscAopNullTest.class, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void senderSendToMensageriaVarargsValidationClassNullTest() {
		sender().sendToMensageria("topico", null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void senderSendToMensageriaVarargsMsgsNullTest() {
		sender().sendToMensageria("topico", CcpSpecificationsMiscAopNullTest.class,
				(CcpJsonRepresentation[]) null);
	}
}
