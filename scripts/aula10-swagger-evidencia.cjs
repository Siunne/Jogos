// Chrome headless isolado na porta 9222; apenas navega na documentação, sem Try it out.
const fs=require('node:fs'),assert=require('node:assert/strict');
const base=process.env.AULA10_BASE_URL||'http://localhost:18080';
assert(['localhost','127.0.0.1'].includes(new URL(base).hostname));
let ws,id=0;const waiting=new Map();
const call=(method,params={})=>new Promise((resolve,reject)=>{const n=++id;waiting.set(n,{resolve,reject});ws.send(JSON.stringify({id:n,method,params}));});
const evaluate=async expression=>(await call('Runtime.evaluate',{expression,returnByValue:true})).result.value;
(async()=>{
 const tab=await(await fetch('http://localhost:9222/json/new?'+encodeURIComponent(base+'/swagger-ui.html'),{method:'PUT'})).json();
 ws=new WebSocket(tab.webSocketDebuggerUrl);await new Promise((ok,no)=>{ws.onopen=ok;ws.onerror=no;});
 ws.onmessage=({data})=>{const m=JSON.parse(data),w=waiting.get(m.id);if(w){waiting.delete(m.id);m.error?w.reject(Error(m.error.message)):w.resolve(m.result);}};
 await call('Page.enable');await call('Runtime.enable');
 await call('Emulation.setDeviceMetricsOverride',{width:1440,height:2800,deviceScaleFactor:1,mobile:false});await call('Page.navigate',{url:base+'/swagger-ui.html'});
 const deadline=Date.now()+30000;while(Date.now()<deadline){if(await evaluate('document.querySelectorAll(".opblock").length===38'))break;await new Promise(r=>setTimeout(r,100));}
 assert.equal(await evaluate('document.querySelectorAll(".opblock").length'),38);
 const tags=await evaluate('[...document.querySelectorAll(".opblock-tag")].map(e=>e.getAttribute("data-tag"))');
 for(const t of ['Pessoas','Clientes','Colaboradores','Vendas'])assert(tags.includes(t),t);
 await evaluate(`document.querySelectorAll('.opblock-tag-section').forEach(s=>{const h=s.querySelector('.opblock-tag');if(h&&!['Pessoas','Clientes','Colaboradores','Vendas'].includes(h.getAttribute('data-tag'))&&s.classList.contains('is-open'))h.click();})`);
 await new Promise(r=>setTimeout(r,500));const png=await call('Page.captureScreenshot',{format:'png'});
 fs.mkdirSync('docs/evidencias',{recursive:true});fs.writeFileSync('docs/evidencias/aula-10-swagger-ui.png',Buffer.from(png.data,'base64'));
 fs.writeFileSync('target/aula10-swagger-ui.json',JSON.stringify({executedAt:new Date().toISOString(),operationsBeforeCollapsing:38,tags,browser:await call('Browser.getVersion'),note:'Tags anteriores recolhidas pela interface para destacar os quatro grupos novos; nenhum endpoint comercial executado.'},null,2)+'\n');
 console.log('Swagger UI real: 38 operações renderizadas, quatro grupos novos confirmados; captura salva.');await call('Browser.close');
})().catch(e=>{console.error(e);ws?.close();process.exitCode=1;});
