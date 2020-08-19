package top.crossoverjie.cicada.db.core.handle;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import top.crossoverjie.cicada.db.core.SqlSession;
import top.crossoverjie.cicada.db.listener.DataChangeListener;
import top.crossoverjie.cicada.db.model.User;

@Slf4j
public class DBHandleImplTest {

    @Test
    public void update2(){
        SqlSession.init("root","root","jdbc:mysql://localhost:3306/ssm?charset=utf8mb4&useUnicode=true&characterEncoding=utf-8");
        User user = new User();
        user.setId(1);
        user.setName("abc");
        DBHandle handle = (DBHandle) new HandleProxy(DBHandle.class).getInstance(new DataChangeListener() {
            @Override
            public void listener(Object obj) {
                User user2 = (User) obj;
                log.info("call back " + user2.toString());
            }
        });
        int x = handle.update(user) ;
        System.out.println(x);
    }


    @Test
    public void insert(){
        SqlSession.init("root","root","jdbc:mysql://localhost:3306/ssm?charset=utf8mb4&useUnicode=true&characterEncoding=utf-8");
        User user = new User();
        user.setName("abc");
        user.setDescription("哈哈哈");
        DBHandle handle = (DBHandle) new HandleProxy(DBHandle.class).getInstance() ;
        handle.insert(user) ;
    }
}