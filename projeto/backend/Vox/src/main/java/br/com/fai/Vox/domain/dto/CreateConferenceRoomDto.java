package br.com.fai.Vox.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateConferenceRoomDto {

    @NotBlank(message = "Nome da sala é obrigatório")
    private String name;

    private String description;

    private Integer moderatorId;
    private Integer municipalityId;

    public CreateConferenceRoomDto() {}
}
