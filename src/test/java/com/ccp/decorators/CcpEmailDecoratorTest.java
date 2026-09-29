package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.aop.CcpNullReturnException;

//it is instantiated as follows: 
//	CcpEmailDecorator decorator = new CcpStringDecorator("onias85@gmail.com").email();

public class CcpEmailDecoratorTest {
	
	@Test
	public void constructorTest() {
		String email = "onias85@gmail.com";
		CcpEmailDecorator decorator = new CcpStringDecorator(email).email();
		assertEquals(decorator.content, email);	
	} 
	
	@Test
	public void toStringTest() {
		String text =  "String";
		CcpEmailDecorator decorator = new CcpStringDecorator(text).email();
		assertEquals(decorator.toString(),text);
	}

	@Test
	public void stripAccentsTest(){
		// outside the IF
		String emailAccent   = "áãéíóú@áãéíóú"; //No Valid
		String emailNoAccent = "aaeiou@aaeiou"; //expected return
		CcpEmailDecorator decorator = new CcpStringDecorator(emailAccent).email();
		System.out.println(decorator.stripAccents());
		System.out.println(emailNoAccent);
		assertEquals(decorator.stripAccents().toString(),emailNoAccent);
		
		//inside the IF of isValid()
		String emailWithManyAtSigns  = "fulano@ciclano@beltrano"; //split.length != 2
		String emailWithoutLocalPart  = "@gmail.com";              //(split[0].trim().isEmpty())
		String digitalDomainEmail  = "email@something.digital"; //endsWith(".digital")
		String wayonGlobalEmail  = "email@wayon.global";      //endsWith("@wayon.global"))
		String corpInovationEmail  = "email@corp.inovation.com.br";//endsWith("@corp.inovation.com.br")
		String docxDomainEmail  = "email@something.docx";        //endsWith(".docx")
		String digiDomainEmail  = "email@something.digi";        //endsWith(".digi")) 
		String onliDomainEmail  = "email@something.onli";        //endsWith(".onli"))
		String globDomainEmail  = "email@something.glob";        //endsWith(".glob"))
		String sociDomainEmail = "email@something.soci";        //endsWith(".soci"))
		String brenDomainEmail = "email@something.bren";        //endsWith(".bren")) 
		String coomDomainEmail = "email@something.coom";        //endsWith(".coom"))

		CcpEmailDecorator manyAtSignsDecorator = new CcpStringDecorator(emailWithManyAtSigns).email();
		CcpEmailDecorator withoutLocalPartDecorator = new CcpStringDecorator(emailWithoutLocalPart).email();
		CcpEmailDecorator digitalDomainDecorator = new CcpStringDecorator(digitalDomainEmail).email();
		CcpEmailDecorator wayonGlobalDecorator = new CcpStringDecorator(wayonGlobalEmail).email();
		CcpEmailDecorator corpInovationDecorator = new CcpStringDecorator(corpInovationEmail).email();
		CcpEmailDecorator docxDomainDecorator = new CcpStringDecorator(docxDomainEmail).email();
		CcpEmailDecorator digiDomainDecorator = new CcpStringDecorator(digiDomainEmail).email();
		CcpEmailDecorator onliDomainDecorator = new CcpStringDecorator(onliDomainEmail).email();
		CcpEmailDecorator globDomainDecorator = new CcpStringDecorator(globDomainEmail).email();
		CcpEmailDecorator sociDomainDecorator = new CcpStringDecorator(sociDomainEmail).email();
		CcpEmailDecorator brenDomainDecorator = new CcpStringDecorator(brenDomainEmail).email();
		CcpEmailDecorator coomDomainDecorator = new CcpStringDecorator(coomDomainEmail).email();
	 	
		System.out.println(manyAtSignsDecorator.stripAccents()); //false
		System.out.println(withoutLocalPartDecorator.stripAccents()); //false
		System.out.println(digitalDomainDecorator.stripAccents()); //true
		System.out.println(wayonGlobalDecorator.stripAccents()); //true
		System.out.println(corpInovationDecorator.stripAccents()); //true
		System.out.println(docxDomainDecorator.stripAccents()); //false
		System.out.println(digiDomainDecorator.stripAccents()); //false
		System.out.println(onliDomainDecorator.stripAccents()); //false
		System.out.println(globDomainDecorator.stripAccents()); //false
		System.out.println(sociDomainDecorator.stripAccents()); //false
		System.out.println(brenDomainDecorator.stripAccents()); //false
		System.out.println(coomDomainDecorator.stripAccents()); //false
		
		// isValid() 
		assertFalse(manyAtSignsDecorator.stripAccents().isValid()); //false
		assertFalse(withoutLocalPartDecorator.stripAccents().isValid()); //false
		assertTrue(digitalDomainDecorator.stripAccents().isValid()); //true
		assertTrue(wayonGlobalDecorator.stripAccents().isValid()); //true
		assertTrue(corpInovationDecorator.stripAccents().isValid()); //true
		assertFalse(docxDomainDecorator.stripAccents().isValid()); //false
		assertFalse(digiDomainDecorator.stripAccents().isValid()); //false
		assertFalse(onliDomainDecorator.stripAccents().isValid()); //false
		assertFalse(globDomainDecorator.stripAccents().isValid()); //false
		assertFalse(sociDomainDecorator.stripAccents().isValid()); //false
		assertFalse(brenDomainDecorator.stripAccents().isValid()); //false
		assertFalse(coomDomainDecorator.stripAccents().isValid()); //false
	};
	
