// Cadastros: uma tela genérica (listagem, busca, formulário, exclusão) guiada por descritores.
// Os nomes dos campos são os mesmos parâmetros da API, então os erros por campo caem no lugar certo.
import { api } from "../api.js";
import {
  icone, tabela, htmlCabecalho, htmlCarregando, htmlErro, htmlVazio,
  abrirFormulario, confirmar, avisar, mensagemDeErro, opcoesDe,
} from "../ui.js";
import { escapar, moeda, quantidade, normalizar, decimalParaApi } from "../formatos.js";

// Listas usadas nos campos de escolha
const FONTES = {
  categorias: { rota: "/api/categorias-produto", valor: "idCategoriaProduto", rotulo: "descricao" },
  tiposProduto: { rota: "/api/tipos-produto", valor: "idTipoProduto", rotulo: "descricao" },
  unidades: { rota: "/api/unidades-medida", valor: "idUnidadeMedida", rotulo: (u) => `${u.descricao} (${u.sigla})` },
  fornecedores: { rota: "/api/fornecedores", valor: "idFornecedor", rotulo: "nome" },
  tiposCliente: { rota: "/api/tipos-cliente", valor: "idTipoCliente", rotulo: "descricao" },
  estados: { rota: "/api/estados", valor: "idEstado", rotulo: (e) => `${e.nome} (${e.uf})` },
  cidades: { rota: "/api/cidades", valor: "idCidade", rotulo: (c) => `${c.nome} (${c.uf})` },
  bairros: { rota: "/api/bairros", valor: "idBairro", rotulo: "nome" },
};

const ordenar = (lista, rotulo) => [...lista].sort((a, b) =>
  normalizar(typeof rotulo === "function" ? rotulo(a) : a[rotulo]).localeCompare(normalizar(typeof rotulo === "function" ? rotulo(b) : b[rotulo])));

/** Carrega uma lista da API ordenada pelo rótulo, no formato de opções */
export async function carregarOpcoes(nomeFonte) {
  const f = FONTES[nomeFonte];
  const lista = await api.get(f.rota);
  return { registros: lista, opcoes: opcoesDe(ordenar(lista, f.rotulo), f.valor, f.rotulo) };
}

const descricaoSimples = (max = 100) => [
  { nome: "descricao", rotulo: "Descrição", tipo: "texto", obrigatorio: true, max, largo: true },
];
const colunaDescricao = { titulo: "Descrição", principal: true, html: (r) => `<span class="celula-principal">${escapar(r.descricao)}</span>` };
const sub = (texto) => (texto ? `<span class="celula-sub">${escapar(texto)}</span>` : "");

/** Mantém no campo bairro só os bairros da cidade escolhida */
function filtrarBairros(ctrl, listas) {
  const idCidade = ctrl.valor("cidade");
  const bairros = listas.bairro?.registros || [];
  const daCidade = idCidade ? bairros.filter((b) => String(b.idCidade) === String(idCidade)) : [];
  ctrl.opcoes("bairro", opcoesDe(ordenar(daCidade, "nome"), "idBairro", "nome"), "Nenhum");
  if (!idCidade) ctrl.ajuda("bairro", "Escolha a cidade primeiro.");
  else if (!daCidade.length) ctrl.ajuda("bairro", "Nenhum bairro cadastrado nesta cidade.");
  else ctrl.ajuda("bairro", "");
}

const camposEndereco = [
  { nome: "cidade", rotulo: "Cidade", tipo: "selecao", fonte: "cidades", chaveResposta: "idCidade" },
  { nome: "bairro", rotulo: "Bairro", tipo: "selecao", fonte: "bairros", chaveResposta: "idBairro" },
];
const enderecoHooks = {
  aoAbrir: (ctrl, listas) => filtrarBairros(ctrl, listas),
  aoMudar: (nome, ctrl, listas) => { if (nome === "cidade") filtrarBairros(ctrl, listas); },
};
const colunaEndereco = {
  titulo: "Endereço",
  html: (r) => r.nomeCidade ? `${escapar(r.nomeCidade)}${sub(r.nomeBairro)}` : '<span class="mudo">–</span>',
};
const colunaContato = {
  titulo: "Contato",
  html: (r) => [r.telefone, r.email].filter(Boolean).map(escapar).join("<br>") || '<span class="mudo">–</span>',
};

