package com.ccp.decorators;

import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpCollectionDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpNumberDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityAsyncTask;

public class CcpCollectionDecoratorTest {
	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());	
	}

	@Test
	public void isLongNumberListTest() {
		List<Integer> asList             = Arrays.asList(1,2,3);
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(asList);
		boolean longNumberList           = decorator.isLongNumberList();
		assertTrue(longNumberList);
	}
 	
	@Test
	public void isNotLongNumberListTest() {
		Object[] array                   = new Object[]{1,2,3,5.7};
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(array);
		boolean longNumberList           = decorator.isLongNumberList();
		assertFalse(longNumberList);

}
	@Test
	public void isDoubleNumberListTest() {
		List<Double> asDoubleList = Arrays.asList(-Double.MIN_VALUE, -Double.MAX_VALUE, 
				                                   Double.MIN_VALUE,  Double.MAX_VALUE);
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(asDoubleList);		
		boolean validDoubleList = decorator.isDoubleNumberList(); 
		assertTrue(validDoubleList);
	}
	
	@Test
	public void isNotDoubleNumberListTest() {
		//Object[] asDoubleInt = new Object[]{String.class, "String", false, true, new Object() };
		List <Object> array              = Arrays.asList(String.class, "String", false, true, new Object());
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(array);		
		boolean validDoubleList          = decorator.isDoubleNumberList(); 
		assertFalse(validDoubleList);
	}
	
	@Test
	public void isBooleanListTest() {
		List<Boolean> asBooleanList      = Arrays.asList(true, false, 1==1, (true && false));
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(asBooleanList);
		boolean validBooleanList         = decorator.isBooleanList();
		assertTrue(validBooleanList);
	}
	
	@Test
	public void isNotBooleanListTest() {
		List<Object> array = Arrays.asList(
			    'A',           // Char
			    42,            // Integer
			    3.14,          // Double
			    "Texto",       // String
			    new ArrayList<>(), // List vazia
			    new HashMap<>(),   // Map vazio
			    new Object()  // Instância genérica de Object
			);

		CcpCollectionDecorator decorator = new CcpCollectionDecorator(array);
		boolean validBooleanList         = decorator.isBooleanList();
		assertFalse(validBooleanList);
	}
 
    @Test
    public void isJsonListValidTest() {
		String registro = "{'nome':'Alice',"
				          + "'sobrenome':'almeida',"
				          + "'idade':32}";
		
		String registro2 = "{'país':'Brasil',"
		                  + "'estado':'são paulo',"
		                  + "'cidade':'santos'}";
		
		CcpJsonRepresentation registro3 = CcpOtherConstants.EMPTY_JSON
				.put("nome", "Diego")
				.put("cidade", "Santos")
				.duplicateValueFromField("cidade", "ciudad", "city", "town")
				.addToItem("gato", "nome", "nina")
				.addToItem("gato", "idade", 21)
				.addToItem("cão", "nome", "sheik")
				.addToItem("cão", "idade", 10)
				;
	    
		Map<String, Object> registro4 = new HashMap<String, Object>();
		
        List<Object> asvalidJsonList     = Arrays.asList(registro,registro2,registro3,registro4);	
        CcpCollectionDecorator decorator = new CcpCollectionDecorator(asvalidJsonList);
		boolean validJsonList            = decorator.isJsonList();
		assertTrue(validJsonList);
	    }
    
    @Test
    public void isNotJsonListValidTest() {
        List<Object> array = Arrays.asList("{\r\n"
        		+ "  \"cidade\": \"Santos\",\r\n"
        		+ "  \"city\": \"Santos\",\r\n"
        		+ "  \"ciudad\": \"Santos\",\r\n"
        		+ "  \"cão\": {\r\n"
        		+ "    \"nome\": \"sheik\",\r\n"
        		+ "    \"idade\": 10\r\n"
        		+ "  },\r\n"
        		+ "  \"gato\": {\r\n"
        		+ "    \"nome\": \"nina\",\r\n"
        		+ "    \"idade\": 21\r\n"
        		+ "  },\r\n"
        		+ "  \"nome\": \"Diego\",\r\n"
        		+ "  \"town\": \"Santos\"\r\n"
        		+ "}",CcpOtherConstants.EMPTY_JSON,new HashMap<String, Object>(),"{}","{'nome':'Diego}");
        CcpCollectionDecorator decorator = new CcpCollectionDecorator(array);
		boolean validJsonList            = decorator.isJsonList();
		assertFalse(validJsonList);
	    }
    
    //isJasonValidList -> isValidList
	
    // Sáb dia 31 
	/*
	 * public Iterator<Object> iterator() { return this.content.iterator(); }
	 */
	
	/*
	 * @Test
	 * 
	 * public void iteratorTest() { List<Object> array = Arrays.asList(1,2,3);
	 * CcpCollectionDecorator decorator = new CcpCollectionDecorator(array); Object
	 * s1 = array; Object s2 = decorator.iterator(); System.out.println("size1:"+
	 * s1); System.out.println("size2:"+ s2); assertTrue(s1 == s2);
	 * 
	 * }
	 */
	 	 
	 @Test
	 public void isEmptyTest() {
		List<Object> asEmptyList = Arrays.asList();//vazia
        CcpCollectionDecorator decorator = new CcpCollectionDecorator(asEmptyList);  
        assertTrue(decorator.isEmpty());
	    }
	 
	 @Test
	 public void isNotEmptyTest() {
		List<Object> noEmptyList         = Arrays.asList("item", true, 89d);//3 itens
	    CcpCollectionDecorator decorator = new CcpCollectionDecorator(noEmptyList);
	    assertFalse(decorator.isEmpty());	 
	    }
	  
	@Test 
	public void sizeTest() {
		{
			Collection<Object> content = new HashSet<>();
			CcpNumberDecorator size    = new CcpCollectionDecorator(content).size();
			boolean assertion = size.equalsTo(0d);
			assertTrue(assertion);
		}
		{
			Collection<Object> content = Arrays.asList("1", 1.5d, "1.7f", 2, false);
			CcpNumberDecorator size    = new CcpCollectionDecorator(content).size();
			boolean assertion = size.greaterThan(0d);
			assertTrue(assertion);
		}
		{
			Collection<Object> content = Arrays.asList("onias", false, new Object(), JnEntityAsyncTask.ENTITY);
			CcpNumberDecorator size    = new CcpCollectionDecorator(content).size();
			boolean assertion = size.greaterThan(0d);
			assertTrue(assertion);
			
		}
		
//		List<Object> sizeList            = Arrays.asList();//vazia
//		CcpCollectionDecorator decorator = new CcpCollectionDecorator(sizeList);
//		System.out.println("size:"+ decorator.content.size() );
//        assertTrue(decorator.content.size() == 0);	
//        
//		List<Object> size3List            = Arrays.asList("item", true, 89d);//3 itens
//		CcpCollectionDecorator decorator3 = new CcpCollectionDecorator(size3List);
//		System.out.println("size:"+ decorator3.content.size() );
//        assertTrue(decorator3.content.size() == 3);	
	}
	
	@Test 
	public void isNoContentSizeTest() {
		List<Object> sizeList            = Arrays.asList();//vazia
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(sizeList);
		System.out.println("size:"+ decorator.content.size() );
        assertFalse(decorator.content.size() != 0);	
        
		List<Object> size3List            = Arrays.asList("item", true, 89d);//3 itens
		CcpCollectionDecorator decorator3 = new CcpCollectionDecorator(size3List);
		System.out.println("size:"+ decorator3.content.size() );
        assertFalse(decorator3.content.size() != 3);	
	}
	
	@Test
	public void hasNonDuplicatedItemsTest() {
		List<Object> items               = Arrays.asList(10,true,false);// 3 itens diferentes
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(items);
		int s1 = decorator.content.size();
		HashSet<Object> hashSet = new HashSet<Object>(items); // resolve duplicatas (se houver)
		int s2 = hashSet.size();
		System.out.println("size1:"+ s1);
		System.out.println("size2:"+ s2);
        assertTrue(s1 == s2);	
	}
	
	@Test
	public void hasDuplicatedItemsTest() {
		List<Object> items               = Arrays.asList(3f,3f, "Hello", "Hello");// 4 itens (duplicatas)
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(items);
		int s1 = decorator.content.size();
		HashSet<Object> hashSet = new HashSet<Object>(items); // deve retornar 2 itens
		int s2 = hashSet.size();
		System.out.println("size1:"+ s1);
		System.out.println("size2:"+ s2);
        assertFalse(s1 == s2);	
	}
	
	@Test
	public void getContentTest() {
		List<Object> values              = Arrays.asList(3f, "Content");
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(values);
		System.out.println("getContent:" + decorator.getContent());
		assertTrue(decorator.getContent() == values);
	}
	
	@Test
	public void getNotContentTest() {
		List<Object> values              = Arrays.asList(3f, "Content");
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(values);
		System.out.println("getContent:" + decorator.getContent());
		assertFalse(decorator.getContent() != values);
	}
	
	@Test
	public void getExclusiveListTest() {

		CcpCollectionDecorator decorator = new CcpCollectionDecorator(new Object[] {"a", "b", "c", "d", "e", "f"});
		List<String> externalList        = Arrays.asList("b", "c", "d");
		{
	
			List<String> exclusiveList = decorator.getExclusiveList(externalList);
			System.out.println(exclusiveList);
			boolean containsAll        = exclusiveList.containsAll(Arrays.asList("a", "e", "f"));
			assertTrue(containsAll);
		}
		{
			List<String> intersectList = decorator.getIntersectList(externalList);
			boolean      containsAll   = intersectList.containsAll(Arrays.asList("b", "c", "d"));
			assertTrue(containsAll);
			
		}
		
	}
		
}


