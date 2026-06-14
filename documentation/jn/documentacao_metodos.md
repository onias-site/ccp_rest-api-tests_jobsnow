# Documentação de Métodos — jobsnow

Documentação abrangente de todos os métodos e classes Java (exceto classes de teste) do projeto **jobsnow**.
Gerada automaticamente por análise do código-fonte.

---
# Documentação do Módulo ccp_commons_jobsnow — Parte 1

> Gerado em 2026-06-09. Cobre os pacotes: `business`, `constantes`, `decorators`, `dependency.injection`, `flow`, `hash`, `json.transformers`, `process`, `service` e `validations`.

---

## Classe: CcpBusiness
**Pacote:** `com.ccp.business`
**Tipo:** interface
**Propósito:** Contrato central de toda lógica de negócio do sistema. Estende `Function<CcpJsonRepresentation, CcpJsonRepresentation>`, garantindo que toda operação de negócio receba e devolva o mapa JSON que flui pelo sistema. Adiciona suporte a validação de entrada e controle de execução assíncrona.

### Métodos:
- **canBeSavedAsAsyncTask()** → `boolean`: Indica se esta operação pode ser salva como tarefa assíncrona. O padrão retorna `true`; implementações específicas podem sobrescrever para desabilitar.
- **getJsonValidationClass()** → `Class<?>`: Retorna a classe usada para buscar as regras de validação do JSON de entrada. Por padrão retorna a própria classe da implementação, permitindo que as anotações de validação sejam encontradas por reflexão.
- **execute(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Ponto de entrada seguro para execução do negócio: primeiro valida o JSON de entrada usando `CcpJsonValidatorEngine`, depois chama `apply(json)`. Use este método ao invés de chamar `apply` diretamente quando a validação for necessária.

---

## Classe: CcpOtherConstants
**Pacote:** `com.ccp.constantes`
**Tipo:** interface
**Propósito:** Repositório de constantes globais compartilhadas em todo o sistema. Define instâncias reusáveis de negócios triviais, um JSON vazio canônico, delimitadores de texto e um campo de nome vazio.

### Campos (constantes de interface):
- **RETURNS_EMPTY_JSON** — `CcpBusiness` que ignora o JSON de entrada e sempre retorna `EMPTY_JSON`. Útil como valor padrão ou no-op que zera o resultado.
- **DO_NOTHING** — `CcpBusiness` que devolve o próprio JSON de entrada sem modificação. Padrão pass-through.
- **EMPTY_JSON** — instância canônica de `CcpJsonRepresentation` vazio. Evita criação repetida de mapas vazios.
- **DELIMITERS_ARRAY** — array de strings com os delimitadores textuais mais comuns (barra, ponto, vírgula, etc.), usado para tokenizar e sanitizar texto.
- **DELIMITERS** — expressão regular equivalente ao array acima, pronta para uso com `String.split()` ou `Pattern`.
- **EMPTY_STRING** — implementação de `CcpJsonFieldName` cujo valor é a string vazia. Representa ausência de nome de campo.

---

## Classe: CCpErrorJsonFieldIsNotValidJsonList
**Pacote:** `com.ccp.decorators`
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando se tenta acessar um campo de um `CcpJsonRepresentation` esperando uma lista JSON, mas o valor real é de outro tipo incompatível (por exemplo, um `Long` ou `String` comum).

### Métodos:
- **CCpErrorJsonFieldIsNotValidJsonList(CcpJsonRepresentation json, Class<?> clazz, String... path)** → construtor: Monta a mensagem de erro indicando qual caminho de campos foi acessado, qual tipo foi encontrado e qual era o JSON completo no momento do erro.

---

## Classe: CcpCollectionDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Decorator sobre `Collection<Object>` que adiciona operações de análise de tipo, comparação entre coleções e fatiamento. Permite verificar se todos os elementos são de um determinado tipo JSON, calcular interseções/diferenças e iterar de forma padronizada.

### Métodos:
- **CcpCollectionDecorator(Collection<?> content)** → construtor: Encapsula qualquer `Collection` já existente.
- **CcpCollectionDecorator(Object[] array)** → construtor: Converte um array em coleção encapsulada.
- **CcpCollectionDecorator(CcpJsonRepresentation json, String key)** → construtor: Extrai a lista do campo `key` dentro do JSON fornecido.
- **isLongNumberList()** → `boolean`: Retorna `true` se todos os elementos da coleção podem ser interpretados como números `long`.
- **isDoubleNumberList()** → `boolean`: Retorna `true` se todos os elementos podem ser interpretados como `double`.
- **isBooleanList()** → `boolean`: Retorna `true` se todos os elementos são `"true"` ou `"false"`.
- **isJsonList()** → `boolean`: Retorna `true` se todos os elementos são strings que representam JSON de objeto válido.
- **iterator()** → `Iterator<Object>`: Implementação de `Iterable`, permite uso em `for-each`.
- **isEmpty()** → `boolean`: Verifica se a coleção não possui elementos.
- **size()** → `CcpNumberDecorator`: Retorna o tamanho da coleção encapsulado em `CcpNumberDecorator` para facilitar comparações.
- **hasNonDuplicatedItems()** → `boolean`: Retorna `true` se todos os itens são únicos (sem duplicatas), comparando os tamanhos com e sem `HashSet`.
- **getExclusiveList(Collection<T> listToCompare)** → `List<T>`: Retorna os itens presentes nesta coleção que NÃO estão em `listToCompare` (diferença).
- **getIntersectList(Collection<T> listToCompare)** → `List<T>`: Retorna os itens presentes em ambas as coleções (interseção).
- **hasIntersect(Collection<T> listToCompare)** → `boolean`: Retorna `true` se a interseção com `listToCompare` não for vazia.
- **getSubCollection(int start, int end)** → `CcpCollectionDecorator`: Retorna uma sub-coleção delimitada pelos índices `start` e `end`, ajustando `end` caso ultrapasse o tamanho real.
- **getContent()** → `Collection<Object>`: Implementação de `CcpDecorator`; devolve a coleção interna.

---

## Classe: CcpDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** interface
**Propósito:** Contrato base do padrão Decorator do framework. Todo wrapper de tipo específico implementa esta interface, garantindo que o conteúdo encapsulado possa ser recuperado de forma tipada.

### Métodos:
- **getContent()** → `T`: Retorna o objeto interno encapsulado pelo decorator.

---

## Classe: CcpEmailDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Decorator especializado em endereços de e-mail. Oferece validação robusta (com regras de negócio específicas do domínio jobsnow, como domínios aceitos e sufixos inválidos), normalização de acentos, extração de e-mails a partir de texto livre e cálculo de hash do endereço.

### Métodos:
- **CcpEmailDecorator(String content)** → construtor: Encapsula a string como e-mail (construtor protegido; criação via `CcpStringDecorator.email()`).
- **toString()** → `String`: Retorna o endereço bruto.
- **stripAccents()** → `CcpEmailDecorator`: Remove acentos do endereço. Se já for um e-mail válido, trata as partes local e domínio separadamente para preservar o `@`; caso contrário, aplica normalização NFD genérica.
- **isValid()** → `boolean`: Verifica se a string é um endereço de e-mail válido segundo a regex padrão e regras de negócio do domínio (aceita `.digital`, `@wayon.global`, `@corp.inovation.com.br`; rejeita extensões malformadas como `.coom`, `.docx`, etc.).
- **findFirst(String delimitadores)** → `CcpEmailDecorator`: Percorre as "palavras" obtidas ao dividir o conteúdo pelos delimitadores fornecidos e retorna o primeiro trecho reconhecido como e-mail válido. Retorna `CcpEmailDecorator("")` se nenhum for encontrado.
- **extractFromText(String delimiter)** → `Set<String>`: Divide o texto pelo delimitador e coleta em um `TreeSet` todos os trechos que são e-mails válidos (em minúsculas).
- **getDomain()** → `String`: Retorna a parte do domínio (após `@`). Retorna string vazia se o endereço não tiver exatamente um `@`.
- **getContent()** → `String`: Implementação de `CcpDecorator`; devolve o endereço.
- **hash()** → `CcpHashDecorator`: Cria um `CcpHashDecorator` sobre o endereço para cálculo de hash.

---

## Classe: CcpErrorFolderParentIsMissing
**Pacote:** `com.ccp.decorators`
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando se tenta ler ou criar um arquivo cujo diretório pai não existe no sistema de arquivos.

### Métodos:
- **CcpErrorFolderParentIsMissing(CcpFileDecorator decorator)** → construtor: Monta a mensagem de erro incluindo o caminho absoluto do diretório pai ausente e o caminho completo do arquivo esperado.

---

## Classe: CcpErrorHashAlgorithmNotFound
**Pacote:** `com.ccp.decorators`
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando o algoritmo de hash solicitado não é reconhecido pela JVM (não encontrado via `MessageDigest.getInstance`).

### Métodos:
- **CcpErrorHashAlgorithmNotFound(String algorithm)** → construtor: Monta a mensagem indicando o nome do algoritmo não encontrado.

---

## Classe: CcpErrorInputStreamMissing
**Pacote:** `com.ccp.decorators`
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando um recurso (arquivo, variável de ambiente ou recurso do classpath) referenciado para abertura de `InputStream` não existe ou está vazio.

### Métodos:
- **CcpErrorInputStreamMissing(String filePath)** → construtor: Monta a mensagem indicando o caminho/nome do recurso ausente.

---

## Classe: CcpErrorJsonFieldNotFound
**Pacote:** `com.ccp.decorators`
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando se tenta obter o valor de um campo obrigatório de um `CcpJsonRepresentation` e esse campo está ausente.

### Métodos:
- **CcpErrorJsonFieldNotFound(String field, CcpJsonRepresentation json)** → construtor: Monta a mensagem informando qual campo estava ausente e o conteúdo completo do JSON no momento do erro.

---

## Classe: CcpErrorJsonInvalid
**Pacote:** `com.ccp.decorators`
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando uma string não pode ser desserializada como JSON válido.

### Métodos:
- **CcpErrorJsonInvalid(String json, Throwable e)** → construtor: Monta a mensagem indicando a string inválida e encadeia a exceção original como causa.

---

## Classe: CcpErrorJsonInvalidFieldFormat
**Pacote:** `com.ccp.decorators`
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando o valor de um campo existe no JSON, mas não pode ser convertido para o tipo esperado (por exemplo, tentar ler `"abc"` como `long`).

### Métodos:
- **CcpErrorJsonInvalidFieldFormat(Object value, String fieldName, String fieldType, CcpJsonRepresentation json)** → construtor: Monta a mensagem indicando o valor encontrado, o nome do campo, o tipo esperado e o JSON completo.

---

## Classe: CcpErrorJsonNull
**Pacote:** `com.ccp.decorators`
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando se tenta construir um `CcpJsonRepresentation` a partir de um `Map` nulo. Sinaliza que o JSON passado é `null` e portanto inválido como entrada.

### Métodos:
- Nenhum método adicional; herda comportamento padrão de `RuntimeException`.

---

## Classe: CcpErrorJsonPathIsMissing
**Pacote:** `com.ccp.decorators`
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando métodos de navegação por caminho em `CcpJsonRepresentation` recebem um array de campos vazio, o que indica erro de programação (caminho não informado).

### Métodos:
- **CcpErrorJsonPathIsMissing(CcpJsonRepresentation json)** → construtor: Monta a mensagem pedindo que o caminho seja preenchido, incluindo o JSON que recebeu a chamada.

---

## Classe: CcpFileDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Decorator sobre um caminho de arquivo do sistema de arquivos. Encapsula operações de leitura, escrita, acréscimo, compactação ZIP, remoção e conversão do conteúdo para outros tipos do framework (JSON, lista de JSONs). Garante criação automática do diretório pai quando necessário.

### Métodos:
- **CcpFileDecorator(String content)** → construtor (protegido): Encapsula o caminho e resolve automaticamente o decorator do diretório pai.
- **zip()** → `CcpFileDecorator`: Compacta o arquivo ou diretório em um arquivo `.zip` com o mesmo nome no diretório corrente.
- **getName()** → `String`: Retorna apenas o nome do arquivo (sem o caminho).
- **getPath()** → `String`: Retorna o caminho absoluto completo do arquivo.
- **getStringContent()** → `String`: Lê todo o conteúdo do arquivo como string UTF-8. Lança `CcpErrorFolderParentIsMissing` se o arquivo não existir.
- **write(String content)** → `CcpFileDecorator`: Sobrescreve o arquivo com o conteúdo fornecido (limpa antes de escrever).
- **append(String content)** → `CcpFileDecorator`: Acrescenta o conteúdo ao final do arquivo (cria o arquivo se não existir).
- **reset()** → `CcpFileDecorator`: Apaga o conteúdo do arquivo, deixando-o vazio (apaga e recria).
- **getLines()** → `List<String>`: Lê todas as linhas do arquivo e as retorna como lista de strings.
- **readLines(FileLineReader reader)** → `CcpFileDecorator`: Lê o arquivo linha a linha, chamando o callback `reader.onRead(linha, índice)` para cada linha.
- **exists()** → `boolean`: Verifica se o arquivo existe no sistema de arquivos.
- **isFile()** → `boolean`: Retorna `true` se o caminho existe e aponta para um arquivo (não um diretório).
- **asFolder()** → `CcpFolderDecorator`: Reinterpreta o caminho como um diretório.
- **asSingleJson()** → `CcpJsonRepresentation`: Lê o conteúdo do arquivo e o desserializa como um único JSON.
- **asJsonList()** → `List<CcpJsonRepresentation>`: Lê o conteúdo do arquivo e o desserializa como uma lista de objetos JSON.
- **remove()** → `CcpFileDecorator`: Remove o arquivo do sistema de arquivos.
- **rename(String newFileName)** → `CcpFileDecorator`: Renomeia o arquivo para o novo nome informado e retorna o decorator do novo arquivo.
- **getContent()** → `String`: Implementação de `CcpDecorator`; retorna o caminho do arquivo.

---

## Classe: CcpFolderDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Decorator sobre um caminho de diretório no sistema de arquivos. Oferece operações de criação de subpastas e arquivos, iteração sobre conteúdo, compactação ZIP e remoção recursiva.

### Métodos:
- **CcpFolderDecorator(String content)** → construtor (protegido): Encapsula o caminho e resolve o diretório pai.
- **asFile()** → `CcpFileDecorator`: Reinterpreta o caminho como um arquivo.
- **zip()** → `CcpFolderDecorator`: Compacta o diretório inteiro em um arquivo `.zip` com o mesmo nome.
- **getName()** → `String`: Retorna apenas o nome do diretório (sem o caminho pai).
- **readFolders(Consumer<CcpFolderDecorator> consumer)** → `CcpFolderDecorator`: Itera sobre cada entrada do diretório (arquivos e subpastas) e chama o `consumer` passando um `CcpFolderDecorator` para cada entrada.
- **readFiles(Consumer<CcpFileDecorator> consumer)** → `CcpFolderDecorator`: Itera sobre cada entrada do diretório e chama o `consumer` com um `CcpFileDecorator` para cada entrada.
- **exists()** → `boolean`: Verifica se o diretório existe.
- **createNewFolderIfNotExists(String folderName)** → `CcpFolderDecorator`: Cria um subdiretório com o nome fornecido (se não existir) e retorna o decorator do novo diretório.
- **createNewFolderIfNotExists()** → `CcpFolderDecorator`: Cria o próprio diretório se não existir.
- **createNewFileIfNotExists(String fileName)** → `CcpFileDecorator`: Cria um arquivo vazio dentro do diretório (garantindo que a estrutura de pastas exista) e retorna seu decorator.
- **writeInTheFile(String fileName, String fileContent)** → `CcpFileDecorator`: Escreve `fileContent` no arquivo `fileName` dentro do diretório e retorna o decorator do arquivo.
- **remove()** → `CcpFolderDecorator`: Remove todos os arquivos do diretório e depois o próprio diretório.
- **getContent()** → `String`: Implementação de `CcpDecorator`; retorna o caminho do diretório.

---

## Classe: CcpHashDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Decorator sobre uma string que oferece operações de hash criptográfico (MD5, SHA1, SHA256, SHA512). Converte o conteúdo interno em bytes e aplica o algoritmo especificado, retornando o resultado como `BigInteger` ou string hexadecimal.

### Métodos:
- **CcpHashDecorator(String content)** → construtor (protegido): Encapsula a string a ser hasheada.
- **toString()** → `String`: Retorna a string original.
- **asString(CcpHashAlgorithm algorithm)** → `String`: Aplica o algoritmo de hash e retorna o resultado como string hexadecimal em minúsculas.
- **asBigInteger(CcpHashAlgorithm algorithm)** → `BigInteger`: Aplica o algoritmo de hash e retorna o resultado como `BigInteger` (útil para operações numéricas sobre o hash).
- **getContent()** → `String`: Implementação de `CcpDecorator`; retorna a string original.

---

## Classe: CcpInputStreamDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Decorator sobre um identificador de recurso (nome de variável de ambiente, caminho de arquivo ou recurso no classpath) que resolve e abre um `InputStream` de diferentes fontes. Suporta fallback automático entre as fontes.

### Métodos:
- **CcpInputStreamDecorator(String content)** → construtor (protegido): Encapsula o identificador do recurso.
- **toString()** → `String`: Retorna o identificador.
- **environmentVariables()** → `InputStream`: Lê a variável de ambiente cujo nome é o conteúdo encapsulado. Se o valor for um caminho de arquivo existente, abre o arquivo; caso contrário, converte o valor em `ByteArrayInputStream`. Lança `CcpErrorInputStreamMissing` se a variável não existir ou estiver vazia.
- **classLoader()** → `InputStream`: Abre o recurso como arquivo do classpath (via `ClassLoader.getResource`). Lança `CcpErrorInputStreamMissing` se o recurso não for encontrado.
- **file()** → `InputStream`: Abre o arquivo do sistema de arquivos pelo caminho encapsulado. Lança `CcpErrorInputStreamMissing` se o arquivo não existir.
- **byteArray()** → `InputStream`: Converte o conteúdo textual diretamente em `ByteArrayInputStream`.
- **fromEnvironmentVariablesOrClassLoaderOrFile()** → `InputStream`: Tenta as três fontes em sequência (variável de ambiente → classpath → arquivo) e retorna o primeiro `InputStream` que for encontrado com sucesso.
- **getContent()** → `String`: Implementação de `CcpDecorator`; retorna o identificador do recurso.

---

## Classe: CcpJsonRepresentation
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Tipo central do framework jobsnow. Representa um documento JSON como um mapa imutável `Map<String, Object>` e é o único tipo de dado que flui entre todos os componentes de negócio. Oferece uma API fluente abrangente para leitura, escrita, transformação, comparação, navegação em profundidade e validação condicional de campos.

### Interface interna: CcpJsonFieldName
Contrato de identificadores de campo. Implementado por enums, lambdas ou classes; garante que nomes de campo sejam referenciados de forma tipada (sem strings literais espalhadas pelo código). O método padrão `getValue()` chama `name()`, suportando enums diretamente.

### Enum interno: Fields
Campos padrão usados para serializar detalhes de exceções: `cause`, `message`, `stackTrace`, `type`, `stackTraceHash`, `completeStackTrace`.

### Construtores:
- **CcpJsonRepresentation()** (protegido): Cria instância com mapa vazio.
- **getEmptyJson()** → `CcpJsonRepresentation` (estático): Fábrica pública para JSON vazio.
- **CcpJsonRepresentation(InputStream is)**: Lê o stream e constrói o JSON; aceita conteúdo JSON ou formato `Properties`.
- **CcpJsonRepresentation(Throwable e)**: Serializa os detalhes de uma exceção (mensagem, stack trace, causa) em JSON.
- **CcpJsonRepresentation(String json)**: Desserializa uma string JSON; lança `CcpErrorJsonInvalid` se inválida.
- **CcpJsonRepresentation(Map<String, Object> content)**: Cria a partir de um mapa existente; lança `CcpErrorJsonNull` se `null`.

### Métodos de leitura de campos:
- **getAsString(CcpJsonFieldName field)** → `String`: Retorna o valor do campo como string. Retorna `""` se ausente ou nulo; serializa Maps e Collections corretamente.
- **getAsLongNumber(CcpJsonFieldName field)** → `Long`: Retorna o valor do campo convertido para `long`. Lança `CcpErrorJsonInvalidFieldFormat` se não for conversível.
- **getAsIntegerNumber(CcpJsonFieldName field)** → `Integer`: Idem para `int`.
- **getAsDoubleNumber(CcpJsonFieldName field)** → `Double`: Idem para `double`.
- **getAsBoolean(CcpJsonFieldName field)** → `boolean`: Retorna o valor interpretado como booleano.
- **getAsEnum(CcpJsonFieldName field, Class<T> clazz)** → `T`: Converte o valor do campo para uma constante de enum via reflexão.
- **getAsStringDecorator(CcpJsonFieldName field)** → `CcpStringDecorator`: Retorna o valor encapsulado em `CcpStringDecorator`.
- **getAsTextDecorator(CcpJsonFieldName field)** → `CcpTextDecorator`: Retorna o valor encapsulado em `CcpTextDecorator`.
- **getOrDefault(CcpJsonFieldName field, Supplier<T> supplier)** → `T`: Retorna o valor do campo ou o valor padrão fornecido pelo `Supplier` se ausente.
- **get(CcpJsonFieldName field)** → `Object`: Retorna o valor bruto do campo. Lança `CcpErrorJsonFieldNotFound` se ausente.
- **getAsObject(CcpJsonFieldName... fields)** → `T`: Retorna o valor bruto do primeiro campo encontrado dentre os fornecidos. Lança `CcpErrorJsonFieldNotFound` se nenhum for encontrado.
- **getAsObjectList(CcpJsonFieldName field)** → `List<Object>`: Retorna o campo como lista de objetos. Suporta arrays, `Collection` e strings JSON.
- **getAsStringList(CcpJsonFieldName... fields)** → `List<String>`: Retorna como lista de strings o primeiro campo da lista que contiver valores.
- **getAsStringArray(CcpJsonFieldName... fields)** → `String[]`: Versão array de `getAsStringList`.
- **getAsCollectionDecorator(String field)** → `CcpCollectionDecorator`: Retorna o campo como `CcpCollectionDecorator`.
- **getAsArrayMetadata(CcpJsonFieldName field)** → `CcpCollectionDecorator`: Semelhante ao anterior; encapsula o conteúdo do campo em `CcpCollectionDecorator`.
- **getAsJsonList(CcpJsonFieldName field)** → `List<CcpJsonRepresentation>`: Retorna o campo como lista de JSONs. Suporta `Collection<Map>` e string JSON.
- **getInnerJson(CcpJsonFieldName field)** → `CcpJsonRepresentation`: Retorna o valor do campo como JSON aninhado (aceita Map, String JSON ou CcpJsonRepresentation).
- **getInnerJsonFromPath(CcpJsonFieldName... paths)** → `CcpJsonRepresentation`: Navega por um caminho de múltiplos campos aninhados e retorna o JSON encontrado.
- **getInnerJsonListFromPath(CcpJsonFieldName... paths)** → `List<CcpJsonRepresentation>`: Navega por um caminho e retorna o valor final como lista de JSONs.
- **getValueFromPath(T defaultValue, CcpJsonFieldName... paths)** → `T`: Navega pelo caminho e retorna o valor tipado; retorna `defaultValue` se qualquer campo do caminho estiver ausente.
- **getJsonPiece(CcpJsonFieldName... fields)** → `CcpJsonRepresentation`: Retorna um novo JSON contendo apenas os campos especificados (projeção).
- **getJsonPiece(Collection<String> fields)** → `CcpJsonRepresentation`: Idem, aceitando coleção de strings.
- **fieldSet()** → `Set<String>`: Retorna o conjunto de nomes de campos presentes.
- **isEmpty()** → `boolean`: Retorna `true` se o JSON não possui campos.
- **isInnerJson(CcpJsonFieldName fieldName)** → `boolean`: Retorna `true` se o valor do campo é um JSON de objeto válido.

### Métodos de escrita/transformação:
- **put(CcpJsonFieldName field, Object value)** → `CcpJsonRepresentation`: Retorna uma nova instância com o campo adicionado/substituído (imutável).
- **put(CcpJsonFieldName field, CcpJsonRepresentation value)** → `CcpJsonRepresentation`: Adiciona um JSON aninhado como valor do campo.
- **put(CcpJsonFieldName field, CcpDecorator<?> map)** → `CcpJsonRepresentation`: Extrai o conteúdo do decorator e o adiciona ao campo.
- **put(CcpJsonFieldName field, Collection<CcpJsonRepresentation> list)** → `CcpJsonRepresentation`: Adiciona uma lista de JSONs convertendo-os para List de Maps.
- **put(CcpJsonFieldName field)** → `CcpJsonRepresentation`: Usa o `getValue()` do próprio enum como valor do campo (conveniente para campos auto-descritivos).
- **putSameValueInManyFields(Object value, CcpJsonFieldName... fields)** → `CcpJsonRepresentation`: Define o mesmo valor em múltiplos campos de uma vez.
- **putIfNotContains(CcpJsonFieldName field, Object value)** → `CcpJsonRepresentation`: Adiciona o campo apenas se ele ainda não existir no JSON.
- **addToList(CcpJsonFieldName field, Object... values)** → `CcpJsonRepresentation`: Acrescenta valores ao final da lista existente no campo (ou cria a lista).
- **addToList(CcpJsonFieldName field, CcpJsonRepresentation value)** → `CcpJsonRepresentation`: Acrescenta um JSON à lista do campo.
- **addToItem(CcpJsonFieldName field, CcpJsonFieldName subField, Object value)** → `CcpJsonRepresentation`: Adiciona um sub-campo ao JSON aninhado dentro de `field`.
- **addToItem(CcpJsonFieldName field, CcpJsonFieldName subField, CcpJsonRepresentation value)** → `CcpJsonRepresentation`: Idem com valor JSON.
- **addJsonTransformer(CcpJsonFieldName field, CcpBusiness process)** → `CcpJsonRepresentation`: Associa um `CcpBusiness` como valor de um campo (armazenamento de transformadores para uso posterior).
- **addJsonTransformer(Integer field, CcpBusiness process)** → `CcpJsonRepresentation`: Idem com campo inteiro (índice).
- **removeFields(CcpJsonFieldName... fields)** → `CcpJsonRepresentation`: Retorna nova instância sem os campos especificados.
- **renameField(CcpJsonFieldName oldField, CcpJsonFieldName newField)** → `CcpJsonRepresentation`: Renomeia um campo (move o valor de `oldField` para `newField`).
- **duplicateValueFromField(CcpJsonFieldName fieldToCopy, CcpJsonFieldName... fieldsToPaste)** → `CcpJsonRepresentation`: Copia o valor de um campo para um ou mais outros campos.
- **copyIfNotContains(CcpJsonFieldName fieldToCopy, CcpJsonFieldName fieldToPaste)** → `CcpJsonRepresentation`: Copia o valor apenas se `fieldToPaste` ainda não existir.
- **mergeWithAnotherJson(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Une dois JSONs, com os campos do argumento sobrepondo os campos existentes em caso de conflito.
- **mergeWithAnotherJson(Map<String, Object> map)** → `CcpJsonRepresentation`: Idem a partir de um mapa.

### Métodos de consulta condicional:
- **whenFieldsAreNotFound(CcpBusiness business, CcpJsonFieldName... fields)** → `CcpJsonRepresentation`: Executa `business` somente se NENHUM dos campos especificados estiver presente.
- **whenAnyFieldsAreFound(CcpBusiness business, CcpJsonFieldName... fields)** → `CcpJsonRepresentation`: Executa `business` se PELO MENOS UM dos campos estiver presente.
- **whenAllFieldsAreFound(CcpBusiness business, CcpJsonFieldName... fields)** → `CcpJsonRepresentation`: Executa `business` somente se TODOS os campos estiverem presentes.
- **getTransformedJsonExecutingIfAndElse(Predicate, CcpBusiness conditionsMet, CcpBusiness conditionsDoNotMet)** → `CcpJsonRepresentation`: Executa `conditionsMet` se o predicado for verdadeiro, caso contrário executa `conditionsDoNotMet`.
- **getTransformedJsonWhenAllConditionsMatch(CcpBusiness conditionsMet, CcpBusiness conditionsDoNotMet, Predicate...)** → `CcpJsonRepresentation`: Avalia múltiplos predicados; se todos passarem, executa `conditionsMet`; ao primeiro falhar, executa `conditionsDoNotMet`.
- **getTransformedJsonConsideringIfAnyOfTheConditionsIsMet(CcpBusiness conditionsMet, CcpBusiness conditionsDoNotMet, Predicate...)** → `CcpJsonRepresentation`: Executa `conditionsMet` ao primeiro predicado verdadeiro; se nenhum passar, executa `conditionsDoNotMet`.
- **extractInformationFromJson(Function<CcpJsonRepresentation, T> extractor)** → `T`: Aplica uma função extratora sobre este JSON e retorna o resultado.
- **getTransformedJson(CcpBusiness... transformers)** → `CcpJsonRepresentation`: Aplica uma sequência de transformadores em cadeia, passando o resultado de um para o próximo.
- **getTransformedJson(List<CcpBusiness> jsonTransformers)** → `CcpJsonRepresentation`: Idem, aceitando lista.

### Métodos de verificação de campos:
- **containsField(CcpJsonFieldName field)** → `boolean`: Verifica se um campo específico existe e não é nulo.
- **containsAllFields(CcpJsonFieldName... fields)** → `boolean`: Retorna `true` se todos os campos estiverem presentes.
- **containsAnyFields(CcpJsonFieldName... fields)** → `boolean`: Retorna `true` se pelo menos um campo estiver presente.
- **containsAllFields(Collection<String> fields)** / **containsAnyFields(Collection<String> fields)**: Versões que aceitam coleção de strings.

### Métodos de serialização e utilidade:
- **asUgglyJson()** → `String`: Serializa o JSON em formato compacto (sem formatação).
- **asPrettyJson()** → `String`: Serializa com indentação e quebras de linha.
- **toString()** → `String`: Serializa em formato pretty, com campos em ordem alfabética (`TreeMap`).
- **toInputStream()** → `InputStream`: Serializa o JSON em bytes UTF-8 e retorna como `ByteArrayInputStream`.
- **getSha1Hash(CcpHashAlgorithm algorithm)** → `String`: Calcula o hash do JSON serializado compacto.
- **hashCode()** → `int`: Baseado no hash SHA1 do conteúdo.
- **equals(Object obj)** → `boolean`: Compara dois JSONs via hash SHA1 (igualdade por conteúdo, não por referência).
- **copy()** → `CcpJsonRepresentation`: Cria uma cópia independente do JSON atual.
- **getContent()** → `Map<String, Object>`: Retorna o mapa interno imutável.
- **getJsonSupplier()** → `Supplier<CcpJsonRepresentation>`: Retorna um `Supplier` que fornece esta instância.
- **redoJson(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Cria uma nova instância a partir do conteúdo de outro JSON (cópia de conteúdo).

---

## Classe: CcpNumberDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Decorator sobre um valor numérico (`double`) que oferece operações de comparação semântica. Recebe o número como string e converte para `double`, facilitando validações de faixa de valores.

### Métodos:
- **CcpNumberDecorator(String content)** → construtor: Converte a string para `double`.
- **toString()** → `String`: Retorna o número como string.
- **greaterThan(Double x)** → `boolean`: Retorna `true` se o valor for estritamente maior que `x`.
- **equalsOrGreaterThan(Double x)** → `boolean`: Retorna `true` se o valor for maior ou igual a `x`.
- **lessThan(Double x)** → `boolean`: Retorna `true` se o valor for estritamente menor que `x`.
- **equalsOrLessThan(Double x)** → `boolean`: Retorna `true` se o valor for menor ou igual a `x`.
- **equalsTo(Double x)** → `boolean`: Retorna `true` se o valor for exatamente igual a `x`.
- **belongsToRestrictedValues(Double... restrictedValues)** → `boolean`: Retorna `true` se o valor estiver em um conjunto fixo de valores permitidos (varargs).
- **belongsToRestrictedValues(Collection<Double> restrictedValues)** → `boolean`: Idem, aceitando coleção.
- **getContent()** → `Double`: Implementação de `CcpDecorator`; retorna o valor como `Double`.

---

## Classe: CcpPasswordDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Decorator sobre uma string de senha que valida força. Encapsula a senha e oferece verificação se ela atende aos critérios de complexidade do sistema (ao menos um dígito, uma letra minúscula, uma maiúscula, um caractere especial e entre 8 e 20 caracteres).

### Métodos:
- **CcpPasswordDecorator(String content)** → construtor (protegido): Encapsula a senha.
- **toString()** → `String`: Retorna a senha bruta.
- **isStrong()** → `boolean`: Verifica se a senha atende à regex de complexidade do sistema.
- **getContent()** → `String`: Implementação de `CcpDecorator`; retorna a senha.

---

## Classe: CcpPropertiesDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Decorator especializado em leitura de arquivos de configuração (formato `.properties` ou JSON). Delega a abertura do stream para `CcpInputStreamDecorator` e converte o resultado em `CcpJsonRepresentation`, permitindo acessar configurações como um mapa JSON uniforme.

### Métodos:
- **CcpPropertiesDecorator(String content)** → construtor (protegido): Encapsula o identificador do recurso.
- **environmentVariables()** → `CcpJsonRepresentation`: Carrega as configurações a partir de uma variável de ambiente.
- **classLoader()** → `CcpJsonRepresentation`: Carrega as configurações a partir do classpath.
- **file()** → `CcpJsonRepresentation`: Carrega as configurações a partir de um arquivo no sistema de arquivos.
- **environmentVariablesOrClassLoaderOrFile()** → `CcpJsonRepresentation`: Tenta as três fontes em sequência e retorna as configurações da primeira disponível.
- **getContent()** → `CcpInputStreamDecorator`: Implementação de `CcpDecorator`; retorna o decorator de stream interno.

---

## Classe: CcpReflectionConstructorDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Decorator sobre o nome qualificado de uma classe Java que oferece operações de reflexão: resolução da classe, criação de instâncias e escolha do contexto de invocação (estático ou por instância). É o ponto de entrada da API fluente de reflexão do framework.

### Métodos:
- **CcpReflectionConstructorDecorator(String content)** → construtor (protegido): Encapsula o nome completo da classe.
- **CcpReflectionConstructorDecorator(CcpJsonRepresentation json, String field)** → construtor: Extrai o nome da classe a partir de um campo do JSON.
- **CcpReflectionConstructorDecorator(Class<?> clazz)** → construtor: Extrai o nome da classe diretamente de um objeto `Class`.
- **forName()** → `Class<?>`: Carrega e retorna o objeto `Class` pelo nome. Lança `RuntimeException` se a classe não for encontrada.
- **thisClassExists()** → `boolean`: Retorna `true` se a classe puder ser carregada; `false` caso contrário (sem lançar exceção).
- **newInstance()** → `T`: Cria uma nova instância da classe via construtor padrão (sem argumentos), mesmo que o construtor seja privado (usa `setAccessible(true)`).
- **toString()** → `String`: Retorna o nome completo da classe.
- **getContent()** → `String`: Implementação de `CcpDecorator`; retorna o nome da classe.
- **fromStaticContext()** → `CcpReflectionOptionsDecorator`: Inicia o contexto de reflexão para chamada de métodos estáticos (retorna `CcpReflectionStaticContextDecorator`).
- **fromNewInstance()** → `CcpReflectionOptionsDecorator`: Inicia o contexto de reflexão criando uma nova instância da classe (retorna `CcpReflectionNewInstanceDecorator`).
- **fromInstance(Object instance)** → `CcpReflectionOptionsDecorator`: Inicia o contexto de reflexão a partir de uma instância já existente.

---

## Classe: CcpReflectionNewInstanceDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Especialização de `CcpReflectionOptionsDecorator` que mantém uma referência a uma instância de objeto para uso em chamadas de métodos de instância via reflexão. Pode encapsular uma instância existente ou criar uma nova via `CcpReflectionConstructorDecorator`.

### Métodos:
- **CcpReflectionNewInstanceDecorator(Object instance, Class<?> clazz)** → construtor: Encapsula uma instância já existente e a classe correspondente.
- **CcpReflectionNewInstanceDecorator(CcpReflectionConstructorDecorator constructor)** → construtor (protegido): Carrega a classe e cria uma nova instância via reflexão.

---

## Classe: CcpReflectionOptionsDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe abstrata
**Propósito:** Classe base da hierarquia de reflexão do framework. Encapsula o objeto `Class<?>` alvo e implementa `CcpDecorator<Class<?>>`. As subclasses (`CcpReflectionNewInstanceDecorator` e `CcpReflectionStaticContextDecorator`) especializam se a chamada é por instância ou por contexto estático.

### Métodos:
- **CcpReflectionOptionsDecorator(Class<?> clazz)** → construtor (protegido): Encapsula a classe.
- **getContent()** → `Class<?>`: Implementação de `CcpDecorator`; retorna a classe encapsulada.

---

## Classe: CcpReflectionStaticContextDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Especialização de `CcpReflectionOptionsDecorator` para chamadas de métodos estáticos via reflexão. Recebe um `CcpReflectionConstructorDecorator` e resolve a classe, sem criar instância.

### Métodos:
- **CcpReflectionStaticContextDecorator(CcpReflectionConstructorDecorator constructor)** → construtor (protegido): Resolve a classe pelo nome usando `constructor.forName()`.

---

## Classe: CcpStringDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Decorator central sobre `String` que serve como hub de conversão para todos os outros tipos do framework. A partir de uma string, oferece acesso fluente a decorators especializados (e-mail, arquivo, hash, JSON, URL, reflexão, etc.) e verificações de tipo.

### Métodos:
- **CcpStringDecorator(String content)** / **(CcpJsonRepresentation, String key)** / **(InputStream)** / **(byte[])** / **(Byte[])** → construtores: Diversas formas de criar o decorator a partir de strings, streams ou arrays de bytes.
- **email()** → `CcpEmailDecorator`: Interpreta a string como endereço de e-mail.
- **file()** → `CcpFileDecorator`: Interpreta a string como caminho de arquivo.
- **folder()** → `CcpFolderDecorator`: Interpreta a string como caminho de diretório.
- **hash()** → `CcpHashDecorator`: Prepara a string para cálculo de hash.
- **number()** → `CcpNumberDecorator`: Converte a string para número.
- **text()** → `CcpTextDecorator`: Acessa operações de manipulação de texto.
- **url()** → `CcpUrlDecorator`: Interpreta a string como URL para encode/decode.
- **json()** → `CcpJsonRepresentation`: Desserializa a string como JSON.
- **password()** → `CcpPasswordDecorator`: Interpreta a string como senha.
- **inputStreamFrom()** → `CcpInputStreamDecorator`: Prepara a string para abertura de stream.
- **propertiesFrom()** → `CcpPropertiesDecorator`: Prepara a string para leitura de configurações.
- **reflection()** → `CcpReflectionConstructorDecorator`: Prepara a string (nome de classe) para reflexão.
- **jsonFieldName()** → `CcpJsonFieldName`: Cria um `CcpJsonFieldName` cujo valor é esta string.
- **isInnerJson()** → `boolean`: Verifica se a string representa um JSON de objeto válido.
- **isList()** → `boolean`: Verifica se a string representa uma lista JSON.
- **isLongNumber()** → `boolean`: Verifica se a string pode ser convertida para `long`.
- **isDoubleNumber()** → `boolean`: Verifica se a string pode ser convertida para `double`.
- **isBoolean()** → `boolean`: Verifica se a string é `"true"` ou `"false"` (case insensitive).
- **toString()** → `String`: Retorna a string interna.
- **getContent()** → `String`: Implementação de `CcpDecorator`.
- **getBytes()** → `Byte[]`: Retorna o array de bytes da string (tipo wrapper `Byte[]`).

---

## Classe: CcpTextDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Decorator especializado em operações de manipulação e análise de texto: remoção de acentos, geração de tokens aleatórios, preenchimento, capitalização, conversão de case, validação de regex, sanitização para busca, encode/decode Base64 e resolução de templates com variáveis.

### Métodos:
- **CcpTextDecorator(String content)** → construtor (protegido): Encapsula o texto.
- **completeLeft(char complement, int length)** → `CcpTextDecorator`: Preenche o texto à esquerda com o caractere `complement` até atingir o tamanho `length`.
- **stripAccents()** → `CcpTextDecorator`: Remove acentos e diacríticos preservando `#` e caracteres alfanuméricos básicos.
- **getPieces(String beginDelimiter, String endDelimiter)** → `List<String>`: Extrai todas as substrings delimitadas por `beginDelimiter` e `endDelimiter`.
- **getPieces(Predicate<String> predicate, String delimiter)** → `List<String>`: Divide o texto pelo delimitador e filtra as partes pelo predicado.
- **removePieces(Predicate<String> predicate, String delimiter)** → `CcpTextDecorator`: Remove as partes que atendem ao predicado, substituindo-as por espaço.
- **removePieces(String beginDelimiter, String endDelimiter)** → `CcpTextDecorator`: Remove todas as substrings delimitadas.
- **removePieces(List<String> pieces)** → `CcpTextDecorator`: Remove da string cada substring da lista fornecida.
- **replace(String oldText, String newText)** → `CcpTextDecorator`: Substitui todas as ocorrências de `oldText` por `newText`.
- **generateToken(long charactersSize)** → `CcpTextDecorator`: Gera um token aleatório de tamanho `charactersSize` selecionando caracteres do conteúdo atual (útil como alfabeto de tokens).
- **getByteArrayInputStream()** → `InputStream`: Decodifica a string Base64 e retorna como `ByteArrayInputStream`.
- **getByteArrayFromBase64String()** → `byte[]`: Decodifica a string Base64 (suportando prefixo `data:xxx,base64`) e retorna o array de bytes.
- **getParameterAsByteArrayInputStream()** → `ByteArrayInputStream`: Alias de `getByteArrayInputStream`.
- **resolveTemplate(CcpJsonRepresentation parameters)** → `CcpTextDecorator`: Substitui os placeholders `{nomeDoCampo}` pelos valores correspondentes do JSON de parâmetros. Suporta também a função dinâmica `{currentTimeMillis()}`.
- **removeStartingCharacters(char c)** → `CcpTextDecorator`: Remove recursivamente todos os caracteres `c` do início da string.
- **removeEndingCharacters(char c)** → `CcpTextDecorator`: Remove recursivamente todos os caracteres `c` do final da string.
- **isValidSingleJson()** → `boolean`: Verifica se o texto é um JSON de objeto válido.
- **asBase64()** → `CcpTextDecorator`: Codifica o texto em Base64.
- **toCamelCase()** → `CcpTextDecorator`: Converte texto em `snake_case` para `CamelCase`.
- **toSnakeCase()** → `CcpTextDecorator`: Converte texto em `CamelCase` para `snake_case`.
- **capitalize()** → `CcpTextDecorator`: Coloca a primeira letra em maiúscula e o restante em minúsculas.
- **lenght()** → `CcpNumberDecorator`: Retorna o tamanho do texto encapsulado em `CcpNumberDecorator`.
- **regexMatches(String regex)** → `boolean`: Verifica se o texto corresponde à expressão regular (case insensitive).
- **contains(String phrase)** → `boolean`: Verifica se o texto contém a frase usando sanitização por delimitadores padrão e comparação de palavras.
- **contains(String[] delimiters, String phrase)** → `boolean`: Idem com delimitadores personalizados.
- **sanitize()** → `CcpTextDecorator`: Substitui os delimitadores padrão por espaço e converte para maiúsculas sem acentos.
- **sanitize(String[] delimiters)** → `CcpTextDecorator`: Idem com delimitadores personalizados.
- **toString()** / **getContent()**: Retornam o texto interno.

---

## Classe: CcpTimeDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Decorator sobre um timestamp em milissegundos (`Long`) que oferece operações de calendário com fuso horário brasileiro (America/Sao_Paulo), formatação de data/hora, cálculo de meia-noite e controle de pausa de thread.

### Métodos:
- **CcpTimeDecorator(Long time)** → construtor: Encapsula o timestamp fornecido.
- **CcpTimeDecorator()** → construtor: Usa `System.currentTimeMillis()` como timestamp atual.
- **getSecondsEnlapsedSinceMidnight()** → `long`: Calcula quantos segundos se passaram desde a meia-noite do dia atual (fuso de São Paulo).
- **getYear()** → `int`: Retorna o ano do timestamp.
- **getMidnight()** → `Long`: Retorna o timestamp da meia-noite do dia atual no fuso de São Paulo.
- **getFormattedDateTime(String pattern)** → `String`: Formata o timestamp usando o padrão fornecido (`SimpleDateFormat`).
- **getBrazilianCalendar()** → `Calendar`: Retorna um `Calendar` configurado com o fuso horário `America/Sao_Paulo`.
- **sleep(int i)** → `boolean`: Pausa a thread por `i` milissegundos. Retorna `true` se a pausa foi concluída; `false` se o valor for ≤ 0 ou se a thread for interrompida.
- **getContent()** → `Long`: Implementação de `CcpDecorator`; retorna o timestamp.

---

## Classe: CcpUrlDecorator
**Pacote:** `com.ccp.decorators`
**Tipo:** classe
**Propósito:** Decorator sobre uma string de URL que oferece encode e decode de caracteres especiais usando o padrão UTF-8 (`java.net.URLEncoder` / `URLDecoder`). Útil para montar e interpretar parâmetros de query string.

### Métodos:
- **CcpUrlDecorator(String content)** → construtor (protegido): Encapsula a URL ou fragmento de URL.
- **toString()** → `String`: Retorna a string original.
- **asDecoded()** → `String`: Decodifica os caracteres percent-encoded (ex.: `%40` → `@`). Retorna a string original em caso de codificação não suportada.
- **asEnconded()** → `String`: Codifica caracteres especiais para uso em query strings (ex.: `@` → `%40`). Retorna a string original em caso de codificação não suportada.
- **getContent()** → `String`: Implementação de `CcpDecorator`; retorna a string.

---

## Classe: CcpDependencyInjection
**Pacote:** `com.ccp.dependency.injection`
**Tipo:** classe (utilitária estática)
**Propósito:** Registro central de injeção de dependência do framework. Mantém um mapa estático de interface → implementação e fornece métodos para registrar, recuperar, remover e substituir temporariamente implementações. É o mecanismo que permite trocar comportamentos (real vs. mock) em testes ou em diferentes ambientes.

### Métodos:
- **replaceDependenciesTemporally(CcpJsonRepresentation json, CcpBusiness business, CcpInstanceProvider<?>... providers)** → `CcpJsonRepresentation`: Substitui temporariamente as dependências informadas pelos `providers`, executa `business.apply(json)`, restaura as dependências originais e retorna o resultado. Garante isolamento em testes.
- **loadAllDependencies(CcpInstanceProvider<?>... providers)** → `void`: Registra cada provider: chama `getInstance()`, identifica a interface implementada (primeira interface da classe) e armazena no mapa estático.
- **hasDependency(Class<T> interfaceClass)** → `boolean`: Verifica se já existe uma implementação registrada para a interface informada.
- **getDependency(Class<T> interfaceClass)** → `T`: Retorna a implementação registrada para a interface. Lança `CcpErrorDependencyInjectionMissing` se não houver implementação.
- **removeDependecy(Class<?> interfaceClass)** → `void`: Remove o registro da implementação para a interface informada.
- **getInstance(Class<CcpInstanceProvider<T>> interfaceClass)** → `T`: Cria uma instância do provider via reflexão e chama `getInstance()`, sem registrar no mapa. Útil para obter instâncias pontuais sem afetar o contexto global.

---

## Classe: CcpErrorDependencyInjectionMissing
**Pacote:** `com.ccp.dependency.injection`
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando `CcpDependencyInjection.getDependency()` é chamado para uma interface que não possui implementação registrada. Indica que o bootstrap do sistema (chamada a `loadAllDependencies`) não foi realizado corretamente.

### Métodos:
- **CcpErrorDependencyInjectionMissing(Class<?> interfaceClass)** → construtor: Monta a mensagem indicando o nome da interface sem implementação registrada.

---

## Classe: CcpInstanceProvider
**Pacote:** `com.ccp.dependency.injection`
**Tipo:** interface
**Propósito:** Contrato de fábrica para o sistema de injeção de dependência. Cada módulo que fornece uma implementação de uma interface especificada pelo framework deve criar uma classe que implemente `CcpInstanceProvider<T>`, onde `T` é a interface a ser fornecida.

### Métodos:
- **getInstance()** → `T`: Cria e retorna a instância da implementação que será registrada no mapa de DI.

---

## Classe: CcpTreeFlow
**Pacote:** `com.ccp.flow`
**Tipo:** classe (utilitária estática)
**Propósito:** Ponto de entrada da API fluente de controle de fluxo condicional do framework. Permite construir pipelines do tipo "tente executar X; se retornar status Y, execute Z; ao final, encerre o statement". A API modela cenários de fluxo com tratamento de exceções de negócio (`CcpErrorFlowDisturb`) de forma declarativa.

### Métodos:
- **beginThisStatement()** → `CcpBeginThisStatement` (estático): Inicia a construção de um statement de fluxo. É o único ponto de entrada; os demais passos seguem a cadeia fluente.

---

## Classe: CcpBeginThisStatement
**Pacote:** `com.ccp.flow`
**Tipo:** classe (final)
**Propósito:** Primeiro elo da cadeia fluente de fluxo. Recebe o processo principal que se deseja tentar executar.

### Métodos:
- **tryToExecuteTheGivenFinalTargetProcess(CcpBusiness givenFinalTargetProcess)** → `CcpTryToExecuteTheGivenFinalTargetProcess`: Registra o processo alvo principal e avança para o próximo passo da cadeia (informar o JSON de entrada).

---

## Classe: CcpTryToExecuteTheGivenFinalTargetProcess
**Pacote:** `com.ccp.flow`
**Tipo:** classe (final)
**Propósito:** Segundo elo da cadeia fluente. Recebe o JSON de entrada que será passado ao processo principal.

### Métodos:
- **usingTheGivenJson(CcpJsonRepresentation givenJson)** → `CcpUsingTheGivenJson`: Registra o JSON de entrada e avança para o passo onde se declaram os tratamentos condicionais por status.

---

## Classe: CcpUsingTheGivenJson
**Pacote:** `com.ccp.flow`
**Tipo:** classe (final)
**Propósito:** Terceiro elo da cadeia. Permite declarar o primeiro tratamento condicional: "mas se esta execução retornar o status X, então...".

### Métodos:
- **butIfThisExecutionReturns(CcpProcessStatus processStatus)** → `CcpIfThisExecutionReturns`: Registra um status de processo a ser tratado e avança para declarar quais processos executar naquele caso.

---

## Classe: CcpIfThisExecutionReturns
**Pacote:** `com.ccp.flow`
**Tipo:** classe (final)
**Propósito:** Quarto elo da cadeia. Associa um status de processo a uma lista de `CcpBusiness` que devem ser executados como tratamento alternativo quando aquele status ocorrer.

### Métodos:
- **thenExecuteTheGivenProcesses(CcpBusiness... givenProcess)** → `CcpExecuteTheGivenProcess`: Registra os processos de tratamento para o status declarado e avança para o passo de encadeamento adicional.

---

## Classe: CcpExecuteTheGivenProcess
**Pacote:** `com.ccp.flow`
**Tipo:** classe (final)
**Propósito:** Quinto elo da cadeia. Permite adicionar mais ramificações condicionais (via `and()`) ou encerrar o statement.

### Métodos:
- **and()** → `CcpAndIfThisExecutionReturns`: Permite adicionar novas condicionais ao fluxo (encadeamento de múltiplos tratamentos por status).

---

## Classe: CcpAndIfThisExecutionReturns
**Pacote:** `com.ccp.flow`
**Tipo:** classe (final)
**Propósito:** Sexto e último elo da cadeia fluente. Contém a lógica real de execução: tenta executar o processo principal e, se uma `CcpErrorFlowDisturb` for lançada, localiza no mapa de fluxo os processos de tratamento para aquele status e os executa recursivamente.

### Métodos:
- **ifThisExecutionReturns(CcpProcessStatus processStatus)** → `CcpIfThisExecutionReturns`: Adiciona mais uma ramificação condicional ao fluxo (volta ao quarto elo da cadeia).
- **endThisStatement(CcpBusiness... whatToNext)** → `CcpJsonRepresentation`: Executa o processo principal. Se bem-sucedido, executa também os processos `whatToNext` (pós-execução) e retorna o resultado. Se uma `CcpErrorFlowDisturb` for lançada, localiza os processos registrados para o status da exceção, os executa como tratamento e repete recursivamente até não haver mais ramificações a tratar.

---

## Classe: CcpErrorFlowDisturb
**Pacote:** `com.ccp.flow`
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção de controle de fluxo de negócio. Não representa um erro técnico, mas sim uma saída alternativa de um processo (similar a um status HTTP de erro). Carrega o status (`CcpProcessStatus`), o JSON com dados de contexto e campos relevantes, permitindo que a cadeia `CcpTreeFlow` capture e trate a situação de forma declarativa.

### Construtores:
- **CcpErrorFlowDisturb(CcpProcessStatus status, String... fields)**: Cria a exceção com JSON vazio e o status informado.
- **CcpErrorFlowDisturb(CcpJsonRepresentation json, CcpProcessStatus status, String... fields)**: Cria a exceção com JSON de contexto completo.
- **CcpErrorFlowDisturb(CcpJsonRepresentation json, CcpProcessStatus status, String message, String... fields)**: Idem com mensagem personalizada.

---

## Enum: CcpHashAlgorithm
**Pacote:** `com.ccp.hash`
**Tipo:** enum
**Propósito:** Catálogo dos algoritmos de hash suportados pelo framework (MD5, SHA1, SHA256, SHA512). Encapsula o nome técnico de cada algoritmo e gerencia um cache de instâncias de `MessageDigest` para evitar re-criação.

### Constantes: `MD5`, `SHA1`, `SHA256`, `SHA512`

### Métodos:
- **getMessageDigest()** → `MessageDigest`: Retorna a instância cacheada de `MessageDigest` para este algoritmo. Na primeira chamada, instancia e armazena; nas seguintes, retorna do cache.
- **getMessageDigest(String algorithm)** → `MessageDigest` (estático): Cria e retorna um `MessageDigest` para um algoritmo especificado por string. Lança `CcpErrorHashAlgorithmNotFound` se o algoritmo não for reconhecido pela JVM.

---

## Interface: CcpTransformers
**Pacote:** `com.ccp.json.transformers`
**Tipo:** interface
**Propósito:** Mixin de transformações JSON reutilizáveis para uso em `CcpBusiness`. Estende `CcpBusiness` e fornece métodos `default` para operações comuns de normalização de dados em um JSON: truncamento de strings, enforçamento de valor mínimo numérico, preenchimento de campos ausentes e verificação de obrigatoriedade condicional.

### Métodos:
- **substring(CcpJsonRepresentation json, String field, int limit)** → `CcpJsonRepresentation`: Se o valor do campo `field` ultrapassar `limit` caracteres, retorna o JSON com o valor truncado; caso contrário, retorna o JSON inalterado.
- **putMinValue(CcpJsonRepresentation json, String field, int minValue)** → `CcpJsonRepresentation`: Se o valor numérico do campo for menor que `minValue`, substitui pelo valor mínimo; se o campo não existir, retorna o JSON inalterado.
- **addLongValue(CcpJsonRepresentation json, String field, Long longValue)** → `CcpJsonRepresentation`: Se o valor do campo não for um número `long` válido, preenche o campo com `longValue`; caso contrário, mantém o existente.
- **addRequiredAtLeastOne(CcpJsonRepresentation json, String field, Object value, String... fields)** → `CcpJsonRepresentation`: Se nenhum dos campos em `fields` estiver presente no JSON, adiciona `field` com `value`. Garante que ao menos um campo do grupo esteja preenchido.

---

## Classe: CcpFunctionThrowException
**Pacote:** `com.ccp.process`
**Tipo:** classe
**Propósito:** Implementação de `CcpBusiness` que sempre lança uma exceção pré-definida ao ser executada. Útil em cenários de fluxo condicional onde uma determinada ramificação deve obrigatoriamente falhar (por exemplo, como valor de "ramo de erro" em uma estrutura `CcpTreeFlow`).

### Métodos:
- **CcpFunctionThrowException(RuntimeException exception)** → construtor: Armazena a exceção que será lançada.
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Lança imediatamente a exceção armazenada, sem processar o JSON.

---

## Interface: CcpProcessStatus
**Pacote:** `com.ccp.process`
**Tipo:** interface
**Propósito:** Contrato para representação de status de processo (análogo a códigos HTTP de resposta). Estende `CcpJsonFieldName`, permitindo uso direto em JSONs como chave de campo. Oferece métodos de verificação de status para uso em testes e de lançamento de exceção de fluxo.

### Métodos:
- **asNumber()** → `int`: Retorna o código numérico do status (ex.: 200, 404, 409).
- **verifyStatus(int actualStatus, String message)** → `String`: Compara `actualStatus` com o esperado; retorna o nome do status se correto ou lança `RuntimeException` com mensagem descritiva se incorreto. Usado em testes de integração.
- **verifyStatusNames(int actualStatus, String actualStatusName)** → `CcpProcessStatus`: Verifica tanto o código numérico quanto o nome do status. Retorna `this` se correto ou lança `RuntimeException` se houver divergência.
- **throwException(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Lança uma `CcpErrorFlowDisturb` com o JSON fornecido e este status. Usado para interromper o fluxo de forma controlada.

---

## Enum: CcpProcessStatusDefault
**Pacote:** `com.ccp.process`
**Tipo:** enum
**Propósito:** Conjunto padrão de status de processo HTTP-like usados em todo o sistema jobsnow. Implementa `CcpProcessStatus` e fornece os códigos mais comuns: `OK(200)`, `CREATED(201)`, `UPDATED(204)`, `REDIRECT(301)`, `INACTIVE_RECORD(302)`, `BAD_REQUEST(400)`, `UNHAUTHORIZED(401)`, `NOT_FOUND(404)`, `CONFLICT(409)`, `UNPROCESSABLE_ENTITY(422)`.

### Métodos:
- **asNumber()** → `int`: Retorna o código HTTP associado ao status.
- **asJsonFieldName()** → `CcpJsonFieldName`: Retorna um `CcpJsonFieldName` cujo valor é o código numérico como string (ex.: `"200"`), permitindo uso como chave de campo JSON.

---

## Interface: CcpService
**Pacote:** `com.ccp.service`
**Tipo:** interface
**Propósito:** Especialização de `CcpBusiness` para serviços expostos externamente (ex.: endpoints REST). Adiciona a obrigatoriedade de nome (`name()`) e validação de JSON de entrada antes da execução, além de converter `Map<String, Object>` para `CcpJsonRepresentation` automaticamente.

### Métodos:
- **getJsonValidationClass()** → `Class<?>`: Retorna a classe onde estão as anotações de validação do JSON de entrada para este serviço.
- **name()** → `String`: Retorna o nome identificador do serviço (usado em logs e mensagens de validação).
- **execute(Map<String, Object> map)** → `Map<String, Object>`: Converte o mapa em `CcpJsonRepresentation`, valida via `CcpJsonValidatorEngine`, executa `apply()` e retorna o mapa de saída. Lança `CcpServiceJsonValidationError` se o JSON gerado for inválido durante o processamento.

---

## Classe: CcpServiceJsonValidationError
**Pacote:** `com.ccp.service`
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada por `CcpService.execute()` quando ocorre uma `CcpErrorJsonInvalid` durante o processamento do serviço, indicando que os dados processados resultaram em um JSON inválido.

### Métodos:
- **CcpServiceJsonValidationError(CcpErrorJsonInvalid e)** → construtor: Encadeia a exceção original como causa.

---

## Classe: CcpAreOfTheType
**Pacote:** `com.ccp.validations`
**Tipo:** classe
**Propósito:** Componente da API fluente de validação. Dado um conjunto de campos de um JSON, verifica se todos os valores são de um tipo específico (booleano, double, JSON, lista, long). É retornada por `CcpItIsTrueThatTheFollowingFields.areAllOfTheType()` e `CcpIfTheyAreArrayValuesSoEachOne.isOfTheType()`.

### Métodos:
- **CcpAreOfTheType(Function<String[], String[]> arrayProducer, String[] fields)** → construtor: Recebe um produtor de valores (que extrai os valores dos campos do JSON) e os campos a verificar.
- **bool()** → `boolean`: Retorna `true` se todos os valores forem booleanos.
- **doubleNumber()** → `boolean`: Retorna `true` se todos os valores forem números double.
- **json()** → `boolean`: Retorna `true` se todos os valores forem JSON de objeto válido.
- **list()** → `boolean`: Retorna `true` se todos os valores forem listas JSON.
- **longNumber()** → `boolean`: Retorna `true` se todos os valores forem números long.

---

## Classe: CcpIfTheyAre
**Pacote:** `com.ccp.validations`
**Tipo:** classe
**Propósito:** Componente da API fluente de validação para campos de valor simples (não-array). Oferece verificações de pertencimento a lista, intervalo de tamanho/valor e conformidade com regex, aplicadas a todos os campos informados.

### Métodos:
- **CcpIfTheyAre(CcpJsonRepresentation content, String[] fields)** → construtor: Encapsula o JSON e os campos a validar.
- **textsThenEachOneIsContainedAtTheList(String... args)** → `boolean`: Retorna `true` se o valor de cada campo estiver na lista de valores permitidos `args`.
- **textsThenEachOneIsContainedAtTheList(Collection<T> args)** → `boolean`: Idem, aceitando coleção.
- **textsThenEachOneHasTheSizeThatIs()** → `CcpRangeSize`: Retorna um `CcpRangeSize` com os tamanhos (em caracteres) dos valores dos campos, para comparação de faixa.
- **numbersThenEachOneIs()** → `CcpRangeSize`: Retorna um `CcpRangeSize` com os valores numéricos dos campos.
- **numbersThenEachOneIsContainedAtTheList(Double... args)** → `boolean`: Retorna `true` se o valor numérico de cada campo estiver na lista fornecida.
- **numbersThenEachOneIsContainedAtTheList(Collection<Object> args)** → `boolean`: Idem, aceitando coleção.
- **textsThenEachOneMatchesWithTheFollowingRegex(String regex)** → `boolean`: Retorna `true` se o valor textual de cada campo corresponder à regex fornecida.

---

## Classe: CcpIfTheyAreArrayValuesSoEachOne
**Pacote:** `com.ccp.validations`
**Tipo:** classe
**Propósito:** Componente da API fluente de validação para campos cujos valores são arrays/listas. Verifica unicidade de itens, tipo dos elementos, tamanho da lista e pertencimento de cada elemento a conjuntos permitidos.

### Métodos:
- **CcpIfTheyAreArrayValuesSoEachOne(CcpJsonRepresentation content, String[] fields)** → construtor: Encapsula o JSON e os campos cujos valores são listas.
- **hasNonDuplicatedItems()** → `boolean`: Retorna `true` se nenhuma das listas possuir itens duplicados.
- **isOfTheType()** → `CcpAreOfTheType`: Retorna um `CcpAreOfTheType` para verificar o tipo de cada elemento das listas.
- **hasTheSizeThatIs()** → `CcpRangeSize`: Retorna um `CcpRangeSize` com o tamanho (quantidade de itens) de cada lista.
- **isTextAndItIsContainedAtTheList(String... args)** → `boolean`: Retorna `true` se cada elemento das listas estiver nos valores permitidos.
- **isTextAndItIsContainedAtTheList(Collection<T> args)** → `boolean`: Idem, aceitando coleção.
- **isNumberAndItIsContainedAtTheList(Double... args)** → `boolean`: Retorna `true` se cada elemento numérico das listas estiver nos valores permitidos.
- **isNumberAndItIsContainedAtTheList(Collection<Object> args)** → `boolean`: Idem, aceitando coleção.
- **isTextAndHasSizeThatIs()** → `CcpRangeSize`: Retorna um `CcpRangeSize` com o tamanho (em caracteres) de cada string nas listas.
- **isNumberAndItIs()** → `CcpRangeSize`: Retorna um `CcpRangeSize` com os valores numéricos de todos os elementos das listas.

---

## Classe: CcpItIsTrueThatTheFollowingFields
**Pacote:** `com.ccp.validations`
**Tipo:** classe
**Propósito:** Ponto de entrada da API fluente de validação de campos de um `CcpJsonRepresentation`. Filtra automaticamente os campos informados para considerar apenas os que existem no JSON, e fornece acesso às verificações de tipo, valores de array e comparações de range. Padrão de uso: `new CcpItIsTrueThatTheFollowingFields(json, fields).areAllOfTheType().longNumber()`.

### Métodos:
- **CcpItIsTrueThatTheFollowingFields(CcpJsonRepresentation content, String[] fields)** → construtor: Filtra `fields` para manter apenas os que existem em `content`.
- **areAllOfTheType()** → `CcpAreOfTheType`: Verifica se todos os campos são de um determinado tipo simples.
- **ifTheyAreAllArrayValuesThenEachOne()** → `CcpIfTheyAreArrayValuesSoEachOne`: Acessa verificações específicas para campos cujos valores são arrays.
- **ifTheyAreAll()** → `CcpIfTheyAre`: Acessa verificações para campos de valor simples.
- **getArrayProducerOfArrays(CcpJsonRepresentation content)** → `Function<String[], String[]>` (estático): Cria um produtor que, dado um array de nomes de campos, retorna os valores como string list achatada (todos os itens de todas as listas juntos). Usado internamente por `CcpAreOfTheType`.
- **getArrayProducerOfItems(CcpJsonRepresentation content)** → `Function<String[], String[]>` (estático): Cria um produtor que, dado um array de nomes de campos, retorna os valores de cada campo (campo a campo, não listas). Usado internamente por `CcpAreOfTheType` e `CcpIfTheyAre`.

---

## Classe: CcpRangeSize
**Pacote:** `com.ccp.validations`
**Tipo:** classe
**Propósito:** Componente terminal da API fluente de validação. Aplica uma comparação numérica (igual, maior, menor, etc.) sobre um conjunto de valores gerado pelo `arrayProducer` (que pode representar tamanhos de strings, valores numéricos de campos ou contagens de itens em listas). Retorna `true` somente se TODOS os valores satisfizerem a condição.

### Métodos:
- **CcpRangeSize(Function<String[], String[]> arrayProducer, String[] fields)** → construtor: Encapsula o produtor de valores e os campos.
- **equalsTo(Double y)** → `boolean`: Retorna `true` se todos os valores forem iguais a `y`.
- **equalsOrGreaterThan(Double y)** → `boolean`: Retorna `true` se todos os valores forem maiores ou iguais a `y`.
- **equalsOrLessThan(Double y)** → `boolean`: Retorna `true` se todos os valores forem menores ou iguais a `y`.
- **greaterThan(Double y)** → `boolean`: Retorna `true` se todos os valores forem estritamente maiores que `y`.
- **lessThan(Double y)** → `boolean`: Retorna `true` se todos os valores forem estritamente menores que `y`.


---

# Documentação do módulo ccp_commons_jobsnow — Parte 2

Cobre os pacotes `com.ccp.especifications.cache` e `com.ccp.especifications.db.bulk` / `db.crud`.

---

## Classe: CcpCache
**Pacote:** com.ccp.especifications.cache
**Tipo:** interface
**Propósito:** Contrato de acesso ao sistema de cache (ex.: GCP Memcache). Define operações básicas de leitura, escrita e remoção de valores em cache, identificados por chave string, com suporte a tempo de expiração e fallback automático quando a chave não está presente.

### Métodos:
- **get(String key)** → Object: Recupera o valor armazenado para a chave informada, retornando `null` se não existir.
- **get(String key, CcpJsonRepresentation json, Function\<CcpJsonRepresentation, V\> taskToGetValue, int cacheSeconds)** → V: Tenta recuperar o valor do cache pela chave; se não houver entrada (ou após deletá-la), executa `taskToGetValue` para obter o valor, grava o resultado no cache com o TTL informado e o retorna.
- **getOrDefault(String key, V defaultValue)** → V: Retorna o valor em cache para a chave ou, se ausente, retorna `defaultValue` sem lançar exceção.
- **getOrThrowException(String key, RuntimeException e)** → V: Retorna o valor em cache para a chave; se ausente, lança a exceção fornecida, forçando o chamador a tratar o caso de cache miss.
- **isPresent(String key)** → boolean: Verifica se existe algum valor associado à chave no cache, retornando `true` se presente.
- **put(String key, Object value, int secondsDelay)** → CcpCache: Armazena `value` no cache sob `key` com expiração em `secondsDelay` segundos; retorna a própria instância para encadeamento.
- **delete(String key)** → V: Remove a entrada da chave informada do cache e retorna o valor que estava armazenado (ou `null` se não havia).

---

## Classe: CcpCacheDecorator
**Pacote:** com.ccp.especifications.cache
**Tipo:** classe (final)
**Propósito:** Wrapper orientado a objetos sobre `CcpCache` que encapsula tanto a chave de cache quanto parâmetros contextuais (`CcpJsonRepresentation`). Simplifica o uso do cache nas camadas de negócio, permitindo construir chaves compostas de forma fluente a partir de entidades e identificadores, sem expor diretamente a interface `CcpCache`.

### Métodos:
- **CcpCacheDecorator(CcpBulkItem bulkItem)** → construtor: Cria o decorator a partir de um `CcpBulkItem`, derivando a chave cache do nome da entidade e do id do item.
- **CcpCacheDecorator(CcpEntity entity, String id)** → construtor: Cria o decorator com chave no formato `records.entity.<nome>.id.<id>`.
- **CcpCacheDecorator(String key)** → construtor: Cria o decorator diretamente com uma chave arbitrária.
- **get(Function\<CcpJsonRepresentation, V\> taskToGetValue, int cacheSeconds)** → V: Delega para `CcpCache.get(...)` usando a chave e parâmetros internos; executa `taskToGetValue` se o cache estiver vazio e armazena o resultado.
- **getOrDefault(V defaultValue)** → V: Retorna o valor em cache ou `defaultValue` caso ausente.
- **getOrThrowException(RuntimeException e)** → V: Retorna o valor em cache ou lança a exceção fornecida caso ausente.
- **isPresentInTheCache()** → boolean: Verifica se existe valor para a chave deste decorator no cache.
- **put(Object value, int secondsDelay)** → CcpCacheDecorator: Grava `value` no cache com TTL e retorna `this` para encadeamento.
- **delete()** → V: Remove e retorna o valor associado à chave deste decorator no cache.
- **incrementKey(String key, Object value)** → CcpCacheDecorator: Cria um novo decorator com chave estendida por `.<key>.<value>` e com o par adicionado ao JSON de parâmetros, permitindo chaves hierárquicas/compostas.
- **incrementKeys(CcpJsonRepresentation json, String... keys)** → CcpCacheDecorator: Extrai um subconjunto de campos do JSON fornecido e aplica `incrementKey` para cada um, retornando um novo decorator com chave acumulada.
- **incrementKeys(CcpJsonRepresentation jsonPiece)** → CcpCacheDecorator: Itera sobre todos os campos do JSON informado e aplica `incrementKey` para cada par campo/valor.
- **toString()** → String: Retorna a chave de cache atual do decorator.

---

## Classe: CcpBulkEntityOperationType
**Pacote:** com.ccp.especifications.db.bulk
**Tipo:** enum (implementa `CcpJsonFieldName`)
**Propósito:** Enumera os tipos de operação bulk possíveis sobre uma entidade do banco (`create`, `update`, `delete`, `noop`) e encapsula a lógica de reprocessamento automático para situações de conflito ou registro não encontrado — por exemplo, promovendo `create` para `update` em caso de conflito, e vice-versa.

### Constantes:
- **create** (prioridade 1): Operação de criação; em caso de conflito (registro já existe), converte automaticamente para `update`.
- **update** (prioridade 2, `createsVersionsToSameRecord = true`): Operação de atualização; em caso de registro não encontrado, converte automaticamente para `create`.
- **delete** (prioridade 3): Operação de exclusão; em caso de registro não encontrado, lança `CcpErrorBulkEntityRecordNotFound`.
- **noop** (prioridade 0): Operação nula/sem efeito; usada para marcar registros que existem mas não precisam ser alterados.

### Métodos:
- **getReprocess(Function\<CcpBulkOperationResult, CcpJsonRepresentation\> reprocessJsonProducer, CcpBulkOperationResult result, CcpEntity entityToReprocess)** → CcpBulkItem: Avalia o status retornado pela operação bulk; se o status não tem handler mapeado, gera um novo `CcpBulkItem` de criação via `reprocessJsonProducer`; caso contrário, aplica o handler correspondente (ex.: troca create↔update) e retorna o item reprocessado.

---

## Classe: CcpBulkErrors
**Pacote:** com.ccp.especifications.db.bulk
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando uma operação bulk retorna erros. Carrega a lista completa de registros que falharam serializada como string na mensagem da exceção, facilitando o diagnóstico em logs.

### Métodos:
- **CcpBulkErrors(List\<CcpJsonRepresentation\> failedRecords)** → construtor: Inicializa a exceção com a representação textual da lista de registros que falharam na operação bulk.

---

## Classe: CcpBulkExecutor
**Pacote:** com.ccp.especifications.db.bulk
**Tipo:** interface
**Propósito:** Contrato para acumulação e execução de operações bulk no banco de dados. Permite adicionar itens individualmente ou em lote, limpar o buffer e disparar a execução, retornando os resultados de cada operação.

### Métodos:
- **clearRecords()** → CcpBulkExecutor: Remove todos os itens acumulados no buffer da operação bulk, reiniciando o executor.
- **addRecord(CcpJsonRepresentation json, CcpBulkEntityOperationType operation, CcpEntity entity)** → CcpBulkExecutor: Converte o JSON e a entidade em `CcpBulkItem`s (via `entity.toBulkItems`) e adiciona cada um ao buffer; retorna `this` para encadeamento.
- **addRecord(CcpBulkItem bulkItem)** → CcpBulkExecutor: Adiciona um único `CcpBulkItem` já construído ao buffer do executor.
- **addRecords(List\<CcpBulkItem\> items)** → CcpBulkExecutor: Adiciona uma lista de `CcpBulkItem`s ao buffer, iterando e chamando `addRecord` para cada um.
- **addRecords(List\<CcpJsonRepresentation\> records, CcpBulkEntityOperationType operation, CcpEntity entity)** → CcpBulkExecutor: Adiciona múltiplos registros JSON ao buffer convertendo cada um conforme a operação e entidade informadas.
- **getBulkOperationResult()** → List\<CcpBulkOperationResult\>: Executa as operações bulk acumuladas e retorna a lista de resultados de cada item processado.

---

## Classe: CcpBulkItem
**Pacote:** com.ccp.especifications.db.bulk
**Tipo:** classe
**Propósito:** Representa um único item (registro) dentro de uma operação bulk, agregando o JSON do registro, o tipo de operação (`CcpBulkEntityOperationType`), a entidade de destino (`CcpEntity`) e o identificador calculado. Utilizado como unidade de trabalho no pipeline de bulk operations.

### Métodos:
- **CcpBulkItem(CcpBulkItem other, CcpBulkEntityOperationType operation)** → construtor: Cria um novo item copiando todos os campos de `other`, mas substituindo o tipo de operação — útil no reprocessamento (ex.: trocar `create` por `update`).
- **CcpBulkItem(CcpJsonRepresentation json, CcpBulkEntityOperationType operation, CcpEntity entity, String id)** → construtor: Cria um item com todos os campos explicitamente informados.
- **toString()** → String: Retorna representação textual contendo entity, operation e id do item, omitindo o JSON completo.
- **asMap()** → CcpJsonRepresentation: Serializa o item para um `CcpJsonRepresentation` contendo os campos `operation`, `entity` (nome), `json` e `id`.
- **hashCode()** → int: Calcula o hash baseado na combinação `entity + "_" + id`, garantindo unicidade por entidade e identificador.
- **equals(Object obj)** → boolean: Compara dois `CcpBulkItem` por entidade e id; retorna `false` se o objeto for de tipo diferente ou se entidade/id diferirem.

---

## Classe: CcpBulkOperationResult
**Pacote:** com.ccp.especifications.db.bulk
**Tipo:** interface
**Propósito:** Contrato que representa o resultado de uma operação bulk individual. Permite inspecionar sucesso/falha, obter detalhes de erro e disparar o reprocessamento adequado do item baseado no status HTTP retornado pelo banco.

### Métodos:
- **getErrorDetails()** → CcpJsonRepresentation: Retorna um JSON com os detalhes do erro ocorrido na operação bulk deste item.
- **getBulkItem()** → CcpBulkItem: Retorna o `CcpBulkItem` original que originou esta operação.
- **hasError()** → boolean: Indica se a operação resultou em erro.
- **status()** → int: Retorna o código de status HTTP da operação bulk (ex.: 200, 201, 404, 409).
- **getReprocess(Function\<CcpBulkOperationResult, CcpJsonRepresentation\> reprocessJsonMapper, CcpEntity reprocessEntity)** → CcpBulkItem: Delega para `CcpBulkEntityOperationType.getReprocess(...)` para determinar como reprocessar o item com base no status retornado.
- **getCacheKey()** → String: Gera e retorna a chave de cache correspondente ao `CcpBulkItem` deste resultado, usando `CcpCacheDecorator`.
- **statusAsJsonFieldName()** → CcpJsonFieldName: Converte o código de status numérico em um `CcpJsonFieldName`, permitindo usá-lo como chave para lookup de handlers no mapa de reprocessamento.

---

## Classe: CcpErrorBulkEntityRecordNotFound
**Pacote:** com.ccp.especifications.db.bulk
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando uma operação bulk de delete ou update tenta acessar um registro que não existe na entidade do banco de dados. A mensagem inclui o id e o nome da entidade, e opcionalmente os valores que compõem a chave primária.

### Métodos:
- **CcpErrorBulkEntityRecordNotFound(String entityName, String id)** → construtor: Cria a exceção informando diretamente o nome da entidade e o id não encontrado.
- **CcpErrorBulkEntityRecordNotFound(CcpEntity entity, CcpJsonRepresentation json)** → construtor: Cria a exceção calculando o id a partir do JSON e da entidade, incluindo na mensagem os valores dos campos da chave primária que foram usados para compor o id.

---

## Classe: CcpErrorBulkItemNotFound
**Pacote:** com.ccp.especifications.db.bulk
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando um `CcpBulkItem` específico não é localizado dentro de uma lista de resultados bulk. A mensagem inclui o id, o nome da entidade e a lista completa de resultados retornados, facilitando o diagnóstico.

### Métodos:
- **CcpErrorBulkItemNotFound(CcpBulkItem bulkItem, List\<CcpJsonRepresentation\> result)** → construtor: Inicializa a exceção com mensagem formatada contendo o id, a entidade e a lista completa de resultados onde o item deveria ter sido encontrado.

---

## Classe: CcpExecuteBulkOperation
**Pacote:** com.ccp.especifications.db.bulk
**Tipo:** interface
**Propósito:** Contrato de alto nível que combina uma busca `unionAll` no banco com a execução subsequente de operações bulk. Permite processar múltiplas entidades em uma única consulta e então enviar os itens resultantes para o bulk, orquestrando busca e escrita de forma integrada.

### Métodos:
- **executeSelectUnionAllThenExecuteBulkOperation(CcpJsonRepresentation json, Consumer\<String[]\> functionToDeleteKeysInTheCache, CcpHandleWithSearchResultsInTheEntity\<List\<CcpBulkItem\>\>... handlers)** → CcpSelectUnionAll: Extrai o conjunto de entidades a buscar a partir dos handlers, executa um `unionAll` para todas elas, aplica cada handler ao resultado (coletando os `CcpBulkItem`s a serem criados/atualizados/deletados) e dispara a operação bulk com todos os itens coletados; retorna o `CcpSelectUnionAll` com os dados do resultado da busca.
- **executeBulk(Collection\<CcpBulkItem\> items, Consumer\<String[]\> functionToDeleteKeysInTheCache)** → CcpExecuteBulkOperation: Executa efetivamente as operações bulk para a coleção de itens fornecida, delegando ao executor concreto e invalidando as chaves de cache necessárias.

---

## Classe: CcpBulkHandlerCreate
**Pacote:** com.ccp.especifications.db.bulk.handlers
**Tipo:** classe (implementa `CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>`)
**Propósito:** Handler bulk que implementa a lógica "criar apenas se não existir": se o registro já existe na entidade, não gera nenhum item bulk (lista vazia); se não existe, gera itens de `create`. Usado como bloco de construção no pipeline de bulk com `CcpExecuteBulkOperation`.

### Métodos:
- **CcpBulkHandlerCreate(CcpEntity mainEntity)** → construtor: Inicializa o handler com a entidade alvo.
- **whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation searchParameter, CcpJsonRepresentation recordFound)** → List\<CcpBulkItem\>: Retorna lista vazia, pois o registro já existe e não deve ser recriado.
- **whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation searchParameter)** → List\<CcpBulkItem\>: Converte o `searchParameter` em itens bulk de criação via `entity.toBulkItems(..., create)`.
- **getEntityToSearch()** → CcpEntity: Retorna a entidade associada a este handler.

---

## Classe: CcpBulkHandlerDelete
**Pacote:** com.ccp.especifications.db.bulk.handlers
**Tipo:** classe (implementa `CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>`)
**Propósito:** Handler bulk que implementa a lógica de exclusão: se o registro existe, gera itens de `delete`; se não existe, aplica uma função customizável (padrão: lista vazia). Permite tratar ausência do registro de forma configurável — ignorando silenciosamente ou lançando exceção.

### Métodos:
- **CcpBulkHandlerDelete(CcpEntity entityToDelete)** → construtor: Cria o handler com comportamento padrão ao não encontrar: lista vazia.
- **CcpBulkHandlerDelete(CcpEntity entityToDelete, Function\<CcpBulkItem, List\<CcpBulkItem\>\> whenRecordWasNotFoundInTheEntitySearch)** → construtor: Cria o handler com comportamento customizado ao não encontrar o registro.
- **whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound)** → List\<CcpBulkItem\>: Gera itens bulk de `delete` para a entidade configurada.
- **whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json)** → List\<CcpBulkItem\>: Aplica a função de "não encontrado" fornecida no construtor ao item bulk correspondente.
- **getEntityToSearch()** → CcpEntity: Retorna a entidade alvo de exclusão.

---

## Classe: CcpBulkHandlerRead
**Pacote:** com.ccp.especifications.db.bulk.handlers
**Tipo:** classe (implementa `CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>`)
**Propósito:** Handler bulk que implementa leitura sem modificação: se o registro existe, gera itens com operação `noop` (sem efeito); se não existe, aplica uma função customizável (padrão: lista vazia). Útil para incluir registros existentes em um lote sem alterá-los.

### Métodos:
- **CcpBulkHandlerRead(CcpEntity entityToRead)** → construtor: Cria o handler com comportamento padrão ao não encontrar: lista vazia.
- **CcpBulkHandlerRead(CcpEntity entityToRead, Function\<CcpBulkItem, List\<CcpBulkItem\>\> whenRecordWasNotFoundInTheEntitySearch)** → construtor: Cria o handler com comportamento customizado ao não encontrar o registro.
- **whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound)** → List\<CcpBulkItem\>: Gera itens bulk com operação `noop`, marcando o registro como "visto" sem alterar seu estado.
- **whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json)** → List\<CcpBulkItem\>: Aplica a função de "não encontrado" fornecida no construtor ao item bulk correspondente.
- **getEntityToSearch()** → CcpEntity: Retorna a entidade alvo de leitura.

---

## Classe: CcpBulkHandlerSave
**Pacote:** com.ccp.especifications.db.bulk.handlers
**Tipo:** classe (implementa `CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>`)
**Propósito:** Handler bulk que implementa a lógica de "upsert" inteligente: se o registro existe, gera itens de `update` mesclando os dados novos com os existentes (respeitando campos atualizáveis e versão); se não existe, gera itens de `create`. Respeita as regras de imutabilidade configuradas na entidade (`isNotAnUpdatableEntity`).

### Métodos:
- **CcpBulkHandlerSave(CcpEntity mainEntity)** → construtor: Inicializa o handler com a entidade alvo do upsert.
- **whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation searchParameter, CcpJsonRepresentation recordFound)** → List\<CcpBulkItem\>: Gera itens de `update`; para entidades imutáveis retorna `noop`; para entidades versionáveis, mescla os dados novos sobre os existentes preservando apenas os campos atualizáveis permitidos.
- **whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation searchParameter)** → List\<CcpBulkItem\>: Gera itens de `create` com os dados do `searchParameter`.
- **getEntityToSearch()** → CcpEntity: Retorna a entidade alvo.

---

## Classe: CcpEntityBulkHandlerSaveTwinEntity
**Pacote:** com.ccp.especifications.db.bulk.handlers
**Tipo:** classe (estende `CcpEntityBulkHandlerTransferRecordToTwinEntity`)
**Propósito:** Handler bulk especializado que salva (cria ou atualiza) um registro na "entidade wrapper" (wrapped entity) de uma entidade twin, sem mover o registro da entidade original. Difere de `CcpEntityBulkHandlerTransferRecordToTwinEntity` por não deletar o registro da entidade de origem.

### Métodos:
- **CcpEntityBulkHandlerSaveTwinEntity(CcpEntity entity)** → construtor: Inicializa com a entidade cujo wrapper receberá o registro.
- **whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound)** → List\<CcpBulkItem\>: Gera itens de `update` na entidade wrapper (wrapped entity).
- **whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json)** → List\<CcpBulkItem\>: Gera itens de `create` na entidade wrapper (wrapped entity).

---

## Classe: CcpEntityBulkHandlerTransferRecordToTwinEntity
**Pacote:** com.ccp.especifications.db.bulk.handlers
**Tipo:** classe (implementa `CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>`)
**Propósito:** Handler bulk que implementa a transferência atômica de um registro de uma entidade para sua entidade twin: se o registro é encontrado, gera um `create` na entidade twin e um `delete` na entidade de origem; se não é encontrado, não gera nenhuma operação. Usado para mover registros entre entidades espelhadas (twin pattern).

### Métodos:
- **CcpEntityBulkHandlerTransferRecordToTwinEntity(CcpEntity entity)** → construtor: Inicializa com a entidade de origem da transferência.
- **whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound)** → List\<CcpBulkItem\>: Gera um item `create` para a entidade twin e um item `delete` para a entidade de origem, efetuando a transferência em uma única operação bulk.
- **whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json)** → List\<CcpBulkItem\>: Retorna lista vazia, pois sem registro na origem não há o que transferir.
- **getEntityToSearch()** → CcpEntity: Retorna a entidade de origem da transferência.

---

## Classe: CcpCrud
**Pacote:** com.ccp.especifications.db.crud
**Tipo:** interface
**Propósito:** Contrato central de acesso ao banco de dados (Elasticsearch). Fornece operações CRUD básicas (busca por id, save, exists, delete) e o mecanismo de busca em múltiplas entidades simultaneamente via `unionAll`, com integração automática à invalidação de cache.

### Métodos:
- **getOneById(String entityName, String id)** → CcpJsonRepresentation: Recupera diretamente um registro do banco pelo nome da entidade e pelo id.
- **getUnionAllExecutor()** → CcpUnionAllExecutor: Retorna o executor responsável por realizar buscas `unionAll` (multi-entidade em batch).
- **unionAll(CcpJsonRepresentation[] jsons, Consumer\<String[]\> functionToDeleteKeysInTheCache, CcpEntity... entities)** → CcpSelectUnionAll: Invalida as chaves de cache relevantes e realiza uma busca em todas as entidades informadas para todos os JSONs fornecidos, retornando um `CcpSelectUnionAll` com os resultados condensados.
- **unionAll(CcpJsonRepresentation json, Consumer\<String[]\> functionToDeleteKeysInTheCache, CcpEntity... entities)** → CcpSelectUnionAll: Versão simplificada do `unionAll` para um único JSON de busca.
- **save(String entityName, CcpJsonRepresentation json, String id)** → CcpJsonRepresentation: Persiste o JSON informado na entidade/índice com o id fornecido e retorna o registro salvo.
- **exists(String entityName, String id)** → boolean: Verifica se existe um documento com o id informado na entidade/índice especificado.
- **delete(String entityName, String id)** → boolean: Remove o documento com o id informado da entidade/índice e retorna `true` se a exclusão foi bem-sucedida.
- **deleteKeysInCache(CcpJsonRepresentation[] jsons, Consumer\<String[]\> functionToDeleteKeysInTheCache, CcpEntity... entities)** → CcpCrud: Calcula as chaves de cache correspondentes a cada combinação entidade/json e as passa para a função de invalidação, retornando `this` para encadeamento.

---

## Classe: CcpErrorCrudMultiGetSearchFailed
**Pacote:** com.ccp.especifications.db.crud
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando uma operação de multi-get no banco de dados retorna um erro explícito. Extrai os campos `type` e `reason` do JSON de erro retornado pela API do banco (Elasticsearch) e os formata na mensagem da exceção.

### Métodos:
- **CcpErrorCrudMultiGetSearchFailed(CcpJsonRepresentation error)** → construtor: Inicializa a exceção com mensagem no formato `"<type>. Reason: <reason>"` extraída do JSON de erro.

---

## Classe: CcpErrorCrudMultiGetSearchUnfeasible
**Pacote:** com.ccp.especifications.db.crud
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando nenhum dos itens da coleção de JSONs fornecida conseguiu produzir um id válido para as entidades informadas, tornando a busca `multiGet` inviável. A mensagem lista as entidades e os JSONs usados na tentativa.

### Métodos:
- **CcpErrorCrudMultiGetSearchUnfeasible(Collection\<CcpJsonRepresentation\> jsons, CcpEntity... entities)** → construtor: Inicializa a exceção com mensagem detalhada contendo os metadados das entidades e os JSONs que não produziram ids válidos.

---

## Classe: CcpErrorFlowFieldsToReturnNotMentioned
**Pacote:** com.ccp.especifications.db.crud
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada no encerramento de um fluxo de busca (`CcpSelectFinally`) quando nenhum campo de retorno foi especificado, o que tornaria o resultado vazio e provavelmente indicaria um erro de programação.

### Métodos:
- **CcpErrorFlowFieldsToReturnNotMentioned(String origin)** → construtor: Inicializa a exceção com mensagem indicando a origem do fluxo que não definiu campos de retorno.

---

## Classe: CcpGetEntityId
**Pacote:** com.ccp.especifications.db.crud
**Tipo:** classe
**Propósito:** Ponto de entrada do fluent API de busca. Recebe um ou mais `CcpJsonRepresentation` como parâmetros de busca e inicia a construção de um `CcpSelectProcedure`, permitindo encadear condições de busca de forma legível.

### Métodos:
- **CcpGetEntityId(CcpJsonRepresentation... parametersToSearch)** → construtor: Recebe os parâmetros de busca (JSONs) que serão usados nas consultas ao banco ao longo do fluent chain.
- **toBeginProcedureAnd()** → CcpSelectProcedure: Inicializa e retorna um `CcpSelectProcedure` com os parâmetros de busca fornecidos e um JSON de statements vazio, dando início ao encadeamento de condições de busca.

---

## Classe: CcpHandleWithSearchResultsInTheEntity
**Pacote:** com.ccp.especifications.db.crud
**Tipo:** interface (genérica `<T>`)
**Propósito:** Contrato de callback para tratamento de resultados de busca em uma entidade. Define dois caminhos — registro encontrado e registro não encontrado — e identifica a entidade alvo da busca. É o mecanismo central do padrão "found/not found" utilizado nos handlers bulk e no `CcpSelectUnionAll`.

### Métodos:
- **whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation searchParameter, CcpJsonRepresentation recordFound)** → T: Chamado quando o registro correspondente ao `searchParameter` foi encontrado na entidade; recebe também os dados do registro encontrado.
- **whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation searchParameter)** → T: Chamado quando o registro correspondente ao `searchParameter` não foi encontrado na entidade.
- **getEntityToSearch()** → CcpEntity: Retorna a entidade (`CcpEntity`) sobre a qual a busca deve ser realizada.

---

## Classe: CcpSelectFinally
**Pacote:** com.ccp.especifications.db.crud
**Tipo:** classe
**Propósito:** Etapa final do fluent chain de busca. Executa todos os statements acumulados (verificações de presença em entidades, ações condicionais e lançamento de status de erro), coordena a busca `unionAll`, aplica as regras do fluxo e retorna apenas os campos especificados no resultado final. Lança `CcpErrorFlowDisturb` quando uma condição de fluxo não é satisfeita.

### Métodos:
- **endThisProcedure(String context, CcpBusiness whenFlowError, CcpBusiness whenFlowSuccess, Consumer\<String[]\> functionToDeleteKeysInTheCache)** → CcpSelectFinally: Executa o fluxo completo de busca e validação de statements, disparando `whenFlowError` ou `whenFlowSuccess` conforme o resultado; retorna `this` (resultado descartado — uso para efeito colateral).
- **endThisProcedureRetrievingTheResultingData(String context, CcpBusiness whenFlowError, CcpBusiness whenFlowSuccess, Consumer\<String[]\> functionToDeleteKeysInTheCache)** → CcpJsonRepresentation: Igual ao anterior, mas retorna o `CcpJsonRepresentation` resultante contendo apenas os campos declarados no `andFinally(fields)`.

---

## Classe: CcpSelectFoundInEntity
**Pacote:** com.ccp.especifications.db.crud
**Tipo:** classe
**Propósito:** Etapa intermediária do fluent chain gerada por `CcpSelectProcedure.ifThisIdIsPresentInEntity(...)` ou `ifThisIdIsNotPresentInEntity(...)`. Permite definir o que fazer quando a condição de presença for satisfeita: executar uma ação de negócio ou retornar um status de processo.

### Métodos:
- **executeAction(CcpBusiness action)** → CcpSelectNextStep: Adiciona ao statement atual a ação de negócio a ser executada quando a condição (encontrado/não encontrado) se confirmar; avança para o próximo passo do fluent chain.
- **returnStatus(CcpProcessStatus status)** → CcpSelectNextStep: Adiciona ao statement atual o status de processo a ser retornado/lançado quando a condição se confirmar; avança para o próximo passo do fluent chain.

---

## Classe: CcpSelectLoadDataFromEntity
**Pacote:** com.ccp.especifications.db.crud
**Tipo:** classe
**Propósito:** Etapa intermediária do fluent chain gerada por `CcpSelectProcedure.loadThisIdFromEntity(...)`. Representa o ponto após declarar que os dados de uma entidade devem ser carregados, oferecendo duas saídas: adicionar mais condições ao fluxo ou finalizar declarando os campos de retorno.

### Métodos:
- **and()** → CcpSelectProcedure: Retorna ao `CcpSelectProcedure` para adicionar mais condições ou entidades ao fluxo de busca.
- **andFinally(String... fields)** → CcpSelectFinally: Encerra a declaração de statements e inicia a etapa final (`CcpSelectFinally`) especificando quais campos devem compor o resultado retornado.

---

## Classe: CcpSelectNextStep
**Pacote:** com.ccp.especifications.db.crud
**Tipo:** classe
**Propósito:** Etapa intermediária do fluent chain que aparece após a definição de uma ação ou status em um statement. Permite continuar adicionando condições ao fluxo ou encerrar a cadeia especificando os campos de retorno.

### Métodos:
- **andFinallyReturningTheseFields(Collection\<String\> fields)** → CcpSelectFinally: Encerra o chain convertendo a coleção de nomes de campos para array e criando o `CcpSelectFinally`.
- **andFinallyReturningTheseFields(String... fields)** → CcpSelectFinally: Versão varargs do método acima; encerra o chain criando o `CcpSelectFinally` com os campos informados.
- **and()** → CcpSelectProcedure: Retorna ao `CcpSelectProcedure` para adicionar mais condições ao fluxo sem encerrá-lo.

---

## Classe: CcpSelectProcedure
**Pacote:** com.ccp.especifications.db.crud
**Tipo:** classe
**Propósito:** Núcleo do fluent API de busca. Permite declarar de forma encadeada e legível quais entidades consultar, quais condições de presença verificar e quais ações executar em cada caso. Cada chamada adiciona um statement à lista interna, que será processada em bloco pelo `CcpSelectFinally`.

### Métodos:
- **loadThisIdFromEntity(CcpEntity entity)** → CcpSelectLoadDataFromEntity: Adiciona um statement para carregar os dados do id (calculado a partir dos parâmetros de busca) na entidade informada; retorna a etapa de continuação correspondente.
- **ifThisIdIsPresentInEntity(CcpEntity entity)** → CcpSelectFoundInEntity: Adiciona um statement condicional com `found = true`; o bloco seguinte define o que fazer se o registro **existir** na entidade.
- **ifThisIdIsNotPresentInEntity(CcpEntity entity)** → CcpSelectFoundInEntity: Adiciona um statement condicional com `found = false`; o bloco seguinte define o que fazer se o registro **não existir** na entidade.
- **executeAction(CcpBusiness action)** → CcpSelectNextStep: Adiciona um statement de ação livre (não vinculado a presença em entidade) que será executado sempre, recebendo o JSON acumulado até aquele ponto.

---

## Classe: CcpSelectUnionAll
**Pacote:** com.ccp.especifications.db.crud
**Tipo:** classe
**Propósito:** Representa o resultado condensado de uma busca `unionAll` — múltiplas entidades buscadas em uma única chamada ao banco. Internamente organiza os resultados em um mapa aninhado `{ entidade → { id → dadosDoRegistro } }` e provê métodos para verificar presença e recuperar dados de entidades específicas.

### Métodos:
- **CcpSelectUnionAll(CcpJsonRepresentation[] searchParameters, List\<CcpJsonRepresentation\> results, CcpEntity... entities)** → construtor: Constrói o objeto condensado a partir dos parâmetros de busca, dos resultados brutos retornados pelo banco e das entidades consultadas, organizando os dados no mapa interno e incluindo o `explainedSearch` (chave primária explicada por entidade e id).
- **isPresent(String entityName, String id)** → boolean: Verifica se existe um registro com o id informado para a entidade de nome `entityName` dentro do resultado condensado.
- **handleRecordInUnionAll(CcpJsonRepresentation searchParameter, CcpHandleWithSearchResultsInTheEntity\<T\> handler)** → T: Verifica se o registro está presente e delega para o callback adequado do handler (`whenFound` ou `whenNotFound`), passando o registro encontrado quando aplicável.
- **getEntityRow(String index, String id)** → CcpJsonRepresentation: Retorna os dados de um registro específico por nome de índice e id; retorna JSON vazio se não encontrado.
- **toString()** → String: Retorna a representação textual do mapa condensado.
- **getEntityRows(CcpEntity entity)** → List\<CcpJsonRepresentation\>: Retorna todos os registros encontrados para a entidade informada como lista de JSONs, adicionando o campo de id a cada um.

---

## Classe: CcpUnionAllExecutor
**Pacote:** com.ccp.especifications.db.crud
**Tipo:** interface
**Propósito:** Contrato de baixo nível para execução de buscas `unionAll` no banco de dados. Recebe uma coleção de JSONs de busca e as entidades alvo, realizando a consulta em lote e retornando o resultado encapsulado em `CcpSelectUnionAll`.

### Métodos:
- **unionAll(Collection\<CcpJsonRepresentation\> values, CcpEntity... entities)** → CcpSelectUnionAll: Executa a busca multi-entidade/multi-registro no banco de dados e retorna o objeto `CcpSelectUnionAll` com os resultados condensados.

---

## Classe: FunctionPutEntity
**Pacote:** com.ccp.especifications.db.crud
**Tipo:** classe (implementa `CcpBusiness`; singleton package-private)
**Propósito:** Transformação utilitária usada internamente pelo `CcpSelectFinally` para serializar objetos `CcpEntity` presentes em um JSON: substitui a referência ao objeto `CcpEntity` pelo nome textual da entidade (`entityName`), tornando o JSON serializável para logs e respostas de erro.

### Métodos:
- **apply(CcpJsonRepresentation j)** → CcpJsonRepresentation: Extrai o objeto `CcpEntity` do campo `entity` no JSON, obtém seu `entityName` e substitui o campo pelo nome textual, retornando o JSON modificado.

---

## Classe: FunctionPutStatus
**Pacote:** com.ccp.especifications.db.crud
**Tipo:** classe (implementa `CcpBusiness`; singleton package-private)
**Propósito:** Transformação utilitária usada internamente pelo `CcpSelectFinally` para serializar objetos `CcpProcessStatus` presentes em um JSON: substitui a referência ao objeto `status` pelo nome textual (`statusName`) e pelo número correspondente (`statusNumber`), tornando o JSON serializável para logs e respostas de erro.

### Métodos:
- **apply(CcpJsonRepresentation j)** → CcpJsonRepresentation: Extrai o objeto `CcpProcessStatus` do campo `status`, obtém seu nome e número, adiciona os campos `statusName` e `statusNumber` ao JSON e remove o campo `status` original, retornando o JSON transformado.


---

# Documentação do Módulo ccp_commons_jobsnow — Parte 3
## Pacotes: db/query e db/utils/entity

---

## Classe: BucketAggregation
**Pacote:** com.ccp.especifications.db.query
**Tipo:** classe (final)
**Propósito:** Representa um bucket de agregação do Elasticsearch (terms ou histogram) dentro do builder fluent de queries. Permite configurar um agrupamento por campo e tamanho, e encerrar voltando ao nó pai de agregações.

### Métodos:
- **endTermsBuckedAndBackToAggregations()** → CcpQueryAggregations: Finaliza o bucket como agregação do tipo `terms` (agrupamento por valor exato) e retorna ao contexto pai de agregações.
- **endHistogramBuckedAndBackToAggregations()** → CcpQueryAggregations: Finaliza o bucket como agregação do tipo `histogram` (agrupamento por intervalo numérico) e retorna ao contexto pai de agregações.
- **startAggregations()** → CcpQueryAggregations: Inicia sub-agregações aninhadas dentro deste bucket.
- **getInstanceCopy()** → BucketAggregation (protected): Retorna uma cópia desta instância preservando os mesmos parâmetros (parent, name, fieldName, size); usado internamente pelo mecanismo de cópia imutável do builder.

---

## Classe: CcpQuery
**Pacote:** com.ccp.especifications.db.query
**Tipo:** classe (final)
**Propósito:** Representa o nó `query` dentro do builder fluent de queries do Elasticsearch. Serve como ponto de entrada para construir o bloco de consulta principal de uma requisição.

### Métodos:
- **startBool()** → CcpQueryBool: Inicia um bloco de query booleana (`bool`) dentro do nó `query`.
- **endQueryAndBackToRequest()** → CcpQueryOptions: Finaliza o nó `query` e retorna ao nó raiz da requisição (CcpQueryOptions).
- **getInstanceCopy()** → CcpQuery (protected): Retorna uma cópia desta instância; usado pelo mecanismo de cópia imutável do builder.

---

## Classe: CcpQueryAggregations
**Pacote:** com.ccp.especifications.db.query
**Tipo:** classe (final)
**Propósito:** Representa o nó `aggs` (agregações) no builder fluent de queries do Elasticsearch. Permite adicionar agregações métricas (min, max, avg, sum) e iniciar buckets (agrupamentos).

### Métodos:
- **endAggregationsAndBackToRequest()** → CcpQueryOptions: Finaliza o bloco de agregações e retorna ao nó raiz da requisição.
- **endAggregationsAndBackToBucket()** → BucketAggregation: Finaliza o bloco de agregações e retorna ao contexto de bucket pai.
- **addMinAggregation(String aggregationName, CcpEntityField fieldName)** → CcpQueryAggregations: Adiciona uma agregação de valor mínimo (`min`) sobre o campo informado.
- **addMaxAggregation(String aggregationName, CcpEntityField fieldName)** → CcpQueryAggregations: Adiciona uma agregação de valor máximo (`max`) sobre o campo informado.
- **addAvgAggregation(String aggregationName, CcpEntityField fieldName)** → CcpQueryAggregations: Adiciona uma agregação de média (`avg`) sobre o campo informado.
- **addSumAggregation(String aggregationName, CcpEntityField fieldName)** → CcpQueryAggregations: Adiciona uma agregação de soma (`sum`) sobre o campo informado.
- **startBucket(String bucketName, CcpEntityField fieldName, long size)** → BucketAggregation: Inicia um bucket de agregação (terms ou histogram) com nome, campo e tamanho definidos.
- **getInstanceCopy()** → CcpQueryAggregations (protected): Retorna uma cópia desta instância; usado pelo mecanismo de cópia imutável do builder.

---

## Classe: CcpQueryBool
**Pacote:** com.ccp.especifications.db.query
**Tipo:** classe (final)
**Propósito:** Representa o nó `bool` dentro de uma query booleana do Elasticsearch. É o ponto central de composição de filtros booleanos, permitindo criar cláusulas `filter`, `must`, `should`, `must_not` e `should_not`.

### Métodos:
- **startFilter()** → CcpQueryFilter: Inicia o bloco `filter` dentro do bool.
- **startMust()** → CcpQueryMust: Inicia o bloco `must` (condições obrigatórias que devem ser satisfeitas).
- **startShould(int minimumShouldMatch)** → CcpQueryShould: Inicia o bloco `should` (condições opcionais) com mínimo de correspondências necessárias.
- **startMustNot()** → CcpQueryMustNot: Inicia o bloco `must_not` (condições que não devem ser satisfeitas).
- **startShouldNot()** → CcpQueryShouldNot: Inicia o bloco `should_not` (condições opcionais negadas).
- **endBoolAndBackToShould()** → CcpQueryShould: Finaliza este bool e retorna ao contexto `should` pai.
- **endBoolAndBackToMust()** → CcpQueryMust: Finaliza este bool e retorna ao contexto `must` pai.
- **endBoolAndBackToShouldNot()** → CcpQueryShouldNot: Finaliza este bool e retorna ao contexto `should_not` pai.
- **endBoolAndBackToMustNot()** → CcpQueryMustNot: Finaliza este bool e retorna ao contexto `must_not` pai.
- **endBoolAndBackToFilter()** → CcpQueryFilter: Finaliza este bool e retorna ao contexto `filter` pai.
- **endBoolAndBackToQuery()** → CcpQuery: Finaliza este bool e retorna ao nó `query` pai.
- **getInstanceCopy()** → CcpQueryBool (protected): Retorna uma cópia desta instância; usado pelo mecanismo de cópia imutável do builder.

---

## Classe: CcpQueryBooleanOperator
**Pacote:** com.ccp.especifications.db.query
**Tipo:** classe abstrata
**Propósito:** Base abstrata para todos os operadores booleanos de query do Elasticsearch (`must`, `should`, `filter`, `must_not`, `should_not`). Gerencia a coleção de condições e fornece métodos genéricos para adicionar diferentes tipos de filtro.

### Métodos:
- **term(CcpJsonFieldName field, Object value)** → T: Adiciona uma condição de igualdade exata (`term`) ao operador.
- **terms(CcpJsonFieldName field, Object value)** → T: Adiciona uma condição de múltiplos valores exatos (`terms`) ao operador.
- **prefix(CcpEntityField field, Object value)** → T: Adiciona uma condição de prefixo de string (`prefix`) ao operador.
- **match(CcpJsonFieldName field, Object value)** → T: Adiciona uma condição de correspondência textual simples (`match`) ao operador.
- **matchPhrase(CcpEntityField field, Object value)** → T: Adiciona uma condição de correspondência de frase (`match_phrase`) ao operador.
- **match(CcpEntityField field, Object value, double boost, String operator)** → T: Adiciona uma condição `match` com boost de relevância e operador lógico (AND/OR).
- **matchPhrase(CcpEntityField field, Object value, double boost)** → T: Adiciona uma condição `match_phrase` com boost de relevância.
- **exists(String field)** → T: Adiciona uma condição de existência de campo (`exists`) ao operador.
- **addCondition(String field, Object value, String key)** → T (protected): Método interno que constrói e adiciona uma condição simples à lista de itens do operador.
- **addCondition(String field, Object value, String key, double boost, String operator)** → T (protected): Método interno que constrói e adiciona uma condição com boost e operador à lista.
- **getValue()** → Object: Retorna a lista de condições acumuladas como um ArrayList, para serialização.
- **addChild(CcpQueryComponent child)** → T: Incorpora um componente filho (ex: um `bool` aninhado) na lista de itens deste operador.
- **copy()** → T (protected): Produz uma cópia profunda deste operador preservando parent, name e items; usado para manter imutabilidade no builder.
- **startRange()** → CcpQueryRange: Inicia um bloco de condição de intervalo (`range`) dentro deste operador.
- **hasChildreen()** → boolean: Retorna `true` se existem condições registradas neste operador.

---

## Classe: CcpQueryComponent
**Pacote:** com.ccp.especifications.db.query
**Tipo:** classe abstrata
**Propósito:** Classe base de todos os nós do builder fluent de queries do Elasticsearch. Define a estrutura de árvore (parent/child), o conteúdo JSON do nó e o mecanismo de cópia imutável que garante que cada operação retorne uma nova instância sem alterar o estado original.

### Métodos:
- **getInstanceCopy()** → T (abstract, protected): Cada subclasse implementa este método para retornar uma nova instância de si mesma com os mesmos parâmetros construtores; mecanismo central do padrão imutável.
- **getValue()** → Object: Retorna o conteúdo interno (`json.content`) do nó para uso na serialização.
- **addChild(CcpQueryComponent child)** → T: Cria uma cópia deste nó e adiciona o valor do filho sob a chave `child.name`; permite compor a árvore de forma imutável.
- **copy()** → T (protected): Produz uma cópia profunda deste nó (name, parent, json), usada como base para todas as modificações do builder.
- **toString()** → String: Serializa o valor deste nó para JSON usando o `CcpJsonHandler` injetado via DI.
- **hasChildreen()** → boolean: Retorna `true` se o JSON interno deste nó contém algum campo.
- **putProperty(String propertyName, Object propertyValue)** → T: Adiciona ou sobrescreve uma propriedade no JSON interno do nó e retorna uma cópia modificada.

---

## Interface: CcpQueryExecutor
**Pacote:** com.ccp.especifications.db.query
**Tipo:** interface
**Propósito:** Contrato de execução de queries no Elasticsearch. Define todas as operações de consulta que podem ser realizadas com base em um `CcpQueryOptions` (a query construída) e os nomes dos índices alvo.

### Métodos:
- **getTermsStatis(CcpQueryOptions elasticQuery, String[] resourcesNames, String fieldName)** → CcpJsonRepresentation: Executa uma agregação de termos e retorna estatísticas dos valores do campo informado.
- **delete(CcpQueryOptions elasticQuery, String... resourcesNames)** → CcpJsonRepresentation: Apaga todos os documentos que correspondam à query nos índices informados.
- **update(CcpQueryOptions elasticQuery, String[] resourcesNames, CcpJsonRepresentation newValues)** → CcpJsonRepresentation: Atualiza os documentos correspondentes à query com os novos valores fornecidos.
- **consumeQueryResult(CcpQueryOptions, String[], String scrollTime, Long size, Consumer<List<CcpJsonRepresentation>>, String... fields)** → CcpQueryExecutor: Itera sobre os resultados da query em lotes usando scroll, passando cada lote para o consumer informado.
- **total(CcpQueryOptions elasticQuery, String[] resourcesNames)** → long: Retorna o total de documentos que correspondem à query.
- **getResultAsList(CcpQueryOptions elasticQuery, String[] resourcesNames, String... fieldsToSearch)** → List&lt;CcpJsonRepresentation&gt;: Retorna os resultados da query como uma lista de documentos JSON.
- **getResultAsMap(CcpQueryOptions elasticQuery, String[] resourcesNames, String field)** → CcpJsonRepresentation: Retorna os resultados agrupados como um mapa indexado pelo campo informado.
- **getResultAsPackage(String url, CcpHttpMethods method, int expectedStatus, CcpQueryOptions elasticQuery, String[] resourcesNames, String... array)** → CcpJsonRepresentation: Executa a query via uma requisição HTTP personalizada e retorna o resultado bruto.
- **getMap(CcpQueryOptions elasticQuery, String[] resourcesNames, String field)** → CcpJsonRepresentation: Variante de `getResultAsMap` com semântica ligeiramente diferente de agrupamento.
- **getAggregations(CcpQueryOptions elasticQuery, String... resourcesNames)** → CcpJsonRepresentation: Executa a query e retorna apenas o bloco de agregações do resultado.
- **consumeQueryResult(CcpQueryOptions, String[], String scrollTime, Integer size, Consumer<CcpJsonRepresentation>, String... fields)** → CcpQueryExecutor: Variante de scroll que entrega um documento por vez ao consumer (em vez de listas).

---

## Classe: CcpQueryExecutorDecorator
**Pacote:** com.ccp.especifications.db.query
**Tipo:** classe
**Propósito:** Decorator sobre `CcpQueryExecutor` que captura a query e os nomes de índices no construtor, simplificando as chamadas ao executor real. Cada método delega ao `CcpQueryExecutor` injetado via DI sem precisar que o chamador repasse esses parâmetros repetidamente.

### Métodos:
- **getResultAsPackage(String url, CcpHttpMethods method, int expectedStatus, String... array)** → CcpJsonRepresentation: Executa a query via HTTP personalizado com os índices e query pré-configurados.
- **getTermsStatis(String fieldName)** → CcpJsonRepresentation: Delega para `CcpQueryExecutor.getTermsStatis` com os parâmetros fixados.
- **delete()** → CcpJsonRepresentation: Delega para `CcpQueryExecutor.delete` com os parâmetros fixados.
- **update(CcpJsonRepresentation newValues)** → CcpJsonRepresentation: Delega para `CcpQueryExecutor.update` com os parâmetros fixados.
- **consumeQueryResult(String scrollTime, int size, Consumer&lt;CcpJsonRepresentation&gt; consumer, String... fields)** → CcpQueryExecutorDecorator: Itera resultados via scroll entregando um documento por vez ao consumer; retorna `this` para encadeamento.
- **total()** → long: Retorna o total de documentos que correspondem à query pré-configurada.
- **getResultAsList(String... fieldsToSearch)** → List&lt;CcpJsonRepresentation&gt;: Retorna os resultados como lista de documentos JSON.
- **getResultAsMap(String field)** → CcpJsonRepresentation: Retorna resultados agrupados como mapa indexado pelo campo informado.
- **getMap(String field)** → CcpJsonRepresentation: Variante de agrupamento de resultados.
- **getAggregations()** → CcpJsonRepresentation: Retorna apenas o bloco de agregações do resultado da query.

---

	
**Pacote:** com.ccp.especifications.db.query
**Tipo:** classe
**Propósito:** Representa as condições de intervalo para um campo específico dentro de um bloco `range` do Elasticsearch. Permite encadear operadores de comparação (`lt`, `lte`, `gt`, `gte`) de forma fluent.

### Métodos:
- **lessThan(Object value)** → CcpQueryFieldRange: Adiciona a condição `lt` (menor que) ao intervalo do campo.
- **lessThanEquals(Object value)** → CcpQueryFieldRange: Adiciona a condição `lte` (menor ou igual a) ao intervalo do campo.
- **greaterThan(Object value)** → CcpQueryFieldRange: Adiciona a condição `gt` (maior que) ao intervalo do campo.
- **greaterThanEquals(Object value)** → CcpQueryFieldRange: Adiciona a condição `gte` (maior ou igual a) ao intervalo do campo.
- **endFieldRangeAndBackToRange()** → CcpQueryRange: Finaliza a definição do intervalo deste campo e retorna ao nó `range` pai.
- **getInstanceCopy()** → CcpQueryFieldRange (protected): Retorna uma cópia desta instância para o mecanismo imutável do builder.

---

## Classe: CcpQueryFilter
**Pacote:** com.ccp.especifications.db.query
**Tipo:** classe (final)
**Propósito:** Representa o nó `filter` dentro de uma query booleana do Elasticsearch. Diferentemente de `must`, as condições de filtro não afetam a pontuação de relevância dos documentos.

### Métodos:
- **startBool()** → CcpQueryBool: Inicia um bloco `bool` aninhado dentro do `filter`.
- **endFilterAndBackToBool()** → CcpQueryBool: Finaliza o `filter` e retorna ao nó `bool` pai.
- **getInstanceCopy()** → CcpQueryFilter (protected): Retorna uma cópia desta instância para o mecanismo imutável do builder.

---

## Classe: CcpQueryMust
**Pacote:** com.ccp.especifications.db.query
**Tipo:** classe (final)
**Propósito:** Representa o nó `must` dentro de uma query booleana do Elasticsearch. As condições adicionadas aqui são obrigatórias e impactam a pontuação de relevância dos documentos retornados.

### Métodos:
- **endMustAndBackToBool()** → CcpQueryBool: Finaliza o bloco `must` e retorna ao nó `bool` pai.
- **matchPhrase(CcpEntityField field, Object value)** → CcpQueryMust: Adiciona condição `match_phrase` ao `must`.
- **prefix(CcpEntityField field, Object value)** → CcpQueryMust: Adiciona condição de prefixo ao `must`.
- **term(CcpJsonFieldName field, Object value)** → CcpQueryMust: Adiciona condição de igualdade exata (`term`) ao `must`.
- **terms(CcpJsonFieldName field, Object value)** → CcpQueryMust: Adiciona condição de múltiplos valores exatos (`terms`) ao `must`.
- **exists(String field)** → CcpQueryMust: Adiciona condição de existência de campo ao `must`.
- **startBool()** → CcpQueryBool: Inicia um bloco `bool` aninhado dentro do `must`.
- **match(CcpEntityField field, Object value)** → CcpQueryMust: Adiciona condição de correspondência textual (`match`) ao `must`.
- **getInstanceCopy()** → CcpQueryMust (protected): Retorna uma cópia desta instância para o mecanismo imutável do builder.

---

## Classe: CcpQueryMustNot
**Pacote:** com.ccp.especifications.db.query
**Tipo:** classe (final)
**Propósito:** Representa o nó `must_not` dentro de uma query booleana do Elasticsearch. As condições aqui presentes excluem documentos que as satisfaçam.

### Métodos:
- **endMustNotAndBackToBool()** → CcpQueryBool: Finaliza o bloco `must_not` e retorna ao nó `bool` pai.
- **prefix(CcpEntityField field, Object value)** → CcpQueryMustNot: Adiciona condição de prefixo ao `must_not`.
- **matchPhrase(CcpEntityField field, Object value)** → CcpQueryMustNot: Adiciona condição `match_phrase` ao `must_not`.
- **term(CcpEntityField field, Object value)** → CcpQueryMustNot: Adiciona condição de igualdade exata ao `must_not`.
- **exists(String field)** → CcpQueryMustNot: Adiciona condição de existência de campo ao `must_not`.
- **startBool()** → CcpQueryBool: Inicia um bloco `bool` aninhado dentro do `must_not`.
- **getInstanceCopy()** → CcpQueryMustNot (protected): Retorna uma cópia desta instância para o mecanismo imutável do builder.

---

## Classe: CcpQueryOptions
**Pacote:** com.ccp.especifications.db.query
**Tipo:** classe
**Propósito:** Nó raiz do builder fluent de queries do Elasticsearch. Ponto de entrada único para iniciar a construção de uma query, configurar paginação, ordenação e executar a consulta nos índices desejados. Possui uma instância singleton `INSTANCE` para uso como ponto de partida.

### Métodos:
- **startQuery()** → CcpQuery: Inicia o bloco `query` principal da requisição.
- **startSimplifiedQuery()** → CcpQuerySimplifiedQuery: Inicia uma query simplificada sem o wrapper bool/filter.
- **startAggregations()** → CcpQueryAggregations: Inicia o bloco de agregações da requisição.
- **addAscSorting(String fields)** → CcpQueryOptions: Adiciona ordenação ascendente pelo(s) campo(s) informado(s).
- **addDescSorting(String... fields)** → CcpQueryOptions: Adiciona ordenação descendente pelo(s) campo(s) informado(s).
- **addSorting(String sortType, String... fields)** → CcpQueryOptions: Adiciona ordenação com tipo customizado (asc/desc) para múltiplos campos.
- **selectFrom(String... resourcesNames)** → CcpQueryExecutorDecorator: Associa esta query a índices pelo nome (string) e retorna um executor pronto para uso.
- **selectFrom(CcpEntity... entities)** → CcpQueryExecutorDecorator: Associa esta query a índices extraídos dos metadados das entidades informadas.
- **setScrollId(String scrollId)** → CcpQueryOptions: Define o ID de scroll para continuar uma iteração paginada.
- **setSize(int size)** → CcpQueryOptions: Define o número máximo de documentos retornados.
- **maxResults()** → CcpQueryOptions: Define o tamanho máximo como 10.000 documentos.
- **zeroResults()** → CcpQueryOptions: Define o tamanho como 0 (útil para consultas que retornam apenas metadados ou agregações).
- **setFrom(int from)** → CcpQueryOptions: Define o offset (paginação por deslocamento) dos resultados.
- **setScrollTime(String scrollTime)** → CcpQueryOptions: Define o tempo de expiração do contexto de scroll (ex: "1m", "5m").
- **matchAll()** → CcpQueryOptions: Adiciona uma cláusula `match_all` que retorna todos os documentos sem filtro.
- **getInstanceCopy()** → CcpQueryOptions (protected): Retorna uma nova instância vazia de `CcpQueryOptions`; usado pelo mecanismo imutável.

---

## Classe: CcpQueryRange
**Pacote:** com.ccp.especifications.db.query
**Tipo:** classe
**Propósito:** Representa o nó `range` no builder de queries do Elasticsearch. Serve como contêiner para definições de intervalo de um ou mais campos, permitindo retornar ao contexto pai correto após a definição.

### Métodos:
- **startFieldRange(String fieldName)** → CcpQueryFieldRange: Inicia a definição de intervalo para um campo específico.
- **endRangeAndBackToSimplifiedQuery()** → CcpQuerySimplifiedQuery: Finaliza o range e retorna ao contexto de query simplificada.
- **endRangeAndBackToShould()** → CcpQueryShould: Finaliza o range e retorna ao contexto `should`.
- **endRangeAndBackToMust()** → CcpQueryMust: Finaliza o range e retorna ao contexto `must`.
- **endRangeAndBackToShouldNot()** → CcpQueryShouldNot: Finaliza o range e retorna ao contexto `should_not`.
- **endRangeAndBackToMustNot()** → CcpQueryMustNot: Finaliza o range e retorna ao contexto `must_not`.
- **getInstanceCopy()** → CcpQueryRange (protected): Retorna uma cópia desta instância para o mecanismo imutável do builder.

---

## Classe: CcpQueryShould
**Pacote:** com.ccp.especifications.db.query
**Tipo:** classe (final)
**Propósito:** Representa o nó `should` dentro de uma query booleana do Elasticsearch. As condições adicionadas aqui são opcionais e incrementam a pontuação de relevância dos documentos que as satisfaçam. Suporta o parâmetro `minimum_should_match` para exigir que pelo menos N condições sejam verdadeiras.

### Métodos:
- **prefix(CcpEntityField field, Object value)** → CcpQueryShould: Adiciona condição de prefixo ao `should`.
- **setMinimumShouldMatch(int minimumShouldMatch)** → CcpQueryShould: Define o número mínimo de condições `should` que devem ser satisfeitas.
- **endShouldAndBackToBool()** → CcpQueryBool: Finaliza o `should` e retorna ao nó `bool` pai.
- **matchPhrase2(CcpEntityField field, Object value)** → CcpQueryShould: Alias para `matchPhrase` com campo tipado como `CcpEntityField`.
- **match(CcpEntityField field, Object value)** → CcpQueryShould: Adiciona condição `match` ao `should`.
- **matchPhrase(String field, Object value, double boost)** → CcpQueryShould: Adiciona condição `match_phrase` com boost usando o nome do campo como string.
- **match(String field, Object value, double boost, String operator)** → CcpQueryShould: Adiciona condição `match` com boost e operador lógico usando o nome do campo como string.
- **term(CcpEntityField field, Object value)** → CcpQueryShould: Adiciona condição de igualdade exata ao `should`.
- **exists(String field)** → CcpQueryShould: Adiciona condição de existência de campo ao `should`.
- **startBool()** → CcpQueryBool: Inicia um bloco `bool` aninhado dentro do `should`.
- **getInstanceCopy()** → CcpQueryShould (protected): Retorna uma cópia desta instância para o mecanismo imutável do builder.

---

## Classe: CcpQueryShouldNot
**Pacote:** com.ccp.especifications.db.query
**Tipo:** classe (final)
**Propósito:** Representa o nó `should_not` dentro de uma query booleana. As condições aqui são opcionais e penalizam a pontuação de documentos que as satisfaçam (semântica negativa opcional).

### Métodos:
- **prefix(CcpEntityField field, Object value)** → CcpQueryShouldNot: Adiciona condição de prefixo ao `should_not`.
- **endShouldNotAndBackToBool()** → CcpQueryBool: Finaliza o `should_not` e retorna ao nó `bool` pai.
- **matchPhrase(CcpEntityField field, Object value)** → CcpQueryShouldNot: Adiciona condição `match_phrase` ao `should_not`.
- **term(CcpEntityField field, Object value)** → CcpQueryShouldNot: Adiciona condição de igualdade exata ao `should_not`.
- **exists(String field)** → CcpQueryShouldNot: Adiciona condição de existência de campo ao `should_not`.
- **startBool()** → CcpQueryBool: Inicia um bloco `bool` aninhado dentro do `should_not`.
- **getInstanceCopy()** → CcpQueryShouldNot (protected): Retorna uma cópia desta instância para o mecanismo imutável do builder.

---

## Classe: CcpQuerySimplifiedQuery
**Pacote:** com.ccp.especifications.db.query
**Tipo:** classe (final)
**Propósito:** Representa uma query simplificada (sem os níveis intermediários `bool`/`filter`/`must`) diretamente no nível `query`. Útil para consultas com uma única condição de filtro sem aninhamento booleano. Diferencia-se das demais por sobrescrever `addCondition` para substituir o JSON por inteiro em vez de acumular condições em lista.

### Métodos:
- **terms(CcpEntityField field, Object value)** → CcpQuerySimplifiedQuery: Adiciona condição `terms` à query simplificada.
- **prefix(CcpEntityField field, Object value)** → CcpQuerySimplifiedQuery: Adiciona condição de prefixo à query simplificada.
- **endSimplifiedQueryAndBackToRequest()** → CcpQueryOptions: Finaliza a query simplificada e retorna ao nó raiz.
- **matchPhrase(CcpEntityField field, Object value)** → CcpQuerySimplifiedQuery: Adiciona condição `match_phrase`.
- **term(CcpEntityField field, Object value)** → CcpQuerySimplifiedQuery: Adiciona condição `term`.
- **match(CcpJsonFieldName field, Object value)** → CcpQuerySimplifiedQuery: Adiciona condição `match`.
- **exists(String field)** → CcpQuerySimplifiedQuery: Adiciona condição de existência de campo.
- **addCondition(String field, Object value, String key)** → CcpQuerySimplifiedQuery (protected): Sobrescrito para substituir completamente o JSON interno pela nova condição (não acumula lista como a versão da superclasse).
- **getValue()** → Object: Retorna o conteúdo JSON interno diretamente (não usa ArrayList de itens).
- **copy()** → CcpQuerySimplifiedQuery (protected): Cópia profunda desta instância; necessária por ter comportamento diferente do pai.
- **addChild(CcpQueryComponent child)** → CcpQuerySimplifiedQuery: Incorpora um filho ao JSON interno desta query simplificada.
- **hasChildreen()** → boolean: Retorna `true` se o JSON interno não está vazio.
- **getInstanceCopy()** → CcpQuerySimplifiedQuery (protected): Retorna nova instância para o mecanismo imutável.

---

## Interface: CcpDbRequester
**Pacote:** com.ccp.especifications.db.utils
**Tipo:** interface
**Propósito:** Contrato de baixo nível para execução de requisições ao banco de dados (Elasticsearch). Abstrai o transporte HTTP e os detalhes de conexão, fornecendo métodos para executar requisições com diferentes assinaturas e utilitários de configuração.

### Métodos:
- **executeHttpRequest(String trace, String url, CcpHttpMethods method, Integer expectedStatus, CcpJsonRepresentation body, String[] resources, CcpHttpResponseTransform&lt;V&gt; transformer)** → V: Executa uma requisição HTTP com corpo JSON, recursos (índices), status esperado e transforma a resposta.
- **executeHttpRequest(String trace, String url, CcpHttpMethods method, Integer expectedStatus, String body, CcpJsonRepresentation headers, CcpHttpResponseTransform&lt;V&gt; transformer)** → V: Variante que aceita o corpo como string e cabeçalhos explícitos.
- **executeHttpRequest(String trace, String url, CcpHttpMethods method, CcpJsonRepresentation flows, CcpJsonRepresentation body, CcpHttpResponseTransform&lt;V&gt; transformer)** → V: Variante que aceita um JSON de fluxos de tratamento de erros em vez de status esperado fixo.
- **executeHttpRequest(String trace, String url, CcpHttpMethods method, Integer expectedStatus, CcpJsonRepresentation body, CcpHttpResponseTransform&lt;V&gt; transformer)** → V: Variante sem lista de recursos.
- **executeDatabaseSetup(String pathToJavaClasses, String hostFolder, String pathToCreateEntityScript, Consumer&lt;CcpErrorDbUtilsIncorrectEntityFields&gt;, Consumer&lt;Throwable&gt;)** → List&lt;CcpBulkOperationResult&gt;: Executa a configuração inicial do banco de dados (criação de índices/mapeamentos), relatando erros de mapeamento e erros gerais via consumers.
- **getConnectionDetails()** → CcpJsonRepresentation: Retorna os detalhes de conexão com o banco (host, porta, credenciais, etc.).
- **getFieldNameToEntity()** → String: Retorna o nome do campo usado para identificar o índice/entidade nas requisições de multi-get.
- **getFieldNameToId()** → String: Retorna o nome do campo usado para identificar o ID do documento nas requisições de multi-get.
- **createTables(String pathToCreateEntityScript, String pathToJavaClasses, String mappingJnEntitiesErrors, String insertErrors)** → CcpDbRequester: Cria os índices/tabelas no banco com base nos scripts e classes informados, registrando erros nos destinos indicados.

---

## Interface: CcpEntity
**Pacote:** com.ccp.especifications.db.utils.entity
**Tipo:** interface
**Propósito:** Contrato central do sistema de entidades do jobsnow. Representa um índice/tabela no Elasticsearch e define todas as operações CRUD, além de utilitários para cálculo de ID, busca em union-all, transferência de dados e validação. Todo enum de entidade do sistema implementa esta interface.

### Métodos:
- **name()** → String: Retorna o nome da entidade (nome do índice no Elasticsearch) a partir dos metadados.
- **calculateId(CcpJsonRepresentation json)** → String: Calcula o ID SHA-1 do documento com base nos campos de chave primária definidos nos metadados. Se não houver chave primária, gera um UUID aleatório.
- **copyDataTo(CcpJsonRepresentation json, CcpEntity entities)** → CcpJsonRepresentation: Copia dados desta entidade para outra entidade sem remover o registro original. Implementação padrão lança `UnsupportedOperationException`.
- **getEntityMetaData()** → CcpEntityMetaData: Retorna os metadados da entidade (campos, chave primária, nome do índice, etc.). Método obrigatório a ser implementado por cada entidade.
- **delete(CcpJsonRepresentation json)** → CcpJsonRepresentation: Remove o documento correspondente ao JSON informado do índice desta entidade.
- **deleteAnyWhere(CcpJsonRepresentation json)** → CcpJsonRepresentation: Variante de delete sem restrições adicionais; delega para `delete` por padrão.
- **exists(CcpJsonRepresentation json)** → boolean: Verifica se existe um documento com o ID calculado a partir do JSON informado.
- **getAssociatedEntities()** → List&lt;CcpEntity&gt;: Retorna a lista de entidades associadas (por padrão, uma lista contendo apenas a própria entidade; sobrescrito em entidades com twin).
- **getHandledJson(CcpJsonRepresentation json)** → CcpJsonRepresentation: Retorna o JSON possivelmente transformado antes de operações. Por padrão retorna o mesmo JSON sem alterações.
- **getOneById(CcpJsonRepresentation json)** → CcpJsonRepresentation: Busca um documento pelo ID calculado; lança `CcpErrorFlowDisturb` com status `NOT_FOUND` se não encontrado.
- **getOneByIdAnyWhere(CcpJsonRepresentation json)** → CcpJsonRepresentation: Busca o documento e retorna envolvendo o resultado sob a chave da própria entidade.
- **getParametersToSearch(CcpJsonRepresentation json)** → List&lt;CcpJsonRepresentation&gt;: Monta a lista de parâmetros (entity + id) necessária para uma busca do tipo union-all.
- **getRecordFromUnionAll(CcpSelectUnionAll unionAll, Supplier&lt;CcpJsonRepresentation&gt; jsonSupplier)** → CcpJsonRepresentation: Extrai o registro desta entidade de um resultado de union-all já executado.
- **getTwinEntity()** → CcpEntity: Retorna a entidade twin correspondente. Por padrão lança `UnsupportedOperationException`.
- **throwException()** → T: Lança `UnsupportedOperationException` indicando que a operação não é suportada por esta entidade. Usado como proteção em operações de escrita em entidades somente leitura.
- **getWrapedEntity()** → CcpEntity: Retorna a entidade "embrulhada" (a entidade base dentro de um decorator). Por padrão retorna `this`.
- **isPresentInThisUnionAll(CcpSelectUnionAll unionAll, CcpJsonRepresentation json)** → boolean: Verifica se o documento desta entidade está presente em um resultado de union-all.
- **save(CcpJsonRepresentation json)** → CcpJsonRepresentation: Salva o documento no índice desta entidade, filtrando apenas os campos existentes nos metadados.
- **toBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation)** → List&lt;CcpBulkItem&gt;: Converte o JSON em itens de operação bulk para uso no executor de bulk.
- **transferDataTo(CcpJsonRepresentation json, CcpEntity entities)** → CcpJsonRepresentation: Transfere (move) dados desta entidade para outra entidade, removendo o registro original. Por padrão lança `UnsupportedOperationException`.
- **validateJson(CcpJsonRepresentation json)** → CcpJsonRepresentation: Valida o JSON antes de operações de escrita. Por padrão retorna o JSON sem validação.
- **getIdToSearchDisposableRecord(CcpJsonRepresentation json)** → CcpJsonRepresentation: Retorna o ID para buscar um registro descartável (disposable). Por padrão lança `UnsupportedOperationException`.

---

## Enum: CcpEntityOperationType
**Pacote:** com.ccp.especifications.db.utils.entity
**Tipo:** enum
**Propósito:** Define os tipos de operação que podem ser executados sobre uma entidade: `save`, `delete`, `deleteAnyWhere`, `transferDataTo` e `copyDataTo`. Cada constante implementa o método `execute` com a lógica específica da operação, funcionando como um command pattern.

### Métodos:
- **execute(CcpEntity entity, CcpJsonRepresentation json)** → CcpJsonRepresentation (abstrato): Executa a operação representada pela constante sobre a entidade e o JSON informados.
- **getTopicHandler(CcpEntity entity, CcpExecuteBulkOperation executeBulkOperation, Consumer&lt;String[]&gt; functionToDeleteKeysInTheCache)** → CcpBusiness: Retorna um `CcpBusiness` (função lambda) que encapsula a execução desta operação sobre a entidade informada; usado como handler de tópicos/mensagens.
- **instanciateFunction(Class&lt;?&gt; x)** → CcpBusiness (static): Instancia via reflexão uma classe que implementa `CcpBusiness` e a retorna.
- **getJsonValidationClass(CcpEntity entity)** → Class&lt;?&gt;: Retorna a classe de validação de campos JSON associada à entidade (anotada com `@CcpEntityFieldsValidator`). Se não houver anotação, retorna a própria classe da constante de enum.

---

## Anotação: CcpEntityAsyncWriter
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.annotations
**Tipo:** anotação
**Propósito:** Marca uma entidade para ter suas escritas realizadas de forma assíncrona. O atributo `value` indica a classe responsável pela escrita assíncrona.

### Atributos:
- **value()** → Class&lt;?&gt;: Classe que implementa a lógica de escrita assíncrona para esta entidade.

---

## Anotação: CcpEntityCache
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.annotations
**Tipo:** anotação
**Propósito:** Marca uma entidade para ter seus registros cacheados. O atributo `value` define o tempo de expiração do cache em segundos.

### Atributos:
- **value()** → int: Tempo de expiração do cache em segundos.

---

## Anotação: CcpEntityDataTransfer
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.annotations
**Tipo:** anotação
**Propósito:** Configura uma transferência de dados entre entidades (copyDataTo ou transferDataTo). Define a origem, o destino, o tipo de transferência, o momento de execução (antes/depois da operação principal) e os negócios a executar com seus tratadores de exceção.

### Atributos:
- **transferHandlers()** → CcpExceptionFlow[]: Tratadores de exceção específicos desta transferência.
- **from()** → CcpEntityType: Entidade de origem da transferência (mainEntity ou twinEntity).
- **transferType()** → CcpEntityDecoratorTransferType: Tipo de transferência (`copyDataTo` ou `transferDataTo`).
- **when()** → CcpEntityOperationStepType: Momento de execução: `before` (antes) ou `after` (depois) da operação principal.
- **execute()** → Class[]: Classes de negócio a executar durante a transferência.
- **to()** → Class&lt;?&gt;: Classe de configuração da entidade destino.

---

## Anotação: CcpEntityDataTransfers
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.annotations
**Tipo:** anotação
**Propósito:** Agrupa múltiplas configurações de `@CcpEntityDataTransfer` em uma entidade, além de definir tratadores de exceção globais para todas as transferências.

### Atributos:
- **globalHandlers()** → CcpExceptionFlow[]: Tratadores de exceção globais aplicados a todas as transferências.
- **transfers()** → CcpEntityDataTransfer[]: Array de transferências configuradas para a entidade.

---

## Anotação: CcpEntityDisposable
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.annotations
**Tipo:** anotação
**Propósito:** Marca uma entidade como descartável (com prazo de expiração automático). Define a fábrica responsável por criar registros expiráveis e a granularidade temporal da expiração.

### Atributos:
- **expurgableEntityFactory()** → Class&lt;?&gt;: Classe factory responsável por criar e gerenciar os registros expiráveis.
- **expurgTime()** → CcpEntityExpurgableOptions: Granularidade do tempo de expiração (yearly, monthly, daily, hourly, minute, second, millisecond).

---

## Anotação: CcpEntityFieldsTransformer
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.annotations
**Tipo:** anotação
**Propósito:** Marca uma entidade para ter seus campos transformados antes das operações. O atributo indica a classe que contém as definições de transformação de cada campo.

### Atributos:
- **classReferenceWithTheFields()** → Class: Classe que contém os transformadores padrão para os campos desta entidade.

---

## Anotação: CcpEntityFieldsValidator
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.annotations
**Tipo:** anotação
**Propósito:** Marca uma entidade para ter seus campos validados antes das operações de escrita. O atributo indica a classe que contém as definições de validação de cada campo (o schema de campos válidos).

### Atributos:
- **classReferenceWithTheFields()** → Class: Classe que define os campos válidos e suas regras de validação para esta entidade.

---

## Anotação: CcpEntityOlyReadable
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.annotations
**Tipo:** anotação
**Propósito:** Marca uma entidade como somente leitura. Quando presente, o decorator `DecoratorReadOnlyEntity` é aplicado, impedindo qualquer operação de escrita (`save`, `delete`, `transferDataTo`) e lançando exceção ao tentar executá-las.

---

## Anotação: CcpEntityOperation
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.annotations
**Tipo:** anotação
**Propósito:** Configura uma operação com side effects para uma entidade (save, delete, deleteAnyWhere). Define a origem, o tipo de operação, o momento de execução e os negócios a executar com seus tratadores de exceção.

### Atributos:
- **operationHandlers()** → CcpExceptionFlow[]: Tratadores de exceção específicos desta operação.
- **from()** → CcpEntityType: Entidade de origem (mainEntity ou twinEntity).
- **operation()** → CcpEntityDecoratorOperationType: Tipo da operação: `save`, `delete` ou `deleteAnyWhere`.
- **when()** → CcpEntityOperationStepType: Momento de execução: `before` (antes) ou `after` (depois) da operação.
- **execute()** → Class[]: Classes de negócio a executar durante a operação.

---

## Anotação: CcpEntityOperations
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.annotations
**Tipo:** anotação
**Propósito:** Agrupa múltiplas configurações de `@CcpEntityOperation` em uma entidade, além de definir tratadores de exceção globais para todas as operações.

### Atributos:
- **globalHandlers()** → CcpExceptionFlow[]: Tratadores de exceção globais aplicados a todas as operações.
- **operations()** → CcpEntityOperation[]: Array de operações configuradas para a entidade.

---

## Anotação: CcpEntityTwin
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.annotations
**Tipo:** anotação
**Propósito:** Configura o padrão de entidade twin, onde um registro migra entre dois índices (entidade principal e entidade twin) conforme seu estado. Define a função de limpeza de cache, o executor de bulk e o nome do índice twin.

### Atributos:
- **functionToDeleteKeysInTheCacheClass()** → Class: Classe que implementa a lógica de invalidação de cache ao mover registros entre entidades.
- **bulkExecutorClass()** → Class: Classe que implementa o executor de operações bulk para esta entidade.
- **twinEntityName()** → String: Nome do índice twin (índice de destino/origem alternativo).

---

## Anotação: CcpEntityVersionable
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.annotations
**Tipo:** anotação
**Propósito:** Marca uma entidade para ter histórico de versões. O atributo indica a classe responsável por gerenciar o versionamento dos registros.

### Atributos:
- **value()** → Class&lt;?&gt;: Classe que implementa a lógica de versionamento para esta entidade.

---

## Anotação: CcpExceptionFlow
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.annotations
**Tipo:** anotação
**Propósito:** Define um fluxo de tratamento de exceção: quando uma exceção do tipo `whenThrowing` for lançada durante a execução de um negócio, os negócios listados em `thenExecute` serão executados em sequência.

### Atributos:
- **thenExecute()** → Class&lt;?&gt;[]: Classes de negócio a executar quando a exceção configurada for capturada.
- **whenThrowing()** → Class&lt;?&gt;: Tipo de exceção que dispara este fluxo de tratamento.

---

## Classe: CcpDefaultEntityDelegator
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.engine
**Tipo:** classe abstrata
**Propósito:** Especialização abstrata de `CcpEntityDelegator` que fornece implementações padrão usando operações bulk para `save`, `delete`, `deleteAnyWhere`, `transferDataTo` e `copyDataTo`. Mantém referências ao executor bulk e à função de limpeza de cache. É a base para todos os decorators que precisam de operações de escrita com suporte a bulk.

### Métodos:
- **delete(CcpJsonRepresentation json)** → CcpJsonRepresentation: Converte o JSON em itens bulk de delete e executa via `executeBulkOperation`.
- **deleteAnyWhere(CcpJsonRepresentation json)** → CcpJsonRepresentation: Semelhante ao `delete`, mas força a operação de deleção mesmo para registros em qualquer localização (usando `CcpBulkItem` com delete explícito).
- **save(CcpJsonRepresentation json)** → CcpJsonRepresentation: Monta os handlers de save para cada item bulk, executa um union-all prévio e depois persiste via bulk. Garante que os dados sejam gravados corretamente em todas as entidades associadas.
- **calculateId(CcpJsonRepresentation json)** → String: Delega para a entidade base o cálculo do ID.
- **getEntityMetaData()** → CcpEntityMetaData: Delega para a entidade base a obtenção dos metadados.
- **getOneById(CcpJsonRepresentation json)** → CcpJsonRepresentation: Delega para a entidade base a busca por ID.
- **getOneByIdAnyWhere(CcpJsonRepresentation json)** → CcpJsonRepresentation: Realiza um union-all em todas as entidades associadas e retorna um mapa com o resultado de cada uma.
- **getParametersToSearch(CcpJsonRepresentation json)** → List&lt;CcpJsonRepresentation&gt;: Delega para a entidade base.
- **getTwinEntity()** → CcpEntity: Delega para a entidade base.
- **getWrapedEntity()** → CcpEntity: Retorna a entidade base sem decorator.
- **isPresentInThisUnionAll(CcpSelectUnionAll unionAll, CcpJsonRepresentation json)** → boolean: Delega para a entidade base.
- **exists(CcpJsonRepresentation json)** → boolean: Delega para a entidade base.
- **throwException()** → T: Delega para a entidade base.
- **getAssociatedEntities()** → List&lt;CcpEntity&gt;: Delega para a entidade base.
- **toBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation)** → List&lt;CcpBulkItem&gt;: Delega para a entidade base.
- **transferDataTo(CcpJsonRepresentation json, CcpEntity... entities)** → CcpJsonRepresentation: Executa a transferência de dados via bulk: apaga da entidade origem e cria nas entidades destino em uma única operação union-all + bulk.
- **copyDataTo(CcpJsonRepresentation json, CcpEntity... entities)** → CcpJsonRepresentation: Copia dados via bulk: lê da entidade origem e cria nas entidades destino sem remover a origem.
- **validateJson(CcpJsonRepresentation json)** → CcpJsonRepresentation: Delega para a entidade base.
- **getIdToSearchDisposableRecord(CcpJsonRepresentation json)** → CcpJsonRepresentation: Delega para a entidade base.
- **getRecordFromUnionAll(CcpSelectUnionAll unionAll, Supplier&lt;CcpJsonRepresentation&gt; jsonSupplier)** → CcpJsonRepresentation: Delega para a entidade base.

---

## Enum: CcpEntityDecoratorTypes
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.engine
**Tipo:** enum
**Propósito:** Cataloga todos os tipos de decorator disponíveis para entidades, associando cada um à sua anotação correspondente, à classe de decorator e a uma prioridade de aplicação. A prioridade determina a ordem de encadeamento dos decorators ao construir a entidade final via `CcpEntityFactory`.

### Constantes e suas prioridades:
- **Disposable** (prio 1): Associado a `@CcpEntityDisposable` / lógica de registros descartáveis.
- **Versionable** (prio 2): Associado a `@CcpEntityVersionable` / histórico de versões.
- **Cacheable** (prio 3): Associado a `@CcpEntityCache` / `DecoratorCacheEntity`.
- **WriteOperations** (prio 4): Associado a `@CcpEntityOperations` / `DecoratorOperationsWriterEntity`.
- **DataTransfer** (prio 4): Associado a `@CcpEntityDataTransfers` / `DecoratorTransferDataEntity`.
- **Twin** (prio 5): Associado a `@CcpEntityTwin` / `DecoratorTwinEntity`.
- **AsyncWriter** (prio 6): Associado a `@CcpEntityAsyncWriter`.
- **DataReadOnly** (prio 7): Associado a `@CcpEntityOlyReadable` / `DecoratorReadOnlyEntity`.
- **FieldsTransformer** (prio 8): Associado a `@CcpEntityFieldsTransformer` / `DecoratorFieldsTransformerEntity`.
- **FieldsValidator** (prio 9): Associado a `@CcpEntityFieldsValidator` / `DecoratorFieldsValidatorEntity`.

### Métodos:
- **isDecorated(Class&lt;?&gt; clazz)** → boolean: Verifica se a classe possui a anotação associada a este tipo de decorator.
- **getEntity(Class&lt;?&gt; clazz, CcpEntity decoratedEntity)** → CcpEntity: Instancia via reflexão o decorator correspondente, passando a entidade já decorada e a classe de configuração como argumentos.

---

## Classe: CcpEntityDelegator
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.engine
**Tipo:** classe
**Propósito:** Implementação base do padrão Decorator para `CcpEntity`. Mantém uma referência à entidade "embrulhada" e delega todas as chamadas de interface para ela. Serve como classe pai para todos os decorators específicos, que sobrescrevem apenas os métodos relevantes ao comportamento que adicionam.

### Métodos:
Todos os métodos delegam para `this.entity`:
- **calculateId(CcpJsonRepresentation json)** → String: Delega o cálculo de ID.
- **getEntityMetaData()** → CcpEntityMetaData: Delega a obtenção de metadados.
- **delete(CcpJsonRepresentation json)** → CcpJsonRepresentation: Delega a deleção.
- **deleteAnyWhere(CcpJsonRepresentation json)** → CcpJsonRepresentation: Delega a deleção irrestrita.
- **getOneById(CcpJsonRepresentation json)** → CcpJsonRepresentation: Delega a busca por ID.
- **getOneByIdAnyWhere(CcpJsonRepresentation json)** → CcpJsonRepresentation: Delega a busca por ID em qualquer entidade associada.
- **getParametersToSearch(CcpJsonRepresentation json)** → List&lt;CcpJsonRepresentation&gt;: Delega a obtenção de parâmetros de busca.
- **getTwinEntity()** → CcpEntity: Delega a obtenção da entidade twin.
- **getWrapedEntity()** → CcpEntity: Retorna a entidade interna embrulhada.
- **isPresentInThisUnionAll(CcpSelectUnionAll unionAll, CcpJsonRepresentation json)** → boolean: Delega a verificação de presença em union-all.
- **save(CcpJsonRepresentation json)** → CcpJsonRepresentation: Delega a persistência.
- **toString()** → String: Gera uma representação textual do stack de decorators no formato `EntityName = [DecoratorA->DecoratorB->...]`.
- **equals(Object obj)** → boolean: Delega para a entidade base.
- **hashCode()** → int: Delega para a entidade base.
- **exists(CcpJsonRepresentation json)** → boolean: Delega a verificação de existência.
- **throwException()** → T: Delega o lançamento de exceção.
- **getAssociatedEntities()** → List&lt;CcpEntity&gt;: Delega a obtenção de entidades associadas.
- **getHandledJson(CcpJsonRepresentation json)** → CcpJsonRepresentation: Delega a transformação do JSON.
- **toBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation)** → List&lt;CcpBulkItem&gt;: Delega a conversão em itens bulk.
- **copyDataTo(CcpJsonRepresentation json, CcpEntity entities)** → CcpJsonRepresentation: Delega a cópia de dados.
- **transferDataTo(CcpJsonRepresentation json, CcpEntity entities)** → CcpJsonRepresentation: Delega a transferência de dados.
- **validateJson(CcpJsonRepresentation json)** → CcpJsonRepresentation: Delega a validação.
- **getIdToSearchDisposableRecord(CcpJsonRepresentation json)** → CcpJsonRepresentation: Delega a busca de registro descartável.
- **getRecordFromUnionAll(CcpSelectUnionAll unionAll, Supplier&lt;CcpJsonRepresentation&gt; jsonSupplier)** → CcpJsonRepresentation: Delega a extração de registro de union-all.

---

## Classe: CcpEntityFactory
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.engine
**Tipo:** classe
**Propósito:** Fábrica central responsável por construir instâncias de `CcpEntity` completamente decoradas a partir de uma classe de configuração. Lê as anotações presentes na classe, determina quais decorators aplicar, respeita a ordem de prioridade e encadeia os decorators um sobre o outro. Também é responsável por extrair os `CcpEntityField` da enum interna `Fields` da classe de configuração.

### Métodos:
- **getCustomEntity(CcpEntityConfigurator configurator, CcpEntityDecoratorTypes... decoratorsToAvoid)** → CcpEntity (static): Constrói uma entidade customizada a partir de um configurador, excluindo os tipos de decorator informados.
- **getCustomEntity(CcpEntity entity, CcpEntityDecoratorTypes... decoratorsToAvoid)** → CcpEntity (static): Variante que recebe uma entidade existente para extrair a classe de configuração e reconstruir excluindo decorators.
- **getEntity(Class&lt;?&gt; configurationClass, Function&lt;Class&lt;?&gt;, String&gt; entityNameExtractor, CcpEntityDecoratorTypes... decoratorsToAvoid)** → CcpEntity (static): Método principal de construção: cria `CcpEntityMetaData`, instancia `DefaultImplementationEntity` como base e aplica em cadeia todos os decorators encontrados na classe (em ordem de prioridade), exceto os informados em `decoratorsToAvoid`.
- **getFields(Class&lt;?&gt; configurationClass)** → CcpEntityField[] (static): Extrai os campos da enum `Fields` interna da classe de configuração, determinando para cada campo se é chave primária, atualizável e qual transformador usar.

---

## Classe: CcpEntityMetaData
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.engine
**Tipo:** classe (final)
**Propósito:** Contém todos os metadados de uma entidade: nome do índice, campos, chaves primárias, campos atualizáveis, classe de configuração e referência à entidade instanciada. É o objeto de dados central consultado em todas as operações de CRUD e query para determinar IDs, campos válidos e comportamentos.

### Métodos:
- **getOperationCallback(CcpEntityOperationType operation)** → CcpBusiness: Retorna um `CcpBusiness` que executa a operação informada sobre a entidade associada a estes metadados.
- **getOnlyUpdatableFields(CcpJsonRepresentation json)** → CcpJsonRepresentation: Filtra o JSON retornando apenas os campos marcados como atualizáveis (não são chave primária).
- **getSortedPrimaryKeyValues(CcpJsonRepresentation json)** → ArrayList&lt;Object&gt;: Extrai e ordeia os valores das chaves primárias do JSON para compor o ID.
- **getOnlyExistingFields(CcpJsonRepresentation json)** → CcpJsonRepresentation: Filtra o JSON retornando apenas os campos que existem na definição da entidade (todos os campos, incluindo chave primária).
- **getEntitiesToSelect()** → String[]: Retorna o array de nomes de índices de todas as entidades associadas, para uso em union-all.
- **getOneByIdOrHandleItIfThisIdWasNotFound(CcpJsonRepresentation json, CcpBusiness ifNotFound)** → CcpJsonRepresentation: Tenta buscar um registro por ID; se não encontrado (`CcpErrorBulkEntityRecordNotFound`), executa o business de tratamento `ifNotFound`.
- **isNotAnUpdatableEntity()** → boolean: Retorna `true` se a entidade não possui campos atualizáveis (somente chaves primárias ou todos os campos são não-atualizáveis).
- **getMultipleByIds(Collection&lt;CcpJsonRepresentation&gt; asList)** → CcpJsonRepresentation: Executa um union-all para buscar múltiplos registros desta entidade por ID e retorna o resultado condensado.
- **getPrimaryKeyValues(Supplier&lt;CcpJsonRepresentation&gt; supplier)** → CcpJsonRepresentation: Extrai os valores das chaves primárias do JSON fornecido pelo supplier.
- **name()** → String: Retorna o nome do índice.
- **toString()** → String: Retorna o nome do índice.
- **toCreateBulkItem(CcpJsonRepresentation json)** → CcpBulkItem: Cria um item bulk de criação (create) para o JSON informado.
- **toUpdateBulkItem(CcpJsonRepresentation json)** → CcpBulkItem: Cria um item bulk de atualização (update) para o JSON informado.
- **toDeleteBulkItem(CcpJsonRepresentation json)** → CcpBulkItem: Cria um item bulk de deleção (delete) para o JSON informado.

---

## Classe: DecoratorCacheEntity
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.engine
**Tipo:** classe (package-private)
**Propósito:** Decorator que adiciona comportamento de cache sobre todas as operações de leitura e escrita de uma entidade. Ao salvar, atualiza o cache; ao deletar, invalida o cache; ao ler, consulta o cache antes de ir ao banco.

### Métodos:
- **delete(CcpJsonRepresentation json)** → CcpJsonRepresentation: Deleta o registro no banco e invalida a entrada correspondente no cache.
- **deleteAnyWhere(CcpJsonRepresentation json)** → CcpJsonRepresentation: Deleta irrestritamente e invalida o cache.
- **exists(CcpJsonRepresentation json)** → boolean: Verifica existência: primeiro no cache (se presente, retorna true sem ir ao banco); se não estiver no cache e existir no banco, popula o cache antes de retornar.
- **getOneById(CcpJsonRepresentation json)** → CcpJsonRepresentation: Busca o registro: primeiro no cache; se não encontrado, busca no banco e popula o cache com o resultado.
- **getRecordFromUnionAll(CcpSelectUnionAll unionAll, CcpJsonRepresentation json)** → CcpJsonRepresentation: Busca o registro de um union-all passando pelo cache.
- **isPresentInThisUnionAll(CcpSelectUnionAll unionAll, CcpJsonRepresentation json)** → boolean: Verifica presença em union-all; se presente, atualiza o cache com o registro encontrado; se ausente, invalida o cache.
- **save(CcpJsonRepresentation json)** → CcpJsonRepresentation: Salva no banco e atualiza o cache com o registro gravado.
- **transferDataTo(CcpJsonRepresentation json, CcpEntity entities)** → CcpJsonRepresentation: Invalida o cache da entidade origem antes de transferir os dados.

---

## Classe: DecoratorFieldsTransformerEntity
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.engine
**Tipo:** classe (package-private)
**Propósito:** Decorator que aplica transformações nos campos do JSON antes de repassar para a entidade base. Para cada campo que possui um transformer configurado, executa a transformação antes de qualquer operação de leitura ou escrita. Usa a classe interna `AlreadyTransformedJson` para evitar dupla transformação.

### Métodos:
- **delete(CcpJsonRepresentation json)** → CcpJsonRepresentation: Transforma o JSON e delega a deleção.
- **exists(CcpJsonRepresentation json)** → boolean: Transforma o JSON e delega a verificação de existência.
- **getHandledJson(CcpJsonRepresentation json)** → CcpJsonRepresentation: Aplica todos os transformadores configurados nos campos da entidade; retorna um `AlreadyTransformedJson` para evitar reprocessamento.
- **getOneById(CcpJsonRepresentation json)** → CcpJsonRepresentation: Transforma o JSON e delega a busca por ID.
- **getParametersToSearch(CcpJsonRepresentation json)** → List&lt;CcpJsonRepresentation&gt;: Transforma o JSON e delega a obtenção de parâmetros de busca.
- **getRecordFromUnionAll(CcpSelectUnionAll unionAll, CcpJsonRepresentation json)** → CcpJsonRepresentation: Transforma o JSON via supplier e delega a extração de registro.
- **isPresentInThisUnionAll(CcpSelectUnionAll unionAll, CcpJsonRepresentation json)** → boolean: Transforma o JSON e delega a verificação em union-all.
- **save(CcpJsonRepresentation json)** → CcpJsonRepresentation: Transforma o JSON e delega a persistência.
- **transferDataTo(CcpJsonRepresentation json, CcpEntity entities)** → CcpJsonRepresentation: Transforma o JSON e delega a transferência.
- **copyDataTo(CcpJsonRepresentation json, CcpEntity entities)** → CcpJsonRepresentation: Transforma o JSON e delega a cópia.

---

## Classe: DecoratorFieldsValidatorEntity
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.engine
**Tipo:** classe (package-private)
**Propósito:** Decorator que valida o JSON contra o schema de campos definido na anotação `@CcpEntityFieldsValidator` antes de operações de escrita. Usa o `CcpJsonValidatorEngine` para realizar a validação.

### Métodos:
- **validateJson(CcpJsonRepresentation json)** → CcpJsonRepresentation: Executa a validação do JSON usando a classe de campos referenciada na anotação `@CcpEntityFieldsValidator`; lança exceção se o JSON for inválido.
- **save(CcpJsonRepresentation json)** → CcpJsonRepresentation: Valida o JSON antes de salvar; delega para a entidade base após validação bem-sucedida.
- **transferDataTo(CcpJsonRepresentation json, CcpEntity entities)** → CcpJsonRepresentation: Valida o JSON antes de transferir dados.
- **copyDataTo(CcpJsonRepresentation json, CcpEntity entities)** → CcpJsonRepresentation: Valida o JSON antes de copiar dados.

---

## Classe: DecoratorOperationsWriterEntity
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.engine
**Tipo:** classe (package-private)
**Propósito:** Decorator que adiciona side effects (operações de negócio antes e depois) às operações de escrita de uma entidade, conforme configurado na anotação `@CcpEntityOperations`. Usa `CcpEntityDecoratorOperationType` para executar os fluxos de negócio encadeados.

### Métodos:
- **save(CcpJsonRepresentation json)** → CcpJsonRepresentation: Executa os flows de negócio before/after do tipo `save` definidos na anotação e então salva na entidade base.
- **delete(CcpJsonRepresentation json)** → CcpJsonRepresentation: Executa os flows de negócio before/after do tipo `delete` e então deleta na entidade base.
- **deleteAnyWhere(CcpJsonRepresentation json)** → CcpJsonRepresentation: Executa os flows de negócio before/after do tipo `deleteAnyWhere` e então deleta na entidade base.

---

## Classe: DecoratorReadOnlyEntity
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.engine
**Tipo:** classe (package-private)
**Propósito:** Decorator que torna uma entidade somente leitura, lançando `UnsupportedOperationException` em qualquer tentativa de escrita. Aplicado automaticamente quando a entidade possui a anotação `@CcpEntityOlyReadable`.

### Métodos:
- **delete(CcpJsonRepresentation json)** → CcpJsonRepresentation: Lança `UnsupportedOperationException` (entidade somente leitura).
- **deleteAnyWhere(CcpJsonRepresentation json)** → CcpJsonRepresentation: Lança `UnsupportedOperationException`.
- **save(CcpJsonRepresentation json)** → CcpJsonRepresentation: Lança `UnsupportedOperationException`.
- **transferDataTo(CcpJsonRepresentation json, CcpEntity... entities)** → CcpJsonRepresentation: Lança `UnsupportedOperationException`.

---

## Classe: DecoratorTransferDataEntity
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.engine
**Tipo:** classe (package-private)
**Propósito:** Decorator que adiciona side effects (operações de negócio antes e depois) às transferências de dados entre entidades, conforme configurado na anotação `@CcpEntityDataTransfers`. Usa `CcpEntityDecoratorTransferType` para executar os fluxos de negócio encadeados.

### Métodos:
- **copyDataTo(CcpJsonRepresentation json, CcpEntity entityToTransferData)** → CcpJsonRepresentation: Executa os flows de negócio before/after para `copyDataTo` e então copia os dados na entidade base.
- **transferDataTo(CcpJsonRepresentation json, CcpEntity entityToTransferData)** → CcpJsonRepresentation: Executa os flows de negócio before/after para `transferDataTo` e então transfere os dados na entidade base.

---

## Classe: DecoratorTwinEntity
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.engine
**Tipo:** classe (package-private)
**Propósito:** Decorator que implementa o padrão de entidade twin. Ao salvar, remove da entidade twin e persiste na entidade principal. Ao deletar, transfere o registro para a entidade twin (movimentação). Ao buscar, procura em ambas as entidades e redireciona se o registro foi movido para a twin.

### Métodos:
- **delete(CcpJsonRepresentation json)** → CcpJsonRepresentation: Em vez de deletar, transfere o registro para a entidade twin via operação bulk (semântica de "arquivamento").
- **getAssociatedEntities()** → List&lt;CcpEntity&gt;: Retorna a lista unificada de entidades associadas da entidade principal e da entidade twin.
- **getOneById(CcpJsonRepresentation json)** → CcpJsonRepresentation: Busca em ambas as entidades via union-all; retorna o registro da principal se encontrado; lança `CcpErrorFlowDisturb` com status `REDIRECT` se o registro está apenas na twin.
- **getTwinEntity()** → CcpEntity: Retorna a entidade twin; a instancia sob demanda via `CcpEntityFactory` se ainda não foi criada.
- **save(CcpJsonRepresentation json)** → CcpJsonRepresentation: Remove da entidade twin e salva na entidade principal em uma única operação bulk.
- **getParametersToSearch(CcpJsonRepresentation json)** → List&lt;CcpJsonRepresentation&gt;: Retorna os parâmetros de busca tanto da entidade principal quanto da twin (necessário para union-all).

---

## Classe: DefaultImplementationEntity
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.engine
**Tipo:** classe (package-private)
**Propósito:** Implementação mínima de `CcpEntity` que serve como camada base no stack de decorators. Não realiza nenhuma operação de banco diretamente; apenas carrega os metadados e delega as operações reais para os decorators acima (via herança de `CcpEntity` defaults). É a primeira instância criada por `CcpEntityFactory` antes de aplicar qualquer decorator.

### Métodos:
- **toString()** → String: Retorna o nome da entidade.
- **equals(Object obj)** → boolean: Compara entidades pelo nome do índice.
- **hashCode()** → int: Baseia o hash no nome do índice.
- **getEntityMetaData()** → CcpEntityMetaData: Retorna os metadados associando a entidade `ENTITY` estática da classe de configuração ao contexto atual (chama `associateEntity()`).

---

## Enum: CcpEntityDecoratorOperationType
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.enums
**Tipo:** enum
**Propósito:** Define os tipos de operação de escrita que podem ter side effects configurados via `@CcpEntityOperations`: `save`, `delete` e `deleteAnyWhere`. Cada constante implementa a operação real na entidade e delega ao mecanismo de flows (`executeFlow`) para executar os negócios before/after configurados.

### Métodos:
- **executeEntityOperation(CcpJsonRepresentation json, CcpEntity entity)** → CcpJsonRepresentation (abstrato): Executa a operação específica de cada constante (save/delete/deleteAnyWhere) na entidade.
- **execute(CcpJsonRepresentation json, Class&lt;?&gt; clazz, CcpEntity entity, CcpEntity... entities)** → CcpJsonRepresentation: Executa o fluxo completo: flows before → operação → flows after.
- **executeFlow(CcpJsonRepresentation json, CcpEntityOperationStepType when, Class&lt;?&gt; clazz, CcpEntity entity)** → CcpJsonRepresentation (protected): Percorre as operações configuradas na anotação `@CcpEntityOperations`, encontra a correspondente à constante atual, ao momento (before/after) e à entidade, e executa os negócios em cadeia com tratamento de exceções.

---

## Enum: CcpEntityDecoratorTransferType
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.enums
**Tipo:** enum
**Propósito:** Define os tipos de transferência de dados entre entidades que podem ter side effects configurados via `@CcpEntityDataTransfers`: `transferDataTo` (move, remove origem) e `copyDataTo` (copia, mantém origem). Funciona de forma análoga a `CcpEntityDecoratorOperationType`, mas para transferências.

### Métodos:
- **executeEntityTransfer(CcpJsonRepresentation json, CcpEntity entity, CcpEntity entities)** → CcpJsonRepresentation (abstrato): Executa a transferência ou cópia específica de cada constante.
- **execute(CcpJsonRepresentation json, Class&lt;?&gt; clazz, CcpEntity entity, CcpEntity entityToTransfer)** → CcpJsonRepresentation: Executa o fluxo completo: flows before → transferência → flows after.
- **executeFlow(CcpJsonRepresentation json, CcpEntityOperationStepType when, Class&lt;?&gt; clazz, CcpEntity entity, CcpEntity entityToTransfer)** → CcpJsonRepresentation (protected): Percorre as transferências configuradas na anotação `@CcpEntityDataTransfers`, filtra pela entidade destino, origem, tipo e momento, e executa os negócios em cadeia com tratamento de exceções.

---

## Enum: CcpEntityExpurgableOptions
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.enums
**Tipo:** enum
**Propósito:** Define as granularidades temporais disponíveis para expiração de entidades descartáveis (disposable). Cada constante representa uma unidade de tempo (millisecond, second, minute, hourly, daily, monthly, yearly) com o formato de data correspondente, o tempo em milissegundos, e o campo `Calendar` para cálculos.

### Métodos:
- **getFormattedDate(Long date)** → String: Formata um timestamp de acordo com o padrão desta granularidade temporal.
- **getFormattedDate()** → String: Formata o momento atual de acordo com o padrão desta granularidade.
- **getNextTimeStamp()** → Long: Calcula o timestamp do próximo período a partir do momento atual (ex: próxima hora, próximo dia).
- **getNextTimeStamp(Long timestamp)** → Long: Calcula o timestamp do próximo período a partir de um timestamp específico.
- **getNextDate()** → String: Retorna a data/hora do próximo período formatada como "dd/MM/yyyy HH:mm:ss.SSS".
- **getNextDate(Long timestamp)** → String: Retorna a data/hora do próximo período a partir de um timestamp formatada.
- **getPastDate(String format, String newFormat, Long timeMillis)** → String (static): Dado um formato de entrada e um timestamp, calcula a data correspondente ao período anterior nessa granularidade e a formata no novo formato.

---

## Enum: CcpEntityOperationStepType
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.enums
**Tipo:** enum
**Propósito:** Define os dois momentos possíveis de execução de flows de negócio nas operações de entidade: `before` (antes da operação principal) e `after` (depois). Usado como parâmetro em `@CcpEntityOperation` e `@CcpEntityDataTransfer`.

---

## Enum: CcpEntityType
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.enums
**Tipo:** enum
**Propósito:** Define de qual entidade (principal ou twin) uma operação ou transferência se origina. Usado nas anotações `@CcpEntityOperation` e `@CcpEntityDataTransfer` para especificar o contexto correto.

### Constantes:
- **mainEntity**: Extrai o nome da entidade principal acessando o campo estático `ENTITY` da classe de configuração.
- **twinEntity**: Extrai o nome da entidade twin lendo o atributo `twinEntityName` da anotação `@CcpEntityTwin`.

### Métodos:
- **extractEntityName(Class&lt;?&gt; clazz)** → String (abstrato): Retorna o nome do índice correspondente a este tipo de entidade para a classe de configuração informada.

---

## Interface: OperationWriter
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.enums
**Tipo:** interface (package-private)
**Propósito:** Interface utilitária compartilhada por `CcpEntityDecoratorOperationType` e `CcpEntityDecoratorTransferType`. Fornece os métodos padrão para executar negócios com tratamento de exceções baseado em fluxos (`CcpExceptionFlow`) e para instanciar negócios via reflexão.

### Métodos:
- **executeBusiness(CcpJsonRepresentation json, CcpBusiness business, Map&lt;Class&lt;?&gt;, List&lt;CcpBusiness&gt;&gt; exceptionHandlers)** → CcpJsonRepresentation (default): Executa um negócio; se uma exceção for lançada e estiver mapeada nos handlers, executa os negócios de tratamento correspondentes; caso contrário, propaga a exceção.
- **getExceptionHandlers(CcpExceptionFlow[] flows)** → Map&lt;Class&lt;?&gt;, List&lt;CcpBusiness&gt;&gt; (default): Converte um array de `@CcpExceptionFlow` em um mapa de `{TipoDeExceção -> [businessHandlers]}` para uso durante a execução.
- **getBusiness(Class&lt;?&gt; clazz)** → CcpBusiness (default): Instancia via reflexão uma classe que implementa `CcpBusiness`.

---

## Interface: CcpEntityConfigurator
**Pacote:** com.ccp.especifications.db.utils.entity.decorators.interfaces
**Tipo:** interface
**Propósito:** Interface marcadora e utilitária para classes de configuração de entidade. Define helpers para obter a entidade configurada (via reflexão no campo estático `ENTITY`) e para criar listas de itens bulk de criação a partir de JSONs.

### Métodos:
- **getFirstRecordsToInsert()** → List&lt;CcpBulkItem&gt; (default): Retorna a lista de registros iniciais a inserir na entidade durante a configuração do banco. Por padrão retorna lista vazia (sobrescrito por entidades que precisam de dados iniciais).
- **toCreateBulkItems(CcpEntity entity, String... jsons)** → List&lt;CcpBulkItem&gt; (default): Converte strings JSON em itens bulk de criação para a entidade informada.
- **toCreateBulkItems(CcpEntity entity, CcpJsonRepresentation... jsons)** → List&lt;CcpBulkItem&gt; (default): Variante que aceita `CcpJsonRepresentation` diretamente.
- **getEntity()** → CcpEntity (default): Acessa via reflexão o campo estático `ENTITY` da implementação concreta e retorna a instância de entidade configurada.

---

## Classe: CcpEntityField
**Pacote:** com.ccp.especifications.db.utils.entity.fields
**Tipo:** classe
**Propósito:** Representa a definição de um campo de uma entidade, encapsulando seu nome, se é chave primária, se é atualizável e qual transformador de valor aplicar. Implementa `CcpJsonFieldName` para ser usável diretamente como chave em `CcpJsonRepresentation`. Possui duas constantes pré-definidas: `TIMESTAMP` e `DATE`.

### Métodos:
- **name()** → String: Retorna o nome do campo (implementação de `CcpJsonFieldName`).
- **toString()** → String: Retorna o nome do campo.

---

## Classe: CcpEntityJsonTransformerError
**Pacote:** com.ccp.especifications.db.utils.entity.fields
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando ocorre um erro durante a transformação de um campo de entidade. É silenciada no `DecoratorFieldsTransformerEntity` — ao ser capturada, a transformação daquele campo é simplesmente ignorada, permitindo que campos sem valor válido para transformação sejam pulados sem interromper o fluxo.

---

## Classe: CcpErrorDbUtilsIncorrectEntityFields
**Pacote:** com.ccp.especifications.db.utils.entity.fields
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando o mapeamento de campos de uma entidade está incorreto (ex: campos declarados no código Java não correspondem ao schema no banco). Carrega a mensagem de erro descritiva do problema encontrado.

---

## Classe: CcpErrorEntityConfigurationFieldsIsMissing
**Pacote:** com.ccp.especifications.db.utils.entity.fields
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando uma classe de configuração de entidade não declara corretamente a enum interna `Fields` (enum pública estática que implementa `CcpJsonFieldName`). A mensagem indica qual classe está com o problema.

---

## Classe: CcpErrorEntityPrimaryKeyIsMissing
**Pacote:** com.ccp.especifications.db.utils.entity.fields
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando o JSON fornecido para uma operação não contém todos os campos de chave primária requeridos pela entidade. A mensagem de erro lista quais campos de chave primária estão faltando no JSON.

---

## Interface: CcpJsonTransformersDefaultEntityField
**Pacote:** com.ccp.especifications.db.utils.entity.fields
**Tipo:** interface
**Propósito:** Contrato para transformadores de campo de entidade que servem como padrão (default) para todos os campos não customizados. Estende `CcpBusiness` (função de transformação) e adiciona a informação de se o transformador pode ser aplicado a campos de chave primária.

### Métodos:
- **canBePrimaryKey()** → boolean: Retorna se este transformador pode ser aplicado a campos que são chave primária. Transformadores que alteram o valor de forma não-determinística (ex: timestamps) não podem ser chave primária.
- **name()** → String: Retorna o nome do transformador/campo.

---

## Anotação: CcpEntityFieldNotUpdatable
**Pacote:** com.ccp.especifications.db.utils.entity.fields.annotations
**Tipo:** anotação
**Propósito:** Marca um campo de entidade como não atualizável. Campos com esta anotação são excluídos da lista de campos atualizáveis nos metadados da entidade, impedindo que sejam sobrescritos em operações de update.

---

## Anotação: CcpEntityFieldPrimaryKey
**Pacote:** com.ccp.especifications.db.utils.entity.fields.annotations
**Tipo:** anotação
**Propósito:** Marca um campo de entidade como chave primária. Campos com esta anotação são incluídos na lista de `primaryKeyNames` nos metadados da entidade e são usados para calcular o ID SHA-1 do documento. Aplica-se a campos declarados em enums de configuração de entidade.

---

## Anotação: CcpEntityFieldTransformer
**Pacote:** com.ccp.especifications.db.utils.entity.fields.annotations
**Tipo:** anotação
**Propósito:** Permite associar um transformador customizado específico a um campo individual de entidade, sobrescrevendo o transformador padrão definido pela anotação `@CcpEntityFieldsTransformer` na classe. O atributo `value` indica a classe do transformador customizado.

### Atributos:
- **value()** → Class&lt;?&gt;: Classe que implementa `CcpBusiness` para transformar o valor deste campo específico.


---

# Documentação: ccp_commons_jobsnow — Parte 4
# Especificações: HTTP, Email, JSON, Mensageria, Validações de Campos

---

## Classe: CcpEmailSender
**Pacote:** com.ccp.especifications.email
**Tipo:** interface
**Propósito:** Define o contrato para envio de e-mails via provedor externo (implementado com SendGrid). Abstrai os detalhes do provedor, permitindo envio de e-mails de texto simples ou HTML.

### Métodos:
- **sendSimpleTextEmailMessage(providerToken, providerUrl, templateId, sender, subject, message, contentType, recipients...)** → CcpJsonRepresentation: Envia um e-mail para um ou mais destinatários usando o provedor configurado.

---

## Classe: CcpErrorEmailInvalidAdresses
**Pacote:** com.ccp.especifications.email
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando endereços de e-mail fornecidos são inválidos. Carrega a lista de endereços na mensagem.

### Métodos:
- **CcpErrorEmailInvalidAdresses(invalidEmails)** → construtor: Recebe a lista de endereços inválidos e os inclui na mensagem.

---

## Classe: CcpFileBucket
**Pacote:** com.ccp.especifications.file.bucket
**Tipo:** interface
**Propósito:** Contrato para operações de armazenamento de arquivos em bucket (GCP Storage): recuperar, salvar e excluir arquivos ou pastas por tenant e nome.

### Métodos:
- **get(tenant, bucketName, fileName)** → String: Recupera o conteúdo de um arquivo no bucket.
- **delete(tenant, bucketName, fileName)** → String: Remove um arquivo específico do bucket.
- **delete(tenant, bucketName)** → String: Remove uma pasta inteira do bucket.
- **save(tenant, bucketName, fileName, fileContent)** → String: Salva um arquivo no bucket.

---

## Classe: CcpFileBucketOperation
**Pacote:** com.ccp.especifications.file.bucket
**Tipo:** enum
**Propósito:** Enum de operações de bucket que obtém a implementação via DI e aplica a operação (deleção de pasta ou leitura) sobre um ou vários arquivos.

### Métodos:
- **deleteFolder.execute(bucket, tenant, folderName, fileName)** → String: Executa deleção de pasta inteira.
- **get.execute(bucket, tenant, folderName, fileName)** → String: Executa leitura de arquivo.
- **execute(tenant, folderName, files...)** → CcpFileBucketOperation: Aplica a operação sobre múltiplos arquivos e retorna o enum para encadeamento.
- **execute(tenant, folderName, file)** → String: Aplica a operação sobre um único arquivo e retorna o resultado.

---

## Classe: CcpErrorHttp
**Pacote:** com.ccp.especifications.http
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção base para erros HTTP. Carrega um JSON com todos os detalhes da requisição que falhou (URL, método, headers, corpo, status, resposta, status esperados).

### Métodos:
- **CcpErrorHttp(entity)** → construtor: Monta a mensagem de erro formatada a partir do JSON de detalhes.

---

## Classe: CcpErrorHttpClient
**Pacote:** com.ccp.especifications.http
**Tipo:** classe (CcpErrorHttp)
**Propósito:** Especialização de `CcpErrorHttp` para erros de cliente HTTP (status 4xx).

### Métodos:
- **CcpErrorHttpClient(entity)** → construtor: Delega ao construtor pai.

---

## Classe: CcpErrorHttpServer
**Pacote:** com.ccp.especifications.http
**Tipo:** classe (CcpErrorHttp)
**Propósito:** Especialização de `CcpErrorHttp` para erros de servidor HTTP (status 5xx).

### Métodos:
- **CcpErrorHttpServer(entity)** → construtor: Delega ao construtor pai.

---

## Classe: CcpHttpApiExecutor
**Pacote:** com.ccp.especifications.http
**Tipo:** interface (estende CcpBusiness)
**Propósito:** Interface para executores de chamadas a APIs HTTP externas com política de retentativa configurável.

### Métodos:
- **getMaxTries()** → int: Número máximo de tentativas (padrão: 3).
- **getSleepTimeToRetry()** → int: Tempo de espera em ms entre tentativas (padrão: 3000).

---

## Classe: CcpHttpBodyBinary
**Pacote:** com.ccp.especifications.http
**Tipo:** classe
**Propósito:** Representa uma parte binária de uma requisição HTTP multipart (tipo de conteúdo, nome do campo, nome do arquivo e bytes).

### Métodos:
- **CcpHttpBodyBinary(contentType, name, fileName, bytes)** → construtor: Inicializa os atributos da parte binária.
- **getBytes()** → byte[]: Converte `Byte[]` (wrapper) para `byte[]` primitivo.

---

## Classe: CcpHttpBodyText
**Pacote:** com.ccp.especifications.http
**Tipo:** classe
**Propósito:** Representa uma parte textual de uma requisição HTTP multipart.

### Métodos:
- **CcpHttpBodyText(contentType, name, text)** → construtor: Inicializa os atributos da parte textual.

---

## Classe: CcpHttpContentType
**Pacote:** com.ccp.especifications.http
**Tipo:** enum
**Propósito:** Enum dos tipos de conteúdo HTTP suportados: `TEXT_PLAIN` e `TEXT_HTML`.

---

## Classe: CcpHttpHandler
**Pacote:** com.ccp.especifications.http
**Tipo:** classe final
**Propósito:** Orquestrador de execução de requisições HTTP. Combina `CcpHttpRequester` com um mapa de fluxos (status → lógica de negócio), executando a lógica correspondente ao status recebido ou lançando `CcpErrorHttp`.

### Métodos:
- **CcpHttpHandler(flows, url)** → construtor: Cria handler com mapa explícito de fluxos.
- **CcpHttpHandler(httpStatus, alternativeFlow, url)** → construtor: Cria handler com um status mapeado para fluxo alternativo.
- **CcpHttpHandler(httpStatus, url)** → construtor: Cria handler com um único status aceito.
- **executeHttpSimplifiedGet(trace, transformer)** → V: Executa GET simples sem headers nem corpo.
- **executeHttpRequest(trace, method, headers, body, transformer)** → V: Executa requisição com corpo JSON.
- **executeHttpRequest(trace, method, headers, request, transformer)** → V: Executa requisição com corpo String.
- **executeMultiPartHttpRequest(trace, method, headers, texts, binaries, transformer)** → V: Executa requisição multipart.
- **executeHttpRequest(trace, method, headers, request, transformer, response)** → V: Versão completa que recebe resposta já obtida, seleciona fluxo pelo status e executa o `CcpBusiness`.

---

## Classe: CcpHttpMethods
**Pacote:** com.ccp.especifications.http
**Tipo:** enum
**Propósito:** Enum dos métodos HTTP suportados: `POST, GET, PATCH, PUT, DELETE, HEAD`.

---

## Classe: CcpHttpRequester
**Pacote:** com.ccp.especifications.http
**Tipo:** interface
**Propósito:** Contrato para execução de requisições HTTP brutas. Além dos métodos de envio, fornece método default que valida o status de retorno e factory que constrói a exceção HTTP correta por faixa de status.

### Métodos:
- **executeHttpRequest(url, method, headers, body)** → CcpHttpResponse: Executa requisição HTTP simples.
- **executeMultiPartHttpRequest(url, method, headers, bodyTexts, bodyBinaries)** → CcpHttpResponse: Executa requisição multipart.
- **executeHttpRequest(url, method, headers, request, numbers...)** → CcpHttpResponse (default): Executa e valida se o status está entre os esperados; lança `CcpErrorHttp` caso contrário.
- **getHttpError(trace, url, method, headers, request, status, response, expectedStatusList)** → CcpErrorHttp (default): Constrói `CcpErrorHttpClient` (4xx), `CcpErrorHttpServer` (5xx) ou `CcpErrorHttp` genérico.

---

## Classe: CcpHttpResponse
**Pacote:** com.ccp.especifications.http
**Tipo:** classe
**Propósito:** Objeto imutável que encapsula a resposta HTTP, oferecendo métodos para interpretar o corpo em diferentes formatos e verificar a faixa do status.

### Métodos:
- **CcpHttpResponse(httpResponse String, httpStatus, curl)** → construtor: Inicializa com corpo, status e curl.
- **CcpHttpResponse(httpResponse InputStream, httpStatus, curl)** → construtor: Lê o InputStream e converte para String.
- **isValidSingleJson()** → boolean: Verifica se o corpo é um JSON de objeto único válido.
- **asSingleJson()** → CcpJsonRepresentation: Converte o corpo para JSON; retorna JSON vazio em falha.
- **asListRecord()** → List&lt;CcpJsonRepresentation&gt;: Converte o corpo como lista de JSONs.
- **asListObject()** → List&lt;Object&gt;: Converte o corpo como lista de objetos genéricos.
- **asBase64()** → String: Codifica o corpo em Base64.
- **toString()** → String: Serializa status e corpo em JSON.
- **isClientError()** → boolean: Status na faixa 400–499.
- **isServerError()** → boolean: Status na faixa 500–599.
- **isSuccess()** → boolean: Status na faixa 200–299.

---

## Classe: CcpHttpResponseTransform
**Pacote:** com.ccp.especifications.http
**Tipo:** interface (funcional)
**Propósito:** Interface funcional para transformar um `CcpHttpResponse` no tipo de retorno desejado.

### Métodos:
- **transform(response)** → V: Transforma a resposta HTTP no tipo parametrizado.

---

## Classe: CcpHttpResponseType
**Pacote:** com.ccp.especifications.http
**Tipo:** interface
**Propósito:** Declara constantes de transformação prontas para os tipos de retorno mais comuns de respostas HTTP.

### Constantes:
- **listRecord**: Converte em `List<CcpJsonRepresentation>`.
- **singleRecord**: Converte em `CcpJsonRepresentation`.
- **byteArray**: Converte em `byte[]`.
- **listObject**: Converte em `List<Object>`.
- **string**: Retorna o corpo como `String`.
- **base64**: Converte o corpo para Base64.

---

## Classe: CcpHttpTooManyRequests
**Pacote:** com.ccp.especifications.http
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando o servidor externo retorna 429 (Too Many Requests).

---

## Classe: CcpErrorInstantMessageThisBotWasBlockedByThisUser
**Pacote:** com.ccp.especifications.instant.messenger
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando um bot Telegram tenta enviar mensagem para um usuário que o bloqueou. Armazena o nome/token do bot.

### Métodos:
- **CcpErrorInstantMessageThisBotWasBlockedByThisUser(token)** → construtor: Armazena o token do bot em `botName`.

---

## Classe: CcpErrorInstantMessengerChatErrorCount
**Pacote:** com.ccp.especifications.instant.messenger
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando não é possível contar membros de um chat Telegram.

### Métodos:
- **CcpErrorInstantMessengerChatErrorCount(chatId)** → construtor: Inclui o chatId na mensagem de erro.

---

## Classe: CcpInstantMessenger
**Pacote:** com.ccp.especifications.instant.messenger
**Tipo:** interface
**Propósito:** Contrato para envio de mensagens via bot (Telegram). Suporta envio de texto e envio de arquivos.

### Métodos:
- **sendTextMessage(botType, botToken, chatId, replyTo, message)** → CcpJsonRepresentation: Envia mensagem de texto para um chat.
- **sendFile(botType, botToken, chatId, replyTo, fileName, caption, fileContent)** → CcpJsonRepresentation: Envia arquivo binário com legenda.

---

## Classe: CcpJsonHandler
**Pacote:** com.ccp.especifications.json
**Tipo:** interface
**Propósito:** Contrato de serialização/desserialização JSON (Gson). Converte objetos Java em JSON e vice-versa, e valida se uma string é JSON bem formado.

### Métodos:
- **toJson(md)** → String: Serializa o objeto para JSON compacto.
- **asPrettyJson(md)** → String: Serializa o objeto para JSON formatado.
- **fromJson(md)** → T: Desserializa JSON para o tipo inferido.
- **isValidJson(src)** → boolean: Verifica se a string é um JSON válido.

---

## Classe: CcpAuthenticationProvider
**Pacote:** com.ccp.especifications.main.authentication
**Tipo:** interface
**Propósito:** Contrato para obtenção de tokens JWT de autenticação (GCP OAuth).

### Métodos:
- **getJwtToken()** → String: Obtém e retorna o token JWT atual.

---

## Classe: CcpErrorMensageriaInvalidName
**Pacote:** com.ccp.especifications.mensageria.receiver
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando o nome de processo informado ao receiver não corresponde a nenhum tópico válido.

### Métodos:
- **CcpErrorMensageriaInvalidName(processName)** → construtor: Inclui o nome do processo inválido na mensagem.

---

## Classe: CcpMensageriaReceiver
**Pacote:** com.ccp.especifications.mensageria.receiver
**Tipo:** classe abstrata
**Propósito:** Base para receptores de mensagens GCP PubSub. Resolve o processo de negócio correto a partir do nome do tópico, suportando tanto `CcpBusiness` quanto `CcpEntityConfigurator` em modo assíncrono.

### Métodos:
- **CcpMensageriaReceiver(operationFieldName)** → construtor: Inicializa o nome do campo de operação no JSON.
- **getProcess(processName, json)** → CcpBusiness: Instancia o processo por reflexão e retorna o handler adequado.
- **getExecuteBulkOperation()** → CcpExecuteBulkOperation (abstrato): Fornece o executor de operações bulk.
- **getFunctionToDeleteKeysInTheCache()** → Consumer&lt;String[]&gt; (abstrato): Fornece a função de invalidação de cache.
- **getInstance(json)** → CcpMensageriaReceiver (estático): Cria instância concreta lendo o nome da classe no campo `mensageriaReceiver` do JSON.
- **name()** → String: Retorna o nome do campo de operação configurado.

---

## Classe: CcpMensageriaSender
**Pacote:** com.ccp.especifications.mensageria.sender
**Tipo:** interface
**Propósito:** Contrato para publicação de mensagens em tópicos GCP PubSub. Valida as mensagens antes de publicar usando a engine de validação JSON.

### Métodos:
- **sendToMensageria(topic, jsonValidationClass, msgs List)** → CcpMensageriaSender (default): Converte lista para array e delega.
- **sendToMensageria(topic, jsonValidationClass, msgs...)** → CcpMensageriaSender (default): Valida cada JSON e converte para String antes de publicar.
- **sendToMensageria(topic, msgs...)** → CcpMensageriaSender: Publica as strings diretamente no tópico.

---

## Classe: CcpPasswordHandler
**Pacote:** com.ccp.especifications.password
**Tipo:** interface
**Propósito:** Contrato para hash e verificação de senhas (BCrypt via Mindrot).

### Métodos:
- **matches(password, hash)** → boolean: Verifica se a senha em texto plano corresponde ao hash.
- **getHash(password)** → String: Gera e retorna o hash BCrypt da senha.

---

## Classe: CcpTextExtractor
**Pacote:** com.ccp.especifications.text.extractor
**Tipo:** interface
**Propósito:** Contrato para extração de texto de documentos (Apache Tika).

### Métodos:
- **extractText(content)** → String: Extrai e retorna o texto puro do conteúdo fornecido.

---

## Classe: CcpJsonCopyFieldValidationsFrom
**Pacote:** com.ccp.json.validations.fields.annotations
**Tipo:** anotação
**Propósito:** Indica que as validações de um campo devem ser copiadas de um campo homônimo em outra classe, evitando duplicação.

### Atributos:
- **value()** → Class&lt;?&gt;: Classe de origem das validações.

---

## Classe: CcpJsonFieldValidatorArray
**Pacote:** com.ccp.json.validations.fields.annotations
**Tipo:** anotação
**Propósito:** Marca o campo como array e define restrições de tamanho e unicidade dos itens.

### Atributos:
- **exactSize()** → int: Tamanho exato obrigatório.
- **nonRepeatedItems()** → boolean: Exige itens únicos (padrão: true).
- **minSize()** → int: Tamanho mínimo.
- **maxSize()** → int: Tamanho máximo.

---

## Classe: CcpJsonFieldValidatorRequired
**Pacote:** com.ccp.json.validations.fields.annotations
**Tipo:** anotação
**Propósito:** Marca o campo como obrigatório na validação do JSON de entrada.

---

## Classe: CcpJsonFieldTypeBoolean
**Pacote:** com.ccp.json.validations.fields.annotations.type
**Tipo:** anotação
**Propósito:** Indica que o valor do campo deve ser booleano.

---

## Classe: CcpJsonFieldTypeCustom
**Pacote:** com.ccp.json.validations.fields.annotations.type
**Tipo:** anotação
**Propósito:** Permite indicar uma implementação customizada de `CcpJsonFieldType` para validar o campo.

### Atributos:
- **value()** → Class&lt;?&gt;: Classe customizada que implementa `CcpJsonFieldType`.

---

## Classe: CcpJsonFieldTypeNestedJson
**Pacote:** com.ccp.json.validations.fields.annotations.type
**Tipo:** anotação
**Propósito:** Indica que o valor é um JSON aninhado, com opção de validação recursiva e controle de JSONs vazios.

### Atributos:
- **jsonValidation()** → Class&lt;?&gt;: Classe de validação do JSON interno (padrão: sem validação).
- **allowsEmptyJson()** → boolean: Se JSON interno vazio é aceito (padrão: true).

---

## Classe: CcpJsonFieldTypeNumber
**Pacote:** com.ccp.json.validations.fields.annotations.type
**Tipo:** anotação
**Propósito:** Restrições para campos numéricos double: valor mínimo, máximo, exato e lista de permitidos.

### Atributos:
- **allowedValues()** → double[], **minValue()** → double, **maxValue()** → double, **exactValue()** → double

---

## Classe: CcpJsonFieldTypeNumberInteger
**Pacote:** com.ccp.json.validations.fields.annotations.type
**Tipo:** anotação
**Propósito:** Restrições para campos inteiros long: valor mínimo, máximo, exato e lista de permitidos.

### Atributos:
- **allowedValues()** → long[], **minValue()** → long, **maxValue()** → long, **exactValue()** → long

---

## Classe: CcpJsonFieldTypeNumberUnsigned
**Pacote:** com.ccp.json.validations.fields.annotations.type
**Tipo:** anotação
**Propósito:** Restrições para campos inteiros long não-negativos (>= 0).

### Atributos:
- **allowedValues()** → long[], **minValue()** → long, **maxValue()** → long, **exactValue()** → long

---

## Classe: CcpJsonFieldTypeString
**Pacote:** com.ccp.json.validations.fields.annotations.type
**Tipo:** anotação
**Propósito:** Restrições para campos String: comprimento mínimo/máximo/exato, valores permitidos, regex e permissão de string vazia.

### Atributos:
- **exactLength()** → int, **minLength()** → int, **maxLength()** → int, **allowsEmptyString()** → boolean, **allowedValues()** → String[], **regexValidation()** → String

---

## Classe: CcpJsonFieldTypeTimeAfter
**Pacote:** com.ccp.json.validations.fields.annotations.type
**Tipo:** anotação
**Propósito:** Valida que um timestamp é posterior ao momento atual dentro de um intervalo configurável.

### Atributos:
- **exactValue()** → int, **intervalType()** → CcpEntityExpurgableOptions, **minValue()** → int, **maxValue()** → int

---

## Classe: CcpJsonFieldTypeTimeBefore
**Pacote:** com.ccp.json.validations.fields.annotations.type
**Tipo:** anotação
**Propósito:** Valida que um timestamp é anterior ao momento atual dentro de um intervalo configurável.

### Atributos:
- **exactValue()** → int, **intervalType()** → CcpEntityExpurgableOptions, **minValue()** → int, **maxValue()** → int

---

## Classe: CcpJsonFieldErrorSkipOthersValidationsToTheField
**Pacote:** com.ccp.json.validations.fields.engine
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção de controle de fluxo que interrompe as demais validações do campo atual ao ser lançada por um validador do tipo `breakFieldValidation`.

### Métodos:
- **CcpJsonFieldErrorSkipOthersValidationsToTheField(error)** → construtor: Armazena o JSON de erros acumulado em `validationResultFromField`.

---

## Classe: CcpJsonFieldNotValidated
**Pacote:** com.ccp.json.validations.fields.engine
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção de controle de fluxo lançada quando um campo não possui nenhuma anotação de tipo reconhecida. Capturada silenciosamente para pular o campo.

---

## Classe: CcpJsonFieldDefaultTypes
**Pacote:** com.ccp.json.validations.fields.enums
**Tipo:** enum (implementa CcpJsonFieldType)
**Propósito:** Catálogo dos tipos de campo padrão. Cada constante encapsula compatibilidade de tipo e validadores associados.

### Constantes:
- **Required**: Presença obrigatória do campo.
- **Boolean**: Valor booleano.
- **Array**: Coleção com restrições de tamanho e unicidade.
- **NestedJson**: JSON aninhado com validação recursiva opcional.
- **Number**: Número double com restrições.
- **NumberInteger**: Inteiro long com restrições.
- **NumberUnsigned**: Inteiro long não-negativo com restrições.
- **String**: String com restrições diversas.
- **TimeBeforeCurrentDate**: Timestamp antes do momento atual.
- **TimeAfterCurrentDate**: Timestamp após o momento atual.

### Métodos (interface CcpJsonFieldType):
- **evaluateCompatibleType(fieldName)** → Predicate: Verifica se o valor é do tipo correto.
- **hasRuleExplanation(field)** → boolean: Verifica se a anotação correspondente está presente.
- **getErrorTypes()** → CcpJsonFieldValidatorInterface[]: Retorna os validadores específicos do tipo.

---

## Classe: CcpJsonFieldError
**Pacote:** com.ccp.json.validations.fields.enums
**Tipo:** enum (implementa CcpJsonFieldValidatorInterface)
**Propósito:** Validadores transversais de campo: tipo incompatível, campo obrigatório ausente e conflito coleção/valor único.

### Constantes:
- **incompatibleType** (breakFieldValidation): Tipo do valor incompatível com o esperado.
- **requiredFieldIsMissing** (continueFieldValidation): Campo obrigatório ausente.
- **validateCollectionOrSigleValue** (breakFieldValidation): Inconsistência entre ser/não ser coleção.

---

## Classe: CcpJsonFieldErrorHandleType
**Pacote:** com.ccp.json.validations.fields.enums
**Tipo:** enum
**Propósito:** Define a estratégia ao detectar erro num campo: `breakFieldValidation` (lança exceção para interromper) ou `continueFieldValidation` (continua acumulando erros).

### Métodos:
- **maybeBreakValidation(error)** → void: Executa a ação da estratégia.

---

## Classe: CcpJsonFieldTypeError
**Pacote:** com.ccp.json.validations.fields.enums
**Tipo:** enum (implementa CcpJsonFieldValidatorInterface)
**Propósito:** Catálogo extenso de validadores de restrições específicas por tipo (números, strings, arrays, timestamps, JSON aninhado). Cada constante valida uma restrição específica lendo os parâmetros da anotação correspondente.

### Grupos de constantes:
- Unsigned: `unsignedNumberMaxValue/MinValue/ExactValue/Allowed`
- Long: `longNumberMaxValue/MinValue/ExactValue/Allowed`
- Double: `doubleNumberMaxValue/MinValue/ExactValue/Allowed`
- Array: `arrayMinSize/ExactSize/MaxSize/NonReapeted`
- String: `stringMinLength/ExactLength/MaxLength/AllowedValues/Regex/NotEmpty`
- TimeBefore: `timeMaxValue/ExactValue/MinValueBeforeCurrentTime`
- TimeAfter: `timeMaxValue/ExactValue/MinValueAfterCurrentTime`
- NestedJson: `nestedJson`, `emptyJson`

---

## Classe: CcpJsonFieldsValidationContext
**Pacote:** com.ccp.json.validations.fields.enums
**Tipo:** enum
**Propósito:** Distingue o contexto de validação: `single` (valor único) ou `collection` (item dentro de array).

---

## Classe: CcpJsonFieldType
**Pacote:** com.ccp.json.validations.fields.interfaces
**Tipo:** interface
**Propósito:** Contrato central para tipos de campo de validação JSON. Orquestra verificação de tipo, acumulação de erros e geração de explicações de regras.

### Métodos:
- **evaluateCompatibleType(fieldName)** → Predicate (abstrato): Verifica compatibilidade de tipo.
- **hasRuleExplanation(field)** → boolean (abstrato): Indica se há regras ativas.
- **name()** → String: Nome do tipo.
- **getErrorTypes()** → CcpJsonFieldValidatorInterface[]: Validadores específicos.
- **hasErrors(json, field, context)** → boolean (default): Executa validações e retorna se há erros.
- **getErrors(errors, json, field, context)** → CcpJsonRepresentation (default): Acumula erros.
- **updateRuleExplanation(ruleExplanation, field)** → CcpJsonRepresentation (default): Adiciona explicações ao JSON de regras.
- **getDefaultValidations()** → List (default): Retorna `incompatibleType` e `validateCollectionOrSigleValue`.

---

## Classe: CcpJsonFieldValidatorInterface
**Pacote:** com.ccp.json.validations.fields.interfaces
**Tipo:** interface
**Propósito:** Contrato para validadores individuais de campo. Define verificação de erro, mensagem, explicação de regra e acumulação de erros em JSON.

### Métodos:
- **isValidValidationContext(context)** → boolean (default): Validador aplicável ao contexto (padrão: sempre).
- **getErrorHandleType()** → CcpJsonFieldErrorHandleType: Estratégia de tratamento.
- **name()** → String: Identificador do validador.
- **getErrorMessage(json, field, type)** → String: Mensagem descritiva do erro.
- **hasError(json, field, type)** → boolean: Verifica ocorrência do erro.
- **getRuleExplanation(field, type)** → Object: Explica a regra em linguagem natural.
- **getError(json, field, type)** → Object (default): Retorna o objeto de erro.
- **getErrors(errors, json, field, type)** → CcpJsonRepresentation (default): Adiciona erro ao JSON e aplica estratégia de tratamento.
- **updateRuleExplanation(allRules, field, type)** → CcpJsonRepresentation (default): Adiciona explicação ao JSON de regras.
- **hasRuleExplanation(field, type)** → boolean: Indica se a regra está ativa.

---

## Classe: CcpJsonCopyGlobalValidationsFrom
**Pacote:** com.ccp.json.validations.global.annotations
**Tipo:** anotação
**Propósito:** Indica que as validações globais da classe devem ser herdadas de outra classe.

### Atributos:
- **value()** → Class&lt;?&gt;: Classe de origem das validações globais.

---

## Classe: CcpJsonGlobalValidations
**Pacote:** com.ccp.json.validations.global.annotations
**Tipo:** anotação
**Propósito:** Define as validações globais do JSON de entrada: grupos de campos com pelo menos um obrigatório, grupos de todos-ou-nenhum, e validadores customizados de classe.

### Atributos:
- **requiresAtLeastOne()** → CcpJsonValidationFieldList[]: Grupos onde ao menos um campo é obrigatório.
- **requiresAllOrNone()** → CcpJsonValidationFieldList[]: Grupos onde todos ou nenhum deve estar presente.
- **customJsonValidators()** → Class[]: Validadores customizados de nível de classe.

---

## Classe: CcpJsonValidationFieldList
**Pacote:** com.ccp.json.validations.global.annotations
**Tipo:** anotação
**Propósito:** Define uma lista de nomes de campos para uso em grupos de validação global.

### Atributos:
- **value()** → String[]: Nomes dos campos do grupo.

---

## Classe: CcpJsonValidationError
**Pacote:** com.ccp.json.validations.global.engine
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando a validação de um JSON falha. Carrega todos os dados diagnósticos: JSON fornecido, erros, explicação das regras, nome da funcionalidade e classe de regras.

### Métodos:
- **CcpJsonValidationError(clazz, givenJson, errors, rulesExplanation, featureName)** → construtor: Monta o JSON de erro completo como mensagem da exceção.

---

## Classe: CcpJsonValidationRulesEngine
**Pacote:** com.ccp.json.validations.global.engine
**Tipo:** classe (singleton)
**Propósito:** Engine de geração de explicações de regras. Percorre uma classe de validação (anotações globais e de campo) e produz um JSON descrevendo todas as regras ativas.

### Métodos:
- **getRulesExplanation(clazz)** → CcpJsonRepresentation: Combina explicações globais e por campo, retornando o JSON completo de regras.

---

## Classe: CcpJsonValidatorEngine
**Pacote:** com.ccp.json.validations.global.engine
**Tipo:** classe (singleton)
**Propósito:** Engine principal de validação de JSONs. Orquestra validações globais (classe) e por campo. Lança `CcpJsonValidationError` se erros forem encontrados.

### Métodos:
- **validateJson(clazz, json, featureName)** → CcpJsonRepresentation: Valida o JSON; lança exceção em caso de erro ou retorna o JSON original.
- **getJsonFieldType(field)** → CcpJsonFieldType: Inspeciona anotações e retorna o tipo de campo correspondente.
- **getReplacedField(field)** → Field: Retorna o campo de referência se `@CcpJsonCopyFieldValidationsFrom` estiver presente.

---

## Classe: CcpJsonValidatorErrorBreakValidationsToTheClass
**Pacote:** com.ccp.json.validations.global.engine
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção de controle de fluxo para interromper validações de nível de classe quando um validador crítico encontra erro.

### Métodos:
- **CcpJsonValidatorErrorBreakValidationsToTheClass(errors)** → construtor: Armazena o JSON de erros acumulado.

---

## Classe: CcpJsonValidatorDefaults
**Pacote:** com.ccp.json.validations.global.enums
**Tipo:** enum (implementa CcpJsonValidator)
**Propósito:** Validações globais padrão do framework: presença de ao menos um campo de um grupo e consistência "todos ou nenhum".

### Constantes:
- **requiredAtLeastOne**: Valida que pelo menos um campo de cada grupo em `requiresAtLeastOne` está presente.
- **requiresAllOrNone**: Valida que todos ou nenhum dos campos de cada grupo em `requiresAllOrNone` estão presentes.

---

## Classe: CcpJsonValidator
**Pacote:** com.ccp.json.validations.global.interfaces
**Tipo:** interface
**Propósito:** Contrato para validadores de nível de classe. O método default `getErrors` orquestra coleta de erros e aciona interrupção quando a validação é crítica.

### Métodos:
- **hasError(json, clazz)** → boolean: Verifica se a validação global falha.
- **getErrorMessage(json, clazz)** → Object: Gera mensagens de erro.
- **isCriticalValidation(json, clazz)** → boolean: Indica se deve interromper as demais validações ao encontrar erro.
- **getRuleExplanation(clazz)** → Object: Explica as regras globais.
- **getErrors(errors, json, clazz)** → CcpJsonRepresentation (default): Acumula erros; lança `CcpJsonValidatorErrorBreakValidationsToTheClass` se crítico.


---

# Documentação do Módulo `jn_business_jobsnow`

> Gerado em 2026-06-09. Módulo central de negócio da plataforma JobsNow.

---

## Índice

1. [Business — HTTP](#business--http)
2. [Business — Login](#business--login)
3. [Business — Mensagens](#business--mensagens)
4. [Bulk — Operações em lote](#bulk--operações-em-lote)
5. [Entidades](#entidades)
6. [Decoradores de entidade](#decoradores-de-entidade)
7. [Transformadores de campo](#transformadores-de-campo)
8. [Exceções](#exceções)
9. [Campos de validação JSON](#campos-de-validação-json)
10. [Mensageria](#mensageria)
11. [Builder de mensagens (fluent API)](#builder-de-mensagens-fluent-api)
12. [Serviços (Services)](#serviços-services)
13. [Status de processo](#status-de-processo)
14. [Utilitários](#utilitários)

---

## Business — HTTP

---

## Classe: JnBusinessSendHttpRequest
**Pacote:** `com.jn.business.http`
**Tipo:** classe
**Propósito:** Executa chamadas HTTP encapsulando um `CcpHttpApiExecutor` e aplica política de retentativa automática para erros de servidor (5xx). Erros de cliente (4xx) são registrados em `JnEntityHttpApiErrorClient` e relançados imediatamente; erros de servidor disparam novas tentativas controladas, com sleep entre elas, até atingir o limite máximo, quando então o erro é registrado em `JnEntityHttpApiErrorServer` e relançado.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Executa a requisição HTTP. Captura `CcpErrorHttpClient` para salvar o detalhe do erro e relançar; captura `CcpErrorHttpServer` para iniciar a lógica de retry.
- **retryToSendIntantMessage(CcpErrorHttp e, CcpJsonRepresentation json, CcpJsonRepresentation httpErrorDetails)** → `CcpJsonRepresentation` *(privado)*: Verifica se o número máximo de tentativas foi excedido (via `JnEntityHttpApiRetrySendRequest.exceededTries`). Se excedeu, salva em `JnEntityHttpApiErrorServer` e relança; caso contrário, aguarda o tempo configurado e chama `execute(json)` recursivamente.

---

## Business — Login

---

## Classe: JnBusinessEvaluateAttempts
**Pacote:** `com.jn.business.login`
**Tipo:** classe
**Propósito:** Avalia tentativas de autenticação (senha ou token) comparando o valor fornecido pelo usuário com o armazenado no banco, via `CcpPasswordHandler.matches`. Se correto, delega ao business de sucesso. Se incorreto, incrementa o contador de tentativas; ao atingir 3 tentativas erradas, aciona o business de bloqueio e lança `CcpErrorFlowDisturb` com o status de excesso de tentativas; antes disso, lança o status de "tipo errado" com o número de tentativas atual.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Busca o segredo no banco, compara com o valor do usuário usando `CcpPasswordHandler`, e controla o fluxo de sucesso/bloqueio/tentativas.

---

## Classe: JnBusinessExecuteLogin
**Pacote:** `com.jn.business.login`
**Tipo:** classe (Singleton)
**Propósito:** Executa o login do usuário após validação bem-sucedida da senha. Em operação bulk atômica: renomeia `sessionToken` para o campo `token` de sessão, invalida a senha atual (transferindo para entidade twin `login_password_locked`), apaga o registro de tentativas de senha, registra o login criando a sessão válida e o possível conflito de sessão.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Orquestra a operação bulk de login (unlock de senha, remoção de tentativas, registro de sessão) e retorna JSON vazio ao concluir.

---

## Classe: JnBusinessExecuteLogout
**Pacote:** `com.jn.business.login`
**Tipo:** classe (Singleton)
**Propósito:** Executa o logout do usuário. Em operação bulk atômica: transfere a sessão ativa para a entidade twin `login_session_terminated` (invalidando a sessão) e remove qualquer registro de conflito de sessão.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Realiza o logout via bulk, invalidando a sessão e limpando o cache. Retorna JSON vazio.

---

## Classe: JnBusinessSavePassword
**Pacote:** `com.jn.business.login`
**Tipo:** classe (Singleton)
**Propósito:** Salva (ou altera) a senha do usuário. Em operação bulk atômica: invalida a sessão atual, salva a nova senha, "desbloqueia" a senha transferindo para a entidade twin, remove as tentativas de senha falhas, registra novo login e resolve conflito de sessão se existir.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Executa todas as operações de atualização de senha e sessão em lote. Retorna JSON vazio.
- **getJsonValidationClass()** → `Class<?>`: Retorna a classe de validação JSON definida em `JnServiceLogin.SavePassword`.

---

## Classe: JnBusinessSendUserToken
**Pacote:** `com.jn.business.login`
**Tipo:** classe (Singleton, estende `JnBusinessSendMessage`)
**Propósito:** Envia o token de acesso ao usuário (via email e/ou mensagem instantânea). Antes de delegar ao `JnBusinessSendMessage`, prepara o JSON mesclando dados do request com os dados de contexto, gera o hash do token, duplica o email para o campo `chatId` do mensageiro instantâneo e o token para o campo `botName`.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Prepara o JSON com transformações de campos (hash de token, mapeamento email→chatId) e delega o envio ao método `apply` da superclasse.

---

## Classe: JnBusinessSessionValidate
**Pacote:** `com.jn.business.login`
**Tipo:** classe (Singleton)
**Propósito:** Valida se a sessão do usuário está ativa, verificando a presença e validade do `sessionToken`. Controla tentativas de uso de token de sessão inválido: após 3 tentativas inválidas, bloqueia a senha. Se o token de sessão for válido, zera o contador de tentativas.

### Campos de validação JSON:
- `sessionToken`: string com exatamente 8 caracteres (obrigatório)
- `email`: validado conforme `JnJsonCommonsFields` (obrigatório)

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Verifica a presença do `sessionToken`, duplica para o campo `token` de sessão, configura callbacks de incremento/reset de tentativas e executa o fluxo de validação de sessão.
- **getJsonValidationClass()** → `Class<?>`: Retorna `JsonFieldNames.class` para validação de entrada.

---

## Classe: JnBusinessResendLoginToken
**Pacote:** `com.jn.business.login.solve.token`
**Tipo:** classe (Singleton)
**Propósito:** Reenvia o token de login para o usuário. Configura temporariamente o `CcpDependencyInjection` substituindo o remetente por `JnBusinessSendUserToken` e acrescenta o tipo do assunto ao JSON antes de disparar o envio.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Adiciona o campo `subjectType` com o nome da classe `JnBusinessSendUserToken`, substitui dependências temporariamente e executa o reenvio.
- **getJsonValidationClass()** → `Class<?>`: Retorna `JsonFieldNames.class`.

---

## Classe: JnBusinessResetLoginToken
**Pacote:** `com.jn.business.login.solve.token`
**Tipo:** classe (Singleton)
**Propósito:** Reseta (exclui de todos os índices) o token de login de um usuário. Útil para forçar a geração de um novo token, limpando o estado anterior.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Delega para `JnEntityLoginToken.ENTITY.deleteAnyWhere(json)`, removendo o token independentemente de qual índice/shard esteja.
- **getJsonValidationClass()** → `Class<?>`: Retorna `JsonFieldNames.class`.

---

## Business — Mensagens

---

## Classe: JnBusinessNotifyContactUs
**Pacote:** `com.jn.business.messages`
**Tipo:** classe (Singleton)
**Propósito:** Notifica o suporte sobre um novo contato recebido (formulário "Fale Conosco"). Utiliza `JnBusinessNotifySupport` com a entidade `JnEntityContactUs` como entidade de bloqueio de reenvio e `JnSendMessageToUser` como remetente.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Delega para `JnBusinessNotifySupport.apply(...)` passando a entidade de contato como base e retorna o JSON original.

---

## Classe: JnBusinessNotifyError
**Pacote:** `com.jn.business.messages`
**Tipo:** classe (Singleton)
**Propósito:** Notifica o suporte sobre um erro ocorrido no sistema. Utiliza `JnBusinessNotifySupport` com a entidade `JnEntityJobsnowError` e `JnSendMessageIgnoringProcessErrors` (que tolera falhas no envio e as registra como warning).

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Envia notificação de erro ao suporte usando o JSON com detalhes do erro.
- **apply(Throwable e)** → `CcpJsonRepresentation`: Sobrecarga que converte uma `Throwable` em `CcpJsonRepresentation` e delega para o método `apply(json)`.

---

## Classe: JnBusinessNotifySupport
**Pacote:** `com.jn.business.messages`
**Tipo:** classe (Singleton)
**Propósito:** Classe central de notificação ao suporte. Lê a propriedade `supportLanguage` do `application_properties`, lança `JnErrorSupportLanguageIsMissing` se ausente, e usa o builder fluent `JnSendMessageToUser` para enviar email e mensagem instantânea ao suporte. Após o envio, salva o resultado em `JnEntityJobsnowPenddingError`.

### Métodos:
- **apply(CcpJsonRepresentation json, String topic, CcpEntity entityToSaveError, JnSendMessageToUser sender)** → `CcpJsonRepresentation`: Configura e executa o envio de notificação ao suporte, garantindo a presença do idioma configurado e registrando o erro pendente.

---

## Classe: JnBusinessSendEmailMessage
**Pacote:** `com.jn.business.messages`
**Tipo:** classe (Singleton, implementa `CcpHttpApiExecutor`)
**Propósito:** Envia um email usando o provedor configurado via `CcpEmailSender` (ex: SendGrid). Extrai do JSON os parâmetros de envio (token, URL, templateId, remetente, assunto, corpo com resolução de template, tipo de conteúdo, destinatários) e registra o envio em `JnEntityEmailMessageSent`.

### Campos (enum `Fields`):
- `email`: destinatário único
- `emails`: lista de destinatários

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Obtém os parâmetros de email do JSON e das propriedades do sistema, resolve o template da mensagem, envia via `CcpEmailSender` e salva o registro de envio.

---

## Classe: JnBusinessSendInstantMessage
**Pacote:** `com.jn.business.messages`
**Tipo:** classe (Singleton, implementa `CcpHttpApiExecutor`)
**Propósito:** Envia mensagens instantâneas via Telegram usando bots configurados. Suporta dois tipos de mensagem (`text` e `file`) definidos no enum interno `JnInstantMessageType`. Implementa retentativa em caso de `CcpHttpTooManyRequests` e trata o bloqueio do bot pelo usuário salvando em `JnEntityInstantMessengerBotLocked`.

### Enums internos:
- **`JnBotType`**: `support`, `user` — identifica o tipo de bot remetente.
- **`JnJsonValidator`**: campos primários obrigatórios: `botName`, `chatId`, `instantMessageType`, `templateId`.
- **`JnMessageTextJsonValidator`**: campos para mensagens de texto: `message`, `botToken`.
- **`JnMessageFileJsonValidator`**: campos para mensagens de arquivo: `caption`, `contentType`, `message`, `botToken`, `fileName`.
- **`JnInstantMessageType`**: `text` e `file` — cada valor implementa `sendMessage(...)` de forma específica.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Obtém o token do bot via `JnSystemProperties`, determina o tipo de mensagem, tenta enviar e trata exceções de rate-limit e bloqueio de bot.
- **retryToSendMessage(CcpJsonRepresentation json)** → `CcpJsonRepresentation` *(privado)*: Incrementa o contador de tentativas, aguarda o tempo de sleep e tenta reenviar até o limite máximo.
- **saveBlockedBot(CcpJsonRepresentation putAll, String token)** → `CcpJsonRepresentation` *(privado)*: Registra que o bot foi bloqueado pelo usuário em `JnEntityInstantMessengerBotLocked`.
- **getJsonValidationClass()** → `Class<?>`: Retorna `JnJsonValidator.class`.

---

## Classe: JnBusinessSendMessage
**Pacote:** `com.jn.business.messages`
**Tipo:** classe abstrata (base para envio de mensagens)
**Propósito:** Classe base para envio de mensagens que combina envio por email e por mensagem instantânea usando o builder fluent `JnSendMessageToUser`. O `templateId` é o nome da classe concreta que a estende; a entidade de bloqueio de reenvio é fornecida pelo construtor.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Cria um `JnSendMessageToUser`, configura os passos padrão de email e mensagem instantânea, e aciona o envio com o templateId, entidade de bloqueio, valores do JSON e idioma.

---

## Classe: JnMessages
**Pacote:** `com.jn.business.messages`
**Tipo:** classe contêiner com classes internas estáticas
**Propósito:** Agrupa templates de notificação ao suporte para situações específicas de solicitação de token. Cada classe interna estende `JnBusinessSendMessage` associando-a à entidade de bloqueio de reenvio correspondente.

### Classes internas:
- **`NotifySupportAboutPendingResendLoginToken`**: Notifica o suporte sobre uma solicitação pendente de reenvio de token de login. Usa `JnEntityLoginTokenRequestResend.ENTITY` como entidade de bloqueio.
- **`NotifySupportAboutPendingUnlockLoginToken`**: Notifica o suporte sobre uma solicitação pendente de desbloqueio de token de login. Usa `JnEntityLoginTokenRequestUnlock.ENTITY` como entidade de bloqueio.

---

## Bulk — Operações em lote

---

## Classe: FunctionReprocessMapper
**Pacote:** `com.jn.db.bulk`
**Tipo:** classe (Singleton, implementa `Function<CcpBulkOperationResult, CcpJsonRepresentation>`)
**Propósito:** Função de mapeamento usada pelo `JnExecuteBulkOperation` para converter um resultado de operação bulk com erro em um registro de reprocessamento (`JnEntityRecordToReprocess`). Previne loops infinitos ao rejeitar itens que já pertencem à entidade de reprocessamento.

### Métodos:
- **apply(CcpBulkOperationResult result)** → `CcpJsonRepresentation`: Extrai detalhes do item bulk com erro, adiciona timestamp atual, renomeia o campo `type` para `errorType` e monta o JSON no formato de `JnEntityRecordToReprocess`. Lança `RuntimeException` se o item for da própria entidade de reprocessamento (prevenção de loop).

---

## Classe: JnExecuteBulkOperation
**Pacote:** `com.jn.db.bulk`
**Tipo:** classe (Singleton, implementa `CcpExecuteBulkOperation`)
**Propósito:** Orquestra operações bulk no Elasticsearch do JobsNow. Sanitiza itens duplicados resolvendo conflitos por prioridade, executa o bulk via `CcpBulkExecutor`, processa os resultados de erro criando registros de reprocessamento recursivamente, e invalida as chaves de cache correspondentes aos itens processados com sucesso.

### Métodos:
- **executeBulk(Collection\<CcpBulkItem\> bulkItems, Consumer\<String[]\> functionToDeleteKeysInTheCache)** → `JnExecuteBulkOperation`: Sanitiza os itens, executa o bulk e processa erros/cache.
- **executeBulk(CcpJsonRepresentation json, CcpBulkEntityOperationType operation, Consumer\<String[]\> functionToDeleteKeysInTheCache, CcpEntity...entities)** → `JnExecuteBulkOperation`: Converte as entidades em `CcpBulkItem` a partir do JSON e da operação informada, e delega para o método anterior.
- **sanitizeItems(Collection\<CcpBulkItem\> bulkItems)** → `HashSet<CcpBulkItem>` *(privado)*: Remove itens com prioridade nula e resolve conflitos de duplicidade mantendo o item com maior prioridade de operação.
- **commitAndSaveErrorsAndDeleteRecordsFromCache(CcpBulkExecutor, Consumer)** → `JnExecuteBulkOperation` *(privado)*: Executa o commit, separa erros, mapeia para registros de reprocessamento (recursão) e invalida cache.
- **deleteKeysFromCache(List\<CcpBulkOperationResult\>)** → `JnExecuteBulkOperation` *(privado)*: Coleta as chaves de cache dos itens sem erro e as deleta.

---

## Classe: JnBulkHandlerRegisterLogin
**Pacote:** `com.jn.db.bulk.handlers`
**Tipo:** classe (Singleton, implementa `CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>`)
**Propósito:** Handler de bulk responsável por registrar o login do usuário. Independentemente de haver ou não conflito de sessão existente, cria itens bulk para salvar um novo registro em `JnEntityLoginSessionConflict` e em `JnEntityLoginSessionValidation`.

### Métodos:
- **whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound)** → `List<CcpBulkItem>`: Retorna os bulk items para criação de sessão e conflito quando já havia um registro.
- **whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json)** → `List<CcpBulkItem>`: Retorna os mesmos bulk items quando não havia registro anterior.
- **getEntityToSearch()** → `CcpEntity`: Retorna `JnEntityLoginSessionConflict.ENTITY` como entidade de pesquisa.

---

## Classe: JnBulkHandlerSolveLoginConflict
**Pacote:** `com.jn.db.bulk.handlers`
**Tipo:** classe (Singleton, implementa `CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>`)
**Propósito:** Handler de bulk que resolve conflito de sessão: se um registro de conflito existir para o email, gera bulk item para deletá-lo; caso não exista, não faz nada.

### Métodos:
- **whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound)** → `List<CcpBulkItem>`: Gera item bulk de exclusão do conflito de sessão existente.
- **whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json)** → `List<CcpBulkItem>`: Retorna lista vazia (sem conflito a resolver).
- **getEntityToSearch()** → `CcpEntity`: Retorna `JnEntityLoginSessionConflict.ENTITY`.

---

## Entidades

> Todas as entidades implementam `CcpEntityConfigurator` e possuem um campo estático `ENTITY` do tipo `CcpEntity` (instância criada pela `CcpEntityFactory`). Os campos são definidos no enum interno `Fields`. As anotações de classe controlam: cache (`@CcpEntityCache`), expiração (`@CcpEntityDisposable`), histórico (`@CcpEntityVersionable`), entidade espelho (`@CcpEntityTwin`), escrita assíncrona (`@CcpEntityAsyncWriter`), somente leitura (`@CcpEntityOlyReadable`), transformadores e validadores de campos.

---

## Classe: JnEntityAsyncTask
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade)
**Propósito:** Representa uma tarefa assíncrona disparada via mensageria. Registra o ciclo de vida da tarefa: início (`started`), fim (`finished`), tempo decorrido (`enlapsedTime`), dados, tópico, request original, id da mensagem no PubSub, se foi bem-sucedido e qual operação foi executada.

### Campos (`Fields`):
| Campo | Descrição |
|---|---|
| `started` | Timestamp de início da tarefa (obrigatório, numérico) |
| `finished` | Timestamp de conclusão (numérico) |
| `enlapsedTime` | Tempo decorrido em ms (unsigned) |
| `data` | Dados textuais da tarefa (obrigatório, string) |
| `topic` | Tópico/classe de negócio (obrigatório, string) |
| `request` | JSON da requisição original (obrigatório) |
| `messageId` | ID da mensagem no PubSub (chave primária, string) |
| `success` | Se a tarefa foi bem-sucedida (booleano) |
| `operation` | Operação executada |
| `response` | Resposta da execução |

---

## Classe: JnEntityContactUs
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityCache(3600)`, `@CcpEntityTwin("contact_us_solved")`)
**Propósito:** Representa uma solicitação de contato recebida pelo formulário "Fale Conosco". Possui entidade twin `contact_us_solved` para quando o contato for resolvido. Cache de 1 hora.

### Campos (`Fields`):
| Campo | Descrição |
|---|---|
| `subjectType` | Tipo do assunto (chave primária) |
| `email` | Email do usuário que entrou em contato (chave primária) |
| `subject` | Assunto da mensagem (obrigatório) |
| `chatId` | ID do chat Telegram (obrigatório) |
| `sender` | Remetente (obrigatório) |

---

## Classe: JnEntityContactUsIgnored
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityDisposable(daily)`, `@CcpEntityCache(86400)`, `@CcpEntityTwin("contact_us_reread")`)
**Propósito:** Registra contatos ignorados pelo suporte. Entidade descartável com expiração diária. Possui twin `contact_us_reread` para reaproveitamento posterior.

### Campos (`Fields`):
| Campo | Descrição |
|---|---|
| `email` | Email do usuário cujo contato foi ignorado (chave primária) |

---

## Classe: JnEntityContactUsSkiped
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityDisposable(daily)`, `@CcpEntityCache(86400)`)
**Propósito:** Registra contatos pulados (skipped) pelo suporte. Similar a `JnEntityContactUsIgnored` mas sem entidade twin — representa um estado diferente de triagem do contato.

### Campos (`Fields`):
| Campo | Descrição |
|---|---|
| `email` | Email do usuário cujo contato foi pulado (chave primária) |

---

## Classe: JnEntityDisposableRecord
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityOlyReadable`)
**Propósito:** Registro de expiração (disposable) de outras entidades. Armazena uma cópia do JSON de uma entidade com timestamp de expiração. Entidade auxiliar usada por `JnDisposableEntity` para implementar TTL (time-to-live) sem depender de recurso nativo do Elasticsearch. Somente leitura (não pode ser gravada diretamente pelas operações normais).

### Campos (`Fields`):
| Campo | Descrição |
|---|---|
| `timestamp` | Timestamp de expiração (não atualizável, obrigatório) |
| `format` | Formato de data usado (não atualizável, obrigatório) |
| `entity` | Nome da entidade dona do registro (chave primária) |
| `date` | Data formatada de expiração (não atualizável, obrigatório) |
| `json` | JSON completo do registro original (obrigatório) |
| `id` | ID do registro original (chave primária) |

### Métodos estáticos:
- **getDataWithTimeStamp(CcpEntity entity, CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Busca o registro de expiração de uma entidade e, se encontrado, retorna os dados enriquecidos com `expirationDate` e `dateItWasSaved`.
- **getDataWithTimeStamp(CcpJsonRepresentation oneById)** → `CcpJsonRepresentation`: A partir de um registro de expiração já carregado, calcula as datas formatadas de criação e expiração e retorna o JSON interno enriquecido.

---

## Classe: JnEntityDisposableTest
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityDisposable(hourly)`)
**Propósito:** Entidade descartável de uso exclusivo para testes automatizados. Expira a cada hora. Permite verificar o comportamento do sistema de TTL sem impactar dados reais.

### Campos (`Fields`):
| Campo | Descrição |
|---|---|
| `email` | Email de referência para o teste (chave primária) |

---

## Classe: JnEntityEmailMessageSent
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityDisposable(daily)`, `@CcpEntityCache(3600)`)
**Propósito:** Registra os emails enviados pela plataforma para impedir reenvio duplicado dentro do mesmo período. A chave composta (subjectType + email) garante que o mesmo tipo de mensagem não seja enviado duas vezes ao mesmo destinatário no mesmo dia.

### Campos (`Fields`):
| Campo | Descrição |
|---|---|
| `subjectType` | Tipo/contexto do email (chave primária) |
| `email` | Email do destinatário (chave primária) |
| `subject` | Assunto do email (obrigatório) |
| `sender` | Remetente do email (obrigatório) |

---

## Classe: JnEntityEmailParametersToSend
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityVersionable`, `@CcpEntityCache(3600)`)
**Propósito:** Armazena os parâmetros de configuração para o envio de emails: remetente, templateId, tipo de assunto, parâmetros adicionais (ex: links para LinkedIn, Telegram) e tipo de conteúdo. Possui registros iniciais pré-cadastrados via `getFirstRecordsToInsert()` para os dois principais contextos de email: notificação de erro e envio de token de login.

### Campos (`Fields`):
| Campo | Descrição |
|---|---|
| `email` | Email do destinatário (sem transformação de hash) |
| `sender` | Email do remetente (obrigatório) |
| `templateId` | ID do template a usar (chave primária) |
| `subjectType` | Tipo do assunto/contexto (obrigatório) |
| `moreParameters` | JSON com parâmetros adicionais para o template |
| `contentType` | Tipo de conteúdo do email |

### Métodos:
- **getFirstRecordsToInsert()** → `List<CcpBulkItem>`: Retorna os registros iniciais para os contextos `JnBusinessNotifyError` e `JnBusinessSendUserToken`.

---

## Classe: JnEntityEmailReportedAsSpam
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityCache(3600)`)
**Propósito:** Registra emails reportados como spam pelos destinatários. Utilizada pelo sistema de envio de mensagens para bloquear reenvios para destinatários que marcaram emails como spam.

### Campos (`Fields`): `subjectType` (PK), `email` (PK), `subject` (obrigatório), `sender` (obrigatório).

---

## Classe: JnEntityEmailTemplateMessage
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityVersionable`, `@CcpEntityCache(3600)`)
**Propósito:** Armazena os templates de email por idioma e por templateId. O campo `message` suporta variáveis de template (ex: `{token}`, `{email}`) resolvidas em tempo de envio. Possui registros iniciais para o template de token de acesso (em português) e o template de notificação de erro.

### Campos (`Fields`): `templateId` (PK), `language` (PK), `subject` (obrigatório), `message` (obrigatório, corpo HTML do email).

### Métodos:
- **getFirstRecordsToInsert()** → `List<CcpBulkItem>`: Retorna os templates iniciais: token de login com instruções de acesso e template de erro com placeholders `{type}`, `{msg}`, `{stackTrace}`, `{cause}`.

---

## Classe: JnEntityHttpApiErrorClient
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityDisposable(hourly)`, `@CcpEntityCache(3600)`)
**Propósito:** Registra erros HTTP de cliente (4xx) ocorridos durante chamadas a APIs externas. Expiração horária (registros temporários para diagnóstico).

### Campos (`Fields`): `url` (PK), `method` (PK), `headers` (PK), `request`, `apiName` (PK), `details` (PK), `response`, `httpStatus`, `date`, `timestamp` (obrigatório).

---

## Classe: JnEntityHttpApiErrorServer
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityDisposable(hourly)`, `@CcpEntityCache(3600)`)
**Propósito:** Registra erros HTTP de servidor (5xx) ocorridos após esgotar todas as tentativas de retry ao chamar APIs externas. Igual estruturalmente a `JnEntityHttpApiErrorClient`, mas com `httpStatus`, `timestamp` e `date` obrigatórios.

### Campos (`Fields`): `url` (PK), `method` (PK), `headers` (PK), `request`, `apiName` (PK), `details` (PK), `response`, `httpStatus` (obrigatório), `timestamp` (obrigatório), `date` (obrigatório).

---

## Classe: JnEntityHttpApiRetrySendRequest
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityDisposable(hourly)`, `@CcpEntityCache(3600)`)
**Propósito:** Controla o número de tentativas de reenvio de uma requisição HTTP que falhou com erro de servidor. Cada tentativa gera um registro com a mesma chave acrescida do número da tentativa (1, 2, 3...).

### Campos (`Fields`): `url` (PK), `method` (PK), `headers` (PK), `request`, `apiName` (PK), `attempts` (PK), `response`, `httpStatus`, `timestamp` (obrigatório), `date` (obrigatório), `details` (obrigatório).

### Métodos estáticos:
- **exceededTries(CcpJsonRepresentation json, String fieldName, int limit)** → `boolean`: Tenta salvar registros para cada número de tentativa de 1 até `limit`; se algum não existir ainda, salva-o e retorna `false`; se todos existirem, retorna `true` (limite excedido).

---

## Classe: JnEntityInstantMessengerBotLocked
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityCache(3600)`)
**Propósito:** Registra combinações de bot + usuário (chatId) em que o bot foi bloqueado pelo usuário. Utilizada pelo sistema de envio de mensagens instantâneas para evitar novas tentativas para destinatários que bloquearam o bot.

### Campos (`Fields`): `botName` (PK), `chatId` (PK), `subjectType` (obrigatório).

---

## Classe: JnEntityInstantMessengerMessageSent
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityDisposable(hourly)`, `@CcpEntityCache(3600)`)
**Propósito:** Registra mensagens instantâneas enviadas com sucesso. O campo `message` é armazenado como hash SHA-1 para funcionar como chave primária e evitar reenvio da mesma mensagem para o mesmo destinatário na mesma hora.

### Campos (`Fields`): `botName` (PK), `chatId` (PK), `templateId` (PK), `instantMessageType` (obrigatório), `caption`, `contentType`, `fileName`, `message` (PK — transformado em hash SHA-1).

---

## Classe: JnEntityInstantMessengerParametersToSend
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityVersionable`, `@CcpEntityCache(3600)`)
**Propósito:** Armazena parâmetros de configuração para envio de mensagens instantâneas: qual bot usar, o templateId, chatId de destino, tipo de mensagem, e parâmetros adicionais como número máximo de tentativas e tempo de sleep entre elas. Possui registro inicial para envio de notificações de erro ao bot de suporte.

### Campos (`Fields`): `botName` (PK), `templateId` (PK), `chatId` (obrigatório), `instantMessageType` (obrigatório), `caption`, `contentType`, `fileName`, `moreParameters`.

### Métodos:
- **getFirstRecordsToInsert()** → `List<CcpBulkItem>`: Retorna o registro inicial configurando o bot de suporte para envio de erros como arquivo texto (tipo `file`, chatId do desenvolvedor, até 10 tentativas, 3 segundos de sleep entre elas).

---

## Classe: JnEntityInstantMessengerTemplateMessage
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityVersionable`, `@CcpEntityCache(3600)`)
**Propósito:** Armazena templates de mensagens instantâneas por idioma e templateId. O `message` suporta variáveis de template. Possui registro inicial para o template de notificação de erro.

### Campos (`Fields`): `templateId` (PK), `language` (PK), `message` (obrigatório).

### Métodos:
- **getFirstRecordsToInsert()** → `List<CcpBulkItem>`: Retorna o template de erro em português com placeholders `{type}`, `{msg}`, `{stackTrace}`, `{cause}`.

---

## Classe: JnEntityJobsnowError
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityDisposable(hourly)`, `@CcpEntityCache(3600)`)
**Propósito:** Registra erros ocorridos no sistema JobsNow. A chave composta por `stackTraceHash` e `type` evita registros duplicados do mesmo erro. Campos `cause` e `stackTrace` são arrays. Expiração horária (erros frequentes não acumulam indefinidamente).

### Campos (`Fields`): `cause` (array), `stackTrace` (array), `stackTraceHash` (PK, string), `type` (PK), `message` (obrigatório).

---

## Classe: JnEntityJobsnowPenddingError
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityVersionable`, `@CcpEntityCache(3600)`, `@CcpEntityTwin("jobsnow_solved_error")`)
**Propósito:** Registra erros pendentes de resolução pela equipe de suporte. Diferente de `JnEntityJobsnowError` (que é efêmero), este possui versionamento e entidade twin `jobsnow_solved_error`, permitindo rastrear o ciclo de vida do erro até sua resolução.

### Campos (`Fields`): `cause` (array), `stackTrace` (array), `stackTraceHash` (PK), `type` (PK), `message` (obrigatório), `timestamp`, `date`.

---

## Classe: JnEntityJobsnowWarning
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityDisposable(hourly)`, `@CcpEntityCache(3600)`)
**Propósito:** Registra alertas (warnings) do sistema — situações não críticas que merecem atenção mas não interrompem o fluxo. Usado por `JnSendMessageAndJustErrors` e `JnSendMessageIgnoringProcessErrors` para registrar falhas no envio de mensagens sem propagar a exceção.

### Campos (`Fields`): `cause`, `stackTrace` (obrigatório), `stackTraceHash`, `type` (PK), `message` (obrigatório).

---

## Classe: JnEntityLoginAnswers
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityVersionable`, `@CcpEntityCache(3600)`)
**Propósito:** Armazena as respostas do questionário de cadastro do usuário, coletadas durante o onboarding: por qual canal o usuário chegou (`channel`) e qual seu objetivo na plataforma (`goal`).

### Campos (`Fields`):
| Campo | Descrição |
|---|---|
| `email` | Email do usuário (chave primária) |
| `channel` | Canal de chegada: `linkedin`, `telegram`, `friends`, `others` (obrigatório) |
| `goal` | Objetivo: `jobs` (candidato) ou `recruiting` (recrutador) (obrigatório) |

---

## Classe: JnEntityLoginEmail
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityCache(3600)`)
**Propósito:** Representa o cadastro de email do usuário na plataforma. Entidade simples que confirma a existência do email como usuário registrado. Sem versionamento nem expiração — o email é permanente.

### Campos (`Fields`): `email` (chave primária).

---

## Classe: JnEntityLoginPassword
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityVersionable`, `@CcpEntityCache(3600)`, `@CcpEntityTwin("login_password_locked")`)
**Propósito:** Armazena o hash da senha do usuário. A entidade twin `login_password_locked` representa o estado de senha bloqueada após múltiplas tentativas incorretas. A senha é sempre armazenada como hash (via transformador `password`).

### Campos (`Fields`): `email` (PK), `password` (hash da senha, obrigatório).

---

## Classe: JnEntityLoginPasswordAttempts
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityDisposable(daily)`, `@CcpEntityCache(86400)`)
**Propósito:** Contador diário de tentativas incorretas de senha por usuário. Expiração diária garante que o bloqueio por tentativas seja resetado automaticamente a cada dia.

### Campos (`Fields`): `email` (PK), `attempts` (contador de tentativas).

---

## Classe: JnEntityLoginSessionConflict
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityDisposable(hourly)`, `@CcpEntityCache(3600)`)
**Propósito:** Registra conflito de sessão para um email: quando um usuário tenta logar enquanto já existe uma sessão ativa de outro dispositivo/IP. Expiração horária.

### Campos (`Fields`): `email` (PK).

---

## Classe: JnEntityLoginSessionTokenAttempts
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityCache(3600)`)
**Propósito:** Contador de tentativas de uso de token de sessão inválido. Quando o limite é atingido, aciona o bloqueio da senha. Possui métodos de fábrica estáticos que retornam `CcpBusiness` para incrementar ou resetar o contador.

### Campos (`Fields`): `email` (PK), `attempts` (obrigatório).

### Métodos estáticos:
- **incrementAttempts(Integer maxAttempts, CcpBusiness whenExceedAttempts)** → `CcpBusiness`: Retorna um `CcpBusiness` lambda que lê o registro de tentativas do JSON carregado, incrementa o contador e, se atingir o máximo, aciona `whenExceedAttempts`; caso contrário, salva o novo valor.
- **resetAttempts()** → `CcpBusiness`: Retorna um `CcpBusiness` lambda que deleta o registro de tentativas se ele existir (zera o contador após validação bem-sucedida).

---

## Classe: JnEntityLoginSessionValidation
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityDisposable(hourly)`, `@CcpEntityCache(3600)`, `@CcpEntityTwin("login_session_terminated")`)
**Propósito:** Representa uma sessão de login ativa. A chave composta inclui email, token (armazenado como hash SHA-1), IP e userAgent, garantindo que cada sessão seja única por combinação de contexto. A twin `login_session_terminated` recebe a sessão ao fazer logout.

### Campos (`Fields`): `email` (PK), `token` (PK — transformado em hash SHA-1), `ip` (PK), `coordinates`, `macAddress`, `userAgent` (PK).

---

## Classe: JnEntityLoginStats
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityCache(3600)`)
**Propósito:** Armazena estatísticas de login do usuário: balanço financeiro, último acesso, contagem de acessos, tickets abertos e fechados e contagem de transações de saldo.

### Campos (`Fields`): `email` (PK, 35-50 chars), `balance` (numérico), `lastAccess` (timestamp anual), `countAccess` (unsigned), `openedTickets` (unsigned), `closedTickets` (unsigned), `balanceTransacionsCount` (unsigned).

---

## Classe: JnEntityLoginToken
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityDisposable(monthly)`, `@CcpEntityCache(86400)`, `@CcpEntityTwin("login_token_locked")`)
**Propósito:** Armazena o token de acesso enviado por email ao usuário durante o onboarding ou recuperação de senha. Expiração mensal. A twin `login_token_locked` indica token bloqueado após excesso de tentativas.

### Campos (`Fields`): `email` (PK), `token` (hash do token gerado).

---

## Classe: JnEntityLoginTokenAttempts
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityCache(3600)`)
**Propósito:** Contador de tentativas de uso incorreto do token de login (durante o fluxo de definição de senha). Usado por `JnBusinessEvaluateAttempts` para controlar o bloqueio do token.

### Campos (`Fields`): `email` (PK), `attempts` (obrigatório).

---

## Classe: JnEntityLoginTokenRequestResend
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityAsyncWriter`, `@CcpEntityDisposable(daily)`, `@CcpEntityCache(3600)`, `@CcpEntityTwin("login_token_fulfilled_resend")`)
**Propósito:** Registra a solicitação de reenvio de token de login feita por um usuário. Ao ser salvo (operação `save`), automaticamente: antes da operação, reseta e reenvia o token (`JnBusinessResetLoginToken`, `JnBusinessResendLoginToken`); depois da operação, notifica o suporte via `JnMessages.NotifySupportAboutPendingResendLoginToken`. As operações são assíncronas via mensageria.

### Campos (`Fields`): `email` (PK, sem transformação), `chatId` (ID do chat Telegram).

---

## Classe: JnEntityLoginTokenRequestUnlock
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityAsyncWriter`, `@CcpEntityDisposable(daily)`, `@CcpEntityCache(3600)`, `@CcpEntityTwin("login_token_fulfilled_unlock")`)
**Propósito:** Registra a solicitação de desbloqueio de token de login feita por um usuário. Comportamento idêntico a `JnEntityLoginTokenRequestResend` mas notifica o suporte via `JnMessages.NotifySupportAboutPendingUnlockLoginToken`. As operações são assíncronas via mensageria.

### Campos (`Fields`): `email` (PK, sem transformação), `chatId` (ID do chat Telegram).

---

## Classe: JnEntityRecordToReprocess
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityOlyReadable`)
**Propósito:** Registra operações bulk que falharam e precisam ser reprocessadas. Somente leitura — registros criados exclusivamente pelo `FunctionReprocessMapper`. Contém o JSON original, a entidade e o ID afetados, o tipo de erro e o status do reprocessamento.

### Campos (`Fields`): `timestamp` (PK), `operation` (obrigatório), `entity` (PK), `id` (PK), `json`, `status`, `reason`, `errorType`.

---

## Classe: JnEntitySystemMessage
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityCache(3600)`)
**Propósito:** Armazena mensagens do sistema exibidas na interface, indexadas por nome (`systemMessageName`) e idioma (`language`). Permite internacionalização de mensagens fixas da plataforma.

### Campos (`Fields`): `systemMessageName` (PK, string), `language` (PK, string), `message` (obrigatório, string).

---

## Classe: JnEntityVersionable
**Pacote:** `com.jn.entities`
**Tipo:** classe (entidade, `@CcpEntityOlyReadable`)
**Propósito:** Tabela de auditoria/histórico de versões. Registra cada operação (save, delete, etc.) realizada sobre entidades versionáveis, armazenando o estado anterior do JSON, a operação, a data e o ID. Somente leitura — gravada exclusivamente por `JnVersionableEntity`.

### Campos (`Fields`): `timestamp` (PK), `operation` (obrigatório), `date` (não atualizável, obrigatório), `entity` (PK), `id` (PK), `json`.

---

## Decoradores de entidade

---

## Classe: JnAsyncWriterEntity
**Pacote:** `com.jn.entities.decorators`
**Tipo:** classe (estende `CcpEntityDelegator`)
**Propósito:** Decorador que transforma operações síncronas de entidade em operações assíncronas via mensageria. Qualquer chamada de `save`, `delete`, `deleteAnyWhere`, `transferDataTo` ou `copyDataTo` é interceptada e enviada para o PubSub via `JnFunctionMensageriaSender`, permitindo processamento assíncrono.

### Métodos:
- **save(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Envia a operação `save` para a mensageria.
- **delete(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Envia a operação `delete` para a mensageria.
- **deleteAnyWhere(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Envia a operação `deleteAnyWhere` para a mensageria.
- **transferDataTo(CcpJsonRepresentation json, CcpEntity entities)** → `CcpJsonRepresentation`: Adiciona a entidade destino ao JSON e envia a operação `transferDataTo` para a mensageria.
- **copyDataTo(CcpJsonRepresentation json, CcpEntity entities)** → `CcpJsonRepresentation`: Adiciona a entidade destino ao JSON e envia a operação `copyDataTo` para a mensageria.
- **sendToMensageria(CcpJsonRepresentation json, CcpEntityOperationType operation)** → `CcpJsonRepresentation` *(privado)*: Cria o `JnFunctionMensageriaSender` com a entidade e operação e executa o envio.

---

## Classe: JnDisposableEntity
**Pacote:** `com.jn.entities.decorators`
**Tipo:** classe (estende `CcpDefaultEntityDelegator<CcpEntityDisposable>`)
**Propósito:** Decorador que implementa TTL (time-to-live) para entidades marcadas com `@CcpEntityDisposable`. Em vez de depender de TTL nativo do Elasticsearch, armazena uma cópia do JSON em `JnEntityDisposableRecord` com timestamp de expiração calculado conforme a opção de tempo configurada (`hourly`, `daily`, etc.). Sobrescreve os métodos de leitura para consultar o registro de expiração e validar se ainda está vigente.

### Métodos principais:
- **calculateId(CcpJsonRepresentation json)** → `String`: Calcula o ID considerando o timestamp formatado mais o ID original, gerando um hash SHA-1 para o período atual.
- **exists(CcpJsonRepresentation json)** → `boolean`: Verifica se o registro existe tanto na entidade original quanto no registro de expiração, validando se o timestamp ainda é futuro.
- **getOneById(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Busca o registro, priorizando a entidade original; se não encontrado, verifica o registro de expiração e retorna o JSON interno se ainda válido.
- **toBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation)** → `List<CcpBulkItem>`: Gera os bulk items da entidade original com ID substituído mais um bulk item para `JnEntityDisposableRecord`.
- **getAssociatedEntities()** → `List<CcpEntity>`: Retorna as entidades associadas incluindo `JnEntityDisposableRecord`.
- **getIdToSearchDisposableRecord(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Retorna o JSON com `entity` e `id` para buscar o registro de expiração correspondente.

---

## Classe: JnVersionableEntity
**Pacote:** `com.jn.entities.decorators`
**Tipo:** classe (estende `CcpDefaultEntityDelegator<CcpEntityVersionable>`)
**Propósito:** Decorador que adiciona versionamento/auditoria a entidades marcadas com `@CcpEntityVersionable`. A cada operação bulk, gera automaticamente um registro de histórico em `JnEntityVersionable` com o estado anterior do JSON, a operação realizada, data e hora.

### Métodos:
- **toBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation)** → `List<CcpBulkItem>`: Gera os bulk items da entidade original mais um item de auditoria em `JnEntityVersionable`.
- **getAssociatedEntities()** → `List<CcpEntity>`: Retorna as entidades associadas incluindo `JnEntityVersionable`.
- **deleteAnyWhere(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Operação não implementada (TODO); retorna o JSON sem ação.
- **getOneByIdAnyWhere(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Lança exceção (não suportado para entidades versionáveis).

---

## Transformadores de campo

---

## Classe: JnJsonTransformersFieldEntityFieldCalculateHash
**Pacote:** `com.jn.entities.fields.transformers`
**Tipo:** classe (implementa `CcpJsonTransformersDefaultEntityField`)
**Propósito:** Transformador de campo que calcula o hash SHA-1 de um campo (ex: token de sessão) e armazena tanto o valor original quanto o hash. Permite que o hash seja usado como chave primária enquanto o valor original fica disponível em outro campo.

### Classes internas:
- **`JnJsonTransformersFieldEntityTokenHash`**: Especialização que transforma o campo `token` de `JnEntityLoginSessionValidation`, lendo o token original de `originalToken`, calculando o hash e salvando em `token` e `tokenHash`.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Obtém o valor do campo, calcula o hash SHA-1 e retorna JSON com o campo substituído pelo hash e o original preservado.
- **canBePrimaryKey()** → `boolean`: Retorna `true` (o hash pode ser chave primária).
- **name()** → `String`: Retorna o nome do campo de hash resultante.

---

## Classe: JnJsonTransformersFieldEntityMessageHash
**Pacote:** `com.jn.entities.fields.transformers`
**Tipo:** classe (implementa `CcpJsonTransformersDefaultEntityField`)
**Propósito:** Transforma o campo `message` de `JnEntityInstantMessengerMessageSent` em seu hash SHA-1, armazenando o original em `originalMessage`. Permite que mensagens idênticas não sejam reenviadas (deduplicação por hash de conteúdo).

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Substitui o campo `message` pelo hash SHA-1 e preserva o original em `originalMessage`.
- **canBePrimaryKey()** → `boolean`: Retorna `true`.
- **name()** → `String`: Retorna `"messageHash"`.

---

## Enum: JnJsonTransformersFieldsEntityDefault
**Pacote:** `com.jn.entities.fields.transformers`
**Tipo:** enum (implementa `CcpJsonTransformersDefaultEntityField`)
**Propósito:** Conjunto de transformadores de campos padrão aplicados às entidades do JobsNow. Cada valor do enum aplica uma transformação específica sobre o JSON.

### Valores:
- **`email`** *(canBePrimaryKey=true)*: Valida se o valor é um email válido (lança `JnErrorIsNotAnEmail` se inválido), calcula o hash SHA-1 do email e armazena o original em `originalEmail`.
- **`password`** *(canBePrimaryKey=false)*: Calcula o hash da senha usando `CcpPasswordHandler.getHash()` (apenas se ainda não foi calculado, verificando flag `passwordAlreadyCalculated`).
- **`token`** *(canBePrimaryKey=false)*: Gera um token aleatório de 8 caracteres se não existir, calcula o hash via `CcpPasswordHandler.getHash()` e armazena o original em `originalToken`.
- **`timestamp`** *(canBePrimaryKey=true)*: Adiciona os campos `timestamp` (milissegundos) e `date` (formatado) se ainda não presentes.
- **`tokenHash`** *(canBePrimaryKey=true)*: Calcula o hash SHA-1 do token (diferente de `token` que usa BCrypt), armazena em `token` e preserva o original em `originalToken`.

### Métodos estáticos:
- **getOriginalToken()** → `String`: Gera um token aleatório de 8 caracteres usando o alfabeto `A-Z0-9`.

---

## Classe: JnJsonTransformersFieldsEntityDoNothing
**Pacote:** `com.jn.entities.fields.transformers`
**Tipo:** classe (implementa `CcpJsonTransformersDefaultEntityField`)
**Propósito:** Transformador nulo que não realiza nenhuma transformação no campo. Usado como `@CcpEntityFieldTransformer` em campos que precisam explicitamente ignorar a transformação padrão (ex: campo `email` em `JnEntityLoginTokenRequestResend` que não deve ter o email transformado em hash).

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Retorna o JSON sem modificações.
- **canBePrimaryKey()** → `boolean`: Retorna `true`.
- **name()** → `String`: Retorna `"doNothing"`.

---

## Exceções

---

## Classe: JnErrorIsNotAnEmail
**Pacote:** `com.jn.exceptions`
**Tipo:** classe (estende `CcpEntityJsonTransformerError`)
**Propósito:** Lançada pelo transformador `email` de `JnJsonTransformersFieldsEntityDefault` quando o valor do campo não é um endereço de email válido. Inclui o valor inválido e o JSON de contexto na mensagem de erro.

---

## Classe: JnErrorSupportLanguageIsMissing
**Pacote:** `com.jn.exceptions`
**Tipo:** classe (estende `RuntimeException`)
**Propósito:** Lançada por `JnBusinessNotifySupport` quando a propriedade `supportLanguage` não está configurada no `application_properties`. Indica erro de configuração do ambiente.

---

## Classe: JnErrorUnableToSendInstantMessage
**Pacote:** `com.jn.exceptions`
**Tipo:** classe (estende `RuntimeException`)
**Propósito:** Lançada por `JnBusinessSendInstantMessage` quando todas as tentativas de envio de mensagem instantânea foram esgotadas sem sucesso. Inclui o JSON com os detalhes da mensagem na mensagem de erro.

---

## Campos de validação JSON

---

## Enum: JnJsonCommonsFields
**Pacote:** `com.jn.json.fields.validation`
**Tipo:** enum
**Propósito:** Centraliza as definições de validação dos campos JSON comuns a diversas entidades e serviços do JobsNow. Cada valor do enum carrega anotações de validação de tipo (`@CcpJsonFieldTypeString`, `@CcpJsonFieldTypeNumber`, etc.) que são referenciadas via `@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)`.

### Campos definidos:
`request` (string), `password` (regex de senha forte: min 8 chars, maiúscula, minúscula, número e especial), `description` (10-500 chars), `explanation` (10-500 chars), `operation` (string, permite vazio), `response` (max 500), `timestamp` (unsigned), `date` (exatamente 23 chars), `entity` (max 50), `id` (string), `json` (nested JSON), `subjectType` (max 100), `email` (regex de email, 7-100 chars), `subject` (max 100), `message` (5-500000 chars), `sender` (max 30), `moreParameters` (nested JSON), `templateId` (max 100), `language` (valores: `portuguese`, `english`, `spanish`), `url` (max 500), `method` (max 10), `headers` (nested JSON), `apiName` (max 30), `details` (string), `status` (string), `cause` (string), `stackTrace` (string), `attempts` (unsigned), `ip` (7-15 chars), `coordinates` (regex lat/long), `macAddress` (regex MAC), `userAgent` (string), `httpStatus` (200-599 unsigned), `contentType` (string).

---

## Enum: JnJsonInstantMessengerFields
**Pacote:** `com.jn.json.fields.validation`
**Tipo:** enum
**Propósito:** Centraliza as definições de validação dos campos JSON específicos de mensagens instantâneas (Telegram). Referenciado via `@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)`.

### Campos definidos:
`message` (string), `chatId` (unsigned), `moreParameters` (nested JSON), `templateId` (max 100), `caption` (string), `contentType` (string), `fileName` (string), `instantMessageType` (string), `commandName` (string), `stepName` (string), `botToken` (string).

---

## Mensageria

---

## Interface: JnBusinessSendToMensageria
**Pacote:** `com.jn.mensageria`
**Tipo:** interface (estende `CcpBusiness`)
**Propósito:** Interface mixin que fornece o método `sendToMensageria(json)` via método default, simplificando o envio assíncrono de qualquer business para a fila de mensageria (PubSub). Classes que implementam esta interface ganham a capacidade de se auto-enviar para processamento assíncrono.

### Métodos:
- **sendToMensageria(CcpJsonRepresentation json)** → `CcpJsonRepresentation` *(default)*: Cria um `JnFunctionMensageriaSender` usando `this` como tópico e executa o envio.

---

## Classe: JnFunctionMensageriaSender
**Pacote:** `com.jn.mensageria`
**Tipo:** classe (implementa `CcpBusiness`)
**Propósito:** Responsável por enviar mensagens/tarefas para a fila de mensageria (PubSub). Cria um registro em `JnEntityAsyncTask` com os detalhes da mensagem (timestamp, request, messageId, tópico, operação) antes de publicar na fila. Suporta dois modos: envio de um `CcpBusiness` (tópico = nome da classe) ou envio de uma operação de entidade (`CcpEntityOperationType`).

### Construtores:
- **JnFunctionMensageriaSender(CcpBusiness topic)**: Configura o sender para um business, usando seu nome de classe como tópico.
- **JnFunctionMensageriaSender(CcpEntity entity, CcpEntityOperationType operation)**: Configura para operação de entidade, usando o nome da classe de configuração da entidade como tópico.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Enriquece o JSON com metadados da tarefa, salva em `JnEntityAsyncTask`, e publica na mensageria com o receptor `JnMensageriaReceiver`.
- **sendToMensageria(List\<CcpJsonRepresentation\> messages)** → `JnFunctionMensageriaSender`: Envia uma lista de mensagens em bulk para o PubSub, salvando em `JnEntityAsyncTask` apenas as que podem ser salvas.
- **sendToMensageria(CcpJsonRepresentation... messages)** → `CcpJsonRepresentation`: Sobrecarga para envio de múltiplas mensagens como varargs.
- **getMessageDetails(CcpJsonRepresentation json)** → `CcpJsonRepresentation` *(privado)*: Monta o JSON com os campos de controle da tarefa assíncrona.
- **canSave(CcpJsonRepresentation x)** → `boolean` *(privado)*: Verifica se o business associado ao tópico permite ser salvo como tarefa assíncrona.

---

## Classe: JnMensageriaReceiver
**Pacote:** `com.jn.mensageria`
**Tipo:** classe (Singleton, estende `CcpMensageriaReceiver`)
**Propósito:** Receptor de mensagens do PubSub. Roteia cada mensagem recebida para o `CcpBusiness` correto com base no campo `operation` (nome da operação de entidade) ou no nome do tópico. Registra o resultado da execução (sucesso ou erro) de volta em `JnEntityAsyncTask`, incluindo tempo decorrido e resposta.

### Métodos:
- **executeProcess(CcpEntity entity, String processName, CcpJsonRepresentation json, CcpBusiness jnAsyncBusinessNotifyError)** → `JnMensageriaReceiver`: Executa o processo identificado por `processName`, salva o resultado (sucesso ou erro) e retorna o receiver.
- **saveResult(CcpEntity entity, CcpJsonRepresentation messageDetails, CcpJsonRepresentation response)** → `JnMensageriaReceiver` *(privado)*: Salva o resultado de sucesso na entidade de tarefa assíncrona.
- **saveResult(CcpEntity entity, CcpJsonRepresentation messageDetails, Throwable e, CcpBusiness jnAsyncBusinessNotifyError)** → `JnMensageriaReceiver` *(privado)*: Converte a exceção em JSON e salva como resultado de falha.
- **saveResult(CcpEntity entity, CcpJsonRepresentation messageDetails, CcpJsonRepresentation response, boolean success)** → `JnMensageriaReceiver` *(privado)*: Implementação base que calcula o tempo decorrido e persiste o resultado.
- **getExecuteBulkOperation()** → `CcpExecuteBulkOperation`: Retorna `JnExecuteBulkOperation.INSTANCE`.
- **getFunctionToDeleteKeysInTheCache()** → `Consumer<String[]>`: Retorna `JnDeleteKeysFromCache.INSTANCE`.

---

## Builder de mensagens (fluent API)

> O pacote `com.jn.messages` implementa um builder fluent para configurar e disparar o envio de mensagens (email e/ou mensagem instantânea) ao usuário. O uso típico é encadeado como: `new JnSendMessageToUser().addDefaultProcessToEmailSending().and().addDefaultStepToInstantMessageSending().soWithAllAddedProcessAnd().withTheTemplateEntity(topic).andWithTheEntityToBlockMessageResend(entity).andWithTheMessageValuesFromJson(json).andWithTheSupportLanguage(lang).sendAllMessages()`.

---

## Classe: JnSendMessageToUser
**Pacote:** `com.jn.messages`
**Tipo:** classe (ponto de entrada do builder)
**Propósito:** Classe principal do builder de envio de mensagens. Mantém listas paralelas de remetentes, entidades de parâmetros, entidades de templates, entidades de bloqueio e entidades de controle de já-enviado. Ao executar `executeAllSteps(...)`, faz um `unionAll` para buscar todos os dados necessários de uma vez, verifica se já foi enviado, e para cada canal configurado, verifica bloqueio e já-enviado antes de enviar.

### Métodos:
- **createStep()** → `JnCreateStep`: Inicia o builder passo a passo para criação de um step customizado.
- **addDefaultProcessToEmailSending()** → `JnAddDefaultStep`: Adiciona o passo padrão de envio de email (usando `JnBusinessSendEmailMessage` com as entidades padrão).
- **addDefaultStepToInstantMessageSending()** → `JnAddDefaultStep`: Adiciona o passo padrão de envio de mensagem instantânea (usando `JnBusinessSendInstantMessage` com as entidades padrão).
- **addOneStep(CcpBusiness messenger, CcpEntity parameterEntity, CcpEntity messageEntity, CcpEntity blockEntity, CcpEntity alreadySentEntity)** → `JnSendMessageToUser`: Cria uma nova instância copiando os steps existentes e adicionando o novo (imutabilidade).
- **executeAllSteps(String templateId, CcpEntity entityToSave, CcpJsonRepresentation entityValues, String languageToUseInErrorCases)** → `CcpJsonRepresentation`: Executa todos os steps configurados: faz o unionAll, verifica se já foi salvo, itera pelos canais de envio e salva o resultado.
- **sendMessage(CcpSelectUnionAll unionAll, CcpJsonRepresentation json, int index)** → `CcpJsonRepresentation` *(privado)*: Para um índice de canal, verifica já-enviado e bloqueio, monta o JSON com parâmetros e template, e executa o remetente.

---

## Classe: JnAddDefaultStep
**Pacote:** `com.jn.messages`
**Tipo:** classe (etapa do builder após adicionar um step padrão)
**Propósito:** Etapa intermediária do builder que permite adicionar mais um step, finalizar a configuração de steps ou encadear outro step padrão.

### Métodos:
- **andCreateAnotherStep()** → `JnCreateStep`: Continua o builder para criar um novo step customizado.
- **soWithAllAddedProcessAnd()** → `JnSoWithAllAddedStepsAnd`: Finaliza a fase de adição de steps e avança para a configuração do template.
- **and()** → `JnSendMessageToUser`: Retorna ao `JnSendMessageToUser` para adicionar mais um step padrão.

---

## Classe: JnAndWithTheEntityToBlockMessageResend
**Pacote:** `com.jn.messages`
**Tipo:** classe (etapa do builder)
**Propósito:** Etapa do builder que recebe a entidade usada para bloquear reenvio de mensagens (deduplicação).

### Métodos:
- **andWithTheMessageValuesFromJson(CcpJsonRepresentation jsonValues)** → `JnAndWithTheJsonValues`: Avança para a próxima etapa fornecendo os valores do JSON da mensagem.

---

## Classe: JnAndWithTheJsonValues
**Pacote:** `com.jn.messages`
**Tipo:** classe (etapa do builder)
**Propósito:** Etapa do builder que carrega os valores do JSON da mensagem.

### Métodos:
- **andWithTheSupportLanguage(String supportLanguage)** → `JnAndWithTheSupportLanguage`: Avança para a próxima etapa fornecendo o idioma de suporte.

---

## Classe: JnAndWithTheParametersEntity
**Pacote:** `com.jn.messages`
**Tipo:** classe (etapa do builder — parte do fluxo de step customizado)
**Propósito:** Etapa do builder que recebe a entidade de parâmetros de envio.

### Métodos:
- **andWithTheTemplateEntity(CcpEntity templateEntity)** → `JnAndWithTheTemplateEntity`: Avança para a próxima etapa fornecendo a entidade de template.

---

## Classe: JnAndWithTheSupportLanguage
**Pacote:** `com.jn.messages`
**Tipo:** classe (etapa final do builder)
**Propósito:** Última etapa do builder fluent. Recebe o idioma de suporte e dispara a execução de todos os steps configurados.

### Métodos:
- **sendAllMessages()** → `CcpJsonRepresentation`: Delega para `JnSendMessageToUser.executeAllSteps(...)` passando todos os parâmetros acumulados pelas etapas anteriores do builder. Este é o método terminal da cadeia fluent.

---

## Classe: JnAndWithTheTemplateEntity
**Pacote:** `com.jn.messages`
**Tipo:** classe (etapa do builder — parte do fluxo de step customizado)
**Propósito:** Etapa do builder que recebe a entidade de template e finaliza a configuração de um step customizado, adicionando-o ao `JnSendMessageToUser`.

### Métodos:
- **andCreateAnotherStep(CcpEntity blockEntity, CcpEntity alreadySentEntity)** → `JnCreateStep`: Adiciona o step atual ao builder e inicia a criação de outro step.
- **soWithAllAddedStepsAnd(CcpEntity blockEntity, CcpEntity alreadySentEntity)** → `JnSoWithAllAddedStepsAnd`: Adiciona o step atual e finaliza a fase de steps.

---

## Classe: JnCreateStep
**Pacote:** `com.jn.messages`
**Tipo:** classe (etapa inicial de step customizado)
**Propósito:** Ponto de entrada para criação de um step customizado no builder, recebendo o business de envio.

### Métodos:
- **withTheProcess(CcpBusiness process)** → `JnWithTheProcess`: Avança para a próxima etapa com o business de envio informado.

---

## Classe: JnSendMessageAndJustErrors
**Pacote:** `com.jn.messages`
**Tipo:** classe (estende `JnSendMessageToUser`)
**Propósito:** Variante do builder que captura exceções nos steps e as registra em `JnEntityJobsnowWarning` sem propagar a exceção. Usada quando queremos continuar o fluxo mesmo que um canal de envio falhe, mas registrando o problema como warning (não como erro crítico).

### Métodos:
- **addOneStep(CcpBusiness step, ...)** → `JnSendMessageToUser`: Sobrescreve o método para envolver o `step` em um bloco try-catch que, na exceção, salva um warning e retorna os valores originais.

---

## Classe: JnSendMessageIgnoringProcessErrors
**Pacote:** `com.jn.messages`
**Tipo:** classe (estende `JnSendMessageToUser`)
**Propósito:** Variante do builder que captura exceções nos steps e, além de registrar o warning, tenta notificar o suporte sobre o erro via `JnBusinessNotifySupport` (usando `JnSendMessageAndJustErrors` para o envio da notificação de erro também ser tolerante a falhas). Usada por `JnBusinessNotifyError` para evitar loops infinitos de erros.

### Métodos:
- **addOneStep(CcpBusiness step, ...)** → `JnSendMessageToUser`: Sobrescreve para envolver em try-catch que, na exceção, notifica o suporte e retorna os valores originais sem relançar.

---

## Classe: JnSoWithAllAddedStepsAnd
**Pacote:** `com.jn.messages`
**Tipo:** classe (etapa do builder após finalizar os steps)
**Propósito:** Etapa do builder que marca a transição entre a fase de configuração de steps e a fase de configuração do template/entidade.

### Métodos:
- **withTheTemplateEntity(String templateId)** → `JnWithTheTemplateId`: Avança para a próxima etapa informando o templateId (geralmente o nome da classe de negócio).

---

## Classe: JnWithTheProcess
**Pacote:** `com.jn.messages`
**Tipo:** classe (etapa de step customizado)
**Propósito:** Etapa do builder de step customizado que carrega o business de envio.

### Métodos:
- **andWithTheParametersEntity(CcpEntity parametersEntity)** → `JnAndWithTheParametersEntity`: Avança informando a entidade de parâmetros.

---

## Classe: JnWithTheTemplateId
**Pacote:** `com.jn.messages`
**Tipo:** classe (etapa do builder após definir o templateId)
**Propósito:** Etapa do builder que carrega o templateId e avança para a configuração da entidade de bloqueio de reenvio.

### Métodos:
- **andWithTheEntityToBlockMessageResend(CcpEntity entityToSave)** → `JnAndWithTheEntityToBlockMessageResend`: Avança informando a entidade que bloqueia reenvio duplicado.

---

## Serviços (Services)

---

## Interface: JnService
**Pacote:** `com.jn.services`
**Tipo:** interface (estende `CcpService`)
**Propósito:** Interface base para todos os serviços do JobsNow. Fornece método default que carrega automaticamente a classe de validação JSON a partir de uma classe interna do mesmo pacote com o nome do valor do enum (convenção: cada valor do enum `JnService` tem uma classe interna com mesmo nome no mesmo pacote).

### Métodos:
- **getJsonValidationClass()** → `Class<?>` *(default)*: Busca por reflexão a classe `<pacote>.<nome_do_valor>` e a retorna como classe de validação.

---

## Enum: JnServiceAsyncTask
**Pacote:** `com.jn.services`
**Tipo:** enum (implementa `JnService`)
**Propósito:** Serviço de operações sobre tarefas assíncronas. Atualmente contém apenas o valor `GetAsyncTaskStatusById` (implementação incompleta — TODO).

### Valores:
- **`GetAsyncTaskStatusById`**: Deveria retornar o status de uma tarefa pelo ID, mas a implementação está pendente (retorna o JSON de entrada sem alteração).

---

## Enum: JnServiceContactUs
**Pacote:** `com.jn.services`
**Tipo:** enum (implementa `JnService`)
**Propósito:** Serviço de gerenciamento de contatos ("Fale Conosco"). Todos os três valores estão com implementações de placeholder (retornam o JSON de entrada sem alteração — implementações pendentes de desenvolvimento).

### Valores:
- **`SaveContactUs`**: Salva um novo contato.
- **`ListContactUsByUser`**: Lista contatos de um usuário.
- **`GetContactUsKpis`**: Retorna KPIs de contatos.

---

## Enum: JnServiceLogin
**Pacote:** `com.jn.services`
**Tipo:** enum (implementa `JnService`)
**Propósito:** Serviço central de autenticação do JobsNow. Orquestra todos os fluxos de login usando `CcpGetEntityId` para buscar dados em múltiplas entidades e aplicar regras de negócio de forma declarativa. Cada valor implementa um passo do ciclo de vida de autenticação.

### Valores (operações):

- **`ExecuteLogin`**: Executa o login com senha. Carrega dados do token (disposable, stats, tentativas), verifica bloqueios (token bloqueado, email não cadastrado, senha bloqueada, conflito de sessão, senha não cadastrada) e avalia a senha via `JnBusinessEvaluateAttempts`. Retorna dados de sessão (sessionToken, datas de expiração, IP, userAgent).

- **`ValidateLogin`**: Valida se a sessão atual está ativa. Delega para `JnBusinessSessionValidate.INSTANCE`.

- **`CreateLoginEmail`**: Cadastra o email do usuário na plataforma. Verifica bloqueios e, se o email ainda não existe, o cria. Retorna status informando se ainda faltam answers ou password.

- **`ExistsLoginEmail`**: Verifica se o email existe e o estado de completude do cadastro (faltando answers, password, etc.). Usado pelo front-end para direcionar o usuário ao passo correto do onboarding.

- **`ExecuteLogout`**: Executa o logout. Verifica se existe sessão ativa e delega para `JnBusinessExecuteLogout` via mensageria.

- **`SaveAnswers`**: Salva as respostas do questionário de onboarding (`channel` e `goal`). Verifica bloqueios e se as respostas já foram salvas.

- **`CreateLoginToken`**: Gera e envia o token de acesso ao usuário. Verifica se o token não está bloqueado, se o email existe e se já não foi enviado um token recente; se não foi, envia via `JnBusinessSendUserToken`.

- **`SavePassword`**: Salva a nova senha do usuário após validação do token. Verifica bloqueios, carrega dados de expiração do token e avalia o token via `JnBusinessEvaluateAttempts`.

- **`ResendLoginToken`**: Registra solicitação de reenvio de token. Verifica se já foi reenvio ou já está na fila; se não, cria o registro em `JnEntityLoginTokenRequestResend`.

- **`UnlockLoginToken`**: Registra solicitação de desbloqueio de token. Verifica se o token está realmente bloqueado, se já foi desbloqueado ou se já existe pedido na fila; se não, cria o registro em `JnEntityLoginTokenRequestUnlock`.

---

## Status de processo

> Todos os enums de status de processo implementam `CcpProcessStatus` e fornecem o método `asNumber()` que retorna o código HTTP correspondente. São usados com `CcpErrorFlowDisturb` para interromper o fluxo e retornar o status HTTP adequado ao cliente.

---

## Enum: JnProcessStatusCreateLoginEmail
**Pacote:** `com.jn.status.login`
**Tipo:** enum (implementa `CcpProcessStatus`)
**Propósito:** Statuses do processo de criação de email de login.

| Valor | HTTP | Descrição |
|---|---|---|
| `invalidEmail` | 400 | Email inválido |
| `lockedToken` | 403 | Token bloqueado |
| `missingEmail` | 404 | Email não encontrado |
| `lockedPassword` | 427 | Senha bloqueada |
| `loginConflict` | 409 | Conflito de sessão |
| `missingSavePassword` | 202 | Falta cadastrar senha (aceito, mas incompleto) |
| `missingSaveAnswers` | 201 | Falta responder questionário (aceito, mas incompleto) |
| `expectedStatus` | 200 | Sucesso |

---

## Enum: JnProcessStatusCreateLoginToken
**Pacote:** `com.jn.status.login`
**Tipo:** enum (implementa `CcpProcessStatus`)
**Propósito:** Statuses do processo de criação/envio de token de login.

| Valor | HTTP | Descrição |
|---|---|---|
| `statusInvalidEmail` | 400 | Email inválido |
| `statusLockedToken` | 403 | Token bloqueado |
| `statusMissingEmail` | 404 | Email não cadastrado |
| `missingSaveAnswers` | 201 | Falta responder questionário |
| `expectedStatus` | 200 | Sucesso |
| `statusAlreadySentToken` | 429 | Token já enviado recentemente |

---

## Enum: JnProcessStatusExecuteLogin
**Pacote:** `com.jn.status.login`
**Tipo:** enum (implementa `CcpProcessStatus`)
**Propósito:** Statuses do processo de execução de login (validação de senha).

| Valor | HTTP | Descrição |
|---|---|---|
| `passwordLockedRecently` | 429 | Senha bloqueada por tentativas recentes |
| `missingSessionToken` | 401 | Token de sessão ausente |
| `missingSavePassword` | 202 | Senha não cadastrada ainda |
| `lockedPassword` | 423 | Senha bloqueada |
| `expectedStatus` | 200 | Sucesso |
| `invalidSession` | 401 | Sessão inválida |
| `wrongPassword` | 427 | Senha incorreta |
| `loginConflict` | 409 | Conflito de sessão |
| `invalidEmail` | 400 | Email inválido |
| `missingSavingEmail` | 404 | Email não cadastrado |
| `weakPassword` | 422 | Senha fraca |
| `lockedToken` | 403 | Token bloqueado |

### Métodos:
- **flowDisturb()** → `CcpBusiness`: Retorna um `CcpBusiness` que ao ser executado lança `CcpErrorFlowDisturb` com este status. Útil para criar callbacks de interrupção de fluxo.

---

## Enum: JnProcessStatusExecuteLogout
**Pacote:** `com.jn.status.login`
**Tipo:** enum (implementa `CcpProcessStatus`)
**Propósito:** Statuses do processo de logout.

| Valor | HTTP | Descrição |
|---|---|---|
| `invalidEmail` | 400 | Email inválido |
| `missingLogin` | 404 | Sessão não encontrada |
| `expectedStatus` | 200 | Sucesso |

---

## Enum: JnProcessStatusExistsLoginEmail
**Pacote:** `com.jn.status.login`
**Tipo:** enum (implementa `CcpProcessStatus`)
**Propósito:** Statuses da verificação de existência do email de login e completude do cadastro.

| Valor | HTTP | Descrição |
|---|---|---|
| `invalidEmail` | 400 | Email inválido |
| `lockedToken` | 403 | Token bloqueado |
| `missingEmail` | 404 | Email não cadastrado |
| `lockedPassword` | 427 | Senha bloqueada |
| `invalidJson` | 422 | JSON inválido |
| `loginConflict` | 409 | Conflito de sessão |
| `missingPassword` | 202 | Falta cadastrar senha |
| `missingAnswers` | 201 | Falta responder questionário |
| `expectedStatus` | 200 | Cadastro completo |

---

## Enum: JnProcessStatusSaveAnswers
**Pacote:** `com.jn.status.login`
**Tipo:** enum (implementa `CcpProcessStatus`)
**Propósito:** Statuses do processo de salvamento das respostas do questionário de onboarding.

| Valor | HTTP | Descrição |
|---|---|---|
| `invalidEmail` | 400 | Email inválido |
| `lockedToken` | 403 | Token bloqueado |
| `tokenFaltando` | 404 | Token não encontrado (email não cadastrado) |
| `lockedPassword` | 427 | Senha bloqueada |
| `loginConflict` | 409 | Conflito de sessão |
| `missingPassword` | 202 | Falta cadastrar senha |
| `expectedStatus` | 200 | Sucesso |

---

## Enum: JnProcessStatusUnlockLoginToken
**Pacote:** `com.jn.status.login`
**Tipo:** enum (implementa `CcpProcessStatus`)
**Propósito:** Statuses dos processos de desbloqueio e reenvio de token.

| Valor | HTTP | Descrição |
|---|---|---|
| `statusTokenNotLocked` | 404 | Token não está bloqueado (não há o que desbloquear) |
| `statusAlreadyRequested` | 409 | Solicitação já existe na fila |
| `statusTokenAlredyResent` | 429 | Token já foi reenviado recentemente |
| `statusTokenAlredyUnlocked` | 429 | Token já foi desbloqueado recentemente |

---

## Enum: JnProcessStatusUpdatePassword
**Pacote:** `com.jn.status.login`
**Tipo:** enum (implementa `CcpProcessStatus`)
**Propósito:** Statuses do processo de definição/atualização de senha (usando o token de acesso).

| Valor | HTTP | Descrição |
|---|---|---|
| `invalidEmail` | 400 | Email inválido |
| `lockedToken` | 403 | Token bloqueado |
| `missingEmail` | 404 | Email não cadastrado |
| `missingToken` | 404 | Token não encontrado |
| `wrongToken` | 427 | Token incorreto |
| `invalidJson` | 422 | JSON inválido |
| `tokenLockedRecently` | 429 | Token bloqueado por tentativas recentes |
| `expectedStatus` | 200 | Sucesso |

---

## Utilitários

---

## Classe: JnDeleteKeysFromCache
**Pacote:** `com.jn.utils`
**Tipo:** classe (Singleton, implementa `CcpBusiness` e `Consumer<String[]>`)
**Propósito:** Utilitário para exclusão de chaves do cache (GCP Memcache). Implementa tanto `CcpBusiness` (aceita JSON com lista de chaves) quanto `Consumer<String[]>` (aceita array de chaves diretamente), sendo usado como callback de limpeza de cache após operações bulk.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Extrai a lista de chaves do campo `keysToDeleteInCache` do JSON e as remove do cache uma a uma.
- **accept(String[] keysToDeleteInCache)** → `void`: Converte o array para lista, monta o JSON e delega para `apply(json)`.

---

## Enum: JnLanguage
**Pacote:** `com.jn.utils`
**Tipo:** enum (implementa `CcpJsonFieldName`)
**Propósito:** Define os idiomas suportados pela plataforma JobsNow. Usado como valor do campo `language` em entidades de template de email e mensagem instantânea.

### Valores:
- **`portuguese`**: Português
- **`english`**: Inglês
- **`spanish`**: Espanhol

---

## Classe: JnSystemProperties
**Pacote:** `com.jn.utils`
**Tipo:** classe
**Propósito:** Centraliza o acesso às propriedades do sistema lidas do `application_properties` (via variáveis de ambiente, classpath ou arquivo). Fornece métodos tipados para cada propriedade conhecida do sistema (URLs de APIs, tokens de autenticação, idioma de suporte, configurações de ambiente local, etc.).

### Enum interno `Fields`:
| Campo | Descrição |
|---|---|
| `databaseAddress` | Endereço do banco de dados (mapeado para `database.address`) |
| `databaseSecret` | Segredo do banco (mapeado para `database.secret`) |
| `supportLanguage` | Idioma padrão para notificações ao suporte |
| `urlEmailKey` | URL do serviço de email |
| `urlInstantMessengerKey` | URL do serviço de mensagem instantânea |
| `tokenEmailKey` | Token de autenticação do serviço de email |
| `tokenInstantMessengerKey` | Token de autenticação do serviço de mensagem instantânea |
| `localEnvironment` | Flag booleano indicando ambiente local |
| `languages` | Lista de idiomas suportados |
| `systems` | Lista de sistemas integrados |

### Métodos:
- **systems()** → `List<String>`: Retorna a lista de sistemas configurados.
- **languages()** → `List<String>`: Retorna a lista de idiomas configurados.
- **localEnvironment()** → `boolean`: Retorna `true` se o ambiente for local.
- **urlInstantMessengerKey()** → `String`: Retorna a URL do mensageiro instantâneo.
- **tokenEmailValue()** → `String`: Retorna o token de autenticação do serviço de email.
- **urlEmailValue()** → `String`: Retorna a URL do serviço de email.
- **supportLanguage()** → `String`: Retorna o idioma de suporte configurado.
- **tokenInstantMessengerKey()** → `String`: Retorna o token do serviço de mensagem instantânea.
- **databaseSecret()** → `String`: Retorna o segredo do banco de dados.
- **databaseAddress()** → `String`: Retorna o endereço do banco de dados.
- **getSystemProperty(CcpJsonFieldName field)** → `T`: Retorna uma propriedade genérica pelo nome do campo.
- **getSystemInnerProperty(CcpJsonFieldName... fields)** → `String`: Retorna uma propriedade aninhada pelo caminho de campos.
- **getSystemInnerJson(String... fields)** → `CcpJsonRepresentation`: Retorna um JSON aninhado pelo caminho de chaves string.
- **getSystemProperty(String field)** → `T`: Retorna uma propriedade genérica pelo nome string.


---

# Documentação do Módulo `vis_business_jobsnow`

> Gerado em: 2026-06-09
> Módulo: `vis_business_jobsnow` — lógica de visualização/matching de currículos e vagas da plataforma JobsNow.

---

## Sumário

1. [Business — Company](#business--company)
2. [Business — Messages](#business--messages)
3. [Business — Position](#business--position)
4. [Business — Recruiter](#business--recruiter)
5. [Business — Resume](#business--resume)
6. [Business — Resume Skills](#business--resume-skills)
7. [Business — Templates / Notify Support](#business--templates--notify-support)
8. [Entities](#entities)
9. [Exceptions](#exceptions)
10. [JSON Fields Validation](#json-fields-validation)
11. [JSON Transformers](#json-transformers)
12. [Scheduling](#scheduling)
13. [Services](#services)
14. [Status](#status)
15. [Utils](#utils)

---

## Business — Company

---

## Classe: VisBusinessGroupCompaniesByTheirFirstThreeInitials
**Pacote:** `com.vis.business.company`
**Tipo:** classe
**Propósito:** Implementação de `CcpBusiness` responsável por agrupar empresas pelas suas três primeiras iniciais. A lógica ainda está pendente de implementação (corpo do método retorna o JSON de entrada sem alterações — marcado internamente como TODO).

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Recebe o JSON de contexto e deveria executar o agrupamento de empresas pelas três primeiras iniciais; atualmente retorna o JSON original sem processamento (TODO).

---

## Business — Messages

---

## Classe: VisMessages
**Pacote:** `com.vis.business.messages`
**Tipo:** classe (contêiner de inner classes estáticas)
**Propósito:** Agrupa as classes de envio de mensagens relacionadas ao ciclo de vida de habilidades (skills) no sistema — aprovação, rejeição e estado pendente, tanto para a habilidade em si quanto para sua hierarquia. Cada inner class herda de `JnBusinessSendMessage` e associa a entidade-destino correspondente. Todos os templates de mensagem estão marcados como FIXME (faltando template).

### Classes internas:
- **RejectedSkillHierarchy**: Envia mensagem utilizando a entidade `VisEntitySkillFixHierarchyRejected` — notifica rejeição de correção de hierarquia de skill.
- **AprovedSkillHierarchy**: Envia mensagem utilizando a entidade `VisEntitySkillFixHierarchyApproved` — notifica aprovação de correção de hierarquia de skill.
- **PendingSkillHierarchy**: Envia mensagem utilizando a entidade `VisEntitySkillFixHierarchyPending` — notifica que uma correção de hierarquia de skill está pendente de análise.
- **RejectedSkill**: Envia mensagem utilizando a entidade `VisEntitySkillRejected` — notifica rejeição de uma nova skill sugerida.
- **PendingSkill**: Envia mensagem utilizando a entidade `VisEntitySkillPending` — notifica que uma nova skill está aguardando aprovação.
- **AprovedSkill**: Envia mensagem utilizando a entidade `VisEntitySkill` — notifica aprovação de uma nova skill.

---

## Business — Position

---

## Classe: VisBusinessDuplicateFieldEmailToFieldMasters
**Pacote:** `com.vis.business.position`
**Tipo:** classe (singleton)
**Propósito:** Implementação de `CcpBusiness` que copia o valor do campo `email` da entidade `VisEntityPosition` para o campo `masters` no mesmo JSON. Utilizada como etapa preparatória para operações de agrupamento que requerem o campo `masters` populado.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Duplica o valor de `VisEntityPosition.Fields.email` para o campo `masters` e retorna o JSON resultante.

---

## Classe: VisBusinessGroupPositionsGroupedByRecruiters
**Pacote:** `com.vis.business.position`
**Tipo:** classe (singleton)
**Propósito:** Implementação de `CcpBusiness` que delega ao utilitário `VisUtils.groupPositionsGroupedByRecruiters` o agrupamento das vagas por recrutador. Serve como ponto de entrada de negócio para disparar esse agrupamento.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Invoca `VisUtils.groupPositionsGroupedByRecruiters(json)` e retorna o resultado do agrupamento.

---

## Classe: VisBusinessPositionResumesSend
**Pacote:** `com.vis.business.position`
**Tipo:** classe (singleton)
**Propósito:** Implementação de `CcpBusiness` que representa o passo de envio de currículos associados a uma vaga. A implementação ainda está pendente (retorna o JSON sem alteração — TODO).

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Deveria executar o envio de currículos para recrutadores; atualmente retorna o JSON de entrada sem processamento (TODO).

---

## Business — Recruiter

---

## Classe: VisBusinessRecruiterReceivingResumes
**Pacote:** `com.vis.business.recruiter`
**Tipo:** classe (singleton)
**Propósito:** Implementação de `CcpBusiness` que representa o processo de recebimento de currículos pelo recrutador. A lógica está pendente de implementação (retorna o JSON de entrada — TODO).

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Deveria processar o recebimento de currículos pelo recrutador; atualmente retorna o JSON sem alteração (TODO).

---

## Classe: VisBusinessResumeViewSave
**Pacote:** `com.vis.business.recruiter`
**Tipo:** classe (singleton)
**Propósito:** Implementação de `CcpBusiness` que registra a visualização de um currículo por um recrutador. Verifica se a visualização é gratuita ou paga (parte financeira pendente), se o currículo está negativado e se a vaga está inativa, e então persiste os registros de `VisEntityResumeLastView` e `VisEntityResumeFreeView` em operação bulk.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Verifica as condições da visualização (gratuidade, negativação do currículo, inatividade da vaga), monta o objeto de visualização com dados do currículo e da vaga, e salva em bulk os registros de última visualização e visualização gratuita. Retorna o JSON original.

---

## Business — Resume

---

## Classe: VisBusinessCalculateResumeHashes
**Pacote:** `com.vis.business.resume`
**Tipo:** classe
**Propósito:** Implementação de `CcpBusiness` destinada ao cálculo de hashes do currículo (provavelmente para indexação ou matching). A implementação está marcada como TODO — retorna o JSON de entrada sem alterações.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Deveria calcular hashes do currículo; atualmente retorna o JSON sem processamento (TODO).
- **getJsonValidationClass()** → `Class<?>`: Retorna a classe `JsonFieldNames` para validação dos campos JSON de entrada.

---

## Classe: VisBusinessResumeSaveViewFailed
**Pacote:** `com.vis.business.resume`
**Tipo:** classe (singleton)
**Propósito:** Implementação de `CcpBusiness` que persiste o registro de uma tentativa de visualização de currículo que falhou. Extrai o status HTTP do campo `errorDetails.status` e salva o registro na entidade `VisEntityResumeViewFailed`.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Extrai o campo `status` de dentro do objeto `errorDetails`, adiciona-o ao JSON raiz e salva o registro de falha na entidade `VisEntityResumeViewFailed`. Retorna o JSON original.

---

## Business — Resume Skills

---

## Classe: VisBusinessApprovingSkill
**Pacote:** `com.vis.business.resume.skills`
**Tipo:** classe
**Propósito:** Implementação de `CcpBusiness` responsável pelo processo de aprovação de uma skill. A implementação está pendente (corpo marcado como TODO — retorna o JSON de entrada sem alterações).

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Deveria executar o fluxo de aprovação de skill; atualmente retorna o JSON sem processamento (TODO).

---

## Business — Templates / Notify Support

---

## Classe (enum): VisTemplatesToNotifySupport
**Pacote:** `com.vis.business.templates.notify.support`
**Tipo:** enum que implementa `CcpBusiness`
**Propósito:** Define os templates de notificação enviados ao suporte quando surgem eventos relacionados a skills. Cada constante do enum corresponde a um template de mensagem: `new_skill` (nova skill sugerida) e `new_skill_hierarchy` (nova hierarquia de skill). Utiliza `JnSendMessageToUser` para enviar e-mail e mensagem instantânea ao suporte, bloqueando reenvios duplicados via `JnEntityJobsnowWarning`.

### Constantes:
- **new_skill_hierarchy**: Template para notificar o suporte sobre uma nova solicitação de correção de hierarquia de skill.
- **new_skill**: Template para notificar o suporte sobre uma nova skill sugerida.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Lê o idioma do JSON, constrói e configura o `JnSendMessageToUser` com o template correspondente à constante do enum, a entidade de bloqueio de reenvio e os valores do JSON, e dispara o envio de e-mail e mensagem instantânea.

### Classes internas:
- **SendEmail** (privada): Implementação de `CcpBusiness` que converte o nome simples da classe para snake_case e delega ao enum correspondente.
- **NewSkill**: Especialização de `SendEmail` para o template de nova skill (FIXME: template faltando).
- **NewSkillHierarchy**: Especialização de `SendEmail` para o template de nova hierarquia de skill (FIXME: template faltando).

---

## Entities

---

## Classe: VisEntityBalance
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa a tabela/índice de saldo de conta do recrutador. Armazena o saldo disponível para consumo de visualizações de currículos, identificado pelo e-mail do recrutador. Possui cache de 1 hora e versionamento habilitado.

### Campos (enum `Fields`):
- **email** (chave primária): E-mail do recrutador, chave de identificação do registro.
- **balance**: Valor numérico não negativo representando o saldo disponível.

---

## Classe: VisEntityDeniedViewToCompany
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa a tabela/índice que registra domínios de empresas cujos recrutadores não podem visualizar determinados currículos. Utiliza o padrão Twin Entity para rastrear quando um bloqueio é revertido (entidade twin: `reallowed_view_to_company`). Possui cache de 1 hora e versionamento.

### Campos (enum `Fields`):
- **domain** (chave primária): Domínio da empresa recrutadora bloqueada.
- **email** (chave primária): E-mail do candidato cujo currículo está bloqueado para a empresa.
- **reasonType**: Tipo do motivo do bloqueio (string entre 2 e 50 caracteres).
- **reasonText**: Texto descritivo do motivo do bloqueio (string entre 2 e 50 caracteres).

---

## Classe: VisEntityFees
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa a tabela/índice de tarifas (fees) cobradas por serviço no sistema de visualizações. Define o custo associado a cada tipo de serviço. Possui cache de 1 hora e versionamento.

### Campos (enum `Fields`):
- **fee**: Valor numérico não negativo da tarifa cobrada pelo serviço.
- **service** (chave primária): Identificador do serviço ao qual a tarifa se aplica.

---

## Classe: VisEntityGroupCompaniesByTheirFirstThreeInitials
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa o agrupamento de nomes de empresas pelas três primeiras letras do domínio de e-mail. Permite buscas rápidas de empresas por prefixo. Possui cache de 1 hora. Inclui lógica de carga inicial de dados.

### Campos (enum `Fields`):
- **firstThreeInitials** (chave primária): As três primeiras letras do nome da empresa (string de tamanho exato 3).
- **companies**: Lista de nomes de empresas que compartilham essas iniciais (array obrigatório com mínimo 1 elemento).
- **timestamp**: Timestamp do registro.
- **date**: Data do registro.

### Métodos:
- **getFirstRecordsToInsert()** → `List<CcpBulkItem>`: Consulta todos os recrutadores existentes (índice `old_recruiters`), extrai o domínio do e-mail de cada um, capitaliza o nome da empresa e agrupa por suas três primeiras iniciais. Retorna os itens de bulk para criação dos registros iniciais.
- **toBulkItem(String initials)** → `CcpBulkItem` (privado): Constrói um `CcpBulkItem` de criação para um dado conjunto de iniciais e suas empresas agrupadas.

---

## Classe: VisEntityGroupPositionsByRecruiter
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa a entidade de agrupamento paginado de vagas por recrutador. Armazena páginas de detalhes de vagas organizadas pelo e-mail do recrutador. Possui cache de 24 horas e é expurgável anualmente.

### Campos (enum `Fields`):
- **email** (chave primária): E-mail do recrutador dono do grupo de vagas.
- **listSize** (chave primária): Tamanho da página de resultados.
- **from** (chave primária): Índice da página (offset de paginação).
- **detail**: Conteúdo da página — lista de vagas do recrutador.

---

## Classe: VisEntityGroupPositionsByResume
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa o agrupamento paginado de vagas associadas a um currículo. Armazena páginas de detalhes de vagas organizadas pelo e-mail do candidato. Possui cache de 24 horas e é expurgável anualmente.

### Campos (enum `Fields`):
- **email** (chave primária): E-mail do candidato cujo currículo foi relacionado às vagas.
- **listSize** (chave primária): Tamanho da página.
- **from** (chave primária): Índice da página.
- **detail**: Lista de vagas relacionadas ao currículo.

---

## Classe: VisEntityGroupPositionsBySkills
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa o índice de habilidades (skills) agrupadas pelas duas primeiras letras da palavra, utilizado como dicionário de lookup para matching de skills em textos. Também contém lógica de carga inicial a partir de arquivo de sinônimos. Possui cache de 1 hora.

### Campos (enum `Fields`):
- **firstTwoInitials** (chave primária): As duas primeiras letras da palavra de skill (string de tamanho exato 2).
- **skill**: Lista de objetos de skill (cada um contendo `skill`, `word` e `parent`) agrupados por essas iniciais.
- **timestamp**: Timestamp do registro.
- **date**: Data do registro.

### Métodos:
- **getWordStatus(String word)** → `int` (estático): Verifica o status de uma palavra no índice de skills. Retorna `0` se a palavra já existe como skill ou sinônimo, `1` se o grupo de iniciais não foi encontrado, `2` se a palavra não está no grupo.
- **getFirstRecordsToInsert()** → `List<CcpBulkItem>`: Lê o arquivo `synonyms.json`, processa sinônimos, pré-requisitos, similares e parents, agrupa todas as palavras pelas suas duas primeiras letras e retorna os itens de bulk para carga inicial.
- **getAllParents(...)** → `Set<String>` (privado): Percorre recursivamente a lista de sinônimos buscando os pais (`parent`) de uma palavra, acumulando-os em um conjunto.

---

## Classe: VisEntityGroupResumeViewsByRecruiter
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa o agrupamento paginado de visualizações de currículos organizadas pelo e-mail do recrutador. Serve para consulta histórica de quais currículos foram vistos por cada recrutador. Possui cache de 24 horas e é expurgável anualmente.

### Campos (enum `Fields`):
- **email** (chave primária): E-mail do recrutador.
- **listSize** (chave primária): Tamanho da página.
- **from** (chave primária): Índice da página.
- **detail**: Lista de registros de visualização do recrutador.

---

## Classe: VisEntityGroupResumeViewsByResume
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa o agrupamento paginado de visualizações de um currículo, organizado pelo e-mail do candidato. Permite consultar quantas e quais empresas visualizaram o currículo. Possui cache de 24 horas e é expurgável anualmente.

### Campos (enum `Fields`):
- **email** (chave primária): E-mail do candidato.
- **listSize** (chave primária): Tamanho da página.
- **from** (chave primária): Índice da página.
- **detail**: Lista de registros de visualização do currículo.

---

## Classe: VisEntityGroupResumesByPosition
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa o agrupamento paginado de currículos filtrados e ordenados para uma vaga específica. A chave inclui além do e-mail o título e senioridade da vaga, permitindo consultas paginadas dos candidatos compatíveis com cada vaga. Possui cache de 24 horas e é expurgável anualmente.

### Campos (enum `Fields`):
- **email** (chave primária): E-mail do recrutador dono da vaga.
- **listSize** (chave primária): Tamanho da página.
- **from** (chave primária): Índice da página.
- **detail**: Lista de currículos filtrados/ordenados para esta vaga.
- **title** (chave primária): Título da vaga.
- **seniority** (chave primária): Senioridade exigida pela vaga.

---

## Classe: VisEntityGroupResumesPerceptionsByRecruiter
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa o agrupamento paginado das percepções/avaliações (opiniões) que um recrutador registrou sobre currículos, organizado pelo e-mail do recrutador. Possui cache de 24 horas e é expurgável anualmente.

### Campos (enum `Fields`):
- **email** (chave primária): E-mail do recrutador.
- **listSize** (chave primária): Tamanho da página.
- **from** (chave primária): Índice da página.
- **detail**: Lista de percepções registradas pelo recrutador.

---

## Classe: VisEntityGroupResumesPerceptionsByResume
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa o agrupamento paginado das percepções/avaliações registradas sobre um currículo específico, organizado pelo e-mail do candidato. Possui cache de 24 horas e é expurgável anualmente.

### Campos (enum `Fields`):
- **email** (chave primária): E-mail do candidato.
- **listSize** (chave primária): Tamanho da página.
- **from** (chave primária): Índice da página.
- **detail**: Lista de percepções registradas sobre o currículo.

---

## Classe: VisEntityPosition
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa a entidade central de Vaga (position) no sistema. Armazena todos os dados de uma vaga publicada por um recrutador: cargo, senioridade, localização (DDD), disponibilidade, canais de contato, skills requeridas e desejadas, faixa salarial (CLT, PJ, BTC), frequência de envio de currículos, data de expiração e critérios de ordenação. Utiliza o padrão Twin Entity para controlar vagas inativas (`inactive_position`), tem escrita assíncrona, versionamento e cache de 1 hora. Ao salvar ou deletar, dispara fluxos de reagrupamento e envio de currículos para recrutadores.

### Campos (enum `Fields`):
- **channel**: Canais de contato aceitos (telegram, whatsapp, email, sms) — array obrigatório.
- **contactChannel**: Identificação do canal de contato específico do recrutador.
- **date**: Data de criação/atualização.
- **ddd**: DDDs de interesse — array obrigatório com 1 a 67 valores.
- **description**: Descrição detalhada da vaga (10 a 10.000 caracteres).
- **desiredSkill**: Skills desejadas (não obrigatórias) — array de objetos JSON.
- **disponibility**: Disponibilidade esperada do candidato.
- **email** (chave primária): E-mail do recrutador/empresa, transformado para hash SHA1 + domínio.
- **expireDate**: Data de expiração da vaga (dentro de 1 ano a partir da criação).
- **frequency**: Frequência de envio de currículos (minute, hourly, daily, weekly, monthly).
- **pcd**: Indica se a vaga é exclusiva para PCD.
- **requiredSkill**: Skills obrigatórias — array com mínimo 1 item.
- **seniority** (chave primária): Senioridade requerida (JR, PL, SR, ES).
- **sortFields**: Critérios de ordenação dos currículos (array obrigatório).
- **timestamp**: Timestamp de criação/atualização.
- **title** (chave primária): Título/cargo da vaga.
- **showSalaryExpectation**: Indica se a faixa salarial deve ser exibida.
- **minBtc / maxBtc**: Faixa salarial BTC mínima e máxima.
- **minClt / maxClt**: Faixa salarial CLT mínima e máxima.
- **minPj / maxPj**: Faixa salarial PJ mínima e máxima.

---

## Classe: VisEntityResume
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa a entidade central de Currículo (resume) do candidato. Armazena o perfil profissional completo: tipo de contrato desejado (CLT/PJ/BTC), disponibilidade, DDDs de interesse, skills, experiência, LinkedIn, idiomas, restrições de empresa, senioridade e tempo disponível para trabalho temporário. Utiliza Twin Entity para controlar currículos inativos, escrita assíncrona, versionamento e cache de 1 hora. Ao salvar ou ao reativar (deletar do twin), dispara o cálculo de hashes e o envio do currículo para recrutadores compatíveis.

### Campos (enum `Fields`):
- **btc**: Pretensão salarial BTC.
- **clt**: Pretensão salarial CLT (obrigatório se não houver PJ).
- **date**: Data de criação/atualização.
- **disponibility**: Disponibilidade em dias (obrigatório).
- **ddd**: DDDs onde o candidato pode trabalhar — array obrigatório com 1 a 67 valores.
- **desiredJob**: Cargo desejado (2 a 50 caracteres, obrigatório).
- **email** (chave primária): E-mail do candidato.
- **experience**: Ano de início da experiência profissional (obrigatório).
- **skill**: Lista de skills do candidato (objetos com `skill`, `word`, `category`, `type`, `parent`).
- **lastJob**: Último cargo ocupado.
- **language**: Idiomas que o candidato domina.
- **linkedinAddress**: URL do LinkedIn (obrigatório, validado por regex).
- **negotiableClaim**: Indica se a pretensão salarial é negociável.
- **notAllowedCompany**: Empresas nas quais o candidato não deseja trabalhar.
- **pcd**: Indica se o candidato é PCD.
- **pj**: Pretensão salarial PJ (obrigatório se não houver CLT).
- **resumeType**: Tipo do currículo (1, 2, 3 ou 4).
- **timestamp**: Timestamp de criação/atualização.
- **temporallyJobTime**: Duração máxima de trabalho temporário em meses (0 a 12).
- **travel**: Indica disponibilidade para viagens.

---

## Classe: VisEntityResumeFreeView
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Registra que um recrutador visualizou um currículo de forma gratuita (dentro do limite gratuito mensal). Sua presença indica que essa visualização específica não gerou cobrança. Possui cache de 24 horas e é expurgável mensalmente.

### Campos (enum `Fields`):
- **recruiter** (chave primária): Hash do e-mail do recrutador.
- **email** (chave primária): E-mail do candidato visualizado.
- **date**: Data da visualização gratuita.
- **timestamp**: Timestamp da visualização.

---

## Classe: VisEntityResumeLastView
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Registra a última visualização de um currículo por um recrutador, armazenando um snapshot completo do currículo e da vaga no momento da visualização, além de flags indicando se o currículo estava negativado e se a vaga estava inativa. Possui escrita assíncrona e cache de 1 hora.

### Campos (enum `Fields`):
- **recruiter** (chave primária): Hash do e-mail do recrutador (35 a 50 caracteres).
- **email** (chave primária): E-mail do candidato.
- **date**: Data da visualização.
- **timestamp**: Timestamp da visualização.
- **negativatedResume**: Indica se o currículo estava negativado no momento da visualização.
- **inactivePosition**: Indica se a vaga estava inativa no momento da visualização.
- **resume**: Snapshot do currículo completo (aninhado conforme `VisEntityResume.Fields`).
- **position**: Snapshot da vaga completa (aninhado conforme `VisEntityPosition.Fields`).

---

## Classe: VisEntityResumePerception
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa a percepção/avaliação (opinião) de um recrutador sobre um currículo. O Twin Entity implícito registra quando a opinião é negativa (negativação do currículo). Possui escrita assíncrona, versionamento e cache de 1 hora.

### Campos (enum `Fields`):
- **recruiter** (chave primária): Hash do e-mail do recrutador que registrou a percepção.
- **email** (chave primária): E-mail do candidato avaliado.
- **timestamp**: Timestamp do registro da percepção.
- **date**: Data do registro.

---

## Classe: VisEntityResumeViewFailed
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Registra tentativas de visualização de currículo que falharam, com o status de erro e o JSON de contexto completo. Utilizado para auditoria e diagnóstico de falhas no processo de visualização. Possui cache de 1 hora.

### Campos (enum `Fields`):
- **recruiter** (chave primária): Hash do e-mail do recrutador que tentou visualizar.
- **email** (chave primária): E-mail do candidato cujo currículo não pôde ser visualizado.
- **status** (chave primária): Código HTTP/status de erro da falha.
- **json**: Payload JSON com dados contextuais da tentativa.
- **timestamp**: Timestamp da tentativa.
- **date**: Data da tentativa.

---

## Classe: VisEntityScheduleSendingResumeFees
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Armazena a tarifa programada para envio de currículos por frequência de serviço. Define o custo associado a cada serviço agendado de envio de currículos. Possui versionamento e cache de 1 hora.

### Campos (enum `Fields`):
- **fee**: Valor numérico da tarifa (obrigatório, não negativo).
- **service** (chave primária): Identificador do serviço de envio.

---

## Classe: VisEntitySkill
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa uma skill (habilidade) aprovada no sistema, com seu ranking de relevância (baseado na quantidade de currículos que a possuem), suas skills-pai na hierarquia e seus sinônimos. Possui cache de 1 hora. Inclui lógica de carga inicial que lê `synonyms.json` e um arquivo de contagem de palavras por currículo para calcular o ranking.

### Campos (enum `Fields`):
- **parent**: Lista de skills-pai na hierarquia de habilidades.
- **ranking**: Posição da skill no ranking de relevância (obrigatório, ≥ 1).
- **skill** (chave primária): Nome canônico da skill.
- **synonym**: Lista de sinônimos reconhecidos para esta skill.

### Métodos:
- **getFirstRecordsToInsert()** → `List<CcpBulkItem>`: Lê `synonyms.json` e o arquivo de contagem de palavras por currículo, calcula o número de currículos que contêm cada skill ou seus sinônimos, ordena por relevância decrescente e gera os itens de bulk com o ranking calculado.
- **getResumesCount(CcpJsonRepresentation json, List<String> lines)** → `int` (privado): Conta o total de currículos que mencionam a skill ou qualquer um de seus sinônimos, somando os valores encontrados no arquivo de contagem.

---

## Classe: VisEntitySkillFixHierarchyApproved
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Registra solicitações de correção de hierarquia de skill que foram aprovadas, armazenando o e-mail do solicitante, a explicação da aprovação e a descrição da correção. Possui cache de 1 hora.

### Campos (enum `Fields`):
- **date**: Data da aprovação.
- **email**: E-mail do usuário que solicitou a correção (obrigatório).
- **explanation**: Explicação fornecida pelo aprovador (obrigatório).
- **description**: Descrição da correção de hierarquia proposta (obrigatório).
- **timestamp**: Timestamp da aprovação.

---

## Classe: VisEntitySkillFixHierarchyPending
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa solicitações pendentes de correção de hierarquia de skill aguardando análise. Ao salvar um registro, dispara transferência de dados para `VisEntitySkillFixHierarchyRejected` ou `VisEntitySkillFixHierarchyApproved` conforme a decisão, enviando mensagens de notificação correspondentes. Possui escrita assíncrona e cache de 1 hora.

### Campos (enum `Fields`):
- **date**: Data da solicitação.
- **email**: E-mail do solicitante (obrigatório).
- **description**: Descrição da correção hierárquica proposta (obrigatório).
- **timestamp**: Timestamp da solicitação.

---

## Classe: VisEntitySkillFixHierarchyRejected
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Registra solicitações de correção de hierarquia de skill que foram rejeitadas, armazenando o e-mail do solicitante, a explicação da rejeição e a descrição da correção proposta. Possui cache de 1 hora.

### Campos (enum `Fields`):
- **date**: Data da rejeição.
- **email**: E-mail do solicitante (obrigatório).
- **explanation**: Explicação fornecida pelo revisor para a rejeição (obrigatório).
- **description**: Descrição da correção proposta que foi rejeitada (obrigatório).
- **timestamp**: Timestamp da rejeição.

---

## Classe: VisEntitySkillPending
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Representa novas skills sugeridas aguardando aprovação. Ao salvar, notifica o suporte via `VisTemplatesToNotifySupport.NewSkill` e envia mensagem de pendência via `VisMessages.PendingSkillHierarchy`. A entidade suporta transferência de dados para `VisEntitySkillRejected` (com notificação de rejeição) ou para `VisEntitySkill` (com aprovação via `VisBusinessApprovingSkill` e notificação). Possui escrita assíncrona e cache de 1 hora.

### Campos (enum `Fields`):
- **date**: Data da sugestão.
- **email**: E-mail do usuário que sugeriu a skill (obrigatório).
- **parent**: Lista de skills-pai na hierarquia proposta.
- **ranking**: Posição proposta no ranking (obrigatório).
- **skill** (chave primária): Nome da skill proposta.
- **synonym**: Lista de sinônimos propostos.
- **timestamp**: Timestamp da sugestão.

---

## Classe: VisEntitySkillRejected
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Registra skills sugeridas que foram rejeitadas, armazenando o motivo da rejeição junto aos dados completos da skill proposta. Possui escrita assíncrona e cache de 1 hora.

### Campos (enum `Fields`):
- **date**: Data da rejeição.
- **email**: E-mail do usuário que sugeriu a skill (obrigatório).
- **explanation**: Explicação do motivo da rejeição (obrigatório).
- **parent**: Lista de skills-pai propostas.
- **ranking**: Ranking proposto.
- **skill** (chave primária): Nome da skill rejeitada.
- **synonym**: Lista de sinônimos propostos.
- **timestamp**: Timestamp da rejeição.

---

## Classe: VisEntityVirtualHashGrouper
**Pacote:** `com.vis.entities`
**Tipo:** classe (configurador de entidade Elasticsearch)
**Propósito:** Entidade virtual utilizada para calcular um hash composto que representa uma combinação específica de atributos de matching entre currículo e vaga (senioridade, disponibilidade, PCD, tipo e valor de remuneração, sinônimos de skill). Esse hash é usado internamente pelo sistema de matching para verificar compatibilidade sem buscas complexas. Possui cache de 1 hora.

### Campos (enum `Fields`):
- **seniority** (chave primária): Senioridade (JR, PL, SR, ES).
- **synonym** (chave primária): Array de sinônimos da skill.
- **disponibility** (chave primária): Disponibilidade em dias.
- **pcd** (chave primária): Flag de PCD.
- **moneyValue** (chave primária): Valor monetário (≥ 1000).
- **moneyType** (chave primária): Tipo de remuneração (CLT, BTC, PJ).

---

## Exceptions

---

## Classe: VisErrorBusinessMissingFeeToFrequency
**Pacote:** `com.vis.exceptions`
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando não existe uma tarifa cadastrada para a frequência de envio configurada em uma vaga. Impede o processamento do envio de currículos sem definição de custo associado.

### Métodos:
- **VisErrorBusinessMissingFeeToFrequency(String frequency)** (construtor): Cria a exceção com a mensagem `"It is missing the fee of frequency <frequency>"`.

---

## Classe: VisErrorBusinessRequiredSkillsMissingInResume
**Pacote:** `com.vis.exceptions`
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando um currículo não possui todas as skills obrigatórias exigidas por uma vaga (nem diretamente, nem por sinônimo, nem por relação de parentesco na hierarquia). Carrega a lista das skills faltantes para diagnóstico.

### Campos:
- **requiredSkillsNotFoundInResume** (`List<String>`): Lista com os nomes das skills obrigatórias ausentes no currículo avaliado.

### Métodos:
- **VisErrorBusinessRequiredSkillsMissingInResume(List<String> requiredSkillsNotFoundInResume)** (construtor): Armazena a lista de skills faltantes.

---

## JSON Fields Validation

---

## Enum: VisJsonCommonsFields
**Pacote:** `com.vis.json.fields.validation`
**Tipo:** enum
**Propósito:** Define as validações compartilhadas pelos campos JSON mais comuns do módulo VIS, servindo como fonte de regras reutilizáveis via `@CcpJsonCopyFieldValidationsFrom` em outras entidades. Centraliza restrições de tipo, tamanho e valores permitidos para campos recorrentes como `email`, `seniority`, `ddd`, `skill`, `recruiter`, entre outros.

### Constantes notáveis:
- **btc**: Número entre 1.000 e 100.000.
- **clt**: Número entre 1.500 e 100.000.
- **detail**: String de 35 a 50 caracteres.
- **disponibility**: Número não negativo até 30.
- **domain**: String de 2 a 50 caracteres.
- **ddd**: Número não negativo com lista restrita de DDDs brasileiros válidos.
- **email**: String de 35 a 50 caracteres (hash SHA1 do e-mail).
- **experience**: Valor de tempo (ano) até 70 anos atrás.
- **fee**: Número ≥ 0.
- **from**: String de 35 a 50 caracteres (paginação).
- **language**: JSON aninhado com `name` (string 3-20) e `level` (1 ou 2).
- **listSize**: Número não negativo.
- **pj**: Número entre 2.500 e 100.000.
- **ranking**: Número não negativo ≥ 1.
- **recruiter**: String com regex de e-mail (7 a 100 caracteres).
- **resumeType**: Número 1, 2, 3 ou 4.
- **service**: String de 2 a 20 caracteres.
- **seniority**: String com valores permitidos JR, PL, SR ou ES.
- **skill**: String de 2 a 50 caracteres.
- **synonym**: String de 2 a 50 caracteres.
- **temporallyJobTime**: Número não negativo até 12.
- **title**: String de 35 a 50 caracteres.
- **word**: String de 2 a 50 caracteres.
- **parent**: String de 2 a 50 caracteres.

---

## Enum: VisJsonFieldsPositionStatis
**Pacote:** `com.vis.json.fields.validation`
**Tipo:** enum
**Propósito:** Define as regras de validação dos campos de estatísticas de posições (vagas), utilizado como schema de validação aninhada (nested JSON). Exige que pelo menos um dos campos `pj` ou `clt` esteja presente. Inclui campos de disponibilidade, DDD, experiência, senioridade, PCD, idioma e faixa salarial.

### Constantes notáveis:
- **email** (chave primária): E-mail do recrutador.
- **title** (chave primária): Título da vaga.
- **disponibility**: Disponibilidade (obrigatório).
- **ddd**: DDDs — array 1 a 67 (obrigatório).
- **experience**: Experiência (obrigatório).
- **pcd**: Flag PCD.
- **travel**: Disponibilidade para viagem.
- **negotiableClaim**: Pretensão negociável.

---

## Enum: VisJsonFieldsSkills
**Pacote:** `com.vis.json.fields.validation`
**Tipo:** enum
**Propósito:** Define as regras de validação dos campos de um objeto de skill dentro de um currículo (nested JSON). Usado em `VisEntityResume.Fields.skill` como referência para validação de cada item da lista de skills do candidato.

### Constantes:
- **associated**: Skill associada (string 2-50, opcional).
- **category**: Categoria da skill (string 2-20, obrigatório).
- **parent**: Lista de skills-pai na hierarquia.
- **skill**: Nome canônico da skill (string 2-50, obrigatório).
- **type**: Tipo da skill (string 2-20, obrigatório).
- **word**: Palavra exata como digitada pelo candidato (string 2-50, obrigatório).

---

## Enum: VisJsonFieldsSkillsGroupedByTheirTwoFirstInitials
**Pacote:** `com.vis.json.fields.validation`
**Tipo:** enum que implementa `CcpJsonFieldName`
**Propósito:** Define as regras de validação dos campos de cada objeto de skill dentro do índice `VisEntityGroupPositionsBySkills` (agrupamento por duas primeiras letras). Inclui o campo `positionStatis` para armazenar estatísticas de vagas associadas a cada skill.

### Constantes:
- **date**: Data do registro.
- **positionStatis**: Array de objetos de estatísticas de posições (aninhado, validado por `VisJsonFieldsPositionStatis`).
- **skill**: Nome canônico da skill (obrigatório).
- **timestamp**: Timestamp do registro.
- **word**: Palavra exata indexada (obrigatório).
- **parent**: Lista de skills-pai.

---

## JSON Transformers

---

## Classe: VisJsonTransformerPutEmailHashAndDomainRecruiter
**Pacote:** `com.vis.json.transformers`
**Tipo:** classe (singleton, implementa `CcpBusiness`)
**Propósito:** Transformador de campo aplicado ao e-mail do recrutador durante a persistência da entidade `VisEntityPosition`. Extrai o e-mail original, calcula seu hash SHA1, extrai o domínio (parte antes do `@`) e enriquece o JSON com o hash (substituindo `recruiter`), o e-mail original (em `originalRecruiter`) e o domínio (em `domain`). Garante o anonimato do recrutador no sistema enquanto mantém a rastreabilidade por domínio.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Extrai o e-mail do campo `recruiter`, calcula o hash SHA1, extrai o domínio, e retorna o JSON enriquecido com `originalRecruiter` (e-mail original), `recruiter` (hash SHA1) e `domain` (domínio da empresa).

---

## Scheduling

---

## Classe: VisBusinessGetRecentLoggedUsers
**Pacote:** `com.vis.schedulling`
**Tipo:** classe (singleton, implementa `CcpBusiness`)
**Propósito:** Tarefa agendada (cron) que busca todos os usuários que fizeram login no último ano e os envia para os processos de agrupamento de visualizações de currículos e percepções. Consome o índice `JnEntityDisposableRecord` filtrando registros de sessão com timestamp dentro do período anual.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Consulta o Elasticsearch por registros de sessão dos últimos 365 dias, ordenados por timestamp decrescente, e para cada lote de usuários invoca `VisSendRecentUsersToGroupings` para disparar os agrupamentos assíncronos. Retorna o JSON original.

---

## Classe: VisBusinessGroupResumeViewsByRecruiter
**Pacote:** `com.vis.schedulling`
**Tipo:** classe (singleton, implementa `JnBusinessSendToMensageria`)
**Propósito:** Tarefa agendada que agrupa as visualizações de currículos por recrutador, usando `VisEntityResumeFreeView` como fonte de dados e `VisEntityGroupResumeViewsByRecruiter` como destino do agrupamento paginado. Delega a lógica ao utilitário `VisUtils.groupDetailsByMasters`.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Invoca `VisUtils.groupDetailsByMasters` passando a entidade de origem (`VisEntityResumeFreeView`) e a entidade de agrupamento (`VisEntityGroupResumeViewsByRecruiter`), agrupando pelo campo `email` com ordenação por `timestamp`. Retorna o resultado do agrupamento.

---

## Classe: VisBusinessGroupResumeViewsByResume
**Pacote:** `com.vis.schedulling`
**Tipo:** classe (singleton, implementa `JnBusinessSendToMensageria`)
**Propósito:** Tarefa agendada que agrupa as visualizações de currículos pelo e-mail do candidato (currículo), usando `VisEntityResumeFreeView` como fonte e `VisEntityGroupResumeViewsByResume` como destino. Delega a lógica ao utilitário `VisUtils.groupDetailsByMasters`.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Invoca `VisUtils.groupDetailsByMasters` agrupando registros de `VisEntityResumeFreeView` pelo campo `email` em `VisEntityGroupResumeViewsByResume`, ordenados por `timestamp`. Retorna o resultado do agrupamento.

---

## Classe: VisBusinessGroupResumesOpinionsByRecruiter
**Pacote:** `com.vis.schedulling`
**Tipo:** classe (singleton, implementa `CcpBusiness`)
**Propósito:** Tarefa agendada que agrupa as percepções/avaliações de currículos pelo e-mail do recrutador, usando `VisEntityResumePerception` como fonte e `VisEntityGroupResumesPerceptionsByRecruiter` como destino. Delega ao utilitário `VisUtils.groupDetailsByMasters`.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Invoca `VisUtils.groupDetailsByMasters` agrupando percepções pelo campo `recruiter` em `VisEntityGroupResumesPerceptionsByRecruiter`, ordenados por `timestamp`. Retorna o resultado.

---

## Classe: VisBusinessGroupResumesOpinionsByResume
**Pacote:** `com.vis.schedulling`
**Tipo:** classe (singleton, implementa `JnBusinessSendToMensageria`)
**Propósito:** Tarefa agendada que agrupa as percepções/avaliações de currículos pelo e-mail do candidato, usando `VisEntityResumePerception` como fonte e `VisEntityGroupResumesPerceptionsByResume` como destino. Delega ao utilitário `VisUtils.groupDetailsByMasters`.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Invoca `VisUtils.groupDetailsByMasters` agrupando percepções pelo campo `email` do candidato em `VisEntityGroupResumesPerceptionsByResume`, ordenados por `timestamp`. Retorna o resultado.

---

## Classe: VisBusinessGroupSkills
**Pacote:** `com.vis.schedulling`
**Tipo:** classe (singleton, implementa `CcpBusiness`)
**Propósito:** Tarefa agendada destinada ao agrupamento de skills. Implementação ainda pendente — retorna o JSON de entrada sem processamento.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Retorna o JSON sem processamento (TODO).

---

## Classe: VisBusinessPositionResumesReceivingByFrequency
**Pacote:** `com.vis.schedulling`
**Tipo:** classe (singleton, implementa `CcpBusiness`)
**Propósito:** Tarefa agendada principal do processo de matching. Para uma determinada frequência de envio, busca as vagas agrupadas por recrutador e os currículos atualizados recentemente, e orquestra o filtro, ordenação e envio dos currículos compatíveis para cada recrutador. É a entrada do ciclo de matching periódico (minute, hourly, daily, weekly, monthly).

### Métodos:
- **apply(CcpJsonRepresentation schedullingPlan)** → `CcpJsonRepresentation`: Lê a frequência do JSON de plano de agendamento, define a função de obtenção dos currículos recentes (por timestamp dentro do período da frequência) e a função de obtenção das vagas agrupadas, e delega o processamento completo a `VisUtils.sendFilteredAndSortedResumesAndTheirStatisByEachPositionToEachRecruiter`. Retorna o plano de agendamento original.

---

## Classe: VisBusinessSearchSkills
**Pacote:** `com.vis.schedulling`
**Tipo:** classe (singleton, implementa `CcpBusiness`)
**Propósito:** Tarefa agendada destinada à busca de skills. Implementação pendente — retorna o JSON de entrada sem processamento.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Retorna o JSON sem processamento (TODO).

---

## Services

---

## Enum: VisServiceCompany
**Pacote:** `com.vis.services`
**Tipo:** enum que implementa `JnService`
**Propósito:** Serviço de acesso a dados de empresas. Expõe operações relacionadas à busca de empresas pelo nome. Cada constante é um endpoint de serviço.

### Constantes (operações):

- **SearchCompaniesByTheirFirstThreeInitials**: Busca empresas cadastradas cujo nome começa com as letras digitadas pelo usuário. Extrai as 3 primeiras letras do termo de busca, consulta `VisEntityGroupCompaniesByTheirFirstThreeInitials`, filtra os resultados pelo prefixo completo (se mais de 3 caracteres foram digitados) e retorna a lista filtrada. Se nenhum resultado for encontrado, retorna o próprio termo buscado como sugestão.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Executa a busca conforme descrito.
  - **getJsonValidationClass()** → `Class<?>`: Retorna `FieldsToSearchCompaniesByTheirFirstThreeInitials.class` para validação do input.

### Campos de validação de entrada:
- **FieldsToSearchCompaniesByTheirFirstThreeInitials.search**: String de 3 a 20 caracteres (obrigatório) — termo de busca da empresa.

---

## Enum: VisServicePosition
**Pacote:** `com.vis.services`
**Tipo:** enum que implementa `JnService`
**Propósito:** Serviço de acesso a dados de vagas. Expõe operações de CRUD e consulta de skills relacionadas a vagas.

### Constantes (operações):

- **ChangeStatus**: Altera o status de uma vaga (ativa/inativa) realizando a exclusão lógica via `VisEntityPosition.ENTITY.delete(json)`, o que ativa o fluxo Twin Entity.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

- **GetData**: Busca os dados completos de uma vaga, verificando nas duas entidades (ativa e inativa via Twin), adicionando o campo `activePosition` no retorno para indicar se a vaga está ativa.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

- **GetImportantSkillsFromText**: Operação futura para extrair skills relevantes de um texto de vaga. Ainda sem implementação.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

- **GetResumeList**: Verifica o status de uma skill sugerida (se já existe, está aprovada, pendente ou rejeitada) consultando as entidades `VisEntitySkill`, `VisEntitySkillPending` e `VisEntitySkillRejected`, retornando o status correspondente.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

- **Save**: Salva uma nova vaga ou atualiza uma existente via `VisEntityPosition.ENTITY.save(json)`, disparando automaticamente os fluxos pós-save configurados na entidade.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

- **SuggestNewSkills**: Verifica o status de uma skill proposta para uma vaga (já existe aprovada, pendente, rejeitada ou nova), seguindo o mesmo fluxo de `GetResumeList`.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

---

## Enum: VisServiceRecruiter
**Pacote:** `com.vis.services`
**Tipo:** enum que implementa `JnService`
**Propósito:** Serviço de acesso a dados do recrutador. Expõe operações relacionadas às interações do recrutador com currículos e vagas.

### Constantes (operações):

- **GetAlreadySeenResumes**: Retorna a lista paginada de currículos que o recrutador já avaliou/opinou, consultando `VisEntityGroupResumesPerceptionsByRecruiter`.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

- **GetPositionsFromThisRecruiter**: Retorna a lista paginada de vagas do recrutador consultando `VisEntityGroupPositionsByRecruiter`.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

- **SaveOpinionAboutThisResume**: Registra a percepção/avaliação positiva do recrutador sobre um currículo salvando em `VisEntityResumePerception`.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

- **SendResumesToEmail**: Envia currículos para o e-mail do recrutador via mensageria, usando `VisBusinessRecruiterReceivingResumes` encapsulado em `JnFunctionMensageriaSender`.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

- **ChangeOpinionAboutThisResume**: Remove/reverte a percepção do recrutador sobre um currículo (negativação) via delete em `VisEntityResumePerception`, ativando o Twin Entity.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

---

## Enum: VisServiceResume
**Pacote:** `com.vis.services`
**Tipo:** enum que implementa `JnService`
**Propósito:** Serviço de acesso a dados de currículos. Expõe as operações de CRUD sobre a entidade `VisEntityResume`.

### Constantes (operações):

- **ChangeStatus**: Altera o status do currículo (ativo/inativo) executando delete lógico em `VisEntityResume`, ativando o fluxo Twin Entity (currículo inativo).
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

- **Delete**: Deleta o currículo do candidato via `VisEntityResume.ENTITY.delete(json)`.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

- **GetData**: Busca os dados completos do currículo (em qualquer estado — ativo ou inativo) via `getOneByIdAnyWhere`.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

- **Save**: Salva/atualiza o currículo do candidato, disparando automaticamente cálculo de hashes e envio para recrutadores compatíveis.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`
  - **getJsonValidationClass()** → `Class<?>`: Retorna `VisEntityResume.Fields.class` para validação.

---

## Enum: VisServiceSkills
**Pacote:** `com.vis.services`
**Tipo:** enum que implementa `JnService`
**Propósito:** Serviço de operações sobre skills: solicitação de novas skills, extração de skills de texto livre e correção de hierarquia. Contém a lógica mais rica do módulo de skills.

### Constantes (operações):

- **RequestToCreateNewSkill**: Recebe uma solicitação de nova skill e verifica se ela já existe, está pendente, aprovada ou rejeitada. Se for genuinamente nova, salva em `VisEntitySkillPending` para análise. Retorna status 202 (analisando) se a skill for nova; caso contrário retorna o status correspondente ao estado atual.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

- **GetSkillsFromText**: Recebe um texto livre e extrai as skills reconhecidas pelo sistema nele. Usa o índice `VisEntityGroupPositionsBySkills` (com cache) para lookup das palavras pelas duas primeiras letras. Filtra palavras muito curtas que sejam fragmentos de outras, remove duplicatas de skills (mantendo o sinônimo mais específico), monta um label legível (ex: `"spring (java)"`) e classifica as skills descartadas com o motivo (fragmento de palavra, fragmento de skill maior, skill duplicada). Respeita lista de skills excluídas fornecidas no input.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

- **FixSkillHierarchy**: Registra uma solicitação de correção de hierarquia de skill, salvando em `VisEntitySkillFixHierarchyPending` para análise posterior.
  - **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`

### Método estático auxiliar:
- **getWordStatus(CcpJsonRepresentation group, String word)** → `int`: Verifica se uma palavra existe no grupo de skills em memória. Retorna `0` se presente, `1` se o grupo de iniciais não existe, `2` se a palavra não está no grupo.

---

## Status

---

## Enum: VisProcessStatusResumeView
**Pacote:** `com.vis.status`
**Tipo:** enum que implementa `CcpProcessStatus`
**Propósito:** Define todos os status de processo para a operação de visualização de currículo, cada um com seu código HTTP correspondente. Utilizado pelo sistema de matching para registrar falhas de visualização em `VisEntityResumeViewFailed`.

### Constantes:
- **inactiveResume** (301): Currículo inativo.
- **insufficientFunds** (402): Saldo insuficiente para a visualização.
- **resumeNotFound** (404): Currículo não encontrado.
- **notAllowedRecruiter** (420): Recrutador bloqueado pelo candidato.
- **missingFee** (427): Tarifa não encontrada para o serviço.
- **missingBalance** (423): Registro de saldo não encontrado.
- **negativatedResume** (0): Currículo negativado pelo recrutador.

### Métodos:
- **asNumber()** → `int`: Retorna o código HTTP numérico do status.
- **toBulkItemCreate(CcpJsonRepresentation json)** → `CcpBulkItem`: Cria um item de bulk para persistir o erro de visualização em `VisEntityResumeViewFailed`.

---

## Enum: VisProcessStatusSuggestNewSkill
**Pacote:** `com.vis.status`
**Tipo:** enum que implementa `CcpProcessStatus`
**Propósito:** Define os status de processo para a operação de sugestão de nova skill, com seus códigos HTTP correspondentes.

### Constantes:
- **rejectedSkill** (420): Skill foi rejeitada anteriormente.
- **approvedSkill** (200): Skill já foi aprovada e existe no sistema.
- **pendingSkill** (202): Skill está aguardando análise.
- **alreadyExists** (409): Skill já existe no cadastro.

### Métodos:
- **asNumber()** → `int`: Retorna o código HTTP numérico do status.

---

## Utils

---

## Enum: GetMoneyValuesFromJson
**Pacote:** `com.vis.utils`
**Tipo:** enum
**Propósito:** Gera listas de valores monetários a partir de um JSON, com comportamento diferente dependendo se o contexto é de currículo ou de vaga. Para currículo, gera todos os valores de remuneração do valor declarado até 100.000 (o candidato aceita salários iguais ou maiores). Para vaga, gera todos os valores do máximo declarado até 1.000 (a vaga aceita candidatos que pedem igual ou menos). Usado no cálculo de hashes de compatibilidade.

### Constantes:
- **resume**: Gera lista crescente de valores monetários a partir da pretensão do candidato até 100.000, incrementando de 100 em 100.
  - **apply(CcpJsonRepresentation json, String field)** → `List<CcpJsonRepresentation>`
- **position**: Gera lista decrescente de valores monetários a partir do máximo da vaga até 1.000, decrementando de 100 em 100.
  - **apply(CcpJsonRepresentation json, String field)** → `List<CcpJsonRepresentation>`

### Métodos:
- **apply(CcpJsonRepresentation json, String field)** → `List<CcpJsonRepresentation>` (abstrato): Deve ser implementado por cada constante para retornar os valores monetários do campo especificado.

---

## Enum: ResumeSkillFoundType
**Pacote:** `com.vis.utils`
**Tipo:** enum (package-private)
**Propósito:** Classifica como uma skill obrigatória de uma vaga foi encontrada no currículo do candidato durante o processo de matching.

### Constantes:
- **CONTAINED_IN_RESUME**: A skill foi encontrada diretamente na lista de skills do currículo.
- **SYNONYM**: A skill foi encontrada como sinônimo de uma skill presente no currículo.
- **PARENT**: A skill foi encontrada como skill-pai de alguma skill presente no currículo.

---

## Enum: ResumeSortOptions
**Pacote:** `com.vis.utils`
**Tipo:** enum (package-private)
**Propósito:** Define os critérios de ordenação de currículos em relação a uma vaga. Cada constante representa um critério de comparação numérica entre dois currículos, usando os campos relevantes do JSON do currículo.

### Constantes:
- **disponibility**: Ordena pela disponibilidade (campo `disponibility` do currículo).
- **desiredSkill**: Ordena pela quantidade de skills desejadas da vaga que o currículo possui (calculado antes da comparação).
- **money**: Ordena pela remuneração (campos `clt`, `pj`, `btc` — usa o primeiro campo presente).
- **experience**: Ordena pela experiência profissional.

### Métodos:
- **compare(CcpJsonRepresentation o1, CcpJsonRepresentation o2)** → `int`: Compara dois currículos pelo critério desta constante.
- **compareTo(CcpJsonRepresentation o1, CcpJsonRepresentation o2, String... keys)** → `int` (privado): Compara dois JSONs pelos campos especificados, retornando a primeira diferença encontrada.

---

## Classe: VisBusinessPositionUpdateGroupingByRecruitersAndSendResumes
**Pacote:** `com.vis.utils`
**Tipo:** classe (singleton, implementa `CcpBusiness`)
**Propósito:** Orquestra a atualização completa do agrupamento de vagas por recrutadores e o envio de currículos compatíveis ao ser disparada após um save ou delete de vaga. Duplica o campo `email` para `masters`, reagrupa as vagas por recrutador, busca todos os currículos do último ano, filtra e ordena os currículos compatíveis com a vaga, e salva o resultado paginado em `VisEntityGroupResumesByPosition`.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: (1) Duplica `email` para `masters`; (2) reagrupa vagas por recrutadores; (3) define como obter currículos recentes (todos do último ano); (4) define como obter vagas agrupadas (a própria vaga atual); (5) processa o matching completo; (6) salva os currículos filtrados e ordenados paginados em `VisEntityGroupResumesByPosition`. Retorna o resultado do processamento.

---

## Classe: VisBusinessResumeSendToRecruiters
**Pacote:** `com.vis.utils`
**Tipo:** classe (singleton, implementa `CcpBusiness`)
**Propósito:** Disparada após o save ou reativação de um currículo para enviá-lo imediatamente a recrutadores com vagas compatíveis na frequência `minute`. Usa o currículo recém-salvo como única fonte de currículos e busca todas as vagas ativas da frequência `minute`.

### Métodos:
- **apply(CcpJsonRepresentation resumeWithSkills)** → `CcpJsonRepresentation`: Define `howToObtainResumes` como uma lista com o único currículo recém-salvo, obtém todas as vagas agrupadas na frequência `minute`, e executa o matching e envio via `VisUtils.sendFilteredAndSortedResumesAndTheirStatisByEachPositionToEachRecruiter`. Retorna o currículo original.

---

## Enum: VisFrequencyOptions
**Pacote:** `com.vis.utils`
**Tipo:** enum
**Propósito:** Define as frequências possíveis de envio de currículos para recrutadores, com o valor em horas correspondente a cada frequência. Usado para calcular janelas de tempo nas queries de busca por currículos e vagas recentes.

### Constantes:
- **minute** (1/60 horas): Envio a cada minuto.
- **hourly** (1 hora): Envio a cada hora.
- **daily** (24 horas): Envio diário.
- **weekly** (168 horas): Envio semanal.
- **yearly** (8.766 horas): Envio anual.
- **montly** (730,5 horas): Envio mensal.

### Campos:
- **hours** (`double`): Valor da frequência em horas.

---

## Enum: VisFunctionsGetDisponibilityValuesFromJson
**Pacote:** `com.vis.utils`
**Tipo:** enum que implementa `Function<CcpJsonRepresentation, List<Integer>>`
**Propósito:** Gera listas de valores de disponibilidade (em dias) para o processo de cálculo de hashes de compatibilidade, com comportamento diferente para currículo e vaga.

### Constantes:
- **resume**: Gera lista de disponibilidades do valor declarado pelo candidato até 70 (candidato com disponibilidade X pode aceitar vagas que exijam X, X+1, ..., 70 dias de aviso prévio).
  - **apply(CcpJsonRepresentation json)** → `List<Integer>`
- **position**: Gera lista de disponibilidades do máximo da vaga até 0 (vaga que aceita até X dias de aviso aceita candidatos com disponibilidade X, X-1, ..., 0).
  - **apply(CcpJsonRepresentation json)** → `List<Integer>`

---

## Enum: VisFunctionsGetPcdValuesFromJson
**Pacote:** `com.vis.utils`
**Tipo:** enum que implementa `Function<CcpJsonRepresentation, List<Boolean>>`
**Propósito:** Determina com quais vagas (ou candidatos) um determinado perfil PCD pode ser comparado, retornando a lista de valores booleanos PCD para geração dos hashes de compatibilidade.

### Constantes:
- **resume**: Para candidatos PCD, retorna `[true, false]` (competem em vagas PCD e normais). Para candidatos normais, retorna `[false]`.
  - **apply(CcpJsonRepresentation json)** → `List<Boolean>`
- **position**: Para vagas PCD, retorna `[true]` (aceitam apenas candidatos PCD). Para vagas normais, retorna `[true, false]` (aceitam PCD e não-PCD).
  - **apply(CcpJsonRepresentation json)** → `List<Boolean>`

---

## Enum: VisFunctionsGetSeniorityValueFromJson
**Pacote:** `com.vis.utils`
**Tipo:** enum que implementa `Function<CcpJsonRepresentation, String>`
**Propósito:** Extrai ou calcula o valor de senioridade para o processo de matching, com lógica diferente para currículo (calcula a partir do ano de início de experiência) e para vaga (lê diretamente do campo).

### Constantes:
- **resume**: Calcula a senioridade pelo tempo de experiência: até 2 anos = JR, até 5 anos = PL, até 10 anos = SR, acima = ES. *Nota: a lógica de comparação está invertida no código atual (provavelmente um bug).*
  - **apply(CcpJsonRepresentation json)** → `String`
- **position**: Lê e retorna o campo `seniority` diretamente da vaga.
  - **apply(CcpJsonRepresentation json)** → `String`

---

## Classe: VisGroupDetailsByMasters
**Pacote:** `com.vis.utils`
**Tipo:** classe que implementa `Consumer<CcpJsonRepresentation>`
**Propósito:** Consumidor de stream de registros que os agrupa por um campo-master (ex: e-mail do recrutador ou do candidato), acumulando os registros em memória para depois salvá-los paginados em bulk. Recebe no construtor as entidades de origem e de destino do agrupamento; ao salvar, mapeia cada grupo (entity principal e entity mirror) para sua entidade-destino correspondente.

### Métodos:
- **VisGroupDetailsByMasters(String masterFieldName, CcpEntity entity, CcpEntity entityGrouper)** (construtor): Inicializa o agrupador com o nome do campo-chave, a entidade de origem e a entidade de agrupamento. Configura automaticamente o mapeamento entre as entities e suas mirrors.
- **accept(CcpJsonRepresentation record)** → `void`: Recebe um registro do stream, extrai o valor do campo-master e do campo `entity`, e acumula o registro na lista correspondente.
- **saveAllDetailsGroupedByMasters()** → `VisGroupDetailsByMasters`: Percorre todos os grupos acumulados, divide os registros em páginas usando `VisUtils.getRecordsInPages` e executa o bulk de criação em Elasticsearch via `JnExecuteBulkOperation`. Retorna `this` para encadeamento.

---

## Enum: VisSendEmailMessageAndRegisterEmailSent
**Pacote:** `com.vis.utils`
**Tipo:** enum que implementa `CcpBusiness` e `CcpJsonFieldName`
**Propósito:** Define os templates de e-mail de notificação ao candidato sobre o status do salvamento do currículo, enviando a mensagem via `JnSendMessageToUser` e bloqueando reenvios duplicados via `JnEntityEmailMessageSent`.

### Constantes:
- **resumeSuccessSaving**: Template de confirmação de sucesso no salvamento do currículo.
- **resumeErrorSaving**: Template de notificação de erro no salvamento do currículo.

### Métodos:
- **apply(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Renomeia o campo `originalEmail` para `email`, define o `subjectType` como o nome da constante, e envia a mensagem de e-mail usando o template correspondente com bloqueio de reenvio por `JnEntityEmailMessageSent`. Retorna o JSON original.

---

## Classe: VisSendRecentUsersToGroupings
**Pacote:** `com.vis.utils`
**Tipo:** classe (singleton, implementa `Consumer<List<CcpJsonRepresentation>>`)
**Propósito:** Consumidor de lista de registros de sessão recentes que extrai os e-mails dos usuários e os envia para os quatro processos de agrupamento assíncronos (opiniões por recrutador, opiniões por currículo, visualizações por recrutador, visualizações por currículo), disparando as mensageiras correspondentes.

### Métodos:
- **accept(List<CcpJsonRepresentation> records)** → `void`: Extrai os e-mails dos registros de sessão recentes, monta um JSON com a lista de e-mails no campo `masters` e dispara via mensageria os quatro agrupamentos: `VisBusinessGroupResumesOpinionsByRecruiter`, `VisBusinessGroupResumesOpinionsByResume`, `VisBusinessGroupResumeViewsByRecruiter` e `VisBusinessGroupResumeViewsByResume`.

---

## Classe: VisSorterResumesByPosition
**Pacote:** `com.vis.utils`
**Tipo:** classe que implementa `Comparator<CcpJsonRepresentation>`
**Propósito:** Comparador de currículos em relação a uma vaga específica. Ordena os currículos considerando os critérios de ordenação definidos na vaga (`sortFields`), sempre incluindo `desiredSkill` (quantidade de skills desejadas que o currículo possui) como critério final, de forma decrescente.

### Métodos:
- **VisSorterResumesByPosition(CcpJsonRepresentation position)** (construtor): Armazena a vaga que será usada como referência para a ordenação.
- **compare(CcpJsonRepresentation o1, CcpJsonRepresentation o2)** → `int`: Enriquece ambos os currículos com a contagem de skills desejadas (negativa, para ordenação decrescente), aplica cada critério de `sortFields` em sequência usando `ResumeSortOptions` e retorna a primeira diferença encontrada.
- **putDesiredSkills(CcpJsonRepresentation o1, List<String> desiredSkills)** → `CcpJsonRepresentation` (privado): Conta quantas skills desejadas da vaga o currículo possui (por interseção com a lista de skills do candidato) e armazena o valor negativo no campo `desiredSkill` do JSON para que a ordenação seja decrescente.

---

## Classe: VisUtils
**Pacote:** `com.vis.utils`
**Tipo:** classe utilitária (métodos estáticos)
**Propósito:** Classe central de utilitários do módulo VIS. Concentra a lógica de alto nível do processo de matching entre currículos e vagas: filtragem, ordenação, cálculo de hashes de compatibilidade, agrupamentos, paginação e envio via mensageria. É a "cola" que conecta todos os componentes do fluxo de envio de currículos para recrutadores.

### Métodos:

- **getTenant()** → `String`: Lê e retorna o identificador do tenant (inquilino) a partir das propriedades da aplicação.

- **isInsufficientFunds(int itemsCount, CcpJsonRepresentation fee, CcpJsonRepresentation balance)** → `boolean`: Verifica se o saldo do recrutador é insuficiente para cobrir o custo de envio de `itemsCount` vagas à tarifa informada.

- **sendFilteredAndSortedResumesAndTheirStatisByEachPositionToEachRecruiter(VisFrequencyOptions frequency, ...)** → `List<CcpJsonRepresentation>`: Sobrecarga que cria um JSON de plano com a frequência informada e delega à versão principal.

- **sendFilteredAndSortedResumesAndTheirStatisByEachPositionToEachRecruiter(CcpJsonRepresentation schedullingPlan, ...)** → `List<CcpJsonRepresentation>`: Método central do fluxo de matching. Obtém vagas agrupadas por recrutador e currículos recentes, executa o filtro e ordenação completo, calcula as estatísticas de cada posição e envia o resultado via mensageria (`VisBusinessPositionResumesSend`). Retorna a lista de posições com currículos filtrados, ordenados e estatísticas.

- **getLastUpdated(CcpEntity entity, VisFrequencyOptions valueOf, String filterFieldName)** → `List<CcpJsonRepresentation>`: Consulta o Elasticsearch buscando registros da entidade informada atualizados dentro da janela de tempo da frequência. Retorna a lista de resultados.

- **getAllPositionsGroupedByRecruiters(VisFrequencyOptions frequency)** → `CcpJsonRepresentation`: Consulta todas as vagas ativas com a frequência informada, agrupadas por e-mail do recrutador (como um mapa).

- **groupPositionsGroupedByRecruiters(CcpJsonRepresentation json)** → `CcpJsonRepresentation`: Lê a lista de `masters` (recrutadores) do JSON e agrupa suas vagas usando `VisEntityGroupPositionsByRecruiter` como destino.

- **groupDetailsByMasters(CcpJsonRepresentation json, CcpEntity entityToRead, CcpEntity entityWhereGroup, CcpJsonFieldName masterField, CcpJsonFieldName ascField)** → `CcpJsonRepresentation`: Método genérico de agrupamento. Consulta todos os registros da `entityToRead` cujos `masterField` estejam na lista `masters` do JSON, os agrupa pelo campo master usando `VisGroupDetailsByMasters` e os salva paginados na `entityWhereGroup`.

- **saveRecordsInPages(List<CcpJsonRepresentation> records, CcpJsonRepresentation primaryKeySupplier, CcpEntity entity)** → `void`: Divide a lista de registros em páginas de 10 itens e os salva em bulk na entidade informada.

- **getRecordsInPages(List<CcpJsonRepresentation> records, CcpJsonRepresentation primaryKeySupplier, CcpEntity entity)** → `List<CcpBulkItem>`: Divide os registros em páginas de 10 itens, criando um `CcpBulkItem` para cada página com os campos de paginação (`listSize`, `from`, `detail`) e retorna a lista de itens de bulk.

- **getAllPositionsWithFilteredAndSortedResumesAndTheirStatis(...)** → `List<CcpJsonRepresentation>` (privado): Para cada combinação de recrutador + currículo, realiza uma consulta em `unionAll` com as entidades relevantes e filtra: taxa não encontrada (lança exceção), saldo não encontrado, saldo insuficiente, currículo inativo, currículo não encontrado, currículo negativado, empresa bloqueada. Monta a estrutura de posição com currículos filtrados, agrupa e ordena.

- **getPositionWithFilteredResumes(...)** → `CcpJsonRepresentation` (privado): Para cada vaga do recrutador, verifica compatibilidade de DDDs, hashes de matching, skills obrigatórias, e se o currículo já foi visto. Se compatível, adiciona o currículo à lista de currículos da posição.

- **getRequiredSkillsInThisResume(...)** → `List<CcpJsonRepresentation>` (privado): Verifica se todas as skills obrigatórias da vaga estão presentes no currículo (diretamente, por sinônimo ou por parentesco). Lança `VisErrorBusinessRequiredSkillsMissingInResume` se alguma skill obrigatória estiver faltando.

- **resumeAlreadySeen(...)** → `boolean` (privado): Verifica se um currículo já foi visto pelo recrutador e se não houve atualização posterior. Se o currículo foi atualizado após a última visualização, considera-o não visto.

- **getPositionWithSortedResumes(...)** → `CcpJsonRepresentation` (privado): Ordena os currículos de uma posição usando `VisSorterResumesByPosition` quando há mais de 1 currículo.

- **getAllSearchParameters(...)** → `List<CcpJsonRepresentation>` (privado): Gera todas as combinações de (recrutador, currículo) como parâmetros de busca para o `unionAll`.

- **getHashes(CcpJsonRepresentation json)** → `List<String>` (privado): Gera todos os hashes virtuais possíveis para um currículo ou vaga, combinando senioridade × disponibilidade × PCD × valores monetários (BTC/CLT/PJ), usando `VisEntityVirtualHashGrouper` para calcular cada hash.

- **getMoneyValues(String enumsType, CcpJsonRepresentation json)** → `List<CcpJsonRepresentation>` (privado): Obtém todos os valores monetários BTC, CLT e PJ usando `GetMoneyValuesFromJson` com o comportamento adequado (currículo ou vaga).

- **getStatisToThisPosition(CcpJsonRepresentation positionsWithFilteredResumes)** → `CcpJsonRepresentation` (privado): Calcula as estatísticas (média de disponibilidade, experiência, BTC, CLT, PJ e contagem total de currículos) para o conjunto de currículos filtrados de uma posição e as adiciona ao objeto de resultado.


---

# Documentação dos Módulos de Infraestrutura e Negócio — jobsnow

---

## Módulo: ccp_cache_aws-redis

---

## Classe: AwsRedisCache
**Pacote:** com.ccp.implementations.cache.aws.redis
**Tipo:** classe (package-private)
**Propósito:** Implementação concreta da interface `CcpCache` usando Redis via AWS (Jedis). Armazena, recupera e remove valores em cache com suporte a TTL. Ao inicializar, lê host e porta do Redis via system properties ou variáveis de ambiente (`REDIS_HOST`, `REDIS_PORT`).

### Métodos:
- **get(String key)** → Object: Busca o valor armazenado para a chave. Desserializa o JSON do Redis e reconstrói `CcpJsonRepresentation` se o valor for um objeto, ou retorna o tipo primitivo correspondente (boolean, número, string).
- **put(String key, Object value, int secondsDelay)** → CcpCache: Armazena um valor no Redis com expiração em segundos. Se o valor for um `CcpJsonRepresentation`, converte para `LinkedHashMap` antes de serializar.
- **delete(String key)** → V: Remove a entrada do cache pela chave e retorna o valor que estava armazenado antes da remoção.

---

## Classe: CcpAwsRedisCache
**Pacote:** com.ccp.implementations.cache.aws.redis
**Tipo:** classe
**Propósito:** Provedor de injeção de dependência (DI) que expõe `AwsRedisCache` como implementação de `CcpCache`. Segue o padrão do framework: uma classe pública wrapper que registra a implementação real no container de DI.

### Métodos:
- **getInstance()** → CcpCache: Instancia e retorna um novo `AwsRedisCache`.

---

## Módulo: ccp_cache_gcp-memcache

---

## Classe: CcpGcpMemCache
**Pacote:** com.ccp.implementations.cache.gcp.memcache
**Tipo:** classe
**Propósito:** Provedor de DI que expõe `GcpMemCache` como implementação de `CcpCache` no Google Cloud Platform.

### Métodos:
- **getInstance()** → CcpCache: Instancia e retorna um novo `GcpMemCache`.

---

## Classe: GcpMemCache
**Pacote:** com.ccp.implementations.cache.gcp.memcache
**Tipo:** classe (package-private)
**Propósito:** Implementação concreta de `CcpCache` usando o serviço Memcache do Google App Engine. Suporta get, put com TTL e delete com retorno do valor anterior.

### Métodos:
- **get(String key)** → Object: Recupera o valor armazenado no Memcache; se o resultado for um `Map`, reconstrói e retorna um `CcpJsonRepresentation`.
- **put(String key, Object value, int secondsDelay)** → CcpCache: Persiste um valor no Memcache com expiração em segundos. Converte `CcpJsonRepresentation` para `LinkedHashMap` antes de armazenar.
- **delete(String key)** → V: Remove o valor do Memcache pela chave, retornando o valor que estava armazenado.

---

## Módulo: ccp_cron-tasks_jobsnow

---

## Classe: CcpCronTasksController
**Pacote:** com.ccp.jn.cron.controller
**Tipo:** classe
**Propósito:** Ponto de entrada para execução de tarefas agendadas (cron jobs). Recebe o nome de um tópico e parâmetros em formato JSON, resolve o processo responsável por esse tópico via `CcpMensageriaReceiver` e o executa.

### Métodos:
- **main(CcpBusiness jnAsyncBusinessNotifyError, String topic, String parameters)** → void: Converte os parâmetros em `CcpJsonRepresentation`, obtém o receptor de mensagens adequado para o ambiente, resolve a lógica de negócio correspondente ao tópico e a executa com os parâmetros fornecidos.

---

## Módulo: ccp_db-bulk_elasticsearch

---

## Classe: BulkItem
**Pacote:** com.ccp.implementations.db.bulk.elasticsearch
**Tipo:** classe (package-private)
**Propósito:** Representa um item individual de operação em bulk para o Elasticsearch. Converte um `CcpBulkItem` (abstrato) em sua representação de texto no formato NDJSON (Newline Delimited JSON) exigido pela API `_bulk` do Elasticsearch.

### Métodos:
- **BulkItem(CcpBulkItem item)** → construtor: A partir de um `CcpBulkItem`, determina a operação (create, update, delete), gera o conteúdo NDJSON correspondente e captura nome de entidade e id.
- **toString()** → String: Representação textual do item para fins de diagnóstico.
- **hashCode()** → int: Hash baseado na combinação de entidade e id.
- **equals(Object obj)** → boolean: Compara dois `BulkItem` considerando iguais aqueles com mesma entidade e mesmo id.

---

## Enum: BulkOperation
**Pacote:** com.ccp.implementations.db.bulk.elasticsearch
**Tipo:** enum
**Propósito:** Define as três operações bulk suportadas pelo Elasticsearch (`delete`, `update`, `create`) e encapsula a lógica de montagem das linhas NDJSON para cada operação.

### Métodos:
- **getContent(CcpBulkItem item)** → String: Monta o conteúdo completo (primeira linha de metadados + segunda linha de dados) para a operação bulk, separados por nova linha.
- **getSecondLine(CcpJsonRepresentation json)** → String (abstrato por variante): Retorna a segunda linha do payload: vazia para `delete`, `{"doc": {...}}` para `update`, e o JSON direto para `create`.

---

## Classe: CcpElasticSerchDbBulk
**Pacote:** com.ccp.implementations.db.bulk.elasticsearch
**Tipo:** classe
**Propósito:** Provedor de DI que cria e expõe uma instância de `ElasticSerchDbBulkExecutor` como implementação de `CcpBulkExecutor`.

### Métodos:
- **getInstance()** → CcpBulkExecutor: Cria uma nova lista vazia de itens e retorna um novo `ElasticSerchDbBulkExecutor`.

---

## Classe: ElasticSearchBulkOperationResult
**Pacote:** com.ccp.implementations.db.bulk.elasticsearch
**Tipo:** classe (package-private)
**Propósito:** Representa o resultado de uma operação individual dentro de uma resposta bulk do Elasticsearch. Localiza o resultado correspondente ao item dentro da lista retornada pela API `_bulk` e expõe status HTTP, detalhes de erro e o item original.

### Métodos:
- **ElasticSearchBulkOperationResult(CcpBulkItem bulkItem, List resultados)** → construtor: Filtra a lista de resultados pelo id e pela entidade do item, lançando `CcpErrorBulkItemNotFound` se não encontrado.
- **getErrorDetails()** → CcpJsonRepresentation: Retorna o JSON de detalhes do erro, ou JSON vazio se não houve erro.
- **getBulkItem()** → CcpBulkItem: Retorna o item bulk original ao qual este resultado se refere.
- **hasError()** → boolean: Retorna `true` se os detalhes de erro não estiverem vazios.
- **status()** → int: Retorna o código HTTP de status da operação individual.
- **toString()** → String: Serializa o resultado como JSON para diagnóstico.

---

## Classe: ElasticSerchDbBulkExecutor
**Pacote:** com.ccp.implementations.db.bulk.elasticsearch
**Tipo:** classe (package-private)
**Propósito:** Implementação de `CcpBulkExecutor` que acumula itens bulk e os envia em uma única requisição HTTP `POST /_bulk` ao Elasticsearch no formato NDJSON.

### Métodos:
- **addRecord(CcpBulkItem bulkItem)** → CcpBulkExecutor: Retorna uma nova instância do executor com o item adicionado à lista (imutabilidade funcional).
- **getBulkOperationResult()** → List&lt;CcpBulkOperationResult&gt;: Monta o corpo NDJSON com todos os itens acumulados, executa a requisição `_bulk`, e retorna a lista de resultados individuais. Após a execução, limpa a lista em bloco sincronizado.
- **clearRecords()** → CcpBulkExecutor: Retorna uma nova instância com lista vazia.

---

## Módulo: ccp_db-crud_elasticsearch

---

## Classe: CcpElasticSearchCrud
**Pacote:** com.ccp.implementations.db.crud.elasticsearch
**Tipo:** classe
**Propósito:** Provedor de DI que expõe `ElasticSearchCrud` como implementação de `CcpCrud`.

### Métodos:
- **getInstance()** → CcpCrud: Instancia e retorna um novo `ElasticSearchCrud`.

---

## Classe: ElasticSearchCrud
**Pacote:** com.ccp.implementations.db.crud.elasticsearch
**Tipo:** classe (package-private)
**Propósito:** Implementação principal de `CcpCrud` e `CcpUnionAllExecutor` para o Elasticsearch. Oferece operações de CRUD (leitura por id, existência, upsert com script Painless, deleção) e busca multi-get (`_mget`).

### Métodos:
- **getRequestBodyToMultipleGet(Collection jsons, CcpEntity... entities)** → CcpJsonRepresentation: Monta o corpo da requisição `_mget` a partir de uma coleção de jsons e entidades, descartando jsons que não possuem todas as chaves primárias da entidade.
- **getRequestBodyToMultipleGet(Set ids, CcpEntity... entities)** → CcpJsonRepresentation: Variante que monta o corpo `_mget` a partir de um conjunto de ids simples (strings).
- **getOneById(String entityName, String id)** → CcpJsonRepresentation: Faz `GET /{entity}/_source/{id}` e retorna o documento; lança exceção se não encontrado (404).
- **exists(String entityName, String id)** → boolean: Faz `HEAD /{entity}/_doc/{id}` e retorna `true` se o status for 200 (OK).
- **save(String entityName, CcpJsonRepresentation json, String id)** → CcpJsonRepresentation: Faz upsert via `POST /{entity}/_update/{id}` usando script Painless (`ctx._source.putAll(params)`). Em conflito (409), aguarda 1 segundo e tenta novamente.
- **delete(String entityName, String id)** → boolean: Faz `DELETE /{entity}/_doc/{id}` e retorna `true` se o documento foi efetivamente deletado.
- **unionAll(Collection values, CcpEntity... entities)** → CcpSelectUnionAll: Executa `_mget` para a coleção de valores e entidades informados e retorna um `CcpSelectUnionAll` com os resultados.
- **getUnionAllExecutor()** → CcpUnionAllExecutor: Retorna a própria instância como executor de union-all.

---

## Enum: ElasticSearchHttpStatus
**Pacote:** com.ccp.implementations.db.crud.elasticsearch
**Tipo:** enum
**Propósito:** Representa os possíveis status HTTP de resposta do Elasticsearch (`OK`, `NOT_FOUND`, `CREATED`) como constantes que também implementam `CcpBusiness`, permitindo usá-las como transformadores de JSON.

### Métodos:
- **apply(CcpJsonRepresentation json)** → CcpJsonRepresentation: Adiciona a si mesmo (o status) como valor no campo `ElasticSearchHttpStatus` do JSON, para que o chamador possa inspecionar o status após a chamada HTTP.

---

## Classe: FunctionResponseHandlerToMget
**Pacote:** com.ccp.implementations.db.crud.elasticsearch
**Tipo:** classe (package-private)
**Propósito:** Transformador de resposta `_mget` que normaliza cada item retornado pelo Elasticsearch: extrai `_source`, injeta `_id` e `_index` no nível raiz do JSON e lança exceção em caso de erro na operação individual.

### Métodos:
- **apply(CcpJsonRepresentation json)** → CcpJsonRepresentation: Verifica se o item contém erro (lança `CcpErrorCrudMultiGetSearchFailed`), caso contrário extrai o `_source` e adiciona `_id` e `_index`.

---

## Módulo: ccp_db-query_elasticsearch

---

## Classe: CcpElasticSearchQueryExecutor
**Pacote:** com.ccp.implementations.db.query.elasticsearch
**Tipo:** classe
**Propósito:** Provedor de DI que expõe `ElasticSearchQueryExecutor` como implementação de `CcpQueryExecutor`.

### Métodos:
- **getInstance()** → CcpQueryExecutor: Instancia e retorna um novo `ElasticSearchQueryExecutor`.

---

## Classe: ElasticSearchQueryExecutor
**Pacote:** com.ccp.implementations.db.query.elasticsearch
**Tipo:** classe (package-private)
**Propósito:** Implementação de `CcpQueryExecutor` para Elasticsearch. Suporta pesquisas simples, paginadas via scroll, contagem, deleção e atualização por query, além de agregações.

### Métodos:
- **getTermsStatis(CcpQueryOptions query, String[] resources, String fieldName)** → CcpJsonRepresentation: Executa uma query de agregação e retorna um mapa com o nome do bucket como chave e a contagem como valor.
- **delete(CcpQueryOptions query, String... resources)** → CcpJsonRepresentation: Executa `POST /_delete_by_query` nos índices informados.
- **update(CcpQueryOptions query, String[] resources, CcpJsonRepresentation newValues)** → CcpJsonRepresentation: Executa `POST /_update_by_query` nos índices informados.
- **consumeQueryResult(CcpQueryOptions query, String[] resources, String scrollTime, Integer pageSize, Consumer consumer, String... fields)** → CcpQueryExecutor: Versão item-a-item do consumo paginado; delega ao método com `Long pageSize` e `Consumer<List>`.
- **consumeQueryResult(CcpQueryOptions query, String[] resources, String scrollTime, Long pageSize, Consumer&lt;List&gt; consumer, String... fields)** → CcpQueryExecutor: Itera sobre todos os registros usando a API de scroll do Elasticsearch, entregando lotes ao consumer. Na primeira página usa `/_search?size=N&scroll=T`, nas subsequentes usa `/_search/scroll`.
- **total(CcpQueryOptions query, String[] resources)** → long: Executa `POST /{indexes}/_count` e retorna o total de documentos que correspondem à query.
- **getIndexes(String[] resources)** → String: Converte o array de nomes de índices no path URL no formato `/{i1},{i2}`.
- **getResultAsList(CcpQueryOptions query, String[] resources, String... fields)** → List&lt;CcpJsonRepresentation&gt;: Executa `POST /_search` e retorna os hits como lista de objetos JSON normalizados.
- **getResultAsMap(CcpQueryOptions query, String[] resources, String field)** → CcpJsonRepresentation: Retorna os resultados da pesquisa como um mapa onde o id do documento é a chave e o valor do campo solicitado é o valor.
- **getResultAsPackage(String url, CcpHttpMethods method, int status, CcpQueryOptions query, String[] resources, String... fields)** → CcpJsonRepresentation: Executa uma requisição HTTP customizável e retorna o pacote de resposta bruto do Elasticsearch.
- **getMap(CcpQueryOptions query, String[] resources, String field)** → CcpJsonRepresentation: Extrai agregações e retorna como mapa chave-valor.
- **getAggregations(CcpQueryOptions query, String... resources)** → CcpJsonRepresentation: Executa a pesquisa e extrai o bloco `aggregations` da resposta.
- **getAggregations(CcpJsonRepresentation resultAsPackage)** → CcpJsonRepresentation (estático): Normaliza o pacote de resposta extraindo total e todas as agregações (tanto métricas simples quanto buckets).

---

## Classe: FunctionResponseHandlerToConsumeSearch
**Pacote:** com.ccp.implementations.db.query.elasticsearch
**Tipo:** classe (package-private)
**Propósito:** Transformador de resposta para pesquisas paginadas via scroll. Extrai os hits do pacote retornado e também preserva o `_scroll_id` para a próxima página.

### Métodos:
- **apply(CcpJsonRepresentation json)** → CcpJsonRepresentation: Extrai os hits de `hits.hits`, aplica `FunctionSourceHandler` a cada um, e retorna um JSON com os campos `hits` (lista normalizada) e `_scroll_id`.

---

## Classe: FunctionResponseHandlerToSearch
**Pacote:** com.ccp.implementations.db.query.elasticsearch
**Tipo:** classe (package-private)
**Propósito:** Transformador de resposta para pesquisas normais (não-scroll). Extrai a lista de hits do pacote de resposta do Elasticsearch e os normaliza.

### Métodos:
- **apply(CcpJsonRepresentation json)** → List&lt;CcpJsonRepresentation&gt;: Navega até `hits.hits`, aplica `FunctionSourceHandler` a cada item e retorna a lista resultante.

---

## Classe: FunctionSourceHandler
**Pacote:** com.ccp.implementations.db.query.elasticsearch
**Tipo:** classe (package-private)
**Propósito:** Normaliza um único hit de resposta de pesquisa do Elasticsearch: extrai o conteúdo de `_source` e injeta os campos `id` e `entity` (derivados de `_id` e `_index`) no nível raiz.

### Métodos:
- **apply(CcpJsonRepresentation x)** → CcpJsonRepresentation: Extrai `_source`, lê `_id` e `_index` do wrapper, e retorna o documento de negócio enriquecido com `id` e `entity`.

---

## Módulo: ccp_db-utils_elasticsearch

---

## Classe: CcpElasticSearchDbRequest
**Pacote:** com.ccp.implementations.db.utils.elasticsearch
**Tipo:** classe
**Propósito:** Provedor de DI que expõe `ElasticSearchDbRequester` como implementação de `CcpDbRequester`.

### Métodos:
- **getInstance()** → CcpDbRequester: Instancia e retorna um novo `ElasticSearchDbRequester`.

---

## Classe: ElasticSearchDbRequester
**Pacote:** com.ccp.implementations.db.utils.elasticsearch
**Tipo:** classe (package-private)
**Propósito:** Implementação de `CcpDbRequester` para Elasticsearch. Centraliza a lógica de conexão (leitura de `application_properties` ou fallback para localhost), execução de requisições HTTP ao Elasticsearch e setup de banco de dados (criação/recriação de índices a partir de scripts de mapeamento e inserção de registros iniciais).

### Métodos:
- **executeHttpRequest(String trace, String url, CcpHttpMethods method, Integer expectedStatus, String body, CcpJsonRepresentation headers, CcpHttpResponseTransform transformer)** → V: Carrega as propriedades de conexão, mescla headers com credenciais e executa a requisição HTTP com corpo textual.
- **executeHttpRequest(String trace, String complemento, CcpHttpMethods method, Integer expectedStatus, CcpJsonRepresentation body, String[] resources, CcpHttpResponseTransform transformer)** → V: Variante que monta a URL combinando a lista de índices com o complemento de path.
- **executeHttpRequest(String trace, String url, CcpHttpMethods method, CcpJsonRepresentation flows, CcpJsonRepresentation body, CcpHttpResponseTransform transformer)** → V: Variante que aceita um mapa de flows (handlers por código HTTP) em vez de um único status esperado.
- **executeHttpRequest(String trace, String url, CcpHttpMethods method, Integer expectedStatus, CcpJsonRepresentation body, CcpHttpResponseTransform transformer)** → V: Variante simplificada com corpo JSON e status esperado único.
- **getConnectionDetails()** → CcpJsonRepresentation: Retorna os detalhes de conexão (URL, Authorization, Content-Type, Accept), carregando-os do arquivo de propriedades se ainda não carregados.
- **createTables(String pathScript, String pathClasses, String mappingErrors, String insertErrors)** → CcpDbRequester: Invoca o setup completo do banco de dados, gravando em arquivo os erros de mapeamento incorreto e os resultados das inserções iniciais.
- **executeDatabaseSetup(String pathClasses, String hostFolder, String pathScript, Consumer whenFieldsIncorrect, Consumer whenError)** → List&lt;CcpBulkOperationResult&gt;: Varre o diretório de classes Java, instancia cada `CcpEntityConfigurator`, valida o mapeamento contra o script, recria o índice (DELETE + PUT) e acumula registros iniciais para inserção em bulk.
- **getFieldNameToEntity()** → String: Retorna `"_index"` como nome do campo que identifica a entidade nos resultados de busca.
- **getFieldNameToId()** → String: Retorna `"_id"` como nome do campo que identifica o id do documento.

---

## Módulo: ccp_email_sendgrid

---

## Classe: CcpSendGridEmailSender
**Pacote:** com.ccp.implementations.email.sendgrid
**Tipo:** classe
**Propósito:** Provedor de DI que expõe `SendGridEmailSender` como implementação de `CcpEmailSender`.

### Métodos:
- **getInstance()** → CcpEmailSender: Instancia e retorna um novo `SendGridEmailSender`.

---

## Classe: SendGridEmailSender
**Pacote:** com.ccp.implementations.email.sendgrid
**Tipo:** classe (package-private)
**Propósito:** Implementação de `CcpEmailSender` usando a API HTTP v3 do SendGrid. Envia e-mails simples (texto ou HTML) para um ou mais destinatários, validando previamente cada endereço de e-mail.

### Métodos:
- **sendSimpleTextEmailMessage(String providerToken, String providerUrl, String templateId, String sender, String subject, String message, CcpHttpContentType contentType, String... emails)** → CcpJsonRepresentation: Monta o payload JSON para a API do SendGrid com personalizations, from, subject e content; valida todos os e-mails destinatários antes do envio; executa `POST` esperando HTTP 202.
- **getPersonalizations(String... emails)** → List (privado): Valida sintaxe de cada e-mail, lançando `CcpErrorEmailInvalidAdresses` se algum for inválido; monta a estrutura `personalizations` do payload SendGrid.

---

## Módulo: ccp_file-bucket_gcp

---

## Classe: CcpGcpFileBucket
**Pacote:** com.ccp.implementations.file.bucket.gcp
**Tipo:** classe
**Propósito:** Provedor de DI que expõe `GcpFileBucket` como implementação de `CcpFileBucket`.

### Métodos:
- **getInstance()** → CcpFileBucket: Instancia e retorna um novo `GcpFileBucket`.

---

## Classe: GcpFileBucket
**Pacote:** com.ccp.implementations.file.bucket.gcp
**Tipo:** classe (package-private)
**Propósito:** Implementação de `CcpFileBucket` usando o Google Cloud Storage. Permite ler arquivos de um bucket GCP autenticando com credenciais definidas na variável de ambiente `credentials_file`.

### Métodos:
- **get(String tenant, String bucketName, String fileName)** → String: Lê o conteúdo do arquivo no bucket GCP e o retorna codificado em Base64.
- **delete(String tenant, String bucketName, String fileName)** → String: Implementação pendente (retorna string vazia); destinado a excluir um arquivo específico de um bucket.
- **save(String tenant, String bucketName, String fileName, String fileContent)** → String: Implementação pendente (retorna string vazia); destinado a salvar um arquivo em um bucket.
- **delete(String tenant, String bucketName)** → String: Implementação pendente (retorna null); destinado a excluir um bucket inteiro.

---

## Módulo: ccp_http_apache-mime

---

## Classe: ApacheMimeHttpRequester
**Pacote:** com.ccp.implementations.http.apache.mime
**Tipo:** classe (package-private)
**Propósito:** Implementação de `CcpHttpRequester` usando Apache HttpClient com suporte a multipart/MIME. Executa requisições HTTP simples e multipart, gerando também a representação cURL equivalente para fins de diagnóstico/log.

### Métodos:
- **executeHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, String body)** → CcpHttpResponse: Executa uma requisição HTTP com corpo textual, adicionando todos os headers informados.
- **executeMultiPartHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, List bodyTexts, List bodyBinaries)** → CcpHttpResponse: Executa uma requisição multipart/form-data, adicionando partes binárias (arquivos) e partes de texto ao builder do Apache MIME.
- **toCurl(HttpUriRequest request)** → String (privado): Gera a representação em linha de comando `curl` da requisição HTTP, incluindo método, URL, headers e body.

---

## Classe: CcpApacheMimeHttp
**Pacote:** com.ccp.implementations.http.apache.mime
**Tipo:** classe
**Propósito:** Provedor de DI que expõe `ApacheMimeHttpRequester` como implementação de `CcpHttpRequester`.

### Métodos:
- **getInstance()** → CcpHttpRequester: Instancia e retorna um novo `ApacheMimeHttpRequester`.

---

## Classe: CcpHttpRequestRetryHandler
**Pacote:** com.ccp.implementations.http.apache.mime
**Tipo:** classe (package-private)
**Propósito:** Implementação de `HttpRequestRetryHandler` do Apache HttpClient. Define a política de retry: até 3 tentativas, exceto para timeouts, host desconhecido, timeout de conexão, erros SSL e requisições com body (não idempotentes).

### Métodos:
- **retryRequest(IOException exception, int executionCount, HttpContext context)** → boolean: Decide se deve tentar novamente a requisição com base no tipo de exceção e no número de tentativas já realizadas.
- **getClient()** → CloseableHttpClient (estático): Cria e configura um `CloseableHttpClient` com suporte a certificados auto-assinados (TrustSelfSignedStrategy) e com este retry handler registrado.

---

## Enum: CustomContentType
**Pacote:** com.ccp.implementations.http.apache.mime
**Tipo:** enum
**Propósito:** Mapeia os tipos de conteúdo do framework (`CcpHttpContentType`) para os objetos `ContentType` do Apache HttpComponents, usados na construção de partes multipart.

### Valores:
- **TEXT_PLAIN**: mapeia para `ContentType.TEXT_PLAIN` do Apache.
- **TEXT_HTML**: mapeia para `ContentType.TEXT_HTML` do Apache.

---

## Enum: HttpMethod
**Pacote:** com.ccp.implementations.http.apache.mime
**Tipo:** enum
**Propósito:** Mapeia os verbos HTTP (`POST`, `GET`, `PUT`, `PATCH`, `DELETE`, `HEAD`) para as classes concretas do Apache HttpClient, criando os objetos de requisição adequados com ou sem body.

### Métodos:
- **getMethodWithBody(String url, String body)** → HttpRequestBase (abstrato por variante): Cria o objeto de requisição HTTP com o body informado (como `StringEntity` JSON onde aplicável).
- **getMethodWithoutBody(String url)** → HttpEntityEnclosingRequestBase (abstrato por variante): Cria o objeto de requisição sem body pré-definido (usado para multipart).
- **getMethod(String url, CcpJsonRepresentation headers, String body)** → HttpRequestBase: Cria a requisição com body e injeta todos os headers informados.

---

## Módulo: ccp_instant-messenger_telegram

---

## Classe: CcpTelegramInstantMessenger
**Pacote:** com.ccp.implementations.instant.messenger.telegram
**Tipo:** classe
**Propósito:** Provedor de DI que expõe `TelegramInstantMessenger` como implementação de `CcpInstantMessenger`.

### Métodos:
- **getInstance()** → CcpInstantMessenger: Instancia e retorna um novo `TelegramInstantMessenger`.

---

## Classe: TelegramInstantMessenger
**Pacote:** com.ccp.implementations.instant.messenger.telegram
**Tipo:** classe (package-private)
**Propósito:** Implementação de `CcpInstantMessenger` para o Telegram. Permite enviar mensagens de texto (com paginação automática para mensagens acima de 4096 caracteres) e arquivos via bot do Telegram, tratando erros de bot bloqueado (403) e rate limit (429).

### Métodos:
- **sendTextMessage(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String message)** → CcpJsonRepresentation: Envia uma mensagem de texto para o chat especificado. Divide automaticamente mensagens longas em partes de até 4096 caracteres, usando cada resposta como `replyTo` da próxima parte.
- **sendFile(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String fileName, String caption, Byte[] fileContent)** → CcpJsonRepresentation: Envia um arquivo via `sendDocument` do Telegram usando requisição multipart, retornando metadados do envio (fileName, caption, conteúdo, messageId).
- **getHttpHandler(CcpJsonFieldName botType, String botToken, String resource)** → CcpHttpHandler (privado): Constrói o `CcpHttpHandler` para a URL da API do Telegram com os handlers de erro (403 → bot bloqueado, 429 → rate limit, 200 → sucesso).
- **throwThisBotWasBlockedByThisUser(String token)** → CcpInstantMessenger: Lança `CcpErrorInstantMessageThisBotWasBlockedByThisUser` (usado como referência de método nos handlers).
- **throwTooManyRequests()** → CcpInstantMessenger: Lança `CcpHttpTooManyRequests` (usado como referência de método nos handlers).

---

## Módulo: ccp_json_gson

---

## Classe: CcpGsonJsonHandler
**Pacote:** com.ccp.implementations.json.gson
**Tipo:** classe
**Propósito:** Provedor de DI que expõe `GsonJsonHandler` como implementação de `CcpJsonHandler`.

### Métodos:
- **getInstance()** → CcpJsonHandler: Instancia e retorna um novo `GsonJsonHandler`.

---

## Classe: GsonJsonHandler
**Pacote:** com.ccp.implementations.json.gson
**Tipo:** classe (package-private)
**Propósito:** Implementação de `CcpJsonHandler` usando a biblioteca Gson. Provê serialização, desserialização, formatação e validação de JSON. Aplica a estratégia de exclusão `JsonRepresentationExclusionStrategy` para ignorar campos do tipo `Class` durante a serialização.

### Métodos:
- **toJson(Object md)** → String: Serializa o objeto para JSON aplicando a estratégia de exclusão, depois desserializa e re-serializa para normalizar (remove campos `Class` e garante formato compacto).
- **asPrettyJson(Object md)** → String: Serializa o objeto para JSON formatado (pretty print) com a estratégia de exclusão.
- **fromJson(String str)** → T: Desserializa uma string JSON para um objeto genérico (tipicamente `Map` ou `List`).
- **isValidJson(String src)** → boolean: Retorna `true` se a string puder ser desserializada como um `Map` JSON válido; `false` caso contrário.

---

## Classe: JsonRepresentationExclusionStrategy
**Pacote:** com.ccp.implementations.json.gson
**Tipo:** classe (package-private)
**Propósito:** Estratégia de exclusão Gson (singleton) que evita a serialização de campos cujo tipo é `Class` ou subtipos (ex: `Class<?>`, `Class<T>`), impedindo erros de serialização circular ou indesejada de metadados de classe.

### Métodos:
- **shouldSkipField(FieldAttributes f)** → boolean: Retorna `true` se o tipo declarado do campo começar com o nome da classe `java.lang.Class`.
- **shouldSkipClass(Class clazz)** → boolean: Sempre retorna `false` (nenhuma classe inteira é excluída, apenas campos específicos).

---

## Módulo: ccp_main-authentication_gcp-oauth

---

## Classe: CcpGcpMainAuthentication
**Pacote:** com.ccp.implementations.main.authentication.gcp.oauth
**Tipo:** classe
**Propósito:** Provedor de DI que expõe `GcpOauthAuthenticationProvider` como implementação de `CcpAuthenticationProvider`.

### Métodos:
- **getInstance()** → CcpAuthenticationProvider: Instancia e retorna um novo `GcpOauthAuthenticationProvider`.

---

## Classe: GcpOauthAuthenticationProvider
**Pacote:** com.ccp.implementations.main.authentication.gcp.oauth
**Tipo:** classe
**Propósito:** Implementação de `CcpAuthenticationProvider` que obtém tokens JWT de acesso do Google Cloud Platform via OAuth2 usando as credenciais de conta de serviço definidas na variável de ambiente `GOOGLE_APPLICATION_CREDENTIALS`.

### Métodos:
- **getJwtToken()** → String: Lê o arquivo de credenciais GCP, cria um `GoogleCredential` com o escopo `cloud-platform`, atualiza o token de acesso e o retorna como string.

---

## Módulo: ccp_mensageria-consumer_gcp-pubsub-pull_dependency-chooser

---

## Classe: CcpMessageReceiver
**Pacote:** com.ccp.topic.consumer.pubsub.pull
**Tipo:** classe
**Propósito:** Implementação de `MessageReceiver` do GCP Pub/Sub que recebe mensagens de uma subscription via pull. Desserializa a mensagem recebida e delega o processamento à lógica de negócio. Em caso de sucesso chama `ack()`, em caso de erro chama `nack()` e notifica o handler de erros.

### Métodos:
- **CcpMessageReceiver(CcpBusiness notifyError, CcpEntity asyncTask, String name, CcpBusiness jnAsyncBusinessNotifyError)** → construtor: Inicializa o receptor com as referências de negócio, entidade de tarefa assíncrona, nome da subscription e handler de erro.
- **receiveMessage(PubsubMessage message, AckReplyConsumer consumer)** → void: Converte o payload da mensagem Pub/Sub para `CcpJsonRepresentation`, executa o processamento de negócio (atualmente comentado/placeholder) e confirma (`ack`) ou rejeita (`nack`) a mensagem conforme o resultado.

---

## Classe: CcpPubSubStarter
**Pacote:** com.ccp.topic.consumer.pubsub.pull
**Tipo:** classe
**Propósito:** Inicia e gerencia um subscriber Pub/Sub pull para uma subscription específica do GCP. Carrega credenciais via `GOOGLE_APPLICATION_CREDENTIALS`, cria o subscriber com thread pool configurável e aguarda mensagens de forma síncrona.

### Métodos:
- **CcpPubSubStarter(CcpBusiness notifyError, CcpMessageReceiver topic, int threads)** → construtor: Inicializa carregando as credenciais e configurando o handler de erro, topic e número de threads.
- **synchronizeMessages()** → CcpPubSubStarter: Cria o subscriber GCP, inicia o consumo assíncrono e aguarda término. Em caso de tópico não encontrado ou outros erros, notifica o handler sem propagar exceção.

---

## Módulo: ccp_mensageria-consumer_gcp-pubsub-push-spring_dependency

---

## Classe: CcpMensageriaConsumerGcpPubSubPushSpringStarter
**Pacote:** com.ccp.jn.topic.consumer.pubsub.push.application
**Tipo:** classe (Spring Boot Application + RestController)
**Propósito:** Aplicação Spring Boot que recebe mensagens do GCP Pub/Sub via push (HTTP POST). Funciona como endpoint HTTP onde o GCP envia as mensagens; desserializa o payload, decodifica o campo `data` de Base64 e delega ao `JnMensageriaReceiver`. Possui também um endpoint de teste direto sem Base64.

### Métodos:
- **main(String[] args)** → void: Inicializa as dependências do framework (Elasticsearch, Telegram, SendGrid, GCP, Gson, HTTP) via `CcpDependencyInjection` e sobe a aplicação Spring Boot.
- **onReceiveMessage(String topic, Map body)** → void (`POST /{topic}`): Extrai o campo `message.data` do payload Pub/Sub, decodifica de Base64, converte para `CcpJsonRepresentation` e executa o processo associado ao tópico.
- **onReceiveMessageTesting(String topic, Map json)** → void (`POST /{topic}/testing`): Endpoint de teste que passa o JSON diretamente (sem decodificação Base64) ao `JnMensageriaReceiver`.

---

## Módulo: ccp_mensageria-sender_gcp-pubsub

---

## Classe: CcpGcpPubSubMensageriaSender
**Pacote:** com.ccp.implementations.mensageria.sender.gcp.pubsub
**Tipo:** classe
**Propósito:** Provedor de DI que expõe `GcpPubSubMensageriaSender` como implementação de `CcpMensageriaSender`.

### Métodos:
- **getInstance()** → CcpMensageriaSender: Instancia e retorna um novo `GcpPubSubMensageriaSender`.

---

## Classe: GcpPubSubMensageriaSender
**Pacote:** com.ccp.implementations.mensageria.sender.gcp.pubsub
**Tipo:** classe (package-private)
**Propósito:** Implementação de `CcpMensageriaSender` para publicar mensagens no GCP Pub/Sub. Mantém um cache de `Publisher` por tópico para reutilização. Oferece três variantes de envio: via SDK nativo (send2/sendToMensageria) e via API REST HTTP (send1).

### Métodos:
- **send2(Enum topicName, String... msgs)** → CcpMensageriaSender: Publica mensagens usando o SDK do Pub/Sub sem callback assíncrono.
- **sendToMensageria(String topicId, String... msgs)** → CcpMensageriaSender: Publica mensagens com callback assíncrono (`ApiFutureCallback`) para tratamento de sucesso/falha. Encerra o publisher após uso.
- **send1(Enum topicName, String... msgs)** → CcpMensageriaSender: Publica mensagens via chamada HTTP à API REST do Pub/Sub, codificando cada mensagem em Base64, usando JWT do `CcpAuthenticationProvider` como Bearer token.
- **map(String message)** → CcpJsonRepresentation (privado): Codifica a mensagem em Base64 e a envolve no formato `{"data": "<base64>"}` esperado pela API REST do Pub/Sub.
- **getPublisher(String topicName)** → Publisher (privado): Retorna o publisher cacheado para o tópico, ou cria um novo autenticado com as credenciais GCP.

---

## Módulo: ccp_password_mindrot

---

## Classe: CcpMindrotPasswordHandler
**Pacote:** com.ccp.implementations.password.mindrot
**Tipo:** classe
**Propósito:** Provedor de DI que expõe `MindrotPasswordHandler` como implementação de `CcpPasswordHandler`.

### Métodos:
- **getInstance()** → CcpPasswordHandler: Instancia e retorna um novo `MindrotPasswordHandler`.

---

## Classe: MindrotPasswordHandler
**Pacote:** com.ccp.implementations.password.mindrot
**Tipo:** classe (package-private)
**Propósito:** Implementação de `CcpPasswordHandler` usando a biblioteca jBCrypt (Mindrot). Provê hash e verificação de senha usando BCrypt com fator de custo 12.

### Métodos:
- **matches(String password, String hash)** → boolean: Verifica se a senha em texto plano corresponde ao hash BCrypt armazenado.
- **getHash(String password)** → String: Gera um novo salt BCrypt (fator 12) e retorna o hash da senha fornecida.

---

## Módulo: ccp_rest-api-handler-exception_spring

---

## Classe: CcpRestApiExceptionHandlerSpring
**Pacote:** com.ccp.rest.api.spring.exceptions.handler
**Tipo:** classe (`@RestControllerAdvice`)
**Propósito:** Handler global de exceções para APIs REST Spring Boot. Trata três categorias: erros de validação JSON (`CcpJsonValidationError` → 422), erros de fluxo de negócio (`CcpErrorFlowDisturb` → status dinâmico), e exceções genéricas (500 → notificadas ao handler configurado externamente). Também filtra e compacta o stack trace para incluir apenas linhas relevantes ao domínio do projeto.

### Métodos:
- **handle(CcpJsonValidationError e)** → Map: Retorna HTTP 422 com o JSON de erros de validação.
- **handle(CcpErrorFlowDisturb e, HttpServletResponse res)** → Map: Configura o status HTTP dinâmico do fluxo de negócio, monta a resposta com a mensagem e campos relevantes do JSON do erro.
- **handle(Throwable e)** → void: Para exceções genéricas, invoca `genericExceptionHandler` se configurado; caso contrário lança `CcpErrorExceptionHandlerIsMissing`.
- **getHandledExceptionToLog(Throwable e)** → CcpJsonRepresentation (estático): Converte a exceção em `CcpJsonRepresentation` e filtra o stack trace.
- **getHandledExceptionToLog(CcpJsonRepresentation json)** → CcpJsonRepresentation (estático): Filtra o stack trace no JSON removendo linhas de frameworks externos (mantendo apenas linhas que contenham pacotes listados em `systems` do `application_properties`), e adiciona um hash do stack trace filtrado.
- **methodNoSupported()** → void: Trata `HttpRequestMethodNotSupportedException` retornando HTTP 405 sem corpo.

---

## Classe: CcpErrorExceptionHandlerIsMissing
**Pacote:** com.ccp.rest.api.spring.servlet.exceptions
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada quando uma exceção genérica é capturada pelo handler global mas o campo estático `genericExceptionHandler` de `CcpRestApiExceptionHandlerSpring` não foi inicializado, indicando um erro de configuração da aplicação.

### Métodos:
- **CcpErrorExceptionHandlerIsMissing(Throwable e)** → construtor: Cria a exceção com mensagem explicativa e a exceção original como causa.

---

## Classe: CcpErrorWebFilterEmailIsInvalid
**Pacote:** com.ccp.rest.api.spring.servlet.exceptions
**Tipo:** classe (RuntimeException)
**Propósito:** Exceção lançada pelo filtro `CcpValidEmailFilter` quando a URL da requisição não contém nenhum dos segmentos de URL esperados que indicam onde o e-mail deveria estar.

### Métodos:
- **CcpErrorWebFilterEmailIsInvalid(String url, String... filtered)** → construtor: Cria a exceção com mensagem informando a URL recebida e os segmentos esperados.

---

## Classe: CcpPutSessionValuesAndExecuteTaskFilter
**Pacote:** com.ccp.rest.api.spring.servlet.filters
**Tipo:** classe (Filter Jakarta)
**Propósito:** Filtro de servlet que enriquece cada requisição HTTP com valores de sessão (IP, sessionToken, userAgent, email, language) e opcionalmente executa uma tarefa de negócio sobre o JSON da requisição antes de passá-la adiante na cadeia de filtros. Também configura os headers CORS em cada resposta.

### Métodos:
- **CcpPutSessionValuesAndExecuteTaskFilter(CcpBusiness task)** → construtor: Configura o filtro com a tarefa a ser executada sobre o JSON da requisição (ou `DO_NOTHING` para o modo sem tarefa).
- **doFilter(ServletRequest req, ServletResponse res, FilterChain chain)** → void: Adiciona headers CORS, ignora requisições `OPTIONS`, envolve a requisição em `CcpPutSessionValuesRequestWrapper` (que injeta valores de sessão e executa a tarefa) e continua a cadeia.
- **init(FilterConfig filterConfig)** → void: Inicialização do filtro (vazio).
- **destroy()** → void: Destruição do filtro (vazio).

---

## Classe: CcpValidEmailFilter
**Pacote:** com.ccp.rest.api.spring.servlet.filters
**Tipo:** classe (Filter Jakarta)
**Propósito:** Filtro de servlet que valida a sintaxe do e-mail extraído da URL da requisição. Retorna HTTP 400 se o e-mail for inválido; caso contrário continua a cadeia. Também configura headers CORS.

### Métodos:
- **CcpValidEmailFilter(String... filtered)** → construtor: Configura os segmentos de URL após os quais o e-mail é esperado (ex: `"login/"`).
- **getEmailSyntaxFilter(String... filtered)** → CcpValidEmailFilter (estático): Factory method para criação do filtro.
- **doFilter(ServletRequest req, ServletResponse res, FilterChain chain)** → void: Extrai o e-mail da URL, valida sua sintaxe e prossegue (ou rejeita com 400).
- **extractEmail(String url)** → String (privado): Localiza o segmento esperado na URL e extrai o e-mail que o segue.
- **init(FilterConfig)** → void: Inicialização (vazia).
- **destroy()** → void: Destruição (vazia).
- **toString()** → String: Representação textual do filtro com os segmentos configurados.

---

## Interface: CcpJsonExtractorFromHttpServletRequest
**Pacote:** com.ccp.rest.api.spring.servlet.request
**Tipo:** interface
**Propósito:** Interface auxiliar com método default para extrair o JSON do corpo de um `ServletRequest` usando Jackson `ObjectMapper`. Usada como mixin por `CcpPutSessionValuesRequestWrapper`.

### Métodos:
- **extractJsonFromHttpServletRequest(ServletRequest request)** → Map&lt;String, Object&gt;: Lê o `InputStream` da requisição e desserializa o corpo JSON como `Map`.

---

## Classe: CcpJsonServletInputStream
**Pacote:** com.ccp.rest.api.spring.servlet.request
**Tipo:** classe (extends ServletInputStream)
**Propósito:** Implementação de `ServletInputStream` que lê a partir de um `CcpJsonRepresentation` convertido em `InputStream`. Permite substituir o body original de uma requisição HTTP por um JSON modificado, necessário para o padrão de request wrapper do Spring.

### Métodos:
- **CcpJsonServletInputStream(CcpJsonRepresentation json)** → construtor: Converte o JSON em `InputStream` para leitura.
- **isFinished()** → boolean: Retorna `true` se não há mais bytes disponíveis no stream.
- **isReady()** → boolean: Sempre retorna `true`.
- **setReadListener(ReadListener listener)** → void: Lança `UnsupportedOperationException` (operação assíncrona não suportada).
- **read()** → int: Lê o próximo byte do stream JSON.

---

## Classe: CcpPutSessionValuesRequestWrapper
**Pacote:** com.ccp.rest.api.spring.servlet.request
**Tipo:** classe (extends HttpServletRequestWrapper)
**Propósito:** Wrapper de `HttpServletRequest` que sobrescreve `getInputStream()` para injetar valores de sessão (email, ip, sessionToken, userAgent, language) no JSON do corpo da requisição e, opcionalmente, transformá-lo com uma `CcpBusiness`. Extrai o e-mail da URL quando o body está vazio.

### Métodos:
- **CcpPutSessionValuesRequestWrapper(HttpServletRequest request, CcpBusiness task)** → construtor: Armazena a requisição original e a tarefa de transformação do JSON.
- **getInputStream()** → ServletInputStream: Extrai o JSON original da requisição, injeta valores de sessão, aplica a tarefa de transformação e retorna o novo stream. Em caso de falha na leitura, extrai e-mail da URL e retorna stream com valores de sessão mínimos.
- **getSessionValues()** → CcpJsonRepresentation (protegido): Retorna os valores de sessão extraídos sem o body original.

---

## Classe: CcpRestApiUtils
**Pacote:** com.ccp.rest.api.utils
**Tipo:** classe
**Propósito:** Utilitário estático para a camada REST API. Atualmente provê apenas verificação se a aplicação está rodando em ambiente local.

### Métodos:
- **isLocalEnvironment()** → boolean (estático): Lê o arquivo `application_properties` e retorna o valor booleano do campo `localEnvironment`, permitindo que a aplicação escolha entre implementações locais e de produção (GCP) para serviços como cache, mensageria e bucket.

---

## Módulo: ccp_text-extractor_apache-tika

---

## Classe: CcpApacheTikaTextExtractor
**Pacote:** com.ccp.implementations.text.extractor.apache.tika
**Tipo:** classe
**Propósito:** Provedor de DI que expõe `ApacheTikaTextExtractor` como implementação de `CcpTextExtractor`.

### Métodos:
- **getInstance()** → CcpTextExtractor: Instancia e retorna um novo `ApacheTikaTextExtractor`.

---

## Classe: ApacheTikaTextExtractor
**Pacote:** com.ccp.implementations.text.extractor.apache.tika
**Tipo:** classe (package-private)
**Propósito:** Implementação de `CcpTextExtractor` usando Apache Tika. Recebe o conteúdo de um arquivo codificado em Base64 (no formato `data:...;base64,...` ou apenas a parte Base64) e extrai o texto legível do documento (PDF, DOCX, etc.).

### Métodos:
- **extractText(String content)** → String: Decodifica o Base64 do conteúdo, cria um `ByteArrayInputStream`, usa `AutoDetectParser` do Tika para detectar o formato do arquivo e extrair o texto via `BodyContentHandler`.
- **getParameterAsByteArrayInputStream(String content)** → ByteArrayInputStream (privado): Decodifica o Base64 e encapsula em `ByteArrayInputStream`.
- **getByteArrayFromBase64String(String content)** → byte[] (privado): Trata o formato `data:...;base64,<dados>` extraindo apenas a parte dos dados antes de decodificar.

---

## Módulo: jb_business_jobsnow

---

## Classe: JbBotEngine
**Pacote:** com.jb.business.bots.engine
**Tipo:** classe
**Propósito:** Motor central dos bots de suporte Telegram do jobsnow. Inicializado como singleton, carrega do Elasticsearch toda a configuração dos bots (tipos, comandos, passos, mensagens, usuários permitidos e explicações) e gerencia o fluxo de interação: receber texto do usuário, identificar o bot e comando, avançar na sessão multi-passo e enviar respostas.

### Tipos internos relevantes:

#### Enum: JnBotType
Representa os dois tipos de bot: `user` (público) e `support` (restrito a usuários autorizados).
- **getBot()** → Bot: Retorna a instância de Bot carregada para este tipo.
- **isRestricted()** → boolean (abstrato): `user` retorna `false`; `support` retorna `true`.

#### Interface: JbBotBusiness
Interface que estende `CcpBusiness` e `CcpJsonFieldName`, representando qualquer componente do bot (bot, comando, passo) que processe mensagens.
- **name()** → String: Nome do componente (padrão: nome da classe).
- **isVisible(CcpJsonRepresentation json)** → boolean: Se o componente deve ser listado/executado para o usuário atual (padrão: `true`).
- **hasPriority(CcpJsonRepresentation json)** → boolean: Se o componente tem prioridade sobre sessão corrente (padrão: `false`).
- **getBot(CcpJsonRepresentation json)** → Bot: Recupera a instância de Bot pelo nome no JSON.
- **getLoadedCommand(CcpJsonRepresentation json)** → BotCommand: Recupera o comando corrente da sessão.
- **loadLabelsWithLanguages(...)** → List: Filtra registros de uma entidade pelo valor de um campo, retornando as linhas que correspondem.
- **getExplanation(CcpJsonRepresentation json)** → String: Texto de explicação do componente (padrão: vazio).
- **hasExplanation(CcpJsonRepresentation json)** → boolean: Se o componente possui texto de explicação (padrão: `false`).
- **getIdentifier(CcpJsonRepresentation json)** → String: Identificador textual do componente no idioma do usuário.

#### Enum interno: CommonsBotCommandStep
Passos de controle comuns a todos os bots: `removeSession` (encerra sessão), `chatId` (responde com o id do chat), `showAllCommands` (lista comandos visíveis), `explainThisBot` (explica o bot), `explainThisCommand` (explica o comando corrente).

---

## Enum: JbSupportBotCommands
**Pacote:** com.jb.business.bots.engine
**Tipo:** enum
**Propósito:** Cataloga os comandos disponíveis no bot de suporte. Atualmente contém apenas `solveLoginTokenTicket`.

---

## Classe: JbBotSolveLoginTokenTicket
**Pacote:** com.jb.business.bots.login.token
**Tipo:** classe (implementa `JbBotBusiness`)
**Propósito:** Lógica de negócio do passo do bot responsável por resolver tickets de token de login (Unlock e Resend). Utiliza condicional if-else: se for o último ticket pendente, lança erro de fluxo; caso contrário, resolve o próximo ticket.

### Métodos:
- **apply(CcpJsonRepresentation json)** → CcpJsonRepresentation: Executa o fluxo condicional — se `ifIsTheLastLoginTokenTicket` for verdadeiro dispara `throwNewHasNoMoreLoginTokenTicketsToSolve`, senão executa `solveLoginTokenTicketsFunction`.
- **getJsonValidationClass()** → Class: Retorna `StepFields.class` para validação dos campos obrigatórios (`typedValue`).

#### Enum interno: StepFields
Campo obrigatório de entrada do passo: `typedValue` (valor digitado pelo operador no bot).

---

## Enum: LoginTokenTicketsJsonConditions
**Pacote:** com.jb.business.bots.login.token
**Tipo:** enum (implementa `Predicate<CcpJsonRepresentation>`)
**Propósito:** Predicados reutilizáveis para avaliar condições sobre o JSON de tickets de token de login.

### Valores:
- **hasAlegation**: Retorna `true` se o campo de alegação no idioma corrente não estiver vazio.
- **ifIsTheLastLoginTokenTicket**: Retorna `true` se a posição atual (counter + 1) for igual ao tamanho da lista de tickets, ou seja, se este é o último ticket a ser resolvido.

### Métodos:
- **getAlegation(CcpJsonRepresentation json)** → String (estático): Extrai a alegação do ticket navegando pelo JSON usando o idioma e o nome da entidade como caminho.

---

## Enum: LoginTokenTicketsJsonFields
**Pacote:** com.jb.business.bots.login.token
**Tipo:** enum
**Propósito:** Define os nomes dos campos usados no JSON de controle de iteração sobre a lista de tickets: `counter` (posição atual), `listValues` (lista de tickets), `listSize` (tamanho da lista), `position` (posição 1-based para exibição).

---

## Enum: LoginTokenTicketsJsonTransformers
**Pacote:** com.jb.business.bots.login.token
**Tipo:** enum (implementa `CcpBusiness`)
**Propósito:** Transformadores de JSON que encapsulam as operações de leitura, seleção e resolução de tickets de token de login pendentes.

### Valores/Métodos:
- **readAllLoginTokenTicketsFunction**: Consulta o Elasticsearch buscando todos os tickets pendentes (`JnEntityLoginTokenRequestResend` e `JnEntityLoginTokenRequestUnlock`), retorna JSON com a lista, tamanho e contador atual. Lança NOT_FOUND se não houver tickets.
- **searchAlegations**: Busca no banco a mensagem de sistema associada à entidade do ticket (explicação da alegação do usuário).
- **chooseOneAlegation**: Extrai a alegação já presente no JSON (quando não precisa buscar do banco).
- **solveLoginTokenTicketsFunction**: Resolve o ticket na posição `counter`: busca todos os tickets, obtém o ticket corrente, exclui o registro da entidade correspondente, busca ou seleciona a alegação e incrementa o contador.
- **throwNewHasNoMoreLoginTokenTicketsToSolve**: Lança exceção de fluxo `NOT_FOUND` indicando que não há mais tickets para resolver.

---

## Entidades do módulo jb_business_jobsnow

As entidades abaixo são definições de tabelas Elasticsearch para o sistema de bots. Todas implementam `CcpEntityConfigurator`, usam `@CcpEntityCache(3600)` (cache de 1 hora), transformadores padrão `JnJsonTransformersFieldsEntityDefault` e validadores definidos pelos seus próprios enums `Fields`.

---

## Classe: JbEntityBot
**Pacote:** com.jb.entities
**Tipo:** classe (entidade)
**Propósito:** Entidade que representa um bot cadastrado no sistema. Armazena o nome do bot e a lista de comandos disponíveis nele. Dados iniciais inserem o bot de suporte com o comando `solveLoginTokenTicket`.

### Campos:
- **botName** (chave primária): Nome do bot (ex: `support`, `user`).
- **commandName**: Lista de nomes de comandos disponíveis neste bot.

### Métodos:
- **getFirstRecordsToInsert()** → List&lt;CcpBulkItem&gt;: Retorna o registro inicial do bot de suporte com seus comandos.

---

## Classe: JbEntityBotAllowedUser
**Pacote:** com.jb.entities
**Tipo:** classe (entidade)
**Propósito:** Entidade que armazena os usuários autorizados a usar um bot restrito. Dados iniciais configuram o chatId `751717896L` como usuário permitido do bot de suporte.

### Campos:
- **botName** (chave primária): Nome do bot restrito.
- **allowedUser**: Lista de chatIds (Long) dos usuários autorizados.

### Métodos:
- **getFirstRecordsToInsert()** → List&lt;CcpBulkItem&gt;: Retorna o registro inicial com o usuário autorizado do bot de suporte.

---

## Classe: JbEntityBotCommand
**Pacote:** com.jb.entities
**Tipo:** classe (entidade, versionável)
**Propósito:** Entidade que representa um comando de bot com seus parâmetros nomeados. Permite que o engine extraia valores do texto digitado pelo usuário e os mapeie para parâmetros do comando.

### Campos:
- **commandName** (chave primária): Nome do comando.
- **parameterName**: Lista de nomes de parâmetros do comando (pode ser vazia).

### Métodos:
- **getFirstRecordsToInsert()** → List&lt;CcpBulkItem&gt;: Retorna o registro inicial do comando `solveLoginTokenTicket` sem parâmetros.

---

## Classe: JbEntityBotCommandExplanation
**Pacote:** com.jb.entities
**Tipo:** classe (entidade, versionável)
**Propósito:** Entidade que armazena a explicação (descrição) de um comando de bot em um idioma específico. Usada pelo passo `explainThisCommand`.

### Campos:
- **commandName** (chave primária): Nome do comando.
- **language** (chave primária): Idioma da explicação.
- **message**: Texto explicativo do comando.

### Métodos:
- **getFirstRecordsToInsert()** → List&lt;CcpBulkItem&gt;: Retorna a explicação em português do comando `solveLoginTokenTicket`.

---

## Classe: JbEntityBotCommandName
**Pacote:** com.jb.entities
**Tipo:** classe (entidade, versionável)
**Propósito:** Armazena o nome localizado (traduzido) de um comando de bot para cada idioma. O identificador de um comando no chat é o valor do campo `message` desta entidade para o idioma do usuário.

### Campos:
- **commandName** (chave primária): Nome técnico do comando.
- **language** (chave primária): Idioma da tradução.
- **message**: Nome do comando neste idioma (ex: `"solucionarTicketsDeTokenDeLogin"` em português).

### Métodos:
- **getFirstRecordsToInsert()** → List&lt;CcpBulkItem&gt;: Retorna o nome português do comando `solveLoginTokenTicket`.

---

## Classe: JbEntityBotCommandStep
**Pacote:** com.jb.entities
**Tipo:** classe (entidade, versionável)
**Propósito:** Define um passo (step) de um comando de bot. Contém o nome do motor de negócio a ser instanciado via reflexão, o próximo passo após a execução bem-sucedida e o fluxo alternativo (mapeamento status HTTP → próximo passo para fluxos de erro).

### Campos:
- **stepName** (chave primária): Nome do passo.
- **stepFlow**: Array de objetos `{status, stepName}` mapeando status de erro para o próximo passo.
- **engine**: Nome completo da classe Java a instanciar como `CcpBusiness` (ex: `com.jb.business.bots.login.token.JbBotSolveLoginTokenTicket`).
- **nextStep**: Nome do próximo passo após sucesso.

### Métodos:
- **getFirstRecordsToInsert()** → List&lt;CcpBulkItem&gt;: Configura o passo `solveLoginTokenTicket` apontando para si mesmo como próximo passo.

---

## Classe: JbEntityBotCommandStepEndMessage
**Pacote:** com.jb.entities
**Tipo:** classe (entidade, versionável)
**Propósito:** Armazena a mensagem enviada ao usuário ao final da execução bem-sucedida de um passo. Suporta texto e arquivos (via campos `instantMessageType`, `fileName`, `caption`, `contentType`).

### Campos:
- **stepName** (chave primária): Nome do passo ao qual a mensagem pertence.
- **language** (chave primária): Idioma da mensagem.
- **message**: Conteúdo da mensagem.
- **instantMessageType**: Tipo de mensagem (`text`, `file`, etc.).
- **caption, contentType, fileName**: Metadados opcionais para mensagens do tipo arquivo.

---

## Classe: JbEntityBotCommandStepExplanation
**Pacote:** com.jb.entities
**Tipo:** classe (entidade)
**Propósito:** Armazena a explicação exibida ao usuário quando ocorre um erro de validação (`CcpJsonValidationError`) durante a execução de um passo.

### Campos:
- **stepName** (chave primária): Nome do passo.
- **language** (chave primária): Idioma.
- **message**: Texto explicativo do passo para o usuário.

---

## Classe: JbEntityBotCommandStepFlowMessage
**Pacote:** com.jb.entities
**Tipo:** classe (entidade, versionável)
**Propósito:** Armazena mensagens associadas a status específicos de fluxo de erro (`CcpErrorFlowDisturb`). Quando um passo lança um erro de fluxo, o bot envia a mensagem correspondente ao status e idioma.

### Campos:
- **stepName** (chave primária): Nome do passo.
- **status** (chave primária): Código HTTP do status de fluxo (ex: 404, 403).
- **language** (chave primária): Idioma.
- **message, instantMessageType, caption, contentType, fileName**: Conteúdo e tipo da mensagem.

---

## Classe: JbEntityBotCommandStepSession
**Pacote:** com.jb.entities
**Tipo:** classe (entidade, descartável diariamente)
**Propósito:** Persiste a sessão corrente de um usuário em um passo de um comando de bot. Armazena o passo atual, o comando, o bot e o JSON acumulado da sessão. A anotação `@CcpEntityDisposable` garante que registros expirados sejam descartados diariamente.

### Campos:
- **chatId** (chave primária): Id do chat no Telegram.
- **botName** (chave primária): Nome do bot.
- **commandName**: Comando corrente na sessão.
- **stepName**: Passo corrente na sessão.
- **json**: JSON acumulado com os dados da interação até o momento.

---

## Classe: JbEntityBotCommandStepStartMessage
**Pacote:** com.jb.entities
**Tipo:** classe (entidade, versionável)
**Propósito:** Armazena a mensagem enviada ao usuário ao iniciar um passo. Também inclui configuração de mensagens de sistema (alegações) para os bots Resend e Unlock. Dados iniciais configuram o template de mensagem de início do passo `solveLoginTokenTicket` com caption para o arquivo enviado ao suporte.

### Campos:
- **stepName** (chave primária): Nome do passo.
- **language** (chave primária): Idioma.
- **message, instantMessageType, caption, contentType, fileName**: Conteúdo e metadados da mensagem de início.

### Métodos:
- **getFirstRecordsToInsert()** → List&lt;CcpBulkItem&gt;: Retorna o registro da mensagem de início do passo `solveLoginTokenTicket` e dois registros de mensagens de sistema (alegações Resend e Unlock em português).
- **toSystemMessage(CcpEntity entity, JnLanguage language, String message)** → CcpBulkItem (privado): Cria um bulk item para inserção de uma mensagem de sistema associada a uma entidade e idioma.

---

## Classe: JbEntityBotExplanation
**Pacote:** com.jb.entities
**Tipo:** classe (entidade)
**Propósito:** Armazena a descrição geral de um bot em cada idioma. Usada pelo passo `explainThisBot`.

### Campos:
- **botName** (chave primária): Nome do bot.
- **language** (chave primária): Idioma.
- **message**: Texto descritivo do bot.

### Métodos:
- **getFirstRecordsToInsert()** → List&lt;CcpBulkItem&gt;: Retorna as explicações em português dos bots `support` e `user`.

---

## Módulo: jn_rest-api_spring_jobsnow_dependency-chooser

---

## Classe: JnRestApiSpringStarter
**Pacote:** com.jn.rest.api
**Tipo:** classe (Spring Boot Application)
**Propósito:** Ponto de entrada da API REST do módulo JN (jobsnow principal). Inicializa o DI com as implementações corretas (produção via GCP/Elasticsearch ou locais conforme `localEnvironment`), configura o handler de exceções globais e registra os filtros de servlet para validação de e-mail, injeção de sessão e validação de sessão.

### Métodos:
- **main(String[] args)** → void: Carrega as dependências do framework, verifica o ambiente (local vs. produção), configura o handler global de exceções e inicia a aplicação Spring Boot.
- **emailFilter()** → FilterRegistrationBean (`@Bean`): Registra o `CcpValidEmailFilter` para validar o e-mail nos paths `/login/*`.
- **putSessionValuesFilter()** → FilterRegistrationBean (`@Bean`): Registra o `CcpPutSessionValuesAndExecuteTaskFilter` (sem tarefa adicional) para os paths `/contact-us/*` e `/login/*`.
- **validateSessionFilter()** → FilterRegistrationBean (`@Bean`): Registra o `CcpPutSessionValuesAndExecuteTaskFilter` com `JnBusinessSessionValidate` para validar sessão ativa nos paths `/contact-us/*`.

---

## Classe: JnRestApiAsyncTask
**Pacote:** com.jn.rest.api.endpoints
**Tipo:** classe (RestController, implementa `JnOpenApiAsyncTask`)
**Propósito:** Controller REST para consulta de tarefas assíncronas pelo id.

### Métodos:
- **getAsyncTaskStatusById(String asyncTaskId)** → Map (`GET /{asyncTaskId}`): Monta o JSON com o `asyncTaskId` e delega ao serviço `JnServiceAsyncTask.GetAsyncTaskStatusById`.

---

## Classe: JnRestApiContactUs
**Pacote:** com.jn.rest.api.endpoints
**Tipo:** classe (arquivo presente mas completamente comentado)
**Propósito:** Controller destinado ao endpoint de "fale conosco". Atualmente toda a implementação está comentada, não existindo endpoints ativos nesta classe.

---

## Classe: JnRestApiLogin
**Pacote:** com.jn.rest.api.endpoints
**Tipo:** classe (RestController, path `/login/{email}`)
**Propósito:** Controller REST para todas as operações de login: verificação de existência do usuário, criação de token de login por e-mail, execução de login com senha, logout, pré-registro, cadastro de senha, e solicitações de resend/unlock de token.

### Métodos:
- **validateLogin(String sessionToken, String body)** → void (`GET /{sessionToken}`): Valida se o sessionToken é válido para o e-mail.
- **executeLogin(Map body)** → Map (`POST /`): Executa o login com e-mail e senha, retornando o sessionToken em caso de sucesso.
- **createLoginEmail(Map body)** → Map (`POST /token`): Cria o e-mail de login (envia token por e-mail para o usuário cadastrar senha).
- **unlockLoginToken(Map body)** → Map (`POST /token/request/resending`): Solicita reenvio de token de login (para o usuário que não recebeu o e-mail).
- **resendLoginToken(Map body)** → Map (`POST /token/request/unlocking`): Solicita desbloqueio de token de login (para o usuário com token bloqueado por excesso de tentativas).
- **existsLoginEmail(String body)** → void (`HEAD /token`): Verifica se o usuário existe no sistema e se há pendências cadastrais.
- **executeLogout(String body, String sessionToken)** → void (`DELETE /{sessionToken}`): Encerra a sessão ativa do usuário.
- **saveAnswers(Map body)** → void (`POST /pre-registration`): Salva os dados de pré-registro do usuário.
- **savePassword(Map body)** → Map (`POST /password`): Cadastra ou altera a senha do usuário usando o token recebido por e-mail.
- **createLoginToken(Map body)** → Map (`POST /token/language/{language}`): Cria o token de login para o idioma especificado.
- **apenasDeErro()** → void (`GET /erro`): Endpoint de teste que lança `RuntimeException` deliberadamente.

---

## Interface: JnOpenApiAsyncTask
**Pacote:** com.jn.rest.api.swagger
**Tipo:** interface (contrato Swagger/OpenAPI)
**Propósito:** Define o contrato OpenAPI para o endpoint de tarefas assíncronas, documentando os possíveis retornos (200 encontrada, 404 não encontrada).

### Métodos:
- **getAsyncTaskStatusById(String asyncTaskId)** → Map: Retorna o status de uma tarefa assíncrona pelo id.

---

## Interface: JnOpenApiContactUs
**Pacote:** com.jn.rest.api.swagger
**Tipo:** interface
**Propósito:** Contrato OpenAPI para o endpoint "fale conosco". Atualmente vazio, sem métodos definidos.

---

## Interface: JnOpenApiLogin
**Pacote:** com.jn.rest.api.swagger
**Tipo:** interface (contrato Swagger/OpenAPI)
**Propósito:** Define o contrato OpenAPI completo para todos os endpoints de login, incluindo descrições detalhadas de todos os possíveis status HTTP de resposta (200, 201, 202, 400, 403, 404, 409, 423, 427, 429) e o comportamento esperado do frontend para cada caso.

### Métodos:
- **createLoginEmail(Map body)** → Map: Cria e-mail de login (POST /token).
- **createLoginToken(Map body)** → Map: Cria token de login com idioma (POST /token/language/{language}).
- **executeLogin(Map body)** → Map: Executa login (POST /).
- **executeLogout(String body, String sessionToken)** → void: Executa logout (DELETE /{sessionToken}).
- **existsLoginEmail(String body)** → void: Verifica existência do e-mail (HEAD /token).
- **saveAnswers(Map body)** → void: Salva pré-registro (POST /pre-registration).
- **savePassword(Map body)** → Map: Salva senha (POST /password).

---

## Módulo: vis_rest-api_spring_jobsnow_dependency-chooser

---

## Classe: VisRestApiSpringStarter
**Pacote:** com.vis.rest.api
**Tipo:** classe (Spring Boot Application)
**Propósito:** Ponto de entrada da API REST do módulo VIS (Visualização). Inicializa as dependências do framework (incluindo `CcpApacheTikaTextExtractor` que é exclusivo deste módulo), configura filtros de validação de e-mail e sessão para os paths `/resume/*` e `/position/*`.

### Métodos:
- **main(String[] args)** → void: Carrega as dependências (produção ou locais), configura handler global de exceções e inicia o Spring Boot.
- **emailFilter()** → FilterRegistrationBean (`@Bean`): Valida o e-mail extraído do path nos endpoints `/resume/*` e `/position/*`.
- **validateSessionFilter()** → FilterRegistrationBean (`@Bean`): Valida a sessão ativa nos endpoints `/resume/*` e `/position/*`.

---

## Classe: VisRestApiCompany
**Pacote:** com.vis.rest.api.endpoints
**Tipo:** classe (RestController, path `/companies`)
**Propósito:** Controller REST para operações relacionadas a empresas no módulo VIS.

### Métodos:
- **searchCompaniesByTheirFirstThreeInitials(String search)** → Map (`GET /search/autocomplete/{search}`): Busca empresas cujo nome começa com os caracteres informados (convertidos para maiúsculas), delegando ao serviço `VisServiceCompany.SearchCompaniesByTheirFirstThreeInitials`.

---

## Classe: VisRestApiPosition
**Pacote:** com.vis.rest.api.endpoints
**Tipo:** classe (RestController, path `/recruiters/{email}/positions/{title}`)
**Propósito:** Controller REST para gerenciamento de vagas (positions) de recrutadores no módulo VIS.

### Métodos:
- **save(String sessionValues)** → Map (`POST` e `PATCH /`): Cria ou atualiza uma vaga com os dados da sessão, delegando a `VisServicePosition.Save`.
- **changeStatus(String sessionValues)** → Map (`DELETE /status`): Altera o status de uma vaga (ativa/inativa), delegando a `VisServicePosition.ChangeStatus`.
- **getData(String sessionValues)** → Map (`GET /`): Retorna os dados de uma vaga, delegando a `VisServicePosition.GetData`.
- **getResumeList(String sessionValues, String fromIndex, String listSize, String title)** → Map (`GET /resumes/fromIndex/{fromIndex}/listSize/{listSize}`): Retorna a lista paginada de currículos para uma vaga.
- **getImportantSkillsFromText(String sessionValues, String title)** → Map (`POST /words`): Extrai as habilidades mais relevantes do texto da descrição da vaga.
- **suggestNewSkills(String sessionValues, String title)** → Map (`PATCH /words`): Sugere novas habilidades com base no conteúdo da vaga.

---

## Classe: VisRestApiRecruiter
**Pacote:** com.vis.rest.api.endpoints
**Tipo:** classe (RestController, path `/recruiter/{email}`)
**Propósito:** Controller REST para operações do recrutador no módulo VIS: envio de currículos, consulta de currículos vistos, listagem de vagas e registro de opiniões sobre currículos.

### Métodos:
- **sendResumesToEmail(String sessionValues, List emails, List resumeIds)** → Map (`POST /resumes/sending/email`): Envia currículos selecionados por e-mail para os destinatários informados.
- **getAlreadySeenResumes(String sessionValues, String opinionType)** → Map (`GET /resumes/seen/{opinionType}`): Retorna os currículos que o recrutador já visualizou, filtrados pelo tipo de opinião.
- **getPositionsFromThisRecruiter(String sessionValues, String positionStatus)** → Map (`GET /positions/{positionStatus}`): Lista as vagas do recrutador filtradas pelo status informado.
- **changeOpinionAboutThisResume(String sessionValues, String resumeId)** → Map (`POST /resumes/{resumeId}`): Alterna a opinião do recrutador sobre um currículo.
- **saveOpinionAboutThisResume(String sessionValues, String resumeId)** → Map (`POST /resumes/{resumeId}/opinion`): Salva a opinião do recrutador sobre um currículo específico.

---

## Classe: VisRestApiResume
**Pacote:** com.vis.rest.api.endpoints
**Tipo:** classe (RestController, path `/resume/{email}`)
**Propósito:** Controller REST para operações de currículo no módulo VIS: salvar, excluir, alterar status e consultar dados do currículo do candidato.

### Métodos:
- **save(Map sessionValues)** → Map (`POST /language/{language}`): Salva ou atualiza o currículo do usuário para o idioma especificado.
- **delete(Map sessionValues)** → Map (`DELETE /language/{language}`): Exclui o currículo do usuário no idioma especificado.
- **changeStatus(Map sessionValues)** → Map (`DELETE /language/{language}/status`): Altera o status (ativo/inativo) do currículo no idioma especificado.
- **getData(Map sessionValues)** → Map (`GET /`): Retorna os dados completos do currículo do usuário.
- **oi()** → String (`GET /oi`): Endpoint de verificação de saúde (health check simples), retorna a string `"oi"`.

---

## Classe: VisRestApiSkill
**Pacote:** com.vis.rest.api.endpoints
**Tipo:** classe (RestController, path `/skills`)
**Propósito:** Controller REST para operações relacionadas a habilidades (skills) no módulo VIS.

### Métodos:
- **getSkillsFromText(Map sessionValues)** → Map (`POST /fromText`): Extrai e retorna habilidades identificadas em um texto livre.
- **saveHierarchyFixSuggestion(Map sessionValues)** → Map (`POST /hierarchy/readjustment`): Salva uma sugestão de correção da hierarquia de habilidades.
- **requestToCreateNewSkill(String skill, Map sessionValues)** → Map (`POST /{skill}`): Registra uma solicitação de criação de nova habilidade.

---

## Interface: VisOpenApiCompany
**Pacote:** com.vis.rest.api.swagger
**Tipo:** interface (contrato de API)
**Propósito:** Define o contrato da API para operações de empresa no módulo VIS.

### Métodos:
- **searchCompaniesByTheirFirstThreeInitials(String search)** → Map: Busca empresas pelo prefixo do nome.

---

## Interface: VisOpenApiPosition
**Pacote:** com.vis.rest.api.swagger
**Tipo:** interface (contrato de API)
**Propósito:** Define o contrato da API para operações de vagas no módulo VIS.

### Métodos:
- **changeStatus(String sessionValues)** → Map: Altera o status de uma vaga.
- **getData(String sessionValues)** → Map: Retorna dados de uma vaga.
- **getImportantSkillsFromText(String sessionValues, String title)** → Map: Extrai habilidades relevantes do texto da vaga.
- **getResumeList(String sessionValues, String fromIndex, String listSize, String title)** → Map: Lista currículos paginados de uma vaga.
- **save(String sessionValues)** → Map: Cria ou atualiza uma vaga.
- **suggestNewSkills(String sessionValues, String title)** → Map: Sugere novas habilidades para a vaga.

---

## Interface: VisOpenApiRecruiter
**Pacote:** com.vis.rest.api.swagger
**Tipo:** interface (contrato de API)
**Propósito:** Define o contrato da API para operações do recrutador no módulo VIS.

### Métodos:
- **sendResumesToEmail(String sessionValues, List emails, List resumeIds)** → Map: Envia currículos por e-mail.
- **getAlreadySeenResumes(String sessionValues, String opinionType)** → Map: Lista currículos já vistos pelo recrutador.
- **getPositionsFromThisRecruiter(String sessionValues, String positionStatus)** → Map: Lista vagas do recrutador por status.
- **changeOpinionAboutThisResume(String sessionValues, String resumeId)** → Map: Alterna a opinião sobre um currículo.
- **saveOpinionAboutThisResume(String sessionValues, String resumeId)** → Map: Salva a opinião sobre um currículo.

---

## Interface: VisOpenApiResume
**Pacote:** com.vis.rest.api.swagger
**Tipo:** interface (contrato de API)
**Propósito:** Define o contrato da API para operações de currículo no módulo VIS.

### Métodos:
- **save(Map sessionValues)** → Map: Salva o currículo.
- **delete(Map sessionValues)** → Map: Exclui o currículo.
- **changeStatus(Map sessionValues)** → Map: Altera o status do currículo.
- **getData(Map sessionValues)** → Map: Retorna os dados do currículo.


---

