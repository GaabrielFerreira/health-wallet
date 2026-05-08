# Identificação de Code Smells — Health Wallet Backend

**Referência:** SCRUM-58  
**Responsável pela análise:** Gabriel Ferreira  
**Data:** 08/05/2026  
**Escopo:** `backend/src/main/java/com/healthwallet/`

---

## O que são Code Smells?

**Code smells** (ou "maus cheiros de código") são características no código-fonte que indicam a presença de um problema mais profundo de design ou implementação. O termo foi popularizado por Martin Fowler no livro *Refactoring: Improving the Design of Existing Code* (1999).

Um code smell **não é necessariamente um bug** — o código pode funcionar corretamente e ainda assim apresentar um smell. A questão é que ele dificulta a manutenção, aumenta o risco de introdução de erros futuros e torna o sistema mais difícil de entender e evoluir.

### Por que identificar e corrigir?

| Sem correção | Com correção |
|---|---|
| Alterar uma funcionalidade exige modificar múltiplos lugares | Mudança localizada em um único ponto |
| Testes são difíceis de escrever pois o código é acoplado | Código testável de forma isolada |
| Novos desenvolvedores demoram para entender o código | Leitura fluida e intenção clara |
| Bugs se escondem atrás de código duplicado ou genérico | Comportamento previsível e tratado explicitamente |

### Categorias identificadas neste documento

- **Duplicated Code** — mesmo trecho de lógica em múltiplos lugares
- **Error Hiding** — exceções genéricas que ocultam o problema real
- **Performance Smell** — operações desnecessariamente repetidas
- **Outdated API** — uso de APIs antigas quando existe alternativa moderna
- **SOLID Violation** — violação de princípios de design orientado a objetos
- **Missing Validation** — ausência de validação em pontos críticos
- **JPA Anti-pattern** — uso de anotações incompatíveis com o ciclo de vida do JPA

---

## Índice

