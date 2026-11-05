// Somente leitura HTTP. Exporta a versão Aula10 e nunca reescreve a evidência Aula09.
const fs=require('node:fs');const assert=require('node:assert/strict');
const base=process.env.AULA10_BASE_URL||'http://localhost:18080';
assert(['localhost','127.0.0.1'].includes(new URL(base).hostname));
(async()=>{
 const jr=await fetch(base+'/v3/api-docs');assert.equal(jr.status,200);const raw=await jr.text(),doc=JSON.parse(raw);
 const ops=Object.values(doc.paths).flatMap(p=>Object.values(p)).filter(o=>o.operationId);assert.equal(ops.length,38);assert.equal(doc.info.title,'Jogos API');
 for(const name of ['Pessoas','Clientes','Colaboradores','Vendas'])assert(doc.tags.some(t=>t.name===name));
 const yr=await fetch(base+'/v3/api-docs.yaml');assert.equal(yr.status,200);const yaml=await yr.text();
 fs.mkdirSync('target',{recursive:true});fs.writeFileSync('target/aula10-openapi.json',raw);fs.writeFileSync('target/aula10-openapi.yaml',yaml);fs.writeFileSync('docs/aula-10-openapi.yaml',yaml);
 console.log('Contrato real exportado: '+ops.length+' operações, '+Object.keys(doc.paths).length+' caminhos, '+Object.keys(doc.components.schemas).length+' schemas. Parsing/equivalência YAML: testes Maven.');
})().catch(e=>{console.error(e);process.exitCode=1;});
