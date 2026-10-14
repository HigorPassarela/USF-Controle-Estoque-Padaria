// Componentes reutilizáveis: ícones, avisos, janelas (formulário e confirmação), tabela e estados.
import { ApiIndisponivel, ErroApi } from "./api.js";
import { decimalParaApi, escapar } from "./formatos.js";

const ICONES = {
  painel: '<rect x="3" y="3" width="7" height="9" rx="1.5"/><rect x="14" y="3" width="7" height="5" rx="1.5"/><rect x="14" y="12" width="7" height="9" rx="1.5"/><rect x="3" y="16" width="7" height="5" rx="1.5"/>',
  saldo: '<path d="M21 8 12 3 3 8v8l9 5 9-5z"/><path d="m3 8 9 5 9-5M12 13v8"/>',
  lote: '<path d="m12 3 9 4.5-9 4.5-9-4.5z"/><path d="m3 12 9 4.5 9-4.5M3 16.5 12 21l9-4.5"/>',
  movimento: '<path d="M7 4v14M3 14l4 4 4-4M17 20V6M13 10l4-4 4 4"/>',
  produto: '<path d="M20.6 13.4 13.4 20.6a2 2 0 0 1-2.8 0L3 13V3h10l7.6 7.6a2 2 0 0 1 0 2.8z"/><circle cx="7.5" cy="7.5" r="1.5"/>',
  categoria: '<path d="M4 5h6v6H4zM14 5h6v6h-6zM4 15h6v6H4zM14 15h6v6h-6z"/>',
  tipo: '<circle cx="7" cy="7" r="3.5"/><path d="M14 4h6v6h-6zM4 14l3.5 6h-7z" transform="translate(3 0)"/>',
  regua: '<path d="M3 17 17 3l4 4L7 21z"/><path d="m7 13 2 2M10 10l2 2M13 7l2 2"/>',
  movimentacao: '<path d="M4 7h13l-3-3M20 17H7l3 3"/>',
  pagamento: '<rect x="3" y="5" width="18" height="14" rx="2"/><path d="M3 10h18M7 15h4"/>',
  pessoas: '<circle cx="9" cy="8" r="3.5"/><path d="M2.5 20a6.5 6.5 0 0 1 13 0M16 4.5a3.5 3.5 0 0 1 0 7M18 14.5a6.5 6.5 0 0 1 3.5 5.5"/>',
  cliente: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
  fornecedor: '<path d="M2 6h11v10H2zM13 10h4l4 4v2h-8z"/><circle cx="6.5" cy="17.5" r="1.8"/><circle cx="17" cy="17.5" r="1.8"/>',
  estado: '<path d="M4 21V4M4 4h12l-2 4 2 4H4"/>',
  cidade: '<path d="M3 21h18M5 21V9l5-3v15M10 21V4l9 4v13M14 10h2M14 14h2M14 18h2"/>',
  bairro: '<path d="M12 21s-7-6.2-7-11.5a7 7 0 0 1 14 0C19 14.8 12 21 12 21z"/><circle cx="12" cy="9.5" r="2.5"/>',
  explorador: '<path d="m5 8 4 4-4 4M12 16h7"/><rect x="2" y="3" width="20" height="18" rx="2"/>',
  externo: '<path d="M7 17 17 7M9 7h8v8"/>',
  mais: '<path d="M12 5v14M5 12h14"/>',
  editar: '<path d="M4 20h4L19 9l-4-4L4 16z"/><path d="m14 6 4 4"/>',
  lixo: '<path d="M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3"/>',
  fechar: '<path d="M6 6l12 12M18 6 6 18"/>',
  busca: '<circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/>',
  entrada: '<path d="M12 3v12M7 10l5 5 5-5M4 21h16"/>',
  saida: '<path d="M12 15V3M7 8l5-5 5 5M4 21h16"/>',
  descarte: '<path d="M4 7h16M6 7l1 13h10l1-13M9 7V4h6v3M10 12l4 4M14 12l-4 4"/>',
  alerta: '<path d="M12 3 2 20h20z"/><path d="M12 10v4M12 17.5v.01"/>',
  info: '<circle cx="12" cy="12" r="9"/><path d="M12 11v5M12 7.5v.01"/>',
  ok: '<circle cx="12" cy="12" r="9"/><path d="m8 12 3 3 5-6"/>',
  erro: '<circle cx="12" cy="12" r="9"/><path d="m9 9 6 6M15 9l-6 6"/>',
  relogio: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
  dinheiro: '<rect x="2" y="6" width="20" height="12" rx="2"/><circle cx="12" cy="12" r="2.5"/><path d="M6 12h.01M18 12h.01"/>',
  sol: '<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>',
  lua: '<path d="M12 3a9 9 0 1 0 9 9 7 7 0 0 1-9-9z"/>',
  recarregar: '<path d="M20 11a8 8 0 1 0-2.3 5.7M20 4v7h-7"/>',
  vazio: '<path d="M3 13h5l1.5 3h5L16 13h5"/><path d="M5.5 5h13L21 13v6H3v-6z"/>',
  enviar: '<path d="M4 12 20 4l-4 16-4-7z"/><path d="m12 13 8-9"/>',
  documento: '<path d="M14 3H6v18h12V7z"/><path d="M14 3v4h4M9 13h6M9 17h6"/>',
  saude: '<path d="M3 12h4l2-5 4 10 2-5h6"/>',
  ligacao: '<path d="M10 14a4 4 0 0 0 5.7 0l3-3a4 4 0 0 0-5.7-5.7l-1 1"/><path d="M14 10a4 4 0 0 0-5.7 0l-3 3a4 4 0 0 0 5.7 5.7l1-1"/>',
};

