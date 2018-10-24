import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.smy.bc.fabric.core.mybatis.entity.TClrOrder;

import java.math.BigDecimal;

/**
 * <p>Title: Dto2Json</p>
 * <p>Description: </p>
 *
 * @author zhaofeng
 * @version 1.0
 * @date 18-9-28
 */
public class Dto2Json {

    public static void main(String[] args) {
        TClrOrder order = new TClrOrder();
        order.setTransAmt(new BigDecimal("123.45"));
        order.setBankCardNo("123456789");
        order.setCustNo("a123456789");
        order.setOrderId("O0123456");
        order.setSysCode("CTS");
        System.out.println(JSONUtil.toJsonStr(order));
        String str =
                "[{\"Key\":\"ClrOrderCTS0123456\", \"Record\":{\"bankCardNo\":\"123456789\",\"custNo\":\"a123456789\",\"orderId\":\"0123456\",\"sysCode\":\"CTS\",\"transAmt\":123.45}},{\"Key\":\"0123456CTS\", \"Record\":{\"bankCardNo\":\"123456789\",\"custNo\":\"a123456789\",\"orderId\":\"0123456\",\"sysCode\":\"CTS\",\"transAmt\":123.45}}]"
                ;
    }

}
