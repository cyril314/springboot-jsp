package com.fit.common.base;

import com.fit.common.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class Page<T> {

    public static final String ASC = "asc";
    public static final String DESC = "desc";
    protected int pageNo = 1;
    protected int pageSize = -1;
    protected String orderBy = null;
    protected String order = null;
    protected boolean autoCount = true;
    protected List<T> result = new ArrayList<T>();
    protected long totalCount = -1L;
    protected String columns;
    protected String primaryKey;
    protected String tableName;

    public Page() {
    }

    public Page(int pageSize) {
        this.pageSize = pageSize;
    }

    public Page(int pageNo, int pageSize, String orderBy, String order) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.orderBy = orderBy;
        this.order = order;
    }

    public String getColumns() {
        return this.columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public String getPrimaryKey() {
        return this.primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getPageNo() {
        return this.pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
        if (pageNo < 1) {
            this.pageNo = 1;
        }
    }

    public Page<T> pageNo(int thePageNo) {
        this.setPageNo(thePageNo);
        return this;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Page<T> pageSize(int thePageSize) {
        this.setPageSize(thePageSize);
        return this;
    }

    public int getFirst() {
        return (this.pageNo - 1) * this.pageSize + 1;
    }

    public String getOrderBy() {
        return this.orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public Page<T> orderBy(String theOrderBy) {
        this.setOrderBy(theOrderBy);
        return this;
    }

    public String getOrder() {
        return this.order;
    }

    public void setOrder(String order) {
        String lowcaseOrder = order.toLowerCase();
        String[] orders = lowcaseOrder.split(",");
        String[] var7 = orders;
        int var6 = orders.length;

        for (int var5 = 0; var5 < var6; ++var5) {
            String orderStr = var7[var5];
            if (!"desc".equals(orderStr) && !"asc".equals(orderStr)) {
                throw new IllegalArgumentException("排序方向" + orderStr + "不是合法值");
            }
        }

        this.order = lowcaseOrder;
    }

    public Page<T> order(String theOrder) {
        this.setOrder(theOrder);
        return this;
    }

    public boolean isOrderBySetted() {
        return StringUtil.isNotBlank(this.orderBy) && StringUtil.isNotBlank(this.order);
    }

    public boolean isAutoCount() {
        return this.autoCount;
    }

    public void setAutoCount(boolean autoCount) {
        this.autoCount = autoCount;
    }

    public Page<T> autoCount(boolean theAutoCount) {
        this.setAutoCount(theAutoCount);
        return this;
    }

    public List<T> getResult() {
        return this.result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public long getTotalCount() {
        return this.totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getTotalPages() {
        if (this.totalCount < 0L) {
            return -1L;
        } else {
            long count = this.totalCount / (long) this.pageSize;
            if (this.totalCount % (long) this.pageSize > 0L) {
                ++count;
            }

            return count;
        }
    }

    public boolean isHasNext() {
        return (long) (this.pageNo + 1) <= this.getTotalPages();
    }

    public int getNextPage() {
        return this.isHasNext() ? this.pageNo + 1 : this.pageNo;
    }

    public boolean isHasPre() {
        return this.pageNo - 1 >= 1;
    }

    public int getPrePage() {
        return this.isHasPre() ? this.pageNo - 1 : this.pageNo;
    }
}