export const RECURSOS = {
  produtos: {
    titulo: "Produtos", singular: "produto", genero: "m", icone: "produto",
    rota: "/api/produtos", chave: "idProduto", exclusao: "logica",
    texto: "O que a padaria vende e estoca. O saldo muda só por entradas e saídas no Estoque.",
    nome: (r) => r.nome,
    busca: (r) => [r.nome, r.nomeCategoria, r.descricaoTipoProduto, r.nomeFornecedor],
    colunas: [
      { titulo: "Produto", principal: true, html: (r) => `<span class="celula-principal">${escapar(r.nome)}</span>${sub([r.nomeCategoria, r.descricaoTipoProduto].filter(Boolean).join(" · "))}` },
      { titulo: "Preço", classe: "num", html: (r) => moeda(r.precoVenda) },
      { titulo: "Saldo", classe: "num", html: (r) => quantidade(r.quantidadeEstoque, r.nomeUnidadeMedida) },
      { titulo: "Fornecedor principal", html: (r) => r.nomeFornecedor ? escapar(r.nomeFornecedor) : '<span class="mudo">–</span>' },
    ],
    campos: [
      { nome: "nome", rotulo: "Nome", tipo: "texto", obrigatorio: true, max: 100, largo: true, placeholder: "Ex.: Pão francês" },
      { nome: "precoVenda", rotulo: "Preço de venda", tipo: "numero", obrigatorio: true, unidade: "R$", ajuda: "Maior que zero. Ex.: 0,75" },
      { nome: "unidadeMedida", rotulo: "Unidade de medida", tipo: "selecao", obrigatorio: true, fonte: "unidades", chaveResposta: "idUnidadeMedida" },
      { nome: "categoria", rotulo: "Categoria", tipo: "selecao", obrigatorio: true, fonte: "categorias", chaveResposta: "idCategoriaProduto" },
      { nome: "tipoProduto", rotulo: "Tipo de produto", tipo: "selecao", obrigatorio: true, fonte: "tiposProduto", chaveResposta: "idTipoProduto" },
      { nome: "fornecedor", rotulo: "Fornecedor principal", tipo: "selecao", fonte: "fornecedores", chaveResposta: "idFornecedor", largo: true, ajuda: "Opcional. Os custos de cada fornecedor ficam na seção abaixo, depois de salvar." },
    ],
    extraEdicao: (container, registro, listas) => painelFornecedores(container, registro, listas.fornecedor?.registros || []),
  },
  clientes: {
    titulo: "Clientes", singular: "cliente", genero: "m", icone: "cliente",
    rota: "/api/clientes", chave: "idCliente", exclusao: "logica",
    texto: "Clientes da padaria. CPF ou CNPJ não se repetem entre clientes ativos.",
    nome: (r) => r.nome,
    busca: (r) => [r.nome, r.cpfCnpj, r.email, r.telefone, r.nomeCidade, r.nomeBairro, r.descricaoTipoCliente],
    colunas: [
      { titulo: "Cliente", principal: true, html: (r) => `<span class="celula-principal">${escapar(r.nome)}</span>${sub(r.cpfCnpj)}` },
      { titulo: "Tipo", html: (r) => escapar(r.descricaoTipoCliente) },
      colunaEndereco,
      colunaContato,
    ],
    campos: [
      { nome: "nome", rotulo: "Nome", tipo: "texto", obrigatorio: true, max: 100, largo: true },
      { nome: "cpfCnpj", rotulo: "CPF ou CNPJ", tipo: "texto", obrigatorio: true, max: 20, ajuda: "Só números ou com pontuação." },
      { nome: "tipoCliente", rotulo: "Tipo de cliente", tipo: "selecao", obrigatorio: true, fonte: "tiposCliente", chaveResposta: "idTipoCliente" },
      { nome: "telefone", rotulo: "Telefone", tipo: "texto", max: 20, placeholder: "(11) 99999-8888" },
      { nome: "email", rotulo: "E-mail", tipo: "email", max: 100 },
      ...camposEndereco,
    ],
    ...enderecoHooks,
  },
  fornecedores: {
    titulo: "Fornecedores", singular: "fornecedor", genero: "m", icone: "fornecedor",
    rota: "/api/fornecedores", chave: "idFornecedor", exclusao: "logica",
    texto: "De quem a padaria compra. O CNPJ não se repete entre fornecedores ativos.",
    nome: (r) => r.nome,
    busca: (r) => [r.nome, r.cnpj, r.email, r.telefone, r.nomeCidade, r.nomeBairro],
    colunas: [
      { titulo: "Fornecedor", principal: true, html: (r) => `<span class="celula-principal">${escapar(r.nome)}</span>${sub(r.cnpj)}` },
      colunaEndereco,
      colunaContato,
    ],
    campos: [
      { nome: "nome", rotulo: "Nome ou razão social", tipo: "texto", obrigatorio: true, max: 100, largo: true },
      { nome: "cnpj", rotulo: "CNPJ", tipo: "texto", obrigatorio: true, max: 20 },
      { nome: "telefone", rotulo: "Telefone", tipo: "texto", max: 20 },
      { nome: "email", rotulo: "E-mail", tipo: "email", max: 100, largo: true },
      ...camposEndereco,
    ],
    ...enderecoHooks,
  },
  "categorias-produto": {
    titulo: "Categorias", singular: "categoria", genero: "f", icone: "categoria",
    rota: "/api/categorias-produto", chave: "idCategoriaProduto", exclusao: "fisica",
    texto: "Agrupam os produtos: pães, doces, bebidas...",
    nome: (r) => r.descricao, busca: (r) => [r.descricao],
    colunas: [colunaDescricao], campos: descricaoSimples(),
  },
  "tipos-produto": {
    titulo: "Tipos de produto", singular: "tipo de produto", genero: "m", icone: "tipo",
    rota: "/api/tipos-produto", chave: "idTipoProduto", exclusao: "fisica",
    texto: "Natureza do produto: matéria-prima, produto acabado, revenda...",
    nome: (r) => r.descricao, busca: (r) => [r.descricao],
    colunas: [colunaDescricao], campos: descricaoSimples(),
  },
  "unidades-medida": {
    titulo: "Unidades de medida", singular: "unidade de medida", genero: "f", icone: "regua",
    rota: "/api/unidades-medida", chave: "idUnidadeMedida", exclusao: "fisica",
    texto: "Como os produtos são medidos: quilo, unidade, litro...",
    nome: (r) => r.descricao, busca: (r) => [r.descricao, r.sigla],
    colunas: [colunaDescricao, { titulo: "Sigla", html: (r) => `<span class="mono">${escapar(r.sigla)}</span>` }],
    campos: [
      { nome: "descricao", rotulo: "Descrição", tipo: "texto", obrigatorio: true, max: 100, placeholder: "Ex.: Quilograma" },
      { nome: "sigla", rotulo: "Sigla", tipo: "texto", obrigatorio: true, max: 10, placeholder: "Ex.: kg" },
    ],
  },
  "tipos-movimentacao": {
    titulo: "Tipos de movimentação", singular: "tipo de movimentação", genero: "m", icone: "movimentacao",
    rota: "/api/tipos-movimentacao", chave: "idTipoMovimentacao", exclusao: "fisica",
    texto: "Motivos de entrada e saída do estoque. Um tipo já usado não pode trocar entre entrada e saída.",
    nome: (r) => r.descricao, busca: (r) => [r.descricao, r.tipo],
    colunas: [
      colunaDescricao,
      { titulo: "Natureza", html: (r) => `<span class="situacao" data-s="${r.tipo === "ENTRADA" ? "entrada" : "saida"}">${r.tipo === "ENTRADA" ? "Entrada" : "Saída"}</span>` },
    ],
    campos: [
      { nome: "descricao", rotulo: "Descrição", tipo: "texto", obrigatorio: true, max: 100, largo: true, placeholder: "Ex.: Compra de fornecedor" },
      { nome: "tipo", rotulo: "Natureza", tipo: "selecao", obrigatorio: true, opcoes: [{ valor: "ENTRADA", rotulo: "Entrada (aumenta o estoque)" }, { valor: "SAIDA", rotulo: "Saída (diminui o estoque)" }] },
    ],
  },
  "metodos-pagamento": {
    titulo: "Métodos de pagamento", singular: "método de pagamento", genero: "m", icone: "pagamento",
    rota: "/api/metodos-pagamento", chave: "idMetodoPagamento", exclusao: "fisica",
    texto: "Formas de pagamento aceitas: dinheiro, Pix, cartão...",
    nome: (r) => r.descricao, busca: (r) => [r.descricao],
    colunas: [colunaDescricao], campos: descricaoSimples(),
  },
  "tipos-cliente": {
    titulo: "Tipos de cliente", singular: "tipo de cliente", genero: "m", icone: "pessoas",
    rota: "/api/tipos-cliente", chave: "idTipoCliente", exclusao: "fisica",
    texto: "Classificação dos clientes: pessoa física, pessoa jurídica...",
    nome: (r) => r.descricao, busca: (r) => [r.descricao],
    colunas: [colunaDescricao], campos: descricaoSimples(),
  },
  estados: {
    titulo: "Estados", singular: "estado", genero: "m", icone: "estado",
    rota: "/api/estados", chave: "idEstado", somenteLeitura: true,
    texto: "Os 27 estados brasileiros, carregados pelo sistema. Só para consulta.",
    nome: (r) => r.nome, busca: (r) => [r.nome, r.uf],
    colunas: [
      { titulo: "Estado", principal: true, html: (r) => `<span class="celula-principal">${escapar(r.nome)}</span>` },
      { titulo: "UF", html: (r) => `<span class="mono">${escapar(r.uf)}</span>` },
    ],
    campos: [],
  },
  cidades: {
    titulo: "Cidades", singular: "cidade", genero: "f", icone: "cidade",
    rota: "/api/cidades", chave: "idCidade", exclusao: "fisica",
    texto: "Cidades usadas no endereço de clientes e fornecedores.",
    nome: (r) => r.nome, busca: (r) => [r.nome, r.nomeEstado, r.uf],
    colunas: [
      { titulo: "Cidade", principal: true, html: (r) => `<span class="celula-principal">${escapar(r.nome)}</span>` },
      { titulo: "Estado", html: (r) => `${escapar(r.nomeEstado)} <span class="mudo mono">${escapar(r.uf)}</span>` },
    ],
    campos: [
      { nome: "nome", rotulo: "Nome", tipo: "texto", obrigatorio: true, max: 100 },
      { nome: "estado", rotulo: "Estado", tipo: "selecao", obrigatorio: true, fonte: "estados", chaveResposta: "idEstado" },
    ],
  },
  bairros: {
    titulo: "Bairros", singular: "bairro", genero: "m", icone: "bairro",
    rota: "/api/bairros", chave: "idBairro", exclusao: "fisica",
    texto: "Bairros de cada cidade, usados no endereço de clientes e fornecedores.",
    nome: (r) => r.nome, busca: (r) => [r.nome, r.nomeCidade, r.uf],
    colunas: [
      { titulo: "Bairro", principal: true, html: (r) => `<span class="celula-principal">${escapar(r.nome)}</span>` },
      { titulo: "Cidade", html: (r) => `${escapar(r.nomeCidade)} <span class="mudo mono">${escapar(r.uf)}</span>` },
    ],
    campos: [
      { nome: "nome", rotulo: "Nome", tipo: "texto", obrigatorio: true, max: 100 },
      { nome: "cidade", rotulo: "Cidade", tipo: "selecao", obrigatorio: true, fonte: "cidades", chaveResposta: "idCidade" },
    ],
  },
};

