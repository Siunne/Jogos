// Confere respostas reais já executadas pelo Newman. Não faz chamadas nem altera bancos.
// Requer exportação prévia pelo aula10-exportar-contrato.cjs.
// Usa somente o subconjunto de JSON Schema descrito no validador da Aula 09.
const fs = require('node:fs');
const assert = require('node:assert/strict');
const {validate} = require('./aula09-schema-validator.cjs');
const doc = JSON.parse(fs.readFileSync('target/aula10-openapi.json', 'utf8'));
const run = JSON.parse(fs.readFileSync(process.argv[2] || 'target/aula10-correcao-newman.json', 'utf8')).run;
assert.equal(run.failures.length, 0);
const used = new Set();
for (const execution of run.executions) {
  const method = execution.request.method.toLowerCase();
  const path = '/' + execution.request.url.path.join('/');
  const route = Object.keys(doc.paths).find(k => new RegExp('^' + k.replace(/\{[^}]+\}/g, '[^/]+') + '$').test(path));
  assert(route, 'Rota ausente: ' + path);
  const response = doc.paths[route][method]?.responses[execution.response.code];
  assert(response, method + ' ' + route + ': resposta não documentada');
  const body = Buffer.from(execution.response.stream.data).toString();
  if (execution.response.code === 204) assert.equal(body, '');
  else {
    const type = execution.response.header.find(h => h.key.toLowerCase() === 'content-type').value.split(';')[0];
    const schema = response.content[type]?.schema;
    assert(schema, 'Schema ausente: ' + type);
    validate(type === 'application/json' ? JSON.parse(body) : body, schema, 'resposta', false, doc);
  }
  used.add(method + ' ' + route);
}
assert.equal(used.size, 38);
console.log(JSON.stringify({respostas: run.executions.length, operacoes: used.size,
  verificacoesNewman: run.stats.assertions.total, falhas: run.failures.length,
  alcance: 'subconjunto de JSON Schema; não certificação integral OpenAPI'}));
