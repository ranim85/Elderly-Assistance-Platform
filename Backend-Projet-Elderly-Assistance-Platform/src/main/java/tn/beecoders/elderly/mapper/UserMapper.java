package tn.beecoders.elderly.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.beecoders.elderly.domain.User;
import tn.beecoders.elderly.dto.UserDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    UserDTO toDto(User user);
}
