package io.nuls.crosschain.nuls.srorage.imp;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.srorage.CommitedCtxService;
import io.nuls.db.model.Entry;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.List;
/**
 * 已打包的跨链交易相关操作
 * Packaged Cross-Chain Transaction Database Related Operations
 *
 * @author  tag
 * 2019/4/16
 * */
@Service
public class CommitedCtxServiceImpl implements CommitedCtxService {
    @Override
    public boolean save(NulsDigestData atxHash, Transaction ctx, int chainID) {
        try {
            if(atxHash == null || ctx == null){
                return false;
            }
            return RocksDBService.put(NulsCrossChainConstant.DB_NAME_COMMITED_CTX+chainID,atxHash.serialize(),ctx.serialize());
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public Transaction get(NulsDigestData atxHash, int chainID) {
        try {
            if(atxHash == null){
                return null;
            }
            byte[] txBytes = RocksDBService.get(NulsCrossChainConstant.DB_NAME_COMMITED_CTX+chainID,atxHash.serialize());
            if(txBytes == null){
                return null;
            }
            Transaction tx = new Transaction();
            tx.parse(txBytes,0);
            return tx;
        }catch (Exception e){
            Log.error(e);
        }
        return null;
    }

    @Override
    public boolean delete(NulsDigestData atxHash, int chainID) {
        try {
            if(atxHash == null){
                return false;
            }
            return RocksDBService.delete(NulsCrossChainConstant.DB_NAME_COMMITED_CTX+chainID,atxHash.serialize());
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public List<Transaction> getList(int chainID){
        try {
            List<Entry<byte[], byte[]>> list = RocksDBService.entryList(NulsCrossChainConstant.DB_NAME_COMMITED_CTX+chainID);
            List<Transaction> txList = new ArrayList<>();
            for (Entry<byte[], byte[]> entry:list) {
                Transaction tx = new Transaction();
                tx.parse(entry.getValue(),0);
                txList.add(tx);
            }
            return txList;
        }catch (Exception e){
            Log.error(e);
        }
        return null;
    }
}
