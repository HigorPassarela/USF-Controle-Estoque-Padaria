// Painel: indicadores do estoque, gráfico disponível x vencido, próximos vencimentos e últimas movimentações.
import { api } from "../api.js";
import { icone, htmlCabecalho, htmlCarregando, htmlErro } from "../ui.js";
import { escapar, quantidade, moeda, data, dataHora, diasAte, textoDias } from "../formatos.js";
import { graficoBarras, htmlLegenda } from "../grafico.js";
import { abrirEntrada, abrirSaida } from "./estoque.js";

const SERIES = [
  { nome: "Disponível", cor: "var(--serie-disponivel)" },
  { nome: "Vencido", cor: "var(--serie-vencido)" },
];
const MAX_PRODUTOS_GRAFICO = 12;
const MAX_ITENS_LISTA = 8;

function indicador({ rotulo, icone: nomeIcone, valor, nota, tom, href }) {
  const tag = href ? "a" : "div";
  return `<${tag} class="indicador" ${href ? `href="${href}"` : ""} ${tom ? `data-tom="${tom}"` : ""}>
      <span class="indicador-rotulo">${icone(nomeIcone)}${escapar(rotulo)}</span>
      <span class="indicador-valor">${valor}</span>
      <span class="indicador-nota">${nota}</span>
    </${tag}>`;
}

