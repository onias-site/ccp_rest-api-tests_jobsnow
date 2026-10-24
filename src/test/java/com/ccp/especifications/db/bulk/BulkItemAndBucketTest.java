package com.ccp.especifications.db.bulk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.file.bucket.CcpFileBucket;
import com.ccp.especifications.file.bucket.CcpFileBucketOperation;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.jn.entities.JnEntitySupportPendingCommand;
import com.jn.entities.JnEntityLoginEmail;

/**
 * Proves the identity and the text form of a {@link CcpBulkItem}, and the bucket operations of
 * {@link CcpFileBucketOperation} over the local bucket (files under {@code c:/logs}).
 */
public class BulkItemAndBucketTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE, CcpLocalInstances.bucket);
	}

	private final CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(JnEntitySupportPendingCommand.Fields.command, "t");

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void twoItemsAreTheSameWhenEntityAndIdAreTheSame() {
		CcpBulkItem create = new CcpBulkItem(this.json, CcpBulkEntityOperationType.create, JnEntitySupportPendingCommand.ENTITY, "1");
		CcpBulkItem delete = new CcpBulkItem(create, CcpBulkEntityOperationType.delete);
		CcpBulkItem otherId = new CcpBulkItem(this.json, CcpBulkEntityOperationType.create, JnEntitySupportPendingCommand.ENTITY, "2");
		CcpBulkItem otherEntity = new CcpBulkItem(this.json, CcpBulkEntityOperationType.create, JnEntityLoginEmail.ENTITY, "1");

		assertEquals(create, delete);
		assertEquals(create.hashCode(), delete.hashCode());
		assertNotEquals(create, otherId);
		assertNotEquals(create, otherEntity);
		assertFalse("not an item".equals(create));
		Set<CcpBulkItem> unique = new HashSet<>();
		unique.add(create);
		unique.add(delete);
		assertEquals(1, unique.size());
		assertEquals(CcpBulkEntityOperationType.delete, delete.operation);
		assertEquals(create.json, delete.json);
	}

	@Test
	public void theTextFormSummarizesTheOperationTheEntityAndTheId() {
		CcpBulkItem item = new CcpBulkItem(this.json, CcpBulkEntityOperationType.update, JnEntitySupportPendingCommand.ENTITY, "abc");

		CcpJsonRepresentation asMap = item.asMap();
		assertEquals("update", asMap.getAsString(CcpBulkItemFields.operation));
		assertEquals("jn_support_pending_command", asMap.getAsString(CcpBulkItemFields.entity));
		assertEquals("abc", asMap.getAsString(CcpBulkItemFields.id));
		String text = item.toString();
		assertTrue(text, text.contains("jn_support_pending_command") && text.contains("abc") && text.contains("update"));
	}

	@Test
	public void theBucketOperationsReadAndDeleteTheFilesOfAFolder() {
		CcpFileBucket bucket = CcpDependencyInjection.getDependency(CcpFileBucket.class);
		String folder = "bucket_operation_test_" + System.nanoTime();
		bucket.save("tenant", folder, "a.txt", "content of a");
		bucket.save("tenant", folder, "b.txt", "content of b");

		String read = CcpFileBucketOperation.get.execute("tenant", folder, "a.txt");
		assertEquals("content of a", read.trim());

		CcpFileBucketOperation.get.execute("tenant", folder, "a.txt", "b.txt");
		CcpFileBucketOperation.deleteFolder.execute("tenant", folder, "ignored.txt");

		assertFalse(new File("c:/logs/" + folder + "/a.txt").exists());
		assertFalse(new File("c:/logs/" + folder + "/b.txt").exists());
	}

	@Test
	public void aProviderOfTheBucketGivesTheLocalImplementation() {
		CcpInstanceProvider<?> provider = CcpLocalInstances.bucket::getInstance;

		assertTrue(provider.getInstance() instanceof CcpFileBucket);
	}
}
