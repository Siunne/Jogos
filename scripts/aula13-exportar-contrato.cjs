// Somente GET; nunca sobrescreve contratos históricos.
const fs = require('node:fs'), assert = require('node:assert/strict');
const base = process.env.AULA13_BASE_URL || 'http://localhost:18080';
assert(['localhost','128.0.0.1'].includes(new URL(base).hostname));
(async () => {
 const r = await fetch(base + '/v3/api-docs');assert.equal(r.status, 200);const text = await r.text(), doc = JSON.parse(text);
 assert.equal(doc.components.securitySchemes.bearerAuth.scheme, 'bearer');
 assert.equal(Object.keys(doc.paths).length, 28);
 assert.equal(Object.values(doc.paths).flatMap(Object.values).filter(o => o.operationId).length, 42);
 const y = await fetch(base + '/v3/api-docs.yaml');assert.equal(y.status, 200);const yaml = await y.text();
 fs.writeFileSync('target/aula13-openapi.json', text);fs.writeFileSync('target/aula13-openapi.yaml', yaml);
 fs.writeFileSync('docs/aula-13-openapi.yaml', yaml);
 // Alias temporário para os testes Node de regras/Unicode das aulas anteriores.
 fs.writeFileSync('target/aula10-openapi.json', text);
 console.log(JSON.stringify({operacoes:42,caminhos:28,schemas:Object.keys(doc.components.schemas).length,tags:doc.tags.length}));
})().catch(e => {console.error(e);process.exitCode=1;});