export const GRUPOS_CADASTRO = [
  { titulo: "Produtos", recursos: ["produtos", "categorias-produto", "tipos-produto", "unidades-medida", "tipos-movimentacao"] },
  { titulo: "Pessoas", recursos: ["clientes", "fornecedores", "tipos-cliente", "metodos-pagamento"] },
  { titulo: "Localização", recursos: ["estados", "cidades", "bairros"] },
];

const maiuscula = (t) => t.charAt(0).toUpperCase() + t.slice(1);
const artigo = (rec) => (rec.genero === "f" ? "a" : "o");
const novo = (rec) => (rec.genero === "f" ? "Nova" : "Novo");

// ---------- Tela ----------

export async function renderCadastro(conteudo, { recurso, definirTopo, vigente }) {
  const rec = RECURSOS[recurso];
  definirTopo("Cadastros", rec.titulo);
  const acoes = rec.somenteLeitura ? "" :
    `<button class="btn btn-primario" type="button" data-acao="novo">${icone("mais")}${novo(rec)} ${escapar(rec.singular)}</button>`;
  conteudo.innerHTML = `
    ${htmlCabecalho({ titulo: rec.titulo, texto: escapar(rec.texto), acoes })}
    <section class="quadro" aria-label="Lista de ${escapar(rec.titulo.toLowerCase())}">
      <div class="ferramentas">
        <div class="campo campo-busca">
          <label for="busca-cadastro">Buscar</label>
          <input id="busca-cadastro" class="entrada" type="search" placeholder="Digite parte do nome..." autocomplete="off">
        </div>
        <span class="espaco"></span>
        <span class="mudo" id="contagem" role="status"></span>
      </div>
      <div id="lista">${htmlCarregando()}</div>
    </section>`;

  const lista = conteudo.querySelector("#lista");
  const busca = conteudo.querySelector("#busca-cadastro");
  const contagem = conteudo.querySelector("#contagem");
  let registros = [];

  const desenhar = () => {
    const termo = normalizar(busca.value.trim());
    const filtrados = termo ? registros.filter((r) => rec.busca(r).some((v) => normalizar(v).includes(termo))) : registros;
    contagem.textContent = termo
      ? `${filtrados.length} de ${registros.length}`
      : `${registros.length} ${registros.length === 1 ? "registro" : "registros"}`;
    if (!registros.length) {
      lista.innerHTML = htmlVazio({
        icone: rec.icone,
        titulo: `Nenhum ${rec.genero === "f" ? "registro" : rec.singular} cadastrado`,
        texto: rec.somenteLeitura ? "" : `Cadastre ${artigo(rec) === "a" ? "a primeira" : "o primeiro"} ${escapar(rec.singular)} para começar.`,
        acao: rec.somenteLeitura ? "" : `<button class="btn btn-primario" type="button" data-acao="novo">${icone("mais")}${novo(rec)} ${escapar(rec.singular)}</button>`,
      });
      return;
    }
    if (!filtrados.length) {
      lista.innerHTML = htmlVazio({ icone: "busca", titulo: "Nada encontrado", texto: `Nenhum registro contém "${escapar(busca.value)}".` });
      return;
    }
    lista.innerHTML = tabela({
      colunas: rec.colunas,
      linhas: filtrados,
      acoes: rec.somenteLeitura ? null : (r) => `
        <button class="btn btn-p" type="button" data-acao="editar" data-id="${r[rec.chave]}">${icone("editar")}Editar</button>
        <button class="btn btn-p btn-fantasma btn-icone" type="button" data-acao="excluir" data-id="${r[rec.chave]}" aria-label="Excluir ${escapar(rec.nome(r))}">${icone("lixo")}</button>`,
    });
  };

  const carregar = async () => {
    lista.innerHTML = htmlCarregando();
    try {
      const dados = await api.get(rec.rota);
      if (!vigente()) return;
      registros = ordenar(dados, rec.nome);
      desenhar();
    } catch (erro) {
      if (vigente()) lista.innerHTML = htmlErro(erro);
    }
  };

  busca.addEventListener("input", desenhar);
  conteudo.onclick = async (e) => {
    const botao = e.target.closest("[data-acao]");
    if (!botao) return;
    const acao = botao.dataset.acao;
    if (acao === "tentar-de-novo") return; // tratado pela casca
    if (acao === "novo") {
      if (await abrirFormularioRecurso(rec, null)) carregar();
    } else if (acao === "editar") {
      if (await abrirFormularioRecurso(rec, botao.dataset.id)) carregar();
    } else if (acao === "excluir") {
      const registro = registros.find((r) => String(r[rec.chave]) === botao.dataset.id);
      if (await excluir(rec, registro)) carregar();
    }
  };

  await carregar();
}

