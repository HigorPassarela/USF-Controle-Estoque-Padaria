// Estoque: saldos com situação, lotes, movimentações e os formulários de entrada, saída FEFO e descarte.
import { api } from "../api.js";
import {
  icone, tabela, htmlCabecalho, htmlCarregando, htmlErro, htmlVazio,
  abrirFormulario, avisar, mensagemDeErro, mostrarResultado, opcoesDe,
} from "../ui.js";
import { escapar, quantidade, moeda, data, dataHora, diasAte, textoDias, hojeIso, normalizar, decimalParaApi } from "../formatos.js";

const ROTULO_SITUACAO = { ok: "OK", baixo: "Baixo", vencido: "Vencido", zerado: "Sem estoque" };

// ---------- Preferência de estoque baixo ----------

export function limiteBaixo() {
  try {
    const v = Number(localStorage.getItem("ce.limiteBaixo"));
    return Number.isFinite(v) && v >= 0 && localStorage.getItem("ce.limiteBaixo") !== null ? v : 5;
  } catch {
    return 5;
  }
}
function salvarLimite(valor) {
  try { localStorage.setItem("ce.limiteBaixo", String(valor)); } catch { /* fica só nesta visita */ }
}

/** Situação do produto: sem estoque, vencido (tem saldo vencido), baixo (disponível até o limite) ou ok */
export function situacaoDe(saldo, limite = limiteBaixo()) {
  if (Number(saldo.saldoTotal) === 0) return "zerado";
  if (Number(saldo.saldoVencido) > 0) return "vencido";
  if (Number(saldo.saldoDisponivel) <= limite) return "baixo";
  return "ok";
}
export const chipSituacao = (s) => `<span class="situacao" data-s="${s}">${ROTULO_SITUACAO[s]}</span>`;

/** Chip de validade do lote: vencido, vence em breve ou neutro */
export function chipValidade(dataValidade) {
  if (!dataValidade) return '<span class="mudo">Sem validade</span>';
  const dias = diasAte(dataValidade);
  const tom = dias < 0 ? "vencido" : dias <= 3 ? "vencido" : dias <= 7 ? "baixo" : "zerado";
  const rotulo = dias < 0 ? "Vencido" : textoDias(dias).replace(/^vence/, "Vence");
  return `<span class="num">${data(dataValidade)}</span><span class="celula-sub"><span class="situacao" data-s="${tom}">${rotulo}</span></span>`;
}

const ordenarPorNome = (lista, campo = "nome") => [...lista].sort((a, b) => normalizar(a[campo]).localeCompare(normalizar(b[campo])));
const decimalParaCampo = (n) => (n === null || n === undefined ? "" : String(Number(n)).replace(".", ","));
const recarregar = () => window.dispatchEvent(new Event("ce:recarregar"));

// ---------- Tela ----------

const ABAS = [
  { id: "saldos", rotulo: "Saldos", icone: "saldo" },
  { id: "lotes", rotulo: "Lotes", icone: "lote" },
  { id: "movimentacoes", rotulo: "Movimentações", icone: "movimento" },
];

export async function renderEstoque(conteudo, ctx) {
  const aba = ABAS.find((a) => a.id === ctx.aba) ? ctx.aba : "saldos";
  const titulo = ABAS.find((a) => a.id === aba).rotulo;
  ctx.definirTopo("Estoque", titulo);
  conteudo.innerHTML = `
    ${htmlCabecalho({
      titulo: "Estoque",
      texto: "Saldo por lote. As saídas consomem primeiro o lote que vence antes, e lotes vencidos ficam fora da baixa automática.",
      acoes: `<button class="btn" type="button" data-acao="saida">${icone("saida")}Registrar saída</button>
              <button class="btn btn-primario" type="button" data-acao="entrada">${icone("entrada")}Registrar entrada</button>`,
    })}
    <nav class="abas" aria-label="Seções do estoque">
      ${ABAS.map((a) => `<a class="aba" href="#/estoque/${a.id}" ${a.id === aba ? 'aria-current="page"' : ""}>${icone(a.icone)}${a.rotulo}</a>`).join("")}
    </nav>
    <section class="quadro" id="area-estoque">${htmlCarregando()}</section>`;

  conteudo.onclick = (e) => {
    const botao = e.target.closest("[data-acao]");
    if (!botao) return;
    const acao = botao.dataset.acao;
    if (acao === "entrada") abrirEntrada({ idProduto: botao.dataset.produto });
    if (acao === "saida") abrirSaida({ idProduto: botao.dataset.produto });
  };

  const area = conteudo.querySelector("#area-estoque");
  if (aba === "saldos") await telaSaldos(area, ctx);
  else if (aba === "lotes") await telaLotes(area, ctx);
  else await telaMovimentacoes(area, ctx);
}

