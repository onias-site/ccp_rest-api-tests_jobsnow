package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.aop.CcpNullReturnException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityAsyncTask;
import static com.ccp.decorators.JsonFieldNames.*;

public class CcpCollectionDecoratorTest {
	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void isLongNumberListTest() {
		List<Integer> integers = Arrays.asList(1, 2, 3);
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(integers);
		boolean longNumberList = decorator.isLongNumberList();
		assertTrue(longNumberList);
	}

	@Test
	public void isNotLongNumberListTest() {
		Object[] array = new Object[] { 1, 2, 3, 5.7 };
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(array);
		boolean longNumberList = decorator.isLongNumberList();
		assertFalse(longNumberList);
	}

	@Test
	public void isDoubleNumberListTest() {
		List<Double> asDoubleList = Arrays.asList(-Double.MIN_VALUE, -Double.MAX_VALUE, Double.MIN_VALUE,
				Double.MAX_VALUE);
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(asDoubleList);
		boolean validDoubleList = decorator.isDoubleNumberList();
		assertTrue(validDoubleList);
	}

	@Test
	public void isNotDoubleNumberListTest() {
		// Object[] asDoubleInt = new Object[]{String.class, "String", false, true, new
		// Object() };
		List<Object> array = Arrays.asList(String.class, "String", false, true, new Object());
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(array);
		boolean validDoubleList = decorator.isDoubleNumberList();
		assertFalse(validDoubleList);
	}

	@Test
	public void isBooleanListTest() {
		int number = 1;
		List<Boolean> asBooleanList = Arrays.asList(true, false, number == 1, (true && false));
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(asBooleanList);
		boolean validBooleanList = decorator.isBooleanList();
		assertTrue(validBooleanList);
	}

	@Test
	public void isNotBooleanListTest() {
		List<Object> array = Arrays.asList('A', // Char
				42, // Integer
				3.14, // Double
				"Texto", // String
				new ArrayList<>(), // empty List
				new HashMap<>(), // empty Map
				new Object() // generic Object instance
		);

		CcpCollectionDecorator decorator = new CcpCollectionDecorator(array);
		boolean validBooleanList = decorator.isBooleanList();
		assertFalse(validBooleanList);
	}

	@Test
	public void isJsonListValidTest() {
		String firstJsonAsString = "{'name':'Alice'," + "'sobrenome':'almeida'," + "'age':32}";

		String secondJsonAsString = "{'país':'Brasil'," + "'estado':'são paulo'," + "'cidade':'santos'}";

		CcpJsonRepresentation thirdJson = CcpOtherConstants.EMPTY_JSON.put(name, "Diego").put(cidade, "Santos")
				.duplicateValueFromField(cidade, ciudad, city, town).addToItem(gato, name, "nina")
				.addToItem(gato, age, 21).addToItem(cão, name, "sheik").addToItem(cão, age, 10);

		Map<String, Object> emptyMap = new HashMap<String, Object>();

		List<Object> asvalidJsonList = Arrays.asList(firstJsonAsString, secondJsonAsString, thirdJson, emptyMap);
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(asvalidJsonList);
		boolean validJsonList = decorator.isJsonList();
		assertTrue(validJsonList);
	}

	@Test
	public void isNotJsonListValidTest() {
		List<Object> array = Arrays.asList(
				"{\r\n" + "  \"cidade\": \"Santos\",\r\n" + "  \"city\": \"Santos\",\r\n"
						+ "  \"ciudad\": \"Santos\",\r\n" + "  \"cão\": {\r\n" + "    \"nome\": \"sheik\",\r\n"
						+ "    \"idade\": 10\r\n" + "  },\r\n" + "  \"gato\": {\r\n" + "    \"nome\": \"nina\",\r\n"
						+ "    \"idade\": 21\r\n" + "  },\r\n" + "  \"nome\": \"Diego\",\r\n"
						+ "  \"town\": \"Santos\"\r\n" + "}",
				CcpOtherConstants.EMPTY_JSON, new HashMap<String, Object>(), "{}", "{'name':'Diego}");
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(array);
		boolean validJsonList = decorator.isJsonList();
		assertFalse(validJsonList);
	}

	@Test
	public void isEmptyTest() {
		List<Object> asEmptyList = Arrays.asList();// empty
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(asEmptyList);
		assertTrue(decorator.isEmpty());
	}

