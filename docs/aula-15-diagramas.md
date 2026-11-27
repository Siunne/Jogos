# Aula 15 — Diagramas

## Experimento

```mermaid
flowchart LR
    A["Arrange: estado conhecido"] --> B["Act: comportamento observado"]
    B --> C{"Assert: corresponde ao oráculo?"}
    O["Regra ou especificação independente"] --> C
    C -->|sim| D["Aprovado"]
    C -->|não| E["Falha observável"]
```

## Pirâmide aplicada

```mermaid
flowchart BT
    U["Unidades: entidades reais em memória e dependências simuladas no service"]
    I["Integração: Spring, JPA, transações e PostgreSQL"]
    C["Componente: MVC simulado com filtros reais e contrato"]
    S["Fluxos externos: Newman via TCP"]
    U --> I --> C --> S
```

## MockMvc, autenticação e autorização

```mermaid
sequenceDiagram
    participant T as JUnit
    participant M as MockMvc
    participant F as Spring Security
    participant C as MVC e controller
    participant S as Proxy transacional e service
    participant P as JPA e PostgreSQL
    T->>M: Monta requisição Servlet simulada
    M->>F: Executa filtros reais
    alt JWT ausente ou inválido em rota protegida
        F-->>M: 401
    else JWT válido e autoridade insuficiente
        F-->>M: 403
    else Política HTTP permite
        F->>C: Encaminha requisição
        C->>S: Valida DTO e chama caso de uso
        S->>S: Aplica PreAuthorize quando configurado
        S->>P: Executa consulta ou alteração em transação
        P-->>S: Estado observado
        S-->>C: Resultado ou exceção
        C-->>M: Status e corpo
    end
    M-->>T: Resultado para assertivas
```

A variante com identidade simulada já fornece o contexto de segurança ao teste. A variante com Bearer real passa pelo decoder. A decisão declarativa do método pode negar a chamada após a passagem pela política HTTP.

## Ciclo TDD

```mermaid
flowchart LR
    R["RED: teste compila e falha na asserção da regra"] --> G["GREEN: implementação mínima e suíte aprovada"]
    G --> F["REFACTOR: melhora de design e nova execução"]
    F -->|próximo comportamento| R
```

## Cobertura e portão

```mermaid
flowchart LR
    P["prepare-agent"] --> J["Surefire inicia JVM com agente"]
    J --> E["jacoco.exec"]
    E --> R["report em verify"]
    B["Bytecode e fontes"] --> R
    R --> H["HTML, XML e CSV"]
    E --> C["check em verify"]
    B --> C
    C -->|piso atendido| S["Build aprovado"]
    C -->|piso violado| F["Build reprovado"]
```

Relatório e portão usam os dados de execução e as classes compiladas. O relatório HTML facilita a leitura, enquanto o portão calcula os contadores para decidir o resultado do build.