/** Abre o formulário de criação (id nulo) ou edição. Resolve true se salvou. */
export async function abrirFormularioRecurso(rec, id) {
  const comFonte = rec.campos.filter((c) => c.fonte);
  let registro = null;
  let listas = {};
  try {
    const [atual, ...carregadas] = await Promise.all([
      id ? api.get(`${rec.rota}/${id}`) : Promise.resolve(null),
      ...comFonte.map((c) => carregarOpcoes(c.fonte)),
    ]);
    registro = atual;
    listas = Object.fromEntries(comFonte.map((c, i) => [c.nome, carregadas[i]]));
  } catch (erro) {
    avisar(mensagemDeErro(erro), "erro");
    return false;
  }

  const campos = rec.campos.map((c) => (c.fonte ? { ...c, opcoes: listas[c.nome].opcoes } : c));
  const valores = {};
  if (registro) {
    for (const c of rec.campos) {
      const v = c.chaveResposta ? registro[c.chaveResposta] : registro[c.nome];
      // Números abrem no formato brasileiro (7,90); a API recebe de volta com ponto
      valores[c.nome] = c.tipo === "numero" && v !== null && v !== undefined
        ? Number(v).toLocaleString("pt-BR", { minimumFractionDigits: c.unidade === "R$" ? 2 : 0, maximumFractionDigits: 3, useGrouping: false })
        : v;
    }
  }
  const a = artigo(rec);
  const salvo = await abrirFormulario({
    titulo: registro ? `Editar ${rec.singular}` : `${novo(rec)} ${rec.singular}`,
    descricao: registro ? escapar(rec.nome(registro)) : "Os campos com * são obrigatórios.",
    tamanho: rec.extraEdicao && registro ? "largo" : "",
    campos,
    valores,
    textoEnviar: registro ? "Salvar alterações" : `Cadastrar ${rec.singular}`,
    aoAbrir: (ctrl) => rec.aoAbrir?.(ctrl, listas),
    aoMudar: (nome, ctrl, digitando) => { if (!digitando) rec.aoMudar?.(nome, ctrl, listas); },
    extra: rec.extraEdicao && registro ? (container) => rec.extraEdicao(container, registro, listas) : null,
    aoEnviar: (dados) => (registro ? api.atualizar(`${rec.rota}/${id}`, dados) : api.criar(rec.rota, dados)),
  });
  if (salvo) {
    avisar(`${maiuscula(rec.singular)} ${registro ? `atualizad${a}` : `cadastrad${a}`}: ${rec.nome(salvo)}.`);
    return true;
  }
  return false;
}

