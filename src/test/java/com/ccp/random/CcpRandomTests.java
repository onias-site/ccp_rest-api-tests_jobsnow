package com.ccp.random;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpCollectionDecorator;
import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpFolderDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpDynamicJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTextDecorator;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.query.CcpQueryExecutor;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityField;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.especifications.password.CcpPasswordHandler;
import com.ccp.especifications.text.extractor.CcpTextExtractor;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.implementations.text.extractor.apache.tika.CcpApacheTikaTextExtractor;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.business.login.JnBusinessExecuteLogout;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginToken;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.vis.entities.VisEntityResume;
import com.vis.resumes.ImportResumeFromOldJobsNow;
import com.vis.services.VisServiceSkills;

public class CcpRandomTests {

	static CcpJsonRepresentation groupedCompanies = CcpOtherConstants.EMPTY_JSON;

	public static void main(String[] args) {

//		countWords(); 
//		saveSkills();
//		relatoriosDasSkillsNosCurriculos();
//		getSkillsFromText();
	} 

	static void getSkillsFromText() {
		CcpJsonRepresentation skillsFromText = getSkillsFromText("ONIAS VIEIRA JUNIOR\r\n"
				+ "EMAIL: onias85@gmail.com NASCIDO EM: 22/06/1985. CONTACTO: (11) 966-058-642.\r\n"
				+ "LOCALIZAÇÃO: Jabaquara  – SÃO PAULO.\r\n"
				+ "\r\n"
				+ "FORMAÇÃO ACADÊMICA / ESCOLARIDADE:\r\n"
				+ "GRADUADO EM TECNOLOGIA DA INFORMAÇÃO PELO CENTRO UNIVERSITÁRIO LUSÍADA, SANTOS – SP em 2007.\r\n"
				+ "MBA EM JAVA COM SOA PELA FIAP, SÃO PAULO – SP em 2014.\r\n"
				+ "\r\n"
				+ "RESUMO DO CURRÍCULO:\r\n"
				+ "Programador Java desde junho de 2010.\r\n"
				+ "Programador desde abril de 2009.\r\n"
				+ "Certificação de Programador Java sun scjp 6 (em 2010).\r\n"
				+ "Certificação de Programador Java oracle ocjp 7 (em 2015).\r\n"
				+ "Certificação Associate Java oracle ocjp 5 e 6 (em 2015).\r\n"
				+ "Inglês intermediário (conversação), avançado (leitura).\r\n"
				+ "Espanhol avançado (conversação), avançado (leitura).\r\n"
				+ "\r\n"
				+ "RESUMO DAS FERRAMENTAS JÁ TRABALHADAS:\r\n"
				+ "CLOUD: Google Cloud (BigQuery, DataStore, App Engine(SAAS), Compute Engine(PAAS), Cloud Sql, Pub/Sub, Cloud Sql, MemCache).\r\n"
				+ "DATABASES: Oracle, Sql Server, MySql, elasticsearch.\r\n"
				+ "METODOLOGIA: Scrum, TDD, DDD.\r\n"
				+ "ARQUITETURA: EJB 2 e 3, Spring 3 e 4, Design Patterns, Reflection, UML, JAB.\r\n"
				+ "WS: AXIS, Restlet, Jersey,  JMS, JSON, XML, XSD, Xpath.\r\n"
				+ "LINGUAGENS: Delphi, C#, Java, Javascript.\r\n"
				+ "IDE: Eclipse, Visual Studio, RTC.\r\n"
				+ "SERVIDORES: Tomcat, Jboss, Weblogic E Websphere.\r\n"
				+ "WEB: Html 5, CSS 3, Bootstrap, primeng.\r\n"
				+ "MOBILE: CORDOVA, IONIC 2.\r\n"
				+ "JS: Jquery, Jquery Ui, Ext JS, AngularJs(1,2 e 5), TypeScript, reactjs.\r\n"
				+ "MVC: JSF 1 e 2, Primefaces 3, JSP, Vraptor 4, Spring MVC, Servlets, Struts 1 e 2, NestJs.\r\n"
				+ "ORM: Hibernate, JPA, JDBC.\r\n"
				+ "TESTES: Junit, Jmeter, Soap UI, Teste NG, Mockito.\r\n"
				+ "S.O: Windows, Linux.\r\n"
				+ "DEPLOY: Ant, Maven, Jenkins.\r\n"
				+ "VERSIONADOR: TFS, GIT, SVN, CVS, VSS. \r\n"
				+ "AOP: ASPECTJ.\r\n"
				+ "PORTLETS: Wem(VCM), Liferay, Fluig (Totvs).\r\n"
				+ "OUTROS: Applet, Swing, IBM Maximo, Java FX.\r\n"
				+ "\r\n"
				+ "\r\n"
				+ "EXPERIENCIA PROFISSIONAL:\r\n"
				+ "CLIENTE: JobsNow.\r\n"
				+ "CONSULTORIA ALOCADORA:  Negócio próprio.\r\n"
				+ "PERÍODO:  10/2018 ao momento atual.\r\n"
				+ "FERRAMENTAS:  Google Cloud (MemCache, PUB/SUB, App engine(SAAS), Compute Engine (PAAS)), react Js, eclipse, java 8, springboot, elasticsearch (construi API  Java para ele), maven, git, css, html, javascript, visual studio code, APIs javascript.\r\n"
				+ "CARGO: Consultor de Tecnologia.\r\n"
				+ "ATIVIDADES:  Cadastro, estatísticas e buscas  (no estilo google) inteligentes de currículo.\r\n"
				+ "\r\n"
				+ "CLIENTE: NoxxonSatt.\r\n"
				+ "PERÍODO:  01/2018 a 12/2023.\r\n"
				+ "FERRAMENTAS:  Google Cloud (BigQuery, DataStore, MemCache, PUB/SUB, CloudSql, App engine(SAAS), Compute Engine (PAAS)), react Js, angular 8, angular Js, eclipse, java 8, springboot, JPA, maven, git, css, html, javascript, visual , NestJs, NodeJs, studio code, APIs javascript e java para georeferencia / geolocalização.\r\n"
				+ "CARGO: Consultor de Tecnologia.\r\n"
				+ "ATIVIDADES:  Monitoramento e geolocalização de 6 mil ônibus intermunicipais geridos pela EMTU, manutenção desse sistema em ambiente de nuvem (Google).\r\n"
				+ "\r\n"
				+ "CLIENTE: Santander.\r\n"
				+ "CONSULTORIA ALOCADORA:  Itera Consultoria.\r\n"
				+ "PERÍODO: 03/2017 a 01/2018\r\n"
				+ "FERRAMENTAS:  Java, Jab(Java Arquitetura Brasil), SQL, Springboot, Eclipse, Jenkins, RTC, IBM Broker\r\n"
				+ "CARGO: Consultor de Tecnologia.\r\n"
				+ "ATIVIDADES:  Exposição de dados de PL/SQL em rest (projeto KiPrev), guarda dos dados de arquivos sequenciais em banco de dados(Projeto Reinf).\r\n"
				+ "\r\n"
				+ "CLIENTE: CERTISIGN, NETSHOES, SEFAZ, TOTVS\r\n"
				+ "CONSULTORIA ALOCADORA: TOTVS SERRA DO MAR.\r\n"
				+ "PERÍODO: 09/2013 até 07/2016\r\n"
				+ "FERRAMENTAS:  Javascript, html, css, spring, oracle, hibernate, jboss, tomcat, eclipse, Visual Studio, Java 4, 6 e 7,  C#, jenkins, ANT deploy, Maven, JMeeter,  Velocity, TFS, SVN, Junit, JMS, SOAP, JSF, Primefaces, SVN, Linux, JSON, JQUERY, JQUERY UI, Fluig\r\n"
				+ "CARGO: Consultor de Tecnologia Java\r\n"
				+ "ATIVIDADES:  Front end Jquery para sistema Fluig (TOTVS), substituição de scriptlet [java com html] em JSPs por SPA em jquery e criação de framework próprio para trabalhar json(SEFAZ). Criação do portal do fornecedor em jsf/primefaces e sua especificação de webservice e seu framework de comunicação SOAP com SAP, criação de comunicação com JMS (Netshoes). Sustentação de sistemas web para gerenciamento de certificado digital, sustentação de framework de assinador digital (Certisign)\r\n"
				+ "\r\n"
				+ "\r\n"
				+ "CLIENTE: BRADESCO\r\n"
				+ "CONSULTORIA ALOCADORA: IFACTORY SOLUTIONS.\r\n"
				+ "PERÍODO: 04/2013 a 09/2013\r\n"
				+ "PROJETO: NPORTALBRA\r\n"
				+ "REGRA DE NEGÓCIO: Site de conteúdo / Portal.\r\n"
				+ "OBJETIVO: Mover todo o conteúdo do portal em html estático para o gestor de conteúdo WEM (VCM).\r\n"
				+ "FERRAMENTAS: JSP, WEM (VCM), SCRUM, SVN, HTML, JAVASCRIPT, CSS, HTML.\r\n"
				+ "CARGO: Analista / programador Pleno\r\n"
				+ "ATIVIDADES:  Criação de componentes em JSP para portar conteúdo estático em templates do WEM [Web Experience Management] (Bradesco)\r\n"
				+ "CLIENTE: CCR/ODDEBRECHT\r\n"
				+ "CONSULTORIA ALOCADORA: CAPITAL AMBIENTAL.\r\n"
				+ "PERÍODO: 09/2012 a 04/2013\r\n"
				+ "PROJETO: CERENSA\r\n"
				+ "REGRA DE NEGÓCIO: Gestão ambiental.\r\n"
				+ "OBJETIVO: Prover software com interface web para inventariado de impactos ambientais.\r\n"
				+ "FERRAMENTAS:  JAVA 6, Vraptor, ExtJS, Spring, Hibernate, My sql, Tomcat, GIT, CSS, HTML.\r\n"
				+ "CARGO: Analista / programador Pleno\r\n"
				+ "ATIVIDADES:  Construção de front end em ExtJs e back end em java com spring\r\n"
				+ "\r\n"
				+ "CLIENTE: VOLKSWAGEN\r\n"
				+ "CONSULTORIA ALOCADORA: CSI SISTEMAS E ENGENHARIA.\r\n"
				+ "PERÍODO: 02/2012 a 09/2012\r\n"
				+ "PROJETO: PICK BY LIGHT\r\n"
				+ "REGRA DE NEGÓCIO: Automação Industrial.\r\n"
				+ "OBJETIVO: Prover software de automação com biometria de impressão digital que informe as etiquetas que devem ser colocadas no carro que está sendo montado e avisar caso seja usada a etiqueta errada.\r\n"
				+ "FERRAMENTAS:  JAVA, 6, Swing, reflection, SVN, Design Pattern State machine, AMGSTROM (LINUX EMBARCADO).\r\n"
				+ "CARGO: Analista / programador Junior\r\n"
				+ "ATIVIDADES:  Criaçaõ de sistema de automação na linha da Volkswagen que permitia ao operador saber quais etquetas colar em um carro.\r\n"
				+ "\r\n"
				+ "CLIENTE: PMSBC (PREFEITURA MUNICIPAL DE SÃO BERNARDO DO CAMPO)\r\n"
				+ "CONSULTORIA ALOCADORA: G & P SISTEMAS.\r\n"
				+ "PERÍODO: 01/2011 a 01/2012\r\n"
				+ "PROJETO: SIGOM (SISTEMA DE GESTÃO ORÇAMENTÁRIA MUNICIPAL)\r\n"
				+ "REGRA DE NEGÓCIO: Gestão Orçamentária Municipal.\r\n"
				+ "OBJETIVO: Manutenção em software que tem por função automatizar a gestão orçamentária municipal.\r\n"
				+ "FERRAMENTAS:  JAVA 6, EJB2, Struts 1, Oracle, JBOSS, SVN, HTML, CSS, JAVASCRIPT, DDD.\r\n"
				+ "CARGO: Analista / programador Junior\r\n"
				+ "ATIVIDADES:  Sustentação de sistema de orçamento municipal em seu back end e front end.\r\n"
				+ "\r\n"
				+ "CLIENTE: SHOPPING RECIFE E AVON.\r\n"
				+ "CONSULTORIA ALOCADORA: NRB SOLUTIONS.\r\n"
				+ "PERÍODO: 06/2010 a 01/2011\r\n"
				+ "PROJETO: CUSTOMIZAÇÃO DO IBM MAXIMO.\r\n"
				+ "REGRA DE NEGÓCIO: EAM (Enterprise Asset Management).\r\n"
				+ "OBJETIVO: Prover customização de classes Java do software IBM MAXIMO.\r\n"
				+ "FERRAMENTAS:  Java 6, Struts 2, Javascript, html, css, oracle, IBM MAXIMO.\r\n"
				+ "CARGO: Analista / programador Junior\r\n"
				+ "ATIVIDADES:  Customização em java de ferramenta da IBM para clientes.\r\n"
				+ "\r\n"
				+ "CLIENTE: EXPORTADORA AGRÍCOLA CARCON, PLANIM LOGÍSTICA.\r\n"
				+ "CONSULTORIA ALOCADORA: MBM SYSTEMS.\r\n"
				+ "PERÍODO: 04/2009 a 06/2010\r\n"
				+ "PROJETO: CARCON.\r\n"
				+ "REGRA DE NEGÓCIO: Exportação agrícola.\r\n"
				+ "OBJETIVO: Prover software web com a finalidade de gerir todo ciclo da cadeia de exportação de algodão.\r\n"
				+ "FERRAMENTAS:  C sharp,Visual Studio, Linq, Visual source Safe, SQL Server, Delphi 2009.\r\n"
				+ "CARGO: Analista / programador Junior\r\n"
				+ "ATIVIDADES:  Construção de front e back end para módulos do sistema de exportação agrícola da Carcon\r\n"
				+ "\r\n"
				+ "\r\n"
				+ "\r\n"
				+ "\r\n"
				+ "");
		System.out.println(skillsFromText);
	}

