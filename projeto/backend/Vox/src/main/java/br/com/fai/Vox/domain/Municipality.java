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

//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getState() {
//        return state;
//    }
//
//    public void setState(String state) {
//        this.state = state;
//    }
}
