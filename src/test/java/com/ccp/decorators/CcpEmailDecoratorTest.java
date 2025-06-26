package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

//ela se instancia da seguinte forma: 
//	CcpEmailDecorator decorator = new CcpStringDecorator("onias85@gmail.com").email();

public class CcpEmailDecoratorTest {
	
	@Test
	public void constructorTest() {
		String strEmail = "onias85@gmail.com";
		CcpEmailDecorator decorator = new CcpStringDecorator(strEmail).email();
		assertEquals(decorator.content, strEmail);	
	} 
	
	@Test
	public void toStringTest() {
		String str =  "String";
		CcpEmailDecorator decorator = new CcpStringDecorator(str).email();
		assertEquals(decorator.toString(),str);
	}

	@Test
	public void stripAccentsTest(){
		// fora do IF
		String emailAccent   = "áãéíóú@áãéíóú"; //No Valid
		String emailNoAccent = "aaeiou@aaeiou"; //expected return
		CcpEmailDecorator decorator = new CcpStringDecorator(emailAccent).email();
		System.out.println(decorator.stripAccents());
		System.out.println(emailNoAccent);
		assertEquals(decorator.stripAccents().toString(),emailNoAccent);
		
		//Dentro do IF isValid()
		String emailNoValid1  = "fulano@ciclano@beltrano"; //split.length != 2
		String emailNoValid2  = "@gmail.com";              //(split[0].trim().isEmpty())
		String emailNoValid3  = "email@something.digital"; //endsWith(".digital")
		String emailNoValid4  = "email@wayon.global";      //endsWith("@wayon.global"))
		String emailNoValid5  = "email@corp.inovation.com.br";//endsWith("@corp.inovation.com.br")
		String emailNoValid6  = "email@something.docx";        //endsWith(".docx")
		String emailNoValid7  = "email@something.digi";        //endsWith(".digi")) 
		String emailNoValid8  = "email@something.onli";        //endsWith(".onli"))
		String emailNoValid9  = "email@something.glob";        //endsWith(".glob"))
		String emailNoValid10 = "email@something.soci";        //endsWith(".soci"))
		String emailNoValid11 = "email@something.bren";        //endsWith(".bren")) 
		String emailNoValid12 = "email@something.coom";        //endsWith(".coom"))

		CcpEmailDecorator decorator1 = new CcpStringDecorator(emailNoValid1).email();
		CcpEmailDecorator decorator2 = new CcpStringDecorator(emailNoValid2).email();
		CcpEmailDecorator decorator3 = new CcpStringDecorator(emailNoValid3).email();
		CcpEmailDecorator decorator4 = new CcpStringDecorator(emailNoValid4).email();
		CcpEmailDecorator decorator5 = new CcpStringDecorator(emailNoValid5).email();
		CcpEmailDecorator decorator6 = new CcpStringDecorator(emailNoValid6).email();
		CcpEmailDecorator decorator7 = new CcpStringDecorator(emailNoValid7).email();
		CcpEmailDecorator decorator8 = new CcpStringDecorator(emailNoValid8).email();
		CcpEmailDecorator decorator9 = new CcpStringDecorator(emailNoValid9).email();
		CcpEmailDecorator decorator10 = new CcpStringDecorator(emailNoValid10).email();
		CcpEmailDecorator decorator11 = new CcpStringDecorator(emailNoValid11).email();
		CcpEmailDecorator decorator12 = new CcpStringDecorator(emailNoValid12).email();
	 	
		System.out.println(decorator1.stripAccents()); //false
		System.out.println(decorator2.stripAccents()); //false
		System.out.println(decorator3.stripAccents()); //true
		System.out.println(decorator4.stripAccents()); //true
		System.out.println(decorator5.stripAccents()); //true
		System.out.println(decorator6.stripAccents()); //false
		System.out.println(decorator7.stripAccents()); //false
		System.out.println(decorator8.stripAccents()); //false
		System.out.println(decorator9.stripAccents()); //false
		System.out.println(decorator10.stripAccents()); //false
		System.out.println(decorator11.stripAccents()); //false
		System.out.println(decorator12.stripAccents()); //false
		
		// isValid() 
		assertFalse(decorator1.stripAccents().isValid()); //false
		assertFalse(decorator2.stripAccents().isValid()); //false
		assertTrue(decorator3.stripAccents().isValid()); //true
		assertTrue(decorator4.stripAccents().isValid()); //true
		assertTrue(decorator5.stripAccents().isValid()); //true
		assertFalse(decorator6.stripAccents().isValid()); //false
		assertFalse(decorator7.stripAccents().isValid()); //false
		assertFalse(decorator8.stripAccents().isValid()); //false
		assertFalse(decorator9.stripAccents().isValid()); //false
		assertFalse(decorator10.stripAccents().isValid()); //false
		assertFalse(decorator11.stripAccents().isValid()); //false
		assertFalse(decorator12.stripAccents().isValid()); //false
	};
	
	@Test
	public void getDomainTest(){
		String emailDomain1 = "@myDomain1";          //expected "","myDomain" == 2
		String emailDomain2 = "something@myDomain2"; //expected "something","myDomain" == 2
		String emailDomain3 = "emailSemArroba";      //expected ""
		String emailDomain4 = "email@com@br"; //expected [email, com, br] -> ""

		CcpEmailDecorator decorator1 = new CcpStringDecorator(emailDomain1).email();
		CcpEmailDecorator decorator2 = new CcpStringDecorator(emailDomain2).email();
		CcpEmailDecorator decorator3 = new CcpStringDecorator(emailDomain3).email();
		CcpEmailDecorator decorator4 = new CcpStringDecorator(emailDomain4).email();
		
		
		System.out.println(decorator1.getDomain());
		System.out.println(decorator2.getDomain());
		System.out.println(decorator3.getDomain()); // ""
		System.out.println(decorator4.getDomain()); // ""
		
		assertEquals(decorator1.getDomain(),"myDomain1");
		assertEquals(decorator2.getDomain(),"myDomain2");
		assertEquals(decorator3.getDomain(), ""); // ""
		assertEquals(decorator4.getDomain(), ""); // ""
		
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
		System.out.println(decorator.extractFromText(delimiter));
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
        assertEquals("teste@exemplo.com", result.content); // assumindo stripAccents()
    }
    
    @Test
    public void isValidTest() {
    	boolean valid = new CcpStringDecorator("onias85@gmail.come").email().isValid();
		assertFalse(valid);
    	boolean valid2 = new CcpStringDecorator("onias85@gmail.com.brx").email().isValid();
		assertFalse(valid2);

		boolean valid3 = new CcpStringDecorator("onias85@gmail.com.br").email().isValid();
		assertTrue(valid3);
    	
    }

}
