# Revisão do synonyms.json — 2026-11-02

Revisão dos campos `directParent` (pré-requisitos) e `synonym` (outros nomes da mesma skill) de `documentation/jn/skills/synonyms.json`, com as skills novas. Cada linha traz a justificativa e, quando faz sentido, em quantas vagas reais a mudança age. Nenhum nome de skill foi renomeado (o nome é a chave). Os campos derivados (`parent`, `mirror`, `childrenCount`, `hasRepeatedParent`, `skillsWithCommonParents`...) foram recalculados com a mesma regra de `atualizar_taxonomia.py`, e nenhuma skill ficou com ancestral repetido.

## Resumo

| # | Item | Antes | Depois |
|---|---|---:|---:|
| 1 | Skills | 1.349 | 1.482 |
| 2 | Sinônimos | 3.208 | 2.998 |
| 3 | Grupos de iniciais (`VisEntityVirtualSkillInitialsGrouper`) | 383 | 372 |
| 4 | Palavras que o motor nunca acha | 276 | 193 |
| 5 | Skills que o motor nunca acha | 11 | 2 |
| 6 | Itens de removidas.txt presentes no arquivo | 16 | 15 |

O total de sinônimos caiu porque saíram 534 inversões e 138 quebras sem sentido do nome (seções 7 e 8), que não aparecem nas vagas ou casam frases sem relação.

| # | Tipo de mudança | Quantidade | Seção |
|---|---|---:|---|
| 1 | Fusões de skills duplicadas | 13 | 1 |
| 2 | Skills novas | 146 | 2 |
| 3 | Pais corrigidos | 217 | 3 |
| 4 | Sinônimos acrescentados | 362 | 4 |
| 5 | Sinônimos que mudaram de skill | 16 | 5 |
| 6 | Sinônimos removidos um a um | 85 | 6 |
| 7 | Inversões removidas | 534 | 7 |
| 8 | Quebras do nome removidas | 138 | 8 |
| 9 | Descrições corrigidas | 4 | 9 |

## Como as vagas foram contadas

- Fonte: os arquivos de `C:\z` (932 HTML do Telegram e o `z.txt` do WhatsApp), lidos como na carga: `VisTelegramMessages`/`VisWhatsAppMessages`, corte do texto (`VisTelegramPositionText.cut`), contato (`VisTelegramContact.find`) e remoção dos links.
- Vagas repetidas foram contadas uma vez pela chave do índice `vis_telegram_position` (contato + skills + dia): 204.856 chaves, contra 204.855 documentos no índice.
- Cada palavra foi procurada com a regra do motor (`VisSkillsInText`): o texto vai para maiúsculas (sem tirar acentos); a palavra só é candidata se as 2 primeiras letras dela iniciam alguma palavra do texto; com menos de 7 caracteres precisa ser uma palavra inteira do texto; com 7 ou mais basta ser um trecho, mas é descartada quando uma palavra mais longa achada no mesmo texto a contém.
- "Vagas" nas tabelas = chaves em que a palavra (ou alguma palavra da skill nova) é achada. Para as palavras removidas, "vagas em que casava" é em quantas vagas a palavra era achada com o arquivo antigo, ou seja, quantos achados (quase sempre falsos) deixam de existir.
- "Ficam sem a skill" (seções 6 a 8): das vagas em que a palavra removida casava, as que, com o arquivo novo, não têm a skill achada por outra palavra dela nem por uma descendente (o motor põe os ancestrais de cada skill achada). É a perda real da remoção.

## 1. Fusões de skills duplicadas

O nome e os sinônimos da skill que some viram sinônimos da que fica; os filhos dela passam para a que fica.

| # | Some | Fica | Palavras levadas | Filhos repassados | Justificativa |
|---|---|---|---|---|---|
| 1 | ECLIPSE MICROPROFILE GRAPHQL | MICROPROFILE GRAPHQL | `ECLIPSE MICROPROFILE GRAPHQL` | — | Duplicata: é a mesma especificação MicroProfile GraphQL; o pai ECLIPSE (a IDE) estava errado — o "Eclipse" do nome é a fundação, não a IDE. |
| 2 | ESB | BARRAMENTO | `ESB` | — | Duplicata: ESB (Enterprise Service Bus) é o barramento de serviços; as duas skills tinham o mesmo pai (SOA). |
| 3 | FI-AA | SAP AA | `FI-AA` | — | Duplicata: FI-AA é o SAP Asset Accounting; além disso o nome FI-AA (5 caracteres com hífen) nunca casava em texto. |
| 4 | HCM | SAP HR | `HCM`, `SAP HCM`, `HUMAN CAPITAL MANAGEMENT`, `SAP-HCM`, `SAPHCM`, `SAP.HCM` | — | Duplicata: SAP HCM é o nome atual do SAP HR (recursos humanos e folha). |
| 5 | MQSERIES | IBM MQ | `MQSERIES`, `MQ-SERIES`, `MQ SERIES`, `MQ.SERIES` | — | Duplicata: MQSeries é o nome antigo do IBM MQ (a própria descrição diz isso). |
| 6 | MVCNODE | NODEMVC | `MVCNODE` | — | Duplicata: MVCNODE e NODEMVC têm os mesmos pais (NODE, MVC) e a mesma descrição. |
| 7 | ORACLE ANALYTICS | OAC | `ORACLE ANALYTICS` | — | Duplicata: as duas descrições são "Oracle Analytics Cloud". |
| 8 | RGPD | GDPR | `RGPD` | — | Duplicata: RGPD é a sigla em português do GDPR (a própria descrição diz isso). |
| 9 | SAP CLOUD PLATFORM | SAP BTP | `SAP CLOUD PLATFORM` | — | Duplicata: SAP Cloud Platform é o nome antigo do SAP BTP (a própria descrição diz isso). |
| 10 | SPRING WEB MVC | SPRINGMVC | `SPRING WEB MVC`, `SPRINGWEBMVC`, `SPRING.WEBMVC`, `SPRING WEBMVC`, `SPRING-WEBMVC` | — | Duplicata: Spring Web MVC é o nome oficial do módulo Spring MVC. |
| 11 | SPRINGDATA | SPRING DATA | — | — | Duplicata: SPRINGDATA e SPRING DATA são o mesmo projeto e já dividiam 14 sinônimos (o motor achava as duas skills na mesma vaga); SPRINGDATA ainda tinha BIGDATA como pai, que não é pré-requisito do Spring Data. |
| 12 | WEBCLIENT | SPRING WEBCLIENT | `WEBCLIENT`, `WEB CLIENT`, `WEB-CLIENT`, `WEB.CLIENT` | SPRING WEBCLIENT | Duplicata: o WEBCLIENT do arquivo é o cliente HTTP reativo do Spring WebFlux. |
| 13 | WMWARE | VMWARE | `WMWARE`, `WM-WARE`, `WM.WARE`, `WM WARE` | — | Duplicata com erro de digitação (WMWARE) de VMWARE. |

## 2. Skills novas

Nenhuma consta em `removidas.txt` (comparação sem espaço, ponto, hífen e barra, em maiúsculas). Quando uma palavra estava como sinônimo de outra skill, a coluna "Veio de" mostra de onde ela saiu.

