// Sanitização antes da gravação, inclusive bytes de respostas do SDK Newman.
function sanitize(value, secrets = new Set(), key = '') {
  if (/^(password|senha|senhaAtual|novaSenha|senhaHash|senha_hash|accessToken|token|authPassword|authPasswordNova)$/i.test(key)) return '[OMITIDO]';
  if (Array.isArray(value)) return value.map(v => sanitize(v, secrets));
  if (value && typeof value === 'object') {
    if (value.type === 'Buffer' && Array.isArray(value.data)) {
      const text = Buffer.from(value.data).toString('utf8');
      let cleaned;
      try { cleaned = JSON.stringify(sanitize(JSON.parse(text), secrets)); }
      catch { cleaned = sanitize(text, secrets); }
      return {type:'Buffer', data:[...Buffer.from(cleaned)]};
    }
    if (typeof value.key === 'string' && /authorization|password|accessToken|^token$/i.test(value.key))
      return {...value, value:'[OMITIDO]'};
    return Object.fromEntries(Object.entries(value).map(([k,v]) => [k, sanitize(v,secrets,k)]));
  }
  if (typeof value === 'string') {
    // request.body.raw também é JSON serializado, não apenas objeto já interpretado.
    if (key === 'raw') {
      try { return JSON.stringify(sanitize(JSON.parse(value), secrets)); } catch {}
    }
    for (const secret of secrets) if (secret) value = value.split(secret).join('[OMITIDO]');
    value = value.replace(/eyJ[A-Za-z0-9_-]*\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+/g,'[OMITIDO]');
  }
  return value;
}
module.exports={sanitize};