export function icone(nome, classe = "") {
  return `<svg class="icone ${classe}" viewBox="0 0 24 24" aria-hidden="true">${ICONES[nome] || ICONES.info}</svg>`;
}

/** Texto amigável para qualquer erro vindo da API ou da rede */
export function mensagemDeErro(erro) {
  if (erro instanceof ApiIndisponivel) return erro.message;
  if (erro instanceof ErroApi) return erro.message;
  console.error(erro);
  return "Algo deu errado na página. Recarregue e tente de novo.";
}

// ---------- Avisos flutuantes ----------

export function avisar(mensagem, tom = "ok", duracao = 5000) {
  const area = document.getElementById("avisos");
  const toast = document.createElement("div");
  toast.className = "toast";
  toast.dataset.tom = tom;
  toast.setAttribute("role", tom === "erro" ? "alert" : "status");
  toast.innerHTML = `${icone(tom === "erro" ? "erro" : tom === "info" ? "info" : "ok")}
    <span>${escapar(mensagem)}</span>
    <button type="button" aria-label="Fechar aviso">${icone("fechar")}</button>`;
  const remover = () => toast.remove();
  toast.querySelector("button").addEventListener("click", remover);
  area.append(toast);
  if (duracao) setTimeout(remover, duracao);
}

// ---------- Janelas ----------

/** Cria um <dialog> modal; ao fechar, remove do DOM e devolve o foco a quem abriu */
export function criarJanela({ titulo, descricao = "", tamanho = "", corpo = "", rodape = "" }) {
  const origem = document.activeElement;
  const dialog = document.createElement("dialog");
  dialog.className = "janela";
  if (tamanho) dialog.dataset.tamanho = tamanho;
  const idTitulo = `janela-titulo-${Math.random().toString(36).slice(2, 8)}`;
  dialog.setAttribute("aria-labelledby", idTitulo);
  dialog.innerHTML = `
    <div class="janela-topo">
      <div>
        <h2 id="${idTitulo}">${escapar(titulo)}</h2>
        ${descricao ? `<p>${descricao}</p>` : ""}
      </div>
      <button class="btn btn-fantasma btn-icone" type="button" data-fechar aria-label="Fechar">${icone("fechar")}</button>
    </div>
    <div class="janela-corpo">${corpo}</div>
    ${rodape ? `<div class="janela-rodape">${rodape}</div>` : ""}`;
  document.body.append(dialog);
  dialog.addEventListener("close", () => {
    dialog.remove();
    if (origem && document.contains(origem)) origem.focus();
  });
  dialog.addEventListener("click", (e) => {
    if (e.target.closest("[data-fechar]")) dialog.close();
  });
  dialog.showModal();
  return {
    dialog,
    corpo: dialog.querySelector(".janela-corpo"),
    rodape: dialog.querySelector(".janela-rodape"),
    fechar: () => dialog.open && dialog.close(),
  };
}