// ---------- Saldos ----------

async function telaSaldos(area, { params, vigente }) {
  let saldos;
  try {
    saldos = await api.get("/api/estoque/saldos");
  } catch (erro) {
    if (vigente()) area.innerHTML = htmlErro(erro);
    return;
  }
  if (!vigente()) return;

  let filtro = ["ok", "baixo", "vencido", "zerado"].includes(params.get("situacao")) ? params.get("situacao") : "todas";
  area.innerHTML = `
    <div class="ferramentas">
      <div class="campo campo-busca">
        <label for="busca-saldo">Buscar produto</label>
        <input id="busca-saldo" class="entrada" type="search" autocomplete="off" placeholder="Nome do produto...">
      </div>
      <div class="campo" style="max-width:190px">
        <label for="limite-baixo">Estoque baixo até</label>
        <input id="limite-baixo" class="entrada" inputmode="decimal" value="${decimalParaCampo(limiteBaixo())}">
      </div>
      <div class="chips" role="group" aria-label="Filtrar por situação" id="chips-situacao"></div>
    </div>
    <div id="lista-saldos"></div>`;
  const lista = area.querySelector("#lista-saldos");
  const busca = area.querySelector("#busca-saldo");
  const campoLimite = area.querySelector("#limite-baixo");
  const chips = area.querySelector("#chips-situacao");

  const desenhar = () => {
    const limite = limiteBaixo();
    const comSituacao = saldos.map((s) => ({ ...s, situacao: situacaoDe(s, limite) }));
    const contagem = { todas: comSituacao.length, ok: 0, baixo: 0, vencido: 0, zerado: 0 };
    comSituacao.forEach((s) => contagem[s.situacao]++);
    chips.innerHTML = [["todas", "Todas"], ["vencido", "Vencido"], ["zerado", "Sem estoque"], ["baixo", "Baixo"], ["ok", "OK"]]
      .map(([id, rotulo]) => `<button class="chip" type="button" data-filtro="${id}" aria-pressed="${filtro === id}">${rotulo} <span class="contagem">${contagem[id]}</span></button>`)
      .join("");
    const termo = normalizar(busca.value.trim());
    const linhas = comSituacao.filter((s) => (filtro === "todas" || s.situacao === filtro) && (!termo || normalizar(s.nomeProduto).includes(termo)));
    if (!saldos.length) {
      lista.innerHTML = htmlVazio({
        icone: "saldo", titulo: "Nenhum produto cadastrado",
        texto: "Cadastre produtos para acompanhar o estoque deles aqui.",
        acao: `<a class="btn btn-primario" href="#/cadastros/produtos">${icone("mais")}Cadastrar produto</a>`,
      });
      return;
    }
    if (!linhas.length) {
      lista.innerHTML = htmlVazio({ icone: "busca", titulo: "Nenhum produto nesta situação" });
      return;
    }
    lista.innerHTML = tabela({
      colunas: [
        { titulo: "Produto", principal: true, html: (s) => `<span class="celula-principal">${escapar(s.nomeProduto)}</span>` },
        { titulo: "Situação", html: (s) => chipSituacao(s.situacao) },
        { titulo: "Disponível", classe: "num", html: (s) => `<strong>${quantidade(s.saldoDisponivel, s.siglaUnidadeMedida)}</strong>` },
        { titulo: "Vencido", classe: "num", html: (s) => Number(s.saldoVencido) > 0 ? `<span class="negativo">${quantidade(s.saldoVencido, s.siglaUnidadeMedida)}</span>` : '<span class="mudo">–</span>' },
        { titulo: "Total", classe: "num", html: (s) => quantidade(s.saldoTotal, s.siglaUnidadeMedida) },
        { titulo: "Próximo vencimento", html: (s) => s.proximoVencimento ? `${data(s.proximoVencimento)}<span class="celula-sub">${textoDias(diasAte(s.proximoVencimento))}</span>` : '<span class="mudo">–</span>' },
      ],
      linhas,
      destaque: (s) => (s.situacao === "vencido" ? "critico" : ""),
      acoes: (s) => `
        <button class="btn btn-p" type="button" data-acao="entrada" data-produto="${s.idProduto}">${icone("entrada")}Entrada</button>
        <button class="btn btn-p" type="button" data-acao="saida" data-produto="${s.idProduto}" ${Number(s.saldoDisponivel) > 0 ? "" : "disabled"}>${icone("saida")}Saída</button>
        <a class="btn btn-p btn-fantasma" href="#/estoque/lotes?produto=${s.idProduto}">Lotes</a>`,
    });
  };

  chips.addEventListener("click", (e) => {
    const chip = e.target.closest("[data-filtro]");
    if (!chip) return;
    filtro = chip.dataset.filtro;
    history.replaceState(null, "", filtro === "todas" ? "#/estoque/saldos" : `#/estoque/saldos?situacao=${filtro}`);
    desenhar();
  });
  busca.addEventListener("input", desenhar);
  campoLimite.addEventListener("change", () => {
    const v = Number(decimalParaApi(campoLimite.value));
    if (Number.isFinite(v) && v >= 0) {
      salvarLimite(v);
      desenhar();
    } else {
      campoLimite.value = decimalParaCampo(limiteBaixo());
    }
  });
  desenhar();
}

