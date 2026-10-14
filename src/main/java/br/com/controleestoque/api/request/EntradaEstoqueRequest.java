package br.com.controleestoque.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EntradaEstoqueRequest(
        @Schema(description = "Escolha o produto na lista (somente ativos)")
        @NotBlank(message = "O produto é obrigatório")
        String produto,

        @Schema(description = "Quantidade recebida, maior que zero e com até 3 casas decimais (ex.: 2.5 kg). Use ponto como separador decimal", example = "25")
        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        @Digits(integer = 7, fraction = 3, message = "A quantidade deve ter no máximo 7 dígitos inteiros e 3 casas decimais")
        BigDecimal quantidade,

        @Schema(description = "Escolha o tipo de movimentação na lista (somente tipos de ENTRADA)")
        @NotBlank(message = "O tipo de movimentação é obrigatório")
        String tipoMovimentacao,

        @Schema(description = "Data de validade do lote no formato AAAA-MM-DD (opcional; sem validade o lote nunca vence). Não pode ser anterior a hoje", example = "2026-12-31")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataValidade,

        @Schema(description = "Escolha o fornecedor na lista (opcional, somente ativos)")
        String fornecedor,

        @Schema(description = "Custo unitário de compra em reais (opcional). Se ficar em branco e o fornecedor estiver vinculado ao produto, usa o custo do vínculo", example = "4.90")
        @DecimalMin(value = "0.00", message = "O custo unitário não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "O custo unitário deve ter no máximo 8 dígitos inteiros e 2 casas decimais")
        BigDecimal custoUnitario,

        @Schema(description = "Número ou código do lote impresso na embalagem (opcional)", example = "L2310-A")
        @Size(max = 50, message = "O número do lote deve ter no máximo 50 caracteres")
        String numeroLote,

        @Schema(description = "Observação sobre a entrada (opcional)", example = "NF 1234")
        @Size(max = 255, message = "A observação deve ter no máximo 255 caracteres")
        String observacao
) {
}
