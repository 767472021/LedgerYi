package cn.ledgeryi.chainbase.core.capsule;

import cn.ledgeryi.common.core.Constant;
import cn.ledgeryi.protos.Protocol;
import cn.ledgeryi.protos.contract.SmartContractOuterClass;
import cn.ledgeryi.protos.contract.SmartContractOuterClass.SmartContract;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Slf4j(topic = "capsule")
public class ContractCapsule implements ProtoCapsule<SmartContract> {

    private SmartContract smartContract;

    /**
     * constructor TransactionCapsule.
     */
    public ContractCapsule(SmartContract smartContract) {
        this.smartContract = smartContract;
    }

    public ContractCapsule(byte[] data) {
        try {
            this.smartContract = SmartContract.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            log.debug(e.getMessage());
        }
    }

    public static SmartContractOuterClass.CreateSmartContract getSmartContractFromTransaction(Protocol.Transaction trx) {
        try {
            Any any = trx.getRawData().getContract().getParameter();
            SmartContractOuterClass.CreateSmartContract createSmartContract = any.unpack(SmartContractOuterClass.CreateSmartContract.class);
            return createSmartContract;
        } catch (InvalidProtocolBufferException e) {
            return null;
        }
    }

    public static SmartContractOuterClass.TriggerSmartContract getTriggerContractFromTransaction(Protocol.Transaction trx) {
        try {
            Any any = trx.getRawData().getContract().getParameter();
            SmartContractOuterClass.TriggerSmartContract contractTriggerContract = any.unpack(SmartContractOuterClass.TriggerSmartContract.class);
            return contractTriggerContract;
        } catch (InvalidProtocolBufferException e) {
            return null;
        }
    }

    public byte[] getCodeHash() {
        return this.smartContract.getCodeHash().toByteArray();
    }

    public void setCodeHash(byte[] codeHash) {
        this.smartContract = this.smartContract.toBuilder().setCodeHash(ByteString.copyFrom(codeHash)).build();
    }

    @Override
    public byte[] getData() {
        return this.smartContract.toByteArray();
    }

    @Override
    public SmartContract getInstance() {
        return this.smartContract;
    }

    @Override
    public String toString() {
        return this.smartContract.toString();
    }

    public byte[] getOriginAddress() {
        return this.smartContract.getOriginAddress().toByteArray();
    }

    public long getConsumeUserResourcePercent() {
        long percent = this.smartContract.getConsumeUserResourcePercent();
        return max(0, min(percent, Constant.ONE_HUNDRED));
    }


    public void clearABI() {
        this.smartContract = this.smartContract.toBuilder().setAbi(SmartContract.ABI.getDefaultInstance()).build();
    }

    public byte[] getTrxHash() {
        return this.smartContract.getTrxHash().toByteArray();
    }

    public long getOriginEnergyLimit() {
        long originEnergyLimit = this.smartContract.getOriginEnergyLimit();
        if (originEnergyLimit == Constant.PB_DEFAULT_ENERGY_LIMIT) {
            originEnergyLimit = Constant.CREATOR_DEFAULT_ENERGY_LIMIT;
        }
        return originEnergyLimit;
    }
}