// ---------- Lotes ----------

async function telaLotes(area, { params, vigente }) {
  let produtos;
  try {
    produtos = ordenarPorNome(await api.get("/api/produtos"));
  } catch (erro) {
    if (vigente()) area.innerHTML = htmlErro(erro);
    return;
  }
  if (!vigente()) return;
  const estado = {
    produto: params.get("produto") || "",
    comSaldo: params.get("comSaldo") !== "0",
    vencidos: params.get("vencidos") === "1",
    vencendo: params.get("vencendo") === "1",
  };
  area.innerHTML = `
    <div class="ferramentas">
      <div class="campo">
        <label for="filtro-produto">Produto</label>
        <select id="filtro-produto" class="entrada">
          <option value="">Todos os produtos</option>
          ${produtos.map((p) => `<option value="${p.idProduto}" ${String(p.idProduto) === estado.produto ? "selected" : ""}>${escapar(p.nome)}</option>`).join("")}
        </select>
      </div>
      <label class="alternador"><input type="checkbox" id="filtro-saldo" ${estado.comSaldo ? "checked" : ""}> Só com saldo</label>
      <label class="alternador"><input type="checkbox" id="filtro-vencidos" ${estado.vencidos ? "checked" : ""}> Só vencidos</label>
      <label class="alternador"><input type="checkbox" id="filtro-vencendo" ${estado.vencendo ? "checked" : ""}> Vencendo em 7 dias</label>
      <span class="espaco"></span>
      <span class="mudo" id="contagem-lotes" role="status"></span>
    </div>
    <div id="lista-lotes">${htmlCarregando()}</div>`;
  const lista = area.querySelector("#lista-lotes");
  const contagem = area.querySelector("#contagem-lotes");
  let lotes = [];

  const atualizarEndereco = () => {
    const q = new URLSearchParams();
    if (estado.produto) q.set("produto", estado.produto);
    if (!estado.comSaldo) q.set("comSaldo", "0");
    if (estado.vencidos) q.set("vencidos", "1");
    if (estado.vencendo) q.set("vencendo", "1");
    const qs = q.toString();
    history.replaceState(null, "", `#/estoque/lotes${qs ? `?${qs}` : ""}`);
  };

  const desenhar = () => {
    let linhas = lotes;
    if (estado.vencendo) linhas = linhas.filter((l) => l.dataValidade && !l.vencido && diasAte(l.dataValidade) <= 7);
    contagem.textContent = `${linhas.length} ${linhas.length === 1 ? "lote" : "lotes"}`;
    if (!linhas.length) {
      lista.innerHTML = htmlVazio({
        icone: "lote", titulo: "Nenhum lote encontrado",
        texto: lotes.length ? "Ajuste os filtros para ver outros lotes." : "Os lotes aparecem aqui quando você registra uma entrada de estoque.",
        acao: lotes.length ? "" : `<button class="btn btn-primario" type="button" data-acao="entrada">${icone("entrada")}Registrar entrada</button>`,
      });
      return;
    }
    lista.innerHTML = tabela({
      colunas: [
        { titulo: "Produto", principal: true, html: (l) => `<span class="celula-principal">${escapar(l.nomeProduto)}</span><span class="celula-sub">Lote #${l.idLote}${l.numeroLote ? ` · ${escapar(l.numeroLote)}` : ""}</span>` },
        { titulo: "Validade", html: (l) => chipValidade(l.dataValidade) },
        { titulo: "Saldo", classe: "num", html: (l) => `<strong>${quantidade(l.quantidadeAtual, l.siglaUnidadeMedida)}</strong><span class="celula-sub">de ${quantidade(l.quantidadeInicial)}</span>` },
        { titulo: "Fornecedor", html: (l) => l.nomeFornecedor ? escapar(l.nomeFornecedor) : '<span class="mudo">–</span>' },
        { titulo: "Custo", classe: "num", html: (l) => (l.custoUnitario !== null ? moeda(l.custoUnitario) : '<span class="mudo">–</span>') },
        { titulo: "Entrada", html: (l) => `<span class="num">${dataHora(l.dataEntrada)}</span>` },
      ],
      linhas,
      destaque: (l) => (l.vencido && Number(l.quantidadeAtual) > 0 ? "critico" : ""),
      acoes: (l) => `
        ${Number(l.quantidadeAtual) > 0 ? `<button class="btn btn-p ${l.vencido ? "btn-perigo" : ""}" type="button" data-lote="${l.idLote}">${icone("descarte")}Descartar</button>` : ""}
        <a class="btn btn-p btn-fantasma" href="#/estoque/movimentacoes?lote=${l.idLote}">Histórico</a>`,
    });
  };

  const carregar = async () => {
    lista.innerHTML = htmlCarregando();
    try {
      lotes = await api.get("/api/estoque/lotes", {
        produto: estado.produto,
        somenteComSaldo: estado.comSaldo ? "true" : "",
        somenteVencidos: estado.vencidos ? "true" : "",
      });
      if (vigente()) desenhar();
    } catch (erro) {
      if (vigente()) lista.innerHTML = htmlErro(erro);
    }
  };

  area.querySelector("#filtro-produto").addEventListener("change", (e) => { estado.produto = e.target.value; atualizarEndereco(); carregar(); });
  area.querySelector("#filtro-saldo").addEventListener("change", (e) => { estado.comSaldo = e.target.checked; atualizarEndereco(); carregar(); });
  area.querySelector("#filtro-vencidos").addEventListener("change", (e) => { estado.vencidos = e.target.checked; atualizarEndereco(); carregar(); });
  area.querySelector("#filtro-vencendo").addEventListener("change", (e) => { estado.vencendo = e.target.checked; atualizarEndereco(); desenhar(); });
  lista.addEventListener("click", (e) => {
    const botao = e.target.closest("[data-lote]");
    if (botao) abrirDescarte(lotes.find((l) => String(l.idLote) === botao.dataset.lote));
  });
  await carregar();
}

