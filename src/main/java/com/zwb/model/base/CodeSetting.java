package com.zwb.model.base;

import lombok.Data;

@Data
public class CodeSetting {
    private Boolean lineNumber; // 启用行号
    private Boolean lineBreak; // 启用自动换行
    private Boolean ligatures; // 启用连字符
    private String previewFormat; // 预览格式

}