1. [CS-01 — Código Duplicado: construção de `AnamnesisResponse`](#cs-01)
2. [CS-02 — Código Duplicado: setters de campos da anamnese](#cs-02)
3. [CS-03 — Código Duplicado: validação de existência do paciente](#cs-03)
4. [CS-04 — `orElseThrow()` sem exceção de domínio](#cs-04)
5. [CS-05 — Consultas desnecessárias ao banco no login](#cs-05)
6. [CS-06 — `Collectors.toList()` obsoleto (Java 8)](#cs-06)
7. [CS-07 — Serviços sem interface (violação do DIP)](#cs-07)
8. [CS-08 — `@Valid` ausente no endpoint de atualização](#cs-08)
9. [CS-09 — `@Data` em entidades JPA](#cs-09)

---

## CS-01 — Código Duplicado: construção de `AnamnesisResponse` {#cs-01}

**Categoria:** Duplicated Code  
**Severidade:** Alta  
**Arquivo:** `service/AnamnesisService.java`  
**Linhas:** 46–55, 65–74, 82–91, 111–120

### Descrição

O bloco de construção do objeto `AnamnesisResponse` com 8 argumentos aparece **4 vezes** no mesmo arquivo, em `create()`, `getByPatientId()`, `listByPatientId()` e `update()`. Qualquer alteração no contrato do DTO (ex: adicionar um novo campo) exige modificar 4 trechos diferentes, o que aumenta o risco de inconsistências.

```java
// Repetido 4 vezes:
return new AnamnesisResponse(
    saved.getId(),
    saved.getPatient().getId(),
    saved.getAllergies(),
    saved.getChronicDiseases(),
    saved.getMedications(),
    saved.getBloodType(),
    saved.getFamilyHistory(),
    saved.getObservations()
);
```

### Como corrigir

Extrair um método privado `toResponse(Anamnesis a)` na classe `AnamnesisService` e substituir todos os blocos duplicados por uma chamada a esse método:

```java
private AnamnesisResponse toResponse(Anamnesis a) {
    return new AnamnesisResponse(
        a.getId(),
        a.getPatient().getId(),
        a.getAllergies(),
        a.getChronicDiseases(),
        a.getMedications(),
        a.getBloodType(),
        a.getFamilyHistory(),
        a.getObservations()
    );
}
```

Nos métodos públicos, substituir os blocos por:

```java
return toResponse(anamnesisRepository.save(anamnesis)); // em create() e update()
return toResponse(anamnesis);                           // em getByPatientId()
.map(this::toResponse)                                  // em listByPatientId()
```

---

## CS-02 — Código Duplicado: setters de campos da anamnese {#cs-02}

**Categoria:** Duplicated Code  
**Severidade:** Média  
**Arquivo:** `service/AnamnesisService.java`  
**Linhas:** 37–42 (em `create`) e 102–107 (em `update`)

### Descrição

Os 6 setters de campos (`allergies`, `chronicDiseases`, `medications`, `bloodType`, `familyHistory`, `observations`) são chamados com a mesma lógica em dois métodos distintos (`create` e `update`). Quando um novo campo for adicionado à entidade, o desenvolvedor precisa lembrar de atualizar **dois** lugares.

```java
// Em create() — linhas 37-42:
anamnesis.setAllergies(request.getAllergies());
anamnesis.setChronicDiseases(request.getChronicDiseases());
anamnesis.setMedications(request.getMedications());
anamnesis.setBloodType(request.getBloodType());
anamnesis.setFamilyHistory(request.getFamilyHistory());
anamnesis.setObservations(request.getObservations());

// Em update() — linhas 102-107: exatamente o mesmo bloco
```

### Como corrigir

Extrair um método privado `applyFields(Anamnesis, String, String, String, String, String, String)` ou, alternativamente, receber diretamente um request que implemente uma interface comum:

```java
private void applyFields(Anamnesis anamnesis, String allergies, String chronicDiseases,
                         String medications, String bloodType,
                         String familyHistory, String observations) {
    anamnesis.setAllergies(allergies);
    anamnesis.setChronicDiseases(chronicDiseases);
    anamnesis.setMedications(medications);
    anamnesis.setBloodType(bloodType);
    anamnesis.setFamilyHistory(familyHistory);
    anamnesis.setObservations(observations);
}
```

Chamar em `create()` e `update()`:

```java
applyFields(anamnesis,
    request.getAllergies(), request.getChronicDiseases(),
    request.getMedications(), request.getBloodType(),
    request.getFamilyHistory(), request.getObservations());
```

---

## CS-03 — Código Duplicado: validação de existência do paciente {#cs-03}

**Categoria:** Duplicated Code  
**Severidade:** Média  
**Arquivo:** `service/AnamnesisService.java`  
**Linhas:** 59–60, 78–79, 96–97

### Descrição

A verificação de existência do paciente via `userRepository.findById(...).orElseThrow(...)` é repetida em `getByPatientId()`, `listByPatientId()` e `update()`. Três ocorrências idênticas do mesmo trecho de código.

```java
// Repetido 3 vezes:
userRepository.findById(patientId)
    .orElseThrow(() -> new PatientNotFoundException(patientId));
```

### Como corrigir

Extrair um método privado `validatePatientExists(UUID patientId)`:

```java
private void validatePatientExists(UUID patientId) {
    userRepository.findById(patientId)
        .orElseThrow(() -> new PatientNotFoundException(patientId));
}
```

Substituir as 3 ocorrências pela chamada `validatePatientExists(patientId)`.

---

## CS-04 — `orElseThrow()` sem exceção de domínio {#cs-04}

**Categoria:** Error Hiding / Inappropriate Intimacy  
**Severidade:** Alta  
**Arquivo:** `service/AuthService.java`  
**Linha:** 52

### Descrição

O método `login()` usa `orElseThrow()` sem argumento ao buscar o usuário pelo email após autenticação. Isso lança uma `java.util.NoSuchElementException` genérica, que:

1. Não é tratada pelo `GlobalExceptionHandler`, resultando em HTTP 500 ao invés de 404 ou 401.
2. Não transmite contexto do domínio (qual usuário não foi encontrado).
3. Loga uma stack trace confusa em produção.

```java
// Linha 52 — problemático:
User user = userRepository.findByEmail(request.getEmail())
    .orElseThrow(); // NoSuchElementException genérica
```

### Como corrigir

`PatientNotFoundException` aceita apenas `UUID` como argumento, por isso não pode ser reutilizada aqui semanticamente. A correção correta é criar uma `UserNotFoundException` na pasta `exception/`, seguindo o mesmo padrão das exceções existentes:

```java
// exception/UserNotFoundException.java
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("Usuário não encontrado: " + email);
    }
}
```

Registrar o handler no `GlobalExceptionHandler`:

```java
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("status", 404);
    body.put("erro", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
}
```

Substituir o `orElseThrow()` em `AuthService.login()`:

```java
User user = userRepository.findByEmail(request.getEmail())
    .orElseThrow(() -> new UserNotFoundException(request.getEmail()));
```

---

## CS-05 — Consultas desnecessárias ao banco no login {#cs-05}

**Categoria:** Inappropriate Intimacy / Performance Smell  
**Severidade:** Média  
**Arquivo:** `service/AuthService.java`  
**Linhas:** 47–55

### Descrição

O fluxo de `login()` realiza **3 consultas ao banco** para um único request:

1. `authenticationManager.authenticate(...)` — internamente chama `UserDetailsServiceImpl.loadUserByUsername()`, que executa `userRepository.findByEmail()`.
2. `userRepository.findByEmail(request.getEmail())` — segunda consulta, linha 51.
3. `userDetailsService.loadUserByUsername(request.getEmail())` — terceira consulta, linha 54.

```java
// 3 queries para o mesmo email:
authenticationManager.authenticate(...);           // query 1 (interna)
User user = userRepository.findByEmail(...);       // query 2
UserDetails userDetails = userDetailsService       // query 3
    .loadUserByUsername(request.getEmail());
String token = jwtService.generateToken(userDetails);
```

### Como corrigir

O `authenticationManager.authenticate()` já retorna um objeto `Authentication` cujo `principal` é o `UserDetails` carregado internamente pelo `UserDetailsServiceImpl`. Basta usar esse retorno ao invés de chamar `loadUserByUsername()` novamente:

```java
public LoginResponse login(LoginRequest request) {
    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
    );

    // UserDetails já carregado pelo authenticate() — sem nova query:
    UserDetails userDetails = (UserDetails) auth.getPrincipal();
    String token = jwtService.generateToken(userDetails);

    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

    return new LoginResponse(token, user.getName(), user.getEmail(), user.getRole());
}
```

Com isso, o fluxo passa de **3 queries** para **2 queries**: a interna do `authenticate()` (inevitável, é parte da autenticação) e o `findByEmail` para montar o `LoginResponse`. A chamada redundante a `loadUserByUsername()` é eliminada.

---

## CS-06 — `Collectors.toList()` obsoleto (Java 8) {#cs-06}

**Categoria:** Outdated API Usage  
**Severidade:** Baixa  
**Arquivo:** `service/AnamnesisService.java`  
**Linha:** 92

### Descrição

O método `listByPatientId()` usa `collect(Collectors.toList())`, que é o padrão do Java 8. O projeto usa **Java 17** (Spring Boot 3), que introduz `Stream.toList()` — mais conciso, imutável por padrão e sem import adicional.

```java
// Linha 92 — padrão antigo:
.collect(Collectors.toList());
```

### Como corrigir

Substituir por:

```java
.toList();
```

E remover o import de `java.util.stream.Collectors` se não for usado em outros lugares.

---

## CS-07 — Serviços sem interface (violação do DIP) {#cs-07}

**Categoria:** SOLID — Dependency Inversion Principle  
**Severidade:** Alta  
**Arquivos:**
- `service/AuthService.java`
- `service/AnamnesisService.java`
- `service/VaccineService.java`
- `controller/AuthController.java`
- `controller/UserController.java`
- `controller/AnamnesisController.java`
- `controller/VaccineController.java`

### Descrição

Os controllers injetam diretamente as classes concretas de serviço (`AnamnesisService`, `AuthService`, `VaccineService`), acoplando a camada de apresentação à implementação. Isso viola o **Princípio da Inversão de Dependência (DIP)**: módulos de alto nível (controllers) devem depender de abstrações (interfaces), não de detalhes (classes concretas).

Consequências práticas:
- Testes unitários que mockam a classe concreta com `@Mock` forçam o Mockito a usar CGLIB/ByteBuddy (geração de subclasse em runtime), acoplando o teste à implementação. Com uma interface, o Mockito usa proxy JDK nativo — mais simples e sem esse acoplamento.
- Trocar a implementação (ex: adicionar cache, logging, versão diferente) exige alterar o controller.

```java
// AnamnesisController.java — depende da classe concreta:
private final AnamnesisService anamnesisService; // deveria ser uma interface
```

### Como corrigir

1. Criar interfaces na pasta `service/`:

```java
// service/AnamnesisService.java (nova interface)
public interface AnamnesisService {
    AnamnesisResponse create(AnamnesisRequest request);
    AnamnesisResponse getByPatientId(UUID patientId);
    List<AnamnesisResponse> listByPatientId(UUID patientId);
    AnamnesisResponse update(UUID patientId, AnamnesisUpdateRequest request);
}
```

2. Renomear as implementações atuais para `*Impl`:

```
AnamnesisService.java  →  AnamnesisServiceImpl.java
AuthService.java       →  AuthServiceImpl.java
VaccineService.java    →  VaccineServiceImpl.java
```

3. Anotar as implementações com `@Service` e implementar as interfaces:

```java
@Service
@RequiredArgsConstructor
public class AnamnesisServiceImpl implements AnamnesisService {
    // implementação existente permanece igual
}
```

4. Os controllers passam a injetar a interface (nenhuma alteração na lógica):

```java
private final AnamnesisService anamnesisService; // agora é a interface
```

> **Atenção:** Este code smell se sobrepõe parcialmente com a task SCRUM-59 (SOLID). Coordenar com o responsável para evitar conflito de merge.

---

## CS-08 — `@Valid` ausente no endpoint de atualização {#cs-08}

**Categoria:** Missing Validation  
**Severidade:** Média  
**Arquivo:** `controller/AnamnesisController.java`  
**Linha:** 79

### Descrição

O endpoint `PUT /api/anamnesis/{patientId}` recebe `AnamnesisUpdateRequest` no body sem a anotação `@Valid`. Se o DTO possuir ou vier a possuir constraints do Bean Validation (`@NotBlank`, `@Size`, etc.), elas **não serão executadas** — os dados chegam ao service sem validação de entrada.

```java
// Linha 79 — sem @Valid:
public ResponseEntity<AnamnesisResponse> update(@PathVariable UUID patientId,
                                                @RequestBody AnamnesisUpdateRequest request) {
```

### Como corrigir

Adicionar `@Valid` antes de `@RequestBody`:

```java
public ResponseEntity<AnamnesisResponse> update(@PathVariable UUID patientId,
                                                @Valid @RequestBody AnamnesisUpdateRequest request) {
```

---

## CS-09 — `@Data` em entidades JPA {#cs-09}

**Categoria:** JPA Anti-pattern  
**Severidade:** Alta  
**Arquivos:** todos em `model/` que usam `@Data` (`User.java`, `Anamnesis.java`, `Vaccine.java`, `Appointment.java`, etc.)

### Descrição

A anotação `@Data` do Lombok em entidades JPA é um anti-pattern amplamente documentado. O `@Data` gera `equals()` e `hashCode()` baseados em **todos os campos** da classe. Para entidades JPA, isso causa dois problemas graves:

1. **Lazy loading quebrado:** o Hibernate usa proxies para relacionamentos `FETCH.LAZY`. Ao chamar `equals()` em um proxy, todos os campos lazy são carregados imediatamente, mesmo fora de uma transação — o que lança `LazyInitializationException`.

   ```java
   // Anamnesis.java — relacionamento lazy:
   @ManyToOne(fetch = FetchType.LAZY)
   private User patient; // se equals() for chamado, o Hibernate tenta buscar este objeto
   ```

2. **`hashCode` instável:** o `@Data` gera `hashCode()` baseado em **todos os campos declarados na entidade** (ex: `allergies`, `chronicDiseases`, `medications`, etc.) — todos mutáveis via setters. Se um desses campos mudar após a entidade ser inserida em um `HashSet` ou `HashMap`, o objeto "desaparece" da coleção porque o bucket calculado no `put()` não bate mais com o do `contains()`.

Adicionalmente, `@EqualsAndHashCode(callSuper = true)` nas entidades filhas chama `Object.hashCode()` da `BaseEntity` (que não tem `@EqualsAndHashCode` gerado), tornando dois objetos com o mesmo `id` e mesmo estado sempre diferentes na visão de `equals()` — o que quebra comparações esperadas entre entidades retornadas em queries distintas.

### Como corrigir

A correção deve ser feita em dois arquivos:

**1. `BaseEntity.java`** — adicionar `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` e marcar `id` com `@EqualsAndHashCode.Include`. O `BaseEntity` já usa `@Getter @Setter` corretamente; basta adicionar as duas anotações:

```java
// BaseEntity.java — adicionar:
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @EqualsAndHashCode.Include   // <-- adicionar esta anotação no campo id
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // createdAt, updatedAt, deletedAt permanecem sem @Include
}
```

**2. Entidades filhas (`User.java`, `Anamnesis.java`, `Vaccine.java`, etc.)** — remover `@Data` e `@EqualsAndHashCode(callSuper = true)`, substituindo por `@Getter @Setter`. As entidades herdarão automaticamente o `equals()`/`hashCode()` baseado em `id` definido em `BaseEntity`:

```java
// Antes (Anamnesis.java):
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
public class Anamnesis extends BaseEntity { ... }

// Depois:
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Anamnesis extends BaseEntity { ... }
```

Com isso, dois objetos `Anamnesis` com o mesmo `id` são iguais, sem depender dos campos mutáveis nem de proxies lazy.

---

## Resumo

| ID    | Code Smell                                       | Arquivo                  | Severidade |
|-------|--------------------------------------------------|--------------------------|------------|
| CS-01 | Código duplicado: construção de `AnamnesisResponse` | `AnamnesisService.java`  | Alta       |
| CS-02 | Código duplicado: setters de campos              | `AnamnesisService.java`  | Média      |
| CS-03 | Código duplicado: validação de paciente          | `AnamnesisService.java`  | Média      |
| CS-04 | `orElseThrow()` sem exceção de domínio           | `AuthService.java`       | Alta       |
| CS-05 | 3 queries ao banco por login                     | `AuthService.java`       | Média      |
| CS-06 | `Collectors.toList()` obsoleto                   | `AnamnesisService.java`  | Baixa      |
| CS-07 | Serviços sem interface (violação do DIP)         | Todos os services        | Alta       |
| CS-08 | `@Valid` ausente no endpoint de update           | `AnamnesisController.java` | Média    |
| CS-09 | `@Data` em entidades JPA                         | Todos os models          | Alta       |
