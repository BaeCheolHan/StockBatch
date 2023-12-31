package com.my.stock.base.response;

import com.my.stock.base.constants.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseResponse {
    private ResponseCode code;
    private String message;
}
