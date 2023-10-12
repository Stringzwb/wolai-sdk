package com.zwb.model.Block;

import com.zwb.model.base.CodeSetting;
import com.zwb.model.base.richtext.CreateRichText;
import lombok.Data;

@Data
public class Code {
    private String type; // 代码块类型
    private String language; // 代码语言
    private CodeSetting codeSetting; // 代码设置
    private String caption; // 代码块说明
    private CreateRichText content; // 代码内容

}