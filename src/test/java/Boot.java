import github.bonapartedawn.bddao.enums.Type;
import github.bonapartedawn.bddao.util.DatabaseUtil;
import github.bonapartedawn.weixin.imp.CreditServiceImp;
import me.chanjar.weixin.mp.bean.kefu.result.WxMpKfList;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Fuzhong.Yan on 17/11/7.
 */
public class Boot {
    private static CreditServiceImp imp = null;
    static {
        Map<String, String> config = new HashMap<>();
        config.put("driverClassName","com.mysql.jdbc.Driver");
        config.put("url","jdbc:mysql://localhost:3306/cms?useUnicode=true&characterEncoding=UTF8");
        config.put("username","root");
        config.put("password","12345abc");
        DataSource datasource = null;
        try {
            datasource = DatabaseUtil.buildDatasource(Type.MySql, config);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        imp = new CreditServiceImp(datasource);
    }
    public static void main(String[] args) throws Exception {
        WxMpKfList kf = imp.service().getKefuService().kfList();
        System.out.println(kf.getKfList().size());
    }
}