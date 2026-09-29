package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Municipality {
    private Integer id;
    private String name;
    private String state;

    public Municipality() {
    }

    public Municipality(Integer id, String name, String state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }

}
