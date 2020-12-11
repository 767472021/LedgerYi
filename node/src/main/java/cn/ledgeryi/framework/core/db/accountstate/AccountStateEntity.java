package cn.ledgeryi.framework.core.db.accountstate;

import cn.ledgeryi.chainbase.common.utils.WalletUtil;
import cn.ledgeryi.protos.Protocol.Account;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "AccountState")
public class AccountStateEntity {

  private Account account;

  public AccountStateEntity() {
  }

  public AccountStateEntity(Account account) {
    Account.Builder builder = Account.newBuilder();
    builder.setAddress(account.getAddress());
    builder.setAllowance(account.getAllowance());
    this.account = builder.build();
  }

  public static AccountStateEntity parse(byte[] data) {
    try {
      return new AccountStateEntity().setAccount(Account.parseFrom(data));
    } catch (Exception e) {
      log.error("parse to AccountStateEntity error! reason: {}", e.getMessage());
    }
    return null;
  }

  public Account getAccount() {
    return account;
  }

  public AccountStateEntity setAccount(Account account) {
    this.account = account;
    return this;
  }

  public byte[] toByteArrays() {
    return account.toByteArray();
  }

  @Override
  public String toString() {
    return "address:" + WalletUtil.encode58Check(account.getAddress().toByteArray()) + "; " + account
        .toString();
  }
}