// ---------- Movimentações ----------

async function telaMovimentacoes(area, { params, vigente }) {
  let produtos;
  let lotes;
  try {
    [produtos, lotes] = await Promise.all([api.get("/api/produtos"), api.get("/api/estoque/lotes")]);
  } catch (erro) {
    if (vigente()) area.innerHTML = htmlErro(erro);
    return;
  }
  if (!vigente()) return;
  produtos = ordenarPorNome(produtos);
  const estado = { produto: params.get("produto") || "", lote: params.get("lote") || "", de: params.get("de") || "", ate: params.get("ate") || "" };
  if (estado.lote && !estado.produto) {
    const lote = lotes.find((l) => String(l.idLote) === estado.lote);
    if (lote) estado.produto = String(lote.idProduto);
  }

  area.innerHTML = `
    <div class="ferramentas">
      <div class="campo">
        <label for="mov-produto">Produto</label>
        <select id="mov-produto" class="entrada"><option value="">Todos os produtos</option>
          ${produtos.map((p) => `<option value="${p.idProduto}" ${String(p.idProduto) === estado.produto ? "selected" : ""}>${escapar(p.nome)}</option>`).join("")}
        </select>
      </div>
      <div class="campo">
        <label for="mov-lote">Lote</label>
        <select id="mov-lote" class="entrada"></select>
      </div>
      <div class="campo" style="max-width:180px">
        <label for="mov-de">De</label>
        <input id="mov-de" class="entrada" type="date" value="${escapar(estado.de)}">
      </div>
      <div class="campo" style="max-width:180px">
        <label for="mov-ate">Até</label>
        <input id="mov-ate" class="entrada" type="date" value="${escapar(estado.ate)}">
      </div>
      <button class="btn btn-fantasma" type="button" id="mov-limpar">Limpar filtros</button>
      <span class="espaco"></span>
      <span class="mudo" id="contagem-mov" role="status"></span>
    </div>
    <div id="lista-mov">${htmlCarregando()}</div>`;

  const selLote = area.querySelector("#mov-lote");
  const lista = area.querySelector("#lista-mov");
  const contagem = area.querySelector("#contagem-mov");

  const preencherLotes = () => {
    const doProduto = estado.produto ? lotes.filter((l) => String(l.idProduto) === estado.produto) : lotes;
    selLote.innerHTML = `<option value="">Todos os lotes</option>` + doProduto
      .map((l) => `<option value="${l.idLote}">#${l.idLote}${l.numeroLote ? ` ${escapar(l.numeroLote)}` : ""} · ${escapar(l.nomeProduto)}</option>`).join("");
    if (!doProduto.some((l) => String(l.idLote) === estado.lote)) estado.lote = "";
    selLote.value = estado.lote;
  };

  const carregar = async () => {
    const q = new URLSearchParams(Object.entries(estado).filter(([, v]) => v));
    history.replaceState(null, "", `#/estoque/movimentacoes${q.toString() ? `?${q}` : ""}`);
    lista.innerHTML = htmlCarregando();
    let movs;
    try {
      movs = await api.get("/api/estoque/movimentacoes", estado);
    } catch (erro) {
      if (vigente()) lista.innerHTML = htmlErro(erro);
      return;
    }
    if (!vigente()) return;
    const sigla = Object.fromEntries(produtos.map((p) => [p.idProduto, p.nomeUnidadeMedida]));
    contagem.textContent = `${movs.length} ${movs.length === 1 ? "movimentação" : "movimentações"}`;
    if (!movs.length) {
      lista.innerHTML = htmlVazio({ icone: "movimento", titulo: "Nenhuma movimentação", texto: "Nada foi registrado com estes filtros." });
      return;
    }
    lista.innerHTML = tabela({
      colunas: [
        { titulo: "Data e hora", principal: true, html: (m) => `<span class="celula-principal num">${dataHora(m.dataMovimentacao)}</span><span class="celula-sub">${escapar(m.descricaoTipoMovimentacao)}</span>` },
        { titulo: "Natureza", html: (m) => `<span class="situacao" data-s="${m.natureza === "ENTRADA" ? "entrada" : "saida"}">${m.natureza === "ENTRADA" ? "Entrada" : "Saída"}</span>` },
        { titulo: "Quantidade", classe: "num", html: (m) => `<strong class="${m.natureza === "ENTRADA" ? "positivo" : "negativo"}">${m.natureza === "ENTRADA" ? "+" : "−"}${quantidade(m.quantidade, sigla[m.idProduto])}</strong>` },
        { titulo: "Produto", html: (m) => escapar(m.nomeProduto) },
        { titulo: "Lote", html: (m) => `#${m.idLote}${m.numeroLote ? ` <span class="mudo">${escapar(m.numeroLote)}</span>` : ""}` },
        { titulo: "Observação", html: (m) => (m.observacao ? escapar(m.observacao) : '<span class="mudo">–</span>') },
      ],
      linhas: movs,
    });
  };

  area.querySelector("#mov-produto").addEventListener("change", (e) => { estado.produto = e.target.value; preencherLotes(); carregar(); });
  selLote.addEventListener("change", (e) => { estado.lote = e.target.value; carregar(); });
  area.querySelector("#mov-de").addEventListener("change", (e) => { estado.de = e.target.value; carregar(); });
  area.querySelector("#mov-ate").addEventListener("change", (e) => { estado.ate = e.target.value; carregar(); });
  area.querySelector("#mov-limpar").addEventListener("click", () => {
    Object.assign(estado, { produto: "", lote: "", de: "", ate: "" });
    area.querySelector("#mov-produto").value = "";
    area.querySelector("#mov-de").value = "";
    area.querySelector("#mov-ate").value = "";
    preencherLotes();
    carregar();
  });
  preencherLotes();
  await carregar();
}

