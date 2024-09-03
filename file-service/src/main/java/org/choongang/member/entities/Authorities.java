package org.choongang.member.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.choongang.member.constants.Authority;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Authorities {

    private Authority authority;
}