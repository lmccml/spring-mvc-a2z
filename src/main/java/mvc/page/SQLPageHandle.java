package mvc.page;

/**
 * @author lmc
 * @date 2019/9/26
 */
public interface SQLPageHandle {
    /**
     * 将传入的SQL做分页处理
     *
     * @param oldSql   原SQL
     * @param pageNo   当前页
     * @param pageSize 每页数量
     */
    String handlerPagingSQL(String oldSql, int pageNo, int pageSize);
}