	@Test
	public void getDomainTest(){
		String emailWithoutLocalPart = "@myDomain1";          //expected "","myDomain" == 2
		String emailWithLocalPart = "something@myDomain2"; //expected "something","myDomain" == 2
		String emailWithoutAtSign = "emailWithoutAtSign";  //expected ""
		String emailWithManyAtSigns = "email@com@br"; //expected [email, com, br] -> ""

		CcpEmailDecorator withoutLocalPartDecorator = new CcpStringDecorator(emailWithoutLocalPart).email();
		CcpEmailDecorator withLocalPartDecorator = new CcpStringDecorator(emailWithLocalPart).email();
		CcpEmailDecorator withoutAtSignDecorator = new CcpStringDecorator(emailWithoutAtSign).email();
		CcpEmailDecorator manyAtSignsDecorator = new CcpStringDecorator(emailWithManyAtSigns).email();
		
		
		System.out.println(withoutLocalPartDecorator.getDomain());
		System.out.println(withLocalPartDecorator.getDomain());
		System.out.println(withoutAtSignDecorator.getDomain()); // ""
		System.out.println(manyAtSignsDecorator.getDomain()); // ""
		
		assertEquals(withoutLocalPartDecorator.getDomain(),"myDomain1");
		assertEquals(withLocalPartDecorator.getDomain(),"myDomain2");
		assertEquals(withoutAtSignDecorator.getDomain(), ""); // ""
		assertEquals(manyAtSignsDecorator.getDomain(), ""); // ""
		
	}	
	
	@Test
	public void getContentTest() {
		String content = "email@com.br"; 
		CcpEmailDecorator decorator = new CcpStringDecorator(content).email();	
		System.out.println(decorator.getContent());
		assertEquals(decorator.getContent(),content);
	}
	