// ---------- Formulários de operação ----------

async function carregarBase(natureza) {
  const [produtos, tipos, fornecedores] = await Promise.all([
    api.get("/api/produtos"),
    api.get("/api/tipos-movimentacao"),
    natureza === "ENTRADA" ? api.get("/api/fornecedores") : Promise.resolve([]),
  ]);
  return {
    produtos: ordenarPorNome(produtos),
    tipos: ordenarPorNome(tipos.filter((t) => t.tipo === natureza), "descricao"),
    fornecedores: ordenarPorNome(fornecedores),
  };
}

function tipoPadrao(tipos, preferido) {
  return (tipos.find((t) => normalizar(t.descricao).startsWith(normalizar(preferido))) || tipos[0])?.idTipoMovimentacao ?? "";
}

/** Formulário de entrada de estoque */
export async function abrirEntrada({ idProduto } = {}) {
  let base;
  try {
    base = await carregarBase("ENTRADA");
  } catch (erro) {
    avisar(mensagemDeErro(erro), "erro");
    return;
  }
  if (!base.produtos.length) {
    avisar("Cadastre um produto antes de registrar uma entrada.", "info");
    return;
  }
  const porId = Object.fromEntries(base.produtos.map((p) => [String(p.idProduto), p]));
  const vinculosCache = {};

  const atualizarCusto = async (ctrl) => {
    const idP = ctrl.valor("produto");
    const idF = ctrl.valor("fornecedor");
    ctrl.placeholder("custoUnitario", "0,00");
    ctrl.ajuda("custoUnitario", "Opcional.");
    if (!idP || !idF) return;
    try {
      vinculosCache[idP] ??= await api.get(`/api/produtos/${idP}/fornecedores`);
    } catch {
      return;
    }
    const vinculo = vinculosCache[idP].find((v) => String(v.idFornecedor) === idF);
    if (vinculo) {
      ctrl.placeholder("custoUnitario", decimalParaCampo(vinculo.custoUnitario));
      ctrl.ajuda("custoUnitario", `Custo combinado com este fornecedor: <strong>${moeda(vinculo.custoUnitario)}</strong>. É usado se o campo ficar em branco.`);
    }
  };
  const aoTrocarProduto = (ctrl) => {
    const p = porId[ctrl.valor("produto")];
    ctrl.unidade("quantidade", p?.nomeUnidadeMedida || "");
    if (p?.idFornecedor && !ctrl.valor("fornecedor")) ctrl.definir("fornecedor", p.idFornecedor);
  };

  const resultado = await abrirFormulario({
    titulo: "Registrar entrada",
    descricao: "Cada entrada cria um lote. Informe a validade para o sistema controlar o vencimento.",
    campos: [
      { nome: "produto", rotulo: "Produto", tipo: "selecao", obrigatorio: true, largo: true, opcoes: opcoesDe(base.produtos, "idProduto", "nome") },
      { nome: "quantidade", rotulo: "Quantidade", tipo: "numero", obrigatorio: true, unidade: "", ajuda: "Até 3 casas decimais. Ex.: 2,5" },
      { nome: "tipoMovimentacao", rotulo: "Tipo de entrada", tipo: "selecao", obrigatorio: true, opcoes: opcoesDe(base.tipos, "idTipoMovimentacao", "descricao") },
      { nome: "dataValidade", rotulo: "Validade", tipo: "data", min: hojeIso(), ajuda: "Opcional. Sem validade, o lote nunca vence." },
      { nome: "numeroLote", rotulo: "Número do lote", tipo: "texto", max: 50, placeholder: "Como está na embalagem" },
      { nome: "fornecedor", rotulo: "Fornecedor", tipo: "selecao", opcoes: opcoesDe(base.fornecedores, "idFornecedor", "nome") },
      { nome: "custoUnitario", rotulo: "Custo unitário", tipo: "numero", unidade: "R$", ajuda: "Opcional." },
      { nome: "observacao", rotulo: "Observação", tipo: "area", max: 255, largo: true, placeholder: "Ex.: NF 1234" },
    ],
    valores: { produto: idProduto || "", tipoMovimentacao: tipoPadrao(base.tipos, "Compra") },
    textoEnviar: "Registrar entrada",
    aoAbrir: (ctrl) => { aoTrocarProduto(ctrl); atualizarCusto(ctrl); },
    aoMudar: (nome, ctrl, digitando) => {
      if (digitando) return;
      if (nome === "produto") aoTrocarProduto(ctrl);
      if (nome === "produto" || nome === "fornecedor") atualizarCusto(ctrl);
    },
    aoEnviar: (dados) => api.criar("/api/estoque/entradas", dados),
  });
  if (resultado) {
    const l = resultado.lote;
    avisar(`Entrada registrada no lote #${l.idLote}${l.numeroLote ? ` (${l.numeroLote})` : ""}. Saldo de ${l.nomeProduto}: ${quantidade(resultado.saldoProduto, l.siglaUnidadeMedida)}.`, "ok", 7000);
    recarregar();
  }
}

