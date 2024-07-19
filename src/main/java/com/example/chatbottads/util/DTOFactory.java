package com.example.chatbottads.util;

import com.example.chatbottads.dto.ReturnInformationDTO;
import com.example.chatbottads.model.Information;

public class DTOFactory {

    public static ReturnInformationDTO fromEntity(Information information) {
        if (information == null) {
            return null;
        }
        String encryptedId = EncryptionUtil.encrypt(information.getId());
        return new ReturnInformationDTO(
                encryptedId,
                information.getName(),
                information.getDescription()
        );
    }

    public static Information toEntity(ReturnInformationDTO dto) {
        if (dto == null) {
            return null;
        }
        Long decryptedId = EncryptionUtil.decrypt(dto.id());
        Information information = new Information();
        information.setId(decryptedId);
        information.setName(dto.name());
        information.setDescription(dto.description());
        return information;
    }
}
