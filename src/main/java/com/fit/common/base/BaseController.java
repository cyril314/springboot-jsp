package com.fit.common.base;

import com.fit.common.utils.DateUtil;
import com.fit.common.utils.StringUtil;
import com.fit.common.utils.security.Encodes;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import javax.servlet.http.HttpServletRequest;
import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BaseController {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
            public void setAsText(String text) {
                this.setValue(text == null ? null : Encodes.escapeHtml(text.trim()));
            }

            public String getAsText() {
                Object value = this.getValue();
                return value != null ? value.toString() : "";
            }
        });
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            public void setAsText(String text) {
                this.setValue(DateUtil.parseDate(text));
            }
        });
        binder.registerCustomEditor(Timestamp.class, new PropertyEditorSupport() {
            public void setAsText(String text) {
                Date date = DateUtil.parseDate(text);
                this.setValue(date == null ? null : new Timestamp(date.getTime()));
            }
        });
    }

    public <T> Page<T> getPage(HttpServletRequest request) {
        int pageNo = 1;
        int pageSize = 20;
        String orderBy = "";
        String order = "asc";
        if (StringUtil.isNotEmpty(request.getParameter("page"))) {
            pageNo = Integer.valueOf(request.getParameter("page")).intValue();
        }

        if (StringUtil.isNotEmpty(request.getParameter("rows"))) {
            pageSize = Integer.valueOf(request.getParameter("rows")).intValue();
        }

        if (StringUtil.isNotEmpty(request.getParameter("sort"))) {
            orderBy = request.getParameter("sort").toString();
        }

        if (StringUtil.isNotEmpty(request.getParameter("order"))) {
            order = request.getParameter("order").toString();
        }

        return new Page(pageNo, pageSize, orderBy, order);
    }

    public <T> Map<String, Object> getEasyUIData(Page<T> page) {
        HashMap map = new HashMap();
        map.put("rows", page.getResult());
        map.put("total", Long.valueOf(page.getTotalCount()));
        map.put("columns", page.getColumns());
        map.put("primaryKey", page.getPrimaryKey());
        return map;
    }
}
