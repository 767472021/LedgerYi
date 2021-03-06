package cn.ledgeryi.contract.utils;

import cn.ledgeryi.contract.core.AbstractActuator;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.Set;

@Slf4j(topic = "TransactionRegister")
public class TransactionRegister {

  public static void registerActuator() {
    Reflections reflections = new Reflections("cn.ledgeryi.contract.core");
    Set<Class<? extends AbstractActuator>> subTypes = reflections.getSubTypesOf(AbstractActuator.class);
    for (Class clazz : subTypes) {
      try {
        clazz.newInstance();
      } catch (Exception e) {
        log.error("{} contract contract register fail!", clazz, e);
      }
    }
  }
}
