package com.my.stock.controller;

import com.my.stock.base.constants.ResponseCode;
import com.my.stock.base.response.BaseResponse;
import com.my.stock.service.QuartzCronService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quartz")
@RequiredArgsConstructor
public class QuartzAdminController {

    private final QuartzCronService quartzCronService;

    @PostMapping("/triggers/{name}/cron")
    public ResponseEntity<BaseResponse> updateCron(@PathVariable("name") String triggerName,
                                                   @RequestParam(value = "group", required = false) String group,
                                                   @RequestParam("cron") String cron) {
        quartzCronService.rescheduleCron(triggerName, group, cron);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse(ResponseCode.SUCCESS, "Rescheduled: " + (group == null?"DEFAULT":group) + "." + triggerName));
    }
}


