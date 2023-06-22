package com.api.bicicletario.service;

import com.api.bicicletario.exception.PagamentoNaoAutorizadoException;
import com.api.bicicletario.model.Cobranca;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class CobrancaServiceTest {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    String data = "31/12/2023";

    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private CobrancaService cobrancaService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testObterCobrancasAtrasadas() throws ParseException {
        // Criação de cobranças simuladas
        Cobranca cobrancaAtrasada = null;
        cobrancaAtrasada = new Cobranca(1, "Aguardando pagamento", LocalDateTime.now(), LocalDateTime.now().plusHours(1),50.0, 3, "1234566789");
        cobrancaAtrasada.setStatus("PENDENTE");
        cobrancaAtrasada.setHoraSolicitacao(LocalDateTime.now().minusHours(13));

        Cobranca cobrancaNaoAtrasada = null;
        cobrancaNaoAtrasada = new Cobranca(2, "Aguardando pagamento", LocalDateTime.now(), LocalDateTime.now().plusHours(1),50.0, 2, "1234566789");
        cobrancaNaoAtrasada.setStatus("PENDENTE");
        cobrancaNaoAtrasada.setHoraSolicitacao(LocalDateTime.now().minusHours(2));

        // Adição das cobranças à lista simulada
        List<Cobranca> cobrancas = new ArrayList<>();
        cobrancas.add(cobrancaAtrasada);
        cobrancas.add(cobrancaNaoAtrasada);
        cobrancaService.setCobrancas(cobrancas);

        // Chamada do método a ser testado
        List<Cobranca> cobrancasAtrasadas = cobrancaService.obterCobrancasAtrasadas();

        // Verificação do resultado
        assertEquals(1, cobrancasAtrasadas.size());
        assertEquals(cobrancaAtrasada, cobrancasAtrasadas.get(0));
    }

    @Test
    public void testRealizarCobranca_PagamentoAutorizado() throws PagamentoNaoAutorizadoException, ParseException {
        Cobranca cobranca = null;
        cobranca = new Cobranca(1, "Aguardando pagamento", LocalDateTime.now(), LocalDateTime.now().plusHours(1),50.0, 3, "1234566789");
        cobranca.setValor(10.0);
        cobranca.setCartao("1234566789");

        List<Cobranca> cobrancas = new ArrayList<>();
        cobrancas.add(cobranca);
        cobrancaService.setCobrancas(cobrancas);

        boolean resultado = cobrancaService.processarPagamento(cobranca.getValor(), cobranca.getCartao());

        assertTrue(resultado);
        assertEquals("Aguardando pagamento", cobranca.getStatus());
        assertNotNull(cobranca.getHoraFinalizacao());
    }

    @Test
    public void testEnviarNotificacao() throws ParseException {
        Cobranca cobranca = new Cobranca(1, "Aguardando pagamento", LocalDateTime.now(), LocalDateTime.now().plusHours(1),50.0, 3, "1234566789");
        cobranca.setCiclista(1);
        cobranca.setHoraSolicitacao(LocalDateTime.now());
        cobranca.setValor(50.0);

        cobrancaService.enviarNotificacao(cobranca);

        verify(notificacaoService, times(1)).enviarEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testProcessarPagamento() {
        double valor = 100.0;
        String cartao = "1234567890";

        assertTrue(cobrancaService.processarPagamento(valor, cartao));
    }
    @Test
    public void testCriarMensagemNotificacao() {
        // Cria uma cobrança para teste
        Cobranca cobranca = new Cobranca(1, "Aguardando pagamento", LocalDateTime.now(), LocalDateTime.now().plusHours(1),50.0, 3, "1234566789");
        cobranca.setCiclista(1);
        cobranca.setHoraSolicitacao(LocalDateTime.now());
        cobranca.setValor(100.0);

        // Chama o método para criar a mensagem de notificação
        String mensagem = cobrancaService.criarMensagemNotificacao(cobranca);

        // Verifica se a mensagem foi criada corretamente
        String mensagemEsperada = "Caro(a) Ciclista " + cobranca.getCiclista() + ",\n\n" +
                "De acordo com nossos registros, identificamos uma cobrança em atraso para a devolução da bicicleta.\n" +
                "Data da cobrança: " + cobranca.getHoraSolicitacao() + "\n" +
                "Valor da cobrança: " + cobranca.getValor() + "\n\n" +
                "Atenciosamente,\n" +
                "Equipe do sistema de aluguel de bicicletas";
        assertEquals(mensagemEsperada, mensagem);
    }
}
