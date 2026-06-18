package com.ccp.decorators;

import static com.ccp.decorators.JsonFieldNames.Umtextoqualquer;
import static com.ccp.decorators.JsonFieldNames.andré;
import static com.ccp.decorators.JsonFieldNames.arrayJson;
import static com.ccp.decorators.JsonFieldNames.avo;
import static com.ccp.decorators.JsonFieldNames.bisnetos;
import static com.ccp.decorators.JsonFieldNames.camila;
import static com.ccp.decorators.JsonFieldNames.campo1;
import static com.ccp.decorators.JsonFieldNames.campo2;
import static com.ccp.decorators.JsonFieldNames.campo3;
import static com.ccp.decorators.JsonFieldNames.campo4;
import static com.ccp.decorators.JsonFieldNames.campo5;
import static com.ccp.decorators.JsonFieldNames.campo6;
import static com.ccp.decorators.JsonFieldNames.campo7;
import static com.ccp.decorators.JsonFieldNames.campo8;
import static com.ccp.decorators.JsonFieldNames.campo9;
import static com.ccp.decorators.JsonFieldNames.campoDeTeste;
import static com.ccp.decorators.JsonFieldNames.campoQueNaoExiste;
import static com.ccp.decorators.JsonFieldNames.carro;
import static com.ccp.decorators.JsonFieldNames.carro2;
import static com.ccp.decorators.JsonFieldNames.cor;
import static com.ccp.decorators.JsonFieldNames.cor2;
import static com.ccp.decorators.JsonFieldNames.cpf;
import static com.ccp.decorators.JsonFieldNames.endereco;
import static com.ccp.decorators.JsonFieldNames.f1;
import static com.ccp.decorators.JsonFieldNames.f2;
import static com.ccp.decorators.JsonFieldNames.f3;
import static com.ccp.decorators.JsonFieldNames.f4;
import static com.ccp.decorators.JsonFieldNames.field;
import static com.ccp.decorators.JsonFieldNames.filho;
import static com.ccp.decorators.JsonFieldNames.frutas;
import static com.ccp.decorators.JsonFieldNames.idade;
import static com.ccp.decorators.JsonFieldNames.jsn;
import static com.ccp.decorators.JsonFieldNames.json2;
import static com.ccp.decorators.JsonFieldNames.json3;
import static com.ccp.decorators.JsonFieldNames.json4;
import static com.ccp.decorators.JsonFieldNames.json5;
import static com.ccp.decorators.JsonFieldNames.juliana;
import static com.ccp.decorators.JsonFieldNames.luciellen;
import static com.ccp.decorators.JsonFieldNames.minhaPropriedadeJson;
import static com.ccp.decorators.JsonFieldNames.nacionalidade;
import static com.ccp.decorators.JsonFieldNames.netos;
import static com.ccp.decorators.JsonFieldNames.nome;
import static com.ccp.decorators.JsonFieldNames.nome2;
import static com.ccp.decorators.JsonFieldNames.nomeCopiado;
import static com.ccp.decorators.JsonFieldNames.nome_da_vó;
import static com.ccp.decorators.JsonFieldNames.nomes;
import static com.ccp.decorators.JsonFieldNames.nomes2;
import static com.ccp.decorators.JsonFieldNames.nomes3;
import static com.ccp.decorators.JsonFieldNames.nomes4;
import static com.ccp.decorators.JsonFieldNames.nomes5;
import static com.ccp.decorators.JsonFieldNames.nomes6;
import static com.ccp.decorators.JsonFieldNames.nomes7;
import static com.ccp.decorators.JsonFieldNames.onias;
import static com.ccp.decorators.JsonFieldNames.pai;
import static com.ccp.decorators.JsonFieldNames.paçoca;
import static com.ccp.decorators.JsonFieldNames.peso;
import static com.ccp.decorators.JsonFieldNames.peso2;
import static com.ccp.decorators.JsonFieldNames.teste;
import static com.ccp.decorators.JsonFieldNames.tipo_sanguineo;
import static com.ccp.decorators.JsonFieldNames.v1;
import static com.ccp.decorators.JsonFieldNames.v2;
import static com.ccp.decorators.JsonFieldNames.v3;
import static com.ccp.decorators.JsonFieldNames.v4;
import static com.ccp.decorators.JsonFieldNames.valor;
import static com.ccp.decorators.JsonFieldNames.valor2;
import static com.ccp.decorators.JsonFieldNames.valor3;
import static com.ccp.decorators.JsonFieldNames.valorDouble;
import static com.ccp.decorators.JsonFieldNames.valorQueNaoEhDouble;
import static com.ccp.decorators.JsonFieldNames.veiculo;
import static com.ccp.decorators.JsonFieldNames.veiculo2;
import static com.ccp.decorators.JsonFieldNames.welton;
import static com.ccp.decorators.JsonFieldNames.x;
import static com.ccp.decorators.JsonFieldNames.x1;
import static com.ccp.decorators.JsonFieldNames.x2;
import static com.ccp.decorators.JsonFieldNames.x3;
import static com.ccp.decorators.JsonFieldNames.x4;
import static com.ccp.decorators.JsonFieldNames.x5;
import static com.ccp.decorators.JsonFieldNames.x6;
import static com.ccp.decorators.JsonFieldNames.x7;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation.CCpErrorJsonFieldIsNotValidJsonList;
import com.ccp.decorators.CcpJsonRepresentation.CcpErrorJsonFieldNotFound;
import com.ccp.decorators.CcpJsonRepresentation.CcpErrorJsonPathIsMissing;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.json.CcpJsonHandler;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.process.CcpProcessStatusDefault;

