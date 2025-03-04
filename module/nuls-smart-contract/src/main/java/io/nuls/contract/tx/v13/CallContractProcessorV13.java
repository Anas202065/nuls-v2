package io.nuls.contract.tx.v13;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.model.bo.BatchInfoV8;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractWrapperTransaction;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.processor.CallContractTxProcessor;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.validator.CallContractTxValidator;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("CallContractProcessorV13")
public class CallContractProcessorV13 implements TransactionProcessor {

    @Autowired
    private CallContractTxProcessor callContractTxProcessor;
    @Autowired
    private CallContractTxValidator callContractTxValidator;
    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ChainManager chainManager;

    @Override
    public int getType() {
        return TxType.CALL_CONTRACT;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        ChainManager.chainHandle(chainId);
        Map<String, Object> result = new HashMap<>();
        List<Transaction> errorList = new ArrayList<>();
        result.put("txList", errorList);
        String errorCode = null;
        CallContractTransaction callTx;
        for(Transaction tx : txs) {
            callTx = new CallContractTransaction();
            callTx.copyTx(tx);
            try {
                Result validate = callContractTxValidator.validateV13(chainId, callTx);
                if(validate.isFailed()) {
                    errorCode = validate.getErrorCode().getCode();
                    errorList.add(tx);
                }
            } catch (NulsException e) {
                Log.error(e);
                errorCode = e.getErrorCode().getCode();
                errorList.add(tx);
            }
        }
        result.put("errorCode", errorCode);
        return result;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader header) {
        try {
            BatchInfoV8 batchInfo = contractHelper.getChain(chainId).getBatchInfoV8();
            if (batchInfo != null) {
                Map<String, ContractResult> contractResultMap = batchInfo.getContractResultMap();
                ContractResult contractResult;
                ContractWrapperTransaction wrapperTx;
                String txHash;
                for (Transaction tx : txs) {
                    txHash = tx.getHash().toString();
                    contractResult = contractResultMap.get(txHash);
                    if (contractResult == null) {
                        Log.warn("empty contract result with txHash: {}, txType: {}", txHash, tx.getType());
                        continue;
                    }
                    wrapperTx = contractResult.getTx();
                    wrapperTx.setContractResult(contractResult);
                    callContractTxProcessor.onCommitV8(chainId, wrapperTx);
                }
            }

            return true;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        try {
            ChainManager.chainHandle(chainId);
            CallContractData call;
            for (Transaction tx : txs) {
                if (tx.getType() == TxType.CROSS_CHAIN) {
                    // add by pierre at 2019-12-01 处理type10交易的业务回滚, 需要协议升级 done
                    if(ProtocolGroupManager.getCurrentVersion(chainId) < ContractContext.UPDATE_VERSION_V250) {
                        continue;
                    }
                    call = ContractUtil.parseCrossChainTx(tx, chainManager);
                    if (call == null) {
                        continue;
                    }
                } else {
                    call = new CallContractData();
                    call.parse(tx.getTxData(), 0);
                }
                callContractTxProcessor.onRollbackV8(chainId, new ContractWrapperTransaction(tx, call));
            }
            return true;
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
    }
}