| # | Skill | Pais | Sinônimos | Veio de | Vagas | Descrição | Justificativa |
|---|---|---|---|---|---:|---|---|
| 1 | ANDROID STUDIO | ANDROID, IDE | — | — | 685 | Android Studio — IDE oficial de desenvolvimento Android. | IDE muito citada nas vagas de Android que não existia. |
| 2 | ARKIT | IOS | — | — | 118 | ARKit — realidade aumentada no iOS. | Framework do iOS citado nas vagas que não existia. |
| 3 | AXURE | UX | — | — | 133 | Axure RP — wireframes e protótipos interativos. | Ferramenta de prototipação citada nas vagas de UX que não existia. |
| 4 | AZURE DATA FACTORY | AZURE, ETL | `DATA FACTORY`, `DATAFACTORY` | — | 826 | Azure Data Factory — serviço de integração e orquestração de dados (ETL/ELT) do Azure. | Ferramenta de ETL do Azure muito citada nas vagas de dados que não existia. A skill ADF do arquivo é o Oracle ADF (framework Java), por isso a sigla não foi usada. |
| 5 | AZURE EVENT HUBS | AZURE, MENSAGERIA | `EVENT HUB`, `EVENTHUB` | — | 606 | Azure Event Hubs — ingestão de streams de eventos do Azure (compatível com Kafka). | Serviço de mensageria do Azure citado nas vagas que não existia; mesmos pais do AZURE SERVICE BUS. |
| 6 | BIGTABLE | GCP, NOSQLDATABASE | `BIG TABLE` | — | 123 | Google Cloud Bigtable — banco NoSQL de colunas largas do GCP. | Banco NoSQL do GCP citado nas vagas que não existia. |
| 7 | BITBUCKET PIPELINES | BITBUCKET, DEVOPS | `BITBUCKET PIPELINE` | — | 156 | Bitbucket Pipelines — CI/CD integrado ao Bitbucket. | Ferramenta de CI/CD citada nas vagas que não existia; mesmos pais do GITHUB ACTIONS. |
| 8 | BIZAGI | BPM | `BIZAGE`, `BIZZAGE`, `BIZZAGI` | BIZAGI (BPM), BIZAGE (BPM), BIZZAGE (BPM), BIZZAGI (BPM) | 247 | Bizagi — suíte de BPM com modelador BPMN, motor de processos e portal de automação. | Bizagi é uma ferramenta de BPM, não outro nome de BPM: estava como sinônimo e transformava toda vaga de Bizagi em "BPM" genérico. |
| 9 | BLUE PRISM | RPA | `BLUEPRISM` | — | 263 | Blue Prism — plataforma de RPA. | Ferramenta de RPA citada nas vagas que não existia (UIPATH e AUTOMATION ANYWHERE já existiam). |
| 10 | BOOMI | IPAAS | `DELL BOOMI`, `BOOMI ATOMSPHERE` | BOOMI (BOOMI API MANAGEMENT) | 40 | Boomi — plataforma de integração como serviço (iPaaS). | BOOMI estava como sinônimo de BOOMI API MANAGEMENT, mas as vagas pedem a plataforma de integração (iPaaS). |
| 11 | CARTHAGE | SWIFT, DEPENDENCY MANAGER | — | — | 192 | Carthage — gerenciador de dependências de projetos iOS. | Gerenciador de dependências citado junto do CocoaPods nas vagas de iOS que não existia. |
| 12 | CELERY | PYTHON | — | — | 101 | Celery — fila de tarefas assíncronas e distribuídas em Python. | Biblioteca citada nas vagas de Python que não existia. |
| 13 | CENTOS | LINUX | `CENT OS` | — | 433 | CentOS — distribuição Linux derivada do Red Hat Enterprise Linux. | Distribuição Linux de servidores que não existia (RHEL já é filho de LINUX). |
| 14 | CISCO | IP | `CISCO SYSTEMS` | — | 1.871 | Cisco — roteadores, switches e firewalls da Cisco (IOS, CCNA). | Fabricante de equipamentos de rede mais citado nas vagas de infraestrutura, que não existia; pressupõe redes IP. |
| 15 | CLEAN CODE | (raiz) | `CLEANCODE`, `CLEAN-CODE`, `CODIGO LIMPO`, `CÓDIGO LIMPO` | — | 5.086 | Clean Code — práticas de código legível e fácil de manter (nomes, funções pequenas, testes). | Uma das práticas mais pedidas nas vagas que não existia (CLEAN ARCHITECTURE e SOLID já existiam). |
| 16 | CLOUDFLARE | CDN | `CLOUD FLARE` | CLOUDFLARE (CLOUDFLARE API GATEWAY), CLOUD FLARE (CLOUDFLARE API GATEWAY) | 45 | Cloudflare — CDN, DNS, WAF e proteção contra DDoS na borda da rede. | CLOUDFLARE estava como sinônimo de CLOUDFLARE API GATEWAY, mas as vagas citam a Cloudflare como CDN/WAF, não o gateway de APIs. |
| 17 | CODEBUILD | AWS, DEVOPS | `AWS CODEBUILD` | — | 157 | AWS CodeBuild — serviço de build e testes de CI da AWS. | Ferramenta de CI da AWS citada nas vagas que não existia; mesmos pais do CODEPIPELINE. |
| 18 | CODEDEPLOY | AWS, DEVOPS | `AWS CODEDEPLOY` | — | 103 | AWS CodeDeploy — automação de deploy em EC2, Lambda e servidores da AWS. | Ferramenta de deploy da AWS citada nas vagas que não existia; mesmos pais do CODEPIPELINE. |
| 19 | CODEPIPELINE | AWS, DEVOPS | `AWS CODEPIPELINE` | — | 151 | AWS CodePipeline — orquestração de pipelines de CI/CD da AWS. | Ferramenta de CI/CD da AWS citada nas vagas que não existia; mesmos pais do GITHUB ACTIONS (provedor + DevOps). |
| 20 | COGNITO | AWS, IAM | `AWS COGNITO`, `AMAZON COGNITO` | — | 257 | Amazon Cognito — cadastro, login e federação de usuários de aplicações na AWS. | Serviço de identidade da AWS citado nas vagas que não existia; mesma categoria de OKTA e KEYCLOAK (IAM). |
| 21 | CONFLUENT | KAFKA | — | — | 407 | Confluent — plataforma Kafka da Confluent (Schema Registry, Connect, ksqlDB, Confluent Cloud). | Distribuição do Kafka citada nas vagas que não existia. |
| 22 | COREDATA | IOS | — | — | 86 | Core Data — persistência de dados do iOS/macOS. | Framework do iOS citado nas vagas que não existia ("Core Data" separado é expressão genérica, por isso ficou só a forma junta). |
| 23 | CRASHLYTICS | FIREBASE | — | — | 173 | Firebase Crashlytics — relatório de falhas de aplicativos mobile. | Ferramenta citada nas vagas mobile que não existia. |
| 24 | CRYSTAL REPORTS | REPORT | `CRYSTAL REPORT`, `CRISTAL REPORT` | — | 307 | Crystal Reports — gerador de relatórios da SAP. | Ferramenta de relatórios citada nas vagas que não existia. |
| 25 | DAGGER | ANDROID | `DAGER`, `DAGGER2`, `DAGGER 2` | DAGGER (KOTLIN), DAGER (KOTLIN) | 225 | Dagger — framework de injeção de dependência em tempo de compilação, padrão no Android. | Dagger estava como sinônimo de KOTLIN: é uma biblioteca de injeção de dependência (do Android/Java), não a linguagem. |
| 26 | DART | POO | — | DART (FLUTTER) | 286 | Dart — linguagem orientada a objetos do Google, usada pelo Flutter. | Dart é a linguagem do Flutter e estava como sinônimo dele; é pré-requisito do Flutter, não outro nome. |
| 27 | DATA GUARD | ORACLE | `DATAGUARD` | — | 262 | Oracle Data Guard — alta disponibilidade e recuperação de desastre do banco Oracle. | Recurso do Oracle citado nas vagas de DBA que não existia. |
| 28 | DATAPROC | GCP, HADOOP | — | — | 112 | Google Dataproc — clusters gerenciados de Hadoop e Spark do GCP. | Serviço de big data do GCP citado nas vagas que não existia; pressupõe o ecossistema Hadoop. |
| 29 | DATAVERSE | POWERAPPS | `COMMON DATA SERVICE` | — | 96 | Microsoft Dataverse — base de dados da Power Platform. | Componente da Power Platform citado nas vagas que não existia. |
| 30 | DEPENDENCY INJECTION | PATTERNS | `INJEÇÃO DE DEPENDÊNCIA`, `INJECAO DE DEPENDENCIA`, `INJEÇÃO DE DEPENDÊNCIAS`, `INVERSION OF CONTROL`, `INVERSÃO DE CONTROLE` | — | 817 | Injeção de dependência / inversão de controle — padrão em que os objetos recebem suas dependências de fora. | Padrão citado nas vagas (Spring, .NET, Angular) que não existia. |
| 31 | DEVEXPRESS | DOTNETFRONTEND | — | — | 189 | DevExpress — suíte de componentes de interface para .NET (WinForms, WPF, ASP.NET, Blazor). | Suíte de componentes citada nas vagas de .NET que não existia (mesma categoria do TELERIK, mas focada em .NET). |
| 32 | DHCP | IP | `DYNAMIC HOST CONFIGURATION PROTOCOL` | — | 1.230 | DHCP — protocolo que distribui endereços IP e configurações de rede automaticamente. | Protocolo de rede pedido em vagas de infraestrutura e suporte que não existia (DNS, IP e TCP já existiam); pressupõe endereçamento IP. |
| 33 | DOCKER COMPOSE | DOCKER | `DOCKER-COMPOSE`, `DOCKERCOMPOSE` | — | 732 | Docker Compose — definição e execução de aplicações com vários containers. | Ferramenta do Docker muito citada nas vagas que não existia. |
| 34 | DOCUMENTDB | AWS, MONGODB | `AMAZON DOCUMENTDB` | — | 198 | Amazon DocumentDB — banco de documentos gerenciado da AWS, compatível com a API do MongoDB. | Serviço da AWS citado nas vagas que não existia; compatível com MongoDB, que é o pré-requisito. |
| 35 | DOTNET CORE | DOTNET | `NET CORE`, `NETCORE` | — | 10.265 | .NET Core — versão multiplataforma e open source do .NET (a partir do .NET 5 chamada só de .NET). | Um dos termos mais citados nas vagas de .NET e não existia; "NET CORE" também casa ".NET Core" e "ASP.NET Core". |
| 36 | ECR | AWS, DOCKER | `AWS ECR`, `AMAZON ECR`, `ELASTIC CONTAINER REGISTRY` | — | 228 | Amazon ECR — registro de imagens de containers da AWS. | Serviço da AWS citado junto de ECS e EKS nas vagas que não existia; pressupõe Docker. |
| 37 | ELASTIC BEANSTALK | AWS | `BEANSTALK` | — | 652 | AWS Elastic Beanstalk — PaaS da AWS para publicar aplicações sem gerenciar servidores. | Serviço da AWS citado nas vagas que não existia (AWS e PAAS como pais repetiriam CLOUD como ancestral). |
| 38 | ELASTICACHE | AWS, CACHE | `ELASTIC CACHE`, `ELASTICCACHE` | — | 517 | Amazon ElastiCache — Redis e Memcached gerenciados da AWS. | Serviço da AWS citado nas vagas que não existia; é cache na AWS. |
| 39 | ELB | AWS, LOADBALANCE | `ELASTIC LOAD BALANCING`, `ELASTIC LOAD BALANCER`, `AWS ELB`, `AWS ALB` | — | 659 | Elastic Load Balancing — balanceadores de carga gerenciados da AWS (ALB, NLB). | Serviço da AWS citado nas vagas ("EC2, ELB, S3") que não existia; é balanceamento de carga na AWS. |
| 40 | ELECTRON | NODE | `ELECTRONJS` | — | 160 | Electron — aplicações desktop com HTML, CSS e JavaScript sobre Node.js e Chromium. | Framework de aplicações desktop citado nas vagas que não existia; pressupõe Node. Atenção: com 8 letras o motor casa o nome como trecho, e 54 das 153 vagas são "electronic"/"electronics" (ver observações). |
| 41 | ELK | ELASTICSEARCH, MONITOR123 | `ELK STACK`, `ELASTIC STACK`, `STACK ELK` | — | 942 | ELK — pilha Elasticsearch, Logstash e Kibana para centralizar e analisar logs. | Sigla muito citada nas vagas que não casava com nenhuma skill; mesmos pais do LOGSTASH (pôr KIBANA e LOGSTASH como pais repetiria ELASTICSEARCH como ancestral). |
| 42 | EMR | AWS, HADOOP | `AWS EMR`, `AMAZON EMR`, `ELASTIC MAPREDUCE` | — | 331 | Amazon EMR — clusters gerenciados de Hadoop e Spark da AWS. | Serviço de big data da AWS citado nas vagas que não existia; pressupõe o ecossistema Hadoop. |
| 43 | ERLANG | BACKEND | `ERLANG OTP` | ERLANG (ELIXIR) | 100 | Erlang — linguagem funcional concorrente da Ericsson com a plataforma OTP, base da BEAM. | Erlang estava como sinônimo de ELIXIR: é outra linguagem (o Elixir roda na máquina virtual dela). |
| 44 | FIRESTORE | FIREBASE | — | — | 124 | Cloud Firestore — banco de documentos NoSQL do Firebase/GCP. | Banco do Firebase citado nas vagas que não existia (FIREBASE e NOSQLDATABASE como pais repetiriam DATABASE). |
| 45 | GITLAB CI | GITLAB, DEVOPS | `GITLABCI`, `GITLAB-CI`, `GITLAB PIPELINE` | — | 495 | GitLab CI/CD — pipelines de integração e entrega contínuas do GitLab. | Ferramenta de CI/CD muito citada nas vagas que não existia; mesmos pais do GITHUB ACTIONS (plataforma + DevOps). |
| 46 | GITOPS | DEVOPSGIT | `GIT OPS` | — | 88 | GitOps — operação de infraestrutura e deploys a partir de repositórios Git (Argo CD, Flux). | Prática citada nas vagas de DevOps que não existia; usa a categoria de combinação DevOps + Git que já existe. |
| 47 | GLPI | ITSM | — | — | 63 | GLPI — gestão de chamados e inventário de TI open source. | Ferramenta de chamados citada nas vagas de suporte que não existia. |
| 48 | GOLDENGATE | ORACLE | `GOLDEN GATE` | — | 165 | Oracle GoldenGate — replicação e captura de dados em tempo real. | Ferramenta da Oracle citada nas vagas de DBA que não existia. |
| 49 | GOOGLE CLOUD FUNCTIONS | GCP, SERVERLESS | `CLOUD FUNCTION` | — | 206 | Google Cloud Functions — funções serverless do GCP. | Serviço serverless do GCP citado nas vagas que não existia (AZURE FUNCTIONS e LAMBDA já existiam). |
| 50 | GOOGLE CLOUD STORAGE | GCP, BUCKET | `GCS` | — | 94 | Google Cloud Storage — armazenamento de objetos (buckets) do GCP. | Serviço do GCP citado nas vagas que não existia; mesmo papel do S3 na AWS. |
| 51 | GPO | ACTIVE DIRECTORY | `GROUP POLICY`, `GROUP POLICIES`, `POLITICAS DE GRUPO`, `POLÍTICAS DE GRUPO` | — | 587 | Group Policy Objects — políticas de configuração aplicadas a usuários e computadores pelo Active Directory. | Administração de Active Directory muito pedida em vagas de infraestrutura que não existia. |
| 52 | IDENTITYSERVER | ASPNET, OIDC | `IDENTITY SERVER` | — | 151 | IdentityServer — servidor de OpenID Connect e OAuth 2.0 para ASP.NET Core. | Ferramenta de autenticação citada nas vagas de .NET que não existia. |
| 53 | INVISION | UX | — | — | 302 | InVision — prototipação e colaboração de design de interfaces. | Ferramenta de design citada nas vagas de UX que não existia (FIGMA e ADOBEXD já existiam). |
| 54 | ITSM | (raiz) | `IT SERVICE MANAGEMENT`, `GESTAO DE SERVICOS DE TI`, `GESTÃO DE SERVIÇOS DE TI` | — | 771 | IT Service Management — gestão de serviços de TI: incidentes, requisições, mudanças e catálogo (ServiceNow, Remedy, Cherwell). | Categoria que faltava: ServiceNow, Remedy e Cherwell eram filhos de LOWCODE/DOCUMENTATION123, que não são o que essas ferramentas pressupõem. ITIL está em removidas.txt, por isso o nome é ITSM. |
| 55 | JUPYTER | PYTHON | `JUPYTER NOTEBOOK`, `JUPYTERLAB`, `JUPYTER LAB` | JUPYTER (JUNIT) | 153 | Jupyter — notebooks interativos para Python, padrão em ciência de dados. | JUPYTER estava como sinônimo de JUNIT (confusão com o "JUnit Jupiter"): o Jupyter Notebook é de Python. |
| 56 | KAFKA STREAMS | KAFKA | `KSTREAMS`, `KSTREAM` | — | 405 | Kafka Streams — biblioteca de processamento de streams do Kafka. | Biblioteca citada nas vagas de Kafka que não existia. |
| 57 | KOTLIN COROUTINES | KOTLIN | `COROUTINE` | — | 140 | Coroutines — programação assíncrona do Kotlin. | Recurso do Kotlin citado nas vagas de Android e backend que não existia. |
| 58 | KSQLDB | KAFKA STREAMS | `KSQL` | — | 357 | ksqlDB — consultas SQL sobre streams do Kafka (construído sobre o Kafka Streams). | Ferramenta citada nas vagas de Kafka que não existia. |
| 59 | KVM | VIRTUALIZATION, LINUX | `KERNEL-BASED VIRTUAL MACHINE` | — | 102 | KVM — hypervisor do kernel Linux. | Hypervisor citado ao lado de VMware e Hyper-V nas vagas que não existia; pressupõe Linux. |
| 60 | LESS | CSS | `LESSCSS`, `LESS CSS` | — | 1.234 | Less — pré-processador de CSS. | Pré-processador de CSS citado junto do Sass ("Sass/Less") nas vagas que não existia. |
| 61 | LIGHTNING WEB COMPONENTS | SALESFORCE | `LWC`, `LIGHTNING WEB COMPONENT` | — | 346 | Lightning Web Components (LWC) — framework de componentes de interface da Salesforce, sobre Web Components. | Tecnologia da Salesforce muito citada nas vagas que não existia ("LWC" em 292 vagas). |
| 62 | MACOS | SO | `MAC OSX`, `MAC OS X`, `OSX` | — | 203 | macOS — sistema operacional dos computadores da Apple. | Sistema operacional citado nas vagas (desenvolvimento iOS e suporte) que não existia. "MAC OS" com espaço e 6 letras o motor não acha, por isso não entrou. |
| 63 | MAPREDUCE | HADOOP | `MAP REDUCE`, `MAP-REDUCE` | — | 126 | MapReduce — modelo de processamento distribuído do Hadoop. | Componente do Hadoop citado nas vagas que não existia (HDFS, HIVE e IMPALA já existiam). |
| 64 | MATERIAL DESIGN | UX | — | — | 420 | Material Design — sistema de design do Google (componentes, grid e guias visuais). | Sistema de design citado nas vagas de frontend e UX que não existia. |
| 65 | MCSA | WINDOWS SERVER | `MICROSOFT CERTIFIED SOLUTIONS ASSOCIATE` | — | 390 | MCSA — certificação Microsoft de administração do Windows Server (descontinuada em 2021, ainda exigida nas vagas). | Certificação pedida em vagas de infraestrutura Windows; o arquivo já tem certificações (AZ900, AZ104, CTFL, PMP). |
| 66 | MICROSERVICES | (raiz) | `MICROSERVICE`, `MICRO SERVICES`, `MICRO-SERVICES`, `MICROSSERVICOS`, `MICROSSERVIÇOS`, `MICROSERVICOS`, `MICROSERVIÇOS`, `MICRO SERVICOS`, `MICRO SERVIÇOS`, `MICROSSERVICO`, `MICROSSERVIÇO` | — | 16.796 | Microsserviços — arquitetura de serviços pequenos, independentes e implantados separadamente. | Arquitetura citada em grande parte das vagas de backend que não existia como skill. |
| 67 | MICROSTRATEGY | B.I | — | — | 360 | MicroStrategy — plataforma de BI e analytics. | Ferramenta de BI citada nas vagas que não existia. |
| 68 | MOBX | REACTJS | — | — | 191 | MobX — gerenciamento de estado reativo, usado sobretudo com React. | Biblioteca de estado citada nas vagas que não existia (REDUX e ZUSTAND já existiam). |
| 69 | MONGOOSE | NODE, MONGODB | — | — | 187 | Mongoose — modelagem de documentos MongoDB (ODM) para Node.js. | Biblioteca citada nas vagas de Node que não existia. |
| 70 | MQTT | MENSAGERIA, PROTOCOL | `MESSAGE QUEUING TELEMETRY TRANSPORT` | — | 348 | MQTT — protocolo leve de mensageria publish/subscribe, padrão em IoT. | Protocolo de mensageria pedido nas vagas de IoT e integração que não existia. |
| 71 | MVVM | PATTERNS | `MODEL VIEW VIEW MODEL`, `MODEL-VIEW-VIEW-MODEL`, `MODEL-VIEW VIEW-MODEL`, `MODEL-VIEW.VIEW-MODEL`, `MODEL-VIEWVIEW-MODEL` | MVVM (MVC), MODEL VIEW VIEW MODEL (MVC), MODEL-VIEW-VIEW-MODEL (MVC), MODEL-VIEW VIEW-MODEL (MVC), MODEL-VIEW.VIEW-MODEL (MVC), MODEL-VIEWVIEW-MODEL (MVC) | 2.527 | Model-View-ViewModel — padrão de apresentação com data binding entre a view e o view model (WPF, Xamarin, Angular, Android). | MVVM é outro padrão, não outro nome de MVC: estava como sinônimo de MVC. |
| 72 | NESSUS | PENTEST | `TENABLE NESSUS` | — | 154 | Nessus — scanner de vulnerabilidades da Tenable. | Ferramenta de varredura de vulnerabilidades citada nas vagas de segurança que não existia. |
| 73 | NEW RELIC | CLOUDMONITOR | `NEWRELIC`, `NEW-RELIC` | — | 849 | New Relic — plataforma SaaS de observabilidade e APM. | Ferramenta de APM citada nas vagas que não existia; mesmo pai do DATADOG. |
| 74 | NGRX | ANGULAR | — | — | 116 | NgRx — gerenciamento de estado do Angular (padrão Redux sobre RxJS). | Biblioteca de estado do Angular citada nas vagas que não existia (ANGULAR e RXJS como pais repetiriam JAVASCRIPT). |
| 75 | ODBC | SQLDATABASE | `OPEN DATABASE CONNECTIVITY` | — | 128 | ODBC — interface padrão de acesso a bancos de dados. | Tecnologia de acesso a dados citada nas vagas que não existia. |
| 76 | ODI | ETL | `ORACLE DATA INTEGRATOR` | ODI (ETL), ORACLE DATA INTEGRATOR (ETL) | 403 | Oracle Data Integrator — ferramenta de ETL/ELT da Oracle. | ODI estava como sinônimo de ETL: é uma ferramenta específica da Oracle, não outro nome do processo. |
| 77 | ORACLE APEX | ORACLE, LOWCODE | `ORACLE APPLICATION EXPRESS` | — | 201 | Oracle APEX (Application Express) — plataforma low-code de aplicações web sobre o banco Oracle. | Plataforma low-code da Oracle que não existia; a palavra APEX sozinha continua em SALESFORCE (linguagem Apex). |
| 78 | ORACLE FORMS | PLSQL | — | — | 310 | Oracle Forms — ferramenta de desenvolvimento de telas sobre o banco Oracle, programada em PL/SQL. | Ferramenta Oracle muito citada nas vagas que não existia; pressupõe PL/SQL. |
| 79 | ORACLE RAC | ORACLE | `RAC`, `REAL APPLICATION CLUSTERS`, `REAL APPLICATION CLUSTER` | — | 281 | Oracle RAC (Real Application Clusters) — banco Oracle em cluster ativo-ativo. | Recurso do Oracle citado nas vagas de DBA que não existia. |
| 80 | ORACLE SERVICE BUS | BARRAMENTO | `OSB` | — | 301 | Oracle Service Bus — ESB da Oracle. | Barramento de serviços da Oracle citado nas vagas que não existia. |
| 81 | ORACLE SOA SUITE | SOA | `SOA SUITE`, `ORACLE SOA` | — | 351 | Oracle SOA Suite — middleware de integração da Oracle (BPEL, mediador, adaptadores). | Middleware citado nas vagas de integração que não existia. |
| 82 | OSGI | JAVA | — | — | 91 | OSGi — sistema de módulos dinâmicos para Java (Eclipse, Karaf, AEM). | Tecnologia Java citada nas vagas que não existia. |
| 83 | OSPF | PROTOCOL | `OPEN SHORTEST PATH FIRST` | — | 452 | OSPF — protocolo de roteamento interno (link-state) de redes IP. | Protocolo de roteamento pedido em vagas de redes que não existia; mesmo pai do BGP. |
| 84 | OTRS | ITSM | — | — | 85 | OTRS — sistema de chamados (help desk e ITSM) open source. | Ferramenta de chamados citada nas vagas de suporte que não existia. |
| 85 | PALO ALTO | FIREWALL | `PALOALTO`, `PALO-ALTO`, `PALO ALTO NETWORKS` | — | 434 | Palo Alto Networks — firewalls de nova geração (NGFW) e plataforma de segurança de rede. | Fabricante de firewalls citado nas vagas de segurança de redes que não existia (FORTINET e SONICWALL já eram filhos de FIREWALL). |
| 86 | PEOPLESOFT | ORACLEERP | `PEOPLE SOFT` | — | 77 | PeopleSoft — ERP/HCM da Oracle. | ERP da Oracle citado nas vagas que não existia (JDE e NETSUITE já são filhos de ORACLEERP). |
| 87 | POWER AUTOMATE | LOWCODE, RPA | `POWERAUTOMATE`, `MICROSOFT FLOW` | — | 481 | Power Automate — automação de fluxos (low-code) e RPA da Microsoft. | Ferramenta de automação muito citada nas vagas que não existia; cobre fluxos low-code e robôs de RPA. |
| 88 | POWER PLATFORM | LOWCODE | `POWERPLATFORM` | — | 385 | Microsoft Power Platform — Power BI, Power Apps, Power Automate e Dataverse. | Plataforma citada nas vagas que não existia; mesmo pai do POWERAPPS. |
| 89 | POWER QUERY | POWERBI | `POWERQUERY` | — | 158 | Power Query — preparação e transformação de dados (linguagem M) do Power BI e do Excel. | Recurso do Power BI citado nas vagas que não existia. |
| 90 | PRTG | MONITOR123 | `PRTG NETWORK MONITOR`, `PAESSLER PRTG` | — | 343 | PRTG — monitoramento de redes e servidores da Paessler. | Ferramenta de monitoramento citada nas vagas de infraestrutura que não existia (NAGIOS e ZABBIX já existiam). |
| 91 | QUASAR | VUE | — | — | 82 | Quasar — framework sobre o Vue.js para web, mobile e desktop. | Framework do Vue citado nas vagas que não existia. |
| 92 | QUICKSIGHT | AWS, B.I | — | — | 73 | Amazon QuickSight — ferramenta de BI e dashboards da AWS. | Ferramenta de BI da AWS citada nas vagas que não existia. |
| 93 | REDUX SAGA | REDUX | `REDUX-SAGA`, `REDUXSAGA`, `REDUX SAGAS`, `REDUX-SAGAS`, `SAGAS` | — | 226 | redux-saga — middleware do Redux para efeitos colaterais assíncronos com generators. | Biblioteca muito citada nas vagas de React que não existia. "SAGAS" entrou aqui e não na skill SAGA (padrão de transações distribuídas): em 44 das 48 vagas em que aparece, é a biblioteca ("React com Sagas e Redux"). |
| 94 | REMOTE DESKTOP SERVICES | VIRTUALIZATIONWINDOWS | `REMOTE DESKTOP`, `TERMINAL SERVER`, `TERMINAL SERVICES`, `ÁREA DE TRABALHO REMOTA`, `AREA DE TRABALHO REMOTA`, `RDP` | — | 110 | Remote Desktop Services (antigo Terminal Services) — sessões e aplicações remotas do Windows Server, pelo protocolo RDP. | O que a skill RDS descrevia antes (ela passou a ser o Amazon RDS, que é o que as vagas querem dizer com a sigla). Fica com o mesmo pai e com os nomes que as vagas usam para o serviço; RDP é o protocolo dele. |
| 95 | RESPONSIVE DESIGN | CSS, UX | `RESPONSIVO`, `RESPONSIVOS`, `RESPONSIVA`, `RESPONSIVIDADE`, `RESPONSIVIDADES`, `RESPONSIVE`, `RESPONSIVITY` | RESPONSIVO (UX), RESPONSIVOS (UX), RESPONSIVIDADE (UX), RESPONSIVIDADES (UX), RESPONSIVE (UX), RESPONSIVITY (UX) | 3.040 | Design responsivo — páginas que se adaptam a qualquer tamanho de tela com media queries, grids e layouts fluidos. | Os termos de responsividade estavam como sinônimos de UX, mas são técnica de CSS (media queries), não outro nome de experiência do usuário. |
| 96 | ROUTE53 | AWS, DNS | `ROUTE 53` | — | 637 | Amazon Route 53 — serviço de DNS gerenciado da AWS. | Serviço da AWS citado nas vagas que não existia; é DNS na AWS. |
| 97 | RXJAVA | JAVA, RX | `RX JAVA` | — | 297 | RxJava — programação reativa na JVM, muito usada no Android. | Biblioteca citada nas vagas de Android que não existia. |
| 98 | RXJS | RX, JAVASCRIPT | — | — | 376 | RxJS — programação reativa (Observables) em JavaScript, base do Angular. | Biblioteca citada nas vagas de Angular que não existia. |
| 99 | RXSWIFT | SWIFT, RX | — | — | 211 | RxSwift — programação reativa em Swift. | Biblioteca citada nas vagas de iOS que não existia. |
| 100 | SALESFORCE COMMERCE CLOUD | SALESFORCE, ECOMERCE | `SFCC`, `DEMANDWARE`, `B2C COMMERCE` | — | 215 | Salesforce Commerce Cloud (antigo Demandware) — plataforma de e-commerce da Salesforce. | Plataforma de e-commerce citada nas vagas que não existia. "COMMERCE CLOUD" sozinho não entrou: Oracle, SAP e Adobe têm produtos com o mesmo nome, e em 32 das 68 vagas em que aparece não há Salesforce por perto. |
| 101 | SALESFORCE MARKETING CLOUD | SALESFORCE | `MARKETING CLOUD`, `SFMC` | — | 425 | Salesforce Marketing Cloud — automação de marketing da Salesforce. | Produto da Salesforce citado nas vagas que não existia. |
| 102 | SALESFORCE SALES CLOUD | SALESFORCE | `SALES CLOUD` | — | 331 | Salesforce Sales Cloud — CRM de vendas da Salesforce. | Produto da Salesforce citado nas vagas que não existia ("SAP Sales Cloud" vai para SAP C4C, a palavra mais longa vence). |
| 103 | SALESFORCE SERVICE CLOUD | SALESFORCE | `SERVICE CLOUD` | — | 523 | Salesforce Service Cloud — atendimento ao cliente da Salesforce. | Produto da Salesforce citado nas vagas que não existia. |
| 104 | SAP BODS | SAP, ETL | `BODS`, `SAP DATA SERVICES`, `BUSINESSOBJECTS DATA SERVICES`, `BUSINESS OBJECTS DATA SERVICES` | — | 323 | SAP BusinessObjects Data Services — ferramenta de ETL e qualidade de dados da SAP. | Ferramenta de ETL da SAP citada nas vagas que não existia. |
| 105 | SAP BUSINESS ONE | SAP | `SAPB1`, `BUSINESS ONE` | — | 396 | SAP Business One — ERP da SAP para pequenas e médias empresas. | ERP citado nas vagas que não existia ("SAP B1" com espaço e 6 caracteres o motor não acha, por isso não entrou). |
| 106 | SAP C4C | SAP CRM | `C4C`, `SAP CLOUD FOR CUSTOMER`, `SAP SALES CLOUD`, `SAP SERVICE CLOUD` | — | 93 | SAP Cloud for Customer (C4C) — CRM em nuvem da SAP (Sales Cloud e Service Cloud). | CRM em nuvem da SAP citado nas vagas que não existia. |
| 107 | SAP FICO | SAP FI | `SAP FI/CO`, `SAP FI-CO`, `SAP FI CO`, `FI/CO` | — | 1.407 | SAP FI/CO — atuação conjunta nos módulos de contabilidade financeira (FI) e controladoria (CO). | Combinação mais pedida nas vagas de SAP e não casava: "SAP FI" e "SAP CO" (6 caracteres com espaço) nunca casam no motor. SAP FI e SAP CO como pais repetiriam SAP como ancestral, por isso o pai é SAP FI e a descrição cita o CO. "FICO" sozinho não entrou porque casaria o verbo "fico". |
| 108 | SAS | B.I | `SAS BASE`, `SAS GUIDE`, `SAS ENTERPRISE GUIDE`, `SAS INSTITUTE` | — | 807 | SAS — plataforma de análise estatística e analytics. | Ferramenta de analytics citada nas vagas de dados que não existia. |
| 109 | SENSEDIA | GATEWAY | — | — | 259 | Sensedia — plataforma de gestão de APIs (API gateway). | Plataforma de APIs citada nas vagas que não existia. |
| 110 | SERVICE MESH | MICROSERVICES | — | — | 280 | Service mesh — camada de rede entre microsserviços (Istio, Linkerd) com tráfego, segurança e observabilidade. | Conceito citado nas vagas de Kubernetes e microsserviços que não existia. |
| 111 | SIDEKIQ | RUBY | — | — | 182 | Sidekiq — processamento de jobs em segundo plano para Ruby, sobre Redis. | Biblioteca citada nas vagas de Ruby on Rails que não existia. |
| 112 | SIEM | MONITOR123SECURITY123 | `SECURITY INFORMATION AND EVENT MANAGEMENT` | — | 690 | SIEM — coleta e correlação de eventos de segurança (Splunk ES, QRadar, Sentinel). | Categoria de ferramentas de segurança muito pedida em vagas de SOC que não existia; usa a categoria de combinação que já existe para monitoramento + segurança. |
| 113 | SNMP | PROTOCOL, MONITOR123 | `SIMPLE NETWORK MANAGEMENT PROTOCOL` | — | 215 | SNMP — protocolo de gerência e monitoramento de equipamentos de rede. | Protocolo de monitoramento de redes pedido em vagas de infraestrutura e NOC que não existia. |
| 114 | SNS | AWS, MENSAGERIA | `AWS SNS`, `AMAZON SNS`, `SIMPLE NOTIFICATION SERVICE` | — | 936 | Amazon SNS — serviço de notificações publish/subscribe da AWS. | Serviço da AWS citado junto do SQS ("SQS/SNS") que não existia; mesmos pais do SQS. |
| 115 | SOCKET.IO | WEBSOCKET, NODE | `SOCKETIO`, `SOCKET IO` | — | 156 | Socket.IO — comunicação em tempo real sobre WebSocket para Node.js. | Biblioteca citada nas vagas de Node que não existia. |
| 116 | SOQL | SALESFORCE | — | — | 164 | SOQL — linguagem de consulta aos objetos da Salesforce. | Linguagem citada nas vagas de desenvolvimento Salesforce que não existia (164 vagas). |
| 117 | SPOTFIRE | B.I | — | — | 96 | TIBCO Spotfire — ferramenta de BI e visualização de dados. | Ferramenta de BI citada nas vagas que não existia. |
| 118 | SSIS | ETL | `SQL SERVER INTEGRATION SERVICES` | — | 842 | SQL Server Integration Services — ferramenta de ETL da Microsoft que acompanha o SQL Server. | Ferramenta de ETL muito pedida que não existia no arquivo. |
| 119 | SSRS | SQLSERVER, REPORT | `SQL SERVER REPORTING SERVICES` | SSRS (SQLSERVER), SQL SERVER REPORTING SERVICES (SQLSERVER) | 217 | SQL Server Reporting Services — servidor de relatórios da Microsoft que acompanha o SQL Server. | SSRS estava como sinônimo de SQLSERVER: é o servidor de relatórios, não o banco. |
| 120 | STORED PROCEDURES | SQLDATABASE | `STORED PROCEDURE`, `PROCEDURES` | — | 3.288 | Stored procedures — rotinas armazenadas e executadas no banco de dados relacional. | Conhecimento muito pedido nas vagas de banco de dados que não existia (só o PL/SQL da Oracle existia). |
| 121 | STORYBOOK | FRONTEND | — | — | 184 | Storybook — ambiente para desenvolver e documentar componentes de interface isolados. | Ferramenta de frontend citada nas vagas que não existia. |
| 122 | STYLED COMPONENTS | REACTJS | `STYLED-COMPONENTS`, `STYLEDCOMPONENTS` | — | 346 | styled-components — biblioteca de CSS-in-JS para React. | Biblioteca de estilos do React citada nas vagas que não existia. |
| 123 | STYLUS | CSS | — | — | 148 | Stylus — pré-processador de CSS. | Pré-processador de CSS citado nas vagas ao lado de Sass que não existia. |
| 124 | SUMO LOGIC | MONITOR123 | `SUMOLOGIC` | — | 194 | Sumo Logic — plataforma SaaS de análise de logs e observabilidade. | Ferramenta de logs citada nas vagas que não existia; mesmo pai do SPLUNK. |
| 125 | SUSE | LINUX | `SLES`, `SUSE LINUX` | — | 249 | SUSE Linux Enterprise — distribuição Linux corporativa (SLES), muito usada com SAP. | Distribuição Linux de servidores citada nas vagas que não existia (RHEL e CentOS já são filhos de LINUX). |
| 126 | TALEND | ETL | — | — | 313 | Talend — plataforma de integração de dados e ETL. | Ferramenta de ETL citada nas vagas de dados que não existia. |
| 127 | TERRAGRUNT | TERRAFORM | — | — | 71 | Terragrunt — camada sobre o Terraform para reaproveitar e organizar configurações. | Ferramenta citada nas vagas de infraestrutura como código que não existia; pressupõe Terraform. |
| 128 | TESTING LIBRARY | JSBDD | `REACT TESTING LIBRARY`, `TESTING-LIBRARY` | — | 164 | Testing Library — testes de componentes pelo comportamento do usuário (React, Vue, Angular). | Biblioteca de testes de frontend citada junto do Jest nas vagas que não existia; mesmo pai do JEST. |
| 129 | TOTVS | ERP | — | — | 1.311 | TOTVS — ERPs e plataformas da TOTVS (Protheus, Datasul, RM, Winthor). | Fabricante citado nas vagas como o próprio ERP ("ERP TOTVS") que não existia; passa a ser o pai do Protheus e do Datasul. |
| 130 | UFT | QA | `QTP`, `UNIFIED FUNCTIONAL TESTING`, `QUICKTEST PROFESSIONAL`, `QUICK TEST PROFESSIONAL` | — | 110 | Micro Focus UFT (antigo QTP) — automação de testes funcionais. | Ferramenta de automação de testes citada nas vagas de QA que não existia. |
| 131 | UIKIT | IOS | — | — | 139 | UIKit — framework de interface do iOS. | Framework do iOS citado nas vagas que não existia (SWIFTUI já existia). |
| 132 | UNIT TESTING | (raiz) | `UNIT TEST`, `UNIT-TEST`, `UNIT.TEST`, `UNIT TESTS`, `TESTES UNITARIOS`, `TESTES UNITÁRIOS`, `TESTE UNITARIO`, `TESTE UNITÁRIO`, `TESTES DE UNIDADE`, `TESTE DE UNIDADE` | UNIT TEST (UNITTEST), UNIT-TEST (UNITTEST), UNIT.TEST (UNITTEST) | 12.550 | Testes unitários — testes automatizados de unidades isoladas do código (classes, funções), base de TDD e CI. | Um dos requisitos mais pedidos nas vagas; "UNIT TEST" estava como sinônimo de UNITTEST, o módulo de testes do Python, e as vagas de Java ou .NET que pediam testes unitários viravam "Python". |
| 133 | UNIX | SO | — | — | 1.195 | Unix — família de sistemas operacionais (AIX, HP-UX, Solaris) que deu origem ao Linux e ao macOS. | Sistema operacional muito pedido em vagas de infraestrutura e mainframe que não existia. |
| 134 | VCENTER | VSPHERE | `VCENTER SERVER` | — | 123 | vCenter — console central de gerência do VMware vSphere. | Produto da VMware citado nas vagas de infraestrutura que não existia; faz parte do vSphere. |
| 135 | VISUALFORCE | SALESFORCE | — | — | 389 | Visualforce — framework de páginas da Salesforce. | Tecnologia da Salesforce citada nas vagas que não existia. |
| 136 | VPC | CLOUD, IP | `AWS VPC`, `AMAZON VPC`, `VIRTUAL PRIVATE CLOUD` | — | 673 | VPC (Virtual Private Cloud) — rede privada virtual na nuvem: sub-redes, rotas, gateways e security groups (AWS, GCP). | Rede em nuvem citada nas vagas de AWS e GCP que não existia; pressupõe nuvem e redes IP. |
| 137 | VUETIFY | VUE | — | — | 143 | Vuetify — biblioteca de componentes Material Design para Vue.js. | Biblioteca de componentes do Vue citada nas vagas que não existia. |
| 138 | VUEX | VUE | — | VUEX (VUE) | 278 | Vuex — biblioteca de gerenciamento de estado do Vue.js. | Vuex estava como sinônimo de VUE: é a biblioteca de estado (como o Redux para o React), que pressupõe Vue. |
| 139 | WCF | DOTNET, WS | `WINDOWS COMMUNICATION FOUNDATION` | WCF (WPF) | 1.397 | Windows Communication Foundation — framework .NET para serviços SOAP/WS-* e comunicação entre processos. | WCF é o framework de serviços do .NET e estava como sinônimo de WPF (interface gráfica), outra tecnologia. |
| 140 | WEB DYNPRO | ABAP | `WEBDYNPRO`, `WEB-DYNPRO` | — | 278 | Web Dynpro ABAP — framework de interfaces web do SAP em ABAP. | Tecnologia SAP citada nas vagas de ABAP que não existia (o SAP DYNPRO é a tela clássica do SAPGUI, outra coisa). |
| 141 | WEBPACK | JSDEPENDENCYMANAGER | — | — | 1.243 | Webpack — empacotador (bundler) de módulos JavaScript. | Ferramenta de build de frontend muito citada nas vagas que não existia; a categoria JSDEPENDENCYMANAGER reúne as ferramentas de pacotes e build de JavaScript. |
| 142 | WINTHOR | TOTVS | — | — | 72 | WinThor — ERP da TOTVS para atacado e distribuição. | ERP citado nas vagas que não existia. |
| 143 | WOOCOMMERCE | WORDPRESS, ECOMERCE | `WOO COMMERCE`, `WOO-COMMERCE`, `WOO.COMMERCE` | WOOCOMMERCE (WORDPRESS), WOO COMMERCE (WORDPRESS), WOO-COMMERCE (WORDPRESS), WOO.COMMERCE (WORDPRESS) | 180 | WooCommerce — plugin de e-commerce do WordPress. | WooCommerce estava como sinônimo de WORDPRESS: é um plugin de loja virtual, que pressupõe WordPress e e-commerce. |
| 144 | WSUS | WINDOWS SERVER | `WINDOWS SERVER UPDATE SERVICES` | — | 523 | WSUS — serviço do Windows Server que distribui e controla as atualizações do Windows na rede. | Serviço do Windows Server pedido em vagas de infraestrutura que não existia. |
| 145 | XSLT | XML | `XSL` | — | 185 | XSLT — linguagem de transformação de documentos XML. | Tecnologia XML citada nas vagas de integração que não existia. |
| 146 | ZENDESK | ITSM | — | — | 167 | Zendesk — help desk e atendimento ao cliente. | Ferramenta de chamados citada nas vagas que não existia. |

## 3. Pais corrigidos

Pai = pré-requisito direto. Quando dois pais levariam ao mesmo ancestral (o "incesto" do `hasRepeatedParent`), ficou só o mais específico, e a justificativa diz isso.

