package tn.beecoders.elderly.mapper;

import org.mapstruct.Mapper;
import tn.beecoders.elderly.domain.User;
import tn.beecoders.elderly.dto.UserDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDto(User user);
}
