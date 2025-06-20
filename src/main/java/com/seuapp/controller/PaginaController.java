package com.seuapp.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seuapp.model.Documento;

@Controller
public class PaginaController {

    private static final Logger logger = LoggerFactory.getLogger(PaginaController.class);

    private final Map<String, Documento> documentos = new HashMap<>();
    private final Map<String, Integer> termosBuscados = new HashMap<>();

    @PostConstruct
    public void carregarDocumentos() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = new ClassPathResource("documentos.json").getInputStream();
            List<Map<String, String>> lista = mapper.readValue(is, new TypeReference<List<Map<String, String>>>() {});
            for (Map<String, String> item : lista) {
                documentos.put(item.get("chave"), new Documento(item.get("titulo"), item.get("descricao")));
            }
        } catch (IOException e) {
            logger.error("Erro ao carregar documentos", e);
        }
    }

    @GetMapping("/")
    public String home(Model model) {
        return "index";
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam(required = false) String termo, Model model) {
        List<Documento> resultados = new ArrayList<>();
        List<Documento> sugestoes = new ArrayList<>();

        if (termo != null && !termo.isEmpty()) {
            String termoLower = termo.toLowerCase();

            // Conta a busca
            termosBuscados.put(termoLower, termosBuscados.getOrDefault(termoLower, 0) + 1);

            // Busca nos documentos
            for (Map.Entry<String, Documento> entry : documentos.entrySet()) {
                Documento doc = entry.getValue();
                boolean contemNaChave = entry.getKey().toLowerCase().contains(termoLower);
                boolean contemNoTitulo = doc.getTitulo().toLowerCase().contains(termoLower);
                boolean contemNaDescricao = doc.getDescricao().toLowerCase().contains(termoLower);

                if (contemNaChave || contemNoTitulo || contemNaDescricao) {
                    resultados.add(doc);
                }
            }

            // Recomendação com base nas buscas mais frequentes (exclui o termo atual)
            termosBuscados.entrySet().stream()
                    .filter(e -> !e.getKey().equalsIgnoreCase(termoLower))
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(4)
                    .forEach(entry -> {
                        Documento sugestao = documentos.get(entry.getKey());
                        if (sugestao != null) {
                            sugestoes.add(sugestao);
                        }
                    });

            model.addAttribute("sugestoes", sugestoes);
        }

        model.addAttribute("resultados", resultados);
        model.addAttribute("resultadoTexto", resultados.size() + " resultado(s) encontrado(s)");

        // Tags mais buscadas
        List<String> tagsMaisBuscadas = termosBuscados.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(8)
                .map(Map.Entry::getKey)
                .toList();

        model.addAttribute("tagsBuscadas", tagsMaisBuscadas);

        return "buscar";
    }
}
