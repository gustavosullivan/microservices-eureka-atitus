package br.edu.atitus.currencyservice.controllers;

import br.edu.atitus.currencyservice.clients.BCBClient;
import br.edu.atitus.currencyservice.clients.BCBResponse;
import br.edu.atitus.currencyservice.dtos.CurrencyDTO;
import br.edu.atitus.currencyservice.entities.CurrencyEntity;
import br.edu.atitus.currencyservice.repositories.CurrencyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("currency")
public class CurrencyController {

    private static final String DATA_FIXA_COTACAO = "05-15-2024";
    private static final String CACHE_BCB = "bcb-currency";

    @Value("${server.port}")
    private String port;

    @Value("${convert.sleep:0}")
    private int sleep;

    private final CurrencyRepository repository;
    private final BCBClient bcbClient;
    private final CacheManager cacheManager;

    public CurrencyController(CurrencyRepository repository, BCBClient bcbClient, CacheManager cacheManager) {
        this.repository = repository;
        this.bcbClient = bcbClient;
        this.cacheManager = cacheManager;
    }

    @GetMapping("/convert")
    public ResponseEntity<CurrencyDTO> getConvert(
            @RequestParam String source,
            @RequestParam String target) throws Exception {

        Thread.sleep(sleep);

        source = source.toUpperCase();
        target = target.toUpperCase();

        CurrencyEntity currency = repository.findBySourceCurrencyAndTargetCurrency(source, target)
                .orElseThrow(() -> new Exception("Currency not found"));

        Double conversionRate = currency.getConversionRate();
        String environment = "Currency-service running on port: " + port;

        String moedaBcb = "BRL".equals(target) ? source : target;
        if (!"BRL".equals(moedaBcb)) {
            Double cotacaoBcb = cacheManager.getCache(CACHE_BCB).get(moedaBcb, Double.class);
            if (cotacaoBcb != null) {
                conversionRate = cotacaoBcb;
                environment = environment + " - BCB in cache";
            } else {
                BCBResponse response = bcbClient.getCotacaoBcb(moedaBcb, DATA_FIXA_COTACAO);
                if (response != null && response.getValue() != null && !response.getValue().isEmpty()) {
                    cotacaoBcb = response.getValue().get(0).getCotacaoVenda();
                    conversionRate = cotacaoBcb;
                    cacheManager.getCache(CACHE_BCB).put(moedaBcb, cotacaoBcb);
                    environment = environment + " - BCB";
                } else {
                    environment = environment + " - BCB Fallback";
                }
            }
        }

        CurrencyDTO dto = new CurrencyDTO(
                currency.getSourceCurrency(),
                currency.getTargetCurrency(),
                conversionRate,
                environment);

        return ResponseEntity.ok(dto);
    }
}
