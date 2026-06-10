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
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpCollectionDecorator;
import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpFolderDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTextDecorator;
import com.ccp.decorators.CcpTextDecorator.CcpTemplateFunctions;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.query.CcpQueryExecutor;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityField;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.http.CcpHttpContentType;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.especifications.password.CcpPasswordHandler;
import com.ccp.especifications.text.extractor.CcpTextExtractor;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.instant.messenger.telegram.CcpTelegramInstantMessenger;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.implementations.text.extractor.apache.tika.CcpApacheTikaTextExtractor;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.business.login.JnBusinessExecuteLogout;
import com.jn.business.messages.JnBusinessSendInstantMessage;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.entities.JnEntityDisposableTest;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginToken;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.vis.entities.VisEntityGroupPositionsBySkills;
import com.vis.entities.VisEntityResume;
import com.vis.resumes.ImportResumeFromOldJobsNow;
import com.vis.services.VisServiceSkills;

public class CcpRandomTests {

	static enum JsonFields implements CcpJsonFieldName{
		implicitSkills, skill, word, childrenCount, hasNoParent, parent, mirror, hasMirror, allParents, hasRepeatedParent, directParent, commonParents, hasSkillsWithCommonParentsSize, skillsWithCommonParents, synonym, similar, preRequisite, positionsCount, parentSize, skillSize, words, id, skillsPerParent, tipoVaga, curriculo, conteudo, text
	}
	static CcpJsonRepresentation groupedCompanies = CcpOtherConstants.EMPTY_JSON;
	private static final String LINKEDIN_REGEX = "^https://(www\\.)?linkedin\\.com/in/[a-zA-Z0-9-_%]+/?$";
	
	public static boolean isValidLinkedInUrl(String url) {
		return Pattern.matches(LINKEDIN_REGEX, url);
	}