async function excluir(rec, registro) {
  const a = artigo(rec);
  const nome = `<strong>${escapar(rec.nome(registro))}</strong>`;
  const mensagem = rec.exclusao === "logica"
    ? `${maiuscula(a)} ${rec.singular} ${nome} fica inativ${a}: some das listas e consultas, mas o histórico é preservado.`
    : `${maiuscula(a)} ${rec.singular} ${nome} é removid${a} definitivamente. Se outro cadastro usar este registro, a exclusão será recusada.`;
  const ok = await confirmar({
    titulo: rec.exclusao === "logica" ? `Inativar ${rec.singular}?` : `Excluir ${rec.singular}?`,
    mensagem,
    textoConfirmar: rec.exclusao === "logica" ? "Inativar" : "Excluir",
    perigo: true,
  });
  if (!ok) return false;
  try {
    await api.excluir(`${rec.rota}/${registro[rec.chave]}`);
    avisar(rec.exclusao === "logica"
      ? `${maiuscula(rec.singular)} ${rec.nome(registro)} inativad${a}.`
      : `${maiuscula(rec.singular)} ${rec.nome(registro)} excluíd${a}.`);
    return true;
  } catch (erro) {
    avisar(mensagemDeErro(erro), "erro", 8000);
    return false;
  }
}

