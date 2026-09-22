package com.own.face.util.base;

import com.own.face.util.Resp;
import com.own.face.trade.TradeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Common, structured exception responses for controllers which extend it.
 */
@Slf4j
public class BaseController {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Resp handleInvalidRequest(IllegalArgumentException exception) {
        return Resp.error(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
    }

    @ExceptionHandler(TradeException.class)
    public ResponseEntity<Resp> handleTradeError(TradeException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(new Resp(exception.getData(), exception.getStatus(), exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Resp handleUnexpectedError(Exception exception) {
        log.error("Unhandled controller error", exception);
        return Resp.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "internal server error");
    }
}
