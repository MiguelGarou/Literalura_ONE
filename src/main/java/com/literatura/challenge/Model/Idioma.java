package com.literatura.challenge.Model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Idioma(
        @JsonAlias("languaje") String idioma,
        @JsonAlias("codigo") String codigoIdioma
) {
}