| # | Skill | Pais antes | Pais depois | Justificativa |
|---|---|---|---|---|
| 1 | 2FA | AUTENTICATION | MFA | Dois fatores é caso particular de múltiplos fatores. |
| 2 | ADABAS | SQLDATABASE | DATABASE, MAINFRAME | ADABAS é um banco de listas invertidas (não relacional) da Software AG, típico de mainframe; não pressupõe SQL. |
| 3 | ADW | CLOUD, DATABASE | DW, OCI | Autonomous Data Warehouse é o data warehouse gerenciado da Oracle Cloud: pressupõe data warehouse e OCI, não um banco qualquer. |
| 4 | AIRFLOW | PYTHON, MONITOR123, DEVOPS | PYTHON, ETL | Orquestrador de pipelines de dados escritos em Python: pressupõe Python e ETL. MONITOR123/DEVOPS não são pré-requisito; os quatro orquestradores ficam com os mesmos pais. |
| 5 | ALLURE REPORTS | BDDQA | QA | Relatório de testes de qualquer framework (JUnit, TestNG, pytest), não só de BDD. |
| 6 | AMDP | HANNA, ORM | HANNA SQL | AMDP são procedures em SQLScript do HANA chamadas pelo ABAP; ORM não tem relação. (ABAP e HANNA juntos repetiriam SAP como ancestral.) |
| 7 | AMPQ | (raiz) | MENSAGERIA, PROTOCOL | AMQP é um protocolo de mensageria; estava sem pai. |
| 8 | ANSIBLE | DEVOPS | AUTOMATIONDEVOPS | Automação de infraestrutura: mesma categoria de CHEF e SALTSTACK. |
| 9 | APOLLO | (raiz) | GRAPHQL | A plataforma Apollo é de GraphQL; estava sem pai. |
| 10 | ATLAS | VETORIALDBCLOUD, MONGODB | MONGODB, CLOUD | O MongoDB Atlas é o MongoDB gerenciado na nuvem; a busca vetorial é só um recurso dele. |
| 11 | AURORA | SAP | AWS, SQLDATABASE | O Aurora das vagas é o banco relacional gerenciado da AWS (compatível com MySQL e PostgreSQL); não existe produto SAP Aurora. |
| 12 | AWS STEP FUNCTIONS | AWS, DEVOPS | AWS, SERVERLESS | Orquestrador serverless da AWS; DEVOPS não é pré-requisito. |
| 13 | AWSMONITOR | (raiz) | AWS, MONITOR123 | Categoria de combinação (AWS + monitoramento) que estava sem pai. |
| 14 | AZURE API MANAGEMENT | GRAPHQLSERVERLESS, AZURE, GATEWAY | AZURE, GATEWAY | Gateway de APIs do Azure; GRAPHQLSERVERLESS não é pré-requisito. |
| 15 | B.I | BIGDATA | SQLDATABASE | BI (relatórios, dashboards, data warehouse) pressupõe SQL e bancos relacionais, não big data. |
| 16 | BFF | (raiz) | PATTERNS | Backend for Frontend é um padrão de arquitetura; estava sem pai. |
| 17 | BOOMI API MANAGEMENT | GATEWAYCLOUD | BOOMI, GATEWAY | Módulo de API management da plataforma Boomi. |
| 18 | BPM | SOA | (raiz) | BPM (gestão de processos de negócio) não pressupõe SOA; fica como raiz. |
| 19 | BTRIEVE | SQLDATABASE | DATABASE | Btrieve é um gerenciador de registros (navegacional), não um banco SQL. |
| 20 | BUGZILLA | DOCUMENTATION123, MONITOR123 | DOCUMENTATION123 | Rastreador de bugs: não é ferramenta de monitoramento (mesmo pai do REDMINE). |
| 21 | BUN | HTTP SERVERJAVASCRIPTREST | JAVASCRIPT | Bun é runtime JavaScript, como o Node. |
| 22 | CAMUNDA | BACKEND | BPM | Camunda é um motor de BPM/BPMN. |
| 23 | CAPYBARA | WEBDRIVER | WEBDRIVER, RUBY | Capybara é uma biblioteca Ruby: pressupõe Ruby. |
| 24 | CHAKRA UI | CSS | REACTJS | Biblioteca de componentes React: pressupõe React (o CSS já vem como ancestral do React). |
| 25 | CHERWELL | LOWCODE | ITSM | Cherwell é ferramenta de ITSM, não plataforma low-code. |
| 26 | CITRIX | VIRTUALIZATION, SECURITY123, HTTP SERVER | VIRTUALIZATION | Citrix (VDI, XenApp, XenServer) é virtualização; segurança e servidor HTTP não são pré-requisito. |
| 27 | CLOUD RUN | SERVERLESS, GCPKUBERNETES | GCP, SERVERLESS | Cloud Run executa containers serverless; não é o GKE (Kubernetes). |
| 28 | CLOUDFLARE API GATEWAY | GATEWAYCLOUD | CLOUDFLARE, GATEWAY | Gateway de APIs da Cloudflare. |
| 29 | CLOUDRA | CLOUD, SPARK, AIRFLOW | HADOOP | Cloudera é uma distribuição do Hadoop; Spark e Airflow são componentes, não pré-requisitos. |
| 30 | CMDB | SQLDATABASE | ITSM | CMDB é o inventário de configuração da gestão de serviços de TI, não um banco SQL qualquer. |
| 31 | COBIT | AGIL | COMPLIANCE | COBIT é framework de governança e conformidade de TI, não metodologia ágil. |
| 32 | CODECOMMIT | GIT, DEVOPS | GIT, AWS | Repositório Git gerenciado da AWS: pressupõe Git e AWS. |
| 33 | CONSUL | MONITOR123 | MICROSERVICES | Consul é descoberta de serviços e configuração de microsserviços; o health check é um recurso, não monitoramento. |
| 34 | COPILOT | LLM, AZURE | LLM | Copilot é assistente baseado em LLM; não exige Azure. |
| 35 | CRM | BACKEND | (raiz) | CRM é categoria de sistemas de negócio, não pressupõe desenvolvimento backend. |
| 36 | CRYPTOGRAPHY | (raiz) | SECURITY123 | Criptografia é área de segurança; estava sem pai. |
| 37 | CTFL | BDDQA | QA | Certificação ISTQB de testes em geral, não de BDD. |
| 38 | CUBO | (raiz) | OLAP | Cubo é a estrutura multidimensional do OLAP. |
| 39 | CYPRESS | WEBDRIVER | BDD, FRONTEND | Não usa WebDriver: controla o navegador por outro protocolo. |
| 40 | DAGSTER | BIGDATA, DEVOPSMONITOR123PYTHON | PYTHON, ETL | Orquestrador de pipelines de dados escritos em Python: pressupõe Python e ETL. MONITOR123/DEVOPS não são pré-requisito; os quatro orquestradores ficam com os mesmos pais. |
| 41 | DATAMART | B.I | DW | Data mart é um recorte do data warehouse. |
| 42 | DATASUL | ERP | TOTVS | O Datasul é ERP da TOTVS (o ERP continua como ancestral pelo TOTVS). |
| 43 | DAX | B.I | POWERBI | DAX é a linguagem de fórmulas do Power BI. |
| 44 | DENO | HTTP SERVERJAVASCRIPTREST | JAVASCRIPT | Deno é runtime JavaScript, como o Node. |
| 45 | DEVSECOPS | SECURITYTDD | DEVOPS, SECURITYTDD | DevSecOps é DevOps com segurança: pressupõe DevOps. |
| 46 | DGRAPH | GRAPHQL | NOSQLDATABASE, GRAPHQL | Dgraph é um banco de grafos (NoSQL) com interface GraphQL. |
| 47 | DJANGO | CMSPYTHON | MVCPYTHON | Django é framework web MVC (MTV) de Python; a própria categoria MVCPYTHON cita o Django. |
| 48 | DYNAMICS 365 | ERP, CRM, AZURE | ERP, CRM | Suíte de ERP e CRM; Azure não é pré-requisito. |
| 49 | EJB | RMI | JAVAEE | EJB é especificação do Java EE; RMI é só um dos transportes. |
| 50 | ENVOY | GATEWAYCLOUD | GATEWAYLOADBALANCE | Envoy é proxy e balanceador (mesma categoria do TRAEFIK e do HAPROXY). |
| 51 | EUREKA | SPRING CLOUD NETFLIX | SPRING CLOUD NETFLIX, MICROSERVICES | Descoberta de serviços de microsserviços. |
| 52 | EVENT SOURCING | PATTERNS, NOSQLDATABASE | PATTERNS | Padrão de persistência que vale para qualquer banco, não só NoSQL. |
| 53 | EXTENT REPORTS | BDDQA | QA | Relatório de testes de qualquer framework, não só de BDD. |
| 54 | FASTIFY | GRAPHQLNODE | NODE | Fastify é framework web do Node; GraphQL é só um plugin. |
| 55 | FLUTTER | ANDROID, FRONTEND | DART, MOBILE | Flutter pressupõe a linguagem Dart; gera apps iOS e Android, não só Android. |
| 56 | FORTIFY | SECURITY123 | DEVSECOPS | Ferramenta de SAST usada no pipeline de DevSecOps. |
| 57 | FORTINET | SECURITY123 | FIREWALL | Fortinet (FortiGate) é fabricante de firewalls. |
| 58 | GATSBY | NODE | REACTJS | Gatsby é framework de React; Node só roda o build. |
| 59 | GENERATIVEAI | LLM | MACHINELEARNING | IA generativa é ramo do machine learning; passa a ser o pai do LLM. |
| 60 | GENEXUS | BACKEND | LOWCODE | GeneXus é plataforma low-code (gera código a partir de modelos). |
| 61 | GOLANG | BACKEND, HTTP SERVER | BACKEND | Go é linguagem; servidor HTTP não é pré-requisito. |
| 62 | GOOGLE CLOUD API | GRAPHQLSERVERLESS, GCP | GATEWAY, GCP | Cloud Endpoints é gerenciamento de APIs do GCP; GraphQL serverless não tem relação. |
| 63 | GRAPHQL CODE GENERATOR | GRAPHQLJS, IDE | GRAPHQLJS | É um gerador de código, não uma IDE. |
| 64 | GRIZZLY | JAXRS | JAVA | Grizzly é framework de I/O; não pressupõe JAX-RS. |
| 65 | HAPROXY | LOADBALANCE, GATEWAY | GATEWAYLOADBALANCE | Usa a categoria de combinação que já existe para gateway + balanceamento. |
| 66 | HONE | BUN | HTTP SERVERJAVASCRIPTREST | Hono é framework web JavaScript que roda em Bun, Deno, Node e na borda, não só no Bun. |
| 67 | HTML | XML | (raiz) | HTML não pressupõe XML (são linguagens irmãs, derivadas do SGML). |
| 68 | HTTPIE | PERFORMANCEQA | PERFORMANCEIDE | Cliente HTTP de linha de comando, da mesma categoria do Postman. |
| 69 | HYPOTHESIS | PYTHONBDD | PYTHON, TDD | Framework de testes de Python; não é ferramenta de BDD. |
| 70 | IBM BROKER | SOA | BARRAMENTO | IBM Integration Bus é um ESB. |
| 71 | IIS | HTTP SERVER, DOTNET | HTTP SERVER, WINDOWS | IIS é o servidor web do Windows; serve qualquer aplicação, não só .NET. |
| 72 | IMPALA | ETL | HADOOP | Impala é motor SQL sobre HDFS: pressupõe Hadoop, não ETL. |
| 73 | IMS | MAINFRAME, SQLDATABASE | MAINFRAME, DATABASE | IMS é banco hierárquico, não SQL. |
| 74 | INFORMIX | BIGDATA | SQLDATABASE | Informix é banco relacional, não big data. |
| 75 | IREPORT | (raiz) | JASPER | iReport é o editor visual de relatórios do JasperReports. |
| 76 | ITEXT | REPORT, JDBC | REPORT, JAVA | iText gera PDF em Java; não tem relação com JDBC. |
| 77 | JASMINE | WEBDRIVER | JSBDD | Jasmine é framework de testes JavaScript, não usa WebDriver. |
| 78 | JASPER | ITEXT, IREPORT | REPORT, JAVA | JasperReports é biblioteca Java de relatórios; iText e iReport não são pré-requisitos. |
| 79 | JAXB | JAXWS | XML, JAVA | JAXB é binding Java-XML; não pressupõe JAX-WS. |
| 80 | JCL | COBOL | Z/OS | JCL é a linguagem de jobs do z/OS, não do COBOL. |
| 81 | JDE | JAVA, ERP | ORACLEERP | JD Edwards é ERP da Oracle; Java não é pré-requisito. |
| 82 | JEST | TDD, JAVASCRIPT | JSBDD | Mesma categoria dos frameworks de teste JavaScript. |
| 83 | JETPACK COMPOSE | ANDROID | KOTLIN, ANDROID | Compose é a UI do Android escrita em Kotlin. |
| 84 | JSF | MVC, JSP | MVC, SERVLET | JSF roda sobre servlets; não pressupõe JSP (usa Facelets). |
| 85 | KANBAM | JIRA | AGIL | Kanban é método ágil; não pressupõe Jira. |
| 86 | KARMA | WEBDRIVER | JSBDD | Karma é test runner JavaScript, não usa WebDriver. |
| 87 | KERBEROS | SECURITY123 | AUTENTICATION | Kerberos é protocolo de autenticação. |
| 88 | KEYCLOAK | SECURITY123 | IAM | Keycloak é servidor de identidade e acesso. |
| 89 | KINESIS | AWSMONITOR | AWS, MENSAGERIA | Kinesis é streaming de dados da AWS, não ferramenta de monitoramento. |
| 90 | KOTLIN | JAVA, ANDROID | JVM | Kotlin roda na JVM; não pressupõe Java nem Android (é usado também no backend). |
| 91 | LAMBDA | CLOUD, SERVERLESS | AWS, SERVERLESS | Lambda é serviço da AWS. |
| 92 | LEININGEN | JAVADEPENDENCYMANAGER | CLOJURE, DEPENDENCY MANAGER | Ferramenta de build de Clojure: pressupõe Clojure, não Java. |
| 93 | LINQ | ORMCSHARP | CSHARP | LINQ é recurso da linguagem C#, não um ORM. |
| 94 | LLM | MACHINELEARNING | GENERATIVEAI | LLMs são modelos de IA generativa. |
| 95 | LOCALSTACK | TDD | TDD, AWS | Emulador da AWS: pressupõe AWS. |
| 96 | LOOKER STUDIO | LOOKER, IDE | B.ICLOUD | Looker Studio (antigo Data Studio) é outro produto, não o Looker; também não é IDE. |
| 97 | LUIGI | DEVOPSPYTHON | PYTHON, ETL | Orquestrador de pipelines de dados escritos em Python: pressupõe Python e ETL. MONITOR123/DEVOPS não são pré-requisito; os quatro orquestradores ficam com os mesmos pais. |
| 98 | LUMEN | PHP | MVCPHP | Mesmo pai do Laravel. |
| 99 | MACHINELEARNING | BIGDATA | (raiz) | Machine learning não pressupõe big data; fica como raiz. |
| 100 | MATERIAL UI | CSS | REACTJS | Biblioteca de componentes React: pressupõe React (o CSS já vem como ancestral do React). |
| 101 | MICRONAUT | SERVLET | JAVA | Micronaut não roda sobre servlets (usa Netty). |
| 102 | MILL | JAVADEPENDENCYMANAGER | SCALA, DEPENDENCY MANAGER | Mill é ferramenta de build do Scala. |
| 103 | MOCHA | NODE, TDD | JSBDD | Mesma categoria dos frameworks de teste JavaScript. |
| 104 | MOCK SERVICE WORKER | MOCK, GRAPHQLNODE | MOCK, JAVASCRIPT | MSW intercepta requisições REST e GraphQL no navegador; não pressupõe GraphQL no Node. |
| 105 | MVC | (raiz) | PATTERNS | MVC é padrão de projeto; estava sem pai. |
| 106 | NETEZZA | NZSQL, HTAP, MPP | DW, MPP | Netezza é data warehouse MPP; NZSQL é o dialeto dele (filho, não pai). |
| 107 | NETSUITE | CLOUD, ERP | ORACLEERP, CLOUD | NetSuite é ERP da Oracle. |
| 108 | NETTY | RX | JAVA | Netty é framework de I/O de rede; não é biblioteca Rx. |
| 109 | NEWGL | SAP FI | SAP FI-GL | New GL é o razão geral (FI-GL). |
| 110 | NEWMAN | PERFORMANCEQA | POSTMAN | Newman executa coleções do Postman: pressupõe Postman. |
| 111 | NEXTJS | JAVASCRIPT | REACTJS | Next.js é framework de React. |
| 112 | NEXUS | JAVA | DEVOPS | Repositório de artefatos (Maven, npm, Docker) usado em pipelines; não é recurso da linguagem Java. |
| 113 | NGINX | GATEWAY | HTTP SERVER, GATEWAYLOADBALANCE | Nginx é servidor web, proxy reverso e balanceador. |
| 114 | NODE | JAVASCRIPT, REST, HTTP SERVER | JAVASCRIPT | Node é runtime JavaScript; REST e servidor HTTP são usos, não pré-requisitos. |
| 115 | NOSE2 | PYTHONBDD | PYTHON, TDD | Framework de testes de Python; não é ferramenta de BDD. |
| 116 | NZSQL | SQL | NETEZZA, SQL | Dialeto SQL do Netezza. |
| 117 | OBIEE | ETL | B.I | OBIEE é ferramenta de BI, não de ETL. |
| 118 | OCTANE | DEVOPS | ALM | Micro Focus Octane é ferramenta de ALM. |
| 119 | OIC | CLOUDINTEGRATION123 | IPAAS | Oracle Integration Cloud é iPaaS. |
| 120 | OPENLIBERTY | JAXRS | JAVA SERVER | Open Liberty é servidor de aplicação Java. |
| 121 | PARQUET | COLUNAROLAPNOSQLDATABASE | COLUNAR, HADOOP | Parquet é formato de arquivo colunar do ecossistema Hadoop, não um banco. |
| 122 | PAYLOAD | IDE, CMS | CMS | Payload é CMS; não é IDE. |
| 123 | PFSENSE | SECURITY123 | FIREWALL | pfSense é firewall. |
| 124 | PHP | HTTP SERVER | BACKEND | PHP é linguagem de backend; servidor HTTP não é pré-requisito. |
| 125 | PLAYWRIGHT | WEBDRIVER | BDD, FRONTEND | Não usa WebDriver: controla o navegador por outro protocolo. |
| 126 | PLOTLY | MACHINELEARNING | PYTHON | Biblioteca de gráficos; não pressupõe machine learning. |
| 127 | PLSQL | SQLDATABASE | ORACLE | PL/SQL é a linguagem procedural do Oracle. |
| 128 | POLARS | PYTHON, DATABASE | PYTHON | Biblioteca de DataFrames, não banco de dados. |
| 129 | PORTAINER | KUBERNETES | DOCKER | Portainer gerencia containers Docker (e também Kubernetes). |
| 130 | POWERAPPS | FRONTEND | LOWCODE | Power Apps é plataforma low-code. |
| 131 | POWERDESIGNER | SAP | DATABASE | PowerDesigner é ferramenta de modelagem de dados; SAP é só o dono. |
| 132 | PREFECT | DEVOPSPYTHON | PYTHON, ETL | Orquestrador de pipelines de dados escritos em Python: pressupõe Python e ETL. MONITOR123/DEVOPS não são pré-requisito; os quatro orquestradores ficam com os mesmos pais. |
| 133 | PRIMAVERA | ERP | PMI | Primavera é gestão de projetos, não ERP. |
| 134 | PROTHEUS | ERP | TOTVS | O Protheus é ERP da TOTVS (o ERP continua como ancestral pelo TOTVS). |
| 135 | PUPPET | DEVOPS | AUTOMATIONDEVOPS | Gerência de configuração: mesma categoria de CHEF e SALTSTACK. |
| 136 | PUPPETEER | WEBDRIVER | NODE | Puppeteer é biblioteca Node que controla o Chrome pelo DevTools Protocol, não pelo WebDriver. |
| 137 | PYTEST | PYTHONBDD | PYTHON, TDD | Framework de testes de Python; não é ferramenta de BDD. |
| 138 | QTEST | BDDQA | QA | Gestão de testes em geral, não de BDD. |
| 139 | QUARKUS | SERVLET | JAVA | Quarkus não depende de servlets (usa Vert.x/RESTEasy Reactive). |
| 140 | QWIK | JAVASCRIPT | FRONTEND | Framework de frontend. |
| 141 | R2DBC | SQLDATABASE, REST | SQLDATABASE, RX, JAVA | Driver reativo Java para bancos SQL; não tem relação com REST. |
| 142 | RABBITMQ | JMS | AMPQ | O RabbitMQ implementa AMQP, não JMS (JMS é uma API Java). |
| 143 | RDS | VIRTUALIZATIONWINDOWS | AWS, SQLDATABASE | Nas vagas, "RDS" é o banco gerenciado da AWS: em 1.622 das 1.659 vagas em que a sigla aparece há AWS, EC2, S3, Lambda ou outro serviço da AWS a até 200 caracteres. Com o pai antigo (VIRTUALIZATIONWINDOWS, Remote Desktop Services) essas vagas ganhavam Windows e virtualização por engano. O nome da skill não muda. |
| 144 | REDMINE | MONITOR123, DOCUMENTATION123 | DOCUMENTATION123 | Gestão de projetos e bugs, não monitoramento. |
| 145 | REDSHIFT | B.I, AWS | DW, AWS | Redshift é data warehouse da AWS. |
| 146 | REMEDY | DOCUMENTATION123 | ITSM | BMC Remedy é ferramenta de ITSM (chamados), não de documentação. |
| 147 | REMIX | JAVASCRIPT | REACTJS | Remix é framework de React. |
| 148 | RESTASSURED | PERFORMANCEQA | JAVABDD, REST | Biblioteca Java de testes de API; não é de teste de performance. |
| 149 | RMI | INTEGRATION123, JAVAEE | JAVA, INTEGRATION123 | RMI é do Java SE, não do Java EE. |
| 150 | RPG | AS400, MAINFRAME | AS400 | RPG é linguagem do AS/400. |
| 151 | SAP BAP | SAP, ODATA | SAP RFC | BAPIs são chamadas por RFC; OData não tem relação. |
| 152 | SAP BO | SAP | SAP, B.I | BusinessObjects é ferramenta de BI. |
| 153 | SAP BTP | SAP | SAPCLOUD | BTP é a plataforma de nuvem da SAP. |
| 154 | SAP BW | HANNA | SAP, DW | BW é data warehouse da SAP; não pressupõe o banco HANA. |
| 155 | SAP BW/4HANNA | HANNA | SAP BW | BW/4HANA é a versão nova do BW. |
| 156 | SAP DYNPRO | UX, MVCJAVA, ABAP | ABAP, UX | Dynpro é tela ABAP; não tem relação com MVC Java. |
| 157 | SAP INTEGRATION SUITE | SAP, WS | SAPCLOUD, WS | Serviço da nuvem SAP (BTP). |
| 158 | SAP LUMIRA | SAP | SAP, B.I | Lumira é ferramenta de BI. |
| 159 | SAP PAYROLL | SAP | SAP HR | Folha de pagamento é componente do SAP HR. |
| 160 | SAP PI | SAP | SAP, BARRAMENTO | SAP PI é middleware de integração (ESB). |
| 161 | SAP PO | SAP | SAP, BARRAMENTO | SAP PO é middleware de integração (ESB). |
| 162 | SAP S/4HANNA | HANNA | SAP | S/4HANA é o ERP da SAP; o banco HANA não é pré-requisito de quem trabalha com ele. |
| 163 | SAP S/4HANNA EMBEDDED ANALYTICS | HANNA | SAP S/4HANNA | Módulo do S/4HANA. |
| 164 | SAP S/4HANNA FINANCE | HANNA | SAP S/4HANNA | Módulo do S/4HANA. |
| 165 | SAP S/4HANNA LOGISTICS | HANNA | SAP S/4HANNA | Módulo do S/4HANA. |
| 166 | SAP SAC | SAPCLOUD | SAPCLOUD, B.I | Analytics Cloud é ferramenta de BI. |
| 167 | SAP UI5 | SAPUX | SAPUX, JAVASCRIPT | UI5 é framework JavaScript. |
| 168 | SBT | JAVADEPENDENCYMANAGER | SCALA, DEPENDENCY MANAGER | SBT é a ferramenta de build do Scala. |
| 169 | SCSS | CSS | SASS | SCSS é a sintaxe do Sass. |
| 170 | SERENETY | BDD | JAVABDD | Serenity BDD é framework Java. |
| 171 | SERVICENOW | LOWCODE | ITSM, LOWCODE | ServiceNow é antes de tudo ITSM (a própria descrição diz); a plataforma low-code é complementar. |
| 172 | SILVERLIGHT | FRONTEND, DOTNET | DOTNETFRONTEND | Usa a categoria de combinação que já existe. |
| 173 | SNOWFLAKE | CLOUDDATA | CLOUDDATA, DW | Snowflake é data warehouse em nuvem. |
| 174 | SONARCLOUD | DEVSECOPS, MONITOR123 | SONAR | SonarCloud é o Sonar em nuvem; não é ferramenta de monitoramento. |
| 175 | SONARQUBE | DEVSECOPS, SONAR | SONAR | SonarQube é o Sonar on-premise (DEVSECOPS + SONAR repetiria DEVOPS como ancestral). |
| 176 | SONICWALL | SECURITY123 | FIREWALL | SonicWall é fabricante de firewalls. |
| 177 | SPRING CLOUD SLEUTH | SPRING CLOUD, MACHINELEARNING | SPRING CLOUD, DEVOPSMONITOR123 | Sleuth é rastreamento distribuído; machine learning não tem relação. |
| 178 | SPRING SESSION | SPRING SECURITY | SPRING | Spring Session é projeto independente do Spring Security. |
| 179 | SPRING TESTCONTAINERS | SPRING | SPRINGBOOT, TESTCONTAINERS | Integração do Spring Boot com Testcontainers. |
| 180 | SPRING WEBCLIENT | SPRING | SPRING WEBFLUX, REST | O WebClient é parte do Spring WebFlux e consome APIs REST. |
| 181 | SPRINGMVC | SPRING, MVC | SPRING WEB, MVC | Spring MVC é o módulo spring-webmvc, que pressupõe o Spring Web (pai que vinha do SPRING WEB MVC). |
| 182 | SPRINGREST | OPENAPI | SPRINGMVC, REST | APIs REST no Spring são feitas com o Spring MVC; OpenAPI é documentação, não pré-requisito. |
| 183 | SQLITE | SQLDATABASE, HTTP SERVER | SQLDATABASE | SQLite é embutido, não precisa de servidor HTTP. |
| 184 | SSAS | B.I | SQLSERVER, OLAP | Analysis Services é o OLAP do SQL Server. |
| 185 | STABLE DIFFUSION | MACHINELEARNING | GENERATIVEAI | Modelo de IA generativa de imagens. |
| 186 | STEPZEN | GATEWAYCLOUD | GATEWAYCLOUD, GRAPHQL | StepZen é gateway GraphQL em nuvem. |
| 187 | STOMP | (raiz) | MENSAGERIA, PROTOCOL | STOMP é protocolo de mensageria; estava sem pai. |
| 188 | SUBSTRATE | POLKADOT, SMARTCONTRACT | POLKADOT, RUST | Substrate é framework em Rust do Polkadot; smart contracts não são pré-requisito. |
| 189 | SUCCESSFACTORS | SAP | SAPCLOUD | SuccessFactors é o RH em nuvem (SaaS) da SAP. |
| 190 | SUPABASE | DATABASE | POSTGRESQL | Supabase é PostgreSQL gerenciado. |
| 191 | SVELTE | AJAX | SPA | Mesmo pai do Vue e do Angular. |
| 192 | SWIFT | OBJECTIVEC | IOS | Swift não pressupõe Objective-C. |
| 193 | SYBASE | SAP | SQLDATABASE | Sybase é banco relacional (só foi comprado pela SAP). |
| 194 | SYNAPSE | AZURE, ETL | AZURE, DW | Synapse é data warehouse do Azure. |
| 195 | TABLEAU | ETL | B.I | Tableau é ferramenta de BI (visualização), não de ETL. |
| 196 | TDD | (raiz) | UNIT TESTING | TDD pressupõe saber escrever testes unitários. |
| 197 | TERADATA | CLOUD, B.I | DW, MPP | Teradata é data warehouse MPP. |
| 198 | TESTCAFE | WEBDRIVER | BDD, FRONTEND | Não usa WebDriver: controla o navegador por outro protocolo. |
| 199 | TESTCOMPLETE | TDD | QA | Ferramenta de automação de testes; não é TDD. |
| 200 | TESTCONTAINERS | TDD | DOCKER, TDD | Testcontainers sobe containers Docker nos testes. |
| 201 | TESTLINK | TDD | BDDQA | Gestão de casos de teste. |
| 202 | TFS | VERSIONING | VERSIONING, ALM | TFS é controle de versão e ALM. |
| 203 | THYMELEAF | FRONTEND | MVCJAVA, HTML | Motor de templates HTML do Spring MVC. |
| 204 | TIBCO | ETL | BARRAMENTO, ETL | TIBCO é ESB e integração de dados. |
| 205 | TRAEFIK | GATEWAYCLOUD | GATEWAYLOADBALANCE | Traefik é proxy reverso e balanceador. |
| 206 | TRELLO | JIRA | KANBAM | Trello é quadro Kanban; não pressupõe Jira. |
| 207 | TRPC | TYPESCRIPT, REST | TYPESCRIPT | tRPC é RPC tipado, não REST. |
| 208 | UIPATH | FRONTEND, AUTOMATION, UX | RPA | UiPath é ferramenta de RPA. |
| 209 | UNITTEST | PYTHONBDD | PYTHON, TDD | Framework de testes de Python; não é ferramenta de BDD. |
| 210 | VEEAM | CLOUD | VIRTUALIZATION | Veeam é backup de ambientes virtualizados. |
| 211 | VICTORIAMETRICS | MONITOR123 | MONITOR123, TIMESERIES | VictoriaMetrics é banco de séries temporais. |
| 212 | VSPHERE | VIRTUALIZATION | VMWARE | vSphere é produto da VMware. |
| 213 | WMS | ORACLEERP | ERP | Nas vagas WMS é o sistema de gestão de armazém em geral (de qualquer fornecedor), não o da Oracle. |
| 214 | WPF | DOTNET, WS | DOTNETFRONTEND | WPF é interface gráfica .NET. |
| 215 | XAMARIN | WEBVIEW, CSHARP | CSHARP, MOBILE | Xamarin gera apps nativos em C#; não usa WebView. |
| 216 | YELLOWBRICK | MPPOLTPSQL | DW, MPP | Yellowbrick é data warehouse MPP. |
| 217 | ZOOKEEPER | MONITOR123 | (raiz) | ZooKeeper é coordenação distribuída, não monitoramento. |

## 4. Sinônimos acrescentados

