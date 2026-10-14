// Formatação de números, dinheiro e datas no padrão brasileiro, e utilidades de texto.

const fmtQuantidade = new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 3 });
const fmtMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
const fmtDataHora = new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "short" });

/** Quantidade com até 3 casas, opcionalmente com a unidade: 2,5 kg */
export function quantidade(valor, sigla) {
  if (valor === null || valor === undefined || valor === "") return "–";
  const texto = fmtQuantidade.format(Number(valor));
  return sigla ? `${texto} ${sigla}` : texto;
}

export function moeda(valor) {
  if (valor === null || valor === undefined || valor === "") return "–";
  return fmtMoeda.format(Number(valor));
}

/** "2026-10-16" -> "16/10/2026" (sem passar por Date, para não sofrer com fuso) */
export function data(iso) {
  if (!iso) return "–";
  const [a, m, d] = String(iso).slice(0, 10).split("-");
  return `${d}/${m}/${a}`;
}

/** Instante em UTC (ISO) no horário local do navegador */
export function dataHora(iso) {
  if (!iso) return "–";
  return fmtDataHora.format(new Date(iso));
}

/** Data local de hoje no formato AAAA-MM-DD */
export function hojeIso() {
  const agora = new Date();
  const m = String(agora.getMonth() + 1).padStart(2, "0");
  const d = String(agora.getDate()).padStart(2, "0");
  return `${agora.getFullYear()}-${m}-${d}`;
}

/** Dias corridos de hoje até a data (negativo se já passou) */
export function diasAte(iso) {
  if (!iso) return null;
  const [a, m, d] = String(iso).slice(0, 10).split("-").map(Number);
  const alvo = Date.UTC(a, m - 1, d);
  const [ha, hm, hd] = hojeIso().split("-").map(Number);
  return Math.round((alvo - Date.UTC(ha, hm - 1, hd)) / 86400000);
}

export function textoDias(dias) {
  if (dias === null) return "sem validade";
  if (dias < 0) return dias === -1 ? "venceu ontem" : `venceu há ${-dias} dias`;
  if (dias === 0) return "vence hoje";
  if (dias === 1) return "vence amanhã";
  return `vence em ${dias} dias`;
}

/** Aceita "2,5" ou "2.5" e devolve "2.5" (o formato que a API espera) */
export function decimalParaApi(valor) {
  if (valor === null || valor === undefined) return valor;
  return String(valor).trim().replace(/\s/g, "").replace(",", ".");
}

export function escapar(valor) {
  if (valor === null || valor === undefined) return "";
  return String(valor)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

/** Comparação de texto sem acento e sem diferenciar maiúsculas */
export function normalizar(texto) {
  return String(texto ?? "").normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase();
}
