// Único ponto de acesso à API. Nenhuma tela chama fetch direto.
//
// Contrato da API: POST e PUT recebem os campos como parâmetros de consulta (não JSON),
// campos vazios são omitidos (a API trata ausente como nulo) e os erros seguem ErroResponse.

/** Resposta de erro da API (4xx/5xx), com a mensagem e a lista de campos inválidos */
export class ErroApi extends Error {
  constructor(status, corpo) {
    super(corpo?.mensagem || `A API respondeu com erro ${status}`);
    this.name = "ErroApi";
    this.status = status;
    this.corpo = corpo;
    this.campos = Array.isArray(corpo?.campos) ? corpo.campos : [];
  }
}

/** A API não respondeu (aplicação desligada, rede caiu) */
export class ApiIndisponivel extends Error {
  constructor() {
    super("Não foi possível falar com a API. Confira se a aplicação está rodando e tente de novo.");
    this.name = "ApiIndisponivel";
  }
}

export function montarQuery(campos) {
  if (!campos) return "";
  const params = new URLSearchParams();
  for (const [nome, valor] of Object.entries(campos)) {
    if (valor === undefined || valor === null) continue;
    const texto = typeof valor === "string" ? valor.trim() : String(valor);
    if (texto === "") continue;
    params.append(nome, texto);
  }
  const qs = params.toString();
  return qs ? `?${qs}` : "";
}

async function requisitar(metodo, caminho, campos) {
  let resposta;
  try {
    resposta = await fetch(caminho + montarQuery(campos), {
      method: metodo,
      headers: { Accept: "application/json" },
    });
  } catch {
    throw new ApiIndisponivel();
  }
  if (resposta.status === 204) return null;
  const texto = await resposta.text();
  let corpo = null;
  try {
    corpo = texto ? JSON.parse(texto) : null;
  } catch {
    corpo = null;
  }
  if (!resposta.ok) {
    // 502/503/504 vindos de proxy também significam que a aplicação não está respondendo
    if ([502, 503, 504].includes(resposta.status) && !corpo) throw new ApiIndisponivel();
    throw new ErroApi(resposta.status, corpo);
  }
  return corpo;
}

export const api = {
  get: (caminho, filtros) => requisitar("GET", caminho, filtros),
  criar: (caminho, campos) => requisitar("POST", caminho, campos),
  atualizar: (caminho, campos) => requisitar("PUT", caminho, campos),
  excluir: (caminho) => requisitar("DELETE", caminho),
};

/**
 * Requisição crua para o explorador: devolve status, tempo e corpo como vieram,
 * sem transformar erro em exceção.
 */
export async function requisicaoCrua(metodo, url) {
  const inicio = performance.now();
  try {
    const resposta = await fetch(url, { method: metodo, headers: { Accept: "application/json" } });
    const texto = await resposta.text();
    const tempo = performance.now() - inicio;
    let json;
    try {
      json = texto ? JSON.parse(texto) : undefined;
    } catch {
      json = undefined;
    }
    return { status: resposta.status, tempo, texto, json, tipo: resposta.headers.get("content-type") || "" };
  } catch {
    return { status: 0, tempo: performance.now() - inicio, texto: "", json: undefined, falhou: true };
  }
}

/** true se a aplicação e o banco respondem */
export async function apiNoAr() {
  try {
    const resposta = await fetch("/actuator/health", { headers: { Accept: "application/json" } });
    if (!resposta.ok) return false;
    const corpo = await resposta.json();
    return corpo?.status === "UP";
  } catch {
    return false;
  }
}
