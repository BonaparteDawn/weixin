package github.bonapartedawn.weixin.dao;

import github.bonapartedawn.common.utils.ListUtils;
import github.bonapartedawn.common.utils.ObjectUtils;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;
import org.apache.commons.collections.MapUtils;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单DAO
 * Created by Fuzhong.Yan on 17/11/7.
 */
public class WeixinMenuDao extends Dao{
    public WeixinMenuDao(DataSource dataSource){
        super(dataSource);
    }

    /**
     * 提取菜单数据
     * @return
     */
    public List<WxMenuButton> queryWxMenuButtons(){
        JdbcTemplate jdbcTemplate = jdbcTemplate();
        List<WxMenuButton> res = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(jdbcTemplate)){
            List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM WEIXIN_MENU");
            if (ObjectUtils.isNotEmpty(list)){
                Map<Integer,List<Map<String, Object>>> container = new HashMap<>();
                for (Map<String, Object> t:list){
                    Integer p_id = MapUtils.getInteger(t,"pid",-1);
                    ListUtils.group(container,p_id).add(t);
                }
                List<Map<String, Object>> bs = container.get(-1);
                for (Map<String, Object> b:bs){
                    WxMenuButton button = new WxMenuButton();
                    Integer id = MapUtils.getInteger(b,"id");
                    BeanMap c = BeanMap.create(button);
                    c.putAll(b);
                    if (container.containsKey(id)){
                        List<WxMenuButton> child = new ArrayList<>();
                        List<Map<String, Object>> bs1 = container.get(id);
                        for (Map<String, Object> b1:bs1){
                            WxMenuButton button1 = new WxMenuButton();
                            BeanMap c1 = BeanMap.create(button1);
                            c1.putAll(b1);
                            child.add(button1);
                        }
                        button.setSubButtons(child);
                    }
                    res.add(button);
                }
            }
        }
        return res;
    }
}