export async function renderPainel(conteudo, { definirTopo, vigente }) {
  definirTopo("Painel", "Visão geral");
  conteudo.innerHTML = `
    ${htmlCabecalho({
      titulo: "Visão geral",
      texto: "Como está o estoque agora: o que acabou, o que venceu e o que vence nos próximos dias.",
      acoes: `<button class="btn" type="button" data-acao="saida">${icone("saida")}Registrar saída</button>
              <button class="btn btn-primario" type="button" data-acao="entrada">${icone("entrada")}Registrar entrada</button>`,
    })}
    <div id="painel-corpo" class="pilha">${htmlCarregando()}</div>`;
  conteudo.onclick = (e) => {
    const acao = e.target.closest("[data-acao]")?.dataset.acao;
    if (acao === "entrada") abrirEntrada();
    if (acao === "saida") abrirSaida();
  };

  const corpo = conteudo.querySelector("#painel-corpo");
  let saldos, produtos, lotes, movimentacoes;
  try {
    [saldos, produtos, lotes, movimentacoes] = await Promise.all([
      api.get("/api/estoque/saldos"),
      api.get("/api/produtos"),
      api.get("/api/estoque/lotes", { somenteComSaldo: "true" }),
      api.get("/api/estoque/movimentacoes"),
    ]);
  } catch (erro) {
    if (vigente()) corpo.innerHTML = htmlErro(erro);
    return;
  }
  if (!vigente()) return;

  if (!produtos.length) {
    corpo.innerHTML = `
      <div class="indicadores">
        ${indicador({ rotulo: "Produtos ativos", icone: "produto", valor: "0", nota: "nenhum cadastrado" })}
        ${indicador({ rotulo: "Sem estoque", icone: "saldo", valor: "0", nota: "produtos zerados" })}
        ${indicador({ rotulo: "Com saldo vencido", icone: "alerta", valor: "0", nota: "produtos" })}
        ${indicador({ rotulo: "Valor estimado", icone: "dinheiro", valor: moeda(0), nota: "pelo preço de venda" })}
      </div>
      <section class="quadro">
        <div class="estado">${icone("produto")}
          <h3>Comece cadastrando seus produtos</h3>
          <p>Antes do primeiro produto, cadastre pelo menos uma categoria, um tipo de produto e uma unidade de medida. Depois é só registrar a primeira entrada de estoque.</p>
          <div class="cabecalho-acoes" style="justify-content:center">
            <a class="btn" href="#/cadastros/categorias-produto">Categorias</a>
            <a class="btn" href="#/cadastros/unidades-medida">Unidades de medida</a>
            <a class="btn btn-primario" href="#/cadastros/produtos">${icone("mais")}Cadastrar produto</a>
          </div>
        </div>
      </section>`;
    return;
  }

  const preco = Object.fromEntries(produtos.map((p) => [p.idProduto, Number(p.precoVenda)]));
  const zerados = saldos.filter((s) => Number(s.saldoTotal) === 0);
  const comVencido = saldos.filter((s) => Number(s.saldoVencido) > 0);
  const saldoVencidoTotal = comVencido.length;
  const vencendo = lotes
    .filter((l) => l.dataValidade && !l.vencido)
    .map((l) => ({ ...l, dias: diasAte(l.dataValidade) }))
    .sort((a, b) => a.dias - b.dias || a.idLote - b.idLote);
  const vencendo7 = vencendo.filter((l) => l.dias <= 7);
  const valorEstimado = saldos.reduce((s, x) => s + Number(x.saldoDisponivel) * (preco[x.idProduto] || 0), 0);

  const paraGrafico = saldos
    .filter((s) => Number(s.saldoTotal) > 0)
    .sort((a, b) => Number(b.saldoTotal) - Number(a.saldoTotal))
    .slice(0, MAX_PRODUTOS_GRAFICO);
  const unidadesDiferentes = new Set(paraGrafico.map((s) => s.siglaUnidadeMedida)).size > 1;

  corpo.innerHTML = `
    <div class="indicadores">
      ${indicador({ rotulo: "Produtos ativos", icone: "produto", valor: produtos.length, nota: `${saldos.length - zerados.length} com saldo`, href: "#/estoque/saldos" })}
      ${indicador({ rotulo: "Sem estoque", icone: "saldo", valor: zerados.length, nota: zerados.length ? "produtos zerados" : "nenhum produto zerado", tom: zerados.length ? "alerta" : "", href: "#/estoque/saldos?situacao=zerado" })}
      ${indicador({ rotulo: "Com saldo vencido", icone: "alerta", valor: saldoVencidoTotal, nota: saldoVencidoTotal ? "produtos com lote vencido" : "nada vencido", tom: saldoVencidoTotal ? "critico" : "", href: "#/estoque/lotes?vencidos=1" })}
      ${indicador({ rotulo: "Vencem em 7 dias", icone: "relogio", valor: vencendo7.length, nota: vencendo7.length === 1 ? "lote" : "lotes", tom: vencendo7.length ? "alerta" : "", href: "#/estoque/lotes?vencendo=1" })}
      ${indicador({ rotulo: "Valor estimado", icone: "dinheiro", valor: moeda(valorEstimado), nota: "saldo disponível × preço de venda" })}
    </div>

    <section class="quadro">
      <div class="quadro-cabecalho">
        <div>
          <h2>Saldo por produto</h2>
          <span class="mudo">${paraGrafico.length < saldos.filter((s) => Number(s.saldoTotal) > 0).length ? `Os ${MAX_PRODUTOS_GRAFICO} maiores saldos. ` : ""}${unidadesDiferentes ? "Cada barra está na unidade do próprio produto." : ""}</span>
        </div>
        ${htmlLegenda(SERIES)}
      </div>
      <div class="quadro-corpo">
        ${paraGrafico.length ? '<div id="grafico-saldos"></div>' : '<p class="mudo">Nenhum produto com saldo ainda. Registre uma entrada para ver o gráfico.</p>'}
        <p style="margin-top:12px"><a href="#/estoque/saldos">Ver todos os saldos em tabela</a></p>
      </div>
    </section>

    <div class="grade-2">
      <section class="quadro">
        <div class="quadro-cabecalho"><h2>Próximos vencimentos</h2><a href="#/estoque/lotes">Ver lotes</a></div>
        <div class="quadro-corpo">
          ${vencendo.length ? `<ul class="lista">${vencendo.slice(0, MAX_ITENS_LISTA).map((l) => `
            <li>
              <span class="lista-texto"><strong>${escapar(l.nomeProduto)}</strong><span>Lote #${l.idLote}${l.numeroLote ? ` · ${escapar(l.numeroLote)}` : ""} · ${data(l.dataValidade)}</span></span>
              <span class="lista-valor">
                <strong>${quantidade(l.quantidadeAtual, l.siglaUnidadeMedida)}</strong>
                <span class="situacao" data-s="${l.dias <= 3 ? "vencido" : l.dias <= 7 ? "baixo" : "zerado"}">${textoDias(l.dias)}</span>
              </span>
            </li>`).join("")}</ul>` : '<p class="mudo">Nenhum lote com validade e saldo.</p>'}
        </div>
      </section>

      <section class="quadro">
        <div class="quadro-cabecalho"><h2>Últimas movimentações</h2><a href="#/estoque/movimentacoes">Ver histórico</a></div>
        <div class="quadro-corpo">
          ${movimentacoes.length ? `<ul class="lista">${movimentacoes.slice(0, MAX_ITENS_LISTA).map((m) => {
            const entrada = m.natureza === "ENTRADA";
            const sigla = produtos.find((p) => p.idProduto === m.idProduto)?.nomeUnidadeMedida || "";
            return `<li>
              <span class="lista-texto"><strong>${escapar(m.nomeProduto)}</strong><span>${escapar(m.descricaoTipoMovimentacao)} · ${dataHora(m.dataMovimentacao)}</span></span>
              <span class="lista-valor"><strong class="${entrada ? "positivo" : "negativo"}">${entrada ? "+" : "−"}${quantidade(m.quantidade, sigla)}</strong><span class="mudo" style="font-size:var(--t-1)">lote #${m.idLote}</span></span>
            </li>`;
          }).join("")}</ul>` : '<p class="mudo">Nenhuma movimentação registrada.</p>'}
        </div>
      </section>
    </div>`;

  const alvoGrafico = corpo.querySelector("#grafico-saldos");
  if (alvoGrafico) {
    graficoBarras(alvoGrafico, {
      itens: paraGrafico.map((s) => ({ rotulo: s.nomeProduto, valores: [s.saldoDisponivel, s.saldoVencido], sigla: s.siglaUnidadeMedida })),
      series: SERIES,
      formatar: (v, sigla) => quantidade(v, unidadesDiferentes ? sigla : sigla || paraGrafico[0]?.siglaUnidadeMedida),
      descricao: `Saldo disponível e vencido de ${paraGrafico.length} produtos`,
    });
  }
}
