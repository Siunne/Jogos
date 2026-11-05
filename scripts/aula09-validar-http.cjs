// Node 22+; executar somente contra uma instância com profile test.
const fs = require('node:fs');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const base = process.env.AULA09_BASE_URL || 'http://localhost:18080';
assert(['localhost', '127.0.0.1'].includes(new URL(base).hostname), 'Usar somente servidor local de testes');
const collection = JSON.parse(fs.readFileSync('Jogos - Aula 07.postman_collection.json', 'utf8'));
let doc;
const {validate: validateSchema} = require('./aula09-schema-validator.cjs');
const validate = (value, schema, label, request = false) => validateSchema(value, schema, label, request, doc);
function operation(method, url) {
  const path = new URL(url).pathname;
  const route = Object.keys(doc.paths).find(candidate => {
    const expression = '^' + candidate.replace(/[.*+?^$()|[\]\\]/g, '\\$&').replace(/\{[^}]+\}/g, '[^/]+') + '$';
    return new RegExp(expression).test(path);
  });
  assert(route, 'Rota ausente do contrato: ' + path);
  const op = doc.paths[route][method.toLowerCase()];
  assert(op, 'Método ausente do contrato: ' + method + ' ' + route);
  return op;
}
const variables = new Map(collection.variable.map(v => [v.key, v.value]));
variables.set('baseUrl', base);
const interpolate = value => value.replace(/\{\{(.*?)\}\}/g, (_, key) => {
  assert(variables.has(key), 'Variável ausente: ' + key);
  return variables.get(key);
});
let checks = 0;
const observations = [];
const pm = {
  collectionVariables: {set: (key, value) => variables.set(key, value), get: key => variables.get(key)},
  test: (name, callback) => { callback(); checks++; },
  expect: value => {
    const chain = {
      eql: expected => assert.deepEqual(value, expected),
      an: type => assert.equal(Array.isArray(value) ? 'array' : typeof value, type),
      keys: (...keys) => assert.deepEqual(Object.keys(value).sort(), keys.sort())
    };
    for (const key of ['to', 'be', 'have', 'all']) chain[key] = chain;
    return chain;
  }
};
function scripts(events, kind) {
  for (const event of events || []) if (event.listen === kind)
    vm.runInNewContext(event.script.exec.join('\n'), {pm}, {timeout: 2000});
}
(async () => {
  fs.mkdirSync('target', {recursive: true});
  const jsonResponse = await fetch(base + '/v3/api-docs');
  assert.equal(jsonResponse.status, 200);
  const rawJson = await jsonResponse.text();
  doc = JSON.parse(rawJson);
  assert.equal(Object.values(doc.paths).flatMap(path => Object.values(path)).filter(op => op.operationId).length, 18,
    'Este exportador pertence à Aula 09. Para Aula 10, use aula10-exportar-contrato.cjs; o histórico não será sobrescrito.');
  assert.equal(doc.info.title, 'Jogos API');
  const yamlResponse = await fetch(base + '/v3/api-docs.yaml');
  assert.equal(yamlResponse.status, 200);
  const rawYaml = await yamlResponse.text();
  fs.writeFileSync('target/aula09-openapi.json', rawJson);
  fs.writeFileSync('target/aula09-openapi.yaml', rawYaml);
  fs.writeFileSync('docs/aula-09-openapi.yaml', rawYaml);
  const ui = await fetch(base + '/swagger-ui.html');
  assert.equal(ui.status, 200);
  assert((await ui.text()).includes('swagger-ui-bundle.js'));
  for (const item of collection.item) {
    pm.info = {requestName: item.name};
    scripts(collection.event, 'prerequest');
    const request = item.request;
    const url = interpolate(typeof request.url === 'string' ? request.url : request.url.raw);
    const op = operation(request.method, url);
    const headers = Object.fromEntries((request.header || []).map(header => [header.key, header.value]));
    const input = request.body ? interpolate(request.body.raw) : undefined;
    const response = await fetch(url, {method: request.method, headers, ...(input ? {body: input} : {})});
    const body = await response.text();
    pm.response = {json: () => JSON.parse(body), text: () => body,
      to: {have: {status: expected => assert.equal(response.status, expected, item.name + ': ' + body)}}};
    scripts(item.event, 'test');
    const output = op.responses[String(response.status)];
    assert(output, 'Status não documentado: ' + response.status + ' ' + url);
    if (response.status === 201) assert.equal(response.headers.get('location'), new URL(url).pathname + '/' + JSON.parse(body).id);
    if (response.status === 204) {
      assert.equal(body, '');
      assert(!output.content || !Object.keys(output.content).length);
    } else {
      const mediaType = response.headers.get('content-type').split(';')[0];
      const schema = output.content?.[mediaType]?.schema;
      validate(mediaType.includes('json') ? JSON.parse(body) : body, schema, item.name);
    }
    if (response.status < 400 && input) validate(JSON.parse(input), op.requestBody.content['application/json'].schema, item.name + ':request', true);
    observations.push({name:item.name, method:request.method, url, request:input || null,
      status:response.status, location:response.headers.get('location'), response:body, schemaSubsetValidated:true});
    console.log(response.status + ' ' + item.name + ' — subconjunto suportado do schema conferido');
  }
  fs.writeFileSync('target/aula09-postman-evidencias.json', JSON.stringify({executedAt:new Date().toISOString(),
    executor:'Node HTTP + scripts Postman + comparação parcial com schemas OpenAPI', profile:'test',
    scope:'Tipos, enum/const, limites, multipleOf decimal, textos/pattern, required, refs, allOf, arrays, propriedades adicionais e formatos date/local-date-time. Não valida integralmente OpenAPI; números sujeitos à precisão Number do JavaScript.',
    requests:observations.length, checks, observations}, null, 2) + '\n');
  console.log('PASS: ' + observations.length + ' requisições; ' + checks + ' verificações da coleção; corpos comparados ao subconjunto suportado. Não é validação integral de OpenAPI.');
})().catch(error => { console.error(error); process.exitCode = 1; });
