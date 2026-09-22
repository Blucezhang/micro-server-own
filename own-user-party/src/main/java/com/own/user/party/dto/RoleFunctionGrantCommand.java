package com.own.user.party.dto;

import java.util.List;
import lombok.Data;

@Data
public class RoleFunctionGrantCommand {
    private List<Long> functionIds;
}
