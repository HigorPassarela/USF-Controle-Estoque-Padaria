// Explorador: lista todas as rotas a partir do OpenAPI da aplicação e permite chamá-las.
import { requisicaoCrua } from "../api.js";
import { icone, htmlCabecalho, htmlCarregando, htmlErro, htmlVazio } from "../ui.js";
import { escapar, normalizar } from "../formatos.js";
import { ApiIndisponivel } from "../api.js";

const ORDEM_METODOS = ["get", "post", "put", "delete", "patch"];

/** JSON formatado com destaque simples de chaves, textos, números e literais */
function destacarJson(valor) {
  const texto = escapar(JSON.stringify(valor, null, 2));
  return texto.replace(/(&quot;(?:[^&]|&(?!quot;))*?&quot;)(\s*:)?|\b(true|false|null)\b|-?\b\d+(?:\.\d+)?(?:[eE][+-]?\d+)?\b/g, (trecho, str, doisPontos, literal) => {
    if (str) return doisPontos ? `<span class="k">${str}</span>${doisPontos}` : `<span class="s">${str}</span>`;
    if (literal) return `<span class="b">${literal}</span>`;
    return `<span class="n">${trecho}</span>`;
  });
}

export async function renderExplorador(conteudo, { definirTopo, vigente }) {
  definirTopo("Ferramentas", "Explorador da API");
  conteudo.innerHTML = `
    ${htmlCabecalho({
      titulo: "Explorador da API",
      texto: "Todas as rotas da aplicação, lidas da documentação OpenAPI. Escolha uma, preencha os parâmetros e envie.",
      acoes: `<a class="btn" href="/swagger-ui.html" target="_blank" rel="noopener">${icone("documento")}Abrir no Swagger</a>`,
    })}
    <div id="explorador-corpo" class="pilha">${htmlCarregando()}</div>`;
  const corpo = conteudo.querySelector("#explorador-corpo");
  conteudo.onclick = null;

  let doc;
  try {
    const r = await requisicaoCrua("GET", "/v3/api-docs");
    if (r.falhou) throw new ApiIndisponivel();
    doc = r.json;
    if (!doc?.paths) throw new Error("documentação inválida");
  } catch (erro) {
    if (vigente()) corpo.innerHTML = htmlErro(erro);
    return;
  }
  if (!vigente()) return;

  // Operações: { id, metodo, caminho, tag, resumo, descricao, parametros, respostas }
  const operacoes = [];
  for (const [caminho, item] of Object.entries(doc.paths)) {
    for (const metodo of ORDEM_METODOS) {
      const op = item[metodo];
      if (!op) continue;
      operacoes.push({
        id: `${metodo.toUpperCase()} ${caminho}`,
        metodo: metodo.toUpperCase(),
        caminho,
        tag: op.tags?.[0] || "Outros",
        resumo: op.summary || "",
        descricao: op.description || "",
        parametros: op.parameters || [],
        respostas: Object.entries(op.responses || {}).map(([codigo, r]) => ({ codigo, descricao: r.description || "" })),
      });
    }
  }
  operacoes.sort((a, b) => a.caminho.localeCompare(b.caminho) || ORDEM_METODOS.indexOf(a.metodo.toLowerCase()) - ORDEM_METODOS.indexOf(b.metodo.toLowerCase()));
  const grupos = [...new Set(operacoes.map((o) => o.tag))].sort((a, b) => a.localeCompare(b, "pt-BR"));
  let selecionada = operacoes.find((o) => o.id === "GET /api/estoque/saldos") || operacoes[0];

  corpo.innerHTML = `
    <div class="explorador">
      <section class="quadro rotas" aria-label="Rotas da API">
        <div class="ferramentas">
          <div class="campo">
            <label for="busca-rota">Buscar rota</label>
            <input id="busca-rota" class="entrada" type="search" autocomplete="off" placeholder="Ex.: lotes, POST, saldo...">
          </div>
          <span class="mudo" style="font-size:var(--t-1)">${operacoes.length} rotas</span>
        </div>
        <div class="rotas-lista" id="rotas-lista"></div>
      </section>
      <section class="quadro" id="painel-rota" aria-live="polite"></section>
    </div>`;

  const listaEl = corpo.querySelector("#rotas-lista");
  const painel = corpo.querySelector("#painel-rota");
  const busca = corpo.querySelector("#busca-rota");

  const desenharLista = () => {
    const termo = normalizar(busca.value.trim());
    const html = grupos.map((g) => {
      const doGrupo = operacoes.filter((o) => o.tag === g && (!termo || normalizar(`${o.id} ${o.resumo} ${o.tag}`).includes(termo)));
      if (!doGrupo.length) return "";
      return `<div class="rotas-grupo"><h3>${escapar(g)}</h3>${doGrupo.map((o) => `
          <button class="rota-item" type="button" data-op="${escapar(o.id)}" ${o === selecionada ? 'aria-current="true"' : ""}>
            <span class="metodo" data-m="${o.metodo}">${o.metodo}</span>
            <span class="rota-caminho">${escapar(o.caminho)}</span>
            <span class="rota-resumo">${escapar(o.resumo)}</span>
          </button>`).join("")}</div>`;
    }).join("");
    listaEl.innerHTML = html || `<p class="mudo" style="padding:12px">Nenhuma rota encontrada.</p>`;
  };

  const montarUrl = (op, form) => {
    let caminho = op.caminho;
    const query = new URLSearchParams();
    for (const p of op.parametros) {
      const v = form.elements.namedItem(p.name)?.value.trim() ?? "";
      if (p.in === "path") caminho = caminho.replace(`{${p.name}}`, v ? encodeURIComponent(v) : `{${p.name}}`);
      else if (p.in === "query" && v) query.append(p.name, v);
    }
    const qs = query.toString();
    return caminho + (qs ? `?${qs}` : "");
  };

  const desenharPainel = () => {
    const op = selecionada;
    const params = op.parametros;
    const campo = (p) => {
      const id = `param-${p.name}`;
      const enumeracao = p.schema?.enum;
      const ajuda = p.description ? `<span class="ajuda">${escapar(p.description)}</span>` : "";
      const rotulo = `<label for="${id}">${escapar(p.name)}${p.required ? '<span class="obrigatorio" aria-hidden="true">*</span>' : ""} <span class="mudo" style="font-weight:400">· ${p.in === "path" ? "caminho" : "consulta"}</span></label>`;
      const controle = enumeracao
        ? `<select id="${id}" name="${escapar(p.name)}" class="entrada"><option value="">${p.required ? "Escolha..." : "(vazio)"}</option>${enumeracao.map((v) => `<option value="${escapar(v)}">${escapar(v)}</option>`).join("")}</select>`
        : `<input id="${id}" name="${escapar(p.name)}" class="entrada" autocomplete="off" placeholder="${escapar(p.schema?.type === "boolean" ? "true ou false" : (p.example ?? p.schema?.example ?? ""))}">`;
      return `<div class="campo">${rotulo}${controle}${ajuda}</div>`;
    };
    painel.innerHTML = `
      <div class="quadro-cabecalho">
        <div style="display:grid;gap:6px;min-width:0">
          <span style="display:flex;align-items:center;gap:10px;flex-wrap:wrap"><span class="metodo" data-m="${op.metodo}">${op.metodo}</span><span class="mono" style="overflow-wrap:anywhere">${escapar(op.caminho)}</span></span>
          <h2>${escapar(op.resumo || op.id)}</h2>
        </div>
      </div>
      <div class="quadro-corpo" style="display:grid;gap:16px">
        ${op.descricao ? `<p class="mudo">${escapar(op.descricao)}</p>` : ""}
        <form id="form-rota" novalidate style="display:grid;gap:16px">
          ${params.length ? `<div class="grade-campos">${params.map(campo).join("")}</div>` : '<p class="mudo">Esta rota não tem parâmetros.</p>'}
          <div class="url-previa"><span class="metodo" data-m="${op.metodo}">${op.metodo}</span><span id="url-previa"></span></div>
          ${op.metodo !== "GET" ? `<div class="aviso" data-tom="alerta">${icone("alerta")}<span>Esta rota altera dados de verdade no banco.</span></div>` : ""}
          <div><button class="btn btn-primario" type="submit">${icone("enviar")}Enviar requisição</button></div>
        </form>
        <div>
          <span class="rotulo" style="font-size:var(--t-2);font-weight:600;color:var(--texto-2)">Respostas documentadas</span>
          <div class="lista-respostas" style="margin-top:6px">${op.respostas.map((r) => `<span class="status-http" data-faixa="${r.codigo[0]}" title="${escapar(r.descricao)}">${escapar(r.codigo)}</span>`).join("")}</div>
        </div>
        <div id="resposta"></div>
      </div>`;
    const form = painel.querySelector("#form-rota");
    const previa = painel.querySelector("#url-previa");
    const atualizarPrevia = () => { previa.textContent = montarUrl(op, form); };
    form.addEventListener("input", atualizarPrevia);
    form.addEventListener("change", atualizarPrevia);
    atualizarPrevia();

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const faltando = params.filter((p) => p.required && !form.elements.namedItem(p.name)?.value.trim());
      const caixa = painel.querySelector("#resposta");
      if (faltando.length) {
        caixa.innerHTML = `<div class="aviso" data-tom="critico">${icone("erro")}<span>Preencha: ${faltando.map((p) => escapar(p.name)).join(", ")}.</span></div>`;
        return;
      }
      const botao = form.querySelector("[type=submit]");
      botao.disabled = true;
      caixa.innerHTML = htmlCarregando();
      const r = await requisicaoCrua(op.metodo, montarUrl(op, form));
      botao.disabled = false;
      if (r.falhou) {
        caixa.innerHTML = htmlVazio({ icone: "erro", titulo: "Sem resposta", texto: "A API não respondeu. Confira se a aplicação está rodando." });
        return;
      }
      const tamanho = new Blob([r.texto]).size;
      caixa.innerHTML = `
        <div style="display:grid;gap:10px">
          <div class="resposta-meta">
            <span class="status-http" data-faixa="${String(r.status)[0]}">${r.status}</span>
            <span>${Math.round(r.tempo)} ms</span>
            <span>${tamanho} bytes</span>
            ${Array.isArray(r.json) ? `<span>${r.json.length} ${r.json.length === 1 ? "item" : "itens"}</span>` : ""}
          </div>
          ${r.status === 204 ? '<p class="mudo">Sem conteúdo (a operação deu certo e não devolve corpo).</p>'
            : r.json !== undefined ? `<pre class="json">${destacarJson(r.json)}</pre>`
            : `<pre class="json">${escapar(r.texto || "(vazio)")}</pre>`}
        </div>`;
    });
  };

  listaEl.addEventListener("click", (e) => {
    const item = e.target.closest("[data-op]");
    if (!item) return;
    selecionada = operacoes.find((o) => o.id === item.dataset.op);
    desenharLista();
    desenharPainel();
    if (matchMedia("(max-width: 719px)").matches) painel.scrollIntoView({ behavior: "smooth", block: "start" });
  });
  busca.addEventListener("input", desenharLista);
  desenharLista();
  desenharPainel();
}