/**
 * Prévia da baixa FEFO, pela mesma regra da API: lotes com validade antes de hoje ficam de fora;
 * o que vence antes sai primeiro, lotes sem validade por último, empate pelo lote mais antigo.
 */
export function calcularFefo(lotes, pedido, hoje = hojeIso()) {
  const comSaldo = lotes.filter((l) => Number(l.quantidadeAtual) > 0);
  const vencidos = comSaldo.filter((l) => l.dataValidade && l.dataValidade < hoje);
  const validos = comSaldo
    .filter((l) => !l.dataValidade || l.dataValidade >= hoje)
    .sort((a, b) => {
      if (a.dataValidade !== b.dataValidade) {
        if (!a.dataValidade) return 1;
        if (!b.dataValidade) return -1;
        return a.dataValidade < b.dataValidade ? -1 : 1;
      }
      return a.idLote - b.idLote;
    });
  const disponivel = validos.reduce((s, l) => s + Number(l.quantidadeAtual), 0);
  let falta = pedido;
  const consumo = validos.map((l) => {
    const baixa = Math.max(0, Math.min(Number(l.quantidadeAtual), falta));
    falta = Math.round((falta - baixa) * 1000) / 1000;
    return { lote: l, baixa };
  });
  return { consumo, vencidos, disponivel, falta: Math.max(0, falta) };
}

