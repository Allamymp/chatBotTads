package com.example.chatbottads.util;

import com.example.chatbottads.dto.ReturnSectorDTO;
import com.example.chatbottads.model.Sector;

public class DTOFactory {

    public static ReturnSectorDTO fromEntity(Sector sector) {
        if (sector == null) {
            return null;
        }
        String encryptedId = EncryptionUtil.encrypt(sector.getId());
        return new ReturnSectorDTO(
                encryptedId,
                sector.getName(),
                sector.getDescription()
        );
    }

    public static Sector toEntity(ReturnSectorDTO dto) {
        if (dto == null) {
            return null;
        }
        Long decryptedId = EncryptionUtil.decrypt(dto.id());
        Sector sector = new Sector();
        sector.setId(decryptedId);
        sector.setName(dto.name());
        sector.setDescription(dto.description());
        return sector;
    }
}
