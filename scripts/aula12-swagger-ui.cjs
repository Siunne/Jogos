// Chrome headless isolado, porta9232. Cria somente fixtures fictícias em jogos_test.
const fs=require('node:fs'),assert=require('node:assert/strict'),vm=require('node:vm'),{randomUUID}=require('node:crypto');
assert.equal(process.env.AULA12_BANCO_TESTE_CONFIRMADO,'jogos_test');
const base=process.env.AULA12_BASE_URL||'http://localhost:18080';assert(['localhost','127.0.0.1'].includes(new URL(base).hostname));
let ws,id=0;const waiting=new Map();
const call=(method,params={})=>new Promise((resolve,reject)=>{const n=++id;waiting.set(n,{resolve,reject});ws.send(JSON.stringify({id:n,method,params}));});
const evaluate=async expression=>{const r=await call('Runtime.evaluate',{expression,returnByValue:true});if(r.exceptionDetails)throw Error('Falha na interação de interface');return r.result.value;};
const until=async expression=>{const end=Date.now()+15000;while(Date.now()<end){if(await evaluate(expression))return;await new Promise(r=>setTimeout(r,100));}throw Error('Timeout na interface');};
(async()=>{
 const ctx=vm.createContext({pm:{info:{requestName:''}}});const c=require('../postman/Jogos-Aula10.postman_collection.json');vm.runInContext(c.event.find(e=>e.listen==='prerequest').script.exec.join('\n'),ctx);ctx.baseCpf=Date.now().toString().slice(-9);const cpf=vm.runInContext('fiscal(baseCpf,false)',ctx);
 const token=randomUUID(),login='ui-'+token.replace(/-/g,''),senha='UI-'+randomUUID();
 const pessoa=await fetch(base+'/api/pessoas',{method:'POST',headers:{'content-type':'application/json'},body:JSON.stringify({nome:'Pessoa fictícia UI',cpf,email:'ui-'+token+'@example.invalid'})});assert.equal(pessoa.status,201);const p=await pessoa.json();
 const conta=await fetch(base+'/api/contas-acesso',{method:'POST',headers:{'content-type':'application/json'},body:JSON.stringify({pessoaId:p.id,login,senha})});assert.equal(conta.status,201);
 const tab=await(await fetch('http://localhost:9232/json/new?'+encodeURIComponent(base+'/swagger-ui.html'),{method:'PUT'})).json();ws=new WebSocket(tab.webSocketDebuggerUrl);await new Promise((ok,no)=>{ws.onopen=ok;ws.onerror=no;});ws.onmessage=({data})=>{const m=JSON.parse(data);const w=waiting.get(m.id);if(w){waiting.delete(m.id);m.error?w.reject(Error('Erro CDP')):w.resolve(m.result);}};
 await call('Page.enable');await call('Runtime.enable');await call('Emulation.setDeviceMetricsOverride',{width:1440,height:2000,deviceScaleFactor:1,mobile:false});await call('Page.navigate',{url:base+'/swagger-ui.html'});
 await until('document.querySelectorAll(".opblock").length===41');
 // Mesma ação do botão Authorize, sem senha em arquivo, console ou captura.
 await evaluate(`window.ui.authActions.authorize({basicAuth:{name:'basicAuth',schema:{type:'http',scheme:'basic'},value:{username:${JSON.stringify(login)},password:${JSON.stringify(senha)}}}})`);
 const selector='#operations-Contas_de_acesso-consultarMinhaConta';
 await evaluate(`document.querySelector(${JSON.stringify(selector)}).querySelector('.opblock-summary-control').click()`);
 await until(`document.querySelector(${JSON.stringify(selector)}).querySelector('.try-out__btn')!==null`);
 await evaluate(`document.querySelector(${JSON.stringify(selector)}).querySelector('.try-out__btn').click()`);
 await evaluate(`document.querySelector(${JSON.stringify(selector)}).querySelector('.execute').click()`);
 await until(`document.querySelector(${JSON.stringify(selector)}).querySelector('.live-responses-table tbody .response-col_status')?.innerText.trim()==='200'`);
 const encoded=Buffer.from(login+':'+senha).toString('base64');
 async function screenshot(suffix){
   await evaluate(`document.querySelectorAll('pre,code').forEach(e=>{if(e.textContent.includes(${JSON.stringify(encoded)}))e.style.visibility='hidden';});document.querySelector(${JSON.stringify(selector)}).scrollIntoView();`);
   assert(await evaluate(`!document.body.innerText.includes(${JSON.stringify(senha)})&&!document.body.innerText.includes(${JSON.stringify(encoded)})`));
   const png=await call('Page.captureScreenshot',{format:'png'});fs.mkdirSync('docs/evidencias',{recursive:true});fs.writeFileSync('docs/evidencias/aula-12-swagger-'+suffix+'.png',Buffer.from(png.data,'base64'));
 }
 await screenshot('autenticado');
 await evaluate(`window.ui.authActions.logout(['basicAuth'])`);
 await until(`!window.ui.authSelectors.authorized().has('basicAuth')`);
 const anonimo=await fetch(base+'/api/contas-acesso/me');assert.equal(anonimo.status,401);
 fs.writeFileSync('target/aula12-swagger-ui.json',JSON.stringify({instante:new Date().toISOString(),operacoes:41,basicAutorizadoUI:200,aposLogoutHTTP:401,aposLogoutVisual:'NÃO VERIFICADO: a automação não comprovou resposta401 renderizada após logout',captura:'docs/evidencias/aula-12-swagger-autenticado.png',credenciais:'Trecho curl com Authorization ocultado por CSS antes da captura',interacao:'AuthActions do Swagger UI, Try it out/Execute reais; não preenchimento manual do diálogo'},null,2)+'\n');
 console.log('Swagger UI real:41 operações e Basic200; anônimo HTTP401. Renderização visual do401 pendente.');await call('Browser.close');
})().catch(e=>{console.error('Falha Swagger UI: '+e.message);ws?.close();process.exitCode=1;});