| # | Skill | Sinônimo | Vagas | Justificativa |
|---|---|---|---:|---|
| 1 | AGIL | `METODOLOGIA ÁGIL` | 6.304 | Forma acentuada de um sinônimo que já existia sem acento: o motor só põe o texto em maiúsculas (não tira acentos), e as vagas escrevem quase sempre com acento. |
| 2 | AGIL | `METODOLOGIAS ÁGEIS` | 11.933 | Forma acentuada de um sinônimo que já existia sem acento: o motor só põe o texto em maiúsculas (não tira acentos), e as vagas escrevem quase sempre com acento. |
| 3 | AMPQ | `AMQP` | 54 | A grafia certa do protocolo é AMQP; AMPQ (o nome da skill) é erro de digitação e não casava com as vagas que escrevem AMQP. |
| 4 | APACHE SUPERSET | `SUPERSET` | 13 | Forma curta usada nas vagas. |
| 5 | ARTIFACTORY | `JFROG` | 48 | Fabricante do Artifactory, citado no lugar do produto. |
| 6 | ASPNET | `ASPX` | 48 | Extensão das páginas ASP.NET Web Forms, citada nas vagas. |
| 7 | ATLAS | `MONGO ATLAS` | 0 | Nome completo do serviço, como as vagas escrevem. |
| 8 | ATLAS | `MONGODB ATLAS` | 7 | Nome completo do serviço, como as vagas escrevem. |
| 9 | AURORA | `AMAZON AURORA` | 29 | Nome completo do serviço da AWS. |
| 10 | AURORA | `AWS AURORA` | 100 | Nome completo do serviço da AWS. |
| 11 | AUTENTICATION | `AUTENTICACAO` | 0 | Grafias corretas em inglês e português; o nome da skill (AUTENTICATION) é erro de digitação e não casava com nenhuma vaga. |
| 12 | AUTENTICATION | `AUTENTICAÇÃO` | 1.155 | Grafias corretas em inglês e português; o nome da skill (AUTENTICATION) é erro de digitação e não casava com nenhuma vaga. |
| 13 | AUTENTICATION | `AUTHENTICATION` | 140 | Grafias corretas em inglês e português; o nome da skill (AUTENTICATION) é erro de digitação e não casava com nenhuma vaga. |
| 14 | AWS APP SYNC | `APPSYNC` | 15 | O nome oficial é AppSync, numa palavra. |
| 15 | AWS APP SYNC | `AWS APPSYNC` | 1 | O nome oficial é AppSync, numa palavra. |
| 16 | AWS GLUE | `GLUE` | 452 | Forma curta usada nas vagas ("S3, Glue, Athena"). |
| 17 | AWS STEP FUNCTIONS | `STEP FUNCTIONS` | 89 | Forma curta usada nas vagas. |
| 18 | AZURE API MANAGEMENT | `APIM` | 69 | Sigla usada nas vagas. |
| 19 | AZURE SERVICE BUS | `SERVICE BUS` | 283 | Forma curta usada nas vagas; "Enterprise Service Bus" continua indo para BARRAMENTO (a palavra mais longa vence). |
| 20 | AZURE SERVICE BUS | `SERVICEBUS` | 61 | Forma curta usada nas vagas; "Enterprise Service Bus" continua indo para BARRAMENTO (a palavra mais longa vence). |
| 21 | AZUREDEVOPS | `AZURE DEVOPS` | 5.028 | Faltava justamente a grafia correta "Azure DevOps": a skill só tinha erros de digitação (AZUERE, AZZURE). VSTS é o nome antigo. |
| 22 | AZUREDEVOPS | `AZURE-DEVOPS` | 0 | Faltava justamente a grafia correta "Azure DevOps": a skill só tinha erros de digitação (AZUERE, AZZURE). VSTS é o nome antigo. |
| 23 | AZUREDEVOPS | `AZURE.DEVOPS` | 0 | Faltava justamente a grafia correta "Azure DevOps": a skill só tinha erros de digitação (AZUERE, AZZURE). VSTS é o nome antigo. |
| 24 | AZUREDEVOPS | `VSTS` | 202 | Faltava justamente a grafia correta "Azure DevOps": a skill só tinha erros de digitação (AZUERE, AZZURE). VSTS é o nome antigo. |
| 25 | B.I | `BUSINESS INTELLIGENCE` | 1.052 | Faltava a grafia correta: a skill só tinha "INTELIGENCE" com um L. "BI" (2 letras) não é aceito pelo saneador. |
| 26 | B.I | `BUSINESS-INTELLIGENCE` | 6 | Faltava a grafia correta: a skill só tinha "INTELIGENCE" com um L. "BI" (2 letras) não é aceito pelo saneador. |
| 27 | B.I | `BUSINESS.INTELLIGENCE` | 0 | Faltava a grafia correta: a skill só tinha "INTELIGENCE" com um L. "BI" (2 letras) não é aceito pelo saneador. |
| 28 | B.I | `BUSINESSINTELLIGENCE` | 53 | Faltava a grafia correta: a skill só tinha "INTELIGENCE" com um L. "BI" (2 letras) não é aceito pelo saneador. |
| 29 | B.I | `INTELIGÊNCIA DE NEGÓCIO` | 148 | Forma acentuada de um sinônimo que já existia sem acento: o motor só põe o texto em maiúsculas (não tira acentos), e as vagas escrevem quase sempre com acento. A forma no singular também casa o plural. |
| 30 | BARRAMENTO | `ENTERPRISE SERVICE BUS` | 69 | Nome por extenso do ESB. |
| 31 | BDD | `BEHAVIOUR DRIVEN DEVELOPMENT` | 37 | Grafia britânica. |
| 32 | BDD | `BEHAVIOUR-DRIVEN DEVELOPMENT` | 3 | Grafia britânica. |
| 33 | BOOTSTRAP | `BOOSTRAP` | 164 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 34 | BSC | `BNB CHAIN` | 1 | Nome atual da Binance Smart Chain. |
| 35 | BSC | `BNB SMART CHAIN` | 0 | Nome atual da Binance Smart Chain. |
| 36 | CIRCLECI | `CIRCLE CI` | 159 | Grafias com separador usadas nas vagas. |
| 37 | CIRCLECI | `CIRCLE-CI` | 5 | Grafias com separador usadas nas vagas. |
| 38 | CLEAN ARCHITECTURE | `ARQUITETURA LIMPA` | 152 | Nome em português usado nas vagas. |
| 39 | CLOUDRA | `CLOUDERA` | 573 | A grafia certa é Cloudera; o nome da skill (CLOUDRA) não casava com nenhuma vaga. |
| 40 | CLOUDRA | `CLOUDERA CDH` | 1 | A grafia certa é Cloudera; o nome da skill (CLOUDRA) não casava com nenhuma vaga. |
| 41 | CLOUDRA | `CLOUDERA CDP` | 0 | A grafia certa é Cloudera; o nome da skill (CLOUDRA) não casava com nenhuma vaga. |
| 42 | COSMODB | `AZURE COSMOS DB` | 25 | Grafia correta "Cosmos DB"; o nome da skill (COSMODB) não casava com as vagas. |
| 43 | COSMODB | `COSMOS DB` | 91 | Grafia correta "Cosmos DB"; o nome da skill (COSMODB) não casava com as vagas. |
| 44 | COSMODB | `COSMOS-DB` | 0 | Grafia correta "Cosmos DB"; o nome da skill (COSMODB) não casava com as vagas. |
| 45 | COSMODB | `COSMOSDB` | 186 | Grafia correta "Cosmos DB"; o nome da skill (COSMODB) não casava com as vagas. |
| 46 | CSS | `CASCADING STYLE SHEETS` | 12 | Nome oficial (a skill só tinha "CASCADE STYLE SHEET"). |
| 47 | CSS | `CSS3` | 4.111 | "CSS3" é uma palavra só para o motor e não casava com CSS (3 letras exigem a palavra inteira). |
| 48 | CUCUMBER | `COCUMBER` | 53 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 49 | DATABASE | `SGBD` | 303 | Sigla em português de sistema gerenciador de banco de dados. |
| 50 | DATABASE | `SGBDS` | 86 | Sigla em português de sistema gerenciador de banco de dados. |
| 51 | DATALAKE | `DATA LAKE` | 1.269 | A skill não tinha sinônimos e as vagas escrevem "Data Lake" separado. |
| 52 | DATALAKE | `DATA-LAKE` | 6 | A skill não tinha sinônimos e as vagas escrevem "Data Lake" separado. |
| 53 | DATALAKE | `DATA.LAKE` | 0 | A skill não tinha sinônimos e as vagas escrevem "Data Lake" separado. |
| 54 | DBT | `DATA BUILD TOOL` | 18 | Nome por extenso. |
| 55 | DDD | `DOMAIN DRIVEN DESIGN` | 705 | Grafia sem hífen, a mais comum nas vagas. |
| 56 | DELTA LAKE | `DELTALAKE` | 2 | Grafia junta. |
| 57 | DEPENDENCY MANAGER | `GERENCIADOR DE DEPENDÊNCIAS` | 41 | Forma acentuada de um sinônimo que já existia sem acento: o motor só põe o texto em maiúsculas (não tira acentos), e as vagas escrevem quase sempre com acento. |
| 58 | DEPH LIMIT | `DEPTH LIMIT` | 0 | Grafia correta; o nome da skill (DEPH) é erro de digitação. |
| 59 | DEPLOY | `DEPLOYMENT` | 1.354 | Formas usadas nas vagas. |
| 60 | DEPLOY | `DEPLOYS` | 325 | Formas usadas nas vagas. |
| 61 | DEVOPS | `CONTINUOUS DEPLOYMENT` | 168 | CI/CD em português e o deploy contínuo; as formas em inglês já eram sinônimos. |
| 62 | DEVOPS | `ENTREGA CONTINUA` | 115 | CI/CD em português e o deploy contínuo; as formas em inglês já eram sinônimos. |
| 63 | DEVOPS | `ENTREGA CONTÍNUA` | 1.051 | CI/CD em português e o deploy contínuo; as formas em inglês já eram sinônimos. |
| 64 | DEVOPS | `INTEGRACAO CONTINUA` | 1 | CI/CD em português e o deploy contínuo; as formas em inglês já eram sinônimos. |
| 65 | DEVOPS | `INTEGRAÇÃO CONTINUA` | 450 | Grafia com acento só no "ç", frequente nas vagas. |
| 66 | DEVOPS | `INTEGRAÇÃO CONTÍNUA` | 2.919 | CI/CD em português e o deploy contínuo; as formas em inglês já eram sinônimos. |
| 67 | DOCKER | `CONTEINER` | 909 | Grafia aportuguesada de container (também casa "conteineres" e "conteinerização"). |
| 68 | DOTNET | `NET` | 27.618 | Nas vagas ".NET" vira a palavra "NET" (o ponto é separador do motor): faltava esse sinônimo. |
| 69 | DYNAMICS 365 | `D365` | 44 | Formas usadas nas vagas. |
| 70 | DYNAMICS 365 | `DYNAMICS365` | 22 | Formas usadas nas vagas. |
| 71 | DYNAMICS 365 | `MICROSOFT DYNAMICS 365` | 125 | Formas usadas nas vagas. |
| 72 | DYNAMICS AX | `AXAPTA` | 0 | Nome original do Dynamics AX. |
| 73 | DYNAMODB | `DINAMODB` | 27 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 74 | ECOMERCE | `COMMERCE` | 3.223 | "E-COMMERCE", "E COMMERCE" e "E.COMMERCE" nunca casavam: o motor escolhe as palavras pelas 2 primeiras letras das palavras do texto, e "E-" não é início de palavra. Essas vagas só eram achadas por acaso, pela inversão "COMMERCE E" (removida). "COMMERCE" casa todas as grafias como trecho; em 2.897 das 3.173 vagas há e-commerce, loja, marketplace, varejo ou pagamento por perto. Nomes de produto mais longos (SAP COMMERCE, B2C COMMERCE, ORACLE COMMERCE CLOUD, WOOCOMMERCE) continuam indo para as skills deles. |
| 75 | EJB | `EJB3` | 166 | Versão citada como palavra única ("EJB3" não casava com EJB). |
| 76 | ELASTICSEARCH | `ELATICSEARCH` | 38 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 77 | ENTITY | `EF CORE` | 136 | Nome do Entity Framework atual. |
| 78 | ENTITY | `EFCORE` | 7 | Nome do Entity Framework atual. |
| 79 | ENTITY | `ENTITY FRAMEWORK CORE` | 237 | Nome do Entity Framework atual. |
| 80 | EPISERVER | `OPTIMIZELY CMS` | 0 | Nome atual do Episerver. |
| 81 | ESXI | `VMWARE ESX` | 117 | Nome antigo do hypervisor, com o fabricante (também casa "VMware ESXi"). "ESX" sozinho não entrou: é também o nome de uma consultoria que publica muitas vagas ("A ESX é uma empresa de tecnologia"), 35 das 84 vagas em que "ESX" aparece. |
| 82 | ETL | `ELT` | 143 | Variante do mesmo processo de integração de dados (carga antes da transformação); as vagas escrevem "ETL/ELT". |
| 83 | EVENT DRIVEN | `EVENT DRIVEN ARCHITECTURE` | 81 | Grafia com hífen, a mais comum. |
| 84 | EVENT DRIVEN | `EVENT-DRIVEN` | 354 | Grafia com hífen, a mais comum. |
| 85 | EVENT DRIVEN | `EVENTDRIVEN` | 18 | Grafia com hífen, a mais comum. |
| 86 | EXTJS | `SENCHA` | 69 | Fabricante do Ext JS, citado no lugar do produto. |
| 87 | F5 | `BIGIP` | 75 | Linha de produtos da F5 ("BIG-IP" com hífen e 6 letras o motor não acha). |
| 88 | F5 | `F5 BIG IP` | 8 | Linha de produtos da F5 ("BIG-IP" com hífen e 6 letras o motor não acha). |
| 89 | F5 | `F5 BIG-IP` | 24 | Linha de produtos da F5 ("BIG-IP" com hífen e 6 letras o motor não acha). |
| 90 | FIDO2 | `WEBAUTHN` | 2 | WebAuthn é a API web do FIDO2. |
| 91 | FORTIFY | `FORTFY` | 47 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 92 | GAME | `GAME DEV` | 31 | Formas usadas nas vagas de desenvolvimento de jogos. |
| 93 | GAME | `GAMEDEV` | 6 | Formas usadas nas vagas de desenvolvimento de jogos. |
| 94 | GATEWAY | `API GATEWAY` | 2.359 | Forma mais comum nas vagas; a skill só tinha quebras de "gate way". "AWS API GATEWAY" continua indo para GATEWAYAWS (a palavra mais longa vence). |
| 95 | GATEWAY | `API-GATEWAY` | 5 | Forma mais comum nas vagas; a skill só tinha quebras de "gate way". "AWS API GATEWAY" continua indo para GATEWAYAWS (a palavra mais longa vence). |
| 96 | GATEWAY | `APIGATEWAY` | 87 | Forma mais comum nas vagas; a skill só tinha quebras de "gate way". "AWS API GATEWAY" continua indo para GATEWAYAWS (a palavra mais longa vence). |
| 97 | GATEWAYAWS | `AMAZON API GATEWAY` | 38 | Nome do serviço da AWS. |
| 98 | GATEWAYAWS | `AWS API GATEWAY` | 92 | Nome do serviço da AWS. |
| 99 | GDPR | `GENERAL DATA PROTECTION REGULATION` | 1 | Nome por extenso. |
| 100 | GENERATIVEAI | `GENAI` | 105 | A skill não tinha sinônimos. |
| 101 | GENERATIVEAI | `GENERATIVE AI` | 38 | A skill não tinha sinônimos. |
| 102 | GENERATIVEAI | `IA GENERATIVA` | 277 | A skill não tinha sinônimos. |
| 103 | GOOGLE CLOUD API | `CLOUD ENDPOINTS` | 0 | Nome do produto (a própria descrição o cita). |
| 104 | GOOGLE CLOUD API | `GOOGLE CLOUD ENDPOINTS` | 0 | Nome do produto (a própria descrição o cita). |
| 105 | GPT | `CHAT GPT` | 4 | Forma como as vagas citam os modelos da OpenAI. |
| 106 | GPT | `CHATGPT` | 71 | Forma como as vagas citam os modelos da OpenAI. |
| 107 | GPT | `OPENAI` | 115 | Forma como as vagas citam os modelos da OpenAI. |
| 108 | HADOOP | `HADDOP` | 42 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 109 | HANNA SQL | `CDS VIEW` | 90 | Singular, comum nas vagas (o plural já era sinônimo). |
| 110 | HEDION GRAPHQL | `HELIDON GRAPHQL` | 0 | O framework se chama Helidon (o nome da skill tem erro de digitação). |
| 111 | HEXAGONAL ARCHITECTURE | `ARQUITETURA HEXAGONAL` | 365 | Nomes usados nas vagas. |
| 112 | HEXAGONAL ARCHITECTURE | `PORTS AND ADAPTERS` | 0 | Nomes usados nas vagas. |
| 113 | HONE | `HONO` | 0 | O nome correto do framework é Hono (o pai BUN mostra que é ele). |
| 114 | HTML | `HTML5` | 6.219 | HTML5 é a forma mais comum nas vagas e não casava ("HTML5" é outra palavra). |
| 115 | HTML | `HYPERTEXT MARKUP LANGUAGE` | 6 | HTML5 é a forma mais comum nas vagas e não casava ("HTML5" é outra palavra). |
| 116 | HYBRIS | `SAP COMMERCE` | 88 | Nome atual do Hybris. |
| 117 | HYBRIS | `SAP COMMERCE CLOUD` | 137 | Nome atual do Hybris. |
| 118 | IAC | `IAAC` | 38 | Sigla alternativa de Infrastructure as Code. |
| 119 | IAM | `IDENTITY AND ACCESS MANAGEMENT` | 48 | Nome por extenso. |
| 120 | IBM BROKER | `APP CONNECT ENTERPRISE` | 2 | Nomes atuais e antigos do produto. |
| 121 | IBM BROKER | `IBM APP CONNECT` | 1 | Nomes atuais e antigos do produto. |
| 122 | IBM BROKER | `IBM INTEGRATION BUS` | 110 | Nomes atuais e antigos do produto. |
| 123 | IBM BROKER | `WEBSPHERE MESSAGE BROKER` | 23 | Nomes atuais e antigos do produto. |
| 124 | IBM MQ | `WEBSPHERE MQ` | 75 | Nome intermediário do produto. |
| 125 | ICOMON | `ICOMOON` | 0 | O nome correto é IcoMoon. |
| 126 | IP | `ENDERECAMENTO IP` | 0 | A skill IP nunca era achada (2 letras ficam fora do motor) e é pai de DHCP, CISCO e VPC; as versões do protocolo e o endereçamento são como as vagas citam IP. |
| 127 | IP | `ENDEREÇAMENTO IP` | 65 | A skill IP nunca era achada (2 letras ficam fora do motor) e é pai de DHCP, CISCO e VPC; as versões do protocolo e o endereçamento são como as vagas citam IP. |
| 128 | IP | `INTERNET PROTOCOL` | 4 | A skill IP nunca era achada (2 letras ficam fora do motor) e é pai de DHCP, CISCO e VPC; as versões do protocolo e o endereçamento são como as vagas citam IP. |
| 129 | IP | `IPV4` | 65 | A skill IP nunca era achada (2 letras ficam fora do motor) e é pai de DHCP, CISCO e VPC; as versões do protocolo e o endereçamento são como as vagas citam IP. |
| 130 | IP | `IPV6` | 103 | A skill IP nunca era achada (2 letras ficam fora do motor) e é pai de DHCP, CISCO e VPC; as versões do protocolo e o endereçamento são como as vagas citam IP. |
| 131 | JAVA | `J2SE` | 36 | Kit e edição padrão do Java, citados no lugar do nome da linguagem ("JDK 17"). |
| 132 | JAVA | `JAVA SE` | 867 | Kit e edição padrão do Java, citados no lugar do nome da linguagem ("JDK 17"). |
| 133 | JAVA | `JDK` | 155 | Kit e edição padrão do Java, citados no lugar do nome da linguagem ("JDK 17"). |
| 134 | JAVA | `JSE` | 131 | Kit e edição padrão do Java, citados no lugar do nome da linguagem ("JDK 17"). |
| 135 | JAVAEE | `JAKARTA EE` | 9 | Nome atual do Java EE. |
| 136 | JAVAEE | `JAKARTAEE` | 1 | Nome atual do Java EE. |
| 137 | JAVASCRIPT | `ES5` | 78 | Versões do ECMAScript, que já era sinônimo: "ES6" não casava com nenhuma skill. |
| 138 | JAVASCRIPT | `ES6` | 1.214 | Versões do ECMAScript, que já era sinônimo: "ES6" não casava com nenhuma skill. |
| 139 | JAVASCRIPT | `ES6+` | 685 | Versões do ECMAScript, que já era sinônimo: "ES6" não casava com nenhuma skill. |
| 140 | JAVASCRIPT | `JAVACRIPT` | 55 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 141 | JAVASCRIPT | `JAVASCRIP` | 61 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 142 | JAVASCRIPT | `JAVASCRIT` | 80 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 143 | JAVASCRIPT | `JAVASCRITP` | 56 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 144 | JAVASCRIPT | `JS VANILLA` | 5 | JavaScript puro, sem framework. |
| 145 | JAVASCRIPT | `VANILLA JS` | 25 | JavaScript puro, sem framework. |
| 146 | JAVASCRIPT | `VANILLAJS` | 23 | JavaScript puro, sem framework. |
| 147 | JDBC | `JAVA DATABASE CONNECTIVITY` | 0 | Grafia correta (a skill só tinha "CONECTIVITY"). |
| 148 | JPA | `JPA2` | 70 | Versão citada como palavra única. |
| 149 | JSF | `JAVASERVER FACES` | 38 | Grafia oficial junta. |
| 150 | JSF | `JSF2` | 74 | Versão citada como palavra única. |
| 151 | JSP | `JAVASERVER PAGES` | 0 | Grafia oficial junta. |
| 152 | JUNIT | `JUNIT JUPITER` | 7 | Nome do motor do JUnit 5. |
| 153 | K6 | `GRAFANA K6` | 1 | A skill K6 nunca era achada (2 caracteres); é o nome oficial atual da ferramenta. |
| 154 | KAFKA | `KAKFA` | 64 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 155 | KDB | `KDB+` | 0 | Nome oficial do banco. |
| 156 | KEYCLOAK | `KEYCLOACK` | 66 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 157 | KUBERNETES | `K8S` | 247 | Abreviação muito usada nas vagas. |
| 158 | KUBERNETES | `KUBERNATES` | 187 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 159 | LAKEHOUSE | `DATA LAKEHOUSE` | 25 | Formas usadas nas vagas. |
| 160 | LAKEHOUSE | `LAKE HOUSE` | 17 | Formas usadas nas vagas. |
| 161 | LAMBDA | `AWS LAMBDA` | 489 | Nome completo do serviço. |
| 162 | LARAVEL | `LAVAREL` | 60 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 163 | LGPD | `LEI GERAL DE PROTECAO DE DADOS` | 0 | Nome por extenso. |
| 164 | LGPD | `LEI GERAL DE PROTEÇÃO DE DADOS` | 225 | Nome por extenso. |
| 165 | LGPD | `LGDP` | 47 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 166 | LLAMAINDEX | `LLAMA INDEX` | 3 | Formas separadas usadas nas vagas. |
| 167 | LLAMAINDEX | `LLAMA-INDEX` | 1 | Formas separadas usadas nas vagas. |
| 168 | LLM | `LARGE LANGUAGE MODELS` | 30 | Plural, comum nas vagas. |
| 169 | LLM | `LLMS` | 249 | Plural, comum nas vagas. |
| 170 | LOADBALANCE | `BALANCEAMENTO DE CARGA` | 79 | Formas usadas nas vagas. |
| 171 | LOADBALANCE | `LOAD BALANCER` | 236 | Formas usadas nas vagas. |
| 172 | LOADBALANCE | `LOAD BALANCING` | 38 | Formas usadas nas vagas. |
| 173 | LOOKER STUDIO | `DATA STUDIO` | 117 | Nome antigo do produto. |
| 174 | LOOKER STUDIO | `GOOGLE DATA STUDIO` | 84 | Nome antigo do produto. |
| 175 | MACHINELEARNING | `ARTIFICIAL INTELLIGENCE` | 51 | Grafia correta em inglês e a forma acentuada, que é a usada nas vagas. |
| 176 | MACHINELEARNING | `INTELIGÊNCIA ARTIFICIAL` | 1.300 | Grafia correta em inglês e a forma acentuada, que é a usada nas vagas. |
| 177 | MACHINELEARNING | `INTELIGÊNCIAARTIFICIAL` | 30 | Forma acentuada de um sinônimo que já existia sem acento: o motor só põe o texto em maiúsculas (não tira acentos), e as vagas escrevem quase sempre com acento. |
| 178 | MAGENTO | `ADOBE COMMERCE` | 29 | Nome atual do Magento Commerce desde a compra pela Adobe. |
| 179 | MATERIAL UI | `MUI` | 14 | Nome atual da biblioteca. |
| 180 | MAUI | `NET MAUI` | 13 | Nome oficial (.NET MAUI vira "NET MAUI" no motor). |
| 181 | MENSAGERIA | `MENSAGEIRA` | 56 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 182 | MENSAGERIA | `MENSSAGERIA` | 64 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 183 | MENSAGERIA | `MESSAGE BROKER` | 170 | Nomes em inglês da categoria. |
| 184 | MENSAGERIA | `MESSAGE BROKERS` | 104 | Nomes em inglês da categoria. |
| 185 | MENSAGERIA | `MESSAGE QUEUE` | 46 | Nomes em inglês da categoria. |
| 186 | MFA | `AUTENTICACAO MULTIFATOR` | 0 | Nome por extenso. |
| 187 | MFA | `AUTENTICAÇÃO MULTIFATOR` | 7 | Nome por extenso. |
| 188 | MFA | `MULTI FACTOR AUTHENTICATION` | 0 | Nome por extenso. |
| 189 | MFA | `MULTI-FACTOR AUTHENTICATION` | 3 | Nome por extenso. |
| 190 | MICRO FRONTEND | `MICRO FRONTENDS` | 59 | Grafias usadas nas vagas. |
| 191 | MICRO FRONTEND | `MICRO-FRONTEND` | 101 | Grafias usadas nas vagas. |
| 192 | MICRO FRONTEND | `MICROFRONTEND` | 67 | Grafias usadas nas vagas. |
| 193 | MICRO FRONTEND | `MICROFRONTENDS` | 79 | Grafias usadas nas vagas. |
| 194 | MICROPROFILE GRAPHQL | `MICROPROFILE-GRAPHQL` | 0 | Grafia correta e a implementação mais usada. |
| 195 | MICROPROFILE GRAPHQL | `SMALLRYE GRAPHQL` | 0 | Grafia correta e a implementação mais usada. |
| 196 | MOCK SERVICE WORKER | `MSW` | 0 | Sigla do projeto. |
| 197 | MOCKITO | `MOKITO` | 92 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 198 | MONGODB | `MONGOBD` | 28 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 199 | NGINX | `NGNIX` | 50 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 200 | NIGHTWATCHJS | `NIGHTWATCH` | 6 | Nome do projeto. |
| 201 | NLP | `PLN` | 10 | Sigla e nome em português. |
| 202 | NLP | `PROCESSAMENTO DE LINGUAGEM NATURAL` | 95 | Sigla e nome em português. |
| 203 | NX | `NRWL` | 3 | A skill NX nunca era achada (2 letras); Nrwl é a empresa e o escopo dos pacotes do Nx ("@nrwl/nx"). |
| 204 | OAUTH | `OAUTH 2.0` | 297 | Versão citada nas vagas. |
| 205 | OCI | `ORACLE CLOUD` | 673 | Forma usada nas vagas. |
| 206 | OIC | `ORACLE INTEGRATION CLOUD` | 105 | Nome por extenso. |
| 207 | OIDC | `OPENID CONNECT` | 126 | Nome por extenso. |
| 208 | OLAP | `ONLINE ANALYTICAL PROCESSING` | 0 | Nome por extenso. |
| 209 | OLTP | `ONLINE TRANSACTION PROCESSING` | 0 | Nome por extenso. |
| 210 | OPENLIBERTY | `LIBERTY PROFILE` | 133 | Versão comercial da IBM baseada no Open Liberty. |
| 211 | OPENLIBERTY | `WEBSPHERE LIBERTY` | 141 | Versão comercial da IBM baseada no Open Liberty. |
| 212 | OPENTELEMETRY | `OPEN TELEMETRY` | 5 | Sigla e grafia separada. |
| 213 | OPENTELEMETRY | `OTEL` | 2 | Sigla e grafia separada. |
| 214 | ORACLE COMMERCE | `ORACLE COMMERCE CLOUD` | 33 | Nome atual do produto, que é o que a descrição da skill já diz; as vagas escrevem "Oracle Commerce Cloud". |
| 215 | PATTERNS | `DESING PATTERNS` | 70 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 216 | PATTERNS | `PADRÕES DE PROJETO` | 2.028 | Forma acentuada de um sinônimo que já existia sem acento: o motor só põe o texto em maiúsculas (não tira acentos), e as vagas escrevem quase sempre com acento. |
| 217 | PEARL | `PERL` | 402 | A linguagem se chama Perl; o nome da skill (PEARL) não casava com nenhuma vaga. |
| 218 | PENTAHO | `KETTLE` | 32 | Ferramenta de ETL do Pentaho. |
| 219 | PENTAHO | `PENTAHO DATA INTEGRATION` | 63 | Ferramenta de ETL do Pentaho. |
| 220 | PHP | `HYPERTEXT PREPROCESSOR` | 0 | Grafia oficial junta. |
| 221 | POLIGON | `POLYGON` | 69 | Grafia correta; o nome da skill tem erro de digitação. |
| 222 | POO | `ORIENTACAO A OBJETOS` | 1 | Nome em português usado nas vagas. |
| 223 | POO | `ORIENTAÇÃO A OBJETOS` | 2.383 | Nome em português usado nas vagas. |
| 224 | POO | `PROGRAMACAO ORIENTADA A OBJETOS` | 0 | Nome em português usado nas vagas. |
| 225 | POO | `PROGRAMAÇÃO ORIENTADA A OBJETOS` | 1.759 | Nome em português usado nas vagas. |
| 226 | POSTGRESQL | `PGSQL` | 22 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). PGSQL é a abreviação usada pelo próprio PostgreSQL. |
| 227 | POSTGRESQL | `POSTGREES` | 190 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). PGSQL é a abreviação usada pelo próprio PostgreSQL. |
| 228 | PRESTO | `PRESTODB` | 0 | Nome do projeto. |
| 229 | PRISMAJS | `PRISMA ORM` | 8 | Nome usado na documentação. |
| 230 | PYTHON | `PYTHON3` | 22 | "Python3" junto é uma palavra só e não casava com PYTHON. |
| 231 | RABBITMQ | `RABBIMQ` | 52 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 232 | RABBITMQ | `RABITMQ` | 40 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 233 | RAILS | `ROR` | 41 | Sigla de Ruby on Rails. |
| 234 | RDS | `AMAZON RDS` | 19 | Nomes do mesmo serviço, com o fabricante. |
| 235 | RDS | `AMAZON RELATIONAL DATABASE SERVICE` | 6 | Nomes do mesmo serviço, com o fabricante. |
| 236 | RDS | `AWS RDS` | 97 | Nomes do mesmo serviço, com o fabricante. |
| 237 | REACTJS | `REACT` | 21.720 | Faltava a forma mais comum nas vagas: "React" sozinho não casava com REACTJS. |
| 238 | RHEL | `RED HAT ENTERPRISE LINUX` | 15 | Nome por extenso. |
| 239 | RPA | `ROBOTIC PROCESS AUTOMATION` | 121 | Nome por extenso. |
| 240 | RX | `PROGRAMACAO REATIVA` | 0 | A skill RX nunca era achada (2 letras); "programação reativa" é o nome do conceito descrito na skill e aparece nas vagas (RxJava, WebFlux, RxSwift). |
| 241 | RX | `PROGRAMAÇÃO REATIVA` | 398 | A skill RX nunca era achada (2 letras); "programação reativa" é o nome do conceito descrito na skill e aparece nas vagas (RxJava, WebFlux, RxSwift). |
| 242 | RX | `REACTIVE PROGRAMMING` | 41 | A skill RX nunca era achada (2 letras); "programação reativa" é o nome do conceito descrito na skill e aparece nas vagas (RxJava, WebFlux, RxSwift). |
| 243 | RX | `REACTIVEX` | 16 | A skill RX nunca era achada (2 letras); "programação reativa" é o nome do conceito descrito na skill e aparece nas vagas (RxJava, WebFlux, RxSwift). |
| 244 | SALESFORCE | `SFDC` | 143 | Abreviação de Salesforce.com usada nas vagas (143 vagas). |
| 245 | SAP AA | `ASSET ACCOUNTING` | 29 | Forma usada nas vagas (FI-AA sozinho, com hífen e 5 letras, o motor não acha). |
| 246 | SAP AA | `SAP FI-AA` | 73 | Forma usada nas vagas (FI-AA sozinho, com hífen e 5 letras, o motor não acha). |
| 247 | SAP BAP | `BAPI` | 450 | O nome correto é BAPI; o nome da skill (BAP) não casava com nenhuma vaga. |
| 248 | SAP BAP | `BAPIS` | 236 | O nome correto é BAPI; o nome da skill (BAP) não casava com nenhuma vaga. |
| 249 | SAP BAP | `SAP BAPI` | 2 | O nome correto é BAPI; o nome da skill (BAP) não casava com nenhuma vaga. |
| 250 | SAP BO | `BUSINESS OBJECTS` | 56 | Formas usadas nas vagas. |
| 251 | SAP BO | `BUSINESSOBJECTS` | 9 | Formas usadas nas vagas. |
| 252 | SAP BW/4HANNA | `BW/4HANA` | 13 | Grafia correta (HANA com um N). |
| 253 | SAP BW/4HANNA | `BW4HANA` | 13 | Grafia correta (HANA com um N). |
| 254 | SAP BW/4HANNA | `SAP BW/4HANA` | 34 | Grafia correta (HANA com um N). |
| 255 | SAP CPI | `SCPI` | 63 | Sigla de SAP Cloud Platform Integration. |
| 256 | SAP FIORI FOR S/4HANNA | `SAP FIORI FOR S/4HANA` | 0 | Grafia correta (HANA com um N). |
| 257 | SAP IDOC | `IDOC` | 547 | Forma usada nas vagas. |
| 258 | SAP IDOC | `IDOCS` | 250 | Forma usada nas vagas. |
| 259 | SAP PS | `PROJECT SYSTEM` | 45 | A skill SAP PS nunca era achada ("SAP PS" tem espaço e menos de 7 caracteres); as vagas escrevem "PS - Project System". |
| 260 | SAP PS | `SAP PROJECT SYSTEM` | 4 | A skill SAP PS nunca era achada ("SAP PS" tem espaço e menos de 7 caracteres); as vagas escrevem "PS - Project System". |
| 261 | SAP R/3 | `R/3` | 55 | Forma usada nas vagas. |
| 262 | SAP S/4HANNA | `S/4 HANA` | 1.345 | Grafia correta (HANA com um N): o nome da skill (HANNA) e os sinônimos com HANNA não casavam com as vagas. |
| 263 | SAP S/4HANNA | `S/4HANA` | 1.544 | Grafia correta (HANA com um N): o nome da skill (HANNA) e os sinônimos com HANNA não casavam com as vagas. |
| 264 | SAP S/4HANNA | `S4/HANA` | 361 | Grafia correta (HANA com um N): o nome da skill (HANNA) e os sinônimos com HANNA não casavam com as vagas. |
| 265 | SAP S/4HANNA | `SAP S/4 HANA` | 436 | Grafia correta (HANA com um N): o nome da skill (HANNA) e os sinônimos com HANNA não casavam com as vagas. |
| 266 | SAP S/4HANNA | `SAP S/4HANA` | 1.159 | Grafia correta (HANA com um N): o nome da skill (HANNA) e os sinônimos com HANNA não casavam com as vagas. |
| 267 | SAP S/4HANNA EMBEDDED ANALYTICS | `S/4HANA EMBEDDED ANALYTICS` | 2 | Grafia correta (HANA com um N). |
| 268 | SAP S/4HANNA FINANCE | `S/4HANA FINANCE` | 10 | Grafia correta (HANA com um N). |
| 269 | SAP S/4HANNA FINANCE | `SAP S/4HANA FINANCE` | 19 | Grafia correta (HANA com um N). |
| 270 | SAP S/4HANNA LOGISTICS | `S/4HANA LOGISTICS` | 13 | Grafia correta (HANA com um N). |
| 271 | SAP S/4HANNA LOGISTICS | `SAP S/4HANA LOGISTICS` | 4 | Grafia correta (HANA com um N). |
| 272 | SAP SAC | `SAP ANALYTICS CLOUD` | 150 | Nome do produto. |
| 273 | SAP SAP IS-OIL | `SAP IS-OIL` | 20 | SAP repetido no nome; esta é a forma usada nas vagas. |
| 274 | SAP UI5 | `OPENUI5` | 13 | Formas usadas nas vagas. |
| 275 | SAP UI5 | `UI5` | 346 | Formas usadas nas vagas. |
| 276 | SCIKITLEARN | `SCIKIT LEARN` | 11 | O nome oficial tem hífen; a skill não tinha sinônimos e não casava com as vagas. |
| 277 | SCIKITLEARN | `SCIKIT-LEARN` | 242 | O nome oficial tem hífen; a skill não tinha sinônimos e não casava com as vagas. |
| 278 | SCIKITLEARN | `SKLEARN` | 11 | O nome oficial tem hífen; a skill não tinha sinônimos e não casava com as vagas. |
| 279 | SCYLLADB | `SCYLLA` | 2 | Forma curta usada nas vagas. |
| 280 | SECURITY123 | `CIBER SEGURANÇA` | 8 | Forma acentuada de um sinônimo que já existia sem acento: o motor só põe o texto em maiúsculas (não tira acentos), e as vagas escrevem quase sempre com acento. |
| 281 | SECURITY123 | `CIBERSEGURANÇA` | 269 | Forma acentuada de um sinônimo que já existia sem acento: o motor só põe o texto em maiúsculas (não tira acentos), e as vagas escrevem quase sempre com acento. |
| 282 | SECURITY123 | `SEGURANCA CIBERNETICA` | 0 | "SEGURANCA CIBER" (inversão de CIBER SEGURANCA, removida) só casava como pedaço de "segurança cibernética"; o nome por extenso entra no lugar. |
| 283 | SECURITY123 | `SEGURANÇA CIBERNÉTICA` | 231 | "SEGURANCA CIBER" (inversão de CIBER SEGURANCA, removida) só casava como pedaço de "segurança cibernética"; o nome por extenso entra no lugar. |
| 284 | SECURITY123 | `SEGURANÇA DA INFORMAÇÃO` | 4.094 | Forma acentuada de um sinônimo que já existia sem acento: o motor só põe o texto em maiúsculas (não tira acentos), e as vagas escrevem quase sempre com acento. |
| 285 | SERENETY | `SERENITY` | 28 | Grafia correta; o nome da skill tem erro de digitação. |
| 286 | SERENETY | `SERENITY BDD` | 0 | Grafia correta; o nome da skill tem erro de digitação. |
| 287 | SERVERLESS | `SERVLESS` | 78 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 288 | SILVER STRIPER | `SILVER STRIPE` | 0 | O nome correto é SilverStripe. |
| 289 | SILVER STRIPER | `SILVERSTRIPE` | 0 | O nome correto é SilverStripe. |
| 290 | SINGLESTORE | `MEMSQL` | 1 | Nome antigo do SingleStore. |
| 291 | SO | `OPERATING SYSTEM` | 201 | Nome por extenso. |
| 292 | SO | `OPERATING SYSTEMS` | 56 | Nome por extenso. |
| 293 | SO | `SISTEMA OPERACIONAL` | 1.862 | Nome por extenso. |
| 294 | SO | `SISTEMAS OPERACIONAIS` | 3.539 | Nome por extenso. |
| 295 | SOA | `ARQUITETURA ORIENTADA A SERVICOS` | 0 | Grafia com hífen e nome em português. |
| 296 | SOA | `ARQUITETURA ORIENTADA A SERVIÇOS` | 201 | Grafia com hífen e nome em português. |
| 297 | SOA | `SERVICE-ORIENTED ARCHITECTURE` | 43 | Grafia com hífen e nome em português. |
| 298 | SOLIDJS | `SOLID JS` | 0 | Grafias usadas. |
| 299 | SOLIDJS | `SOLID.JS` | 0 | Grafias usadas. |
| 300 | SOLMAN | `SAP SOLUTION MANAGER` | 142 | Nome por extenso. |
| 301 | SOLMAN | `SOLUTION MANAGER` | 195 | Nome por extenso. |
| 302 | SPA | `SINGLE-PAGE APPLICATION` | 19 | Grafia com hífen. |
| 303 | SPRING AMPQ | `SPRING AMQP` | 5 | Grafia correta (AMQP). |
| 304 | SPRING AMPQ | `SPRING-AMQP` | 0 | Grafia correta (AMQP). |
| 305 | SPRING AMPQ | `SPRINGAMQP` | 0 | Grafia correta (AMQP). |
| 306 | SPRING CLOUD OPENFEIGN | `FEIGN` | 9 | Formas usadas nas vagas. |
| 307 | SPRING CLOUD OPENFEIGN | `FEIGN CLIENT` | 3 | Formas usadas nas vagas. |
| 308 | SPRING CLOUD OPENFEIGN | `OPENFEIGN` | 13 | Formas usadas nas vagas. |
| 309 | SPRING EXPRESSION LANGUAGE | `SPEL` | 2 | Sigla usada nas vagas. |
| 310 | SPRING MOCKMVC | `MOCKMVC` | 6 | Forma curta usada nas vagas. |
| 311 | SPRING OPENAPI | `SPRINGDOC` | 0 | Bibliotecas que geram o OpenAPI no Spring. |
| 312 | SPRING OPENAPI | `SPRINGFOX` | 2 | Bibliotecas que geram o OpenAPI no Spring. |
| 313 | SPRING WEBFLUX | `WEBFLUX` | 96 | Forma curta usada nas vagas. |
| 314 | SPRING WEBSERVICES | `SPRING WS` | 4 | Nome do projeto. |
| 315 | SPRING WEBSERVICES | `SPRING-WS` | 0 | Nome do projeto. |
| 316 | SPRINGBOOT | `SPRINGBOT` | 56 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 317 | SPRINGBOOT | `SPRINT BOOT` | 257 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 318 | SPRINGBOOT | `SPRINTBOOT` | 70 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 319 | SPRINGBOOT ACTUATOR | `SPRING BOOT ACTUATOR` | 3 | Grafia com espaço, a mais comum. |
| 320 | SPRINGBOOT ADMIN | `SPRING BOOT ADMIN` | 0 | Grafia com espaço, a mais comum. |
| 321 | SPRINGBOOT CLI | `SPRING BOOT CLI` | 0 | Grafia com espaço, a mais comum. |
| 322 | SPRINGBOOTSTARTER | `SPRING BOOT STARTER` | 1 | Grafias usadas nas vagas. |
| 323 | SPRINGBOOTSTARTER | `SPRING-BOOT-STARTER` | 0 | Grafias usadas nas vagas. |
| 324 | SQL | `SLQ` | 50 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 325 | SQL | `STRUCTURED QUERY LANGUAGE` | 22 | Nome por extenso. |
| 326 | SQLSERVER | `TRANSACT SQL` | 204 | Nome por extenso do T-SQL, que já era sinônimo. |
| 327 | SQLSERVER | `TRANSACT-SQL` | 248 | Nome por extenso do T-SQL, que já era sinônimo. |
| 328 | SRE | `SITE RELIABILITY` | 230 | Nome por extenso. |
| 329 | SRE | `SITE RELIABILITY ENGINEERING` | 86 | Nome por extenso. |
| 330 | SSR | `SERVER SIDE RENDERING` | 28 | Nome por extenso. |
| 331 | SSR | `SERVER-SIDE RENDERING` | 15 | Nome por extenso. |
| 332 | SUCCESSFACTORS | `SUCESSFACTORS` | 48 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 333 | SWAGGER | `SWAGGER UI` | 1 | Ferramenta mais citada do Swagger. |
| 334 | SYNAPSE | `AZURE SYNAPSE` | 48 | Nome completo do serviço. |
| 335 | SYNAPSE | `AZURE SYNAPSE ANALYTICS` | 39 | Nome completo do serviço. |
| 336 | TACACS | `TACACS+` | 20 | Versão atual do protocolo ("+" não é separador do motor). |
| 337 | TFS | `TFVC` | 105 | Controle de versão do TFS (Team Foundation Version Control). |
| 338 | TLS | `SSL/TLS` | 23 | Formas usadas nas vagas ("/" não é separador). |
| 339 | TLS | `TLS/SSL` | 2 | Formas usadas nas vagas ("/" não é separador). |
| 340 | TYPESCRIPT | `TYPSCRIPT` | 39 | Erro de digitação frequente nas vagas (o arquivo já registra erros comuns como sinônimos, ex.: PYTON, ZABIX). |
| 341 | VB | `VB6` | 394 | Versão mais citada. |
| 342 | VB.NET | `VISUAL BASIC .NET` | 33 | Nome por extenso. |
| 343 | VBA | `VISUAL BASIC FOR APPLICATIONS` | 6 | Nome por extenso. |
| 344 | VERSIONING | `CONTROLE DE VERSÃO` | 3.311 | Forma acentuada de um sinônimo que já existia sem acento: o motor só põe o texto em maiúsculas (não tira acentos), e as vagas escrevem quase sempre com acento. |
| 345 | VERSIONING | `CONTROLE DE VERSÕES` | 222 | Forma acentuada de um sinônimo que já existia sem acento: o motor só põe o texto em maiúsculas (não tira acentos), e as vagas escrevem quase sempre com acento. |
| 346 | VERSIONING | `VERSIONADOR` | 133 | Termo em português (também casa "versionadores"). |
| 347 | VERSIONING | `VERSIONAMENTO` | 8.000 | Forma em português, a mais comum nas vagas. |
| 348 | VERTEXAI | `VERTEX AI` | 41 | Grafia separada. |
| 349 | VETORIALDB | `BANCO DE DADOS VETORIAL` | 0 | Nomes usados nas vagas. |
| 350 | VETORIALDB | `BANCO VETORIAL` | 0 | Nomes usados nas vagas. |
| 351 | VETORIALDB | `VECTOR DATABASE` | 17 | Nomes usados nas vagas. |
| 352 | VETORIALDB | `VECTOR DB` | 3 | Nomes usados nas vagas. |
| 353 | VIRTUALENV | `VENV` | 2 | Módulo venv do Python. |
| 354 | VIRTUALIZATION | `VIRTUALIZAÇÃO` | 2.070 | Forma acentuada de um sinônimo que já existia sem acento: o motor só põe o texto em maiúsculas (não tira acentos), e as vagas escrevem quase sempre com acento. |
| 355 | VIRTUALIZATION | `VIRTUALIZAÇÕES` | 5 | Forma acentuada de um sinônimo que já existia sem acento: o motor só põe o texto em maiúsculas (não tira acentos), e as vagas escrevem quase sempre com acento. |
| 356 | WEBDRIVERIO | `WDIO` | 0 | Nome do pacote e da CLI do WebdriverIO. |
| 357 | WEBNONYXS | `WEBONYX` | 0 | Grafia correta. |
| 358 | WINFORMS | `WINDOWS FORMS` | 372 | Nome por extenso. |
| 359 | WSO2 | `WSO2 IDENTITY SERVER` | 14 | Produto da WSO2 citado nas vagas; como palavra mais longa, impede que essas vagas ganhem IDENTITYSERVER (o IdentityServer do .NET, outro produto). |
| 360 | YII | `YII2` | 85 | Versão citada como palavra única. |
| 361 | Z/OS | `ZOS` | 71 | Grafia sem barra. |
| 362 | ZEND | `LAMINAS` | 3 | Nome atual do Zend Framework. |

