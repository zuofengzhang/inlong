/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.inlong.manager.pojo.sink.paimon;

import org.apache.inlong.manager.common.enums.ErrorCodeEnum;
import org.apache.inlong.manager.common.exceptions.BusinessException;
import org.apache.inlong.manager.common.util.JsonUtils;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Paimon column info
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaimonColumnInfo {

    @ApiModelProperty("Length of fixed type")
    private Integer length;

    @ApiModelProperty("Precision of decimal type")
    private Integer precision;

    @ApiModelProperty("Scale of decimal type")
    private Integer scale;

    @ApiModelProperty("Field partition strategy, including: None, Identity, Year, Month, Day, Hour, Bucket, Truncate")
    private String partitionStrategy;

    @ApiModelProperty("Bucket num param of bucket partition")
    private Integer bucketNum;

    @ApiModelProperty("Width param of truncate partition")
    private Integer width;

    // The following are passed from base field and need not be part of API for extra param
    private String name;
    private String type;
    private String desc;
    private boolean required;

    private boolean isPartition;

    /**
     * Get the extra param from the Json
     */
    public static PaimonColumnInfo getFromJson(String extParams) {
        if (StringUtils.isEmpty(extParams)) {
            return new PaimonColumnInfo();
        }
        try {
            return JsonUtils.parseObject(extParams, PaimonColumnInfo.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnum.SINK_INFO_INCORRECT.getMessage() + ": " + e.getMessage());
        }
    }
}
