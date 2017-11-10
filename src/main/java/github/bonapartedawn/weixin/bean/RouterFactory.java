package github.bonapartedawn.weixin.bean;

import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpMessageInterceptor;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

import java.util.Map;

/**
 * Created by Fuzhong.Yan on 17/11/9.
 */
public class RouterFactory {
    public static WxMpMessageRouter router(WxMpService service){
        WxMpMessageRouter router = new WxMpMessageRouter(service);
        router.rule().async(false).msgType("text").interceptor(new WxMpMessageInterceptor() {
                    @Override
                    public boolean intercept(WxMpXmlMessage wxMpXmlMessage, Map<String, Object> map, WxMpService wxMpService, WxSessionManager wxSessionManager) throws WxErrorException {
                        return true;
                    }
                }).handler(new WxMpMessageHandler() {
            @Override
            public WxMpXmlOutMessage handle(WxMpXmlMessage wxMpXmlMessage, Map<String, Object> map, WxMpService wxMpService, WxSessionManager wxSessionManager) throws WxErrorException {
                return WxMpXmlOutMessage.TEXT()
                        .content("来自服务器的内容")
                        .fromUser(wxMpXmlMessage.getToUser())
                        .toUser(wxMpXmlMessage.getFromUser())
                        .build();
            }
        }).end();
        return router;
    }
}
