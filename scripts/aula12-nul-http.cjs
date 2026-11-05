// Regressão HTTP independente; somente fixtures próprias em jogos_test, sem gravar credenciais.
const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict'),{randomUUID}=require('node:crypto');
assert.equal(process.env.AULA12_BANCO_TESTE_CONFIRMADO,'jogos_test');
const base=process.env.AULA12_BASE_URL||'http://localhost:18080';assert(['localhost','127.0.0.1'].includes(new URL(base).hostname));
(async()=>{
 const col=require('../postman/Jogos-Aula10.postman_collection.json'),ctx=vm.createContext({pm:{info:{requestName:''}}});
 vm.runInContext(col.event.find(e=>e.listen==='prerequest').script.exec.join('\n'),ctx);ctx.baseCpf=Date.now().toString().slice(-9);const cpf=vm.runInContext('fiscal(baseCpf,false)',ctx);
 const login='nul-'+randomUUID().replace(/-/g,''),senha='Teste-'+randomUUID(),nova='😀'.repeat(12),resultados=[];
 let requests=0;
 async function call(label,method,path,body,cred,expected){
   const headers={'content-type':'application/json'};if(cred!==undefined)headers.Authorization='Basic '+Buffer.from(login+':'+cred).toString('base64');
   const r=await fetch(base+path,{method,headers,body:body===undefined?undefined:JSON.stringify(body),signal:AbortSignal.timeout(10000)});requests++;
   const raw=await r.text();assert.equal(r.status,expected,label);assert(!raw.includes(senha));assert(!raw.includes(nova));assert(!raw.includes('{bcrypt}'));
   resultados.push({cenario:label,http:r.status});return raw?JSON.parse(raw):null;
 }
 const p=await call('Pessoa fictícia','POST','/api/pessoas',{nome:'Regressão NUL fictícia',email:randomUUID()+'@example.invalid',cpf},undefined,201);
 for(const n of [12,13,20])await call('cadastro NUL'+n,'POST','/api/contas-acesso',{pessoaId:p.id,login,senha:'\0'.repeat(n)},undefined,400);
 await call('cadastro válido após rejeições','POST','/api/contas-acesso',{pessoaId:p.id,login,senha},undefined,201);
 for(const n of [12,13,20])await call('Basic NUL'+n,'GET','/api/contas-acesso/me',undefined,'\0'.repeat(n),401);
 await call('nova senha com NUL','PUT','/api/contas-acesso/me/senha',{senhaAtual:senha,novaSenha:'\0'.repeat(12)},senha,400);
 await call('senha atual com NUL','PUT','/api/contas-acesso/me/senha',{senhaAtual:'\0'.repeat(13),novaSenha:nova},senha,400);
 await call('troca com Basic NUL','PUT','/api/contas-acesso/me/senha',{senhaAtual:'\0'.repeat(13),novaSenha:nova},'\0'.repeat(20),401);
 await call('senha anterior preservada após rejeições','GET','/api/contas-acesso/me',undefined,senha,200);
 await call('troca válida Unicode','PUT','/api/contas-acesso/me/senha',{senhaAtual:senha,novaSenha:nova},senha,204);
 await call('antiga após troca válida','GET','/api/contas-acesso/me',undefined,senha,401);
 await call('Unicode nova correta','GET','/api/contas-acesso/me',undefined,nova,200);
 await call('Unicode diferente rejeitada','GET','/api/contas-acesso/me',undefined,'😀'.repeat(13),401);
 const report={instante:new Date().toISOString(),banco:'jogos_test',requests,resultados};
 fs.writeFileSync('target/aula12-nul-http.json',JSON.stringify(report,null,2)+'\n');console.log(JSON.stringify(report,null,2));
})().catch(e=>{console.error(e.name+': falha na regressão HTTP NUL');process.exitCode=1;});