## 5. Sinônimos que mudaram de skill

Além destes, as palavras da coluna "Veio de" da seção 2 saíram de outras skills para as skills novas.

| # | Sinônimo | De | Para | Vagas | Justificativa |
|---|---|---|---|---:|---|
| 1 | `NUXT JS` | VUE | NUXT | 0 | Nuxt é framework sobre o Vue e já é skill; estava como sinônimo do Vue. |
| 2 | `NUXT-JS` | VUE | NUXT | 0 | Nuxt é framework sobre o Vue e já é skill; estava como sinônimo do Vue. |
| 3 | `NUXT.JS` | VUE | NUXT | 120 | Nuxt é framework sobre o Vue e já é skill; estava como sinônimo do Vue. |
| 4 | `NUXTJS` | VUE | NUXT | 22 | Nuxt é framework sobre o Vue e já é skill; estava como sinônimo do Vue. |
| 5 | `S4 HANA` | HANNA | SAP S/4HANNA | 308 | S/4HANA é o ERP, que roda sobre o banco HANA; essas formas estavam no banco (HANNA). |
| 6 | `S4 HANNA` | HANNA | SAP S/4HANNA | 32 | S/4HANA é o ERP, que roda sobre o banco HANA; essas formas estavam no banco (HANNA). |
| 7 | `S4-HANA` | HANNA | SAP S/4HANNA | 0 | S/4HANA é o ERP, que roda sobre o banco HANA; essas formas estavam no banco (HANNA). |
| 8 | `S4-HANNA` | HANNA | SAP S/4HANNA | 0 | S/4HANA é o ERP, que roda sobre o banco HANA; essas formas estavam no banco (HANNA). |
| 9 | `S4.HANA` | HANNA | SAP S/4HANNA | 0 | S/4HANA é o ERP, que roda sobre o banco HANA; essas formas estavam no banco (HANNA). |
| 10 | `S4.HANNA` | HANNA | SAP S/4HANNA | 0 | S/4HANA é o ERP, que roda sobre o banco HANA; essas formas estavam no banco (HANNA). |
| 11 | `S4HANA` | HANNA | SAP S/4HANNA | 694 | S/4HANA é o ERP, que roda sobre o banco HANA; essas formas estavam no banco (HANNA). |
| 12 | `S4HANNA` | HANNA | SAP S/4HANNA | 23 | S/4HANA é o ERP, que roda sobre o banco HANA; essas formas estavam no banco (HANNA). |
| 13 | `SWAGER` | OPENAPI | SWAGGER | 19 | Erro de digitação de Swagger. |
| 14 | `TENSOR FLOW` | NLP | TENSORFLOW | 20 | TensorFlow é framework de machine learning (e já é skill), não outro nome de NLP. |
| 15 | `TENSOR-FLOW` | NLP | TENSORFLOW | 0 | TensorFlow é framework de machine learning (e já é skill), não outro nome de NLP. |
| 16 | `TENSOR.FLOW` | NLP | TENSORFLOW | 0 | TensorFlow é framework de machine learning (e já é skill), não outro nome de NLP. |

## 6. Sinônimos removidos um a um

