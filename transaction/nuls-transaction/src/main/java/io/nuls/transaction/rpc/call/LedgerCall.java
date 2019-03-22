package io.nuls.transaction.rpc.call;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.log.Log;
import io.nuls.tools.model.BigIntegerUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.VerifyTxResult;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: qinyifeng
 * @date: 2018/12/20
 */
public class LedgerCall {

    /**
     * 验证CoinData
     * @param chain
     * @param txHex
     * @return
     */
    public static VerifyTxResult verifyCoinData(Chain chain, String txHex, boolean batch) {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("txHex", txHex);
            params.put("isBatchValidate", batch);
            HashMap result = (HashMap) TransactionCall.request(ModuleE.LG.abbr,"validateCoinData", params);
            return new VerifyTxResult((int)result.get("validateCode"), (String)result.get("validateDesc"));
        } catch (Exception e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            return new VerifyTxResult(VerifyTxResult.OTHER_EXCEPTION, "Call validateCoinData failed!");
        }
    }

    /**
     * 验证CoinData
     * @param chain
     * @param tx
     * @return
     */
    public static VerifyTxResult verifyCoinData(Chain chain, Transaction tx, boolean batch) throws NulsException {
        try {
            return verifyCoinData(chain, tx.hex(), batch);
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 验证区块中的交易CoinData
     * @param chain
     * @param txHexList
     * @param blockHeight
     * @return
     * @throws NulsException
     */
    public static boolean verifyBlockTxsCoinData(Chain chain, List<String> txHexList, Long blockHeight) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("txHexList", txHexList);
            params.put("blockHeight", blockHeight);
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("%%%%%%%%% 验证区块交易, %%%%%%%%%%%%");
            HashMap result = (HashMap)TransactionCall.request(ModuleE.LG.abbr, "blockValidate", params);
            return (int) result.get("value") == 1;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 查询nonce值
     *
     * @param chain
     * @param address
     * @param assetChainId
     * @param assetId
     * @return
     * @throws NulsException
     */
    public static byte[] getNonce(Chain chain, String address, int assetChainId, int assetId) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("address", address);
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            HashMap result = (HashMap) TransactionCall.request(ModuleE.LG.abbr, "getNonce", params);
            String nonce = (String) result.get("nonce");
            return HexUtil.decode(nonce);
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }


    /**
     * 查询账户特定资产的余额(包含未确认的余额)
     * Check the balance of an account-specific asset
     */
    public static BigInteger getBalanceNonce(Chain chain, byte[] address, int assetChainId, int assetId) throws NulsException {
        try {
            String addressString = AddressTool.getStringAddressByBytes(address);
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            params.put("address", addressString);
            Map result = (Map)TransactionCall.request(ModuleE.LG.abbr, "getBalanceNonce", params);
            Object available = result.get("available");
            return BigIntegerUtils.stringToBigInteger(String.valueOf(available));
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 查询账户特定资产的余额(只获取已确认的余额)
     * Check the balance of an account-specific asset
     */
    public static BigInteger getBalance(Chain chain, byte[] address, int assetChainId, int assetId) throws NulsException {
        try {
            String addressString = AddressTool.getStringAddressByBytes(address);
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            params.put("address", addressString);
            Map result = (Map)TransactionCall.request(ModuleE.LG.abbr, "getBalance", params);
            Object available = result.get("available");
            return BigIntegerUtils.stringToBigInteger(String.valueOf(available));
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 开始批量验证coindata的通知
     * @param chain
     * @return
     * @throws NulsException
     */
    public static boolean coinDataBatchNotify(Chain chain) {
        Long timeStartTest = System.currentTimeMillis();
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            HashMap result = (HashMap)TransactionCall.request(ModuleE.LG.abbr, "bathValidateBegin", params);
            Log.debug("##### 通知账本花费的时间:{}", System.currentTimeMillis() - timeStartTest);
            return (int) result.get("value") == 1;
        } catch (Exception e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            return false;
        }

    }

    /**
     * 提交未确认交易给账本
     * @param chain
     * @param txHex
     */
    public static boolean commitUnconfirmedTx(Chain chain, String txHex) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("txHex", txHex);
            HashMap result = (HashMap)TransactionCall.request(ModuleE.LG.abbr, "commitUnconfirmedTx", params);
            return (int) result.get("value") == 1;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 提交已确认交易给账本
     * @param chain
     * @param txHexList
     */
    public static boolean commitTxsLedger(Chain chain, List<String> txHexList, Long blockHeight) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("txHexList", txHexList);
            params.put("blockHeight", blockHeight);
            HashMap result = (HashMap)TransactionCall.request(ModuleE.LG.abbr, "commitBlockTxs", params);
            return (int) result.get("value") == 1;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }


    /**
     * 调用账本回滚未确认的交易
     * @param chain
     * @param txHex
     */
    public static boolean rollbackTxValidateStatus(Chain chain, String txHex) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("txHex", txHex);
            HashMap result = (HashMap)TransactionCall.request(ModuleE.LG.abbr, "rollbackTxValidateStatus", params);
            return (int) result.get("value") == 1;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 调用账本回滚未确认的交易
     * @param chain
     * @param txHex
     */
    public static boolean rollBackUnconfirmTx(Chain chain, String txHex) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("txHex", txHex);
            HashMap result = (HashMap)TransactionCall.request(ModuleE.LG.abbr, "rollBackUnconfirmTx", params);
            return (int) result.get("value") == 1;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 调用账本回滚已确认的交易
     * @param chain
     * @param txHexList
     */
    public static boolean rollbackTxsLedger(Chain chain, List<String> txHexList, Long blockHeight) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("txHexList", txHexList);
            params.put("blockHeight", blockHeight);
            HashMap result = (HashMap)TransactionCall.request(ModuleE.LG.abbr, "rollBackBlockTxs", params);
            return (int) result.get("value") == 1;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }



}