/** Confirmação na própria página. Resolve true se o usuário confirmar. */
export function confirmar({ titulo, mensagem, textoConfirmar = "Confirmar", perigo = false }) {
  return new Promise((resolver) => {
    let confirmado = false;
    const janela = criarJanela({
      titulo,
      tamanho: "pequeno",
      corpo: `<p>${mensagem}</p>`,
      rodape: `<button class="btn" type="button" data-fechar>Cancelar</button>
               <button class="btn ${perigo ? "btn-perigo" : "btn-primario"}" type="button" data-confirmar>${escapar(textoConfirmar)}</button>`,
    });
    janela.dialog.querySelector("[data-confirmar]").addEventListener("click", () => {
      confirmado = true;
      janela.fechar();
    });
    janela.dialog.addEventListener("close", () => resolver(confirmado));
    janela.dialog.querySelector("[data-confirmar]").focus();
  });
}

/** Janela de resultado (só leitura) com um botão de fechar */
export function mostrarResultado({ titulo, descricao = "", corpo }) {
  const janela = criarJanela({
    titulo,
    descricao,
    corpo,
    rodape: `<button class="btn btn-primario" type="button" data-fechar>Fechar</button>`,
  });
  janela.dialog.querySelector(".janela-rodape [data-fechar]").focus();
  return janela;
}

// ---------- Formulário ----------

function htmlCampo(c, valor) {
  const id = `campo-${c.nome}-${Math.random().toString(36).slice(2, 7)}`;
  const obrig = c.obrigatorio ? '<span class="obrigatorio" aria-hidden="true">*</span>' : "";
  const comuns = `id="${id}" name="${c.nome}" class="entrada" ${c.obrigatorio ? "required" : ""} ${c.desabilitado ? "disabled" : ""}`;
  const v = valor ?? "";
  let controle;
  if (c.tipo === "selecao") {
    const opcoes = (c.opcoes || [])
      .map((o) => `<option value="${escapar(o.valor)}" ${String(o.valor) === String(v) ? "selected" : ""}>${escapar(o.rotulo)}</option>`)
      .join("");
    controle = `<select ${comuns}><option value="">${escapar(c.vazio || (c.obrigatorio ? "Escolha..." : "Nenhum"))}</option>${opcoes}</select>`;
  } else if (c.tipo === "area") {
    controle = `<textarea ${comuns} ${c.max ? `maxlength="${c.max}"` : ""} placeholder="${escapar(c.placeholder || "")}">${escapar(v)}</textarea>`;
  } else {
    const tipo = c.tipo === "numero" ? "text" : c.tipo === "data" ? "date" : c.tipo === "email" ? "email" : "text";
    const extras = [
      c.tipo === "numero" ? 'inputmode="decimal" autocomplete="off"' : "",
      c.max ? `maxlength="${c.max}"` : "",
      c.min ? `min="${c.min}"` : "",
      c.placeholder ? `placeholder="${escapar(c.placeholder)}"` : "",
    ].join(" ");
    const entrada = `<input type="${tipo}" ${comuns} ${extras} value="${escapar(v)}">`;
    controle = c.unidade !== undefined
      ? `<div class="entrada-unidade">${entrada}<span class="unidade" data-unidade>${escapar(c.unidade)}</span></div>`
      : entrada;
  }
  return `<div class="campo ${c.largo ? "largo" : ""}" data-campo="${c.nome}">
      <label for="${id}">${escapar(c.rotulo)}${obrig}</label>
      ${controle}
      <span class="ajuda" data-ajuda>${c.ajuda || ""}</span>
      <span class="erro-campo" data-erro-texto role="alert"></span>
    </div>`;
}

/**
 * Abre um formulário em janela. `aoEnviar(dados, ctrl)` faz a chamada à API; se lançar ErroApi,
 * a mensagem aparece no topo e cada campo inválido recebe sua mensagem. Resolve com o retorno de
 * `aoEnviar`, ou null se o usuário cancelar.
 */