| # | Skill | Sinônimo | Vagas em que casava | Ficam sem a skill | Justificativa |
|---|---|---|---:|---:|---|
| 1 | ASSERTJ | `ASSEERT J` | 0 | 0 | Grafia errada (ASSEERT) que quebra o nome AssertJ; não ocorre nas vagas. |
| 2 | ASSERTJ | `ASSEERT-J` | 0 | 0 | Grafia errada (ASSEERT) que quebra o nome AssertJ; não ocorre nas vagas. |
| 3 | ASSERTJ | `ASSEERT.J` | 0 | 0 | Grafia errada (ASSEERT) que quebra o nome AssertJ; não ocorre nas vagas. |
| 4 | AURORA | `SAPAURORA` | 0 | 0 | Não existe produto SAP Aurora; o Aurora é da AWS. |
| 5 | AUTENTICATION | `AUTHORIZATION` | 64 | 37 | Autorização (o que o usuário pode fazer) é outro conceito, não outro nome de autenticação (quem ele é). |
| 6 | BTRIEVE | `B TRIEVE SQL` | 0 | 0 | Quebra sem sentido do nome. |
| 7 | CLOUDDATA | `DATA CLOUD` | 67 | 57 | "Data Cloud" é o nome de um produto da Salesforce: casava vagas de Salesforce como data warehouse em nuvem. |
| 8 | CUCUMBER | `CUCUMBER REPOR` | 0 | 0 | Palavra truncada (REPOR); não ocorre nas vagas. |
| 9 | CUCUMBER | `CUCUMBER-REPOR` | 0 | 0 | Palavra truncada (REPOR); não ocorre nas vagas. |
| 10 | CUCUMBER | `CUCUMBER.REPOR` | 0 | 0 | Palavra truncada (REPOR); não ocorre nas vagas. |
| 11 | CUCUMBER | `CUCUMBERREPOR` | 0 | 0 | Palavra truncada (REPOR); não ocorre nas vagas. |
| 12 | DDD | `DOMAIN DESIGN DRIVEN` | 0 | 0 | Ordem errada das palavras. |
| 13 | DELPHI | `DELHI` | 38 | 38 | Cidade da Índia: em 37 das 38 vagas em que casava, "Delhi" é o local da vaga ("Location: Delhi", "Delhi NCR"), não erro de digitação de Delphi. |
| 14 | DELPHI | `PASCAL` | 26 | 11 | Pascal é outra linguagem; o Delphi usa Object Pascal (que continua como sinônimo). |
| 15 | DEPLOY | `BUILD` | 2.089 | 468 | Build (compilar e empacotar) não é deploy (implantar); "build" casava qualquer frase ("Docker build", "build de interfaces"). |
| 16 | DGRAPH | `DQL` | 43 | 43 | DQL é a linguagem de consulta do Dgraph (e também do Doctrine, em PHP), não outro nome do banco. |
| 17 | ELIXIR | `PHOENIX` | 102 | 59 | Phoenix é o framework web do Elixir (e nome de empresas), não outro nome da linguagem. |
| 18 | FASTIFY | `MERCURIUS` | 1 | 0 | Mercurius é o plugin GraphQL do Fastify, não outro nome dele. |
| 19 | GCP | `G CLOUD` | 0 | 0 | Quebra sem sentido de GCLOUD. |
| 20 | GRAPHQL.NET | `GRAPHQL-.NET` | 0 | 0 | Pontuação dobrada sem sentido. |
| 21 | GRAPHQL.NET | `GRAPHQL..NET` | 0 | 0 | Pontuação dobrada sem sentido. |
| 22 | GRIZZLY | `NETTY` | 3 | 3 | Netty é outro framework e já é skill (o sinônimo igual ao nome de outra skill confundia o motor). |
| 23 | GRIZZLY | `NETY` | 0 | 0 | Netty é outro framework e já é skill (o sinônimo igual ao nome de outra skill confundia o motor). |
| 24 | HEDION GRAPHQL | `HEDION-GRAPPHQL` | 0 | 0 | Erro de digitação (GRAPPHQL). |
| 25 | HEDION GRAPHQL | `HEDION.GRAPPHQL` | 0 | 0 | Erro de digitação (GRAPPHQL). |
| 26 | HEDION GRAPHQL | `HEDIONGRAPPHQL` | 0 | 0 | Erro de digitação (GRAPPHQL). |
| 27 | IONIC | `CORDOVA` | 386 | 125 | Cordova é outra ferramenta e já é skill. |
| 28 | IREPORT | `IREPOPRT` | 0 | 0 | Erro de digitação. |
| 29 | JQUERY | `J QUERY JS` | 0 | 0 | Quebra sem sentido do nome. |
| 30 | JSF | `XHTML` | 58 | 49 | XHTML é formato de página, não outro nome do JSF. |
| 31 | JUNIT | `JUPTER` | 1 | 1 | Erro de digitação de "Jupiter". |
| 32 | MATERIAL UI | `MATERIAL` | 4.249 | 4.249 | "MATERIAL" é palavra comum ("material de apoio", "material didático"): fazia 36% das vagas com MATERIAL UI terem só essa skill. |
| 33 | MICROPROFILE GRAPHQL | `MICROPROFILE-GRAPPHQL` | 0 | 0 | Erro de digitação (GRAPPHQL). |
| 34 | MICROPROFILE GRAPHQL | `MICROPROFILE.GRAPPHQL` | 0 | 0 | Erro de digitação (GRAPPHQL). |
| 35 | MICROPROFILE GRAPHQL | `MICROPROFILEGRAPPHQL` | 0 | 0 | Erro de digitação (GRAPPHQL). |
| 36 | NEAR PROTOCOL | `NEAR` | 29 | 29 | "Near" é palavra comum ("near real time"): toda vaga com essa expressão virava blockchain. |
| 37 | NLP | `TENSORFLOW` | 364 | 286 | TensorFlow já é skill: o sinônimo igual ao nome de outra skill confundia o motor. |
| 38 | OPENAPI | `SWAGGER` | 2.474 | 0 | Swagger já é skill: o sinônimo igual ao nome de outra skill confundia o motor. |
| 39 | PANDAS | `PANDA` | 29 | 29 | "Panda" é nome de antivírus e de pessoas, não da biblioteca. |
| 40 | POSTGRAPHILE | `SUPABASE GRAPHQL` | 0 | 0 | O Supabase usa pg_graphql, não o PostGraphile. |
| 41 | POSTGRAPHILE | `SUPABASE-GRAPHQL` | 0 | 0 | O Supabase usa pg_graphql, não o PostGraphile. |
| 42 | POSTGRAPHILE | `SUPABASE.GRAPHQL` | 0 | 0 | O Supabase usa pg_graphql, não o PostGraphile. |
| 43 | POSTGRAPHILE | `SUPABASEGRAPHQL` | 0 | 0 | O Supabase usa pg_graphql, não o PostGraphile. |
| 44 | REDMINE | `READ MINE` | 0 | 0 | Quebra sem sentido do nome. |
| 45 | REDMINE | `READ-MINE` | 0 | 0 | Quebra sem sentido do nome. |
| 46 | REDMINE | `READ.MINE` | 0 | 0 | Quebra sem sentido do nome. |
| 47 | REPORT | `REPORTS` | 2.035 | 1.676 | Em inglês, "reports" nas vagas é quase sempre relatório de acompanhamento ("status reports", "reports executivos"): em 1.098 das 1.803 vagas em que casava não há ferramenta de relatório, SQL ou BI por perto. As ferramentas (CRYSTAL REPORTS, JASPER, SSRS, ITEXT) são filhas de REPORT e continuam trazendo a categoria. O próprio nome REPORT tem o mesmo problema e está nas observações. |
| 48 | RMI | `JAVA NAME AND DIRECTORY INTERFACE` | 0 | 0 | JNDI é outra API (diretório de nomes). |
| 49 | RMI | `JAVA NAME DIRECTORY INTERFACE` | 0 | 0 | JNDI é outra API (diretório de nomes). |
| 50 | RMI | `JNDI` | 22 | 17 | JNDI é outra API (diretório de nomes). |
| 51 | SALESFORCE | `APEX` | 1.370 | 383 | Apex é a linguagem da Salesforce, mas o Oracle APEX tem o mesmo nome. Das 1.370 vagas em que APEX casava, 383 não citam Salesforce nem produto dela: 294 têm Oracle, PL/SQL ou Forms por perto, 88 não têm contexto, e nenhuma tem só contexto de Salesforce. As vagas de Salesforce citam a plataforma ou um produto dela e continuam recebendo a skill. APPEX (erro de digitação) sai pelo mesmo motivo. |
| 52 | SALESFORCE | `APPEX` | 3 | 0 | Apex é a linguagem da Salesforce, mas o Oracle APEX tem o mesmo nome. Das 1.370 vagas em que APEX casava, 383 não citam Salesforce nem produto dela: 294 têm Oracle, PL/SQL ou Forms por perto, 88 não têm contexto, e nenhuma tem só contexto de Salesforce. As vagas de Salesforce citam a plataforma ou um produto dela e continuam recebendo a skill. APPEX (erro de digitação) sai pelo mesmo motivo. |
| 53 | SAP SAC | `SAP SAP ANALYTICS CLOUD` | 0 | 0 | SAP repetido. |
| 54 | SPLUNK | `APACHE SPLUNK` | 0 | 0 | O Splunk não é projeto Apache. |
| 55 | SPLUNK | `APACHE-SPLUNK` | 0 | 0 | O Splunk não é projeto Apache. |
| 56 | SPLUNK | `APACHE.SPLUNK` | 0 | 0 | O Splunk não é projeto Apache. |
| 57 | SPLUNK | `APACHESPLUNK` | 0 | 0 | O Splunk não é projeto Apache. |
| 58 | SPRING BATCH | `SPRING SCHEDULING` | 0 | 0 | O agendamento do Spring (@Scheduled) é outro recurso, não o Spring Batch. |
| 59 | SPRING BATCH | `SPRING-SCHEDULING` | 0 | 0 | O agendamento do Spring (@Scheduled) é outro recurso, não o Spring Batch. |
| 60 | SPRING BATCH | `SPRING.SCHEDULING` | 0 | 0 | O agendamento do Spring (@Scheduled) é outro recurso, não o Spring Batch. |
| 61 | SPRING BATCH | `SPRINGSCHEDULING` | 0 | 0 | O agendamento do Spring (@Scheduled) é outro recurso, não o Spring Batch. |
| 62 | SPRING DATA | `SPRING IBATIS` | 0 | 0 | A integração do MyBatis/iBatis com o Spring é outro projeto, não o Spring Data. |
| 63 | SPRING DATA | `SPRING MY BATIS` | 0 | 0 | A integração do MyBatis/iBatis com o Spring é outro projeto, não o Spring Data. |
| 64 | SPRING DATA | `SPRING MYBATIS` | 0 | 0 | A integração do MyBatis/iBatis com o Spring é outro projeto, não o Spring Data. |
| 65 | SPRING DATA | `SPRING-IBATIS` | 0 | 0 | A integração do MyBatis/iBatis com o Spring é outro projeto, não o Spring Data. |
| 66 | SPRING DATA | `SPRING-MYBATIS` | 0 | 0 | A integração do MyBatis/iBatis com o Spring é outro projeto, não o Spring Data. |
| 67 | SPRING DATA | `SPRING.IBATIS` | 0 | 0 | A integração do MyBatis/iBatis com o Spring é outro projeto, não o Spring Data. |
| 68 | SPRING DATA | `SPRING.MYBATIS` | 0 | 0 | A integração do MyBatis/iBatis com o Spring é outro projeto, não o Spring Data. |
| 69 | SPRING DATA | `SPRINGIBATIS` | 0 | 0 | A integração do MyBatis/iBatis com o Spring é outro projeto, não o Spring Data. |
| 70 | SPRING DATA | `SPRINGMYBATIS` | 0 | 0 | A integração do MyBatis/iBatis com o Spring é outro projeto, não o Spring Data. |
| 71 | SPRING OPENAPI | `SPRING REST DOCS` | 13 | 13 | Spring REST Docs é outro projeto (documentação a partir de testes). |
| 72 | SPRING WEBCLIENT | `WEB CLIENT` | 46 | 46 | "Web client" é expressão genérica (qualquer cliente web); vieram do WEBCLIENT fundido. |
| 73 | SPRING WEBCLIENT | `WEB-CLIENT` | 0 | 0 | "Web client" é expressão genérica (qualquer cliente web); vieram do WEBCLIENT fundido. |
| 74 | SPRING WEBCLIENT | `WEB.CLIENT` | 0 | 0 | "Web client" é expressão genérica (qualquer cliente web); vieram do WEBCLIENT fundido. |
| 75 | SPRING WEBFLUX | `SPRING REACTIVE DATA` | 0 | 0 | Dados reativos são do Spring Data, não do WebFlux. |
| 76 | SPRINGBOOT DEVTOOLS | `DEV TOOLS` | 11 | 11 | "DevTools" sozinho é o painel de desenvolvedor dos navegadores; só as formas com "Spring Boot" são do módulo. |
| 77 | SPRINGBOOT DEVTOOLS | `DEV-TOOLS` | 0 | 0 | "DevTools" sozinho é o painel de desenvolvedor dos navegadores; só as formas com "Spring Boot" são do módulo. |
| 78 | SPRINGBOOT DEVTOOLS | `DEV.TOOLS` | 0 | 0 | "DevTools" sozinho é o painel de desenvolvedor dos navegadores; só as formas com "Spring Boot" são do módulo. |
| 79 | SPRINGBOOT DEVTOOLS | `DEVTOOLS` | 11 | 11 | "DevTools" sozinho é o painel de desenvolvedor dos navegadores; só as formas com "Spring Boot" são do módulo. |
| 80 | VERSIONING | `VERSAO` | 15 | 4 | "Version"/"versão" casam qualquer número de versão ("Java versão 17") e, como trecho, "conVERSION" e "diVERSOES". |
| 81 | VERSIONING | `VERSION` | 8.310 | 114 | "Version"/"versão" casam qualquer número de versão ("Java versão 17") e, como trecho, "conVERSION" e "diVERSOES". |
| 82 | VERSIONING | `VERSIONS` | 49 | 25 | "Version"/"versão" casam qualquer número de versão ("Java versão 17") e, como trecho, "conVERSION" e "diVERSOES". |
| 83 | VERSIONING | `VERSOES` | 1 | 0 | "Version"/"versão" casam qualquer número de versão ("Java versão 17") e, como trecho, "conVERSION" e "diVERSOES". |
| 84 | WEBDRIVER | `E2E` | 873 | 800 | E2E é tipo de teste, não outro nome do WebDriver (e está em removidas.txt). |
| 85 | WEBNONYXS | `GRAPHQL-PHP` | 0 | 0 | graphql-php já é sinônimo da categoria GRAPHQLPHP. |

## 7. Inversões removidas

Justificativa de todas: Inversão da ordem das palavras de outro nome da mesma skill (ex.: "MQ ACTIVE" para ACTIVE MQ): ninguém escreve assim nas vagas, e uma inversão de 7+ caracteres casa como trecho de frases sem relação.

Ficaram as inversões que são o nome usado de fato (ex.: `ANSI C`, `GRAPHENE DJANGO`, `DOMAIN-DRIVEN DESIGN`), as das categorias de combinação (ex.: `CMSJAVA`, `GRAPHQLNODE`), em que as duas ordens são naturais, e as que aparecem escritas assim de propósito nas vagas, com o sentido da skill (tabela 7.1). Para achar estas, conferi uma a uma as vagas que ficariam sem a skill depois de cada remoção; saíram mesmo assim as que só casavam outra coisa ou palavras vizinhas de uma lista (tabela 7.2).

### 7.1 Inversões e quebras que ficaram porque aparecem nas vagas

| # | Skill | Palavra | Como aparece nas vagas | Vagas em que casa | Ficariam sem a skill |
|---|---|---|---|---:|---:|
| 1 | GCP | `CLOUD GOOGLE` | "Analista Cloud Google", "métodos ágeis, Cloud Google e AWS" | 61 | 38 |
| 2 | SPRING BATCH | `BATCH SPRING` | "Analista Desenvolvedor Java (Batch Spring)" | 17 | 17 |
| 3 | IBM BROKER | `BROKER IBM` | "Oracle WebLogic, Message Broker IBM, Oracle SQL, IBM MQSeries" | 11 | 11 |
| 4 | WS | `API WEB` | "mecanismo de segurança na API Web (OAuth ou SAML)", "integradas com API Web" | 40 | 11 |
| 5 | ADOBEXD | `XD ADOBE` | "conhecer Figma ou XD Adobe" | 4 | 4 |
| 6 | EXPRESSJS | `JS EXPRESS` | "experiência com Node.js Express, TypeScript" | 4 | 4 |
| 7 | SAPCLOUD | `CLOUD SAP` | "ambiente Cloud SAP RISE", "integração com Cloud SAP – Fiori" | 7 | 4 |
| 8 | SAP BTP | `BTP SAP` | "Diferencial: BTP SAP Workflow" | 5 | 3 |
| 9 | SAP CRM | `CRM SAP` | "conhecimento do CRM SAP em Sales e Service" | 4 | 3 |
| 10 | MACHINELEARNING | `LEARNING MACHINE` | "RPA, learning machines e IA" | 22 | 3 |
| 11 | SECURITY123 | `SECURITY INFORMATION` | título "Security Information GRC" | 12 | 2 |
| 12 | CONTEXTAPI | `API CONTEXT` | "Redux, MobX, Recoil, Zustand ou a API Context" | 1 | 1 |
| 13 | IBM CLOUD | `CLOUD IBM` | "TI híbrida (Cloud IBM)" | 7 | 1 |
| 14 | SECURITY123 | `SECURITY CYBER` | título "Security Cyber \| Alphaville/SP" | 1 | 1 |
| 15 | JAVA SERVER | `SERVER JAVA` | "administração de application server Java (Jenkins/Git/JBoss/Apache)" | 2 | 1 |
| 16 | SOAPUI | `UI SOAP` | "Java, Maven, Jenkins, UI SOAP, JUnit" | 1 | 1 |
| 17 | MATLAB | `MAT LAB` | "Mat Lab, Simulink, CAD" (quebra do nome) | 1 | 1 |
| 18 | MYBATIS | `MY BATIS` | "SQL, lógica de programação, My Batis" (quebra do nome) | 1 | 1 |

### 7.2 Inversões e quebras removidas que tiram a skill de alguma vaga

| # | Skill | Palavra | Tipo | Ficam sem a skill | O que a palavra casava |
|---|---|---|---|---:|---|
| 1 | DATAFLOW | `DATA FLOW` | quebra | 99 | "data flow" genérico (fluxo de dados, ETL); só 6 das 100 vagas são do Google Cloud Dataflow |
| 2 | IREPORT | `REPORT I` | inversão | 52 | "report internacional", "report interno", "report in S4" |
| 3 | GCP | `CLOUD G` | inversão | 51 | "Oracle Cloud GL" (General Ledger), "Spring Cloud Gateway", "cloud governance", "cloud go-live" |
| 4 | BUILDR | `BUILD R` | quebra | 47 | "ferramentas de build relevantes", "build requested items", "build robust" |
| 5 | DATASTORE | `DATA STORE` | quebra | 34 | "data stores" genéricos ("Postgres for our persistent data store") |
| 6 | SAP BAP | `BAP SAP` | inversão | 26 | "desenvolvedor ABAP SAP" (trecho de ABAP) |
| 7 | DATAMART | `MART DATA` | inversão | 15 | "Smart Data", "SDI - Smart Data Integration" |
| 8 | POWERBI | `BI POWER` | inversão | 13 | "BI PowerCenter" (Informatica) |
| 9 | PYTEST | `TEST PY` | inversão | 13 | "test pyramid" |
| 10 | VB | `BASIC VISUAL` | inversão | 11 | "basic visual and user experience principles" |
| 11 | INTERBASE | `BASE INTER` | inversão | 10 | "base interior", "base interna", "base internacional" |
| 12 | SPRING WEB | `WEB SPRING` | inversão | 8 | palavras vizinhas de uma lista: "Java Web Spring Boot" (o Spring Boot continua achado) |
| 13 | VITEST | `TEST VI` | inversão | 7 | "Rational Test Virtualization Server" |
| 14 | VSCODE | `CODE VS` | inversão | 5 | "low-code vs custom" |
| 15 | FIREBASE | `BASE FIRE` | inversão | 4 | "database firewall" |
| 16 | SPRINGBOOT DEVTOOLS | `TOOLS DEV` | inversão | 4 | "reporting tools developer", "tools development" |
| 17 | DJANGO | `JANGO D` | inversão | 3 | "Python/Django developer": o Django é da vaga, mas só era achado por acaso; o motor não acha DJANGO em "PYTHON/DJANGO" porque "/" não separa palavras (seção 14) |
| 18 | NESTJS | `JS NEST` | inversão | 3 | "React.js nesta organização" |
| 19 | APPENGINE | `ENGINE APP` | inversão | 2 | "PeopleSoft Application Engine" |
| 20 | ASPNET | `NET ASP` | inversão | 2 | ".NET, ASP clássico" |
| 21 | BUILDR | `BUILD-R` | quebra | 2 | "build-run", "build-release engineer" |
| 22 | ECOMERCE | `COMERCE E` | inversão | 2 | "WooCommerce e OpenCart" escrito "WOOCOMERCE E" |
| 23 | JAVAFX | `FX JAVA` | inversão | 2 | palavras vizinhas de uma lista ("FX Java Python Angular") |
| 24 | OPENSTACK | `STACK OPEN` | inversão | 2 | "full-stack open source" |
| 25 | SPRINGREST | `REST SPRING` | inversão | 2 | palavras vizinhas de listas ("JPA REST Spring Boot JUnit", "Git – REST Spring – JavaScript") |
| 26 | CSHARP | `SHARP C` | inversão | 1 | "C Sharp C#": é C#, mas só era achado por acaso; o motor não acha "C SHARP" nem "C#" (seções 12 e 14) |
| 27 | EXADATA | `DATA EXA` | inversão | 1 | "data exata" |
| 28 | JAVASCRIPT | `SCRIPTS JAVA` | inversão | 1 | "experiência em scripts Java" (ServiceNow) |
| 29 | JQUERY | `QUERY J` | inversão | 1 | "query JavaScript" |
| 30 | REDSHIFT | `SHIFT RED` | inversão | 1 | "OpenShift Red Hat" |
| 31 | SPRING SECURITY | `SECURITY SPRING` | inversão | 1 | palavras vizinhas de uma lista ("API security, Spring Boot") |
| 32 | SPRINGBOOT | `BOOT SPRING` | inversão | 1 | palavras vizinhas de uma lista ("Spring MVC ou Boot, Spring Data") |
| 33 | SPRINGMVC | `MVC SPRING` | inversão | 1 | palavras vizinhas de uma lista ("Spring Core (Security, MVC, Spring Boot)") |
| 34 | TESTCOMPLETE | `COMPLETE TEST` | inversão | 1 | "complete test cases" |
| 35 | UNITTEST | `TEST UNIT` | inversão | 1 | "semi-industrial test units" |
| 36 | VISUALSTUDIO | `STUDIO VISUAL` | inversão | 1 | "Studio Visual" (nome de agência) |
| 37 | WEBCLIENT | `CLIENT WEB` | inversão | 1 | "service client webhosting" |

### 7.3 Todas as inversões removidas

Entre parênteses: vagas em que a palavra casava → vagas que ficam sem a skill. Sem parênteses: a palavra não casava em nenhuma vaga.

