package br.edu.atitus.currencyservice.clients;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BCBResponse {

    private List<BCBValue> value;

    public List<BCBValue> getValue() {
        return value;
    }

    public void setValue(List<BCBValue> value) {
        this.value = value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BCBValue {

        private Double cotacaoVenda;

        public Double getCotacaoVenda() {
            return cotacaoVenda;
        }

        public void setCotacaoVenda(Double cotacaoVenda) {
            this.cotacaoVenda = cotacaoVenda;
        }
    }
}
