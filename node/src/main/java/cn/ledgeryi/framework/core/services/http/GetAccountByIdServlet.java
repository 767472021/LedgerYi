package cn.ledgeryi.framework.core.services.http;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import cn.ledgeryi.common.utils.ByteArray;
import cn.ledgeryi.framework.core.Wallet;
import cn.ledgeryi.protos.Protocol.Account;


@Component
@Slf4j(topic = "API")
public class GetAccountByIdServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  private String convertOutput(Account account) {
    // convert asset id
    if (account.getAssetIssuedID().isEmpty()) {
      return JsonFormat.printToString(account, false);
    } else {
      JSONObject accountJson = JSONObject.parseObject(JsonFormat.printToString(account, false));
      String assetId = accountJson.get("asset_issued_ID").toString();
      accountJson.put(
          "asset_issued_ID", ByteString.copyFrom(ByteArray.fromHexString(assetId)).toStringUtf8());
      return accountJson.toJSONString();
    }
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      boolean visible = Util.getVisible(request);
      String accountId = request.getParameter("account_id");
      Account.Builder build = Account.newBuilder();
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("account_id", accountId);
      JsonFormat.merge(jsonObject.toJSONString(), build, visible);

      Account reply = wallet.getAccountById(build.build());
      if (reply != null) {
        if (visible) {
          response.getWriter().println(JsonFormat.printToString(reply, true));
        } else {
          response.getWriter().println(convertOutput(reply));
        }
      } else {
        response.getWriter().println("{}");
      }
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String account = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(account);
      boolean visible = Util.getVisiblePost(account);
      Account.Builder build = Account.newBuilder();
      JsonFormat.merge(account, build, visible);

      Account reply = wallet.getAccountById(build.build());
      if (reply != null) {
        if (visible) {
          response.getWriter().println(JsonFormat.printToString(reply, true));
        } else {
          response.getWriter().println(convertOutput(reply));
        }
      } else {
        response.getWriter().println("{}");
      }
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }
}