export function abrirFormulario({
  titulo, descricao = "", tamanho = "", campos, valores = {}, textoEnviar = "Salvar",
  aoMudar, aoAbrir, aoEnviar, extra,
}) {
  return new Promise((resolver) => {
    let resultado = null;
    const janela = criarJanela({
      titulo,
      descricao,
      tamanho,
      corpo: `<form novalidate id="form-janela">
          <div class="aviso" data-tom="critico" data-erro-geral hidden role="alert">${icone("erro")}<span></span></div>
          <div class="grade-campos">${campos.map((c) => htmlCampo(c, valores[c.nome])).join("")}</div>
        </form>
        <div data-extra></div>`,
      rodape: `<button class="btn" type="button" data-fechar>Cancelar</button>
               <button class="btn btn-primario" type="submit" form="form-janela" data-enviar>${escapar(textoEnviar)}</button>`,
    });
    const form = janela.dialog.querySelector("form");
    const botaoEnviar = janela.dialog.querySelector("[data-enviar]");
    const caixaErro = form.querySelector("[data-erro-geral]");
    const tipos = Object.fromEntries(campos.map((c) => [c.nome, c.tipo]));
    const elemento = (nome) => form.elements.namedItem(nome);
    const blocoCampo = (nome) => form.querySelector(`[data-campo="${nome}"]`);
    let envioBloqueado = false;

    const ctrl = {
      form,
      dialog: janela.dialog,
      extra: janela.dialog.querySelector("[data-extra]"),
      valor: (nome) => elemento(nome)?.value ?? "",
      definir(nome, valor) {
        const el = elemento(nome);
        if (el) el.value = valor ?? "";
      },
      opcoes(nome, lista, rotuloVazio) {
        const el = elemento(nome);
        if (!el) return;
        const atual = el.value;
        const vazio = rotuloVazio ?? el.options[0]?.textContent ?? "";
        el.innerHTML = `<option value="">${escapar(vazio)}</option>` + lista
          .map((o) => `<option value="${escapar(o.valor)}">${escapar(o.rotulo)}</option>`).join("");
        el.value = lista.some((o) => String(o.valor) === atual) ? atual : "";
      },
      ajuda(nome, html) {
        const el = blocoCampo(nome)?.querySelector("[data-ajuda]");
        if (el) el.innerHTML = html || "";
      },
      unidade(nome, texto) {
        const el = blocoCampo(nome)?.querySelector("[data-unidade]");
        if (el) el.textContent = texto || "";
      },
      placeholder(nome, texto) {
        const el = elemento(nome);
        if (el) el.placeholder = texto || "";
      },
      habilitarEnvio(sim) {
        envioBloqueado = !sim;
        botaoEnviar.disabled = !sim;
      },
      limparErros() {
        caixaErro.hidden = true;
        form.querySelectorAll("[data-campo]").forEach((b) => {
          b.removeAttribute("data-erro");
          b.querySelector("[data-erro-texto]").textContent = "";
        });
      },
      erroGeral(mensagem) {
        caixaErro.querySelector("span").textContent = mensagem;
        caixaErro.hidden = false;
      },
      erroCampo(nome, mensagem) {
        const b = blocoCampo(nome);
        if (!b) return false;
        b.dataset.erro = "";
        b.querySelector("[data-erro-texto]").textContent = mensagem;
        return true;
      },
      mostrarErro(erro) {
        ctrl.limparErros();
        if (erro instanceof ErroApi && erro.campos.length) {
          const soltos = erro.campos.filter((c) => !ctrl.erroCampo(c.campo, c.mensagem));
          const resumo = soltos.length ? soltos.map((c) => `${c.campo}: ${c.mensagem}`).join(" · ") : erro.message;
          ctrl.erroGeral(soltos.length || erro.campos.length > 1 ? resumo : erro.message);
          form.querySelector("[data-erro] .entrada")?.focus();
        } else {
          ctrl.erroGeral(mensagemDeErro(erro));
        }
        janela.corpo.scrollTop = 0;
      },
      fechar: janela.fechar,
    };

    form.addEventListener("input", (e) => {
      const bloco = e.target.closest("[data-campo]");
      if (bloco?.hasAttribute("data-erro")) {
        bloco.removeAttribute("data-erro");
        bloco.querySelector("[data-erro-texto]").textContent = "";
      }
    });
    if (aoMudar) {
      form.addEventListener("change", (e) => e.target.name && aoMudar(e.target.name, ctrl));
      form.addEventListener("input", (e) => e.target.name && tipos[e.target.name] !== "selecao" && aoMudar(e.target.name, ctrl, true));
    }

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      if (envioBloqueado) return;
      ctrl.limparErros();
      // Validação básica de obrigatórios antes de ir à API
      const faltando = campos.filter((c) => c.obrigatorio && !c.desabilitado && !String(ctrl.valor(c.nome)).trim());
      if (faltando.length) {
        faltando.forEach((c) => ctrl.erroCampo(c.nome, "Campo obrigatório"));
        ctrl.erroGeral("Preencha os campos obrigatórios.");
        elemento(faltando[0].nome)?.focus();
        return;
      }
      const dados = {};
      for (const c of campos) {
        if (c.desabilitado) continue;
        const v = ctrl.valor(c.nome);
        dados[c.nome] = c.tipo === "numero" ? decimalParaApi(v) : v;
      }
      botaoEnviar.disabled = true;
      const textoOriginal = botaoEnviar.textContent;
      botaoEnviar.textContent = "Salvando...";
      try {
        resultado = await aoEnviar(dados, ctrl);
        janela.fechar();
      } catch (erro) {
        ctrl.mostrarErro(erro);
      } finally {
        botaoEnviar.textContent = textoOriginal;
        botaoEnviar.disabled = envioBloqueado;
      }
    });

    janela.dialog.addEventListener("close", () => resolver(resultado));
    if (extra) extra(ctrl.extra, ctrl);
    if (aoAbrir) aoAbrir(ctrl);
    form.querySelector(".entrada:not([disabled])")?.focus();
  });
}

