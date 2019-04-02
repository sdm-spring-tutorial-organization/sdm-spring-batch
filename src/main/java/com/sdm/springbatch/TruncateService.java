package com.sdm.springbatch;

import org.springframework.batch.core.ExitStatus;

public interface TruncateService {
    ExitStatus execute();
}