	@Test
	public void isNotEmptyTest() {
		List<Object> noEmptyList = Arrays.asList("item", true, 89d);// 3 items
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(noEmptyList);
		assertFalse(decorator.isEmpty());
	}

	@Test
	public void sizeTest() {
		{
			Collection<Object> content = new HashSet<>();
			CcpNumberDecorator size = new CcpCollectionDecorator(content).size();
			boolean assertion = size.equalsTo(0d);
			assertTrue(assertion);
		}
		{
			Collection<Object> content = Arrays.asList("1", 1.5d, "1.7f", 2, false);
			CcpNumberDecorator size = new CcpCollectionDecorator(content).size();
			boolean assertion = size.greaterThan(0d);
			assertTrue(assertion);
		}
		{
			Collection<Object> content = Arrays.asList("onias", false, new Object(), JnEntityAsyncTask.ENTITY);
			CcpNumberDecorator size = new CcpCollectionDecorator(content).size();
			boolean assertion = size.greaterThan(0d);
			assertTrue(assertion);

		}
	}

	@Test
	public void isNoContentSizeTest() {
		List<Object> sizeList = Arrays.asList();// empty
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(sizeList);
		System.out.println("size:" + decorator.content.size());
		assertFalse(decorator.content.size() != 0);

		List<Object> threeItemsList = Arrays.asList("item", true, 89d);// 3 items
		CcpCollectionDecorator threeItemsDecorator = new CcpCollectionDecorator(threeItemsList);
		System.out.println("size:" + threeItemsDecorator.content.size());
		assertFalse(threeItemsDecorator.content.size() != 3);
	}

	@Test
	public void hasNonDuplicatedItemsTest() {
		List<Object> items = Arrays.asList(10, true, false);// 3 distinct items
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(items);
		boolean hasNonDuplicatedItems = decorator.hasNonDuplicatedItems();
		assertTrue(hasNonDuplicatedItems); // must return true
	}

	@Test
	public void hasDuplicatedItemsTest() {
		List<Object> items = Arrays.asList(3f, 3f, "Hello", "Hello");// 4 items (duplicates)
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(items);
		boolean hasNonDuplicatedItems = decorator.hasNonDuplicatedItems();
		assertFalse(hasNonDuplicatedItems); // must return false
	}

	@Test
	public void getContentTest() {
		List<Object> values = Arrays.asList(3f, "Content");
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(values);
		System.out.println("getContent:" + decorator.getContent());
		assertTrue(decorator.getContent() == values);
	}

	@Test
	public void getNotContentTest() {
		List<Object> values = Arrays.asList(3f, "Content");
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(values);
		System.out.println("getContent:" + decorator.getContent());
		assertFalse(decorator.getContent() != values);
	}

	@Test
	public void getExclusiveListTest() {

		CcpCollectionDecorator decorator = new CcpCollectionDecorator(new Object[] { "a", "b", "c", "d", "e", "f" });
		List<String> externalList = Arrays.asList("b", "c", "d");
		{

			List<String> exclusiveList = decorator.getExclusiveList(externalList);
			System.out.println(exclusiveList);
			boolean containsAll = exclusiveList.containsAll(Arrays.asList("a", "e", "f"));
			assertTrue(containsAll);
		}
		{
			List<String> intersectList = decorator.getIntersectList(externalList);
			boolean containsAll = intersectList.containsAll(Arrays.asList("b", "c", "d"));
			assertTrue(containsAll);

		}
	}

	@Test
	public void iteratorTest() {
		List<Object> items = Arrays.asList("item1", "item2", "item3");
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(items);
		Iterator<Object> iterator = decorator.iterator();

		assertNotNull(iterator); // , "The iterator must not be null");

		assertTrue(iterator.hasNext());// , "The iterator must have elements");
		assertEquals("item1", iterator.next()); // , "The first element must be 'item1'");
		assertEquals("item2", iterator.next()); // , "The second element must be 'item2'");
		assertEquals("item3", iterator.next()); // , "The third element must be 'item3'");
		assertFalse(iterator.hasNext()); // , "The iterator must not have any more elements");
	}

	@Test
	public void hasIntersectTest() {
		List<Object> list = Arrays.asList(1, 2, 3, 4, 5);
		Object[] array = new Integer[] { 3, 4 };
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(array);
		boolean hasIntersection = decorator.hasIntersect(list);
		assertTrue(hasIntersection);
	}

