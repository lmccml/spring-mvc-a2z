package mvc.dao;

import mvc.page.Page;
import mvc.page.SQLPageHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lmc
 * @date 2019/9/26
 */
@Repository
public class BaseDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Resource
    protected SQLPageHandle sQLPageHandle;

    /**
     * 获取新增对象的主键Id
     *
     * @param sql
     * @param params
     * @return
     */

    public Long getSaveObjId(final String sql, final Object[] params) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(new PreparedStatementCreator() {

                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                    PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    for (int index = 0; index < params.length; index++) {
                        ps.setObject(index + 1, params[index]);
                    }
                    return ps;
                }
            }, keyHolder);
            return keyHolder.getKey().longValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public <T> List<T> findList(Class<T> tClass, final String sql) {

        return findList(tClass, sql, null);
    }

    /**
     * 返回指定class的集合
     *
     * @param tClass
     * @param sql
     * @param params
     * @param <T>
     * @return
     */

    public <T> List<T> findList(Class<T> tClass, final String sql, Object... params) {
        List<T> resultList = new ArrayList<>();
        try {
            if (params != null && params.length > 0)
                resultList = jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<T>(tClass));
            else
                resultList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<T>(tClass));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }



    public List<Map<String, Object>> findList(final String sql) {

        return findList(sql, null);
    }

    /**
     * 返回Map键值对的集合
     *
     * @param sql
     * @param params
     * @return
     */

    public List<Map<String, Object>> findList(final String sql, Object... params) {
        try {
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            return null;
        }
    }



    public <T> T findFirst(Class<T> tClass, final String sql) {
        return findFirst(tClass, sql, null);
    }

    /**
     * 返回指定的class对象
     *
     * @param tClass
     * @param sql
     * @param params
     * @param <T>
     * @return
     */

    public <T> T findFirst(Class<T> tClass, final String sql, Object... params) {
        try {
            List<T> list = findList(tClass, sql, params);
            return CollectionUtils.isEmpty(list) ? null : list.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    public Map<String, Object> findFirst(final String sql) {
        return findFirst(sql, null);
    }

    /**
     * 返回Map键值对
     *
     * @param sql
     * @param params
     * @return
     */

    public Map<String, Object> findFirst(final String sql, Object... params) {
        try {
            List<Map<String, Object>> list = findList(sql, params);
            return CollectionUtils.isEmpty(list) ? new HashMap<>() : list.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    public int execute(final String sql) {
        return execute(sql, null);
    }

    /**
     * 执行sql，如果成功，返回影响记录数，如果失败，返回0 ，如果有异常，返回-1
     *
     * @param sql
     * @param params
     * @return
     */

    public int execute(final String sql, final Object... params) {
        int num = 0;
        try {
            if (params == null || params.length == 0) {
                num = jdbcTemplate.update(sql);
            } else {
                num = jdbcTemplate.update(sql, new PreparedStatementSetter() {
                    public void setValues(PreparedStatement ps) throws SQLException {
                        for (int i = 0; i < params.length; i++)
                            ps.setObject(i + 1, params[i]);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return num;
    }



    public int[] batchExecute(final String sql) {
        return batchExecute(sql, null);
    }

    /**
     * 批量执行sql，返回影响的行数
     *
     * @param sql
     * @param objs
     * @return
     */

    public int[] batchExecute(final String sql, List<Object[]> objs) {
        return jdbcTemplate.batchUpdate(sql, objs);
    }



    public <T> Page<T> queryPagination(Class<T> tClass, int pageNo, int pageSize, final String sql) {
        return queryPagination(tClass, pageNo, pageSize, sql, null);
    }

    /**
     * 分页查询对象
     *
     * @param sql
     * @param tClass
     * @param pageNo
     * @param pageSize
     * @param params
     * @param <T>
     * @return
     */

    public <T> Page<T> queryPagination(Class<T> tClass, int pageNo, int pageSize, final String sql, Object... params) {
        try {
            // 将SQL语句进行分页处理
            String newSql = sQLPageHandle.handlerPagingSQL(sql, pageNo, pageSize);
            List<T> list, totalList;
            if (params == null || params.length <= 0) {
                totalList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<T>(tClass));
                list = jdbcTemplate.query(newSql, new BeanPropertyRowMapper<T>(tClass));
            } else {
                totalList = jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<T>(tClass));
                list = jdbcTemplate.query(newSql, params, new BeanPropertyRowMapper<T>(tClass));
            }
            // 根据参数的个数进行差别查询
            Page<T> page = new Page<T>(pageNo, pageSize, totalList.isEmpty() ? 0 : totalList.size(), list);
            return page;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 分页查询对象
     *
     * @param sql
     * @param pageNo
     * @param pageSize
     * @param params
     * @return
     */

    public Page<Map<String, Object>> queryPagination(int pageNo, int pageSize, final String sql, Object... params) {
        try {
            // 将SQL语句进行分页处理
            String newSql = sQLPageHandle.handlerPagingSQL(sql, pageNo, pageSize);
            List<Map<String, Object>> list, totalList;
            if (params == null || params.length <= 0) {
                totalList = jdbcTemplate.queryForList(sql);
                list = jdbcTemplate.queryForList(newSql);
            } else {
                totalList = jdbcTemplate.queryForList(sql, params);
                list = jdbcTemplate.queryForList(newSql, params);
            }
            // 根据参数的个数进行差别查询
            Page<Map<String, Object>> page = new Page<>(pageNo, pageSize, totalList.isEmpty() ? 0 : totalList.size(), list);
            return page;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
