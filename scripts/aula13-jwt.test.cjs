const test=require('node:test'), assert=require('node:assert/strict'), fs=require('node:fs');
const {sanitize}=require('./aula13-sanitizar.cjs');
const doc=()=>JSON.parse(fs.readFileSync('target/aula13-openapi.json'));
test('OpenAPI servido declara Bearer JWT sem Basic e login público',()=>{
 const d=doc();assert.deepEqual(Object.keys(d.components.securitySchemes),['bearerAuth']);
 assert.equal(d.components.securitySchemes.bearerAuth.scheme,'bearer');
 assert.equal(d.components.securitySchemes.bearerAuth.bearerFormat,'JWT');
 assert.deepEqual(d.paths['/api/autenticacao/login'].post.security,[]);
 assert.deepEqual(d.paths['/api/contas-acesso/me'].get.security,[{bearerAuth:[]}]);
 assert.equal(Object.keys(d.paths).length,28);
 assert.equal(Object.values(d.paths).flatMap(Object.values).filter(x=>x.operationId).length,42);
});
test('login e emissão não admitem perfis ou senha na resposta',()=>{
 const s=doc().components.schemas;
 assert.deepEqual(Object.keys(s.LoginRequest.properties).sort(),['login','senha']);
 assert.equal(s.LoginRequest.properties.senha.writeOnly,true);
 assert.equal(s.LoginRequest.properties.senha.minLength,12);
 assert(new RegExp(s.LoginRequest.properties.senha.pattern,'u').test('Unicode-😀-válido'));
 assert(!new RegExp(s.LoginRequest.properties.senha.pattern,'u').test('Unicode-\0-inválido'));
 for(const field of ['login','senha']) {
   const p=new RegExp(s.LoginRequest.properties[field].pattern,'u');
   for(const value of ['', ' '.repeat(12), '\t\n'.repeat(12), '\u2003'.repeat(12)])assert(!p.test(value));
   assert(p.test('\u00a0'.repeat(12)));
 }
 assert.deepEqual(Object.keys(s.TokenResponse.properties).sort(),['accessToken','expiresAt','expiresIn','tokenType']);
 assert.equal(s.TokenResponse.properties.expiresIn.example,1800);
});
test('contas e alteração preservam regras da política de senha no contrato atual',()=>{
 const schemas=doc().components.schemas;
 for(const [name,fields]of [['ContaAcessoRequest',['senha']],['AlteracaoSenhaRequest',['senhaAtual','novaSenha']]])for(const field of fields){
   const s=schemas[name].properties[field];assert.equal(s.minLength,12);assert.equal(s.maxLength,72);
   assert.equal(s.writeOnly,true);assert(s.description.includes('U+0000'));
 }
});
test('Newman captura automaticamente token e variáveis versionadas estão vazias',()=>{
 const c=JSON.parse(fs.readFileSync('postman/Jogos-Aula13.postman_collection.json'));
 assert.equal(c.auth.type,'bearer');assert.equal(c.auth.bearer[0].value,'{{accessToken}}');
 for(const name of ['accessToken','authPassword','authPasswordNova','authLogin'])assert.equal(c.variable.find(v=>v.key===name).value,'');
 const all=c.item.flatMap(i=>i.item||[i]);
 const login=all.find(i=>i.name==='13-01 Login e captura JWT');
 assert.equal(login.request.auth.type,'noauth');assert(login.event[0].script.exec.join('\n').includes("set('accessToken'"));
});
test('sanitizador remove token inclusive nos bytes de resposta SDK',()=>{
 const fake='eyJmaWN0aWNpbyI.eyJzdWIiOiJ0ZXN0ZSJ9.assinaturaFicticia';
 const r=sanitize({response:{stream:{type:'Buffer',data:[...Buffer.from(JSON.stringify({accessToken:fake,tokenType:'Bearer'}))]}},request:{header:[{key:'Authorization',value:'Bearer '+fake}]}},new Set([fake]));
 assert.equal(JSON.parse(Buffer.from(r.response.stream.data).toString()).accessToken,'[OMITIDO]');
 assert.equal(r.request.header[0].value,'[OMITIDO]');assert(!JSON.stringify(r).includes(fake));
});
test('sanitizador remove senhas em raw JSON e no objeto sem perder códigos HTTP',()=>{
 const r=sanitize({raw:JSON.stringify({login:'ficticio',senha:'NUNCA-GRAVAR'}),status:401,senhaHash:'hash-ficticio'});
 assert.equal(JSON.parse(r.raw).senha,'[OMITIDO]');assert.equal(r.status,401);assert.equal(r.senhaHash,'[OMITIDO]');
});
test('relatório real Newman não preserva tokens nem Authorization',()=>{
 const r=JSON.parse(fs.readFileSync('target/aula13-newman.json'));assert.equal(r.run.failures.length,0);
 let checked=0,issued=0;
 for(const e of r.run.executions){
   for(const h of e.request.header||[])if(h.key.toLowerCase()==='authorization'){assert.equal(h.value,'[OMITIDO]');checked++;}
   const text=Buffer.from(e.response.stream.data).toString();assert(!/eyJ[A-Za-z0-9_-]*\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+/.test(text));
   try{const b=JSON.parse(text);if('accessToken'in b){assert.equal(b.accessToken,'[OMITIDO]');issued++;}}catch(e){if(e.code==='ERR_ASSERTION')throw e;}
 }
 assert(checked>50);assert(issued>=2);
});
test('erros de login referenciam ApiError e token ausente tem schema próprio',()=>{
 const p=doc().paths;for(const c of ['400','401'])assert.equal(p['/api/autenticacao/login'].post.responses[c].content['application/json'].schema.$ref,'#/components/schemas/ApiError');
 assert.equal(p['/api/contas-acesso/me'].get.responses['401'].content['application/json'].schema.$ref,'#/components/schemas/ErroAutenticacao');
});