	static void countWords() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll();
		Set<String> words = new HashSet<>();
		Consumer<CcpJsonRepresentation> consumer = json -> {
			List<CcpJsonRepresentation> skills = json.getDynamicVersion().getAsJsonList("skill");
			for (CcpJsonRepresentation skill : skills) {
				String word = skill.getDynamicVersion().getAsString("word");
				words.add(word);
			}
		};
		queryExecutor.consumeQueryResult(query, new String[] {"group_positions_by_skills"}, "1m", 10_000, consumer, "skill");
		System.out.println(words.size());
	}


	static Set<String> getOtherWords(String word){
		String[] split = word.split(" ");
		if(split.length != 2) {
			List<String> asList = Arrays.asList(word);
			HashSet<String> hashSet = new HashSet<>(asList);
			return hashSet;
		}
		
		String r1 = word.replace(" ", "");
		String r2 = word.replace(" ", "-");
		String r3 = word.replace(" ", ".");
		
		Set<String> response = new HashSet<>( Arrays.asList(r1, r2, r3));
		String secondPiece = split[1];
		
		boolean longNumber = new CcpStringDecorator(secondPiece).isLongNumber();
		
		if(longNumber) {
			return response;
		}
		String firstPiece = split[0];
		
		String reverse = secondPiece + " " + firstPiece;
		
		response.add(reverse);
		
		return response;
	}	
	
	static Set<String> getAllWords(List<String> lines){
		List<Set<String>> collect = lines.stream().map(x -> Arrays.asList(x.split(",")).stream().map(y -> y.trim().toUpperCase()).filter(y -> y.length() > 1).collect(Collectors.toSet())).collect(Collectors.toList());
		Set<String> allWords = new HashSet<>();
		for (Set<String> set : collect) {
			allWords.addAll(set);
		}
		return allWords;
	}
	
	static void saveSynonyms() {
		String folder = "C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\database\\elasticsearch\\";
		List<CcpJsonRepresentation> asJsonList = new CcpStringDecorator(folder+ "report_skills.json").file().asJsonList();
		List<String> lines = new CcpStringDecorator(folder+ "synonyms.txt").file().getLines();
		CcpFileDecorator synonyms2 = new CcpStringDecorator(folder+ "synonyms2.txt").file().reset();
		List<Set<String>> collect = lines.stream().map(x -> Arrays.asList(x.split(",")).stream().map(y -> y.trim().toUpperCase()).filter(y -> y.length() > 1).collect(Collectors.toSet())).collect(Collectors.toList());
		List<String> allSynonyms = new ArrayList<>(lines);
		for (CcpJsonRepresentation json : asJsonList) {
			CcpDynamicJsonRepresentation dv = json.getDynamicVersion();
			String skill = dv.getAsString("skill");
			boolean skillFound = false;
			for (Set<String> set : collect) {
				skillFound = set.contains(skill);
				if(skillFound) {
					break;
				}
			}
			if(false == skillFound) {
				allSynonyms.add(skill);
			}
		}
		
		allSynonyms.sort((a, b) -> a.compareTo(b));
		
		for (String string : allSynonyms) {
			String trim = string.toUpperCase().trim();
			if(trim.length() < 2) {
				continue;
			}
			synonyms2.append(trim);
		}
	}
	
	static Set<String> getOtherWords(Set<String> otherWords){
		Set<String> response = new HashSet<>();
		for (String word : otherWords) {
			if("JAVAGRAPHQL".equals(word)) {
				System.out.println();
			}
			Set<String> otherWords2 = getOtherWords(word);
			response.addAll(otherWords2);
		}
		
		return response;
	}
	
	static void saveSkills() {
		String folder = "C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\database\\elasticsearch\\";
		List<String> lines = new CcpStringDecorator(folder+ "ajustes_synonyms.txt").file().getLines();
		List<List<String>> filtered = lines
		.stream()
		.filter(x -> x.startsWith("adicionarParent="))
		.map(x-> x.split("=")[1])
		.map(x -> x.split(","))
		.map(x -> Arrays.asList(x).stream().map(y -> y.trim().toUpperCase()).filter(y -> false == y.isEmpty()).collect(Collectors.toList()))
		.collect(Collectors.toList())
		;
		
		HashSet<String> skills = new HashSet<>();
		
		for (List<String> list : filtered) {
			skills.addAll(list);
		}
		
		List<CcpJsonRepresentation> report = skills
		.stream()
		.map(x -> CcpOtherConstants.EMPTY_JSON.getDynamicVersion().put("skill", x))
		.map(x -> x.getDynamicVersion().put("childrenCount", new ArrayList<>( filtered).stream().filter(skillsNestaLinha -> skillsNestaLinha.indexOf(x.getDynamicVersion().getAsString("skill")) > 0).count()))
		.map(x -> {
			Optional<List<String>> findFirst = new ArrayList<>( filtered).stream().filter(skillsNestaLinha -> skillsNestaLinha.indexOf(x.getDynamicVersion().getAsString("skill")) == 0).findFirst();
			boolean hasNoParent = false == findFirst.isPresent();
			if(hasNoParent) {
				return x;
			}
			List<String> list = findFirst.get();
			List<String> parent = list.subList(1, list.size());
			CcpJsonRepresentation put = x.getDynamicVersion().put("parent", parent);
			return put;
		})
		.map(x -> x.getDynamicVersion().put("hasNoParent", new ArrayList<>( filtered).stream().allMatch(skillsNestaLinha -> skillsNestaLinha.indexOf(x.getDynamicVersion().getAsString("skill")) != 0)))
		.collect(Collectors.toList());
		
		
		Comparator<? super CcpJsonRepresentation> sorter = getSorter("hasRepeatedParent", "hasSkillsWithCommonParentsSize", "hasMirror", "childrenCount", "skill");
		
		List<CcpJsonRepresentation> collect = report
		.stream()
		.map(x -> x.getDynamicVersion().put("mirror", getSynonym(x, report)))
		.map(x -> x.getDynamicVersion().put("hasMirror", false == x.getDynamicVersion().getAsString("mirror").isEmpty()))
		.map(x -> getSkillsWithCommonParentsSize(x, report))
		.collect(Collectors.toList());
		
		
		CcpFileDecorator reportFile = new CcpStringDecorator(folder+ "report_skills.json").file().reset();
		
		List<CcpJsonRepresentation> newList = new ArrayList<>();
		
		List<String> synonyms3 = new CcpStringDecorator(folder+ "synonyms3.txt").file().getLines();

		List<Set<String>> synonyms = synonyms3.stream()
				.map(x -> Arrays.asList(x.split(",")).stream()
						.map(y -> y.trim().toUpperCase())
						.filter(y -> y.length() > 1)
						.filter(y -> y.length() < 35)
						.collect(Collectors.toSet()))
//				.map(x -> getOtherWords(x))
				.collect(Collectors.toList());
		
		
		
		
		for (CcpJsonRepresentation json : collect) {
			List<String> allParents = new ArrayList<>();
			getAllParents(allParents, report, json);
			
			HashSet<String> set = new HashSet<String>(allParents);
			boolean hasRepeatedParent = set.size() != allParents.size();
			CcpJsonRepresentation put = json.getDynamicVersion()
					.put("allParents", allParents.stream()
//					.map(skill -> CcpOtherConstants.EMPTY_JSON.getDynamicVersion().put("skill",skill)
//							.getDynamicVersion()
//							.put("parent", getParent(skill, report))
//							)
					.collect(Collectors.toList())
					).getDynamicVersion()
					.put("hasRepeatedParent", hasRepeatedParent);
					
					;
			
			String skill = put.getDynamicVersion().getAsString("skill");
			
			List<Set<String>> foundSynonyms = synonyms.stream()
					.filter(x -> x.stream().anyMatch(y -> y.trim().equals(skill)))
					.collect(Collectors.toList());
			
			if(foundSynonyms.isEmpty()) {
				throw new RuntimeException(skill + " has no synonyms");
			}
			
			if(foundSynonyms.size() > 1) {
				throw new RuntimeException(skill + " has more than one synonym: " + foundSynonyms);
			}

			List<CcpJsonRepresentation> foundSynonym = foundSynonyms.get(0).stream()
					.filter(x -> false == x.equals(skill))
					.map(x -> CcpOtherConstants.EMPTY_JSON.getDynamicVersion().put("skill", x)).collect(Collectors.toList());
			
			CcpJsonRepresentation withSynonym = put
					.getDynamicVersion().put("synonym", foundSynonym)
//					.getDynamicVersion().getJsonPiece(
//					"skill", "childrenCount"
//					, "parent"
//					)
					.getDynamicVersion().renameField("parent", "directParent")
					.getDynamicVersion().renameField("allParents", "parent")
					.getDynamicVersion().getJsonPiece("parent", "skill", "directParent", "childrenCount", "synonym", "hasNoParent")
					;
			
			newList.add(withSynonym);	
		}
		
		newList.sort(sorter);
		reportFile.append(newList.toString());
	}
	
	static List<String> getParent(String skill, List<CcpJsonRepresentation> report){
		return report.stream().map(x -> x.getDynamicVersion()).filter(x -> skill.equals(x.getAsString("skill"))).findFirst().get()
				.getAsStringList("parent");
	}
	static Comparator<? super CcpJsonRepresentation> getSorter(String... fields){
		Comparator<? super CcpJsonRepresentation> sorter = (a, b) -> {
			
			for (String field : fields) {
				CcpDynamicJsonRepresentation dv1 = a.getDynamicVersion();
				CcpDynamicJsonRepresentation dv2 = b.getDynamicVersion();
				CcpStringDecorator sd1 = dv1.getAsStringDecorator(field);
			
				if(sd1.isLongNumber()) {
					Integer int2 = dv2.getAsIntegerNumber(field);
					Integer int1 = dv1.getAsIntegerNumber(field);
					int subtration = int2 - int1;
					if(subtration == 0) {
						continue;
					}
					return subtration;
				}
				
				var b1 = dv1.getAsString(field);
				var b2 = dv2.getAsString(field);

				if(sd1.isBoolean()) {
					int compareTo = b2.compareTo(b1);
					if(compareTo == 0) {
						continue;
					}
					return compareTo;
				}
				int compareTo = b1.compareTo(b2);
				if(compareTo == 0) {
					continue;
				}
				return compareTo;
			}
			
			return 0;
		};
		return sorter;
	}

	
	static CcpJsonRepresentation getSkillsWithCommonParentsSize(CcpJsonRepresentation json, List<CcpJsonRepresentation> report) {
		List<String> collect = report.stream().map(x -> x.getDynamicVersion())
		.filter(x -> false == x.getAsString("skill").equals(json.getDynamicVersion().getAsString("skill")))
		.map(x -> x.put("commonParents", getCommonParents(x.json, json)).getDynamicVersion())
		.filter(x -> x.getAsStringList("commonParents").size() > 1)
		.map(x -> x.getAsString("skill"))
		.collect(Collectors.toList());
		CcpJsonRepresentation put = json.getDynamicVersion().put("hasSkillsWithCommonParentsSize", false == collect.isEmpty()).getDynamicVersion()
				.put("skillsWithCommonParents", collect);
		return put;
	}
	
	static List<String> getCommonParents(CcpJsonRepresentation json1, CcpJsonRepresentation json2) {
		List<String> parent1 = json1.getDynamicVersion().getAsStringList("parent");
		List<String> parent2 = json2.getDynamicVersion().getAsStringList("parent");
		List<String> intersectList = new CcpCollectionDecorator(parent1).getIntersectList(parent2);
		return intersectList;
	}
	
	
	static List<String> getAllParents(List<String> allParents, List<CcpJsonRepresentation> report, CcpJsonRepresentation json){
		
		List<String> parents = json.getDynamicVersion().getAsStringList("parent");
		
		String skill = json.getDynamicVersion().getAsString("skill");
		System.out.println(skill + ": " + parents);
		allParents.addAll(parents);
		for (String parent : parents) {
			CcpJsonRepresentation parentJson = report.stream().map(x -> x.getDynamicVersion())
			.filter(x -> parent.equals(x.getAsString("skill")))
			.map(x -> x.json)
			.findFirst()
			.get();
			getAllParents(allParents, report, parentJson);
		}
		return allParents;
	}

	static String getSynonym(CcpJsonRepresentation json, List<CcpJsonRepresentation> report) {
		List<String> parent = json.getDynamicVersion().getAsStringList("parent");
		if(parent.size() != 1) {
			return "";
		}
		String parentName = parent.get(0);
		String orElseGet = new ArrayList<>(report)
		.stream()
		.map(x -> x.getDynamicVersion())
		.filter(x -> x.getAsString("skill").equals(parentName))
		.filter(x -> x.getAsIntegerNumber("childrenCount") == 1)
		.map(x -> x.getAsString("skill"))
		.findFirst()
		.orElseGet(() -> "");
		
		return orElseGet;
	}
	
	static void getMissingWords() {
		HashSet<String> todos = new HashSet<>();
		List<List<String>> collect = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\database\\elasticsearch\\ajustes_synonyms.txt").file().getLines()
		.stream().filter(x -> x.startsWith("adicionarParent="))
		.map(x -> x.trim().split("=")[1])
		.map(x -> x.split(","))
		.map(x -> Arrays.asList(x).stream().map(y -> y.trim().toUpperCase()).collect(Collectors.toList()))
		.collect(Collectors.toList());
		
		for (List<String> list : collect) {
			todos.addAll(list);
		}
		CcpFileDecorator synonymsFile = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\skills\\synonyms.json").file();
		List<CcpJsonRepresentation> synonyms = new ArrayList<CcpJsonRepresentation>(synonymsFile.asJsonList());
		
		
		int ajustadas = 0;
		List<CcpJsonRepresentation> missing = new ArrayList<>();
		for (CcpJsonRepresentation json : synonyms) {
			
			CcpDynamicJsonRepresentation dynamicVersion = json.getDynamicVersion();
			String skill = dynamicVersion.getAsString("skill");
			
			
			if(todos.contains(skill)) {
				ajustadas ++;
				continue;
			}
			List<CcpJsonRepresentation> asJsonList = dynamicVersion.getAsJsonList("synonym");
			boolean anyMatch = asJsonList.stream().map(x -> x.getDynamicVersion().getAsString("skill")).anyMatch(x -> todos.contains(x));
			
			if(anyMatch) {
				ajustadas ++;
				continue;
			}
			missing.add(json);
		}
		List<String> removidas = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\database\\elasticsearch\\removidas.txt").file().getLines().stream().map(x -> x.trim().split(" = ")[0]).collect(Collectors.toList());
		int estudar = 0;
		for (var copy : missing) {
			String skill = copy.getDynamicVersion().getAsString("skill");
			if(removidas.contains(skill)) {
				continue;
			}
			System.out.println(skill);
			estudar++;
		}
		System.out.println("INICIO: " + synonyms.size());
		System.out.println("TODOS: " + todos.size());
		System.out.println("REMOVIDAS: " + removidas.size());
		System.out.println("AJUSTADAS: " + ajustadas);
		System.out.println("ESTUDAR: " + estudar);
	}

		static void aumentarArquivoDeSinonimos() {
		CcpFileDecorator synonymsFile = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\skills\\synonyms.json").file();
		CcpFileDecorator countByWords = new CcpStringDecorator("c:/logs/skills/countByWords.txt").file();
		List<String> existingWords = countByWords.getLines().subList(0, 1408).stream().map(x -> x.split(" = ")[0]).collect(Collectors.toList());
		List<CcpJsonRepresentation> synonyms = new ArrayList<CcpJsonRepresentation>(synonymsFile.asJsonList());
		System.out.println(synonyms.size());

		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll();
		Set<String> words = new HashSet<>();
		Consumer<CcpJsonRepresentation> consumer = json -> {
			String[] fields = new String[] {"requisitosDesejaveis", "requisitosObrigatorios", "must", "should"};
			for (String field : fields) {
				List<String> list = json.getDynamicVersion().getAsStringList(field).stream()
						.map(x -> new CcpStringDecorator(x).text().stripAccents().sanitize().getContent().toUpperCase().trim())
						.filter(x -> x.length() > 2 && x.length() <= 35)
						.filter(x -> false == existingWords.contains(x))
						.collect(Collectors.toList());
				
				words.addAll(list);
				
			}
			System.out.println(counter++ + " = " + words.size());
		};
		queryExecutor.consumeQueryResult(query, new String[] {"pesquisa_curriculos"}, "1m", 10_000, consumer, "requisitosDesejaveis", "requisitosObrigatorios", "must", "should");
		

		for (String word : words) {
			CcpJsonRepresentation synonym = CcpOtherConstants.EMPTY_JSON.getDynamicVersion().put("skill", word);
			synonyms.add(synonym);
		}
		System.out.println(synonyms.size());
		synonymsFile.reset().append(synonyms.toString());
	}

	static void addNewWords() {
		CcpFileDecorator synonymsFile = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\skills\\synonyms.json").file();
		List<CcpJsonRepresentation> synonyms = synonymsFile.asJsonList();
		Set<String> allSimilar = new HashSet<>();
		for (CcpJsonRepresentation synonym : synonyms) {
			var similar = synonym.getDynamicVersion().getAsJsonList("similar")
					.stream()
					.map(x -> x.getDynamicVersion().getAsString("word").replace("_", " "))
					.collect(Collectors.toList())
					;
			allSimilar.addAll(similar);
		}
		System.out.println(allSimilar.size());
		for (CcpJsonRepresentation synonym : synonyms) {
			{
				String skill = synonym.getDynamicVersion().getAsString("skill");
				allSimilar.remove(skill);
			}
			List<String> parents = synonym.getDynamicVersion().getAsStringList("parent");
			for (String parent : parents) {
				allSimilar.remove(parent);
			}
			
			List<CcpJsonRepresentation> asJsonList = synonym.getDynamicVersion().getAsJsonList("synonym");
			for (var sym : asJsonList) {
				String skill = sym.getDynamicVersion().getAsString("skill");
				allSimilar.remove(skill);
				
			}
		}
		System.out.println(allSimilar.size());
		var newWords = new CcpStringDecorator("c:/logs/skills/newWords.txt").file().reset();
		var countByWords = new CcpStringDecorator("c:/logs/skills/countByWords.txt").file().getLines();
		
		for (String similar : allSimilar) {
			String string = countByWords.stream().filter(x -> x.startsWith(similar)).findFirst().get();
			newWords.append(string);
		}
	
	}
	
	static void sanitizarArquivoDeSinonimos() {
		List<String> removidas = new CcpStringDecorator("c:/logs/skills/removidas.txt").file().getLines().stream().map(x -> x.trim().split(" = ")[0]).collect(Collectors.toList());
		CcpFileDecorator synonymsFile = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\skills\\synonyms.json").file();
		List<CcpJsonRepresentation> synonyms = synonymsFile.asJsonList();
		List<CcpJsonRepresentation> newSynonyms = new ArrayList<>();
		
		for (CcpJsonRepresentation synonym : synonyms) {
			
				String skill = synonym.getDynamicVersion().getAsString("skill");
				if(skill.trim().length() < 2) {
					synonym = synonym.getDynamicVersion().removeField("skill");
				}
				if(skill.trim().length() > 35) {
					synonym = synonym.getDynamicVersion().removeField("skill");
				}
				if(removidas.contains(skill)) {
					synonym = synonym.getDynamicVersion().removeField("skill");
				}
				List<String> parent = synonym.getDynamicVersion().getAsStringList("parent").stream()
						.filter(x -> x.length() > 2 && x.length() <= 35)
						.filter(x -> false == removidas.contains(x))
						.collect(Collectors.toList());
				
				
				synonym = synonym.getDynamicVersion().put("parent", parent);
				{
					List<CcpJsonRepresentation> collect = synonym.getDynamicVersion().getAsJsonList("synonym").stream().filter(x -> {
						String word = x.getDynamicVersion().getAsString("skill");
						return word.length() > 2 && word.length() <= 35 && false == removidas.contains(word) && false == skill.equals(word);
					}).collect(Collectors.toList());
					
					synonym = synonym.getDynamicVersion().put("synonym", collect);
				}
				{
					List<CcpJsonRepresentation> collect = synonym.getDynamicVersion().getAsJsonList("preRequisite").stream().filter(x -> {
						String word = x.getDynamicVersion().getAsString("word");
						return word.length() > 2 && word.length() <= 35 && false == removidas.contains(word);
					}).collect(Collectors.toList());
					
					synonym = synonym.getDynamicVersion().put("preRequisite", collect);
				}
				{
					List<CcpJsonRepresentation> collect = synonym.getDynamicVersion().getAsJsonList("similar").stream().filter(x -> {
						String word = x.getDynamicVersion().getAsString("word").replace("_", " ");
						return word.length() > 2 && word.length() <= 35 && false == removidas.contains(word);
					}).collect(Collectors.toList());
					
					synonym = synonym.getDynamicVersion().put("similar", collect);
				}
				
				newSynonyms.add(synonym);
			}

		List<CcpJsonRepresentation> collect = newSynonyms.stream()
		.filter(x -> x.getDynamicVersion().containsAllFields("skill") || false == x.getDynamicVersion().getAsJsonList("synonym").isEmpty())
		.map(x -> x.getDynamicVersion().containsAllFields("skill") ? x : transferSynonymToSkill(x))
		.map(x -> false == x.getDynamicVersion().getAsObjectList("parent").isEmpty() ?  x :  x.getDynamicVersion().removeField("parent"))
		.map(x -> false == x.getDynamicVersion().getAsObjectList("similar").isEmpty() ?  x :  x.getDynamicVersion().removeField("similar"))
		.map(x -> false == x.getDynamicVersion().getAsObjectList("synonym").isEmpty() ?  x :  x.getDynamicVersion().removeField("synonym"))
		.map(x -> false == x.getDynamicVersion().getAsObjectList("preRequisite").isEmpty() ?  x :  x.getDynamicVersion().removeField("preRequisite"))
		.collect(Collectors.toList());
		CcpFileDecorator newSynonymsFile = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\skills\\new_synonyms.json").file().reset();
		newSynonymsFile.append(collect.toString());
		for (CcpJsonRepresentation json : collect) {
			System.out.println(json.getDynamicVersion().getAsString("skill"));
		}
		System.out.println(collect.size());
	}
	
	static CcpJsonRepresentation transferSynonymToSkill(CcpJsonRepresentation x) {
		List<CcpJsonRepresentation> synonym = x.getDynamicVersion().getAsJsonList("synonym");
		ArrayList<CcpJsonRepresentation> list = new ArrayList<>(synonym);
		list.sort((a,b) ->  b.getDynamicVersion().getAsIntegerNumber("positionsCount") - a.getDynamicVersion().getAsIntegerNumber("positionsCount"));
		String skill = list.remove(0).getDynamicVersion().getAsString("skill");
		CcpJsonRepresentation put = x.getDynamicVersion().put("skill", skill).getDynamicVersion().put("synonym", list);
		return put;
	}
	
	static List<String> getAllWords() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll().setSize(10000);
		List<CcpJsonRepresentation> resultAsList = queryExecutor.getResultAsList(query, new String[] {"group_positions_by_skills"}, "skill");
		
		Set<String> set = new HashSet<>();
		for (CcpJsonRepresentation json : resultAsList) {
			List<CcpJsonRepresentation> list = json.getDynamicVersion().getAsJsonList("skill");
			List<String> collect = list.stream().map(x -> x.getDynamicVersion().getAsString("word")).collect(Collectors.toList());
			set.addAll(collect);
		}
		ArrayList<String> list = new ArrayList<>(set);
		list.sort((a,b) -> a.length() - b.length());
		return list;
	}

	static void relatoriosDasSkillsNosCurriculos() {
		CcpTextExtractor textExtractor = CcpDependencyInjection.getDependency(CcpTextExtractor.class);
		
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll();
		Map<String, Integer> countByResume = new HashMap<>();
		Map<String, Integer> countByWords = new HashMap<>();
		Map<String, Set<String>> groupedResumes = new HashMap<>();
		
		Consumer<CcpJsonRepresentation> consumer = json -> {
			try {
				String base64 = json.getDynamicVersion().getValueFromPath("", "curriculo", "conteudo");
				
				String resumeText = textExtractor.extractText(base64);
				CcpJsonRepresentation md = getSkillsFromText(resumeText);
				
				String id = json.getDynamicVersion().getAsString("id");

				List<CcpJsonRepresentation> skills = md.getDynamicVersion().getAsJsonList("skill").stream()
						.collect(Collectors.toList());
				
				CcpTextDecorator completeLeft = new CcpStringDecorator(""+ skills.size()).text().completeLeft('0', 3);
				
				String fileName = completeLeft +"_" + id + ".json";
				
				for (CcpJsonRepresentation skill : skills) {
					String word = skill.getDynamicVersion().getAsString("word"); 
					Integer counter = countByWords.getOrDefault(word, 0) + 1;
					countByWords.put(word, counter);
					Set<String> orDefault = groupedResumes.getOrDefault(word, new HashSet<>());
					orDefault.add(id);
					groupedResumes.put(word, orDefault);
				}
				
				countByResume.put(id, skills.size());
				
				new CcpStringDecorator("c:/logs/skills/" + fileName).file().reset().append(md.getDynamicVersion().put("skill", skills).asPrettyJson());
				System.out.println(counter++ + " = " + fileName);
				
			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println(counter++);
			}
		
		};
		queryExecutor.consumeQueryResult(query, new String[] {"profissionais2"}, "1m", 10, consumer, "curriculo.conteudo", "id");
		List<String> allWords = getAllWords();

		createReport(countByWords, "c:/logs/skills/countByWords.txt",  word -> {
			allWords.remove(word);
		});
		
		createReport(countByResume, "c:/logs/skills/countByResume.txt",  word -> {});
		
		CcpFileDecorator removidas = new CcpStringDecorator("c:/logs/skills/removidas.txt").file().reset();
		for (String removedWord : allWords) {
			removidas.append(removedWord);
		}
		CcpFileDecorator groupedResumesFile = new CcpStringDecorator("c:/logs/skills/groupedResumes.txt").file().reset();
		Set<String> skills = groupedResumes.keySet();
		for (String skill : skills) {
			groupedResumesFile.append(skill + "=" + groupedResumes.get(skill));
		}
	}

	static CcpJsonRepresentation getSkillsFromText(String resumeText) {
		String text = new CcpStringDecorator(resumeText).text().stripAccents().getContent();
		Map<String, Object> sessionValues =  CcpOtherConstants.EMPTY_JSON
				 .getDynamicVersion()
				 .put("text", text)
				 .content
				 ;
		Map<String, Object> execute = VisServiceSkills.GetSkillsFromText.execute(sessionValues);

		
		CcpJsonRepresentation md = new CcpJsonRepresentation(execute);
		return md;
	}
	
	static void createReport(Map<String, Integer> reportSource, String reportFile, Consumer<String> consumer) {
		
		ArrayList<String> list = new ArrayList<String>(reportSource.keySet());
		list.sort((a, b) -> reportSource.get(b) - reportSource.get(a));
		CcpFileDecorator report = new CcpStringDecorator(reportFile).file().reset();
		Integer total = 0;
		Map<Integer, Integer> numbers = new TreeMap<>();
		for (String word : list) {
			Integer count = reportSource.get(word);
			report.append(word + " = " + count);
			consumer.accept(word);
			total += count;
			Integer orDefault = numbers.getOrDefault(count, 0) + 1;
			numbers.put(count, orDefault);
		}
		Integer average = total / list.size();
		
		report.append("AVERAGE = " + average);
		
		Set<Integer> keySet = numbers.keySet();
		for (Integer integer : keySet) {
			Integer value = numbers.get(integer);
			report.append(integer + " = " + value);
		}
		
	}
	
	
	static void saveGroupedCompanies() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll();
		
		Consumer<CcpJsonRepresentation> consumer = json -> {
			String x = json.getDynamicVersion().getAsString("id");
				String[] split = x.split("@");
				if(split.length != 2) {
					return;
				}
				
				
			String domain = split[1];
			
			String[] split1 = domain.split("\\.");			

			String companyName = split1[0].toUpperCase().trim();
			
			if(companyName.length() < 3) {
				return;
			}
			
			String capitalizedCompanyName = new CcpStringDecorator(companyName).text().capitalize().content;
			
			String initials = companyName.substring(0, 3);
			
			LinkedHashSet<String> orDefault = groupedCompanies.getDynamicVersion().getOrDefault(initials, new LinkedHashSet<>());
			orDefault.add(capitalizedCompanyName);
			groupedCompanies = groupedCompanies.getDynamicVersion().put(initials, orDefault);
		};
		queryExecutor.consumeQueryResult(query, new String[] {"old_recruiters"}, "1s", 10000, consumer, "id");

		ArrayList<String> arrayList = new ArrayList<> (groupedCompanies.fieldSet());
		arrayList.sort((a, b) ->{
			Set<String> set1 = groupedCompanies.getDynamicVersion().getAsObject(a);
			Set<String> set2 = groupedCompanies.getDynamicVersion().getAsObject(b);
			return set2.size() - set1.size();
		});
		
		CcpJsonRepresentation result = CcpOtherConstants.EMPTY_JSON;
		int total = 0;
		for (String string : arrayList) {
			Set<String> value = groupedCompanies.getDynamicVersion().getAsObject(string);
			result = result.getDynamicVersion().put(string, value);
			int size = value.size();
			System.out.println(string +" = " +  size);
			total += size;
		}
		System.out.println(total);
		new CcpStringDecorator("c:\\logs\\teste.json").file().reset().append(result.asPrettyJson());
	}

	static void saveResume() {
		VisEntityResume.ENTITY.delete(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, "onias85@gmail.com"));
		CcpHttpHandler http = new CcpHttpHandler(200, CcpOtherConstants.DO_NOTHING);
		String path = "http://localhost:9200/profissionais2/_doc/onias85@gmail.com/_source";
		CcpHttpMethods method = CcpHttpMethods.GET;
		CcpJsonRepresentation headers = CcpOtherConstants.EMPTY_JSON;
		String asUgglyJson = "";
		CcpHttpResponse response = http.ccpHttp.executeHttpRequest(path, method, headers, asUgglyJson);
		CcpJsonRepresentation asSingleJson = response.asSingleJson();
		ImportResumeFromOldJobsNow.INSTANCE.accept(asSingleJson);
	}

	static void transferToReverseEntity() {
		CcpJsonRepresentation json = new CcpJsonRepresentation("{\r\n"
				+ "    \"userAgent\": \"Apache-HttpClient/4.5.4 (Java/17.0.9)\",\r\n"
				+ "    \"ip\": \"127.0.0.1\",\r\n"
				+ "    \"email\": \"exhhi8gp@teste.com\",\r\n"
				+ "    \"password\": \"Jobsnow1!\",\r\n"
				+ "    \"token\": \"4ISALLOL\"\r\n"
				+ "  }");
		JnEntityLoginToken.ENTITY.save(json);
		JnEntityLoginToken.ENTITY.transferToReverseEntity(json);
	}

	static void saveLoginToken() {
		String value = "12345678";
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityLoginToken.Fields.email, "onias85@gmail.com")
				.put(JnEntityLoginToken.Fields.token, value)
				.put(JnEntityLoginToken.Fields.ip, "127.0.0.1")
				.put(JnEntityLoginToken.Fields.userAgent, "teste");
		JnEntityLoginToken.ENTITY.save(json);
		CcpJsonRepresentation oneById = JnEntityLoginToken.ENTITY.getOneById(json);
		String token = oneById.getAsString(JnEntityLoginToken.Fields.token);
		CcpPasswordHandler dependency = CcpDependencyInjection.getDependency(CcpPasswordHandler.class);
		System.out.println(dependency.matches(value, token));
	}

	enum JsonFieldNames implements CcpJsonFieldName{
		type, cause, stackTrace, email, mappings, properties, name, ddd, _id, docs, _source, id, mail, contato, vaga, channel, description, contactChannel, candidate, candidato
	}
	static {
		CcpDependencyInjection.loadAllDependencies(
				new CcpElasticSearchQueryExecutor(), 
				new CcpElasticSearchDbRequest(),
				CcpLocalInstances.mensageriaSender,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				new CcpApacheTikaTextExtractor(),
				new CcpMindrotPasswordHandler(), 
				new CcpElasticSerchDbBulk(), 
				CcpLocalCacheInstances.map,
				new CcpElasticSearchCrud(), 
				new CcpGsonJsonHandler(), 
				new CcpApacheMimeHttp()
				);
	}

	static Map<String, Object> getJson(CcpFileDecorator arquivo){
		boolean file = arquivo.isFile();
		Map<String, Object> json = new LinkedHashMap<>();
		
		if(file) {
			CcpJsonRepresentation asSingleJson = arquivo.asSingleJson();
			return asSingleJson.content;
		}
		CcpFolderDecorator asFolder = arquivo.asFolder();
		asFolder.readFiles(subFile -> {
			Map<String, Object> subJson = getJson(subFile);
			json.put(subFile.getName().replace(".json", ""), subJson);
		});
		return json;
	}
	
	 static void qualquerCoisa() {
		Field[] declaredFields = JnEntityJobsnowError.Fields.class.getDeclaredFields();
		for (Field field : declaredFields) {
			System.out.println(field.getName());
		}
	}

	static void mudarLocalDoArquivo() {
		CcpFolderDecorator folder = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\github").folder();

		folder.readFiles(file -> {
			String oldName = file.content + "\\.gitignore";
			String newName = oldName.replace("github", "");
			new CcpStringDecorator(oldName).file().rename(newName);
		});
	}

	static void testarExpurgable2() {
		CcpJsonRepresentation json = new CcpJsonRepresentation("{\r\n" + "  \"email\": \"onias85@gmail.com\",\r\n"
				+ "  \"ip\": \"127.0.0.1\",\r\n" + "  \"password\": \"Jobsnow1!\",\r\n"
				+ "  \"token\": \"M6ZRDQ83\",\r\n" + "  \"originalToken\": \"M6ZRDQ83\",\r\n"
				+ "  \"userAgent\": \"Apache-HttpClient/4.5.4 (Java/17.0.9)\"\r\n" + "}");
		testarExpurgable(json, JnEntityLoginSessionValidation.ENTITY);
	}

	static void testarExpurgable() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.type, "teste")
				.put(JsonFieldNames.stackTrace, "teste")
				.put(JsonFieldNames.cause, "teste");
		CcpEntity entity = JnEntityJobsnowError.ENTITY;
		testarExpurgable(json, entity);
	}

	private static void testarExpurgable(CcpJsonRepresentation json, CcpEntity entity) {
		entity.save(json);
		CcpJsonRepresentation[] jsons = new CcpJsonRepresentation[] { json };
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		for (int k = 0; k < 60; k++) {
			CcpTimeDecorator ctd = new CcpTimeDecorator();
			String formattedDateTime = ctd.getFormattedDateTime("dd/MM/yyyy HH:mm:ss.SSS");
			boolean exists = entity.exists(json);
			System.out.println("Exists: " + formattedDateTime + " = " + exists);
			CcpSelectUnionAll unionAll = crud.unionAll(jsons, JnDeleteKeysFromCache.INSTANCE, entity);
			boolean presentInThisUnionAll = entity.isPresentInThisUnionAll(unionAll, json);
			System.out.println("unionAll1: " + formattedDateTime + " = " + presentInThisUnionAll);
			ctd.sleep(1000);
		}

	}

	static void testarSalvamentoDeSenha() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityLoginPassword.Fields.password, "123456").put(JsonFieldNames.email, "onias85@gmail.com");
		JnEntityLoginPassword.ENTITY.save(json);

		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpSelectUnionAll unionAll = crud.unionAll(new CcpJsonRepresentation[] { json }, JnDeleteKeysFromCache.INSTANCE,
				JnEntityLoginPassword.ENTITY);
		System.out.println(unionAll);

		System.out.println(com.ccp.dependency.injection.CcpDependencyInjection
				.getDependency(com.ccp.especifications.password.CcpPasswordHandler.class)
				.matches("123456", "$2a$12$mjfndltYxA2TsM9Eo8rnSOaNCr3QTTerfoVcj5ucAGO5C/vavQofC"));
		System.out.println(com.ccp.dependency.injection.CcpDependencyInjection
				.getDependency(com.ccp.especifications.password.CcpPasswordHandler.class)
				.matches("123456", "$2a$12$FYwjF4ysRKHCwg9cp1H/meLBRLeevbDlT5ZQvoSGQX6D1osAtWVde"));
	}

	static void extracted() {
		CcpFolderDecorator folder = new CcpStringDecorator(
				"documentation/database/elasticsearch/scripts/entities/create").folder();
		Map<String, List<String>> map = new TreeMap<>();
		folder.readFiles(file -> {
			String fileName = file.getName();
			String javaClassName = "JnEntity" + new CcpStringDecorator(fileName).text().toCamelCase().toString();
			CcpJsonRepresentation json = file.asSingleJson().getInnerJsonFromPath(JsonFieldNames.mappings, JsonFieldNames.properties);
			Set<String> fields = json.fieldSet();
			for (String field : fields) {
				List<String> javaClassesName = map.getOrDefault(field, new ArrayList<>());
				javaClassesName.add(javaClassName);
				map.put(field, javaClassesName);
			}
		});
		CcpFileDecorator reset = new CcpStringDecorator("c:\\logs\\fields.txt").file().reset();
		Set<String> keySet = map.keySet();
		for (String string : keySet) {
			List<String> list = map.get(string);
			reset.append(string + " = " + list);
		}
	}

	static void testarValidacoes() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.name, "L")
				.put(JsonFieldNames.ddd, 20);
		try {
			VisEntityResume.ENTITY.save(json);
		} catch (CcpJsonValidationError e) {
			new CcpStringDecorator("C:\\logs\\errosDeCurriculo.json").file().reset().append(e.json.asPrettyJson());
		}
	}

	static void metodoDoLucas() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.email, "onias85@gmail.com")

		;
		List<CcpJsonRepresentation> parametersToSearch = JnEntityLoginSessionValidation.ENTITY
				.getParametersToSearch(json);

		System.out.println(parametersToSearch);

		JnEntityLoginSessionValidation.ENTITY.getTwinEntity().save(json);
		JnBusinessExecuteLogout.INSTANCE.apply(json);
	}

	static void testarExpurgableEntity() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames.cause, new CcpJsonRepresentation("{'nome':'onias'}")).put(JsonFieldNames.stackTrace, "{'nome':'vieira'}")
				.put(JsonFieldNames.type, "any");
		CcpJsonRepresentation oneById = JnEntityJobsnowError.ENTITY.getOneByIdOrHandleItIfThisIdWasNotFound(json,
				CcpOtherConstants.RETURNS_EMPTY_JSON);
		System.out.println(oneById);
	}

	static void criarArquivoDeVagas() {
		CcpQueryOptions queryMatchAll = CcpQueryOptions.INSTANCE.matchAll();
		queryMatchAll.startAggregations().startBucket("x", null, 1).startAggregations().addAvgAggregation(null, null);
		Set<Object> emailsDasVisualizacoes = getEmails(queryMatchAll, "visualizacao_de_curriculo", "email");
		Set<Object> emailsDasVagas = getEmails(queryMatchAll, "vagas", "email", "mail");

		List<Object> intersectList = new CcpCollectionDecorator(emailsDasVisualizacoes)
				.getIntersectList(emailsDasVagas);

		CcpJsonRepresentation mgetJson = CcpOtherConstants.EMPTY_JSON;
		CcpEntityField idField = new CcpEntityField("email", false, CcpOtherConstants.DO_NOTHING);

		CcpQueryOptions queryToSearchViews = CcpQueryOptions.INSTANCE.startSimplifiedQuery()
				.terms(idField, intersectList).endSimplifiedQueryAndBackToRequest();
		Set<Object> candidatos = getEmails(queryToSearchViews, "visualizacao_de_curriculo", "candidato", "candidate");
		CcpJsonRepresentation template = new CcpJsonRepresentation("{\r\n" + "    \"_index\": \"profissionais2\",\r\n"
				+ "    \"_source\": {\r\n" + "        \"include\": [\r\n" + "            \"curriculo.arquivo\",\r\n"
				+ "            \"pretensaoPj\",\r\n" + "            \"pretensaoClt\",\r\n" + "            \"pcd\",\r\n"
				+ "            \"mudanca\",\r\n" + "            \"homeoffice\",\r\n"
				+ "            \"experiencia\",\r\n" + "            \"disponibilidade\",\r\n"
				+ "            \"ddd\",\r\n" + "            \"bitcoin\"\r\n" + "        ]\r\n" + "    }\r\n" + "}");
		for (Object candidato : candidatos) {
			CcpJsonRepresentation doc = template.put(JsonFieldNames._id, candidato);
			mgetJson = mgetJson.addToList(JsonFieldNames.docs, doc);
		}

		CcpHttpResponse executeHttpRequest = CcpDependencyInjection.getDependency(CcpHttpRequester.class)
				.executeHttpRequest("http://localhost:9200/_mget", CcpHttpMethods.POST, CcpOtherConstants.EMPTY_JSON,
						mgetJson.asUgglyJson());
		CcpJsonRepresentation asSingleJson = executeHttpRequest.asSingleJson();

		List<CcpJsonRepresentation> collect = asSingleJson.getAsJsonList(JsonFieldNames.docs).stream().map(json -> {
			String id = json.getAsString(JsonFieldNames._id);
			CcpJsonRepresentation source = json.getInnerJson(JsonFieldNames._source);
			CcpJsonRepresentation put = source.put(JsonFieldNames.id, id);
			return put;
		}).collect(Collectors.toList());

		CcpJsonRepresentation resumes = CcpOtherConstants.EMPTY_JSON;

		for (CcpJsonRepresentation curriculo : collect) {
			String id = curriculo.getAsString(JsonFieldNames.id);
			resumes = resumes.getDynamicVersion().put(id, curriculo);
		}

		CcpJsonRepresentation candidatosAgrupadosPorRecrutadores = getCandidatosAgrupadosPorRecrutadores(queryMatchAll);
		CcpJsonRepresentation vagasAgrupadosPorRecrutadores = getVagasAgrupadosPorRecrutadores(intersectList);
		Set<String> recrutadores = vagasAgrupadosPorRecrutadores.fieldSet();
		List<CcpJsonRepresentation> todasAsVagas = new ArrayList<>();
		CcpJsonRepresentation res = new CcpJsonRepresentation(resumes.content);
		for (String recrutador : recrutadores) {
			List<CcpJsonRepresentation> curriculos = candidatosAgrupadosPorRecrutadores.getDynamicVersion().getAsStringList(recrutador)
					.stream().map(x -> res.getDynamicVersion().getInnerJson(x)).collect(Collectors.toList());

			List<CcpJsonRepresentation> vagas = vagasAgrupadosPorRecrutadores.getDynamicVersion().getAsJsonList(recrutador);

			int k = 0;
			for (CcpJsonRepresentation curriculo : curriculos) {
				CcpJsonRepresentation vaga = vagas.get(k++ % vagas.size());
				CcpJsonRepresentation putAll = vaga.mergeWithAnotherJson(curriculo);
				todasAsVagas.add(putAll);
			}
		}
		CcpFileDecorator vagasFile = new CcpStringDecorator("c:\\logs\\vagas.json").file().reset();
		vagasFile.append(todasAsVagas.toString());
	}

	static class AgruparVagasPorRecrutadores implements java.util.function.Consumer<CcpJsonRepresentation> {

		CcpJsonRepresentation vagasAgrupadasPorRecrutadores = CcpOtherConstants.EMPTY_JSON;

		public void accept(CcpJsonRepresentation json) {

			String recrutador = json.getAsObject(JsonFieldNames.mail);
			String contato = json.getAsString(JsonFieldNames.contato);
			String texto = json.getAsString(JsonFieldNames.vaga);
			String contactChannel = new CcpStringDecorator(contato.trim()).email().isValid() ? "email" : "link";

			CcpJsonRepresentation vaga = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.channel, contato).put(JsonFieldNames.email, recrutador)
					.put(JsonFieldNames.description, texto).put(JsonFieldNames.contactChannel, contactChannel);

			this.vagasAgrupadasPorRecrutadores = this.vagasAgrupadasPorRecrutadores.getDynamicVersion().addToList(recrutador, vaga);
		}

	}

	static class AgruparCandidatosPorRecrutadores implements java.util.function.Consumer<CcpJsonRepresentation> {

		CcpJsonRepresentation candidatosAgrupadosPorRecrutadores = CcpOtherConstants.EMPTY_JSON;

		public void accept(CcpJsonRepresentation json) {
			String candidato = json.getAsObject(JsonFieldNames.candidate, JsonFieldNames.candidato);
			String recrutador = json.getAsString(JsonFieldNames.email);
			this.candidatosAgrupadosPorRecrutadores = this.candidatosAgrupadosPorRecrutadores.getDynamicVersion().addToList(recrutador,
					candidato);
		}
	}

	static CcpJsonRepresentation getVagasAgrupadosPorRecrutadores(List<Object> intersectList) {
		CcpEntityField idField = new CcpEntityField("mail", false, CcpOtherConstants.DO_NOTHING);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.startSimplifiedQuery().terms(idField, intersectList)
				.endSimplifiedQueryAndBackToRequest();

		String[] resourcesNames = new String[] { "vagas" };
		AgruparVagasPorRecrutadores consumer = new AgruparVagasPorRecrutadores();
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		queryExecutor.consumeQueryResult(query, resourcesNames, "10s", 10000, consumer, "contato", "vaga", "mail");
		return consumer.vagasAgrupadasPorRecrutadores;
	}

	static CcpJsonRepresentation getCandidatosAgrupadosPorRecrutadores(CcpQueryOptions query) {

		String[] resourcesNames = new String[] { "visualizacao_de_curriculo" };
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);

		AgruparCandidatosPorRecrutadores consumer = new AgruparCandidatosPorRecrutadores();
		queryExecutor.consumeQueryResult(query, resourcesNames, "10s", 10000, consumer, "candidate", "candidato",
				"email");
		return consumer.candidatosAgrupadosPorRecrutadores;
	}

	static Set<Object> getEmails(CcpQueryOptions query, String tabela, String... fields) {
		String[] resourcesNames = new String[] { tabela };
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		Set<Object> set = new HashSet<>();
		queryExecutor.consumeQueryResult(query, resourcesNames, "10s", 10000, json -> {
			for (String field : fields) {

				String value = json.getDynamicVersion().getAsTextDecorator(field).content.trim().toLowerCase();

				if (value.isEmpty()) {
					continue;
				}
				set.add(value);
			}

		}, fields);
		return set;
	}

	static void excluirCurriculo() {
		CcpHttpResponse executeHttpRequest = CcpDependencyInjection.getDependency(CcpHttpRequester.class)
				.executeHttpRequest("http://localhost:9200/profissionais2/_doc/lucascavalcantedeo@gmail.com",
						CcpHttpMethods.DELETE, CcpOtherConstants.EMPTY_JSON, "");
		System.out.println(executeHttpRequest);
	}

	static void testarTempo() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames.cause, new CcpJsonRepresentation("{'nome':'onias'}"))
				.put(JsonFieldNames.stackTrace, "{'nome':'vieira'}")
				.put(JsonFieldNames.type, "any");
		JnEntityJobsnowError.ENTITY.delete(json);
		JnEntityJobsnowError.ENTITY.save(json);
		while (true) {
			boolean exists = JnEntityJobsnowError.ENTITY.exists(json);
			if (false == exists) {
				JnEntityJobsnowError.ENTITY.save(json);
				System.out.println(new CcpTimeDecorator().getFormattedDateTime("dd/MM/yyyy HH:mm:ss.SSS"));
			}
			new CcpTimeDecorator().sleep(1000);

		}
	}

	static void errarInfinitamente() {
		CcpTimeDecorator ccpTimeDecorator = new CcpTimeDecorator();
		CcpHttpRequester dependency = CcpDependencyInjection.getDependency(CcpHttpRequester.class);

		while (true) {
			dependency.executeHttpRequest("http://localhost:8080/login/r066u1bd@teste.com", CcpHttpMethods.GET,
					CcpOtherConstants.EMPTY_JSON, "");
			ccpTimeDecorator.sleep(60_000);
		}
	}

	static int counter;

	static void salvarVagaDoJobsNowAntigo() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions queryToSearchLastUpdatedResumes = CcpQueryOptions.INSTANCE.matchAll();
		CcpFileDecorator file = new CcpStringDecorator("vagas.txt").file();
		String[] resourcesNames = new String[] { "vagas" };
		queryExecutor.consumeQueryResult(queryToSearchLastUpdatedResumes, resourcesNames, "10s", 10000, vaga -> {
			String texto = vaga.getAsString(JsonFieldNames.vaga).replace("\n", "").trim();
			String completeLeft = new CcpStringDecorator("" + ++counter).text().completeLeft('0', 6).content;
			file.append(completeLeft + ": " + texto);
//					CcpTimeDecorator.appendLog(counter);
		}, "vaga");
	}

}

abstract class Pai {
	abstract void a();
}

class Filho1 extends Pai {
	void a() {

	}
}

class Filho2 extends Pai {
	void a() {

	}
}

interface MinhaInterface {
	String meuMetodo(String p1, String p2);
}

class Pessoa {
	final int idade;
	final String nome;

	public Pessoa(int idade, String nome) {
		this.idade = idade;
		this.nome = nome;
	}

	@Override
	public String toString() {
		return "Pessoa [idade=" + idade + ", nome=" + nome + "]";
	}
}

class A {
	Object a;
}
class B extends A{
	static Object b;
}
enum Fields implements CcpJsonFieldName{
	@CcpEntityFieldPrimaryKey
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	email, 
	ddd,
}

