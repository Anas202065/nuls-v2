/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.model.dto;


import io.nuls.base.basic.AddressTool;
import io.nuls.contract.model.bo.ContractInternalCreate;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

import java.util.Arrays;

/**
 * @author: PierreLuo
 */
@ApiModel
public class ContractInternalCreateDto {
    @ApiModelProperty(description = "交易创建者地址")
    private String sender;
    @ApiModelProperty(description = "创建的合约地址")
    private String contractAddress;
    @ApiModelProperty(description = "内部创建所依据的合约")
    private String codeCopyBy;
    @ApiModelProperty(description = "参数列表")
    private String args;

    public ContractInternalCreateDto(ContractInternalCreate internalCreate) {
        this.sender = AddressTool.getStringAddressByBytes(internalCreate.getSender());
        this.contractAddress = AddressTool.getStringAddressByBytes(internalCreate.getContractAddress());
        this.codeCopyBy = AddressTool.getStringAddressByBytes(internalCreate.getCodeCopyBy());
        this.args = internalCreate.getArgs() == null ? null : Arrays.deepToString(internalCreate.getArgs());
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getCodeCopyBy() {
        return codeCopyBy;
    }

    public void setCodeCopyBy(String codeCopyBy) {
        this.codeCopyBy = codeCopyBy;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }
}
