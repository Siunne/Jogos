// Chrome headless isolado na porta 9233. Somente fixtures fictícias em jogos_test.
const fs=require('node:fs'),assert=require('node:assert/strict'),vm=require('node:vm'),{randomUUID}=require('node:crypto');
assert.equal(process.env.AULA13_BANCO_TESTE_CONFIRMADO,'jogos_test');
const base=process.env.AULA13_BASE_URL||'http://localhost:18081';assert(['localhost','127.0.0.1'].includes(new URL(base).hostname));
let ws,id=0;const waiting=new Map();
const call=(method,params={})=>new Promise((resolve,reject)=>{const n=++id;waiting.set(n,{resolve,reject});ws.send(JSON.stringify({id:n,method,params}));});
const evaluate=async expression=>{const r=await call('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});if(r.exceptionDetails)throw Error('Falha na interação de interface');return r.result.value;};
const until=async expression=>{const end=Date.now()+20000;while(Date.now()<end){if(await evaluate(expression))return;await new Promise(r=>setTimeout(r,100));}throw Error('Timeout na interface');};
(async()=>{
 const ctx=vm.createContext({pm:{info:{requestName:''}}});const c=require('../postman/Jogos-Aula10.postman_collection.json');vm.runInContext(c.event.find(e=>e.listen==='prerequest').script.exec.join('\n'),ctx);ctx.baseCpf=Date.now().toString().slice(-9);const cpf=vm.runInContext('fiscal(baseCpf,false)',ctx);
 const uid=randomUUID(),login='ui-'+uid.replace(/-/g,''),senha='UI-'+randomUUID();
 const pessoa=await fetch(base+'/api/pessoas',{method:'POST',headers:{'content-type':'application/json'},body:JSON.stringify({nome:'Pessoa fictícia UI',cpf,email:'ui-'+uid+'@example.invalid'})});assert.equal(pessoa.status,201);const p=await pessoa.json();
 const conta=await fetch(base+'/api/contas-acesso',{method:'POST',headers:{'content-type':'application/json'},body:JSON.stringify({pessoaId:p.id,login,senha})});assert.equal(conta.status,201);
 const tab=await(await fetch('http://localhost:9233/json/new?'+encodeURIComponent(base+'/swagger-ui.html'),{method:'PUT'})).json();ws=new WebSocket(tab.webSocketDebuggerUrl);await new Promise((ok,no)=>{ws.onopen=ok;ws.onerror=no;});ws.onmessage=({data})=>{const m=JSON.parse(data);const w=waiting.get(m.id);if(w){waiting.delete(m.id);m.error?w.reject(Error('Erro CDP')):w.resolve(m.result);}};
 await call('Page.enable');await call('Runtime.enable');await call('Emulation.setDeviceMetricsOverride',{width:1440,height:1800,deviceScaleFactor:1,mobile:false});await call('Page.navigate',{url:base+'/swagger-ui.html'});
 await until('document.querySelectorAll(".opblock").length===42');
 const ls='#operations-Autenticacao-autenticar';
 // Seletor pelo operationId evita depender da transliteração da tag.
 const loginSelector=await evaluate(`Array.from(document.querySelectorAll('.opblock')).find(e=>e.id.endsWith('-autenticar')).id`);
 const loginCss='#'+loginSelector;
 async function execute(selector){
   await evaluate(`document.querySelector(${JSON.stringify(selector)}).querySelector('.opblock-summary-control').click()`);
   await until(`document.querySelector(${JSON.stringify(selector)}).querySelector('.try-out__btn')!==null`);
   await evaluate(`document.querySelector(${JSON.stringify(selector)}).querySelector('.try-out__btn').click()`);
 }
 await execute(loginCss);
 await evaluate(`{const e=document.querySelector(${JSON.stringify(loginCss)}).querySelector('textarea');const setter=Object.getOwnPropertyDescriptor(HTMLTextAreaElement.prototype,'value').set;setter.call(e,${JSON.stringify(JSON.stringify({login,senha}))});e.dispatchEvent(new Event('input',{bubbles:true}));}`);
 await evaluate(`document.querySelector(${JSON.stringify(loginCss)}).querySelector('.execute').click()`);
 await until(`document.querySelector(${JSON.stringify(loginCss)}).querySelector('.live-responses-table tbody .response-col_status')?.innerText.trim()==='200'`);
 const token=await evaluate(`JSON.parse(document.querySelector(${JSON.stringify(loginCss)}).querySelector('.response-col_description .microlight').textContent).accessToken`);
 assert.equal(typeof token,'string');assert.equal(token.split('.').length,3);
 await evaluate(`window.ui.authActions.authorize({bearerAuth:{name:'bearerAuth',schema:{type:'http',scheme:'bearer',bearerFormat:'JWT'},value:${JSON.stringify(token)}}})`);
 const selector='#operations-Contas_de_acesso-consultarMinhaConta';
 await execute(selector);
 await evaluate(`document.querySelector(${JSON.stringify(selector)}).querySelector('.execute').click()`);
 await until(`document.querySelector(${JSON.stringify(selector)}).querySelector('.live-responses-table tbody .response-col_status')?.innerText.trim()==='200'`);
 // Remove campos/respostas do login e curl de acesso ANTES da captura. Não grava token.
 await evaluate(`document.querySelector(${JSON.stringify(loginCss)}).remove();document.querySelectorAll('pre,code').forEach(e=>{if(e.textContent.includes(${JSON.stringify(token)})||e.textContent.includes(${JSON.stringify(senha)}))e.remove();});document.querySelector(${JSON.stringify(selector)}).scrollIntoView();`);
 assert(await evaluate(`!document.body.innerText.includes(${JSON.stringify(token)})&&!document.body.innerText.includes(${JSON.stringify(senha)})`));
 const png=await call('Page.captureScreenshot',{format:'png'});fs.mkdirSync('docs/evidencias',{recursive:true});fs.writeFileSync('docs/evidencias/aula-13-swagger-bearer.png',Buffer.from(png.data,'base64'));
 fs.writeFileSync('target/aula13-swagger-ui.json',JSON.stringify({instante:new Date().toISOString(),operacoes:42,loginUI:200,bearerUI:200,captura:'docs/evidencias/aula-13-swagger-bearer.png',sanitizacao:'Login removido e curl com Authorization removido antes da captura',interacao:'Try it out/Execute reais; Authorize via AuthActions, não preenchimento manual do diálogo'},null,2)+'\n');
 console.log('Swagger UI: login real200, Authorize Bearer e /me200; captura sem token.');await call('Browser.close');
})().catch(e=>{console.error('Falha Swagger UI: '+e.message);ws?.close();process.exitCode=1;});
