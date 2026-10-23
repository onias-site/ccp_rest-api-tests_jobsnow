package com.ccp.implementations.db.utils.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.fields.CcpErrorDbUtilsIncorrectEntityFields;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * {@link ElasticSearchEntitySetupFromJavaFile} sets up the entity of each java file of the entities folder: a file whose
 * class does not exist and a class that is not an entity are skipped, and a failure goes to the handler of unexpected
 * errors. The path of the classes is cut at the host folder to give the package.
 */
public class ElasticSearchEntitySetupFromJavaFileTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE, CcpApacheMimeHttp.INSTANCE);
	}

	private static final String PATH_TO_CLASSES = "src/test/java/com/ccp/implementations/db/utils/elasticsearch";

	private final List<CcpBulkItem> bulkItems = new ArrayList<>();

	private final List<CcpErrorDbUtilsIncorrectEntityFields> incorrectFields = new ArrayList<>();

	private final List<Throwable> unexpectedErrors = new ArrayList<>();

	private void setUp(String javaFileName) {
		CcpHttpRequester http = CcpDependencyInjection.getDependency(CcpHttpRequester.class);
		ElasticSearchEntitySetupFromJavaFile setup = new ElasticSearchEntitySetupFromJavaFile(new ElasticSearchDbRequester(), http,
				PATH_TO_CLASSES, "java/", "folder/without/scripts", this.bulkItems, this.incorrectFields::add, this.unexpectedErrors::add);
		setup.accept(new CcpStringDecorator(PATH_TO_CLASSES + "/" + javaFileName).file());
	}

	@Test
	public void aFileWhoseClassDoesNotExistIsSkipped() {
		this.setUp("NoSuchClass.java");

		assertTrue(this.bulkItems.isEmpty() && this.incorrectFields.isEmpty() && this.unexpectedErrors.isEmpty());
	}

	@Test
	public void aClassThatIsNotAnEntityIsSkipped() {
		this.setUp("ElasticSearchEntitySetupFromJavaFileTest.java");

		assertTrue(this.bulkItems.isEmpty() && this.incorrectFields.isEmpty() && this.unexpectedErrors.isEmpty());
	}

	@Test
	public void aFailureOfTheSetupGoesToTheHandlerOfUnexpectedErrors() {
		this.setUp("SetupTestEntity.java");

		assertEquals(1, this.unexpectedErrors.size());
		assertTrue(this.bulkItems.isEmpty() && this.incorrectFields.isEmpty());
	}
}
