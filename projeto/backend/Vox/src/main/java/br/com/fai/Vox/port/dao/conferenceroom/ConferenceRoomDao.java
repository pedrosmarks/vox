package br.com.fai.Vox.port.dao.conferenceroom;

import br.com.fai.Vox.domain.ConferenceRoom;
import br.com.fai.Vox.domain.dto.CreateConferenceRoomDto;

import java.util.List;

public interface ConferenceRoomDao {

    int create(CreateConferenceRoomDto dto);

    void delete(int id);

    ConferenceRoom findById(int id);

    List<ConferenceRoom> findByMunicipalityId(int municipalityId);

    void updateStatus(int id, ConferenceRoom.RoomStatus status);
}