// ---------- Fornecedores do produto (dentro da edição do produto) ----------

function painelFornecedores(container, produto, fornecedores) {
  const rota = `/api/produtos/${produto.idProduto}/fornecedores`;
  container.innerHTML = `<section class="secao-janela" aria-labelledby="titulo-fornecedores">
      <div>
        <h3 id="titulo-fornecedores">Fornecedores e custos</h3>
        <p class="mudo" style="font-size:var(--t-2)">Quem fornece este produto e por quanto. O custo é sugerido nas entradas de estoque.</p>
      </div>
      <div class="aviso" data-tom="critico" hidden role="alert" data-erro-vinculo>${icone("erro")}<span></span></div>
      <div data-vinculos>${htmlCarregando()}</div>
      <form class="vinculo vinculo-novo" data-novo-vinculo novalidate>
        <div class="campo">
          <label for="novo-fornecedor">Fornecedor</label>
          <select id="novo-fornecedor" name="fornecedor" class="entrada"></select>
        </div>
        <div class="campo">
          <label for="novo-custo">Custo unitário</label>
          <div class="entrada-unidade"><input id="novo-custo" name="custoUnitario" class="entrada" inputmode="decimal" autocomplete="off" placeholder="0,00"><span class="unidade">R$</span></div>
        </div>
        <button class="btn" type="submit">${icone("ligacao")}Vincular</button>
      </form>
    </section>`;
  const caixa = container.querySelector("[data-vinculos]");
  const erro = container.querySelector("[data-erro-vinculo]");
  const formNovo = container.querySelector("[data-novo-vinculo]");
  const mostrarErro = (e) => {
    erro.querySelector("span").textContent = mensagemDeErro(e);
    erro.hidden = false;
  };

  const desenhar = async () => {
    erro.hidden = true;
    let vinculos;
    try {
      vinculos = await api.get(rota);
    } catch (e) {
      caixa.innerHTML = "";
      mostrarErro(e);
      return;
    }
    const vinculados = new Set(vinculos.map((v) => v.idFornecedor));
    const livres = ordenar(fornecedores.filter((f) => !vinculados.has(f.idFornecedor)), "nome");
    formNovo.querySelector("select").innerHTML = `<option value="">${livres.length ? "Escolha..." : "Todos já vinculados"}</option>`
      + livres.map((f) => `<option value="${f.idFornecedor}">${escapar(f.nome)}</option>`).join("");
    caixa.innerHTML = vinculos.length
      ? `<ul class="lista">${vinculos.map((v) => `
          <li class="vinculo" data-fornecedor="${v.idFornecedor}">
            <span class="lista-texto"><strong>${escapar(v.nomeFornecedor)}</strong><span>${escapar(v.cnpjFornecedor || "")}</span></span>
            <div class="entrada-unidade"><input class="entrada" aria-label="Custo de ${escapar(v.nomeFornecedor)}" inputmode="decimal" value="${escapar(String(v.custoUnitario).replace(".", ","))}"><span class="unidade">R$</span></div>
            <button class="btn btn-p" type="button" data-salvar>Salvar custo</button>
            <button class="btn btn-p btn-fantasma btn-icone" type="button" data-remover aria-label="Desvincular ${escapar(v.nomeFornecedor)}">${icone("lixo")}</button>
          </li>`).join("")}</ul>`
      : `<p class="mudo">Nenhum fornecedor vinculado ainda.</p>`;
  };

  caixa.addEventListener("click", async (e) => {
    const item = e.target.closest("[data-fornecedor]");
    if (!item) return;
    const idFornecedor = item.dataset.fornecedor;
    erro.hidden = true;
    try {
      if (e.target.closest("[data-salvar]")) {
        await api.atualizar(`${rota}/${idFornecedor}`, { custoUnitario: decimalParaApi(item.querySelector("input").value) });
        avisar("Custo atualizado.");
        await desenhar();
      } else if (e.target.closest("[data-remover]")) {
        await api.excluir(`${rota}/${idFornecedor}`);
        avisar("Fornecedor desvinculado.");
        await desenhar();
      }
    } catch (err) {
      mostrarErro(err);
    }
  });

  formNovo.addEventListener("submit", async (e) => {
    e.preventDefault();
    erro.hidden = true;
    try {
      await api.criar(rota, {
        fornecedor: formNovo.elements.fornecedor.value,
        custoUnitario: decimalParaApi(formNovo.elements.custoUnitario.value),
      });
      formNovo.reset();
      avisar("Fornecedor vinculado.");
      await desenhar();
    } catch (err) {
      mostrarErro(err);
    }
  });

  desenhar();
}