| # | Skill | Removidos |
|---|---|---|
| 1 | ACTIVEMQ | `MQ ACTIVE` |
| 2 | ADO.NET | `NET ADO` |
| 3 | ADONISJS | `JS ADONIS` |
| 4 | AGIL | `AGEIS METODOLOGIAS`, `AGIL METODOLOGIA`, `METHODOLOGY AGILE` |
| 5 | AIRFLOW | `AIRFLOW APACHE`, `FLOW AIR` |
| 6 | ALLURE REPORTS | `REPORTS ALLURE` |
| 7 | ANGULAR | `JS ANGULAR` (11 → 0) |
| 8 | APACHEAB | `AB APACHE` |
| 9 | APIGEE | `EE APIG` |
| 10 | APOLLO CLIENT | `CLIENT APOLLO` |
| 11 | APOLLO GATEWAY | `GATEWAY APOLLO` |
| 12 | APOLLO IOS | `IOS APOLLO` |
| 13 | APOLLO KOTLIN | `KOTLIN APOLLO` |
| 14 | APOLLO SERVER | `SERVER APOLLO` |
| 15 | APOLLO STUDIO | `STUDIO APOLLO` |
| 16 | APPCC | `CC APP` |
| 17 | APPDYNAMICS | `DYNAMICS APP` |
| 18 | APPENGINE | `ENGINE APP` (2 → 2) |
| 19 | ASPECTJ | `J ASPECT` |
| 20 | ASPNET | `FORM WEB`, `FORMS WEB` (1 → 0), `NET ASP` (11 → 2) |
| 21 | ASSERTJ | `J ASSEERT` |
| 22 | ASYNC GRAPHQL | `GRAPHQL ASYNC` |
| 23 | AWS | `WEBSERVICE AMAZON`, `WEBSERVICES AMAZON` |
| 24 | AXIS | `AXIS APACHE` |
| 25 | AZUREDEVOPS | `DEVOPS AZUERE`, `DEVOPS AZZURE` |
| 26 | B.I | `I B`, `INTELIGENCE BUSINESS` |
| 27 | BACKEND | `END BACK` (4 → 0) |
| 28 | BDD | `DEVELOPMENT BEHAVIOR-DRIVEN` |
| 29 | BIGDATA | `DATA BIG` (7 → 0) |
| 30 | BIGQUERY | `QUERY BIG` |
| 31 | BITBUCKET | `BUCKET BIT` |
| 32 | BITCOIN | `COIN BIT` |
| 33 | BLAZEMETER | `METER BLAZE` |
| 34 | BLOCKCHAIN | `CHAIN BLOCK` |
| 35 | BOOTSTRAP | `STRAP BOOT`, `STRAP BOT` |
| 36 | BTRIEVE | `SQL BTRIEVE` |
| 37 | BUGZILLA | `ZILA BUG`, `ZILLA BUG` |
| 38 | BUILDR | `R BUILD` |
| 39 | BURP SUITE | `SUITE BURP` |
| 40 | CAKE | `PHP CAKE` (5 → 0) |
| 41 | CHAI | `JS CHAI` |
| 42 | CHAKRA UI | `UI CHAKRA` |
| 43 | CHECKMARX | `MARX CHECK` |
| 44 | CHERWELL | `WELL CHER` |
| 45 | CITRIX | `APP XEN`, `SERVER XEN` |
| 46 | CLEARCASE | `CASE CLEAR` |
| 47 | CLOUDFORMATION | `FORMATION CLOUD` |
| 48 | CLOUDFRONT | `FRONT CLOUD` |
| 49 | CLOUDWATCH | `WATCH CLOUD` |
| 50 | COCOAPODS | `PODS COCOA` |
| 51 | CODECEPTION | `CEPTION CODE` |
| 52 | CODECOMMIT | `COMMIT CODE` |
| 53 | CODEIGNITER | `IGNITER CODE` |
| 54 | COSMODB | `DB COSMO` |
| 55 | COUCHDB | `DB COUCH` |
| 56 | CSHARP | `SHARP C` (1 → 1) |
| 57 | CUCUMBER | `REPOR CUCUMBER`, `REPORT CUCUMBER`, `REPORTS CUCUMBER` |
| 58 | DATABASE | `BASE DATA`, `BASES DATA` |
| 59 | DATABRICKS | `BRICK DATA`, `BRICKS DATA` |
| 60 | DATADOG | `DOG DATA` |
| 61 | DATAFLEX | `FLEX DATA` |
| 62 | DATAFLOW | `FLOW DATA` |
| 63 | DATAMART | `MART DATA` (15 → 15), `MARTS DATA` |
| 64 | DATASTAGE | `STAGE DATA` |
| 65 | DATASTORE | `STORE DATA` |
| 66 | DATASUL | `SUL DATA` |
| 67 | DELPHI | `PASCAL OBJECT` |
| 68 | DEPH LIMIT | `LIMIT DEPH` |
| 69 | DEVOPS | `CD CI`, `DELIVERY CONTINUOUS`, `INTEGRATION CONTINUOUS`, `OPS DEV` (14 → 0) |
| 70 | DEVSECOPS | `SECOPS DEV` (1 → 0) |
| 71 | DHTML | `HTML DYNAMIC` |
| 72 | DJANGO | `JANGO D` (4 → 3) |
| 73 | DOTCMS | `CMS DOT` |
| 74 | DOTNET | `NET DOT` (26 → 0), `NET PUNTO` |
| 75 | DOTNETNUKE | `NUKE DOTNET` |
| 76 | DYNAMODB | `DB DYNAMO` |
| 77 | DYNATRACE | `TRACE DYNA` |
| 78 | EASYB | `B EASY` |
| 79 | EASYMOCK | `MOCK EASY` |
| 80 | EASYTRIEVE | `TRIEVE EASY` |
| 81 | ECOMERCE | `COMERCE E` (2 → 2), `COMMERCE E` (567 → 0) |
| 82 | ELASTICSEARCH | `SEARCH ELASTIC` |
| 83 | EMBER | `JS EMBBER`, `JS EMBER` |
| 84 | ENT GRAPHQL | `GRAPHQL ENT` |
| 85 | ENTITY | `FRAMEWORK ENTITY` |
| 86 | ENZYME | `JS ENZYME` |
| 87 | EPISERVER | `SERVER EPI` |
| 88 | EUREKA | `EUREKA SPRING` |
| 89 | EXADATA | `DATA EXA` (1 → 1) |
| 90 | EXPRESS GRAPHQL | `GRAPHQL EXPRESS` |
| 91 | EXTENT REPORTS | `REPORTS EXTENT` |
| 92 | EXTJS | `JS EXT` |
| 93 | FETCH | `JS FETCH` |
| 94 | FIREBASE | `BASE FIRE` (4 → 4) |
| 95 | FIREBIRD | `BIRD FIRE` |
| 96 | FLASK GRAPHQL | `GRAPHQL FLASK` |
| 97 | FLEXBOX | `BOX FLEX` |
| 98 | FLUME | `FLUME APACHE` |
| 99 | FLYWAY | `WAY FLY` |
| 100 | FOXPRO | `PRO FOX` |
| 101 | FRONTEND | `END FRONT` (2 → 0) |
| 102 | FULLSTACK | `STACK FULL` (3 → 0) |
| 103 | FULLTEXT | `SEARCH FULLTEXT`, `TEXT FULL` |
| 104 | GATEWAY | `WAY GATE` |
| 105 | GCP | `CLOUD G` (192 → 51) |
| 106 | GITFLOW | `FLOW GIT` |
| 107 | GITHUB | `HUB GIT` |
| 108 | GITHUB ACTIONS | `ACTIONS GITHUB` |
| 109 | GITLAB | `LAB GIT` |
| 110 | GLASSFISH | `FISH GLASS` |
| 111 | GOLANG | `LANG GO` |
| 112 | GRAPHIQL | `QL GRAPHI` |
| 113 | GRAPHQL | `QL GRAPH` |
| 114 | GRAPHQL CORE | `CORE GRAPHQL` |
| 115 | GRAPHQL GO | `GO GRAPHQL` |
| 116 | GRAPHQL HELIX | `HELIX GRAPHQL` |
| 117 | GRAPHQL INSPECTOR | `INSPECTOR GRAPHQL` |
| 118 | GRAPHQL MESH | `MESH GRAPHQL` |
| 119 | GRAPHQL PLAYGROUND | `PLAYGROUND GRAPHQL` |
| 120 | GRAPHQL RUBY | `RUBY GRAPHQL` |
| 121 | GRAPHQL SHIELD | `SHIELD GRAPHQL` |
| 122 | GRAPHQL YOGA | `YOGA GRAPHQL` |
| 123 | GRAPHQL.NET | `.NET GRAPHQL` |
| 124 | GRAPHQLITE | `ITE GRAPHQL` |
| 125 | GRAYLOG | `LOG GRAY` |
| 126 | GRC | `GRC SAP` (5 → 0) |
| 127 | GROOVY | `GROOVY APACHE`, `GROVY APACHE` |
| 128 | GRUNT | `JS GRUNT` |
| 129 | GULP | `JS GULP` |
| 130 | HADOOP | `HADOOP APACHE` (3 → 0), `HADOP APACHE` |
| 131 | HAMCREST | `CREST HAM` |
| 132 | HANAMI GRAPHQL | `GRAPHQL HANAMI` |
| 133 | HANGFIRE | `FIRE HANG` |
| 134 | HANNA | `HANA S4` (5 → 0), `HANNA S4` |
| 135 | HANNA SQL | `SQL HANA`, `SQL HANNA`, `VIEWS CDS` (1 → 0) |
| 136 | HAPROXY | `PROXY HA` |
| 137 | HASURA CLOUD | `CLOUD HASURA` |
| 138 | HATEOAS | `OAS HATE` |
| 139 | HBASE | `BASE H` |
| 140 | HCM | `HCM SAP` |
| 141 | HEDERA HASHGRAPH | `HASHGRAPH HEDERA` |
| 142 | HEDION GRAPHQL | `GRAPPHQL HEDION` |
| 143 | HEROKU | `CLOUD HEROKU` |
| 144 | HIVE | `HIVE APACHE` |
| 145 | HOT CHOCOLATE | `CHOCOLATE HOT` |
| 146 | HOVERFLY | `FLY HOVER` |
| 147 | HTTP SERVER | `SERVER HTTP`, `SERVER-HTTP`, `SERVER.HTTP` |
| 148 | HTTPIE | `IE HTTP` |
| 149 | HYBRIS | `HYBRIS SAP` (4 → 0) |
| 150 | HYPERLEDGER | `LEDGER HYPER` |
| 151 | HYPERV | `V HYPER` |
| 152 | IBATIS | `BATIS I` |
| 153 | ICEFACES | `FACE ICE`, `FACES ICE` |
| 154 | ICOMON | `MON ICO` |
| 155 | IMPALA | `IMPALA APACHE` |
| 156 | INSOMNIA GRAPHQL | `GRAPHQL INSOMNIA` |
| 157 | INTERBASE | `BASE INTER` (10 → 10) |
| 158 | IREPORT | `REPORT I` (54 → 52), `REPPORT I` |
| 159 | ITEXT | `TEXT I` |
| 160 | IVY | `IVY APACHE` |
| 161 | J# | `SHARP J` |
| 162 | JACKRABBIT | `RABBITT JACK`, `RABIT JACK` |
| 163 | JASMINE | `JS JASMINE` |
| 164 | JASPER | `REPORT JASPER` (1 → 0), `REPORTS JASPER`, `REPPORT JASPER`, `REPPORTS JASPER` |
| 165 | JAVAEE | `EE JAVA` (22 → 0) |
| 166 | JAVAFX | `FX JAVA` (2 → 2) |
| 167 | JAVASCRIPT | `SCRIPT ECMA` (1 → 0), `SCRIPT JAVA` (11 → 0), `SCRIPTS ECMA`, `SCRIPTS JAVA` (2 → 1) |
| 168 | JAXRS | `RS JAX` |
| 169 | JAXWS | `WS JAX` |
| 170 | JBEHAVE | `BEHAVE J` |
| 171 | JBOSS | `FLY WILD` |
| 172 | JDE | `EDWARDS JD` |
| 173 | JEST | `JS JEST` |
| 174 | JQUERY | `JS JQUERY`, `QUERY J` (2 → 1) |
| 175 | JRUBY | `RUBY J` |
| 176 | JUNIT | `UNIT J` |
| 177 | KAFKA | `KAFKA APACHE` (11 → 0) |
| 178 | KARATE DSL | `DSL KARATE` |
| 179 | KARMA | `JS KARMA` |
| 180 | KEYCLOAK | `CLOAK KEY` |
| 181 | KNOCKOUT | `JS KNOCKOUT` |
| 182 | LIFERAY | `RAY LIFE` |
| 183 | LIGHT HOUSE | `HOUSE LIGHT` |
| 184 | LIQUIBASE | `BASE LIQUI` |
| 185 | LOADBALANCE | `BALANCE LOAD` |
| 186 | LOADRUNNER | `RUNNER LOAD` |
| 187 | LOCALSTACK | `STACK LOCAL` |
| 188 | LOGSTASH | `STASH LOG` |
| 189 | LOWCODE | `CODE LOW` |
| 190 | LUCENE | `LUCENE APACHE` |
| 191 | MACHINELEARNING | `ARTIFICIAL INTELIGENCIA`, `INTELIGENCE ARTIFICIAL`, `LEARNING DEEP` |
| 192 | MAINFRAME | `FRAME MAIN` |
| 193 | MARIADB | `DB MARIA` |
| 194 | MATERIAL UI | `UI MATERIAL` |
| 195 | MATLAB | `LAB MAT` |
| 196 | MATPLOTLIB | `LIB MATPLOT` |
| 197 | MEMCACHE | `CACHE MEM` |
| 198 | MEMCACHED | `CACHED MEM` |
| 199 | METASPLOIT | `SPLOIT META` |
| 200 | MICROMETER | `METER MICRO` |
| 201 | MICRONAUT | `NAUT MICRO` |
| 202 | MICROPROFILE GRAPHQL | `GRAPPHQL MICROPROFILE` |
| 203 | MICROVIX | `VIX MICRO` |
| 204 | MINITEST | `TEST MINI` |
| 205 | MOCHA | `JS MOCHA` |
| 206 | MOCKSERVER | `SERVER MOCK` |
| 207 | MONGODB | `DB MONDO`, `DB MONGO` |
| 208 | MOUNTEBANK | `BANK MOUNTE` |
| 209 | MQSERIES | `SERIES MQ` |
| 210 | MSTEST | `TEST MS` |
| 211 | MULE | `ESB MULE`, `SOFT MULE` |
| 212 | MVC | `VIEW-MODEL MODEL-VIEW` |
| 213 | MVN | `MAVEN APACHE`, `MVN APACHE` |
| 214 | MYBATIS | `BATIS MY`, `BATTIS MY` |
| 215 | MYFACES | `FACES MY` |
| 216 | MYSQL | `SQL MY` |
| 217 | NEAR PROTOCOL | `PROTOCOL NEAR` |
| 218 | NEO4J GRAPHQL | `GRAPHQL NEO4J` |
| 219 | NESTJS | `JS NEST` (3 → 3) |
| 220 | NETBEANS | `BEANS NET` |
| 221 | NETFLIX DGS | `DGS NETFLIX` |
| 222 | NETSUITE | `SUITE NET` |
| 223 | NETWEAVER | `WEAVER NET` |
| 224 | NEXTJS | `JS NEXT` |
| 225 | NIFI | `NIFI APACHE` (1 → 0) |
| 226 | NLP | `FLOW TENSOR` |
| 227 | NODE | `JS NODE` (15 → 0) |
| 228 | NOSQL | `SQL NO` |
| 229 | OBJECTIVEC | `C OBJECTIVE` |
| 230 | OCTOBERCMS | `CMS OCTOBER` |
| 231 | ODATA | `DATA O` |
| 232 | OPENAPI | `API OPEN` |
| 233 | OPENCV | `CV OPEN` |
| 234 | OPENLDAP | `LDAP OPEN` |
| 235 | OPENSHIFT | `SHIFT OPEN` |
| 236 | OPENSTACK | `STACK OPEN` (2 → 2) |
| 237 | ORACLE | `DATABASE ORACLE` (25 → 0) |
| 238 | ORM | `MAPPING OBJECT-RELATIONAL` |
| 239 | OWASP | `ZAP OWASP` |
| 240 | OXM | `MAPPING OBJECT-XML` |
| 241 | PATTERNS | `PATTERNS DESIGN` (2 → 0) |
| 242 | PENTEST | `TEST PEN` |
| 243 | PERSISTED QUERIES | `QUERIES PERSISTED` |
| 244 | PFSENSE | `SENSE PF` |
| 245 | PHONEGAP | `GAP PHONE` |
| 246 | PHPSPEC | `SPEC PHP` |
| 247 | PHPUNIT | `UNIT PHP` |
| 248 | PIPENV | `ENV PIP` |
| 249 | PLAYWRIGHT | `WRIGHT PLAY` |
| 250 | PLSQL | `SQL PL` |
| 251 | PMBOOK | `BOK PM`, `BOOK PM` |
| 252 | POO | `PROGRAM OBJECT-ORIENTED`, `PROGRAMMING OBJECT-ORIENTED` |
| 253 | POSTGRAPHILE | `GRAPHQL SUPABASE` |
| 254 | POSTGRESQL | `QL POSTGRE`, `QL POSTGRESS` |
| 255 | POSTMAN | `MAN POST` (1 → 0) |
| 256 | POSTMAN GRAPHQL | `GRAPHQL POSTMAN` |
| 257 | POWERAPPS | `APP POWER`, `APPS POWER` |
| 258 | POWERBI | `BI POWER` (24 → 13) |
| 259 | POWERBUILDER | `BUILDER POWER` |
| 260 | POWERCENTER | `CENTER POWER` |
| 261 | POWERDESIGNER | `DESIGNER POWER` |
| 262 | PRIMAVERA | `PRIMAVERA ORACLE` (1 → 0) |
| 263 | PRIMEFACES | `FACE PRIME`, `FACES PRIME` |
| 264 | PRISMAJS | `JS PRISMA` |
| 265 | PROFIT | `PROFIT SAP` |
| 266 | PROTRACTOR | `JS PROTACTOR`, `JS PROTRACTOR` |
| 267 | PUBSUB | `SUB PUB` |
| 268 | PYENV | `ENV PY` |
| 269 | PYSPARK | `SPARK PY` (1 → 0) |
| 270 | PYTEST | `TEST PY` (13 → 13) |
| 271 | QA | `ASSURANCE QUALITY` |
| 272 | QLIKVIEW | `SENSE QLIK`, `VIEW QLIK` |
| 273 | QUARKUS GRAPHQL | `GRAPHQL QUARKUS` |
| 274 | RABBITMQ | `MQ RABBIT` |
| 275 | RAILS GRAPHQL | `GRAPHQL RAILS` |
| 276 | REACTJS | `JS REACT` (31 → 0) |
| 277 | REACTNATIVE | `NATIVE REACT` (30 → 0) |
| 278 | READYAPI | `API READY` |
| 279 | REDMINE | `MINE READ` |
| 280 | REDSHIFT | `SHIFT RED` (1 → 1) |
| 281 | REST | `FUL REST`, `FULL REST` (1 → 0) |
| 282 | RESTASSURED | `ASSURED REST` |
| 283 | RETAIL | `RETAIL SAP` (4 → 0) |
| 284 | RICHFACES | `FACES RICH` |
| 285 | ROBOT | `FRAMEWORK ROBBOT`, `FRAMEWORK ROBOT` (10 → 0) |
| 286 | RUBYGEMS | `GEMS RUBY` |
| 287 | RUNDECK | `DECK RUN` |
| 288 | SALESFORCE | `FORCE SALES` (18 → 0) |
| 289 | SAP AA | `AA SAP` |
| 290 | SAP APO | `APO SAP` |
| 291 | SAP BAP | `BAP SAP` (29 → 26) |
| 292 | SAP BO | `BO SAP`, `BUSINESSOBJECTS SAP` |
| 293 | SAP BW | `BW SAP` |
| 294 | SAP BW/4HANNA | `BW/4HANNA SAP` |
| 295 | SAP CAP | `CAP SAP` |
| 296 | SAP CO | `CO SAP`, `CONTROLLING SAP` |
| 297 | SAP CPI | `CPI SAP` |
| 298 | SAP DYNPRO | `DYNPRO SAP` |
| 299 | SAP ECC | `ECC SAP` (4 → 0) |
| 300 | SAP FI | `FI SAP` |
| 301 | SAP FI-AP | `FI-AP SAP` |
| 302 | SAP FI-AR | `FI-AR SAP` |
| 303 | SAP FI-GL | `FI-GL SAP` |
| 304 | SAP FIORI | `FIORI SAP` (1 → 0) |
| 305 | SAP HR | `HR SAP` |
| 306 | SAP IBP | `IBP SAP` |
| 307 | SAP IDOC | `IDOC SAP` (1 → 0) |
| 308 | SAP IS-BANKING | `IS-BANKING SAP` |
| 309 | SAP IS-HEALTH | `IS-HEALTH SAP` |
| 310 | SAP IS-RETAIL | `IS-RETAIL SAP` |
| 311 | SAP IS-U | `IS-U SAP` |
| 312 | SAP KYMA | `KYMA SAP` |
| 313 | SAP LUMIRA | `LUMIRA SAP` |
| 314 | SAP ME | `ME SAP` |
| 315 | SAP MII | `MII SAP` |
| 316 | SAP MM | `MM SAP` |
| 317 | SAP PAYROLL | `PAYROLL SAP` |
| 318 | SAP PI | `PI SAP` |
| 319 | SAP PM | `MAINTENANCE PLANT`, `PM SAP` |
| 320 | SAP PO | `PO SAP` |
| 321 | SAP PP | `PP SAP` |
| 322 | SAP QM | `QM SAP` |
| 323 | SAP R/3 | `R/3 SAP` |
| 324 | SAP RFC | `RFC SAP` |
| 325 | SAP S/4HANNA | `S/4HANNA SAP` |
| 326 | SAP SAC | `SAC SAP` |
| 327 | SAP SD | `SD SAP` |
| 328 | SAP SRM | `SRM SAP` |
| 329 | SAP SSO | `SSO SAP` |
| 330 | SAP TM | `TM SAP` |
| 331 | SAP UI5 | `UI5 SAP` |
| 332 | SAP WM | `WM SAP` |
| 333 | SAPAUTENTICATION | `AUTENTICATION SAP` |
| 334 | SAPUX | `UX SAP` |
| 335 | SEABORN | `BORN SEA` |
| 336 | SECURITY123 | `SEGURANCA CIBER` |
| 337 | SEMANTIC UI | `UI SEMANTIC` |
| 338 | SERVERLESS | `LESS SERVER` |
| 339 | SERVICENOW | `NOW SERVICE` (2 → 0) |
| 340 | SETUPTOOLS | `TOOLS SETUP` |
| 341 | SHAREPOINT | `POINT SHARE` (1 → 0) |
| 342 | SILVER STRIPER | `STRIPER SILVER` |
| 343 | SILVERLIGHT | `LIGHT SILVER` |
| 344 | SITECORE | `CORE SITE` |
| 345 | SMARTCONTRACT | `CONTRACT SMART`, `CONTRACTS SMART` |
| 346 | SNOWFLAKE | `FLAKE SNOW` |
| 347 | SOLMAN | `SOLMAN SAP` |
| 348 | SOLR | `SOLR APACHE` |
| 349 | SONARQUBE | `QUBE SONAR`, `QUBE SONNAR` |
| 350 | SONICWALL | `WALL SONIC` (5 → 0) |
| 351 | SPARK | `SPARK APACHE` (5 → 0) |
| 352 | SPECFLOW | `FLOW SPEC` |
| 353 | SPLUNK | `SPLUNK APACHE` |
| 354 | SPRING AMPQ | `AMPQ SPRING` |
| 355 | SPRING AOP | `AOP SPRING` |
| 356 | SPRING ASYNC | `ASYNC SPRING` |
| 357 | SPRING BATCH | `SCHEDULING SPRING` |
| 358 | SPRING BEANS | `BEANS SPRING` |
| 359 | SPRING CLOUD | `CLOUD SPRING` |
| 360 | SPRING CONTEXT | `CONTEXT SPRING` |
| 361 | SPRING DATA | `DATA SPRING`, `IBATIS SPRING`, `MYBATIS SPRING` |
| 362 | SPRING GRAPHQL | `GRAPHQL SPRING` |
| 363 | SPRING HATEOAS | `HATEOAS SPRING` |
| 364 | SPRING INITIALIZR | `INITIALIZR SPRING` |
| 365 | SPRING INTEGRATION | `INTEGRATION SPRING` |
| 366 | SPRING JMS | `JMS SPRING` |
| 367 | SPRING JTA | `JTA SPRING`, `TRANSACTION SPRING` |
| 368 | SPRING KAFKA | `KAFKA SPRING` |
| 369 | SPRING MICROMETER | `MICROMETER SPRING` |
| 370 | SPRING MOCKMVC | `MOCKMVC SPRING` |
| 371 | SPRING OPENAPI | `OPENAPI SPRING` |
| 372 | SPRING ORM | `ORM SPRING` |
| 373 | SPRING OXM | `OXM SPRING` |
| 374 | SPRING PROFILES | `PROFILE SPRING`, `PROFILES SPRING` |
| 375 | SPRING REMOTING | `REMOTING SPRING` |
| 376 | SPRING RETRY | `RETRY SPRING` |
| 377 | SPRING RMI | `RMI SPRING` |
| 378 | SPRING ROO | `ROO SPRING` |
| 379 | SPRING SECURITY | `SECURITY SPRING` (1 → 1) |
| 380 | SPRING SESSION | `SESSION SPRING` |
| 381 | SPRING STOMP | `STOMP SPRING` |
| 382 | SPRING TEST | `TEST SPRING`, `TESTCONTEXT SPRING` |
| 383 | SPRING TESTCONTAINERS | `TESTCONTAINERS SPRING` |
| 384 | SPRING WEB | `WEB SPRING` (9 → 8) |
| 385 | SPRING WEB MVC | `WEBMVC SPRING` |
| 386 | SPRING WEBCLIENT | `WEBCLIENT SPRING` |
| 387 | SPRING WEBFLUX | `REACTOR SPRING`, `WEBFLUX SPRING` |
| 388 | SPRING WEBSERVICES | `WEBSERVICE SPRING`, `WEBSERVICES SPRING` |
| 389 | SPRING WEBSOCKET | `WEBSOCKET SPRING` |
| 390 | SPRING XD | `XD SPRING` |
| 391 | SPRINGBOOT | `BOOT SPRING` (4 → 1) |
| 392 | SPRINGBOOT ACTUATOR | `ACTUATOR SPRINGBOOT` |
| 393 | SPRINGBOOT ADMIN | `ADMIN SPRINGBOOT` |
| 394 | SPRINGBOOT CLI | `CLI SPRINGBOOT` |
| 395 | SPRINGBOOT DEVTOOLS | `DEVTOOLS SPRINGBOOT`, `TOOLS DEV` (4 → 4) |
| 396 | SPRINGBOOT TEST | `TEST SPRINGBOOT` |
| 397 | SPRINGDATA | `DATA SPRING`, `IBATIS SPRING`, `MYBATIS SPRING` |
| 398 | SPRINGMVC | `MVC SPRING` (1 → 1) |
| 399 | SPRINGREST | `REST SPRING` (2 → 2) |
| 400 | SQLALCHEMY | `ALCHEMY SQL` |
| 401 | SQLSERVER | `SERVER SQL`, `SQL MS`, `SQL T` |
| 402 | STARTEAM | `EAM START` |
| 403 | STRAWBERRY GRAPHQL | `GRAPHQL STRAWBERRY` |
| 404 | STRAWBERRY SHAKE | `SHAKE STRAWBERRY` |
| 405 | SUCCESSFACTORS | `FACTORS SUCCESS`, `SUCCESSFACTORS SAP` |
| 406 | SVELTE | `JS SVELTE` |
| 407 | SVN | `SUBVERSION APACHE`, `SVN APACHE` |
| 408 | SWIFT GRAPHQL | `GRAPHQL SWIFT` |
| 409 | SYSLOG | `LOG SYS` |
| 410 | TAILWIND | `CSS TAILWIND`, `WIND TAIL` |
| 411 | TERADATA | `DATA TERA` |
| 412 | TERRAFORM | `FORM TERRA` |
| 413 | TESTCAFE | `CAFE TEST` |
| 414 | TESTCOMPLETE | `COMPLETE TEST` (1 → 1) |
| 415 | TESTCONTAINERS | `CONTAINER TEST`, `CONTAINERS TEST` |
| 416 | TESTLINK | `LINK TEST` |
| 417 | TESTNG | `NG TEST` |
| 418 | TESTRAIL | `RAIL TEST` |
| 419 | THYMELEAF | `LEAF THYME` |
| 420 | TOMCAT | `TOMCAT APACHE` (7 → 0) |
| 421 | TRAVIS | `CI TRAVIS` |
| 422 | TYPESCRIPT | `SCRIPT TYPE` (3 → 0) |
| 423 | UIPATH | `PATH UI` (1 → 0) |
| 424 | UNITTEST | `TEST UNIT` (1 → 1) |
| 425 | VB | `BASIC VISUAL` (12 → 11) |
| 426 | VB.NET | `NET VB` |
| 427 | VBSCRIPT | `SCRIPT VB` (4 → 0) |
| 428 | VERSIONING | `CONTROL VERSION` |
| 429 | VIRTUALBOX | `BOX VIRTUAL` |
| 430 | VIRTUALENV | `ENV VIRTUAL` |
| 431 | VISUALAGE | `AGE VISUAL` |
| 432 | VISUALSTUDIO | `STUDIO VISUAL` (1 → 1) |
| 433 | VITEST | `TEST VI` (7 → 7) |
| 434 | VMWARE | `WARE VM` (3 → 0) |
| 435 | VSCODE | `CODE VS` (5 → 5) |
| 436 | VUE | `JS NUXT`, `JS VUE` |
| 437 | WEBCLIENT | `CLIENT WEB` (1 → 1) |
| 438 | WEBDRIVER | `DRIVER WEB` |
| 439 | WEBDRIVERIO | `IO WEBDRIVER` |
| 440 | WEBLOGIC | `LOGIC WEB` |
| 441 | WEBSOCKET | `SOCKET WEB`, `SOCKETS WEB` |
| 442 | WEBSPHERE | `PHERE WEBS` |
| 443 | WEBVIEW | `VIEW WEB` |
| 444 | WIREMOCK | `MOCK WIRE` |
| 445 | WMWARE | `WARE WM` |
| 446 | WORDPRESS | `COMMERCE WOO` (1 → 0), `PRESS WORD` |
| 447 | WS | `POINT END`, `POINTS END`, `SERVICE WEB`, `SERVICES WEB` |
| 448 | WUNDER GRAPH | `GRAPH WUNDER` |
| 449 | XCUITEST | `TEST XCUI` |
| 450 | ZOOKEEPER | `KEEPER ZOO` |
| 451 | ZUUL | `ZUL SPRING`, `ZUUL SPRING` |

## 8. Quebras do nome removidas

Justificativa de todas: Quebra sem sentido do nome em pedaços (ex.: "APIG EE" para APIGEE): não ocorre nas vagas, e pedaços como "DATA FLOW" e "DATA STORE" casam expressões genéricas. As quebras que ficaram e as que tiram a skill de alguma vaga estão nas tabelas 7.1 e 7.2. Entre parênteses: vagas em que a palavra casava → vagas que ficam sem a skill.

| # | Skill | Removidos |
|---|---|---|
| 1 | APIGEE | `APIG EE`, `APIG-EE`, `APIG.EE` |
| 2 | BUILDR | `BUILD R` (47 → 47), `BUILD-R` (2 → 2), `BUILD.R` |
| 3 | CHERWELL | `CHER WELL`, `CHER-WELL`, `CHER.WELL` |
| 4 | CODECEPTION | `CODE CEPTION`, `CODE-CEPTION`, `CODE.CEPTION` |
| 5 | DATAFLOW | `DATA FLOW` (100 → 99), `DATA-FLOW`, `DATA.FLOW` |
| 6 | DATASTORE | `DATA STORE` (34 → 34), `DATA-STORE`, `DATA.STORE` |
| 7 | DJANGO | `D JANGO`, `D-JANGO`, `D.JANGO` |
| 8 | DYNATRACE | `DYNA TRACE`, `DYNA-TRACE`, `DYNA.TRACE` |
| 9 | EASYB | `EASY B`, `EASY-B`, `EASY.B` |
| 10 | EASYTRIEVE | `EASY TRIEVE`, `EASY-TRIEVE`, `EASY.TRIEVE` |
| 11 | EPISERVER | `EPI SERVER`, `EPI-SERVER`, `EPI.SERVER` |
| 12 | EXADATA | `EXA DATA`, `EXA-DATA`, `EXA.DATA` |
| 13 | GRAPHIQL | `GRAPHI QL`, `GRAPHI-QL`, `GRAPHI.QL` |
| 14 | GRAPHQLITE | `GRAPHQL ITE`, `GRAPHQL-ITE`, `GRAPHQL.ITE` |
| 15 | HAMCREST | `HAM CREST`, `HAM-CREST`, `HAM.CREST` |
| 16 | HANGFIRE | `HANG FIRE`, `HANG-FIRE`, `HANG.FIRE` |
| 17 | HATEOAS | `HATE OAS`, `HATE-OAS`, `HATE.OAS` |
| 18 | HBASE | `H BASE`, `H-BASE`, `H.BASE` |
| 19 | HOVERFLY | `HOVER FLY`, `HOVER-FLY`, `HOVER.FLY` |
| 20 | HTTPIE | `HTTP IE`, `HTTP-IE`, `HTTP.IE` |
| 21 | IBATIS | `I BATIS`, `I-BATIS`, `I.BATIS` |
| 22 | ICOMON | `ICO MON`, `ICO-MON`, `ICO.MON` |
| 23 | IREPORT | `I REPORT`, `I REPPORT`, `I-REPORT`, `I-REPPORT`, `I.REPORT`, `I.REPPORT` |
| 24 | ITEXT | `I TEXT`, `I-TEXT`, `I.TEXT` |
| 25 | JBEHAVE | `J BEHAVE`, `J-BEHAVE`, `J.BEHAVE` |
| 26 | JQUERY | `J QUERY`, `J-QUERY`, `J.QUERY` |
| 27 | JRUBY | `J RUBY`, `J-RUBY`, `J.RUBY` |
| 28 | JUNIT | `J UNIT`, `J-UNIT`, `J.UNIT` |
| 29 | LIFERAY | `LIFE RAY`, `LIFE-RAY`, `LIFE.RAY` |
| 30 | LIQUIBASE | `LIQUI BASE`, `LIQUI-BASE`, `LIQUI.BASE` |
| 31 | MATLAB | `MAT-LAB`, `MAT.LAB` |
| 32 | MATPLOTLIB | `MAT PLOT LIB`, `MATPLOT LIB`, `MATPLOT-LIB`, `MATPLOT.LIB` |
| 33 | METASPLOIT | `META SPLOIT`, `META-SPLOIT`, `META.SPLOIT` |
| 34 | MICRONAUT | `MICRO NAUT`, `MICRO-NAUT`, `MICRO.NAUT` |
| 35 | MICROVIX | `MICRO VIX`, `MICRO-VIX`, `MICRO.VIX` |
| 36 | MOUNTEBANK | `MOUNTE BANK`, `MOUNTE-BANK`, `MOUNTE.BANK` |
| 37 | MYBATIS | `MY BATTIS`, `MY-BATIS`, `MY-BATTIS`, `MY.BATIS`, `MY.BATTIS` |
| 38 | MYFACES | `MY FACES`, `MY-FACES`, `MY.FACES` |
| 39 | ODATA | `O DATA`, `O-DATA`, `O.DATA` |
| 40 | RESTLET | `REST LET` |
| 41 | SEABORN | `SEA BORN`, `SEA-BORN`, `SEA.BORN` |
| 42 | STARTEAM | `START EAM`, `START-EAM`, `START.EAM` |
| 43 | TAILWIND | `TAIL WIND`, `TAIL-WIND`, `TAIL.WIND` |
| 44 | VITEST | `VI TEST`, `VI-TEST`, `VI.TEST` |
| 45 | WEBSPHERE | `WEBS PHERE`, `WEBS-PHERE`, `WEBS.PHERE` |

## 9. Descrições corrigidas

Só 4: as que descreviam outro produto (o nome da skill e os pais dizem qual é) e a do RDS, que mudou de significado (seção 3). As outras descrições erradas estão na seção 15.