	public static void main(String[] args) {
		CcpJsonRepresentation json = new CcpJsonRepresentation("{\r\n"
				+ "  \"_index\": \"profissionais2\",\r\n"
				+ "  \"_type\": \"_doc\",\r\n"
				+ "  \"_id\": \"onias85@gmail.com\",\r\n"
				+ "  \"_version\": 50,\r\n"
				+ "  \"_seq_no\": 54491,\r\n"
				+ "  \"_primary_term\": 10,\r\n"
				+ "  \"found\": true,\r\n"
				+ "  \"_source\": {\r\n"
				+ "    \"observacao\": \"Disponibilidade Imediata\\nApenas e somente Remoto\\n15.000 PJ \",\r\n"
				+ "    \"tipoVaga\": 1,\r\n"
				+ "    \"chatId\": 751717896,\r\n"
				+ "    \"keywords\": [\r\n"
				+ "      \"JAVA\",\r\n"
				+ "      \"SQL\",\r\n"
				+ "      \"REACT\"\r\n"
				+ "    ],\r\n"
				+ "    \"curriculo\": {\r\n"
				+ "      \"arquivo\": \"react-angular-elasticsearch-gcloud-spring-experiencia_13-onias.docx\",\r\n"
				+ "      \"conteudo\": \"data:application/vnd.openxmlformats-officedocument.wordprocessingml.document;base64,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\",\r\n"
				+ "      \"t\": \"ONIAS VIEIRA JUNIOR\\nEMAIL: onias85@gmail.com NASCIDO EM: 22/06/1985. CONTACTO: (11) 966-058-642.\\nLOCALIZAÇÃO: Jabaquara  – SÃO PAULO.\\n\\nFORMAÇÃO ACADÊMICA / ESCOLARIDADE:\\nGRADUADO EM TECNOLOGIA DA INFORMAÇÃO PELO CENTRO UNIVERSITÁRIO LUSÍADA, SANTOS – SP em 2007.\\nMBA EM JAVA COM SOA PELA FIAP, SÃO PAULO – SP em 2014.\\n\\nRESUMO DO CURRÍCULO:\\nProgramador Java desde junho de 2010.\\nProgramador desde abril de 2009.\\nCertificação de Programador Java sun scjp 6 (em 2010).\\nCertificação de Programador Java oracle ocjp 7 (em 2015).\\nCertificação Associate Java oracle ocjp 5 e 6 (em 2015).\\nInglês avançado (conversação), avançado (leitura).\\nEspanhol avançado (conversação), avançado (leitura).\\n\\nRESUMO DAS FERRAMENTAS JÁ TRABALHADAS:\\nCLOUD: Google Cloud (BigQuery, DataStore, App Engine(SAAS), Compute Engine(PAAS), Cloud Sql, Pub/Sub, Cloud Sql, MemCache).\\nDATABASES: Oracle, Sql Server, MySql, elasticsearch.\\nMETODOLOGIA: Scrum, TDD, DDD.\\nARQUITETURA: EJB 2 e 3, Spring 3 e 4, Design Patterns, Reflection, UML, JAB.\\nWS: AXIS, Restlet, Jersey,  JMS, JSON, XML, XSD, Xpath.\\nLINGUAGENS: Delphi, C#, Java, Javascript.\\nIDE: Eclipse, Visual Studio, RTC.\\nSERVIDORES: Tomcat, Jboss, Weblogic E Websphere.\\nWEB: Html 5, CSS 3, Bootstrap, primeng.\\nMOBILE: CORDOVA, IONIC 2.\\nJS: Jquery, Jquery Ui, Ext JS, AngularJs(1,2 e 5), TypeScript, reactjs.\\nMVC: JSF 1 e 2, Primefaces 3, JSP, Vraptor 4, Spring MVC, Servlets, Struts 1 e 2.\\nORM: Hibernate, JPA, JDBC.\\nTESTES: Junit, Jmeter, Soap UI, Teste NG, Mockito.\\nS.O: Windows, Linux.\\nDEPLOY: Ant, Maven, Jenkins.\\nVERSIONADOR: TFS, GIT, SVN, CVS, VSS. \\nAOP: ASPECTJ.\\nPORTLETS: Wem(VCM), Liferay, Fluig (Totvs).\\nOUTROS: Applet, Swing, IBM Maximo, Java FX.\\n\\n\\nOBSERVAÇÕES:\\n1) Minha disponibilidade para início é imediata.\\n2) Minha pretensão PJ é de 100 / hora.\\n\\nEXPERIENCIA PROFISSIONAL:\\nCLIENTE: JobsNow.\\nCONSULTORIA ALOCADORA:  Negócio próprio.\\nPERÍODO:  10/2018 ao momento atual.\\nFERRAMENTAS:  Google Cloud (MemCache, PUB/SUB, App engine(SAAS), Compute Engine (PAAS)), react Js, eclipse, java 8, springboot, elasticsearch (construi API  Java para ele), maven, git, css, html, javascript, visual studio code, APIs javascript.\\nCARGO: Consultor de Tecnologia.\\nATIVIDADES:  Cadastro, estatísticas e buscas  (no estilo google) inteligentes de currículo.\\n\\nCLIENTE: NoxxonSatt.\\nPERÍODO:  01/2018 ao momento atual.\\nFERRAMENTAS:  Google Cloud (BigQuery, DataStore, MemCache, PUB/SUB, CloudSql, App engine(SAAS), Compute Engine (PAAS)), react Js, eclipse, java 8, springboot, JPA, maven, git, css, html, javascript, visual studio code, APIs javascript e java para georeferencia / geolocalização.\\nCARGO: Consultor de Tecnologia.\\nATIVIDADES:  Monitoramento e geolocalização de 6 mil ônibus intermunicipais geridos pela EMTU, manutenção desse sistema em ambiente de nuvem (Google).\\n\\nCLIENTE: Santander.\\nCONSULTORIA ALOCADORA:  Itera Consultoria.\\nPERÍODO: 03/2017 a 01/2018\\nFERRAMENTAS:  Java, Jab(Java Arquitetura Brasil), SQL, Springboot, Eclipse, Jenkins, RTC, IBM Broker\\nCARGO: Consultor de Tecnologia.\\nATIVIDADES:  Exposição de dados de PL/SQL em rest (projeto KiPrev), guarda dos dados de arquivos sequenciais em banco de dados(Projeto Reinf).\\n\\nCLIENTE: CERTISIGN, NETSHOES, SEFAZ, TOTVS\\nCONSULTORIA ALOCADORA: TOTVS SERRA DO MAR.\\nPERÍODO: 09/2013 até 07/2016\\nFERRAMENTAS:  Javascript, html, css, spring, oracle, hibernate, jboss, tomcat, eclipse, Visual Studio, Java 4, 6 e 7,  C#, jenkins, ANT deploy, Maven, JMeeter,  Velocity, TFS, SVN, Junit, JMS, SOAP, JSF, Primefaces, SVN, Linux, JSON, JQUERY, JQUERY UI, Fluig\\nCARGO: Consultor de Tecnologia Java\\nATIVIDADES:  Front end Jquery para sistema Fluig (TOTVS), substituição de scriptlet [java com html] em JSPs por SPA em jquery e criação de framework próprio para trabalhar json(SEFAZ). Criação do portal do fornecedor em jsf/primefaces e sua especificação de webservice e seu framework de comunicação SOAP com SAP, criação de comunicação com JMS (Netshoes). Sustentação de sistemas web para gerenciamento de certificado digital, sustentação de framework de assinador digital (Certisign)\\n\\nCLIENTE: BRADESCO\\nCONSULTORIA ALOCADORA: IFACTORY SOLUTIONS.\\nPERÍODO: 04/2013 a 09/2013\\nPROJETO: NPORTALBRA\\nREGRA DE NEGÓCIO: Site de conteúdo / Portal.\\nOBJETIVO: Mover todo o conteúdo do portal em html estático para o gestor de conteúdo WEM (VCM).\\nFERRAMENTAS: JSP, WEM (VCM), SCRUM, SVN, HTML, JAVASCRIPT, CSS, HTML.\\nCARGO: Analista / programador Pleno\\nATIVIDADES:  Criação de componentes em JSP para portar conteúdo estático em templates do WEM [Web Experience Management] (Bradesco)\\nCLIENTE: CCR/ODDEBRECHT\\nCONSULTORIA ALOCADORA: CAPITAL AMBIENTAL.\\nPERÍODO: 09/2012 a 04/2013\\nPROJETO: CERENSA\\nREGRA DE NEGÓCIO: Gestão ambiental.\\nOBJETIVO: Prover software com interface web para inventariado de impactos ambientais.\\nFERRAMENTAS:  JAVA 6, Vraptor, ExtJS, Spring, Hibernate, My sql, Tomcat, GIT, CSS, HTML.\\nCARGO: Analista / programador Pleno\\nATIVIDADES:  Construção de front end em ExtJs e back end em java com spring\\n\\nCLIENTE: VOLKSWAGEN\\nCONSULTORIA ALOCADORA: CSI SISTEMAS E ENGENHARIA.\\nPERÍODO: 02/2012 a 09/2012\\nPROJETO: PICK BY LIGHT\\nREGRA DE NEGÓCIO: Automação Industrial.\\nOBJETIVO: Prover software de automação com biometria de impressão digital que informe as etiquetas que devem ser colocadas no carro que está sendo montado e avisar caso seja usada a etiqueta errada.\\nFERRAMENTAS:  JAVA, 6, Swing, reflection, SVN, Design Pattern State machine, AMGSTROM (LINUX EMBARCADO).\\nCARGO: Analista / programador Junior\\nATIVIDADES:  Criaçaõ de sistema de automação na linha da Volkswagen que permitia ao operador saber quais etquetas colar em um carro.\\n\\nCLIENTE: PMSBC (PREFEITURA MUNICIPAL DE SÃO BERNARDO DO CAMPO)\\nCONSULTORIA ALOCADORA: G & P SISTEMAS.\\nPERÍODO: 01/2011 a 01/2012\\nPROJETO: SIGOM (SISTEMA DE GESTÃO ORÇAMENTÁRIA MUNICIPAL)\\nREGRA DE NEGÓCIO: Gestão Orçamentária Municipal.\\nOBJETIVO: Manutenção em software que tem por função automatizar a gestão orçamentária municipal.\\nFERRAMENTAS:  JAVA 6, EJB2, Struts 1, Oracle, JBOSS, SVN, HTML, CSS, JAVASCRIPT, DDD.\\nCARGO: Analista / programador Junior\\nATIVIDADES:  Sustentação de sistema de orçamento municipal em seu back end e front end.\\n\\nCLIENTE: SHOPPING RECIFE E AVON.\\nCONSULTORIA ALOCADORA: NRB SOLUTIONS.\\nPERÍODO: 06/2010 a 01/2011\\nPROJETO: CUSTOMIZAÇÃO DO IBM MAXIMO.\\nREGRA DE NEGÓCIO: EAM (Enterprise Asset Management).\\nOBJETIVO: Prover customização de classes Java do software IBM MAXIMO.\\nFERRAMENTAS:  Java 6, Struts 2, Javascript, html, css, oracle, IBM MAXIMO.\\nCARGO: Analista / programador Junior\\nATIVIDADES:  Customização em java de ferramenta da IBM para clientes.\\n\\nCLIENTE: EXPORTADORA AGRÍCOLA CARCON, PLANIM LOGÍSTICA.\\nCONSULTORIA ALOCADORA: MBM SYSTEMS.\\nPERÍODO: 04/2009 a 06/2010\\nPROJETO: CARCON.\\nREGRA DE NEGÓCIO: Exportação agrícola.\\nOBJETIVO: Prover software web com a finalidade de gerir todo ciclo da cadeia de exportação de algodão.\\nFERRAMENTAS:  C sharp,Visual Studio, Linq, Visual source Safe, SQL Server, Delphi 2009.\\nCARGO: Analista / programador Junior\\nATIVIDADES:  Construção de front e back end para módulos do sistema de exportação agrícola da Carcon\\n\\n\\n\\n\\n\\n\",\r\n"
				+ "      \"textoProcessado\": \"  &  /  01/2011  01/2012  01/2018  02/2012  03/2017  04/2009  04/2013  058  06/2010  07/2016  09/2012  09/2013  1  10/2018  100  11  2  2.  2007.  2009.  2010).  2010.  2014.  2015).  22/06/1985.  3  4  5  5)  6  642.  7  8  966  A  ABRIL  ACADEMICA  AGRICOLA  AGRICOLA.  ALGODAO.  ALOCADORA  AMBIENTAIS.  AMBIENTAL.  AMBIENTE  AMGSTROM  ANALISTA  ANGULARJS  ANT  AO  AOP  API  APIS  APP  APPLET  ARQUITETURA  ARQUIVOS  AS  ASPECTJ.  ASSET  ASSINADOR  ASSOCIATE  ATE  ATIVIDADES  ATUAL.  AUTOMACAO  AUTOMATIZAR  AVANCADO  AVISAR  AVON.  AXIS  BACK  BANCO  BERNARDO  BIGQUERY  BIOMETRIA  BOOTSTRAP  BRADESCO  BRADESCO)  BRASIL)  BROKER  BUSCAS  BY  C  C#  CADASTRO  CADEIA  CAMPO)  CAPITAL  CARCON  CARCON.  CARGO  CARRO  CARRO.  CASO  CCR/ODDEBRECHT  CENTRO  CERENSA  CERTIFICACAO  CERTIFICADO  CERTISIGN  CERTISIGN)  CICLO  CLASSES  CLIENTE  CLIENTES.  CLOUD  CLOUDSQL  CODE  COLAR  COLOCADAS  COM  COMPONENTES  COMPUTE  COMUNICACAO  CONSTRUCAO  CONSTRUI  CONSULTOR  CONSULTORIA  CONSULTORIA.  CONTACTO  CONTEUDO  CONVERSACAO)  CORDOVA  CRIACAO  CSI  CSS  CURRICULO  CURRICULO.  CUSTOMIZACAO  CVS  DA  DADOS  DAS  DATABASES  DATASTORE  DDD.  DE  DELPHI  DEPLOY  DESDE  DESIGN  DESSE  DEVEM  DIGITAL  DISPONIBILIDADE  DO  DOS  E  EAM  ECLIPSE  EJB  EJB2  ELASTICSEARCH  ELASTICSEARCH.  ELE)  EM  EMAIL  EMBARCADO).  EMTU  END  END.  ENGENHARIA.  ENGINE  ENTERPRISE  ERRADA.  ESCOLARIDADE  ESPANHOL  ESPECIFICACAO  ESTA  ESTATICO  ESTATISTICAS  ESTILO  ETIQUETA  ETIQUETAS  ETQUETAS  EXPERIENCE  EXPERIENCIA  EXPORTACAO  EXPORTADORA  EXPOSICAO  EXT  EXTJS  FERRAMENTA  FERRAMENTAS  FIAP  FINALIDADE  FLUIG  FORMACAO  FORNECEDOR  FRAMEWORK  FRONT  FUNCAO  FX.  G  GEOLOCALIZACAO  GEOLOCALIZACAO.  GEOREFERENCIA  GERENCIAMENTO  GERIDOS  GERIR  GESTAO  GESTOR  GIT  GOOGLE  GOOGLE).  GRADUADO  GUARDA  HIBERNATE  HORA.  HTML  HTML.  IBM  IDE  IFACTORY  IMEDIATA.  IMPACTOS  IMPRESSAO  INDUSTRIAL.  INFORMACAO  INFORME  INGLES  INICIO  INTELIGENTES  INTERFACE  INTERMUNICIPAIS  INVENTARIADO  IONIC  ITERA  JA  JAB  JAB.  JABAQUARA  JAVA  JAVASCRIPT  JAVASCRIPT.  JBOSS  JDBC.  JENKINS  JENKINS.  JERSEY  JMEETER  JMETER  JMS  JOBSNOW.  JPA  JQUERY  JS  JSF  JSF/PRIMEFACES  JSON  JSP  JSPS  JUNHO  JUNIOR  JUNIT  KIPREV)  LEITURA).  LIFERAY  LIGHT  LINGUAGENS  LINHA  LINQ  LINUX  LINUX.  LOCALIZACAO  LOGISTICA.  LUSIADA  MACHINE  MANAGEMENT  MANAGEMENT).  MANUTENCAO  MAR.  MAVEN  MAXIMO  MAXIMO.  MBA  MBM  MEMCACHE  MEMCACHE).  METODOLOGIA  MIL  MINHA  MOBILE  MOCKITO.  MODULOS  MOMENTO  MONITORAMENTO  MONTADO  MOVER  MUNICIPAL  MUNICIPAL)  MUNICIPAL.  MVC  MY  MYSQL  NA  NASCIDO  NEGOCIO  NETSHOES  NETSHOES).  NG  NO  NOXXONSATT.  NPORTALBRA  NRB  NUVEM  O  OBJETIVO  OBSERVACOES  OCJP  ONIAS  ONIAS85@GMAIL.COM  ONIBUS  OPERADOR  ORACLE  ORCAMENTARIA  ORCAMENTO  ORM  OUTROS  P  PAAS)  PAAS))  PARA  PATTERN  PATTERNS  PAULO  PAULO.  PELA  PELO  PERIODO  PERMITIA  PICK  PJ  PL/SQL  PLANIM  PLENO  PMSBC  POR  PORTAL  PORTAL.  PORTAR  PORTLETS  PREFEITURA  PRETENSAO  PRIMEFACES  PRIMENG.  PROFISSIONAL  PROGRAMADOR  PROJETO  PROPRIO  PROPRIO.  PROVER  PUB/SUB  QUAIS  QUE  REACT  REACTJS.  RECIFE  REFLECTION  REGRA  REINF).  REST  RESTLET  RESUMO  RTC  RTC.  S.O  SAAS)  SABER  SAFE  SANTANDER.  SANTOS  SAO  SAP  SCJP  SCRIPTLET  SCRUM  SEFAZ  SEFAZ).  SEJA  SENDO  SEQUENCIAIS  SER  SERRA  SERVER  SERVIDORES  SERVLETS  SEU  SHARP  SHOPPING  SIGOM  SISTEMA  SISTEMAS  SISTEMAS.  SITE  SOA  SOAP  SOFTWARE  SOLUTIONS.  SOURCE  SP  SPA  SPRING  SPRINGBOOT  SQL  STATE  STRUTS  STUDIO  SUA  SUBSTITUICAO  SUN  SUSTENTACAO  SVN  SWING  SYSTEMS.  TDD  TECNOLOGIA  TECNOLOGIA.  TEM  TEMPLATES  TESTE  TESTES  TFS  TODO  TOMCAT  TOTVS  TOTVS)  TOTVS).  TRABALHADAS  TRABALHAR  TYPESCRIPT  UI  UM  UML  UNIVERSITARIO  USADA  VCM)  VCM).  VELOCITY  VERSIONADOR  VIEIRA  VISUAL  VOLKSWAGEN  VRAPTOR  VSS.  WEB  WEBLOGIC  WEBSERVICE  WEBSPHERE.  WEM  WINDOWS  WS  XML  XPATH.  XSD  –\",\r\n"
				+ "      \"sha1\": \"3cd6d1cea966cfec6bd852672dceef637dd5a736\"\r\n"
				+ "    },\r\n"
				+ "    \"pretensaoNegociavel\": false,\r\n"
				+ "    \"sinonimos\": [\r\n"
				+ "      \"JAVA\",\r\n"
				+ "      \"WEBLOGIC\",\r\n"
				+ "      \"HIBERNATE\",\r\n"
				+ "      \"LINUX\",\r\n"
				+ "      \"INGLES\",\r\n"
				+ "      \"JBOSS\",\r\n"
				+ "      \"TESTES\",\r\n"
				+ "      \"HTML\",\r\n"
				+ "      \"IDE\",\r\n"
				+ "      \"STRUTS\",\r\n"
				+ "      \"ORACLE\",\r\n"
				+ "      \"TOMCAT\",\r\n"
				+ "      \"ECLIPSE\",\r\n"
				+ "      \"AUTOMACAO\",\r\n"
				+ "      \"UI\",\r\n"
				+ "      \"MONITORAMENTO\",\r\n"
				+ "      \"JSF\",\r\n"
				+ "      \"PRIMEFACES\",\r\n"
				+ "      \"DEPLOY\",\r\n"
				+ "      \"NUVEM\",\r\n"
				+ "      \"ESPANHOL\",\r\n"
				+ "      \"ANGULARJS\",\r\n"
				+ "      \"JSP\",\r\n"
				+ "      \"SCRUM\",\r\n"
				+ "      \"IONIC\",\r\n"
				+ "      \"SERVIDORES\",\r\n"
				+ "      \"SOAP\",\r\n"
				+ "      \"JENKINS\",\r\n"
				+ "      \"SQL\",\r\n"
				+ "      \"MAVEN\",\r\n"
				+ "      \"SCJP\",\r\n"
				+ "      \"BIGQUERY\",\r\n"
				+ "      \"TESTE\",\r\n"
				+ "      \"WEB\",\r\n"
				+ "      \"JPA\",\r\n"
				+ "      \"OCJP\",\r\n"
				+ "      \"WEM\",\r\n"
				+ "      \"C#\",\r\n"
				+ "      \"JAVASCRIPT\",\r\n"
				+ "      \"VCM\",\r\n"
				+ "      \"SAP\",\r\n"
				+ "      \"CLOUD\",\r\n"
				+ "      \"ANT\",\r\n"
				+ "      \"JS\",\r\n"
				+ "      \"JSON\",\r\n"
				+ "      \"ORM\",\r\n"
				+ "      \"CERTIFICACAO\",\r\n"
				+ "      \"TYPESCRIPT\",\r\n"
				+ "      \"FLUIG\",\r\n"
				+ "      \"TDD\",\r\n"
				+ "      \"SERVER\",\r\n"
				+ "      \"WINDOWS\",\r\n"
				+ "      \"WS\",\r\n"
				+ "      \"DATASTORE\",\r\n"
				+ "      \"CSS\",\r\n"
				+ "      \"SPRINGBOOT\",\r\n"
				+ "      \"MYSQL\",\r\n"
				+ "      \"SOA\",\r\n"
				+ "      \"WEBSERVICE\",\r\n"
				+ "      \"LIFERAY\",\r\n"
				+ "      \"CORDOVA\",\r\n"
				+ "      \"REST\",\r\n"
				+ "      \"BOOTSTRAP\",\r\n"
				+ "      \"S.O\",\r\n"
				+ "      \"SUSTENTACAO\",\r\n"
				+ "      \"ELASTICSEARCH\",\r\n"
				+ "      \"JMETER\",\r\n"
				+ "      \"REACT\",\r\n"
				+ "      \"JQUERY\",\r\n"
				+ "      \"EXTJS\",\r\n"
				+ "      \"MOBILE\"\r\n"
				+ "    ],\r\n"
				+ "    \"pcd\": false,\r\n"
				+ "    \"requisitos\": [\r\n"
				+ "      \"JAVA\",\r\n"
				+ "      \"VELOCITY\",\r\n"
				+ "      \"WEBLOGIC\",\r\n"
				+ "      \"HIBERNATE\",\r\n"
				+ "      \"INGLES\",\r\n"
				+ "      \"PATTERNS\",\r\n"
				+ "      \"FRONT\",\r\n"
				+ "      \"ORACLE\",\r\n"
				+ "      \"SERVLETS\",\r\n"
				+ "      \"MONITORAMENTO\",\r\n"
				+ "      \"DEPLOY\",\r\n"
				+ "      \"JSF\",\r\n"
				+ "      \"TFS\",\r\n"
				+ "      \"SPRING\",\r\n"
				+ "      \"NUVEM\",\r\n"
				+ "      \"ESPANHOL\",\r\n"
				+ "      \"JSP\",\r\n"
				+ "      \"IONIC\",\r\n"
				+ "      \"SERVIDORES\",\r\n"
				+ "      \"JENKINS\",\r\n"
				+ "      \"MAVEN\",\r\n"
				+ "      \"TESTE\",\r\n"
				+ "      \"WEB\",\r\n"
				+ "      \"SYSTEMS\",\r\n"
				+ "      \"EJB\",\r\n"
				+ "      \"WEM\",\r\n"
				+ "      \"OCJP\",\r\n"
				+ "      \"JAVASCRIPT\",\r\n"
				+ "      \"C#\",\r\n"
				+ "      \"JS\",\r\n"
				+ "      \"SVN\",\r\n"
				+ "      \"TYPESCRIPT\",\r\n"
				+ "      \"FLUIG\",\r\n"
				+ "      \"MOCKITO\",\r\n"
				+ "      \"WINDOWS\",\r\n"
				+ "      \"SPRINGBOOT\",\r\n"
				+ "      \"SOA\",\r\n"
				+ "      \"SWING\",\r\n"
				+ "      \"WEBSERVICE\",\r\n"
				+ "      \"LIFERAY\",\r\n"
				+ "      \"CORDOVA\",\r\n"
				+ "      \"JMS\",\r\n"
				+ "      \"TOTVS\",\r\n"
				+ "      \"BOOTSTRAP\",\r\n"
				+ "      \"S.O\",\r\n"
				+ "      \"WEBSPHERE\",\r\n"
				+ "      \"JQUERY\",\r\n"
				+ "      \"MOBILE\",\r\n"
				+ "      \"DELPHI\",\r\n"
				+ "      \"XPATH\",\r\n"
				+ "      \"SPA\",\r\n"
				+ "      \"LINUX\",\r\n"
				+ "      \"JBOSS\",\r\n"
				+ "      \"APIS\",\r\n"
				+ "      \"TESTES\",\r\n"
				+ "      \"HTML\",\r\n"
				+ "      \"STRUTS\",\r\n"
				+ "      \"IDE\",\r\n"
				+ "      \"TOMCAT\",\r\n"
				+ "      \"VRAPTOR\",\r\n"
				+ "      \"VERSIONADOR\",\r\n"
				+ "      \"ECLIPSE\",\r\n"
				+ "      \"UI\",\r\n"
				+ "      \"AUTOMACAO\",\r\n"
				+ "      \"NEGOCIO\",\r\n"
				+ "      \"PRIMEFACES\",\r\n"
				+ "      \"ANGULARJS\",\r\n"
				+ "      \"SCRUM\",\r\n"
				+ "      \"SOAP\",\r\n"
				+ "      \"SQL\",\r\n"
				+ "      \"SCJP\",\r\n"
				+ "      \"BIGQUERY\",\r\n"
				+ "      \"GIT\",\r\n"
				+ "      \"UML\",\r\n"
				+ "      \"JPA\",\r\n"
				+ "      \"MVC\",\r\n"
				+ "      \"SAP\",\r\n"
				+ "      \"CLOUD\",\r\n"
				+ "      \"ANT\",\r\n"
				+ "      \"JSON\",\r\n"
				+ "      \"ORM\",\r\n"
				+ "      \"CERTIFICACAO\",\r\n"
				+ "      \"TDD\",\r\n"
				+ "      \"SERVER\",\r\n"
				+ "      \"REACTJS\",\r\n"
				+ "      \"JDBC\",\r\n"
				+ "      \"WS\",\r\n"
				+ "      \"DATASTORE\",\r\n"
				+ "      \"CSS\",\r\n"
				+ "      \"MYSQL\",\r\n"
				+ "      \"REST\",\r\n"
				+ "      \"JUNIT\",\r\n"
				+ "      \"XML\",\r\n"
				+ "      \"BACK\",\r\n"
				+ "      \"SUSTENTACAO\",\r\n"
				+ "      \"ELASTICSEARCH\",\r\n"
				+ "      \"API\",\r\n"
				+ "      \"JMETER\",\r\n"
				+ "      \"REACT\",\r\n"
				+ "      \"EXTJS\"\r\n"
				+ "    ],\r\n"
				+ "    \"experiencia\": 2009,\r\n"
				+ "    \"profissaoDesejada\": \"Dev Java Sr\",\r\n"
				+ "    \"arquivo\": \"react-angular-elasticsearch-gcloud-spring-experiencia_13-onias.docx\",\r\n"
				+ "    \"id\": \"onias85@gmail.com\",\r\n"
				+ "    \"dataDeInclusaoFormatada\": \"28/02/2022 23:15:08\",\r\n"
				+ "    \"disponibilidade\": \"1\",\r\n"
				+ "    \"ddd\": 0,\r\n"
				+ "    \"dataDeInclusao\": 1646090108441,\r\n"
				+ "    \"ultimaProfissao\": \"Dev Java Sr\",\r\n"
				+ "    \"homeoffice\": true,\r\n"
				+ "    \"pretensaoPj\": \"15000\",\r\n"
				+ "    \"pseudonimo\": \"KJDZF2HG1Y\",\r\n"
				+ "    \"empresas\": [\r\n"
				+ "      \"weconnect.com\",\r\n"
				+ "      \"google.com\"\r\n"
				+ "    ],\r\n"
				+ "    \"recebeTelegram\": true,\r\n"
				+ "    \"senioridade\": 4,\r\n"
				+ "    \"dataDeAlteracaoFormatada\": \"24/10/2023 00:23:35\",\r\n"
				+ "    \"dataDeAlteracao\": 1698107015444,\r\n"
				+ "    \"bitcoin\": \"15000\",\r\n"
				+ "    \"status\": 0\r\n"
				+ "  }\r\n"
				+ "}").getInnerJson(() -> "_source");
		
		System.out.println(json.fieldSet());
	}

