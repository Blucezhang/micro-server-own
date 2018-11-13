package com.own.owncommon.order;

import org.springframework.data.domain.PageRequest;

public class IFPageRequest {

    /**
     * 第几页
     */
    private Integer pageNo = 0;
    /**
     * 每页记录数
     */
    private Integer pageSize = 999;

    public PageRequest getPageRequest() {

        return new PageRequest(pageNo, pageSize, null);
    }

    public Integer getPageNo() {

        return pageNo;
    }

    public void setPageNo(Integer pageNo) {

        this.pageNo = pageNo;
    }

    public Integer getPageSize() {

        return pageSize;
    }

    public void setPageSize(Integer pageSize) {

        this.pageSize = pageSize;
    }

}