// ---------- Tabela ----------

/**
 * Tabela que vira lista de cartões no celular.
 * colunas: [{ titulo, html(linha), classe, principal }]; acoes(linha) devolve os botões.
 */
export function tabela({ colunas, linhas, acoes, destaque, rotuloAcoes = "Ações" }) {
  const cab = colunas.map((c) => `<th scope="col" class="${c.classe || ""}">${escapar(c.titulo)}</th>`).join("")
    + (acoes ? `<th scope="col" class="acoes"><span class="visualmente-oculto">${rotuloAcoes}</span></th>` : "");
  const corpo = linhas.map((l) => {
    const tds = colunas.map((c) => `<td class="${c.classe || ""}" data-rotulo="${escapar(c.titulo)}" ${c.principal ? "data-principal" : ""}>${c.html(l)}</td>`).join("");
    const acoesHtml = acoes ? `<td class="acoes"><div class="acoes-grupo">${acoes(l)}</div></td>` : "";
    const d = destaque ? destaque(l) : "";
    return `<tr ${d ? `data-destaque="${d}"` : ""}>${tds}${acoesHtml}</tr>`;
  }).join("");
  return `<div class="quadro-tabela"><table class="tabela tabela-cartoes"><thead><tr>${cab}</tr></thead><tbody>${corpo}</tbody></table></div>`;
}

// ---------- Estados ----------

export function htmlCarregando() {
  return `<div class="carregando-linhas" aria-label="Carregando" role="status"><span></span><span></span><span></span></div>`;
}

export function htmlVazio({ icone: nomeIcone = "vazio", titulo, texto = "", acao = "" }) {
  return `<div class="estado">${icone(nomeIcone)}<h3>${escapar(titulo)}</h3>${texto ? `<p>${texto}</p>` : ""}${acao}</div>`;
}

export function htmlErro(erro) {
  return `<div class="estado" data-tipo="erro" role="alert">${icone("erro")}
      <h3>Não foi possível carregar</h3>
      <p>${escapar(mensagemDeErro(erro))}</p>
      <button class="btn" type="button" data-acao="tentar-de-novo">${icone("recarregar")}Tentar de novo</button>
    </div>`;
}

/** Cabeçalho padrão de página */
export function htmlCabecalho({ titulo, texto = "", acoes = "" }) {
  return `<div class="cabecalho">
      <div class="cabecalho-texto"><h1>${escapar(titulo)}</h1>${texto ? `<p>${texto}</p>` : ""}</div>
      ${acoes ? `<div class="cabecalho-acoes">${acoes}</div>` : ""}
    </div>`;
}

/** Converte uma lista de registros em opções de <select> */
export function opcoesDe(lista, valor, rotulo) {
  return lista.map((r) => ({ valor: r[valor], rotulo: typeof rotulo === "function" ? rotulo(r) : r[rotulo] }));
}
