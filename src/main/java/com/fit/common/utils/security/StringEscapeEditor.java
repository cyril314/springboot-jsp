package com.fit.common.utils.security;

import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.JavaScriptUtils;

import java.beans.PropertyEditorSupport;

public class StringEscapeEditor extends PropertyEditorSupport {
    private boolean escapeHTML;
    private boolean escapeJavaScript;

    public StringEscapeEditor() {
    }

    public StringEscapeEditor(boolean escapeHTML, boolean escapeJavaScript) {
        this.escapeHTML = escapeHTML;
        this.escapeJavaScript = escapeJavaScript;
    }

    public String getAsText() {
        Object value = this.getValue();
        return value != null?value.toString():"";
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if(text == null) {
            this.setValue((Object)null);
        } else {
            String value = text;
            if(this.escapeHTML) {
                value = HtmlUtils.htmlEscape(text);
            }

            if(this.escapeJavaScript) {
                value = JavaScriptUtils.javaScriptEscape(value);
            }

            this.setValue(value);
        }

    }
}
