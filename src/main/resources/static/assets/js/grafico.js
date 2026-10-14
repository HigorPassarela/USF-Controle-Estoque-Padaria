// Gráfico de barras horizontais empilhadas em SVG, sem biblioteca.
// Cada linha: nome do produto em cima, barra embaixo, total na ponta. Escala única para todas as linhas.
import { escapar } from "./formatos.js";

function passoBonito(bruto) {
  if (bruto <= 0) return 1;
  const potencia = 10 ** Math.floor(Math.log10(bruto));
  const n = bruto / potencia;
  const fator = n <= 1 ? 1 : n <= 2 ? 2 : n <= 2.5 ? 2.5 : n <= 5 ? 5 : 10;
  return fator * potencia;
}

const soma = (valores) => valores.reduce((s, v) => s + Math.max(0, Number(v) || 0), 0);

/**
 * itens: [{ rotulo, valores: [n, n], sigla }]
 * series: [{ nome, cor }] - cor é uma variável CSS, ex.: "var(--serie-disponivel)"
 * formatar(valor, sigla): texto do valor
 */
export function graficoBarras(container, { itens, series, formatar, descricao }) {
  container.classList.add("grafico");
  container.innerHTML = `<div class="dica" hidden></div>`;
  const dica = container.querySelector(".dica");
  let svgAtual = null;

  const desenhar = () => {
    const largura = Math.max(260, Math.floor(container.clientWidth));
    const margemDireita = 84;
    const topo = 24;
    const alturaLinha = 48;
    const altura = topo + itens.length * alturaLinha + 4;
    const maximo = Math.max(0, ...itens.map((i) => soma(i.valores)));
    const passo = passoBonito((maximo || 1) / 4);
    const limite = Math.max(passo, Math.ceil((maximo || 1) / passo) * passo);
    const util = largura - margemDireita;
    const x = (v) => (v / limite) * util;
    const caracteres = Math.max(12, Math.floor(largura / 7.2));

    let grade = "";
    for (let t = 0; t <= limite + passo / 1000; t += passo) {
      const px = x(t);
      const ancora = t === 0 ? "start" : "middle";
      grade += `<line class="linha-grade" x1="${px}" x2="${px}" y1="${topo - 6}" y2="${altura}"/>`
        + `<text x="${px}" y="12" text-anchor="${ancora}">${escapar(formatar(t))}</text>`;
    }

    const linhas = itens.map((item, i) => {
      const y = topo + i * alturaLinha;
      let acumulado = 0;
      const barras = item.valores.map((v, s) => {
        const valor = Math.max(0, Number(v) || 0);
        if (valor === 0) return "";
        const inicio = x(acumulado);
        acumulado += valor;
        // 2px de respiro entre segmentos empilhados
        const largo = Math.max(2, x(acumulado) - inicio - (acumulado < soma(item.valores) ? 2 : 0));
        return `<rect class="barra" x="${inicio}" y="${y + 22}" width="${largo}" height="14" rx="3" style="fill:${series[s].cor}"/>`;
      }).join("");
      const total = soma(item.valores);
      const nome = item.rotulo.length > caracteres ? `${item.rotulo.slice(0, caracteres - 1)}…` : item.rotulo;
      return `<g class="linha" data-i="${i}">
          <text class="rotulo-produto" x="0" y="${y + 14}">${escapar(nome)}</text>
          ${barras || `<rect x="0" y="${y + 22}" width="2" height="14" style="fill:var(--borda-forte)"/>`}
          <text class="rotulo-valor" x="${x(total) + 8}" y="${y + 33}">${escapar(formatar(total, item.sigla))}</text>
          <rect class="alvo" x="0" y="${y}" width="${largura}" height="${alturaLinha}" data-i="${i}"/>
        </g>`;
    }).join("");

    const svg = `<svg viewBox="0 0 ${largura} ${altura}" width="${largura}" height="${altura}" role="img" aria-label="${escapar(descricao || "Gráfico de barras")}">${grade}${linhas}</svg>`;
    if (svgAtual) svgAtual.remove();
    container.insertAdjacentHTML("afterbegin", svg);
    svgAtual = container.querySelector("svg");
  };

  const mostrarDica = (i, yLinha) => {
    const item = itens[i];
    dica.innerHTML = `<strong>${escapar(item.rotulo)}</strong>`
      + series.map((s, k) => `<div class="dica-linha"><span><i style="background:${s.cor}"></i>${escapar(s.nome)}</span><b>${escapar(formatar(item.valores[k] || 0, item.sigla))}</b></div>`).join("")
      + `<div class="dica-linha"><span>Total</span><b>${escapar(formatar(soma(item.valores), item.sigla))}</b></div>`;
    dica.hidden = false;
    const larguraDica = dica.offsetWidth;
    dica.style.left = `${Math.max(0, Math.min(container.clientWidth - larguraDica, container.clientWidth / 2 - larguraDica / 2))}px`;
    dica.style.top = `${yLinha + 52}px`;
    svgAtual.querySelectorAll(".linha").forEach((g) => g.classList.toggle("linha-ativa", g.dataset.i === String(i)));
  };
  const esconderDica = () => {
    dica.hidden = true;
    svgAtual?.querySelectorAll(".linha-ativa").forEach((g) => g.classList.remove("linha-ativa"));
  };

  // Mouse: dica ao passar por cima. Toque: dica ao tocar (pointerdown).
  const aoApontar = (e) => {
    const alvo = e.target.closest(".alvo");
    if (!alvo) return esconderDica();
    mostrarDica(Number(alvo.dataset.i), Number(alvo.getAttribute("y")) * (svgAtual.clientWidth / svgAtual.viewBox.baseVal.width || 1));
  };
  container.addEventListener("pointermove", aoApontar);
  container.addEventListener("pointerdown", aoApontar);
  container.addEventListener("pointerleave", (e) => { if (e.pointerType === "mouse") esconderDica(); });

  desenhar();
  let largura = container.clientWidth;
  const observador = new ResizeObserver(() => {
    if (Math.abs(container.clientWidth - largura) < 2) return;
    largura = container.clientWidth;
    esconderDica();
    desenhar();
  });
  observador.observe(container);
  return { redesenhar: desenhar };
}

/** Legenda HTML das séries */
export function htmlLegenda(series) {
  return `<div class="legenda">${series.map((s) => `<span><i style="background:${s.cor}"></i>${escapar(s.nome)}</span>`).join("")}</div>`;
}
