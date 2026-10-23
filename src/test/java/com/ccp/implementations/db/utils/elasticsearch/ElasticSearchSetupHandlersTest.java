package com.ccp.implementations.db.utils.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.Test;

import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.especifications.db.utils.entity.fields.CcpErrorDbUtilsIncorrectEntityFields;

/**
 * The handlers of the setup of the entities: {@link ElasticSearchMappingErrorWriter} writes the message of a wrong
 * mapping to the file, and {@link ElasticSearchSetupErrorHandler} ignores a class not found and rethrows any other failure.
 */
public class ElasticSearchSetupHandlersTest {

	@Test
	public void theMessageOfTheWrongMappingGoesToTheFile() throws Exception {
		File temporaryFile = Files.createTempFile("mapping-errors", ".txt").toFile();
		temporaryFile.deleteOnExit();
		CcpFileDecorator file = new CcpStringDecorator(temporaryFile.getAbsolutePath()).file().reset();

		new ElasticSearchMappingErrorWriter(file).accept(new CcpErrorDbUtilsIncorrectEntityFields("the field x is missing"));

		List<String> lines = file.getLines();
		assertEquals(List.of("the field x is missing"), lines);
	}

	@Test
	public void aClassNotFoundIsIgnored() {
		ElasticSearchSetupErrorHandler.INSTANCE.accept(new ClassNotFoundException("com.any.Missing"));
	}

	@Test
	public void anyOtherFailureIsRethrownWithItsCause() {
		IllegalStateException failure = new IllegalStateException("broken");
		try {
			ElasticSearchSetupErrorHandler.INSTANCE.accept(failure);
			fail("the failure was swallowed");
		} catch (RuntimeException e) {
			assertSame(failure, e.getCause());
		}
	}
}
