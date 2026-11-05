// Subconjunto explícito de JSON Schema/OpenAPI; não é um validador integral da especificação.
const assert = require('node:assert/strict');
function decimal(value) {
  const [mantissa, exponent = '0'] = String(value).toLowerCase().split('e');
  const [integer, fraction = ''] = mantissa.split('.');
  let scale = fraction.length - Number(exponent);
  let units = BigInt(integer + fraction);
  if (scale < 0) { units *= 10n ** BigInt(-scale); scale = 0; }
  return {units, scale};
}
function multipleOf(value, step) {
  assert(Number.isFinite(step) && step > 0, 'multipleOf deve ser positivo');
  const a = decimal(value), b = decimal(step), scale = Math.max(a.scale, b.scale);
  return (a.units * 10n ** BigInt(scale - a.scale)) % (b.units * 10n ** BigInt(scale - b.scale)) === 0n;
}
function validate(value, inputSchema, label = 'valor', request = false, doc = {}) {
  if (inputSchema === true) return;
  assert(inputSchema !== false && inputSchema, 'Schema ausente/proibido: ' + label);
  const schema = inputSchema.$ref
    ? inputSchema.$ref.slice(2).split('/').reduce((v, key) => v?.[key.replace(/~1/g, '/').replace(/~0/g, '~')], doc)
    : inputSchema;
  assert(schema, 'Referência ausente: ' + label);
  for (const keyword of ['oneOf', 'anyOf', 'not', 'if', 'contains', 'patternProperties', 'unevaluatedProperties'])
    assert(!Object.hasOwn(schema, keyword), 'Restrição ainda não suportada: ' + keyword);
  if (schema.allOf) schema.allOf.forEach(part => validate(value, part, label, request, doc));
  const types = Array.isArray(schema.type) ? schema.type : schema.type ? [schema.type] : [];
  const type = value === null ? 'null' : Array.isArray(value) ? 'array' : typeof value;
  if (types.length) assert(types.includes(type) || (type === 'number' && types.includes('integer') && Number.isInteger(value)), label + ': tipo incompatível');
  if (schema.enum) assert(schema.enum.includes(value), label + ': enum inválido');
  if (Object.hasOwn(schema, 'const')) assert.deepEqual(value, schema.const, label);
  if (value === null) return;
  if (typeof value === 'number') {
    assert(Number.isFinite(value), label + ': número não finito');
    if (schema.minimum !== undefined) assert(value >= schema.minimum, label);
    if (schema.maximum !== undefined) assert(value <= schema.maximum, label);
    if (typeof schema.exclusiveMinimum === 'number') assert(value > schema.exclusiveMinimum, label);
    if (typeof schema.exclusiveMaximum === 'number') assert(value < schema.exclusiveMaximum, label);
    if (schema.multipleOf !== undefined) assert(multipleOf(value, schema.multipleOf), label + ': multipleOf inválido');
  }
  if (typeof value === 'string') {
    const length = [...value].length;
    if (schema.maxLength !== undefined) assert(length <= schema.maxLength, label);
    if (schema.minLength !== undefined) assert(length >= schema.minLength, label);
    if (schema.pattern) assert(new RegExp(schema.pattern, 'u').test(value), label);
    if (schema.format === 'date' || schema.format === 'local-date-time') {
      const date = value.slice(0, 10);
      assert(/^\d{4}-\d{2}-\d{2}$/.test(date), label);
      const parsed = new Date(date + 'T00:00:00Z');
      assert(!Number.isNaN(parsed.getTime()) && parsed.toISOString().slice(0, 10) === date, label);
      if (schema.format === 'date') assert.equal(value.length, 10, label);
      else assert(/^\d{4}-\d{2}-\d{2}T(?:[01]\d|2[0-3]):[0-5]\d:[0-5]\d(?:\.\d+)?$/.test(value), label);
    }
  }
  if (Array.isArray(value)) {
    if (schema.minItems !== undefined) assert(value.length >= schema.minItems, label);
    if (schema.maxItems !== undefined) assert(value.length <= schema.maxItems, label);
    if (schema.uniqueItems) assert.equal(new Set(value.map(v => JSON.stringify(v))).size, value.length, label);
    if (schema.items) value.forEach((item, index) => validate(item, schema.items, label + '[' + index + ']', request, doc));
  } else if (typeof value === 'object') {
    for (const key of schema.required || []) {
      if (request && schema.properties?.[key]?.readOnly) continue;
      assert(Object.hasOwn(value, key), label + ': obrigatório ausente ' + key);
    }
    for (const [key, item] of Object.entries(value)) {
      if (schema.properties?.[key]) validate(item, schema.properties[key], label + '.' + key, request, doc);
      else if (schema.additionalProperties === false) assert.fail(label + ': propriedade adicional ' + key);
      else if (typeof schema.additionalProperties === 'object') validate(item, schema.additionalProperties, label + '.' + key, request, doc);
    }
  }
}
module.exports = {validate, multipleOf};
