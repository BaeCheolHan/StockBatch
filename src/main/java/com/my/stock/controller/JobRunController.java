package com.my.stock.controller;

import com.my.stock.base.constants.ResponseCode;
import com.my.stock.base.response.BaseResponse;
import com.my.stock.service.JobRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor
public class JobRunController {
	private final JobRunService jobRunService;

	@GetMapping("/run/{jobName}")
	public ResponseEntity<BaseResponse> runJob(@PathVariable String jobName, @RequestParam Map<String, Object> parameters) {
		JobParametersBuilder jpb = new JobParametersBuilder();
		jpb.addString("JobID", String.valueOf(System.currentTimeMillis()));
		parameters.forEach((key, value) -> jpb.addString(key, value.toString()));

		jobRunService.runjob(jobName, jpb.toJobParameters());
		return ResponseEntity.status(HttpStatus.OK)
				.body(new BaseResponse(ResponseCode.SUCCESS, ResponseCode.SUCCESS.getMessage()));

	}
}
