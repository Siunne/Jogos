# Tema do Projeto

## Identificação

- Nome do projeto: jogos
- Tema: gerenciamento de jogos
- Objetivo: cadastrar e organizar jogos por gênero.

## Entidade de classificação

- Nome: GeneroJogo
- Exemplos: RPG, Ação, Terror, Aventura

## Entidade principal

- Nome: Jogo
- Código único: código do jogo
- Descrição: nome do jogo
- Medida quantitativa: unidades disponíveis
- Valor monetário: preço
- Data relevante: data de cadastro
- Status: ativo ou inativo

## Relacionamento

Um gênero pode possuir vários jogos.

Cada jogo pertence a um gênero.

Relacionamento:

GeneroJogo 1:N Jogo

## Exemplos

1. Minecraft | JG001 | Aventura | 10 unidades | R$ 99,90
2. The Witcher 3 | JG002 | RPG | 5 unidades | R$ 149,90
3. Resident Evil 4 | JG003 | Terror | 8 unidades | R$ 129,90