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

package org.apache.inlong.manager.service.node.paimon;

import org.apache.inlong.manager.common.consts.DataNodeType;
import org.apache.inlong.manager.common.enums.ErrorCodeEnum;
import org.apache.inlong.manager.common.exceptions.BusinessException;
import org.apache.inlong.manager.common.util.CommonBeanUtils;
import org.apache.inlong.manager.common.util.Preconditions;
import org.apache.inlong.manager.dao.entity.DataNodeEntity;
import org.apache.inlong.manager.pojo.node.DataNodeInfo;
import org.apache.inlong.manager.pojo.node.DataNodeRequest;
import org.apache.inlong.manager.pojo.node.paimon.PaimonDataNodeDTO;
import org.apache.inlong.manager.pojo.node.paimon.PaimonDataNodeInfo;
import org.apache.inlong.manager.pojo.node.paimon.PaimonDataNodeRequest;
import org.apache.inlong.manager.service.node.AbstractDataNodeOperator;
import org.apache.inlong.manager.service.resource.sink.paimon.PaimonCatalogClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaimonDataNodeOperator extends AbstractDataNodeOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaimonDataNodeOperator.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Boolean accept(String dataNodeType) {
        return getDataNodeType().equals(dataNodeType);
    }

    @Override
    public String getDataNodeType() {
        return DataNodeType.PAIMON;
    }

    @Override
    public DataNodeInfo getFromEntity(DataNodeEntity entity) {
        if (entity == null) {
            throw new BusinessException(ErrorCodeEnum.DATA_NODE_NOT_FOUND);
        }

        PaimonDataNodeInfo paimonDataNodeInfo = new PaimonDataNodeInfo();
        CommonBeanUtils.copyProperties(entity, paimonDataNodeInfo);
        if (StringUtils.isNotBlank(entity.getExtParams())) {
            PaimonDataNodeDTO dto = PaimonDataNodeDTO.getFromJson(entity.getExtParams());
            CommonBeanUtils.copyProperties(dto, paimonDataNodeInfo);
        }
        return paimonDataNodeInfo;
    }

    @Override
    protected void setTargetEntity(DataNodeRequest request, DataNodeEntity targetEntity) {
        PaimonDataNodeRequest paimonNodeRequest = (PaimonDataNodeRequest) request;
        CommonBeanUtils.copyProperties(paimonNodeRequest, targetEntity, true);
        try {
            PaimonDataNodeDTO dto = PaimonDataNodeDTO.getFromRequest(paimonNodeRequest, targetEntity.getExtParams());
            targetEntity.setExtParams(objectMapper.writeValueAsString(dto));
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnum.SOURCE_INFO_INCORRECT,
                    String.format("Failed to build extParams for Paimon node: %s", e.getMessage()));
        }
    }

    @Override
    public Boolean testConnection(DataNodeRequest request) {
        PaimonDataNodeRequest paimonRequest = (PaimonDataNodeRequest) request;
        String metastoreUri = paimonRequest.getUrl();
        String warehouse = paimonRequest.getWarehouse();
        Preconditions.expectNotBlank(metastoreUri, ErrorCodeEnum.INVALID_PARAMETER, "connection url cannot be empty");
        try (PaimonCatalogClient client = new PaimonCatalogClient(metastoreUri, warehouse)) {
            client.open();
            client.listAllDatabases();
            LOGGER.info("paimon connection not null - connection success for metastoreUri={}, warehouse={}",
                    metastoreUri, warehouse);
            return true;
        } catch (Exception e) {
            String errMsg = String.format("paimon connection failed for metastoreUri=%s, warehouse=%s", metastoreUri,
                    warehouse);
            LOGGER.error(errMsg, e);
            throw new BusinessException(errMsg);
        }
    }

}
