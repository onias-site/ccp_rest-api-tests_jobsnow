# ccp_text-extractor_apache-tika (arquivado)

Implementação de `CcpTextExtractor` (de `ccp_commons_jobsnow`) com o Apache Tika: extrai o texto de um
arquivo recebido em base64. Foi um módulo Maven e um repositório git próprios
(`onias-site/ccp_text-extractor_apache-tika`) até 2026-10-02, quando saiu do workspace a pedido do
usuário: nenhuma classe de negócio usava o extrator; só a API do vis o registrava na injeção de
dependência, e quem o pedia era o `SkillManagerOld` (código de apoio em `ccp_rest-api-tests_jobsnow`),
apagado no mesmo dia.

Conteúdo, guardado como fonte puro, do mesmo jeito que o front-end legado em `documentation/jn/frontend/legado`:

- `pom.xml` e `src/main/java` — o módulo como estava no disco no dia do arquivamento;
- `src/test/java/.../CcpApacheTikaTextExtractorTest.java` — o teste que vivia em `ccp_rest-api-tests_jobsnow`;
- `repositorio-git.bundle` — o repositório git inteiro (8 commits, até `634b4de`), já que o repositório
  remoto foi apagado. Para recuperar o histórico:

  ```bash
  git clone repositorio-git.bundle ccp_text-extractor_apache-tika
  ```

Para voltar a usar: restaurar o módulo na raiz do workspace, recolocá-lo no `<modules>` do `pom.xml`
agregador (camada 1), adicionar a dependência `com.ccp:ccp_text-extractor_apache-tika` em quem for usar e
registrar `new CcpApacheTikaTextExtractor()` no `CcpDependencyInjection.loadAllDependencies` da aplicação.
