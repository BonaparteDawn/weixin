package github.bonapartedawn.weixin.dao;

import github.bonapartedawn.bddao.model.MyJdbcTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * Created by Fuzhong.Yan on 17/11/7.
 */
public abstract class Dao {
    private DataSource dataSource;
    public Dao(DataSource dataSource){
        this.dataSource = dataSource;
    }
    protected JdbcTemplate jdbcTemplate() {
        return new MyJdbcTemplate(dataSource);
    }
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate(){
        return new NamedParameterJdbcTemplate(jdbcTemplate());
    }
}