function htmlPrevia(lotes, pedido, sigla) {
  if (!lotes) return `<p class="mudo">Escolha o produto para ver de quais lotes a saída vai sair.</p>`;
  const r = calcularFefo(lotes, pedido);
  if (!r.consumo.length) {
    return `<div class="aviso" data-tom="alerta">${icone("alerta")}<span>Este produto não tem saldo disponível${r.vencidos.length ? ": só há lotes vencidos, que saem pelo descarte na lista de lotes" : ""}.</span></div>`;
  }
  const linhas = r.consumo.map(({ lote, baixa }) => {
    const pct = Math.round((baixa / Number(lote.quantidadeAtual)) * 100);
    return `<div class="previa-lote" ${baixa === 0 ? "data-ignorado" : ""}>
        <span>Lote #${lote.idLote}${lote.numeroLote ? ` · ${escapar(lote.numeroLote)}` : ""} <span class="mudo">· ${lote.dataValidade ? `vence ${data(lote.dataValidade)}` : "sem validade"} · tem ${quantidade(lote.quantidadeAtual, sigla)}</span></span>
        <strong class="num">${baixa > 0 ? `sai ${quantidade(baixa, sigla)}` : "não usado"}</strong>
        <div class="previa-barra"><span style="width:${pct}%"></span></div>
      </div>`;
  }).join("");
  const vencidos = r.vencidos.length
    ? `<p class="mudo" style="font-size:var(--t-1)">${r.vencidos.length} ${r.vencidos.length === 1 ? "lote vencido fica" : "lotes vencidos ficam"} de fora (${quantidade(r.vencidos.reduce((s, l) => s + Number(l.quantidadeAtual), 0), sigla)}). Para tirá-los do estoque, use Descartar na lista de lotes.</p>`
    : "";
  const resumo = pedido > 0 && r.falta > 0
    ? `<div class="aviso" data-tom="critico">${icone("alerta")}<span>Só há <strong>${quantidade(r.disponivel, sigla)}</strong> disponíveis. Faltam ${quantidade(r.falta, sigla)}.</span></div>`
    : `<p class="mudo" style="font-size:var(--t-1)">Disponível: ${quantidade(r.disponivel, sigla)}.</p>`;
  return `<h3>De onde vai sair</h3>${linhas}${resumo}${vencidos}`;
}

