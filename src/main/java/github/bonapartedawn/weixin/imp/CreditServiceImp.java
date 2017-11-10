package github.bonapartedawn.weixin.imp;

import github.bonapartedawn.keys.util.Base64Util;
import github.bonapartedawn.keys.util.SecurityUtil;
import github.bonapartedawn.weixin.bean.RouterFactory;
import github.bonapartedawn.weixin.dao.WeixinConfigDao;
import github.bonapartedawn.weixin.dao.WeixinMenuDao;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 信用卡专属服务
 * Created by Fuzhong.Yan on 17/11/7.
 */
public class CreditServiceImp {
    /**
     * 菜单访问数据DAO
     */
    private WeixinMenuDao weixinMenuDao;
    /**
     * 配置DAO
     */
    private WeixinConfigDao weixinConfigDao;
    /**
     * 缓存
     */
    private Map<String,Object> cache = new HashedMap();

    /**
     * 初始化
     * @param
     */
    public CreditServiceImp(){
    }

    /**
     * 初始化
     * @param dataSource
     */
    public CreditServiceImp(DataSource dataSource){
        this.weixinMenuDao = new WeixinMenuDao(dataSource);
        this.weixinConfigDao = new WeixinConfigDao(dataSource);
    }

    /**
     * 获得配置数据
     * @return
     */
    public Map<String, Object> config() throws Exception {
        if (!cache.containsKey("config")){
            refresh();
        }
        return (Map<String, Object>) cache.get("config");
    }

    /**
     * 提取菜单数据
     * @return
     */
    public List<WxMenuButton> menuButtons() throws Exception {
        if (!cache.containsKey("menuButtons")){
            refresh();
        }
        return (List<WxMenuButton>) cache.get("menuButtons");
    }

    /**
     * 刷新
     */
    public void refresh() throws Exception {
        //=================加载数据库数据
        //刷新配置
        Map<String, Object> config = this.weixinConfigDao.config();
        cache.put("config",config);
        //菜单数据
        List<WxMenuButton> menuButtons = this.weixinMenuDao.queryWxMenuButtons();
        cache.put("menuButtons",menuButtons);
        //=================根据数据库的数据生成缓存实体
        //微信服务
        WxMpInMemoryConfigStorage configStorage = new WxMpInMemoryConfigStorage();
        configStorage.setAppId(MapUtils.getString(config,"APP_ID"));
        configStorage.setSecret(MapUtils.getString(config,"APP_SECRET"));
        configStorage.setToken(MapUtils.getString(config,"APP_TOKEN"));
        cache.put("WxMpInMemoryConfigStorage",configStorage);
        WxMpService service = new WxMpServiceImpl();
        service.setWxMpConfigStorage(configStorage);
        cache.put("WxMpService",service);
        //刷新菜单
        WxMenu t = new WxMenu();
        t.setButtons(menuButtons);
        service.getMenuService().menuCreate(t);
    }

    public WxMpInMemoryConfigStorage wxMpInMemoryConfigStorage() throws Exception {
        if (!cache.containsKey("WxMpInMemoryConfigStorage")){
            refresh();
        }
        return (WxMpInMemoryConfigStorage) cache.get("WxMpInMemoryConfigStorage");
    }


    /**
     * 微信服务
     * @return
     */
    public WxMpService service() throws Exception {
        if (!cache.containsKey("WxMpService")){
            refresh();
        }
        return (WxMpService) cache.get("WxMpService");
    }

    /**
     * 检查签名密钥
     * @param signStr
     * @param args
     * @return
     */
    public boolean checkSign(String signStr,String... args) throws Exception {
        boolean res = false;
        if (signStr!=null){
            res = signStr.equals(createSign(args));
        }
        return res;
    }
    /**
     * 生成签名密钥
     * @param args
     * @return
     */
    public String createSign(String... args) throws Exception {
        String res = null;
        if (args!=null){
            StringBuilder builder = new StringBuilder();
            for (String arg:args){
                builder.append(arg);
            }
            String key = MapUtils.getString(config(),"SIGN_KEY");
            res = SecurityUtil.MAC().encrypt(builder.toString(), Base64Util.encode(key.getBytes()));
        }
        return res;
    }

    public void message(HttpServletRequest request, HttpServletResponse response) throws Exception{
        try {
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            Map<String, Object> args = new HashMap();
            Enumeration<String> names = request.getParameterNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                args.put(name, request.getParameter(name));
            }
            InputStream is = request.getInputStream();
            String inMessage = IOUtils.toString(is,"UTF-8");
            is.close();
            String outMessage = message(args,inMessage);
            if (outMessage != null){
                response.getWriter().print(outMessage);
                response.getWriter().close();
            }
        }catch (Exception e){
            throw e;
        }
    }

    /**
     * 腾讯服务器与当前服务器的消息通信接口
     * @param args 参数
     * @param message 传入的消息
     * @return 输出消息[如果为null,代表消息被异步处理]
     * @throws IOException
     */
    public String message(Map<String, Object> args, String message) throws IOException {
        try {
            String signature = MapUtils.getString(args,"signature");
            String nonce = MapUtils.getString(args,"nonce");
            String timestamp = MapUtils.getString(args,"timestamp");
            if (!service().checkSignature(timestamp, nonce, signature)) {
                // 消息签名不正确，说明不是公众平台发过来的消息
                return "非法请求";
            }
            String echostr = MapUtils.getString(args,"echostr");
            if (StringUtils.isNotBlank(echostr)) {
                // 说明是一个仅仅用来验证的请求，回显echostr
                return echostr;
            }
            String encrypt_type =  MapUtils.getString(args,"encrypt_type");
            String encryptType = StringUtils.isBlank(encrypt_type) ?"raw":encrypt_type;
            WxMpMessageRouter wxMpMessageRouter = RouterFactory.router(service());
            if ("raw".equals(encryptType)) {
                // 明文传输的消息
                WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(message);
                WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);
                if (outMessage!=null){
                    return outMessage.toXml();
                }else {
                    return null;
                }
            }
            if ("aes".equals(encryptType)) {
                // 是aes加密的消息
                String msgSignature =  MapUtils.getString(args,"msg_signature");
                WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(message, wxMpInMemoryConfigStorage(), timestamp, nonce, msgSignature);
                WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);
                if (outMessage!=null){
                    return outMessage.toEncryptedXml(wxMpInMemoryConfigStorage());
                }else {
                    return null;
                }
            }
            return "不可识别的加密类型";
        }catch (Exception e){
            return e.getMessage();
        }
    }
}