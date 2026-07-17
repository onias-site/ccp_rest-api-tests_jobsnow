# SkillManager.java — Explicação Completa (versão atual)

**Arquivo:** `ccp_rest-api-tests_jobsnow/src/main/java/com/vis/skills/SkillManager.java`

Esta versão é mais enxuta que a anterior — os métodos de relatório de currículos foram removidos. O que sobrou é o núcleo de **curadoria da taxonomia de skills**.

---

## O que foi removido em relação à versão anterior

Os seguintes métodos foram extraídos ou deletados:
- `relatorioDeSkillsPesquisadas()`
- `getSkillsFromText(String)`
- `getAllWords(List<String>)` e `getAllWords()`
- `relatoriosDasSkillsNosCurriculos()`
- `getSubReportFromSkills()`
- `createReport()`

Junto com eles, foram removidos os imports de `CcpQueryExecutor`, `CcpQueryOptions`, `CcpTextExtractor`, `CcpDependencyInjection` e `VisEntityGroupPositionsBySkills`. A classe perdeu toda dependência do Elasticsearch e do pipeline de currículos.

---

## Enum `JsonFields` (linhas 25–53)

Mesmas constantes da versão anterior, mas alguns campos como `tipoVaga`, `curriculo`, `conteudo`, `id` agora estão **órfãos** — existem no enum mas não são mais usados em nenhum método da classe.

`static int counter` (linha 54) também está órfão — era usado nos `System.out.println` do consumer de currículos, que foi removido.

---

## Métodos

### `getOtherWords(String word)` (linha 56)

Para uma skill de duas palavras gera variantes de grafia: sem espaço (`java8`), com hífen (`java-8`), com ponto (`java.8`) e ordem invertida (`8 java`), exceto quando a segunda parte é um número longo. Skills de uma palavra retornam apenas a si mesmas.

### `getOtherWords(Set<String> otherWords)` (linha 87)

Aplica o método acima a cada palavra de um conjunto, acumulando todas as variantes. Há um ponto de debug hardcoded para `"JAVAGRAPHQL"` (linha 90) que nunca faz nada além de `System.out.println()` vazio.

### `saveSynonyms()` (linha 100)

Lê `report_skills.json` e `synonyms.txt`, identifica skills do relatório que ainda não têm entrada no arquivo de sinônimos, adiciona-as e grava tudo em `synonyms2.txt` ordenado alfabeticamente.

### `saveSkills()` (linha 133) — método principal

Lê `ajustes_synonyms.txt` filtrando linhas `adicionarParent=SKILL,PAI1,PAI2,...` e:

1. Monta a lista de todas as skills com `childrenCount` e `parent`
2. Detecta `mirror` via `getSynonym()` — skill com 1 pai que tem 1 único filho
3. Detecta `skillsWithCommonParents` via `getSkillsWithCommonParentsSize()`
4. Para cada skill coleta recursivamente todos os ancestrais via `getAllParents()`
5. Detecta `hasRepeatedParent` (ancestral que aparece mais de uma vez = ciclo/diamante)
6. Cruza com `synonyms3.txt` usando `getOtherWords(Set)` para expandir variantes
7. Valida que cada skill tem **exatamente um** grupo de sinônimos — lança `RuntimeException` se faltar ou houver duplicata
8. Aplica dois renames de campo: `parent` → `directParent` e `allParents` → `parent`
9. Filtra os campos finais com `getJsonPiece()` e remove `synonym`
10. Ordena e grava em `report_skills.json`

### `getParent(String skill, List<...> report)` (linha 247)

Busca os pais diretos de uma skill no relatório em memória. Usado internamente por `saveSkills()` ao montar o objeto `allParents`.

### `getSorter(String... fields)` (linha 251)

Comparador multi-campo genérico: detecta o tipo de cada campo automaticamente (número → compara como `int`; booleano → `true` antes de `false`; string → alfabético) e compara na ordem declarada, passando para o próximo campo em caso de empate.

### `getSkillsWithCommonParentsSize(...)` (linha 289)

Para uma skill dada, encontra todas as outras skills que compartilham **mais de 1 pai** com ela. Marca `hasSkillsWithCommonParentsSize = true` e lista as skills encontradas em `skillsWithCommonParents`.

### `getCommonParents(...)` (linha 301)

Retorna a interseção dos pais de duas skills usando `CcpCollectionDecorator.getIntersectList()`.

### `getAllParents(...)` (linha 309)

Recursão em profundidade que percorre toda a árvore de ancestrais de uma skill, adicionando cada pai à lista acumulada. Imprime progresso via `System.out.println` a cada nível.

### `getSynonym(...)` (linha 326)

Determina se uma skill é "mirror" (sinônimo estrutural): a skill precisa ter exatamente 1 pai **e** esse pai deve ter `childrenCount == 1`. Quando isso ocorre, pai e filho são essencialmente o mesmo conceito com nomes diferentes.

---

## Estado atual dos métodos

Todos os métodos são **package-private e estáticos**, e nenhum arquivo externo os chama. Os dois "pontos de entrada" reais são `saveSkills()` e `saveSynonyms()` — precisam ser invocados manualmente. Os demais (`getParent`, `getSorter`, `getSkillsWithCommonParentsSize`, `getCommonParents`, `getAllParents`, `getSynonym`, `getOtherWords`) são auxiliares chamados apenas internamente por `saveSkills()`.

---

## Fluxo de execução

```
ajustes_synonyms.txt  +  synonyms3.txt
           |
        saveSkills()
           |-- getSynonym()
           |-- getSkillsWithCommonParentsSize()
           |     |-- getCommonParents()
           |-- getAllParents()  [recursivo]
           |-- getParent()
           |-- getOtherWords(Set)
           |     |-- getOtherWords(String)
           |-- getSorter()
           v
      report_skills.json
           |
       saveSynonyms()  <-- synonyms.txt
           v
       synonyms2.txt
```
