// Antes: Maven (gera casos executando entidades reais) e exportação do OpenAPI da instância test.
const test = require('node:test');
const fs = require('node:fs');
const assert = require('node:assert/strict');

function artifacts() {
  assert(fs.existsSync('target/aula10-unicode-equivalencia.json'),
    'Execute OpenApiUnicodeEquivalenceTest ou a suíte Maven antes deste teste.');
  assert(fs.existsSync('target/aula10-openapi.json'),
    'Exporte o contrato real da instância test com aula10-exportar-contrato.cjs.');
  return {
    java: JSON.parse(fs.readFileSync('target/aula10-unicode-equivalencia.json', 'utf8')),
    api: JSON.parse(fs.readFileSync('target/aula10-openapi.json', 'utf8'))
  };
}

test('ECMAScript e padrões servidos equivalem aos resultados das entidades Java no corpus gerado', () => {
  const {java, api} = artifacts();
  assert(java.casosJava.length > 4500);
  assert.equal(Object.keys(java.padroesExportados).length, 9);
  for (const [key, pattern] of Object.entries(java.padroesExportados)) {
    const [schema, field] = key.split('.');
    const served = api.components.schemas[schema].properties[field].pattern;
    assert.equal(served, pattern, key + ': contrato servido difere do MockMvc');
    const regex = new RegExp(served);
    for (const c of java.casosJava)
      assert.equal(regex.test(c.texto), c.aceitoPeloDominio,
        key + ' codepoints=' + Array.from(c.texto, s => s.codePointAt(0).toString(16)).join(','));
  }
});

test('caso da auditoria e separadores de linha finais usam fim absoluto sem proibir controles internos', () => {
  const {api} = artifacts();
  const regex = new RegExp(api.components.schemas.PessoaRequest.properties.nome.pattern);
  for (const text of ['\u2003\u001b\u2003', '\u2003\u001b\u2028', '\u2003\u001b\u2029',
    'A\u001bB', '\u00a0', '\u0000Nome\u001b']) assert(regex.test(text));
  for (const text of ['', '\u0000', '\u001b', '\u2003', '\u0000\u2003\u001b', ' \t\n'])
    assert(!regex.test(text));
});
