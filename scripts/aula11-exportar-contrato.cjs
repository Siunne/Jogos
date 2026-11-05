// Somente GET local; preserva os contratos históricos das aulas 09 e 10.
const fs = require('node:fs');
const assert = require('node:assert/strict');
const base = process.env.AULA11_BASE_URL || 'http://localhost:18080';
assert(['localhost', '127.0.0.1'].includes(new URL(base).hostname));
(async () => {
  const jr = await fetch(base + '/v3/api-docs'); assert.equal(jr.status, 200);
  const raw = await jr.text(), doc = JSON.parse(raw);
  assert(!doc.paths['/api/contas-acesso'], 'Use aula12-exportar-contrato.cjs; contrato Aula11 é histórico.');
  assert.equal(Object.keys(doc.paths).length, 24);
  assert.equal(Object.keys(doc.components.schemas).length, 30);
  assert.equal(doc.tags.length, 8);
  assert.equal(Object.values(doc.paths).flatMap(Object.values).filter(o => o.operationId).length, 38);
  assert.match(doc.paths['/api/vendas/{id}/finalizacao'].post.description, /PESSIMISTIC_WRITE/);
  for (const path of ['/api/jogos/{id}/estoque/entradas', '/api/jogos/{id}/estoque/saidas', '/api/vendas/{id}/finalizacao']) {
    assert(doc.paths[path].post.responses['409']);
  }
  const yr = await fetch(base + '/v3/api-docs.yaml'); assert.equal(yr.status, 200);
  const yaml = await yr.text();
  fs.mkdirSync('target', {recursive: true});
  fs.writeFileSync('target/aula11-openapi.json', raw);
  fs.writeFileSync('target/aula11-openapi.yaml', yaml);
  // Alias temporário para os testes Node históricos, nunca altera a entrega histórica.
  fs.writeFileSync('target/aula10-openapi.json', raw);
  fs.writeFileSync('docs/aula-11-openapi.yaml', yaml);
  console.log('Contrato Aula 11 real exportado: 38 operações, 24 caminhos, 30 schemas, 8 tags.');
})().catch(e => { console.error(e); process.exitCode = 1; });
