// Chrome/Edge headless com CDP na porta 9222 e perfil isolado em target/.
// A aplicação em localhost:18080 deve estar usando exclusivamente o profile test.
const fs = require('node:fs');
const assert = require('node:assert/strict');
const base = process.env.AULA09_BASE_URL || 'http://localhost:18080';
assert(['localhost', '127.0.0.1'].includes(new URL(base).hostname));
const pause = ms => new Promise(resolve => setTimeout(resolve, ms));
async function until(callback, message, timeout = 30000) {
  const start = Date.now();
  while (Date.now() - start < timeout) {
    const value = await callback();
    if (value) return value;
    await pause(100);
  }
  throw Error('Timeout: ' + message);
}
let socket, nextId = 0, jogoId, generoId;
const pending = new Map(), network = new Map(), observations = [];
function call(method, params = {}) {
  const id = ++nextId;
  return new Promise((resolve, reject) => {
    pending.set(id, {resolve, reject});
    socket.send(JSON.stringify({id, method, params}));
  });
}
async function evaluate(expression) {
  const result = await call('Runtime.evaluate', {expression, awaitPromise:true, returnByValue:true});
  if (result.exceptionDetails) throw Error(result.exceptionDetails.text + ': ' + result.exceptionDetails.exception?.description);
  return result.result.value;
}
function responseSchema(doc, operation, status) {
  let schema = operation.responses[String(status)].content['application/json'].schema;
  if (schema.$ref) schema = doc.components.schemas[schema.$ref.split('/').at(-1)];
  return schema;
}
async function execute(operationId, {body, params = {}, expected = 200} = {}) {
  const definition = await evaluate(`(() => {
    const doc = window.ui.specSelectors.specJson().toJS();
    for (const [path, methods] of Object.entries(doc.paths)) for (const [method, op] of Object.entries(methods))
      if (op.operationId === ${JSON.stringify(operationId)}) return {path, method};
  })()`);
  assert(definition, operationId);
  let path = definition.path.replace(/\{([^}]+)\}/g, (_, key) => encodeURIComponent(params[key]));
  await evaluate(`(() => {
    const op = [...document.querySelectorAll('.opblock')].find(e => e.id.endsWith('-' + ${JSON.stringify(operationId)}));
    if (!op) throw Error('Operação não renderizada');
    if (!op.classList.contains('is-open')) op.querySelector('.opblock-summary-control').click();
  })()`);
  await until(() => evaluate(`(() => {
    const op = [...document.querySelectorAll('.opblock')].find(e => e.id.endsWith('-' + ${JSON.stringify(operationId)}));
    return !!op?.querySelector('.try-out__btn');
  })()`), 'Try it out');
  await evaluate(`(() => {
    const op = [...document.querySelectorAll('.opblock')].find(e => e.id.endsWith('-' + ${JSON.stringify(operationId)}));
    const button = op.querySelector('.try-out__btn');
    if (button.textContent.includes('Try it out')) button.click();
  })()`);
  await pause(150);
  async function fill(name, value) {
    const selector = `(() => {
      const op = [...document.querySelectorAll('.opblock')].find(e => e.id.endsWith('-' + ${JSON.stringify(operationId)}));
      if (${JSON.stringify(name)} === null) return op.querySelector('textarea');
      const row = [...op.querySelectorAll('tr[data-param-name]')].find(e => e.dataset.paramName === ${JSON.stringify(name)});
      if (!row) throw Error('Parâmetro ausente');
      if (!row.querySelector('input,select')) {
        [...row.querySelectorAll('button')].find(e => /Add.*item/i.test(e.textContent))?.click();
      }
      return row.querySelector('input,select');
    })()`;
    await until(() => evaluate(`!!(${selector})`), 'campo editável ' + name);
    const tag = await evaluate(`(() => { const field = ${selector}; field.focus();
      if (field.tagName !== 'SELECT') field.select(); return field.tagName; })()`);
    if (tag === 'SELECT') {
      await evaluate(`(() => { const field = ${selector}; field.value = ${JSON.stringify(String(value))};
        field.dispatchEvent(new Event('change', {bubbles:true})); })()`);
    } else {
      // Eventos reais do navegador atualizam também o estado controlado do React.
      await call('Input.insertText', {text:String(value)});
    }
    await pause(100);
  }
  if (body !== undefined) await fill(null, JSON.stringify(body, null, 2));
  for (const [name, value] of Object.entries(params)) await fill(name, value);
  await until(() => evaluate(`(() => {
    const values = window.ui.specSelectors.parameterValues([${JSON.stringify(definition.path)}, ${JSON.stringify(definition.method)}]).toJS();
    return Object.entries(${JSON.stringify(params)}).every(([name, value]) => {
      const actual = values['path.' + name] ?? values['query.' + name];
      return value === '' ? actual == null || actual === '' : String(actual) === String(value);
    });
  })()`), 'estado dos parâmetros no Swagger UI');
  await pause(350);
  const previous = new Set(network.keys());
  await evaluate(`(() => {
    const op = [...document.querySelectorAll('.opblock')].find(e => e.id.endsWith('-' + ${JSON.stringify(operationId)}));
    op.querySelector('button.execute').click();
  })()`);
  const entry = await until(() => [...network.entries()].find(([id, item]) => !previous.has(id)
    && item.request?.method.toLowerCase() === definition.method && new URL(item.request.url).pathname === path
    && item.finished && item.response), 'resposta HTTP da interface ' + operationId);
  const [id, info] = entry;
  const payload = await call('Network.getResponseBody', {requestId:id});
  const raw = payload.base64Encoded ? Buffer.from(payload.body, 'base64').toString('utf8') : payload.body;
  assert.equal(info.response.status, expected, operationId + ': ' + raw);
  const value = raw ? JSON.parse(raw) : null;
  if (expected === 201) assert.equal(info.response.headers.Location || info.response.headers.location, path + '/' + value.id);
  observations.push({operationId, method:definition.method.toUpperCase(), url:info.request.url,
    request:info.request.postData || null, status:info.response.status, response:value,
    location:info.response.headers.Location || info.response.headers.location || null, source:'Swagger UI Try it out / Execute'});
  console.log(expected + ' Swagger UI: ' + operationId);
  return value;
}
async function screenshot(path, operationId) {
  if (operationId) await evaluate(`(() => {
    const op = [...document.querySelectorAll('.opblock')].find(e => e.id.endsWith('-' + ${JSON.stringify(operationId)}));
    op.scrollIntoView({block:'start'});
  })()`);
  await pause(300);
  const result = await call('Page.captureScreenshot', {format:'png', captureBeyondViewport:false});
  fs.writeFileSync(path, Buffer.from(result.data, 'base64'));
}
(async () => {
  const tab = await (await fetch('http://localhost:9222/json/new?' + encodeURIComponent(base + '/swagger-ui.html'), {method:'PUT'})).json();
  socket = new WebSocket(tab.webSocketDebuggerUrl);
  await new Promise((resolve, reject) => { socket.onopen = resolve; socket.onerror = reject; });
  socket.onmessage = ({data}) => {
    const message = JSON.parse(data);
    if (message.id) {
      const task = pending.get(message.id); pending.delete(message.id);
      if (message.error) task?.reject(Error(message.error.message)); else task?.resolve(message.result);
    } else if (message.method === 'Network.requestWillBeSent') {
      const item = network.get(message.params.requestId) || {};
      item.request = message.params.request; network.set(message.params.requestId, item);
    } else if (message.method === 'Network.responseReceived') {
      const item = network.get(message.params.requestId) || {};
      item.response = message.params.response; network.set(message.params.requestId, item);
    } else if (message.method === 'Network.loadingFinished') {
      const item = network.get(message.params.requestId) || {};
      item.finished = true; network.set(message.params.requestId, item);
    }
  };
  await call('Network.enable'); await call('Page.enable'); await call('Runtime.enable');
  await call('Emulation.setDeviceMetricsOverride', {width:1440, height:1100, deviceScaleFactor:1, mobile:false});
  await call('Page.navigate', {url:base + '/swagger-ui.html'});
  await until(() => evaluate('window.ui?.specSelectors?.specJson()?.toJS()?.info?.title === "Jogos API"'), 'Swagger UI renderizado');
  await until(() => evaluate('document.querySelectorAll(".opblock").length === 18'), '18 operações renderizadas');
  const doc = await evaluate('window.ui.specSelectors.specJson().toJS()');
  fs.mkdirSync('docs/evidencias', {recursive:true});
  if (!fs.existsSync('docs/evidencias/aula-09-swagger-ui.png'))
    await screenshot('docs/evidencias/aula-09-swagger-ui.png');
  const token = Date.now().toString();
  const nomeGenero = 'Aventura Lab ' + token;
  generoId = (await execute('cadastrarGenero', {body:{nome:nomeGenero}, expected:201})).id;
  const created = await execute('cadastrarJogo', {expected:201, body:{codigo:'LAB-' + token, nome:'Reino das Estrelas ' + token,
    unidadesDisponiveis:10, preco:149.90, estoqueMinimo:3, generoId, fornecedorId:null}});
  jogoId = created.id;
  const consulted = await execute('consultarJogo', {params:{id:jogoId}});
  assert.deepEqual(consulted, created);
  const changed = await execute('alterarJogo', {params:{id:jogoId}, body:{nome:'Reino das Estrelas Completo ' + token,
    preco:129.90, estoqueMinimo:3, generoId, fornecedorId:null}});
  for (const key of ['codigo','dataCadastro','unidadesDisponiveis','status']) assert.deepEqual(changed[key], created[key], key);
  assert.equal((await execute('entradaEstoque', {params:{id:jogoId}, body:{quantidade:5}})).unidadesDisponiveis,15);
  assert.equal((await execute('entradaEstoque', {params:{id:jogoId}, body:{quantidade:5}})).unidadesDisponiveis,20);
  if (!fs.existsSync('docs/evidencias/aula-09-entrada-repetida.png'))
    await screenshot('docs/evidencias/aula-09-entrada-repetida.png', 'entradaEstoque');
  await evaluate(`(() => {
    const op = [...document.querySelectorAll('.opblock')].find(e => e.id.endsWith('-entradaEstoque'));
    op.querySelector('.responses-wrapper').scrollIntoView({block:'start'});
  })()`);
  await screenshot('docs/evidencias/aula-09-entrada-resposta.png');
  const page = await execute('pesquisarJogos', {params:{nome:changed.nome, generoId, fornecedorId:'',
    abaixoEstoqueMinimo:false, page:0, size:5, sort:'nome,asc'}});
  assert.equal(page.totalElementos,1); assert.equal(page.conteudo[0].id,jogoId); assert.equal(page.pagina,0); assert.equal(page.tamanho,5);
  assert.deepEqual(Object.keys(page).sort(), ['conteudo','pagina','tamanho','totalElementos','totalPaginas','primeira','ultima'].sort());
  for (const result of [
    await execute('entradaEstoque', {params:{id:jogoId}, body:{quantidade:0}, expected:400}),
    await execute('consultarJogo', {params:{id:'9223372036854775807'}, expected:404}),
    await execute('cadastrarGenero', {body:{nome:nomeGenero}, expected:409})
  ]) {
    const operationId = result.status === 400 ? 'entradaEstoque' : result.status === 404 ? 'consultarJogo' : 'cadastrarGenero';
    const op = Object.values(doc.paths).flatMap(path => Object.values(path)).find(op => op.operationId === operationId);
    const schema = responseSchema(doc, op, result.status);
    assert.deepEqual(Object.keys(result).sort(), Object.keys(schema.properties).sort());
    assert.equal(typeof result.fields,'object'); assert.equal(typeof result.message,'string');
  }
  await execute('excluirGenero', {params:{id:generoId}, expected:409});
  await execute('excluirJogo', {params:{id:jogoId}, expected:204}); jogoId = null;
  await execute('excluirGenero', {params:{id:generoId}, expected:204}); generoId = null;
  fs.writeFileSync('target/aula09-laboratorio-ui.json', JSON.stringify({executedAt:new Date().toISOString(),
    browser:await call('Browser.getVersion'), scenarios:7, operationsRendered:18, observations},null,2) + '\n');
  console.log('PASS: sete cenários pelo Swagger UI; entradas repetidas 10 -> 15 -> 20; captura adicional da resposta, anteriores preservadas.');
  await call('Browser.close');
})().catch(async error => {
  console.error(error);
  // Somente fixtures deste laboratório, no banco de testes.
  if (jogoId) await fetch(base + '/api/jogos/' + jogoId, {method:'DELETE'}).catch(() => {});
  if (generoId) await fetch(base + '/api/generos/' + generoId, {method:'DELETE'}).catch(() => {});
  socket?.close(); process.exitCode = 1;
});
