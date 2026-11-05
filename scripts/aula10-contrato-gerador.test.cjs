const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const collection = JSON.parse(fs.readFileSync('postman/Jogos-Aula10.postman_collection.json', 'utf8'));
const script = collection.event.find(e => e.listen === 'prerequest').script.exec.join('\n');

function execute(now) {
  const values = new Map();
  const context = vm.createContext({Date: {now: () => now}, pm: {
    info: {requestName: '00 - Health'},
    collectionVariables: {get: key => values.get(key), set: (key, value) => values.set(key, value)}
  }});
  vm.runInContext(script, context);
  return {values, context};
}

// Implementação independente: calcula cada DV com pesos explícitos, sem reutilizar o gerador.
function valid(document, cnpj) {
  if (!new RegExp(`^\\d{${cnpj ? 14 : 11}}$`).test(document) || /^(\d)\1+$/.test(document)) return false;
  const weights = cnpj ? [[5,4,3,2,9,8,7,6,5,4,3,2], [6,5,4,3,2,9,8,7,6,5,4,3,2]]
    : [[10,9,8,7,6,5,4,3,2], [11,10,9,8,7,6,5,4,3,2]];
  return weights.every((w, index) => {
    const remainder = w.reduce((sum, weight, i) => sum + Number(document[i]) * weight, 0) % 11;
    return Number(document[(cnpj ? 12 : 9) + index]) === (remainder < 2 ? 0 : 11 - remainder);
  });
}

test('horário da auditoria gera dois CPFs válidos distintos e CNPJs válidos', () => {
  const {values} = execute(1793000000000);
  assert.notEqual(values.get('a10cpf'), '00000000000');
  assert.notEqual(values.get('a10cpf'), values.get('a10cpf2'));
  for (const name of ['a10cpf', 'a10cpf2']) assert(valid(values.get(name), false), name);
  for (const name of ['a10cnpj', 'cnpjTeste']) assert(valid(values.get(name), true), name);
});

test('todas as bases repetidas são descartadas antes dos verificadores', () => {
  const {context} = execute(1793000000000);
  for (let digit = 0; digit <= 9; digit++) {
    for (const cnpj of [false, true]) {
      const result = vm.runInContext(`fiscal('${String(digit).repeat(cnpj ? 12 : 9)}', ${cnpj})`, context);
      assert(valid(result, cnpj));
    }
  }
  assert.throws(() => vm.runInContext("fiscal('abc',false)", context));
});

test('amostra determinística de horários preserva documentos válidos e CPFs distintos', () => {
  for (const time of [0, 1111111111111, 1999999999999, ...Array.from({length: 200}, (_, i) => 1793000000000 + i * 123457)]) {
    const {values} = execute(time);
    // Inclui epoch zero; o gerador completa a base antes de calcular os verificadores.
    for (const name of ['a10cpf', 'a10cpf2']) assert(valid(values.get(name), false));
    assert.notEqual(values.get('a10cpf'), values.get('a10cpf2'));
    for (const name of ['a10cnpj', 'cnpjTeste']) assert(valid(values.get(name), true));
  }
});

test('padrão compartilhado interpreta espaços Unicode no JavaScript conforme a validação Java', () => {
  const source = fs.readFileSync('src/main/java/br/com/emilly/jogos/config/SchemaPatterns.java', 'utf8');
  const pattern = new RegExp(JSON.parse(source.match(/=\s*(".*");/)[1]));
  for (const text of ['', ' ', '\t\n', '\u001c', '\u1680', '\u2003', '\u2028', '\u3000']) assert(!pattern.test(text));
  for (const text of ['\u00a0', '\u2007', '\u202f', ' Nome ']) assert(pattern.test(text));
});

test('contrato de texto normalizado rejeita controles que desaparecem após trim e mantém NBSP', () => {
  const source = fs.readFileSync('src/main/java/br/com/emilly/jogos/config/SchemaPatterns.java', 'utf8');
  const pattern = new RegExp(JSON.parse(source.match(/NORMALIZADO_NAO_BRANCO\s*=\s*(".*");/)[1]));
  for (let i = 0; i <= 32; i++) assert(!pattern.test(String.fromCharCode(i)));
  for (const text of ['', '\t\n', '\u2003', '\u0000\u2003\u001b']) assert(!pattern.test(text));
  for (const text of ['\u00a0', '\u2007', '\u202f', ' Nome ', '\u0000Nome\u001b']) assert(pattern.test(text));
});