	@Test
	public void hasNoIntersectTest() {
		List<Object> list = Arrays.asList(1, 2, 3, 4, 5);
		Object[] array = new Integer[] { 0, 6, 10 };
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(array);
		boolean hasIntersection = decorator.hasIntersect(list);
		assertFalse(hasIntersection);
	}

	@Test
	public void getSubCollectionTest() {
		List<Object> content = Arrays.asList("A", "B", "C", "D", "E");
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(content);
		CcpCollectionDecorator subCollection = decorator.getSubCollection(1, 3);
		List<Object> expected = Arrays.asList("B", "C");
		System.out.println("getContent:" + decorator.getContent());
		System.out.println("subCollection:" + subCollection.getContent());
		System.out.println("expected:" + expected);
		assertTrue(expected.equals(subCollection.getContent()));

		// if(end > this.content.size()) {
		// end = this.content.size();
		CcpCollectionDecorator subCollectionBeyondEnd = decorator.getSubCollection(1, 6);// index beyond the end of the array
		System.out.println("end:" + subCollectionBeyondEnd.content.size());
		assertTrue(subCollectionBeyondEnd.content.size() == 4);
	}

	@Test
	public void constructorTest() {
		CcpJsonRepresentation emptyJson = new CcpJsonRepresentation(); // empty json {}//
		Collection<Object> jsonBodyList = Arrays.asList("Brazil", "Uruguai", "Chile");
		CcpJsonRepresentation jsonWithLocation = CcpOtherConstants.EMPTY_JSON.put(pais, jsonBodyList).put(estado, "Sao Paulo")
				.put(cidade, "Santos");
		System.out.println("emptyJson:   " + emptyJson);
		System.out.println("jsonContent: " + jsonWithLocation);
		CcpCollectionDecorator missingKeyDecorator = new CcpCollectionDecorator(emptyJson, "falseKey"); //
		CcpCollectionDecorator countriesDecorator = new CcpCollectionDecorator(jsonWithLocation, "pais"); //
		CcpCollectionDecorator stateDecorator = new CcpCollectionDecorator(jsonWithLocation, "estado"); //
		CcpCollectionDecorator cityDecorator = new CcpCollectionDecorator(jsonWithLocation, "cidade"); //
		System.out.println("json0 content:" + missingKeyDecorator.content);
		System.out.println("json1 content:" + countriesDecorator.content);
		System.out.println("json1 content:" + stateDecorator.content);
		System.out.println("json1 content:" + cityDecorator.content);

		List<Object> emptyList = new ArrayList<>();
		System.out.println("empty " + emptyList + " " + missingKeyDecorator.getContent());
		assertEquals(emptyList, missingKeyDecorator.getContent()); // EMPTY_JSON
		assertEquals(countriesDecorator.getContent(), jsonBodyList);// ("pais" ,"Brazil", "Uruguai", "Chile")
		assertTrue(stateDecorator.getContent().contains(jsonWithLocation.get(estado))); // ("estado","Sao Paulo")
		assertTrue(cityDecorator.getContent().contains(jsonWithLocation.get(cidade))); // ("cidade","Santos")
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void constructorCollectionNullParamTest() {
		new CcpCollectionDecorator((Collection<?>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void constructorArrayNullParamTest() {
		new CcpCollectionDecorator((Object[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void constructorJsonNullParamJsonTest() {
		new CcpCollectionDecorator((CcpJsonRepresentation) null, "k");
	}

	@Test(expected = CcpNullParameterException.class)
	public void constructorJsonNullParamKeyTest() {
		new CcpCollectionDecorator(CcpOtherConstants.EMPTY_JSON, (String) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getExclusiveListNullParamTest() {
		new CcpCollectionDecorator(Arrays.asList(1, 2)).getExclusiveList(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getIntersectListNullParamTest() {
		new CcpCollectionDecorator(Arrays.asList(1, 2)).getIntersectList(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void hasIntersectNullParamTest() {
		new CcpCollectionDecorator(Arrays.asList(1, 2)).hasIntersect(null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────

	private static CcpCollectionDecorator withNullContent() throws Exception {
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(Arrays.asList(1));
		Field contentField = CcpCollectionDecorator.class.getDeclaredField("content");
		contentField.setAccessible(true);
		contentField.set(decorator, null);
		return decorator;
	}

	@Test(expected = CcpNullReturnException.class)
	public void getContentNullReturnTest() throws Exception {
		withNullContent().getContent();
	}
}
