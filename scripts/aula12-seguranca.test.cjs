const test = require('node:test'), assert = require('node:assert/strict'), fs = require('node:fs');
const doc = () => JSON.parse(fs.readFileSync('target/aula12-openapi.json'));
test('padrões efetivamente exportados rejeitam NUL em toda posição e preservam Unicode sem NUL',()=>{
 for(const [name,fields] of [['ContaAcessoRequest',['senha']],['AlteracaoSenhaRequest',['senhaAtual','novaSenha']]])for(const field of fields){
   const s=doc().components.schemas[name].properties[field],p=new RegExp(s.pattern,'u');
   assert(s.description.includes('U+0000'));
   for(const value of ['\0'.repeat(12),'\0'.repeat(13),'\0'.repeat(20),'\0frase-secreta-fictícia','frase-secreta-fictícia\0','início\0final'])assert(!p.test(value));
   for(const value of ['á'.repeat(12),'😀'.repeat(12),'\u00a0'.repeat(12),'Frase\u001b válida de estudo','e\u0301'.repeat(12)])assert(p.test(value));
 }
});
test('login servido usa alfabeto ASCII e espaços externos U+0020, com limite normalizado', () => {
 const p = new RegExp(doc().components.schemas.ContaAcessoRequest.properties.login.pattern);
 for(const s of ['abc',' AbC_09.- ','a'.repeat(80),' '+ 'a'.repeat(80)+' ']) assert(p.test(s));
 for(const s of ['','  ','ab','a b','ábc','\tabc','abc\n','\u00a0abc\u00a0','a'.repeat(81)]) assert(!p.test(s));
});
test('schemas de senha são writeOnly e a resposta não contém hash', () => {
 const schemas=doc().components.schemas;
 for(const [s,fields] of [['ContaAcessoRequest',['senha']],['AlteracaoSenhaRequest',['senhaAtual','novaSenha']]])for(const field of fields){assert.equal(schemas[s].properties[field].writeOnly,true);assert.equal(schemas[s].properties[field].format,'password');}
 for(const k of ['senha','senhaHash','senha_hash'])assert(!schemas.ContaAcessoResponse.properties[k]);
});
test('coleção não guarda credenciais e herda Basic via variáveis',()=>{
 const c=JSON.parse(fs.readFileSync('postman/Jogos-Aula12.postman_collection.json'));
 assert.equal(c.auth.type,'basic');assert.equal(c.auth.basic.find(v=>v.key==='password').value,'{{authPassword}}');
 for(const key of ['authLogin','authPassword','authPasswordNova'])assert.equal(c.variable.find(v=>v.key===key).value,'');
});
test('relatório Newman sanitizado não contém headers Authorization utilizáveis',()=>{
 const r=JSON.parse(fs.readFileSync('target/aula12-newman.json'));
 let checked=0;
 for(const e of r.run.executions) {
   assert(Array.isArray(e.request.header));
   for(const h of e.request.header)if(h.key.toLowerCase()==='authorization'){assert.equal(h.value,'[OMITIDO]');checked++;}
   if(e.request.body?.raw && e.request.url.path[1]==='contas-acesso') {const b=JSON.parse(e.request.body.raw);for(const k of ['senha','senhaAtual','novaSenha'])if(b[k])assert.equal(b[k],'[OMITIDO]');}
 }
 assert(checked>50, 'Precisa inspecionar headers reais, não uma lista ausente');
 assert.equal(r.run.failures.length,0);
});
