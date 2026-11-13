package br.unicesumar.maridodealuguel.domain;

/**
 * Estados da ordem de serviço (RF05). A regra de transição fica no próprio enum:
 * cada constante sobrescreve podeTransitarPara() com os destinos que aceita.
 * Aberta -> Aceita | Cancelada; Aceita -> Em execução | Cancelada; Em execução -> Concluída.
 */
public enum StatusOrdem {

    ABERTA("Aberta") {
        @Override
        public boolean podeTransitarPara(StatusOrdem destino) {
            return destino == ACEITA || destino == CANCELADA;
        }
    },
    ACEITA("Aceita") {
        @Override
        public boolean podeTransitarPara(StatusOrdem destino) {
            return destino == EM_EXECUCAO || destino == CANCELADA;
        }
    },
    EM_EXECUCAO("Em execução") {
        @Override
        public boolean podeTransitarPara(StatusOrdem destino) {
            return destino == CONCLUIDA;
        }
    },
    CONCLUIDA("Concluída") {
        @Override
        public boolean podeTransitarPara(StatusOrdem destino) {
            return false;
        }
    },
    CANCELADA("Cancelada") {
        @Override
        public boolean podeTransitarPara(StatusOrdem destino) {
            return false;
        }
    };

    private final String descricao;

    StatusOrdem(String descricao) {
        this.descricao = descricao;
    }

    public abstract boolean podeTransitarPara(StatusOrdem destino);

    public String getDescricao() {
        return descricao;
    }
}