public class CcpJsonRepresentationTests {
	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());	
	}

	@Test
	public void getAsLongNumberTest() {
		String valorLong = "{'valor': 1}";
		CcpJsonRepresentation json = new CcpJsonRepresentation(valorLong);
		Long asLongNumber = json.getAsLongNumber(valor);
		assertTrue(asLongNumber instanceof Long);
		json.getAsLongNumber(valor);
	}
	
	@Test
	public void containsAnyFields() {
		Collection<String> fields = Arrays.asList(CcpProcessStatusDefault.CONFLICT.name(), CcpProcessStatusDefault.BAD_REQUEST.name());
		assertTrue(CcpOtherConstants.EMPTY_JSON.put(CcpProcessStatusDefault.BAD_REQUEST).containsAnyFields(fields));
	}
	
	@Test
	public void redoJson() {
		assertTrue(CcpOtherConstants.EMPTY_JSON.redoJson(CcpOtherConstants.EMPTY_JSON) instanceof CcpJsonRepresentation);
	}

	@Test
	public void getAsEnum() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
		.put(CcpProcessStatusDefault.BAD_REQUEST, CcpProcessStatusDefault.BAD_REQUEST.name())
		.put(CcpProcessStatusDefault.CONFLICT, CcpProcessStatusDefault.CONFLICT)
		.put(CcpProcessStatusDefault.CREATED)
		;
		
		CcpProcessStatusDefault item1 = json.getAsEnum(CcpProcessStatusDefault.BAD_REQUEST, CcpProcessStatusDefault.class);
		CcpProcessStatusDefault item2 = json.getAsEnum(new CcpFieldName(CcpProcessStatusDefault.CONFLICT.name()), CcpProcessStatusDefault.class);
		CcpProcessStatusDefault item3 = json.getAsEnum(CcpProcessStatusDefault.CREATED, CcpProcessStatusDefault.class);
		assertEquals(item1, CcpProcessStatusDefault.BAD_REQUEST);
		assertEquals(item2, CcpProcessStatusDefault.CONFLICT);
		assertEquals(item3, CcpProcessStatusDefault.CREATED);
	}

	@Test(expected = RuntimeException.class)
	public void getAsEnumTrowsRuntimeException() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
		.put(nomes7)
		;
		
		json.getAsEnum(nomes7, CcpProcessStatusDefault.class);
	}

	@Test (expected = RuntimeException.class)
	public void jsonDeSyntaxIncorretaParaConstrutor() {
		//Jason iválido
		CcpJsonRepresentation objeto = new CcpJsonRepresentation("Testando getAsLongNumberTest()");

		assertTrue(objeto.getAsLongNumber(Umtextoqualquer) instanceof Long);
		assertTrue(objeto.getAsLongNumber(Umtextoqualquer) instanceof Object);
	}

	@Test (expected = RuntimeException.class)
	public void obterPropriedadeQueNaoExiste() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON;
		json.getAsLongNumber(minhaPropriedadeJson);
		//		CcpJsonRepresentation.getMap(null);
	}

	@Test
	public void getAsIntegerNumberTest() {
		String inteiro = "{'valor':1}";
		CcpJsonRepresentation json = new CcpJsonRepresentation(inteiro);
		assertTrue(json.getAsIntegerNumber(valor) == 1);
		assertTrue(json.getAsIntegerNumber(valor) == 1);
	}
	
	@Test
	public void teste() {
		CcpJsonRepresentation json = new CcpJsonRepresentation(CcpOtherConstants.EMPTY_JSON.put(nomes7, String.class).content);
		assertEquals(1, json.fieldSet().size());
	}

	@Test (expected = RuntimeException.class)
	public void getAsIntegerNumberErrorTest() {
		//		//Json Inválido
		//		String inteiro = "EuNãoSouUmJson";
		//		CcpJsonRepresentation json = new CcpJsonRepresentation(inteiro);
		//		assertTrue(json.getAsIntegerNumber("EuNãoSouUmJson") instanceof Integer);
		//		
		//		//Parâmetro não é um inteiro
		String x = "{'nome':'Fulano da Silva'}";
		CcpJsonRepresentation json2 = new CcpJsonRepresentation(x);
		assertTrue(json2.getAsIntegerNumber(nome) instanceof Integer);

		//Parâmetro estoura a capacidade de integer
		//		String explodeCoracao = "{'valor':"+Math.pow(2, 32)+"}";
		//		CcpJsonRepresentation json3 = new CcpJsonRepresentation(explodeCoracao);
		//		assertFalse(json3.getAsIntegerNumber("valor") instanceof Integer);
	}

	@Test
	public void getAsDoubleNumberTest() {
		String pontoFlutuante = "{'valorDouble':8.67}";
		CcpJsonRepresentation json = new CcpJsonRepresentation(pontoFlutuante);
		assertTrue(json.getAsDoubleNumber(valorDouble) instanceof Double);
		assertTrue(json.getAsDoubleNumber(valorDouble) instanceof Double);
	}

	@Test (expected = RuntimeException.class)
	public void getAsDoubleNumberErrorTest() {

		//JASON NÃO RETORNA DOUBLE
		String pontoFlutuante = "{'valorQueNaoEhDouble':'Qualquer coisa'}";
		CcpJsonRepresentation json = new CcpJsonRepresentation(pontoFlutuante);
		assertTrue(json.getAsDoubleNumber(valorQueNaoEhDouble) instanceof Double);

		//JSON INVÁLIDO
		String x = "valorQueNaoEhDouble':9.30";
		CcpJsonRepresentation json2 = new CcpJsonRepresentation(x);
		assert(json2.getAsDoubleNumber(new CcpFieldName(x)) instanceof Double);
	}

	@Test
	public void getAsBooleanTest() {
		String x = "{'valor': true}";
		CcpJsonRepresentation json = new CcpJsonRepresentation(x);
		assertTrue(json.getAsBoolean(valor));
		assertTrue(json.getAsBoolean(valor));
	}

	@Test
	public void getAsBooleanErrorTest() {
		String y = "{'valor': 'abacaxi'}";
		CcpJsonRepresentation json2 = new CcpJsonRepresentation(y);
		assertFalse(json2.getAsBoolean(valor));
	}

	@Test
	public void getAsTextDecoratorTest() {
		String x = "{'nome':'Lucas'}";

		CcpJsonRepresentation json = new CcpJsonRepresentation(x);
		System.out.println(json.getAsTextDecorator(nome));

		assertTrue(json.getAsTextDecorator(nome) instanceof CcpTextDecorator);
		assertTrue(json.getAsTextDecorator(nome) instanceof CcpTextDecorator);
	}

	@Test
	public void getAsStringTest() {

		int numero = 1;
		String x = "{'valor' : '" + numero + "'}";

		CcpJsonRepresentation json = new CcpJsonRepresentation(x)
				.put(nomes5, Arrays.asList(x))
				.put(nomes6, Arrays.asList(CcpOtherConstants.EMPTY_JSON))
				.put(nomes7, new ArrayList<>())
				.put(valor3, (Object) null)
				
				;

		assertTrue(json.get(valor) instanceof String);
		
		
		assertTrue(json.getAsString(valor2).isEmpty()); 
		assertFalse(json.getAsString(nomes5).isEmpty()); 
		assertEquals("[{}]", json.getAsString(nomes6)); 
		assertEquals("[]", json.getAsString(nomes7)); 
		assertTrue(json.getAsString(valor3).isEmpty());
		assertTrue(CcpOtherConstants.EMPTY_JSON.put(jsn, (Object)json).getAsTextDecorator(jsn).isValidSingleJson());
	}
	
	@Test
	public void getTransformedJsonTest() {
		String x = "{'valor' : '" + 1 + "'}";

		CcpJsonRepresentation json = new CcpJsonRepresentation(x);
		{
			CcpJsonRepresentation transformedJson = json.getTransformedJson(j -> j.put(valor2, j.getAsIntegerNumber(valor) * 2), j -> j.put(valor3, j.getAsIntegerNumber(valor) * 3));
			
			assertTrue(transformedJson.getAsIntegerNumber(valor3).equals(3));
			assertTrue(transformedJson.getAsIntegerNumber(valor2).equals(2));
			assertTrue(transformedJson.getAsIntegerNumber(valor).equals(1));
			
		}
		
		List<CcpBusiness> jsonTransformers = new ArrayList<>();
		jsonTransformers.add(j -> j.put(valor2, j.getAsIntegerNumber(valor) * 2));
		jsonTransformers.add(j -> j.put(valor3, j.getAsIntegerNumber(valor) * 3));
		
		CcpJsonRepresentation transformedJson = json.getTransformedJson(jsonTransformers);
		assertTrue(transformedJson.getAsIntegerNumber(valor3).equals(3));
		assertTrue(transformedJson.getAsIntegerNumber(valor2).equals(2));
		assertTrue(transformedJson.getAsIntegerNumber(valor).equals(1));
	}
	
	@Test
	public void extractInformationFromJsonTest() {
		Integer multiplicador = 2; 
		Integer numero = 37;
		String str = "{'valor' : '" + numero + "'}";

		CcpJsonRepresentation json = new CcpJsonRepresentation(str);
		
		Integer resultado = json.extractInformationFromJson( x -> x.getAsIntegerNumber(valor) * multiplicador);
		
		assertTrue(resultado == numero * multiplicador);
	}
	
	@Test
	public void getJsonPieceTest() {
		String json = "{'nome':'João', 'telefone': 1234}";
		CcpJsonRepresentation objJson = new CcpJsonRepresentation(json);
		assertEquals(objJson.getJsonPiece(Arrays.asList( "nome", "telefone")).fieldSet().size(), 2);
		assertEquals(objJson.getJsonPiece(nome).fieldSet().size(), 1);
		assertTrue(objJson.getJsonPiece(nome2).isEmpty());
		assertTrue(objJson.getJsonPiece(nome2).isEmpty());
	}

	@Test
	public void getOrDefaultTest() {
		String json = "{'veiculo':'carro'}";
		CcpJsonRepresentation objJson = new CcpJsonRepresentation(json);
		String resultado2 = objJson.getOrDefault(veiculo2, () -> "default");
		String resultado = objJson.getOrDefault(veiculo, () -> "default");

		assertEquals("default", resultado2);
		assertEquals("carro", resultado);
		String orDefault = objJson.getOrDefault(new CcpFieldName("1"), () -> "teste");
		assertTrue("teste".equals(orDefault));
	}


	@Test (expected = RuntimeException.class)
	public void getOrDefaultErroTest() {
		CcpJsonRepresentation objJson = new CcpJsonRepresentation("");
		String resultado = objJson.getOrDefault(veiculo, () -> "default");

		assertEquals("default", resultado);
	}

	//getgetAsUglyJsonTest -  observar caractere não convertido
	@Test
	public void getgetAsUglyJsonTest() {
		String filha = "{"
				+ "'nome':'Márcia',"
				+ "'idade':34"
				+ "}";

		String mae = "{"
				+ "'nome':'Maria',"
				+ "'idade': 58',"
				+ "'filha': "+filha
				+ "}";

		CcpJsonRepresentation json = new CcpJsonRepresentation(mae);
		System.out.println("Ugly json = "+ json.asUgglyJson());
	}

	//getAsPrettyJson - observar caractere não convertido
	@Test
	public void getgetAsPrettyJsonTest() {
		String filha = "{"
				+ "'nome':'Márcia',"
				+ "'idade':34"
				+ "}";

		String mae = "{"
				+ "'nome':'Maria',"
				+ "'idade': 58',"
				+ "'filha': "+filha
				+ "}";

		CcpJsonRepresentation json = new CcpJsonRepresentation(mae);
		System.out.println("Pretty Json = "+json.asPrettyJson());
	}

	@Test
	public void testarConstrutores() throws FileNotFoundException {
		FileInputStream jsonFile = new FileInputStream("newSynonyms.json");
		new CcpJsonRepresentation(jsonFile);
		FileInputStream propsFile = new FileInputStream("teste.properties");
		new CcpJsonRepresentation(propsFile);
		new CcpJsonRepresentation(new RuntimeException());
		new CcpJsonRepresentation((RuntimeException) null);
		new CcpJsonRepresentation(new RuntimeException(new RuntimeException()));
		try {
			this.getNumeroIncrementado(1); 
		} catch (Exception e) {
			new CcpJsonRepresentation(e);
		}
	}
	
	private int getNumeroIncrementado(int numero) {
		if(numero>=200) {
			throw new RuntimeException();
		}
		int numeroIncrementado = this.getNumeroIncrementado(numero + 1);
		return numeroIncrementado;
	}
	
	@Test
	public void toStringTest() {
		String filha = "{"
				+ "'nome':'Márcia',"
				+ "'idade':34"
				+ "}";

		String mae = "{"
				+ "'nome':'Maria',"
				+ "'idade': 58',"
				+ "'filha': "+filha
				+ "}";

		CcpJsonRepresentation json = new CcpJsonRepresentation(mae);
		System.out.println("toString = "+ json.toString());
		assertTrue(json.toString().equals(json.toString()));
	}

	@Test
	public void fieldSetTest() {
		String filha = "{"
				+ "'nome':'Márcia',"
				+ "'idade':34"
				+ "}";

		String mae = "{"
				+ "'nome':'Maria',"
				+ "'idade': 58',"
				+ "'filha': "+filha
				+ "}";

		CcpJsonRepresentation json = new CcpJsonRepresentation(mae);
		System.out.println("Retorno de fieldSetTest() = "+ json.fieldSet());
	}

	@Test
	public void putTest() {
		String valor = "{'frutas':''}";
		String fruits = "{'banana','laranja','maçã','tangerina','uva'}";
		CcpJsonRepresentation json = new CcpJsonRepresentation(valor);

		System.out.println("Retorno putTest()= " + json.put(frutas, fruits));
		System.out.println("Retorno putTest()= " + json.put(frutas, fruits));
		System.out.println("Retorno putTest()= " + json.put(frutas, CcpOtherConstants.EMPTY_JSON));
		System.out.println("Retorno putTest()= " + json.put(frutas, Arrays.asList(CcpOtherConstants.EMPTY_JSON)));

	}

	@Test
	public void putMultiTest() {

		CcpJsonRepresentation pessoa1 = new CcpJsonRepresentation("{'nome':'João','idade':49}");
		CcpJsonRepresentation pessoa2 = new CcpJsonRepresentation("{'nome':'Maria','idade':51}");
		CcpJsonRepresentation pessoa3 = new CcpJsonRepresentation("{'nome':'José','idade':11}");

		List<CcpJsonRepresentation> listaDePessoas = Arrays.asList(pessoa1, pessoa2, pessoa3);

		// Criação de um objeto JSON vazio para receber o array
		CcpJsonRepresentation json = new CcpJsonRepresentation("{}");

		CcpJsonRepresentation resultado = json.put(arrayJson, listaDePessoas);

		// Exibe o resultado final
		System.out.println("putMultiTest() = "+resultado);
	}

	@Test
	//Se o campo não existir ele cria e copia o valor para ele
	public void duplicateValueFromFieldErrorTest() {
		String variavel = "{"
				+ "'nome':'Pedro',"
				+ "'nome_copiado':''"
				+ "}";

		CcpJsonRepresentation json = new CcpJsonRepresentation(variavel);

		System.out.println("duplicateValueFromFieldTestError() = "+
				json.duplicateValueFromField(nome, nome_da_vó));
	}

	@Test
	public void renameFieldTest() {
		String pessoa = ""
				+ "    	   { "
				+ "        'nome': 'Alexandre Thomas Figueiredo', "
				+ "        'idade': 19, "
				+ "        'cpf': '09051710984', "
				+ "        'rg': '237564956', "
				+ "        'data_nasc': '01/05/2005', "
				+ "        'sexo': 'Masculino', "
				+ "        'signo': 'Touro', "
				+ "        'mae': 'Melissa Tatiane', "
				+ "        'pai': 'Yuri Murilo Figueiredo', "
				+ "        'email': 'alexandrethomasfigueiredo@ferplast.com.br', "
				+ "        'senha': 'PUjKB9KD9f', "
				+ "        'cep': '84185970', "
				+ "        'endereco': 'Rodovia do Cerne km 90', "
				+ "        'numero': 177, "
				+ "        'bairro': 'Centro', "
				+ "        'cidade': 'Abapã', "
				+ "        'estado': 'PR', "
				+ "        'telefone_fixo': '4235499785', "
				+ "        'celular': '42995209139', "
				+ "        'altura': '1,82', "
				+ "        'peso': 108, "
				+ "        'tipo_sanguineo': 'AB+', "
				+ "        'cor': 'vermelho' "
				+ "    } ";

		//Se o campo não existir ele ignora
		CcpJsonRepresentation json = new CcpJsonRepresentation(pessoa);
	
		CcpJsonRepresentation renameField = json.renameField(cor,carro).renameField(cor2, carro2);
		assertFalse(renameField.containsAllFields(carro2));
		assertTrue(renameField.containsAllFields(carro));
		assertFalse(renameField.containsAllFields(cor));
		assertFalse(renameField.containsAllFields(cor));
		renameField.renameField(new CcpFieldName("1"), new CcpFieldName("2"));
	}

	@Test
	public void removeFieldTest() {
		String pessoa = ""
				+ "    	   { "
				+ "        'nome': 'Alexandre Thomas Figueiredo', "
				+ "        'idade': 19, "
				+ "        'cpf': '09051710984', "
				+ "        'rg': '237564956', "
				+ "        'data_nasc': '01/05/2005', "
				+ "        'sexo': 'Masculino', "
				+ "        'signo': 'Touro', "
				+ "        'mae': 'Melissa Tatiane', "
				+ "        'pai': 'Yuri Murilo Figueiredo', "
				+ "        'email': 'alexandrethomasfigueiredo@ferplast.com.br', "
				+ "        'senha': 'PUjKB9KD9f', "
				+ "        'cep': '84185970', "
				+ "        'endereco': 'Rodovia do Cerne km 90', "
				+ "        'numero': 177, "
				+ "        'bairro': 'Centro', "
				+ "        'cidade': 'Abapã', "
				+ "        'estado': 'PR', "
				+ "        'telefone_fixo': '4235499785', "
				+ "        'celular': '42995209139', "
				+ "        'altura': '1,82', "
				+ "        'peso': 108, "
				+ "        'tipo_sanguineo': 'AB+', "
				+ "        'cor': 'vermelho' "
				+ "    } ";
		
		//SE O CAMPO NÃO EXISTIR ELE IGNORA
		CcpJsonRepresentation json = new CcpJsonRepresentation(pessoa);
		System.out.println("removeFieldTest() = "+json.removeFields(tipo_sanguineo));
		System.out.println("removeFieldTest() = "+json.removeFields(tipo_sanguineo));
	}

	@Test
	public void removeFieldsTest() {
		String pessoa = ""
				+ "    	   { "
				+ "        'nome': 'Alexandre Thomas Figueiredo', "
				+ "        'idade': 19, "
				+ "        'cpf': '09051710984', "
				+ "        'rg': '237564956', "
				+ "        'data_nasc': '01/05/2005', "
				+ "        'sexo': 'Masculino', "
				+ "        'signo': 'Touro', "
				+ "        'mae': 'Melissa Tatiane', "
				+ "        'pai': 'Yuri Murilo Figueiredo', "
				+ "        'email': 'alexandrethomasfigueiredo@ferplast.com.br', "
				+ "        'senha': 'PUjKB9KD9f', "
				+ "        'cep': '84185970', "
				+ "        'endereco': 'Rodovia do Cerne km 90', "
				+ "        'numero': 177, "
				+ "        'bairro': 'Centro', "
				+ "        'cidade': 'Abapã', "
				+ "        'estado': 'PR', "
				+ "        'telefone_fixo': '4235499785', "
				+ "        'celular': '42995209139', "
				+ "        'altura': '1,82', "
				+ "        'peso': 108, "
				+ "        'tipo_sanguineo': 'AB+', "
				+ "        'cor': 'vermelho' "
				+ "    } ";
		
		//SE O CAMPO NÃO EXISTIR ELE IGNORA
		CcpJsonRepresentation json = new CcpJsonRepresentation(pessoa);
		System.out.println("removeFieldsTest() = "+
				json.removeFields(
						tipo_sanguineo,
						cor,
						cpf
						));
		System.out.println("removeFieldsTest() = "+
				json.removeFields(tipo_sanguineo, cor, cpf));
	}
	
	@Test
	public void getContentTest() {
		String pessoa = "{'nome':'fulano','idade':30}";
		
		CcpJsonRepresentation json = new CcpJsonRepresentation(pessoa);
		System.out.println("getContentTest() = "+json.getContent()+"\n");
	}
	
	@Test
	public void copyTest() {
		String pessoa = "{'nome':'fulano','idade':30}";
		
		CcpJsonRepresentation json = new CcpJsonRepresentation(pessoa);
		CcpJsonRepresentation jsonCopia = json.copy();
		
		assertEquals(json, jsonCopia);
	}
	
	@Test
	public void getInnerJsonFromPathTest() {
	    String pessoa = ""
	        + "       { "
	        + "        'nome': 'Alexandre Thomas Figueiredo', "
	        + "        'idade': 19, "
	        + "        'cpf': '09051710984', "
	        + "        'rg': '237564956', "
	        + "        'data_nasc': '01/05/2005', "
	        + "        'sexo': 'Masculino', "
	        + "        'signo': 'Touro', "
	        + "        'mae': 'Melissa Tatiane', "
	        + "        'pai': 'Yuri Murilo Figueiredo', "
	        + "        'email': 'alexandrethomasfigueiredo@ferplast.com.br', "
	        + "        'senha': 'PUjKB9KD9f', "
	        + "        'endereco': { "
	        + "            'rua': 'Rodovia do Cerne km 90', "
	        + "            'numero': 177, "
	        + "            'bairro': 'Centro', "
	        + "            'cidade': 'Abapã', "
	        + "            'estado': 'PR', "
	        + "            'cep': '84185970' "
	        + "        }, "
	        + "        'telefone_fixo': '4235499785', "
	        + "        'celular': '42995209139', "
	        + "        'altura': '1,82', "
	        + "        'peso': 108, "
	        + "        'tipo_sanguineo': 'AB+', "
	        + "        'cor': 'vermelho' "
	        + "    } ";
	    
	    String valor = "valorDeTeste";
		CcpJsonRepresentation json = new CcpJsonRepresentation(pessoa).put(jsn, CcpOtherConstants.EMPTY_JSON.put(campoDeTeste, valor));
	    json.getInnerJsonFromPath(endereco);
	    json.getInnerJsonFromPath(endereco, "teste");
	    json.getInnerJsonFromPath(endereco);
	    assertTrue(json.getInnerJsonFromPath(json2).isEmpty());
	   
	    assertTrue(json.getInnerJsonFromPath(jsn).getAsString(campoDeTeste).equals(valor));
	    assertTrue(json.getInnerJsonFromPath(jsn).getAsString(campoDeTeste).equals(valor));
	    
	}
	
	@Test (expected = ClassCastException.class) 
	public void getInnerJsonFromPathTestError() {
				String pessoa = ""
				+ "    	   { "
				+ "        'nome': 'Alexandre Thomas Figueiredo', "
				+ "        'idade': 19, "
				+ "        'cpf': '09051710984', "
				+ "        'rg': '237564956', "
				+ "        'data_nasc': '01/05/2005', "
				+ "        'sexo': 'Masculino', "
				+ "        'signo': 'Touro', "
				+ "        'mae': 'Melissa Tatiane', "
				+ "        'pai': 'Yuri Murilo Figueiredo', "
				+ "        'email': 'alexandrethomasfigueiredo@ferplast.com.br', "
				+ "        'senha': 'PUjKB9KD9f', "
				+ "        'cep': '84185970', "
				+ "        'endereco': 'Rodovia do Cerne km 90', "
				+ "        'numero': 177, "
				+ "        'bairro': 'Centro', "
				+ "        'cidade': 'Abapã', "
				+ "        'estado': 'PR', "
				+ "        'telefone_fixo': '4235499785', "
				+ "        'celular': '42995209139', "
				+ "        'altura': '1,82', "
				+ "        'peso': 108, "
				+ "        'tipo_sanguineo': 'AB+', "
				+ "        'cor': 'vermelho' "
				+ "    } ";
		
		 CcpJsonRepresentation json = new CcpJsonRepresentation(pessoa);
		 CcpJsonRepresentation enderecoDoJson = json.getInnerJsonFromPath(endereco);
		    
		 System.out.println("getInnerJsonFromPathTestError() " + enderecoDoJson);
	
	}
	

	@Test
	public void getAsStringDecorator() {
		CcpOtherConstants.EMPTY_JSON.getAsStringDecorator(filho);
		CcpOtherConstants.EMPTY_JSON.getAsStringDecorator(filho);
	}
	
	@Test
	public void whenFieldsAreNotFound() {
		CcpJsonRepresentation jsn = CcpOtherConstants.EMPTY_JSON.whenFieldsAreNotFound(json -> json.put(pai, "pai"), filho);
		assertTrue(jsn.fieldSet().size() == 1);
		assertTrue(jsn.containsAllFields(pai));
		CcpJsonRepresentation whenFieldsAreNotFound = jsn.whenFieldsAreNotFound(CcpOtherConstants.RETURNS_EMPTY_JSON, pai);
		assertTrue(whenFieldsAreNotFound.containsAllFields(pai));
	}
	
	@Test
	public void addJsonTransformer() {
		CcpJsonRepresentation addJsonTransformer = CcpOtherConstants.EMPTY_JSON.addJsonTransformer(pai, json -> json.put(filho, "filho"));
		CcpBusiness business = addJsonTransformer.getAsObject(pai);
		CcpJsonRepresentation apply = business.apply(CcpOtherConstants.EMPTY_JSON);
		assertTrue(apply.fieldSet().size() == 1);
		assertTrue(apply.containsAllFields(filho));
		assertTrue(apply.containsAllFields(filho));
		apply.addJsonTransformer(teste, CcpOtherConstants.RETURNS_EMPTY_JSON);
	
	}

	@Test
	public void addJsonTransformerInteger() {
		CcpJsonRepresentation addJsonTransformer = CcpOtherConstants.EMPTY_JSON.addJsonTransformer(1, json -> json.put(filho, "filho"));
		CcpBusiness business = addJsonTransformer.getAsObject(new CcpFieldName(1));
		CcpJsonRepresentation apply = business.apply(CcpOtherConstants.EMPTY_JSON);
		assertTrue(apply.fieldSet().size() == 1);
		assertTrue(apply.containsAllFields(filho));
	
	}
	
	@Test
	public void toStringTestError() {
		CcpDependencyInjection.removeDependecy(CcpJsonHandler.class);
		assertTrue(CcpOtherConstants.EMPTY_JSON.toString().equals(CcpOtherConstants.EMPTY_JSON.content.toString()));
		try {
			CcpOtherConstants.EMPTY_JSON.asUgglyJson();
			assertTrue(false);
		} catch (RuntimeException e) {
			assertTrue(true);
		}
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());	
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void equalsFalse() {
		assertFalse(CcpOtherConstants.EMPTY_JSON.equals(x7)); 
		
	}
	
	
	@Test
	public void getAsJsonListTest() {
		String string = "	  ["
					    + "  {'nome': 'Alice Monteiro'}, "
					    + "  {'nome': 'Bruno Silva'}, "
					    + "  {'nome': 'Carla Souza'}, "
					    + "  {'nome': 'Diego Almeida'}, "
					    + "  {'nome': 'Eduarda Ferreira'}, "
					    + "  {'nome': 'Felipe Costa'}, "
					    + "  {'nome': 'Gabriela Santos'}, "
					    + "  {'nome': 'Hugo Pereira'}, "
					    + "  {'nome': 'Isabela Rocha'}, "
					    + "  {'nome': 'João Oliveira'}, "
					    + "  {'nome': 'Lara Ramos'}"
					    + "]";
		String dados = "{'nomes':"
			    + string
			    + "}";
		CcpJsonRepresentation json = new CcpJsonRepresentation(dados)
				.put(nomes7, (Object)Arrays.asList(CcpOtherConstants.EMPTY_JSON))
				.put(nomes2, (Object) null)
				.put(nomes3, "zoeira")
				.put(nomes4, string)
				.put(nomes5, 1)
				;
		assertTrue(json.getAsJsonList(nomes7).get(0).isEmpty());
		assertEquals(json.getAsJsonList(nomes4).size(), 11);
		assertEquals(json.getAsJsonList(nomes).size(), 11);
		assertTrue(json.getAsJsonList(nomes2).isEmpty());
		assertTrue(json.getAsJsonList(nomes3).isEmpty());
		assertTrue(json.getAsJsonList(nomes5).isEmpty());
		assertTrue(json.getAsJsonList(nomes6).isEmpty());
		assertTrue(json.getAsJsonList(nomes6).isEmpty());
	}
	
	@Test
	/*Aparentemente a classe CcpCollectionDecorator precisa de um toString()*/
	public void getAsCollectionDecoratorTest() {
		String dados = "{"
			    + "	 'nomes': ["
			    + "  {'nome': 'Alice Monteiro'}, "
			    + "  {'nome': 'Bruno Silva'}, "
			    + "  {'nome': 'Carla Souza'}, "
			    + "  {'nome': 'Diego Almeida'}, "
			    + "  {'nome': 'Eduarda Ferreira'}, "
			    + "  {'nome': 'Felipe Costa'}, "
			    + "  {'nome': 'Gabriela Santos'}, "
			    + "  {'nome': 'Hugo Pereira'}, "
			    + "  {'nome': 'Isabela Rocha'}, "
			    + "  {'nome': 'João Oliveira'}, "
			    + "  {'nome': 'Lara Ramos'}"
			    + "]"
			    + "}";
		CcpJsonRepresentation json = new CcpJsonRepresentation(dados);
		System.out.println("getAsCollectionDecorator() = "+json.getAsCollectionDecorator("nomes"));
		
	}
	
	@Test
	public void getAsStringListTest() {
		String names = "{"
				 + "'nomes'"
				 + ":"
				 + "['Pedro',"
				 + "'João',"
				 + "'Tiago']"
				 + "}";
		
		CcpJsonRepresentation json = new CcpJsonRepresentation(names)
				.put(nomes2, Arrays.asList("Pedro", "Tiago"))
				.put(nomes5, Arrays.asList("Pedro",null, "Tiago"))
				;
		
		assertTrue(json.getAsStringList(nomes3, nomes2, nomes).size() == 2);
		assertTrue(json.getAsStringList(nomes3, nomes, nomes2).size() == 3);
		assertTrue(json.getAsStringList(nomes, nomes2).size() == 3);
		assertTrue(json.getAsStringList(nomes2, nomes).size() == 2);
		assertTrue(json.getAsStringList(nomes5).size() == 2);
		assertTrue(json.getAsStringList(nomes2).size() == 2);
		assertTrue(json.getAsStringList(nomes).size() == 3);
		assertTrue(json.getAsStringList(nomes3, nomes4).isEmpty());
		assertTrue(json.getAsStringList(nomes3).isEmpty());
		assertTrue(json.getAsStringList(nomes3).isEmpty());
		
	}
	
	@Test
	public void getAsObjectListTest() {
		String keys = "{"
					+ "'nome': 'Lucas',"
					+ "'pais': 'Brasil'"
					+ "}";
		
		Object[] value = new Object[] {1,2,3,4};
		CcpJsonRepresentation json = new CcpJsonRepresentation(keys)
				.put(x1, (Object) null)
				.put(x2, new Object[] {})
				.put(x3, value)
				.put(x4, Arrays.asList())
				.put(x5, Arrays.asList(value))
				.put(x6, "[]")
				.put(nomes7, "")
				.put(x7, Arrays.asList(value).toString())
				;
		assertTrue(json.getAsObjectList(x).isEmpty());
		assertTrue(json.getAsObjectList(x1).isEmpty());
		assertTrue(json.getAsObjectList(x2).isEmpty());
		assertTrue(json.getAsObjectList(x4).isEmpty());
		assertTrue(json.getAsObjectList(x6).isEmpty());
		assertTrue(json.getAsObjectList(nomes7).isEmpty());
		assertEquals(json.getAsObjectList(x3).size(), value.length);
		assertEquals(json.getAsObjectList(x5).size(), value.length);
		assertEquals(json.getAsObjectList(x7).size(), value.length);
	}
	
	@Test
	public void putAllTest() {
		String keys = "{'pais':'Brasil',"
					+ "'cidade':'São Paulo',"
					+ "'Estado':'SP'}";
		
		CcpJsonRepresentation json = new CcpJsonRepresentation(keys);
		CcpJsonRepresentation putAll = json.mergeWithAnotherJson(json);
		System.out.println("\nputAllTest() = "+ putAll);
	}
	
	@Test
	public void containsFieldTest() {
		String campos = "{"
					  + "'nome':'Chico',"
					  + "'idade':50,"
					  + "'peso':62.3,"
					  + "'nacionalidade':'brasileiro'"
					  + "}";
		
		CcpJsonRepresentation json = new CcpJsonRepresentation(campos);
		System.out.println("\ncontainsFieldTest() "+json.containsField(nome));
		System.out.println("containsFieldTest() "+json.containsField(paçoca));
		System.out.println("containsFieldTest() "+json.containsField(nacionalidade)+"\n");
	}
	
	@Test
	public void containsAllFieldsJsonTest() {
		String campos = "{"
					  + "'nome':'Chico',"
					  + "'idade':50,"
					  + "'peso':62.3,"
					  + "'nacionalidade':'brasileiro'"
					  + "}";
		
		CcpJsonRepresentation json = new CcpJsonRepresentation(campos);

	    Collection<String> fields = Arrays.asList("nome", "idade", "peso");

	    boolean result = json.containsAllFields(fields);
	    assertTrue(result);	
	    boolean result2 = json.containsAllFields(nome, idade, peso, campoQueNaoExiste);
	    assertFalse(result2);	
	    assertFalse(json.containsAnyFields(onias, juliana, luciellen, andré, camila, welton));	
	    
	    System.out.println("\ncontainsAllFieldsJsonTest() " + result+"\n");
	    json.containsAllFields(new CcpFieldName("1"));
	    
	    
	}
	
	@Test
	public void containsAllFieldsTest() {
		String campos = "{"
				+ "'nome':'Chico',"
				+ "'idade':50,"
				+ "'peso':62.3,"
				+ "'nacionalidade':'brasileiro'"
				+ "}";

		CcpJsonRepresentation json = new CcpJsonRepresentation(campos);

		Collection<String> fields = Arrays.asList("nome", "idade", "peso");

		boolean result = json.containsAllFields(fields);

		System.out.println("\ncontainsAllFieldsTest() " + result+"\n");
	}
	
	@Test
	//Retorna false apenas de conter o campo "nome"
	public void containsAnyFieldsTest() {
		String campos = "{"
				+ "'nome':'Chico',"
				+ "'idade':50,"
				+ "'peso':62.3,"
				+ "'nacionalidade':'brasileiro'"
				+ "}";

		CcpJsonRepresentation json = new CcpJsonRepresentation(campos);

		Collection<String> fields = Arrays.asList("nome", "sobrenome", "carro");

		boolean result = json.containsAllFields(fields);

		System.out.println("\ncontainsAnyFieldsTest() " + result+"\n");
		json.containsAnyFields(new CcpFieldName("1"));
	}
	
	@Test
	public void containsAnyFieldsJsonTest() {
		String campos = "{"
				+ "'nome':'Chico',"
				+ "'idade':50,"
				+ "'peso':62.3,"
				+ "'nacionalidade':'brasileiro'"
				+ "}";

		CcpJsonRepresentation json = new CcpJsonRepresentation(campos);

		System.out.println("\ncontainsAnyFieldsJsonTest() " + json.containsAnyFields(peso,carro,idade)+"\n");
	}
	
	@Test
	public void getTest() {
		String campos = "{"
				+ "'nome':'Chico',"
				+ "'idade':50,"
				+ "'peso':62.3,"
				+ "'nacionalidade':'brasileiro'"
				+ "}";

		CcpJsonRepresentation json = new CcpJsonRepresentation(campos);
		
		Double valorConvertidoEmObjeto = (double) json.get(peso);
		
		System.out.println("\ngetTest() "+ (valorConvertidoEmObjeto+1) + "\n");
		
		try {
			json.get(peso2);
			assertTrue(false);
		} catch (CcpErrorJsonFieldNotFound e) {
			assertTrue(true);
		}
		json.get(peso);
	}
	
	@Test
	public void getAsObjectTest() {
		String campos = "{"
				+ "'nome':'Chico',"
				+ "'idade':50,"
				+ "'peso':62.3,"
				+ "'nacionalidade':'brasileiro'"
				+ "}";

		CcpJsonRepresentation json = new CcpJsonRepresentation(campos);
		
		//Muda para qualquer tipo sem a necessidade de fazer o casting
		Double valorConvertidoEmObjeto =  json.getAsObject(onias, juliana, luciellen, andré, camila, welton,peso);
		
		assertTrue(valorConvertidoEmObjeto instanceof Double);
		try {
			json.getAsObject(onias, juliana, luciellen, andré, camila, welton);
			assertTrue(false);
		} catch (CcpErrorJsonFieldNotFound e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void isEmptyTest() {
		String vazio = "{}";
		
		CcpJsonRepresentation json = new CcpJsonRepresentation(vazio);
		
		assertEquals(json.isEmpty(), CcpOtherConstants.EMPTY_JSON.isEmpty());
	}
	
	//VERIFICAR SE MÉTODO ESTÁ FUNCIONANDO
	@Test
	public void copyIfNotContainsTest() {
		String json = "{'nome':'Lucas'}";

		CcpJsonRepresentation objJson = new CcpJsonRepresentation(json);
		assertFalse(objJson.containsAllFields(nomeCopiado));
		CcpJsonRepresentation copyIfNotContains = objJson.copyIfNotContains(nome, nomeCopiado);
		assertTrue(copyIfNotContains.containsAllFields(nomeCopiado));
		String nomeCopiadoStr = copyIfNotContains.getAsString(nomeCopiado);
		String nomeStr = copyIfNotContains.getAsString(nome);
		assertEquals(nomeStr, nomeCopiadoStr);
		String actual = "merda";
		CcpJsonRepresentation copyIfNotContains2 = copyIfNotContains
				.put(nome, actual).copyIfNotContains(nomeCopiado, nome);
		assertNotEquals(copyIfNotContains2.getAsString(nome), copyIfNotContains.getAsString(nomeCopiado));
		assertEquals(copyIfNotContains2.getAsString(nome), actual);
		objJson.copyIfNotContains(nome, nomeCopiado);
	}
	
	//VERIFICAR SE MÉTODO ESTÁ FUNCIONANDO
	@Test
	public void putIfNotContainsTest() {
		String json = "{}";// TALVEZ ELE IDENTIFIQUE O CAMPO EM BRANCO COMO ALGO, COLOCAR A PALAVRA
							// 'undefined' TAMBÉM NÃO FUNCIONPU

		CcpJsonRepresentation objJson = new CcpJsonRepresentation(json);
		assertFalse(objJson.containsAllFields(valor));
		CcpJsonRepresentation putIfNotContains = objJson.putIfNotContains(valor, "teste");
		assertTrue(putIfNotContains.containsAllFields(valor));
		CcpJsonRepresentation putIfNotContains2 = putIfNotContains.putIfNotContains(valor, "teste2");
		assertTrue("teste".equals(putIfNotContains2.getAsString(valor)));
		assertTrue("teste".equals(putIfNotContains2.getAsString(valor)));
		assertTrue("teste".equals(putIfNotContains2.getAsString(valor)));
		putIfNotContains2.putIfNotContains(teste, putIfNotContains2);
	}	
	
	@Test
	public void getAsArrayMetadataTest() {
		String json = "{'field':'value'}";
		
		CcpJsonRepresentation objJson = new CcpJsonRepresentation(json);
		
		//Ao tentar imprimir o decorator recebi o endereço de memória talvez esteja faltando o toString()
		assertTrue(objJson.getAsArrayMetadata(field) instanceof CcpCollectionDecorator);
		assertTrue(objJson.getAsArrayMetadata(field) instanceof CcpCollectionDecorator);
	}
	
	@Test
	public void hashCodeTest() {
		String registro = "{'valor':25}";

		CcpJsonRepresentation json = new CcpJsonRepresentation(registro);
		
		System.out.println(json.hashCode());
	}
	
	//Equals não retornou o resultado esperado
	@Test
	public void equalsTest() {
		String registro = "{'valor':25}";

		CcpJsonRepresentation json = new CcpJsonRepresentation(registro);
		CcpJsonRepresentation json2 = new CcpJsonRepresentation(registro);
		
		
		assertTrue(json.equals(json2));
	}
	
	
	@Test
	public void getInnerJsonListFromPathTest() {
		List<Object> granChildreen = Arrays.asList(
				CcpOtherConstants.EMPTY_JSON.put(nome, "onias").content,
				CcpOtherConstants.EMPTY_JSON.put(nome, "juliana"),
				"{'nome': 'luciellen'}"
				);
		CcpJsonRepresentation dad = CcpOtherConstants.EMPTY_JSON.addToItem(filho, netos, 
				granChildreen);
		
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.addToItem(avo, pai, dad);
		
		json.getInnerJsonListFromPath(avo, pai, filho, netos);
	
		ArrayList<Object> netos2 = new ArrayList<>(granChildreen);
		netos2.add(0);
		dad = CcpOtherConstants.EMPTY_JSON.addToItem(filho, netos, netos2);
		
		json = CcpOtherConstants.EMPTY_JSON.addToItem(avo, pai, dad);
		
		try {
			json.getInnerJsonListFromPath(avo, pai, filho, netos);
			assertTrue(false);
		} catch (CCpErrorJsonFieldIsNotValidJsonList e) {
			assertTrue(true);
		}
		dad = CcpOtherConstants.EMPTY_JSON.addToItem(filho, netos, granChildreen);
		
		json = CcpOtherConstants.EMPTY_JSON.addToItem(avo, pai, dad);

		List<CcpJsonRepresentation> list = json.getInnerJsonListFromPath(avo, pai, filho, netos, bisnetos);
		
		assertTrue(list.isEmpty());
		
		CcpOtherConstants.EMPTY_JSON.addToItem(new CcpFieldName("1"), new CcpFieldName("2"), json.toString());
		CcpOtherConstants.EMPTY_JSON.addToItem(new CcpFieldName("1"), new CcpFieldName("2"), json);
		json.getInnerJsonListFromPath(avo, pai, filho, netos, bisnetos);
	}
	
	@Test
	public void putSameValueInManyFieldsTest() {
		String value = "valor";
		CcpJsonRepresentation putSameValueInManyFields = CcpOtherConstants.EMPTY_JSON.putSameValueInManyFields(value, v1, v2, v3, v4);
		Set<String> fieldSet = putSameValueInManyFields.fieldSet();
		for (String field : fieldSet) { 
			JsonFieldNames valueOf = JsonFieldNames.valueOf(field);
			Object object = putSameValueInManyFields.get(valueOf);
			assertEquals(object, value);
		}
		
		CcpOtherConstants.EMPTY_JSON.putSameValueInManyFields(value, v1, v2, v3, v4);
	}
	
	@Test
	public void duplicateValueFromFieldTest() {
		assertTrue(CcpOtherConstants.EMPTY_JSON.isEmpty());
		assertTrue(CcpOtherConstants.EMPTY_JSON.put(f1,"v1").duplicateValueFromField(f1, f2, f3, f4).fieldSet().size() == 4);
		assertTrue(CcpOtherConstants.EMPTY_JSON.duplicateValueFromField(f1, f2, f3, f4).isEmpty());
		CcpOtherConstants.EMPTY_JSON.duplicateValueFromField(CcpOtherConstants.EMPTY_STRING, CcpOtherConstants.EMPTY_STRING);
	}
	
	@Test
	public void getInnerJsonTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(json3, (Object)CcpOtherConstants.EMPTY_JSON.put(campo1, "valor1"))
				.put(jsn, "{'nome': 'onias'}")
				.put(json2, new HashMap<>())
				.put(json4, (Object)null)
				.put(json5, 1)
				
				;
		assertTrue(put.getInnerJson(jsn).containsAllFields(nome));
		assertTrue(put.getInnerJson(json3).fieldSet().size() == 1);
		assertTrue(put.getInnerJson(json2).isEmpty());
		assertTrue(put.getInnerJson(json4).isEmpty());
		assertTrue(put.getInnerJson(json5).isEmpty());
		assertTrue(put.getInnerJson(json5).isEmpty());
	}
	
	@Test
	public void addToListTest() {
		CcpJsonRepresentation addToList = CcpOtherConstants.EMPTY_JSON
				.addToList(frutas, "laranja", "banana", "abacate", "limão")
				.addToList(x7, CcpOtherConstants.EMPTY_JSON)
				;
		assertEquals(addToList.getAsStringList(frutas).size(), 4);
		addToList.addToList(teste, addToList);
		addToList.addToList(teste, addToList, addToList);
	}
	
	@Test
	public void isInnerJsonTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
		.put(campo1, "s")
		.put(campo2, "{}")
		.put(campo3, CcpOtherConstants.EMPTY_JSON)
		.put(campo4, CcpOtherConstants.EMPTY_JSON.content)
		.put(campo5, (Object) null)
		.put(campo6, 1)
		.put(campo7, false)
		.put(campo8, 1.0)
		.put(campo9, "");
		assertFalse(json.isInnerJson(campo1));
		assertTrue(json.isInnerJson(campo2));
		assertTrue(json.isInnerJson(campo3));
		assertTrue(json.isInnerJson(campo4));
		assertFalse(json.isInnerJson(campo5));
		assertFalse(json.isInnerJson(campo6));
		assertFalse(json.isInnerJson(campo7));
		assertFalse(json.isInnerJson(campo8));
		assertFalse(json.isInnerJson(campo9));
		assertFalse(json.isInnerJson(campo9));
	}
	
	@Test
	public void getExclusiveListTest() {
		
		CcpCollectionDecorator decorator = new CcpCollectionDecorator(new Object[] {"a", "b", "c", "d", "e", "f"});
		
		List<String> externalList = Arrays.asList("b", "c", "d");
		{
			List<String> exclusiveList = decorator.getExclusiveList(externalList);

			boolean containsAll = exclusiveList.containsAll(Arrays.asList("a", "e", "f"));
			
			assertTrue(containsAll);
			
		}
		{
			List <String> intersectList = decorator.getIntersectList(externalList);
			
			boolean containsAll = intersectList.containsAll(Arrays.asList("b", "c", "d"));
			
			assertTrue(containsAll);
		}		
	
	}
	
	@Test
	public void x() {
		String email = "onias85@gmail.com@teste.com";
		CcpEmailDecorator decorator = new CcpStringDecorator(email).email();
		boolean valid = decorator.isValid();
		assertFalse(valid);
	}

	// ── getJsonSupplier ───────────────────────────────────────────────────────

	@Test
	public void getJsonSupplierTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(nome, "Onias");
		var supplier = json.getJsonSupplier();
		assertEquals(json, supplier.get());
	}

	// ── whenAnyFieldsAreFound ─────────────────────────────────────────────────

	@Test
	public void whenAnyFieldsAreFoundExecutaBusinessTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(nome, "Onias");
		CcpJsonRepresentation result = json.whenAnyFieldsAreFound(j -> j.put(filho, "ok"), nome, campoQueNaoExiste);
		assertTrue(result.containsAllFields(filho));
	}

	@Test
	public void whenAnyFieldsAreFoundNaoExecutaBusinessTest() {
		CcpJsonRepresentation result = CcpOtherConstants.EMPTY_JSON.whenAnyFieldsAreFound(j -> j.put(filho, "ok"), nome, campoQueNaoExiste);
		assertFalse(result.containsAllFields(filho));
	}

	// ── whenAllFieldsAreFound ─────────────────────────────────────────────────

	@Test
	public void whenAllFieldsAreFoundExecutaBusinessTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(nome, "Onias").put(idade, 39);
		CcpJsonRepresentation result = json.whenAllFieldsAreFound(j -> j.put(filho, "ok"), nome, idade);
		assertTrue(result.containsAllFields(filho));
	}

	@Test
	public void whenAllFieldsAreFoundNaoExecutaBusinessTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(nome, "Onias");
		CcpJsonRepresentation result = json.whenAllFieldsAreFound(j -> j.put(filho, "ok"), nome, idade);
		assertFalse(result.containsAllFields(filho));
	}

	// ── getValueFromPath ──────────────────────────────────────────────────────

	@Test
	public void getValueFromPathTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(nome, "Onias");
		String result = json.getValueFromPath("default", nome);
		assertEquals("Onias", result);
		String missing = json.getValueFromPath("default", campoQueNaoExiste);
		assertEquals("default", missing);
	}

	@Test(expected = CcpErrorJsonPathIsMissing.class)
	public void getValueFromPathMissingTest() {
		CcpOtherConstants.EMPTY_JSON.getValueFromPath("");
	}

	// ── getTransformedJsonExecutingIfAndElse ──────────────────────────────────

	@Test
	public void getTransformedJsonExecutingIfAndElseConditionMetTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(nome, "Onias");
		CcpJsonRepresentation result = json.getTransformedJsonExecutingIfAndElse(
				j -> j.containsAllFields(nome), j -> j.put(filho, "met"), j -> j.put(filho, "notmet"));
		assertEquals("met", result.getAsString(filho));
	}

	@Test
	public void getTransformedJsonExecutingIfAndElseConditionNotMetTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(nome, "Onias");
		CcpJsonRepresentation result = json.getTransformedJsonExecutingIfAndElse(
				j -> j.containsAllFields(campoQueNaoExiste), j -> j.put(filho, "met"), j -> j.put(filho, "notmet"));
		assertEquals("notmet", result.getAsString(filho));
	}

	// ── getTransformedJsonWhenAllConditionsMatch ───────────────────────────────

	@SuppressWarnings("unchecked")
	@Test
	public void getTransformedJsonWhenAllConditionsMatchTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(nome, "Onias").put(idade, 39);
		CcpJsonRepresentation result = json.getTransformedJsonWhenAllConditionsMatch(
				j -> j.put(filho, "met"), j -> j.put(filho, "notmet"),
				j -> j.containsAllFields(nome), j -> j.containsAllFields(idade));
		assertEquals("met", result.getAsString(filho));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getTransformedJsonWhenAllConditionsMatchNaoSatisfazTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(nome, "Onias");
		CcpJsonRepresentation result = json.getTransformedJsonWhenAllConditionsMatch(
				j -> j.put(filho, "met"), j -> j.put(filho, "notmet"),
				j -> j.containsAllFields(nome), j -> j.containsAllFields(campoQueNaoExiste));
		assertEquals("notmet", result.getAsString(filho));
	}

	// ── getTransformedJsonConsideringIfAnyOfTheConditionsIsMet ────────────────

	@SuppressWarnings("unchecked")
	@Test
	public void getTransformedJsonConsideringIfAnyOfTheConditionsIsMetConditionMetTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(nome, "Onias");
		CcpJsonRepresentation result = json.getTransformedJsonConsideringIfAnyOfTheConditionsIsMet(
				j -> j.put(filho, "met"), j -> j.put(filho, "notmet"),
				j -> j.containsAllFields(nome), j -> j.containsAllFields(campoQueNaoExiste));
		assertEquals("met", result.getAsString(filho));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getTransformedJsonConsideringIfAnyOfTheConditionsIsMetNenhumaCondicaoTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(nome, "Onias");
		CcpJsonRepresentation result = json.getTransformedJsonConsideringIfAnyOfTheConditionsIsMet(
				j -> j.put(filho, "met"), j -> j.put(filho, "notmet"),
				j -> j.containsAllFields(campoQueNaoExiste));
		assertEquals("notmet", result.getAsString(filho));
	}

	// ── getAsStringArray ──────────────────────────────────────────────────────

	@Test
	public void getAsStringArrayTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(nomes, Arrays.asList("Pedro", "João", "Tiago"));
		String[] array = json.getAsStringArray(nomes);
		assertEquals(3, array.length);
		assertTrue(Arrays.asList(array).contains("Pedro"));
	}

	// ── mergeWithAnotherJson(Map) ─────────────────────────────────────────────

	@Test
	public void mergeWithAnotherJsonMapTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(nome, "Onias");
		HashMap<String, Object> extra = new HashMap<>();
		extra.put(idade.getValue(), 39);
		CcpJsonRepresentation merged = json.mergeWithAnotherJson(extra);
		assertTrue(merged.containsAllFields(nome));
		assertEquals(39, (int) merged.getAsIntegerNumber(idade));
	}

	// ── put(CcpJsonFieldName, CcpDecorator) ───────────────────────────────────

	@Test
	public void putDecoratorTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(nome, new CcpStringDecorator("Onias"));
		assertEquals("Onias", json.getAsString(nome));
	}

	// ── toInputStream ─────────────────────────────────────────────────────────

	@Test
	public void toInputStreamTest() throws Exception {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(nome, "Onias");
		var is = json.toInputStream();
		byte[] bytes = is.readAllBytes();
		String str = new String(bytes);
		assertTrue(str.contains("Onias"));
	}

	// ── getSha1Hash ───────────────────────────────────────────────────────────

	@Test
	public void getSha1HashTest() {
		CcpJsonRepresentation json1 = CcpOtherConstants.EMPTY_JSON.put(nome, "Onias");
		CcpJsonRepresentation json2 = CcpOtherConstants.EMPTY_JSON.put(nome, "Onias");
		String hash1 = json1.getSha1Hash(com.ccp.hash.CcpHashAlgorithm.SHA1);
		String hash2 = json2.getSha1Hash(com.ccp.hash.CcpHashAlgorithm.SHA1);
		assertFalse(hash1.isEmpty());
		assertEquals(hash1, hash2);
	}

}

