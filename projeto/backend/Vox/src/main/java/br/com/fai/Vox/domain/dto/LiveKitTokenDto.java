package br.com.fai.Vox.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LiveKitTokenDto {

    private String token;

    public LiveKitTokenDto() {}

    public LiveKitTokenDto(String token) {
        this.token = token;
    }
}
