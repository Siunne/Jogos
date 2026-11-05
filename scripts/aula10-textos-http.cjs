// Executar somente contra a instância local iniciada com profile test e banco jogos_test.
// Cria fixtures; não limpa nem altera registros legados. Não registra documentos pessoais.
const fs = require('node:fs');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const crypto = require('node:crypto');
const base = process.env.AULA10_BASE_URL || 'http://localhost:18080';
assert(['localhost', '127.0.0.1'].includes(new URL(base).hostname));
const values = new Map();
const collection = JSON.parse(fs.readFileSync('postman/Jogos-Aula10.postman_collection.json', 'utf8'));
vm.runInNewContext(collection.event.find(e => e.listen === 'prerequest').script.exec.join('\n'), {
  Date, pm: {info: {requestName: '00 - Health'}, collectionVariables: {
    get: k => values.get(k), set: (k, v) => values.set(k, v)}}
});
const invalid = [null, '', ...Array.from({length: 33}, (_, i) => String.fromCharCode(i)),
  '\t\n', '\u2003', '\u1680', '\u3000', '\u0000\u2003\u001b'];
const result = {entradasInvalidas: invalid.length, requisicoes: 0, status: {},
  camposAnterioresPreservados: true, nbspPreservado: true};
async function call(method, path, body, expected) {
  const r = await fetch(base + path, {method, headers: {'Content-Type': 'application/json'},
    ...(body === undefined ? {} : {body: JSON.stringify(body)})});
  result.requisicoes++;
  result.status[r.status] = (result.status[r.status] || 0) + 1;
  assert.equal(r.status, expected, method + ' ' + path);
  const data = await r.json();
  if (expected === 400) {
    assert.equal(data.status, 400); assert.equal(data.error, 'Bad Request');
    assert.equal(data.path, path); assert(data.timestamp && data.message);
    assert(data.fields && typeof data.fields === 'object');
  }
  return data;
}
(async () => {
  const email = crypto.randomUUID() + '@example.com';
  const cpf = values.get('a10cpf'), cpf2 = values.get('a10cpf2');
  const p = await call('POST', '/api/pessoas', {nome: ' Nome anterior ', email, cpf}, 201);
  assert.equal(p.nome, 'Nome anterior');
  const path = '/api/pessoas/' + p.id;
  for (const text of invalid) {
    await call('POST', '/api/pessoas', {nome: text, email: 'outro-' + email, cpf: cpf2}, 400);
    await call('PUT', path, {nome: text, email, cpf}, 400);
    const after = await call('GET', path, undefined, 200);
    assert.equal(after.nome, p.nome); assert.equal(after.email, email); assert.equal(after.cpf, cpf);
    await call('POST', '/api/colaboradores', {pessoaId: p.id, matricula: text,
      funcao: 'VENDEDOR', dataAdmissao: '2026-11-05'}, 400);
  }
  // 201 comprova que nenhuma tentativa inválida consumiu o papel único da pessoa.
  const matricula = 'V-' + crypto.randomUUID().slice(0, 20);
  const c = await call('POST', '/api/colaboradores', {pessoaId: p.id,
    matricula: '\u0000 ' + matricula + ' \u001b', funcao: 'VENDEDOR', dataAdmissao: '2026-11-05'}, 201);
  assert.equal(c.matricula, matricula);
  const colPath = '/api/colaboradores/' + c.id;
  for (const text of invalid) {
    await call('PUT', colPath, {matricula: text, funcao: 'GERENTE'}, 400);
    const after = await call('GET', colPath, undefined, 200);
    assert.equal(after.matricula, matricula); assert.equal(after.funcao, 'VENDEDOR');
  }
  await call('PUT', path, {nome: ' Nome alterado ', email, cpf}, 200);
  assert.equal((await call('GET', path, undefined, 200)).nome, 'Nome alterado');
  await call('PUT', colPath, {matricula: ' ' + matricula + ' ', funcao: 'GERENTE'}, 200);
  assert.equal((await call('GET', colPath, undefined, 200)).matricula, matricula);
  // O CPF reservado só é cadastrado agora: as tentativas anteriores não o persistiram.
  const nb = await call('POST', '/api/pessoas', {nome: '\u00a0', email: 'nbsp-' + email, cpf: cpf2}, 201);
  assert.equal((await call('GET', '/api/pessoas/' + nb.id, undefined, 200)).nome, '\u00a0');
  const nbMatricula = '\u00a0' + crypto.randomUUID().slice(0, 20);
  const nbCol = await call('POST', '/api/colaboradores', {pessoaId: nb.id, matricula: nbMatricula,
    funcao: 'VENDEDOR', dataAdmissao: '2026-11-05'}, 201);
  assert.equal((await call('GET', '/api/colaboradores/' + nbCol.id, undefined, 200)).matricula, nbMatricula);
  const largeEmail = '\u0130'.repeat(63) + '@' + ('a'.repeat(36) + '.').repeat(3) + 'com';
  await call('POST', '/api/pessoas', {nome: 'Nome', email: largeEmail, cpf}, 400);
  await call('PUT', path, {nome: 'Outro', email: largeEmail, cpf}, 400);
  const preserved = await call('GET', path, undefined, 200);
  assert.equal(preserved.email, email); assert.equal(preserved.nome, 'Nome alterado');
  fs.writeFileSync('target/aula10-textos-http.json', JSON.stringify(result, null, 2));
  console.log(JSON.stringify(result));
})().catch(e => { console.error(e); process.exitCode = 1; });