	static void fodasse() {
		CcpDependencyInjection.loadAllDependencies(new CcpTelegramInstantMessenger());
		CcpTemplateFunctions.currentTimeMillis.get();
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnBusinessSendInstantMessage.JnJsonValidator.chatId, 751717896L)
				.put(JnBusinessSendInstantMessage.JnJsonValidator.templateId, "teste")
				.put(JnBusinessSendInstantMessage.JnMessageFileJsonValidator.fileName, "{chatId}.txt")
				.put(JnBusinessSendInstantMessage.JnMessageFileJsonValidator.caption, "{templateId}.{currentTimeMillis()}")
				.put(JnBusinessSendInstantMessage.JnMessageFileJsonValidator.contentType, CcpHttpContentType.TEXT_PLAIN)
				.put(JnBusinessSendInstantMessage.JnMessageFileJsonValidator.message, "mensagem de teste")
				.put(JnBusinessSendInstantMessage.JnJsonValidator.botName, JnBusinessSendInstantMessage.JnBotType.support)
				.put(JnBusinessSendInstantMessage.JnJsonValidator.instantMessageType, JnBusinessSendInstantMessage.JnInstantMessageType.text)
				;
		
		boolean exists = JnEntityInstantMessengerMessageSent.ENTITY.exists(json);
		System.out.println(exists);
	}

	static void enviarArquivoPorTelegram() {
		CcpDependencyInjection.loadAllDependencies(new CcpTelegramInstantMessenger());
		CcpInstantMessenger dependency = CcpDependencyInjection.getDependency(CcpInstantMessenger.class);
		
		CcpJsonRepresentation sendFile = dependency.sendFile(() -> "", "1154866992:AAGvXIU01UXgpA1gFOBE4pJXjhicf7JnRd8", 751717896L, 0L, "teste.txt", "legenda", 
				new CcpStringDecorator("teste do tio onias").getBytes());
		
		System.out.println(sendFile);
	}

	static void getDataWithTimeStamp() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityDisposableTest.Fields.email, "onias85@gmail.com")
				;
		JnEntityDisposableTest.ENTITY.save(json);
		{
			CcpJsonRepresentation dataWithTimeStamp = JnEntityDisposableRecord.getDataWithTimeStamp(JnEntityDisposableTest.ENTITY, json);
			System.out.println(dataWithTimeStamp);
		}
		JnEntityDisposableTest.ENTITY.delete(json);
		{
			CcpJsonRepresentation dataWithTimeStamp = JnEntityDisposableRecord.getDataWithTimeStamp(JnEntityDisposableTest.ENTITY, json);
			System.out.println(dataWithTimeStamp);
		}
	}

	static void testarDisposable() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityDisposableTest.Fields.email, "onias85@gmail.com")
				;
		CcpEntity entity = JnEntityDisposableTest.ENTITY;
		entity.save(json);
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class); 
		for(int k = 0; k < 70; k++) {
			CcpSelectUnionAll unionAll = crud.unionAll(json, JnDeleteKeysFromCache.INSTANCE, entity);
			CcpTimeDecorator ctd = new CcpTimeDecorator();
			String formattedDateTime = CcpEntityExpurgableOptions.millisecond.getFormattedDate();
			Supplier<CcpJsonRepresentation> jsonSupplier = json.getJsonSupplier();
			var exists = entity.getRecordFromUnionAll(unionAll, jsonSupplier);
			System.out.println(formattedDateTime + ": " + exists);
			ctd.sleep(60*1000);
		}
	}

	static void apagarTodosOsAgrupamentosDeSkills() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll();
		queryExecutor.delete(query, "group_positions_by_skills");
	} 

	static void relatorioDeSkillsPesquisadas(String...skils) {
		for (String skill : skils) {
			int wordStatus = VisEntityGroupPositionsBySkills.getWordStatus(skill);
			System.out.println(skill + " = " + wordStatus);
		}
	}
	
	
	static void getSkillsFromText() {
		CcpJsonRepresentation skillsFromText = getSkillsFromText(" VAGA | Arquiteto de Integração Java / Telecom\r\n"
				+ "📌 Projeto: 5 meses\r\n"
				+ "\r\n"
				+ "🔧 Requisitos Técnicos\r\n"
				+ "Arquitetura de Integração, SOA e Microservices\r\n"
				+ "Java 8+ / Spring Boot / Spring Cloud\r\n"
				+ "APIs REST/JSON, SOAP, GraphQL\r\n"
				+ "Mensageria: Kafka, RabbitMQ, JMS\r\n"
				+ "Arquitetura event-driven e integrações assíncronas\r\n"
				+ "Experiência em ambientes Telecom (BSS/OSS)\r\n"
				+ "Cloud (AWS, Azure ou GCP)\r\n"
				+ "Docker e Kubernetes\r\n"
				+ "Observabilidade (ELK, Prometheus, Grafana)\r\n"
				+ "\r\n"
				+ "🎯 Desejável\r\n"
				+ "TM Forum Open APIs\r\n"
				+ "eTOM, SID, TAM\r\n"
				+ "DDD / TOGAF\r\n"
				+ "\r\n"
				+ "📩 Interessou ou conhece alguém com esse perfil?\r\n"
				+ "Envie o cv com pretensão salarial no e-mail: isadora@bluesix.com.br");
		System.out.println(skillsFromText.removeFields(JsonFields.implicitSkills));
	}

	static void countWords() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll();
		Set<String> words = new HashSet<>();
		Consumer<CcpJsonRepresentation> consumer = json -> {
			List<CcpJsonRepresentation> skills = json.getAsJsonList(JsonFields.skill);
			for (CcpJsonRepresentation skill : skills) {
				String word = skill.getAsString(JsonFields.word);
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
			String skill = json.getAsString(JsonFields.skill);
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
		.map(x -> CcpOtherConstants.EMPTY_JSON.put(JsonFields.skill, x))
		.map(x -> x.put(JsonFields.childrenCount, new ArrayList<>(filtered).stream().filter(skillsNestaLinha -> skillsNestaLinha.indexOf(x.getAsString(JsonFields.skill)) > 0).count()))
		.map(x -> {
			Optional<List<String>> findFirst = new ArrayList<>(filtered).stream().filter(skillsNestaLinha -> skillsNestaLinha.indexOf(x.getAsString(JsonFields.skill)) == 0).findFirst();
			boolean hasNoParent = false == findFirst.isPresent();
			if(hasNoParent) {
				return x;
			}
			List<String> list = findFirst.get();
			List<String> parent = list.subList(1, list.size());
			CcpJsonRepresentation put = x.put(JsonFields.parent, parent);
			return put;
		})
		.map(x -> x.put(JsonFields.hasNoParent, new ArrayList<>( filtered).stream().allMatch(skillsNestaLinha -> skillsNestaLinha.indexOf(x.getAsString(JsonFields.skill)) != 0)))
		.collect(Collectors.toList());
		
		
		Comparator<? super CcpJsonRepresentation> sorter = getSorter("hasRepeatedParent", "hasSkillsWithCommonParentsSize", "hasMirror", "childrenCount", "skill");
		
		List<CcpJsonRepresentation> collect = report
		.stream()
		.map(x -> x.put(JsonFields.mirror, getSynonym(x, report)))
		.map(x -> x.put(JsonFields.hasMirror, false == x.getAsString(JsonFields.mirror).isEmpty()))
		.map(x -> getSkillsWithCommonParentsSize(x, report))
		.collect(Collectors.toList());
		
		
		CcpFileDecorator reportFile = new CcpStringDecorator(folder+ "report_skills.json").file().reset();
		
		List<CcpJsonRepresentation> newList = new ArrayList<>();
		
		List<String> synonyms3 = new CcpStringDecorator(folder+ "synonyms3.txt").file().getLines();

		List<Set<String>> synonyms = synonyms3.stream()
				.map(x -> Arrays.asList(x.split(",")).stream()
						.map(y -> y.trim().toUpperCase())
						.filter(y -> y.length() > 1)
						.filter(y -> y.length() < 50)
						.collect(Collectors.toSet()))
//				.map(x -> getOtherWords(x))
				.collect(Collectors.toList());
		
		
		
		
		for (CcpJsonRepresentation json : collect) {
			List<String> allParents = new ArrayList<>();
			getAllParents(allParents, report, json);
			
			HashSet<String> set = new HashSet<String>(allParents);
			boolean hasRepeatedParent = set.size() != allParents.size();
			CcpJsonRepresentation put = json.put(JsonFields.allParents, allParents.stream()
//					.map(skill -> CcpOtherConstants.EMPTY_JSON.getDynamicVersion().put("skill",skill)
//							.getDynamicVersion()
//							.put("parent", getParent(skill, report))
//							)
					.collect(Collectors.toList())
					)
					.put(JsonFields.hasRepeatedParent, hasRepeatedParent);
					
					;
			
			String skill = put.getAsString(JsonFields.skill);
			
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
					.map(x -> CcpOtherConstants.EMPTY_JSON.put(JsonFields.skill, x)).collect(Collectors.toList());
			
			CcpJsonRepresentation withSynonym = put
					.put(JsonFields.synonym, foundSynonym)
//					.getDynamicVersion().getJsonPiece(
//					"skill", "childrenCount"
//					, "parent"
//					)
					.renameField(JsonFields.parent, JsonFields.directParent)
					.renameField(JsonFields.allParents, JsonFields.parent)
//					.getDynamicVersion().getJsonPiece("parent", "skill", "directParent", "childrenCount", "synonym", "hasNoParent")
//					.getDynamicVersion().removeFields("synonym")
					;
			
			newList.add(withSynonym);	
		}
		
		newList.sort(sorter);
		reportFile.append(newList.toString());
	}
	
	static List<String> getParent(String skill, List<CcpJsonRepresentation> report){
		return report.stream().filter(x -> skill.equals(x.getAsString(JsonFields.skill))).findFirst().get()
				.getAsStringList(JsonFields.parent);
	}
	static Comparator<? super CcpJsonRepresentation> getSorter(String... fields){
		Comparator<? super CcpJsonRepresentation> sorter = (a, b) -> {
			
			for (String field : fields) {
				CcpStringDecorator sd1 = a.getAsStringDecorator(() -> field);

				if(sd1.isLongNumber()) {
					Integer int2 = b.getAsIntegerNumber(() -> field);
					Integer int1 = a.getAsIntegerNumber(() -> field);
					int subtration = int2 - int1;
					if(subtration == 0) {
						continue;
					}
					return subtration;
				}

				var b1 = a.getAsString(() -> field);
				var b2 = b.getAsString(() -> field);

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
		List<String> collect = report.stream()
		.filter(x -> false == x.getAsString(JsonFields.skill).equals(json.getAsString(JsonFields.skill)))
		.map(x -> x.put(JsonFields.commonParents, getCommonParents(x, json)))
		.filter(x -> x.getAsStringList(JsonFields.commonParents).size() > 1)
		.map(x -> x.getAsString(JsonFields.skill))
		.collect(Collectors.toList());
		CcpJsonRepresentation put = json.put(JsonFields.hasSkillsWithCommonParentsSize, false == collect.isEmpty())
				.put(JsonFields.skillsWithCommonParents, collect);
		return put;
	}
	
	static List<String> getCommonParents(CcpJsonRepresentation json1, CcpJsonRepresentation json2) {
		List<String> parent1 = json1.getAsStringList(JsonFields.parent);
		List<String> parent2 = json2.getAsStringList(JsonFields.parent);
		List<String> intersectList = new CcpCollectionDecorator(parent1).getIntersectList(parent2);
		return intersectList;
	}
	
	
	static List<String> getAllParents(List<String> allParents, List<CcpJsonRepresentation> report, CcpJsonRepresentation json){
		
		List<String> parents = json.getAsStringList(JsonFields.parent);

		String skill = json.getAsString(JsonFields.skill);
		System.out.println(skill + ": " + parents);
		allParents.addAll(parents);
		for (String parent : parents) {
			CcpJsonRepresentation parentJson = report.stream()
			.filter(x -> parent.equals(x.getAsString(JsonFields.skill)))
			.findFirst()
			.get();
			getAllParents(allParents, report, parentJson);
		}
		return allParents;
	}

	static String getSynonym(CcpJsonRepresentation json, List<CcpJsonRepresentation> report) {
		List<String> parent = json.getAsStringList(JsonFields.parent);
		if(parent.size() != 1) {
			return "";
		}
		String parentName = parent.get(0);
		String orElseGet = new ArrayList<>(report)
		.stream()
		.filter(x -> x.getAsString(JsonFields.skill).equals(parentName))
		.filter(x -> x.getAsIntegerNumber(JsonFields.childrenCount) == 1)
		.map(x -> x.getAsString(JsonFields.skill))
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
			
			String skill = json.getAsString(JsonFields.skill);


			if(todos.contains(skill)) {
				ajustadas ++;
				continue;
			}
			List<CcpJsonRepresentation> asJsonList = json.getAsJsonList(JsonFields.synonym);
			boolean anyMatch = asJsonList.stream().map(x -> x.getAsString(JsonFields.skill)).anyMatch(x -> todos.contains(x));
			
			if(anyMatch) {
				ajustadas ++;
				continue;
			}
			missing.add(json);
		}
		List<String> removidas = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\database\\elasticsearch\\removidas.txt").file().getLines().stream().map(x -> x.trim().split(" = ")[0]).collect(Collectors.toList());
		int estudar = 0;
		for (CcpJsonRepresentation copy : missing) {
			String skill = copy.getAsString(JsonFields.skill);
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
				List<String> list = json.getAsStringList(() -> field).stream()
						.map(x -> new CcpStringDecorator(x).text().stripAccents().sanitize().getContent().toUpperCase().trim())
						.filter(x -> x.length() > 2 && x.length() <= 50)
						.filter(x -> false == existingWords.contains(x))
						.collect(Collectors.toList());
				
				words.addAll(list);
				
			}
			System.out.println(counter++ + " = " + words.size());
		};
		queryExecutor.consumeQueryResult(query, new String[] {"pesquisa_curriculos"}, "1m", 10_000, consumer, "requisitosDesejaveis", "requisitosObrigatorios", "must", "should");
		

		for (String word : words) {
			CcpJsonRepresentation synonym = CcpOtherConstants.EMPTY_JSON.put(JsonFields.skill, word);
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
			var similar = synonym.getAsJsonList(JsonFields.similar)
					.stream()
					.map(x -> x.getAsString(JsonFields.word).replace("_", " "))
					.collect(Collectors.toList())
					;
			allSimilar.addAll(similar);
		}
		System.out.println(allSimilar.size());
		for (CcpJsonRepresentation synonym : synonyms) {
			{
				String skill = synonym.getAsString(JsonFields.skill);
				allSimilar.remove(skill);
			}
			List<String> parents = synonym.getAsStringList(JsonFields.parent);
			for (String parent : parents) {
				allSimilar.remove(parent);
			}

			List<CcpJsonRepresentation> asJsonList = synonym.getAsJsonList(JsonFields.synonym);
			for (CcpJsonRepresentation sym : asJsonList) {
				String skill = sym.getAsString(JsonFields.skill);
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
			
				String skill = synonym.getAsString(JsonFields.skill);
				if(skill.trim().length() < 2) {
					synonym = synonym.removeFields(JsonFields.skill);
				}
				if(skill.trim().length() > 50) {
					synonym = synonym.removeFields(JsonFields.skill);
				}
				if(removidas.contains(skill)) {
					synonym = synonym.removeFields(JsonFields.skill);
				}
				List<String> parent = synonym.getAsStringList(JsonFields.parent).stream()
						.filter(x -> x.length() > 2 && x.length() <= 50)
						.filter(x -> false == removidas.contains(x))
						.collect(Collectors.toList());
				
				
				synonym = synonym.put(JsonFields.parent, parent);
				{
					List<CcpJsonRepresentation> collect = synonym.getAsJsonList(JsonFields.synonym).stream().filter(x -> {
						String word = x.getAsString(JsonFields.skill);
						return word.length() > 2 && word.length() <= 50 && false == removidas.contains(word) && false == skill.equals(word);
					}).collect(Collectors.toList());

					synonym = synonym.put(JsonFields.synonym, collect);
				}
				{
					List<CcpJsonRepresentation> collect = synonym.getAsJsonList(JsonFields.preRequisite).stream().filter(x -> {
						String word = x.getAsString(JsonFields.word);
						return word.length() > 2 && word.length() <= 50 && false == removidas.contains(word);
					}).collect(Collectors.toList());

					synonym = synonym.put(JsonFields.preRequisite, collect);
				}
				{
					List<CcpJsonRepresentation> collect = synonym.getAsJsonList(JsonFields.similar).stream().filter(x -> {
						String word = x.getAsString(JsonFields.word).replace("_", " ");
						return word.length() > 2 && word.length() <= 50 && false == removidas.contains(word);
					}).collect(Collectors.toList());

					synonym = synonym.put(JsonFields.similar, collect);
				}
				
				newSynonyms.add(synonym);
			}

		List<CcpJsonRepresentation> collect = newSynonyms.stream()
		.filter(x -> x.containsAllFields(JsonFields.skill) || false == x.getAsJsonList(JsonFields.synonym).isEmpty())
		.map(x -> x.containsAllFields(JsonFields.skill) ? x : transferSynonymToSkill(x))
		.map(x -> false == x.getAsObjectList(JsonFields.parent).isEmpty() ?  x :  x.removeFields(JsonFields.parent))
		.map(x -> false == x.getAsObjectList(JsonFields.similar).isEmpty() ?  x :  x.removeFields(JsonFields.similar))
		.map(x -> false == x.getAsObjectList(JsonFields.synonym).isEmpty() ?  x :  x.removeFields(JsonFields.synonym))
		.map(x -> false == x.getAsObjectList(JsonFields.preRequisite).isEmpty() ?  x :  x.removeFields(JsonFields.preRequisite))
		.collect(Collectors.toList());
		CcpFileDecorator newSynonymsFile = new CcpStringDecorator("C:\\eclipse-workspaces\\ccp\\ccp_rest-api-tests_jobsnow\\documentation\\jn\\skills\\new_synonyms.json").file().reset();
		newSynonymsFile.append(collect.toString());
		for (CcpJsonRepresentation json : collect) {
			System.out.println(json.getAsString(JsonFields.skill));
		}
		System.out.println(collect.size());
	}
	
	static CcpJsonRepresentation transferSynonymToSkill(CcpJsonRepresentation x) {
		List<CcpJsonRepresentation> synonym = x.getAsJsonList(JsonFields.synonym);
		ArrayList<CcpJsonRepresentation> list = new ArrayList<>(synonym);
		list.sort((a,b) ->  b.getAsIntegerNumber(JsonFields.positionsCount) - a.getAsIntegerNumber(JsonFields.positionsCount));
		String skill = list.remove(0).getAsString(JsonFields.skill);
		CcpJsonRepresentation put = x.put(JsonFields.skill, skill).put(JsonFields.synonym, list);
		return put;
	}
	
	static List<String> getAllWords() {
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll().setSize(10000);
		List<CcpJsonRepresentation> resultAsList = queryExecutor.getResultAsList(query, new String[] {"group_positions_by_skills"}, "skill");
		
		Set<String> set = new HashSet<>();
		for (CcpJsonRepresentation json : resultAsList) {
			List<CcpJsonRepresentation> list = json.getAsJsonList(JsonFields.skill);
			List<String> collect = list.stream().map(x -> x.getAsString(JsonFields.word)).collect(Collectors.toList());
			set.addAll(collect);
		}
		ArrayList<String> list = new ArrayList<>(set);
		list.sort((a,b) -> a.length() - b.length());
		return list;
	}
	
	static CcpJsonRepresentation getSubReportFromSkills(CcpJsonRepresentation md, String id) {
		List<CcpJsonRepresentation> skills = md.getAsJsonList(JsonFields.skill);

		Set<String> set1 = new HashSet<>();
		Set<String> allSkills = new HashSet<>();
		Set<String> allWords = new HashSet<>();

		for (CcpJsonRepresentation skill : skills) {
			List<String> parents = skill.getAsStringList(JsonFields.parent);
			String skillName = skill.getAsString(JsonFields.skill);
			String word = skill.getAsString(JsonFields.word);
			set1.addAll(parents);
			allSkills.add(skillName);
			allWords.add(word);
		}
		
		List<String> allParents = set1.stream().filter(parent -> false == allSkills.contains(parent)).collect(Collectors.toList());
		
		double allSkillsSize = allSkills.size();
		double allParentsSize = allParents.size();
		

		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(JsonFields.parentSize, allParentsSize)
				.put(JsonFields.skillSize, allSkillsSize)
				.put(JsonFields.words, allWords)
				.put(JsonFields.id, id)
				;

		if(allParentsSize > 0) {
			double skillsPerParent = allSkillsSize / allParentsSize;
			put = put.put(JsonFields.skillsPerParent, skillsPerParent);
		}
		
		return put;
		
	}

	static void relatoriosDasSkillsNosCurriculos() {
		CcpTextExtractor textExtractor = CcpDependencyInjection.getDependency(CcpTextExtractor.class);
		
		CcpQueryExecutor queryExecutor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		CcpQueryOptions query = CcpQueryOptions.INSTANCE.matchAll();
		Map<String, Integer> countByResume = new HashMap<>();
		Map<String, Integer> countByWords = new HashMap<>();
		Map<String, Set<String>> groupedResumes = new HashMap<>();
		Map<String, CcpJsonRepresentation> reports = new HashMap<>();
		Consumer<CcpJsonRepresentation> consumer = json -> {
			try {
				String base64 = json.getValueFromPath("", JsonFields.curriculo, JsonFields.conteudo);
				
				String resumeText = textExtractor.extractText(base64);
				CcpJsonRepresentation md = getSkillsFromText(resumeText);
				
				String id = json.getAsString(JsonFields.id);
				String tipoVaga = json.getAsString(JsonFields.tipoVaga);

				List<CcpJsonRepresentation> skills = md.getAsJsonList(JsonFields.skill).stream()
						.collect(Collectors.toList());
				
				CcpTextDecorator completeLeft = new CcpStringDecorator(""+ skills.size()).text().completeLeft('0', 3);
				
				String fileName = completeLeft + "_" + id  + "_" + tipoVaga + ".json";
				
				for (CcpJsonRepresentation skill : skills) {
					String word = skill.getAsString(JsonFields.word);
					Integer counter = countByWords.getOrDefault(word, 0) + 1;
					countByWords.put(word, counter);
					Set<String> orDefault = groupedResumes.getOrDefault(word, new HashSet<>());
					orDefault.add(id);
					groupedResumes.put(word, orDefault);
				}
				
				countByResume.put(id, skills.size());
				CcpJsonRepresentation subReportFromSkills = getSubReportFromSkills(md, id + "_" + tipoVaga);
				
				String asPrettyJson = md
						.put(JsonFields.skill, skills)
						.asPrettyJson();
				
				reports.put(fileName, subReportFromSkills);
				
				new CcpStringDecorator("c:/logs/skills/" + fileName).file().reset().append(asPrettyJson);
				System.out.println(counter++ + " = " + fileName);
				
			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println(counter++);
			}
		
		};
		queryExecutor.consumeQueryResult(query, new String[] {"profissionais2"}, "100m", 10, consumer, "curriculo.conteudo", "id", "tipoVaga");
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
		
		Set<String> keySet = reports.keySet();
		double skillsCount = 0;
		double parentsCount = 0;
		
		for (String string : keySet) {
			CcpJsonRepresentation report = reports.get(string);
			Integer parentSize = report.getAsIntegerNumber(JsonFields.parentSize);
			Integer skillSize = report.getAsIntegerNumber(JsonFields.skillSize);
			
			skillsCount += skillSize;
			parentsCount += parentSize;
		}
		
		System.out.println(skillsCount / parentsCount);
		
		List<CcpJsonRepresentation> arrayList = new ArrayList<>(reports.values());
		arrayList.sort((a, b) -> {
			
			if(false == b.containsAllFields(JsonFields.skillsPerParent)) {
				return -1;
			}

			if(false == a.containsAllFields(JsonFields.skillsPerParent)) {
				return 1;
			}

			Integer x2 = (int)(b.getAsDoubleNumber(JsonFields.skillsPerParent) * 1000) ;
			Integer x1 = (int)(a.getAsDoubleNumber(JsonFields.skillsPerParent) * 1000);
			
			return x2 - x1;
		});
		
		CcpFileDecorator report = new CcpStringDecorator("c:/logs/skills/report.txt").file().reset();
		
		for (CcpJsonRepresentation rep : arrayList) {
			report.append(rep.asUgglyJson());
		}
		

	}

	static CcpJsonRepresentation getSkillsFromText(String resumeText) {
		String text = new CcpStringDecorator(resumeText).text().stripAccents().getContent();
		Map<String, Object> sessionValues =  CcpOtherConstants.EMPTY_JSON
				 .put(JsonFields.text, text)
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
			String x = json.getAsString(JsonFields.id);
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
			
			LinkedHashSet<String> orDefault = groupedCompanies.getOrDefault(() -> initials, () -> new LinkedHashSet<>());
			orDefault.add(capitalizedCompanyName);
			groupedCompanies = groupedCompanies.put(() -> initials, orDefault);
		};
		queryExecutor.consumeQueryResult(query, new String[] {"old_recruiters"}, "1s", 10000, consumer, "id");

		ArrayList<String> arrayList = new ArrayList<> (groupedCompanies.fieldSet());
		arrayList.sort((a, b) ->{
			Set<String> set1 = groupedCompanies.getAsObject(() -> a);
			Set<String> set2 = groupedCompanies.getAsObject(() -> b);
			return set2.size() - set1.size();
		});
		
		CcpJsonRepresentation result = CcpOtherConstants.EMPTY_JSON;
		int total = 0;
		for (String string : arrayList) {
			Set<String> value = groupedCompanies.getAsObject(() -> string);
			result = result.put(() -> string, value);
			int size = value.size();
			System.out.println(string +" = " +  size);
			total += size;
		}
		System.out.println(total);
		new CcpStringDecorator("c:\\logs\\teste.json").file().reset().append(result.asPrettyJson());
	}

	static void saveResume() {
		VisEntityResume.ENTITY.delete(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, "onias85@gmail.com"));
		String path = "http://localhost:9200/profissionais2/_doc/onias85@gmail.com/_source";
		CcpHttpHandler http = new CcpHttpHandler(200, CcpOtherConstants.DO_NOTHING, path);
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
		JnEntityLoginToken.ENTITY.delete(json);
	}

	static void saveLoginToken() {
		String value = "12345678";
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityLoginToken.Fields.email, "onias85@gmail.com")
				.put(JnEntityLoginToken.Fields.token, value)
				.put(JnEntityLoginSessionValidation.Fields.ip, "127.0.0.1")
				.put(JnEntityLoginSessionValidation.Fields.userAgent, "teste");
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
		CcpJsonRepresentation oneById = JnEntityJobsnowError.ENTITY.getEntityMetaData().getOneByIdOrHandleItIfThisIdWasNotFound(json,
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
		CcpEntityField idField = new CcpEntityField("email", false, true, CcpOtherConstants.DO_NOTHING);

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
			resumes = resumes.put(() -> id, curriculo);
		}

		CcpJsonRepresentation candidatosAgrupadosPorRecrutadores = getCandidatosAgrupadosPorRecrutadores(queryMatchAll);
		CcpJsonRepresentation vagasAgrupadosPorRecrutadores = getVagasAgrupadosPorRecrutadores(intersectList);
		Set<String> recrutadores = vagasAgrupadosPorRecrutadores.fieldSet();
		List<CcpJsonRepresentation> todasAsVagas = new ArrayList<>();
		CcpJsonRepresentation res = new CcpJsonRepresentation(resumes.content);
		for (String recrutador : recrutadores) {
			List<CcpJsonRepresentation> curriculos = candidatosAgrupadosPorRecrutadores.getAsStringList(() -> recrutador)
					.stream().map(x -> res.getInnerJson(() -> x)).collect(Collectors.toList());

			List<CcpJsonRepresentation> vagas = vagasAgrupadosPorRecrutadores.getAsJsonList(() -> recrutador);

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

			this.vagasAgrupadasPorRecrutadores = this.vagasAgrupadasPorRecrutadores.addToList(() -> recrutador, vaga);
		}

	}

	static class AgruparCandidatosPorRecrutadores implements java.util.function.Consumer<CcpJsonRepresentation> {

		CcpJsonRepresentation candidatosAgrupadosPorRecrutadores = CcpOtherConstants.EMPTY_JSON;

		public void accept(CcpJsonRepresentation json) {
			String candidato = json.getAsObject(JsonFieldNames.candidate, JsonFieldNames.candidato);
			String recrutador = json.getAsString(JsonFieldNames.email);
			this.candidatosAgrupadosPorRecrutadores = this.candidatosAgrupadosPorRecrutadores.addToList(() -> recrutador,
					candidato);
		}
	}

	static CcpJsonRepresentation getVagasAgrupadosPorRecrutadores(List<Object> intersectList) {
		CcpEntityField idField = new CcpEntityField("mail", false, true, CcpOtherConstants.DO_NOTHING);
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

				String value = json.getAsTextDecorator(() -> field).content.trim().toLowerCase();

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

