const assert = require('node:assert/strict');
const fs = require('node:fs');
const {test} = require('node:test');
const doc = JSON.parse(fs.readFileSync('target/aula14-openapi.json'));
const collection = require('../postman/Jogos-Aula14.postman_collection.json');
const run = JSON.parse(fs.readFileSync('target/aula14-newman.json')).run;

test('contrato expõe administração com Bearer e 403', () => {
  const ops = [
    doc.paths['/api/contas-acesso'].post,
    doc.paths['/api/contas-acesso/{id}/perfis'].put,
    doc.paths['/api/contas-acesso/{id}/status'].put,
    doc.paths['/api/pessoas/{id}/status'].put
  ];
  for (const op of ops) {
    assert.match(op.description, /ADMINISTRADOR/);
    assert.deepEqual(op.security, [{bearerAuth:[]}]);
    assert(op.responses[401] && op.responses[403]);
  }
  const perfis = doc.components.schemas.PerfisContaRequest.properties.perfis;
  assert.equal(perfis.type, 'array');
  assert.equal(perfis.minItems, 1);
  assert.deepEqual(perfis.items.enum, ['OPERADOR','ADMINISTRADOR']);
});
test('coleção e respostas reais comprovam a separação de privilégios', () => {
  assert.equal(run.failures.length, 0);
  assert(collection.variable.filter(v => /token|password/i.test(v.key)).every(v => !v.value));
  const codes = Object.fromEntries(run.executions.map(e => [e.item.name.split(' ')[0], e.response.code]));
  for (const [id, code] of Object.entries({'14-02':200,'14-03':403,'14-04':403,
    '14-05':200,'14-06':403,'14-08':409,'14-10':200,'14-11':401,'14-12':200,'14-14':401}))
    assert.equal(codes[id], code, id);
});
test('relatório não conserva credenciais nem bytes de JWT', () => {
  const jwt = /eyJ[A-Za-z0-9_-]*\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+/;
  assert.doesNotMatch(JSON.stringify(run), jwt);
  for (const {request,response} of run.executions) {
    for (const h of (request.header || []))
      if (h.key.toLowerCase() === 'authorization') assert.equal(h.value, '[OMITIDO]');
    assert.doesNotMatch(Buffer.from(response.stream.data).toString(), jwt);
  }
});
