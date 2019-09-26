package mvc.page;

/**
 * @author lmc
 * @date 2019/9/26
 */
public class MysqlSQLPageHandleImpl implements SQLPageHandle {
    public String handlerPagingSQL(String oldSQL, int pageNo, int pageSize) {
        StringBuffer sql = new StringBuffer(oldSQL);
        if (pageSize > 0) {
            int firstResult = (pageNo - 1) * pageSize;
            if (firstResult <= 0) {
                sql.append(" limit ").append(pageSize);
            } else {
                sql.append(" limit ").append(firstResult).append(",")
                        .append(pageSize);
            }
        }
        return sql.toString();
    }
}