| # | Skill | Antes | Depois | Justificativa |
|---|---|---|---|---|
| 1 | AURORA | SAP Aurora — produto SAP para consolidação e reporting financeiro | Amazon Aurora — banco relacional gerenciado da AWS, compatível com MySQL e PostgreSQL. | A descrição falava de um "SAP Aurora" que não existe; a skill é o Amazon Aurora. |
| 2 | HONE | Ferramenta de otimização de custos e performance para workloads em nuvem, com foco em visibilidade e eficiência de recursos | Hono — framework web JavaScript/TypeScript leve que roda em Bun, Deno, Node.js e na borda (Cloudflare Workers). | A descrição falava de uma ferramenta de custos em nuvem, mas o pai (BUN) mostra que a skill é o framework Hono. |
| 3 | WMS | Oracle Warehouse Management System — gestão de armazéns da Oracle | Warehouse Management System — sistema de gestão de armazéns (endereçamento, separação, expedição, inventário). | A descrição falava só do WMS da Oracle; as vagas usam a sigla para qualquer sistema de armazém. |
| 4 | RDS | Remote Desktop Services — serviço Windows para virtualização de sessões e desktops remotos | Amazon RDS — bancos relacionais gerenciados da AWS (MySQL, PostgreSQL, Oracle, SQL Server, Aurora). | Nas vagas, "RDS" é o banco gerenciado da AWS: em 1.622 das 1.659 vagas em que a sigla aparece há AWS, EC2, S3, Lambda ou outro serviço da AWS a até 200 caracteres. Com o pai antigo (VIRTUALIZATIONWINDOWS, Remote Desktop Services) essas vagas ganhavam Windows e virtualização por engano. O nome da skill não muda. |

## 10. Falsos positivos que o arquivo sozinho não resolve

O nome da skill é a chave e não muda, e o motor casa palavra curta (até 6 caracteres) como palavra inteira e palavra de 7 ou mais como trecho do texto. Estas palavras casam muitas vagas que não têm a ver com a skill. "Sem contexto" = nenhum termo da área da skill perto da palavra; os exemplos são de vagas reais. A correção fica fora do `synonyms.json`: incluir a palavra em `documentation/vis/telegram/words-that-are-not-skills.json` (palavras que a carga ignora) ou mudar o motor (seção 14).

| # | Palavra | Skill | Vagas em que casa | Sem contexto da skill | O que é, na maioria | Sugestão |
|---|---|---|---:|---:|---|---|
| 1 | `CVS` | CVS | 3.042 | 2.916 (96%) | Plural de CV ("envie seus CVs para...") | Incluir em words-that-are-not-skills.json |
| 2 | `VERTICA` | VERTICA | 360 | 346 (96%) | "Vertical", "verticais": com 7 letras o motor casa como trecho | Incluir em words-that-are-not-skills.json (perde as 14 vagas do banco Vertica) |
| 3 | `SBT` | SBT | 221 | 216 (98%) | A emissora de TV ("próximo ao SBT") | Incluir em words-that-are-not-skills.json |
| 4 | `REPORT` | REPORT | 1.668 | 1.302 (78%) | "Status report", "report para a gestão" | Incluir em words-that-are-not-skills.json: a categoria continua vindo pelas filhas (CRYSTAL REPORTS, JASPER, SSRS, ITEXT). O sinônimo REPORTS já saiu (seção 6) |
| 5 | `SAGA` | SAGA | 342 | 221 (65%) | A biblioteca redux-saga: a palavra curta casa dentro de "REDUX SAGA" e "REDUX-SAGA" | Mudança 3 do motor; a skill nova REDUX SAGA já é achada nessas vagas |
| 6 | `CHEF` | CHEF | 812 | ao menos 197 (24%) | Chef de cozinha | Aceitar, ou incluir em words-that-are-not-skills.json |
| 7 | `PROTOCOL` | PROTOCOL | 4.712 | ao menos 1.043 (22%) | "Protocolo de atendimento", "seguir protocolos": casa como trecho de PROTOCOLO | Mudança 4 do motor |
| 8 | `ELECTRON` | ELECTRON (nova) | 153 | 54 (35%) | "Electronic", "electronics" | Mudança 4 do motor |
| 9 | `LESS` | LESS (nova) | 1.234 | 183 (15%) | LeSS (Large-Scale Scrum, framework ágil), "server-less", "less-experienced" | Aceitar |
| 10 | `ADF` | ADF | 173 | 108 (62%) | Azure Data Factory; a skill é o Oracle ADF | Decidir: manter o Oracle ADF (sigla dividida) ou trocar o significado, como foi feito com o RDS (seção 3) |

Conferidas e sem problema relevante: DDD (3.779 vagas, 89% com arquitetura ou padrões por perto; o resto é quase tudo lista de tecnologias), CRM (86% com contexto de CRM), COMPLIANCE (a skill é conformidade em geral, e as vagas usam assim), UNIX (89%), CISCO (94%), TOTVS (94%), SAS (87%), PROCEDURES (87%), ORACLE RAC (100%), SNS, ELB, VPC e GLUE (99% ou mais com AWS por perto).

## 11. Skills que o motor não acha

Antes eram 11; agora são 2. Voltaram a ser achadas pelos sinônimos acrescentados (seção 4) ou pelas fusões (seção 1):

| # | Skill | Palavras que passam a achá-la |
|---|---|---|
| 1 | F5 | `BIGIP`, `F5 BIG-IP`, `F5 BIG IP` |
| 2 | FI-AA | Fundida em SAP AA (seção 1), que passa a ser achada por `SAP FI-AA`, `ASSET ACCOUNTING` |
| 3 | IBM MQ | `MQSERIES`, `MQ-SERIES`, `MQ SERIES`, `MQ.SERIES`, `WEBSPHERE MQ` |
| 4 | IP | `IPV4`, `IPV6`, `ENDEREÇAMENTO IP`, `ENDERECAMENTO IP`, `INTERNET PROTOCOL` |
| 5 | K6 | `GRAFANA K6` |
| 6 | NX | `NRWL` |
| 7 | RX | `PROGRAMAÇÃO REATIVA`, `PROGRAMACAO REATIVA`, `REACTIVE PROGRAMMING`, `REACTIVEX` |
| 8 | SAP PS | `SAP PROJECT SYSTEM`, `PROJECT SYSTEM` |
| 9 | SO | `SISTEMA OPERACIONAL`, `SISTEMAS OPERACIONAIS`, `OPERATING SYSTEM`, `OPERATING SYSTEMS` |

Continuam sem ser achadas:

| # | Skill | Por quê | O que fazer |
|---|---|---|---|
| 1 | B.ICLOUD | Categoria de combinação (B.I + CLOUD); o nome começa com "B." e nenhuma palavra do texto começa assim | Nada: é ancestral das filhas e aparece por elas |
| 2 | DW | 2 letras (o motor exige 3) e nenhum sinônimo; DATA WAREHOUSE e DATAWAREHOUSE constam em removidas.txt, por isso não entraram | Decidir: tirar DATA WAREHOUSE de removidas.txt e pô-lo como sinônimo de DW, ou apagar DW |

## 12. Palavras que o motor nunca acha

Eram 276; agora são 193. As que saíram eram inversões, quebras ou sinônimos errados (seções 6 a 8). As outras ficaram: não atrapalham e voltam a valer se o motor mudar (seção 14). Nenhuma palavra acrescentada nesta revisão está aqui.

| # | Motivo | Palavras | Exemplos |
|---|---|---:|---|
| 1 | Menos de 7 caracteres com separador | 144 | `APP CC`, `APP.CC`, `APP-CC`, `AZ 104`, `AZ.104`, `AZ-104` |
| 2 | Separador num dos 2 primeiros caracteres | 31 | `B.I`, `B I`, `B-I`, `B.ICLOUD`, `C ANSI`, `C-ANSI` |
| 3 | Menos de 3 caracteres | 18 | `C#`, `DW`, `F5`, `GO`, `IP`, `J#` |

<details><summary>Lista completa, por skill</summary>

| # | Skill | Palavras mortas |
|---|---|---|
| 1 | APPCC | `APP CC`, `APP-CC`, `APP.CC` |
| 2 | AZ104 | `AZ 104`, `AZ-104`, `AZ.104` |
| 3 | AZ900 | `AZ 900`, `AZ-900`, `AZ.900` |
| 4 | B.I | `B I`, `B-I`, `B.I` |
| 5 | B.ICLOUD | `B.ICLOUD` |
| 6 | C ANSI | `ANSI C`, `ANSI-C`, `ANSI.C`, `C ANSI`, `C-ANSI`, `C.ANSI` |
| 7 | CPP | `C PLUS PLUS` |
| 8 | CSHARP | `C SHARP`, `C#`, `C-SHARP`, `C.SHARP` |
| 9 | DEVOPS | `CI CD`, `CI-CD`, `CI.CD` |
| 10 | DW | `DW` |
| 11 | ECOMERCE | `E COMERCE`, `E COMMERCE`, `E-COMERCE`, `E-COMMERCE`, `E.COMERCE`, `E.COMMERCE` |
| 12 | EXTJS | `EXT JS`, `EXT-JS`, `EXT.JS` |
| 13 | F5 | `F5` |
| 14 | GCP | `G-CLOUD`, `G.CLOUD` |
| 15 | GOLANG | `GO` |
| 16 | HTML | `HMTL 4`, `HMTL 5`, `HMTL-4`, `HMTL-5`, `HMTL.4`, `HMTL.5` |
| 17 | IBM MQ | `IBM MQ` |
| 18 | IP | `IP` |
| 19 | J# | `J SHARP`, `J#`, `J-SHARP`, `J.SHARP` |
| 20 | JAVA | `JAVA 1`, `JAVA 2`, `JAVA 3`, `JAVA 4`, `JAVA 5`, `JAVA 6`, `JAVA 7`, `JAVA 8`, `JAVA 9`, `JAVA-1`, `JAVA-2`, `JAVA-3`, `JAVA-4`, `JAVA-5`, `JAVA-6`, `JAVA-7`, `JAVA-8`, `JAVA-9`, `JAVA.1`, `JAVA.2`, `JAVA.3`, `JAVA.4`, `JAVA.5`, `JAVA.6`, `JAVA.7`, `JAVA.8`, `JAVA.9` |
| 21 | JAVASCRIPT | `JS` |
| 22 | JAXB | `JAX B` |
| 23 | JAXRS | `JAX RS`, `JAX-RS`, `JAX.RS` |
| 24 | JAXWS | `JAX WS`, `JAX-WS`, `JAX.WS` |
| 25 | JSBDD | `BDD JS`, `JS BDD`, `JS-BDD`, `JS.BDD` |
| 26 | JSORM | `JS ORM`, `JS-ORM`, `JS.ORM`, `ORM JS` |
| 27 | K6 | `K6` |
| 28 | MACHINELEARNING | `A I`, `A-I`, `A.I`, `I A`, `I-A`, `I.A` |
| 29 | MYSQL | `MY SQL`, `MY-SQL`, `MY.SQL` |
| 30 | NOSQL | `NO SQL`, `NO-SQL`, `NO.SQL` |
| 31 | NX | `NX` |
| 32 | PLSQL | `PL SQL`, `PL-SQL`, `PL.SQL` |
| 33 | PMBOOK | `PM BOK`, `PM-BOK`, `PM.BOK` |
| 34 | PYENV | `PY ENV`, `PY-ENV`, `PY.ENV` |
| 35 | QA | `QA` |
| 36 | RX | `RX` |
| 37 | S3 | `S3` |
| 38 | SAP AA | `FI-AA`, `SAP AA`, `SAP-AA`, `SAP.AA` |
| 39 | SAP BO | `SAP BO`, `SAP-BO`, `SAP.BO` |
| 40 | SAP BW | `SAP BW`, `SAP-BW`, `SAP.BW` |
| 41 | SAP CO | `SAP CO`, `SAP-CO`, `SAP.CO` |
| 42 | SAP FI | `SAP FI`, `SAP-FI`, `SAP.FI` |
| 43 | SAP HR | `SAP HR`, `SAP-HR`, `SAP.HR` |
| 44 | SAP ME | `SAP ME`, `SAP-ME`, `SAP.ME` |
| 45 | SAP MM | `SAP MM`, `SAP-MM`, `SAP.MM` |
| 46 | SAP PI | `SAP PI`, `SAP-PI`, `SAP.PI` |
| 47 | SAP PM | `SAP PM`, `SAP-PM`, `SAP.PM` |
| 48 | SAP PO | `SAP PO`, `SAP-PO`, `SAP.PO` |
| 49 | SAP PP | `SAP PP`, `SAP-PP`, `SAP.PP` |
| 50 | SAP PS | `SAP PS` |
| 51 | SAP QM | `SAP QM`, `SAP-QM`, `SAP.QM` |
| 52 | SAP SD | `SAP SD`, `SAP-SD`, `SAP.SD` |
| 53 | SAP TM | `SAP TM`, `SAP-TM`, `SAP.TM` |
| 54 | SAP WM | `SAP WM`, `SAP-WM`, `SAP.WM` |
| 55 | SAPUX | `SAP UX`, `SAP-UX`, `SAP.UX` |
| 56 | SO | `SO` |
| 57 | SQLSERVER | `MS SQL`, `MS-SQL`, `MS.SQL`, `T SQL`, `T-SQL`, `T.SQL` |
| 58 | TYPESCRIPT | `TS` |
| 59 | UX | `UI`, `UX` |
| 60 | VB | `VB` |
| 61 | VB.NET | `VB NET`, `VB-NET`, `VB.NET` |
| 62 | VUE | `VUE JS`, `VUE-JS`, `VUE.JS` |
| 63 | WS | `WS` |

</details>

## 13. Itens de removidas.txt que continuam no arquivo

Não mexi neles: o pedido era não acrescentar nada da lista. Como continuam no arquivo, a carga continua achando essas palavras nas vagas. Decida se saem do arquivo ou da lista. Nenhum ganhou sinônimo nesta revisão.

| # | Item | Skill | Como está no arquivo |
|---|---|---|---|
| 1 | `AS400` | AS400 | nome da skill |
| 2 | `CRIPTOGRAFIA` | CRYPTOGRAPHY | sinônimo |
| 3 | `DEBIAN` | DEBIAN | nome da skill |
| 4 | `FULLSTACK` | FULLSTACK | nome da skill |
| 5 | `FULL.STACK` | FULLSTACK | sinônimo |
| 6 | `FULL STACK` | FULLSTACK | sinônimo |
| 7 | `FULL-STACK` | FULLSTACK | sinônimo |
| 8 | `ISO27001` | ISO27001 | nome da skill |
| 9 | `JUNIPER` | JUNIPER | nome da skill |
| 10 | `MS PROJECT` | MS PROJECT | nome da skill |
| 11 | `ROBOT` | ROBOT | nome da skill |
| 12 | `SWARM` | SWARM | nome da skill |
| 13 | `UNITY` | UNITY | nome da skill |
| 14 | `XUNIT` | XUNIT | nome da skill |
| 15 | `ZEPHYR` | ZEPHYR | nome da skill |

## 14. Sugestões para o motor (VisSkillsInText)

Medi as perdas nas mesmas vagas, com o arquivo novo. "Ficam sem a skill" = a skill não é achada por outra palavra dela nem por uma descendente. As duas primeiras são as maiores perdas do motor e nenhuma mudança no `synonyms.json` as resolve.

| # | Mudança | O que resolve |
|---|---|---|
| 1 | Aceitar palavras de 2 caracteres de uma lista de permitidas | O motor descarta palavras com menos de 3 caracteres, e 31.832 vagas perdem ao menos uma skill por isso (tabela 14.1). Só o `C#` deixa 13.483 vagas sem CSHARP |
| 2 | Procurar também as partes de uma palavra do texto que tem "/" | "/" não separa palavras: em "SCRUM/KANBAN", "REST/SOAP" ou "JAVA/ANGULAR" as palavras de menos de 7 caracteres não são achadas, e 23.697 vagas perdem ao menos uma skill por isso (tabela 14.2). A palavra inteira precisa continuar sendo procurada, para `PL/SQL`, `CI/CD`, `FI/CO` e `S/4HANA` |
| 3 | Escolher as palavras pelas iniciais da palavra sem separadores | As palavras com separador nas 2 primeiras letras, como `E-COMMERCE`, `.NET CORE` e `B.I` (seção 12) |
| 4 | Casar palavra curta com separador juntando palavras vizinhas do texto | `SAP MM`, `SAP FI`, `MAC OS`, `BIG-IP` |
| 5 | Descartar a palavra curta quando uma palavra mais longa achada a contém como palavra inteira | SAGA em "REDUX SAGA" (221 vagas); o mesmo já acontecia com APEX em "ORACLE APEX" antes de o sinônimo sair |
| 6 | Palavra única de 7+ letras casar só no início de uma palavra do texto | VERTICA em "vertical" (346 vagas), PROTOCOL em "protocolo" (1.043), ELECTRON em "electronic" (54). Cuidado: o trecho é o que faz "MICROSSERVIÇO" casar "MICROSSERVIÇOS"; casar no início da palavra mantém isso |
| 7 | Tirar os acentos do texto e das palavras | As formas sem acento do arquivo quase não casam: "TESTES UNITARIOS" em 193 vagas, "TESTES UNITÁRIOS" em 9.478. Cuidado: o stripAccents do projeto também apaga pontuação e cola palavras; teste antes de uma carga inteira |

### 14.1 Palavras de 2 caracteres que o motor descarta

| # | Palavra | Skill | Vagas em que aparece | Ficam sem a skill | Cuidado |
|---|---|---|---:|---:|---|
| 1 | `C#` | CSHARP | 17.146 | 13.483 | — |
| 2 | `QA` | QA | 6.373 | 5.095 | — |
| 3 | `GO` | GOLANG | 4.481 | 3.771 | "GO" também é a sigla de Goiás: só com contexto |
| 4 | `UX` | UX | 5.026 | 3.434 | — |
| 5 | `S3` | S3 | 3.174 | 2.914 | — |
| 6 | `UI` | UX | 3.561 | 2.686 | — |
| 7 | `VB` | VB | 1.534 | 1.390 | — |
| 8 | `SO` | SO | 1.048 | 584 | "so" do inglês ("so that"): só com contexto |
| 9 | `DW` | DW | 658 | 508 | DATA WAREHOUSE consta em removidas.txt (seção 11) |
| 10 | `IP` | IP | 968 | 498 | — |
| 11 | `JS` | JAVASCRIPT | 19.365 | 331 | — |
| 12 | `F5` | F5 | 419 | 324 | — |
| 13 | `WS` | WS | 623 | 140 | — |
| 14 | `RX` | RX | 96 | 74 | também é raio-X |
| 15 | `TS` | TYPESCRIPT | 121 | 66 | — |
| 16 | `K6` | K6 | 50 | 49 | — |
| 17 | `NX` | NX | 40 | 38 | — |

### 14.2 Skills que mais perdem com o "/"

| # | Skill | Ficam sem a skill | Palavras do texto mais comuns |
|---|---|---:|---|
| 1 | SCRUM | 1.891 | SCRUM/KANBAN, AGILE/SCRUM |
| 2 | TCP | 1.877 | TCP/IP, TCP/UDP (TCP/IP e TCPIP constam em removidas.txt) |
| 3 | SOAP | 1.803 | REST/SOAP, SOAP/REST |
| 4 | REST | 1.712 | REST/SOAP, SOAP/REST, REST/JSON |
| 5 | LINUX | 1.240 | UNIX/LINUX, LINUX/WINDOWS |
| 6 | KANBAM | 1.161 | SCRUM/KANBAN |
| 7 | GIT | 983 | GIT/GITHUB, GIT/BITBUCKET, GIT/GITLAB |
| 8 | JAVA | 961 | JAVA/ANGULAR, JAVA/KOTLIN |
| 9 | UNIX | 845 | UNIX/LINUX, LINUX/UNIX |
| 10 | REACTJS | 736 | NODE/REACT, REACT/NODE, JAVA/REACT |
| 11 | ORACLE | 658 | SQL/ORACLE, ORACLE/SQL |
| 12 | AWS | 653 | AWS/AZURE, AZURE/AWS, AWS/GCP |
| 13 | JPA | 572 | JPA/HIBERNATE, HIBERNATE/JPA |
| 14 | NODE | 570 | NODE/REACT, PHP/NODE |
| 15 | AZURE | 566 | AWS/AZURE, AZURE/AWS |
| 16 | KOTLIN | 536 | JAVA/KOTLIN, KOTLIN/JAVA |

Outras 439 skills perdem vagas do mesmo jeito (ex.: CPP em "C/C++").

## 15. Descrições com erro que deixei como estão

O pedido era sobre pais e sinônimos; só corrigi as descrições que falavam de outro produto e a do RDS, que mudou de significado (seção 9). Estas ficam para você decidir:

| # | Skill | Descrição atual | Problema |
|---|---|---|---|
| 1 | APPCC | SAP APPCC — ferramenta SAP de análise e monitoramento de sistemas | Em português, APPCC é a Análise de Perigos e Pontos Críticos de Controle (segurança de alimentos). As 6 vagas em que a sigla aparece são de alimentos, nenhuma com SAP. Candidata a sair do arquivo. |
| 2 | ADABAS | Banco de dados colunar da Software AG... | O Adabas é um banco de listas invertidas, não colunar. |
| 3 | WEBDRIVER | Protocolo W3C ... base do Selenium, Playwright e Cypress | Playwright e Cypress não usam WebDriver (o Cypress roda dentro do navegador; o Playwright usa protocolos próprios). |
| 4 | VSPHERE | VSPHERE (possível typo) — plataforma de virtualização da VMware... | O "(possível typo)" ficou no texto. |
| 5 | XRAIL | Ferramenta de gestão de testes e qualidade integrada ao Jira | Não existe produto XRAIL; a descrição é a do Xray (ou do TestRail). |
| 6 | SPECTRUM | Ferramenta de gestão de projetos e portfolio da Broadcom (antes CA Technologies) | O produto de portfólio da Broadcom é o Clarity PPM; o CA Spectrum é monitoramento de redes. E "SPECTRUM", com 8 letras, casa como trecho em qualquer texto. |
| 7 | JUNIPER | Biblioteca GraphQL Rust schema-first... | Nas vagas, Juniper é o fabricante de equipamentos de rede; o item também consta em removidas.txt. |

## 16. Decisões mantidas

| # | Decisão | Motivo |
|---|---|---|
| 1 | ANGULARJS continua sinônimo de ANGULAR | Separar o AngularJS (1.x) exigiria saber a versão, e quase nenhuma vaga diz: a versão aparece em cerca de 100 vagas, com "1" em 77 e "2" a "11" no resto |
| 2 | MEMCACHE não foi fundida em MEMCACHED | O roteiro de teste 02 usa a skill MEMCACHE (passo 8.1) |
| 3 | GRC continua sendo o SAP GRC, separado de COMPLIANCE | O nome e os sinônimos (SAP GRC, SAPGRC) são do módulo SAP |
| 4 | Skills que constam em removidas.txt não ganharam sinônimos | O pedido era não acrescentar nada da lista |
| 5 | As categorias com "123" (ex.: SECURITY123) ganharam sinônimos | O motor acha essas categorias no texto como as outras skills; o "123" só some do nome dos pais na resposta |

## 17. Impacto nos roteiros de teste

Simulei a leitura do `cv.txt` dos roteiros com o arquivo antigo e com o novo. O antigo reproduz a tabela do roteiro 02 (ORMJAVA 2, SQLDATABASE 11, NOSQLDATABASE 2, JAVA SERVER 13). Com o novo, só muda o `SQLDATABASE`, que passa a 10, sem LINQ: o pai do LINQ virou CSHARP (seção 3). É exatamente a correção que o cenário H4 do roteiro 02 pede para aprovar ("LINQ é do mundo .NET e não depende de banco SQL"). Na próxima execução, escolha outra habilidade de SQLDATABASE para o H4, como o próprio roteiro orienta ("se mudaram, escolha outras equivalentes"). O currículo também passa a ter TOTVS (skill nova). Nenhum outro nome citado nos roteiros mudou de skill ou de pais.

## 18. Como aplicar

| # | Passo | Detalhe |
|---|---|---|
| 1 | Não rode `executaCcpCreateEntities.bat vis` | Recria o `vis_recruiter` (44.686 registros) junto com o `vis_skill` |
| 2 | Regrave as skills com os registros que a semente monta (`VisEntitySkill.getFirstRecordsToInsert`), uma a uma, por `VisEntitySkill.ENTITY.save` | O arquivo só é lido pela semente, que roda no passo 1; pelo save, os grupos de iniciais no `jn_grouper` são atualizados e o `vis_recruiter` fica intacto |
| 3 | Apague do `vis_skill` as 13 skills fundidas | ECLIPSE MICROPROFILE GRAPHQL, ESB, FI-AA, HCM, MQSERIES, MVCNODE, ORACLE ANALYTICS, RGPD, SAP CLOUD PLATFORM, SPRING WEB MVC, SPRINGDATA, WEBCLIENT, WMWARE |
| 4 | Rode `executaRecargaTelegram.bat` | Exige todos os arquivos em `C:\z`, pede que você digite APAGAR, apaga as vagas, os gráficos, os grupos `vis_statis_skill*` e as cópias dos descartáveis e recarrega com `manter` |
| 5 | Reprocesse os currículos que devem ganhar as skills novas | Como no passo 4.7 do roteiro 02 |

## 19. Decisões e correções depois da revisão (2026-11-02)

| # | Item | O que foi feito |
|---|---|---|
| 1 | CVS, SBT, VERTICA e REPORT (seção 10) | Incluídos em `documentation/vis/telegram/words-that-are-not-skills.json`. A exclusão vale pela palavra, então REPORT continua vindo como pai de CRYSTAL REPORTS, JASPER, SSRS e ITEXT |
| 2 | DW (seção 11) | DATAWAREHOUSE saiu de `removidas.txt`; DW ganhou os sinônimos DATA WAREHOUSE e DATAWAREHOUSE |
| 3 | ADF (seção 10) | A palavra ADF virou sinônimo de AZURE DATA FACTORY. A Oracle ADF continua como a skill ORACLE ADF, com os mesmos pais, sinônimos e descrição; a skill ADF foi apagada do `vis_skill` |
| 4 | H4 do roteiro 02 (seção 17) | BIGQUERY no lugar do LINQ; SQLDATABASE passa a 10 habilidades e, depois da aprovação, a 9 |
| 5 | Palavras de 2 caracteres (seção 14.1) | **A seção 14.1 estava errada:** o motor já acha palavras de 2 caracteres (`SHORTEST_WORD = 2`). C#, QA, UX, S3 e DW são achadas, como prova `SkillsFromTextTest.twoCharacterWordsAreFound`. A simulação que gerou os números descartava essas palavras e o motor não descarta |
| 6 | "/" (seção 14.2) | Corrigido em `VisSkillsInText`: cada lado de uma palavra com "/" também é palavra do texto (SCRUM/KANBAN acha SCRUM e KANBAM; TCP/IP acha TCP e IP). A palavra inteira continua valendo (PL/SQL, CI/CD, FI/CO). O lado de uma palavra inteira que já é skill só vale se também aparecer sozinho: "PL/SQL" não dá SQL, "PL/SQL e SQL" dá. Testes `eachSideOfASlashedWordIsAWord` e `aSlashedSkillWordKeepsItsSidesOut` |
| 7 | Itens de `removidas.txt` no arquivo (seção 13) | Os 15 saíram de `removidas.txt` (12 linhas: FULLSTACK cobre as 4 grafias) e ficam como skills |
| 8 | Descrições com erro (seção 15) | APPCC sai do arquivo (as vagas são de segurança de alimentos). XRAIL sai (produto inexistente, nenhuma vaga). SPECTRUM sai: nas 98 menções das vagas aparecem 3 produtos diferentes (IBM Spectrum, Redshift Spectrum e CA Spectrum), nenhum de gestão ágil, que era o pai. JUNIPER passa a ser a Juniper Networks, com o pai IP, como o CISCO. ADABAS, WEBDRIVER e VSPHERE têm a descrição corrigida. O arquivo fica com 1.479 skills e 372 grupos de iniciais |
| 9 | XRAY (skill nova, pedido do usuário) | App de gestão de testes do Jira, com os pais BDDQA (como ZEPHYR e TESTRAIL) e JIRA; sinônimos XRAY TEST MANAGEMENT, XRAY FOR JIRA e XRAY TESTING. X-RAY não entrou como sinônimo (raio-X). Cuidado: cerca de 72 menções de "XRay" nas vagas são o AWS X-Ray e também casam. O arquivo fica com 1.480 skills |
