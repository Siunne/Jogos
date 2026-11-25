// Newman real. Credenciais fictícias existem só na memória; relatórios são sanitizados antes da escrita.
const fs = require('node:fs'), path = require('node:path'), assert = require('node:assert/strict');
const dir = process.env.PATH.split(path.delimiter).find(p => fs.existsSync(path.join(p, 'newman.cmd')) || fs.existsSync(path.join(p, 'newman')));
assert(dir, 'Execute via npm exec --offline --package=newman -- node scripts/aula13-newman.cjs');
const newman = require(path.resolve(dir, '..', 'newman'));
const base = process.env.AULA13_BASE_URL || 'http://localhost:18080';
assert(['localhost', '127.0.0.1'].includes(new URL(base).hostname));
assert.equal(process.env.AULA13_BANCO_TESTE_CONFIRMADO, 'jogos_test');
const secrets = new Set();
function collect(req) {
  req.headers?.each(h => {if (h.key.toLowerCase() === 'authorization') secrets.add(h.value);});
  try {const b = JSON.parse(req.body?.raw || '{}');for (const key of ['senha','senhaAtual','novaSenha']) if (b[key] && !b[key].includes('{{')) secrets.add(b[key]);} catch {}
}
const {sanitize} = require('./aula13-sanitizar.cjs');
const clean = value => sanitize(value, secrets);
newman.run({collection: 'postman/Jogos-Aula13.postman_collection.json', envVar: [{key: 'baseUrl', value: base}], reporters: []})
 .on('beforeRequest', (err, args) => {if (!err) collect(args.request);})
 .on('request', (err, args) => {if (!err) {
   collect(args.request);
   try {const body=JSON.parse(args.response.stream.toString());if(body.accessToken)secrets.add(body.accessToken);}catch{}
 }})
 .on('done', (err, result) => {
   if (err) {console.error('Falha na infraestrutura Newman');process.exitCode = 1;return;}
   // toJSON do SDK preserva header[] e Buffer{data}, sem gravar o intermediário sensível.
   const normalized = JSON.parse(JSON.stringify({run: {stats: result.run.stats, failures: result.run.failures, executions: result.run.executions}}));
   const report = clean(normalized);
   const encoded = JSON.stringify(report, null, 2);
   for (const secret of secrets) assert(!encoded.includes(secret), 'Falha na sanitização do relatório');
   fs.writeFileSync('target/aula13-newman.json', encoded + '\n');
   console.log(JSON.stringify({requests: result.run.stats.requests.total, assertions: result.run.stats.assertions.total, failures: result.run.failures.length, credenciais: 'omitidas antes da gravação'}));
   if (result.run.failures.length) {console.error(result.run.failures.map(f => ({item: f.source?.name, erro: clean(f.error?.test || f.error?.name || 'falha')})));process.exitCode = 1;}
 });
