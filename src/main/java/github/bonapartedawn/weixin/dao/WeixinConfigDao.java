package github.bonapartedawn.weixin.dao;

import github.bonapartedawn.common.utils.JsonUtil;
import github.bonapartedawn.common.utils.ObjectUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Fuzhong.Yan on 17/11/7.
 */
public class WeixinConfigDao extends Dao{
    public WeixinConfigDao(DataSource dataSource) {
        super(dataSource);
    }
    public Map<String,Object> config(){
        JdbcTemplate jdbcTemplate = jdbcTemplate();
        Map<String,Object> res = new HashMap<>();
        if (ObjectUtils.isNotEmpty(jdbcTemplate)){
            List<Map<String, Object>> temp = jdbcTemplate.queryForList("SELECT * FROM sys_config_data where code='WEIXIN_APP_CONFIG'");
            if (ObjectUtils.isNotEmpty(temp)){
                String json = MapUtils.getString(temp.get(0),"data");
                Map tMap = JsonUtil.toObject(json, Map.class);
                res.putAll(tMap);
            }
        }
        return res;
    }
}