	@Test
	public void CcpHashDecoratorTest() {
		String hashContent = "email@com.br"; 
		CcpEmailDecorator decorator = new CcpStringDecorator(hashContent).email();	
		System.out.println(decorator.hash());
		
		assertEquals(decorator.hash().toString(),hashContent);	
	}
	
	
	@Test
	public void extractFromTextTest() {
		String delimiter   = ";";
		String emailInText = "fulano@gmail.com; ciclano@gmail.com; beltrano@gmail.com";
		CcpEmailDecorator decorator = new CcpStringDecorator(emailInText).email();
		Set<String> emails = decorator.extractFromText(delimiter);
		assertEquals(3, emails.size());
		assertTrue(emails.contains("fulano@gmail.com"));
		assertTrue(emails.contains("ciclano@gmail.com"));
		assertTrue(emails.contains("beltrano@gmail.com"));
	}

	@Test
	public void extractFromTextWithoutValidEmailsTest() {
		String delimiter = ";";
		String text = "not-an-email; nor this; invalid";
		Set<String> emails = new CcpStringDecorator(text).email().extractFromText(delimiter);
		assertTrue(emails.isEmpty());
	}

    @Test
    public void findFirstEmailWithPlusTest() {
    	CcpEmailDecorator processor = new CcpEmailDecorator("nome+teste@example.com");
        CcpEmailDecorator result = processor.findFirst("\\s+");
        assertEquals("teste@example.com", result.content);
    }

    @Test
    public void findFirstEmailSimpleTest() {
    	CcpEmailDecorator processor = new CcpEmailDecorator("user@example.com outro@fake.com");
        CcpEmailDecorator result = processor.findFirst("\\s+");
        assertEquals("user@example.com", result.content);
    }
 
    @Test
    public void findFirstEmailWithDotTest() {
    	CcpEmailDecorator processor = new CcpEmailDecorator("final@example.com.");
        CcpEmailDecorator result = processor.findFirst("\\s+");
        assertEquals("final@example.com", result.content);
    }

    @Test
    public void findFirstInvalidEmailTest() {
    	CcpEmailDecorator processor = new CcpEmailDecorator("InvalidEmail");
        CcpEmailDecorator result = processor.findFirst("\\s+");
        assertEquals("", result.content);
    }
 
    @Test
    public void findFirstEmailWithAccentsTest() {
    	CcpEmailDecorator processor = new CcpEmailDecorator("téstê@exemplo.com");
        CcpEmailDecorator result = processor.findFirst("\\s+");
        assertEquals("teste@exemplo.com", result.content); // assuming stripAccents()
    }
    
    @Test
    public void isValidTest() {
    	boolean comeDomainIsValid = new CcpStringDecorator("onias85@gmail.come").email().isValid();
		assertFalse(comeDomainIsValid);
    	boolean brxDomainIsValid = new CcpStringDecorator("onias85@gmail.com.brx").email().isValid();
		assertFalse(brxDomainIsValid);

		boolean comBrDomainIsValid = new CcpStringDecorator("onias85@gmail.com.br").email().isValid();
		assertTrue(comBrDomainIsValid);

    }

	// ── null-parameter tests (AOP) ────────────────────────────────────────────
	// Note: the CcpEmailDecorator constructor is protected, so it is out of scope (public only).

	@Test(expected = CcpNullParameterException.class)
	public void findFirstNullParamTest() {
		new CcpEmailDecorator("x@y.com").findFirst(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void extractFromTextNullParamTest() {
		new CcpEmailDecorator("x@y.com").extractFromText(null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────

	@Test(expected = CcpNullReturnException.class)
	public void getContentNullReturnTest() throws Exception {
		CcpEmailDecorator decorator = new CcpEmailDecorator("x@y.com");
		Field contentField = CcpEmailDecorator.class.getDeclaredField("content");
		contentField.setAccessible(true);
		contentField.set(decorator, null);
		decorator.getContent();
	}

	@Test(expected = CcpNullReturnException.class)
	public void toStringNullReturnTest() throws Exception {
		CcpEmailDecorator decorator = new CcpEmailDecorator("x@y.com");
		Field contentField = CcpEmailDecorator.class.getDeclaredField("content");
		contentField.setAccessible(true);
		contentField.set(decorator, null);
		decorator.toString();
	}

}
