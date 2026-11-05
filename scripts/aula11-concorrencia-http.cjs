// Complementa os testes de barreira Java: requisições reais, sem garantir o escalonamento.
// Escreve somente fixtures próprias. Confirme o catálogo no log JDBC antes de autorizar.
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const {randomUUID} = require('node:crypto');
const base = process.env.AULA11_BASE_URL || 'http://localhost:18080';
assert(['localhost', '127.0.0.1'].includes(new URL(base).hostname));
assert.equal(process.env.AULA11_BANCO_TESTE_CONFIRMADO, 'jogos_test');
const collection = require('../postman/Jogos-Aula10.postman_collection.json');
const context = vm.createContext({pm: {info: {requestName: ''}}});
vm.runInContext(collection.event.find(e => e.listen === 'prerequest').script.exec.join('\n'), context);
let requests = 0;
const results = [];
async function call(method, path, body, expected = 200) {
  const response = await fetch(base + path, {method, headers: {'content-type': 'application/json'},
    body: body === undefined ? undefined : JSON.stringify(body), signal: AbortSignal.timeout(10000)});
  requests++;
  const text = await response.text(), data = text ? JSON.parse(text) : null;
  if (expected !== null) assert.equal(response.status, expected, path + ': ' + text);
  return {status: response.status, data};
}
(async () => {
  const token = randomUUID();
  context.baseCpf = String(Date.now()).slice(-9);
  const cpf = vm.runInContext('fiscal(baseCpf, false)', context);
  const pessoa = (await call('POST', '/api/pessoas', {nome: 'Concorrência fictícia', cpf,
    email: 'a11-' + token + '@example.invalid'}, 201)).data;
  const cliente = (await call('POST', '/api/clientes', {pessoaId: pessoa.id}, 201)).data;
  const vendedor = (await call('POST', '/api/colaboradores', {pessoaId: pessoa.id,
    matricula: token.slice(0, 25), funcao: 'VENDEDOR', dataAdmissao: '2026-11-05'}, 201)).data;
  const genero = (await call('POST', '/api/generos', {nome: 'A11 ' + token}, 201)).data;
  async function jogo(suffix) {
    return (await call('POST', '/api/jogos', {codigo: token + suffix, nome: 'Disputa ' + suffix,
      generoId: genero.id, unidadesDisponiveis: 5, preco: 100, estoqueMinimo: 1}, 201)).data;
  }
  async function venda(jogos) {
    const v = (await call('POST', '/api/vendas', {clienteId: cliente.id, vendedorId: vendedor.id}, 201)).data;
    for (const j of jogos) await call('POST', `/api/vendas/${v.id}/itens`, {jogoId: j.id, quantidade: 4});
    return v;
  }
  async function saldo(j, esperado) {
    const atual = (await call('GET', `/api/jogos/${j.id}`)).data;
    assert.equal(atual.unidadesDisponiveis, esperado);
  }
  const a = await jogo('a'), va = await venda([a]), vb = await venda([a]);
  const finais = await Promise.all([va, vb].map(v => call('POST', `/api/vendas/${v.id}/finalizacao`, undefined, null)));
  assert.deepEqual(finais.map(r => r.status).sort(), [200, 400]);
  for (const fail of finais.filter(r => r.status === 400)) assert.equal(fail.data.message, 'Unidades disponíveis insuficientes');
  const estados = await Promise.all([va, vb].map(async v => (await call('GET', `/api/vendas/${v.id}`)).data.status));
  assert.deepEqual([...estados].sort(), ['ABERTA', 'FINALIZADA']); await saldo(a, 1);
  results.push({cenario: 'vendas4+4/saldo5', http: finais.map(r => r.status), estados, saldo: 1});
  const b = await jogo('b');
  const saidas = await Promise.all([1, 2].map(() => call('POST', `/api/jogos/${b.id}/estoque/saidas`, {quantidade: 4}, null)));
  assert.deepEqual(saidas.map(r => r.status).sort(), [200, 400]); await saldo(b, 1);
  results.push({cenario: 'saídas4+4/saldo5', http: saidas.map(r => r.status), saldo: 1});
  const c = await jogo('c');
  await Promise.all([2, 3].map(quantidade => call('POST', `/api/jogos/${c.id}/estoque/entradas`, {quantidade})));
  await saldo(c, 10); results.push({cenario: 'entradas2+3', saldo: 10});
  const d = await jogo('d'), vd = await venda([d]);
  const misto = await Promise.all([call('POST', `/api/vendas/${vd.id}/finalizacao`, undefined, null),
    call('POST', `/api/jogos/${d.id}/estoque/saidas`, {quantidade: 4}, null)]);
  assert.deepEqual(misto.map(r => r.status).sort(), [200, 400]); await saldo(d, 1);
  results.push({cenario: 'venda e saída4+4', http: misto.map(r => r.status), saldo: 1});
  const e = await jogo('e'), f = await jogo('f');
  const ve = await venda([f, e]), vf = await venda([e, f]);
  const inversas = await Promise.all([ve, vf].map(v => call('POST', `/api/vendas/${v.id}/finalizacao`, undefined, null)));
  assert.deepEqual(inversas.map(r => r.status).sort(), [200, 400]); await saldo(e, 1); await saldo(f, 1);
  results.push({cenario: 'dois jogos, itens inversos', http: inversas.map(r => r.status), saldos: [1, 1]});
  const report = {instante: new Date().toISOString(), bancoConfirmado: 'jogos_test', requests, results,
    limite: 'HTTP simultâneo complementa, não substitui barreiras e duas conexões dos testes Java.'};
  fs.writeFileSync('target/aula11-http.json', JSON.stringify(report, null, 2) + '\n');
  console.log(JSON.stringify(report, null, 2));
})().catch(e => {console.error(e); process.exitCode = 1;});
