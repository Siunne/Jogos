const {test} = require('node:test');
const assert = require('node:assert/strict');
const {validate, multipleOf} = require('./aula09-schema-validator.cjs');
test('rejeita 1.2345 para multipleOf 0.001', () => {
  assert.throws(() => validate(1.2345, {type:'number', multipleOf:0.001}), /multipleOf/);
});
test('aceita múltiplos decimais sem divisão binária imprecisa', () => {
  for (const value of [0, 0.001, 1.234, 5, 149.9]) assert(multipleOf(value, 0.001));
  assert(multipleOf(0.3, 0.1)); assert(multipleOf(1e-7, 1e-8));
});
test('valida limites inclusivos e exclusivos', () => {
  assert.throws(() => validate(0,{exclusiveMinimum:0}));
  assert.throws(() => validate(5,{exclusiveMaximum:5}));
  assert.throws(() => validate(-1,{minimum:0}));
  assert.throws(() => validate(6,{maximum:5}));
  validate(5,{minimum:0, maximum:5});
});
test('valida texto obrigatório, pattern e comprimento por codepoint', () => {
  const schema={type:'string', minLength:1, maxLength:5, pattern:'\\S'};
  for (const value of ['', '   ', 'abcdef']) assert.throws(() => validate(value,schema));
  validate('🎮',{type:'string',minLength:1,maxLength:1});
});
test('valida referências, required, campos adicionais e nulabilidade', () => {
  const doc={components:{schemas:{Valor:{type:['integer','null']}}}};
  validate(null,{$ref:'#/components/schemas/Valor'},'valor',false,doc);
  assert.throws(() => validate(1.5,{$ref:'#/components/schemas/Valor'},'valor',false,doc));
  assert.throws(() => validate({},{type:'object', required:['nome']}));
  assert.throws(() => validate({outro:1},{type:'object',additionalProperties:false}));
});
test('valida enum, arrays e datas de calendário', () => {
  assert.throws(() => validate('INVALIDO',{enum:['ATIVO','INATIVO']}));
  assert.throws(() => validate([1,1],{type:'array',uniqueItems:true}));
  assert.throws(() => validate('2026-02-30',{type:'string',format:'date'}));
  assert.throws(() => validate('2026-11-04T25:00:00',{format:'local-date-time'}));
  validate('2026-11-04T21:30:00.123',{format:'local-date-time'});
});
test('recusa composição que ainda não implementa, em vez de declarar sucesso', () => {
  assert.throws(() => validate('x',{oneOf:[{type:'string'}]}), /não suportada/);
});
