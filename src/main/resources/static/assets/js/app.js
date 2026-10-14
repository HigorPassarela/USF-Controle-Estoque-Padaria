// Casca da interface: menu, roteamento por hash, tema e indicador de conexão com a API.
import { apiNoAr } from "./api.js";
import { icone, htmlErro } from "./ui.js";
import { renderPainel } from "./views/painel.js";
import { renderEstoque } from "./views/estoque.js";
import { renderCadastro, RECURSOS, GRUPOS_CADASTRO } from "./views/cadastros.js";
import { renderExplorador } from "./views/explorador.js";

const conteudo = document.getElementById("conteudo");
const menuLinks = document.getElementById("menu-links");

// ---------- Menu ----------

function montarMenu() {
  const link = (href, nome, rotulo) =>
    `<a class="menu-link" href="${href}" data-rota="${href}">${icone(nome)}<span>${rotulo}</span></a>`;
  const cadastros = GRUPOS_CADASTRO.map((g) => `
    <div class="menu-grupo">
      <span class="menu-titulo">${g.titulo}</span>
      ${g.recursos.map((id) => link(`#/cadastros/${id}`, RECURSOS[id].icone, RECURSOS[id].titulo)).join("")}
    </div>`).join("");
  menuLinks.innerHTML = `
    <div class="menu-grupo">
      <span class="menu-titulo">Visão geral</span>
      ${link("#/painel", "painel", "Painel")}
    </div>
    <div class="menu-grupo">
      <span class="menu-titulo">Estoque</span>
      ${link("#/estoque/saldos", "saldo", "Saldos")}
      ${link("#/estoque/lotes", "lote", "Lotes")}
      ${link("#/estoque/movimentacoes", "movimento", "Movimentações")}
    </div>
    ${cadastros}
    <div class="menu-grupo">
      <span class="menu-titulo">Ferramentas</span>
      ${link("#/explorador", "explorador", "Explorador da API")}
      <a class="menu-link" href="/swagger-ui.html" target="_blank" rel="noopener">${icone("documento")}<span>Swagger</span>${icone("externo", "externo")}</a>
      <a class="menu-link" href="/actuator/health" target="_blank" rel="noopener">${icone("saude")}<span>Saúde da aplicação</span>${icone("externo", "externo")}</a>
    </div>`;
}

function marcarMenu(caminho) {
  menuLinks.querySelectorAll("[data-rota]").forEach((a) => {
    const rota = a.dataset.rota.slice(1);
    const ativo = caminho === rota || (rota !== "/painel" && caminho.startsWith(rota + "/"));
    if (ativo) a.setAttribute("aria-current", "page");
    else a.removeAttribute("aria-current");
  });
}

// Gaveta do menu no celular
const botaoMenu = document.getElementById("botao-menu");
function abrirMenu(abrir) {
  if (abrir) document.body.dataset.menuAberto = "";
  else delete document.body.dataset.menuAberto;
  botaoMenu.setAttribute("aria-expanded", String(abrir));
  botaoMenu.setAttribute("aria-label", abrir ? "Fechar menu" : "Abrir menu");
  if (abrir) menuLinks.querySelector("a")?.focus();
}
botaoMenu.addEventListener("click", () => abrirMenu(!("menuAberto" in document.body.dataset)));
document.getElementById("veu-menu").addEventListener("click", () => abrirMenu(false));
menuLinks.addEventListener("click", (e) => { if (e.target.closest("a")) abrirMenu(false); });
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape" && "menuAberto" in document.body.dataset) {
    abrirMenu(false);
    botaoMenu.focus();
  }
});

// ---------- Tema ----------

const botaoTema = document.getElementById("botao-tema");
function temaAtual() {
  const explicito = document.documentElement.dataset.theme;
  if (explicito) return explicito;
  return matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}
function atualizarBotaoTema() {
  const escuro = temaAtual() === "dark";
  botaoTema.innerHTML = `${icone(escuro ? "sol" : "lua")}<span>${escuro ? "Tema claro" : "Tema escuro"}</span>`;
  botaoTema.setAttribute("aria-label", escuro ? "Mudar para o tema claro" : "Mudar para o tema escuro");
}
botaoTema.addEventListener("click", () => {
  const novo = temaAtual() === "dark" ? "light" : "dark";
  document.documentElement.dataset.theme = novo;
  try { localStorage.setItem("ce.tema", novo); } catch { /* preferência só nesta sessão */ }
  atualizarBotaoTema();
  window.dispatchEvent(new Event("ce:tema"));
});
matchMedia("(prefers-color-scheme: dark)").addEventListener("change", () => {
  atualizarBotaoTema();
  window.dispatchEvent(new Event("ce:tema"));
});

// ---------- Status da API ----------

const statusApi = document.getElementById("status-api");
async function verificarApi() {
  const ok = await apiNoAr();
  statusApi.dataset.estado = ok ? "ok" : "erro";
  statusApi.textContent = ok ? "API e banco conectados" : "API sem resposta";
}

// ---------- Topo ----------

export function definirTopo(secao, titulo) {
  document.getElementById("topo-secao").textContent = secao;
  document.getElementById("topo-titulo").textContent = titulo;
  document.title = `${titulo} · Controle de Estoque`;
}

// ---------- Rotas ----------

/** "#/estoque/lotes?vencidos=1" -> { caminho: "/estoque/lotes", params } */
export function rotaAtual() {
  const bruto = location.hash.replace(/^#/, "") || "/painel";
  const [caminho, query = ""] = bruto.split("?");
  return { caminho: caminho || "/painel", params: new URLSearchParams(query) };
}

/** Troca a rota (gera nova entrada no histórico) */
export function irPara(hash) {
  if (location.hash === hash) renderizar();
  else location.hash = hash;
}

let renderizacao = 0;
async function renderizar() {
  const { caminho, params } = rotaAtual();
  const minha = ++renderizacao;
  marcarMenu(caminho);
  const partes = caminho.split("/").filter(Boolean);
  // Uma tela que termina de carregar depois de o usuário já ter navegado não deve desenhar nada
  const ctx = { params, definirTopo, irPara, vigente: () => minha === renderizacao };
  try {
    if (partes[0] === "painel") await renderPainel(conteudo, ctx);
    else if (partes[0] === "estoque") await renderEstoque(conteudo, { ...ctx, aba: partes[1] || "saldos" });
    else if (partes[0] === "cadastros" && RECURSOS[partes[1]]) await renderCadastro(conteudo, { ...ctx, recurso: partes[1] });
    else if (partes[0] === "explorador") await renderExplorador(conteudo, ctx);
    else {
      location.replace("#/painel");
      return;
    }
  } catch (erro) {
    if (minha !== renderizacao) return;
    conteudo.innerHTML = htmlErro(erro);
  }
}

conteudo.addEventListener("click", (e) => {
  if (e.target.closest('[data-acao="tentar-de-novo"]')) {
    renderizar();
    verificarApi();
  }
});

window.addEventListener("hashchange", () => {
  renderizar();
  conteudo.focus({ preventScroll: true });
  window.scrollTo({ top: 0 });
});
window.addEventListener("ce:recarregar", renderizar);

montarMenu();
atualizarBotaoTema();
verificarApi();
setInterval(verificarApi, 30000);
renderizar();