/** Formulário de saída com prévia FEFO */
export async function abrirSaida({ idProduto } = {}) {
  let base;
  try {
    base = await carregarBase("SAIDA");
  } catch (erro) {
    avisar(mensagemDeErro(erro), "erro");
    return;
  }
  if (!base.produtos.length) {
    avisar("Cadastre um produto antes de registrar uma saída.", "info");
    return;
  }
  const porId = Object.fromEntries(base.produtos.map((p) => [String(p.idProduto), p]));
  let lotesDoProduto = null;
  let carregando = 0;

  const atualizarPrevia = (ctrl) => {
    const sigla = porId[ctrl.valor("produto")]?.nomeUnidadeMedida || "";
    const pedido = Number(decimalParaApi(ctrl.valor("quantidade"))) || 0;
    ctrl.extra.querySelector(".previa").innerHTML = htmlPrevia(lotesDoProduto, pedido, sigla);
    const r = lotesDoProduto ? calcularFefo(lotesDoProduto, pedido) : null;
    ctrl.habilitarEnvio(Boolean(r && pedido > 0 && r.falta === 0));
  };
  const carregarLotes = async (ctrl) => {
    const idP = ctrl.valor("produto");
    ctrl.unidade("quantidade", porId[idP]?.nomeUnidadeMedida || "");
    lotesDoProduto = null;
    atualizarPrevia(ctrl);
    if (!idP) return;
    const minha = ++carregando;
    try {
      const lotes = await api.get("/api/estoque/lotes", { produto: idP, somenteComSaldo: "true" });
      if (minha === carregando) lotesDoProduto = lotes;
    } catch (erro) {
      ctrl.erroGeral(mensagemDeErro(erro));
    }
    if (minha === carregando) atualizarPrevia(ctrl);
  };

  const resultado = await abrirFormulario({
    titulo: "Registrar saída",
    descricao: "A baixa sai primeiro do lote que vence antes. Lotes vencidos não entram.",
    campos: [
      { nome: "produto", rotulo: "Produto", tipo: "selecao", obrigatorio: true, largo: true, opcoes: opcoesDe(base.produtos, "idProduto", "nome") },
      { nome: "quantidade", rotulo: "Quantidade", tipo: "numero", obrigatorio: true, unidade: "" },
      { nome: "tipoMovimentacao", rotulo: "Tipo de saída", tipo: "selecao", obrigatorio: true, opcoes: opcoesDe(base.tipos, "idTipoMovimentacao", "descricao") },
      { nome: "observacao", rotulo: "Observação", tipo: "area", max: 255, largo: true, placeholder: "Ex.: Fornada da manhã" },
    ],
    valores: { produto: idProduto || "", tipoMovimentacao: tipoPadrao(base.tipos, "Consumo") },
    textoEnviar: "Registrar saída",
    extra: (container) => { container.innerHTML = `<div class="previa" aria-live="polite"></div>`; },
    aoAbrir: (ctrl) => carregarLotes(ctrl),
    aoMudar: (nome, ctrl) => {
      if (nome === "produto") carregarLotes(ctrl);
      if (nome === "quantidade") atualizarPrevia(ctrl);
    },
    aoEnviar: (dados) => api.criar("/api/estoque/saidas", dados),
  });
  if (resultado) {
    const p = porId[String(resultado.idProduto)];
    const sigla = p?.nomeUnidadeMedida || "";
    mostrarResultado({
      titulo: "Saída registrada",
      descricao: `${escapar(p?.nome || "")}: saíram ${quantidade(resultado.quantidadeBaixada, sigla)}.`,
      corpo: `<ul class="lista">${resultado.movimentacoes.map((m) => `
          <li><span class="lista-texto"><strong>Lote #${m.idLote}${m.numeroLote ? ` · ${escapar(m.numeroLote)}` : ""}</strong><span>${escapar(m.descricaoTipoMovimentacao)}</span></span>
          <span class="lista-valor"><strong class="negativo">−${quantidade(m.quantidade, sigla)}</strong></span></li>`).join("")}</ul>
        <div class="aviso" data-tom="ok">${icone("ok")}<span>Novo saldo: <strong>${quantidade(resultado.saldoProduto, sigla)}</strong></span></div>`,
    });
    recarregar();
  }
}

/** Saída de um lote específico (perda, descarte), inclusive vencido */
export async function abrirDescarte(lote) {
  let tipos;
  try {
    tipos = ordenarPorNome((await api.get("/api/tipos-movimentacao")).filter((t) => t.tipo === "SAIDA"), "descricao");
  } catch (erro) {
    avisar(mensagemDeErro(erro), "erro");
    return;
  }
  const sigla = lote.siglaUnidadeMedida;
  const resultado = await abrirFormulario({
    titulo: `Descartar do lote #${lote.idLote}`,
    descricao: `${escapar(lote.nomeProduto)}${lote.numeroLote ? ` · lote ${escapar(lote.numeroLote)}` : ""} · ${lote.dataValidade ? `validade ${data(lote.dataValidade)}` : "sem validade"} · saldo ${quantidade(lote.quantidadeAtual, sigla)}`,
    tamanho: "pequeno",
    campos: [
      { nome: "quantidade", rotulo: "Quantidade", tipo: "numero", obrigatorio: true, unidade: sigla, ajuda: `No máximo ${quantidade(lote.quantidadeAtual, sigla)}.` },
      { nome: "tipoMovimentacao", rotulo: "Motivo", tipo: "selecao", obrigatorio: true, opcoes: opcoesDe(tipos, "idTipoMovimentacao", "descricao") },
      { nome: "observacao", rotulo: "Observação", tipo: "area", max: 255, placeholder: "Ex.: Embalagem violada" },
    ],
    valores: { quantidade: decimalParaCampo(lote.quantidadeAtual), tipoMovimentacao: tipoPadrao(tipos, "Perda") },
    textoEnviar: "Descartar",
    extra: lote.vencido ? (c) => { c.innerHTML = `<div class="aviso" data-tom="critico">${icone("alerta")}<span>Este lote está vencido e não sai pela baixa automática. Registre aqui o descarte.</span></div>`; } : null,
    aoEnviar: (dados) => api.criar(`/api/estoque/lotes/${lote.idLote}/saidas`, dados),
  });
  if (resultado) {
    avisar(`Descarte registrado. Saldo de ${lote.nomeProduto}: ${quantidade(resultado.saldoProduto, sigla)}.`, "ok", 7000);
    recarregar();
  }
}
