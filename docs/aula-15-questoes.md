# Aula 15 — Questões de revisão

Enunciados reformulados na ordem da [seção 27 do material oficial](https://github.com/jeffersonarpasserini/suporteos2026/blob/main/docs/15aula/15aula.md#27-questões-de-revisão).

1. **Por que executar todo o código pode deixar um defeito passar?** Uma suíte pode percorrer todos os ramos com entradas pouco representativas, ou verificar apenas que houve retorno. Uma venda pode produzir total incorreto apesar de executar todo o cálculo.
2. **O que muda entre simular um repositório e usar PostgreSQL?** O stub devolve uma resposta escolhida para testar a decisão do serviço. PostgreSQL executa consultas, aplica constraints, mantém locks e fornece evidência transacional real.
3. **Quando carregar a aplicação com `@SpringBootTest`?** Quando o comportamento depende da composição real, por exemplo filtros, configuração, proxies ou persistência. Uma conta em memória pode ser testada por instanciação direta.
4. **Qual fronteira o MockMvc deixa de fora?** Ele usa requisições e respostas Servlet simuladas. O fluxo não passa por servidor em uma porta TCP, por configuração do ambiente implantado ou por rede.
5. **Como determinar se uma execução está correta?** Um oráculo estabelece o esperado por regra, especificação ou cálculo independente. A matriz RBAC exige 403 para o operador em operação administrativa.
6. **Como diferem stub e mock?** Stub fornece uma resposta preparada. Mock permite verificar uma chamada esperada, quando a interação em si é parte do comportamento.
7. **O que o RED comprova?** O teste compila, alcança a regra e detecta a ausência específica antes da implementação. Uma falha de infraestrutura não fornece essa evidência.
8. **Que lacuna branches mostram além das linhas?** Uma linha com `&&` pode estar verde sem que o segundo operando seja falso. Branches mostram resultados de decisão exercitados, sem provar MC/DC.
9. **Por que manter o mínimo abaixo da fotografia inicial?** O limite estabelece um piso reproduzível para a suíte existente e absorve pequenas mudanças legítimas no denominador. A análise por classe continua necessária em decisões críticas.
10. **Uma transação de teste prova o commit?** Rollback automático protege dados e permite observar escrita antes do fim. Constraints diferidas, visibilidade externa e efeitos pós-commit exigem commit ou fronteira independente.
11. **Como distinguir 401 de 403?** Execute a mesma operação protegida sem identidade válida e, depois, com JWT válido de OPERADOR sem o papel exigido. Confira ainda o sucesso com o perfil autorizado.
12. **Por que limitar esperas em testes concorrentes?** Um erro de coordenação ou lock pode travar o teste. O timeout limita a espera e permite diagnóstico, junto de limpeza do executor.
13. **Quando verificar chamadas com Mockito?** Quando a chamada é uma saída contratual, como notificar um gateway uma única vez após um evento. Contar leituras internas tende a acoplar a suíte à implementação.
14. **Como avaliar uma linha vermelha?** Pergunte qual decisão existe, impacto e probabilidade da falha, evidência já disponível, custo e se o teste terá um oráculo forte.
15. **O piso global suficiente protege uma classe de autorização ausente?** A média deixa lacunas locais passarem. Verifique o caminho de autorização da classe, com casos permitidos, negados e evidência em serviço e HTTP conforme o risco.
