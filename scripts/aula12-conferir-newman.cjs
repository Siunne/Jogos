// Verifica respostas reais sanitizadas. Subconjunto explícito de JSON Schema, não certificação OpenAPI.
const fs = require('node:fs'), assert = require('node:assert/strict');
const {validate} = require('./aula09-schema-validator.cjs');
const doc = JSON.parse(fs.readFileSync('target/aula12-openapi.json'));
const run = JSON.parse(fs.readFileSync('target/aula12-newman.json')).run;
assert.equal(run.failures.length, 0);
const used = new Set();
for (const e of run.executions) {
 const path = '/' + e.request.url.path.join('/'), method = e.request.method.toLowerCase();
 const route = Object.keys(doc.paths).find(p => new RegExp('^' + p.replace(/\{[^}]+}/g, '[^/]+') + '$').test(path));
 assert(route);const response = doc.paths[route][method].responses[e.response.code];assert(response, method + ' ' + route + ':' + e.response.code);
 const text = Buffer.from(e.response.stream.data).toString();
 if (e.response.code === 204) assert.equal(text, '');
 else {
   const type = e.response.header.find(h => h.key.toLowerCase() === 'content-type').value.split(';')[0];
   validate(type === 'application/json' ? JSON.parse(text) : text, response.content[type].schema, 'resposta', false, doc);
 }
 used.add(method + ' ' + route);
}
assert.equal(used.size, 41);
console.log(JSON.stringify({respostas: run.executions.length, operacoes: used.size, falhas: 0, alcance: 'subconjunto de JSON Schema'}));
