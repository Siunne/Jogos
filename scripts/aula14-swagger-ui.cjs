const assert = require('node:assert/strict');
const {randomUUID} = require('node:crypto');
const vm = require('node:vm');

const base = process.env.AULA14_BASE_URL;
assert.equal(new URL(base).hostname, '127.0.0.1');
assert.equal(process.env.AULA14_BANCO_TESTE_CONFIRMADO, 'jogos_test');
const admin = {login: process.env.INITIAL_ADMIN_LOGIN, senha: process.env.INITIAL_ADMIN_PASSWORD};
assert(admin.login && admin.senha);
const collection = require('../postman/Jogos-Aula14.postman_collection.json');
const context = vm.createContext({pm:{info:{requestName:''}}});
vm.runInContext(collection.event.find(e => e.listen === 'prerequest').script.exec.join('\n'), context);
const cpf = () => {
  context.cpfBase = Date.now().toString().slice(-9);
  return vm.runInContext('fiscal(cpfBase, false)', context);
};

async function post(path, body, token, status) {
  const headers = {'content-type':'application/json'};
  if (token) headers.authorization = 'Bearer ' + token;
  const r = await fetch(base + path, {method:'POST', headers, body:JSON.stringify(body)});
  assert.equal(r.status, status);
  return r.json();
}

let ws;
async function run() {
  const {accessToken} = await post('/api/autenticacao/login', admin, null, 200);
  async function pessoa() {
    return post('/api/pessoas', {nome:'Pessoa fictícia UI', email:randomUUID()+'@example.invalid',cpf:cpf()}, accessToken, 201);
  }
  const op = {login:'ui-'+randomUUID(), senha:'Teste-'+randomUUID()};
  const p = await pessoa();
  await post('/api/contas-acesso', {pessoaId:p.id,...op}, accessToken, 201);
  const alvo = await pessoa();
  const novaConta = {pessoaId:alvo.id, login:'ui-'+randomUUID(), senha:'Teste-'+randomUUID()};
  const tabUrl = 'http://127.0.0.1:9234/json/new?'+encodeURIComponent(base+'/swagger-ui.html');
  const tab = await (await fetch(tabUrl, {method:'PUT'})).json();
  ws = new WebSocket(tab.webSocketDebuggerUrl);
  await new Promise((resolve,reject) => {ws.onopen=resolve; ws.onerror=reject;});
  let id=0;
  const pending=new Map();
  const responses = [];
  const finished = new Set();
  ws.onmessage=({data}) => {
    const m=JSON.parse(data);
    if (m.method === 'Network.responseReceived') {
      const {url,status}=m.params.response;
      const path=new URL(url).pathname;
      if (path.startsWith('/api/')) responses.push({path, status, id:m.params.requestId});
    }
    if (m.method === 'Network.loadingFinished') finished.add(m.params.requestId);
    const call=pending.get(m.id);
    if (call) { pending.delete(m.id); m.error ? call.reject(Error('CDP')) : call.resolve(m.result); }
  };
  const call=(method, params={}) => new Promise((resolve,reject) => {
    const next=++id;
    pending.set(next,{resolve,reject});
    ws.send(JSON.stringify({id:next,method,params}));
  });
  let stage = 'carregar interface';
  async function evaluate(expression) {
    const result=await call('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});
    if (result.exceptionDetails) throw Error('Falha no navegador em ' + stage + ' (' + (result.exceptionDetails.exception?.className || 'Runtime') + ')');
    return result.result.value;
  }
  async function until(expression) {
    const deadline=Date.now()+15000;
    while (Date.now() < deadline) {
      if (await evaluate(expression)) return;
      await new Promise(resolve=>setTimeout(resolve,100));
    }
    throw Error('Timeout Swagger em ' + stage);
  }
  const block=opId=>`Array.from(document.querySelectorAll('.opblock')).find(e=>e.id.endsWith(${JSON.stringify('-'+opId)}))`;
  async function execute(operation, body, status) {
    stage = operation + " / " + status;
    const op=block(operation);
    await evaluate(`{const e=${op};if(!e.classList.contains('is-open'))e.querySelector('.opblock-summary-control').click();}`);
    stage += ' preparar formulário';
    await until(`!!${op}.querySelector('.try-out__btn')`);
    await evaluate(`{const e=${op};if(!e.querySelector('.execute'))e.querySelector('.try-out__btn').click();}`);
    await until(`!!${op}.querySelector('.execute')`);
    stage += ' executar';
    if (body) await evaluate(`{const t=${op}.querySelector('textarea');Object.getOwnPropertyDescriptor(HTMLTextAreaElement.prototype,'value').set.call(t,${JSON.stringify(JSON.stringify(body))});t.dispatchEvent(new Event('input',{bubbles:true}));}`);
    const index = responses.length;
    await evaluate(`${op}.querySelector('.execute').click()`);
    const deadline = Date.now() + 15000;
    while (Date.now() < deadline) {
      const fresh = responses[index];
      if (fresh && finished.has(fresh.id)) {
        assert.equal(fresh.status, status, operation);
        console.log('Swagger HTTP ' + fresh.path + ' ' + fresh.status);
        return fresh.id;
      }
      await new Promise(resolve => setTimeout(resolve, 100));
    }
    throw Error('Resposta HTTP não recebida em ' + operation);
  }
  async function login(credentials) {
    const requestId = await execute('autenticar',credentials,200);
    const response = await call('Network.getResponseBody', {requestId});
    const raw = response.base64Encoded ? Buffer.from(response.body, 'base64').toString() : response.body;
    const token = JSON.parse(raw).accessToken;
    assert.equal(token.split('.').length,3);
    await evaluate(`window.ui.authActions.authorize({bearerAuth:{name:'bearerAuth',schema:{type:'http',scheme:'bearer',bearerFormat:'JWT'},value:${JSON.stringify(token)}}})`);
  }
  await call('Network.enable');
  await until('document.querySelectorAll(".opblock").length===44');
  await login(op);
  await execute('consultarMinhaConta',null,200);
  await execute('criarContaAcesso',novaConta,403);
  await evaluate('window.ui.authActions.logout(["bearerAuth"])');
  await login(admin);
  await execute('criarContaAcesso',novaConta,201);
  console.log('Swagger UI: login OPERADOR 200, /me 200, cadastro 403, login ADMINISTRADOR 200, cadastro 201');
}
run().catch(e=>{console.error(e.message);process.exitCode=1;}).finally(()=>ws?.close());
